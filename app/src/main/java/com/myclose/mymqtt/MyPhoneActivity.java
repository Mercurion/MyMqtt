package com.myclose.mymqtt;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
/**<pre>
 * Questa classe gestisce la view riguardante i numeri di telefono associati al tracker.
 * @author Giacomo
 * @author Wilson
 * @see Sender
 * @see MyCloseMenuActivity
 * </pre>
 */
public class MyPhoneActivity extends Activity {

	private static final int PICK_CONTACT = 200;

	private Sender smsSender;

	MySqlClose myDbPhone;
	private String DEV_MASTER_ID;
	private String DEV_NUMBER;

	private int Lnumber;
	private int Snumber;
	private String selectedItem = "";
	private String selectedOption = "";
	SmsManager smsManager;
	private LocalBroadcastManager lBManagerPhone;
	PendingIntent sentPI;
	PendingIntent deliveredPI;
	Button gomenu;
	TextView phoneNumber;
	TextView phoneNumber1;
	TextView phoneNumber2;
	TextView phoneNumber3;
	TextView phoneNumber4;
	TextView et;
	int numeroBottone;
	Button bcolornonetadd;
	Button mButtonadd;
	Button mButton;
	Button mButton1;
	Button mButton2;
	Button mButton3;
	Button mButton4;
	Button bcolornonet;
	Button bcolornonet1;
	Button bcolornonet2;
	Button bcolornonet3;
	Button bcolornonet4;
	Button bcolorsms;
	private BroadcastReceiver smsReciverPhone;
	//	private static final int LanguageSms = 0;
//	private static final int Bottonetelefono0 = 0;
	protected static final String MyPREFERENCES = "MyPrefs";
	SharedPreferences sharedpreferences;
	private int intcolorSms;
	private int valoreparametro1flag;
	private int valoreparametro2flag;
	private String valoreparametro3flag;
	private int valoreparametro4flag;
	private int valoreparametro5flag;
	private String valoreparametro6flag;
	private int valoreparametro7flag;
	private int valoreparametro8flag;
	private String valoreparametro9flag;
	private int valoreparametro10flag;
	private int valoreparametro11flag;
	private String valoreparametro12flag;
	private int valoreparametro13flag;
	private int valoreparametro14flag;
	private String valoreparametro15flag;
	private String telefonoPassato;


	private Uri contactData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.myphone);
		addListenerGomenu();
		myDbPhone = MySqlClose.getIstance(getApplicationContext());
		loadFromdb();
		lBManagerPhone = LocalBroadcastManager.getInstance(this);
		smsManager = SmsManager.getDefault();
		phoneNumber = (TextView) findViewById(R.id.number);
		phoneNumber1 = (TextView) findViewById(R.id.number1);
		phoneNumber2 = (TextView) findViewById(R.id.number2);
		phoneNumber3 = (TextView) findViewById(R.id.number3);
		phoneNumber4 = (TextView) findViewById(R.id.number4);
		mButton = (Button) findViewById(R.id.bcolor);
		mButton1 = (Button) findViewById(R.id.bcolor1);
		mButton2 = (Button) findViewById(R.id.bcolor2);
		mButton3 = (Button) findViewById(R.id.bcolor3);
		mButton4 = (Button) findViewById(R.id.bcolor4);
		bcolornonet = (Button) findViewById(R.id.bcolornone);
		bcolornonet1 = (Button) findViewById(R.id.bcolornone1);
		bcolornonet2 = (Button) findViewById(R.id.bcolornone2);
		bcolornonet3 = (Button) findViewById(R.id.bcolornone3);
		bcolornonet4 = (Button) findViewById(R.id.bcolornone4);
		bcolorsms = (Button) findViewById(R.id.bcolorsms);
		sharedpreferences = getSharedPreferences(MyPREFERENCES,
				Context.MODE_PRIVATE);
		intcolorSms = sharedpreferences.getInt("LanguageSms", 0);
		// changeBottonetelefono(buttonx,btn,number);
		changeFlag(intcolorSms);


		valoreparametro1flag = sharedpreferences.getInt("nomeparametro1", 1);
		valoreparametro2flag = sharedpreferences.getInt("nomeparametro2", 1);
