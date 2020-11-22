package com.sobot.online.base;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sobot.common.ZCBaseConstant;
import com.sobot.common.api.SobotBaseApi;
import com.sobot.common.frame.http.SobotOkHttpUtils;
import com.sobot.common.listener.SobotPermissionListener;
import com.sobot.common.listener.SobotPermissionListenerImpl;
import com.sobot.common.ui.image.SobotSelectPicUtil;
import com.sobot.common.ui.notchlib.SobotINotchScreen;
import com.sobot.common.ui.notchlib.SobotNotchScreenManager;
import com.sobot.common.ui.statusbar.SobotStatusBarCompat;
import com.sobot.common.utils.SobotActivityManager;
import com.sobot.common.utils.SobotAppUtils;
import com.sobot.common.utils.SobotKeyboardUtils;
import com.sobot.common.utils.SobotLogUtils;
import com.sobot.common.utils.SobotMarkConfig;
import com.sobot.common.utils.SobotResourceUtils;
import com.sobot.common.utils.SobotSDCardUtils;
import com.sobot.common.utils.SobotSPUtils;
import com.sobot.online.OnlineConstant;
import com.sobot.online.activity.SobotCameraActivity;
import com.sobot.online.weight.toast.SobotToastUtil;

import java.util.Locale;

public abstract class SobotBaseActivity extends FragmentActivity {


