package com.busesred.consumer.schedules.service;

import com.busesred.consumer.schedules.model.ScheduleMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class ScheduleConsumerService {

    @Value("${file.storage.path}")
    private String storagePath;

    private final ObjectMapper objectMapper;

    public ScheduleConsumerService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @PostConstruct
    public void init() {
        try {
            Path path = Paths.get(storagePath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("Directorio creado: {}", storagePath);
            }
        } catch (IOException e) {
            log.error("Error al crear directorio: ", e);
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.schedules}")
    public void receiveSchedule(ScheduleMessage scheduleMessage) {
        try {
            log.info("Mensaje recibido de RabbitMQ: {}", scheduleMessage);
            
            // Generar nombre de archivo con timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
            String fileName = String.format("schedule_%s_%s.json", 
                scheduleMessage.getBusId(), 
                timestamp);
            
            // Ruta completa del archivo
            Path filePath = Paths.get(storagePath, fileName);
            
            // Escribir archivo JSON
            objectMapper.writeValue(filePath.toFile(), scheduleMessage);
            
            log.info("Archivo JSON generado exitosamente: {}", filePath);
            
        } catch (Exception e) {
            log.error("Error al procesar mensaje de horario: ", e);
            throw new RuntimeException("Error al generar archivo JSON", e);
        }
    }

    public File[] getStoredFiles() {
        File directory = new File(storagePath);
        return directory.listFiles();
    }

    public File getFileByName(String fileName) {
        File file = new File(storagePath, fileName);
        if (file.exists() && file.isFile()) {
            return file;
        }
        return null;
    }
}
