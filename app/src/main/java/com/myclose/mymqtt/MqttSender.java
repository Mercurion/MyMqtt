package com.myclose.mymqtt;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.jetbrains.annotations.Contract;

import java.io.UnsupportedEncodingException;


/**
 * Questa classe serve per inviare messaggi MQTT
 * @author jack
 */
public class MqttSender {
    private final String TAG = "Debug";
    private MqttAndroidClient mqttAndroidClient;
    private MqttAndroidClient client;
    MqttConnectOptions options;
    private String topic_pub = "test/helloAndroid";
    private String topic_sub = "helloAndroid";
    private int qos = 1;
    /*generate random clientId*/
    private String clientId = MqttClient.generateClientId();
    private String server = "ec2-52-40-248-111.us-west-2.compute.amazonaws.com";
    private String port = "1883";
    private String broker = "tcp://" + server + ":" + port;

    String masterPhone;
    String masterID;
    private String messaggio;

    private Context context;



    public MqttSender(Context cnt) {
        this.context = cnt;


        client = new MqttAndroidClient(context, broker,
                clientId);

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {
                System.out.printf("Exception handled, reconnecting...\nDetail:\n%s\n", throwable.getMessage());

//Called when connection is lost.
            }
            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                System.out.println("Topic: " + topic);
                System.out.println(new String(mqttMessage. getPayload()));
                System.out.println("QoS: " + mqttMessage. getQos());
                System.out.println("Retained: " + mqttMessage. isRetained());
            }
            @Override
            public void deliveryComplete(final IMqttDeliveryToken iMqttDeliveryToken) {

//When message delivery was complete
            }
        });

        options = new MqttConnectOptions();
        options.setKeepAliveInterval(180);
        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, "onSuccess");
                    Toast.makeText(context, "Connection, ok!",
                            Toast.LENGTH_LONG).show();

                    try {
                        IMqttToken subToken = client.subscribe(topic_sub, qos);
                        subToken.setActionCallback(new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                Toast.makeText(context, "Subscribe, ok!",
                                        Toast.LENGTH_LONG).show();
                                // The message was published
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken,
                                                  Throwable exception) {
                                // The subscription could not be performed, maybe the user was not
                                // authorized to subscribe on the specified topic e.g. using wildcards
                            }
                        });
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure");
                    Toast.makeText(context, "wrong",
                            Toast.LENGTH_LONG).show();

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    private void setMessaggio(String msg){
        this.messaggio = msg;
    }

    /**
     * set the master ID, to be used in the messages
     * @param id the ID ofthe master
     */
    public void setMasterID(String id) {
        this.masterID = id;
    }


    /**
     * Questo metodo verifica la connessione e avvia il metodo per mandare il comando
     */
    private void sendComand() {
        if (client == null) {
            Toast.makeText(context, "NON CONNESSO", Toast.LENGTH_LONG).show();
            return;

        } else if (client.isConnected()) {
            checkMQTTMessage();
        } else {
            Toast.makeText(context, "NOT sent",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Contract(pure = true)
    private static boolean isMessageValid(String message) {
        /*replaced false with true */
        boolean isValid = true;
        /*simple validation */
        //TODO: da validare il messaggio
        return isValid;
    }

    /**
     * Questo metodo verifica la validita del messaggio e invia il messaggio MQTT
     */
    private void checkMQTTMessage() {
        if (isMessageValid(messaggio)) {
            String payload = messaggio;
            byte[] encodedPayload = new byte[0];
            try {
                encodedPayload = payload.getBytes("UTF-8");
                MqttMessage message = new MqttMessage(encodedPayload);
                client.publish(topic_pub, message);
                Log.d(TAG, "onSent");
                Toast.makeText(context, "sent" + "to" + topic_pub,
                        Toast.LENGTH_LONG).show();

            } catch (UnsupportedEncodingException | MqttException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(context, "messaggio non valido",
                    Toast.LENGTH_SHORT).show();
            return;
        }
    }

    /**
     * this method send the message to shutdown the master
     */
    public void ShutdownMessage() {
        String tmp;
        tmp = this.masterID + " " + "00" + " " + "SHUTDOWN";
        setMessaggio(tmp);
        sendComand();
    }

    /**
     * this method send the ping message
     */
    public void PingMessage() {
        String tmp;
        tmp = this.masterID + " " + "01" + " " + "ALIVE?";
        setMessaggio(tmp);
        sendComand();
    }

    /**
     * this method send the STATUS message, used in the starting phase
     */
    public void StatusMessage() {
        String tmp;
        tmp = this.masterID + " " + "01" + " " + "STATUS?";
        setMessaggio(tmp);
        sendComand();
    }

    public void NewStatusMessage0 (){
        String tmp;
        tmp = this.masterID + " " + "01 0" + " " + "STATUS?";
        setMessaggio(tmp);
        sendComand();
    }

    public void NewStatusMessage1 (){
        String tmp;
        tmp = this.masterID + " " + "01 1" + " " + "STATUS?";
        setMessaggio(tmp);
        sendComand();
    }

    /**
     * this method send the message to shut down the alarm
     */
    public void AlarmOFFMessage() {
        String tmp;
        tmp = this.masterID + " " + "02" + " " + "ALARM OFF";
        setMessaggio(tmp);
        sendComand();
    }

    /**
     * this method send the message to ALARM ON the system
     */
    public void AlarmONMessage() {
        String tmp;
        tmp = this.masterID + " " + "03" + " " + "ALARM ON";
        setMessaggio(tmp);
        sendComand();
    }

    /**
     * Questo metodo fa la richiesta di posizione al master
     */
    public void AcquisisciPosizioneMessage() {
        String tmp;
        tmp = this.masterID + " " + "04" + " " + "POSITION REQUESTED";
        setMessaggio(tmp);
        sendComand();
    }

    /**
     * Questo metodo manda la configurazione standard al master
     */
    public void BasicConfigurationMessage() {
        String tmp;
        tmp = this.masterID + " " + "10" + " " + "BASIC ON";
        setMessaggio(tmp);
        sendComand();
    }

    /**
     * Questo metodo manda la configurazione TOP al master
     */
    public void TopConfigurationMessage() {
        String tmp;
        tmp = this.masterID + " " + "11" + " " + "TOP ON";
        setMessaggio(tmp);
        sendComand();
    }


    /**
     * questo metodo manda l'informazione per il time-out di verifica se il lucchetto è attivo
     * @param intervallo in valori da 1 a 5. 0 se è disattivo
     */
    public void IntervalloTimeOutMessage(int intervallo) {
        String tmp;
        tmp = this.masterID + " " + "12" + " " + intervallo + " " + "TIMEOUT";
        setMessaggio(tmp);
        sendComand();
    }

    /**
     * questo metodo manda il parametro di sensibilità, quanti movimenti al minuto per attivare un allarme
     * @param sensibility da 1 a 3, con 1 basso, 2 medio, 3 alto
     */
    public void SensibilitaLucchettoMessage(int sensibility) {
        String tmp;
        tmp = this.masterID + " " + "13" + " " + sensibility + " " + "SENS";
        setMessaggio(tmp);
        sendComand();
    }

    /**
     * Questo metodo manda la configurazione ON o OFF per l'allarme di batteria scarica (se ON avvisa oltre una certa soglia)
     * @param batteria un boolean: true è ON, false è OFF
     */
    public void BatteriaMessage(boolean batteria) {
        if (batteria) {
            String tmp;
            tmp = this.masterID + " " + "14" + " " + "BATT ON";
            setMessaggio(tmp);
            sendComand();
        } else {
            String tmp;
            tmp = this.masterID + " " + "14" + " " + "BATT OFF";
            setMessaggio(tmp);
            sendComand();
        }
    }

    /**
     * questo metodo aggiunge numeri nella lista dei numeri da contattare
     * @param pos posizione in lista del numero
     * @param numero numero da chiamare, con prefisso internazionale (0039 per L'italia, etc..)
     */
    public void NumeroChiamareMessage(int pos, String numero) {
        String tmp;
        tmp = this.masterID + " " + "20" + pos + " " + numero + " " + "PHONE NUMBER";
        setMessaggio(tmp);
        sendComand();
    }

    /**
     * metodo per la configurazione della lingua nei messaggi dal master al cellulare
     * @param lingua lingua da 0 a 6, in ordine: Inglese, Ialiano, Francese, Tedesco, Spagnolo, Olandese, Portoghese
     */
    public void LinguaMessage(int lingua) {
        String tmp;
        tmp = this.masterID + " " + "30" + " " + lingua + " " + "SMS LANGUAGE";
        setMessaggio(tmp);
        sendComand();
    }

    /**
     * questo metodo attiva la modalità furto
     */
    public void FurtoMessage() {
        String tmp;
        tmp = this.masterID + " " + "40" + " " + "THEFT ON";
        setMessaggio(tmp);
        sendComand();
    }

    /**
     * Questo metodo rimuove dal sistema un lucchetto
     * @param lucchetto ID del lucchetto da rimuovere
     */
    public void RimozioneLucchettoMessage(int lucchetto) {
        String tmp;
        tmp = this.masterID + " " + "50" + " " + lucchetto + " " + "REMOVE";
        setMessaggio(tmp);
        sendComand();
    }

    /**
     * Questo metodo attiva il flag dell'accensione del master: ON il master avvisa quando si accende
     * @param accensione se true è ON, false altrimenti
     */
    public void AccensioneMasterMessage(boolean accensione) {
        if (accensione) {
            String tmp;
            tmp = this.masterID + " " + "51" + " " + "MA ON";
            setMessaggio(tmp);
            sendComand();
        } else {
            String tmp;
            tmp = this.masterID + " " + "51" + " " + "MA OFF";
            setMessaggio(tmp);
            sendComand();
        }
    }

    /**
     * this method sends all the custom configuration in one SMS
     * @param batt true is ON, false otherwise
     * @param acc true is ON, false otherwise
     * @param intervallo the time-out interval tocheck the lock. can be 0-5 (0 means OFF)
     * @param sensibility the value of moovements to activate the moovement alarm (1-3)
     * @return the sent message
     */
    public String TotalCustomSetMessage (boolean batt, boolean acc, int intervallo, int sensibility) {
        String tmp;
        String batteria;
        String accensione;
        if (batt)
            batteria = " BATT ON ";
        else
            batteria = " BATT OFF ";
        if (acc)
            accensione =" MA ON";
        else
            accensione = " MA OFF";

        tmp = this.masterID + " " + "16" + batteria + "SENS "  + sensibility+ " TIMEOUT " +intervallo  + accensione;
        setMessaggio(tmp);
        sendComand();
        Log.println(Log.INFO,"Messaggio mandato",tmp);
        return tmp;
    }

}
