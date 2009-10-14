/*
 * JMockit: a class library for developer testing with "mock methods"
 * Copyright (c) 2006, 2007 Rogério Liesenfeld
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
package orderMngr.service;

import java.sql.*;

/**
 * A static facade for database access through JDBC. It assumes the application can use a single
 * global DB connection. (This class is just for the sake of demonstration; in the real world,
 * direct use of JDBC like this is not too practical.)
 */
public final class Database
{
   private static Connection connection;

   public static synchronized Connection connection()
   {
      if (connection == null) {
         try {
            connection = DriverManager.getConnection("jdbc:test:ordersDB");
         }
         catch (SQLException e) {
            throw new RuntimeException(e);
         }
      }

      return connection;
   }

   public static void executeInsertUpdateOrDelete(String sql, Object... args)
   {
      PreparedStatement stmt = null;

      try {
         stmt = createStatement(sql, args);
         stmt.executeUpdate();
      }
      catch (SQLException e) {
         throw new RuntimeException(e);
      }
      finally {
         closeStatement(stmt);
      }
   }

   private static PreparedStatement createStatement(String sql, Object... args) throws SQLException
   {
      PreparedStatement stmt = connection().prepareStatement(sql);
      int i = 1;

      for (Object arg : args) {
         stmt.setObject(i, arg);
         i++;
      }

      return stmt;
   }

   public static void closeStatement(Statement stmt)
   {
      if (stmt != null) {
         try {
            stmt.close();
         }
         catch (SQLException e) {
            throw new RuntimeException(e);
         }
      }
   }

   public static void closeStatement(ResultSet result)
   {
      if (result != null) {
         try {
            result.getStatement().close();
         }
         catch (SQLException e) {
            throw new RuntimeException(e);
         }
      }
   }

   public static ResultSet executeQuery(String sql, Object... args)
   {
      try {
         PreparedStatement stmt = createStatement(sql, args);
         return stmt.executeQuery();
      }
      catch (SQLException e) {
         throw new RuntimeException(e);
      }
   }
}
