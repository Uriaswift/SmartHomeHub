package ru.homeswift.smarthome_hub.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.homeswift.smarthome_hub.domain.Device;

import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByTopic(String topic);
}