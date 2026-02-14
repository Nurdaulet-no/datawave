package org.dw.datawave.service;

import org.dw.datawave.repository.TickRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class AdminService {
    private final TickRepository repository;

    public AdminService(TickRepository repository){
        this.repository = repository;
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void truncatePartition(){
        LocalDate yesterday  = LocalDate.now().minusDays(1);
        try {
            repository.truncateByRange(yesterday);
        }catch (Exception e){
            System.err.println("Failed truncate partition: " + e.getMessage());
        }
    }
}
