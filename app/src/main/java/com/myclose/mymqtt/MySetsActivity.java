package com.myclose.mymqtt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import net.myclose.myclose.R;

/**
* Questa classe gestisce le configurazioni TOP E STANDARD oltre all'accesso alla classe delle configurazioni CUSTOM
 */

public class MySetsActivity extends Activity {
	private Button basicButton;
	private Button topButton;
	private Button customButton;
	Button gomenu;

	/*
	parametri shared preferences
	 */
	SharedPreferences sharedpreferences;
	private int parametroBottoneStandard;
	private int parametroBottoneTop;
	private boolean isCustomOn;
	private static final String MyPREFERENCES = "MyPrefs";
	private boolean isAlarmOn;

    SmsManager smsManager;

	private Sender messageSender = new Sender();
	private MessageAnalyzer smsAnalyzer;

	/*
	parametri per il time-out
	 */
	private long clickTime;
	long currentTime;
	static final long WAITTIME = 40000;

	/*
	Handler per i time-out
	 */
	private final Handler topHandler = new Handler();
    private final Handler standardHandler = new Handler();
    private final Handler shutdownHandler = new Handler();
    private Runnable topRunnable;
    private Runnable standardRunnable;
    private Runnable shutdownRunnable;


	private LocalBroadcastManager lBManagerSet;

