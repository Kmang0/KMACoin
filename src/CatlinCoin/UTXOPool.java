package CatlinCoin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A UTXOPool organizes a collection of UTXOs.
 */

public class UTXOPool {

    private Map<String, UTXO> utxoPool;
    private Currency currency;

    public UTXOPool(Currency currency) {
        this.currency = currency;
        utxoPool = new HashMap<>();
    }

    /** Determines if the UTXO Pool contains a UTXO with the given information.
     *
     * @param transactionHash The Hash of the Transaction
     * @param coinIndex The index of the Transaction Output within the Transaction
     *
     * @return Returns true if the UTXO Pool contains the described UTXO, and false otherwise
     */

    public boolean containsUTXO(Hash transactionHash, byte coinIndex) {
        return utxoPool.containsKey(utxoKey(transactionHash, coinIndex));
    }

    /** Removes all UTXOs from the pool. */

    public void clear() {
        utxoPool.clear();
    }

    /** Returns the number of UTXOs in the pool. */

    public int size() {
        return utxoPool.size();
    }

    /** Returns a collection of all of the UTXOs in the pool. */

    public Collection<UTXO> getAllUTXOs() {
        return utxoPool.values();
    }

    /** Updates the UTXO Pool to reflect the result of every Transaction in the given Block. */

    public void recordUTXOs(Block block) {
        for (Hash transactionHash : block.getTransactionHashes()) {
            recordUTXOs(block.getCurrency().getBlockChain().getTransaction(transactionHash));
        }
    }

    /** Updates the UTXO Pool to reflect the result of the given Transaction. */

    public void recordUTXOs(Transaction transaction) {
        for (Transaction.TxInput txInput : transaction.getInputs()) {
            removeUTXO(txInput.getSourceTxHash(), txInput.getSourceCoinIndex());
        }

        for (Transaction.TxOutput txOutput : transaction.getOutputs()) {
            addUTXO(transaction.getHash(), txOutput.getCoinIndex());
        }
    }

    /** Updates the UTXO Pool to remove the results of every Transaction in the given Block. */

    public void unRecordUTXOs(Block block) {
        for (int i = block.getTransactionHashes().length - 1; i >= 0; i--) {
            Hash transactionHash = block.getTransactionHashes()[i];
            unRecordUTXOs(block.getCurrency().getBlockChain().getTransaction(transactionHash));
        }
    }

    /** Updates the UTXO Pool to remove the results of the given Transaction. */


    public void unRecordUTXOs(Transaction transaction) {
        for (Transaction.TxOutput txOutput : transaction.getOutputs()) {
            removeUTXO(transaction.getHash(), txOutput.getCoinIndex());
        }

        for (Transaction.TxInput txInput : transaction.getInputs()) {
            addUTXO(txInput.getSourceTxHash(), txInput.getSourceCoinIndex());
        }
    }



    private void addUTXO(Hash transactionHash, byte coinIndex) {
        Transaction transaction = currency.getBlockChain().getTransaction(transactionHash);
        utxoPool.put(utxoKey(transactionHash, coinIndex), new UTXO(transaction, coinIndex));
    }

    private void removeUTXO(Hash transactionHash, byte coinIndex) {
        UTXO oldUTXO = utxoPool.remove(utxoKey(transactionHash, coinIndex));
        if (oldUTXO == null) {
            System.out.println("****** failed attempt to remove UTXO " + transactionHash + " index " + coinIndex);
        }
    }

    /** Generates a String to use has the hash table key when making a hash table of UTXOs. */

    private String utxoKey(Hash transactionHash, byte coinIndex) {
        return transactionHash.getHex() + coinIndex;  // just concatenate them to make a key for the HashSet
    }


}
