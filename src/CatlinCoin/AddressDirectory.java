package CatlinCoin;

import java.io.*;
import java.util.HashMap;

/**
 * A repository for known addresses (public key hashes) and user-friendly tags for each address.
 * The repository automatically includes every public key stored in the keys folder
 * and everyone listed in the addresses.txt file.
 * You can add additional records by editing addresses.txt or by calling the {@link AddressDirectory#addAddress addAddress} function.
 * <br>
 * <pre>
 * The format of addresses.txt is:
 *
 * 8169CC106257C4B3272FFA5B1738DA0A5A46027A9D2CB7558BF3F28B4662F6F2 frodo
 * 2F1E749F577F6FA76583E8DA16192BB5AB5D61BDF3751A29B3A6235F65D1CF51 bilbo
 *
 * </pre>
 */

public class AddressDirectory {

    private static HashMap<Address, String> address2tagMap = new HashMap<>();
    private static HashMap<String, Address> tag2addressMap = new HashMap<>();

    static {
        // add every address from the keys folder
        for (String nickname : CryptoRSA.getAllNicknames()) {
            addAddress(CryptoRSA.getPublicKeyAddress(nickname), nickname);
        }

        // add every address in the "addresses.txt" file
        File addressesFile = new File("addresses.txt");
        if (addressesFile.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(addressesFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    String[] fields = line.split(" +", 2);
                    String publicKeyAddress = fields[0].trim();
                    String addressTag = fields[1].trim();
                    addAddress(new Address(publicKeyAddress), addressTag);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private AddressDirectory() { }  // no need to make objects of this class, since all methods are static

    /****************************************************************************************************************
     * Adds an address and tag to the repository.
     *
     * @param publicKeyAddress the address
     * @param addressTag       the user readable label for this address
     */

    public static void addAddress(Address publicKeyAddress, String addressTag) {
        address2tagMap.put(publicKeyAddress, addressTag);
        tag2addressMap.put(addressTag, publicKeyAddress);
    }

    /****************************************************************************************************************
     * Returns the tag for a given address.
     *   If there is no tag in the directory, then return the first few hex digits of the address.
     *
     * @param publicKeyAddress the address
     * @return the user readable label for this address
     */

    public static String getTag(Address publicKeyAddress) {
        String tag = address2tagMap.get(publicKeyAddress);
        if (tag == null) {
            return publicKeyAddress.getHex().substring(0, 5);
        } else {
            return tag;
        }
    }

    /****************************************************************************************************************
     * Returns the address for a given tag.
     *
     * @param addressTag the user readable label for the address
     * @return the address
     */

    public static Address getAddress(String addressTag) {
        return tag2addressMap.get(addressTag);
    }

    /****************************************************************************************************************
     *  Determines if the given Address has a user readable tag in the directory.
     *
     * @param publicKeyAddress the address
     * @return true if the address has a stored tag, and false otherwise
     */

    public static boolean hasTag(Address publicKeyAddress) {
        return address2tagMap.get(publicKeyAddress) != null;
    }


}
