package com.elton.eventplanner.event.presentation;

import com.elton.eventplanner.controllers.exceptions.GlobalExceptionHandler;
import com.elton.eventplanner.event.application.EventApplicationService;
import com.elton.eventplanner.event.application.dto.EventResult;
import com.elton.eventplanner.event.domain.exception.EventAlreadyCancelledException;
import com.elton.eventplanner.event.domain.exception.EventNotFoundException;
import com.elton.eventplanner.event.presentation.request.CreateEventRequest;
import com.elton.eventplanner.event.presentation.request.UpdateEventRequest;
import com.elton.eventplanner.services.exceptions.RoleNotAllowedException;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
@Import(GlobalExceptionHandler.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventApplicationService eventService;

    @Autowired
    private ObjectMapper objectMapper;

    private EventResult sampleResult;

    @BeforeEach
    void setUp() {
        sampleResult = new EventResult(
                1L, "Team Rocket Kickoff", LocalDate.of(2026, 6, 15),
                "Amsterdam", "Een kickoff meeting voor Team Rocket.", "PLANNED", 1L
        );
    }

    @Test
    void findAll_returns200WithList() throws Exception {
        when(eventService.findAll()).thenReturn(List.of(sampleResult));

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Team Rocket Kickoff"))
                .andExpect(jsonPath("$[0].status").value("PLANNED"));
    }

    @Test
    void findAll_emptyList_returns200WithEmptyArray() throws Exception {
        when(eventService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void findById_found_returns200() throws Exception {
        when(eventService.findById(1L)).thenReturn(sampleResult);

        mockMvc.perform(get("/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Team Rocket Kickoff"))
                .andExpect(jsonPath("$.location").value("Amsterdam"));
    }

    @Test
    void findById_notFound_returns404() throws Exception {
        when(eventService.findById(99L)).thenThrow(new EventNotFoundException(99L));

        mockMvc.perform(get("/events/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_valid_returns201WithLocation() throws Exception {
        when(eventService.create(any())).thenReturn(sampleResult);

        CreateEventRequest request = new CreateEventRequest(
                "Team Rocket Kickoff", LocalDate.of(2026, 6, 15),
                "Amsterdam", "Een kickoff meeting voor Team Rocket.", 1L
        );

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/events/1")))
                .andExpect(jsonPath("$.name").value("Team Rocket Kickoff"));
    }

    @Test
    void create_blankName_returns400() throws Exception {
        CreateEventRequest request = new CreateEventRequest(
                "", LocalDate.of(2026, 6, 15),
                "Amsterdam", "Een kickoff meeting voor Team Rocket.", 1L
        );

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_roleNotAllowed_returns403() throws Exception {
        when(eventService.create(any())).thenThrow(
                new RoleNotAllowedException("USER", "manage an event"));

        CreateEventRequest request = new CreateEventRequest(
                "Team Rocket Kickoff", LocalDate.of(2026, 6, 15),
                "Amsterdam", "Een kickoff meeting voor Team Rocket.", 2L
        );

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void update_valid_returns200() throws Exception {
        when(eventService.update(any())).thenReturn(sampleResult);

        UpdateEventRequest request = new UpdateEventRequest(
                "Team Rocket Kickoff", LocalDate.of(2026, 6, 15),
                "Amsterdam", "Een kickoff meeting voor Team Rocket.", "PLANNED", 1L
        );

        mockMvc.perform(put("/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Team Rocket Kickoff"));
    }

    @Test
    void update_notFound_returns404() throws Exception {
        when(eventService.update(any())).thenThrow(new EventNotFoundException(99L));

        UpdateEventRequest request = new UpdateEventRequest(
                "Team Rocket Kickoff", LocalDate.of(2026, 6, 15),
                "Amsterdam", "Een kickoff meeting voor Team Rocket.", "PLANNED", 1L
        );

        mockMvc.perform(put("/events/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_exists_returns204() throws Exception {
        doNothing().when(eventService).delete(1L);

        mockMvc.perform(delete("/events/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        doThrow(new EventNotFoundException(99L)).when(eventService).delete(99L);

        mockMvc.perform(delete("/events/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancel_valid_returns200WithCancelledStatus() throws Exception {
        EventResult cancelled = new EventResult(
                1L, "Team Rocket Kickoff", LocalDate.of(2026, 6, 15),
                "Amsterdam", "Een kickoff meeting voor Team Rocket.", "CANCELLED", 1L
        );
        when(eventService.cancel(1L)).thenReturn(cancelled);

        mockMvc.perform(put("/events/cancel/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cancel_notFound_returns404() throws Exception {
        when(eventService.cancel(99L)).thenThrow(new EventNotFoundException(99L));

        mockMvc.perform(put("/events/cancel/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancel_alreadyCancelled_returns409() throws Exception {
        when(eventService.cancel(1L)).thenThrow(new EventAlreadyCancelledException(1L));

        mockMvc.perform(put("/events/cancel/1"))
                .andExpect(status().isConflict());
    }

    @Test
    void autoStatusUpdate_valid_returns200() throws Exception {
        when(eventService.autoStatusUpdate(1L)).thenReturn(sampleResult);

        mockMvc.perform(put("/events/autostatusupdate/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
