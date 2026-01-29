package CatlinCoin;

import javax.crypto.Cipher;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;
import java.util.ArrayList;
import java.util.Base64;

/**
 * Provides static utility functions for generating RSA key pairs and saving them in files,
 * reading RSA public and private keys from files,
 * creating and verifying RSA digital signatures,
 * computing the address of a public key,
 * and encrypting and decrypting data using RSA.
 */


public class CryptoRSA {

    // DO NOT CHANGE ONE OF THESE CONSTANTS WITHOUT CHANGING THE OTHERS!
    //  use the checkKeySize() function (below) to compute new values for a given number of bits
    public static final int RSA_KEY_BITS = 1024;
    public static final int RSA_PUBLIC_KEY_BYTES = 162;
    public static final int RSA_SIGNATURE_BYTES = 128;

    private static final String keyDirName = "keys";  // all of your keys will be stored in this folder

    private static final KeyFactory KEY_FACTORY_RSA;
    private static final Signature SIGNATURE_SHA256_RSA;

    static {
        try {
            KEY_FACTORY_RSA = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Java library does not support RSA");
        }

        try {
            SIGNATURE_SHA256_RSA = Signature.getInstance("SHA256withRSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Java library does not support SHA256withRSA");
        }
    }

    private CryptoRSA() { }  // no need to create objects of this class, since all methods are static

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    /******************************************************************************************************
     * Generates an RSA public/private key pair and stores it in files with the given nickname.
     * The keys will be automatically stored in files in the keyDirName folder ("keys" by default).
     *
     * @param nickname The label to use in the filename of the files that will store the generated keys.
     * @return The KeyPair containing the public and private keys.
     */

    public static KeyPair generateKeyPair(String nickname) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(RSA_KEY_BITS);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            if (keyPair.getPublic().getEncoded().length != RSA_PUBLIC_KEY_BYTES) {
                throw new RuntimeException("RSA_PUBLIC_KEY_BYTES does not agree with RSA_KEY_BITS");
            }

            checkKeysDirectory();
            writeKeyFile(keyPair.getPublic(), nickname, "publickey", "RSA PUBLIC KEY");
            writeKeyFile(keyPair.getPrivate(), nickname, "privatekey", "RSA PRIVATE KEY");

