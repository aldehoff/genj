package genj.util;

import java.util.*;

/**
 * Utility to mangle and otherwise modify text in String
 */
public class Text {

  /**
   * Truncates a given String and appends [optional] ending
   */
  public static String truncate(String text, int length, String padding) {

    // nothing necessary?
    if (text.length()<=length) {
      return text;
    }

    // cut it
    return text.substring(0, length-padding.length()) + padding;
  }
}
