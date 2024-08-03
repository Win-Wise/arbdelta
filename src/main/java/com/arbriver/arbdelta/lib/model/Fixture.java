package com.arbriver.arbdelta.lib.model;

import com.arbriver.arbdelta.lib.model.constants.Bookmaker;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
public class Fixture {
    private final String eventID;
    private final double score;
    private final String text;
    private final Instant startTime;
    private final Bookmaker book;
    private final String home;
    private final String away;
    private String hyperlink;
    private List<Position> positions;
}
