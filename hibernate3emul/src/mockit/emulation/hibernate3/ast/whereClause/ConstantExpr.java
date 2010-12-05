/*
 * JMockit Hibernate 3 Emulation
 * Copyright (c) 2006-2010 Rogério Liesenfeld
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
package mockit.emulation.hibernate3.ast.whereClause;

import java.util.*;

import mockit.emulation.hibernate3.ast.*;

final class ConstantExpr extends PrimaryExpr
{
   final Object value;

   ConstantExpr(Object value)
   {
      this.value = value;
   }

   @SuppressWarnings({"UnusedCatchParameter"})
   public static ConstantExpr parse(Tokens tokens)
   {
      String strValue = tokens.next();
      Object value;

      try {
         value = Integer.valueOf(strValue);
      }
      catch (NumberFormatException ignore1) {
         try {
            value = Long.valueOf(strValue);
         }
         catch (NumberFormatException ignore2) {
            try {
               value = Double.valueOf(strValue);
            }
            catch (NumberFormatException ignore3) {
               if ("true".equalsIgnoreCase(strValue)) {
                  value = true;
               }
               else if ("false".equalsIgnoreCase(strValue)) {
                  value = false;
               }
               else if ("null".equalsIgnoreCase(strValue)) {
                  value = null;
               }
               else if ("empty".equalsIgnoreCase(strValue)) {
                  value = Collections.EMPTY_SET;
               }
               else if (
                  strValue.charAt(0) == '\'' && strValue.charAt(strValue.length() - 1) == '\'') {
                  value = strValue.substring(1, strValue.length() - 1);
               }
               else {
                  return null;
               }
            }
         }
      }

      return new ConstantExpr(value);
   }

   @Override
   public Object evaluate(QueryEval eval)
   {
      return value;
   }
}
