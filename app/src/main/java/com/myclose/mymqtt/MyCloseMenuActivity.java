package com.myclose.mymqtt;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import android.graphics.Point;
import android.telephony.TelephonyManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.telephony.SmsManager;
import android.content.DialogInterface;
import android.content.SharedPreferences.Editor;

/**
 * <pre>
 * Questa Classe gestisce il menù di ingresso all'applicazione MyClose:
 * Da qui è possibile andare alla classe dei Numeri e dei setting.
 * Se l'allarme era ON, attiva in automatico la verifica (con messaggio STATUS), altrimenti si pone in ALARM OFF.
 * In questa classe è possibile gestire gli stati di ALARM ON/OFF e passare da uno all'altro.
 * Da questa classe è possibile chiedere la posizione.
 *@author Giacomo
 * @see Sender
 * @see MessageAnalyzer
 * @see AlarmActivity
 * @see MySetsActivity
 * @see MyPhoneActivity
 * </pre>
 */

@SuppressLint("SimpleDateFormat") public class MyCloseMenuActivity extends Activity implements DeleteInterface {

	SmsManager smsManager;
	private Button numberButton; //bottone dei numeri
	private Button buttonSetting; //bottone per il setting
	private Button b;
	private String DEV_MASTER_ID;
	private String DEV_NUMBER;
	private Button mainButton;


	Animation buttonClickOn;
	Animation buttonAlert;
	AnimationDrawable sincroAnimationAllOk;
	AnimationDrawable sincroAnimationPhoneProb;
	AnimationDrawable sincroAnimationLockProb;
	AnimationDrawable sincroAnimationLockOk;
	AnimationDrawable sincroAnimationMasterProb;
	//wil
	AnimationDrawable sincroAnimationPhone;
	//end wil

	MySqlClose myDbInstance;

	/*
	parametri sharedpreferences
	 */
	SharedPreferences sharedpreferences;
	private static final String MyPREFERENCES = "MyPrefs";
	private boolean isAlarmOn;

	private BroadcastReceiver smsReciver;
	private LocalBroadcastManager lBManager;

	/*
	variabili handler e runnables per gestire i passaggi grafici
	 */
	private final Handler phoneProbHandler = new Handler();
	private final Handler myHandlerSearch = new Handler();
	private final Handler myHandlerStatus= new Handler();
	private final Handler myHandlerAlarmON= new Handler();
	private final Handler myHandlerAlarmOFF= new Handler();


	private Runnable statusRunnable;
	private Runnable alarmONRunnable;
	private Runnable alarmOFFRunnable;
	private Runnable phoneProbRunnable;
	private Runnable phoneOkRunnable;
	private int setTimeMessage;
	private int setTimeMessageSearch;
	private int setTimeMessageOff;

	/*
	variabili per l'invio e la lettura dei messaggi
	 */
	private Sender smsSender;
	private MessageAnalyzer smsAnalyzer;

	private ListView mListView;
	private ListaAdapter mAdapter;
	List<String> notifications;

	/*
	variabili di tempo
	 */

