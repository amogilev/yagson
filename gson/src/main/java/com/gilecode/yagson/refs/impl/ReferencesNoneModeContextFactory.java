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

import com.gilecode.yagson.refs.ReferencesContextFactory;
import com.gilecode.yagson.refs.ReferencesReadContext;
import com.gilecode.yagson.refs.ReferencesWriteContext;

/**
 * A factory which creates {@link ReferencesReadContext} and {@link ReferencesWriteContext} for a
 * {@link com.gilecode.yagson.refs.ReferencesPolicy#DISABLED} references policy.
 * <p/>
 * NOTE: This is a non-default policy, use with care!
 *
 * @author Andrey Mogilev
 */
public class ReferencesNoneModeContextFactory implements ReferencesContextFactory {
  
  public ReferencesReadContext createReadContext() {
    return ReferencesNoneModeContext.instance;
  }

  public ReferencesWriteContext createWriteContext(Object root) {
    return ReferencesNoneModeContext.instance;
  }
}
