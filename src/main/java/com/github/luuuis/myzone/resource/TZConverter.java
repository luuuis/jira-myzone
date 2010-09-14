/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.github.luuuis.myzone.resource;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.I18nBean;
import com.opensymphony.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static java.util.Calendar.*;

/**
 * This service converts dates and times between different time zones.
 */
@Consumes (MediaType.APPLICATION_JSON)
@Produces (MediaType.APPLICATION_JSON)
@Path ("convert")
public class TZConverter
{
    /**
     * A DateTZ instance with no conversion performed.
     */
    private static final DateTZ NULL_DATE_TZ = new DateTZ("");

    /**
     * Logger for this TZConverter instance.
     */
    private final Logger logger = LoggerFactory.getLogger(TZConverter.class);

    /**
     * A JiraAuthenticationContext.
     */
    private final JiraAuthenticationContext authContext;

    /**
     * The JIRA application properties.
     */
    private final ApplicationProperties applicationProperties;

    /**
     * The I18nBean factory.
     */
    private final I18nBean.BeanFactory i18nFactory;

    /**
     * Creates a new TZConverter.
     *
     * @param applicationProperties an ApplicationProperties
     * @param authContext a JiraAuthenticationContext
     * @param i18nFactory an I18nBean factory
     */
    public TZConverter(ApplicationProperties applicationProperties, JiraAuthenticationContext authContext, I18nHelper.BeanFactory i18nFactory)
    {
        this.applicationProperties = applicationProperties;
        this.authContext = authContext;
        this.i18nFactory = i18nFactory;
    }

    @POST
    public DateTZ convert(DateTZ request)
    {
        logger.debug("Received date: {}", request);
        try
        {
            User user = authContext.getUser();
            if (user == null)
            {
                throw new WebApplicationException(401);
            }

            String selectedTZ = user.getPropertySet().getString(Prefs.SELECTED_TZ);
            if (selectedTZ == null)
            {
                return NULL_DATE_TZ;
            }

            TimeZone userTZ = TimeZone.getTimeZone(selectedTZ);
            Locale userLocale = authContext.getLocale();

            // do the conversion
            Date dateInJiraTZ = parse(request.getTime(), userLocale);
            String dateInUserTZ = format(dateInJiraTZ, userTZ, userLocale);

            // return a date string w/ TZ info
            return new DateTZ(String.format("%s %s", dateInUserTZ, userTZ.getDisplayName(true, TimeZone.SHORT)));
        }
        catch (ParseException e)
        {
            logger.debug("Unable to convert date: {}", request);
            return NULL_DATE_TZ;
        }
    }

    private String format(Date dateInJiraTZ, TimeZone userTZ, Locale locale) {
        SimpleDateFormat userDateFormat = createDateFormat(getCompleteDateFormatString(), userTZ, locale);

        return userDateFormat.format(dateInJiraTZ);
    }

