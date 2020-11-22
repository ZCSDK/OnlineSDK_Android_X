package com.sobot.online.base;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.sobot.common.ZCBaseConstant;
import com.sobot.common.api.SobotBaseApi;
import com.sobot.common.frame.http.SobotOkHttpUtils;
import com.sobot.common.listener.SobotPermissionListener;
import com.sobot.common.listener.SobotPermissionListenerImpl;
import com.sobot.common.ui.image.SobotSelectPicUtil;
import com.sobot.common.ui.notchlib.SobotINotchScreen;
import com.sobot.common.ui.notchlib.SobotNotchScreenManager;
import com.sobot.common.utils.SobotAppUtils;
import com.sobot.common.utils.SobotKeyboardUtils;
import com.sobot.common.utils.SobotMarkConfig;
import com.sobot.common.utils.SobotResourceUtils;
import com.sobot.common.utils.SobotSDCardUtils;
import com.sobot.online.weight.toast.SobotToastUtil;

import java.io.File;

/**
 * @author Created by jinxl on 2018/2/1.
 */
public abstract class SobotBaseFragment extends Fragment {

    protected File cameraFile;

    private Activity activity;
    //权限回调
    public SobotPermissionListener permissionListener;

    public SobotBaseFragment() {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (activity == null) {
            activity = (Activity) context;
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SobotBaseApi.getSwitchMarkStatus(SobotMarkConfig.DISPLAY_INNOTCH) && SobotBaseApi.getSwitchMarkStatus(SobotMarkConfig.LANDSCAPE_SCREEN)) {
            // 支持显示到刘海区域
            SobotNotchScreenManager.getInstance().setDisplayInNotch(getActivity());
            // 设置Activity全屏
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    public void displayInNotch(final View view) {
        if (SobotBaseApi.getSwitchMarkStatus(SobotMarkConfig.LANDSCAPE_SCREEN) && SobotBaseApi.getSwitchMarkStatus(SobotMarkConfig.DISPLAY_INNOTCH) && view != null) {
            // 获取刘海屏信息
            SobotNotchScreenManager.getInstance().getNotchInfo(getActivity(), new SobotINotchScreen.NotchScreenCallback() {
                @Override
                public void onResult(SobotINotchScreen.NotchScreenInfo notchScreenInfo) {
                    if (notchScreenInfo.hasNotch) {
                        for (Rect rect : notchScreenInfo.notchRects) {
                            if (view instanceof WebView && view.getParent() instanceof LinearLayout) {
                                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
                                layoutParams.leftMargin = rect.right + 14;
                                view.setLayoutParams(layoutParams);
                            } else if (view instanceof WebView && view.getParent() instanceof RelativeLayout) {
                                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                                layoutParams.leftMargin = rect.right + 14;
                                view.setLayoutParams(layoutParams);
                            } else {
                                view.setPadding(rect.right, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
                            }
                        }
                    }
                }
            });

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SobotOkHttpUtils.getInstance().cancelTag(SobotBaseFragment.this);
    }

    public SobotBaseFragment getSobotBaseFragment() {
        return this;
    }

    public int getResId(String name) {
        return SobotResourceUtils.getIdByName(getSobotActivity(), "id", name);
    }

    public int getResDrawableId(String name) {
        return SobotResourceUtils.getIdByName(getSobotActivity(), "drawable", name);
    }

    public int getResLayoutId(String name) {
        return SobotResourceUtils.getIdByName(getSobotActivity(), "layout", name);
    }

    public int getResStringId(String name) {
        return SobotResourceUtils.getIdByName(getSobotActivity(), "string", name);
    }

    public int getResDimenId(String name) {
        return SobotResourceUtils.getIdByName(getSobotActivity(), "dimen", name);
    }

    public String getResString(String name) {
        return SobotResourceUtils.getResString(getSobotActivity(), name);
//        return getResources().getString(getResStringId(name));
    }

    public float getDimens(String name) {
        return getResources().getDimension(getResDimenId(name));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case ZCBaseConstant.SOBOT_PERMISSIONS_REQUEST_CODE:
                try {
                    for (int i = 0; i < grantResults.length; i++) {
                        //判断权限的结果，如果有被拒绝，就return
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            String permissionTitle = "sobot_app_no_permission_text";
                            if (permissions[i] != null && permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                permissionTitle = "sobot_app_no_write_external_storage_permission";
//                                SobotSobotToastUtil.showToast(getSobotActivity(), getResString("sobot_app_no_write_external_storage_permission"));
                            } else if (permissions[i] != null && permissions[i].equals(Manifest.permission.RECORD_AUDIO)) {
                                permissionTitle = "sobot_app_no_record_audio_permission";
//                                SobotSobotToastUtil.showToast(getSobotActivity(), getResString("sobot_app_no_record_audio_permission"));
                            } else if (permissions[i] != null && permissions[i].equals(Manifest.permission.CAMERA)) {
                                permissionTitle = "sobot_app_no_camera_permission";
//                                SobotSobotToastUtil.showToast(getSobotActivity(), getResString("sobot_app_no_camera_permission"));
                            }
                            //只有全部权限允许，我们才走回调。
                            if (permissionListener != null) {
                                permissionListener.onPermissionErrorListener(getSobotActivity(), getResString(permissionTitle));
                            }
                            return;
//                            permissionListener = null;
                        }
                    }
                    if (permissionListener != null) {
                        permissionListener.onPermissionSuccessListener();
                    }
                } catch (Exception e) {
//                    e.printStackTrace();
                }
                break;
        }
    }

    /**
     * 检查存储权限
     *
     * @return true, 已经获取权限;false,没有权限,尝试获取
     */
    protected boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= 23 && SobotAppUtils.getTargetSdkVersion(getSobotActivity()) >= 23) {
            if (ContextCompat.checkSelfPermission(getSobotActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                //申请WRITE_EXTERNAL_STORAGE权限
                ActivityCompat.requestPermissions(getSobotActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        ZCBaseConstant.SOBOT_PERMISSIONS_REQUEST_CODE);
                return false;
            }
        }
        return true;
    }

    /**
     * 检查录音权限
     *
     * @return true, 已经获取权限;false,没有权限,尝试获取
     */
    protected boolean checkStorageAndAudioPermission() {
        if (Build.VERSION.SDK_INT >= 23 && SobotAppUtils.getTargetSdkVersion(getSobotActivity()) >= 23) {
            if (ContextCompat.checkSelfPermission(getSobotActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getSobotActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                        ZCBaseConstant.SOBOT_PERMISSIONS_REQUEST_CODE);
                return false;
            }
            if (ContextCompat.checkSelfPermission(getSobotActivity(), Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getSobotActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                        ZCBaseConstant.SOBOT_PERMISSIONS_REQUEST_CODE);
                return false;
            }
        }
        return true;
    }

    /**
     * 检查相机权限
     *
     * @return true, 已经获取权限;false,没有权限,尝试获取
     */
    protected boolean checkStorageAndCameraPermission() {
        if (Build.VERSION.SDK_INT >= 23 && SobotAppUtils.getTargetSdkVersion(getSobotActivity()) >= 23) {
            if (ContextCompat.checkSelfPermission(getSobotActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getSobotActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                        ZCBaseConstant.SOBOT_PERMISSIONS_REQUEST_CODE);
                return false;
            }
            if (ContextCompat.checkSelfPermission(getSobotActivity(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getSobotActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                        ZCBaseConstant.SOBOT_PERMISSIONS_REQUEST_CODE);
                return false;
            }
        }
        return true;
    }

    /**
     * 通过照相上传图片
     */
    public void selectPicFromCamera() {

        if (!SobotSDCardUtils.isSDCardEnableByEnvironment()) {
            SobotToastUtil.showCustomToast(getSobotActivity(), getResString("sobot_app_sdcard_does_not_exist"),
                    Toast.LENGTH_SHORT);
            return;
        }

        permissionListener = new SobotPermissionListenerImpl() {
            @Override
            public void onPermissionSuccessListener() {
                cameraFile = SobotSelectPicUtil.openCamera(getSobotActivity());
            }
        };

        if (!checkStorageAndCameraPermission()) {
            return;
        }
        cameraFile = SobotSelectPicUtil.openCamera(getSobotActivity());
    }

    /**
     * 从图库获取图片
     */
    public void selectPicFromLocal() {
        permissionListener = new SobotPermissionListenerImpl() {
            @Override
            public void onPermissionSuccessListener() {
                SobotSelectPicUtil.openSelectPic(getSobotActivity());
            }
        };
        if (!checkStoragePermission()) {
            return;
        }
        SobotSelectPicUtil.openSelectPic(getSobotActivity());
    }


    public static boolean isCameraCanUse() {

        boolean canUse = false;
        Camera mCamera = null;

        try {
            mCamera = Camera.open(0);
            Camera.Parameters mParameters = mCamera.getParameters();
            mCamera.setParameters(mParameters);
        } catch (Exception e) {
            canUse = false;
        }

        if (mCamera != null) {
            mCamera.release();
            canUse = true;
        }

        return canUse;
    }

    /**
     * 返回activity
     *
     * @return
     */
    public Activity getSobotActivity() {
        Activity activity = getActivity();
        if (activity == null) {
            return this.activity;
        }
        return activity;

    }

    //隐藏键盘
    public void hideSoftInput(){
        if (getSobotActivity() != null) {
            SobotKeyboardUtils.hideSoftInput(getSobotActivity());
        }
    }

}