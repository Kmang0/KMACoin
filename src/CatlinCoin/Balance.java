package CatlinCoin;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A Balance can be used to hold a collection of UTXOs and the total amount of money they represent.
 */

public class Balance implements Iterable<UTXO> {

    private long amount;
    private ArrayList<UTXO> utxos;

    /*****************************************************************************************************************
     * Creates a new Balance.
     */

    public Balance() {
        utxos = new ArrayList<>();
        amount = 0;
    }

    /*****************************************************************************************************************
     * Returns the total amount of money in this Balance.
     *
     * @return the total amount of money
     */

    public long getAmount() {
        return amount;
    }

    /*****************************************************************************************************************
     * Adds a UTXO to the balance.
     *
     * @param utxo the UTXO to add
     */

    public void addUTXO(UTXO utxo) {
        utxos.add(utxo);
        this.amount += utxo.getAmount();
    }

    /*****************************************************************************************************************
     * Gets the UTXO at a given index from the balance
     *
     * @param index the index of the desired UTXO
     * @return the UTXO at the given index
     */

    public UTXO getUTXO(int index) {
        return utxos.get(index);
    }

    /*****************************************************************************************************************
     * Gets the number of UTXOs in this balance
     *
     * @return the number of UTXOs
     */

    public int getCountUTXOs() {
        return utxos.size();
    }

    /*****************************************************************************************************************
     * Displays all of the data in this balance
     */

    public void display() {
        System.out.println("BALANCE total amount " + amount);
        for (UTXO utxo : utxos) {
            System.out.println("   " + utxo);
        }
    }

    /*****************************************************************************************************************
     * Returns an iterator that will provide access to all of the UTXOs in the balance
     *
     * @return an iterator for the UTXOs
     */

    @Override
    public Iterator<UTXO> iterator() {
        return utxos.iterator();
    }
}
