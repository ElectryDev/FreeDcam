/*
 *
 *     Copyright (C) 2015 Ingo Fuchs
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * /
 */

package freed.cam.apis.camera1.parameters;

import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import freed.cam.apis.KEYS;
import freed.cam.apis.basecamera.CameraWrapperInterface;
import freed.cam.apis.basecamera.FocusRect;
import freed.cam.apis.basecamera.modules.ModuleChangedEvent;
import freed.cam.apis.basecamera.parameters.AbstractParameterHandler;
import freed.cam.apis.basecamera.parameters.modes.LocationParameter;
import freed.cam.apis.basecamera.parameters.modes.MatrixChooserParameter;
import freed.cam.apis.basecamera.parameters.modes.ModuleParameters;
import freed.cam.apis.camera1.Camera1Fragment;
import freed.cam.apis.camera1.CameraHolder;
import freed.cam.apis.camera1.FocusHandler;
import freed.cam.apis.camera1.parameters.manual.BaseManualParameter;
import freed.cam.apis.camera1.parameters.manual.ExposureManualParameter;
import freed.cam.apis.camera1.parameters.manual.ZoomManualParameter;
import freed.cam.apis.camera1.parameters.manual.focus.BaseFocusManual;
import freed.cam.apis.camera1.parameters.manual.focus.FocusManualHuawei;
import freed.cam.apis.camera1.parameters.manual.htc.FocusManualParameterHTC;
import freed.cam.apis.camera1.parameters.manual.lg.AE_Handler_LGG4;
import freed.cam.apis.camera1.parameters.manual.mtk.AE_Handler_MTK;
import freed.cam.apis.camera1.parameters.manual.mtk.FocusManualMTK;
import freed.cam.apis.camera1.parameters.manual.qcom.BaseISOManual;
import freed.cam.apis.camera1.parameters.manual.qcom.BurstManualParam;
import freed.cam.apis.camera1.parameters.manual.shutter.ExposureTime_MicroSec;
import freed.cam.apis.camera1.parameters.manual.shutter.ExposureTime_MilliSec;
import freed.cam.apis.camera1.parameters.manual.shutter.ShutterManualG2pro;
import freed.cam.apis.camera1.parameters.manual.shutter.ShutterManualKrillin;
import freed.cam.apis.camera1.parameters.manual.shutter.ShutterManualMeizu;
import freed.cam.apis.camera1.parameters.manual.shutter.ShutterManualParameterHTC;
import freed.cam.apis.camera1.parameters.manual.shutter.ShutterManualSony;
import freed.cam.apis.camera1.parameters.manual.whitebalance.BaseCCTManual;
import freed.cam.apis.camera1.parameters.manual.zte.FXManualParameter;
import freed.cam.apis.camera1.parameters.modes.BaseModeParameter;
import freed.cam.apis.camera1.parameters.modes.ExposureLockParameter;
import freed.cam.apis.camera1.parameters.modes.FocusPeakModeParameter;
import freed.cam.apis.camera1.parameters.modes.HDRModeParameter;
import freed.cam.apis.camera1.parameters.modes.NightModeXiaomi;
import freed.cam.apis.camera1.parameters.modes.NightModeZTE;
import freed.cam.apis.camera1.parameters.modes.OpCodeParameter;
import freed.cam.apis.camera1.parameters.modes.PictureFormatHandler;
import freed.cam.apis.camera1.parameters.modes.PictureSizeParameter;
import freed.cam.apis.camera1.parameters.modes.PreviewFpsParameter;
import freed.cam.apis.camera1.parameters.modes.PreviewSizeParameter;
import freed.cam.apis.camera1.parameters.modes.VideoProfilesParameter;
import freed.cam.apis.camera1.parameters.modes.VideoStabilizationParameter;
import freed.cam.apis.camera1.parameters.modes.VirtualLensFilter;
import freed.utils.AppSettingsManager;
import freed.utils.DeviceUtils.Devices;
import freed.utils.StringUtils;
import freed.utils.StringUtils.FileEnding;

import static freed.utils.AppSettingsManager.FRAMEWORK_MTK;
import static freed.utils.AppSettingsManager.SETTING_OrientationHack;
import static freed.utils.AppSettingsManager.SHUTTER_G2PRO;
import static freed.utils.AppSettingsManager.SHUTTER_MTK;
import static freed.utils.AppSettingsManager.SHUTTER_SONY;

