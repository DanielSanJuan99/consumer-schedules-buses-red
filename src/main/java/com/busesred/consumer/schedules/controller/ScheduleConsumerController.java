package com.busesred.consumer.schedules.controller;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.busesred.consumer.schedules.service.ScheduleConsumerService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/consumer-schedules")
@Slf4j
public class ScheduleConsumerController {

    private final ScheduleConsumerService consumerService;

    public ScheduleConsumerController(ScheduleConsumerService consumerService) {
        this.consumerService = consumerService;
    }

    @GetMapping("/files")
    public ResponseEntity<Map<String, Object>> getStoredFiles() {
        log.info("Consultando archivos almacenados");
        
        File[] files = consumerService.getStoredFiles();
        
        Map<String, Object> response = new HashMap<>();
        response.put("totalFiles", files != null ? files.length : 0);
        
        if (files != null && files.length > 0) {
            List<Map<String, Object>> fileList = new ArrayList<>();
            for (File file : files) {
                Map<String, Object> fileInfo = new HashMap<>();
                fileInfo.put("name", file.getName());
                fileInfo.put("size", file.length());
                fileInfo.put("lastModified", new Date(file.lastModified()));
                fileList.add(fileInfo);
            }
            response.put("files", fileList);
        } else {
            response.put("files", Collections.emptyList());
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "consumer-schedules-buses-red");
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
}