    //权限回调
    public SobotPermissionListener permissionListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            if (!SobotBaseApi.getSwitchMarkStatus(SobotMarkConfig.LANDSCAPE_SCREEN)) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);//竖屏
            } else {
                //先判断是否已经是横屏，不是横屏再设置为横屏
                if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//横屏
                }
            }
        }
        if (SobotBaseApi.getSwitchMarkStatus(SobotMarkConfig.LANDSCAPE_SCREEN) && SobotBaseApi.getSwitchMarkStatus(SobotMarkConfig.DISPLAY_INNOTCH)) {
            // 支持显示到刘海区域
            SobotNotchScreenManager.getInstance().setDisplayInNotch(this);
            // 设置Activity全屏
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(getContentViewResId());
        int sobot_app_status_bar_color = getResColor("sobot_app_status_bar_color");
        if (sobot_app_status_bar_color != 0) {
            try {
                SobotStatusBarCompat.setStatusBarColor(getSobotActivity(), sobot_app_status_bar_color);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        try {
            initBundleData(savedInstanceState);
            initView();
            initData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //修改国际化语言
        changeAppLanguage();
        SobotActivityManager.getInstance().addActivity(this);
        if (getHearderLeftView()!=null){
            getHearderLeftView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getSobotActivity()!=null){
                        SobotKeyboardUtils.hideSoftInput(getSobotActivity());
                    }
                    finish();
                }
            });
        }
    }

    public void displayInNotchByPadding(final View view) {
        if (SobotBaseApi.getSwitchMarkStatus(SobotMarkConfig.LANDSCAPE_SCREEN) && SobotBaseApi.getSwitchMarkStatus(SobotMarkConfig.DISPLAY_INNOTCH) && view != null && getSobotActivity() != null) {
            // 获取刘海屏信息
            SobotNotchScreenManager.getInstance().getNotchInfo(this, new SobotINotchScreen.NotchScreenCallback() {
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
                                view.setPadding(rect.right + view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
                            }
                        }
                    }
                }
            });

        }
    }

    public void displayInNotchByMargin(final View view) {
        if (SobotBaseApi.getSwitchMarkStatus(SobotMarkConfig.LANDSCAPE_SCREEN) && SobotBaseApi.getSwitchMarkStatus(SobotMarkConfig.DISPLAY_INNOTCH) && view != null && getSobotActivity() != null) {
            // 获取刘海屏信息
            SobotNotchScreenManager.getInstance().getNotchInfo(this, new SobotINotchScreen.NotchScreenCallback() {
                @Override
                public void onResult(SobotINotchScreen.NotchScreenInfo notchScreenInfo) {
                    if (notchScreenInfo.hasNotch) {
                        for (Rect rect : notchScreenInfo.notchRects) {
                            if (view.getParent() instanceof LinearLayout) {
                                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
                                layoutParams.leftMargin = rect.right + layoutParams.leftMargin;
                                view.setLayoutParams(layoutParams);
                            } else if (view.getParent() instanceof RelativeLayout) {
                                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                                layoutParams.leftMargin = rect.right + layoutParams.leftMargin;
                                view.setLayoutParams(layoutParams);
                            } else {
                                view.setPadding(rect.right + view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
                            }
                        }
                    }
                }
            });

        }
    }

    public void changeAppLanguage() {
        Locale language = (Locale) SobotSPUtils.getInstance().getObject("SobotLanguage");
        if (language != null) {
            // 本地语言设置
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = new Configuration();
            conf.locale = language;
            res.updateConfiguration(conf, dm);
        } else {
            //清除上次app 语言设置
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = new Configuration();
            conf.locale = null;
            res.updateConfiguration(conf, dm);
        }
    }


    @Override
    protected void onDestroy() {
        SobotOkHttpUtils.getInstance().cancelTag(SobotBaseActivity.this);
        SobotActivityManager.getInstance().finishActivity(this);
        super.onDestroy();
    }

    //返回布局id
    protected abstract int getContentViewResId();

    protected void initBundleData(Bundle savedInstanceState) {
    }

    protected abstract void initView();

    protected abstract void initData();

    //返回整个头部
    public RelativeLayout getHearderView() {
        return findViewById(getResId("rl_sobot_online_base_header_root"));
    }

    //返回头部左侧返回按钮控件
    public TextView getHearderLeftView() {
        return findViewById(getResId("tv_sobot_online_base_header_left"));
    }

    //返回头部左侧返回按钮控件
    public TextView getHearderRightView() {
        return findViewById(getResId("tv_sobot_online_base_header_right"));
    }

    //返回头部中间标题
    public TextView getHearderTitleView() {
        return findViewById(getResId("tv_sobot_online_base_header_title"));
    }

    //设置头部中间标题
    public void setHearderTitle(String title) {
        TextView titleTV = getHearderTitleView();
        if (titleTV != null) {
            titleTV.setText(title);
        }
    }

    public int getResId(String name) {
        return SobotResourceUtils.getIdByName(SobotBaseActivity.this, "id", name);
    }

    public int getResDrawableId(String name) {
        return SobotResourceUtils.getIdByName(SobotBaseActivity.this, "drawable", name);
    }

    public int getResLayoutId(String name) {
        return SobotResourceUtils.getIdByName(SobotBaseActivity.this, "layout", name);
    }

    public int getResColorId(String name) {
        return SobotResourceUtils.getIdByName(SobotBaseActivity.this, "color", name);
    }

    public int getResStringId(String name) {
        return SobotResourceUtils.getIdByName(SobotBaseActivity.this, "string", name);
    }

    public String getResString(String name) {
        return SobotResourceUtils.getResString(SobotBaseActivity.this, name);
    }

    public int getResColor(String name) {
        int resColorId = getResColorId(name);
        if (resColorId != 0) {
            return getResources().getColor(resColorId);
        }
        return 0;
    }


    public SobotBaseActivity getSobotActivity() {
        return this;
    }

    public Context getSobotContext() {
        return this;
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
//                                SobotSobotToastUtil.showToast(getApplicationContext(), getResString("sobot_app_no_write_external_storage_permission"));
                            } else if (permissions[i] != null && permissions[i].equals(Manifest.permission.RECORD_AUDIO)) {
                                permissionTitle = "sobot_app_no_record_audio_permission";
//                                SobotSobotToastUtil.showToast(getApplicationContext(), getResString("sobot_app_no_record_audio_permission"));
                            } else if (permissions[i] != null && permissions[i].equals(Manifest.permission.CAMERA)) {
                                permissionTitle = "sobot_app_no_camera_permission";
//                                SobotSobotToastUtil.showToast(getApplicationContext(), getResString("sobot_app_no_camera_permission"));
                            }
                            //只有全部权限允许，我们才走回调。
                            if (permissionListener != null) {
                                permissionListener.onPermissionErrorListener(this, getResString(permissionTitle));
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
        if (Build.VERSION.SDK_INT >= 23 && SobotAppUtils.getTargetSdkVersion(getApplicationContext()) >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                //申请WRITE_EXTERNAL_STORAGE权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
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
        if (Build.VERSION.SDK_INT >= 23 && SobotAppUtils.getTargetSdkVersion(getApplicationContext()) >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                        ZCBaseConstant.SOBOT_PERMISSIONS_REQUEST_CODE);
                return false;
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                        ZCBaseConstant.SOBOT_PERMISSIONS_REQUEST_CODE);
                return false;
            }
        }
        return true;
    }

    /**
     * 检查拍摄权限
     *
     * @return true, 已经获取权限;false,没有权限,尝试获取
     */
    protected boolean checkStorageAudioAndCameraPermission() {
        if (Build.VERSION.SDK_INT >= 23 && SobotAppUtils.getTargetSdkVersion(getSobotActivity().getApplicationContext()) >= 23) {
            if (ContextCompat.checkSelfPermission(getSobotActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE
                                , Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}
                        , ZCBaseConstant.SOBOT_PERMISSIONS_REQUEST_CODE);
                return false;
            }
            if (ContextCompat.checkSelfPermission(getSobotActivity(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE
                                , Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}
                        , ZCBaseConstant.SOBOT_PERMISSIONS_REQUEST_CODE);
                return false;
            }
            if (ContextCompat.checkSelfPermission(getSobotActivity(), Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE
                                , Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}
                        , ZCBaseConstant.SOBOT_PERMISSIONS_REQUEST_CODE);
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
            SobotToastUtil.showCustomToast(getApplicationContext(), getResString("sobot_app_sdcard_does_not_exist"),
                    Toast.LENGTH_SHORT);
            return;
        }

        permissionListener = new SobotPermissionListenerImpl() {
            @Override
            public void onPermissionSuccessListener() {
                //如果有拍照所需的权限，跳转到拍照界面
                startActivityForResult(SobotCameraActivity.newIntent(getSobotContext()), OnlineConstant.SOBOT_REQUEST_CODE_CAMERA);

            }
        };

        if (!checkStorageAudioAndCameraPermission()) {
            return;
        }
        // 打开拍摄页面
        startActivityForResult(SobotCameraActivity.newIntent(getSobotContext()), OnlineConstant.SOBOT_REQUEST_CODE_CAMERA);
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
        SobotSelectPicUtil.openSelectPic(this);
    }

    /**
     * 从图库获取视频
     */
    public void selectVedioFromLocal() {
        permissionListener = new SobotPermissionListenerImpl() {
            @Override
            public void onPermissionSuccessListener() {
                SobotSelectPicUtil.openSelectVedio(getSobotActivity());
            }
        };
        if (!checkStoragePermission()) {
            return;
        }
        SobotSelectPicUtil.openSelectVedio(getSobotActivity());
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int currentNightMode = newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                SobotLogUtils.i("=====关闭夜间模式====");
                recreate();
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                SobotLogUtils.i("=====开启夜间模式====");
                recreate();
                break;
            default:
                break;
        }
    }

    /**
     * 是否是全屏
     *
     * @return
     */
    protected boolean isFullScreen() {
        return (getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN;
    }

    public void hideSoftInput(){
        if (getSobotActivity() != null) {
            SobotKeyboardUtils.hideSoftInput(getSobotActivity());
        }
    }
}