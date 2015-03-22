package com.troop.freedcam.ui.menu.themes.classic.menu;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;


import com.troop.freedcam.i_camera.AbstractCameraUiWrapper;
import com.troop.freedcam.ui.AppSettingsManager;
import com.troop.freedcam.ui.MainActivity_v2;
import com.troop.freedcam.ui.menu.themes.R;
import com.troop.freedcam.ui.menu.themes.classic.menu.MenuHandler;

/**
 * Created by troop on 27.02.2015.
 */
public class MenuFragment extends Fragment
{
    public LinearLayout settingsLayoutHolder;
    public MenuHandler menuHandler;
    public AppSettingsManager appSettingsManager;
    public AbstractCameraUiWrapper cameraUiWrapper;
    public SurfaceView surfaceView;

    public View view;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // create ContextThemeWrapper from the original Activity Context with the custom theme
        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_NoActionBar_Fullscreen);
        inflater = getActivity().getLayoutInflater().cloneInContext(contextThemeWrapper);
        view = inflater.inflate(R.layout.menu_fragment, container, false);
        settingsLayoutHolder = (LinearLayout)view.findViewById(R.id.settings_menuHolder);
        menuHandler = new MenuHandler(this,getActivity(), appSettingsManager);
        menuHandler.SetCameraUiWrapper(cameraUiWrapper, surfaceView);


        return view;
    }

    public void SetAppSettings(AppSettingsManager appSettingsManager)
    {
        this.appSettingsManager = appSettingsManager;

    }

    public void SetCameraUIWrapper(AbstractCameraUiWrapper cameraUiWrapper, SurfaceView surfaceView)
    {
        this.surfaceView =surfaceView;
        this.cameraUiWrapper = cameraUiWrapper;
        if (menuHandler != null)
            menuHandler.SetCameraUiWrapper(cameraUiWrapper, surfaceView);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}