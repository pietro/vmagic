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

package de.upb.hni.vmagic.parser.antlr;

import de.upb.hni.vmagic.AssociationElement;
import de.upb.hni.vmagic.DeclarativeRegion;
import de.upb.hni.vmagic.DiscreteRange;
import de.upb.hni.vmagic.NamedEntity;
import de.upb.hni.vmagic.RangeAttributeName;
import de.upb.hni.vmagic.RangeProvider;
import de.upb.hni.vmagic.Signature;
import de.upb.hni.vmagic.SubtypeDiscreteRange;
import de.upb.hni.vmagic.builtin.Standard;
import de.upb.hni.vmagic.object.Target;
import de.upb.hni.vmagic.declaration.Attribute;
import de.upb.hni.vmagic.declaration.Component;
import de.upb.hni.vmagic.declaration.Function;
import de.upb.hni.vmagic.expression.Expression;
import de.upb.hni.vmagic.expression.FunctionCall;
import de.upb.hni.vmagic.expression.Primary;
import de.upb.hni.vmagic.expression.TypeConversion;
import de.upb.hni.vmagic.libraryunit.Configuration;
import de.upb.hni.vmagic.libraryunit.Entity;
import de.upb.hni.vmagic.literal.EnumerationLiteral;
import de.upb.hni.vmagic.literal.PhysicalLiteral;
import de.upb.hni.vmagic.literal.StringLiteral;
import de.upb.hni.vmagic.object.AttributeExpression;
import de.upb.hni.vmagic.object.Signal;
import de.upb.hni.vmagic.object.Variable;
import de.upb.hni.vmagic.expression.VhdlObject;
import de.upb.hni.vmagic.parser.ParseError.Type;
import de.upb.hni.vmagic.type.EnumerationType;
import de.upb.hni.vmagic.type.IndexSubtypeIndication;
import de.upb.hni.vmagic.type.RangeSubtypeIndication;
import de.upb.hni.vmagic.type.SubtypeIndication;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.antlr.runtime.tree.Tree;

/**
 * Temporary name used during meta class generation.
 */
class TemporaryName {

    private final PartList parts = new PartList();
    private final MetaClassCreator mcc;
    private final Tree tree;

    public TemporaryName(MetaClassCreator mcc, Tree tree, StringLiteral stringLiteral) {
        this.mcc = mcc;
        this.tree = tree;
        parts.add(Part.createOperatorSymbol(stringLiteral));
    }

    public TemporaryName(MetaClassCreator mcc, Tree tree, String prefix) {
        this.mcc = mcc;
        this.tree = tree;
        parts.add(Part.createSelected(prefix));
    }

    public void addPart(Part part) {
        parts.add(part);
    }

    //used only for error reporting
    private String toIdentifier() {
        parts.resetContinuousIterator();

        StringBuilder selectedName = new StringBuilder();

        boolean first = true;
        for (Part part : parts) {
            if (part.getType() == Part.Type.SELECTED) {
                if (first) {
                    first = false;
                } else {
                    selectedName.append('.');
                }
                selectedName.append(part.getSuffix());
            } else {
                parts.currentIndex--;
                break;
            }
        }

        return (selectedName.length() == 0 ? "unknown" : selectedName.toString());
    }

    private <T> T resolve(DeclarativeRegion scope, Class<T> clazz) {
        parts.resetContinuousIterator();

        for (Part part : parts) {
            if (part.getType() == Part.Type.SELECTED) {
                Object obj = scope.getScope().resolve(part.getSuffix());

                if (clazz.isInstance(obj)) { //check first might also be a declarative region
                    //checked by surrounding if statement
                    @SuppressWarnings("unchecked")
                    T tmp = (T) obj;
                    return tmp;
                } else if (obj instanceof DeclarativeRegion) {
                    scope = (DeclarativeRegion) obj;
                } else {
                    return null;
                }
            }
        }

        return null;
    }

    public Entity toEntity(DeclarativeRegion scope) {
        Entity entity = resolve(scope, Entity.class);
        if (parts.finished() && entity != null) {
            return entity;
        } else {
            String identifier = toIdentifier();
            mcc.resolveError(tree, Type.UNKNOWN_ENTITY, identifier);

            if (mcc.getSettings().isCreateDummyObjects()) {
                Entity dummy = new Entity(identifier);
                //set parent to allow resolving of names in architectures
                dummy.setParent(scope);
                return dummy;
            } else {
                return null;
            }
        }
    }

