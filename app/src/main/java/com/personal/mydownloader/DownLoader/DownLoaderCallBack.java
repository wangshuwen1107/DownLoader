package com.personal.mydownloader.DownLoader;

/**
 * Created by wn_dev on 2016/12/17.
 */

public interface DownLoaderCallBack {
         void onStart(String fileName);
         void onProgress(String fileName,int progress);
         void finished(String fileName);
         void exception(String fileName,int code);
         void pause(String fileName);
}
