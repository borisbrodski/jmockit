/*
 * JMockit Coverage
 * Copyright (c) 2006-2009 Rogério Liesenfeld
 * All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package mockit.coverage;

public final class CoveragePercentage
{
   public static int calculate(int coveredCount, int totalCount)
   {
      if (totalCount <= 0) {
         return -1;
      }

      return (int) (100.0 * coveredCount / totalCount + 0.5);
   }

   public static String percentageColor(int percentage)
   {
      if (percentage == 0) {
         return "ff0000";
      }
      else if (percentage == 100) {
         return "00ff00";
      }
      else {
         int green = 0xFF * percentage / 100;
         int red = 0xFF - green;

         StringBuilder color = new StringBuilder(6);
         appendColorInHexadecimal(color, red);
         appendColorInHexadecimal(color, green);
         color.append("00");

         return color.toString();
      }
   }

   private static void appendColorInHexadecimal(StringBuilder colorInHexa, int rgb)
   {
      String hex = Integer.toHexString(rgb);

      if (hex.length() == 1) {
         colorInHexa.append('0');
      }

      colorInHexa.append(hex);
   }
}