//		valoreparametro3flag = sharedpreferences.getString("nomeparametro3",
//				null);

		String telefonopassatosenzazero = "+"
				+ telefonoPassato.substring(2);
		valoreparametro3flag = telefonopassatosenzazero;

		valoreparametro4flag = sharedpreferences.getInt("nomeparametro4", 0);
		valoreparametro5flag = sharedpreferences.getInt("nomeparametro5", 0);
		valoreparametro6flag = sharedpreferences.getString("nomeparametro6",
				null); // stringa
		valoreparametro7flag = sharedpreferences.getInt("nomeparametro7", 0);
		valoreparametro8flag = sharedpreferences.getInt("nomeparametro8", 0);
		valoreparametro9flag = sharedpreferences.getString("nomeparametro9",
				null); // stringa
		valoreparametro10flag = sharedpreferences.getInt("nomeparametro10", 0);
		valoreparametro11flag = sharedpreferences.getInt("nomeparametro11", 0);
		valoreparametro12flag = sharedpreferences.getString("nomeparametro12",
				null); // stringa
		valoreparametro13flag = sharedpreferences.getInt("nomeparametro13", 0);
		valoreparametro14flag = sharedpreferences.getInt("nomeparametro14", 0);
		valoreparametro15flag = sharedpreferences.getString("nomeparametro15",
				null); // stringa

		funzioneriga1(valoreparametro1flag, valoreparametro2flag,
				valoreparametro3flag);
		funzioneriga2(valoreparametro4flag, valoreparametro5flag,
				valoreparametro6flag);
		funzioneriga3(valoreparametro7flag, valoreparametro8flag,
				valoreparametro9flag);
		funzioneriga4(valoreparametro10flag, valoreparametro11flag,
				valoreparametro12flag);
		funzioneriga5(valoreparametro13flag, valoreparametro14flag,
				valoreparametro15flag);

		smsReciverPhone = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {

				Bundle b = intent.getExtras();
				String value = b.getString("MESSAGE");
				readMessage(value, context);

			}
		};

		smsSender = new Sender();
		smsSender.setMasterPhone(DEV_NUMBER);
		smsSender.setMasterID(DEV_MASTER_ID);

	}

	public void readMessage(String value, Context context) {

		value = value.replace(" ", "");

		value = value.substring(0, 21);
		String temp = DEV_MASTER_ID.toString().substring(8, 10);
		if (value.equals("Attention!PoweroffMas")
				|| value.equals("Attenzione!Spegniment")
				|| value.equals("Mastericlosefermen." + temp)
				|| value.equals("Achtung!Deaktivierung")
				|| value.equals("Atencion!ApagadoMaste")
				|| value.equals("Attentie!UitzettenMas")) {
			MyPhoneActivity.this.finish();
		}

		// else if (value.equals("Attenzione!Allarmebas")
		// || value.equals("Alertesignalradiofaib")
		// || value.equals("Attention!Lowradiosig")
		// || value.equals("Achtung!Funksignalsch")
		// || value.equals("Atencion!Al
		// armase�alb")
		// || value.equals("Attentie!Laagradiosig")) {
		// // sharepreference get
		//
		// String messaggio = "Attention! Low radio signal";
		//
		// notifications.add(messaggio);
		// mAdapter.notifyDataSetChanged();
		//
		// }

		// else if (value.equals("Attenzione!Livellobat")
		// || value.equals("AlertebatteriefaibleM")
		// || value.equals("Attention!Lowbatteryc")
		// || value.equals("Achtung!Batteriewarnu")
		// || value.equals("Atencion!Nivelbateria")
		// || value.equals("Attentie!Batterijbijn")) {
		// // sharepreference get
		//
		// String messaggio = "Attention! Low battery on Master!";
		//
		// notifications.add(messaggio);
		// mAdapter.notifyDataSetChanged();
		//
		// }

		// da adattare al livello batteria iclose(lucchetto)
		//
		// else if (value.equals("Attenzione!Livellobat") ||
		// value.equals("Alertebatteriefaiblei") ||
		// value.equals("Attention!Lowbatteryc") ||
		// value.equals("Achtung!Batteriewarnu") ||
		// value.equals("Atencion!Nivelbateria") ||
		// value.equals("Attentie!Batterijbijn")){
		// //sharepreference get
		//
		// String messaggio= "Attention! Low battery on Master!";
		//
		// notifications.add(messaggio);
		// mAdapter.notifyDataSetChanged();
		//
		// }



		else if (value.equals(DEV_MASTER_ID + "00CONFIRMED")) {

			MyPhoneActivity.this.finish();
		}
	}

