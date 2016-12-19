package com.rokid.mydownloader.DownLoader;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by wn_dev on 2016/12/16.
 */
@DatabaseTable(tableName = "task_info")
public class DownTaskInfo {

    public static final String START="start";
    public static final String END="end";
    public static final String THREAD_ID="threadId";
    public static final String URL_STR="urlStr";
    public static final String COMPLETE_SIZE="completeSize";
    public static final String SAVE_FILE_PATH="saveFilePath";



    @DatabaseField(columnName =START)
    private long start;   //下载开始的位置

    @DatabaseField(columnName = END)
    private long end;    //下载的结束位置

    @DatabaseField(id = true)
    private String threadId; //线程ID

    @DatabaseField
    private String urlStr;

    @DatabaseField(columnName = COMPLETE_SIZE)
    private  int fileCompleteSize; //已经完成

    @DatabaseField(columnName = SAVE_FILE_PATH)
    private  String saveFilePath;  //SD文件路径

    public DownTaskInfo(String threadId, long start, long end, String urlStr, int fileCompleteSize, String saveFilePath)
    {
        this.threadId=threadId;
        this.start=start;
        this.end=end;
        this.urlStr=urlStr;
        this.fileCompleteSize=fileCompleteSize;
        this.saveFilePath=saveFilePath;
    }


    public DownTaskInfo() {
    }


    public String getSaveFilePath() {
        return saveFilePath;
    }

    public void setSaveFilePath(String saveFilePath) {
        this.saveFilePath = saveFilePath;
    }

    public int getFileCompleteSize() {
        return fileCompleteSize;
    }

    public int getfileCompleteSize() {
        return fileCompleteSize;
    }

    public void setFileCompleteSize(int fileCompleteSize) {
        this.fileCompleteSize = fileCompleteSize;
    }

    public String getUrlStr() {
        return urlStr;
    }

    public void setUrlStr(String urlStr) {
        this.urlStr = urlStr;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }
}
