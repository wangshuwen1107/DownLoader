package com.rokid.mydownloader.db;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.rokid.mydownloader.DownLoader.DownTaskInfo;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by wn_dev on 2016/12/19.
 */

public class TaskDao {

    private Dao<DownTaskInfo, Integer> dao;
    private DownLoaderDBHelper helper;
    private static TaskDao instance;

    private TaskDao(Context context) {
        helper = DownLoaderDBHelper.getInstance(context);
        try {
            dao = helper.getDao(DownTaskInfo.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static TaskDao getInstance(Context context) {
        if (instance == null) {
            synchronized (TaskDao.class) {
                if (instance == null) {
                    instance = new TaskDao(context);
                }
            }
        }
        return instance;
    }

    public boolean isFirstDownLoader(String url){
        try {
            List<DownTaskInfo> infos = dao.queryBuilder().where().eq(DownTaskInfo.URL_STR, url).query();
            if (infos!=null&&infos.size()>=0){
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
        return true;
    }


    public List<DownTaskInfo> getTaskInfo(String url){
        List<DownTaskInfo> infos=null;
        try {
             infos = dao.queryBuilder().where().eq(DownTaskInfo.URL_STR, url).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return infos;
    }


    public void addOrUpdateTaskInfo(DownTaskInfo info){
        try {
             info.setThreadId(info.getUrlStr()+info.getThreadId());
             dao.createOrUpdate(info);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




}
