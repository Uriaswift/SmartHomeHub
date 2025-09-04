package ru.homeswift.smarthome_hub.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.homeswift.smarthome_hub.api.dto.DeviceDto;
import ru.homeswift.smarthome_hub.domain.Device;
import ru.homeswift.smarthome_hub.integration.ha.HomeAssistantDiscoveryPublisher;
import ru.homeswift.smarthome_hub.repo.DeviceRepository;

import java.util.List;

@Service
public class DeviceService {

    private final DeviceRepository repo;
    private final MqttGateway mqtt;
    private final HomeAssistantDiscoveryPublisher haPublisher;
    private final ObjectMapper mapper = new ObjectMapper();

    public DeviceService(DeviceRepository repo, MqttGateway mqtt, HomeAssistantDiscoveryPublisher haPublisher) {
        this.repo = repo;
        this.mqtt = mqtt;
        this.haPublisher = haPublisher;
    }

    public List<DeviceDto> getAll() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

    public DeviceDto getById(Long id) {
        return toDto(find(id));
    }

    @Transactional
    public DeviceDto create(DeviceDto dto) {
        Device d = new Device(dto.name(), dto.type(), dto.topic(),
                dto.online() != null && dto.online(),
                dto.onState() != null && dto.onState());
        Device saved = repo.save(d);

        // Discovery для HA
        haPublisher.publishDiscovery(saved);

        // Публикуем текущее состояние
        publishState(saved);
        return toDto(saved);
    }

    @Transactional
    public DeviceDto update(Long id, DeviceDto dto) {
        Device d = find(id);
        boolean stateChanged = dto.onState() != null && dto.onState() != d.isOnState();

        d.setName(dto.name());
        d.setType(dto.type());
        d.setTopic(dto.topic());
        d.setOnline(dto.online() != null && dto.online());
        d.setOnState(dto.onState() != null && dto.onState());

        Device saved = repo.save(d);

        haPublisher.publishDiscovery(saved);
        if (stateChanged) publishState(saved);
        return toDto(saved);
    }

    @Transactional
    public void delete(Long id) {
        Device d = find(id);
        haPublisher.removeDiscovery(d);
        repo.delete(d);
    }

    @Transactional
    public DeviceDto toggle(Long id) {
        Device d = find(id);
        d.setOnState(!d.isOnState());
        Device saved = repo.save(d);
        publishState(saved);
        return toDto(saved);
    }

    @Transactional
    public DeviceDto setState(Long id, boolean on) {
        Device d = find(id);
        d.setOnState(on);
        Device saved = repo.save(d);
        publishState(saved);
        return toDto(saved);
    }

    // ---------- helpers ----------

    private Device find(Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Device not found: id=" + id));
    }

    private DeviceDto toDto(Device d) {
        return new DeviceDto(d.getId(), d.getName(), d.getType(), d.getTopic(), d.isOnline(), d.isOnState());
    }

    private void publishState(Device d) {
        try {
            // 1) JSON в базовый топик — по желанию
            var payload = mapper.createObjectNode()
                    .put("id", d.getId())
                    .put("name", d.getName())
                    .put("type", d.getType())
                    .put("online", d.isOnline())
                    .put("on", d.isOnState())
                    .toString();
            mqtt.publish(d.getTopic(), payload);

            // 2) Для HA: ON/OFF в <base>/state
            String stateTopic = d.getTopic() + "/state";
            mqtt.publish(stateTopic, d.isOnState() ? "ON" : "OFF", 1, true); // retained=true
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish device state", e);
        }
    }
}