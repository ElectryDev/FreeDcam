/*
 *
 *     Copyright (C) 2015 George Kiarie
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

package freed.cam.apis.camera1.parameters.manual.shutter;


import android.hardware.Camera.Parameters;

import freed.cam.apis.basecamera.CameraWrapperInterface;
import freed.cam.apis.basecamera.parameters.manual.AbstractManualShutter;

/**
 * Created by GeorgeKiarie on 02/04/2016.
 */
public class ShutterManualKrillin extends AbstractManualShutter {

    private final String TAG = ShutterManualKrillin.class.getSimpleName();
    private final Parameters parameters;

    public ShutterManualKrillin(Parameters parameters, CameraWrapperInterface cameraUiWrapper) {
        super(cameraUiWrapper);
        this.parameters =  parameters;
        isSupported = true;
        isVisible = isSupported;
        stringvalues = cameraUiWrapper.GetAppSettingsManager().manualExposureTime.getValues();
    }

    @Override
    public boolean IsSetSupported() {
        return true;
    }

    @Override
    public void SetValue(int valueToSet)
    {
        currentInt = valueToSet;
        if (valueToSet == 0) {
            parameters.set("hw-hwcamera-flag", "on");
            parameters.set(cameraUiWrapper.GetAppSettingsManager().manualExposureTime.getKEY(), "auto");
        } else {

            parameters.set("hw-hwcamera-flag", "on");
            parameters.set(cameraUiWrapper.GetAppSettingsManager().manualExposureTime.getKEY(), stringvalues[currentInt]);
        }
        ThrowCurrentValueStringCHanged(stringvalues[valueToSet]);
    }

}