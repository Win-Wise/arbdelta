package com.arbriver.arbdelta.lib.model;

import com.arbriver.arbdelta.lib.model.constants.Sport;
import com.arbriver.arbdelta.lib.model.converters.ConstantConverter;
import org.springframework.data.annotation.Id;
import org.springframework.data.convert.ValueConverter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "matches")
public record Match(
    @Id
    String _id,
    String home,
    String away,
    @ValueConverter(ConstantConverter.SportConverter.class)
    Sport sport,
    Instant start_time,
    String text,
    List<Fixture> links
) {}
