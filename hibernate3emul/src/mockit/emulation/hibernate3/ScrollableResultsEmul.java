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
package mockit.emulation.hibernate3;

import java.math.*;
import java.sql.*;
import java.util.*;
import java.util.Date;

import org.hibernate.*;
import org.hibernate.type.*;

@SuppressWarnings({"ClassWithTooManyMethods"})
final class ScrollableResultsEmul implements ScrollableResults
{
   private final List<?> results;
   private int pos;

   ScrollableResultsEmul(List<?> results)
   {
      this.results = results;
      pos = -1;
   }

   public boolean next()
   {
      if (pos >= results.size()) {
         return false;
      }

      pos++;
      return true;
   }

   public boolean previous()
   {
      if (pos <= 0) {
         return false;
      }

      pos--;
      return true;
   }

   public boolean scroll(int i)
   {
      pos += i;
      return pos >= 0 && pos < results.size();
   }

   public boolean last()
   {
      pos = results.size() - 1;
      return pos >= 0;
   }

   public boolean first()
   {
      pos = 0;
      return !results.isEmpty();
   }

   public void beforeFirst()
   {
      pos = -1;
   }

   public void afterLast()
   {
      pos = results.size();
   }

   public boolean isFirst()
   {
      return pos == 0;
   }

   public boolean isLast()
   {
      return pos == results.size() - 1;
   }

   public void close()
   {
   }

   public Object[] get()
   {
      if (results.isEmpty() || pos < 0 || pos >= results.size()) {
         return null;
      }

      Object row = results.get(pos);

      return row instanceof Object[] ? (Object[]) row : new Object[] { row };
   }

   public Object get(int i)
   {
      return get()[i];
   }

   public Type getType(int i)
   {
      return null;
   }

   public Integer getInteger(int col)
   {
      return (Integer) get(col);
   }

   public Long getLong(int col)
   {
      return (Long) get(col);
   }

   public Float getFloat(int col)
   {
      return (Float) get(col);
   }

   public Boolean getBoolean(int col)
   {
      return (Boolean) get(col);
   }

   public Double getDouble(int col)
   {
      return (Double) get(col);
   }

   public Short getShort(int col)
   {
      return (Short) get(col);
   }

   public Byte getByte(int col)
   {
      return (Byte) get(col);
   }

   public Character getCharacter(int col)
   {
      return (Character) get(col);
   }

   public byte[] getBinary(int col)
   {
      return (byte[]) get(col);
   }

   public String getText(int col)
   {
      return (String) get(col);
   }

   public Blob getBlob(int col)
   {
      return (Blob) get(col);
   }

   public Clob getClob(int col)
   {
      return (Clob) get(col);
   }

   public String getString(int col)
   {
      return (String) get(col);
   }

   public BigDecimal getBigDecimal(int col)
   {
      return (BigDecimal) get(col);
   }

   public BigInteger getBigInteger(int col)
   {
      return (BigInteger) get(col);
   }

   public Date getDate(int col)
   {
      return (Date) get(col);
   }

   public Locale getLocale(int col)
   {
      return (Locale) get(col);
   }

   public Calendar getCalendar(int col)
   {
      return (Calendar) get(col);
   }

   public TimeZone getTimeZone(int col)
   {
      return (TimeZone) get(col);
   }

   public int getRowNumber()
   {
      return pos;
   }

   public boolean setRowNumber(int rowNumber)
   {
      pos = rowNumber >= 0 ? rowNumber : results.size() + rowNumber;

      return pos >= 0 && pos < results.size();
   }
}