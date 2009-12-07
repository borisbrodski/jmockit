/*  Copyright (c) 2000-2006 hamcrest.org
 */
package mockit.external.hamcrest;

import java.util.*;

import mockit.external.hamcrest.internal.*;

/**
 * A {@link Description} that is stored as a string.
 */
public abstract class BaseDescription implements Description
{
   public final Description appendText(CharSequence text)
   {
      append(text);
      return this;
   }

   public final Description appendDescriptionOf(SelfDescribing value)
   {
      value.describeTo(this);
      return this;
   }

   public final Description appendValue(Object value)
   {
      if (value == null) {
         append("null");
      }
      else if (value instanceof String) {
         toJavaSyntax((String) value);
      }
      else if (value instanceof Character) {
         append('"');
         toJavaSyntax((Character) value);
         append('"');
      }
      else if (value instanceof Short) {
         append('<');
         append(String.valueOf(value));
         append("s>");
      }
      else if (value instanceof Long) {
         append('<');
         append(String.valueOf(value));
         append("L>");
      }
      else if (value instanceof Float) {
         append('<');
         append(String.valueOf(value));
         append("F>");
      }
      else if (value.getClass().isArray()) {
         appendValueList("[", ", ", "]", new ArrayIterator(value));
      }
      else {
         append('<');
         append(String.valueOf(value));
         append('>');
      }
      return this;
   }

   public final <T> Description appendValueList(
      String start, String separator, String end, T... values)
   {
      return appendValueList(start, separator, end, Arrays.asList(values));
   }

   public final <T> Description appendValueList(
      String start, String separator, String end, Iterable<T> values)
   {
      return appendValueList(start, separator, end, values.iterator());
   }

   private <T> Description appendValueList(
      String start, String separator, String end, Iterator<T> values)
   {
      return appendList(start, separator, end, new SelfDescribingValueIterator<T>(values));
   }

   private Description appendList(
      String start, String separator, String end, Iterator<? extends SelfDescribing> i)
   {
      append(start);

      boolean separate = false;

      while (i.hasNext()) {
         if (separate) {
            append(separator);
         }

         appendDescriptionOf(i.next());
         separate = true;
      }

      append(end);

      return this;
   }

   /**
    * Append the String <var>str</var> to the description.
    * The default implementation passes every character to {@link #append(char)}.
    * Override in subclasses to provide an efficient implementation.
    */
   protected void append(CharSequence str)
   {
      for (int i = 0; i < str.length(); i++) {
         append(str.charAt(i));
      }
   }

   /**
    * Append the char <var>c</var> to the description.
    */
   protected abstract void append(char c);

   private void toJavaSyntax(String unformatted)
   {
      append('"');

      for (int i = 0; i < unformatted.length(); i++) {
         toJavaSyntax(unformatted.charAt(i));
      }

      append('"');
   }

   private void toJavaSyntax(char ch)
   {
      switch (ch) {
         case '"':
            append("\\\"");
            break;
         case '\n':
            append("\\n");
            break;
         case '\r':
            append("\\r");
            break;
         case '\t':
            append("\\t");
            break;
         default:
            append(ch);
      }
   }
}
