package com.arbriver.arbdelta.lib.model.dbmodel;

import com.arbriver.arbdelta.lib.model.constants.Sport;
import com.arbriver.arbdelta.lib.model.converters.ConstantConverter;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.convert.ValueConverter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Document(collection = "matches")
public class MatchDTO {
    @Id
    private final String _id;
    private final String home;
    private final String away;
    @ValueConverter(ConstantConverter.SportConverter.class)
    private final Sport sport;
    private final Instant start_time;
    private final String text;
    private final List<FixtureDTO> links;
}
