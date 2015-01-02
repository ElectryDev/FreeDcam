package com.troop.freedcam.sonyapi;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;

import com.troop.freedcam.camera.parameters.manual.ISOManualParameter;
import com.troop.freedcam.camera.parameters.manual.ZoomManualParameter;
import com.troop.freedcam.i_camera.AbstractCameraHolder;
import com.troop.freedcam.i_camera.interfaces.I_CameraChangedListner;
import com.troop.freedcam.i_camera.parameters.AbstractManualParameter;
import com.troop.freedcam.sonyapi.modules.I_PictureCallback;
import com.troop.freedcam.sonyapi.parameters.ParameterHandlerSony;
import com.troop.freedcam.sonyapi.parameters.manual.BaseManualParameterSony;
import com.troop.freedcam.sonyapi.parameters.manual.ZoomManualSony;
import com.troop.freedcam.sonyapi.sonystuff.JsonUtils;
import com.troop.freedcam.sonyapi.sonystuff.ServerDevice;
import com.troop.freedcam.sonyapi.sonystuff.SimpleCameraEventObserver;
import com.troop.freedcam.sonyapi.sonystuff.SimpleRemoteApi;
import com.troop.freedcam.sonyapi.sonystuff.SimpleStreamSurfaceView;
import com.troop.freedcam.sonyapi.sonystuff.SonyUtils;
import com.troop.freedcam.ui.MainActivity_v2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by troop on 11.12.2014.
 */
public class CameraHolderSony extends AbstractCameraHolder
{
    final static String TAG = CameraHolderSony.class.getSimpleName();

    MainActivity_v2 context;

    ServerDevice serverDevice;

    private SimpleCameraEventObserver mEventObserver;

    private SimpleCameraEventObserver.ChangeListener mEventListener = new SimpleCameraEventObserver.ChangeListenerTmpl()
    {

        @Override
        public void onShootModeChanged(String shootMode) {

        }

        @Override
        public void onCameraStatusChanged(String status)
        {


        }

        @Override
        public void onTimout() {
            cameraChangedListner.onCameraError("Camera connection timed out");
        }

        @Override
        public void onApiListModified(List<String> apis) {

            synchronized (mAvailableCameraApiSet) {
                mAvailableCameraApiSet.clear();
                for (String api : apis) {
                    mAvailableCameraApiSet.add(api);
                }
                ParameterHandler.SetCameraApiSet(mAvailableCameraApiSet);
                if (!mEventObserver.getLiveviewStatus() //
                        && JsonUtils.isCameraApiAvailable("startLiveview", mAvailableCameraApiSet)) {
                    if (mLiveviewSurface != null && !mLiveviewSurface.isStarted()) {
                        startLiveview();
                    }
                }
                if (JsonUtils.isCameraApiAvailable("actZoom", mAvailableCameraApiSet)) {


                } else {

                }
            }
        }

        @Override
        public void onZoomPositionChanged(int zoomPosition)
        {
            ((ZoomManualSony)ParameterHandler.Zoom).setZoomsHasChanged(zoomPosition);
        }

        @Override
        public void onIsoChanged(int iso)
        {
            ((BaseManualParameterSony)ParameterHandler.ISOManual).currentValueChanged(iso);
        }

        @Override
        public void onIsoValuesChanged(String[] isovals) {
            ((BaseManualParameterSony)ParameterHandler.ISOManual).BackgroundValuesChanged(isovals);
        }

        @Override
        public void onFnumberValuesChanged(String[] fnumbervals) {
            ((BaseManualParameterSony)ParameterHandler.ManualFNumber).BackgroundValuesChanged(fnumbervals);
        }

        @Override
        public void onFnumberChanged(int fnumber) {
            ((BaseManualParameterSony)ParameterHandler.ManualFNumber).currentValueChanged(fnumber);
        }

        @Override
        public void onLiveviewStatusChanged(boolean status) {

        }

        @Override
        public void onStorageIdChanged(String storageId) {

        }




    };

