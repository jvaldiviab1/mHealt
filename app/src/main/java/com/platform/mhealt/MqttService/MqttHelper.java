package com.platform.mhealt.MqttService;

import android.util.Log;


import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

public class MqttHelper implements MqttCallback {

    private MqttHelperListener mqttHelperListener;
    private final static String TAG = "MqttHelper";
    private final static String HOST = "tcp://demo.thingsboard.io:1883";

    private final static String CLIENTID = "DANP";
    private final static String USERNAME = "DANP";
    private final static String PASSWORD = "123123";
    private MqttAsyncClient mqttAndroidClient;
    private static MqttHelper instance;
    private static String TOPIC = "v1/devices/me/telemetry";
    private static int QOS = 0;
    private static int DATA = 150;

    private final int MAX_SIZE = 1000000;
    private MqttMessageWrapper mqttMessageWrapperArray[] = new MqttMessageWrapper[MAX_SIZE];
    private static int COUNTER = 0;


    public static MqttHelper getInstance() {
        if (instance == null) {
            instance = new MqttHelper();
            Log.d(TAG, "starting MqttHelper ... ");
        }
        return instance;
    }

    private MqttHelper() {
        if (TOPIC.trim() == "") try {
            throw new Exception("Topic was not defined");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (QOS < 0) try {
            throw new Exception("QOS was not defined");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            //String clientId = MqttAsyncClient.generateClientId();
            mqttAndroidClient = new MqttAsyncClient(HOST, CLIENTID, new MemoryPersistence());
            connect();
            mqttAndroidClient.setCallback(this);

        } catch (MqttException e) {
            e.printStackTrace();

        }

    }

    public static void setInitParameters(String topic, int qos) {
        TOPIC = topic;
        QOS = qos;
    }

    public void setMqttHelperListener(MqttHelperListener mqttHelperListener) {
        this.mqttHelperListener = mqttHelperListener;
    }

    @Override
    public void connectionLost(Throwable cause) {
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        try {

            MqttMessageWrapper msg = (MqttMessageWrapper) ToolHelper.deserialize(message.getPayload());
            msg.setTimeEnd(System.currentTimeMillis());
            msg.setOrderArrive(COUNTER);
            mqttMessageWrapperArray[COUNTER] = msg;
            String msg2 = COUNTER + ":" + msg.getOrderSend() + ":" + msg.getTimeInit() + ":" + msg.getTimeEnd();// + ":" + (new String(message.getPayload()));
            mqttHelperListener.displayMessage(msg2);
            COUNTER++;
            Log.d(TAG, msg2);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

    private void connect() {
        try {

            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setAutomaticReconnect(true);
            mqttConnectOptions.setCleanSession(false);
            mqttConnectOptions.setUserName(USERNAME);
            mqttConnectOptions.setPassword(PASSWORD.toCharArray());
            mqttConnectOptions.setMaxInflight(10);

            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "connect to: onSuccess");

                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    //subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "Failed to connect to: " + exception.toString());

                }
            });


        } catch (MqttException ex) {
            ex.printStackTrace();
        }

    }

    public void publishBatch(int message) throws InterruptedException {
        int _delay = 1 * 1000;
        try {
            mqttMessageWrapperArray = new MqttMessageWrapper[MAX_SIZE];
            MqttMessage mqttMessage;
            if (mqttAndroidClient.isConnected()) {
                String jsonString = "{\"Message\":"+message+"}";
                JSONObject json = new JSONObject(jsonString);
                byte[] objAsBytes = json.toString().getBytes("UTF-8");

                mqttAndroidClient.publish(TOPIC,objAsBytes,QOS,true);

                mqttMessage = new MqttMessage();
                mqttMessage.setPayload(objAsBytes);
                mqttMessage.setQos(QOS);

                Log.d(TAG, "Message sent :"+message);
            }else {
                mqttAndroidClient.connect();
                Log.d(TAG, " is running:");
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
        Thread.sleep(_delay);


    }

    private IMqttActionListener publisher_IMqttActionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            Log.d(TAG, "Publisher_IMqttActionListener: onSuccess");
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            Log.d(TAG, "Publisher_IMqttActionListener: onFailure");
        }
    };

    private IMqttActionListener subscriber_IMqttActionListener = new IMqttActionListener() {

        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            Log.d(TAG, "Subscriber_IMqttActionListener: onSuccess");
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            Log.d(TAG, "Subscriber_IMqttActionListener: onFailure");
        }
    };

    public void close() {
        try {
            mqttAndroidClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            mqttHelperListener.saveMessage(mqttMessageWrapperArray, COUNTER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
