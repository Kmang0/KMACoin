package CatlinCoin;

/**
 * An Address is the hash of the public key of the sender or recipient of coins.
 */

public class Address {

    private final Hash publicKeyHash;

    /****************************************************************************************************************
     *  Constructs a new Address from a Hash of a public key.
     *
     * @param publicKeyHash the Hash of the public key.
     */

    public Address(Hash publicKeyHash) {
        this.publicKeyHash = publicKeyHash;
    }

    /****************************************************************************************************************
     *  Constructs a new Address from a String that contains the hexadecimal hash of a public key.
     *
     * @param hexString the String containing the hexadecimal hash of a public key
     */

    public Address(String hexString) {
        this.publicKeyHash = new Hash(hexString);
    }

    /****************************************************************************************************************
     *  Constructs a new Address from an array of bytes that stores the hash of a public key.
     *
     * @param hashBytes the array of bytes
     */

    public Address(byte[] hashBytes) {
        this.publicKeyHash = new Hash(hashBytes);
    }

    /****************************************************************************************************************
     *  Returns the address as a string of hex digits.
     *
     * @return A string containing the hexadecimal digits of the hash of the public key
     */

    public String getHex() {
        return publicKeyHash.getHex();
    }

    /****************************************************************************************************************
     *  Returns the address as an array of bytes.
     *
     * @return An array of bytes containing the hash of the public key
     */

    public byte[] getBytes() {
        return publicKeyHash.getBytes();
    }

    /****************************************************************************************************************
     *  Returns the Hash of the address.
     *
     * @return the Hash object for this address
     */

    public Hash getHash() {
        return publicKeyHash;
    }

    /****************************************************************************************************************
     *  Returns a user friendly display string for this address
     *
     * @return the public key hash hex string followed by its address tag if an address tag is known
     */

    @Override
    public String toString() {
        if (AddressDirectory.hasTag(this)) {
            return publicKeyHash.getHex() + " (" + AddressDirectory.getTag(this) + ")";
        } else {
            return publicKeyHash.getHex();
        }
    }

    /****************************************************************************************************************/

    @Override
    public boolean equals(Object other) {
        Address otherAddress = (Address) other;
        return this.publicKeyHash.equals(otherAddress.publicKeyHash);
    }

    /****************************************************************************************************************/

    @Override
    public int hashCode() {  // for use by HashMap and HashSet
        return publicKeyHash.hashCode();
    }

}
