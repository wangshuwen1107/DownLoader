package com.personal.mydownloader.DownLoader;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.personal.mydownloader.db.TaskDao;
import com.personal.mydownloader.net.HttpUtils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import okhttp3.Response;

/**
 * Created by wn_dev on 2016/12/16.
 */

public class DownLoader {
    public static final String TAG=DownLoader.class.getSimpleName();
    /*
    *mark threadCount
     */
    private int threadCount;
    /*
    *the origin downLoad link
     */
    private String urlstr;
    /*
    *mark file size
     */
    private long fileSize;
    /*
    *the inputStream for okHttp response
     */
    RandomAccessFile raf;
    /*
    * Memory sdCard File
     */
    private File saveFile;
    /*
    *mark downLoader state
     */
    private int downLoaderSate;
    /*
    *the lock for sync state
     */
    private Object mLock=new Object();
    public static final int IDLE=2000;
    public static final int PAUSE=2001;
    public static final int DOWNLOADING=2002;
    public static final int FINISH=2003;

    /*
    *netWork error code
     */
    public static final int NET_ERROR=3000;
    /*
    *state error code
     */
    public static final int STATE_ERROR=3001;
    /*
    *downLoader info error code
     */
    public static final int INFO_ERROR=3002;
    /*
   * executorService to execute task
    */
    ScheduledExecutorService executorService;
    /*
  * callBack for external
   */
    private DownLoaderCallBack mCallBack;
    /*
    *target fileName
     */
    private String fileName;
    /*
   *mark current all task CompleteSize
    */
    private long currentCounter;
    /*
    *mark current all task CompleteProgress
     */
    private int currentProgress=0;
    /*
   * Control DB dao
    */
    private TaskDao taskDao;
    /*
    *list save all task
     */
    List <DownTask>downTasks;
    private  Context  context;
    Response response;


    public void registerCallBack(DownLoaderCallBack callback){
        this.mCallBack=callback;
    }

    public int getDownLoaderSate() {
        int flag;
        synchronized (mLock){
            flag=downLoaderSate;
        }
        return flag;
    }

    public void setDownLoaderSate(int downLoaderSate) {
        synchronized (mLock){
            this.downLoaderSate = downLoaderSate;
        }
        Log.i(TAG, "setDownLoaderSate  ="+toStringState(downLoaderSate));
    }

    public String toStringState(int downLoaderSate){
        switch (downLoaderSate){
            case IDLE:
                 return "IDLE";
            case DOWNLOADING:
                return "DOWNLOADING";
            case PAUSE:
                return "PAUSE";
            case FINISH:
                return "FINISH";
            default:
                return "state no such";
        }
    }


    public DownLoader(int threadSize, String urlstr, Context context,DownLoaderCallBack callBack) {
        this.threadCount = threadSize;
        this.urlstr = urlstr;
        this.mCallBack=callBack;
        executorService=Executors.newScheduledThreadPool(threadSize);
        setDownLoaderSate(IDLE);
        currentProgress=0;
        currentCounter=0;
        this.context=context;
        taskDao=TaskDao.getInstance(context);
    }

   //check manager info
    private boolean checkDownLoaderManager(){
        if (threadCount<=0){
            threadCount=1;
        }
        if (TextUtils.isEmpty(urlstr)){
            callBackException("",INFO_ERROR);
            return false;
        }
        return true;
    }
    

    //the interface for start downLoader task
    public void start(){
       new Thread(new Runnable() {
           @Override
           public void run() {
               if (checkDownLoaderManager()){
                   try {
                       //crete New File  in local SdCard
                       fileName = getFileNameForUrl(urlstr);
                       response = HttpUtils.getInstance().sendGetRequest(urlstr);
                       if (response!=null&&response.code()==200){
                           Log.i(TAG, "response code ==200");

                           fileSize = response.body().contentLength();
                           Log.i(TAG, "startDownLoader fileSize="+fileSize);
                           saveFile = new File(Environment.getExternalStorageDirectory(), fileName);
                           if (!saveFile.exists()) {
                               Log.i(TAG, "startDownLoader file is not exists");
                               saveFile.createNewFile();
                               raf = new RandomAccessFile(saveFile, "rwd");
                               raf.setLength(fileSize);
                           }
                           if (taskDao.isFirstDownLoader(urlstr)){
                               //the first downLoader
                               Log.i(TAG, "startDownLoader is first downLoader");
                               startTask(null);
                           }else {
                               Log.i(TAG, "startDownLoader is not first downLoader");
                               List<DownTaskInfo> taskInfos = taskDao.getTaskInfo(urlstr);
                               startTask(taskInfos);
                           }
                       }else {
                           Log.i(TAG, "response code error ");
                           callBackException(fileName,NET_ERROR);
                       }
                   } catch (Exception e) {
                       e.printStackTrace();
                   }finally {
                       closeIO(raf);
                       if (response!=null){
                           response.close();
                       }
                   }
               }
           }
       }).start();

    }

    //begin all task
    private void startTask(List<DownTaskInfo> infos){
        //buildInfo begin task
        List<DownTaskInfo>  downTaskInfos=buildThreadInfo(infos);
        if (getDownLoaderSate()==IDLE){
            if (!isFinish(downTaskInfos)) {
                setDownLoaderSate(DOWNLOADING);
                downTasks=buildDownThread(downTaskInfos);
                callBackStart(fileName);
                for (DownTask task:downTasks) {
                    executorService.submit(task);
                }
            }else {
                Log.i(TAG, "finish return true setSate  finished");
                setDownLoaderSate(FINISH);
                callBackFinish(fileName);
            }
        }
        else {
            Log.i(TAG, "Start runnable state="+getDownLoaderSate()+" is error");
            callBackException(fileName,STATE_ERROR);
        }
    }


