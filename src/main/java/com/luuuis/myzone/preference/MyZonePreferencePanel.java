package com.luuuis.myzone.preference;

import com.atlassian.jira.plugin.profile.OptionalUserProfilePanel;
import com.atlassian.jira.plugin.profile.ViewProfilePanel;
import com.atlassian.jira.plugin.profile.ViewProfilePanelModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opensymphony.user.User;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TimeZone;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.2
 */
public class MyZonePreferencePanel implements ViewProfilePanel, OptionalUserProfilePanel
{
    /**
     * Logger for this MyZonePreferencePanel instance.
     */
    private final Logger log = LoggerFactory.getLogger(MyZonePreferencePanel.class);

    /**
     * A JiraAuthenticationContext.
     */
    private final JiraAuthenticationContext authContext;

    /**
     * An I18nHelper factory.
     */
    private final I18nHelper.BeanFactory i18nFactory;

    /**
     * A reference to the module descriptor for this panel.
     */
    private ViewProfilePanelModuleDescriptor moduleDescriptor;

    /**
     * Creates a new MyZonePreferencePanel.
     *
     * @param authContext a JiraAuthenticationContext
     * @param i18nFactory a I18nHelper.BeanFactory
     */
    public MyZonePreferencePanel(JiraAuthenticationContext authContext, I18nHelper.BeanFactory i18nFactory)
    {
        this.i18nFactory = i18nFactory;
        this.authContext = authContext;
    }

    /**
     * Initialises this module descriptor.
     *
     * @param moduleDescriptor a ViewProfilePanelModuleDescriptor
     */
    public void init(ViewProfilePanelModuleDescriptor moduleDescriptor)
    {
        this.moduleDescriptor = moduleDescriptor;
    }

    /**
     * Only display the panel when a user is looking at his own profile.
     *
     * @param profileUser the User whose profile is being viewed
     * @param callingUser the User who is viewing a profile
     * @return true if profileUser and callingUser are one and the same
     */
    public boolean showPanel(User profileUser, User callingUser)
    {
        return callingUser != null && callingUser.equals(profileUser);
    }

    /**
     * Returns the HTML to display in the preference panel.
     *
     * @param profileUser the User whose profile is being viewed
     * @return a String containing the HTML to display
     */
    public String getHtml(User profileUser)
    {
        User callingUser = authContext.getUser();

        Map<String, Object> params = Maps.newHashMap();
        params.put("callingUser", callingUser);
        params.put("profileUser", profileUser);
        params.put("stringEscapeUtils", new StringEscapeUtils());
        params.put("i18n", i18nFactory.getInstance(authContext.getLocale()));
        params.put("timezones", TimeZones.ALL);

        return moduleDescriptor.getHtml(VIEW_TEMPLATE, params);
    }

    /**
     * Initialisation on demand holder class.
     */
    static class TimeZones
    {
        static final ImmutableList<TimeZone> ALL;
        static
        {
            ArrayList<TimeZone> timeZones = Lists.newArrayList();
            for (String tzID : TimeZone.getAvailableIDs())
            {
                timeZones.add(TimeZone.getTimeZone(tzID));
            }

            Collections.sort(timeZones, new RawOffsetComparator());
            ALL = ImmutableList.copyOf(timeZones);
        }
    }

    /**
     * Compares TimeZone instances using their "raw" offset (from UTC).
     */
    private static class RawOffsetComparator implements Comparator<TimeZone>
    {
        public int compare(TimeZone tz1, TimeZone tz2)
        {
            return tz1.getRawOffset() - tz2.getRawOffset();
        }
    }
}
