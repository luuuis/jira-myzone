package com.luuuis.myzone.resource;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.2
 */
@Path ("prefs")
public class Preferences
{
    /**
     * Sets the user preferences.
     */
    @PUT
    public void setPreferences()
    {
    }

    /**
     * Gets the user preferences.
     *
     * @return the user preferences
     */
    @GET
    public Object getPreferences()
    {
        return null;
    }
}
