package com.luuuis.myzone.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * This service converts dates and times between different time zones.
 */
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("convert")
public class TZConverter
{
    /**
     * Logger for this TZConverter instance.
     */
    private final Logger log = LoggerFactory.getLogger(TZConverter.class);

    @POST
    public String convert(String dateString)
    {
        log.warn("Received date: {}", dateString);
        return dateString;
    }
}
