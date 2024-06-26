package com.application.androcom;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ActivityVideoCall extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback, handleReceiveData {

    private Camera camera;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private UDPServer up;
    private UDPServer upa;
    private ImageView imageView;
    private float mCameraOrientation;
    private AudioCall call;
    private boolean IN_CALL = false;
    private int currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private boolean isCameraOn = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make the activity full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Remove title
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_video_call);

        initializeViews();
        initializeCamera();

        startCallThread();
        startServiceThread();
    }

    private void initializeViews() {
        surfaceView = findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(this);

        imageView = findViewById(R.id.imageView);

        ImageButton endButton = findViewById(R.id.buttonReject);
        ImageButton switchButton = findViewById(R.id.buttonSwitchCam);
        ImageButton offButton = findViewById(R.id.buttonOffCam);
        ImageButton toggleMic = findViewById(R.id.toggleMic);

        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endCall();
                finish();
            }
        });

        toggleMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                call.toggleMic();
                if(call.micCheck()){
                    toggleMic.setImageResource(R.drawable.mike);
                }else{
                    toggleMic.setImageResource(R.drawable.unmute);
                }
            }
        });

        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });

        offButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCameraOn) {
                    releaseCamera();
                } else {
                    initializeCamera();
                }
                isCameraOn = !isCameraOn;
            }
        });
    }

    private void startCallThread() {
        Thread callThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String callDestination = getIntent().getStringExtra("ip");
                InetAddress address;
                try {
                    address = InetAddress.getByName(callDestination);
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
                call = new AudioCall(address);
                IN_CALL = true;
                call.startCall();
            }
        });
        callThread.start();
    }

    private void startServiceThread() {
        Thread serviceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    up = new UDPServer(8804);
                    up.setReceiveCallback(ActivityVideoCall.this);
                    up.start();

                    upa = new UDPServer(8805);
                    upa.setReceiveCallback(new handleReceiveData() {
                        @Override
                        public void handleReceive(byte[] data) {
                            // Handle receiving data here
                        }
                    });
                    upa.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        serviceThread.start();
    }

    private void initializeCamera() {
        releaseCamera();
        camera = Camera.open(currentCameraId);
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.setPreviewCallback(this);
            configureCameraOrientation();
            camera.startPreview();
        } catch (IOException e) {
            camera.release();
            camera = null;
        }
    }

    private void switchCamera() {
        currentCameraId = (currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT)
                ? Camera.CameraInfo.CAMERA_FACING_BACK
                : Camera.CameraInfo.CAMERA_FACING_FRONT;

        initializeCamera();
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Camera.Size previewSize = camera.getParameters().getPreviewSize();
        int[] rgb = decodeYUV420SP(data, previewSize.width, previewSize.height);
        Bitmap bmp = Bitmap.createBitmap(rgb, previewSize.width, previewSize.height, Bitmap.Config.ARGB_8888);

        Bitmap bmpSmall = getSmallBitmap(previewSize, bmp);
        Bitmap bmpSmallRotated = rotateBitmap(bmpSmall, mCameraOrientation);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmpSmallRotated.compress(Bitmap.CompressFormat.WEBP, 100, baos);
//        bmpSmallRotated.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        String callAddress = getIntent().getStringExtra("ip");
        up.sendMsg(baos.toByteArray(), callAddress);
    }

    private Bitmap getSmallBitmap(Camera.Size previewSize, Bitmap bmp) {
        int dimension = 200;
        int smallWidth, smallHeight;

        if (previewSize.width > previewSize.height) {
            smallWidth = dimension;
            smallHeight = dimension * previewSize.height / previewSize.width;
        } else {
            smallHeight = dimension;
            smallWidth = dimension * previewSize.width / previewSize.height;
        }

        return Bitmap.createScaledBitmap(bmp, smallWidth, smallHeight, false);
    }

    private Bitmap rotateBitmap(Bitmap bmp, float orientation) {
        Matrix matrix = new Matrix();
        matrix.postRotate(orientation);
        return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, false);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        initializeCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        configureCameraOrientation();
        camera.startPreview();
    }

    private void configureCameraOrientation() {
        Camera.Parameters parameters = camera.getParameters();
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(currentCameraId, info);

        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = getDegreesFromRotation(rotation);

        int resultA = (currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT)
                ? (360 + 360 - info.orientation - degrees) % 360
                : (info.orientation - degrees + 360) % 360;

        int resultB = (info.orientation + degrees) % 360;

        camera.setDisplayOrientation(resultA);
        parameters.setRotation(resultB);

        mCameraOrientation = resultB;
        camera.setParameters(parameters);
    }

    private int getDegreesFromRotation(int rotation) {
        switch (rotation) {
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
            default:
                return 0;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }

    @Override
    public void handleReceive(final byte[] data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                imageView.setImageBitmap(bitmap);
            }
        });
    }

    private void endCall() {
        if (IN_CALL) {
            call.endCall();
        }
        if (up != null) {
            up.stop();
        }
        if (upa != null) {
            upa.stop();
        }
    }

    public int[] decodeYUV420SP(byte[] yuv420sp, int width, int height) {
        final int frameSize = width * height;
        int rgb[] = new int[width * height];
        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0) y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }
                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);
                if (r < 0) r = 0;
                else if (r > 262143) r = 262143;
                if (g < 0) g = 0;
                else if (g > 262143) g = 262143;
                if (b < 0) b = 0;
                else if (b > 262143) b = 262143;
                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000)
                        | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
        return rgb;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        endCall();
    }
}
