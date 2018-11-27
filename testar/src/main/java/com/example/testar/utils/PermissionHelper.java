package com.example.testar.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.View;

import java.util.Arrays;
import java.util.List;

/**
 * Created on 2017/2/7 16:09.
 * Author as tss@sinnye.com
 * Function description : 权限访问管理
 */
public class PermissionHelper {

    public static final int REQUEST_PERMISSION_CODE = 1000;

    private Object mContext;

    private PermissionListener mListener;

    private List<String> mPermissionList;

    private View view;

    public PermissionHelper(@NonNull Object object, View view) {
        checkCallingObjectSuitability(object);
        this.mContext = object;
        this.view = view;
    }
    public PermissionHelper(@NonNull Object object) {
        checkCallingObjectSuitability(object);
        this.mContext = object;
    }

    /**
     * 判断是否具有某权限
     *
     * @param object
     * @param perms
     * @return
     */
    public static boolean hasPermissions(@NonNull Object object, @NonNull String... perms) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        for (String perm : perms) {
            boolean hasPerm = (ContextCompat.checkSelfPermission(getActivity(object), perm) ==
                    PackageManager.PERMISSION_GRANTED);
            if (!hasPerm) {
                return false;
            }
        }

        return true;
    }

    /**
     * 兼容fragment
     *
     * @param object
     * @param perm
     * @return
     */
    @TargetApi(23)
    private static boolean shouldShowRequestPermissionRationale(@NonNull Object object, @NonNull String perm) {
        if (object instanceof Activity) {
            return ActivityCompat.shouldShowRequestPermissionRationale((Activity) object, perm);
        } else if (object instanceof Fragment) {
            return ((Fragment) object).shouldShowRequestPermissionRationale(perm);
        } else if (object instanceof android.app.Fragment) {
            return ((android.app.Fragment) object).shouldShowRequestPermissionRationale(perm);
        } else {
            return false;
        }
    }

    @TargetApi(11)
    private static Activity getActivity(@NonNull Object object) {
        if (object instanceof Activity) {
            return ((Activity) object);
        } else if (object instanceof Fragment) {
            return ((Fragment) object).getActivity();
        } else if (object instanceof android.app.Fragment) {
            return ((android.app.Fragment) object).getActivity();
        } else {
            return null;
        }
    }

    public static boolean isNeedRequest() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static void getAppDetailSettingIntent(Context context) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
        }
        context.startActivity(localIntent);
    }

    /**
     * 权限授权申请
     *
     * @param hintMessage 要申请的权限的提示
     * @param permissions 要申请的权限
     * @param listener    申请成功之后的callback
     */
    public void requestPermissions(@NonNull CharSequence hintMessage, final int requestCode, @Nullable PermissionListener listener, @NonNull final String... permissions) {
            if (listener != null) {
            mListener = listener;
        }
        mPermissionList = Arrays.asList(permissions);

        //没全部权限
        if (!hasPermissions(mContext, permissions)) {

            //需要向用户解释为什么申请这个权限
            boolean shouldShowRationale = true;
            for (String perm : permissions) {
                shouldShowRationale =
                        shouldShowRationale || shouldShowRequestPermissionRationale(mContext, perm);
            }

            if (!shouldShowRationale) {
                if (view != null) {
                }
            } else {
                // 暂时这么写 2018年3月20日
                if ("\"即时聊天\"想访问你的录音功能，为了更好的聊天，请打开此权限。".equals(hintMessage)) {

                }else {
                    executePermissionsRequest(mContext, permissions,
                            requestCode == -1 ? REQUEST_PERMISSION_CODE : requestCode);
                }
            }
        } else if (mListener != null) { //有全部权限
            mListener.doAfterGrand(permissions);
        }
    }

    /**
     * 处理onRequestPermissionsResult
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public  void handleRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE:
                boolean allGranted = true;
                for (int grant : grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false;
                        break;
                    }
                }

                if (allGranted && mListener != null) {

                    mListener.doAfterGrand((String[]) mPermissionList.toArray());

                } else if (!allGranted && mListener != null) {
                    mListener.doAfterDenied((String[]) mPermissionList.toArray());
                }
                break;
        }
    }

    /**
     * 执行申请,兼容fragment
     *
     * @param object
     * @param perms
     * @param requestCode
     */
    @TargetApi(23)
    public void executePermissionsRequest(@NonNull Object object, @NonNull String[] perms, int requestCode) {
        if (object instanceof Activity) {
            ActivityCompat.requestPermissions((Activity) object, perms, requestCode);
        } else if (object instanceof Fragment) {
            ((Fragment) object).requestPermissions(perms, requestCode);
        } else if (object instanceof android.app.Fragment) {
            ((android.app.Fragment) object).requestPermissions(perms, requestCode);
        }
    }

    /**
     * 检查传递Context是否合法
     *
     * @param object
     */
    private void checkCallingObjectSuitability(@Nullable Object object) {
        if (object == null) {
            throw new NullPointerException("Activity or Fragment should not be null");
        }

        boolean isActivity = object instanceof Activity;
        boolean isSupportFragment = object instanceof Fragment;
        boolean isAppFragment = object instanceof android.app.Fragment;
        if (!(isSupportFragment || isActivity || (isAppFragment && isNeedRequest()))) {
            if (isAppFragment) {
                throw new IllegalArgumentException(
                        "Target SDK needs to be greater than 23 if caller is android.app.Fragment");
            } else {
                throw new IllegalArgumentException("Caller must be an Activity or a Fragment.");
            }
        }
    }

    public interface PermissionListener {

        void doAfterGrand(String... permission);

        void doAfterDenied(String... permission);
    }


}
