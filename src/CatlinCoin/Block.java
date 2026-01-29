package CatlinCoin;

import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A Block contains a group of Transactions that are bundled together.
 * A Block is the fundamental unit of the mining process.
 */

public class Block {

    private Currency currency;

    private byte[] bytes;

    private Hash blockHeaderHash;
    private int height;
    private Hash previousBlockHash;
    private long nonce;
    private short numTransactions;
    private Hash transactionListHash;
    private Hash[] transactionHashes;

    private int transactionIndex;
    private Status status = Status.UNKNOWN;
    private String statusMessage = "";

    private static final int BLOCK_HEADER_BYTES = Hash.BYTE_LENGTH + 4 + Hash.BYTE_LENGTH + 8 + 2 + Hash.BYTE_LENGTH;

    private static Random random = new Random();

    /*******************************************************************************************************
     * Constructs a new block.  Normally this is called as part of the mining process.
     *
     * @param currency                 the currency to use
     * @param height                   the height of the new block
     * @param previousBlockHash        the Hash of the previous block to this one in the block chain
     * @param numTransactions          the number of transactions in the block, including the coinbase transaction
     */

    private Block(Currency currency, int height, Hash previousBlockHash, int numTransactions) {
        this.bytes = null;
        this.currency = currency;
        this.height = height;
        this.previousBlockHash = previousBlockHash;
        this.nonce = 0;
        this.numTransactions = (short) numTransactions;
        this.transactionHashes = new Hash[this.numTransactions];
        this.transactionIndex = 0;
    }

    /*******************************************************************************************************
     * Use this constructor for existing blocks that have been downloaded from the network
     *
     * @param currency   the currency to use
     * @param bytes      an array of bytes containing the block in raw binary form, as downloaded from the network
     */

    public Block(Currency currency, byte[] bytes) {
        this.currency = currency;
        this.bytes = bytes;
        unWrap();
    }

    /*******************************************************************************************************
     * A static factory function that creates a new block, mines it, and uploads it.
     *
     * @param currency            the currency to use
     * @param minerNickname       the nickname of the miner, who must have a local private key
     * @return the newly mined block
     */

    public static Block mineNewBlock(Currency currency, String minerNickname) {

        // TODO 27: construct a new block with new coinbase transaction and unconfirmed transactions, mine a legal hash, and upload it

        Block newBlock = null;

        return newBlock;
    }

    /*******************************************************************************************************
     * A static factory function that creates a new genesis block for a new currency.
     *
     * @param newCurrency           the new currency
     * @param creatorNickname       the nickname of the creator of the block, who must have a local private key
     * @param creatorCoinGrant      the number of coins the creator will be issued in the coinbase transaction
     * @return the new genesis block
     */

