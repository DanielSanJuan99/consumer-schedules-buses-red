package com.busesred.consumer.schedules.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
        response.put("total_archivos", files != null ? files.length : 0);
        
        if (files != null && files.length > 0) {
            List<Map<String, Object>> fileList = new ArrayList<>();
            for (File file : files) {
                Map<String, Object> fileInfo = new HashMap<>();
                fileInfo.put("nombre", file.getName());
                fileInfo.put("tamano", file.length());
                fileInfo.put("ultima_modificacion", new Date(file.lastModified()));
                fileInfo.put("url_vista", "/api/consumer-schedules/files/" + file.getName());
                fileList.add(fileInfo);
            }
            response.put("archivos", fileList);
        } else {
            response.put("archivos", Collections.emptyList());
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/files/{fileName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getFileContent(@PathVariable String fileName) {
        log.info("Obteniendo contenido del archivo: {}", fileName);
        
        try {
            File file = consumerService.getFileByName(fileName);
            
            if (file == null || !file.exists()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Archivo no encontrado");
                error.put("nombre_archivo", fileName);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            String content = Files.readString(file.toPath());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(content);
                    
        } catch (IOException e) {
            log.error("Error al leer archivo: {}", fileName, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al leer archivo");
            error.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("estado", "ACTIVO");
        response.put("servicio", "consumer-schedules-buses-red");
        response.put("marca_tiempo", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
}
