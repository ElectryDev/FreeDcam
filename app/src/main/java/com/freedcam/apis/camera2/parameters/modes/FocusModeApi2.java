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

package com.freedcam.apis.camera2.parameters.modes;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.os.Build.VERSION_CODES;

import com.freedcam.apis.basecamera.interfaces.CameraWrapperInterface;
import com.freedcam.apis.camera2.CameraHolderApi2;

/**
 * Created by Ingo on 01.05.2015.
 */
@TargetApi(VERSION_CODES.LOLLIPOP)
public class FocusModeApi2 extends BaseModeApi2
{
    public enum FocusModes
    {
        off,
        auto,
        macro,
        continouse_movie,
        continouse_picture,
        edof,
    }

    public FocusModeApi2(CameraWrapperInterface cameraUiWrapper) {
        super(cameraUiWrapper);
    }

    @Override
    public boolean IsSupported()
    {
        return ((CameraHolderApi2) cameraUiWrapper.GetCameraHolder()).characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES) != null;
    }

    @Override
    public void SetValue(String valueToSet, boolean setToCamera)
    {
        if (valueToSet.contains("unknown Focus"))
            return;
        FocusModes sceneModes = Enum.valueOf(FocusModes.class, valueToSet);
        ((CameraHolderApi2) cameraUiWrapper.GetCameraHolder()).SetParameterRepeating(CaptureRequest.CONTROL_AF_MODE, sceneModes.ordinal());
        BackgroundValueHasChanged(valueToSet);
        //cameraHolder.mPreviewRequestBuilder.build();
    }

    @Override
    public String GetValue()
    {

        int i = ((CameraHolderApi2) cameraUiWrapper.GetCameraHolder()).get(CaptureRequest.CONTROL_AF_MODE);
        FocusModes sceneModes = FocusModes.values()[i];
        return sceneModes.toString();
    }

    @Override
    public String[] GetValues()
    {
        int[] values = ((CameraHolderApi2) cameraUiWrapper.GetCameraHolder()).characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
        String[] retvals = new String[values.length];
        for (int i = 0; i < values.length; i++)
        {
            try {
                FocusModes sceneModes = FocusModes.values()[values[i]];
                retvals[i] = sceneModes.toString();
            }
            catch (Exception ex)
            {
                retvals[i] = "unknown Focus" + values[i];
            }

        }
        return retvals;
    }
}
