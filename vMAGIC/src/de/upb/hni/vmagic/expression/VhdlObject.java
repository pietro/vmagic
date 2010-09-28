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

package de.upb.hni.vmagic.expression;

import de.upb.hni.vmagic.DiscreteRange;
import de.upb.hni.vmagic.NamedEntity;
import de.upb.hni.vmagic.declaration.Attribute;
import de.upb.hni.vmagic.object.ArrayElement;
import de.upb.hni.vmagic.object.AttributeExpression;
import de.upb.hni.vmagic.object.RecordElement;
import de.upb.hni.vmagic.object.Slice;
import de.upb.hni.vmagic.output.OutputEnum;
import de.upb.hni.vmagic.type.SubtypeIndication;
import java.util.List;

/**
 * Vhdl object.
 * @param <T> the object type
 */
public abstract class VhdlObject<T extends VhdlObject> extends Primary<VhdlObject>
        implements NamedEntity {

    /**
     * Returns the identifier of this object.
     * @return the identifier
     */
    public abstract String getIdentifier();

    /**
     * Sets the identifier of this object.
     * @param identifier the identifier
     */
    public abstract void setIdentifier(String identifier);

    /**
     * Returns the type of this object.
     * @return the type
     */
    public abstract SubtypeIndication getType();

    /**
     * Sets the type of this object.
     * @param type the type
     */
    public abstract void setType(SubtypeIndication type);

    /**
     * Returns the mode of this vhdl object.
     * @return the mode
     */
    public abstract Mode getMode();

    /**
     * Sets the mode of this vhdl object.
     * @param mode the mode
     */
    public abstract void setMode(Mode mode);

    /**
     * Returns a slice of this vhdl object.
     * @param range the slice range.
     * @return the slice
     */
    public abstract Slice<T> getSlice(DiscreteRange range);

    /**
     * Returns an array element of this object.
     * @param index the index of the array element
     * @return the array element
     */
    public abstract ArrayElement<T> getArrayElement(Expression index);

    /**
     * Returns an array element of this object.
     * @param index the index of the array element
     * @return the array element
     */
    public abstract ArrayElement<T> getArrayElement(int index);

    /**
     * Returns an array element of this object.
     * @param indices the indices of the array element
     * @return the array element
     */
    public abstract ArrayElement<T> getArrayElement(List<Expression> indices);

    /**
     * Returns an array element of this object.
     * @param indices the indices of the array element
     * @return the array element
     */
    public abstract ArrayElement<T> getArrayElement(Expression... indices);

    /**
     * Returns a record element of this object.
     * @param element the identifier of the record element
     * @return the record element
     */
    public abstract RecordElement<T> getRecordElement(String element);

    /**
     * Returns a attribute expression of this object.
     * @param attribute the attribute
     * @return the record element
     */
    public abstract AttributeExpression<T> getAttributeExpression(Attribute attribute);

    /**
     * Returns a attribute expression of this object.
     * @param attribute the attribute
     * @param parameter the parameter
     * @return the record element
     */
    public abstract AttributeExpression<T> getAttributeExpression(Attribute attribute, Expression parameter);

    /**
     * Returns the type of this VhdlObject.
     * @return the object class
     */
    public abstract ObjectClass getObjectClass();

    @Override
    void accept(ExpressionVisitor visitor) {
        visitor.visitVhdlObject(this);
    }

    @Override
    public VhdlObject copy() {
        return this;
    }

    /**
     * Object class describes the type of VhdlObject.
     */
    public static enum ObjectClass implements OutputEnum {

        /** Constant. */
        CONSTANT("constant"),
        /** File. */
        FILE("file"),
        /** Signal. */
        SIGNAL("signal"),
        /** Variable. */
        VARIABLE("variable");
        private final String lower;
        private final String upper;

        ObjectClass(String text) {
            lower = text;
            upper = text.toUpperCase();
        }

        public String getLowerCase() {
            return lower;
        }

        public String getUpperCase() {
            return upper;
        }
    }

    /**
     * Vhdl object mode.
     */
    public static enum Mode implements OutputEnum {

        /** None. */
        NONE,
        /** In. */
        IN,
        /** Out. */
        OUT,
        /** InOut. */
        INOUT,
        /** Buffer. */
        BUFFER,
        /** Linkage. */
        LINKAGE;

        public String getLowerCase() {
            if (this == NONE) {
                return "";
            }
            return this.toString().toLowerCase();
        }

        public String getUpperCase() {
            if (this == NONE) {
                return "";
            }
            return this.toString().toUpperCase();
        }
    }
}
