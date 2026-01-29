package CatlinCoin;

import java.security.KeyPair;
import java.util.Arrays;
import java.util.TreeMap;

/**
 * A class for storing currency specific settings, including mining difficulties and mining rewards.
 */

public class Currency {

    private final String currencyName;
    private Hash genesisBlockHash;
    private long minimumTransactionFee;
    private BlockChain blockChain;

    // maps block height threshold -> mining reward amount
    private TreeMap<Integer, Long> miningRewards = new TreeMap<>();

    // maps block height threshold -> mining difficulty level
    private TreeMap<Integer, String> miningDifficultyLevels = new TreeMap<>();

    /**********************************************************************************************************
     * Constructs a new Currency object.  This is used to record the configuration for a currency that already exists.
     * To actually create a brand new currency, use the {@link Currency#createNewCurrency createNewCurrency} function.
     *
     * @param currencyName                     The name of the currency
     * @param genesisBlockHashHex              The Hash for the genesis block for this currency
     * @param minimumTransactionFee            The minimum transaction fee
     * @param initialMinerReward               The initial mining reward
     * @param initialMiningDifficultyLevel     The initial mining difficulty level, as a string that starts with zeros, such as "0001"
     */

    public Currency(String currencyName, String genesisBlockHashHex, long minimumTransactionFee, long initialMinerReward, String initialMiningDifficultyLevel) {
        this.currencyName = currencyName;
        if (genesisBlockHashHex != null)
            this.genesisBlockHash = new Hash(genesisBlockHashHex);
        this.minimumTransactionFee = minimumTransactionFee;
        this.blockChain = new BlockChain(this);
        addMiningReward(0, initialMinerReward);
        addMiningDifficultyLevel(0, initialMiningDifficultyLevel);
    }

    /**********************************************************************************************************
     * Gets the name of the currency.
     *
     * @return the name of the currency
     */

    public String getName() {
        return currencyName;
    }

    /**********************************************************************************************************
     *  Gets the BlockChain for this currency.
     *
     * @return the BlockChain for the currency
     */

    public BlockChain getBlockChain() {
        return blockChain;
    }

    /**********************************************************************************************************
     *  Gets the block hash of the genesis block for this currency.
     *
     * @return the Hash of the genesis block for this currency
     */
    public Hash getGenesisBlockHash() {
        return genesisBlockHash;
    }

    /**********************************************************************************************************
     * Gets the minimum transaction fee.
     *
     * @return the minimum transaction fee
     */

    public long getMinimumTransactionFee() {
        return minimumTransactionFee;
    }


    /**********************************************************************************************************
     *  Sets the mining reward for blocks with a height greater than or equal to the given height threshold.
     *
     * @param blockHeightThreshold   the height of the first block that will use the new mining reward
     * @param miningReward           the new mining reward
     */

    public void addMiningReward(int blockHeightThreshold, long miningReward) {
        miningRewards.put(blockHeightThreshold, miningReward);
    }

    /**********************************************************************************************************
     *  Gets the mining reward for a block with the given height.
     *
     * @param blockHeight      the height of the block that we are asking about
     * @return the mining reward in effect for a block at that height
     */

    public long getMiningReward(int blockHeight) {
        return miningRewards.floorEntry(blockHeight).getValue();
    }

    /**********************************************************************************************************
     *  Adds a new mining difficulty level for blocks with a height greater than or equal to the given height threshold.
     *
     * @param blockHeightThreshold    the height of the first block that will use the new mining difficulty level
     * @param miningDifficultyLevel   the new difficulty level, as a String that starts with zeros, such as "0001"
     */

    public void addMiningDifficultyLevel(int blockHeightThreshold, String miningDifficultyLevel) {
        miningDifficultyLevels.put(blockHeightThreshold, miningDifficultyLevel);
    }

    /**********************************************************************************************************
     * Gets the mining difficulty level for a block with the given height.
     *
     * @param blockHeight  the height of the block that we are asking about
     * @return the mining difficulty in effect for a block at that height
     */

    public String getMiningDifficultyLevel(int blockHeight) {
        return miningDifficultyLevels.floorEntry(blockHeight).getValue();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////

    /*********************************************************************************************************
     * Creates a brand new currency, including building, mining, and uploading the genesis block for the new currency.
     *
     * @param currencyName                     the name for the new currency
     * @param creatorNickname                  the nickname of the user creating the currency (who must have a local private key available)
     * @param creatorCoinGrant                 how many coins to grant to the creator by fiat
     * @param minimumTransactionFee            the minimum transaction fee
     * @param initialMinerReward               the initial mining reward
     * @param initialMiningDifficultyLevel     the initial mining difficulty level
     * @return the new currency
     */

    public static Currency createNewCurrency(String currencyName, String creatorNickname, long creatorCoinGrant, long minimumTransactionFee, long initialMinerReward, String initialMiningDifficultyLevel) {

        // add a new currency with a null genesis block hash (temporarily)
        Currency newCurrency = new Currency(currencyName, null, minimumTransactionFee, initialMinerReward, initialMiningDifficultyLevel);

        // make a key pair for the creator, unless they have one already
        KeyPair creatorKeyPair = null;
        if (CryptoRSA.hasPrivateKey(creatorNickname)) {
            creatorKeyPair = CryptoRSA.readKeyFiles(creatorNickname);
        } else {
            creatorKeyPair = CryptoRSA.generateKeyPair(creatorNickname);
        }
        Address creatorPublicKeyAddress = CryptoRSA.getPublicKeyAddress(creatorNickname);
        System.out.println("public key for " + creatorNickname + " = " + creatorPublicKeyAddress);

        Block genesisBlock = Block.createGenesisBlock(newCurrency, creatorNickname, creatorCoinGrant);

        // now that we know the genesis block hash, update the genesisBlockHash of the new Currency
        newCurrency.genesisBlockHash = genesisBlock.getHash();

        // optionally, display the new block
        genesisBlock.display();

        // upload it to the network
        if (genesisBlock.upload()) {
            System.out.println("currency " + currencyName + " has new genesis block hash = " + genesisBlock.getHash());
        } else {
            System.out.println("ERROR: attempt to upload genesis block failed!");
        }

        return newCurrency;
    }

}
