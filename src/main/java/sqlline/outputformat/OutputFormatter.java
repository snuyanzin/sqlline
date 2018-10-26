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
package sqlline.outputformat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import sqlline.DispatchCallback;
import sqlline.SqlLine;
import sqlline.SqlLineOpts;

/**
 * OutputFormatter.
 */
public class OutputFormatter {
  private OutputFormatter() {
  }

  ///////////////////////////////////////
  // ResultSet output formatting classes
  ///////////////////////////////////////

  public static int print(
      SqlLine sqlLine, ResultSet rs, DispatchCallback callback)
      throws SQLException {
    final SqlLineOpts opts = sqlLine.getOpts();
    String format = opts.getOutputFormat();
    OutputFormat f = sqlLine.getOutputFormats().get(format);
    if ("csv".equals(format)) {
      final SeparatedValuesOutputFormat csvOutput =
          (SeparatedValuesOutputFormat) f;
      if ((csvOutput.separator == null && opts.getCsvDelimiter() != null)
          || (csvOutput.separator != null
              && !csvOutput.separator.equals(opts.getCsvDelimiter())
              || csvOutput.quoteCharacter != opts.getCsvQuoteCharacter())) {
        f = new SeparatedValuesOutputFormat(sqlLine,
            opts.getCsvDelimiter(), opts.getCsvQuoteCharacter());
        Map<String, OutputFormat> updFormats =
            new HashMap<>(sqlLine.getOutputFormats());
        updFormats.put("csv", f);
        sqlLine.updateOutputFormats(updFormats);
      }
    }

    if (f == null) {
      sqlLine.error(
          sqlLine.loc(
              "unknown-format", format, sqlLine.getOutputFormats().keySet()));
      f = new TableOutputFormat(sqlLine);
    }

    Rows rows;
    if (opts.getIncremental()) {
      rows = new IncrementalRows(sqlLine, rs, callback);
    } else {
      rows = new BufferedRows(sqlLine, rs);
    }

    return f.print(rows);
  }
}

// End OutputFormatter.java
