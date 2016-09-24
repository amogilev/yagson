/*
 * Copyright (C) 2016 Andrey Mogilev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gilecode.yagson.refs.impl;

import com.gilecode.yagson.refs.ReferencesPolicy;
import com.google.gson.JsonSyntaxException;

/**
 * Used to find circular dependencies and duplicate references during the 
 * serialization.
 * 
 * @author Andrey Mogilev
 */
public class ReferencesCircularOnlyModeContext extends ReferencesAllDuplicatesModeContext {
  
  static ReferencesPolicy policy = ReferencesPolicy.CIRCULAR_ONLY;
  
  class RefsWriteContext extends ReferencesAllDuplicatesModeContext.RefsWriteContext {
    public RefsWriteContext(Object root) {
      super(root);
    }

    @Override
    public ReferencesPolicy getPolicy() {
      return policy;
    }

    @Override
    protected void endObject(Object value) {
      super.endObject(value);
      if (value != null) {
        references.remove(value);        
      }
    }
  }
  
  class RefsReadContext extends ReferencesAllDuplicatesModeContext.RefsReadContext {

    @Override
    public ReferencesPolicy getPolicy() {
      return policy;
    }

    @Override
    protected void afterObjectRead() {
      objectsByReference.remove(getCurrentReference());
      super.afterObjectRead();
    }

    @Override
    protected Object getObjectByReference(String reference) throws JsonSyntaxException {
      Object value = objectsByReference.get(reference);
      if (value == null) {
        if (!getCurrentReference().contains(reference)) {
          throw new JsonSyntaxException("The reference cannot be read, as the current ReferencesPolicy " +
                  "allows only circular references: '" + reference + "'");
        }
        throw new JsonSyntaxException("Missing reference '" + reference + "'");
      }
      return value;
    }
  }
}
