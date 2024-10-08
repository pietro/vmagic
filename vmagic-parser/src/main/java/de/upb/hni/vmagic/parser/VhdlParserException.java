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

package de.upb.hni.vmagic.parser;

/**
 * Vhdl parser exception.
 */
public class VhdlParserException extends Exception {

    /**
     * Creates a new instance of <code>VhdlParserException</code> without detail message.
     */
    public VhdlParserException() {
    }


    /**
     * Constructs an instance of <code>VhdlParserException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public VhdlParserException(String msg) {
        super(msg);
    }
}
