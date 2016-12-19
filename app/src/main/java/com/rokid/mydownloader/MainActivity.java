package com.rokid.mydownloader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.rokid.mydownloader.DownLoader.DownLoader;
import com.rokid.mydownloader.DownLoader.DownLoaderCallBack;

public class MainActivity extends AppCompatActivity {
    public static final String url="http://10.88.9.150:8080/weibonews/11.mp4";
    private DownLoader downLoader;
    public static final String TAG=MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        downLoader = new DownLoader(3, url,this, new DownLoaderCallBack() {
            @Override
            public void onStart(String fileName) {
                Log.i(TAG, "onStart fileName="+fileName);
            }

            @Override
            public void onProgress(String fileName, int progress) {
                Log.i(TAG, "onProgress fileName=" +fileName+" progress= "+progress);
            }

            @Override
            public void finished(String fileName) {
                Log.i(TAG, "finished  filename="+fileName);
            }

            @Override
            public void exception(String fileName, int code) {
                Log.i(TAG, "exception filename="+fileName+" code="+code);
            }

            @Override
            public void pause(String fileName) {
                Log.i(TAG, "pause filename="+fileName);
            }
        });
    }

    public void start(View view){
        downLoader.start();
    }

    public void pause(View view){
        downLoader.pause();
    }

    public void resume(View view){
        downLoader.resume();
    }

    public void stop(View view){downLoader.stop();}

}
