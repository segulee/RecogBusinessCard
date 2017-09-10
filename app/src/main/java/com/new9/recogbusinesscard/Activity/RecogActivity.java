package com.new9.recogbusinesscard.Activity;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.OCR;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;
import com.new9.recogbusinesscard.R;

import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.contourArea;

/**
 * Created by SEGU on 2017-05-30.
 */

public class RecogActivity extends AppCompatActivity
        implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "opencv";
    private CameraBridgeViewBase mOpenCvCameraView;


    private Mat                    mRgba;
    private Mat                    mIntermediateMat;
    private Mat                    mGray;
    Mat hierarchy;
    private Bitmap mBitmap;
    private VisionServiceClient client;
    private String subscription_key = "82e93b120d714e009eb3264b93cfa732";
    List<MatOfPoint> contours;

    private boolean stop = false;
    private Button takePhoto;

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
    }



    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_recog);



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //퍼미션 상태 확인
            if (!hasPermissions(PERMISSIONS)) {

                //퍼미션 허가 안되어있다면 사용자에게 요청
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }
        takePhoto = (Button) findViewById(R.id.kimchi);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stop = true;
            }
        });
        mOpenCvCameraView = (CameraBridgeViewBase)findViewById(R.id.activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(0); // front-camera(1),  back-camera(0)
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "onResume :: Internal OpenCV library not found.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "onResum :: OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();

        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
        hierarchy = new Mat();

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        if(stop) {
            mRgba = inputFrame.rgba();
            contours = new ArrayList<MatOfPoint>();
            hierarchy = new Mat();
            Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_RGB2GRAY); // RGB -> GRAY
            Imgproc.GaussianBlur(mGray, mIntermediateMat, new Size(3, 3),0);

            Imgproc.Canny(mIntermediateMat, mIntermediateMat, 75, 200);
            Imgproc.findContours(mIntermediateMat, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

            hierarchy.release();

            int maxContourIndex = -1;
            double maxContourarea=0;
            MatOfPoint maxPoints = new MatOfPoint();
            for ( int contourIdx=0; contourIdx < contours.size(); contourIdx++ )
            {
                // Minimum size allowed for consideration
                MatOfPoint2f approxCurve = new MatOfPoint2f();
                MatOfPoint2f contour2f = new MatOfPoint2f( contours.get(contourIdx).toArray() );
                //Processing on mMOP2f1 which is in type MatOfPoint2f
                double approxDistance = Imgproc.arcLength(contour2f, true)*0.02;
                Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);

                //Convert back to MatOfPoint
                MatOfPoint points = new MatOfPoint( approxCurve.toArray() );

                // Get bounding rect of contour
                //Rect rect = Imgproc.boundingRect(points);
                double contourarea = Imgproc.contourArea(contours.get(contourIdx));
                if (contourarea > maxContourarea&&points.toArray().length==4){
                    maxContourarea = contourarea;
                    maxContourIndex = contourIdx;
                    maxPoints = points;
                }

            }

            if(maxPoints.toArray().length !=4||contourArea(contours.get(maxContourIndex))<10000)
                return mRgba;

            int min = 0;
            for (int i = 1; i < 4; i++)
                if (maxPoints.toArray()[i].x + maxPoints.toArray()[i].y < maxPoints.toArray()[min].x + maxPoints.toArray()[min].y)
                    min = i;

            Size size = new Size(mRgba.size().width,mRgba.size().height);
            MatOfPoint2f src = new MatOfPoint2f(
                    maxPoints.toArray()[min%4], // tl
                    maxPoints.toArray()[(min+1)%4], // tr
                    maxPoints.toArray()[(min+2)%4], // br
                    maxPoints.toArray()[(min+3)%4] // bl
            );
            MatOfPoint2f dst = new MatOfPoint2f(
                    new org.opencv.core.Point(0,0), // awt has a Point class too, so needs canonical name here
                    new org.opencv.core.Point(0,mRgba.height()),
                    new org.opencv.core.Point(mRgba.width(),mRgba.height()),
                    new org.opencv.core.Point(mRgba.width(),0)
            );

            Mat perspectiveTransform = Imgproc.getPerspectiveTransform(src, dst);

            Mat dstMat=mGray.clone();

            Imgproc.warpPerspective(mGray, dstMat, perspectiveTransform, size);

            Gson gson = new Gson();
            mBitmap = Bitmap.createBitmap(dstMat.cols(),dstMat.rows(),Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(dstMat,mBitmap);
            // Put the image into an input stream for detection.

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream .toByteArray();

            ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
            Log.d("byteArray",mBitmap.getWidth()+"");
            OCR ocr;
            if (client==null){
                client = new VisionServiceRestClient(subscription_key);
            }
            String result = "";
            try {
                ocr = this.client.recognizeText(inputStream, "ko", true);
                result = gson.toJson(ocr);
            } catch (VisionServiceException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("result", result);

            long addr = dstMat.getNativeObjAddr();
            Intent intent = new Intent();
            intent.putExtra("ADDRESS" ,addr);
            intent.putExtra("RESULT", result);
            setResult(RESULT_OK, intent);
            finish();

            return dstMat;
        }
        Mat matInput = inputFrame.rgba();
        return matInput;

    }


    //여기서부턴 퍼미션 관련 메소드
    static final int PERMISSIONS_REQUEST_CODE = 1000;
    String[] PERMISSIONS  = {"android.permission.CAMERA","android.permission.INTERNET"};


    private boolean hasPermissions(String[] permissions) {
        int result;
        //스트링 배열에 있는 퍼미션들의 허가 상태 여부 확인
        for (String perms : permissions){

            result = ContextCompat.checkSelfPermission(this, perms);

            if (result == PackageManager.PERMISSION_DENIED){
                //허가 안된 퍼미션 발견
                return false;
            }
        }

        //모든 퍼미션이 허가되었음
        return true;
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){

            case PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraPermissionAccepted = grantResults[0]
                            == PackageManager.PERMISSION_GRANTED;
                    boolean internetPermissionAccepted = grantResults[1]
                            == PackageManager.PERMISSION_GRANTED;
                    if (!cameraPermissionAccepted||!internetPermissionAccepted)
                        showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
                }
                break;
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RecogActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id){
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });
        builder.create().show();
    }


}

