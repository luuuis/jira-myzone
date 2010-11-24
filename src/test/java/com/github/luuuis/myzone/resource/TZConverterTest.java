package com.github.luuuis.myzone.resource;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Locale;
import java.util.TimeZone;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for TZConverter.
 */
public class TZConverterTest
{
    /**
     * Sydney time zone.
     */
    private static final TimeZone TZ_SYDNEY = TimeZone.getTimeZone("Australia/Sydney");

    /**
     * Amsterdam time zone.
     */
    private static final TimeZone TZ_AMSTERDAM = TimeZone.getTimeZone("Europe/Amsterdam");

    private final Locale userLocale = Locale.UK;
    private final DateTime timeInSydney = new DateTime(2010, 11, 23, 2, 53, 0, 0, DateTimeZone.forTimeZone(TZ_SYDNEY));
    private final DateTime timeInAmsterdam = new DateTime(2010, 11, 22, 16, 53, 0, 0, DateTimeZone.forTimeZone(TZ_AMSTERDAM));

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private JiraAuthenticationContext authContext;

    @Mock
    private I18nHelper.BeanFactory i18nFactory;

    @Mock
    private I18nHelper i18n;

    @Mock
    private DateFactory dateFactory;

    @Test
    public void testRelativeDateFormattingIssueCreatedToday() throws Exception
    {
        final DateTime nov22InAmsterdam = new DateTime(2010, 11, 22, 16, 53, 0, 0, DateTimeZone.forTimeZone(TZ_AMSTERDAM));
        prepareMocksFor(nov22InAmsterdam);
        TZConverter converter = new TZConverter(applicationProperties, authContext, i18nFactory, dateFactory);

        // now = Nov 22 in amsterdam, so Nov 22 is today
        String formatted = converter.format(new TZConverter.ParsedDate(true, timeInSydney.toDate()), TZ_AMSTERDAM, userLocale);
        assertThat(formatted, equalTo("Today 4:53 PM"));
    }

    @Test
    public void testRelativeDateFormattingIssueCreatedYesterday() throws Exception
    {
        final DateTime nov23InAmsterdam = new DateTime(2010, 11, 23, 16, 53, 0, 0, DateTimeZone.forTimeZone(TZ_AMSTERDAM));
        prepareMocksFor(nov23InAmsterdam);
        TZConverter converter = new TZConverter(applicationProperties, authContext, i18nFactory, dateFactory);

        // now = Nov 23 in amsterdam, so Nov 22 was yesterday
        String formatted = converter.format(new TZConverter.ParsedDate(true, timeInSydney.toDate()), TZ_AMSTERDAM, userLocale);
        assertThat(formatted, equalTo("Yesterday 4:53 PM"));
    }

    private void prepareMocksFor(DateTime now)
    {
        // enable relative dates, set the default formats
        when(applicationProperties.getOption(APKeys.JIRA_LF_DATE_RELATIVE)).thenReturn(true);
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_LF_DATE_COMPLETE)).thenReturn("dd/MMM/yy h:mm a");
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_LF_DATE_TIME)).thenReturn("h:mm a");
        when(i18nFactory.getInstance(userLocale)).thenReturn(i18n);
        when(i18n.getUnescapedText("common.concepts.today")).thenReturn("Today {0}");
        when(i18n.getUnescapedText("common.concepts.yesterday")).thenReturn("Yesterday {0}");

        // set the right locale
        when(authContext.getLocale()).thenReturn(userLocale);

        // set the mock dates
        when(dateFactory.newDate(DateTimeZone.forTimeZone(TZ_AMSTERDAM))).thenReturn(now);
        when(dateFactory.newDate(timeInSydney.toDate(), DateTimeZone.forTimeZone(TZ_AMSTERDAM))).thenReturn(timeInAmsterdam);
    }

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }
}
