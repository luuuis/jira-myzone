package com.luuuis.myzone.resource;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A date with a timezone.
 */
@XmlRootElement
public class DateTZ
{
    @XmlAttribute
    private Integer offset;

    @XmlAttribute
    private String dst;

    @XmlElement
    private String time;

    public DateTZ()
    {
    }

    public DateTZ(Integer offset, String dst, String time)
    {
        this.offset = offset;
        this.dst = dst;
        this.time = time;
    }

    public Integer getOffset()
    {
        return offset;
    }

    public void setOffset(Integer offset)
    {
        this.offset = offset;
    }

    public String getDst()
    {
        return dst;
    }

    public void setDst(String dst)
    {
        this.dst = dst;
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
        return "DateTZ{" +
                "offset=" + offset +
                ", dst='" + dst + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
