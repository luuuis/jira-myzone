package com.luuuis.myzone.resource;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.google.common.collect.Maps;
import com.opensymphony.user.User;

import java.util.HashMap;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;

/**
 * This resource is used to get and set MyZone-related user preferences.
 *
 * @since v4.2
 */
@Path ("prefs")
public class Prefs
{
    /**
     * Key used for the selected timezone id in the user property set.
     */
    public static final String SELECTED_TZ = "myzone-selected-timezone";

    /**
     * Key used for the selected timezone id in JSON.
     */
    private static final String TZ_ID = "tzId";

    /**
     * A JiraAuthenticationContext.
     */
    private final JiraAuthenticationContext authContext;

    /**
     * Creates a new Preferences.
     *
     * @param authContext a JiraAuthenticationContext
     */
    public Prefs(JiraAuthenticationContext authContext)
    {
        this.authContext = authContext;
    }

    /**
     * Sets the user preferences.
     *
     * @param prefsMap a map containing the user's preferences
     */
    @PUT
    public void setPreferences(HashMap<String, String> prefsMap)
    {
        User user = authContext.getUser();
        if (user == null)
        {
            throw new WebApplicationException(401);
        }

        String timezoneID = prefsMap.get(TZ_ID);
        user.getPropertySet().setString(SELECTED_TZ, timezoneID);
    }

    /**
     * Gets the user preferences.
     *
     * @return the user preferences
     */
    @GET
    public HashMap<String, String> getPreferences()
    {
        User user = authContext.getUser();
        if (user == null)
        {
            throw new WebApplicationException(401);
        }

        HashMap<String, String> prefsMap = Maps.newHashMap();
        prefsMap.put(TZ_ID, user.getPropertySet().getString(SELECTED_TZ));

        return prefsMap;
    }
}
