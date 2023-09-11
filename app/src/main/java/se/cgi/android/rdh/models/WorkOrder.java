package se.cgi.android.rdh.models;

/***
 * WorkOrder - Model class for work order.
 *
 * @author  Janne Gunnarsson CGI
 *
 */
public class WorkOrder {
    public static final int MaxWorkOrderNoLength = 10;
    public static final int MaxWorkOrderNameLength = 50;

    int id;
    String workOrderNo, workOrderName;

    // Constructors
    public WorkOrder() {
    }

    public WorkOrder(int id, String workOrderNo, String workOrderName) {
        this.id = id;
        this.workOrderNo = workOrderNo;
        this.workOrderName = workOrderName;
    }

    // Id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // WorkOrderNo
    public String getWorkOrderNo() {
        return workOrderNo;
    }

    public void setWorkOrderNo(String workOrderNo) throws Exception {
        if (workOrderNo == null || "".equals(workOrderNo)) {
            throw new Exception("Arbetsorder är obligatoriskt");
        }

        workOrderNo = workOrderNo.trim();

        if (workOrderNo.length() <= MaxWorkOrderNoLength) {
            this.workOrderNo = workOrderNo;
        } else {
           throw new Exception("Arbetsorder får bara innehålla 10 tecken");
        }
    }

    // WorkOrderName
    public String getWorkOrderName() {
        return workOrderName;
    }

    public void setWorkOrderName(String workOrderName) throws Exception {
        if (workOrderName == null) {
            workOrderName = "";
        }
        if (!workOrderName.equals("")) {
            workOrderName = workOrderName.trim();
        }
        if (workOrderName.length() <= MaxWorkOrderNameLength) {
            this.workOrderName = workOrderName;
        } else {
            throw new Exception("Namn får bara innehålla 50 tecken");
        }
    }
}