    private SimpleRemoteApi mRemoteApi;

    private final Set<String> mAvailableCameraApiSet = new HashSet<String>();

    private final Set<String> mSupportedApiSet = new HashSet<String>();
    private SimpleStreamSurfaceView mLiveviewSurface;

    public ParameterHandlerSony ParameterHandler;



    public CameraHolderSony(Context context, SimpleStreamSurfaceView simpleStreamSurfaceView, I_CameraChangedListner cameraChangedListner,HandlerThread backGroundThread, Handler backGroundHandler,Handler UIHandler)
    {
        super(cameraChangedListner, backGroundThread ,backGroundHandler, UIHandler);
        this.context = (MainActivity_v2)context;
        this.mLiveviewSurface = simpleStreamSurfaceView;
    }


    public boolean OpenCamera(ServerDevice serverDevice)
    {
        this.serverDevice = serverDevice;
        mRemoteApi = new SimpleRemoteApi(serverDevice);
        ParameterHandler.SetRemoteApi(mRemoteApi);
        mEventObserver = new SimpleCameraEventObserver(context, mRemoteApi);
        mEventObserver.activate();


        StartPreview();
        return false;
    }

    @Override
    public void CloseCamera()
    {
        closeConnection();
    }

    @Override
    public Camera GetCamera() {
        return null;
    }

    @Override
    public int CameraCout() {
        return 0;
    }

    @Override
    public boolean IsRdy() {
        return false;
    }


    @Override
    public void StartPreview()
    {
        prepareOpenConnection();
    }

    @Override
    public void StopPreview()
    {
        stopLiveview();
    }