    public Configuration toConfiguration(DeclarativeRegion scope) {
        Configuration configuration = resolve(scope, Configuration.class);
        if (parts.finished() && configuration != null) {
            return configuration;
        } else {
            String identifier = toIdentifier();
            mcc.resolveError(tree, Type.UNKNOWN_CONFIGURATION, identifier);

            if (mcc.getSettings().isCreateDummyObjects()) {
                return new Configuration(identifier, null, null);
            } else {
                return null;
            }
        }
    }

    //TODO: don't use subtype indication
    public SubtypeIndication toTypeMark(DeclarativeRegion scope) {
        SubtypeIndication type = resolve(scope, SubtypeIndication.class);
        if (parts.finished() && type != null) {
            return type;
        } else {
            String identifier = toIdentifier();
            mcc.resolveError(tree, Type.UNKNOWN_TYPE, identifier);

            if (mcc.getSettings().isCreateDummyObjects()) {
                return new EnumerationType(identifier);
            } else {
                return null;
            }
        }
    }

    public Component toComponent(DeclarativeRegion scope) {
        Component component = resolve(scope, Component.class);
        if (parts.finished() && component != null) {
            return component;
        } else {
            String identifier = toIdentifier();
            mcc.resolveError(tree, Type.UNKNOWN_COMPONENT, identifier);

            if (mcc.getSettings().isCreateDummyObjects()) {
                return new Component(identifier);
            } else {
                return null;
            }
        }
    }

    public Signal toSignal(DeclarativeRegion scope) {
        Signal signal = resolve(scope, Signal.class);
        if (parts.finished() && signal != null) {
            return signal;
        } else {
            String identifier = toIdentifier();
            mcc.resolveError(tree, Type.UNKNOWN_SIGNAL, identifier);

            if (mcc.getSettings().isCreateDummyObjects()) {
                return new Signal(identifier, null);
            } else {
                return null;
            }
        }
    }

    private String toSelectedName() {
        StringBuilder selectedName = new StringBuilder();

        boolean first = true;
        for (Part part : parts) {
            if (part.getType() == Part.Type.SELECTED) {
                if (first) {
                    first = false;
                } else {
                    selectedName.append('.');
                }
                selectedName.append(part.getSuffix());
            } else {
                return null;
            }
        }

        return (selectedName.length() == 0 ? null : selectedName.toString());
    }

    //TODO: don't use string return value
    public String toProcedure(DeclarativeRegion scope) {
        return toSelectedName();
    }

    //TODO: don't use a String
    public String toFunction(DeclarativeRegion scope) {
        return toSelectedName();
    }

    private <T extends VhdlObject<T>> VhdlObject<T> addTargetParts(VhdlObject<T> obj, boolean strict) {
        for (Part part : parts) {
            switch (part.getType()) {
                case ASSOCIATION:
                    List<Expression> indices = new ArrayList<Expression>();
                    for (AssociationElement element : part.getAssociationList()) {
                        indices.add(element.getActual());
                    }
                    obj = obj.getArrayElement(indices);
                    break;

                case INDEXED:
                    obj = obj.getArrayElement(part.getIndices());
                    break;

                case SELECTED:
                    obj = obj.getRecordElement(part.getSuffix());
                    break;

                case SLICE:
                    obj = obj.getSlice(part.getRange());
                    break;

                default:
                    if (strict) {
                        return null;
                    }
                    break;
            }
        }
        return obj;
    }

    public <T extends VhdlObject<T>> Target<T> toTarget(DeclarativeRegion scope, Class<T> clazz) {
        VhdlObject<T> obj = resolve(scope, clazz);
        if (obj == null) {
            return null;
        }

        obj = addTargetParts(obj, true);

        if (obj instanceof Target) {
            //TODO: find typesafe solution
            @SuppressWarnings("unchecked")
            Target<T> tmp = (Target<T>) obj;
            return tmp;
        } else {
            return null;
        }
    }

