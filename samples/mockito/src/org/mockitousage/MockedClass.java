package org.mockitousage;

import java.util.*;

public class MockedClass
{
   private final List<String> items = new ArrayList<String>();

   public String someMethod(String s) { return s; }
   public int getSomeValue() { return -1; }
   public void doSomething(String s, boolean b) { items.add(b ? s : s.trim()); }
   public String getItem(int index) { return items.get(index); }
}