	private long generalSendTime = 0;
	private long currentTime;
	private long timeoutAckSearch;
	private long currentTimeoutSearch;
	static final long WAITTIMEOUT = 90000;
	static final long WAITTIMEOUTSTATUS = 75000;
	private boolean waitingForStatus = false;
	private boolean waitingForAlarmON = false;
	private boolean waitingForAlarmOFF = false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.myclosemenu);

		// controllo sessione utente valida

		// se utente valido vai a
		// altrimenti login
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mListView = (ListView) findViewById(R.id.listview);
		notifications = new ArrayList<String>();

		mAdapter = new ListaAdapter(getApplicationContext(), notifications);

		mListView.setAdapter(mAdapter);

		myDbInstance = MySqlClose.getIstance(getApplicationContext());
		loadFromdb();
		lBManager = LocalBroadcastManager.getInstance(this);

		if (myDbInstance.getUtente() == null) {

			Toast.makeText(getApplicationContext(), "Utente NON esistente",
					Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(getApplicationContext(),
					MainActivity.class);
			startActivity(intent);
			this.finish();
		} else {
			Toast.makeText(getApplicationContext(), "Welcome",
					Toast.LENGTH_LONG).show();

		}

		smsSender = new Sender();
		smsSender.setMasterPhone(DEV_NUMBER);
		smsSender.setMasterID(DEV_MASTER_ID);
		smsAnalyzer = new MessageAnalyzer();
		smsAnalyzer.setMasterID(DEV_MASTER_ID);

		mainButton =  (Button) findViewById(R.id.toggle);
		addListenerOnButtonNumbers();
		addListenerOnButtonSetting();
		addListenerOnMainButton();

		b = (Button) findViewById(R.id.alert_button);
		setVisibleAlert();
		sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

		this.isAlarmOn = sharedpreferences.getBoolean("alarm", true);

		smsReciver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Bundle b = intent.getExtras();
				String value = b.getString("MESSAGE");
				readMessage(value, context);
			}


		};

		sincroAnimationPhoneProb = new AnimationDrawable();
		sincroAnimationAllOk = new AnimationDrawable();
		sincroAnimationPhone=new AnimationDrawable();
		sincroAnimationLockProb = new AnimationDrawable();
		sincroAnimationLockOk = new AnimationDrawable();
		sincroAnimationMasterProb = new AnimationDrawable();


		setSincroAnimationMasterProblem();

		setRunnables();
		if (!isAlarmOn) {
			mainButton.setBackgroundResource(R.drawable.myclose1);
			setNoAlarmBackground();
		}
		else {
			//se non c'è rete, allora parte l'animazione del problema sul telefono telefono. altrimenti parte l'animazione per il telefono a posto, manda status e aspetta
			if (!isMobileAvailable(getBaseContext())) {
				mainButton.setBackgroundResource(R.drawable.myclose1);
				this.phoneProbHandler.postDelayed(this.phoneProbRunnable, 3000);
				Handler tmp = new Handler();
				tmp.postDelayed(new Runnable() {
					@Override
					public void run() {
						sincroAnimationPhoneProb.stop();
						mainButton.clearAnimation();
					}
				}, 20000);
			} else {
				setSincroAnimationPhone();
				mainButton.clearAnimation();
				mainButton.setBackgroundResource(R.drawable.myclose2);
				Handler tmp = new Handler();
				tmp.postDelayed(new Runnable() {
					@Override
					public void run() {
						mainButton.setBackground(sincroAnimationPhone);
						sincroAnimationPhone.start();
					}
				}, 2000);
				sendStatus();
			}
		}

	}


	private void setSincroAnimationPhone() {
		sincroAnimationPhone.addFrame(getResources().getDrawable(R.drawable.myclose3), 2000);
		sincroAnimationPhone.addFrame(getResources().getDrawable(R.drawable.myclose4), 2000);
		sincroAnimationPhone.setOneShot(false);

	}

	/**
	 * check network connection
	 * @param appcontext actual context to check for telefony service
	 */
	private static Boolean isMobileAvailable(Context appcontext) {
		TelephonyManager tel = (TelephonyManager) appcontext.getSystemService(Context.TELEPHONY_SERVICE);
		return ((tel.getNetworkOperator() != null && tel.getNetworkOperator().equals("")) ? false : true);
	}


	private void setSincroAnimationMasterProblem () {
		sincroAnimationMasterProb.addFrame(getResources().getDrawable(R.drawable.myclose8), 1000);
		sincroAnimationMasterProb.addFrame(getResources().getDrawable(R.drawable.myclose9), 1000);
		sincroAnimationMasterProb.setOneShot(false);
	}


	private void setSincroAnimationLockProblem () {
		sincroAnimationLockProb.addFrame(getResources().getDrawable(R.drawable.myclose11), 1000);
		sincroAnimationLockProb.addFrame(getResources().getDrawable(R.drawable.myclose12), 1000);
		sincroAnimationLockProb.setOneShot(false);
	}

	private void setSincroAnimationLockFinal () {
		sincroAnimationLockOk.addFrame(getResources().getDrawable(R.drawable.myclose14), 1000);
		sincroAnimationLockOk.addFrame(getResources().getDrawable(R.drawable.myclose15), 1000);
		sincroAnimationLockOk.setOneShot(false);
	}

	private void setSincroAnimationPhoneProblem () {
		sincroAnimationPhoneProb.addFrame(getResources().getDrawable(R.drawable.myclose5), 1000);
		sincroAnimationPhoneProb.addFrame(getResources().getDrawable(R.drawable.myclose6), 1000);
		sincroAnimationPhoneProb.setOneShot(false);
	}

	/**
	 * ferma le animazioni in corso e imposta lo sfondo in modalità senza allarmi.
	 */
	private void setNoAlarmBackground () {
		mainButton.clearAnimation();
		mainButton.setBackgroundResource(R.drawable.myclose1);
	}

	/**
	 * Set all the runnables needed to guarantee the correct flow of the graphic reactions
	 */
	private void setRunnables () {
		this.statusRunnable = new Runnable() {
			/*
			questo runnable parte se non arriva la risposta al messaggio status e quindi il master è in errore
			 */
			@Override
			public void run() {

				if (waitingForStatus) {
					Toast.makeText(getApplicationContext(), "COMMAND STATUS NOT CONFIRMED",
							Toast.LENGTH_LONG).show();
					waitingForStatus = false;
					isAlarmOn =false;
					saveAlarmStatus(isAlarmOn);
					//showNotification();
					sincroAnimationPhone.stop();
					mainButton.clearAnimation();
					generalSendTime =0;
					masterProblemAnimation();
					sincroAnimationMasterProb.start();
					animationAlert();

					String messaggio = "Something Wrong: COMMAND STATUS NOT CONFIRMED";
					String messaggio1 = tempoMessaggio(messaggio);
					notifications.add(messaggio1);
					setVisibleAlert();
					mAdapter.notifyDataSetChanged();
					openStatus();
					Toast.makeText(getApplicationContext(), "DID NOT RETRIVE ANSWER FROM MASTER",
							Toast.LENGTH_LONG).show();
				}

			}
		};

		this.alarmONRunnable = new Runnable() {
			/*
			questo runnable parte se non arriva la risposta al messaggio di ALARM ON
			 */
			@Override
			public void run() {

				if (waitingForAlarmON){
					Toast.makeText(getApplicationContext(), "Command ALARM ON NOT CONFIRMED",
							Toast.LENGTH_LONG).show();
					waitingForAlarmON = false;
					sincroAnimationPhone.stop();
					masterProblemAnimation();
					sincroAnimationMasterProb.start();
					showNotification();

					animationAlert();

					String messaggio = "Something Wrong: Command ALARM ON NOT CONFIRMED";
					String messaggio1 = tempoMessaggio(messaggio);
					notifications.add(messaggio1);

					setVisibleAlert();
					mAdapter.notifyDataSetChanged();
				}
			}
		};

		this.alarmOFFRunnable = new Runnable() {
			/*
			questo runnable parte se non arriva la risposta al messaggio di ALARM OFF
			 */
			@Override
			public void run() {

				if (waitingForAlarmOFF) {
					Toast.makeText(getApplicationContext(),"Command ALARM OFF NOT CONFIRMED", Toast.LENGTH_LONG).show();
					waitingForAlarmOFF = false;
				}

			}
		};

		this.phoneProbRunnable = new Runnable() {
			/*
            questo runnable parte se non c'è rete sms
             */
			@Override
			public void run() {
				setSincroAnimationPhoneProblem();
				mainButton.setBackground(sincroAnimationPhoneProb);
				sincroAnimationPhoneProb.start();
			}
		};

		this.phoneOkRunnable = new Runnable() {
			/*
            questo runnable parte se c'è rete sms
             */
			@Override
			public void run() {
				setSincroAnimationPhone();
				mainButton.setBackground(sincroAnimationPhone);
				sincroAnimationPhone.start();
			}
		};

	}

	/**
	 * Questo metodo verifica se si è in attesa di una risposta dal Master. se si sta aspettando, ci si deve fermare.
	 * @return true se è possibile reagire al click, false se si è in attesa di risposta dal master
	 */
	private boolean checkIfCanClick () {
		if (!waitingForStatus && !waitingForAlarmOFF && !waitingForAlarmON)
			return true;
		else
			return false;
	}

	/**
	 * determina il comportamento dopo aver ricevuto il messaggio di status
	 * @param value value è il corrispettivo della risposta analisi del messaggio. vedere su messageAnalyzer per maggiori informazioni
	 * @see MessageAnalyzer
	 */
	private void analisiStatus (int value) {
		sincroAnimationPhone.stop();
		mainButton.clearAnimation();
		mainButton.setBackgroundResource(R.drawable.myclose10);
		notifications.clear();
		setVisibleAlert();
		mAdapter.notifyDataSetChanged();
		if (value == 1 ) { //lucchetto attivo e senza allarmi
			setSincroAnimationLockFinal();
			mainButton.setBackground(sincroAnimationLockOk);
			sincroAnimationLockOk.start();
			Handler tmp = new Handler();
			tmp.postDelayed(new Runnable () {
				@Override
				public void run() {
					sincroAnimationLockOk.stop();
					mainButton.clearAnimation();
					mainButton.setBackgroundResource(R.drawable.myclose16);
				}
			},15000);
		}

		//TODO: questi 2 punti non sono ancora stati decisi.
		if (value == 2) {
			setSincroAnimationLockProblem();
			mainButton.setBackground(sincroAnimationLockProb);
			sincroAnimationLockProb.start();
		}

		if (value == 3) {
			setSincroAnimationLockProblem();
			mainButton.setBackground(sincroAnimationLockProb);
			sincroAnimationLockProb.start();
		}
	}

	private void mainButtonClick () {
		if (this.isAlarmOn)
			smsSender.AlarmOFFMessage();
		if (!this.isAlarmOn)
			smsSender.AlarmONMessage();
	}

	/**
	 * Questo metodo viene chiamato per caricare i dati dal DB. I dati necessari sono Id del master e numero di telefono.
	 */
	private void loadFromdb() {
		Utente u = myDbInstance.getUtente();
//		String sim = u.getIdSim();
//		String idTel = u.getIdTel();
		DEV_MASTER_ID = u.getIdMaster();
		DEV_NUMBER = u.getIdTelMaster();
	}

	public void setVisibleAlert() {
		if ((notifications.size() == 0)) {

			b.setClickable(false);
			b.setBackgroundResource(R.drawable.alertbottonhide);
		} else {
			b.setClickable(true);
		}
	}

	/**
	 * Questo metodo imposta la schermata al rientro, in base allo stato di allarme.
	 */
	private void checkAlarmStatusWhenBack() {
		this.isAlarmOn = sharedpreferences.getBoolean("alarm", true);
		if(!isAlarmOn)
			mainButton.setBackgroundResource(R.drawable.myclose1);
		else
			mainButton.setBackgroundResource(R.drawable.myclose16);
	}


	public void animationAlert() {
		b.setBackgroundResource(R.drawable.alertbotton);
		// alertButton.setBackgroundResource(R.drawable.alertbotton);
		buttonAlert = AnimationUtils.loadAnimation(getApplicationContext(),
				R.anim.animationalert);
		b.startAnimation(buttonAlert);
	}


	/**
	 * metodo per la lettura dei messaggi. avviato dal service che legge i messaggi in arrivo
	 * @param value il messaggio arrivato dal master
	 * @param context
	 */
	private void readMessage(String value, Context context) {
		String totValue = value;
		if (!value.contains("NACK")) {
			value = value.replace(" ", "");
			value = value.substring(0, 21);
			String temp = DEV_MASTER_ID.toString().substring(8, 10);
			if (value.equals("Attention!PoweroffMas")
					|| value.equals("Attenzione!Spegniment")
					|| value.equals("Mastericlosefermen." + temp)
					|| value.equals("Achtung!Deaktivierung")
					|| value.equals("Atencion!ApagadoMaste")
					|| value.equals("Attentie!UitzettenMas")) {
				MyCloseMenuActivity.this.finish();
			} else if (value.equals("Attenzione!Allarmebas")
					|| value.equals("Alertesignalradiofaib")
					|| value.equals("Attention!Lowradiosig")
					|| value.equals("Achtung!Funksignalsch")
					|| value.equals("Atencion!Alarmase�alb")
					|| value.equals("Attentie!Laagradiosig")) {
				String messaggio = "Attention! Low Radio Signal";
				notifications.add(messaggio);
				mAdapter.notifyDataSetChanged();
			} else if (value.equals("Attenzione!Livellobat")
					|| value.equals("AlertebatteriefaibleM")
					|| value.equals("Attention!Lowbatteryc")
					|| value.equals("Achtung!Batteriewarnu")
					|| value.equals("Atencion!Nivelbateria")
					|| value.equals("Attentie!Batterijbijn")) {
				// sharepreference get

				String messaggio = "Attention! Low Battery on Master!";
				notifications.add(messaggio);
				mAdapter.notifyDataSetChanged();
			}

		/*
		lettura messaggi di allarme
		 */
			else if (value.contains("Allarme") || value.contains("Alarm") || value.contains("Movement")) {
				int tmp;
				tmp = smsAnalyzer.chekTypeAlarm(totValue);
				waitingForStatus = false;
				waitingForAlarmOFF = false;
				waitingForAlarmON = false;
				Intent intent = new Intent(context, AlarmActivity.class);
				intent.putExtra("parametroIntro", tmp);
				startActivity(intent);
			}


		/*
		lettura del messaggio di STATUS
		*/

			else if (value.contains("STATUS")) {
				int tmp;
				tmp = smsAnalyzer.checkStatus(totValue);
				waitingForStatus = false;
				analisiStatus(tmp);
			} else if (value.equals(DEV_MASTER_ID + "03CONFIRMED")) {
				Log.println(Log.INFO, "Messaggio ricevuto", "messaggio di alarm on");
				waitingForAlarmON = false;
				currentTime = System.currentTimeMillis();

				long timePassedout = currentTime - generalSendTime;
				if (timePassedout < WAITTIMEOUT) {
					Log.println(Log.INFO, "operazione", "analisi di alarm on");
					sincroAnimationPhone.stop();
					mainButton.clearAnimation();
					isAlarmOn = true;
					saveAlarmStatus(isAlarmOn);

					setSincroAnimationLockFinal();
					mainButton.setBackground(sincroAnimationLockOk);
					sincroAnimationLockOk.start();
					Handler tmp = new Handler();
					tmp.postDelayed(new Runnable() {
						@Override
						public void run() {
							sincroAnimationLockOk.stop();
							mainButton.clearAnimation();
							mainButton.setBackgroundResource(R.drawable.myclose16);
						}
					}, 10000);
					Toast.makeText(context, "CONFIRMED ALARM ON",
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(context, "TIMEOUT Command ALARM ON ", Toast.LENGTH_SHORT).show();
				}
			} else if (value.equals(DEV_MASTER_ID + "00CONFIRMED")) {
				MyCloseMenuActivity.this.finish();
			} else if (value.equals("http://i-close.it/t.p")) {

				currentTimeoutSearch = System.currentTimeMillis();
				long timePassedoutSearch = currentTimeoutSearch - timeoutAckSearch;

				if (timePassedoutSearch < WAITTIMEOUT) {
					setTimeMessageSearch = 1;

					Toast.makeText(context, "CONFIRMED POSITION REQUESTED",
							Toast.LENGTH_LONG).show();
				} else {
					setTimeMessageSearch = 0;
					Toast.makeText(context, "TIMEOUT Command POSITION REQUESTED", Toast.LENGTH_SHORT).show();
				}

			} else if (value.equals(DEV_MASTER_ID + "02CONFIRMED")) {
				/*
				Se arriva la conferma di alarmOFF
				 */
				waitingForAlarmOFF = false;
				currentTime = System.currentTimeMillis();
				long timePassedoutOff = currentTime - generalSendTime;
				if (timePassedoutOff < WAITTIMEOUT) {
					setNoAlarmBackground();
					isAlarmOn = false;
					saveAlarmStatus(isAlarmOn);
					Toast.makeText(context, "CONFIRMED ALARM OFF",
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(context, "TIMEOUT Command CONFIRMED ALARM OFF", Toast.LENGTH_SHORT).show();
				}
			}
		}
		else
			Toast.makeText(context, "COMANDO NON VALIDO", Toast.LENGTH_LONG).show();
	}


	/**
	 * questo metodo salva l'attuale stato di allarme.
	 * @param parametro boolean che rappresenta l'allarme: TRUE è ON, FALSE altrimenti
	 */
	private void saveAlarmStatus (boolean parametro) {
		Editor editor = sharedpreferences.edit();
		editor.putBoolean("alarm", parametro);
		editor.apply();
	}


	public void showNotification() {

		// define sound URI, the sound to be played when there's a notification
		//
		// Uri soundUri =
		// RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

		// intent triggered, you can add other intent for other actions

		Intent intent = new Intent(MyCloseMenuActivity.this,
				MyCloseMenuActivity.class);

		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pIntent = PendingIntent.getActivity(
				MyCloseMenuActivity.this, 0, intent, 0);

		// this is it, we'll build the notification!

		// in the addAction method, if you don't want any icon, just set the
		// first param to 0

		Notification mNotification = new Notification.Builder(this)

				.setContentTitle("Something Wrong!,Check MyClose!")

				.setContentText("check here!")

				.setSmallIcon(R.drawable.ic_launcher)

				.setContentIntent(pIntent)

				// .setSound(soundUri)

				// .addAction(R.drawable.notificalu, "View", pIntent)
				//
				// .addAction(0, "Remind", pIntent)

				.build();

		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		// If you want to hide the notification after it was selected, do the
		// code below

		mNotification.flags |= Notification.FLAG_AUTO_CANCEL;
		mNotification.defaults |= Notification.DEFAULT_VIBRATE;

		notificationManager.notify(0, mNotification);

	}


	/**
	 * Aggiunge il listener per il bottone di ALARM ON/OFF
	 */
	private void addListenerOnMainButton() {
		mainButton = (Button) findViewById(R.id.toggle);
		mainButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				isAlarmOn = sharedpreferences.getBoolean("alarm", true);
				currentTime = System.currentTimeMillis();
				if (checkIfCanClick()) {
					if (isAlarmOn) {
						openAlertonoff(v);}
					else {
						openAlertOffOn(v);
					}
				} else {
					if (waitingForStatus) {
						long timePassed = currentTime - generalSendTime;
						int timeRemaining = (int) ((WAITTIMEOUTSTATUS - timePassed) / 1000);
						Toast.makeText(getApplicationContext(), "Waiting for the Master...",
								Toast.LENGTH_LONG).show();
					} else {
						long timePassed = currentTime - generalSendTime;
						int timeRemaining = (int) ((WAITTIMEOUT - timePassed) / 1000);
						Toast.makeText(getApplicationContext(), "Waiting for the Master...",
								Toast.LENGTH_LONG).show();
					}
				}
			}
		});
	}

	/**
	 * Questo metodo carica l'animazione per il bottone del telefono.
	 */
	private void phoneAnimation(){
		mainButton =  (Button) findViewById(R.id.toggle);
		mainButton.setBackground(sincroAnimationPhone);
	}

	/**
	 * Questo metodo carica l'animazione per il problema sul Master, se non c'è risposta dal messaggio di status.
	 */
	private void masterProblemAnimation(){
		mainButton =  (Button) findViewById(R.id.toggle);
		mainButton.setBackground(sincroAnimationMasterProb);
	}


	/**
	 * Metodo che gescisce l'invio del comando di status
	 */
	private void sendStatus (){
		if (!isMobileAvailable(getApplicationContext()))
			Toast.makeText(getApplicationContext(), "No mobile connection, can't send status message",
					Toast.LENGTH_LONG).show();
		else {
			smsSender.StatusMessage();
			waitingForStatus = true;
			generalSendTime = System.currentTimeMillis();
			myHandlerStatus.postDelayed(statusRunnable,WAITTIMEOUTSTATUS);
		}
	}


	/**
	 *  Metodo per l'invio del comando di ALARM ON
	 * @param view necessaria per aprire l'alert
	 */
	private void openAlertOffOn(View view) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				MyCloseMenuActivity.this);

		alertDialogBuilder.setTitle("ALARM ON");
		alertDialogBuilder.setMessage("Are you sure?");
		// set positive button: Yes message
		alertDialogBuilder.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						generalSendTime = System.currentTimeMillis();
						setSincroAnimationPhone();
						phoneAnimation();
						sincroAnimationPhone.start();
						myHandlerAlarmON.postDelayed(alarmONRunnable, WAITTIMEOUT);
						try {
							smsSender.AlarmONMessage();
							waitingForAlarmON = true;
							Toast.makeText(getApplicationContext(), "Command SMS Sent!", Toast.LENGTH_LONG)
									.show();
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



	/**
	 * Questo metodo apre un alert se lo status non riceve risposta
	 */
	private void openStatus (){

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				MyCloseMenuActivity.this);

		alertDialogBuilder.setTitle("CHECK YOUR MASTER!");
		alertDialogBuilder.setMessage("RESTART SYSTEM CHECK?");
		// set positive button: Yes message
		alertDialogBuilder.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						sendStatus();
						mainButton.clearAnimation();
						mainButton.setBackground(sincroAnimationPhone);
						sincroAnimationPhone.start();
					}
				});
		// set negative button: No message
		alertDialogBuilder.setNegativeButton("No",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

						sincroAnimationPhone.stop();
						setNoAlarmBackground();
						isAlarmOn = false;
						saveAlarmStatus(isAlarmOn);
						// cancel the alert box and put a Toast to the user
						dialog.cancel();
					}
				});

		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		WindowManager.LayoutParams wmlp= alertDialog.getWindow().getAttributes();
		wmlp.gravity= Gravity.CENTER|Gravity.BOTTOM;
		Display display= getWindowManager().getDefaultDisplay();
		Point size= new Point();
		display.getSize(size);
		int width= size.y;

		wmlp.x=0;
		wmlp.y=(int)(width*0.10);
		alertDialog.setCancelable(false);
		alertDialog.show();
	}


	/**
	 *  Metodo per l'invio del comando di ALARM OFF
	 * @param view necessaria per aprire l'alert
	 */
	private void openAlertonoff(View view) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MyCloseMenuActivity.this);

		alertDialogBuilder.setTitle("ALARM OFF");
		alertDialogBuilder.setMessage("Are you sure?");
		// set positive button: Yes message
		alertDialogBuilder.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						generalSendTime = System.currentTimeMillis();
						myHandlerAlarmOFF.postDelayed(alarmOFFRunnable, WAITTIMEOUT);

						try {
							smsSender.AlarmOFFMessage();
							Toast.makeText(getApplicationContext(),"Command SMS Sent!", Toast.LENGTH_LONG).show();
							waitingForAlarmOFF = true;
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

	/**
	 * questo metodo aggiunge un listener sul bottone che apre la pagine per l'inserimento dei numeri di telefono da contattare
	 */
	private void addListenerOnButtonNumbers() {

		final Context context = this;

		numberButton = (Button) findViewById(R.id.numbers);

		numberButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (checkIfCanClick()) {
					Intent intent = new Intent(context, MyPhoneActivity.class);
					startActivity(intent);
				} else {
					if (waitingForStatus) {
						long timePassed = currentTime - generalSendTime;
						int timeRemaining = (int) ((WAITTIMEOUTSTATUS - timePassed) / 1000);
						Toast.makeText(getApplicationContext(), "Waiting for the Master...",
								Toast.LENGTH_LONG).show();
					} else {
						long timePassed = currentTime - generalSendTime;
						int timeRemaining = (int) ((WAITTIMEOUT - timePassed) / 1000);
						Toast.makeText(getApplicationContext(), "Waiting for the Master...",
								Toast.LENGTH_LONG).show();
					}
				}
			}
		});
	}

	/**
	 * questo metodo aggiunge un listener sul bottone che apre le impostazione del setting di Myclose
	 */
	private void addListenerOnButtonSetting() {

		final Context context = this;
		buttonSetting = (Button) findViewById(R.id.set);

		buttonSetting.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (checkIfCanClick()) {
					Intent intent = new Intent(context, MySetsActivity.class);
					startActivity(intent);
				} else {
					if (waitingForStatus) {
						long timePassed = currentTime - generalSendTime;
						int timeRemaining = (int) ((WAITTIMEOUTSTATUS - timePassed) / 1000);
						Toast.makeText(getApplicationContext(), "Waiting for the Master...",
								Toast.LENGTH_LONG).show();
					} else {
						long timePassed = currentTime - generalSendTime;
						int timeRemaining = (int) ((WAITTIMEOUT - timePassed) / 1000);
						Toast.makeText(getApplicationContext(), "Waiting for the Master...",
								Toast.LENGTH_LONG).show();
					}
				}

			}

		});

	}

	/**
	 * Questo metodo è avviato cliccando sul bottone "search my position". apre il metodo che gestisce l'alert
	 * @param v - la view necessaria per aprire l'alert dialog
	 */
	public void onClicksms(View v) {
		if (checkIfCanClick())
			openAlert(v);
		else {
			if (waitingForStatus) {
				long timePassed = currentTime - generalSendTime;
				int timeRemaining = (int) ((WAITTIMEOUTSTATUS - timePassed) / 1000);
				Toast.makeText(getApplicationContext(), "Waiting for the Master...",
						Toast.LENGTH_LONG).show();
			} else {
				long timePassed = currentTime - generalSendTime;
				int timeRemaining = (int) ((WAITTIMEOUT - timePassed) / 1000);
				Toast.makeText(getApplicationContext(), "Waiting for the Master...",
						Toast.LENGTH_LONG).show();
			}
		}

	}

	/**
	 * Questo metoto apre l'alert per l'invio della richiesta di posizione
	 * @param view apre l'alert
	 */
	private void openAlert(View view) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				MyCloseMenuActivity.this);

		alertDialogBuilder.setTitle("SEARCH MY VEHICLE");
		alertDialogBuilder.setMessage("Are you sure?");
		// set positive button: Yes message
		alertDialogBuilder.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {

						timeoutAckSearch = System.currentTimeMillis();

						myHandlerSearch.postDelayed(new Runnable() {
							public void run() {
								if (setTimeMessageSearch == 1) {
									setTimeMessageSearch=0;
								} else {
									Toast.makeText(getApplicationContext(),
											"Command POSITION REQUESTED NOT CONFIRMED",
											Toast.LENGTH_LONG).show();
									showNotification();

									animationAlert();

									String messaggio = "Something Wrong: Command POSITION REQUESTED NOT CONFIRMED";
									String messaggio1 = tempoMessaggio(messaggio);
									notifications.add(messaggio1);
									setVisibleAlert();
									mAdapter.notifyDataSetChanged();

								}
							}
						}, WAITTIMEOUT);



						try {
							smsSender.AcquisisciPosizioneMessage();
							Toast.makeText(getApplicationContext(),
									"Command SMS Sent!",
									Toast.LENGTH_LONG).show();

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
		// show alert
		alertDialog.show();
	}

	public void AppExit(View v) {

		openAlertExit(v);

	}

	public void AppAlert(View v) {

		RelativeLayout fl = (RelativeLayout) findViewById(R.id.frameAlert);
		fl.setVisibility(View.VISIBLE);
		findViewById(R.id.quitbotton).setClickable(false);
		findViewById(R.id.search).setClickable(false);
		numberButton.setClickable(false);
		buttonSetting.setClickable(false);
		mainButton.setClickable(false);
	}

	public void closeFrame(View v) {
		RelativeLayout fl = (RelativeLayout) findViewById(R.id.frameAlert);
		fl.setVisibility(View.INVISIBLE);
		findViewById(R.id.quitbotton).setClickable(true);
		findViewById(R.id.search).setClickable(true);
		numberButton.setClickable(true);
		buttonSetting.setClickable(true);
		mainButton.setClickable(true);
		if (notifications.size() == 0) {
			Button b = (Button) findViewById(R.id.alert_button);
			b.setClickable(false);
			b.setBackgroundResource(R.drawable.alertbottonhide);
			b.clearAnimation();

		}

	}

	/**
	 * Questo metodo apre l'alert per l'uscita dall'applicazione
	 * @param view view in ingresso per l'apertura dell'alert
	 */
	private void openAlertExit(View view) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				MyCloseMenuActivity.this);

		alertDialogBuilder.setTitle("EXIT");
		alertDialogBuilder.setMessage("Are you sure?");
		// set positive button: Yes message
		alertDialogBuilder.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						MyCloseMenuActivity.this.finish();
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
	protected void onPause() {
		super.onPause();
		lBManager.unregisterReceiver(smsReciver);

	}
