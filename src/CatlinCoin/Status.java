package CatlinCoin;

/**
 * An enumeration whose values represent the different statuses that a Transaction or Block might have.
 */

public enum Status {

    /**
     * Status has not been determined
     */
    UNKNOWN,

    /**
     * Does not meet basic consistency and sanity checks
     */
    ILLEGAL,

    /**
     * Does meet basic tests, but might or might not conflict with other blocks or transactions
     */
    LEGAL,

    /**
     * Legal but conflicts with the rest of the blockchain
     */
    INVALID,

    /**
     * Legal and consistent with all previous blocks/transactions in blockchain
     */
    VALID,

    ;

    /**********************************************************************************************************
     * Determines if a status is VALID.
     *
     * @return true if this status is VALID, and false otherwise
     */

    public boolean isValid() {
        return this == VALID;
    }

    /**********************************************************************************************************
     * Determines if a status is Legal (meaning LEGAL, VALID, or INVALID).
     *
     * @return True if this status indicates a Legal item (which also might be Valid or Invalid).
     */


    public boolean isLegal() {
        return this == LEGAL || this == VALID || this == INVALID;
    }

}
