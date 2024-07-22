package com.arbriver.arbdelta.lib.model;

import com.arbriver.arbdelta.lib.model.constants.Bookmaker;
import com.arbriver.arbdelta.lib.model.converters.ConstantConverter;
import lombok.Data;
import org.springframework.data.annotation.Transient;
import org.springframework.data.convert.ValueConverter;

import java.time.Instant;
import java.util.List;

@Data
public class Fixture {
    private final String event_id;
    private final double score;
    private final String text;
    private final Instant start_time;
    @ValueConverter(ConstantConverter.BookmakerConverter.class)
    private final Bookmaker book;
    private final String home;
    private final String away;
    private String hyperlink;
    @Transient
    private List<Position> positions;
}
