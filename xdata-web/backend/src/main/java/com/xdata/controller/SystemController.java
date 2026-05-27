package com.xdata.controller;

import com.xdata.service.AccessControlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
public class SystemController {
    private final AccessControlService accessControlService;

    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        if (!accessControlService.isAdmin()) {
            return ResponseEntity.status(403).build();
        }

        Map<String, Object> status = new HashMap<>();
        
        // Memory
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        status.put("memoryUsed", memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024));
        status.put("memoryMax", memoryBean.getHeapMemoryUsage().getMax() / (1024 * 1024));
        
        // OS
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        status.put("osName", osBean.getName());
        status.put("availableProcessors", osBean.getAvailableProcessors());
        status.put("systemLoad", osBean.getSystemLoadAverage());
        
        // Disk
        File root = new File("/");
        status.put("diskTotal", root.getTotalSpace() / (1024 * 1024 * 1024));
        status.put("diskFree", root.getFreeSpace() / (1024 * 1024 * 1024));
        
        // Z3 Check
        boolean z3Available = false;
        try {
            Process p = Runtime.getRuntime().exec("z3 --version");
            z3Available = p.waitFor() == 0;
        } catch (Exception ignored) {}
        status.put("z3Available", z3Available);
        
        status.put("uptime", ManagementFactory.getRuntimeMXBean().getUptime() / 1000);
        
        return ResponseEntity.ok(status);
    }
}
