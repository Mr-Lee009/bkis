package vn.edu.bkis.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BkisNumberUtils {

  /**
   * Returns a default price value. If the input price is null, it returns BigDecimal.ZERO.
   * Otherwise, it returns the price rounded to 2 decimal places using HALF_UP rounding mode.
   *
   * @param price the input price to be processed
   * @return a non-null BigDecimal representing the price, with a default of BigDecimal.ZERO if input is null
   */
  public static BigDecimal defaultPrice(BigDecimal price) {
    return price == null ? BigDecimal.ZERO : price.setScale(2, RoundingMode.HALF_UP);
  }

  /**
   * Returns a default integer value. If the input value is null, it returns the specified fallback value.
   * Otherwise, it returns the input value.
   * @param value the input Integer to be processed
   * @param fallback the default value to return if the input value is null
   * @return a non-null Integer representing the input value or the fallback if input is null
   */
  public static Integer defaultInteger(Integer value, int fallback) {
    return value == null ? fallback : value;
  }
}
