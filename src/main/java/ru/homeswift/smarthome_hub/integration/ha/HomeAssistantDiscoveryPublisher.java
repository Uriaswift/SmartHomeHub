package ru.homeswift.smarthome_hub.integration.ha;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.homeswift.smarthome_hub.domain.Device;
import ru.homeswift.smarthome_hub.domain.DeviceType;
import ru.homeswift.smarthome_hub.service.MqttGateway;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class HomeAssistantDiscoveryPublisher {

    private final MqttGateway mqtt;
    private final ObjectMapper om = new ObjectMapper();

    /** Публикуем discovery при создании/обновлении. */
    public void publishDiscovery(Device d) {
        if (Objects.equals(d.getType(), DeviceType.LIGHT.toString())) {
            publishLight(d);
        }
        // TODO: сюда же позже добавим SWITCH/SENSOR/DIMMER и т.д.
    }

    private void publishLight(Device d) {
        try {
            // базовый топик устройства, который хранится у нас, например: smarthome/devices/kitchen/light1
            String base = d.getTopic();

            // для HA discovery нужен уникальный object_id, пусть device_<id>
            String objectId = "device_" + d.getId();

            // стандарт MQTT Light: command_topic и state_topic
            String cmdTopic   = base + "/set";
            String stateTopic = base + "/state";

            Map<String, Object> payload = new HashMap<>();
            payload.put("name", d.getName());
            payload.put("uniq_id", objectId);
            payload.put("cmd_t", cmdTopic);   // command_topic
            payload.put("stat_t", stateTopic); // state_topic
            payload.put("pl_on", "ON");
            payload.put("pl_off", "OFF");

            Map<String, Object> device = new HashMap<>();
            device.put("identifiers", new String[]{ objectId });
            device.put("name", d.getName());
            payload.put("dev", device);

            String discoveryTopic = "homeassistant/light/" + objectId + "/config";
            mqtt.publish(discoveryTopic, om.writeValueAsString(payload), 1, true); // retained=true, чтобы HA увидел после рестарта
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish HA discovery for device id=" + d.getId(), e);
        }
    }

    /** На удаление устройства публикуем пустой конфиг (так HA его уберёт). */
    public void removeDiscovery(Device d) {
        try {
            String objectId = "device_" + d.getId();
            String discoveryTopic = "homeassistant/light/" + objectId + "/config";
            mqtt.publish(discoveryTopic, "", 1, true);
        } catch (Exception e) {
            throw new RuntimeException("Failed to remove HA discovery for device id=" + d.getId(), e);
        }
    }
}