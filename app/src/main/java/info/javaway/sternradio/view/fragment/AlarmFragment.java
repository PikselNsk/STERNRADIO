package info.javaway.sternradio.view.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import info.javaway.sternradio.App;
import info.javaway.sternradio.R;
import info.javaway.sternradio.Utils;
import info.javaway.sternradio.handler.PrefManager;
import info.javaway.sternradio.handler.SignalManager;
import info.javaway.sternradio.model.Alarm;

public class AlarmFragment extends Fragment implements TimePickerDialog.OnTimeSetListener {

    Alarm alarm;
    TextView mondayTv;
    TextView tuesdayTv;
    TextView wednesdayTv;
    TextView thursdayTv;
    TextView fridayTv;
    TextView saturdayTv;
    TextView sundayTv;
    TextView singleAlarmTv;
    TextView cancelTv;
    TextView okTv;
    TextView alarmText;

    boolean tempMonday;
    boolean tempTuesday;
    boolean tempWednesday;
    boolean tempThursday;
    boolean tempFriday;
    boolean tempSaturday;
    boolean tempSanday;
    int tempHour;
    int tempMinute;
    boolean tempIsCheck;

    private OnFragmentInteractionListener mListener;

    public AlarmFragment() {
        // Required empty public constructor
    }


    public static AlarmFragment newInstance(String param1, String param2) {
        AlarmFragment fragment = new AlarmFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        alarm = Alarm.getInstance();

        View v = inflater.inflate(R.layout.fragment_alarm, container, false);
        alarmText = v.findViewById(R.id.alarm_text);
        mondayTv = v.findViewById(R.id.monday_text_view);
        tuesdayTv = v.findViewById(R.id.tuesday_text_view);
        wednesdayTv = v.findViewById(R.id.wednesday_text_view);
        thursdayTv = v.findViewById(R.id.thursday_text_view);
        fridayTv = v.findViewById(R.id.friday_text_view);
        saturdayTv = v.findViewById(R.id.saturday_text_view);
        sundayTv = v.findViewById(R.id.sunday_text_view);
        singleAlarmTv = v.findViewById(R.id.single_alarm_info_tv);
        cancelTv = v.findViewById(R.id.cancel_tv);
        okTv = v.findViewById(R.id.ok_tv);
        setDaysListeners();
        initDays();
        showDays(!alarm.isSingleAlarm());

        tempMonday = alarm.isMonday();
        tempTuesday = alarm.isTuesday();
        tempWednesday = alarm.isWednesday();
        tempThursday = alarm.isThursday();
        tempFriday = alarm.isFriday();
        tempSaturday = alarm.isSaturday();
        tempSanday = alarm.isSunday();
        tempHour = alarm.getHour();
        tempMinute = alarm.getMinute();
        tempIsCheck = PrefManager.isCheckedAlarm();

        cancelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarm.setMinute(tempMinute);
                alarm.setHour(tempHour);
                alarm.setMonday(tempMonday);
                alarm.setTuesday(tempTuesday);
                alarm.setWednesday(tempWednesday);
                alarm.setThursday(tempThursday);
                alarm.setFriday(tempFriday);
                alarm.setHour(tempHour);
                alarm.setMinute(tempMinute);
                if (tempIsCheck){
                    SignalManager.getInstance(App.getContext())
                            .setAlarm(alarm);
                }
                getActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .remove(AlarmFragment.this)
                        .commit();
            }
        });

        okTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .remove(AlarmFragment.this)
                        .commit();
                SignalManager.getInstance(App.getContext())
                        .setAlarm(alarm);
                mListener.alarmSwitch(true);
            }
        });

        singleAlarmTv.setOnClickListener(v12 -> {
            alarm.setMonday(true);
            alarm.setTuesday(true);
            alarm.setWednesday(true);
            alarm.setThursday(true);
            alarm.setFriday(true);
            alarm.setSaturday(true);
            alarm.setSunday(true);
            PrefManager.setMonday(true);
            PrefManager.setTuesday(true);
            PrefManager.setWednesday(true);
            PrefManager.setThursday(true);
            PrefManager.setFriday(true);
            PrefManager.setSaturday(true);
            PrefManager.setSunday(true);
            initDays();
            showDays(!alarm.isSingleAlarm());
        });

        alarmText.setText(Utils.getStringAlarm(App.getContext(), alarm, false));
        alarmText.setOnClickListener(v1 -> {
            TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(

                    this,
                    alarm.getHour(),
                    alarm.getMinute(),
                    true);
            timePickerDialog.setAccentColor(
                    ContextCompat.getColor(App.getContext(), R.color.colorPrimary));
            timePickerDialog.setThemeDark(true);
            timePickerDialog.setCancelable(false);
            timePickerDialog.show(getActivity().getSupportFragmentManager(), "Set alarm");
        });
        return v;
    }

    private void setDaysListeners() {

        mondayTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarm.setMonday(!alarm.isMonday());
                initDays();
                PrefManager.setMonday(alarm.isMonday());
                showDays(!alarm.isSingleAlarm());
            }
        });

        tuesdayTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarm.setTuesday(!alarm.isTuesday());
                initDays();
                PrefManager.setTuesday(alarm.isTuesday());
                showDays(!alarm.isSingleAlarm());
            }
        });

        wednesdayTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarm.setWednesday(!alarm.isWednesday());
                initDays();

                PrefManager.setWednesday(alarm.isWednesday());
                showDays(!alarm.isSingleAlarm());
            }
        });

        thursdayTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarm.setThursday(!alarm.isThursday());
                initDays();
                PrefManager.setThursday(alarm.isThursday());
                showDays(!alarm.isSingleAlarm());
            }
        });

        fridayTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarm.setFriday(!alarm.isFriday());
                initDays();
                PrefManager.setFriday(alarm.isFriday());
                showDays(!alarm.isSingleAlarm());
            }
        });

        saturdayTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarm.setSaturday(!alarm.isSaturday());
                initDays();
                PrefManager.setSaturday(alarm.isSaturday());
                showDays(!alarm.isSingleAlarm());
            }
        });

        sundayTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarm.setSunday(!alarm.isSunday());
                initDays();
                PrefManager.setSunday(alarm.isSunday());
                showDays(!alarm.isSingleAlarm());
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        Utils.simpleLog("AlarmFragment onTimeSet " + hourOfDay + " " + minute);
        alarm.setHour(hourOfDay);
        alarm.setMinute(minute);
        PrefManager.setHour(hourOfDay);
        PrefManager.setMinute(minute);
        alarmText.setText(Utils.getStringAlarm(App.getContext(), alarm, false));
    }

    public interface OnFragmentInteractionListener {
        void alarmSwitch(boolean isSwitch);
    }

    private void showDays(boolean show) {
        if (show) {
            mondayTv.setVisibility(View.VISIBLE);
            tuesdayTv.setVisibility(View.VISIBLE);
            wednesdayTv.setVisibility(View.VISIBLE);
            thursdayTv.setVisibility(View.VISIBLE);
            fridayTv.setVisibility(View.VISIBLE);
            saturdayTv.setVisibility(View.VISIBLE);
            sundayTv.setVisibility(View.VISIBLE);
            singleAlarmTv.setVisibility(View.GONE);
        } else {
            mondayTv.setVisibility(View.GONE);
            tuesdayTv.setVisibility(View.GONE);
            wednesdayTv.setVisibility(View.GONE);
            thursdayTv.setVisibility(View.GONE);
            fridayTv.setVisibility(View.GONE);
            saturdayTv.setVisibility(View.GONE);
            sundayTv.setVisibility(View.GONE);
            singleAlarmTv.setVisibility(View.VISIBLE);
        }
    }

    void initDays() {
        if (alarm.isMonday()) {
            mondayTv.setBackground(ContextCompat.getDrawable(
                    App.getContext(),
                    R.drawable.day_on
            ));
        } else {
            mondayTv.setBackground(ContextCompat.getDrawable(
                    App.getContext(),
                    R.drawable.day_off
            ));
        }
        if (alarm.isTuesday()) {
            tuesdayTv.setBackground(ContextCompat.getDrawable(
                    App.getContext(),
                    R.drawable.day_on
            ));
        } else {
            tuesdayTv.setBackground(ContextCompat.getDrawable(
                    App.getContext(),
                    R.drawable.day_off
            ));
        }
        if (alarm.isWednesday()) {
            wednesdayTv.setBackground(ContextCompat.getDrawable(
                    App.getContext(),
                    R.drawable.day_on
            ));
        } else {
            wednesdayTv.setBackground(ContextCompat.getDrawable(
                    App.getContext(),
                    R.drawable.day_off
            ));
        }
        if (alarm.isThursday()) {
            thursdayTv.setBackground(ContextCompat.getDrawable(
                    App.getContext(),
                    R.drawable.day_on
            ));
        } else {
            thursdayTv.setBackground(ContextCompat.getDrawable(
                    App.getContext(),
                    R.drawable.day_off
            ));
        }
        if (alarm.isFriday()) {
            fridayTv.setBackground(ContextCompat.getDrawable(
                    App.getContext(),
                    R.drawable.day_on
            ));
        } else {
            fridayTv.setBackground(ContextCompat.getDrawable(
                    App.getContext(),
                    R.drawable.day_off
            ));
        }
        if (alarm.isSaturday()) {
            saturdayTv.setBackground(ContextCompat.getDrawable(
                    App.getContext(),
                    R.drawable.day_on
            ));
        } else {
            saturdayTv.setBackground(ContextCompat.getDrawable(
                    App.getContext(),
                    R.drawable.day_off
            ));
        }
        if (alarm.isSunday()) {
            sundayTv.setBackground(ContextCompat.getDrawable(
                    App.getContext(),
                    R.drawable.day_on
            ));
        } else {
            sundayTv.setBackground(ContextCompat.getDrawable(
                    App.getContext(),
                    R.drawable.day_off
            ));
        }
    }
}
