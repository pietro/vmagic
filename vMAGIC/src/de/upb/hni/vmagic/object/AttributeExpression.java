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
import de.upb.hni.vmagic.declaration.Attribute;
import de.upb.hni.vmagic.expression.Expression;

/**
 * Attribute expression.
 * @param <T> the object type
 */
public class AttributeExpression<T extends VhdlObject> extends ForwardingVhdlObject<T> {

    private final Attribute attribute;
    private final Expression parameter;

    /**
     * Creates an attribute expression.
     * @param base the base object
     * @param attribute the attribute
     */
    public AttributeExpression(T base, Attribute attribute) {
        super(base);
        this.attribute = attribute;
        this.parameter = null;
    }

    /**
     * Creates an attribute expression with a parameter.
     * @param base the base object
     * @param attribute the attribute
     * @param parameter the parameter
     */
    public AttributeExpression(T base, Attribute attribute, Expression parameter) {
        super(base);
        this.attribute = attribute;
        this.parameter = parameter;
    }

    /**
     * Returns the attribute.
     * @return the attribute
     */
    public Attribute getAttribute() {
        return attribute;
    }

    /**
     * Returns the parameter.
     * @return the parameter
     */
    public Expression getParameter() {
        return parameter;
    }
}
