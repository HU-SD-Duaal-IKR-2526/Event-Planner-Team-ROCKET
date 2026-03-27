package com.elton.eventplanner.event.infrastructure.persistence;

import com.elton.eventplanner.event.domain.model.EventStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "tb_event")
public class EventJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private LocalDate date;
    private String location;
    private String description;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    @Column(name = "user_id")
    private Long userId;

    public EventJpaEntity() {}

    public EventJpaEntity(Long id, String name, LocalDate date, String location,
                          String description, EventStatus status, Long userId) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.location = location;
        this.description = description;
        this.status = status;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public EventStatus getStatus() {
        return status;
    }

    public Long getUserId() {
        return userId;
    }
}
