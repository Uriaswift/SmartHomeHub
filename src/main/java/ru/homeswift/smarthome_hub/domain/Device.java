package ru.homeswift.smarthome_hub.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // Тип устройства (свет, розетка и т.п.). Пока строкой, позже можно сделать ENUM.
    @Column(nullable = false)
    private String type;

    // MQTT topic, куда шлем команды / откуда слушаем события
    @Column(nullable = false, unique = true)
    private String topic;

    @Column(nullable = false)
    private boolean online = true;

    @Column(nullable = false)
    private boolean onState = false;

    public Device() {}

    public Device(String name, String type, String topic, boolean online, boolean onState) {
        this.name = name;
        this.type = type;
        this.topic = topic;
        this.online = online;
        this.onState = onState;
    }

    // getters/setters

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    public String getTopic() { return topic; }

    public void setTopic(String topic) { this.topic = topic; }

    public boolean isOnline() { return online; }

    public void setOnline(boolean online) { this.online = online; }

    public boolean isOnState() { return onState; }

    public void setOnState(boolean onState) { this.onState = onState; }
}