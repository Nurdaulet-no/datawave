package org.dw.datawave.repository;

import org.dw.datawave.dto.TickRangeQuery;
import org.dw.datawave.dto.TickRangeUpdate;
import org.dw.datawave.dto.TimeRange;
import org.dw.datawave.model.Tick;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
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

    public List<Tick> findRange(TickRangeQuery rangeRequest){
        return jdbcTemplate.query(
                """
                        select symbol, price, volume, created_at from ticks
                        where created_at >= ? and created_at < ? order by created_at desc limit ?
                        """,
                (rs, i) -> new Tick(
                        rs.getString("symbol"),
                        rs.getDouble("price"),
                        rs.getInt("volume"),
                        rs.getTimestamp("created_at").toInstant()
                ),
                Timestamp.from(rangeRequest.range().from()), Timestamp.from(rangeRequest.range().to()), rangeRequest.limit()
        );
    }

    public void updateByRange(TickRangeUpdate request){
        jdbcTemplate.update("""
                       update ticks 
                       set volume = volume + ?
                       where created_at >= ? and created_at < ?
       )
        """, request.delta(), Timestamp.from(request.range().from()), Timestamp.from(request.range().to()));
    }


    public void deleteByRange(TimeRange range){
        jdbcTemplate.update("""
                delete from ticks where created_at >= ? and created_at < ?
                """, Timestamp.from(range.from()), Timestamp.from(range.to()));
    }

    public void truncateByRange(LocalDate day){
        String tableName = String.format("ticks_%04d_%02d_%02d", day.getYear(), day.getMonthValue(), day.getDayOfMonth());
        if(!tableName.matches("ticks_\\d{4}_\\d{2}_\\d{2}")) throw new IllegalArgumentException("Invalid partition");

        jdbcTemplate.execute("TRUNCATE TABLE " + tableName);
    }
}
