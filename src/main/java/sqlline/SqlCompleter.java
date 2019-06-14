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

import java.sql.DatabaseMetaData;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jline.reader.*;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.utils.AttributedString;

/**
 * Suggests completions for SQL statements.
 */
class SqlCompleter extends StringsCompleter {
  protected SqlLine sqlLine;
  SqlCompleter(SqlLine sqlLine, boolean skipMeta) {
    super();
    this.sqlLine = sqlLine;
    for (Map.Entry<String, Collection<String>> candidatesMapEntry
        : getCompletions(sqlLine, skipMeta).entrySet()) {
      for (String candidateName: candidatesMapEntry.getValue()) {
        candidates.add(
            new Candidate(AttributedString.stripAnsi(candidateName),
                candidateName, candidatesMapEntry.getKey(),
                null, null, null, true));
      }
    }
  }

  @Override
  public void complete(
      LineReader reader, ParsedLine commandLine, List<Candidate> candidates) {
    try {
      String sql = reader.getBuffer().substring(0);
      sqlLine.getLineReader().getParser().parse(sql, sql.length(),
          Parser.ParseContext.ACCEPT_LINE);
    } catch (EOFError e) {
      final String missing = e.getMissing();
      if (missing != null
          && (missing.endsWith("quote") || missing.endsWith("*/"))) {
        return;
      }
    }
    super.complete(reader, commandLine, candidates);
  }

  private static Map<String, Collection<String>> getCompletions(
      SqlLine sqlLine, boolean skipMeta) {
    Map<String, Collection<String>> completions = new TreeMap<>();

    // now add the keywords from the current connection

    final DatabaseMetaData meta = sqlLine.getDatabaseConnection().meta;
    try {
      final Set<String> keywords = Stream.of(
          meta.getSQLKeywords().split(","))
              .collect(Collectors.toCollection(TreeSet::new));
      completions.put("Keywords", keywords);
    } catch (Throwable t) {
      // ignore
    }
    try {
      final Set<String> stringFunctions = Stream.of(
          meta.getStringFunctions().split(","))
              .collect(Collectors.toCollection(TreeSet::new));
      completions.put("String functions", stringFunctions);
    } catch (Throwable t) {
      // ignore
    }
    try {
      final Set<String> numericFunctions = Stream.of(
          meta.getNumericFunctions().split(","))
              .collect(Collectors.toCollection(TreeSet::new));
      completions.put("Numeric functions", numericFunctions);
    } catch (Throwable t) {
      // ignore
    }
    try {
      final Set<String> systemFunctions = Stream.of(
          meta.getSystemFunctions().split(","))
              .collect(Collectors.toCollection(TreeSet::new));
      completions.put("System functions", systemFunctions);
    } catch (Throwable t) {
      // ignore
    }
    try {
      final Set<String> timedateFunctions = Stream.of(
          meta.getTimeDateFunctions().split(","))
              .collect(Collectors.toCollection(TreeSet::new));
      completions.put("Time date functions", timedateFunctions);
    } catch (Throwable t) {
      // ignore
    }

    // now add the tables and columns from the current connection
    if (!skipMeta) {
      completions.putAll(sqlLine.getTableColumnNames(meta));
    }

    completions.computeIfAbsent("Keywords", k -> new TreeSet<>());
    completions.get("Keywords").addAll(Dialect.DEFAULT_KEYWORD_SET);
    // set the Strings that will be completed
    return completions;
  }
}

// End SqlCompleter.java