    public static Block createGenesisBlock(Currency newCurrency, String creatorNickname, long creatorCoinGrant) {

        // make the coinbase transaction, and upload it
        Transaction coinbaseTransaction = Transaction.createCoinbaseTransaction(newCurrency, creatorNickname, 0, 0);
        coinbaseTransaction.getOutput(0).setAmount(creatorCoinGrant);
        if (coinbaseTransaction.upload()) {

            // previous block hash for a genesis block is all zeros
            byte[] previousBlockHashBytes = new byte[Hash.BYTE_LENGTH];
            Arrays.fill(previousBlockHashBytes, (byte) 0);
            Hash previousBlockHash = new Hash(previousBlockHashBytes);

            // actually make the new block
            Block genesisBlock = new Block(newCurrency, 0, previousBlockHash, 1);
            genesisBlock.addTransaction(coinbaseTransaction);

            // even genesis blocks need to be mined
            genesisBlock.mineBlockHash();

            return genesisBlock;

        } else {
            return null;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////

    /*******************************************************************************************************
     * Returns the hash of the block
     *
     * @return the hash of the block
     */

    public Hash getHash() {
        return blockHeaderHash;
    }

    /*******************************************************************************************************
     * Returns the status of the block
     *
     * @return the status of the block
     */

    public Status getStatus() {
        return status;
    }

    /*******************************************************************************************************
     * Returns the hash of the block before this one in the block chain
     *
     * @return the hash of the previous block
     */

    public Hash getPreviousBlockHash() {
        return previousBlockHash;
    }

    /*******************************************************************************************************
     * Returns the height of the block
     *
     * @return the height of the block
     */

    public int getHeight() {
        return height;
    }

    /*******************************************************************************************************
     * Returns the hashes of the transactions in the block
     *
     * @return an array of the hashes of the transactions in the block
     */

    public Hash[] getTransactionHashes() {
        return transactionHashes;
    }

    /*******************************************************************************************************
     * Returns the currency of the block
     *
     * @return the currency of the block
     */

    public Currency getCurrency() {
        return currency;
    }

    /*******************************************************************************************************
     * Adds a transaction to the block.
     *
     * @param transaction   the transaction to add to the block
     */

    public void addTransaction(Transaction transaction) {
        transactionHashes[transactionIndex++] = transaction.getHash();
    }

    /*******************************************************************************************************
     * Uploads a block to the network.
     *
     * @return true if the upload was successful, and false otherwise
     */

    public boolean upload() {
        checkLegality();
        if (status.isLegal()) {
            if (Network.uploadBlock(currency, bytes)) {
                System.out.println("block upload successful");
                return true;
            }
        }
        return false;
    }

    /*******************************************************************************************************
     * Displays a description of this block on System.out.
     */

    public void display() {
        System.out.println("BLOCK:  (" + status + " status)   " + statusMessage);

        if (blockHeaderHash == null)
            System.out.println("   hash: null");
        else
            System.out.println("   hash: " + blockHeaderHash);

        System.out.println("   height: " + height);
        System.out.println("   previous block hash: " + previousBlockHash);
        System.out.println("   nonce: " + nonce);
        System.out.println("   transactions: " + numTransactions);
        System.out.println("   transaction list hash: " + transactionListHash);
        for (int i = 0; i < numTransactions; i++) {
            System.out.println("      transaction hash: " + transactionHashes[i]);
        }
    }

    /*******************************************************************************************************
     * Writes the data for the block into the bytes array in preparation for mining.
     */

    private void wrapUp() {
        int totalBytes = BLOCK_HEADER_BYTES + Hash.BYTE_LENGTH * numTransactions;
        bytes = new byte[totalBytes];
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);

        // TODO 28: Put the contents of this block into the byteBuffer, including computing the transactionListHash
        //   Do not compute or write the block header hash, that must wait until after mining

    }


    /*******************************************************************************************************
     * Reads the raw binary form in the bytes[] array and sets the Block's fields.
     * This is normally called just after downloading a block from the network.
     */

    private void unWrap() {
        try {

            if (bytes.length < 142) {
                throw new IllegalBlockException("block has only " + bytes.length + " bytes, which too small");
            }

            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);

            byte[] hashBytes = new byte[Hash.BYTE_LENGTH];
            byteBuffer.get(hashBytes);
            blockHeaderHash = new Hash(hashBytes);

            Hash newHash = Hash.compute(bytes, Hash.BYTE_LENGTH, BLOCK_HEADER_BYTES - Hash.BYTE_LENGTH);
            if (!blockHeaderHash.equals(newHash)) {
                throw new IllegalBlockException("block has invalid hash");
            }

            height = byteBuffer.getInt();

            hashBytes = new byte[Hash.BYTE_LENGTH];
            byteBuffer.get(hashBytes);
            previousBlockHash = new Hash(hashBytes);

            nonce = byteBuffer.getLong();

            numTransactions = byteBuffer.getShort();

            if (numTransactions < 1) {
                throw new IllegalBlockException("block must have a positive number of transactions, not " + numTransactions);
            }

            if (bytes.length != 110 + numTransactions * Hash.BYTE_LENGTH) {
                throw new IllegalBlockException("block has " + bytes.length + " bytes but should have " + (110+numTransactions*Hash.BYTE_LENGTH) + " bytes to fit " + numTransactions + " transactions");
            }

            hashBytes = new byte[Hash.BYTE_LENGTH];
            byteBuffer.get(hashBytes);
            transactionListHash = new Hash(hashBytes);

            transactionHashes = new Hash[numTransactions];
            for (int i = 0; i < numTransactions; i++) {
                hashBytes = new byte[Hash.BYTE_LENGTH];
                byteBuffer.get(hashBytes);
                transactionHashes[i] = new Hash(hashBytes);
            }

            checkLegality();

        } catch (IllegalBlockException illegalBlockException) {
            recordIllegalBlock(illegalBlockException.getMessage());
        }
    }

