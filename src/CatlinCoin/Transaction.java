package CatlinCoin;

import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.HexFormat;
import java.util.Objects;

/**
 * A Transaction is the transfer of money from one address to another.  Each Transaction can take money
 * from one or more UXTOs (unspent outputs from prior transactions) and pay it to one or more
 * recipients.
 */

public class Transaction {

    private Currency currency;

    private String sourceNickname;  // the nickname for the keys of the sender of this transaction

    private byte[] bytes;  // the binary representation of the transaction

    private Hash transactionHash;
    private byte[] sourcePublicKeyBytes;
    private byte[] signatureBytes;
    private int coinbase;
    private byte numInputs;
    private byte numOutputs;
    private TxInput[] txInputs;
    private TxOutput[] txOutputs;
    private long fee;

    private byte inputIndex = 0;
    private byte outputIndex = 0;
    private Status status = Status.UNKNOWN;
    private String statusMessage = "";

    /****************************************************************************************************
     *  Constructs a Transaction. This is the constructor that is most commonly used.
     * The sourceNickname should correspond to a public/private key pair stored in the keys folder.
     *
     * After constructing a new Transaction, you need to use addInput and addOutput
     *  to add the needed inputs and outputs.
     *
     * When you are all done building a Transaction, use the upload function
     *   to upload it to the network.
     *
     * @param currency           The currency to use
     * @param sourceNickname     The nickname of the account that is sending the coins
     * @param numInputs          The number of inputs that this Transaction is going to have
     * @param numOutputs         The number of outputs that this Transaction is going to have
     * @param fee                The fee that will be paid to the miner who includes this in a block
     */

    public Transaction(Currency currency, String sourceNickname, int numInputs, int numOutputs, long fee) {
        this.currency = currency;
        this.sourceNickname = sourceNickname;
        this.coinbase = -1;
        this.numInputs = (byte) numInputs;
        this.numOutputs = (byte) numOutputs;
        this.txInputs = new TxInput[numInputs];
        this.txOutputs = new TxOutput[numOutputs];
        this.fee = fee;
    }

    /********************************************************************************************************
     * Constructs a new Transaction from its binary representation.
     * This is only used when downloading Transactions from the network.
     *
     * @param currency the currency to use
     * @param bytes    an array of bytes containing an entire transaction in binary form, as downloaded from the network
     */

    public Transaction(Currency currency, byte[] bytes) {
        this.currency = currency;
        this.bytes = bytes;
        unWrap();
    }

    /*******************************************************************************************************
     *  A static factory function to create a new coinbase transaction for a new block.
     *
     * @param currency        the currency to use
     * @param minerNickname   the nickname for the miner of the block that this coinbase transaction is for
     * @param height          the height of the block that this coinbase transaction is for
     * @param totalFee        the total of all of the fees for all of the transactions in the block
     * @return the new coinbase transaction
     */

