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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
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
    private final Logger log = LoggerFactory.getLogger(TZConverter.class);

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
        log.debug("Received date: {}", request);
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

            // do the conversion
            Date dateInJiraTZ = parse(request.getTime());
            String dateInUserTZ = format(dateInJiraTZ, userTZ);

            // return a date string w/ TZ info
            return new DateTZ(String.format("%s %s", dateInUserTZ, userTZ.getDisplayName(true, TimeZone.SHORT)));
        }
        catch (ParseException e)
        {
            log.debug("Unable to convert date: {}", request);
            return NULL_DATE_TZ;
        }
    }

    private String format(Date dateInJiraTZ, TimeZone userTZ) {
        SimpleDateFormat userDateFormat = createDateFormat(getCompleteDateFormatString(), userTZ);

        return userDateFormat.format(dateInJiraTZ);
    }

    private Date parse(String timeString) throws ParseException {
        TimeZone serverTZ = TimeZone.getDefault();

        SimpleDateFormat completeFmt = createDateFormat(getCompleteDateFormatString(), serverTZ);
        try {
            return completeFmt.parse(timeString);
        }
        catch (ParseException e1) {
            I18nHelper i18n = i18nFactory.getInstance(authContext.getLocale());

            // ok, maybe yesterday?
            try {
                String yesterdayFmt = i18n.getUnescapedText("common.concepts.yesterday");
                Object[] timeYesterday = new MessageFormat(yesterdayFmt).parse(timeString);
                log.debug("Time yesterday: {}", Arrays.toString(timeYesterday));

                Date hourYesterday = createDateFormat(getTimeFormatString(), serverTZ).parse((String) timeYesterday[0]);

                Calendar hoursCal = getInstance(serverTZ);
                hoursCal.setTime(hourYesterday);

                Calendar dateCal = getInstance(serverTZ);
                dateCal.setTime(new Date());
                dateCal.set(HOUR_OF_DAY, hoursCal.get(HOUR_OF_DAY));
                dateCal.set(MINUTE, hoursCal.get(MINUTE));
                dateCal.set(SECOND, hoursCal.get(SECOND));
                dateCal.add(DAY_OF_YEAR, -1);

                return dateCal.getTime();
            }
            catch (ParseException e3)
            {
                // surely today!
                String todayFmt = i18n.getUnescapedText("common.concepts.today");
                Object[] timeToday = new MessageFormat(todayFmt).parse(timeString);
                log.debug("Time today: {}", Arrays.toString(timeToday));

                Date hoursToday = createDateFormat(getTimeFormatString(), serverTZ).parse((String) timeToday[0]);

                Calendar hoursCal = getInstance(serverTZ);
                hoursCal.setTime(hoursToday);

                Calendar dateCal = getInstance(serverTZ);
                dateCal.setTime(new Date());
                dateCal.set(HOUR_OF_DAY, hoursCal.get(HOUR_OF_DAY));
                dateCal.set(MINUTE, hoursCal.get(MINUTE));
                dateCal.set(SECOND, hoursCal.get(SECOND));

                return dateCal.getTime();
            }
        }
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
     * @return a SimpleDateFormat
     */
    protected SimpleDateFormat createDateFormat(String formatString, TimeZone timeZone) {
        SimpleDateFormat result = new SimpleDateFormat(formatString);
        result.setTimeZone(timeZone);

        return result;
    }
}
