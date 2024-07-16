package com.arbriver.arbdelta.lib.model;

import com.arbriver.arbdelta.lib.model.constants.Bookmaker;
import com.arbriver.arbdelta.lib.model.converters.ConstantConverter;
import org.springframework.data.convert.ValueConverter;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

public record Fixture(
    String event_id,
    double score,
    String text,
    Instant start_time,
    @ValueConverter(ConstantConverter.BookmakerConverter.class)
    Bookmaker book,
    String home,
    String away,
    String hyperlink
) {}
