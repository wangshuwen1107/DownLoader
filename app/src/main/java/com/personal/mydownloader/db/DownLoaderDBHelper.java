package com.personal.mydownloader.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.personal.mydownloader.DownLoader.DownTaskInfo;

import java.sql.SQLException;

/**
 * Created by wn_dev on 2016/12/19.
 */

public class DownLoaderDBHelper extends OrmLiteSqliteOpenHelper {
    public static final String DATABASE_NAME = "downloaderDB";
    public static final int DATABASE_VERSION = 1;


    private DownLoaderDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DownLoaderDBHelper instance;

    public static DownLoaderDBHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (DownLoaderDBHelper.class) {
                if (instance == null) {
                    instance = new DownLoaderDBHelper(context);
                }
            }
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTableIfNotExists(connectionSource, DownTaskInfo.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {


    }
}
