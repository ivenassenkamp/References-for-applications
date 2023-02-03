package me.mypvp.base.core.utils;

import java.time.Duration;
import java.util.regex.Pattern;

public class DurationUtils {

  private static final Pattern SPLIT_PATTERN = Pattern.compile(
      "(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");

  /**
   * Parsed the given human readable string to a duration.
   * <p>
   * <b>Example:</b><br>
   * {@code "10days"} =&gt; {@code "PT240H"}<br>
   * {@code "5m"} =&gt; {@code "PT5M"}<br>
   * {@code "12hrs"} =&gt; {@code "PT12H"}<br>
   *
   * @param durationString which should be parsed.
   *
   * @return a {@link Duration} representing the {@code durationString}.
   *
   * @throws IllegalArgumentException if the specified string is not parseable.
   */
  public static Duration parseDuration(String durationString) {
    Checks.notNull(durationString, "durationString");

    String[] durationSplit = SPLIT_PATTERN.split(durationString.toLowerCase());

    long duration = Long.parseLong(durationSplit[0]);

    return switch (durationSplit[1]) {
      case "s", "seconds", "sekunden", "second", "sek", "secs", "sec" -> Duration.ofSeconds(duration);
      case "m", "minutes", "minute", "mins", "min" -> Duration.ofMinutes(duration);
      case "h", "hours", "hour", "hrs", "hr" -> Duration.ofHours(duration);
      case "d", "days", "day" -> Duration.ofDays(duration);
      case "w", "weeks", "week" -> Duration.ofDays(duration * 7);
      case "mo", "months", "month" -> Duration.ofDays(duration * 30);
      case "y", "years", "year" -> Duration.ofDays(duration * 365);
      default -> throw new IllegalArgumentException("The specified string is not parseable");
    };
  }

}
