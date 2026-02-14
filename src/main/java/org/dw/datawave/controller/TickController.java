package org.dw.datawave.controller;

import org.dw.datawave.dto.TickRangeQuery;
import org.dw.datawave.dto.TickRangeUpdate;
import org.dw.datawave.dto.TimeRange;
import org.dw.datawave.model.Tick;
import org.dw.datawave.service.EventService;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController()
@RequestMapping("/api/v1/ticks")
public class TickController {
    private final EventService eventService;

    public TickController(EventService eventService){
        this.eventService = eventService;
    }

    @GetMapping
    public List<Tick> getTicks(@RequestParam Instant from, @RequestParam Instant to, @RequestParam int limit){
        return eventService.getTickRange(new TickRangeQuery(new TimeRange(from, to), limit));
    }

    @PostMapping
    public void ingest(@RequestBody Tick tick){
        eventService.ingestion(tick);
    }

    @PatchMapping("/volume")
    public void updateTickByRange(@RequestParam int delta, @RequestParam Instant from , @RequestParam Instant to){
        eventService.updateTickByRange(new TickRangeUpdate(new TimeRange(from, to), delta));
    }
}
