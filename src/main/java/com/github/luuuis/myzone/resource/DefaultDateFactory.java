package com.github.luuuis.myzone.resource;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * Default date factory implementation.
 */
public class DefaultDateFactory implements DateFactory
{
    public DateTime newDate(DateTimeZone zone)
    {
        return new DateTime(zone);
    }
}