    //pause All task
    public void pause(){
        Log.i(TAG, "pause thread is called");
        if (getDownLoaderSate()==DOWNLOADING){
            setDownLoaderSate(PAUSE);
            for(DownTask thread:downTasks){
                thread.pauseThread();
            }
            callBackPause(fileName);
        }else {
            Log.i(TAG, "pause thread currentState="+getDownLoaderSate()+" is error do nothing");
        }
    }

    //the interface resume or task
    public void resume(){
        Log.i(TAG, "resume thread is called ");
        if (getDownLoaderSate()==PAUSE){
            setDownLoaderSate(DOWNLOADING);
            for(DownTask task:downTasks){
                executorService.submit(task);
            }
        }else {
            Log.i(TAG, "resume thread currentState="+getDownLoaderSate()+" is error do nothing");
        }
    }


    //the interface for save task info in db and pause all task
    public void stop(){
        Log.i(TAG, "stop is called");
        if (currentProgress!=1){
            pause();
        }
        if (downTasks!=null&&downTasks.size()>0){
            for (DownTask task:downTasks){
                Log.i(TAG, "stop insert or update info");
                taskDao.addOrUpdateTaskInfo(task.getInfo());
            }
        }
    }

    //通过URL获取文件名字
    private String getFileNameForUrl(String url) {
        String fileName="defalut";
        if (!TextUtils.isEmpty(url)){
             fileName = url.substring(url.lastIndexOf("/")+1, url.length());
        }
        Log.i(TAG, "getFileNameForUrl  fileName="+fileName);
        return fileName;
    }

     //判断任务是否下载完成
    private boolean isFinish(List<DownTaskInfo> infos){
        if (infos ==null|| infos.size()==0){
            Log.i(TAG, "isFinish :downloaderInfo is empty return false");
            return false;
        }
        int completeSize=0;
        for (DownTaskInfo info: infos){
           completeSize+=info.getfileCompleteSize();
        }
        if (completeSize>=fileSize){
            Log.i(TAG, "isFinish completeSize="+completeSize+" fileSize="+fileSize+" return true");
            return true;
        }else {
            Log.i(TAG, "isFinish completeSize="+completeSize+" fileSize="+fileSize+" return flase");
            return false;
        }
    }

   //配置下载任务DownTask
    private List<DownTask> buildDownThread(List<DownTaskInfo> downTaskInfos) {
        List<DownTask> list=null;
        if (downTaskInfos ==null|| downTaskInfos.size()==0){
            Log.i(TAG, "buildDownThread info is empty");
            return list;
        }
        if (list==null){
            list=new ArrayList<>();
        }
        for (DownTaskInfo info: downTaskInfos) {
            DownTask thread = new DownTask(info, context,new DownTask.TaskCallBack() {
                @Override
                public void completed(int completed) {
                    currentCounter+=completed;
                    if ((int) (currentCounter*100/fileSize)-currentProgress>=1||currentProgress==100){
                        if (currentProgress==100){
                            callBackFinish(fileName);
                        }
                        callBackProgress(fileName, (int) (currentCounter*100/fileSize));
                    }
                    currentProgress=(int) (currentCounter*100/fileSize);
//                    Log.i(TAG, "completed currentCounter="+currentCounter+" fileSize="+fileSize);
                }
            });
            list.add(thread);
        }
        return list;
    }

   //初始化配置每个thread信息
    private List<DownTaskInfo> buildThreadInfo(List<DownTaskInfo>infos) {
        if (infos ==null|| infos.size()==0){
            Log.i(TAG, "buildThreadInfo infos is empty");
            infos = new ArrayList<>();
            long range=fileSize/threadCount;
            for (int i = 0; i <threadCount ; i++) {
                long start=i*range;
                long end=0;
                if (i!=threadCount-1){
                     end=(i+1)*range-1;
                }else {
                    end=fileSize;
                }
                DownTaskInfo downTaskInfo = new DownTaskInfo(""+i, start, end,urlstr,0,saveFile.getAbsolutePath());
                infos.add(downTaskInfo);
            }
        }else {
            Log.i(TAG, "buildThreadInfo  infos have element");
        }
        return infos;
    }


    private void callBackStart(String fileName){
        Log.i(TAG, "callBackStart is called");
        if (mCallBack!=null){
            mCallBack.onStart(fileName);
        }
    }

    private void callBackProgress(String fileName,int progress){
        if (mCallBack!=null&&getDownLoaderSate()==DOWNLOADING){
            mCallBack.onProgress(fileName,progress);
        }
    }

    private void callBackFinish(String fileName){
        Log.i(TAG, "callBackFinish  is called");
        if (mCallBack!=null){
            mCallBack.finished(fileName);
        }
    }

    private void callBackException(String fileName,int errorCode){
        Log.i(TAG, "callBackException  is called");
        if (mCallBack!=null){
            mCallBack.exception(fileName,errorCode);
        }
    }

    private void callBackPause(String fileName){
        Log.i(TAG, "callBackPause is called");
        if (mCallBack!=null){
            mCallBack.pause(fileName);
        }
    }

    private void closeIO(Closeable io){
        if (io!=null){
            try {
                io.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
