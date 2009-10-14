package mockit.emulation.hibernate3.ast;

public final class QuerySyntaxException extends RuntimeException
{
   public QuerySyntaxException(Tokens query)
   {
      super("Syntax error in query:\n" + query);
   }
}
