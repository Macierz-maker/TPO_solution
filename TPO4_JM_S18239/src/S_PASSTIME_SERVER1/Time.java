/**
 *
 *  @author Jaworski Maciej S18239
 *
 */

package S_PASSTIME_SERVER1;


import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Time {
    private static final Locale LOCALE = Locale.getDefault();
    private static final DecimalFormat DECIMAL_FORMATTER = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.ENGLISH));
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Warsaw");

    public static String passed(String from, String to) {
        StringBuilder builder = new StringBuilder();
        try {
            boolean fromHasTime = from.contains("T");
            boolean toHasTime = to.contains("T");
            ZonedDateTime fromDate = fromHasTime ? LocalDateTime.parse(from).atZone(ZONE_ID) : LocalDate.parse(from).atStartOfDay(ZONE_ID);
            ZonedDateTime toDate = toHasTime ? LocalDateTime.parse(to).atZone(ZONE_ID) : LocalDate.parse(to).atStartOfDay(ZONE_ID);
            builder.append(
                    String.format("Od %s do %s",
                            translateToPolish(fromDate, fromHasTime),
                            translateToPolish(toDate, toHasTime))
            );
            builder.append(getDateDifference(fromDate, toDate));
            builder.append(getTimeDifference(fromDate, toDate, toHasTime));
            builder.append(getCalendarDifference(fromDate, toDate));
            return builder.toString();
        } catch (DateTimeParseException e) {
            return String.format("*** %s: %s", e.getClass().getCanonicalName(), e.getMessage());
        }
    }


    private static String translateToPolish(ZonedDateTime dateTime, boolean hasTime) {
        String month = dateTime.getMonth().getDisplayName(TextStyle.FULL, LOCALE);
        String dayOfWeek = dateTime.getDayOfWeek().getDisplayName(TextStyle.FULL, LOCALE);
        String time = DATE_FORMATTER.format(dateTime);
        return hasTime ? String.format("%d %s %d (%s) godz. %s", dateTime.getDayOfMonth(), month, dateTime.getYear(), dayOfWeek, time) :
                String.format("%d %s %d (%s)", dateTime.getDayOfMonth(), month, dateTime.getYear(), dayOfWeek);
        // d - decimal, s - object (np.String)
    }

    private static String getDateDifference(ZonedDateTime fromDate, ZonedDateTime toDate) {
        int daysDifference = (int) Math.ceil(ChronoUnit.HALF_DAYS.between(fromDate, toDate) / 2.0);
        String weeksDifference = DECIMAL_FORMATTER.format(daysDifference / 7.0);
        return String.format("\n - mija: %d %s, tygodni %s", daysDifference, getDaysSuffix(daysDifference), weeksDifference);
    }

    private static String getTimeDifference(ZonedDateTime fromDate, ZonedDateTime toDate, boolean hasTime) {
        long hourDifference = ChronoUnit.HOURS.between(fromDate, toDate);
        long minuteDifference = ChronoUnit.MINUTES.between(fromDate, toDate);
        return hasTime ? String.format("\n - godzin: %d, minut: %d", hourDifference, minuteDifference) : "";
    }

    private static String getCalendarDifference(ZonedDateTime fromDate, ZonedDateTime toDate) {
        Period period = Period.between(fromDate.toLocalDate(), toDate.toLocalDate());
        String years = period.getYears() != 0 ? String.valueOf(period.getYears()) + " " + getYearSuffix(period.getYears()): "";
        String months = period.getMonths() != 0 ? String.valueOf(period.getMonths()) + " " + getMonthsSuffix(period.getMonths()): "";
        String days = period.getDays() != 0 ? String.valueOf(period.getDays()) + " " + getDaysSuffix(period.getDays()): "";
        String calendarDifferenceString = Stream.of(years, months, days)
                .filter(string -> !string.equals(""))
                .collect(Collectors.joining(", "));
        if (period.getDays() > 0 ||  period.getMonths() > 0 ||  period.getYears() > 0)
            return String.format("\n - kalendarzowo: %s", calendarDifferenceString);
        return "";
    }

    private static String getYearSuffix(int yearCount) {
        if (yearCount == 1)
            return "rok";
        else if (yearCount > 1 && yearCount <= 4)
            return "lata";
        else
            return "lat";
    }

    private static String getMonthsSuffix(int monthsCount) {
        if (monthsCount == 1)
            return "miesiąc";
        else if (monthsCount > 1 && monthsCount <= 4)
            return "miesiące";
        else
            return "miesięcy";
    }

    private static String getDaysSuffix(int daysCount) {
        if (daysCount == 1)
            return "dzień";
        else
            return "dni";
    }
}
