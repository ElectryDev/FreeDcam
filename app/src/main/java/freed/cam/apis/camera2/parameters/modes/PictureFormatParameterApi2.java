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

package freed.cam.apis.camera2.parameters.modes;

import android.annotation.TargetApi;
import android.graphics.ImageFormat;
import android.os.Build;
import android.os.Build.VERSION_CODES;

import java.util.ArrayList;

import freed.cam.apis.KEYS;
import freed.cam.apis.basecamera.CameraWrapperInterface;
import freed.cam.apis.camera2.CameraHolderApi2;

/**
 * Created by troop on 12.12.2014.
 */
public class PictureFormatParameterApi2 extends BaseModeApi2
{
    private String format = KEYS.JPEG;
    public PictureFormatParameterApi2(CameraWrapperInterface cameraUiWrapper)
    {
        super(cameraUiWrapper);
    }

    @Override
    public boolean IsSupported() {
        return cameraUiWrapper.GetAppSettingsManager().pictureFormat.isSupported();
    }

    @Override
    public void SetValue(String valueToSet, boolean setToCamera)
    {
        onValueHasChanged(valueToSet);
        format = valueToSet;
        if (setToCamera)
        {
            cameraUiWrapper.StopPreview();
            cameraUiWrapper.StartPreview();
        }
    }

    @Override
    public String GetValue() {
        return format;
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    @Override
    public String[] GetValues()
    {
        return cameraUiWrapper.GetAppSettingsManager().pictureFormat.getValues();
    }
}
