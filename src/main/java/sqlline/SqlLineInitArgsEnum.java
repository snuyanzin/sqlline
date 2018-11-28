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

import java.util.Objects;

import static sqlline.SqlLine.COMMAND_PREFIX;

/**
 * Sqlline arguments.
 */
public enum SqlLineInitArgsEnum implements SqlLineInitArg {
  // The list is ordered in a way the arguments will be applied
  APP_CONFIG("-ac", "appconfig"),
  PASS("-p", null),
  URL("-u", "connect"),
  USER("-n", null),
  DRIVER("-d", null),
  NICK_NAME("-nn", "nickname"),
  LOG_FILE("-log", "record"),
  COMMAND_HANDLER("-ch", "commandhandler"),
  FILES("", "properties", true, null, false, false),
  COMMANDS("-e", null),
  FILE_TO_RUN("-f", "run", false, "run", true, true);

  private final String key;
  private final String command;
  private final boolean isMultiple;
  private final String propertiesKey;
  private final boolean failIfActionNotSuccessful;
  private final boolean exitAfter;

  SqlLineInitArgsEnum(String key, String command) {
    this(key, command, false, null, false, false);
  }

  SqlLineInitArgsEnum(
      String key,
      String command,
      boolean isMultiple,
      String propertiesKey,
      boolean failIfActionNotSuccessful,
      boolean exitAfter) {
    this.key = key;
    this.command = command;
    this.isMultiple = isMultiple;
    this.propertiesKey = propertiesKey;
    this.failIfActionNotSuccessful = failIfActionNotSuccessful;
    this.exitAfter = exitAfter;
  }

  public String getCommand() {
    return command == null ? null : COMMAND_PREFIX + command;
  }

  public String getKey() {
    return key;
  }

  public boolean isMultiple() {
    return isMultiple;
  }

  private String getPropertiesKey() {
    return propertiesKey;
  }

  public boolean failIfActionNotSuccessful() {
    return failIfActionNotSuccessful;
  }

  public boolean isQuitAfter() {
    return exitAfter;
  }

  public static SqlLineInitArgsEnum of(final String key) {
    for (SqlLineInitArgsEnum arg : values()) {
      if (arg.getKey().equals(key)) {
        return arg;
      }
    }
    return null;
  }

  public static SqlLineInitArgsEnum fromProp(final String propertiesKey) {
    for (SqlLineInitArgsEnum arg : values()) {
      if (Objects.equals(arg.getPropertiesKey(), propertiesKey)) {
        return arg;
      }
    }
    return null;
  }
}

// End SqlLineInitArgsEnum.java
