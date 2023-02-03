package me.mypvp.base.core.utils;

import org.jetbrains.annotations.NotNull;

public class LeetUtils {

  private static final char[] charsBig = { 'A', 'B', 'C', 'D', 'E', 'F', 'G',
      'H', 'I', 'J', 'K', 'L', 'O', 'P', 'R', 'S', 'T', 'U', 'Y' };
  private static final char[] charsSmall = { 'a', 'b', 'c', 'd', 'e', 'f', 'g',
      'h', 'i', 'j', 'k', 'l', 'o', 'p', 'r', 's', 't', 'u', 'y' };
  private static final char[] leet = { '4', '8', '(', ')', '3', '}', '6', '#',
      '!', ']', 'X', '|', '0', '9', '2', 'Z', '7', 'M', 'J' };

  public static @NotNull String convertFromLeet(@NotNull String string) {
    Checks.notNull(string, "string");

    StringBuilder newString = new StringBuilder();

    for (int i = 0; i < string.length(); i++) {

      char tmp = string.charAt(i);
      boolean found = false;
      for (int j = 0; j < leet.length; j++) {
        if (tmp == leet[j]) {
          newString.append(charsSmall[j]);
          found = true;
          break;
        }
      }

      if (!found)
        newString.append(tmp);

    }
    return newString.toString();
  }

  public static @NotNull String convertToLeet(@NotNull String string) {
    Checks.notNull(string, "string");

    StringBuilder newString = new StringBuilder();

    for (int i = 0; i < string.length(); i++) {

      char tmp = string.charAt(i);
      boolean found = false;
      for (int j = 0; j < leet.length; j++) {
        if (tmp == charsBig[j] || tmp == charsSmall[j]) {
          newString.append(leet[j]);
          found = true;
          break;
        }
      }

      if (!found)
        newString.append(tmp);

    }
    return newString.toString();
  }

}
