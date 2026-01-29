package CatlinCoin;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * A class providing static functions for uploading and downloading both transactions and blocks.
 */

public class Network {

    private static final String API_URL = "https://cs.catlin.edu/catlincoin/api.py";
    private static final String API_KEY = "b5RehS287uowMbN5x1aPkL82NsazqdHH3cvwX";

    private Network() { } // no need to create objects of this class, since all methods are static

    /**********************************************************************************************
     * Downloads all the Transactions from the network for a given currency.
     *
     * @param currency   the desired currency
     * @return a Map from Transaction Hashes to Transactions
     */

    public static Map<Hash, Transaction> downloadAllTransactions(Currency currency) {
        return downloadRecentTransactions(currency, 0);
    }

    /**********************************************************************************************
     * Downloads all the recent Transactions from the network for a given currency.
     *
     * @param currency     the desired currency
     * @param pastSeconds  only transactions created within the last pastSeconds seconds will be downloaded (0 means download them all)
     * @return a Map from Transaction Hashes to Transactions
     */

    public static Map<Hash, Transaction> downloadRecentTransactions(Currency currency, int pastSeconds) {
        HashMap<String, String> args = new HashMap<>();
        args.put("action", "download");
        args.put("item", "T");  // T for Transaction
        args.put("currency", currency.getName());
        if (pastSeconds > 0)
            args.put("recent", "" + pastSeconds);
        List<byte[]> downloadedBytes = download(args);
        int goodTransactionCount = 0;
        int badTransactionCount = 0;
        HashMap<Hash, Transaction> transactions = new HashMap<>();
        for (byte[] bytes : downloadedBytes) {
            Transaction transaction = new Transaction(currency, bytes);
            if (transaction.getStatus().isLegal()) {
                transactions.put(transaction.getHash(), transaction);
                goodTransactionCount++;
            } else {
                badTransactionCount++;
            }
        }
        System.out.println("downloaded " + goodTransactionCount + " legal transactions and ignored " + badTransactionCount + " illegal transactions");
        return transactions;
    }

    /**********************************************************************************************
     * Downloads all the Blocks from the network for a given currency.
     *
     * @param currency   the desired currency
     * @return a Map from Block Hashes to Blocks
     */


    public static Map<Hash, Block> downloadAllBlocks(Currency currency) {
        return downloadRecentBlocks(currency, 0);
    }

    /**********************************************************************************************
     * Downloads all the recent Blocks from the network for a given currency.
     *
     * @param currency     the desired currency
     * @param pastSeconds  only blocks created within the last pastSeconds seconds will be downloaded (0 means download them all)
     * @return a Map from Block Hashes to Blocks
     */

    public static Map<Hash, Block> downloadRecentBlocks(Currency currency, int pastSeconds) {
        Map<String, String> args = new HashMap<>();
        args.put("action", "download");
        args.put("item", "B");  // B for Block
        args.put("currency", currency.getName());
        if (pastSeconds > 0)
            args.put("recent", "" + pastSeconds);
        List<byte[]> downloadedBytes = download(args);
        int goodBlockCount = 0;
        int badBlockCount = 0;
        HashMap<Hash, Block> blocks = new HashMap<>();
        for (byte[] bytes : downloadedBytes) {
            Block block = new Block(currency, bytes);
            if (block.getStatus().isLegal()) {
                blocks.put(block.getHash(), block);
                goodBlockCount++;
            } else {
                badBlockCount++;
            }
        }
        System.out.println("downloaded " + goodBlockCount + " legal blocks and ignored " + badBlockCount + " illegal blocks");
        return blocks;
    }

    /**********************************************************************************************
     * Uploads a transaction to the network.  This should only be called from Transaction.upload.
     *
     * @param currency              the currency of the transaction
     * @param transactionBytes      an array of bytes containing the transaction in binary form, prepared for uploading
     * @return true if the upload was successful, and false otherwise
     */

    public static boolean uploadTransaction(Currency currency, byte[] transactionBytes) {
        HashMap<String, String> args = new HashMap<>();
        args.put("action", "upload");
        args.put("item", "T");  // T for Transaction
        args.put("currency", currency.getName());
        args.put("apikey", API_KEY);
        args.put("data", Base64.getUrlEncoder().encodeToString(transactionBytes));
        return upload(args);
    }

