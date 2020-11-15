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

import java.util.function.Function;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import static sqlline.SqlLine.rpad;

/**
 * OutputFormat for a pretty, table-like format.
 */
class TableOutputFormat implements OutputFormat {
  private final SqlLine sqlLine;

  TableOutputFormat(SqlLine sqlLine) {
    this.sqlLine = sqlLine;
  }

    /** Header type to differentiate
     * header position: bottom, middle, top */
  enum LineType {
    HEADER_TOP(TableOutputFormatStyle::getHeaderCrossDown,
        TableOutputFormatStyle::getHeaderLine,
        TableOutputFormatStyle::getHeaderTopLeft,
        TableOutputFormatStyle::getHeaderTopRight),
    HEADER_BOTTOM(TableOutputFormatStyle::getBodyCrossUp,
        TableOutputFormatStyle::getHeaderLine,
        TableOutputFormatStyle::getBodyBottomLeft,
        TableOutputFormatStyle::getBodyBottomRight),
    HEADER_BODY(TableOutputFormatStyle::getHeaderBodyCross,
        TableOutputFormatStyle::getHeaderLine,
        TableOutputFormatStyle::getHeaderBodyCrossLeft,
        TableOutputFormatStyle::getHeaderBodyCrossRight),
    BODY_BOTTOM(TableOutputFormatStyle::getBodyCrossUp,
        TableOutputFormatStyle::getBodyLine,
        TableOutputFormatStyle::getBodyBottomLeft,
        TableOutputFormatStyle::getBodyBottomRight);

    private final Function<TableOutputFormatStyle, Character> cross;
    private final Function<TableOutputFormatStyle, Character> line;
    private final Function<TableOutputFormatStyle, Character> left;
    private final Function<TableOutputFormatStyle, Character> right;

    LineType(Function<TableOutputFormatStyle, Character> cross,
        Function<TableOutputFormatStyle, Character> line,
        Function<TableOutputFormatStyle, Character> left,
        Function<TableOutputFormatStyle, Character> right) {
      this.line = line;
      this.cross = cross;
      this.left = left;
      this.right = right;
    }
  }

  public int print(Rows rows) {
    int index = 0;
    StringBuilder topHeader = null;
    StringBuilder headerBody = null;
    StringBuilder bottomHeader = null;
    StringBuilder bottomBody = null;
    AttributedString headerCols = null;
    final int width = getCalculatedWidth();
    final TableOutputFormatStyle style =
        BuiltInTableOutputFormatStyles.BY_NAME.get(
            sqlLine.getOpts().getTableStyle());

    // normalize the columns sizes
    rows.normalizeWidths(sqlLine.getOpts().getMaxColumnWidth());

    while (rows.hasNext()) {
      Rows.Row row = rows.next();
      AttributedString attributedString =
          getOutputString(rows, row, style, index == 0, width);
      attributedString = attributedString
          .substring(0, Math.min(attributedString.length(), width + 4));

      if (sqlLine.getOpts().getShowHeader()) {
        if (index <= 1) {
          topHeader = buildHeaderLine(row, style, LineType.HEADER_TOP, width);
          headerBody = buildHeaderLine(row, style, LineType.HEADER_BODY, width);
          bottomHeader =
              buildHeaderLine(row, style, LineType.HEADER_BOTTOM, width);
          bottomBody = buildHeaderLine(row, style, LineType.BODY_BOTTOM, width);
          headerCols = index == 0 ? attributedString : headerCols;
        }

        final int headerInterval =
            sqlLine.getOpts().getHeaderInterval();
        if (index <= 1
            || headerInterval > 0 && index % headerInterval == 0) {
          if (index == 0) {
            printRow(buildHeader(headerCols, topHeader));
            printRow(headerCols);
          } else {
            printRow(buildHeader(headerCols, headerBody));
          }
        }
      }

      if (index != 0) { // don't output the topHeader twice
        printRow(attributedString);
      }

      index++;
    }

    if (sqlLine.getOpts().getShowHeader()) {
      if (index > 1 && bottomBody != null) {
        printRow(buildHeader(headerCols, bottomBody));
      } else if (bottomHeader != null) {
        printRow(buildHeader(headerCols, bottomHeader));
      }
    }
    return index - 1;
  }

  private int getCalculatedWidth() {
    final int maxWidth = sqlLine.getOpts().getMaxWidth();
    int width = (maxWidth == 0 && sqlLine.getLineReader() != null
        ? sqlLine.getLineReader().getTerminal().getWidth()
        : maxWidth) - 4;
    return Math.max(width, 0);
  }

