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

import de.upb.hni.vmagic.parser.ParseError;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Parse errors annotation.
 * The parse errors annotation is used to store parse errors in a <code>VhdlFile</code> instance.
 */
public class ParseErrors {

    private final List<ParseError> errors;

    public ParseErrors(List<ParseError> errors) {
        this.errors = Collections.unmodifiableList(new ArrayList<ParseError>(errors));
    }

    public List<ParseError> getErrors() {
        return errors;
    }
}
