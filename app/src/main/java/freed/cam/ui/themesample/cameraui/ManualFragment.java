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

package freed.cam.ui.themesample.cameraui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.troop.freedcam.R;
import com.troop.freedcam.R.id;
import com.troop.freedcam.R.layout;

import freed.ActivityInterface;
import freed.cam.apis.KEYS;
import freed.cam.apis.basecamera.modules.ModuleChangedEvent;
import freed.cam.apis.basecamera.parameters.AbstractParameterHandler;
import freed.cam.apis.basecamera.parameters.manual.AbstractManualParameter.I_ManualParameterEvent;
import freed.cam.apis.sonyremote.SonyCameraRemoteFragment;
import freed.cam.ui.themesample.AbstractFragment;
import freed.utils.AppSettingsManager;

/**
 * Created by troop on 08.12.2015.
 */
public class ManualFragment extends AbstractFragment implements OnSeekBarChangeListener, I_ManualParameterEvent, ModuleChangedEvent
{
    private int currentValuePos;

    private RotatingSeekbar seekbar;

    private ManualButton currentButton;


    private AfBracketSettingsView afBracketSettingsView;

    private LinearLayout manualItemsHolder;

    private final String TAG = ManualFragment.class.getSimpleName();


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(layout.cameraui_manual_fragment_rotatingseekbar, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fragment_activityInterface = (ActivityInterface)getActivity();
        seekbar = (RotatingSeekbar)view.findViewById(id.seekbar);
        seekbar.setOnSeekBarChangeListener(this);
        seekbar.setVisibility(View.GONE);

        manualItemsHolder = (LinearLayout)view.findViewById(id.manualItemsHolder);

        afBracketSettingsView = (AfBracketSettingsView)view.findViewById(id.manualFragment_afbsettings);
        afBracketSettingsView.setVisibility(View.GONE);
    }

    @Override
    protected void setCameraUiWrapperToUi()
    {
        if (manualItemsHolder == null)
            return;
        manualItemsHolder.removeAllViews();
        if (cameraUiWrapper != null)
        {
            cameraUiWrapper.GetModuleHandler().addListner(this);
            AppSettingsManager aps = cameraUiWrapper.GetAppSettingsManager();
            AbstractParameterHandler parms = cameraUiWrapper.GetParameterHandler();
            if (parms.Zoom != null)
            {
                ManualButton btn = new ManualButton(getContext(), aps.manualZoom, parms.Zoom, R.drawable.manual_zoom);
                btn.setOnClickListener(manualButtonClickListner);
                manualItemsHolder.addView(btn);
            }

            if (parms.ManualFocus != null) {
                ManualButton btn = new ManualButton(getContext(), aps.manualFocus, parms.ManualFocus, R.drawable.manual_focus);
                btn.setOnClickListener(manualButtonClickListner);
                manualItemsHolder.addView(btn);
            }
            if (parms.ManualIso != null) {
                ManualButton btn = new ManualButton(getContext(), aps.manualIso, parms.ManualIso, R.drawable.manual_iso);
                btn.setOnClickListener(manualButtonClickListner);
                manualItemsHolder.addView(btn);
            }
            if (parms.ManualShutter != null) {
                ManualButton btn = new ManualButton(getContext(), aps.manualExposureTime, parms.ManualShutter, R.drawable.manual_shutter);
                btn.setOnClickListener(manualButtonClickListner);
                manualItemsHolder.addView(btn);
            }
            if (parms.ManualFNumber != null) {
                ManualButton btn = new ManualButton(getContext(), aps.manualFnumber, parms.ManualFNumber, R.drawable.manual_fnum);
                btn.setOnClickListener(manualButtonClickListner);
                manualItemsHolder.addView(btn);
            }
            if (parms.ManualExposure != null) {
                ManualButton btn = new ManualButton(getContext(), aps.manualExposureCompensation, parms.ManualExposure, R.drawable.manual_exposure);
                btn.setOnClickListener(manualButtonClickListner);
                manualItemsHolder.addView(btn);
            }
            if (parms.CCT != null) {
                ManualButton btn = new ManualButton(getContext(), aps.manualWhiteBalance, parms.CCT, R.drawable.manual_wb);
                btn.setOnClickListener(manualButtonClickListner);
                manualItemsHolder.addView(btn);
            }
            if (parms.CCT != null) {
                ManualButton btn = new ManualButton(getContext(), aps.manualWhiteBalance, parms.CCT, R.drawable.manual_wb);
                btn.setOnClickListener(manualButtonClickListner);
                manualItemsHolder.addView(btn);
            }
            if (parms.Burst != null) {
                ManualButton btn = new ManualButton(getContext(), aps.manualBurst, parms.CCT, R.drawable.manual_burst);
                btn.setOnClickListener(manualButtonClickListner);
                manualItemsHolder.addView(btn);
            }
            if (parms.ManualContrast != null) {
                ManualButton btn = new ManualButton(getContext(), aps.manualContrast, parms.ManualContrast, R.drawable.manual_contrast);
                btn.setOnClickListener(manualButtonClickListner);
                manualItemsHolder.addView(btn);
            }
            if (parms.ManualBrightness != null) {
                ManualButton btn = new ManualButton(getContext(), aps.manualBrightness, parms.ManualBrightness, R.drawable.brightness);
                btn.setOnClickListener(manualButtonClickListner);
                manualItemsHolder.addView(btn);
            }
            if (parms.ManualSaturation != null) {
                ManualButton btn = new ManualButton(getContext(), aps.manualSaturation, parms.ManualSaturation, R.drawable.manual_saturation);
                btn.setOnClickListener(manualButtonClickListner);
                manualItemsHolder.addView(btn);
            }
            if (parms.ManualSharpness != null) {
                ManualButton btn = new ManualButton(getContext(), aps.manualSharpness, parms.ManualSharpness, R.drawable.manual_sharpness);
                btn.setOnClickListener(manualButtonClickListner);
                manualItemsHolder.addView(btn);
            }
            if (parms.ManualConvergence != null) {
                ManualButton btn = new ManualButton(getContext(), aps.manualConvergence, parms.ManualConvergence, R.drawable.manual_convergence);
                btn.setOnClickListener(manualButtonClickListner);
                manualItemsHolder.addView(btn);
            }
            if (parms.FX != null) {
                ManualButton btn = new ManualButton(getContext(), aps.manualFx, parms.FX, R.drawable.manual_fx);
                btn.setOnClickListener(manualButtonClickListner);
                manualItemsHolder.addView(btn);
            }
            if (parms.ProgramShift != null) {
                ManualButton btn = new ManualButton(getContext(), aps.manualProgramShift, parms.ProgramShift, R.drawable.manual_shift);
                btn.setOnClickListener(manualButtonClickListner);
                manualItemsHolder.addView(btn);
            }
            if (parms.PreviewZoom != null) {
                ManualButton btn = new ManualButton(getContext(), aps.manualPreviewZoom, parms.PreviewZoom, R.drawable.manual_zoom);
                btn.setOnClickListener(manualButtonClickListner);
                manualItemsHolder.addView(btn);
            }

            seekbar.setVisibility(View.GONE);
            afBracketSettingsView.SetCameraWrapper(cameraUiWrapper);
            if (cameraUiWrapper.GetModuleHandler().GetCurrentModuleName().equals(KEYS.MODULE_AFBRACKET) /*&& currentButton == mf*/ && seekbar.getVisibility() == View.VISIBLE)
                afBracketSettingsView.setVisibility(View.VISIBLE);
            else
                afBracketSettingsView.setVisibility(View.GONE);
        }
    }

