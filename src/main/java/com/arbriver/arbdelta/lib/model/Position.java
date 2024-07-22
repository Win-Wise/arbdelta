package com.arbriver.arbdelta.lib.model;

import lombok.Data;

@Data
public class Position {
    private final String bet_type;
    private final double odds;
    private final String value;
    private boolean lay;
    private double volume;
}
