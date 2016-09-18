package com.gilecode.yagson.refs.impl;

import com.gilecode.yagson.refs.ReferencesContextFactory;
import com.gilecode.yagson.refs.ReferencesReadContext;
import com.gilecode.yagson.refs.ReferencesWriteContext;

public class ReferencesCircularAndSiblingsContextFactory implements ReferencesContextFactory {

    public ReferencesReadContext createReadContext() {
        return new ReferencesCircularAndSiblingsModeContext().new RefsReadContext();
    }

    public ReferencesWriteContext createWriteContext(Object root) {
        return new ReferencesCircularAndSiblingsModeContext().new RefsWriteContext(root);
    }

}