/**
 * Created by troop on 17.08.2014.
 * this class handels all camera1 releated parameters.
 */
public class ParametersHandler extends AbstractParameterHandler
{

    private final String TAG = ParametersHandler.class.getSimpleName();

    private Parameters cameraParameters;
    public Parameters getParameters(){return cameraParameters;}

    public ParametersHandler(CameraWrapperInterface cameraUiWrapper)
    {
        super(cameraUiWrapper);
    }

    public void SetParametersToCamera(Parameters params)
    {
        Log.d(TAG, "SetParametersToCam");
        ((CameraHolder) cameraUiWrapper.GetCameraHolder()).SetCameraParameters(params);
    }

    @Override
    protected void SetParameters() {
        ((CameraHolder) cameraUiWrapper.GetCameraHolder()).SetCameraParameters(cameraParameters);
    }

    public void LoadParametersFromCamera()
    {
        cameraParameters = ((CameraHolder) cameraUiWrapper.GetCameraHolder()).GetCameraParameters();
        initParameters();
    }

    private void logParameters(Parameters parameters)
    {
        Log.d(TAG, "Manufactur:" + Build.MANUFACTURER);
        Log.d(TAG, "Model:" + Build.MODEL);
        Log.d(TAG, "Product:" + Build.PRODUCT);
        Log.d(TAG, "OS:" + System.getProperty("os.version"));
        String[] split = parameters.flatten().split(";");
        for(String e : split)
        {
            Log.d(TAG,e);
        }
    }


