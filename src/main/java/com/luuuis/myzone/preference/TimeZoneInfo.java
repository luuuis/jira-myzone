package com.luuuis.myzone.preference;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringEscapeUtils;
import org.joda.time.DateTimeZone;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.join;
import static org.apache.commons.lang.StringUtils.split;

/**
 * A wrapper for a {@link org.joda.time.DateTimeZone} instance.
 */
public class TimeZoneInfo implements Comparable<TimeZoneInfo>
{
    /**
     * The separator used in ids.
     */
    private static final String ID_SEPARATOR = "/";

    /**
     * The separator used in display names.
     */
    private static final String DISPLAY_SEPARATOR = " - ";

    /**
     * Character sequence to strip from display name.
     */
    private static final String DISPLAY_STRIP = "_";

    /**
     * Returns a new TimeZoneInfo instance that wraps the given DateTimeZone.
     *
     * @param timezone a DateTimeZone
     * @return a TimeZoneInfo
     */
    public static TimeZoneInfo from(DateTimeZone timezone)
    {
        if (timezone == null) { throw new NullPointerException("timezone"); }
        return new TimeZoneInfo(timezone);
    }

    /**
     * The TimeZone's id.
     */
    private final String id;

    /**
     * The TimeZone.
     */
    private final DateTimeZone timeZone;

    /**
     * Creates a new TimeZoneInfo.
     *
     * @param timezone a TimeZone
     */
    private TimeZoneInfo(DateTimeZone timezone)
    {
        this.id = timezone.getID();
        this.timeZone = timezone;
    }

    public String getId()
    {
        return id;
    }

    public String getIdHtml()
    {
        return StringEscapeUtils.escapeHtml(getId());
    }

    public String getDisplayName()
    {
        return format("(%s) %s", getDisplayOffset(), getDisplayId());
    }

    public String getDisplayNameHtml()
    {
        return StringEscapeUtils.escapeHtml(getDisplayName());
    }

    public int compareTo(TimeZoneInfo that)
    {
        long now = System.currentTimeMillis();
        int offsetDifference = timeZone.getOffset(now) - that.timeZone.getOffset(System.currentTimeMillis());
        if (offsetDifference == 0)
        {
            return getDisplayId().compareTo(that.getDisplayId());
        }

        return offsetDifference;
    }

    @Override
    public String toString()
    {
        return "TimeZoneInfo{id='" + id + '\'' + '}';
    }

    private String getDisplayId()
    {
        // return city name before country/continent
        List<String> tokens = Lists.newArrayList(split(timeZone.getID(), ID_SEPARATOR));
        Collections.reverse(tokens);

        return join(tokens, DISPLAY_SEPARATOR).replace(DISPLAY_STRIP, " ");
    }

    private String getDisplayOffset()
    {
        long now = System.currentTimeMillis();
        long offset = TimeUnit.SECONDS.convert(timeZone.getOffset(now), TimeUnit.MILLISECONDS);

        long minutes = (offset % 3600) / 60;
        long hours = offset / 3600;

        return format("GMT%+03d:%02d", hours, minutes < 0 ? minutes * -1 : minutes);
    }
}
