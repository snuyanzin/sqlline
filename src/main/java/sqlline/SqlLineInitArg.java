/*
// Licensed to Julian Hyde under one or more contributor license
// agreements. See the NOTICE file distributed with this work for
// additional information regarding copyright ownership.
//
// Julian Hyde licenses this file to you under the Modified BSD License
// (the "License"); you may not use this file except in compliance with
// the License. You may obtain a copy of the License at:
//
// http://opensource.org/licenses/BSD-3-Clause
*/
package sqlline;

import java.util.Collection;
import java.util.Map;

/**
 * Definition of sqlline argument.
 */
public interface SqlLineInitArg {

  String getCommand();
  String getKey();
  boolean isMultiple();
  boolean failIfActionNotSuccessful();
  boolean isQuitAfter();

  /**
   * OneAction.
   */
  interface OneAction {
    void action(String arg,
        Map<SqlLineInitArg, Collection<String>> sessionPropertiesMap,
        DispatchCallback callback);
  }
}

// End SqlLineInitArg.java