            return keyPair;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("Java library does not support RSA");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("File not found: " + e);
        }
    }

    /******************************************************************************************************
     * Creates the keys directory if it does not exist already.
     */

    private static void checkKeysDirectory() {
        File keyDir = new File(keyDirName);
        if (keyDir.exists() && !keyDir.isDirectory()) {
            throw new RuntimeException(keyDirName + " must be a folder");
        }
        if (!keyDir.exists()) {
            keyDir.mkdir();
        }
    }

    /******************************************************************************************************
     * Writes a key to a file in the standard PEM format.
     *
     * @param key         The PublicKey or PrivateKey to save
     * @param nickname    The label for the filenames of the files that arte created
     * @param filePrefix  The prefix for the filenames (typically "publickey" or "privatekey"
     * @param label       The identifying label to write on the first and last line of the file
     *
     * @throws FileNotFoundException
     */

    private static void writeKeyFile(Key key, String nickname, String filePrefix, String label) throws FileNotFoundException {
        File file = new File(keyDirName, filePrefix + "-" + nickname + ".pem");
        PrintStream stream = new PrintStream(new FileOutputStream(file));
        stream.println("-----BEGIN " + label + "-----");
        stream.println(Base64.getMimeEncoder().encodeToString(key.getEncoded()));
        stream.println("-----END " + label + "-----");
        stream.close();
    }

    /******************************************************************************************************
     *  Determines whether the given nickname already has a private key stored in a local file.
     *
     * @param nickname the nickname, which must identify a privatekey file in the keys folder
     * @return true if the key exists, and false otherwise
     */

    public static boolean hasPrivateKey(String nickname) {
        File file = new File(keyDirName, "privatekey-" + nickname + ".pem");
        return file.exists();
    }

    /******************************************************************************************************
     * Reads both the public and private key for a given nickname and returns them as a KeyPair.
     *
     * @param nickname the nickname, which must identify a pair of key files in the keys folder
     * @return The KeyPair containing the public and private keys.
     */

    public static KeyPair readKeyFiles(String nickname) {
        return new KeyPair(readPublicKeyFromFile(nickname), readPrivateKeyFromFile(nickname));
    }

    /******************************************************************************************************
     * Returns a PublicKey read from a file.
     *
     * @param nickname the nickname that identifies the file
     * @return the PublicKey contained in the file
     */

    public static PublicKey readPublicKeyFromFile(String nickname) {
        try {
            byte[] key = readKeyFile(nickname, "publickey");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(key);
            return KEY_FACTORY_RSA.generatePublic(keySpec);

        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid Key Spec: " + e);
        }
    }

    /******************************************************************************************************
     * Returns a PrivateKey read from a file.
     *
     * @param nickname the nickname that identifies the file
     * @return the PrivateKey contained in the file
     */

    public static PrivateKey readPrivateKeyFromFile(String nickname) {
        try {
            byte[] key = readKeyFile(nickname, "privatekey");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(key);
            return KEY_FACTORY_RSA.generatePrivate(keySpec);

        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid Key Spec: " + e);
        }
    }

    /******************************************************************************************************
     * Reads a PEM formatted key file and decodes the body into an array of bytes.
     *
     * @param nickname    the nickname that identifies the file
     * @param filePrefix  the prefix for the filenames (typically "publickey" or "privatekey"
     * @return an array of bytes containing the raw key data
     */

    private static byte[] readKeyFile(String nickname, String filePrefix) {
        File file = new File(keyDirName, filePrefix + "-" + nickname + ".pem");
        try {
            String keyString = "";
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            boolean readingKey = false;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("-----BEGIN"))
                    readingKey = true;
                else if (line.startsWith("-----END"))
                    readingKey = false;
                else if (readingKey)
                    keyString += line;
            }
            br.close();
            byte[] decodedKey = Base64.getMimeDecoder().decode(keyString);
            return decodedKey;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("File not found: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("IO Exception: " + e);
        }
    }

    /******************************************************************************************************
     * Reads a Public Key from a byte array.
     *
     * @param bytes    An array of bytes containing a raw public key
     * @return A PublicKey object that contains the public key
     */

    public static PublicKey readPublicKeyFromData(byte[] bytes) {
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
            return KEY_FACTORY_RSA.generatePublic(keySpec);

        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid Key Spec: " + e);
        }
    }

    /******************************************************************************************************
     * Returns a list of all nicknames that have a local public key file.
     *
     * @return The list of nicknames for locally stored public keys.
     */

    public static List<String> getAllNicknames() {
        checkKeysDirectory();
        File keyDir = new File(keyDirName);
        List<String> nicknames = new ArrayList<>();
        for (File file : keyDir.listFiles()) {
            String filename = file.getName();
            if (!filename.endsWith(".pem")) continue;
            if (!filename.startsWith("publickey-")) continue;
            String nickname = filename.substring(10, filename.length() - 4);
            nicknames.add(nickname);
        }
        return nicknames;
    }


    /******************************************************************************************************
     * Produces a Digital Signature for the given bytes, signed with the given Private Key.
     * This version of the function signs the contents of the entire dataToSign array.
     *
     * @param dataToSign    The array of bytes containing data to be signed
     * @param privateKey    The private key to use when signing the data
     * @return An array of bytes that contains the signature
     */

    public static byte[] sign(byte[] dataToSign, PrivateKey privateKey) {
        return sign(dataToSign, 0, dataToSign.length, privateKey);
    }

    /******************************************************************************************************
     * Produces a Digital Signature for the given bytes, signed with the given Private Key.
     * This version of the function signs only the contents of the dataToSign array starting
     * at the start byte and continuing for length bytes.
     *
     * @param dataToSign     The array of bytes containing data to be signed
     * @param start          The index of the first byte in the array that should be signed
     * @param length         The number of bytes in the array that should be signed
     * @param privateKey     The private key to use when signing the data
     * @return An array of bytes that contains the signature
     */

    public static byte[] sign(byte[] dataToSign, int start, int length, PrivateKey privateKey) {
        try {
            Signature signer = SIGNATURE_SHA256_RSA;
            signer.initSign(privateKey);
            signer.update(dataToSign, start, length);
            byte[] signature = signer.sign();
            if (signature.length != RSA_SIGNATURE_BYTES) {
                throw new RuntimeException("RSA_SIGNATURE_BYTES does not agree with RSA_KEY_BITS");
            }
            return signature;

        } catch (InvalidKeyException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid Key: " + e);
        } catch (SignatureException e) {
            e.printStackTrace();
            throw new RuntimeException("Signature Exception: " + e);
        }
    }

    /******************************************************************************************************
     * Verifies the legitimacy of a digital signature.
     * This version assumes that the entire signedData array was signed.
     *
     * @param signedData     An array of bytes containing the data that was previously signed
     * @param signature      An array of bytes containing the digital signature
     * @param publicKey      The PublicKey of the user who supposedly signed the data and produced the signature
     * @return true if the signature is valid, and false if it is not
     */

    public static boolean verifySignature(byte[] signedData, byte[] signature, PublicKey publicKey) {
        return verifySignature(signedData, 0, signedData.length, signature, publicKey);
    }

    /******************************************************************************************************
     * Verifies the legitimacy of a digital signature.
     * This version assumes that only a portion of the signedData array was signed,
     * starting at the start byte, and extending for length bytes.
     *
     * @param signedData     An array of bytes containing the data that was previously signed
     * @param start          The index of the first byte in the array that was signed
     * @param length         The number of bytes in the array that were signed
     * @param signature      An array of bytes containing the digital signature
     * @param publicKey      The PublicKey of the user who supposedly signed the data and produced the signature
     * @return true if the signature is valid, and false if it is not
     */

    public static boolean verifySignature(byte[] signedData, int start, int length, byte[] signature, PublicKey publicKey) {
        try {
            Signature verifier = SIGNATURE_SHA256_RSA;
            verifier.initVerify(publicKey);
            verifier.update(signedData, start, length);
            return verifier.verify(signature);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid Key: " + e);
        } catch (SignatureException e) {
            e.printStackTrace();
            throw new RuntimeException("Signature Exception: " + e);
        }
    }

    /******************************************************************************************************
     *  Returns the Address for a given nickname
     *
     * @param nickname the nickname, which must identify a publickey file in the keys folder
     * @return the Address for the public key
     */

    public static Address getPublicKeyAddress(String nickname) {
        return new Address(getPublicKeyHash(readPublicKeyFromFile(nickname)));
    }

    /******************************************************************************************************
     *  Returns the Address for a public key contained in an array of bytes.
     *
     * @param publicKeyBytes an array of bytes that contains the full public key (not hashed)
     * @return the Address for the public key
     */

    public static Address getPublicKeyAddress(byte[] publicKeyBytes) {
        return new Address(getPublicKeyHash(readPublicKeyFromData(publicKeyBytes)));
    }

    /******************************************************************************************************
     * Returns the Address for a given PublicKey.
     *
     * @param publicKey the given PublicKey
     * @return the Address for the public key
     */

    public static Address getPublicKeyAddress(PublicKey publicKey) {
        return new Address(getPublicKeyHash(publicKey));
    }


    /******************************************************************************************************
     *  Computes the SHA-256 hash of a public key.
     *
     * @param publicKey the PublicKey to hash
     * @return the Hash of the public key
     */

    private static Hash getPublicKeyHash(PublicKey publicKey) {
        return Hash.compute(publicKey.getEncoded());
    }


    /******************************************************************************************************
     * Encrypts an array of bytes using an RSA public or private key.  This is not needed for the cryptocurrency project.
     *
     * @param plainBytes   The array of bytes containing the data that will be encrypted
     * @param key          The PublicKey or PrivateKey that will be used to perform the encryption
     * @return An array of bytes containing the encrypted data
     */

    public static byte[] encryptRSA(byte[] plainBytes, Key key) {
        try {
            Cipher rsa = Cipher.getInstance("RSA");
            rsa.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = rsa.doFinal(plainBytes);
            return encryptedBytes;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    /******************************************************************************************************
     *  Decrypts an array of bytes using an RSA public or private key.  This is not needed for the cryptocurrency project.
     *
     * @param encryptedBytes    The array of bytes containing the encrypted data
     * @param key               The PublicKey or PrivateKey that will be used to perform the encryption
     * @return An array of bytes containing the decrypted data
     */

    public static byte[] decryptRSA(byte[] encryptedBytes, Key key) {
        try {
            Cipher rsa = Cipher.getInstance("RSA");
            rsa.init(Cipher.DECRYPT_MODE, key);
            byte[] plainBytes = rsa.doFinal(encryptedBytes);
            return plainBytes;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    /******************************************************************************************************
     * Determines the size in bytes of an RSA public key and digital signature given the desired bit size for an RSA key.
     * You do not need to use this function for the cryptocurrency project, unless you want to start a new network
     * with a new RSA key size.  Please do not try different key sizes on the class network.
     *
     * @param bits  THe number of bits in the desired RSA keys
     */

    private static void checkKeySize(int bits) {
        try {
            System.out.println("RSA_KEY_BITS = " + bits);

            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(bits);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            System.out.println("RSA_PUBLIC_KEY_BYTES = " + keyPair.getPublic().getEncoded().length);

            byte[] bytes = new byte[]{3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5, 8, 9, 7, 9, 3, 2, 3, 8, 4, 6, 2, 6, 4, 3, 3};

            Signature signer = Signature.getInstance("SHA256withRSA");
            signer.initSign(keyPair.getPrivate());
            signer.update(bytes);
            byte[] signature = signer.sign();

            System.out.println("RSA_SIGNATURE_BYTES = " + signature.length);

        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }
    }

}
