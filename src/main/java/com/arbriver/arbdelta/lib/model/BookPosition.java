package com.arbriver.arbdelta.lib.model;

import com.arbriver.arbdelta.lib.model.constants.Bookmaker;
import com.arbriver.arbdelta.lib.model.converters.ConstantConverter;
import org.springframework.data.convert.ValueConverter;

public record BookPosition(
        @ValueConverter(ConstantConverter.BookmakerConverter.class)
        Bookmaker bookmaker,
        Position position) { }