    /**
     * init and check the parameters used by camera1
     */
    private void initParameters()
    {

        logParameters(cameraParameters);

        //setup first Pictureformat its needed for manual parameters to
        // register their listners there if its postprocessing parameter
        PictureFormat = new PictureFormatHandler(cameraParameters, cameraUiWrapper, this);
        if (appSettingsManager.getDngProfilesMap().size() > 0)
            opcode = new OpCodeParameter(appSettingsManager);
        cameraUiWrapper.GetModuleHandler().addListner((ModuleChangedEvent) PictureFormat);
        AppSettingsManager appS = cameraUiWrapper.GetAppSettingsManager();
        if (appS.pictureSize.isSupported())
            PictureSize = new PictureSizeParameter(cameraParameters, cameraUiWrapper);

        if (appS.focusMode.isSupported()) {
            FocusMode = new BaseModeParameter(cameraParameters, cameraUiWrapper, KEYS.FOCUS_MODE, appS.focusMode.getValues());
            FocusMode.addEventListner(((FocusHandler) cameraUiWrapper.getFocusHandler()).focusModeListner);
        }

        if (appS.whiteBalanceMode.isSupported())
            WhiteBalanceMode = new BaseModeParameter(cameraParameters, cameraUiWrapper, KEYS.WHITEBALANCE, appS.whiteBalanceMode.getValues());

        if (appS.exposureMode.isSupported())
            ExposureMode = new BaseModeParameter(cameraParameters,cameraUiWrapper, appS.exposureMode.getKEY(), appS.exposureMode.getValues());

        if (appS.colorMode.isSupported())
            ColorMode = new BaseModeParameter(cameraParameters,cameraUiWrapper,KEYS.COLOR_EFFECT, appS.colorMode.getValues());

        if (appS.flashMode.isSupported())
            ColorMode = new BaseModeParameter(cameraParameters,cameraUiWrapper,KEYS.FLASH_MODE, appS.focusMode.getValues());

        if (appS.isoMode.isSupported())
            IsoMode = new BaseModeParameter(cameraParameters,cameraUiWrapper,appS.isoMode.getKEY(), appS.isoMode.getValues());

        if (appS.antiBandingMode.isSupported())
            AntiBandingMode = new BaseModeParameter(cameraParameters,cameraUiWrapper, KEYS.ANTIBANDING, appS.antiBandingMode.getValues());

        if (appS.imagePostProcessing.isSupported())
            ImagePostProcessing = new BaseModeParameter(cameraParameters, cameraUiWrapper, KEYS.IMAGEPOSTPROCESSING, appS.imagePostProcessing.getValues());

        if (appS.previewSize.isSupported())
            PreviewSize =  new PreviewSizeParameter(cameraParameters,cameraUiWrapper,"preview-size", appS.pictureSize.getValues());

        if (appS.jpegQuality.isSupported())
            JpegQuality = new BaseModeParameter(cameraParameters,cameraUiWrapper,KEYS.JPEG, appS.jpegQuality.getValues());

        if (appS.aeBracket.isSupported())
            AE_Bracket = new BaseModeParameter(cameraParameters, cameraUiWrapper, KEYS.AE_BRACKET_HDR, appS.aeBracket.getValues());

        if (appS.previewFps.isSupported())
            PreviewFPS = new PreviewFpsParameter(cameraParameters,cameraUiWrapper,KEYS.PREVIEW_FRAME_RATE,appS.previewFps.getValues());

        if (appS.previewFormat.isSupported())
            PreviewFormat = new BaseModeParameter(cameraParameters,cameraUiWrapper,"preview-format", appS.previewFormat.getValues());

        if (appS.sceneMode.isSupported())
            SceneMode = new BaseModeParameter(cameraParameters,cameraUiWrapper,KEYS.SCENE_MODE,appS.sceneMode.getValues());

        if (appS.redEyeMode.isSupported())
            RedEye = new BaseModeParameter(cameraParameters,cameraUiWrapper,KEYS.REDEYE_REDUCTION, appS.redEyeMode.getValues());

        if (appS.lenshade.isSupported())
            LensShade = new BaseModeParameter(cameraParameters,cameraUiWrapper,KEYS.LENSSHADE,appS.lenshade.getValues());

        if (appS.zeroshutterlag.isSupported())
            ZSL = new BaseModeParameter(cameraParameters,cameraUiWrapper,appS.zeroshutterlag.getKEY(), appS.zeroshutterlag.getValues());

        if (appS.sceneDetectMode.isSupported())
            SceneDetect = new BaseModeParameter(cameraParameters,cameraUiWrapper, KEYS.SCENE_DETECT, appS.sceneDetectMode.getValues());

        if (appS.memoryColorEnhancement.isSupported())
            MemoryColorEnhancement = new BaseModeParameter(cameraParameters,cameraUiWrapper,KEYS.MEMORYCOLORENHANCEMENT, appS.memoryColorEnhancement.getValues());

        if (appS.videoSize.isSupported())
            VideoSize = new BaseModeParameter(cameraParameters,cameraUiWrapper,"video-size",appS.videoSize.getValues());

        if (appS.correlatedDoubleSampling.isSupported())
            CDS_Mode = new BaseModeParameter(cameraParameters,cameraUiWrapper,"cds-mode", appS.correlatedDoubleSampling.getValues());

        if (appS.opticalImageStabilisation.isSupported())
            oismode = new BaseModeParameter(cameraParameters,cameraUiWrapper,appS.opticalImageStabilisation.getKEY(),appS.opticalImageStabilisation.getValues());

        if (appS.videoHDR.isSupported())
            VideoHDR = new BaseModeParameter(cameraParameters,cameraUiWrapper,appS.videoHDR.getKEY(),appS.videoHDR.getValues());

        if (appS.videoHFR.isSupported())
            VideoHighFramerateVideo = new BaseModeParameter(cameraParameters,cameraUiWrapper,appS.videoHFR.getKEY(),appS.videoHFR.getValues());


        if (appSettingsManager.manualFocus.isSupported())
        {
            if (appSettingsManager.getFrameWork() == FRAMEWORK_MTK)
            {
                ManualFocus = new FocusManualMTK(cameraParameters, cameraUiWrapper,appS.manualFocus);
            }
            else
            {
                //htc mf
                if (appSettingsManager.manualFocus.getKEY().equals(KEYS.FOCUS))
                    ManualFocus = new FocusManualParameterHTC(cameraParameters,cameraUiWrapper);
                    //huawai mf
                else if (appS.manualFocus.getKEY().equals(KEYS.HW_MANUAL_FOCUS_STEP_VALUE))
                    ManualFocus = new FocusManualHuawei(cameraParameters, cameraUiWrapper, appS.manualFocus);
                    //qcom
                else
                    ManualFocus = new BaseFocusManual(cameraParameters,cameraUiWrapper,appS.manualFocus);
            }

        }

        if (appS.manualSaturation.isSupported()) {
            ManualSaturation = new BaseManualParameter(cameraParameters, cameraUiWrapper, appS.manualSaturation);
        }

        if (appS.manualSharpness.isSupported())
            ManualSharpness = new BaseManualParameter(cameraParameters,cameraUiWrapper,appS.manualSharpness);

        if (appS.manualBrightness.isSupported())
            ManualBrightness = new BaseManualParameter(cameraParameters,cameraUiWrapper,appS.manualBrightness);

        if(appS.manualContrast.isSupported())
            ManualContrast = new BaseManualParameter(cameraParameters,cameraUiWrapper,appS.manualContrast);

        if (appS.getDngProfilesMap().size() > 0)
            matrixChooser = new MatrixChooserParameter(cameraUiWrapper.GetAppSettingsManager().getMatrixesMap());

        if (appS.digitalImageStabilisationMode.isSupported())
            DigitalImageStabilization = new BaseModeParameter(cameraParameters,cameraUiWrapper,appS.digitalImageStabilisationMode.getKEY(),appS.digitalImageStabilisationMode.getValues());

        if(appS.denoiseMode.isSupported())
            Denoise = new BaseModeParameter(cameraParameters,cameraUiWrapper,appS.denoiseMode.getKEY(),appS.denoiseMode.getValues());

        if(appS.nonZslManualMode.isSupported())
            NonZslManualMode = new BaseModeParameter(cameraParameters,cameraUiWrapper,appS.nonZslManualMode.getKEY(),appS.nonZslManualMode.getValues());

        if (appS.virtualLensfilter.isSupported())
            LensFilter = new VirtualLensFilter(cameraParameters,cameraUiWrapper);

        if (appS.manualExposureTime.isSupported())
        {
            if (appS.manualExposureTime.getType() == AppSettingsManager.SHUTTER_HTC)
                ManualShutter = new ShutterManualParameterHTC(cameraParameters,cameraUiWrapper);
            else if(appS.manualExposureTime.getType() == AppSettingsManager.SHUTTER_QCOM_MICORSEC)
                ManualShutter = new ExposureTime_MicroSec(cameraUiWrapper,cameraParameters);
            else if (appS.manualExposureTime.getType() == AppSettingsManager.SHUTTER_QCOM_MILLISEC)
                ManualShutter = new ExposureTime_MilliSec(cameraUiWrapper,cameraParameters);
            else if (appS.manualExposureTime.getType() == SHUTTER_MTK)
            {
                AE_Handler_MTK ae_handler_mtk = new AE_Handler_MTK(cameraParameters,cameraUiWrapper,1600);
                ManualShutter = ae_handler_mtk.getShutterManual();
                ManualIso = ae_handler_mtk.getManualIso();
            }
            else if (appS.manualExposureTime.getType() == AppSettingsManager.SHUTTER_LG)
            {
                AE_Handler_LGG4 ae_handler_lgg4 = new AE_Handler_LGG4(cameraParameters,cameraUiWrapper);
                ManualShutter = ae_handler_lgg4.getShutterManual();
                ManualIso = ae_handler_lgg4.getManualIso();
            }
            else if (appS.manualExposureTime.getType() == AppSettingsManager.SHUTTER_MEIZU)
                ManualShutter = new ShutterManualMeizu(cameraParameters,cameraUiWrapper);
            else if (appS.manualExposureTime.getType() == AppSettingsManager.SHUTTER_KRILLIN)
                ManualShutter = new ShutterManualKrillin(cameraParameters,cameraUiWrapper);
            else if (appS.manualExposureTime.getType() == SHUTTER_SONY)
                ManualShutter = new ShutterManualSony(cameraParameters,cameraUiWrapper);
            else if (appS.manualExposureTime.getType() == SHUTTER_G2PRO)
                ManualShutter = new ShutterManualG2pro(cameraParameters,cameraUiWrapper);
        }

        //mtk and g4 aehandler set it already
        if (appS.manualIso.isSupported() && ManualIso == null)
        {
            ManualIso = new BaseISOManual(cameraParameters,cameraUiWrapper);
        }

        if (appS.manualWhiteBalance.isSupported())
            CCT = new BaseCCTManual(cameraParameters,cameraUiWrapper);

        if (appS.nightMode.isSupported()) {
            switch (appSettingsManager.getDevice()) {
                case XiaomiMI3W:
                case XiaomiMI4C:
                case XiaomiMI4W:
                case XiaomiMI_Note_Pro:
                case Xiaomi_RedmiNote:
                    NightMode = new NightModeXiaomi(cameraParameters, cameraUiWrapper);
                break;

                case ZTE_ADV:
                case ZTEADVIMX214:
                case ZTEADV234:
                case ZTE_Z5SMINI:
                case ZTE_Z11:
                    NightMode = new NightModeZTE(cameraParameters, cameraUiWrapper);
                break;
            }
        }

        switch (appS.getDevice())
        {
            case XiaomiMI5:
            case XiaomiMI5s:
                break;
            default:
                HDRMode = new HDRModeParameter(cameraParameters, cameraUiWrapper);
                VideoStabilization = new VideoStabilizationParameter(cameraParameters,cameraUiWrapper);
                break;
        }


        VideoProfiles = new VideoProfilesParameter(cameraUiWrapper);


        locationParameter = new LocationParameter(cameraUiWrapper);

        ManualConvergence = new BaseManualParameter(cameraParameters, KEYS.MANUAL_CONVERGENCE, KEYS.SUPPORTED_MANUAL_CONVERGENCE_MAX, KEYS.SUPPORTED_MANUAL_CONVERGENCE_MIN, cameraUiWrapper,1);

        ManualExposure = new ExposureManualParameter(cameraParameters, cameraUiWrapper,1);

        FX = new FXManualParameter(cameraParameters, cameraUiWrapper);
        PictureFormat.addEventListner(((BaseManualParameter) FX).GetPicFormatListner());
        cameraUiWrapper.GetModuleHandler().addListner(((BaseManualParameter) FX).GetModuleListner());

        Burst = new BurstManualParam(cameraParameters, cameraUiWrapper);
        cameraUiWrapper.GetModuleHandler().addListner(((BaseManualParameter) Burst).GetModuleListner());

        Zoom = new ZoomManualParameter(cameraParameters, cameraUiWrapper);

        ExposureLock = new ExposureLockParameter(cameraParameters, cameraUiWrapper);

        Focuspeak = new FocusPeakModeParameter(cameraUiWrapper,((Camera1Fragment) cameraUiWrapper).focusPeakProcessorAp1);

        SetCameraRotation();

        SetPictureOrientation(0);

        Module = new ModuleParameters(cameraUiWrapper, appSettingsManager);


        //set last used settings
        SetAppSettingsToParameters();

        cameraUiWrapper.GetModuleHandler().SetModule(appSettingsManager.GetCurrentModule());
    }

