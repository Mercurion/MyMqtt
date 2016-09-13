package com.myclose.mymqtt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * <pre>
 * Questa classe gestisce le impostazioni CUSTOM del tracker.
 * avviata dalla classe MySet, ritorna alla classe precedente con la ricezio di conferma dei dati corretti.
 * L'invio dei parametri avviene con un oggetto Sender
 * @author Giacomo
 * @see MySetsActivity
 * @see MessageAnalyzer
 * @see Sender
 * @version 1.6
 * </pre>
 */
public class CustomSetActivity extends Activity {
    /*
    questi sono i bottoni per tornare indietro o per salvare le impostazioni
     */
    private Button saveprofile_button;
    private Button goback_button;

    /*
    questi sono i toggle (True/False)
     */
    private ToggleButton togglePerditaSegnale;
    private ToggleButton toggleBatteria;
    private ToggleButton toggleAccensione;

    private SeekBar sensibilitaLucchetto;
    private SeekBar intervalloSegnale;
    private Button infoButtonOne;
    private Button infoButtonTwo;
    private Button infoButtonThree;

    // textValue sensibilità
    private TextView lowset;
    private TextView medium;
    private TextView high;

    //textValue timeout
    private TextView textValue1;
    private TextView textValue2;
    private TextView textValue3;
    private TextView textValue4;
    private TextView textValue5;


    // qua i parametri per shared preferences
    SharedPreferences sharedpreferences;
    private boolean battery;
    private boolean perdita_segnale;
    private boolean accensione_segnale;
    private int sensibility_lock;
    private int time_out_notifica;

    //parametro che determina se lo stato "custom" è attivo
    private boolean isCustomOn;

    //parametri di comunicazione
    String sentmessage;
    private Sender messageSender = new Sender();
    MessageAnalyzer smsAnalyzer = new MessageAnalyzer();

    //parametri di controllo del tempo
    private long clickTime;
    private long currentTime;
    private static long WAITTIME = 40000;

    //handler e runnable per il controllo del time-out
    private final Handler timeHandler = new Handler();
    private Runnable timeRunnable;
    private int pro;

    //tutti gli altri parametri
    private LocalBroadcastManager lBManagerSet;
    private static final String MyPREFERENCES = "MyPrefs";
    MySqlClose myDbSet;
    private String DEV_MASTER_ID;
    private String DEV_NUMBER;
    private BroadcastReceiver smsReciverSet;

    @Override
    public void onStart() {
        super.onStart();
        lBManagerSet.registerReceiver(smsReciverSet, new IntentFilter(
                "RETURN_MESSAGE"));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_setting);
        myDbSet = MySqlClose.getIstance(getApplicationContext());
        lBManagerSet = LocalBroadcastManager.getInstance(this);

        saveprofile_button = (Button) findViewById(R.id.save_profile_button);
        goback_button = (Button) findViewById(R.id.go_setting_button);

        togglePerditaSegnale = (ToggleButton) findViewById(R.id.toggleSignalTimeOut);
        toggleBatteria = (ToggleButton) findViewById(R.id.toggleBatteria);
        toggleAccensione = (ToggleButton) findViewById(R.id.toggleMasterOnOff);

        sensibilitaLucchetto = (SeekBar) findViewById(R.id.seekBarSensitivity);
        intervalloSegnale = (SeekBar) findViewById(R.id.seekBarTimeOut);

        infoButtonOne =(Button) findViewById(R.id.info_button_one);
        infoButtonTwo = (Button) findViewById(R.id.info_button_two);
        infoButtonThree = (Button) findViewById(R.id.info_button_three);

        //textValue sensibilità
        lowset = (TextView) findViewById(R.id.lowset);
        medium = (TextView) findViewById(R.id.medium);
        high = (TextView) findViewById(R.id.high);

