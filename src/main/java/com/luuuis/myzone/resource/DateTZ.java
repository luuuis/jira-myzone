package com.luuuis.myzone.resource;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A date with a timezone.
 */
@XmlRootElement
public class DateTZ
{
    @XmlElement
    private String time;

    public DateTZ()
    {
    }

    public DateTZ(String time)
    {
        this.time = time;
    }

    public String getTime()
    {
        return time;
    }

    public void setTime(String time)
    {
        this.time = time;
    }

    @Override
    public String toString()
    {
        return "DateTZ{time='" + time + '\'' + '}';
    }
}
