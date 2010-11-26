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
import de.upb.hni.vmagic.DiscreteRange;

/**
 * Slice of a VhdlObject.
 * @param <T> the object type
 */
public class Slice<T extends VhdlObject> extends ForwardingVhdlObject<T> implements Target<T> {

    private final DiscreteRange range;

    /**
     * Creates a slice.
     * @param base the sliced object
     * @param range the range
     */
    public Slice(T base, DiscreteRange range) {
        super(base);
        this.range = range;
    }

    /**
     * Returns the range of this slice.
     * @return the range
     */
    public DiscreteRange getRange() {
        return range;
    }
}
