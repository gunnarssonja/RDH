package se.cgi.android.rdh.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import se.cgi.android.rdh.models.Trans;
import se.cgi.android.rdh.models.WorkOrder;
import se.cgi.android.rdh.utils.Logger;

/***
 * DatabaseHelper - Datatabase handling class.
 *
 * @author  Janne Gunnarsson CGI
 *
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = DatabaseHelper.class.getSimpleName();
    private static DatabaseHelper sInstance;

    // Database Version
    public static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "rdh.db";

    // New record
    public static final int NEW_RECORD = -1;

    // Table Names
    private static final String TABLE_WORKORDER = "WorkOrder";
    private static final String TABLE_TRANS = "Trans";

    // Tables Column Names and Swedish Headings
    public static final String WORKORDER_KEY_ID = "WorkOrderId";
    public static final String WORKORDER_KEY_WORKORDER_NO = "WorkOrderNo";
    public static final String WORKORDER_KEY_WORKORDER_NAME = "WorkOrderName";

    public static final String TRANS_KEY_ID = "TransId";
    public static final String TRANS_KEY_TRANS_TYPE = "TransType";
    public static final String TRANS_KEY_WORKORDER_ID = "WorkOrderId";
    public static final String TRANS_KEY_WORKORDER_NO = "WorkOrderNo";
    public static final String TRANS_KEY_ARTICLE_NO = "ArticleNo";
    public static final String TRANS_KEY_QUANTITY = "Quantity";
    public static final String TRANS_KEY_DATE_TIME = "DateTime";

    // Table Creation String
    private static final String CREATE_TABLE_WORKORDER = "CREATE TABLE  "
            + TABLE_WORKORDER + "(" + WORKORDER_KEY_ID
            + " INTEGER PRIMARY KEY NOT NULL, "
            + WORKORDER_KEY_WORKORDER_NO + " VARCHAR(10) NOT NULL, "
            + WORKORDER_KEY_WORKORDER_NAME + " VARCHAR(50) NOT NULL); ";

    private static final String CREATE_TABLE_TRANS = "CREATE TABLE "
            + TABLE_TRANS + "(" + TRANS_KEY_ID
            + " INTEGER PRIMARY KEY NOT NULL, "
            + TRANS_KEY_TRANS_TYPE + " VARCHAR(2) NOT NULL, "
            + TRANS_KEY_WORKORDER_ID + " INTEGER, "
            + TRANS_KEY_WORKORDER_NO + " VARCHAR(10), "
            + TRANS_KEY_ARTICLE_NO + " VARCHAR(13), "
            + TRANS_KEY_QUANTITY + " INTEGER, "
            + TRANS_KEY_DATE_TIME + " VARCHAR(14));";

    private static final String CREATE_INDEX_WORKORDER_WORKORDERNO = "CREATE UNIQUE INDEX "
            + "IX_WorkOrder_WorkOrderNo ON WorkOrder (WorkOrderNo);";

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    //public DatabaseHelper(Context context) {
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating database (database is created when getReadableDatabase is called for the first time)
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_WORKORDER);
        db.execSQL(CREATE_TABLE_TRANS);
        db.execSQL(CREATE_INDEX_WORKORDER_WORKORDERNO);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORKORDER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANS);

        // Create new tables
        onCreate(db);
    }

    // Closing database
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    /**
     * Get WorkOrder by id
     */
    public WorkOrder getWorkOrderById(int id) {
        WorkOrder workOrder = new WorkOrder();
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_WORKORDER + " WHERE " + WORKORDER_KEY_ID + " = " + id;
        Logger.d(TAG, selectQuery);

        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor != null) {
            if (cursor.moveToFirst() ) {
                try {
                    workOrder.setId(cursor.getInt(cursor.getColumnIndex(WORKORDER_KEY_ID)));
                    workOrder.setWorkOrderNo(cursor.getString(cursor.getColumnIndex(WORKORDER_KEY_WORKORDER_NO)));
                    workOrder.setWorkOrderName(cursor.getString(cursor.getColumnIndex(WORKORDER_KEY_WORKORDER_NAME)));
                } catch (Exception e) {
                    Logger.e(TAG, e.getMessage());
                }
            }
            cursor.close();
        }
        return workOrder;
    }

    /**
     * Create WorkOrder
     */
    public void createWorkOrder(WorkOrder workOrder) throws Exception {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues cv = new ContentValues(2);
            cv.put(WORKORDER_KEY_WORKORDER_NO, workOrder.getWorkOrderNo());
            cv.put(WORKORDER_KEY_WORKORDER_NAME, workOrder.getWorkOrderName());

            db.insertOrThrow(TABLE_WORKORDER, null, cv);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Logger.e(TAG, "Error: Fel vid skapande av arbetsorder post!");
            throw new Exception("Error: Fel vid skapande av arbetsorder post!");
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Update WorkOrder
     */
    public int updateWorkOrder(WorkOrder workOrder) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(WORKORDER_KEY_ID, String.valueOf(workOrder.getId()));
        values.put(WORKORDER_KEY_WORKORDER_NO, workOrder.getWorkOrderNo());
        values.put(WORKORDER_KEY_WORKORDER_NAME, workOrder.getWorkOrderName());

        return db.update(TABLE_WORKORDER, values, WORKORDER_KEY_ID + " = ?",
                new String[] { String.valueOf(workOrder.getId()) });
    }

    /**
     * Delete WorkOrder by id
     */
    public void deleteWorkOrderById(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_WORKORDER, WORKORDER_KEY_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    /**
     * Delete all WorkOrders
     */
    public void deleteAllWorkOrder() {
        // Opens the database for writing
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_WORKORDER, null, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Logger.e(TAG, "Error: Fel vid borttag av arbetsorder!");
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Get all WorkOrders by returning an arrayList
     */
    public List<WorkOrder> getAllWorkOrderList() {
        List<WorkOrder> workOrderList = new ArrayList<>();

        // Opens the database for reading
        SQLiteDatabase db = this.getReadableDatabase();

        // Query for items from the database and get a cursor back
        Cursor cursor = db.rawQuery("SELECT " +  WORKORDER_KEY_ID + ", "
                + WORKORDER_KEY_WORKORDER_NO + ", "
                + WORKORDER_KEY_WORKORDER_NAME + " FROM "
                + TABLE_WORKORDER, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndex(WORKORDER_KEY_ID));
                    String workOrderNo = cursor.getString(cursor.getColumnIndex(WORKORDER_KEY_WORKORDER_NO));
                    String workOrderName = cursor.getString(cursor.getColumnIndex(WORKORDER_KEY_WORKORDER_NAME));
                    WorkOrder workOrder = new WorkOrder(id, workOrderNo, workOrderName);
                    workOrderList.add(workOrder);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return workOrderList;
    }

    /**
     * Get all WorkOrder's by WorkOrderNo
     */
    public List<WorkOrder> getAllWorkOrderListByWorkOrderNo() {
        List<WorkOrder> workOrderList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT " +  WORKORDER_KEY_ID + ", "
                + WORKORDER_KEY_WORKORDER_NO + ", "
                + WORKORDER_KEY_WORKORDER_NAME + " FROM "
                + TABLE_WORKORDER + " ORDER BY " + WORKORDER_KEY_WORKORDER_NO, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndex(WORKORDER_KEY_ID));
                    String workOrderNo = cursor.getString(cursor.getColumnIndex(WORKORDER_KEY_WORKORDER_NO));
                    String workOrderName = cursor.getString(cursor.getColumnIndex(WORKORDER_KEY_WORKORDER_NAME));

                    WorkOrder workOrder = new WorkOrder(id, workOrderNo, workOrderName);
                    workOrderList.add(workOrder);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return workOrderList;
    }

    /**
     * Get all WorkOrder's by WorkOrderName
     */
    public List<WorkOrder> getAllWorkOrderListByWorkOrderName() {
        List<WorkOrder> workOrderList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT " +  WORKORDER_KEY_ID + ", "
                + WORKORDER_KEY_WORKORDER_NO + ", "
                + WORKORDER_KEY_WORKORDER_NAME + " FROM "
                + TABLE_WORKORDER + " ORDER BY " + WORKORDER_KEY_WORKORDER_NAME, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndex(WORKORDER_KEY_ID));
                    String workOrderNo = cursor.getString(cursor.getColumnIndex(WORKORDER_KEY_WORKORDER_NO));
                    String workOrderName = cursor.getString(cursor.getColumnIndex(WORKORDER_KEY_WORKORDER_NAME));

                    WorkOrder workOrder = new WorkOrder(id, workOrderNo, workOrderName);
                    workOrderList.add(workOrder);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return workOrderList;
    }

    /**
     * Check if WorkOrder exist by ArticleNo
     */
    public boolean checkIfWorkOrderExists(String workOrderNo) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_WORKORDER + " WHERE " + WORKORDER_KEY_WORKORDER_NO + " LIKE '" + workOrderNo + "'";
        Logger.d(TAG, selectQuery);

        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    /**
     * Create Trans
     */
    public void createTrans(Trans trans) {
        // Opens the database for writing
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues(8);
        cv.put(TRANS_KEY_TRANS_TYPE, trans.getTransType());
        cv.put(TRANS_KEY_WORKORDER_ID, trans.getWorkOrderId());
        cv.put(TRANS_KEY_WORKORDER_NO, trans.getWorkOrderNo());
        cv.put(TRANS_KEY_ARTICLE_NO, trans.getArticleNo());
        cv.put(TRANS_KEY_QUANTITY, trans.getQuantity());
        cv.put(TRANS_KEY_DATE_TIME, trans.getDateTime());
        db.insert(TABLE_TRANS, null, cv);
    }


    /**
     * Check if Trans record exist
     */
    public boolean checkIfTransRecordExist(Trans trans) {
        boolean recordExist = false;

        // Opens the database for writing
        SQLiteDatabase db = this.getReadableDatabase();

        // Query for items from the database and get a cursor back
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TRANS + " WHERE "
                + TRANS_KEY_TRANS_TYPE    + " = '" + trans.getTransType()     + "'" + " AND "
                + TRANS_KEY_WORKORDER_NO    + " = '" + trans.getWorkOrderNo() + "'" + " AND "
                + TRANS_KEY_ARTICLE_NO + " = '" + trans.getArticleNo() + "'", null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                recordExist = true;
            }
            cursor.close();
        }
        return recordExist;
    }

    /**
     * Check if Trans record exist with WorkOrderNo
     */
    public boolean checkIfWorkOrderTransExists(String workOrderNo) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_TRANS + " WHERE " + WORKORDER_KEY_WORKORDER_NO + " LIKE '" + workOrderNo + "'";
        Logger.d(TAG, selectQuery);

        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    /**
     * Update Transaction
     */
    public int updateTrans(Trans trans) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TRANS_KEY_TRANS_TYPE, trans.getTransType());
        values.put(TRANS_KEY_WORKORDER_ID, trans.getWorkOrderId());
        values.put(TRANS_KEY_WORKORDER_NO, trans.getWorkOrderNo());
        values.put(TRANS_KEY_ARTICLE_NO, trans.getArticleNo());
        values.put(TRANS_KEY_QUANTITY, trans.getQuantity());
        values.put(TRANS_KEY_DATE_TIME, trans.getDateTime());

        return db.update(TABLE_TRANS, values, TRANS_KEY_ID + " = ?",
                new String[] { String.valueOf(trans.getId()) });
    }

    /**
     * Delete Transaction by id
     */
    public void deleteTransById(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TRANS, TRANS_KEY_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    /**
     * Delete all Trans records
     */
    public void deleteAllTrans() {
        // Opens the database for writing
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_TRANS, null, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Logger.e(TAG, "Error: Fel vid borttag av Transposter!");
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Get number of Trans records
     */
    public long getTransCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, TABLE_TRANS);
        //db.close();
        return count;
    }

    /**
     * Get all Trans by returning an arrayList
     */
    public List<Trans> getAllTransList() {
        List<Trans> transList = new ArrayList<>();

        // Opens the database for reading
        SQLiteDatabase db = this.getReadableDatabase();

        // Query for items from the database and get a cursor back
        Cursor cursor = db.rawQuery("SELECT " +  TRANS_KEY_ID + ", "
                + TRANS_KEY_TRANS_TYPE + ", "
                + TRANS_KEY_WORKORDER_ID + ", "
                + TRANS_KEY_WORKORDER_NO + ", "
                + TRANS_KEY_ARTICLE_NO + ", "
                + TRANS_KEY_QUANTITY + ", "
                + TRANS_KEY_DATE_TIME + " FROM "
                + TABLE_TRANS + " ORDER BY " + TRANS_KEY_ID + " DESC", null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndex(TRANS_KEY_ID));
                    String transType = cursor.getString(cursor.getColumnIndex(TRANS_KEY_TRANS_TYPE));
                    int workOrderId = cursor.getInt(cursor.getColumnIndex(TRANS_KEY_WORKORDER_ID));
                    String workOrderNo = cursor.getString(cursor.getColumnIndex(TRANS_KEY_WORKORDER_NO));
                    String articleNo = cursor.getString(cursor.getColumnIndex(TRANS_KEY_ARTICLE_NO));
                    int quantity = cursor.getInt(cursor.getColumnIndex(TRANS_KEY_QUANTITY));
                    String dataTime = cursor.getString(cursor.getColumnIndex(TRANS_KEY_DATE_TIME));

                    Trans trans = new Trans(id, transType, workOrderId, workOrderNo, articleNo, quantity, dataTime);
                    transList.add(trans);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return transList;
    }

    /**
     * Get all Trans by returning an arrayList
     */
    public List<Trans> getAllTransListByWorkOrderNo() {
        List<Trans> transList = new ArrayList<>();

        // Opens the database for reading
        SQLiteDatabase db = this.getReadableDatabase();

        // Query for items from the database and get a cursor back
        Cursor cursor = db.rawQuery("SELECT " +  TRANS_KEY_ID + ", "
                + TRANS_KEY_TRANS_TYPE + ", "
                + TRANS_KEY_WORKORDER_ID + ", "
                + TRANS_KEY_WORKORDER_NO + ", "
                + TRANS_KEY_ARTICLE_NO + ", "
                + TRANS_KEY_QUANTITY + ", "
                + TRANS_KEY_DATE_TIME + " FROM "
                + TABLE_TRANS + " ORDER BY " + TRANS_KEY_WORKORDER_NO + " ASC", null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndex(TRANS_KEY_ID));
                    String transType = cursor.getString(cursor.getColumnIndex(TRANS_KEY_TRANS_TYPE));
                    int workOrderId = cursor.getInt(cursor.getColumnIndex(TRANS_KEY_WORKORDER_ID));
                    String workOrderNo = cursor.getString(cursor.getColumnIndex(TRANS_KEY_WORKORDER_NO));
                    String articleNo = cursor.getString(cursor.getColumnIndex(TRANS_KEY_ARTICLE_NO));
                    int quantity = cursor.getInt(cursor.getColumnIndex(TRANS_KEY_QUANTITY));
                    String dataTime = cursor.getString(cursor.getColumnIndex(TRANS_KEY_DATE_TIME));

                    Trans trans = new Trans(id, transType, workOrderId, workOrderNo, articleNo, quantity, dataTime);
                    transList.add(trans);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return transList;
    }

    public void beginTransaction() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
    }

    public void endTransaction() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.endTransaction();
    }

    public void setTransactionSuccessful() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.setTransactionSuccessful();
    }
}
