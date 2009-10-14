package mockit.emulation.hibernate3.ast.whereClause;

import mockit.emulation.hibernate3.ast.*;

public abstract class Expr
{
   public static Expr parse(Tokens tokens)
   {
      return LogicalOrExpr.parse(tokens);
   }

   public abstract Object evaluate(QueryEval eval);
}
