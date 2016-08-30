package com.myclose.mymqtt;

import com.myclose.mymqtt.R;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
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

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MQTTT extends AppCompatActivity {
    private final String TAG = "Debug";
    private MqttAndroidClient mqttAndroidClient;
    private MqttAndroidClient client;
    private String topic_pub = "test/helloAndroid";
    private String topic_sub = "helloAndroid";
    private int qos = 1;
    /*generate random clientId*/
    private String clientId = MqttClient.generateClientId();
    private String server = "ec2-52-40-248-111.us-west-2.compute.amazonaws.com";
    private String port = "1883";
    private String broker = "tcp://" + server + ":" + port;
    private String payload;

    /** username and password
     *
    private String ID = "name@surname";
    private String PASS = "jango";
    */

    /* Switch Button:it exchanges sub and pub, if u wanna chat with a second client*/

    Switch switchButton;

    /*End Switch Button*/

    private Button sendMessageMQTT;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mqtt);
        Toast.makeText(getBaseContext(), "Welcome OnCreate",
                Toast.LENGTH_SHORT).show();

     /** Alternative code
      *  new MqttAndroidClient(this, ""tcp://" + server + ":" + port;", "d:lite:test:")
      */
        /**mqtt client
        mqttAndroidClient = new MqttAndroidClient(this, broker, clientId ) {
            @Override
            public void onReceive(Context context, Intent intent) {
                super.onReceive(context, intent);

                Bundle data = intent.getExtras();

                String action = data.getString("MqttService.callbackAction");
                Object parcel = data.get("MqttService.PARCEL");
                String destinationName = data.getString("MqttService.destinationName");


                if(action.equals("messageArrived"))
                {
                    Log.d(TAG,destinationName + " " + parcel.toString());
                    Toast.makeText(getBaseContext(),destinationName + " " + parcel.toString(),
                            Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getBaseContext(), "sei qui",
                            Toast.LENGTH_LONG).show();

                }

            }
        };
        end mqtt client */

      /** try anc catch connection

        try {

            /** connection options, e.g: password and username
             *
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(ID);
            options.setPassword(PASS.toCharArray());
            */

            /** change  mqttAndroidClient.connect(options, null, new IMqttActionListener()
             * with     mqttAndroidClient.connect(null, new IMqttActionListener()

            mqttAndroidClient.connect(null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    Log.d(TAG, "onSuccess");
                    Toast.makeText(getBaseContext(), "ok",
                            Toast.LENGTH_LONG).show();

                    try {
                        /* default:  mqttAndroidClient.subscribe("hoge@github/#", 0);
                        mqttAndroidClient.subscribe("helloAndroid", 1);
                        Log.d(TAG, "subscribe");
                    } catch (MqttException e) {
                        Log.d(TAG, e.toString());
                    }
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    Log.d(TAG, "onFailure");
                    Toast.makeText(getBaseContext(), "ko",
                            Toast.LENGTH_LONG).show();
                }
            });

        }

        catch (MqttException e) {
            Log.d(TAG, e.toString());
        }
        */
        /*Switch Button code*/
        switchButton = (Switch) findViewById(R.id.switch1);
        switchButton.setTextOn("On");
        switchButton.setTextOff("Off");
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked) {
                String statusSwitch;
                if (bChecked) {

                    statusSwitch = switchButton.getTextOn().toString();


                } else {
                    statusSwitch = switchButton.getTextOff().toString();

                }
                Toast.makeText(getApplicationContext(), "Switch :" + statusSwitch, Toast.LENGTH_SHORT).show();
            }
        });


        /*End Switch Button code*/
        client = new MqttAndroidClient(this.getApplicationContext(), broker,
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
                TextView myAwesomeTextView = (TextView)findViewById(R.id.textView2);
                myAwesomeTextView.setText(new String(mqttMessage. getPayload()));
                System.out.println("QoS: " + mqttMessage. getQos());
                System.out.println("Retained: " + mqttMessage. isRetained());
            }
            @Override
            public void deliveryComplete(final IMqttDeliveryToken iMqttDeliveryToken) {

//When message delivery was complete
            }
        });

        MqttConnectOptions options = new MqttConnectOptions();
        options.setKeepAliveInterval(180);
        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, "onSuccess");
                    Toast.makeText(getBaseContext(), "Connection, ok!",
                            Toast.LENGTH_LONG).show();

                    try {
                        IMqttToken subToken = client.subscribe(topic_sub, qos);
                        subToken.setActionCallback(new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                Toast.makeText(getBaseContext(), "Subscribe, ok!",
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
                    Toast.makeText(getBaseContext(), "wrong",
                            Toast.LENGTH_LONG).show();

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        addListenerOnButton();

    }

    public static boolean isMessageValid(String message) {
        /*replaced false with true */
        boolean isValid = true;
        /*simple validation */
        String expression = "[A-Z]";
        CharSequence inputStr = message;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            /*replaced true with false */
            isValid = false;
        }
        return isValid;
    }

    public void checkMQTTMessage() {

        EditText payloadText = (EditText) findViewById(R.id.editText);
        // check if the field is empty
        if (payloadText.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Field can't be empty",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        {
            if (isMessageValid(payloadText.getText().toString())) {

                payload=payloadText.getText().toString();
                byte[] encodedPayload = new byte[0];
                try {
                    encodedPayload = payload.getBytes("UTF-8");
                    MqttMessage message = new MqttMessage(encodedPayload);
                    client.publish(topic_pub, message);
                    Log.d(TAG, "onSent");
                    Toast.makeText(getBaseContext(), "sent" + "to" + topic_pub,
                            Toast.LENGTH_LONG).show();

                } catch (UnsupportedEncodingException | MqttException e) {
                    e.printStackTrace();
                }

                /*insert everything u wanna send
                long timeStamp = System.nanoTime() / 1000000000;
                */



            } else {
                Toast.makeText(getApplicationContext(), "Text NOT Allowed",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        ;

    }


    public void addListenerOnButton() {
        sendMessageMQTT = (Button) findViewById(R.id.button);
        sendMessageMQTT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (client == null) {
                    Toast.makeText(getBaseContext(), "nessuno",
                            Toast.LENGTH_LONG).show();
                    return;

                } else if (client.isConnected()) {
                    checkMQTTMessage();


                } else {
                    Toast.makeText(getBaseContext(), "NOT sent",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        Toast.makeText(getBaseContext(), "Welcome OnResume",
                Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Toast.makeText(getBaseContext(), "Welcome OnRestart!",
                Toast.LENGTH_SHORT).show();

    }
    @Override
    protected void onStop() {
        super.onStop();
        Toast.makeText(getBaseContext(), "Welcome OnStop!",
                Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onPause() {
        super.onPause();
        Toast.makeText(getBaseContext(), "Welcome OnPause",
                Toast.LENGTH_SHORT).show();
       /**
        try {
           /*replace mqttAndroidClient with client
            if(client.isConnected()) {
                client.disconnect();
                Log.d(TAG,"disconnect");
            }

            client.unregisterResources();

        } catch (MqttException e) {
            Log.d(TAG,e.toString());
        }
       */
    }
}
