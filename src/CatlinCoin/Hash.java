package CatlinCoin;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;

/**
 * Stores an SHA-256 hash, as an array of 32 bytes or a String of 64 hex digits.
 * <p>
 * If you want to compute the hash of some raw data, then use one of the static {@link Hash#compute compute} functions.
 */

public class Hash {

    private byte[] bytes;
    private String hex;

    ////////////////////////////////////////////////////////////////////////////////////////////

    public static final int BYTE_LENGTH = 32;

    private static final MessageDigest MESSAGE_DIGEST_SHA_256;

    static {
        try {
            MESSAGE_DIGEST_SHA_256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Java library does not support SHA-256");
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    /*****************************************************************************************************************
     *  Creates a Hash from an array of 32 bytes that contains the hash value.
     *
     * @param bytes the hash value as an array of 32 bytes
     */

    public Hash(byte[] bytes) {
        if (bytes == null || bytes.length != BYTE_LENGTH) {
            throw new IllegalArgumentException("Hash must be exactly " + BYTE_LENGTH + " bytes");
        }
        this.bytes = bytes;
        this.hex = null;
    }

    /*****************************************************************************************************************
     *  Creates a Hash from a String of 64 hex digits
     *
     * @param hex the hash value as a String of 64 hex digits
     */

    public Hash(String hex) {
        if (hex == null || hex.length() != (BYTE_LENGTH * 2)) {
            throw new IllegalArgumentException("Hash must be exactly " + (BYTE_LENGTH * 2) + " hex digits : " + hex);
        }
        this.hex = hex;
        this.bytes = null;
    }

    /*****************************************************************************************************************
     *  Computes the hash of data stored in an array of bytes.  This version hashes the entire array.
     *
     * @param data an array of bytes containing data that needs to be hashed
     * @return the Hash of the given data
     */

    public static Hash compute(byte[] data) {
        return compute(data, 0, data.length);
    }

    /*****************************************************************************************************************
     *  Computes the hash of data stored in a portion of an array of bytes.  This version hashes the indicated range of bytes within the array.
     *
     * @param data an array of bytes containing data that needs to be hashed
     * @param start the first byte within the array to include in the data to be hashed
     * @param length the number of bytes in the data to be hashed
     * @return the Hash of the indicated range of the given data
     */

    public static Hash compute(byte[] data, int start, int length) {
        MESSAGE_DIGEST_SHA_256.reset();
        MESSAGE_DIGEST_SHA_256.update(data, start, length);
        byte[] hashBytes = MESSAGE_DIGEST_SHA_256.digest();
        return new Hash(hashBytes);
    }

    /*****************************************************************************************************************
     *  Returns the hash as an array of bytes.
     *
     * @return the array of 32 bytes containing the hash
     */

    public byte[] getBytes() {
        if (bytes == null) {
            bytes = HexFormat.of().parseHex(hex);
        }
        return bytes;
    }

    /*****************************************************************************************************************
     *  Returns the hash as a hexadecimal String
     *
     * @return a String of 64 hex digits containing the hash
     */

    public String getHex() {
        if (hex == null) {
            hex = HexFormat.of().formatHex(bytes).toUpperCase();
        }
        return hex;
    }

    /*****************************************************************************************************************
     *  Returns the hash as a hexadecimal String
     *
     * @return a String of 64 hex digits containing the hash
     */

    @Override
    public String toString() {
        return getHex();
    }

    /*****************************************************************************************************************/

    @Override
    // a "smart" equals function that doesn't convert between bytes and hex if it doesn't have to
    public boolean equals(Object other) {
        Hash otherHash = (Hash) other;
        if (this.bytes != null && otherHash.bytes != null) {
            return Arrays.equals(this.bytes, otherHash.bytes);
        } else if (this.hex != null && otherHash.hex != null) {
            return this.hex.equals(otherHash.hex);
        } else {
            return Arrays.equals(this.getBytes(), otherHash.getBytes());
        }
    }

    /*****************************************************************************************************************/

    @Override
    // this is so we can use a Hash as the key in a HashSet or HashMap data structure
    // yes, it feels weird to be hashing a hash!
    public int hashCode() {  // for use by HashMap and HashSet
        return getHex().hashCode();
    }
}