//	private String loadTelefonoUser() {
//		Utente u = myDbPhone.getUtente();
//		// String sim = u.getIdSim();
//		String idTel = u.getIdTel();
//
//		// DEV_MASTER_ID = u.getIdMaster();
//		// ;
//		// DEV_NUMBER = u.getIdTelMaster();
//		// Toast.makeText(getApplicationContext(),
//		// DEV_MASTER_ID + " " + DEV_NUMBER + " " + sim + " " + idTel,
//		//
//		// Toast.LENGTH_LONG).show();
//		//
//
//		return idTel;
//
//	}

	/**
	 * Questo metodo viene chiamato per caricare i dati dal DB. I dati necessari sono Id del master e numero di telefono.
	 */
	private void loadFromdb() {
		Utente u = myDbPhone.getUtente();
//		String sim = u.getIdSim();
		telefonoPassato = u.getIdTel();
		DEV_MASTER_ID = u.getIdMaster();
		DEV_NUMBER = u.getIdTelMaster();
	}



	private void saveTelefonoUser(String numeroTel) {

		Utente u = myDbPhone.getUtente();
		numeroTel=numeroTel.substring(1);
		numeroTel="00"+numeroTel;
		u.setIdTel(numeroTel);
		myDbPhone.upDateUtente(u);
//		String sim = u.getIdSim();
//		String idTel = u.getIdTel();
//
//		DEV_MASTER_ID = u.getIdMaster();


		// Toast.makeText(getApplicationContext(),
		// DEV_MASTER_ID + " " + DEV_NUMBER + " " + sim + " " + idTel,
		//
		// Toast.LENGTH_LONG).show();

	}

	private void Funzione(int numero1, String numerotel) {
		int numero = numero1 - 1;

		switch (numero) {

			case 0:
				Editor editor = sharedpreferences.edit();
				editor.putInt("nomeparametro1", 1);
				editor.apply();

				Editor editor1 = sharedpreferences.edit();
				editor1.putInt("nomeparametro2", 1);
				editor1.apply();



//			Editor editor2 = sharedpreferences.edit();
//			editor2.putString("nomeparametro3", numerotel);
//			editor2.commit();

				saveTelefonoUser(numerotel);

				loadFromdb();
				funzioneriga1(1, 1, numerotel);
				Toast.makeText(getBaseContext(), "Phone Number 1", Toast.LENGTH_SHORT)
						.show();
				break;

			case 1:

				Editor editor3 = sharedpreferences.edit();
				editor3.putInt("nomeparametro4", 1);
				editor3.apply();

				Editor editor4 = sharedpreferences.edit();
				editor4.putInt("nomeparametro5", 1);
				editor4.apply();

				Editor editor5 = sharedpreferences.edit();
				editor5.putString("nomeparametro6", numerotel);
				editor5.apply();
				funzioneriga2(1, 1, numerotel);
				Toast.makeText(getBaseContext(), "Phone Number 2", Toast.LENGTH_SHORT)
						.show();
				break;

			case 2:

				Editor editor6 = sharedpreferences.edit();
				editor6.putInt("nomeparametro7", 1);
				editor6.apply();

				Editor editor7 = sharedpreferences.edit();
				editor7.putInt("nomeparametro8", 1);
				editor7.apply();

				Editor editor8 = sharedpreferences.edit();
				editor8.putString("nomeparametro9", numerotel);
				editor8.apply();
				funzioneriga3(1, 1, numerotel);
				Toast.makeText(getBaseContext(), "Phone Number 3", Toast.LENGTH_SHORT)
						.show();
				break;

			case 3:

				Editor editor9 = sharedpreferences.edit();
				editor9.putInt("nomeparametro10", 1);
				editor9.apply();

				Editor editor10 = sharedpreferences.edit();
				editor10.putInt("nomeparametro11", 1);
				editor10.apply();

				Editor editor11 = sharedpreferences.edit();
				editor11.putString("nomeparametro12", numerotel);
				editor11.apply();
				funzioneriga4(1, 1, numerotel);
				Toast.makeText(getBaseContext(), "Phone Number 4", Toast.LENGTH_SHORT)
						.show();
				break;

			case 4:

				Editor editor12 = sharedpreferences.edit();
				editor12.putInt("nomeparametro13", 1);
				editor12.apply();

				Editor editor13 = sharedpreferences.edit();
				editor13.putInt("nomeparametro14", 1);
				editor13.apply();

				Editor editor14 = sharedpreferences.edit();
				editor14.putString("nomeparametro15", numerotel);
				editor14.apply();
				funzioneriga5(1, 1, numerotel);
				Toast.makeText(getBaseContext(), "Phone Number 5", Toast.LENGTH_SHORT)
						.show();
				break;
		}

	}

	private void FunzioneReset(int numero1, String numerotel) {
		int numero = numero1 - 1;

		switch (numero) {

			case 0:
				Editor editor = sharedpreferences.edit();
				editor.putInt("nomeparametro1", 0);
				editor.commit();

				Editor editor1 = sharedpreferences.edit();
				editor1.putInt("nomeparametro2", 0);
				editor1.commit();

//			Editor editor2 = sharedpreferences.edit();
//			editor2.putString("nomeparametro3", numerotel);
//			editor2.commit();

				saveTelefonoUser(numerotel);
				loadFromdb();

				Toast.makeText(getBaseContext(), "Phone Number 1", Toast.LENGTH_SHORT)
						.show();
				break;

			case 1:

				Editor editor3 = sharedpreferences.edit();
				editor3.putInt("nomeparametro4", 0);
				editor3.commit();

				Editor editor4 = sharedpreferences.edit();
				editor4.putInt("nomeparametro5", 0);
				editor4.commit();

				Editor editor5 = sharedpreferences.edit();
				editor5.putString("nomeparametro6", numerotel);
				editor5.commit();

				Toast.makeText(getBaseContext(), "Phone Number 2", Toast.LENGTH_SHORT)
						.show();
				break;

			case 2:

				Editor editor6 = sharedpreferences.edit();
				editor6.putInt("nomeparametro7", 0);
				editor6.commit();

				Editor editor7 = sharedpreferences.edit();
				editor7.putInt("nomeparametro8", 0);
				editor7.commit();

				Editor editor8 = sharedpreferences.edit();
				editor8.putString("nomeparametro9", numerotel);
				editor8.commit();

				Toast.makeText(getBaseContext(), "Phone Number 3", Toast.LENGTH_SHORT)
						.show();
				break;

			case 3:

				Editor editor9 = sharedpreferences.edit();
				editor9.putInt("nomeparametro10", 0);
				editor9.commit();

				Editor editor10 = sharedpreferences.edit();
				editor10.putInt("nomeparametro11", 0);
				editor10.commit();

				Editor editor11 = sharedpreferences.edit();
				editor11.putString("nomeparametro12", numerotel);
				editor11.commit();

				Toast.makeText(getBaseContext(), "Phone Number 4", Toast.LENGTH_SHORT)
						.show();
				break;

			case 4:

				Editor editor12 = sharedpreferences.edit();
				editor12.putInt("nomeparametro13", 0);
				editor12.commit();

				Editor editor13 = sharedpreferences.edit();
				editor13.putInt("nomeparametro14", 0);
				editor13.commit();

				Editor editor14 = sharedpreferences.edit();
				editor14.putString("nomeparametro15", numerotel);
				editor14.commit();

				Toast.makeText(getBaseContext(), "Phone Number 5", Toast.LENGTH_SHORT)
						.show();
				break;
		}

	}

	private void funzioneriga1(int valore1, int valore2, String valore3) {
		if (valore1 == 1) {
			phoneNumber.setText(valore3);
			mButton.setBackgroundColor(Color.parseColor("#eb5b25"));
			bcolornonet.setBackgroundResource(R.drawable.oknumber);
		}
	}

	private void funzioneriga2(int valore1, int valore2, String valore3) {
		if (valore1 == 1) {
			phoneNumber1.setText(valore3);
			mButton1.setBackgroundColor(Color.parseColor("#eb5b25"));
			bcolornonet1.setBackgroundResource(R.drawable.oknumber);
		}
	}

	private void funzioneriga3(int valore1, int valore2, String valore3) {
		if (valore1 == 1) {
			phoneNumber2.setText(valore3);
			mButton2.setBackgroundColor(Color.parseColor("#eb5b25"));
			bcolornonet2.setBackgroundResource(R.drawable.oknumber);
		}
	}

	private void funzioneriga4(int valore1, int valore2, String valore3) {
		if (valore1 == 1) {
			phoneNumber3.setText(valore3);
			mButton3.setBackgroundColor(Color.parseColor("#eb5b25"));
			bcolornonet3.setBackgroundResource(R.drawable.oknumber);
		}
	}

	private void funzioneriga5(int valore1, int valore2, String valore3) {
		if (valore1 == 1) {
			phoneNumber4.setText(valore3);
			mButton4.setBackgroundColor(Color.parseColor("#eb5b25"));
			bcolornonet4.setBackgroundResource(R.drawable.oknumber);
		}
	}

