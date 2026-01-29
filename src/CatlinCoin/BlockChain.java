package CatlinCoin;

import java.util.*;

/**
 * A BlockChain organizes transactions and blocks and provides access to various data structures.
 */

public class BlockChain {

    private final Currency currency;

    private Map<Hash, Transaction> transactions;
    private Map<Hash, Block> blocks;

    private LinkedList<Block> blockchain;

    public UTXOPool utxoPool;
    public HashSet<Hash> unConfirmedTransactions;

    private Map<Hash, LinkedList<Block>> nextBlocks;
    private Block highestBlock;

    private Map<Address, Balance> balances;   // public key hash hex -> Balance

    /////////////////////////////////////////////////////////////////////////////////////////

    public BlockChain(Currency currency) {
        if (currency.getBlockChain() != null) {
            throw new IllegalStateException("Currency " + currency.getName() + " already has a BlockChain object, do not constuct a new one");
        }
        this.currency = currency;
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    /** Returns the Transaction with the given Hash. */

    public Transaction getTransaction(Hash hash) {
        return transactions.get(hash);
    }

    /** Returns the Block with the given Hash. */

    public Block getBlock(Hash hash) {
        return blocks.get(hash);
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    /** Determines if the blockchain contains a UTXO with the given information.
     *
     * @param transactionHash The Hash of the Transaction
     * @param coinIndex The index of the Transaction Output within the Transaction
     *
     * @return Returns true if the blockchain contains the described UTXO, and false otherwise
     */
    public boolean containsUTXO(Hash transactionHash, byte coinIndex) {
        return utxoPool.containsUTXO(transactionHash, coinIndex);
    }

    /////////////////////////////////////////////////////////////////////////////////

    /** Builds (or rebuilds) the blockchain.  */

    public void build() {
        transactions = Network.downloadAllTransactions(currency);
        blocks = Network.downloadAllBlocks(currency);

        blockchain = new LinkedList<>();
        nextBlocks = new HashMap<>();
        utxoPool = new UTXOPool(currency);

        // fill out the nextBlock map
        for (Block block : blocks.values()) {
            Hash prevBlockHash = block.getPreviousBlockHash();
            if (!nextBlocks.containsKey(prevBlockHash))
                nextBlocks.put(prevBlockHash, new LinkedList<Block>());
            nextBlocks.get(prevBlockHash).add(block);
        }

        // recursively visit and validate all blocks in the main block tree
        visit(getBlock(currency.getGenesisBlockHash()));

        // identify the highest block on the main block tree
        highestBlock = null;
        int highestHeight = -1;
        int numValidBlocks = 0;
        for (Block block : blocks.values()) {
            if (block.getStatus().isValid() ) {
                numValidBlocks++;
                if (block.getHeight() > highestHeight) {
                    highestHeight = block.getHeight();
                    highestBlock = block;
                }
            }
        }

        // collect the blocks on the active (longest) block chain into the blockchain list
        Hash currentHash = highestBlock.getHash();
        while (!currentHash.equals(currency.getGenesisBlockHash())) {
            Block currentBlock = blocks.get(currentHash);
            blockchain.addFirst(currentBlock);
            currentHash = currentBlock.getPreviousBlockHash();
        }
        blockchain.addFirst(blocks.get(currentHash)); // don't forget to add the genesis block itself

        // fill out the UTXO pool
        utxoPool.clear();  // ought to be empty already!
        for (Block block : blockchain) {
            utxoPool.recordUTXOs(block);
        }

        // find all of the confirmed transactions
        HashSet<Hash> confirmedTransactions = new HashSet<>();
        for (Block block : blockchain) {
            for (Hash transactionHash : block.getTransactionHashes()) {
                confirmedTransactions.add(transactionHash);
            }
        }

        // find all of the unconfirmed non-coinbase transactions
        unConfirmedTransactions = new HashSet<>();
        for (Transaction transaction : transactions.values()) {
            if (!transaction.isCoinbase()) {
                if (!confirmedTransactions.contains(transaction.getHash())) {
                    unConfirmedTransactions.add(transaction.getHash());
                }
            }
        }

        computeBalances();

        System.out.println("-- BlockChain build -----------------------------------------------------");
        System.out.println("     num valid blocks = " + numValidBlocks);
        System.out.println("     blockchain length = " + blockchain.size());
        System.out.println("     highest height = " + highestHeight);
        System.out.println("     utxo pool size = " + utxoPool.size());
        System.out.println("     unconfirmed transactions size = " + unConfirmedTransactions.size());
        System.out.println("------------------------------------------------------------------------");
    }

    // Recursively visit each Block's children to build the tree

    private void visit(Block block) {
        block.validate();
        if (block.getStatus().isValid()) {
            LinkedList<Block> followers = nextBlocks.get(block.getHash());
            if (followers != null) {
                utxoPool.recordUTXOs(block);
                for (Block followingBlock : followers) {
                    visit(followingBlock);
                }
                utxoPool.unRecordUTXOs(block);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

    public Balance getBalance(Address publicKeyAddress) {
        return balances.get(publicKeyAddress);
    }

    public Block getHighestBlock() {
        return highestBlock;
    }

    public Collection<Hash> getUnconfirmedTransactionHashes() {
        return unConfirmedTransactions;
    }

    private void computeBalances() {
        balances = new HashMap<Address, Balance>();

        // TODO 19: Write the computeBalances function that computes the Balance for each Address, using the current UTXOs
        for (UTXO utxo : utxoPool.getAllUTXOs()) {
            Address address = utxo.getDestinationAddress();

            Balance balance = balances.get(address);
            if (balance == null) {
                balance = new Balance();
                balances.put(address, balance);
            }
            balance.addUTXO(utxo);
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    public void displayBalances() {
        for (Address publicKeyAddress : balances.keySet()) {
            Balance balance = balances.get(publicKeyAddress);
            System.out.println("Balance for " + publicKeyAddress + " is " + balance.getAmount());
        }
    }

    public void displayBalancesWithUTXOs() {
        for (Address publicKeyAddress : balances.keySet()) {
            Balance balance = balances.get(publicKeyAddress);
            System.out.println("Balance for " + publicKeyAddress + " is " + balance.getAmount());
            for (UTXO utxo : balance) {
                System.out.println("     " + utxo);
            }
        }
    }

    public void displayUTXOsForAddress(String addressTag) {
        Address publicKeyAddress = AddressDirectory.getAddress(addressTag);
        Balance balance = balances.get(publicKeyAddress);
        System.out.println("Balance for " + publicKeyAddress + " is " + balance.getAmount());
        for (UTXO utxo : balance) {
            System.out.println("     " + utxo);
        }
    }

    public void displayUnconfirmedTransactions() {
        System.out.println("UnConfirmed Transactions:");
        for (Hash transactionHash : unConfirmedTransactions) {
            getTransaction(transactionHash).display();
        }
    }

    public void displayRealTransactions() {
        System.out.println("========== BLOCKCHAIN ===========");
        for (Block block : blockchain) {
            block.display();
            for (Hash transactionHash : block.getTransactionHashes()) {
                getTransaction(transactionHash).display();
            }
        }
        System.out.println("\n========== UNCONFIRMED TRANSACTIONS ===========");
        for (Hash transactionHash : unConfirmedTransactions) {
            Transaction transaction = getTransaction(transactionHash);
            transaction.display();

        }

        System.out.println("\n========== BAD BLOCKS ===========");
        for (Block block : blocks.values()) {
            if (!blockchain.contains(block)) {
                System.out.println("=== BAD BLOCK");
                block.display();
            }
        }


    }

    public void displaySummary() {
        System.out.println("= = = = BLOCK CHAIN = = = = = = = = = = = = = = = = = = = = =");
        for (Block block : blockchain) {
            System.out.println("-*-*-*-*-*-*-*- BLOCK: " + block.getHash() + " ----- height " + block.getHeight() + " ------");
            for (Hash transactionHash : block.getTransactionHashes()) {
                getTransaction(transactionHash).displaySummary();
            }
        }
        System.out.println("\n========== UNCONFIRMED TRANSACTIONS ===========");
        for (Hash transactionHash : unConfirmedTransactions) {
            Transaction transaction = getTransaction(transactionHash);
            System.out.println("------------");
            transaction.displaySummary();
        }
        System.out.println("================================================");
    }

    public void displayUTXOs() {
        System.out.println("\n================== UTXOs ===================");
        for (UTXO utxo : utxoPool.getAllUTXOs()) {
            System.out.println(utxo);
        }
        System.out.println("================================================");

    }
}