    @Override
    public void SetMeterAREA(final FocusRect meteringAreas)
    {
        if(appSettingsManager.getDevice() == Devices.ZTE_ADV || appSettingsManager.getDevice() == Devices.ZTEADV234 || appSettingsManager.getDevice() == Devices.ZTEADVIMX214)
        {
            cameraParameters.set("touch-aec","on");
            cameraParameters.set("selectable-zone-af","spot-metering");
            cameraParameters.set("raw-size","4208x3120");
            cameraParameters.set("touch-index-aec", meteringAreas.x + "," + meteringAreas.y);
            SetParametersToCamera(cameraParameters);
        }
    }

    @Override
    public void SetFocusAREA(FocusRect focusAreas)
    {
        if (appSettingsManager.useQcomFocus())
            setQcomFocus(focusAreas);
        else
            setAndroidFocus(focusAreas);
    }

    private void setQcomFocus(FocusRect focusAreas)
    {
        cameraParameters.set("touch-aec", "on");
        cameraParameters.set("touch-index-af", focusAreas.x + "," + focusAreas.y);
        SetParametersToCamera(cameraParameters);
    }

    private void setAndroidFocus(FocusRect focusAreas)
    {
        if (focusAreas != null) {
            List<Camera.Area> l = new ArrayList<>();
            l.add(new Camera.Area(new Rect(focusAreas.left, focusAreas.top, focusAreas.right, focusAreas.bottom), 1000));
            cameraParameters.setFocusAreas(l);
        }
        else
            cameraParameters.setFocusAreas(null);
        SetParametersToCamera(cameraParameters);
    }

