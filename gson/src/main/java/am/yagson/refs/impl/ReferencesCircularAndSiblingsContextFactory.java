package am.yagson.refs.impl;

import am.yagson.refs.ReferencesContextFactory;
import am.yagson.refs.ReferencesReadContext;
import am.yagson.refs.ReferencesWriteContext;

public class ReferencesCircularAndSiblingsContextFactory implements ReferencesContextFactory {

    public ReferencesReadContext createReadContext() {
        return new ReferencesCircularAndSiblingsModeContext().new RefsReadContext();
    }

    public ReferencesWriteContext createWriteContext(Object root) {
        return new ReferencesCircularAndSiblingsModeContext().new RefsWriteContext(root);
    }

}
