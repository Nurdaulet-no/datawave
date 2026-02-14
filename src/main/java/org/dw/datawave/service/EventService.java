package org.dw.datawave.service;

import org.dw.datawave.model.Tick;
import org.dw.datawave.repository.TickRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

@Service
public class EventService {
    private final TickRepository repository;
    private final int bufSize = 200_000;
    private final int batchSize = 6000;
    private final ArrayBlockingQueue<Tick> queue = new ArrayBlockingQueue<>(bufSize);

    private final Duration flush = Duration.ofMillis(50);

    public EventService(TickRepository repository){
        this.repository = repository;
        processBatch();
    }

    public void ingestion(Tick tick){
        try {
            queue.put(tick);
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted ", e);
        }
    }

    private void processBatch(){
        Thread t = new Thread(() -> {
            List<Tick> batch = new ArrayList<>(batchSize);
            long lastFlush = System.nanoTime();

            while(!Thread.currentThread().isInterrupted()){
                try {
                    Tick item = queue.poll(flush.toMillis(), TimeUnit.MILLISECONDS);
                    if(item != null) batch.add(item);

                    boolean sizeReady = batch.size() >= batchSize;
                    boolean timeReady = (System.nanoTime() - lastFlush) >= flush.toNanos();

                    if(!batch.isEmpty() && (sizeReady || timeReady)){
                        repository.insertBatch(batch);
                        batch.clear();
                        lastFlush = System.nanoTime();
                    }
                }catch (InterruptedException e){
                    Thread.currentThread().interrupt();
                }catch (Exception e){
                    e.printStackTrace();
                    batch.clear();
                }
            }
        });

        t.setName("tick-batch-wr");
        t.setDaemon(true);
        t.start();
    }
}
