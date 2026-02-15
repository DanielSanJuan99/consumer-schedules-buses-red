package com.busesred.consumer.schedules.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleMessage implements Serializable {
    private String busId;
    private String route;
    private String routeName;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private String changeType;
    private String description;
    private LocalDateTime timestamp;
    private String origin;
    private String destination;
}
