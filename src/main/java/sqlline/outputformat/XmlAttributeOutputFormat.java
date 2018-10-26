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

import sqlline.SqlLine;

/**
 * Implementation of {@link OutputFormat} that formats rows as XML
 * elements, and each of their columns as an XML attribute.
 */
public class XmlAttributeOutputFormat extends AbstractOutputFormat {
  // '> in case double quotes for attribute value
  // "> in case single quotes for attribute value
  private static final String ALLOWED_NOT_ENCODE_SYMBOLS = "'>";
  public XmlAttributeOutputFormat(SqlLine sqlLine) {
    super(sqlLine);
  }

  public void printHeader(Rows.Row header) {
    sqlLine.output("<resultset>");
  }

  public void printFooter(Rows.Row header) {
    sqlLine.output("</resultset>");
  }

  public void printRow(Rows rows, Rows.Row header, Rows.Row row) {
    String[] head = header.values;
    String[] vals = row.values;

    StringBuilder result = new StringBuilder("  <result");

    for (int i = 0; (i < head.length) && (i < vals.length); i++) {
      result.append(' ').append(head[i]).append("=\"").append(
          Rows.xmlEncode(vals[i], ALLOWED_NOT_ENCODE_SYMBOLS)).append('"');
    }

    result.append("/>");

    sqlLine.output(result.toString());
  }
}

// End XmlAttributeOutputFormat.java