    //######## ManualButton Stuff#####
    private final OnClickListener manualButtonClickListner = new OnClickListener() {
        @Override
        public void onClick(View v)
        {
            if (currentButton != null)
                currentButton.RemoveParameterListner(ManualFragment.this);
            //when same button gets clicked second time
            if(v == currentButton && seekbar.getVisibility() == View.VISIBLE)
            {
                //hideseekbar and set color back from button
                seekbar.setVisibility(View.GONE);
                currentButton.SetActive(false);
                afBracketSettingsView.setVisibility(View.GONE);
            }
            //if no button was active or a different was clicked
            else
            {
                if (seekbar.getVisibility() == View.GONE)
                    seekbar.setVisibility(View.VISIBLE);
                //when already a button is active disable it
                if (currentButton != null)
                    currentButton.SetActive(false);
                //set the returned view as active and fill seekbar
                currentButton = (ManualButton) v;
                currentButton.SetActive(true);
                currentButton.SetParameterListner(ManualFragment.this);
                if (/*currentButton == mf &&*/ cameraUiWrapper.GetModuleHandler().GetCurrentModuleName().equals(KEYS.MODULE_AFBRACKET))
                    afBracketSettingsView.setVisibility(View.VISIBLE);
                else
                    afBracketSettingsView.setVisibility(View.GONE);
                String[]vals = currentButton.getStringValues();
                if (vals == null || vals.length == 0) {
                    currentButton.SetActive(false);
                    seekbar.setVisibility(View.GONE);
                    Log.e(TAG, "Values returned from currentButton are NULL!");
                    return;
                }
                seekbar.SetStringValues(vals);
                seekbar.setProgress(currentButton.getCurrentItem(),false);
                currentValuePos = currentButton.getCurrentItem();
                Log.d(TAG, "CurrentvaluePos " + currentValuePos);
            }

        }
    };

    //#########################SEEKBAR STUFF#############################


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
        Log.d(TAG, "onProgressChanged:" + progress);
        currentValuePos = progress;
        if (!(cameraUiWrapper instanceof SonyCameraRemoteFragment)) {
            currentButton.setValueToParameters(progress);
            currentButton.onCurrentValueChanged(progress);

        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (cameraUiWrapper instanceof SonyCameraRemoteFragment) {
            currentButton.setValueToParameters(currentValuePos);
            currentButton.onCurrentValueChanged(currentValuePos);
        }
    }

    @Override
    public void onIsSupportedChanged(boolean value)
    {
        if (!value) {
            seekbar.setVisibility(View.GONE);
            currentButton.SetActive(false);
        }
    }

    @Override
    public void onIsSetSupportedChanged(boolean value)
    {
        if (value)
            seekbar.setVisibility(View.VISIBLE);
        else
            seekbar.setVisibility(View.GONE);
    }


    @Override
    public void onCurrentValueChanged(int current)
    {
       /* if(!seekbar.IsAutoScrolling()&& !seekbar.IsMoving())
        {
            seekbar.setProgress(current, false);
        }*/
    }

    @Override
    public void onValuesChanged(String[] values)
    {
        seekbar.SetStringValues(values);
    }

    @Override
    public void onCurrentStringValueChanged(String value) {

    }

    /**
     * Gets called when the module has changed
     *
     * @param module
     */
    @Override
    public void onModuleChanged(String module)
    {
        if (module.equals(KEYS.MODULE_AFBRACKET) && seekbar.getVisibility() == View.VISIBLE)
            afBracketSettingsView.setVisibility(View.VISIBLE);
        else
            afBracketSettingsView.setVisibility(View.GONE);
    }

}