/*
	@Override
	protected void onResume() {
		super.onResume();
		Log.println(Log.INFO, "Situazione", "resume");

		mAdapter.setDelegate(this);
		sharedpreferences = getSharedPreferences(MyPREFERENCES,
				Context.MODE_PRIVATE);
		checkAlarmStatusWhenBack();
	}
	*/

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		Log.println(Log.INFO, "Situazione", "restart");
		checkAlarmStatusWhenBack();
		mAdapter.setDelegate(this);
		// leggi preference.
	}

	@Override
	public void onStart() {
		super.onStart();

		lBManager.registerReceiver(smsReciver, new IntentFilter(
				"RETURN_MESSAGE"));
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub

		super.onStop();
		mAdapter.setDelegate(null);
		lBManager.unregisterReceiver(smsReciver);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// implement last opertion before crash!

		// unregisterReceiver(menuReceiverDelivered);
		// unregisterReceiver(menuReceiverSend);

		saveAlarmStatus(isAlarmOn);
		myHandlerStatus.removeCallbacks(statusRunnable);
		lBManager.unregisterReceiver(smsReciver);

	}


	@Override
	public void onBackPressed() {

		RelativeLayout fl = (RelativeLayout) findViewById(R.id.frameAlert);

		if (fl.getVisibility() == View.VISIBLE) {

			fl.setVisibility(View.INVISIBLE);
			findViewById(R.id.quitbotton).setClickable(true);
			findViewById(R.id.search).setClickable(true);
			numberButton.setClickable(true);
			buttonSetting.setClickable(true);
			mainButton.setClickable(true);
		} else {

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					MyCloseMenuActivity.this);

			alertDialogBuilder.setTitle("EXIT");
			alertDialogBuilder.setMessage("Are you sure?");
			// set positive button: Yes message
			alertDialogBuilder.setPositiveButton("Yes",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int id) {
							MyCloseMenuActivity.this.finish();


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
		}

	}

	public String tempoMessaggio(String testo) {

		long timeInMillis = System.currentTimeMillis();
		Calendar cal1 = Calendar.getInstance();
		cal1.setTimeInMillis(timeInMillis);
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"dd/MM/yyyy hh:mm:ss a");
		String dateforrow = dateFormat.format(cal1.getTime());
		dateforrow = testo + " " + dateforrow;
		return dateforrow;
	}

	public void addAlertToList() {
		mAdapter.values.clear();
		String s = "Ciao 4";
		notifications.add(s);
		mAdapter.values.addAll(notifications);
		mAdapter.notifyDataSetChanged();
	}

	// list view methods

	public void clearList(View v) {

		for (int i = notifications.size() - 1; i >= 0; i--) {
			notifications.remove(notifications.get(i));
		}
		List<String> newList = new ArrayList<String>();
		newList.addAll(notifications);
		mAdapter.values.clear();
		mAdapter.values.addAll(newList);
		mAdapter.notifyDataSetChanged();

	}

	@Override
	public void deleteItemList(String item) {
		notifications.remove(item);
		List<String> newList = new ArrayList<String>();
		newList.addAll(notifications);
		mAdapter.values.clear();
		mAdapter.values.addAll(newList);

		mAdapter.notifyDataSetChanged();

	}
}
