/*
 * Copyright 2009, 2010 University of Paderborn
 *
 * This file is part of vMAGIC.
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

package de.upb.hni.vmagic.object;

import de.upb.hni.vmagic.expression.VhdlObject;

/**
 * Record element.
 * @param <T> the object type
 */
public class RecordElement<T extends VhdlObject> extends ForwardingVhdlObject<T> implements Target<T> {

    private final String element;

    /**
     * Creates a record element.
     * @param base the base object
     * @param element the identifier of the element
     */
    public RecordElement(T base, String element) {
        super(base);
        this.element = element;
    }

    /**
     * Returns the identifier of the record element.
     * @return the identifier
     */
    public String getElement() {
        return element;
    }
}