    private Date parse(String timeString, Locale locale) throws ParseException {
        TimeZone serverTZ = TimeZone.getDefault();

        String completeFormatString = getCompleteDateFormatString();
        try {
            Date date = createDateFormat(completeFormatString, serverTZ, locale).parse(timeString);
            logger.debug("Parsed using format '{}': {}", getCompleteDateFormatString(), timeString);
            return date;
        }
        catch (ParseException e1) {
            logger.debug("Failed to parse using format '{}': {}", getCompleteDateFormatString(), timeString);

            I18nHelper i18n = i18nFactory.getInstance(locale);

            // a date in the last week?
            String dayFormatString = getDayFormatString();
            try {
                Calendar timeAndDayOfWeek = Calendar.getInstance(serverTZ);
                timeAndDayOfWeek.setTime(createDateFormat(dayFormatString, serverTZ, locale).parse(timeString));

                Calendar now = calendarNow(serverTZ);

                // copy the time of day
                copy(HOUR_OF_DAY, timeAndDayOfWeek, now);
                copy(MINUTE, timeAndDayOfWeek, now);
                copy(SECOND, timeAndDayOfWeek, now);

                // adjust the day
                int dowToday = now.get(DAY_OF_WEEK);
                int dowIssue = timeAndDayOfWeek.get(DAY_OF_WEEK);
                int daysAgo = dowIssue < dowToday ? (dowToday - dowIssue) : (7 - (dowIssue - dowToday));
                now.add(DAY_OF_YEAR, -1*daysAgo);
                
                logger.debug("Parsed with format '{}': {}", dayFormatString, timeString);
                return now.getTime();
            }
            catch (ParseException e2)
            {
                logger.debug("Failed to parse with format '{}': {}", dayFormatString, timeString);

                // ok, maybe yesterday?
                String yesterdayFmt = i18n.getUnescapedText("common.concepts.yesterday");
                try {
                    Object[] timeYesterday = new MessageFormat(yesterdayFmt).parse(timeString);

                    Date hourYesterday = createDateFormat(getTimeFormatString(), serverTZ, locale).parse((String) timeYesterday[0]);

                    Calendar timeYesterdayCal = getInstance(serverTZ);
                    timeYesterdayCal.setTime(hourYesterday);

                    Calendar dateCal = calendarNow(serverTZ);
                    copy(HOUR_OF_DAY, timeYesterdayCal, dateCal);
                    copy(MINUTE, timeYesterdayCal, dateCal);
                    copy(SECOND, timeYesterdayCal, dateCal);
                    dateCal.add(DAY_OF_YEAR, -1);

                    logger.debug("Parsed with format '{}': {}", yesterdayFmt, timeString);
                    return dateCal.getTime();
                }
                catch (ParseException e3)
                {
                    logger.debug("Failed to parse with format '{}': {}", yesterdayFmt, timeString);

                    // surely today!
                    String todayFmt = i18n.getUnescapedText("common.concepts.today");
                    logger.debug("Attempting to parse with format '{}': {}", todayFmt, timeString);
                    Object[] timeToday = new MessageFormat(todayFmt).parse(timeString);

                    Date hoursToday = createDateFormat(getTimeFormatString(), serverTZ, locale).parse((String) timeToday[0]);

                    Calendar timeTodayCal = getInstance(serverTZ);
                    timeTodayCal.setTime(hoursToday);

                    Calendar dateCal = calendarNow(serverTZ);
                    copy(HOUR_OF_DAY, timeTodayCal, dateCal);
                    copy(MINUTE, timeTodayCal, dateCal);
                    copy(SECOND, timeTodayCal, dateCal);

                    logger.debug("Parsed with format '{}': {}", todayFmt, timeString);
                    return dateCal.getTime();
                }
            }
        }
    }

    /**
     * Copies the value of the field from one Calendar instance to another.
     *
     * @see java.util.Calendar#set(int, int)
     * @see java.util.Calendar#get(int)
     *
     * @param field the field to copy
     * @param from the Calendar instance to copy from
     * @param to the Calendar instance to copy to
     */
    protected void copy(int field, Calendar from, Calendar to) {
        to.set(field, from.get(field));
    }

    /**
     * Returns the complete date format.
     *
     * @return a String containing the complete date format
     */
    protected String getCompleteDateFormatString()
    {
        return applicationProperties.getDefaultBackedString(APKeys.JIRA_LF_DATE_COMPLETE);
    }

    /**
     * Returns the day date format.
     *
     * @return a String containing the day date format
     */
    protected String getDayFormatString()
    {
        return applicationProperties.getDefaultBackedString(APKeys.JIRA_LF_DATE_DAY);
    }

    /**
     * Returns the time date format.
     *
     * @return a String containing the time date format
     */
    protected String getTimeFormatString()
    {
        return applicationProperties.getDefaultBackedString(APKeys.JIRA_LF_DATE_TIME);
    }

    /**
     * Creates a new SimpleDateFormat.
     *
     * @param formatString a format string
     * @param timeZone a TimeZone
     * @param locale the user's Locale
     * @return a SimpleDateFormat
     */
    static protected SimpleDateFormat createDateFormat(String formatString, TimeZone timeZone, Locale locale) {
        SimpleDateFormat result = new SimpleDateFormat(formatString, locale);
        result.setTimeZone(timeZone);

        return result;
    }

    /**
     * Returns a new Calendar with the given TimeZone, and the date initialised to now.
     *
     * @param tz a TimeZone to use
     * @return a Calendar
     */
    static protected Calendar calendarNow(TimeZone tz) {
        Calendar result = Calendar.getInstance(tz);
        result.setTimeInMillis(System.currentTimeMillis());

        return result;
    }
}