//	private void changeBottonetelefono(Button buttonx, Button btn, int position) {
//		switch (position) {
//
//		case 0:
//			buttonx.setBackgroundColor(Color.parseColor("#eb5b25"));
//			btn.setBackgroundResource(R.drawable.oknumber);
//			Editor editor = sharedpreferences.edit();
//			editor.putInt("Bottonetelefono0", 1);
//			editor.commit();
//
//			break;
//		case 1:
//			buttonx.setBackgroundColor(Color.parseColor("#eb5b25"));
//			btn.setBackgroundResource(R.drawable.oknumber);
//			Editor editor1 = sharedpreferences.edit();
//			editor1.putInt("Bottonetelefono0", position);
//			editor1.commit();
//			break;
//
//		}
//
//	}


	@Deprecated
	private void sendSMS(String phoneNumber, String message) {
		String SENT = "SMS_SENT";
		String DELIVERED = "SMS_DELIVERED";

		sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);

		deliveredPI = PendingIntent.getBroadcast(this, 0,
				new Intent(DELIVERED), 0);

		// ---when the SMS has been sent---
		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
					case Activity.RESULT_OK:
						Toast.makeText(getBaseContext(), "SMS Sent!",
								Toast.LENGTH_SHORT).show();
						break;
					case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
						Toast.makeText(getBaseContext(), "Generic failure",
								Toast.LENGTH_SHORT).show();
						break;
					case SmsManager.RESULT_ERROR_NO_SERVICE:
						Toast.makeText(getBaseContext(), "No service",
								Toast.LENGTH_SHORT).show();
						break;
					case SmsManager.RESULT_ERROR_NULL_PDU:
						Toast.makeText(getBaseContext(), "Null PDU",
								Toast.LENGTH_SHORT).show();
						break;
					case SmsManager.RESULT_ERROR_RADIO_OFF:
						Toast.makeText(getBaseContext(), "Radio off",
								Toast.LENGTH_SHORT).show();
						break;
				}
			}
		}, new IntentFilter(SENT));

		// ---when the SMS has been delivered---
		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
					case Activity.RESULT_OK:
						Toast.makeText(getBaseContext(), "SMS delivered",
								Toast.LENGTH_SHORT).show();
						break;
					case Activity.RESULT_CANCELED:
						Toast.makeText(getBaseContext(), "SMS not delivered",
								Toast.LENGTH_SHORT).show();
						break;
				}
			}
		}, new IntentFilter(DELIVERED));

		smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(phoneNumber, null, message, sentPI,
				deliveredPI);
	}

	private void addListenerGomenu() {
		gomenu = (Button) findViewById(R.id.menubottom);

		gomenu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				MyPhoneActivity.this.finish();

			}

		});

	}

	public void onPickNumberButton(View v) {
		switch (v.getId()) {
			case R.id.btn1:
			case R.id.bcolor:
			case R.id.number_txt:

				if (valoreparametro1flag == 0) {
					openPickNumberButtonClicktCancel(phoneNumber, mButton,
							bcolornonet, 1);
				} else {
					openPickNumberButtonClickt(phoneNumber, mButton, bcolornonet, 1);
				}
				break;
			case R.id.btn2:
			case R.id.bcolor1:
			case R.id.number_txt1:
				valoreparametro4flag = sharedpreferences
						.getInt("nomeparametro4", 0);
				if (valoreparametro4flag == 1) {
					openPickNumberButtonClicktCancel(phoneNumber1, mButton1,
							bcolornonet1, 2);
				} else {
					openPickNumberButtonClickt(phoneNumber1, mButton1,
							bcolornonet1, 2);
				}
				break;
			case R.id.btn3:
			case R.id.bcolor2:
			case R.id.number_txt2:
				valoreparametro7flag = sharedpreferences
						.getInt("nomeparametro7", 0);
				if (valoreparametro7flag == 1) {
					openPickNumberButtonClicktCancel(phoneNumber2, mButton2,
							bcolornonet2, 3);
				} else {
					openPickNumberButtonClickt(phoneNumber2, mButton2,
							bcolornonet2, 3);
				}
				break;
			case R.id.btn4:
			case R.id.bcolor3:
			case R.id.number_txt4:
				valoreparametro10flag = sharedpreferences.getInt("nomeparametro10",
						0);
				if (valoreparametro10flag == 1) {
					openPickNumberButtonClicktCancel(phoneNumber3, mButton3,
							bcolornonet3, 4);
				} else {
					openPickNumberButtonClickt(phoneNumber3, mButton3,
							bcolornonet3, 4);
				}
				break;
			case R.id.btn5:
			case R.id.bcolor4:
			case R.id.number_txt5:
				valoreparametro13flag = sharedpreferences.getInt("nomeparametro13",
						0);
				if (valoreparametro13flag == 1) {
					openPickNumberButtonClicktCancel(phoneNumber4, mButton4,
							bcolornonet4, 5);
				} else {
					openPickNumberButtonClickt(phoneNumber4, mButton4,
							bcolornonet4, 5);
				}
				break;
		}

	}


	private void openPickNumberButtonClicktCancel(final TextView phoneNumberty,
												  final Button buttonx, final Button btn, final int numberButton) {

		final AlertDialog.Builder alertDialogBuilder0 = new AlertDialog.Builder(
				MyPhoneActivity.this);

		alertDialogBuilder0.setTitle("Phone Number Options");
		final CharSequence[] Options = { "Insert a Phone Number",
				"Select from your contacts", "Cancel Phone Number" };

		selectedOption = "00";
		Snumber = 6;

		alertDialogBuilder0.setSingleChoiceItems(Options, -1,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						selectedOption = (String) Options[which];
						Snumber = which;

					}
				});
		alertDialogBuilder0.setPositiveButton("ok",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						if (selectedOption.equals("00") || Snumber == -2) {

							Toast toast = Toast
									.makeText(getApplicationContext(),
											"Selected: " + "No Phone Number selected",
											Toast.LENGTH_SHORT);
							toast.show();
						} else {

							switch (Snumber) {
								case 0:
									// if(Snumber==0){ //cancellare
									AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
											MyPhoneActivity.this);
									// dialog.cancel();

									alertDialogBuilder
											.setTitle("Insert a Phone Number");
									alertDialogBuilder
											.setMessage("00 + CountryCode + PhoneNumber"
													+ "\n" + "(Ex:00393401234567)");

									final EditText input_number = new EditText(
											getApplicationContext());
									input_number
											.setInputType(InputType.TYPE_CLASS_NUMBER);
									input_number
											.setHint("Click here to insert your Phone");
									input_number.setTextColor(Color
											.parseColor("#000000"));
									input_number.setBackgroundColor(Color.WHITE);
									alertDialogBuilder.setView(input_number);

									// set positive button: Yes message
									alertDialogBuilder.setPositiveButton("Add",
											new DialogInterface.OnClickListener() {

												public void onClick(
														DialogInterface dialog,
														int id) {

													// 1 : check if number is right
													// save to database the added
													// number
													// set number to number textView

													String number1 = input_number
															.getText().toString()
															.trim();

													if (!number1.isEmpty()) {
														String number = "+"
																+ number1
																.substring(2);
														phoneNumberty
																.setText(number);

														buttonx.setBackgroundColor(Color
																.parseColor("#eb5b25"));
														btn.setBackgroundResource(R.drawable.oknumber);
														Funzione(numberButton,
																number);
														//
														// changeBottonetelefono(buttonx,btn,number);

														try {
															smsSender.NumeroChiamareMessage(numberButton,number);
														/*sendSMS(DEV_NUMBER,
																DEV_MASTER_ID
																		+ " "
																		+ "20"
																		+ " "
																		+ numberButton
																		+ " "
																		+ number
																		+ " "
																		+ "PHONE NUMBER");
																		*/
															// smsManager.sendTextMessage("+393478401746",
															// "+393478401746",
															// "SHUTDOWN", null,
															// null);
															// this.finish();
															Toast.makeText(
																	getApplicationContext(),
																	"Command SMS Sent!",
																	Toast.LENGTH_LONG)
																	.show();

														} catch (Exception e) {
															// TODO: handle
															// exception
															e.printStackTrace();
														}
													} else {
														Toast.makeText(
																getApplicationContext(),
																"No Phone Number selected",
																Toast.LENGTH_LONG)
																.show();
														dialog.cancel();
													}
												}

											});

									// set negative button: No message
									alertDialogBuilder.setNegativeButton("Cancel",
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int id) {
													// cancel the alert box and put
													// a
													// Toast to the user

													dialog.cancel();

												}
											});

									AlertDialog alertDialog = alertDialogBuilder
											.create();
									// show alert
									alertDialog.show();
									dialog.cancel();
									// TODO Auto-generated method stub

									// } //cancellare
									break;
								case 1:
									// else{ //cancellare

									numeroBottone = numberButton;
									et = phoneNumberty;
									bcolornonetadd = buttonx;
									mButtonadd = btn;
									Intent intent = new Intent(
											Intent.ACTION_PICK,
											ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
									intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
									startActivityForResult(intent, PICK_CONTACT);

									// } //cancellare
									break;

								case 2:
									String number = "";
									phoneNumberty.setText(number);
									buttonx.setBackgroundColor(Color
											.parseColor("#1C202A"));
									btn.setBackgroundResource(R.drawable.oknumbert);
									FunzioneReset(numberButton, number);
									//
									// changeBottonetelefono(buttonx,btn,number);

									try {
										smsSender.NumeroChiamareMessage(numberButton,number);
									/*sendSMS(DEV_NUMBER, DEV_MASTER_ID + " "
											+ "20" + " " + numberButton + " "
											+ number + " " + "PHONE NUMBER");
											*/


										Toast.makeText(getApplicationContext(),
												"Command SMS Sent!",
												Toast.LENGTH_LONG).show();

									} catch (Exception e) {
										e.printStackTrace();
									}
									dialog.cancel();
									break;
							}
						}
					}

				});

		// set negative button: No message
		alertDialogBuilder0.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// cancel the alert box and put a Toast to the user
						dialog.cancel();

					}
				});

		AlertDialog alertDialog = alertDialogBuilder0.create();
		// show alert
		alertDialog.show();

	}

	private void openPickNumberButtonClickt(final TextView phoneNumberty,
											final Button buttonx, final Button btn, final int numberButton) {

		final AlertDialog.Builder alertDialogBuilder0 = new AlertDialog.Builder(
				MyPhoneActivity.this);

		alertDialogBuilder0.setTitle("Phone Number Options");
		final CharSequence[] Options = { "Insert a Phone Number",
				"Select from your contacts" };

		selectedOption = "00";
		Snumber = 6;

		alertDialogBuilder0.setSingleChoiceItems(Options, -1,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						selectedOption = (String) Options[which];
						Snumber = which;

					}
				});

		// set positive button: Yes message
		alertDialogBuilder0.setPositiveButton("ok",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						if (selectedOption.equals("00") || Snumber == -2) {

							Toast toast = Toast
									.makeText(getApplicationContext(),
											"Selected: " + "No Phone Number selected",
											Toast.LENGTH_SHORT);
							toast.show();
						} else {

							if (Snumber == 0) {
								AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
										MyPhoneActivity.this);
								// dialog.cancel();

								alertDialogBuilder
										.setTitle("Insert a phone number");
								alertDialogBuilder
										.setMessage("00 + CountryCode + PhoneNumber"
												+ "\n" + "(Ex:00393401234567)");

								final EditText input_number = new EditText(
										getApplicationContext());
								input_number
										.setInputType(InputType.TYPE_CLASS_NUMBER);
								input_number
										.setHint("Click here to insert your phone");
								input_number.setTextColor(Color
										.parseColor("#000000"));
								input_number.setBackgroundColor(Color.WHITE);
								alertDialogBuilder.setView(input_number);

								// set positive button: Yes message
								alertDialogBuilder.setPositiveButton("Add",
										new DialogInterface.OnClickListener() {

											public void onClick(
													DialogInterface dialog,
													int id) {

												// 1 : check if number is right
												// save to database the added
												// number
												// set number to number textView

												String number1 = input_number
														.getText().toString()
														.trim();

												if (!number1.isEmpty()) {
													String number = "+"
															+ number1
															.substring(2);
													phoneNumberty
															.setText(number);

													buttonx.setBackgroundColor(Color
															.parseColor("#eb5b25"));
													btn.setBackgroundResource(R.drawable.oknumber);
													Funzione(numberButton,
															number);
													//
													// changeBottonetelefono(buttonx,btn,number);

													try {
														smsSender.NumeroChiamareMessage(numberButton,number);
														/*sendSMS(DEV_NUMBER,
																DEV_MASTER_ID
																		+ " "
																		+ "20"
																		+ " "
																		+ numberButton
																		+ " "
																		+ number
																		+ " "
																		+ "PHONE NUMBER");
																		*/

														Toast.makeText(
																getApplicationContext(),
																"Command SMS Sent!",
																Toast.LENGTH_LONG)
																.show();

													} catch (Exception e) {
														// TODO: handle
														// exception
														e.printStackTrace();
													}
												} else {
													Toast.makeText(
															getApplicationContext(),
															"No phone Number selected",
															Toast.LENGTH_LONG)
															.show();
													dialog.cancel();
												}
											}

										});

								// set negative button: No message
								alertDialogBuilder.setNegativeButton("Cancel",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												// cancel the alert box and put
												// a
												// Toast to the user

												dialog.cancel();

											}
										});

								AlertDialog alertDialog = alertDialogBuilder
										.create();
								// show alert
								alertDialog.show();
								dialog.cancel();
								// TODO Auto-generated method stub

							} else {

								numeroBottone = numberButton;
								et = phoneNumberty;
								bcolornonetadd = buttonx;
								mButtonadd = btn;
								Intent intent = new Intent(
										Intent.ACTION_PICK,
										ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
								intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
								startActivityForResult(intent, PICK_CONTACT);

							}
						}
					}

				});

		// set negative button: No message
		alertDialogBuilder0.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// cancel the alert box and put a Toast to the user
						dialog.cancel();

					}
				});

		AlertDialog alertDialog = alertDialogBuilder0.create();
		// show alert
		alertDialog.show();

	}

	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);
		if (reqCode == PICK_CONTACT && resultCode == Activity.RESULT_OK) {
			contactData = data.getData();

			retrieveContacNumber();

		} else {
			Toast.makeText(getApplicationContext(), "No Phone Number selected",
					Toast.LENGTH_SHORT).show();
		}
	}

	private void retrieveContacNumber() {

		String cNumber = "";

		Cursor c = getContentResolver().query(contactData, null, null, null, null);
		if (c.moveToFirst()) {

			String id = c.getString(c
					.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

			String hasPhone = c
					.getString(c
							.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

			if (hasPhone.equalsIgnoreCase("1")) {
				Cursor phones = getContentResolver().query(
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						null,
						ContactsContract.CommonDataKinds.Phone.CONTACT_ID
								+ " = " + id, null, null);
				phones.moveToFirst();
				cNumber = phones.getString(phones.getColumnIndex("data1"));
				cNumber = cNumber.replaceAll("\\s+", "");

			}

//			String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

			if(cNumber.equals("")){


				Toast.makeText(getApplicationContext(),
						"Phone Number is empty, select another Phone Number", Toast.LENGTH_LONG).show();
			}

			else{
				et.setText(cNumber);

				bcolornonetadd.setBackgroundColor(Color.parseColor("#eb5b25"));
				mButtonadd.setBackgroundResource(R.drawable.oknumber);
				Funzione(numeroBottone, cNumber);

				Toast.makeText(getApplicationContext(), "number = " + cNumber,
						Toast.LENGTH_SHORT).show();
				try {

					smsSender.NumeroChiamareMessage(numeroBottone,cNumber);
				/*sendSMS(DEV_NUMBER, DEV_MASTER_ID + " " + "20" + " "
						+ numeroBottone + " " + cNumber + " " + "PHONE NUMBER");
						*/

					Toast.makeText(getApplicationContext(),
							"Command SMS Sent!", Toast.LENGTH_LONG).show();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	public void onClickLanguage(View v) {
		openAlertLanguage(v);
	}

	private void openAlertLanguage(View view) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				MyPhoneActivity.this);

		alertDialogBuilder.setTitle("SMS LANGUAGE");
		final CharSequence[] items = { "English", "Italiano", "Fran"+"\u00E7"+"ais",
				"Deutsch", "Espa"+ "\u00f1" + "ol", "Nederlands" };

		selectedItem = "00";
		Lnumber = 6;

		alertDialogBuilder.setSingleChoiceItems(items, -1,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						selectedItem = (String) items[which];
						Lnumber = which;
					}
				});

		// set positive button: Yes message
		alertDialogBuilder.setPositiveButton("ok",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {

						if (selectedItem.equals("00") || Lnumber == -2) {

							Toast toast = Toast
									.makeText(getApplicationContext(),
											"Selected: " + "No Phone Number selected",
											Toast.LENGTH_SHORT);
							toast.show();
						} else {
							changeFlag(Lnumber);

							try {
								smsSender.LinguaMessage(Lnumber);
								/*
								sendSMS(DEV_NUMBER, DEV_MASTER_ID + " " + "30"
										+ " " + Lnumber + " " + "SMS LANGUAGE");
										*/
								Toast.makeText(getApplicationContext(),
										"Command SMS Sent!",
										Toast.LENGTH_LONG).show();

							} catch (Exception e) {
								e.printStackTrace();
							}

						}
					}
				});
		// set negative button: No message
		alertDialogBuilder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// cancel the alert box and put a Toast to the user
						dialog.cancel();
					}
				});

		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}

	private void saveValue(int position) {
		Editor editor = sharedpreferences.edit();
		editor.putInt("LanguageSms", position);
		editor.apply();
	}

	private void changeFlag(int position) {

		switch (position) {
			case 0:
				bcolorsms.setBackgroundResource(R.drawable.buttonflageng);
				saveValue(position);

				break;
			case 1:
				bcolorsms.setBackgroundResource(R.drawable.buttonflagita);
				saveValue(position);
				break;
			case 2:
				bcolorsms.setBackgroundResource(R.drawable.buttonflagfr);
				saveValue(position);
				break;
			case 3:
				bcolorsms.setBackgroundResource(R.drawable.buttonflagde);
				saveValue(position);
				break;
			case 4:
				bcolorsms.setBackgroundResource(R.drawable.buttonflages);
				saveValue(position);
				break;
			case 5:
				bcolorsms.setBackgroundResource(R.drawable.buttonflagdu);
				saveValue(position);
				break;
			default:
				bcolorsms.setBackgroundResource(R.drawable.buttonflageng);
				Editor editor = sharedpreferences.edit();
				editor.putInt("LanguageSms", 0);
				editor.apply();

		}

	}

