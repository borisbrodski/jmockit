/*
 * JMockit Samples
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
package org.jmock.samples.sniper;

import org.junit.*;

import mockit.*;

public final class AuctionSniper_JMockit_Test
{
   final Money increment = new Money(2);
   final Money maximumBid = new Money(20);
   final Money beatableBid = new Money(10);
   final Money unbeatableBid = maximumBid.add(new Money(1));

   @Mocked Auction auction;
   @Mocked AuctionSniperListener listener;

   AuctionSniper sniper;

   @Before
   public void init()
   {
      sniper = new AuctionSniper(auction, increment, maximumBid, listener);
   }

   @Test
   public void triesToBeatTheLatestHighestBid() throws Exception
   {
      final Money expectedBid = beatableBid.add(increment);

      new Expectations()
      {{
         auction.bid(expectedBid);
      }};

      sniper.bidAccepted(beatableBid);
   }

   @Test
   public void willNotBidPriceGreaterThanMaximum() throws Exception
   {
      new Expectations()
      {{
         auction.bid((Money) any); times = 0;
      }};

      sniper.bidAccepted(unbeatableBid);
   }

   @Test
   public void willLimitBidToMaximum() throws Exception
   {
      new Expectations()
      {{
         auction.bid(maximumBid);
      }};

      sniper.bidAccepted(maximumBid.subtract(new Money(1)));
   }

   @Test
   public void willAnnounceItHasFinishedIfPriceGoesAboveMaximum()
   {
      new Expectations()
      {{
         listener.sniperFinished(sniper);
      }};

      sniper.bidAccepted(unbeatableBid);
   }

   @Test
   public void catchesExceptionsAndReportsThemToErrorListener() throws Exception
   {
      final AuctionException exception = new AuctionException("test");

      new Expectations()
      {{
         auction.bid((Money) any); result = exception;
         listener.sniperBidFailed(sniper, exception);
      }};

      sniper.bidAccepted(beatableBid);
   }
}