    @Override
    public void SetPictureOrientation(int orientation)
    {
        if (appSettingsManager.getApiString(SETTING_OrientationHack).equals(KEYS.ON))
        {
            int or = orientation +180;
            if (or >360)
                or = or - 360;
            orientation = or;
        }

        cameraParameters.setRotation(orientation);
        SetParametersToCamera(cameraParameters);

    }

    @Override
    public float[] getFocusDistances() {
        float focusdistance[] = new float[3];
        ((CameraHolder)cameraUiWrapper.GetCameraHolder()).GetCameraParameters().getFocusDistances(focusdistance);
        return focusdistance;
    }

    @Override
    public long getCurrentExposuretime()
    {
        Camera.Parameters parameters = ((CameraHolder) cameraUiWrapper.GetCameraHolder()).GetCameraParameters();
        if (appSettingsManager.getFrameWork() == AppSettingsManager.FRAMEWORK_MTK) {
            if (parameters.get(KEYS.CUR_EXPOSURE_TIME_MTK) != null) {
                if (Long.parseLong(parameters.get(KEYS.CUR_EXPOSURE_TIME_MTK)) == 0) {
                    return 0;
                } else
                    return Long.parseLong(parameters.get(KEYS.CUR_EXPOSURE_TIME_MTK));
            } else if (parameters.get(KEYS.CUR_EXPOSURE_TIME_MTK1) != null) {
                if (Long.parseLong(parameters.get(KEYS.CUR_EXPOSURE_TIME_MTK1)) == 0) {
                    return 0;
                } else
                    return Long.parseLong(parameters.get(KEYS.CUR_EXPOSURE_TIME_MTK1));
            } else
                return 0;
        }
        else
        {
            if (parameters.get(KEYS.CUR_EXPOSURE_TIME)!= null)
                return (long)Float.parseFloat(parameters.get(KEYS.CUR_EXPOSURE_TIME))*1000000;
        }
        return 0;
    }

