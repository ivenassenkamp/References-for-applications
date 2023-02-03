package me.mypvp.base.core.utils;

public class CircledNumberUtils {

  private final static String[] circledNumbers = { "\u2780",
      "\u2781", "\u2782", "\u2783", "\u2784", "\u2785", "\u2786", "\u2787",
      "\u2788", "\u2789", "\u278A", "\u278B", "\u278C", "\u278D", "\u278E",
      "\u278F", "\u2790", "\u2791", "\u2792", "\u2793" };

  /**
   * Returns the specified number as an circled number
   *
   * @param number   which is to be returned as a circled number
   * @param negative indicates whether the fullness of the circled number is to be
   *                 reversed
   *
   * @return the requested circled number
   *
   * @throws IllegalArgumentException If the number is greater than 10 or less
   *                                  than 1
   */
  public static String getCircledNumber(int number, boolean negative) {
    if (number < 1 || number > 10) {
      throw new IllegalArgumentException("number must not be less than 1 or "
          + "greater than 10");
    }

    if (negative) {
      number += 10;
    }

    return circledNumbers[number - 1];
  }

}
