package ru.homeswift.smarthome_hub.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Управление службами NanoPi (FriendlyWrt/OpenWrt/Systemd) локально или по SSH.
 *
 * Режим выбирается переменной окружения SMH_SYSTEM_MODE:
 *  - "local" (по умолчанию) — исполняем команды на локальной машине
 *  - "ssh"                — отправляем команду на удалённый хост по ssh
 *
 * Для ssh-режима:
 *   SMH_SSH_HOST  — хост (обязательно)
 *   SMH_SSH_USER  — пользователь (по умолчанию "root")
 *   SMH_SSH_PORT  — порт (по умолчанию "22")
 *   SMH_SSH_KEY   — путь к приватному ключу (желательно)
 *   SMH_SSH_PASS  — пароль (опционально; потребуется sshpass в контейнере/системе)
 *
 * Безопасность: есть allowlist сервисов (см. ALLOWED_SERVICES).
 */
@RestController
@RequestMapping("/api/system")
public class SystemServiceController {

    // Разрешённые сервисы (добавляй сюда свои названия)
    private static final Set<String> ALLOWED_SERVICES = new HashSet<>(Arrays.asList(
            "zapret", "xray", "mosquitto", "dnsmasq", "uhttpd", "network"
    ));

    private static final Set<String> ALLOWED_ACTIONS = new HashSet<>(Arrays.asList(
            "start", "stop", "restart", "reload", "enable", "disable", "status"
    ));

    // ---------- HTTP endpoint ----------
    @PostMapping("/{service}/{action}")
    public ResponseEntity<?> control(@PathVariable String service,
                                     @PathVariable String action) {
        // Базовая валидация
        if (!service.matches("[a-zA-Z0-9._-]+")) {
            return bad("Bad service name");
        }
        if (!ALLOWED_ACTIONS.contains(action)) {
            return bad("Unsupported action: " + action);
        }
        if (!ALLOWED_SERVICES.isEmpty() && !ALLOWED_SERVICES.contains(service)) {
            return bad("Service is not in allowlist: " + service);
        }

        String mode = env("SMH_SYSTEM_MODE", "local").trim().toLowerCase(Locale.ROOT);
        String cmd = buildCommand(service, action); // подберём systemctl или /etc/init.d

        ExecResult res;
        try {
            if ("ssh".equals(mode)) {
                res = execSsh(cmd);
            } else {
                res = execLocal(cmd);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "ok", false,
                    "error", e.getMessage()
            ));
        }

        return ResponseEntity.status(res.code == 0 ? 200 : 500).body(Map.of(
                "ok", res.code == 0,
                "code", res.code,
                "mode", res.mode,
                "cmd", res.cmd,
                "stdout", res.stdout,
                "stderr", res.stderr
        ));
    }

    // ---------- Командостроитель ----------
    private String buildCommand(String service, String action) {
        // Если есть systemctl — используем его
        boolean hasSystemctl = (execQuiet("command -v systemctl") == 0);
        if (hasSystemctl) {
            return "systemctl " + action + " " + service;
        }
        // Иначе FriendlyWrt/OpenWrt
        return "/etc/init.d/" + service + " " + action;
    }

    // ---------- Исполнение локально ----------
    private ExecResult execLocal(String cmd) throws Exception {
        Process p = new ProcessBuilder("/bin/sh", "-c", cmd)
                .redirectErrorStream(false)
                .start();
        String out = read(p.getInputStream());
        String err = read(p.getErrorStream());
        int code = p.waitFor();
        return new ExecResult(code, out, err, cmd, "local");
    }

    // ---------- Исполнение по SSH ----------
    private ExecResult execSsh(String cmd) throws Exception {
        String host = env("SMH_SSH_HOST", null);
        String user = env("SMH_SSH_USER", "root");
        String port = env("SMH_SSH_PORT", "22");
        String key  = env("SMH_SSH_KEY", null);
        String pass = env("SMH_SSH_PASS", null);

        if (host == null || host.isBlank()) {
            throw new IllegalStateException("SMH_SSH_HOST is not set");
        }

        List<String> args = new ArrayList<>();
        // Если хотим пароль — потребуется sshpass
        if (pass != null && !pass.isBlank()) {
            if (execQuiet("command -v sshpass") == 0) {
                args.add("sshpass"); args.add("-p"); args.add(pass);
            } else {
                throw new IllegalStateException("Password auth requires sshpass. Use SMH_SSH_KEY or install sshpass.");
            }
        }

        args.add("ssh");
        args.add("-p"); args.add(port);
        args.add("-o"); args.add("StrictHostKeyChecking=no");
        if (key != null && !key.isBlank()) {
            args.add("-i"); args.add(key);
        }
        args.add(user + "@" + host);
        args.add(cmd);

        Process p = new ProcessBuilder(args).start();
        String out = read(p.getInputStream());
        String err = read(p.getErrorStream());
        int code = p.waitFor();
        String full = String.join(" ", args);

        return new ExecResult(code, out, err, full, "ssh");
    }

    // ---------- Утилиты ----------
    private int execQuiet(String command) {
        try {
            Process p = new ProcessBuilder("/bin/sh", "-c", command).start();
            return p.waitFor();
        } catch (Exception e) {
            return 1;
        }
    }

    private String read(InputStream is) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append('\n');
            return sb.toString();
        }
    }

    private String env(String k, String def) {
        String v = System.getenv(k);
        return v != null ? v : def;
    }

    private ResponseEntity<?> bad(String msg) {
        return ResponseEntity.badRequest().body(Map.of("ok", false, "error", msg));
    }

    // маленький holder
    private static class ExecResult {
        final int code; final String stdout; final String stderr; final String cmd; final String mode;
        ExecResult(int code, String stdout, String stderr, String cmd, String mode) {
            this.code = code; this.stdout = stdout; this.stderr = stderr; this.cmd = cmd; this.mode = mode;
        }
    }
}