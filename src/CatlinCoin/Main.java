package CatlinCoin;

import java.security.KeyPair;

public class Main {

    public static void main(String[] args) {

        // TODO 3: Generate a Key Pair and display your Address
        KeyPair keys = CryptoRSA.generateKeyPair("KMA");
        System.out.println(CryptoRSA.getPublicKeyAddress("KMA"));
        // AF2EFB5AF18FF657D37C4494482FC68846755318AE262FE6523AB25E60BF6547 (KMA)

        // TODO 4: Add the PlayCoin Currency
Currency pepeCoin = new Currency("pepeCoin", "000032690B9175CB69445E119D34CA3777A2032BB9037601E18B6AC821FB42FF", 1, 10, "0001");

       // TODO 5: Display all blocks and transactions for PlayCoin
Utility.displayAllBlocks(pepeCoin);
Utility.displayAllTransactions(pepeCoin);


        // TODO 20: Display the block chain for PlayCoin
        BlockChain playCoinChain = pepeCoin.getBlockChain();
        playCoinChain.build();
        playCoinChain.displaySummary();

        // TODO 21: Display everyone's current balances for PlayCoin
        playCoinChain.displayBalances();
        playCoinChain.displaySummary();

    }

}

