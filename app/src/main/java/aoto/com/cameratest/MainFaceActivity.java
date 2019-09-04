package aoto.com.cameratest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


import java.util.Timer;
import java.util.TimerTask;

public class MainFaceActivity extends AppCompatActivity {

    private static final String TAG = "MainFaceActivityWhy";
    private static final int CAMERA_ID_RGB = 0;
    private static final int CAMERA_ID_INFRARED = 1;
    private static final int CAMERA_STATUS_OPEN = 0X02;
    private static final int CAMERA_STATUS_CLOSE = 0X03;
    private static final int CAMERA_START_OPEN = 0X04;
    private static final int CAMERA_START_CLOSE = 0X05;

    private int mCameraNumbers = 0;
    private EditText time_step_view;

    private Timer testTimer;
    private CameraPreviewView[] mPreviews = null;
    private Camera[] mCameras = null;
    boolean isOpen = false;
    private int openNum = 0;
    private TextView open_num_view;
    private TextView camera_status;

    private CameraPreviewView rgbPreview;
    private CameraPreviewView irPreview;
    //private CameraPreviewView[] previewViewsList;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        open_num_view = findViewById(R.id.tv_num);
        time_step_view = findViewById(R.id.time_step_view);
        camera_status = findViewById(R.id.camera_status);
        rgbPreview=findViewById(R.id.preview_rgb);
        irPreview=findViewById(R.id.preview_infrared);

        mCameraNumbers = Camera.getNumberOfCameras();
        Log.e(TAG, "onCreate: 摄像头数量"+mCameraNumbers );


       // if(checkSelfPermission(Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    start();
                }
            }, 500);

      //  }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @SuppressWarnings("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CAMERA_STATUS_CLOSE:
                    camera_status.setText("Camera状态：关");
                    camera_status.setTextColor(getResources().getColor(R.color.colorAccent));
                    break;
                case CAMERA_STATUS_OPEN:
                    open_num_view.setText("已经开关：" + openNum);
                    camera_status.setText("Camera状态：开");
                    camera_status.setTextColor(getResources().getColor(R.color.colorPrimary));
                    break;
                case CAMERA_START_OPEN:
                    start();
                    break;
                case CAMERA_START_CLOSE:
                    stop();
                    break;
            }
        }
    };

    private void stop() {
        Log.e(TAG, "stop: 资源释放" );
        for (int i = 0; i < mCameraNumbers; i++) {
            mCameras[i].setPreviewCallback(null);
            mCameras[i].stopPreview();
            mCameras[i].release();
        }
        isOpen = false;
        MessageHelper.sendMessage(mHandler,CAMERA_STATUS_CLOSE);
    }

    private void start() {
        mPreviews = new CameraPreviewView[mCameraNumbers];
        mCameras = new Camera[mCameraNumbers];
        for (int i = 0; i < mCameraNumbers; i++) {
            CameraPreviewView previewView = null;
            if (i == CAMERA_ID_RGB) {
                previewView = rgbPreview;
            }
            if (i == CAMERA_ID_INFRARED) {
                previewView = irPreview;
            }
            mPreviews[i] = previewView;
        }

        for (int i = 0; i < mCameraNumbers; i++) {
            try {
                mCameras[i] = Camera.open(i);
                Camera.Parameters parameters=mCameras[i].getParameters();
                for(Camera.Size s:parameters.getSupportedPreviewSizes()){
                    Log.e(TAG, "start: "+s.width+"  "+s.height );
                }

                Log.e(TAG, "相机ID:"+i+" "+mCameras[i] );
                mPreviews[i].setCamera(mCameras[i]);
            } catch (RuntimeException ex) {
                ex.printStackTrace();
                Log.e(TAG, "start: "+ex.getMessage());
            }
        }

        addCameraPreviewCallback();
        openNum++;
        MessageHelper.sendMessage(mHandler,CAMERA_STATUS_OPEN);
        //Log.e(TAG, "start: " + openNum);
        isOpen = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        System.exit(0);
        super.onDestroy();
    }

    private void addCameraPreviewCallback() {
        Log.e(TAG, "addCameraPreviewCallback: " );
        for (int i = 0; i < mCameraNumbers; i++) {
            if (i == CAMERA_ID_RGB && mCameras[i] != null) {
                final Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(i, info);
                mCameras[i].setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        Log.i("MainFaceActivity", "RGB---");
                    }
                });
            }
            if (i == CAMERA_ID_INFRARED && mCameras[i] != null) {
                final Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(i, info);
                mCameras[i].setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        Log.i("MainFaceActivity", "INF---");

                    }
                });
            }
        }
    }

    public void pressTest(View view) {
        openNum = 0;
        MessageHelper.sendMessage(mHandler,CAMERA_STATUS_OPEN);
        if (testTimer != null) {
            testTimer.cancel();
            testTimer = null;
        }
        long step = 10000;
        if (!time_step_view.getText().toString().trim().isEmpty()) {
            step = Long.parseLong(time_step_view.getText().toString().trim());
        }
        testTimer = new Timer();
        testTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isOpen) {
                    MessageHelper.sendMessage(mHandler,CAMERA_START_CLOSE);
                } else {
                    MessageHelper.sendMessage(mHandler,CAMERA_START_OPEN);
                }
            }
        }, 0, step);
    }


    /**
     * 停止测试
     * @param view
     */
    public void stopTest(View view){
        if(testTimer!=null){
            testTimer.cancel();
            testTimer=null;
        }
        stop();
    }


}