    /*******************************************************************************************************
     * Determines if a Block is legal or not, and sets the status field accordingly.
     */

    public void checkLegality() {
        if (status != Status.UNKNOWN)
            return;

        try {

            if (!blockHeaderHash.equals(new Hash(Arrays.copyOfRange(bytes, 0, Hash.BYTE_LENGTH))))
                throw new IllegalBlockException(" incorrect block header hash in block bytes array");

            Hash newHash = Hash.compute(bytes, Hash.BYTE_LENGTH, BLOCK_HEADER_BYTES - Hash.BYTE_LENGTH);
            if (!blockHeaderHash.equals(newHash))
                throw new IllegalBlockException(" block header hash does not agree with byte array data");

            if (blockHeaderHash.getHex().compareTo(currency.getMiningDifficultyLevel(height)) > 0)
                throw new IllegalBlockException(" block header hash is not difficult enough");

            if (numTransactions <= 0)
                throw new IllegalBlockException("numTransactions " + numTransactions + " must be positive");

            if (numTransactions != transactionHashes.length)
                throw new IllegalBlockException("numTransactions " + numTransactions + " does not match length of transaction list: " + transactionHashes.length);

            Hash newTransactionListHash = Hash.compute(bytes, BLOCK_HEADER_BYTES, bytes.length - BLOCK_HEADER_BYTES);
            if (!transactionListHash.equals(newTransactionListHash))
                throw new IllegalBlockException("transaction list hash is incorrect: " + transactionListHash);

            status = Status.LEGAL;

        } catch (IllegalBlockException illegalBlockException) {
            recordIllegalBlock(illegalBlockException.getMessage());
        }
    }

    /*******************************************************************************************************
     * Determines if a Block is valid or not, and sets the status field accordingly.
     */

    public void validate() {
        //System.out.println("## validating block " + height + " hash " + getHash());

        if (status == Status.UNKNOWN)
            checkLegality();
        if (!status.isLegal()) return;
        if (status == Status.VALID || status == Status.INVALID)  // in case we tried validating this block earlier
            status = Status.LEGAL;

        // genesis blocks are always valid
        boolean isGenesisBlock = blockHeaderHash.equals(currency.getGenesisBlockHash());
        if (isGenesisBlock) {
            status = Status.VALID;
            return;
        }

        try {

            BlockChain myBlockChain = currency.getBlockChain();


            Block previousBlock = myBlockChain.getBlock(previousBlockHash);
            if (previousBlock == null)
                throw new InvalidBlockException("previous block does not exist: " + previousBlockHash);

// TODO 14: verify the previous block is already valid (just use getStatus) and has correct height
            if (!previousBlock.getStatus().isValid())
                throw new InvalidBlockException("Previous block is not valid");

            if (previousBlock.getHeight() + 1 != this.height)
                throw new InvalidBlockException("Block height is incorrect: expected " + (previousBlock.getHeight() + 1) + ", got " + this.height);



            // TODO 15: Verify that every transaction in this block exists
            for (Hash txHash : transactionHashes) {
                Transaction tx = myBlockChain.getTransaction(txHash);
                if (tx == null) {
                    throw new InvalidBlockException("Transaction " + txHash + " does not exist");
                }
            }

            // TODO 16: Verify that no transaction is included in this block more than once
            Set<Hash> seenTxs = new HashSet<>();
            for (Hash txHash : transactionHashes) {
                if (!seenTxs.add(txHash)) {
                    throw new InvalidBlockException("Duplicate transaction found in block: " + txHash);
                }
            }

            // TODO 17: Use Transaction's validateCoinbase function to make sure coinbase transaction is valid
            Transaction coinbaseTx = myBlockChain.getTransaction(transactionHashes[0]);
            long totalFees = 0;

            for (int i = 1; i < transactionHashes.length; i++) {
                Transaction utx = myBlockChain.getTransaction(transactionHashes[i]);
                totalFees += utx.getFee();
            }

            coinbaseTx.validateCoinbase(height, totalFees);
            if (!coinbaseTx.getStatus().isValid()) {
                throw new InvalidBlockException("Coinbase transaction is invalid");
            }

            // TODO 18: Verify that every non-coinbase transaction in this Block is valid
            for (int i = 1; i < transactionHashes.length; i++) {
                Transaction utx = myBlockChain.getTransaction(transactionHashes[i]);
                utx.checkLegality();
                utx.validate();

                if (!utx.getStatus().isValid()) {
                    throw new InvalidBlockException("Transaction " + utx.getHash() + " failed validation");
                }
            }

            status = Status.VALID;

        } catch (InvalidBlockException invalidBlockException) {
            recordInvalidBlock(invalidBlockException.getMessage());
        }

        //System.out.println("## done validating block " + height + " hash " + getHash());
    }

