/**
   Copyright Katros LTD (2014)
**/
package net.katros.services.proto.grep;

import net.katros.services.proto.GrepBaseListener;
import net.katros.services.proto.GrepParser;
import net.katros.services.proto.GrepParser.IndexContext;
import java.util.Set;

/**
 * @author boris@temk.org
**/
public class GrepDeclarationListener extends  GrepBaseListener {
    private final Set<String> set;
    private int depth = 0;
    
    public GrepDeclarationListener(Set<String> set) {
        this.set = set;
    }

    @Override
    public void enterIndex(IndexContext ctx) {
      if (ctx.ID() != null) {
          set.add(ctx.ID().getText());
      }
    }

    @Override
    public void exitRoot(GrepParser.RootContext ctx) {
        if (ctx.ROOT() != null) {
            if (depth < ctx.ROOT().getText().length()) {
                depth = ctx.ROOT().getText().length();
            }
        }
    }

    public int getDepth() {
        return depth;
    }
}
