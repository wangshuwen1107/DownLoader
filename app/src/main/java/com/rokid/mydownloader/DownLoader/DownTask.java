package com.rokid.mydownloader.DownLoader;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.rokid.mydownloader.net.HttpUtils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.Response;

class DownTask implements Runnable{

    public static final String TAG=DownTask.class.getSimpleName();
    /*
    * task info for downTask
     */
    private DownTaskInfo info;
    /*
    * inputStream form okHttp response
     */
    private InputStream is;
    /*
    *the mark diff task is running or pause
     */
    private boolean isPause;
    /*
    *the callBack for DownLoader
     */
    private TaskCallBack mCallBack;
    /*
    *the outPutStream form sdCard
     */
    private RandomAccessFile raf;

    Response response;

    interface TaskCallBack{
        void completed(int completed);
    }

    public DownTask(DownTaskInfo downTaskInfo,Context context, TaskCallBack callBack){
        this.info= downTaskInfo;
        this.mCallBack=callBack;
}

    private boolean checkDownThreadInfo(DownTaskInfo downTaskInfo){
        if (downTaskInfo ==null){
            Log.i(TAG, "checkInfo DownTaskInfo is empty ");
            return false;
        }
        if (TextUtils.isEmpty(downTaskInfo.getUrlStr())){
            Log.i(TAG, "checkInfo url is empty");
            return false;
        }
        if (downTaskInfo.getEnd()==0){
            Log.i(TAG, "checkInfo end is 0");
            return false;
        }
        return true;
    }


    @Override
    public void run() {
        if (!checkDownThreadInfo(info)) {
            Log.i(TAG, "checkInfo is not right");
            return;
        }
        try {
            isPause=false;
            response = HttpUtils.getInstance().sendHeaderRequest(info.getStart() + info.getfileCompleteSize(), info.getEnd(), info.getUrlStr());
            if (response.code()==206){
            Log.i(TAG, "response.code==206");
            Log.i(TAG, "thread id="+Thread.currentThread().getId()+"  seek to number "+(info.getStart()+info.getfileCompleteSize()));
            raf = new RandomAccessFile(new File(info.getSaveFilePath()), "rwd");
            raf.seek(info.getStart()+info.getfileCompleteSize());
            is = response.body().byteStream();
            byte[] buffer = new byte[4096];
            int length = -1;
            while ((length = is.read(buffer)) != -1) {
                if   (isPause){
                    Log.i(TAG, "thread id="+Thread.currentThread().getId()+"isPause true shutDown runnable");
                    return;
                }
                raf.write(buffer, 0, length);
                callBackCompleted(length);
                //Real-time updates every task completedSize in meomory
                info.setFileCompleteSize(info.getfileCompleteSize()+length);
            }
                Log.i(TAG, "thread id =" + Thread.currentThread().getId() + " finished");
            }
            } catch (Exception e) {
                e.printStackTrace();
             }finally {
                closeIO(raf);
                closeIO(is);
                if (response!=null){
                   response.close();
                }
        }
    }

    /*
    *the interface for  Downloader pause task
     */
    public void pauseThread(){
        isPause=true;
    }

   /*
   *the callBack for DownLoader count completedSize
    */
    public void callBackCompleted(int completeSize){
        if (mCallBack!=null){
            mCallBack.completed(completeSize);
        }
    }

   /*
   *the function for close inputStream or outPutStream
    */
    private void closeIO(Closeable io){
        if (io!=null){
            try {
                io.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public DownTaskInfo getInfo() {
        return info;
    }

    public void setInfo(DownTaskInfo info) {
        this.info = info;
    }
}



