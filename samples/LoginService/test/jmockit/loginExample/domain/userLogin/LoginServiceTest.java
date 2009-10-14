/*
 * JMockit Samples
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
package jmockit.loginExample.domain.userLogin;

import org.junit.*;
import org.junit.runner.*;

import mockit.*;
import mockit.integration.junit4.*;

import jmockit.loginExample.domain.userAccount.*;

@RunWith(JMockit.class)
public final class LoginServiceTest
{
   @Mocked private UserAccount account;
   private LoginService service;

   @Before
   public void init()
   {
      service = new LoginService();

      new NonStrictExpectations()
      {{
         UserAccount.find("john"); returns(account);
      }};
   }

   @Test
   public void setAccountToLoggedInWhenPasswordMatches() throws Exception
   {
      willMatchPassword(true);

      service.login("john", "password");

      new Verifications()
      {{
         account.setLoggedIn(true);
      }};
   }

   private void willMatchPassword(final boolean match)
   {
      new NonStrictExpectations()
      {{
         account.passwordMatches(withAny("")); returns(match);
      }};
   }

   @Test
   public void setAccountToRevokedAfterThreeFailedLoginAttempts() throws Exception
   {
      willMatchPassword(false);

      for (int i = 0; i < 3; i++) {
         service.login("john", "password");
      }

      new Verifications()
      {{
         account.setRevoked(true);
      }};
   }

   @Test
   public void notSetAccountLoggedInIfPasswordDoesNotMatch() throws Exception
   {
      willMatchPassword(false);

      service.login("john", "password");

      new Verifications()
      {{
         account.setLoggedIn(true); repeats(0);
      }};
   }

   @Test
   public void notRevokeSecondAccountAfterTwoFailedAttemptsOnFirstAccount(
      final UserAccount secondAccount) throws Exception
   {
      willMatchPassword(false);
      new NonStrictExpectations()
      {{
         UserAccount.find("roger"); returns(secondAccount);
         secondAccount.passwordMatches(withAny("")); returns(false);
      }};

      service.login("john", "password");
      service.login("john", "password");
      service.login("roger", "password");

      new AccountNotRevoked(secondAccount);
   }

   private static final class AccountNotRevoked extends Verifications
   {
      public AccountNotRevoked(UserAccount accountToVerify)
      {
         accountToVerify.setRevoked(true); repeats(0);
      }
   }

   @Test(expected = AccountLoginLimitReachedException.class)
   public void disallowConcurrentLogins() throws Exception
   {
      willMatchPassword(true);
      new NonStrictExpectations()
      {{
         account.isLoggedIn(); returns(true);
      }};

      service.login("john", "password");
   }

   @Test(expected = UserAccountNotFoundException.class)
   public void throwExceptionIfAccountNotFound() throws Exception
   {
      new NonStrictExpectations()
      {{
         UserAccount.find("roger"); returns(null);
      }};

      new LoginService().login("roger", "password");
   }

   @Test(expected = UserAccountRevokedException.class)
   public void disallowLoggingIntoRevokedAccount() throws Exception
   {
      willMatchPassword(true);
      new NonStrictExpectations()
      {{
         account.isRevoked(); returns(true);
      }};

      service.login("john", "password");
   }

   @Test
   public void resetBackToInitialStateAfterSuccessfulLogin() throws Exception
   {
      new NonStrictExpectations()
      {{
         account.passwordMatches(withAny("")); returns(false, false, true, false);
      }};

      service.login("john", "password");
      service.login("john", "password");
      service.login("john", "password");
      service.login("john", "password");

      new AccountNotRevoked(account);
   }
}
