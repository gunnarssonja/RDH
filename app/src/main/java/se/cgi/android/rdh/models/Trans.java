package se.cgi.android.rdh.models;

public class Trans {
    public static final int MaxTransTypeLength = 2;
    public static final int MaxArticleNoLength = 13;
    public static final int MaxQuantityLength = 9;
    public static final int MaxDateTimeLength = 14;
    public static final int MaxNumberOfTrans = 500;

    private int id, workOrderId, quantity;
    private String transType, workOrderNo,articleNo, dateTime;

    // Constructors
    public Trans() {
    }

    public Trans(int id, String transType, int workOrderId, String workOrderNo, String articleNo, int quantity, String dateTime) {
        this.id = id;
        this.transType = transType;
        this.workOrderId = workOrderId;
        this.workOrderNo = workOrderNo;
        this.articleNo = articleNo;
        this.quantity = quantity;
        this.dateTime = dateTime;
    }

    // Id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // TransType
    public String getTransType() {
        return transType;
    }

    public void setTransType(String transType) {
        this.transType = transType;
    }

    // WorkOrderId
    public int getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(int workOrderId) {
        this.workOrderId = workOrderId;
    }

    // WorkOrderNo
    public String getWorkOrderNo() {
        return workOrderNo;
    }

    public void setWorkOrderNo(String workOrderNo) {
        this.workOrderNo = workOrderNo;
    }

    // ArticleNo
    public String getArticleNo() {
        return articleNo;
    }

    public void setArticleNo(String articleNo) {
        this.articleNo = articleNo;
    }

    // Quantity
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // DateTime
    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public void isFullyPopulated() throws Exception {
        try {
            // Check if null or empty
            if (this.transType == null || this.transType.equals("")) {
                throw new Exception("Transaktionstyp saknas!");
            }
            if (this.workOrderId == 0 || this.workOrderId == -1) {
                throw new Exception("Arbetsorderid saknas!");
            }
            if (this.workOrderNo == null || this.workOrderNo.equals("")) {
                throw new Exception("Arbetsorder saknas!");
            }
            if (this.articleNo == null || this.articleNo.equals("")) {
                throw new Exception("Artikel/Fbet saknas!");
            }
            if (this.quantity == 0 || this.quantity == -1) {
                throw new Exception("Antal saknas!");
            }
            if (this.dateTime == null || this.dateTime.equals("")) {
                throw new Exception("Datum och tid saknas!");
            }
        }
        catch(Exception e) {
            throw new Exception("Fel vid skapandet av transaktionspost:" + "\n" + e.getMessage());
        }
    }
}