    private void startLiveview() {
        if (mLiveviewSurface == null) {
            Log.w(TAG, "startLiveview mLiveviewSurface is null.");
            return;
        }
        new Thread() {
            @Override
            public void run() {

                try {
                    JSONObject replyJson = null;
                    replyJson = mRemoteApi.startLiveview();

                    if (!SimpleRemoteApi.isErrorReply(replyJson)) {
                        JSONArray resultsObj = replyJson.getJSONArray("result");
                        if (1 <= resultsObj.length()) {
                            // Obtain liveview URL from the result.
                            final String liveviewUrl = resultsObj.getString(0);
                            context.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    mLiveviewSurface.start(liveviewUrl, //
                                            new SimpleStreamSurfaceView.StreamErrorListener() {

                                                @Override
                                                public void onError(StreamErrorReason reason) {
                                                    stopLiveview();
                                                }
                                            });
                                }
                            });
                        }
                    }
                } catch (IOException e) {
                    Log.w(TAG, "startLiveview IOException: " + e.getMessage());
                } catch (JSONException e) {
                    Log.w(TAG, "startLiveview JSONException: " + e.getMessage());
                }
            }
        }.start();
    }

    private void stopLiveview() {
        new Thread() {
            @Override
            public void run() {
                try {
                    mRemoteApi.stopLiveview();
                } catch (IOException e) {
                    Log.w(TAG, "stopLiveview IOException: " + e.getMessage());

                }
            }
        }.start();
    }

    private void prepareOpenConnection() {
        Log.d(TAG, "prepareToOpenConection() exec");


        new Thread() {

            @Override
            public void run() {
                try {
                    // Get supported API list (Camera API)
                    JSONObject replyJsonCamera = mRemoteApi.getCameraMethodTypes();
                    JsonUtils.loadSupportedApiList(replyJsonCamera, mSupportedApiSet);
                    ParameterHandler.SetSupportedApiSet(mSupportedApiSet);

                    try {
                        // Get supported API list (AvContent API)
                        JSONObject replyJsonAvcontent = mRemoteApi.getAvcontentMethodTypes();
                        JsonUtils.loadSupportedApiList(replyJsonAvcontent, mSupportedApiSet);
                    } catch (IOException e) {
                        Log.d(TAG, "AvContent is not support.");
                    }



                    if (!JsonUtils.isApiSupported("setCameraFunction", mSupportedApiSet)) {

                        // this device does not support setCameraFunction.
                        // No need to check camera status.

                        openConnection();

                    } else {

                        // this device supports setCameraFunction.
                        // after confirmation of camera state, open connection.
                        Log.d(TAG, "this device support set camera function");

                        if (!JsonUtils.isApiSupported("getEvent", mSupportedApiSet)) {
                            Log.e(TAG, "this device is not support getEvent");
                            openConnection();
                            return;
                        }

                        // confirm current camera status
                        String cameraStatus = null;
                        JSONObject replyJson = mRemoteApi.getEvent(false);
                        JSONArray resultsObj = replyJson.getJSONArray("result");
                        JSONObject cameraStatusObj = resultsObj.getJSONObject(1);
                        String type = cameraStatusObj.getString("type");
                        if ("cameraStatus".equals(type)) {
                            cameraStatus = cameraStatusObj.getString("cameraStatus");
                        } else {
                            throw new IOException();
                        }

                        if (SonyUtils.isShootingStatus(cameraStatus)) {
                            Log.d(TAG, "camera function is Remote Shooting.");
                            openConnection();
                        } else {
                            // set Listener
                            startOpenConnectionAfterChangeCameraState();

                            // set Camera function to Remote Shooting
                            replyJson = mRemoteApi.setCameraFunction("Remote Shooting");
                        }
                    }
                } catch (IOException e) {
                    Log.w(TAG, "prepareToStartContentsListMode: IOException: " + e.getMessage());

                } catch (JSONException e) {
                    Log.w(TAG, "prepareToStartContentsListMode: JSONException: " + e.getMessage());

                }
            }
        }.start();
    }

    private void openConnection() {

        mEventObserver.setEventChangeListener(mEventListener);
        new Thread() {

            @Override
            public void run() {
                Log.d(TAG, "openConnection(): exec.");

                try {
                    JSONObject replyJson = null;

                    // getAvailableApiList
                    replyJson = mRemoteApi.getAvailableApiList();
                    JsonUtils.loadAvailableCameraApiList(replyJson, mAvailableCameraApiSet);
                    ParameterHandler.SetCameraApiSet(mAvailableCameraApiSet);

                    // check version of the server device
                    if (JsonUtils.isCameraApiAvailable("getApplicationInfo", mAvailableCameraApiSet)) {
                        Log.d(TAG, "openConnection(): getApplicationInfo()");
                        replyJson = mRemoteApi.getApplicationInfo();

                    } else {
                        // never happens;
                        return;
                    }

                    // startRecMode if necessary.
                    if (JsonUtils.isCameraApiAvailable("startRecMode", mAvailableCameraApiSet)) {
                        Log.d(TAG, "openConnection(): startRecMode()");
                        replyJson = mRemoteApi.startRecMode();

                        // Call again.
                        replyJson = mRemoteApi.getAvailableApiList();
                        JsonUtils.loadAvailableCameraApiList(replyJson, mAvailableCameraApiSet);
                    }

                    // getEvent start
                    if (JsonUtils.isCameraApiAvailable("getEvent", mAvailableCameraApiSet)) {
                        Log.d(TAG, "openConnection(): EventObserver.start()");
                        mEventObserver.start();
                    }

                    // Liveview start
                    if (JsonUtils.isCameraApiAvailable("startLiveview", mAvailableCameraApiSet)) {
                        Log.d(TAG, "openConnection(): LiveviewSurface.start()");
                        startLiveview();
                    }

                    // prepare UIs
                    if (JsonUtils.isCameraApiAvailable("getAvailableShootMode", mAvailableCameraApiSet)) {
                        Log.d(TAG, "openConnection(): prepareShootModeSpinner()");

                        // Note: hide progress bar on title after this calling.
                    }

                    // prepare UIs
                    if (JsonUtils.isCameraApiAvailable("actZoom", mAvailableCameraApiSet)) {
                        Log.d(TAG, "openConnection(): prepareActZoomButtons()");

                    } else {

                    }

                    Log.d(TAG, "openConnection(): completed.");
                } catch (IOException e) {
                    Log.w(TAG, "openConnection : IOException: " + e.getMessage());

                }
            }
        }.start();

    }

    /**
     * Stop monitoring Camera events and close liveview connection.
     */
    private void closeConnection() {

        Log.d(TAG, "closeConnection(): exec.");
        if (mLiveviewSurface == null || mEventObserver == null || mAvailableCameraApiSet == null)
            return;
        // Liveview stop
        Log.d(TAG, "closeConnection(): LiveviewSurface.stop()");
        if (mLiveviewSurface != null) {
            mLiveviewSurface.stop();
            mLiveviewSurface = null;
            stopLiveview();
        }

        // getEvent stop
        Log.d(TAG, "closeConnection(): EventObserver.release()");
        mEventObserver.release();

        // stopRecMode if necessary.
        if (JsonUtils.isCameraApiAvailable("stopRecMode", mAvailableCameraApiSet)) {
            new Thread() {

                @Override
                public void run() {
                    Log.d(TAG, "closeConnection(): stopRecMode()");
                    try {
                        mRemoteApi.stopRecMode();
                    } catch (IOException e) {
                        Log.w(TAG, "closeConnection: IOException: " + e.getMessage());
                    }
                }
            }.start();
        }

        Log.d(TAG, "closeConnection(): completed.");
    }



    private void startOpenConnectionAfterChangeCameraState() {
        Log.d(TAG, "startOpenConectiontAfterChangeCameraState() exec");

        context.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mEventObserver.setEventChangeListener(mEventListener);
                mEventObserver.start();
            }
        });
    }


    public void TakePicture(final I_PictureCallback pictureCallback)
    {
        new Thread() {

            @Override
            public void run() {
                try {
                    JSONObject replyJson = mRemoteApi.actTakePicture();
                    JSONArray resultsObj = replyJson.getJSONArray("result");
                    JSONArray imageUrlsObj = resultsObj.getJSONArray(0);
                    String postImageUrl = null;
                    if (1 <= imageUrlsObj.length()) {
                        postImageUrl = imageUrlsObj.getString(0);
                    }
                    if (postImageUrl == null) {
                        Log.w(TAG, "takeAndFetchPicture: post image URL is null.");

                        return;
                    }
                    // Show progress indicator


                    URL url = new URL(postImageUrl);
                    pictureCallback.onPictureTaken(url);
                    //InputStream istream = new BufferedInputStream(url.openStream());


                } catch (IOException e) {
                    Log.w(TAG, "IOException while closing slicer: " + e.getMessage());

                } catch (JSONException e) {
                    Log.w(TAG, "JSONException while closing slicer");

                } finally {

                }
            }
        }.start();
    }

    public void SetShootMode(final String mode)
    {
        new Thread() {

            @Override
            public void run() {
                try {
                    JSONObject replyJson = mRemoteApi.setShootMode(mode);
                    JSONArray resultsObj = replyJson.getJSONArray("result");
                    int resultCode = resultsObj.getInt(0);
                    if (resultCode == 0) {
                        // Success, but no refresh UI at the point.
                        Log.v(TAG, "setShootMode: success.");
                    } else {
                        Log.w(TAG, "setShootMode: error: " + resultCode);

                    }
                } catch (IOException e) {
                    Log.w(TAG, "setShootMode: IOException: " + e.getMessage());
                } catch (JSONException e) {
                    Log.w(TAG, "setShootMode: JSON format error.");
                }
            }
        }.start();
    }
}
