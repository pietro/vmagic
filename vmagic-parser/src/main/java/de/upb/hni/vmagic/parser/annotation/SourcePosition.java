/*
 * Copyright 2009, 2010 University of Paderborn
 *
 * This file is part of vMAGIC parser.
 *
 * vMAGIC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * vMAGIC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with vMAGIC. If not, see <http://www.gnu.org/licenses/>.
 *
 * Authors: Ralf Fuest <rfuest@users.sourceforge.net>
 *          Christopher Pohl <cpohl@users.sourceforge.net>
 */

package de.upb.hni.vmagic.parser.annotation;

/**
 * Position in the source code.
 */
public final class SourcePosition {

    private final int line;
    private final int column;
    private final int index;

    /**
     * Creates a SourcePosition.
     * @param line the line in the source code
     * @param column the colum in the line
     */
    public SourcePosition(int line, int column, int index) {
        this.line = line;
        this.column = column;
        this.index = index;
    }

    /**
     * Returns the column inside the line.
     * @return the column
     */
    public int getColumn() {
        return column;
    }

    /**
     * Returns the line.
     * @return the line
     */
    public int getLine() {
        return line;
    }

    /**
     * Returns the character index in the file.
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return "row: " + line + ", col: " + column;
    }
}
