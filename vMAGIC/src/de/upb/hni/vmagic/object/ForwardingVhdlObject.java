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
import de.upb.hni.vmagic.expression.VhdlObject.Mode;
import de.upb.hni.vmagic.expression.VhdlObject.ObjectClass;
import de.upb.hni.vmagic.type.SubtypeIndication;
import java.util.Collections;
import java.util.List;

/**
 * Forwarding VHDL object.
 * @param <T> the object type
 */
public abstract class ForwardingVhdlObject<T extends VhdlObject> extends AbstractVhdlObject<T> {

    private final T base;

    /**
     * Creates a forwarding VHDL object.
     * @param base
     */
    public ForwardingVhdlObject(T base) {
        this.base = base;
    }

    /**
     * Returns the base object.
     * @return the base object
     */
    public T getBase() {
        return base;
    }

    public String getIdentifier() {
        return base.getIdentifier();
    }

    public void setIdentifier(String identifier) {
        base.setIdentifier(identifier);
    }

    //TODO: implement correctly
    public SubtypeIndication getType() {
        return base.getType();
    }

    //TODO: implement correctly
    public void setType(SubtypeIndication type) {
        base.setType(type);
    }

    public Mode getMode() {
        return base.getMode();
    }

    public void setMode(Mode mode) {
        base.setMode(mode);
    }

    public ObjectClass getObjectClass() {
        return base.getObjectClass();
    }

    public List<T> getVhdlObjects() {
        return Collections.singletonList(base);
    }
}
