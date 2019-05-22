package info.javaway.sternradio.view.dialog;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.fragment.app.DialogFragment;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.List;

import info.javaway.sternradio.App;
import info.javaway.sternradio.R;
import info.javaway.sternradio.handler.PrefManager;
import info.javaway.sternradio.service.NotificationHelper;


public class SettingsDialog extends DialogFragment {

    Button okButton;
    AppCompatCheckBox notificationCheckBox;
    private String message;

    ExpandableLayout mXiaomiEl;
    ExpandableLayout mSamsungEl;
    ExpandableLayout mHuaweiEl;
    ExpandableLayout mAsusEl;
    ExpandableLayout mOppoEl;
    ExpandableLayout mOnePlusEl;
    ExpandableLayout mLenovoEl;
    ExpandableLayout mXperiaEl;
    ExpandableLayout mOtherEl;
    ExpandableLayout mInstructionsExpandableLayout;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_settings, null);
        mInstructionsExpandableLayout = v.findViewById(R.id.instructions_life_alarm_el);
        mXiaomiEl = v.findViewById(R.id.xiaomi_info_el);
        mSamsungEl = v.findViewById(R.id.samsung_info_el);
        mHuaweiEl = v.findViewById(R.id.huawei_info_el);
        mAsusEl = v.findViewById(R.id.asus_info_el);
        mOppoEl = v.findViewById(R.id.oppo_info_el);
        mOnePlusEl = v.findViewById(R.id.oneplus_info_el);
        mLenovoEl = v.findViewById(R.id.lenovo_info_el);
        mXperiaEl = v.findViewById(R.id.xperia_info_el);
        mOtherEl = v.findViewById(R.id.other_info_el);
        builder.setView(v);
        AlertDialog alertDialog = builder.create();

        v.findViewById(R.id.dont_call_alarm_text_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean expanded = mInstructionsExpandableLayout.isExpanded();
                if (expanded) {
                    mInstructionsExpandableLayout.collapse();
                } else {
                    mInstructionsExpandableLayout.expand();

                }
            }
        });

        notificationCheckBox = v.findViewById(R.id.notification_check_box);
        notificationCheckBox.setChecked(PrefManager.getNotification());
        notificationCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefManager.setNotification(isChecked);
                if (isChecked) {
                    NotificationHelper.sendNotificationToManagment(
                            App.getContext(),
                            "",
                            0,
                            null,
                            null,
                            null,
                            false);
                } else {
                    NotificationHelper.clearNotification();
                }
            }
        });

        okButton = v.findViewById(R.id.ok_button);
        okButton.setOnClickListener(v1 -> dismiss());

        v.findViewById(R.id.samsung_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSamsungEl.isExpanded()) {
                    mSamsungEl.collapse();
                } else {
                    mSamsungEl.expand();
                }
            }
        });
        v.findViewById(R.id.xiaomi_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mXiaomiEl.isExpanded()) {
                    mXiaomiEl.collapse();
                } else {
                    mXiaomiEl.expand();
                }
            }
        });
        v.findViewById(R.id.huawei_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mHuaweiEl.isExpanded()) {
                    mHuaweiEl.collapse();
                } else {
                    mHuaweiEl.expand();
                }
            }
        });
        v.findViewById(R.id.samsung_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSamsungEl.isExpanded()) {
                    mSamsungEl.collapse();
                } else {
                    mSamsungEl.expand();
                }
            }
        });
        v.findViewById(R.id.oppo_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOppoEl.isExpanded()) {
                    mOppoEl.collapse();
                } else {
                    mOppoEl.expand();
                }
            }
        });
        v.findViewById(R.id.asus_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAsusEl.isExpanded()) {
                    mAsusEl.collapse();
                } else {
                    mAsusEl.expand();
                }
            }
        });

        v.findViewById(R.id.oneplus_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnePlusEl.isExpanded()) {
                    mOnePlusEl.collapse();
                } else {
                    mOnePlusEl.expand();
                }
            }
        });

        v.findViewById(R.id.lenovo_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLenovoEl.isExpanded()) {
                    mLenovoEl.collapse();
                } else {
                    mLenovoEl.expand();
                }
            }
        });

        v.findViewById(R.id.xperia_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mXperiaEl.isExpanded()) {
                    mXperiaEl.collapse();
                } else {
                    mXperiaEl.expand();
                }
            }
        });

        v.findViewById(R.id.other_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOtherEl.isExpanded()) {
                    mOtherEl.collapse();
                } else {
                    mOtherEl.expand();
                }
            }
        });
        PackageManager packageManager = getActivity().getPackageManager();
        final Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        List activities = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        boolean isIntentSafe = activities.size() > 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isIntentSafe) {
            View batterySettingsTv = v.findViewById(R.id.open_battery_settings_tv);
            batterySettingsTv.setVisibility(View.VISIBLE);
            batterySettingsTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                        getActivity().startActivity(intent);
                    } catch (Throwable e) {
                    }
                }
            });
        }
        return alertDialog;
    }

}