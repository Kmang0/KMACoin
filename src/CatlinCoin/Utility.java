package CatlinCoin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A place for miscellaneous static utility functions.
 */

public class Utility {

    private Utility() { } // all methods are static, so no need to make objects of this class

    /*******************************************************************************************************
     *  Displays all of the legal transactions for a currency, in arbitrary order.
     *
     * @param currency the currency to use
     */

    static void displayAllTransactions(Currency currency) {
        System.out.println("============================================================================");
        System.out.println("================ All Transactions ==========================================");
        Map<Hash, Transaction> transactions = Network.downloadAllTransactions(currency);

        for (Transaction tx : transactions.values()) {
            tx.display();
            // or tx.displaySummary();
        }
        System.out.println("============================================================================");
    }

    /*******************************************************************************************************
     *  Displays all of the legal blocks for a currency, in arbitrary order.
     *
     * @param currency the currency to use
     */

    static void displayAllBlocks(Currency currency) {
        System.out.println("============================================================================");
        System.out.println("================ All Blocks ================================================");
        Map<Hash, Block> blocks = Network.downloadAllBlocks(currency);

        for (Block block : blocks.values()) {
            block.display();
        }
        System.out.println("============================================================================");
    }

    /*******************************************************************************************************
     * Creates and upoloads a transaction that pays money from one user to another.  This functions finds the
     * needed UTXOs and pays any leftover change back to the source.
     *
     * @param currency          the currency to use
     * @param sourceNickname    the nickname for the sender of the money
     * @param destAddressTag    the tag for the address of the receiver of the money
     * @param paymentAmount     the amount of money to pay to the receiver
     * @param fee               the fee paid to the miner for including this transaction in their block
     */

    public static void payment(Currency currency, String sourceNickname, String destAddressTag, long paymentAmount, long fee) {

        // TODO 25: Write the payment function
// TODO 25: Write the payment function
        BlockChain chain = currency.getBlockChain();
        Address sourceAddress = CryptoRSA.getPublicKeyAddress(sourceNickname);
        Address destAddress = AddressDirectory.getAddress(destAddressTag);

        long requiredTotal = paymentAmount + fee;
        long collected = 0;

        ArrayList<UTXO> selectedUTXOs = new ArrayList<>();

        for (UTXO utxo : chain.utxoPool.getAllUTXOs()) {
            if (utxo.getDestinationAddress().equals(sourceAddress)) {
                selectedUTXOs.add(utxo);
                collected += utxo.getAmount();
                if (collected >= requiredTotal) break;
            }
        }

        if (collected < requiredTotal) {
            System.out.println("Not enough funds: required " + requiredTotal + ", available " + collected);
            return;
        }

        Transaction tx = new Transaction(currency, sourceNickname, selectedUTXOs.size(), (collected > requiredTotal) ? 2 : 1, fee);

        for (UTXO utxo : selectedUTXOs) {
            tx.addInput(utxo.getTransactionHash(), utxo.getCoinIndex(), utxo.getAmount());
        }

        tx.addOutput(destAddress, paymentAmount);

        if (collected > requiredTotal) {
            long change = collected - requiredTotal;
            tx.addOutput(sourceAddress, change);
        }

        boolean uploaded = tx.upload();
        if (!uploaded) {
            System.out.println("Transaction upload failed.");
        }
    }

}
