package org.dw.datawave.controller;

import org.dw.datawave.model.Tick;
import org.dw.datawave.service.EventService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/api/v1/ticks")
public class TickController {
    private final EventService eventService;

    public TickController(EventService eventService){
        this.eventService = eventService;
    }

    @PostMapping
    public void ingest(@RequestBody Tick tick){
        eventService.ingestion(tick);
    }
}