    public Target<Signal> toSignalTarget(DeclarativeRegion scope) {
        Target<Signal> target = toTarget(scope, Signal.class);
        if (target != null) {
            return target;
        }

        String identifier = toIdentifier();
        mcc.resolveError(tree, Type.UNKNOWN_SIGNAL_ASSIGNMENT_TARGET, identifier);

        if (mcc.getSettings().isCreateDummyObjects()) {
            Signal dummy = new Signal(identifier, null);
            VhdlObject<Signal> signal = addTargetParts(dummy, false);

            //TODO: find typesafe solution
            @SuppressWarnings("unchecked")
            Target<Signal> tmp = (Target<Signal>) signal;
            return tmp;
        } else {
            return null;
        }
    }

    public Target<Variable> toVariableTarget(DeclarativeRegion scope) {
        Target<Variable> target = toTarget(scope, Variable.class);
        if (target != null) {
            return target;
        }

        String identifier = toIdentifier();
        mcc.resolveError(tree, Type.UNKNOWN_VARIABLE_ASSIGNMENT_TARGET, identifier);

        if (mcc.getSettings().isCreateDummyObjects()) {
            Variable dummy = new Variable(identifier, null);
            VhdlObject<Variable> variable = addTargetParts(dummy, false);

            //TODO: find typesafe solution
            @SuppressWarnings("unchecked")
            Target<Variable> tmp = (Target<Variable>) variable;
            return tmp;
        } else {
            return null;
        }
    }

    public String toUseClauseName(DeclarativeRegion scope) {
        return toSelectedName();
    }

    private Primary addPrimaryParts(VhdlObject<?> obj) {
        for (Part part : parts) {
            switch (part.getType()) {
                case ASSOCIATION:
                    List<Expression> indices = new ArrayList<Expression>();
                    for (AssociationElement element : part.getAssociationList()) {
                        indices.add(element.getActual());
                    }
                    obj = obj.getArrayElement(indices);
                    break;

                case INDEXED:
                    obj = obj.getArrayElement(part.getIndices());
                    break;

                case SELECTED:
                    obj = obj.getRecordElement(part.getSuffix());
                    break;

                case SLICE:
                    obj = obj.getSlice(part.getRange());
                    break;

                case ATTRIBUTE:
                    //TODO: remove dummy attribute
                    Attribute attrb = new Attribute(part.getIdentifier(), null);
                    obj = new AttributeExpression<VhdlObject>(obj, attrb, part.getExpression());
            }
        }
        return obj;
    }

    public Primary toPrimary(DeclarativeRegion scope, boolean inElementAssociation) {
        //don't try to resolve simple names in choices inside an aggregate
        if (inElementAssociation) {
            if (parts.remainingParts() == 1) {
                Part part = parts.iterator().next();

                if (part.getType() == Part.Type.SELECTED) {
                    //TODO: don't use dummy signal
                    return new Signal(part.getSuffix(), Standard.STRING);
                }
            }
        }

        VhdlObject<?> obj = resolve(scope, VhdlObject.class);
        if (obj != null) {
            return addPrimaryParts(obj);
        }

        SubtypeIndication type = resolve(scope, SubtypeIndication.class);
        if (type instanceof NamedEntity) {
            if (parts.remainingParts() == 1) {
                Part part = parts.iterator().next();

                switch (part.getType()) {
                    case ATTRIBUTE:
                        //TODO: remove dummy objects
                        String identifier = ((NamedEntity) type).getIdentifier();
                        Signal dummy = new Signal(identifier, null);
                        Attribute dummyAttrb = new Attribute(part.getIdentifier(), null);
                        return new AttributeExpression<Signal>(dummy, dummyAttrb, part.getExpression());

                    case ASSOCIATION:
                        if (part.getAssociationList().size() == 1) {
                            AssociationElement ae = part.getAssociationList().get(0);
                            if (ae.getActual() != null && ae.getFormal() == null) {
                                return new TypeConversion(type, ae.getActual());
                            }
                        }
                        break;

                    case INDEXED:
                        if (part.getIndices().size() == 1) {
                            return new TypeConversion(type, part.getIndices().get(0));
                        }
                        break;
                }
            }
        }

        Function function = resolve(scope, Function.class);
        if (function != null) {
            FunctionCall call = new FunctionCall(function);

            if (parts.remainingParts() == 0) {
                return call;
            } else {
                Part part = parts.iterator().next();

                if (!function.getParameters().isEmpty()) {
                    switch (part.getType()) {
                        case ASSOCIATION:
                            call.getParameters().addAll(part.getAssociationList());
                            break;

                        case INDEXED:
                            for (Expression index : part.getIndices()) {
                                call.getParameters().add(new AssociationElement(index));
                            }
                            break;

                        default:
                            return null;
                    }
                }
                return call;
            }
        }

        parts.resetContinuousIterator();
        if (parts.remainingParts() == 1) {
            Part part = parts.iterator.next();
            switch (part.getType()) {
                case SELECTED:
                    //TODO: check overloading
                    Object o = scope.getScope().resolve(part.getSuffix());
                    if (o instanceof EnumerationLiteral) {
                        return (EnumerationLiteral) o;
                    } else if (o instanceof PhysicalLiteral) {
                        return (PhysicalLiteral) o;
                    }
                    break;

                case OPERATOR_SYMBOL:
                    //TODO: this isn't always an operator symbol
                    return part.getLiteral();
            }
        }

        mcc.resolveError(tree, Type.UNKNOWN_OTHER, toIdentifier());
        if (mcc.getSettings().isCreateDummyObjects()) {
            Signal dummy = new Signal(toIdentifier(), null);
            return addPrimaryParts(dummy);
        } else {
            return null;
        }
    }

