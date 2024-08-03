package com.arbriver.arbdelta.lib.model;

import com.arbriver.arbdelta.lib.model.constants.Bookmaker;
import com.arbriver.arbdelta.lib.model.constants.Sport;
import com.arbriver.arbdelta.lib.model.dbmodel.FixtureDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.HashMap;

@Getter
@Setter
@Builder
public class Match {
    private final String id;
    private final String home;
    private final String away;
    private final Sport sport;
    private final Instant startTime;
    private final String text;
    private HashMap<Bookmaker, Fixture> links;
}
