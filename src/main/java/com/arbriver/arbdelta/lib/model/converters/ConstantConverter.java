package com.arbriver.arbdelta.lib.model.converters;

import com.arbriver.arbdelta.lib.model.constants.Bookmaker;
import com.arbriver.arbdelta.lib.model.constants.Sport;
import lombok.NonNull;
import org.springframework.data.convert.PropertyValueConverter;
import org.springframework.data.convert.ValueConversionContext;

public class ConstantConverter {
    public static class SportConverter implements PropertyValueConverter<Sport, String, ValueConversionContext<?>> {
        @Override
        public Sport read(@NonNull String value, @NonNull ValueConversionContext context) {
            return Sport.valueOf(value);
        }

        @Override
        public String write(Sport value, @NonNull ValueConversionContext context) {
            return value.toString();
        }
    }

    public static class BookmakerConverter implements PropertyValueConverter<Bookmaker, String, ValueConversionContext<?>> {
        @Override
        public Bookmaker read(@NonNull String value, @NonNull ValueConversionContext context) {
            return Bookmaker.valueOf(value);
        }

        @Override
        public String write(Bookmaker value, @NonNull ValueConversionContext context) {
            return value.toString();
        }
    }
}