	MySqlClose myDbSet;
	private String DEV_MASTER_ID;
	private String DEV_NUMBER;
	private BroadcastReceiver smsReciverSet;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.myset);
		myDbSet = MySqlClose.getIstance(getApplicationContext());
		loadFromdb();

        gomenu = (Button) findViewById(R.id.menubottom);
        gomenu.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                listenerGomenu();
            }
        });

		lBManagerSet = LocalBroadcastManager.getInstance(this);
		smsManager = SmsManager.getDefault();
		basicButton = (Button) findViewById(R.id.basicset);
		topButton = (Button) findViewById(R.id.topset);
		customButton = (Button) findViewById(R.id.customset);
		sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        isCustomOn = sharedpreferences.getBoolean("isCustomOn", false);
		if (isCustomOn) {
			saveValueTastoBasic(0);
			saveValueTastoTop(0);
		}
		parametroBottoneStandard = sharedpreferences.getInt("tasto1", 0);
		parametroBottoneTop = sharedpreferences.getInt("tasto2", 0);
		clickTime = sharedpreferences.getLong("tempo", 0L);
		changeStatoTastoStandard(parametroBottoneStandard);
		changeStatoTastoTop(parametroBottoneTop);
		changeStatoTastoCustom(isCustomOn);

		smsAnalyzer = new MessageAnalyzer();
		smsAnalyzer.setMasterID(DEV_MASTER_ID);
		
		smsReciverSet = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Bundle b = intent.getExtras();
				String value = b.getString("MESSAGE");
				readMessage(value, context);
			}
		};

		setAllRunnables();
	}

	/**
	 * Questo metodo analizza i messaggi in arrivo, riguardanti questa classe e i messaggi di allarme
	 * @param value messaggio arrivato
	 * @param context context dell'applicazione, necessario per avviare altre activity
     */
	private void readMessage(String value, Context context) {
		if (value.contains("Spegnimento master")
				|| value.contains("Power off")){
			Toast.makeText(getApplicationContext(),
					"The master is shutting down",
					Toast.LENGTH_LONG).show();
			shutdownHandler.removeCallbacks(shutdownRunnable);
			MySetsActivity.this.finish();
		} else
			if (value.contains("Allarme") || value.contains("alarm") || value.contains("Alarm")) {
				int tmp;
				tmp = smsAnalyzer.chekTypeAlarm(value);
				Log.println(Log.INFO,"MYSETACTIVITY","ho ricevuto un allarme");
				Intent alarmIntent = new Intent(context, AlarmActivity.class);
				alarmIntent.putExtra("parametroIntro", tmp);
				startActivity(alarmIntent);
		} else
			if (value.contains("00 CONFIRMED")) {
				Toast.makeText(getApplicationContext(),
						"Confirmed the master to shut down",
						Toast.LENGTH_LONG).show();
				shutdownHandler.removeCallbacks(shutdownRunnable);
				MySetsActivity.this.finish();
			} else
				if (value.contains("11 CONFIRMED")) {
					//Todo:conferma la configurazione TOP
					this.clickTime =0;
					changeStatoTastoStandard(0);
					changeStatoTastoTop(1);
					changeStatoTastoCustom(false);
					Toast.makeText(getApplicationContext(),
							"TOP configuration confirmed",
							Toast.LENGTH_LONG).show();
                    topHandler.removeCallbacks(topRunnable);

                } else
				if (value.contains("10 CONFIRMED")) {
					//Todo:conferma la configurazione BASIC
					Toast.makeText(getApplicationContext(),
							"STANDARD configuration confirmed",
							Toast.LENGTH_LONG).show();
					changeStatoTastoStandard(1);
					changeStatoTastoTop(0);
					changeStatoTastoCustom(false);
					this.clickTime =0;
					standardHandler.removeCallbacks(standardRunnable);
				}
	}


	private void loadFromdb(){
		Utente u = myDbSet.getUtente();
//		 String sim= u.getIdSim();
//		 String idTel =u.getIdTel();
		DEV_MASTER_ID = u.getIdMaster();
		DEV_NUMBER =  u.getIdTelMaster();
		messageSender.setMasterPhone(DEV_NUMBER);
		messageSender.setMasterID(DEV_MASTER_ID);
	}

	private void setAllRunnables () {
		this.topRunnable = new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(),
						"OMG SOMETHING WENT WRONG",
						Toast.LENGTH_LONG).show();
			}
		};

		this.shutdownRunnable = new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(),
						"SOMETHING WENT WRONG WITH THE SHUTDOWN MESSAGE",
						Toast.LENGTH_LONG).show();
			}
		};

		this.standardRunnable = new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(),
						"SOMETHING WENT WRONG WITH THE STANDARD MESSAGE",
						Toast.LENGTH_LONG).show();
			}
		};
	}

    private void saveValueCustom (boolean parametro)  {
        Editor editor = sharedpreferences.edit();
        editor.putBoolean("isCustomOn", parametro);
        editor.apply();
    }

	private void saveValueTastoBasic(int parametro) {
		Editor editor = sharedpreferences.edit();
		editor.putInt("tasto1", parametro);
		editor.apply();
	}

	private void saveValueTastoTop(int parametro) {
		Editor editor = sharedpreferences.edit();
		editor.putInt("tasto2", parametro);
		editor.apply();
	}

	private void initialTime(long parametro) {
		Editor editor = sharedpreferences.edit();
		editor.putLong("tempo", parametro);
		editor.apply();
	}

    private void changeStatoTastoCustom (boolean parametro) {
        if (parametro) {
            customButton.setBackgroundResource(R.drawable.custom_light_eng);
            saveValueCustom(parametro);
        } else {
            customButton.setBackgroundResource(R.drawable.custom_dark_eng);
            saveValueCustom(parametro);
        }
    }

	private void changeStatoTastoStandard(int parametro) {
		switch (parametro) {
		case 0:
			basicButton.setBackgroundResource(R.drawable.standard_dark);
			saveValueTastoBasic(parametro);
			break;
		case 1:
			basicButton.setBackgroundResource(R.drawable.standard_light);
			saveValueTastoBasic(parametro);
			break;
		}
	}

	private void changeStatoTastoTop(int parametro) {
		switch (parametro) {
		case 0:
			topButton.setBackgroundResource(R.drawable.top_plan_eng_dark);
			saveValueTastoTop(parametro);
			break;
		case 1:
			topButton.setBackgroundResource(R.drawable.top_plan_eng_light);
			saveValueTastoTop(parametro);
			break;
		}
	}



	public void onToggleStandardClick(View view) {
		parametroBottoneStandard = sharedpreferences.getInt("tasto1", 1);
		currentTime = System.currentTimeMillis();

		if (parametroBottoneStandard == 1) { // nothing
			Toast.makeText(getBaseContext(), "Standard", Toast.LENGTH_SHORT)
					.show();
		} else {
			if (clickTime != 0) {
				long timePassed = currentTime - clickTime;
				if (timePassed >= WAITTIME) {
					onClickBasic(view); // old
				} else {
					int timeRemaining = (int) ((WAITTIME - timePassed) / 1000);
					Toast.makeText(getApplicationContext(),
							"Wait " + timeRemaining + " More Seconds",
							Toast.LENGTH_LONG).show();
				}
			} else { // old

				onClickBasic(view); // old

			}
		}
	}

	public void onToggleTopClick(View view) {
		parametroBottoneTop = sharedpreferences.getInt("tasto2", 0);
		currentTime = System.currentTimeMillis();

		if (parametroBottoneTop == 0) {
			if (clickTime != 0) {
				long timePassed = currentTime - clickTime;
				if (timePassed >= WAITTIME) {
					onClickTop(view); // old

				} else {
					int timeRemaining = (int) ((WAITTIME - timePassed) / 1000);
					Toast.makeText(getApplicationContext(),
							"Wait " + timeRemaining + " More Seconds",
							Toast.LENGTH_LONG).show();
				}
			} else { // old

				onClickTop(view); // old

			}

		} else {
			Toast.makeText(getBaseContext(), "Top", Toast.LENGTH_SHORT).show();
		}
	}

    public void onToggleClickedCustom (View view) {
        currentTime = System.currentTimeMillis();

            if (clickTime != 0) {
                long timePassed = currentTime - clickTime;
                if (timePassed >= WAITTIME) {
                    Intent openCustomSet = new Intent(MySetsActivity.this,CustomSetActivity.class);
                    startActivity(openCustomSet); // old

                } else {
                    int timeRemaining = (int) ((WAITTIME - timePassed) / 1000);
                    Toast.makeText(getApplicationContext(),
                            "Wait " + timeRemaining + " More Seconds",
                            Toast.LENGTH_LONG).show();
                }
            } else { // old
                Intent openCustomSet = new Intent(MySetsActivity.this,CustomSetActivity.class);
                startActivity(openCustomSet);
            }
    }

	private void listenerGomenu() {
        currentTime = System.currentTimeMillis();

        if (clickTime != 0) {
            long timePassed = currentTime - clickTime;
            if (timePassed >= WAITTIME) {
                MySetsActivity.this.finish();
            } else {
                int timeRemaining = (int) ((WAITTIME - timePassed) / 1000);
                Toast.makeText(getApplicationContext(),
                        "Wait " + timeRemaining + " More Seconds",
                        Toast.LENGTH_LONG).show();
            }
        } else {
            MySetsActivity.this.finish();
        }

	}




	public void onClickShutdown(View v) {
		currentTime= System.currentTimeMillis();
		if (clickTime==0 || currentTime-clickTime> WAITTIME)
			openAlert(v);
		else
			Toast.makeText(getBaseContext(), "Wait more"+(WAITTIME - currentTime+clickTime)/1000+" seconds.",
					Toast.LENGTH_LONG).show();
	}

	private void openAlert(View view) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				MySetsActivity.this);

		alertDialogBuilder.setTitle("SHUTDOWN");
		alertDialogBuilder.setMessage("Are you sure?");
		// set positive button: Yes message
		alertDialogBuilder.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {

						try {
							messageSender.ShutdownMessage();
							Toast.makeText(getApplicationContext(),
									"Command SMS Sent!", Toast.LENGTH_LONG)
									.show();
							shutdownHandler.postDelayed(shutdownRunnable,WAITTIME);

						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
						}

					}
				}).setNegativeButton("No",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

						// cancel the alert box and put a Toast to the user
						dialog.cancel();

					}
				});
		// set negative button: No message

		AlertDialog alertDialog = alertDialogBuilder.create();
		// show alert
		alertDialog.show();
	}

	public void onClickBasic(View v) {

		openAlertBasic(v);

	}

	private void openAlertBasic(View view) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				MySetsActivity.this);

		alertDialogBuilder.setTitle("STANDARD PROTECTION");
		alertDialogBuilder.setMessage("Are you sure?");
		// set positive button: Yes message
		alertDialogBuilder.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						clickTime = System.currentTimeMillis();
						initialTime(clickTime);

						try {
							messageSender.BasicConfigurationMessage();
							Toast.makeText(getApplicationContext(),
									"Command SMS Sent!", Toast.LENGTH_LONG)
									.show();
							standardHandler.postDelayed(standardRunnable,WAITTIME);

						} catch (Exception e) {
							// TODO: handle exception
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

					}
				});

		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.setCancelable(false);
		alertDialog.show();
	}

	public void onClickTop(View v) {

		openAlertTop(v);

	}

	private void openAlertTop(View view) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				MySetsActivity.this);

		alertDialogBuilder.setTitle("TOP PROTECTION");
		alertDialogBuilder.setMessage("Are you sure?");
		// set positive button: Yes message
		alertDialogBuilder.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						clickTime = System.currentTimeMillis();
						initialTime(clickTime);
						try {
							messageSender.TopConfigurationMessage();
							/*
							sendSMS(DEV_NUMBER, DEV_MASTER_ID + " "
									+ "11 TOP ON");
									*/

							Toast.makeText(getApplicationContext(),
									"Command TOP SMS Sent!", Toast.LENGTH_LONG)
									.show();
                            topHandler.postDelayed(topRunnable,WAITTIME);

						} catch (Exception e) {
							// TODO: handle exception
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

					}
				});

		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.setCancelable(false);
		alertDialog.show();
	}


	@Override
	public void onStart() {
		super.onStart();
		isCustomOn = sharedpreferences.getBoolean("isCustomOn", false);
		if (isCustomOn) {
			saveValueTastoBasic(0);
			saveValueTastoTop(0);
		}
		parametroBottoneStandard = sharedpreferences.getInt("tasto1", 0);
		parametroBottoneTop = sharedpreferences.getInt("tasto2", 0);
		clickTime = sharedpreferences.getLong("tempo", 0L);
        changeStatoTastoStandard(parametroBottoneStandard);
        changeStatoTastoTop(parametroBottoneTop);
        changeStatoTastoCustom(isCustomOn);

		lBManagerSet.registerReceiver(smsReciverSet, new IntentFilter(
				"RETURN_MESSAGE"));
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		// implement last opertion before crash!
		lBManagerSet.unregisterReceiver(smsReciverSet);
	}

	@Override
	public void onPause () {
		super.onPause();
		// implement last opertion before crash!
		lBManagerSet.unregisterReceiver(smsReciverSet);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		if (clickTime==0 || currentTime-clickTime> WAITTIME) {

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					MySetsActivity.this);

			alertDialogBuilder.setTitle("Go Back");
			alertDialogBuilder.setMessage("Are you sure?");
			// set positive button: Yes message
			alertDialogBuilder.setPositiveButton("Yes",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int id) {
							MySetsActivity.this.finish();
						}
					});
			// set negative button: No message
			alertDialogBuilder.setNegativeButton("No",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// cancel the alert box and put a Toast to the user
							dialog.cancel();
						}
					});

			AlertDialog alertDialog = alertDialogBuilder.create();

			alertDialog.show();
		} else {
			Toast.makeText(getBaseContext(), "Wait more"+(WAITTIME - currentTime+clickTime)/1000+" seconds.",
					Toast.LENGTH_LONG).show();
		}
	}
}