    public static Transaction createCoinbaseTransaction(Currency currency, String minerNickname, int height, long totalFee) {

        //TODO 26: construct a new Transaction that is a coinbase transaction with the appropriate output and return it

        Transaction coinbaseTransaction = null;

        return coinbaseTransaction;
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    /*******************************************************************************************************
     * Returns the Hash of the transaction.
     *
     * @return the Hash
     */

    public Hash getHash() {
        return transactionHash;
    }

    /*******************************************************************************************************
     * Returns the fee for the transaction.
     *
     * @return the fee
     */

    public long getFee() {
        return fee;
    }

    /*******************************************************************************************************
     * Returns the status of the transaction.
     *
     * @return the status
     */

    public Status getStatus() {
        return status;
    }

    /*******************************************************************************************************
     * Returns the status message of the transaction, indicating any reasons for bad status
     *
     * @return the status message
     */

    public String getStatusMessage() {
        return statusMessage;
    }

    /*******************************************************************************************************
     * Returns one of the outputs of the transaction.
     *
     * @param coinNum the coin index of the desired output
     * @return the indicated output
     */

    public TxOutput getOutput(int coinNum) {
        return txOutputs[coinNum];
    }

    /*******************************************************************************************************
     * Returns whether this is a coinbase transaction or not.
     *
     * @return True if this is a coinbase transaction, False for regular transactions.
     */

    public boolean isCoinbase() {
        return coinbase >= 0;
    }

    /*******************************************************************************************************
     * Returns the currency used by this transaction
     *
     * @return the currency
     */

    public Currency getCurrency() {
        return currency;
    }

    /*******************************************************************************************************
     * Returns the inputs that feed into this transaction
     *
     * @return the array of TxInputs
     */

    public TxInput[] getInputs() {
        return txInputs;
    }

    /*******************************************************************************************************
     * Returns the outputs that feed out of this transaction
     *
     * @return the array of TxOutputs
     */

    public TxOutput[] getOutputs() {
        return txOutputs;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    /*******************************************************************************************************
     * Adds an input to the transaction.
     *
     * @param sourceTxHash   The Hash of the source transaction that contains the output that is being spent
     * @param sourceCoinNum  The index of the output being spent within the source transaction
     * @param amount         The amount of coins being spent
     */

    public void addInput(Hash sourceTxHash, int sourceCoinNum, long amount) {
        if (inputIndex >= numInputs) throw new IllegalStateException("trying to add too many transaction inputs");
        txInputs[inputIndex++] = new TxInput(sourceTxHash, (byte) sourceCoinNum, amount);
    }

    /*******************************************************************************************************
     * Add an output to the transaction.
     *
     * @param destinationAddress   The Address of the recipient of the coins
     * @param amount               The amount of coins to send to this destination
     */

    public void addOutput(Address destinationAddress, long amount) {
        if (outputIndex >= numOutputs) throw new IllegalStateException("trying to add too many transaction outputs");
        txOutputs[outputIndex] = new TxOutput(outputIndex, destinationAddress, amount);
        outputIndex++;
    }

    /*******************************************************************************************************
     * Uploads this transaction to the network.
     *
     * @return True if the upload was successful, False if the transaction is illegal or the upload failed.
     */

    public boolean upload() {
        wrapUp();
        if (status.isLegal()) {
            if (Network.uploadTransaction(currency, bytes)) {
                System.out.println("transaction upload successful");
                return true;
            }
        }
        return false;
    }

    /*******************************************************************************************************
     * Displays a full description of this transaction on System.out.
     */

    public void display() {
        System.out.println("TRANSACTION: (status " + status + ")");

        if (transactionHash == null)
            System.out.println("   hash: null");
        else
            System.out.println("   hash: " + transactionHash);

        if (sourceNickname != null)
            System.out.println("   source nickname: " + sourceNickname);
        if (sourcePublicKeyBytes != null) {
            System.out.println("   source public key hash: " + new Address(Hash.compute(sourcePublicKeyBytes)));
            System.out.println("   source public key: " + HexFormat.of().formatHex(sourcePublicKeyBytes).toUpperCase());
        }
        if (signatureBytes != null) {
            System.out.println("   signature: " + HexFormat.of().formatHex(signatureBytes).toUpperCase());
        }

        System.out.println("   coinbase: " + coinbase + (isCoinbase() ? "  is a coinbase transaction" : "  is not a coinbase transaction"));

        System.out.println("   inputs: " + numInputs);
        for (int i = 0; i < numInputs; i++)
            System.out.println(txInputs[i]);

        System.out.println("   outputs: " + numOutputs);
        for (int i = 0; i < numOutputs; i++)
            System.out.println(txOutputs[i]);

        System.out.println("   fee: " + fee);
    }

    /*******************************************************************************************************
     * Displays an abbreviated, summary description of this transaction on System.out.
     */

    public void displaySummary() {
        System.out.println("  --- Transaction: " + getHash() + " ---");
        if (coinbase > -1) {
            String destTag = AddressDirectory.getTag(txOutputs[0].destinationAddress);
            System.out.println("    coinbase: " + destTag + " earned " + txOutputs[0].amount + " coins");
        } else {
            String sourceTag = AddressDirectory.getTag(CryptoRSA.getPublicKeyAddress(sourcePublicKeyBytes));
            System.out.println("       " + sourceTag + " paid with fee " + fee);
            for (int i = 0; i < numOutputs; i++) {
                String destTag = AddressDirectory.getTag(txOutputs[i].destinationAddress);
                System.out.println("           " + destTag + " received " + txOutputs[i].amount + " coins");
            }
        }
    }


    /*******************************************************************************************************
     * Converts the transaction into its binary form in the bytes[] array.
     * This is normally called just before uploading a transaction to the network.
     */

    private void wrapUp() {
        for (TxInput txInput : txInputs) {
            if (txInput == null) {
                recordIllegalTransaction("missing inputs");
                return;
            }
        }
        for (TxOutput txOutput : txOutputs) {
            if (txOutput == null) {
                recordIllegalTransaction("missing outputs");
                return;
            }
        }

        int preambleBytes = Hash.BYTE_LENGTH + CryptoRSA.RSA_PUBLIC_KEY_BYTES + CryptoRSA.RSA_SIGNATURE_BYTES;
        int totalBytes = preambleBytes + 4 + 1 + 1 + numInputs * TxInput.BYTE_LENGTH + numOutputs * TxOutput.BYTE_LENGTH + 8;

        bytes = new byte[totalBytes];
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.position(preambleBytes);
        byteBuffer.putInt(coinbase);
        byteBuffer.put(numInputs);
        byteBuffer.put(numOutputs);
        for (TxInput txInput : txInputs) {
            txInput.putBytes(byteBuffer);
        }
        for (TxOutput txOutput : txOutputs) {
            txOutput.putBytes(byteBuffer);
        }
        byteBuffer.putLong(fee);

        // TODO 22: Get the encoded binary form of the source's full (not hashed) public key and put it in the byteBuffer
        KeyPair keyPair = CryptoRSA.readKeyFiles(sourceNickname); // your helper method to read from disk
        PublicKey pubKey = keyPair.getPublic();
        sourcePublicKeyBytes = pubKey.getEncoded();
        byteBuffer.position(Hash.BYTE_LENGTH);
        byteBuffer.put(sourcePublicKeyBytes);

        // TODO 23: Sign the transaction and put the signature into the byteBuffer
        int bStart = Hash.BYTE_LENGTH + CryptoRSA.RSA_PUBLIC_KEY_BYTES + CryptoRSA.RSA_SIGNATURE_BYTES;
        int bLength = bytes.length - bStart;
        signatureBytes = CryptoRSA.sign(bytes, bStart, bLength, keyPair.getPrivate());
        byteBuffer.position(Hash.BYTE_LENGTH + CryptoRSA.RSA_PUBLIC_KEY_BYTES);
        byteBuffer.put(signatureBytes);

        // TODO 24: Compute the transaction hash and put the hash into the byteBuffer
        transactionHash = Hash.compute(bytes, Hash.BYTE_LENGTH, bytes.length - Hash.BYTE_LENGTH);
        byteBuffer.position(0);
        byteBuffer.put(transactionHash.getBytes());

        checkLegality();
    }


    /*******************************************************************************************************
     * Reads the raw binary form in the bytes[] array and sets the Transaction's fields.
     * This is normally called just after downloading a transaction from the network.
     */

    private void unWrap() {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);

        byte[] hashBytes = new byte[Hash.BYTE_LENGTH];
        byteBuffer.get(hashBytes);
        transactionHash = new Hash(hashBytes);

        Hash newHash = Hash.compute(bytes, Hash.BYTE_LENGTH, bytes.length - Hash.BYTE_LENGTH);
        if (!transactionHash.equals(newHash)) {
            System.out.println("transaction unWrap failed due to hash mismatch");
            return;
        }

        sourcePublicKeyBytes = new byte[CryptoRSA.RSA_PUBLIC_KEY_BYTES];
        byteBuffer.get(sourcePublicKeyBytes);

        signatureBytes = new byte[CryptoRSA.RSA_SIGNATURE_BYTES];
        byteBuffer.get(signatureBytes);

        coinbase = byteBuffer.getInt();
        numInputs = byteBuffer.get();
        numOutputs = byteBuffer.get();

        txInputs = new TxInput[numInputs];
        for (int i = 0; i < numInputs; i++) {
            txInputs[i] = new TxInput(byteBuffer);
        }

        txOutputs = new TxOutput[numOutputs];
        for (int i = 0; i < numOutputs; i++) {
            txOutputs[i] = new TxOutput(byteBuffer);
        }

        fee = byteBuffer.getLong();

        checkLegality();
    }


    /*******************************************************************************************************
     * Determines if a Transaction is legal or not, and sets the status field accordingly.
     */

    public void checkLegality() {
        if (status != Status.UNKNOWN)
            return;

        try {

            Hash newHash = Hash.compute(bytes, Hash.BYTE_LENGTH, bytes.length - Hash.BYTE_LENGTH);
            if (!transactionHash.equals(newHash))
                throw new IllegalTransactionException("bad transaction hash");

            PublicKey publicKey = CryptoRSA.readPublicKeyFromData(sourcePublicKeyBytes);

            // TODO 6: verify the digital signature and throw an IllegalTransactionException if the signature fails verification
            byte[] signedData = java.util.Arrays.copyOfRange(bytes, 0, bytes.length - Hash.BYTE_LENGTH);
            if (!CryptoRSA.verifySignature(signedData, signatureBytes, publicKey)) {
                throw new IllegalTransactionException("Invalid digital signature");
            }


            if (numOutputs <= 0) {
                throw new IllegalTransactionException("numOutputs must be greater than zero: " + numOutputs);
            }
            for (int i = 0; i < numOutputs; i++) {
                if (txOutputs[i] == null)
                    throw new IllegalTransactionException("output " + i + " is null");
                if (txOutputs[i].coinIndex != i)
                    throw new IllegalTransactionException("output " + i + " has bad coin number " + txOutputs[i].coinIndex);

                // TODO 7: verify that the amount of money in this output is positive
                if (txOutputs[i].amount <= 0) {
                    throw new IllegalTransactionException("output " + i + " has non-positive amount: " + txOutputs[i].amount);
                }
            }

            if (isCoinbase()) {  // coinbase transaction
                if (numInputs != 0)
                    throw new IllegalTransactionException("coinbase transaction must have numInputs = 0");
                if (fee != 0)
                    throw new IllegalTransactionException("coinbase transaction must have fee = 0");
            } else {  // non-coinbase transactions

                if (numInputs < 1) {
                    throw new IllegalTransactionException("numInputs must be greater than zero: " + numInputs);
                }

                // TODO 8: check that the fee is at least the minimum transaction fee for this currency
                if (fee < currency.getMinimumTransactionFee()) {
                    throw new IllegalTransactionException("Transaction fee too small: " + fee);
                }

                long totalInputAmount = 0;
                for (int i = 0; i < numInputs; i++) {
                    if (txInputs[i] == null)
                        throw new IllegalTransactionException("input " + i + " is null");
                    if (txInputs[i].sourceCoinIndex < 0)
                        throw new IllegalTransactionException("input " + i + " source coin num cannot be negative");
                    if (txInputs[i].amount <= 0)
                        throw new IllegalTransactionException("input " + i + " amount must be positive");
                    totalInputAmount += txInputs[i].amount;
                }

                // TODO 9: verify that the total of the input amounts is equal to
                //   the total output amounts plus the transaction fee
                long totalOutputAmount = 0;
                for (int i = 0; i < numOutputs; i++) {
                    totalOutputAmount += txOutputs[i].amount;
                }

                if (totalInputAmount != totalOutputAmount + fee) {
                    throw new IllegalTransactionException("Input amount " + totalInputAmount +
                            " does not equal output total " + totalOutputAmount + " plus fee " + fee);
                }
            }

            if (status == Status.UNKNOWN)
                status = Status.LEGAL;

        } catch (IllegalTransactionException illegalTransactionException) {
            recordIllegalTransaction(illegalTransactionException.getMessage());
        }
    }

    /*******************************************************************************************************
     * Determines if a non-coinbase Transaction is valid or not, and sets the status field accordingly.
     */

    public void validate() {
        if (status == Status.UNKNOWN)
            throw new IllegalStateException("Must check legality before attempting to validate a transaction");
        if (status == Status.ILLEGAL) return;
        if (status == Status.VALID || status == Status.INVALID)  // in case we tried validating this transaction earlier
            status = Status.LEGAL;

        try {

            BlockChain myBlockChain = currency.getBlockChain();

            if (coinbase != -1)
                throw new InvalidTransactionException("non-coinbase transaction must have coinbase field == -1");


            for (int i = 0; i < numInputs; i++) {

                TxInput theInput = txInputs[i];

                Transaction sourceTransaction = myBlockChain.getTransaction(theInput.sourceTxHash);
                if (sourceTransaction == null)
                    throw new InvalidTransactionException("input " + i + " source transaction not found");

                if (theInput.sourceCoinIndex < 0)
                    throw new InvalidTransactionException("input " + i + " source coin index num must be postive");

                if (theInput.sourceCoinIndex >= sourceTransaction.numOutputs)
                    throw new InvalidTransactionException("input " + i + " source coin index num too large for source transaction");


                // TODO 10: verify that the amount of this input matches its source output amount
                TxOutput sourceOutput = sourceTransaction.txOutputs[theInput.sourceCoinIndex];
                if (theInput.amount != sourceOutput.amount) {
                    throw new InvalidTransactionException("Input " + i + " claims " + theInput.amount + " but referenced output has " + sourceOutput.amount);
                }


                // TODO 11: verify that the public key in the source output matches the public key of the this transaction
                Address sourceOutputAddress = sourceOutput.destinationAddress;
                Address transactionAddress = CryptoRSA.getPublicKeyAddress(sourcePublicKeyBytes);
                if (!Objects.equals(sourceOutputAddress, transactionAddress)) {
                    throw new InvalidTransactionException("Input " + i + " public key does not match the source output's public key.");
                }

                // TODO 12: verify that the Input refers to a UTXO
                if (!myBlockChain.containsUTXO(theInput.sourceTxHash, theInput.sourceCoinIndex)) {
                    throw new InvalidTransactionException("Input " + i + " does not refer to a valid UTXO.");
                }
            }

            status = Status.VALID;

        } catch (InvalidTransactionException invalidTransactionException) {
            recordInvalidTransaction(invalidTransactionException.getMessage());
        }

    }

    /*******************************************************************************************************
     * Determines if a coinbase Transaction is valid or not, and sets the status field accordingly.
     *
     * @param height                    The height of the block containing this coinbase transaction
     * @param totalTransactionFees      The total fees of all of the other transactions in the block
     */

    public void validateCoinbase(int height, long totalTransactionFees) {
        if (status == Status.ILLEGAL) return;

        try {

            if (coinbase != height)
                throw new InvalidTransactionException("coinbase transaction must have coinbase field == height of block");

            if (numInputs != 0)
                throw new InvalidTransactionException("coinbase transaction must have 0 inputs");

            if (fee != 0)
                throw new InvalidTransactionException("coinbase transaction must have 0 fee");

            if (numOutputs != 1)
                throw new InvalidTransactionException("coinbase transaction must have exactly 1 output");


            // TODO 13: verify that the amount of the coinbase transaction is equal the sum of the current mining reward for this currency and the total transaction fees for this block
            long expected = currency.getMiningReward(height) + totalTransactionFees;
            if (txOutputs[0].amount != expected) {
                throw new InvalidTransactionException("Coinbase amount " + txOutputs[0].amount +
                        " does not match expected " + expected);
            }

            status = Status.VALID;

        } catch (InvalidTransactionException invalidTransactionException) {
            recordInvalidTransaction(invalidTransactionException.getMessage());
        }
    }

    /*****************************************************************************************************
     * An Exception to throw when a Transaction is found to be illegal.
     */

    private static class IllegalTransactionException extends Exception {
        IllegalTransactionException(String message) {
            super(message);
        }
    }

    /*****************************************************************************************************
     * An Exception to throw when a Transaction is found to be invalid.
     */

    private static class InvalidTransactionException extends Exception {
        InvalidTransactionException(String message) {
            super(message);
        }
    }

    /*******************************************************************************************************
     * Sets the Transaction's status to ILLEGAL.
     *
     * @param message  The new statusMessage indicating the reason the transaction is illegal
     */

    private void recordIllegalTransaction(String message) {
        System.out.println("Illegal Transaction " + transactionHash + ": " + message);
        status = Status.ILLEGAL;
        statusMessage = message;
    }

    /*******************************************************************************************************
     * Sets the Transaction's status to INVALID.
     *
     * @param message  The new statusMessage indicating the reason the transaction is invalid
     */

    private void recordInvalidTransaction(String message) {
        System.out.println("Invalid Transaction " + transactionHash + ": " + message);
        status = Status.INVALID;
        statusMessage = message;
    }

    //////////////////////////////////////////////////////////////////////////////////////

    /******************************************************************************************************/

    @Override
    public boolean equals(Object other) {
        Transaction otherTransaction = (Transaction) other;
        return this.transactionHash.equals(otherTransaction.transactionHash);
    }

    /******************************************************************************************************/

    @Override
    // so a Transaction can be used as the key in a HashMap or HashSet, if desired
    public int hashCode() {
        return transactionHash.hashCode();
    }

    /******************************************************************************************************/

    @Override
    public String toString() {
        return "Transaction: " + transactionHash;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////


    /********************************************************************************************************
     * A TxInput is one input to a Transaction.
     */

    public class TxInput {

        private Hash sourceTxHash;        // which Transaction is the money coming from?
        private byte sourceCoinIndex;     // which Output in that Transaction
        private long amount;              // how much money

        static private final int BYTE_LENGTH = 32 + 1 + 8;

        /********************************************************************************************************
         * Construct a new Transaction Input.
         *
         * @param sourceTxHash    The Hash of the Transaction that this money is coming from
         * @param sourceCoinNum   The index of the Output coin within that Transaction
         * @param amount          The amount of money being transferred
         */

        private TxInput(Hash sourceTxHash, byte sourceCoinNum, long amount) {
            this.sourceTxHash = sourceTxHash;
            this.sourceCoinIndex = sourceCoinNum;
            this.amount = amount;
        }

        /********************************************************************************************************
         * Constructs a new Transaction Input by reading from a ByteBuffer at its current position.
         *
         * @param byteBuffer  The ByteBuffer that is positioned at the beginning of a Transaction Input
         */

        private TxInput(ByteBuffer byteBuffer) {
            byte[] hashBytes = new byte[Hash.BYTE_LENGTH];
            byteBuffer.get(hashBytes);
            sourceTxHash = new Hash(hashBytes);
            sourceCoinIndex = byteBuffer.get();
            amount = byteBuffer.getLong();
        }

        /********************************************************************************************************
         * Writes the contents of this Transaction Input to the given ByteBuffer at its current position.
         *
         * @param byteBuffer  The ByteBuffer to write to
         */

        private void putBytes(ByteBuffer byteBuffer) {
            byteBuffer.put(sourceTxHash.getBytes());
            byteBuffer.put(sourceCoinIndex);
            byteBuffer.putLong(amount);
        }

        /********************************************************************************************************
         * Returns the Hash of the Transaction that this money is coming from.
         *
         * @return the Hash
         */

        public Hash getSourceTxHash() {
            return sourceTxHash;
        }

        /********************************************************************************************************
         * Returns the index of the Output coin within that Transaction
         *
         * @return the index
         */

        public byte getSourceCoinIndex() {
            return sourceCoinIndex;
        }

        /********************************************************************************************************
         * Returns the amount of money being transferred
         *
         * @return the amount of money
         */

        public long getAmount() {
            return amount;
        }

        /********************************************************************************************************/

        public String toString() {
            return "      tx: " + sourceTxHash + "  coin: " + sourceCoinIndex + "  amount: " + amount;
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /********************************************************************************************************
     * A TxOutput is one output from a Transaction.
     */

    public class TxOutput {

        private byte coinIndex;               // sequential integer identifying this output within the Transaction
        private long amount;                  // how much money
        private Address destinationAddress;   // who is the money going to?

        static private final int BYTE_LENGTH = 1 + 8 + 32;

        /********************************************************************************************************
         * Constructs a new Transaction Output.
         *
         * @param coinIndex              the index of this output within this transaction
         * @param destinationAddress     the address where the money is being transferred to
         * @param amount                 the amount of money being transferred
         */

        private TxOutput(byte coinIndex, Address destinationAddress, long amount) {
            this.coinIndex = coinIndex;
            this.amount = amount;
            this.destinationAddress = destinationAddress;
        }

        /********************************************************************************************************
         * Constructs a new Transaction Output by reading from a ByteBuffer at its current position.
         *
         * @param byteBuffer   The ByteBuffer that is positioned at the beginning of a Transaction Output
         */

        private TxOutput(ByteBuffer byteBuffer) {
            coinIndex = byteBuffer.get();
            amount = byteBuffer.getLong();
            byte[] hashBytes = new byte[Hash.BYTE_LENGTH];
            byteBuffer.get(hashBytes);
            destinationAddress = new Address(hashBytes);
        }

        /********************************************************************************************************
         * Writes the contents of this Transaction Output to the given ByteBuffer at its current position.
         *
         * @param byteBuffer  The ByteBuffer to write to
         */

        private void putBytes(ByteBuffer byteBuffer) {
            byteBuffer.put(coinIndex);
            byteBuffer.putLong(amount);
            byteBuffer.put(destinationAddress.getBytes());
        }

        /********************************************************************************************************
         * Returns the index of this output within the transaction
         *
         * @return the index of the output
         */

        public byte getCoinIndex() {
            return coinIndex;
        }

        /********************************************************************************************************
         * Returns the amount of this output
         *
         * @return the amount
         */

        public long getAmount() {
            return amount;
        }

        /********************************************************************************************************
         * Returns the Address where the coins are going
         *
         * @return the destination address
         */

        public Address getDestinationAddress() {
            return destinationAddress;
        }

        /********************************************************************************************************
         * Sets the amount of the output.  This is only allowed for coinbase transactions in a genesis block.
         *
         * @param amount   The amount that this output should transfer.
         */

        public void setAmount(long amount) {
            if (Transaction.this.coinbase != 0)
                throw new IllegalStateException("setAmount is only allowed for genesis block coinbase transaction");
            this.amount = amount;
        }

        /********************************************************************************************************/

        public String toString() {
            return "      coin: " + coinIndex + "  public key: " + destinationAddress + "  amount: " + amount;
        }
    }

}
