package org.dw.datawave.repository;

import org.dw.datawave.model.Tick;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class TickRepository {
    private final JdbcTemplate jdbcTemplate;

    public TickRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    // write
    public void insertBatch(List<Tick> ticks){
        jdbcTemplate.batchUpdate(
                """
            insert into ticks(symbol, price, volume, created_at)
            values (?, ?, ?, ?)
            """,ticks, 6000,
                (ps, tick) -> {
                    ps.setString(1, tick.symbol());
                    ps.setDouble(2, tick.price());
                    ps.setInt(3, tick.volume());
                    ps.setTimestamp(4, Timestamp.from(tick.createdAt()));
                }
        );
    }
}
