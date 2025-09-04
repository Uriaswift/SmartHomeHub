package ru.homeswift.smarthome_hub.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.homeswift.smarthome_hub.api.dto.DeviceDto;
import ru.homeswift.smarthome_hub.service.DeviceService;

import java.util.List;

@RestController
@RequestMapping(path = "/api/devices", produces = MediaType.APPLICATION_JSON_VALUE)
public class DeviceController {

    private final DeviceService service;

    public DeviceController(DeviceService service) {
        this.service = service;
    }

    @GetMapping
    public List<DeviceDto> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public DeviceDto getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DeviceDto create(@Valid @RequestBody DeviceDto dto) {
        return service.create(dto);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public DeviceDto update(@PathVariable Long id, @Valid @RequestBody DeviceDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PostMapping("/{id}/toggle")
    public DeviceDto toggle(@PathVariable Long id) {
        return service.toggle(id);
    }

    @PostMapping("/{id}/state")
    public DeviceDto setState(@PathVariable Long id, @RequestParam("on") boolean on) {
        return service.setState(id, on);
    }
}