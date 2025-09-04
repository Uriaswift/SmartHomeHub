package ru.homeswift.smarthome_hub.service;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Service;

@Service
public class MqttGateway {

    private final IMqttClient client;

    public MqttGateway(IMqttClient client) {
        this.client = client;
    }

    /**
     * Публикация простого сообщения с QoS=0 без retain.
     */
    public void publish(String topic, String payload) {
        publish(topic, payload, 0, false);
    }

    /**
     * Публикация сообщения в MQTT.
     *
     * @param topic   - топик
     * @param payload - тело сообщения
     * @param qos     - уровень QoS (0,1,2)
     * @param retained - сохранить ли последнее сообщение в брокере
     */
    public void publish(String topic, String payload, int qos, boolean retained) {
        try {
            ensureConnected();
            MqttMessage msg = new MqttMessage(payload.getBytes());
            msg.setQos(qos);
            msg.setRetained(retained);
            client.publish(topic, msg);
        } catch (Exception e) {
            throw new RuntimeException("MQTT publish failed: topic=" + topic, e);
        }
    }

    /**
     * Подписка на топик без коллбека (только факт подписки).
     */
    public void subscribe(String topicFilter) {
        try {
            ensureConnected();
            client.subscribe(topicFilter);
        } catch (Exception e) {
            throw new RuntimeException("MQTT subscribe failed: filter=" + topicFilter, e);
        }
    }

    /**
     * Подписка на топик с обработчиком сообщений.
     *
     * @param topicFilter - фильтр топика (можно с подстановками, например home/+/state)
     * @param listener    - коллбек для входящих сообщений
     */
    public void subscribe(String topicFilter, IMqttMessageListener listener) {
        try {
            ensureConnected();
            client.subscribe(topicFilter, listener);
        } catch (Exception e) {
            throw new RuntimeException("MQTT subscribe with listener failed: filter=" + topicFilter, e);
        }
    }

    /**
     * Доступ к "сырому" клиенту, если нужно делать что-то напрямую.
     */
    public IMqttClient raw() {
        return client;
    }

    // --- private helpers ---

    private void ensureConnected() throws Exception {
        if (!client.isConnected()) {
            client.reconnect();
        }
    }
}