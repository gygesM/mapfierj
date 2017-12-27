package io.ermdev.mapfierj.typeconverter;

import io.ermdev.mapfierj.core.TypeConverter;
import io.ermdev.mapfierj.core.TypeConverterAdapter;

@TypeConverter
public class LongStringConverter extends TypeConverterAdapter<Long, String> {

    @Override
    public Long convertTo(String o) {
        try {
            return Long.parseLong(o);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    @Override
    public String convertFrom(Long o) {
        return String.valueOf(o);
    }
}