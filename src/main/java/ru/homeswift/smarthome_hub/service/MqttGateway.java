package ru.homeswift.smarthome_hub.service;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Service;

@Service
public class MqttGateway {

    private final IMqttClient client;

    public MqttGateway(IMqttClient client) {
        this.client = client;
    }

    public void publish(String topic, String payload) {
        publish(topic, payload, 0, false);
    }

    public void publish(String topic, String payload, int qos, boolean retained) {
        try {
            if (!client.isConnected()) {
                client.reconnect();
            }
            MqttMessage msg = new MqttMessage(payload.getBytes());
            msg.setQos(qos);
            msg.setRetained(retained);
            client.publish(topic, msg);
        } catch (Exception e) {
            throw new RuntimeException("MQTT publish failed: topic=" + topic, e);
        }
    }

    public void subscribe(String topicFilter) {
        try {
            if (!client.isConnected()) {
                client.reconnect();
            }
            client.subscribe(topicFilter);
        } catch (Exception e) {
            throw new RuntimeException("MQTT subscribe failed: filter=" + topicFilter, e);
        }
    }

    public IMqttClient raw() {
        return client;
    }
}