    /******************************************************************************************************
     * An Exception to throw when a Block is found to be illegal.
     */

    private static class IllegalBlockException extends Exception {
        IllegalBlockException(String message) {
            super(message);
        }
    }

    /*******************************************************************************************************
     * An Exception to throw when a Block is found to be invalid.
     */

    private static class InvalidBlockException extends Exception {
        InvalidBlockException(String message) {
            super(message);
        }
    }

    /*******************************************************************************************************
     * Sets the Block's status to ILLEGAL.
     *
     * @param message  The new statusMessage indicating the reason the block is illegal
     */

    private void recordIllegalBlock(String message) {
        status = Status.ILLEGAL;
        statusMessage = message;
        System.out.println("Illegal Block " + blockHeaderHash + ": " + message);
    }

    /*******************************************************************************************************
     * Sets the Block's status to INVALID.
     *
     * @param message  The new statusMessage indicating the reason the block is invalid
     */

    private void recordInvalidBlock(String message) {
        status = Status.INVALID;
        statusMessage = message;
        System.out.println("Invalid Block " + blockHeaderHash + ": " + message);
    }

    /*******************************************************************************************************/

    @Override
    public String toString() {
        return "Block: " + blockHeaderHash;
    }

    /*******************************************************************************************************/

    @Override
    public int hashCode() {  // for storing Blocks in HashMaps and HashSets
        return blockHeaderHash.hashCode();
    }

    /*******************************************************************************************************/

    @Override
    public boolean equals(Object other) {
        Block otherBlock = (Block) other;
        return this.blockHeaderHash.equals(otherBlock.blockHeaderHash);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    /*******************************************************************************************************
     * Mines a block.  After converting a block to binary form in the bytes array, searches for a legal block hash.
     */

    private void mineBlockHash() {
        wrapUp();
        long numHashesTried = 0;
        String miningDifficultyLevel = currency.getMiningDifficultyLevel(height);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        long startTime = System.currentTimeMillis();

        // TODO 29: Try different nonce values until the block hash is less than the currency's mining difficulty level
        //       Remember to set both the blockHeaderHash and nonce fields,
        //        and use the ByteBuffer to write both into the bytes array

        long endTime = System.currentTimeMillis();
        checkLegality();
        double durationSeconds = ((endTime - startTime) / 1000.0);
        double hashesPerSecond = numHashesTried / durationSeconds;
        System.out.printf("Mining took = %.3f seconds with %d attempts at a rate of %.3f KiloHashes/Second\n", durationSeconds, numHashesTried, (hashesPerSecond / 1000.0));
    }

}
