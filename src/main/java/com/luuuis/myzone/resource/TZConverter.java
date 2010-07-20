package com.luuuis.myzone.resource;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static java.lang.String.format;

/**
 * This service converts dates and times between different time zones.
 */
@Consumes (MediaType.APPLICATION_JSON)
@Produces (MediaType.APPLICATION_JSON)
@Path ("convert")
public class TZConverter
{
    /**
     * Logger for this TZConverter instance.
     */
    private final Logger log = LoggerFactory.getLogger(TZConverter.class);

    /**
     * The JIRA application properties.
     */
    private final ApplicationProperties applicationProperties;

    /**
     * Creates a new TZConverter.
     *
     * @param applicationProperties an ApplicationProperties
     */
    public TZConverter(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    @POST
    public DateTZ convert(DateTZ dateString)
    {
        log.debug("Received date: {}", dateString);
        try
        {
            DateFormat parser = new SimpleDateFormat(getDateFormatString());
            parser.setTimeZone(TimeZone.getDefault());

            // TODO Use 'dst'
            TimeZone timeZone = TimeZone.getTimeZone(format("GMT%+d", dateString.getOffset()));
            DateFormat formatter = new SimpleDateFormat(getDateFormatString());
            formatter.setTimeZone(timeZone);

            // do the conversion
            Date date = parser.parse(dateString.getTime());
            String prettyDate = String.format("%s (%s)", formatter.format(date), timeZone.getDisplayName());

            return new DateTZ(dateString.getOffset(), dateString.getDst(), prettyDate);
        }
        catch (ParseException e)
        {
            log.error("Unable to convert date: {}", dateString);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }

    /**
     * Returns the date format string used by JIRA.
     *
     * @return a String specifying the date format used by JIRA
     */
    protected String getDateFormatString()
    {
        return applicationProperties.getDefaultBackedString(APKeys.JIRA_LF_DATE_COMPLETE);
    }
}
