package com.platform.mhealt.MqttService;



public interface MqttHelperListener {
    void displayMessage(String data);
    void saveMessage(MqttMessageWrapper[] data, int size);
}
