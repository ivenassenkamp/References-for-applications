package me.mypvp.base.core.utils;

import java.util.TreeMap;

import org.jetbrains.annotations.NotNull;

public class RomanNumberUtils {

  private final static TreeMap<Integer, String> map = new TreeMap<>();

  static {
    map.put(1000, "M");
    map.put(900, "CM");
    map.put(500, "D");
    map.put(400, "CD");
    map.put(100, "C");
    map.put(90, "XC");
    map.put(50, "L");
    map.put(40, "XL");
    map.put(10, "X");
    map.put(9, "IX");
    map.put(5, "V");
    map.put(4, "IV");
    map.put(1, "I");
  }

  public static @NotNull String toRoman(int number) {
    Checks.greaterThan(number, "number", 0);

    int toRemove = map.floorKey(number);
    if (number == toRemove) {
      return map.get(number);
    }

    return map.get(toRemove) + toRoman(number - toRemove);
  }

}
