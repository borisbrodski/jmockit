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

import jmockit.loginExample.domain.userAccount.*;

public final class LoginService
{
   private static final int MAX_LOGIN_ATTEMPTS = 3;

   private int loginAttemptsRemaining = MAX_LOGIN_ATTEMPTS;
   private String previousAccountId;
   private UserAccount account;

   public void login(String accountId, String password)
      throws UserAccountNotFoundException, UserAccountRevokedException,
      AccountLoginLimitReachedException
   {
      account = UserAccount.find(accountId);

      if (account == null) {
         throw new UserAccountNotFoundException();
      }

      if (account.passwordMatches(password)) {
         registerNewLogin();
      }
      else {
         handleFailedLoginAttempt(accountId);
      }
   }

   private void registerNewLogin()
      throws AccountLoginLimitReachedException, UserAccountRevokedException
   {
      if (account.isLoggedIn()) {
         throw new AccountLoginLimitReachedException();
      }

      if (account.isRevoked()) {
         throw new UserAccountRevokedException();
      }

      account.setLoggedIn(true);
      loginAttemptsRemaining = MAX_LOGIN_ATTEMPTS;
   }

   private void handleFailedLoginAttempt(String accountId)
   {
      if (previousAccountId == null || accountId.equals(previousAccountId)) {
         loginAttemptsRemaining--;
      }
      else {
         loginAttemptsRemaining = MAX_LOGIN_ATTEMPTS;
      }

      previousAccountId = accountId;

      if (loginAttemptsRemaining == 0) {
         account.setRevoked(true);
         loginAttemptsRemaining = MAX_LOGIN_ATTEMPTS;
      }
   }
}
