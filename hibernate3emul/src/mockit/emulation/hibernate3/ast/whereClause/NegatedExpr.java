package mockit.emulation.hibernate3.ast.whereClause;

import mockit.emulation.hibernate3.ast.*;

final class NegatedExpr extends Expr
{
   final Expr equalityExpr;

   NegatedExpr(Expr equalityExpr)
   {
      this.equalityExpr = equalityExpr;
   }

   public static Expr parse(Tokens tokens)
   {
      String token = tokens.next();

      if ("not".equalsIgnoreCase(token)) {
         Expr expr = EqualityExpr.parse(tokens);
         return new NegatedExpr(expr);
      }
      else {
         tokens.pushback();
         Expr expr = EqualityExpr.parse(tokens);
         return expr;
      }
   }

   @Override
   public Boolean evaluate(QueryEval eval)
   {
      return !(Boolean) equalityExpr.evaluate(eval);
   }
}
