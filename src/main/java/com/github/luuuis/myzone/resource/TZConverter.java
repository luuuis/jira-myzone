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
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import static java.util.Calendar.DAY_OF_WEEK;
import static java.util.Calendar.DAY_OF_YEAR;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.SECOND;
import static java.util.Calendar.getInstance;
import static org.apache.commons.lang.StringUtils.isEmpty;

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
    public ConvertDTO convert(ConvertDTO request)
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
            if (isEmpty(selectedTZ))
            {
                return newDTO(null);
            }

            TimeZone userTZ = TimeZone.getTimeZone(selectedTZ);
            Locale userLocale = authContext.getLocale();

            // do the conversion
            ParsedDate dateInJiraTZ = parse(request.getTime(), userLocale);
            String dateInUserTZ = format(dateInJiraTZ, userTZ, userLocale);

            // return a date string w/ TZ info
            return newDTO(String.format("%s %s", dateInUserTZ, userTZ.getDisplayName(true, TimeZone.SHORT)));
        }
        catch (ParseException e)
        {
            logger.debug("Unable to convert date: {}", request);
            return newDTO(null);
        }
    }

    /**
     * Creates a new ConvertDTO with the given date string. If the date string is null, it is converted into "".
     *
     * @param dateString a String containing a date
     * @return a ConvertDTO
     */
    private ConvertDTO newDTO(String dateString) {
        I18nHelper i18n = i18nFactory.getInstance(authContext.getLocale());

        return new ConvertDTO(i18n.getText("myzone.popup.label"), dateString != null ? dateString : "");
    }

    private String format(ParsedDate parsedDate, TimeZone userTZ, Locale locale)
    {
        if (applicationProperties.getOption(APKeys.JIRA_LF_DATE_RELATIVE) && parsedDate.isRelative)
        {
            DateTime dateTime = new DateTime(parsedDate.date, DateTimeZone.forTimeZone(userTZ));
            DateTime nowInUserTZ = new DateTime(DateTimeZone.forTimeZone(userTZ));

            int days = Days.daysBetween(dateTime, nowInUserTZ).getDays();

            // today
            if (days < 1)
            {
                String todayFmt = getTodayFormatString();
                SimpleDateFormat dateFormat = createDateFormat(getTimeFormatString(), userTZ, locale);

                return new MessageFormat(todayFmt).format(new Object[] {dateFormat.format(dateTime.toDate())});
            }

            // yesterday
            if (days < 2)
            {
                String yesterdayFmt = getYesterdayFormatString();
                SimpleDateFormat dateFormat = createDateFormat(getTimeFormatString(), userTZ, locale);

                return new MessageFormat(yesterdayFmt).format(new Object[] {dateFormat.format(dateTime.toDate())});
            }

            // last week
            if (days < 7)
            {
                SimpleDateFormat dateFormat = createDateFormat(getDayFormatString(), userTZ, locale);

                return dateFormat.format(dateTime.toDate());
            }
        }

        // complete format
        return createDateFormat(getCompleteDateFormatString(), userTZ, locale).format(parsedDate.date);
    }

    private ParsedDate parse(String timeString, Locale locale) throws ParseException
    {
        TimeZone serverTZ = TimeZone.getDefault();

        String completeFormatString = getCompleteDateFormatString();
        try
        {
            Date date = createDateFormat(completeFormatString, serverTZ, locale).parse(timeString);
            logger.debug("Parsed using format '{}': {}", getCompleteDateFormatString(), timeString);

            return new ParsedDate(false, date);
        }
        catch (ParseException e1)
        {
            logger.debug("Failed to parse using format '{}': {}", getCompleteDateFormatString(), timeString);

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
                return new ParsedDate(true, now.getTime());
            }
            catch (ParseException e2)
            {
                logger.debug("Failed to parse with format '{}': {}", dayFormatString, timeString);

                // ok, maybe yesterday?
                String yesterdayFmt = getYesterdayFormatString();
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
                    return new ParsedDate(true, dateCal.getTime());
                }
                catch (ParseException e3)
                {
                    logger.debug("Failed to parse with format '{}': {}", yesterdayFmt, timeString);

                    // surely today!
                    String todayFmt = getTodayFormatString();
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
                    return new ParsedDate(true, dateCal.getTime());
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
    protected void copy(int field, Calendar from, Calendar to)
    {
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
     * Returns the format string used for "today" relative dates.
     *
     * @return the format string used for "today" relative dates
     */
    protected String getTodayFormatString()
    {
        I18nHelper i18n = i18nFactory.getInstance(authContext.getLocale());
        return i18n.getUnescapedText("common.concepts.today");
    }

    /**
     * Returns the format string used for "yesterday" relative dates.
     *
     * @return the format string used for "yesterday" relative dates
     */
    protected String getYesterdayFormatString()
    {
        I18nHelper i18n = i18nFactory.getInstance(authContext.getLocale());
        return i18n.getUnescapedText("common.concepts.yesterday");
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
    static protected SimpleDateFormat createDateFormat(String formatString, TimeZone timeZone, Locale locale)
    {
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
    static protected Calendar calendarNow(TimeZone tz)
    {
        Calendar result = Calendar.getInstance(tz);
        result.setTimeInMillis(System.currentTimeMillis());

        return result;
    }

    /**
     * This struct represents a date that was parsed from a String.
     */
    static class ParsedDate
    {
        final boolean isRelative;
        final Date date;

        ParsedDate(boolean relative, Date date) {
            isRelative = relative;
            this.date = date;
        }
    }
}
