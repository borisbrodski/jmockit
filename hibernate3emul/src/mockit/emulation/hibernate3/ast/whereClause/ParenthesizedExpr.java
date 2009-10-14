package mockit.emulation.hibernate3.ast.whereClause;

import mockit.emulation.hibernate3.ast.*;

final class ParenthesizedExpr
{
   static Expr parse(Tokens tokens)
   {
      if ('(' == tokens.nextChar()) {
         Expr subExpr = Expr.parse(tokens);

         if (subExpr == null) {
            // TODO: "(" expressionOrVector | subQuery ")"
         }

         tokens.next(")");
         return subExpr;
      }

      return null;
   }
}
