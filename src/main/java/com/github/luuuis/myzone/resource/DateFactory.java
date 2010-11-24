package com.github.luuuis.myzone.resource;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Date;

/**
 * This interface abstracts date creation functionality, to make it easier to create classes that manipulate dates and
 * can be easily unit tested.
 */
public interface DateFactory
{
    /**
     * Creates a new DateTime with the time set to the current system time, in the given time zone.
     *
     * @param zone a DateTimeZone
     * @return a new DateTime
     */
    DateTime newDate(DateTimeZone zone);

    /**
     * Creates a new DateTime with the time set to the given time, in the given time zone.
     *
     * @param date a Date
     * @param zone a DateTimeZone
     * @return a new DateTime
     */
    DateTime newDate(Date date, DateTimeZone zone);
}