        //textValue timeout
        textValue1 =(TextView) findViewById(R.id.textValue1);
        textValue2 =(TextView) findViewById(R.id.textValue2);
        textValue3 =(TextView) findViewById(R.id.textValue3);
        textValue4 =(TextView) findViewById(R.id.textValue4);
        textValue5 =(TextView) findViewById(R.id.textValue5);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, getApplicationContext().MODE_PRIVATE);

        clickTime = sharedpreferences.getLong("tempo", 0L);

        smsReciverSet = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Bundle b = intent.getExtras();
                String value = b.getString("MESSAGE");
                readMessage(value, context);
            }
        };

        goback_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                goToMenuClick();
            }
        });

        saveprofile_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                openAlertInvia(arg0);
            }
        });

        togglePerditaSegnale.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                intervalloSegnale.setEnabled(getNotificaPerditaSegnaleStatus());
            }
        });

        infoButtonOne.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                infoOneClick();
            }
        });
        infoButtonTwo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                infoTwoClick();
            }
        });
        infoButtonThree.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                infoThreeClick();
            }
        });

        loadFromdb();
        getFromPreference();
        setBatteriaStatus(this.battery);
        setNotificaAccensioneMaster(this.accensione_segnale);
        setNotificaPerditaSegnale(this.perdita_segnale);
        setValueIntervalloSegnale(this.time_out_notifica);
        setValueSensibilita(this.sensibility_lock);

        //set color textValues from db
        colorTimeoutdb(time_out_notifica);
        colorSensibilitydb(sensibility_lock);


        smsAnalyzer.setMasterID(DEV_MASTER_ID);

        this.timeRunnable = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), "PROBLEMA NELL'INVIO DELLE IMPOSTAZIONI, CONTROLLARE IL MASTER. IMPOSTAZIONI NON SALVATE",
                        Toast.LENGTH_LONG).show();
            }
        };


        //set color text seekbar sensibilita
        sensibilitaLucchetto.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {

                // TODO Auto-generated method stub
                //metodo cambia colore ai textValue

                int CurrentLevel = seekBar.getProgress();
                if(CurrentLevel==0 && CurrentLevel < 0.5){
                    CurrentLevel = 0;}
                else if(CurrentLevel>=0.5 && CurrentLevel<=1){
                    CurrentLevel=1; }
                else if(CurrentLevel >=1 &&CurrentLevel<1.5){
                    CurrentLevel=1;
                }
                else if(CurrentLevel >=1.5 &&CurrentLevel<=2){
                    CurrentLevel=2;
                }
                seekBar.setProgress(CurrentLevel);

                color(progress, seekBar.getId());

                pro=progress;	//we can use the progress value of pro as anywhere
            }
        });

        intervalloSegnale.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {

                // TODO Auto-generated method stub
                color(progress,seekBar.getId());
                pro=progress;	//we can use the progress value of pro as anywhere
            }
        });


    }


    private void loadFromdb(){
        Utente u = myDbSet.getUtente();
        DEV_MASTER_ID = u.getIdMaster();
        DEV_NUMBER =  u.getIdTelMaster();
        messageSender.setMasterPhone(DEV_NUMBER);
        messageSender.setMasterID(DEV_MASTER_ID);
    }

    private void getFromPreference () {
        this.battery = sharedpreferences.getBoolean("battery_flag", false);
        this.perdita_segnale = sharedpreferences.getBoolean("perdita_segnale_flag", true);
        this.accensione_segnale = sharedpreferences.getBoolean("segnale_accensione", false);
        this.sensibility_lock = sharedpreferences.getInt("sensibility_lucchetto", 0);
        this.time_out_notifica = sharedpreferences.getInt("time_out_notifica", 0);
        this.isCustomOn = sharedpreferences.getBoolean("isCustomOn", false);
    }

    private void saveAllCurrentValues (){
        saveValueBattery(getBatteriaStatus());
        saveValuePerditaSegnale(getNotificaPerditaSegnaleStatus());
        saveValueAccensione(getNotificaAccensioneMaster());
        saveValueSensibility(getValueSensibilita());
        saveValueTimeOut(getValueIntervalloSegnale());
    }

    private void saveValueCustomOn (boolean parametro) {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("isCustomOn", parametro);
        editor.apply();
    }

    private void saveValueBattery(boolean parametro) {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("battery_flag", parametro);
        editor.apply();
    }

    private void saveValueAccensione(boolean parametro) {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("segnale_accensione", parametro);
        editor.apply();
    }

    private void saveValuePerditaSegnale(boolean parametro) {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("perdita_segnale_flag", parametro);
        editor.apply();
    }

    private void saveValueSensibility (int valore) {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt("sensibility_lucchetto", valore);
        editor.apply();
    }

    private void saveValueTimeOut (int valore) {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt("time_out_notifica", valore);
        editor.apply();

    }

    private boolean getBatteriaStatus (){
        return toggleBatteria.isChecked();
    }

    private void setBatteriaStatus (boolean value) {
        toggleBatteria.setChecked(value);
    }

    private boolean getNotificaAccensioneMaster () {
        boolean tmp;
        if (toggleAccensione.isChecked())
            tmp = true;
        else
            tmp = false;
        return tmp;
    }

    private void setNotificaAccensioneMaster (boolean value) {
        toggleAccensione.setChecked(value);
    }

    private boolean getNotificaPerditaSegnaleStatus() {
        boolean tmp;
        if (togglePerditaSegnale.isChecked())
            tmp = true;
        else
            tmp = false;
        return tmp;
    }

    private void setNotificaPerditaSegnale (boolean value) {
        togglePerditaSegnale.setChecked(value);
    }

    private int getValueSensibilita () {
        int value;
        value = sensibilitaLucchetto.getProgress();
        return value;
    }

    //da usare in avvio di activity
    private void setValueSensibilita (int value) {
        sensibilitaLucchetto.setProgress(value);
    }

    private int getValueIntervalloSegnale () {
        int value;
        value = intervalloSegnale.getProgress();
        return value;
    }

    private void setValueIntervalloSegnale (int value){
        intervalloSegnale.setProgress(value);
    }

    private void sendAllConfigurations() {
        boolean perdita_segnale;
        boolean batteria_status;
        boolean acc;
        int intervallo_notifica;
        int sens;
        sens = getValueSensibilita();
        acc = getNotificaAccensioneMaster();
        batteria_status =getBatteriaStatus();
        perdita_segnale = getNotificaPerditaSegnaleStatus();
        intervallo_notifica = getValueIntervalloSegnale() +1;

        if (perdita_segnale) {
            this.sentmessage= messageSender.TotalCustomSetMessage(batteria_status,acc,intervallo_notifica,sens);
        }
        else  {
            this.sentmessage= messageSender.TotalCustomSetMessage(batteria_status,acc,0,sens);
        }

        Toast.makeText(getBaseContext(), R.string.SendAllENG,
                Toast.LENGTH_LONG).show();
        clickTime= System.currentTimeMillis();
        this.timeHandler.postDelayed(this.timeRunnable, WAITTIME);
    }

    private void goToMenuClick () {
        if (clickTime==0)
            CustomSetActivity.this.finish();
        else {
            currentTime= System.currentTimeMillis();
            if (currentTime-clickTime>WAITTIME)
                CustomSetActivity.this.finish();
            else
                Toast.makeText(getBaseContext(), "wait for more "+(WAITTIME - currentTime+clickTime)/1000+" seconds.",
                        Toast.LENGTH_LONG).show();
        }
    }


    //implementazione metodo cambia colore ai textValue
    public void color(int progress,int seek) {

        switch(seek) {
            case R.id.seekBarTimeOut:

                if (progress==0) {

                    textValue1.setTextColor(Color.parseColor("#e8590b"));
                    textValue2.setTextColor(Color.WHITE);
                    textValue3.setTextColor(Color.WHITE);
                    textValue4.setTextColor(Color.WHITE);
                    textValue5.setTextColor(Color.WHITE);

                }
                else if (progress==1) {
                    textValue1.setTextColor(Color.WHITE);
                    textValue2.setTextColor(Color.parseColor("#e8590b"));
                    textValue3.setTextColor(Color.WHITE);
                    textValue4.setTextColor(Color.WHITE);
                    textValue5.setTextColor(Color.WHITE);
                }
                else if (progress == 2) {
                    textValue1.setTextColor(Color.WHITE);
                    textValue2.setTextColor(Color.WHITE);
                    textValue3.setTextColor(Color.parseColor("#e8590b"));
                    textValue4.setTextColor(Color.WHITE);
                    textValue5.setTextColor(Color.WHITE);

                }
                else if (progress==3) {
                    textValue1.setTextColor(Color.WHITE);
                    textValue2.setTextColor(Color.WHITE);
                    textValue3.setTextColor(Color.WHITE);
                    textValue4.setTextColor(Color.parseColor("#e8590b"));
                    textValue5.setTextColor(Color.WHITE);
                }
                else if (progress == 4) {   textValue1.setTextColor(Color.WHITE);
                    textValue1.setTextColor(Color.WHITE);
                    textValue2.setTextColor(Color.WHITE);
                    textValue3.setTextColor(Color.WHITE);
                    textValue4.setTextColor(Color.WHITE);
                    textValue5.setTextColor(Color.parseColor("#e8590b"));

                }


                break;
            case R.id.seekBarSensitivity:

                if (progress <1 && progress==0) {
                    lowset.setTextColor(Color.parseColor("#e8590b"));
                    medium.setTextColor(Color.WHITE);
                    high.setTextColor(Color.WHITE);


                }
                else if (progress ==1) {
                    lowset.setTextColor(Color.WHITE);
                    medium.setTextColor(Color.parseColor("#e8590b"));
                    high.setTextColor(Color.WHITE);
                }
                else if (progress > 1 && progress==2) {
                    lowset.setTextColor(Color.WHITE);
                    medium.setTextColor(Color.WHITE);
                    high.setTextColor(Color.parseColor("#e8590b"));

        }

                break;
            default:
                break;
        }
    }

   //implementazione colorset TextValues timeout from db
    public void colorTimeoutdb(int value){


        if (value==0) {

            textValue1.setTextColor(Color.parseColor("#e8590b"));
            textValue2.setTextColor(Color.WHITE);
            textValue3.setTextColor(Color.WHITE);
            textValue4.setTextColor(Color.WHITE);
            textValue5.setTextColor(Color.WHITE);

        }
        else if (value==1) {
            textValue1.setTextColor(Color.WHITE);
            textValue2.setTextColor(Color.parseColor("#e8590b"));
            textValue3.setTextColor(Color.WHITE);
            textValue4.setTextColor(Color.WHITE);
            textValue5.setTextColor(Color.WHITE);
        }
        else if (value == 2) {
            textValue1.setTextColor(Color.WHITE);
            textValue2.setTextColor(Color.WHITE);
            textValue3.setTextColor(Color.parseColor("#e8590b"));
            textValue4.setTextColor(Color.WHITE);
            textValue5.setTextColor(Color.WHITE);

        }
        else if (value==3) {
            textValue1.setTextColor(Color.WHITE);
            textValue2.setTextColor(Color.WHITE);
            textValue3.setTextColor(Color.WHITE);
            textValue4.setTextColor(Color.parseColor("#e8590b"));
            textValue5.setTextColor(Color.WHITE);
        }
        else if (value == 4) {   textValue1.setTextColor(Color.WHITE);
            textValue1.setTextColor(Color.WHITE);
            textValue2.setTextColor(Color.WHITE);
            textValue3.setTextColor(Color.WHITE);
            textValue4.setTextColor(Color.WHITE);
            textValue5.setTextColor(Color.parseColor("#e8590b"));

        }




    }
    //implementazione colorset TextValues sensibility from db
    public void colorSensibilitydb(int value){

        if (value==0) {
            lowset.setTextColor(Color.parseColor("#e8590b"));
            medium.setTextColor(Color.WHITE);
            high.setTextColor(Color.WHITE);


        }
        else if (value==1) {
            lowset.setTextColor(Color.WHITE);
            medium.setTextColor(Color.parseColor("#e8590b"));
            high.setTextColor(Color.WHITE);
        }
        else if (value== 2) {
            lowset.setTextColor(Color.WHITE);
            medium.setTextColor(Color.WHITE);
            high.setTextColor(Color.parseColor("#e8590b"));

        }


    }


    private void openAlertInvia(final View view) {
        currentTime= System.currentTimeMillis();
        if (clickTime==0 || currentTime-clickTime>WAITTIME) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    CustomSetActivity.this);

            alertDialogBuilder.setTitle("SEND CONFIGURATION");
            alertDialogBuilder.setMessage("Are you sure?");
            // set positive button: Yes message
            alertDialogBuilder.setPositiveButton("Yes",
                    new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                sendAllConfigurations();
                                openAlertWait(view);

                                Toast.makeText(getApplicationContext(),
                                        R.string.CommSentENG, Toast.LENGTH_LONG)
                                        .show();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
            // set negative button: No message
            alertDialogBuilder.setNegativeButton("No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            // cancel the alert box and put a Toast to the user
                            dialog.cancel();
                            Toast.makeText(getApplicationContext(),
                                    R.string.CommNOTSentENG, Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setCancelable(false);
            alertDialog.show();
        }
        else
            Toast.makeText(getBaseContext(), "Wait for "+(WAITTIME - currentTime+clickTime)/1000+" more seconds.",
                    Toast.LENGTH_LONG).show();
    }

    private void openAlertWait(View view) {
        AlertDialog.Builder alertDialogWait = new AlertDialog.Builder(CustomSetActivity.this);

        alertDialogWait.setTitle("WAIT FOR THE COMMAND TO BE SENT");
        alertDialogWait.setMessage("Wait 10 seconds");

        alertDialogWait.setNeutralButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
/*                        try {
                            Log.v("msg", "WAIT CheckFrequencyRun");
                            Thread.sleep(10000); // giving time to connect to wifi
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } */
                        // cancel the alert box
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogWait.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    public void readMessage(String value, Context context) {
        Log.println(Log.INFO,"Messaggio ricevuto",value);

        if (value.contains("Allarme") || value.contains("alarm") || value.contains("Alarm")) {
            int tmp;
            tmp = smsAnalyzer.chekTypeAlarm(value);
            Intent intent = new Intent(context, AlarmActivity.class);
            intent.putExtra("parametroIntro", tmp);
            startActivity(intent);
        }
        else if (value.contains("NACK")) {
            this.clickTime=0;
            this.timeHandler.removeCallbacks(timeRunnable);
            Toast.makeText(context, R.string.ErrorInvioEng, Toast.LENGTH_LONG).show();
        }
            else if (value.contains("CONFIRMED")) {
                if (smsAnalyzer.checkIfSameString(value,this.sentmessage)) {
                    Toast.makeText(context, R.string.ModEffettuateENG, Toast.LENGTH_LONG).show();
                    saveAllCurrentValues();
                    saveValueCustomOn(true);
                    this.clickTime=0;
                    this.timeHandler.removeCallbacks(timeRunnable);
                    CustomSetActivity.this.finish();
            } else {
                    Toast.makeText(context, R.string.ErrENG, Toast.LENGTH_LONG).show();
                    this.timeHandler.removeCallbacks(timeRunnable);
                }
        } else {
            Toast.makeText(context, R.string.ErrorInvioEng, Toast.LENGTH_LONG).show();
            this.timeHandler.removeCallbacks(timeRunnable);
        }
    }

    private void infoOneClick() {
        AlertDialog.Builder alertDialogWait = new AlertDialog.Builder(
                CustomSetActivity.this);

        alertDialogWait.setTitle("Lock sensitivity");
        alertDialogWait.setMessage(R.string.TestoInfoOne);
        alertDialogWait.setNeutralButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // cancel the alert box
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogWait.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    private void infoTwoClick() {
        AlertDialog.Builder alertDialogWait = new AlertDialog.Builder(
                CustomSetActivity.this);

        alertDialogWait.setTitle("Lock signal lost alert");
        alertDialogWait.setMessage(R.string.TestoInfoTwo);

        alertDialogWait.setNeutralButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // cancel the alert box
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogWait.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    private void infoThreeClick() {
        AlertDialog.Builder alertDialogWait = new AlertDialog.Builder(
                CustomSetActivity.this);

        alertDialogWait.setTitle("Master Switch on/off - Battery alert");
        alertDialogWait.setMessage(R.string.TestoInfoThree);

        alertDialogWait.setNeutralButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // cancel the alert box
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogWait.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        lBManagerSet.unregisterReceiver(smsReciverSet);

    }

    @Override
    public void onBackPressed() {
        if (clickTime==0)
            CustomSetActivity.this.finish();
        else {
            currentTime= System.currentTimeMillis();
            if (currentTime-clickTime>WAITTIME)
                CustomSetActivity.this.finish();
            else
                Toast.makeText(getBaseContext(), "wait for "+(WAITTIME - currentTime+clickTime)/1000+" more seconds. Waiting for the Master to answer.",
                        Toast.LENGTH_LONG).show();
        }

    }

}
