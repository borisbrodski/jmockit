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

import org.testng.annotations.*;

import mockit.*;

import jmockit.loginExample.domain.userAccount.*;

public final class LoginServiceTest
{
   @Mocked UserAccount account;
   LoginService service;

   @BeforeMethod
   public void init()
   {
      service = new LoginService();

      new NonStrictExpectations()
      {{
         UserAccount.find("john"); result = account;
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
         account.passwordMatches(anyString); result = match;
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
         account.setLoggedIn(true); times = 0;
      }};
   }

   @Test
   public void notRevokeSecondAccountAfterTwoFailedAttemptsOnFirstAccount(
      final UserAccount secondAccount) throws Exception
   {
      willMatchPassword(false);

      new NonStrictExpectations()
      {{
         UserAccount.find("roger"); result = secondAccount;
         secondAccount.passwordMatches(anyString); result = false;
      }};

      service.login("john", "password");
      service.login("john", "password");
      service.login("roger", "password");

      new AccountNotRevoked(secondAccount);
   }

   private static final class AccountNotRevoked extends Verifications
   {
      AccountNotRevoked(UserAccount accountToVerify)
      {
         accountToVerify.setRevoked(true); times = 0;
      }
   }

   @Test(expectedExceptions = AccountLoginLimitReachedException.class)
   public void disallowConcurrentLogins() throws Exception
   {
      willMatchPassword(true);

      new NonStrictExpectations()
      {{
         account.isLoggedIn(); result = true;
      }};

      service.login("john", "password");
   }

   @Test(expectedExceptions = UserAccountNotFoundException.class)
   public void throwExceptionIfAccountNotFound() throws Exception
   {
      new NonStrictExpectations()
      {{
         UserAccount.find("roger"); result = null;
      }};

      new LoginService().login("roger", "password");
   }

   @Test(expectedExceptions = UserAccountRevokedException.class)
   public void disallowLoggingIntoRevokedAccount() throws Exception
   {
      willMatchPassword(true);

      new NonStrictExpectations()
      {{
         account.isRevoked(); result = true;
      }};

      service.login("john", "password");
   }

   @Test
   public void resetBackToInitialStateAfterSuccessfulLogin() throws Exception
   {
      new NonStrictExpectations()
      {{
         account.passwordMatches(anyString); returns(false, false, true, false);
      }};

      service.login("john", "password");
      service.login("john", "password");
      service.login("john", "password");
      service.login("john", "password");

      new AccountNotRevoked(account);
   }
}