  void printRow(AttributedString attributedString) {
    AttributedStringBuilder builder = new AttributedStringBuilder();
    sqlLine.output(
            builder.append(attributedString)
                    .toAttributedString());
  }

  private StringBuilder buildHeaderLine(Rows.Row row,
      TableOutputFormatStyle style, LineType lineType, int width) {
    StringBuilder header = new StringBuilder();
    final char lineChar = lineType.line.apply(style);
    final char crossChar = lineType.cross.apply(style);
    final char leftChar = lineType.left.apply(style);
    final char rightChar = lineType.right.apply(style);
    header.append(leftChar);
    for (int j = 0; j < row.sizes.length; j++) {
      if (j != 0) {
        header.append(crossChar);
      }
      header.append(lineChar);
      final int min = Math.min(width - 2 - header.length(), row.sizes[j]);
      for (int k = 0; k < min; k++) {
        header.append(lineChar);
      }
      header.append(lineChar);
      if (header.length() >= width - 1) {
        break;
      }
    }
    header.append(rightChar);
    return header;
  }

  private AttributedString buildHeader(
      AttributedString headerCols, StringBuilder hTop) {
    AttributedString topHeader;
    topHeader =
        new AttributedStringBuilder()
            .append(hTop.toString(), AttributedStyles.GREEN)
            .toAttributedString()
            .subSequence(0, Math.min(hTop.length(), headerCols.length()));
    return topHeader;
  }

  public AttributedString getOutputString(
      Rows rows, Rows.Row row, TableOutputFormatStyle style,
      boolean header, int width) {
    return getOutputString(rows, row,
        "" + (header ? style.getHeaderSeparator()
                : style.getBodySeparator()), width);
  }

  private AttributedString getOutputString(
      Rows rows, Rows.Row row, String delim, int width) {
    AttributedStringBuilder builder = new AttributedStringBuilder();

    boolean isStyled = sqlLine.getOpts().getColor();
    int[] offsets = new int[row.values.length];
    boolean finished = false;
    int prevLength = 0;
    while (!finished) {
      finished = true;
      for (int i = 0; i < row.values.length; i++) {
        if (i > 0) {
          builder.append(' ');
        }
        builder.append(delim,
            isStyled ? AttributedStyles.GREEN : AttributedStyle.DEFAULT);
        builder.append(' ');

        String v;

        String value;
        if (offsets[i] == -1 || row.values[i] == null) {
          value = "";
        } else {
          final int nextLine = row.values[i].indexOf('\n', offsets[i]);
          value = nextLine == -1
              ? row.values[i].substring(offsets[i])
                  : row.values[i].substring(offsets[i], nextLine);
          offsets[i] = nextLine == -1 || row.values[i].length() == nextLine + 1
              ? -1 : nextLine + 1;
          if (nextLine >= 0) {
            finished = false;
          }
        }
        if (row.isMeta) {
          v = SqlLine.center(value, row.sizes[i]);
          if (builder.length() + v.length() - prevLength > width + 2) {
            v = v.substring(0,
                Math.max(0, width + 2 + prevLength - builder.length()));
          }
          if (rows.isPrimaryKey(i)) {
            builder.append(v, AttributedStyles.CYAN);
          } else {
            builder.append(v, AttributedStyle.BOLD);
          }
        } else {
          v = rpad(value, row.sizes[i]);
          if (builder.length() + v.length() - prevLength > width + 2) {
            v = v.substring(0,
                Math.max(0, width + 2 + prevLength - builder.length()));
          }
          if (rows.isPrimaryKey(i)) {
            builder.append(v, AttributedStyles.CYAN);
          } else {
            builder.append(v);
          }
        }
        if (builder.length() - prevLength >= width) {
          break;
        }
        prevLength = builder.length();
      }
      builder.append(' ').append(delim,
          isStyled ? AttributedStyles.GREEN
              : AttributedStyle.DEFAULT);
      if (!finished) {
        builder.append(System.lineSeparator());
      }
    }

    if (row.deleted) { // make deleted rows red
      return new AttributedStringBuilder()
          .append(builder.toString(), AttributedStyles.RED)
          .toAttributedString();
    } else if (row.updated) { // make updated rows blue
      return new AttributedStringBuilder()
          .append(builder.toString(), AttributedStyles.BLUE)
          .toAttributedString();
    } else if (row.inserted) { // make new rows green
      return new AttributedStringBuilder()
          .append(builder.toString(), AttributedStyles.GREEN)
          .toAttributedString();
    }

    return builder.toAttributedString();
  }

}

// End TableOutputFormat.java
