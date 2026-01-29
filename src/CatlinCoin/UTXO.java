package CatlinCoin;

/**
 * A UTXO is an Unspent Transaction Output and it represents active, spendable coins in the system.
 */

public class UTXO {

    private Hash transactionHash;        // where this UTXO comes from
    private byte coinIndex;              // which output index it comes from
    private long amount;                 // how much money
    private Address destinationAddress;  // who this money is for

    /******************************************************************************************************************
     *  Creates a new Unspent Transaction Output.
     *
     * @param transaction The Transaction that created this output.
     * @param coinIndex The index of this output within its Transaction.
     */

    public UTXO(Transaction transaction, byte coinIndex) {
        this.transactionHash = transaction.getHash();
        this.coinIndex = coinIndex;
        Transaction.TxOutput txOutput = transaction.getOutput(coinIndex);
        amount = txOutput.getAmount();
        destinationAddress = txOutput.getDestinationAddress();
    }

    /******************************************************************************************************************
     *  Returns the Hash of the Transaction that created this UTXO.
     *
     * @return the Transaction Hash
     */

    public Hash getTransactionHash() {
        return transactionHash;
    }

    /******************************************************************************************************************
     *  Returns the index of the output that created this UTXO.
     *
     * @return the index
     */

    public byte getCoinIndex() {
        return coinIndex;
    }

    /******************************************************************************************************************
     *  Returns the amount of money represented by this UTXO.
     *
     * @return the amount of money that this UTXO represents
     */

    public long getAmount() {
        return amount;
    }

    /******************************************************************************************************************
     *  Returns the owner of the money represented by this UTXO
     *
     * @return the address that the money in this UTXO belongs to
     */

    public Address getDestinationAddress() {
        return destinationAddress;
    }

    /*****************************************************************************************************************/

    @Override
    public boolean equals(Object other) {
        UTXO otherUTXO = (UTXO) other;
        return this.transactionHash.equals(otherUTXO.transactionHash) && this.coinIndex == otherUTXO.coinIndex;
    }

    /*****************************************************************************************************************/

    @Override
    // so UTXO can be used as the key in a HashMap or HashSet, if desired
    public int hashCode() {
        return transactionHash.hashCode() + 29 * coinIndex;
    }

    /*****************************************************************************************************************/

    @Override
    public String toString() {
        return "UTXO: " + transactionHash + " coin " + coinIndex + " amount " + amount + " for " + destinationAddress;
    }
}
