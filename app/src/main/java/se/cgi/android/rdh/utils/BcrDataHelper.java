package se.cgi.android.rdh.utils;

/***
 * BcrDataHelper - Class to handle data identifiers
 *
 * @author  Janne Gunnarsson CGI
 *
 */

public final class BcrDataHelper {
    // Data identifiers
    public static final String DID_ARTICLE_NO = "P";      // ARTICLE NUMBER / FBET
    public static final String DID_USER_ID = "5Y";        // FMID
    public static final String DID_FREE_TXT = "9Y";       // FREE TEXT (INSTANCE)
    public static final String DID_MEASURE_CODE = "2W";   // MEASURE CODE
    public static final String DID_INDIVIDUAL_NO = "S";   // INDIVIDUAL NUMBER
    public static final String DID_QUANTITY = "Q";        // QUANTITY

    // Article Number / FBET
    public static boolean isArticleNo(String data) {
        return data.startsWith(DID_ARTICLE_NO);
    }

    // User Id (FMID)
    public static boolean isUserId(String data) {
        return data.startsWith(DID_USER_ID);
    }

    // Free Text (Instance)
    public static boolean isFreeText(String data) {
        return data.startsWith(DID_FREE_TXT);
    }

    // Measure Code
    public static boolean isMeasureCode(String data) {
        return data.startsWith(DID_MEASURE_CODE);
    }

    // Individual Number
    public static boolean isIndividualNo(String data) {
        return data.startsWith(DID_INDIVIDUAL_NO);
    }

    // Quantity
    public static boolean isQuantity(String data) {
        return data.startsWith(DID_QUANTITY);
    }
}

