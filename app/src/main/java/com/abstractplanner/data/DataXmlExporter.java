package com.abstractplanner.data;

import java.io.IOException;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class DataXmlExporter {
    private static final String TAG = "DataXmlExporter";

    private final SQLiteDatabase mDb;
    private XmlBuilder mXmlBuilder;

    public DataXmlExporter(SQLiteDatabase db) {
        this.mDb = db;
    }

    public String getDBContentsInXml(String dbName) throws IOException {
        Log.i(TAG, "exporting database - " + dbName);

        this.mXmlBuilder = new XmlBuilder();
        this.mXmlBuilder.start(dbName);

        // get the tables
        String sql = "select * from sqlite_master";
        Cursor c = this.mDb.rawQuery(sql, null);
        Log.d(TAG, "select * from sqlite_master, cur size " + c.getCount());
        if (c.moveToFirst()) {
            do {
                String tableName = c.getString(c.getColumnIndex("name"));
                Log.d(TAG, "table name " + tableName);

                // skip metadata, sequence, and uidx (unique indexes)
                if (!tableName.equals("android_metadata") && !tableName.equals("sqlite_sequence")
                        && !tableName.startsWith("uidx")) {
                    try {
                        this.exportTable(tableName);
                    } catch (SQLiteException e){
                        Log.e(TAG, "Unable to export table " + tableName + ": " + e.getMessage());
                    }
                }
            } while (c.moveToNext());
        }
        c.close();

        this.mDb.close();
        return this.mXmlBuilder.end();
    }

    private void exportTable(final String tableName) throws IOException {
        XmlBuilder tableBuilder = new XmlBuilder();

        Log.d(TAG, "exporting table - " + tableName);
        tableBuilder.openTable(tableName);
        String sql = "select * from " + tableName;
        Cursor c = this.mDb.rawQuery(sql, null);
        if (c.moveToFirst()) {
            int cols = c.getColumnCount();
            do {
                tableBuilder.openRow();
                for (int i = 0; i < cols; i++) {
                    tableBuilder.addColumn(c.getColumnName(i), c.getString(i));
                }
                tableBuilder.closeRow();
            } while (c.moveToNext());
        }
        c.close();
        tableBuilder.closeTable();

        this.mXmlBuilder.sb.append(tableBuilder.sb.toString());
    }

    class XmlBuilder {
        private static final String OPEN_XML_STANZA = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
        private static final String CLOSE_WITH_TICK = "'>";
        private static final String DB_OPEN = "<database name='";
        private static final String DB_CLOSE = "</database>";
        private static final String TABLE_OPEN = "<table name='";
        private static final String TABLE_CLOSE = "</table>";
        private static final String ROW_OPEN = "<row>";
        private static final String ROW_CLOSE = "</row>";
        private static final String COL_OPEN = "<col name='";
        private static final String COL_CLOSE = "</col>";

        private final StringBuilder sb;

        public XmlBuilder() throws IOException {
            this.sb = new StringBuilder();
        }

        void start(String dbName) {
            this.sb.append(OPEN_XML_STANZA);
            this.sb.append(DB_OPEN + dbName + CLOSE_WITH_TICK);
        }

        String end() throws IOException {
            this.sb.append(DB_CLOSE);
            return this.sb.toString();
        }

        void openTable(String tableName) {
            this.sb.append(TABLE_OPEN + tableName + CLOSE_WITH_TICK);
        }

        void closeTable() {
            this.sb.append(TABLE_CLOSE);
        }

        void openRow() {
            this.sb.append(ROW_OPEN);
        }

        void closeRow() {
            this.sb.append(ROW_CLOSE);
        }

        void addColumn(final String name, final String val) throws IOException {
            this.sb.append(COL_OPEN + name + CLOSE_WITH_TICK + val + COL_CLOSE);
        }
    }
}
