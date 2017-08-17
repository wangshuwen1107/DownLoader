package com.personal.mydownloader.DownLoader;

/**
 * Created by wn_dev on 2016/12/16.
 */

public class DownThreadState {
    private int state;
    public static final int IDEL=1000;
    public static final int PAUSE=1001;
    public static final int DOWNLOADING=1002;

 private Object mLock=new Object();
    public int getState() {
        int flag;
        synchronized (mLock){
            flag=state;
        }
        return flag;
    }

    public void setState(int state) {
        synchronized (mLock){
         this.state = state;
        }
    }
}
