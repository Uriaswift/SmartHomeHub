package ru.homeswift.smarthome_hub.mqtt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Slf4j
@Component
@RequiredArgsConstructor
public class MqttCommandListener {

    private final IMqttClient client;

    private static final String COMMANDS_TOPIC = "smarthome/commands/#";

    @PostConstruct
    void init() {
        try {
            // Подписываемся на все команды
            client.subscribe(COMMANDS_TOPIC, 1, (topic, msg) -> handleMessage(topic, msg));
            log.info("✅ Subscribed to MQTT topic: {}", COMMANDS_TOPIC);
        } catch (MqttException e) {
            throw new IllegalStateException("❌ Failed to subscribe to MQTT topic: " + COMMANDS_TOPIC, e);
        }
    }

    private void handleMessage(String topic, MqttMessage message) {
        String payload = new String(message.getPayload());
        log.info("📩 MQTT command received: topic={}, payload={}", topic, payload);

        // TODO: тут можно вызвать DeviceService.toggle() или setState()
    }
}