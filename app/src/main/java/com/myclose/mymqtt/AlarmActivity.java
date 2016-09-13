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


import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * Questa activity viene invocata in caso di messaggi di allarme.
 *     variabili di ingresso per il tipo di allarme:
 *     1: lock signal lost
 *     2: movement alarm
 *
 *     Cliccando sul tasto centrale, viene richiesta la posizione, attraverso un oggetto Sender
 *     @author Giacomo
 *     @version 1.01
 *     @see Sender
 *     @see MyCloseMenuActivity
 *     @see MessageAnalyzer
 *     </pre>
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

    /**
     * Metodo che legge i parametri di ingresso alla classe, determinando il tipo di allarme passato.
     */
    private void read(){
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int value = extras.getInt("parametroIntro");
            setAlarmType(value);
        }
        else
            setAlarmType(0);
    }

    /**
     * Questo metodo viene chiamato per caricare i dati dal DB. I dati necessari sono Id del master e numero di telefono.
     */
    private void loadFromdb() {
        Utente u = myDbInstance.getUtente();
        DEV_MASTER_ID = u.getIdMaster();
        DEV_NUMBER =  u.getIdTelMaster();
        messageSender.setMasterPhone(DEV_NUMBER);
        messageSender.setMasterID(DEV_MASTER_ID);
    }

    /**
     * Metodo che imposta il background dell'allarme di colore scuro.
     */
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

    /**
     * <pre>
     * Metodo che imposta l'animazione in base al tipo di allarme.
     * L'alternanza delle immagini è di 0.5 secondi.
     * </pre>
     */
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

    /**
     * Metodo per settare il valore di alarmType, in base al tipo di allarme in arrivo
     * @param value il valore passato in ingresso all'activity
     */
    private void setAlarmType (int value) {
        this.alarmType = value;
    }


    /**
     * Metodo per la richiesta di posizione
     * @param v la view relativa all''activity necessaria per aprire il toast
     */
    public void getPosition(View v) {
        Toast.makeText(getApplicationContext(), "POSITION REQUESTED", Toast.LENGTH_LONG).show();
        messageSender.AcquisisciPosizioneMessage();
        closeActivity();
    }

    /**
     * Metodo per chiudere l'activity e tornare alla precedente
     */
    private void closeActivity () {
        AlarmActivity.this.finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        closeActivity();
    }
}
