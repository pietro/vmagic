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

package de.upb.hni.vmagic.output;

import de.upb.hni.vmagic.expression.Expression;
import de.upb.hni.vmagic.expression.VhdlObject;
import de.upb.hni.vmagic.object.ArrayElement;
import de.upb.hni.vmagic.object.AttributeExpression;
import de.upb.hni.vmagic.object.Constant;
import de.upb.hni.vmagic.object.FileObject;
import de.upb.hni.vmagic.object.RecordElement;
import de.upb.hni.vmagic.object.Signal;
import de.upb.hni.vmagic.object.Slice;
import de.upb.hni.vmagic.object.Variable;

/**
 * VHDL output helper class.
 */
class VhdlObjectOutputHelper {

    private VhdlObjectOutputHelper() {
    }

    public static void object(VhdlObject object, VhdlWriter writer, OutputModule output) {
        if (object instanceof RecordElement) {
            recordElement((RecordElement) object, writer, output);
        } else if (object instanceof ArrayElement) {
            arrayElement((ArrayElement) object, writer, output);
        } else if (object instanceof Slice) {
            slice((Slice) object, writer, output);
        } else if (object instanceof AttributeExpression) {
            attributeExpression((AttributeExpression) object, writer, output);
        } else {
            writer.appendIdentifier(object);
        }
    }


    public static void interfaceSuffix(VhdlObject object, VhdlWriter writer, OutputModule output) {
        if (object instanceof Signal) {
            signalInterfaceSuffix((Signal) object, writer, output);
        } else if (object instanceof Constant) {
            constantInterfaceSuffix((Constant) object, writer, output);
        } else if (object instanceof Variable) {
            variableInterfaceSuffix((Variable) object, writer, output);
        } else if (object instanceof FileObject) {
            fileInterfaceSuffix((FileObject) object, writer, output);
        } else {
            throw new IllegalStateException("Unknown interface element.");
        }
    }

    //TODO: write BUS keyword
    public static void signalInterfaceSuffix(Signal signal, VhdlWriter writer, OutputModule output) {
        output.writeSubtypeIndication(signal.getType());

        if (signal.getKind() == Signal.Kind.BUS) {
            writer.append(' ').append(signal.getKind());
        } else if (signal.getKind() == Signal.Kind.REGISTER) {
            throw new IllegalStateException("Signal kind register isn't allowed in an interface declaration");
        }

        if (signal.getDefaultValue() != null) {
            writer.append(" := ");
            output.writeExpression(signal.getDefaultValue());
        }
    }

    //TODO: add mode
    public static void variableInterfaceSuffix(Variable variable, VhdlWriter writer, OutputModule output) {
        output.writeSubtypeIndication(variable.getType());
        if (variable.getDefaultValue() != null) {
            writer.append(" := ");
            output.writeExpression(variable.getDefaultValue());
        }
    }

    public static void constantInterfaceSuffix(Constant constant, VhdlWriter writer, OutputModule output) {
        output.writeSubtypeIndication(constant.getType());
        if (constant.getDefaultValue() != null) {
            writer.append(" := ");
            output.writeExpression(constant.getDefaultValue());
        }
    }

    public static void fileInterfaceSuffix(FileObject file, VhdlWriter writer, OutputModule output) {
        output.writeSubtypeIndication(file.getType());
    }

    public static void slice(Slice slice, VhdlWriter writer, OutputModule output) {
        output.writeExpression(slice.getBase());
        writer.append('(');
        output.writeDiscreteRange(slice.getRange());
        writer.append(')');
    }

    public static void arrayElement(ArrayElement<?> arrayElement, VhdlWriter writer, OutputModule output) {
        output.writeExpression(arrayElement.getBase());
        writer.append('(');
        boolean first = true;
        for (Expression index : arrayElement.getIndices()) {
            if (first) {
                first = false;
            } else {
                writer.append(", ");
            }
            output.writeExpression(index);
        }
        writer.append(')');
    }

    public static void recordElement(RecordElement recordElement, VhdlWriter writer, OutputModule output) {
        output.writeExpression(recordElement.getBase());
        writer.append('.');
        writer.append(recordElement.getElement());
    }

    public static void attributeExpression(AttributeExpression expression, VhdlWriter writer, OutputModule output) {
        output.writeExpression(expression.getBase());
        writer.append('\'');
        writer.appendIdentifier(expression.getAttribute());
        if (expression.getParameter() != null) {
            writer.append('(');
            output.writeExpression(expression.getParameter());
            writer.append(')');
        }
    }
}
