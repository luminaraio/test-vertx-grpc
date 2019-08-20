package io.luminara.commons.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;

public class UuidConverter extends ClassicConverter {

    private TimeBasedGenerator generator = Generators.timeBasedGenerator();

    @Override
    public String convert(ILoggingEvent iLoggingEvent) {
        return generator
                .generate()
                .toString();
    }
}
