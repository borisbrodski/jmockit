/*  Copyright (c) 2000-2009 hamcrest.org
 */
package org.hamcrest.number;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class OrderingComparison<T extends Comparable<T>> extends TypeSafeMatcher<T> {
    private static final int LESS_THAN = -1;
    private static final int GREATER_THAN = 1;
    private static final int EQUAL = 0;
    private final T expected;
    private final int minCompare, maxCompare;

    private OrderingComparison(T expected, int minCompare, int maxCompare) {
        this.expected = expected;
        this.minCompare = minCompare;
        this.maxCompare = maxCompare;
    }

    @Override
    public boolean matchesSafely(T actual) {
        int compare = Integer.signum(actual.compareTo(expected));
        return minCompare <= compare && compare <= maxCompare;
    }

    @Override
    public void describeMismatchSafely(T actual, Description mismatchDescription) {
      mismatchDescription.appendValue(expected) .appendText(" was ")
                         .appendText(comparison(actual.compareTo(expected)))
                         .appendText(" ").appendValue(actual);
    };
    
    public void describeTo(Description description) {
        description.appendText("a value ").appendText(comparison(minCompare));
        if (minCompare != maxCompare) {
            description.appendText(" or ").appendText(comparison(maxCompare));
        }
        description.appendText(" ").appendValue(expected);
    }

    private String comparison(int compare) {
      if (compare == EQUAL) {
        return "equal to";
      } else if (compare > EQUAL) {
        return "greater than";
      } else {
        return "less than";
      }
    }

    /**
     * Is value = expected?
     */
    @Factory
    public static <T extends Comparable<T>> Matcher<? super T> comparesEqualTo(T value) {
        return new OrderingComparison<T>(value, EQUAL, EQUAL);
    }

    /**
     * Is value > expected?
     */
    @Factory
    public static <T extends Comparable<T>> Matcher<? super T> greaterThan(T value) {
        return new OrderingComparison<T>(value, GREATER_THAN, GREATER_THAN);
    }

    /**
     * Is value >= expected?
     */
    @Factory
    public static <T extends Comparable<T>> Matcher<? super T> greaterThanOrEqualTo(T value) {
        return new OrderingComparison<T>(value, EQUAL, GREATER_THAN);
    }

    /**
     * Is value < expected?
     */
    @Factory
    public static <T extends Comparable<T>> Matcher<? super T> lessThan(T value) {
        return new OrderingComparison<T>(value, LESS_THAN, LESS_THAN);
    }

    /**
     * Is value <= expected?
     */
    @Factory
    public static <T extends Comparable<T>> Matcher<? super T> lessThanOrEqualTo(T value) {
        return new OrderingComparison<T>(value, LESS_THAN, EQUAL);
    }
}