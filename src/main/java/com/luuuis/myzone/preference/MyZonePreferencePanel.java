package com.luuuis.myzone.preference;

import com.atlassian.jira.plugin.profile.OptionalUserProfilePanel;
import com.atlassian.jira.plugin.profile.ViewProfilePanel;
import com.atlassian.jira.plugin.profile.ViewProfilePanelModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opensymphony.user.User;
import org.apache.commons.lang.StringEscapeUtils;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
        Locale locale = authContext.getLocale();
        User callingUser = authContext.getUser();

        Map<String, Object> params = Maps.newHashMap();
        params.put("locale", locale);
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
        static final ImmutableList<TimeZoneInfo> ALL;
        static
        {
            // eliminate duplicate timezones
            Set<DateTimeZone> uniqueTimeZones = Sets.newHashSet();
            for (Object tzID : DateTimeZone.getAvailableIDs())
            {
                uniqueTimeZones.add(DateTimeZone.forID((String) tzID));
            }

            // sort before setting value
            List<TimeZoneInfo> timeZoneInfos = Lists.newArrayList();
            for (DateTimeZone timeZone : uniqueTimeZones)
            {
                timeZoneInfos.add(TimeZoneInfo.from(timeZone));
            }
            Collections.sort(timeZoneInfos);

            ALL = ImmutableList.copyOf(timeZoneInfos);
        }
    }
}
