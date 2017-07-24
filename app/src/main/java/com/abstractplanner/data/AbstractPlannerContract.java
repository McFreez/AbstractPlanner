package com.abstractplanner.data;

import android.provider.BaseColumns;

public class AbstractPlannerContract {

    public static final class AreaEntry implements BaseColumns{

        public static final String TABLE_NAME = "area";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DESCRIPTION = "description";
    }

    public static final class TaskEntry implements BaseColumns{

        public static final String TABLE_NAME = "task";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_AREA_ID = "area_id";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_STATUS = "status";
    }
}