    private RangeAttributeName toRangeAttributeName(String prefix) {
        if (parts.remainingParts() != 1) {
            return null;
        }

        Part part = parts.iterator().next();

        if (part.getType() == Part.Type.ATTRIBUTE) {
            //TODO: check other parameters of attribute part

            if (part.getIdentifier().equalsIgnoreCase("range")) {
                return new RangeAttributeName(prefix, RangeAttributeName.Type.RANGE, part.getExpression());
            } else if (part.getIdentifier().equalsIgnoreCase("reverse_range")) {
                return new RangeAttributeName(prefix, RangeAttributeName.Type.REVERSE_RANGE, part.getExpression());
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public RangeProvider toRangeName(DeclarativeRegion scope) {
        RangeProvider range = resolveRangeName(scope);
        if (range != null) {
            return range;
        }

        String identifier = toIdentifier();
        mcc.resolveError(tree, Type.UNKNOWN_OTHER, identifier);
        if (mcc.getSettings().isCreateDummyObjects()) {
            RangeAttributeName name = toRangeAttributeName(identifier);
            if (name != null) {
                return name;
            } else {
                return new RangeAttributeName(identifier, RangeAttributeName.Type.RANGE);
            }
        } else {
            return null;
        }
    }

    private RangeProvider resolveRangeName(DeclarativeRegion scope) {
        VhdlObject obj = resolve(scope, VhdlObject.class);
        if (obj != null) {
            return toRangeAttributeName(obj.getIdentifier());
        }

        SubtypeIndication subtype = resolve(scope, SubtypeIndication.class);
        if (subtype instanceof NamedEntity) {
            String identifier = ((NamedEntity) subtype).getIdentifier();
            return toRangeAttributeName(identifier);
        } else {
            return null;
        }
    }

    public DiscreteRange toDiscreteRange(DeclarativeRegion scope) {
        RangeProvider range = resolveRangeName(scope);
        if (range != null) {
            return range;
        }

        SubtypeIndication subtype = resolve(scope, SubtypeIndication.class);
        if (subtype != null && parts.finished()) {
            return new SubtypeDiscreteRange(subtype);
        }

        String identifier = toIdentifier();
        mcc.resolveError(tree, Type.UNKNOWN_OTHER, identifier);

        if (mcc.getSettings().isCreateDummyObjects()) {
            if (parts.remainingParts() == 1) {
                Part part = parts.iterator.next();

                if (part.getType() == Part.Type.ATTRIBUTE) {
                    if (part.getIdentifier().equalsIgnoreCase("range")) {
                        return new RangeAttributeName(identifier, RangeAttributeName.Type.RANGE);
                    } else if (part.getIdentifier().equalsIgnoreCase("reverse_range")) {
                        return new RangeAttributeName(identifier, RangeAttributeName.Type.REVERSE_RANGE);
                    }
                }
            }

            return new SubtypeDiscreteRange(new EnumerationType(identifier));
        } else {
            return null;
        }
    }

    public DiscreteRange toDiscreteRange(DeclarativeRegion scope, List<DiscreteRange> indices) {
        SubtypeIndication type = toTypeMark(scope);

        if (type != null) {
            return new SubtypeDiscreteRange(new IndexSubtypeIndication(type, indices));
        } else {
            return null;
        }
    }

    public DiscreteRange toDiscreteRange(DeclarativeRegion scope, RangeProvider range) {
        SubtypeIndication type = toTypeMark(scope);

        if (type != null) {
            return new SubtypeDiscreteRange(new RangeSubtypeIndication(type, range));
        } else {
            return null;
        }
    }

    public static Part createIndexedOrSlicePart(TemporaryName name, DeclarativeRegion scope) {
        SubtypeIndication type = name.resolve(scope, SubtypeIndication.class);
        if (type != null && name.parts.finished()) {
            return Part.createSlice(new SubtypeDiscreteRange(type));
        }

        RangeProvider range = name.resolveRangeName(scope);
        if (range != null) {
            return Part.createSlice(range);
        }

        return Part.createIndexed(Collections.<Expression>singletonList(name.toPrimary(scope, false)));
    }

    public static class Part {

        private final Type type;
        private StringLiteral literal;
        private String identifier;
        private Expression expression;
        private Signature signature;
        private List<Expression> indices;
        private String suffix;
        private DiscreteRange range;
        private List<AssociationElement> associationList;

        private Part(Type type) {
            this.type = type;
        }

        public static Part createOperatorSymbol(StringLiteral literal) {
            Part part = new Part(Type.OPERATOR_SYMBOL);
            part.literal = literal;
            return part;
        }

        public static Part createAttribute(String identifier, Expression expression, Signature signature) {
            Part part = new Part(Type.ATTRIBUTE);
            part.identifier = identifier;
            part.expression = expression;
            part.signature = signature;
            return part;
        }

        public static Part createIndexed(List<Expression> indices) {
            Part part = new Part(Type.INDEXED);
            part.indices = indices;
            return part;
        }

        public static Part createSelected(String suffix) {
            Part part = new Part(Type.SELECTED);
            part.suffix = suffix;
            return part;
        }

        public static Part createSlice(DiscreteRange range) {
            Part part = new Part(Type.SLICE);
            part.range = range;
            return part;
        }

        public static Part createAssociation(List<AssociationElement> associationList) {
            Part part = new Part(Type.ASSOCIATION);
            part.associationList = associationList;
            return part;
        }

        public Type getType() {
            return type;
        }

        public StringLiteral getLiteral() {
            return literal;
        }

        public Expression getExpression() {
            return expression;
        }

        public String getIdentifier() {
            return identifier;
        }

        public Signature getSignature() {
            return signature;
        }

        public List<Expression> getIndices() {
            return indices;
        }

        public String getSuffix() {
            return suffix;
        }

        public List<AssociationElement> getAssociationList() {
            return associationList;
        }

        public DiscreteRange getRange() {
            return range;
        }

        public enum Type {

            OPERATOR_SYMBOL,
            ATTRIBUTE,
            INDEXED,
            SELECTED,
            SLICE,
            ASSOCIATION
        }
    }

    private static class PartList implements Iterable<Part> {

        private int currentIndex;
        private final Iterator<Part> iterator = new ContinuousIterator();
        private final List<Part> parts = new ArrayList<Part>();

        public int remainingParts() {
            return parts.size() - currentIndex;
        }

        public void add(Part part) {
            parts.add(part);
        }

        public void resetContinuousIterator() {
            currentIndex = 0;
        }

        public Iterator<Part> iterator() {
            return iterator;
        }

        public boolean finished() {
            return remainingParts() == 0;
        }

        private class ContinuousIterator implements Iterator<Part> {

            public boolean hasNext() {
                return remainingParts() > 0;
            }

            public Part next() {
                if (hasNext()) {
                    return parts.get(currentIndex++);
                } else {
                    throw new NoSuchElementException();
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        }
    }
}
