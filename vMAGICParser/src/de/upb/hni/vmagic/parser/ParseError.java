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

import de.upb.hni.vmagic.parser.annotation.PositionInformation;

/**
 * Parse error.
 */
public class ParseError {

    private final PositionInformation position;
    private final Type type;
    private final String message;

    /**
     * Creates a new parse error.
     * @param position the position of the error
     * @param type the error type
     * @param message the message or identifier
     */
    public ParseError(PositionInformation position, Type type, String message) {
        this.position = position;
        this.type = type;
        this.message = message;
    }

    public PositionInformation getPosition() {
        return position;
    }

    public Type getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public enum Type {

        UNKNOWN_CONFIGURATION,
        UNKNOWN_CONSTANT,
        UNKNOWN_COMPONENT,
        UNKNOWN_ENTITY,
        UNKNOWN_FILE,
        UNKNOWN_SIGNAL,
        UNKNOWN_SIGNAL_ASSIGNMENT_TARGET,
        UNKNOWN_LOOP,
        UNKNOWN_PACKAGE,
        UNKNOWN_TYPE,
        UNKNOWN_VARIABLE,
        UNKNOWN_VARIABLE_ASSIGNMENT_TARGET,
        UNKNOWN_OTHER;
    }
}
