package ru.homeswift.smarthome_hub.api;

import org.springframework.web.bind.annotation.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

@RestController
@RequestMapping("/api/system")
public class SystemServiceController {

    private String exec(String cmd) {
        try {
            Process process = new ProcessBuilder("bash", "-c", cmd).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();
            return output.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @PostMapping("/{service}/start")
    public String startService(@PathVariable String service) {
        return exec("systemctl start " + service);
    }

    @PostMapping("/{service}/stop")
    public String stopService(@PathVariable String service) {
        return exec("systemctl stop " + service);
    }

    @GetMapping("/{service}/status")
    public String statusService(@PathVariable String service) {
        return exec("systemctl status " + service + " --no-pager");
    }

    @PostMapping("/{service}/restart")
    public String restartService(@PathVariable String service) {
        return exec("systemctl restart " + service);
    }
}