    /**********************************************************************************************
     * Uploads a block to the network.  This should only be called from Block.upload.
     *
     * @param currency        the currency of the block
     * @param blockBytes      an array of bytes containing the block in binary form, prepared for uploading
     * @return true if the upload was successful, and false otherwise
     */

    public static boolean uploadBlock(Currency currency, byte[] blockBytes) {
        HashMap<String, String> args = new HashMap<>();
        args.put("action", "upload");
        args.put("item", "B");  // B for Block
        args.put("currency", currency.getName());
        args.put("apikey", API_KEY);
        args.put("data", Base64.getUrlEncoder().encodeToString(blockBytes));
        return upload(args);
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    /**********************************************************************************************
     * Uploads data to the network server using HTTP POST.
     *
     * @param args  a Map containing the data for the form-encoded payload
     * @return true if the upload succeeds, and false if it does not
     */

    private static boolean upload(Map<String, String> args) {
        HttpURLConnection connection = null;
        try {
            byte[] out = encodeArgs(args).getBytes(StandardCharsets.UTF_8);

            connection = (HttpURLConnection) new URI(API_URL).toURL().openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            connection.setFixedLengthStreamingMode(out.length);

            connection.connect();
            OutputStream os = connection.getOutputStream();
            os.write(out);
            os.close();

            InputStream is = connection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = br.readLine();
            if (line != null && line.equals("OK")) {
                is.close();
                return true;
            }
            while (line != null) {
                System.out.println("Web: " + line);
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) connection.disconnect();
        }
        return false;
    }


    /**********************************************************************************************
     * Downloads a sequence of binary items from the network server.
     * Each item begins with the magic number 123456789 (as a 4-byte integer),
     *   followed by a second 4-byte integer containing the number of bytes in this item.
     *   After these two integers follows the binary data for the item.
     *
     * @param args  a Map containing the URL query parameters for the request
     * @return a LinkedList of arrays of bytes, containing each downloaded item in binary form
     */


    private static LinkedList<byte[]> download(Map<String, String> args) {
        LinkedList<byte[]> downloadedBytes = new LinkedList<>();
        HttpURLConnection connection = null;
        try {
            String url = API_URL + "?" + encodeArgs(args);
            connection = (HttpURLConnection) new URI(url).toURL().openConnection();
            connection.connect();
            DataInputStream dataInputStream = new DataInputStream(connection.getInputStream());
            while (true) {
                int numBytes;
                try {
                    int magicNumber = dataInputStream.readInt();
                    if (magicNumber != 123456789) {
                        printConnection(connection);
                        break;
                    }
                    numBytes = dataInputStream.readInt();
                } catch (EOFException e) {
                    break;
                }
                byte[] bytes = new byte[numBytes];
                dataInputStream.readFully(bytes);
                downloadedBytes.add(bytes);
            }
            connection.disconnect();
            return downloadedBytes;
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException();
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    /**********************************************************************************************
     * Connects to the network server via HTTP GET, without attempting to upload or download any larger data items.
     *
     * @param args  a Map containing the URL query parameters
     */

    private static void connect(Map<String, String> args) {
        HttpURLConnection connection = null;
        try {
            String url = API_URL + "?" + encodeArgs(args);
            connection = (HttpURLConnection) new URI(url).toURL().openConnection();
            connection.connect();
            printConnection(connection);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException();
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    /**********************************************************************************************
     * URL encodes a map of query parameters.
     *
     * @param args  a map of query parameters to encode
     * @return a URL encoded string containing all the query parameters
     */


    private static String encodeArgs(Map<String, String> args) {
        StringJoiner stringJoiner = new StringJoiner("&");
        for (Map.Entry<String, String> entry : args.entrySet())
            stringJoiner.add(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)
                    + "="
                    + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        return stringJoiner.toString();
    }

    /**********************************************************************************************
     * Displays on System.out any data read from an HTTP connection.
     *
     * @param connection the HTTP connection
     */


    private static void printConnection(HttpURLConnection connection) {
        try {
            InputStream is = connection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println("Web: " + line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }
    }

}
