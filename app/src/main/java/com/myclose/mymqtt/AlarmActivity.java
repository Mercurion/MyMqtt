package com.myclose.mymqtt;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.myclose.mymqtt.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jackb on 01/06/2016.
 */

/*
questa activity viene aperta quando c'è un allarme di qualunque cosa
 */
public class AlarmActivity extends Activity {
    /*
    tipo di allarme:
    1: lock signal lost
    2: movement alarm
     */
    private static final int LOCK_LOST = 1;
    private static final int MOVEMENT = 2;
    int alarmType;
    private Sender messageSender = new Sender();

    Button alarmButton;
    Button b;


    private String DEV_MASTER_ID;
    private String DEV_NUMBER;

    MySqlClose myDbInstance;
    SharedPreferences sharedpreferences;
    private static final String MyPREFERENCES = "MyPrefs";


    private ListView mListView;
    private ListaAdapter mAdapter;
    List<String> notifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        /*
        imposta il tipo di allarme con cui si entra
         */
        read();

        mListView = (ListView) findViewById(R.id.listview);
        notifications = new ArrayList<String>();

        mAdapter = new ListaAdapter(getApplicationContext(), notifications);

        mListView.setAdapter(mAdapter);

        myDbInstance = MySqlClose.getIstance(getApplicationContext());
        loadFromdb();

        b = (Button) findViewById(R.id.alert_button);
        alarmButton = (Button) findViewById(R.id.button_alarm);

        sharedpreferences = getSharedPreferences(MyPREFERENCES,
                Context.MODE_PRIVATE);
        setDarkAlarmBackground();
        setAnimation();
    }

    private void read(){
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int value = extras.getInt("parametroIntro");
            setAlarmType(value);
        }
        else
            setAlarmType(0);
    }

    private void loadFromdb() {
        Utente u = myDbInstance.getUtente();
        DEV_MASTER_ID = u.getIdMaster();
        DEV_NUMBER =  u.getIdTelMaster();
        messageSender.setMasterPhone(DEV_NUMBER);
        messageSender.setMasterID(DEV_MASTER_ID);
    }

    private void setDarkAlarmBackground () {
        switch (this.alarmType) {
            case LOCK_LOST:
                this.alarmButton.setBackgroundResource(R.drawable.alarm_signal_lost_dark);
                break;
            case MOVEMENT:
                this.alarmButton.setBackgroundResource(R.drawable.alarm_movement_dark);
                break;
        }
    }

    private void setLightAlarmBackground () {
        switch (this.alarmType) {
            case LOCK_LOST:
                this.alarmButton.setBackgroundResource(R.drawable.alarm_signal_lost_light);
                break;
            case MOVEMENT:
                this.alarmButton.setBackgroundResource(R.drawable.alarm_movement_light);
                break;
        }
    }

    private void setAnimation () {
        AnimationDrawable animation = new AnimationDrawable();
        if (alarmType == 1) {
            animation.addFrame(getResources().getDrawable(R.drawable.alarm_signal_lost_dark), 500);
            animation.addFrame(getResources().getDrawable(R.drawable.alarm_signal_lost_light), 500);
        } else {
            animation.addFrame(getResources().getDrawable(R.drawable.alarm_movement_dark), 500);
            animation.addFrame(getResources().getDrawable(R.drawable.alarm_movement_light), 500);
        }
        animation.setOneShot(false);
        alarmButton.setBackground(animation);
        // start the animation!
        animation.start();
    }

    public void setAlarmType (int value) {
        this.alarmType = value;
    }


    public void getPosition(View v) {
        Toast.makeText(getApplicationContext(), "CONFIRMED POSITION REQUESTED", Toast.LENGTH_LONG).show();
        messageSender.AcquisisciPosizioneMessage();
        closeActivity();
    }

    private void closeActivity () {
        AlarmActivity.this.finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        closeActivity();
    }
}