//	private void phoneNumberOptions() {
//		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
//				MyPhoneActivity.this);
//
//		alertDialogBuilder.setTitle("SEARCH MY VEHICLE");
//		alertDialogBuilder.setMessage("Are you sure?");
//		// set positive button: Yes message
//		alertDialogBuilder.setPositiveButton("Yes",
//				new DialogInterface.OnClickListener() {
//
//					public void onClick(DialogInterface dialog, int id) {
//
//						try {
//
//							sendSMS(DEV_NUMBER, "");
//							// this.finish();
//							Toast.makeText(getApplicationContext(),
//									"SMS Sent!", Toast.LENGTH_LONG).show();
//
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//
//					}
//				});
//		// set negative button: No message
//		alertDialogBuilder.setNegativeButton("No",
//				new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int id) {
//						// cancel the alert box and put a Toast to the user
//						dialog.cancel();
//
//					}
//				});
//
//		AlertDialog alertDialog = alertDialogBuilder.create();
//		// show alert
//		alertDialog.show();
//	}


	@Override
	public void onStart() {
		super.onStart();
		lBManagerPhone.registerReceiver(smsReciverPhone, new IntentFilter(
				"RETURN_MESSAGE"));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// implement last opertion before crash!

		// unregisterReceiver(menuReceiverDelivered);
		// unregisterReceiver(menuReceiverSend);
		lBManagerPhone.unregisterReceiver(smsReciverPhone);
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
}