    @Override
    public int getCurrentIso() {
        Camera.Parameters parameters = ((CameraHolder) cameraUiWrapper.GetCameraHolder()).GetCameraParameters();
        if (appSettingsManager.getFrameWork() == FRAMEWORK_MTK)
        {
            if(parameters.get(KEYS.CUR_ISO_MTK)!= null) {
                if (Integer.parseInt(parameters.get(KEYS.CUR_ISO_MTK)) == 0) {
                    return 0;
                }
                return Integer.parseInt(parameters.get(KEYS.CUR_ISO_MTK)) / 256 * 100;
            }
            else if(parameters.get(KEYS.CUR_ISO_MTK2)!= null)
            {
                if (Integer.parseInt(parameters.get(KEYS.CUR_ISO_MTK2)) == 0) {
                    return 0;
                }
                return Integer.parseInt(parameters.get(KEYS.CUR_ISO_MTK2)) / 256 * 100;
            }
            else
                return 0;
        }
        else
        {
            if (parameters.get(KEYS.CUR_ISO)!= null)
                return Integer.parseInt(parameters.get(KEYS.CUR_ISO));
        }
        return 0;
    }

    public float getFnumber()
    {
        if (cameraParameters.get("f-number")!= null) {
            return Float.parseFloat(cameraParameters.get("f-number"));
        }
        else
            return 2.0f;
    }

    public float getFocal()
    {
        return cameraParameters.getFocalLength();
    }

    public void SetCameraRotation()
    {
        if (appSettingsManager.getApiString(SETTING_OrientationHack).equals(""))
        {
            appSettingsManager.setApiString(SETTING_OrientationHack , KEYS.OFF);
        }
        if (appSettingsManager.getApiString(SETTING_OrientationHack).equals(KEYS.OFF))
            ((CameraHolder) cameraUiWrapper.GetCameraHolder()).SetCameraRotation(0);
        else
            ((CameraHolder) cameraUiWrapper.GetCameraHolder()).SetCameraRotation(180);
    }

    public void SetupMTK()    {


        cameraParameters.set("afeng_raw_dump_flag", "1");
        cameraParameters.set("isp-mode", "1");
        cameraParameters.set("rawsave-mode", "2");
        cameraParameters.set("rawfname", StringUtils.GetInternalSDCARD()+"/DCIM/FreeDCam/mtk_."+ FileEnding.BAYER);
        cameraParameters.set("zsd-mode", "on");
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Log.e(TAG,e.getMessage());
        }
    }

    public void Set_RAWFNAME(String filename)
    {
        cameraParameters.set("rawfname", filename);
        SetParametersToCamera(cameraParameters);
    }



    public void SetZTE_AE()
    {
        cameraParameters.set("slow_shutter", "-1");
        //cameraParameters.set("slow_shutter_addition", "0");
        SetParametersToCamera(cameraParameters);


    }

    public void SetZTE_RESET_AE_SETSHUTTER(String Shutter)
    {
        SetZTE_AE();
        cameraUiWrapper.StopPreview();
        cameraUiWrapper.StartPreview();
        cameraParameters.set("slow_shutter",Shutter);
        cameraParameters.set("slow_shutter_addition", "1");
        SetParametersToCamera(cameraParameters);


    }
}
