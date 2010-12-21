/*
 * Copyright 2008, 2009, 2010 University of Paderborn
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

tree grammar MetaClassCreator;

options {
    tokenVocab = VhdlAntlr;
    ASTLabelType = CommonTree;
    superClass = AbstractMetaClassCreator;
}

@header {
    package de.upb.hni.vmagic.parser.antlr;

    import de.upb.hni.vmagic.*;
    import de.upb.hni.vmagic.annotation.*;
    import de.upb.hni.vmagic.parser.*;
    import de.upb.hni.vmagic.parser.annotation.*;
    import de.upb.hni.vmagic.concurrent.*;
    import de.upb.hni.vmagic.configuration.*;
    import de.upb.hni.vmagic.declaration.*;
    import de.upb.hni.vmagic.expression.*;
    import de.upb.hni.vmagic.libraryunit.*;
    import de.upb.hni.vmagic.literal.*;
    import de.upb.hni.vmagic.object.*;
    import de.upb.hni.vmagic.statement.*;
    import de.upb.hni.vmagic.type.*;
}

@members {
    public MetaClassCreator(TreeNodeStream input, VhdlParserSettings settings,
            RootDeclarativeRegion rootScope, LibraryDeclarativeRegion libraryScope) {
        super(input, settings, rootScope, libraryScope);
    }

    private boolean inElementAssociation = false;
}

abstract_literal returns [AbstractLiteral value]
@after { addAnnotations($value, $start); }
    :   DECIMAL_LITERAL { $value = new DecimalLiteral($DECIMAL_LITERAL.text); }
    |   BASED_LITERAL   { $value = new BasedLiteral($BASED_LITERAL.text); }
    ;

access_type_definition[String ident] returns [AccessType value]
@after { addAnnotations($value, $start); }
    :   ^( ACCESS subtype_indication )
        { $value = new AccessType($ident, $subtype_indication.value); }
    ;

adding_operator returns [ExpressionType value]
    :   PLUS      { $value = ExpressionType.ADD; }
    |   MINUS     { $value = ExpressionType.SUB; }
    |   AMPERSAND { $value = ExpressionType.CONCAT; }
    ;

aggregate returns [Aggregate value = new Aggregate()]
@init { boolean hasChoices; }
@after { addAnnotations($value, $start); }
    :   ^( AGGREGATE
            (
                {
                    hasChoices = false;
                    inElementAssociation = true;
                }
                ( choices { hasChoices = true;} )?
                { inElementAssociation = false; }
                expression
                {
                    if (hasChoices) {
                        $value.createAssociation($expression.value, $choices.value);
                    } else {
                        $value.createAssociation($expression.value);
                    }
                }
            )+
        )
    ;

//TODO: don't use name.text
alias_declaration returns [Alias value]
@after { addAnnotations($value, $start); }
    :   ^( ALIAS
            alias_designator subtype_indication? name
            { $value = new Alias($alias_designator.value, $subtype_indication.value, $name.text); }
            ( signature { $value.setSignature($signature.value); } )?
        )
        
    ;

//TODO: handle different literal types?
alias_designator returns [String value]
@after { $value = $alias_designator.text; }
    :   identifier
    |   CHARACTER_LITERAL
    |   STRING_LITERAL
    ;

allocator returns [Expression value]
@after { addAnnotations($value, $start); }
    :   ^( NEW
            (
                    subtype_indication
                    { $value = new SubtypeIndicationAllocator($subtype_indication.value); }
                |   qualified_expression
                    { $value = new QualifiedExpressionAllocator($qualified_expression.value); }
            )
        )
    ;

architecture_body returns [Architecture value]
@init { DeclarativeRegion oldScope = currentScope; }
@after {
    currentScope = oldScope;
    addAnnotations($value, $architecture_body.start);
}
    :   ^( ARCHITECTURE
            identifier entity=name
            {
                $value = new Architecture($identifier.text, $entity.value.toEntity(currentScope));
                $value.setParent(oldScope);
                currentScope = $value;
            }
            ( d=block_declarative_item { $value.getDeclarations().add($d.value); } )*
            ( s=concurrent_statement { $value.getStatements().add($s.value); } )*
        )
    ;

assertion_statement[String label] returns [AssertionStatement value]
@after { addAnnotations($value, $start); }
    :   ^( ASSERT
            condition=expression          { $value = new AssertionStatement($condition.value); }
            { $value.setLabel($label); }
            ^( REPORT rep=expression? )   { $value.setReportedExpression($rep.value); }
            ^( SEVERITY sev=expression? ) { $value.setSeverity($sev.value); }
        )
    ;

//TODO: check
association_element returns [AssociationElement value]
@after { addAnnotations($value, $start); }
    :   formal=name
        (
                actual=expression { $value = new AssociationElement($formal.text, $actual.value); }
            |   OPEN              { $value = new AssociationElement($formal.text, null); }
        )
    |   actual=expression { $value = new AssociationElement($actual.value); }
    |   OPEN              { $value = new AssociationElement(null); }
    ;

association_list returns [List<AssociationElement> value = new ArrayList<AssociationElement>()]
    :   ^( ASSOCIATION_LIST 
            ( e=association_element { $value.add($e.value); } )+
        )
    ;

attribute_declaration returns [Attribute value]
@after { addAnnotations($value, $start); }
    :   ^( ATTRIBUTE_DECLARATION identifier type_mark=name )
        { $value = new Attribute($identifier.text, $type_mark.value.toTypeMark(currentScope)); }
    ;

//TODO: remove dummy attribute
attribute_designator returns [Attribute value]
@after { addAnnotations($value, $start); }
    :   identifier { $value = new Attribute($identifier.text, UnresolvedType.NO_NAME); }
    ;

attribute_specification returns [AttributeSpecification value]
@after { addAnnotations($value, $start); }
    :   ^( ATTRIBUTE_SPECIFICATION attribute_designator entity_name_list entity_class expression )
        {
            $value = new AttributeSpecification($attribute_designator.value,
                $entity_name_list.value, $entity_class.value, $expression.value);
        }
    ;

block_configuration returns [AbstractBlockConfiguration value]
@after { addAnnotations($value, $start); }
    :   ^( BLOCK_CONFIGURATION
            block_specification { $value = $block_specification.value; }
            ( uc=use_clause { $value.getUseClauses().add($uc.value); } )*
            ( ci=configuration_item { $value.getConfigurationItems().add($ci.value); } )*
        )
    ;

block_declarative_item returns [BlockDeclarativeItem value]
    :   subprogram_declaration      { $value = $subprogram_declaration.value; }
    |   subprogram_body             { $value = $subprogram_body.value; }
    |   type_declaration            { $value = $type_declaration.value; }
    |   subtype_declaration         { $value = $subtype_declaration.value; }
    |   constant_declaration        { $value = $constant_declaration.value; }
    |   signal_declaration          { $value = $signal_declaration.value; }
    |   variable_declaration        { $value = $variable_declaration.value; }
    |   file_declaration            { $value = $file_declaration.value; }
    |   alias_declaration           { $value = $alias_declaration.value; }
    |   component_declaration       { $value = $component_declaration.value; }
    |   attribute_specification     { $value = $attribute_specification.value; }
    |   attribute_declaration       { $value = $attribute_declaration.value; }
    |   configuration_specification { $value = $configuration_specification.value; }
    |   disconnection_specification { $value = $disconnection_specification.value; }
    |   use_clause                  { $value = $use_clause.value; }
    |   group_template_declaration  { $value = $group_template_declaration.value; }
    |   group_declaration           { $value = $group_declaration.value; }
    ;

//TODO: check
block_specification returns [AbstractBlockConfiguration value]
@after { addAnnotations($value, $start); }
    : name
        {
            Architecture dummy = new Architecture($name.text, new Entity("##dummy##"));
            $value = new ArchitectureConfiguration(dummy);
        }
    ;

block_statement[String label] returns [BlockStatement value]
@init { DeclarativeRegion oldScope = currentScope; }
@after {
    currentScope = oldScope;
    addAnnotations($value, $start);
}
    :   ^( BLOCK_STATEMENT
            {
                $value = new BlockStatement($label);
                $value.setParent(oldScope);
                currentScope = $value;
            }
            ( guard_expression=expression { $value.setGuardExpression($guard_expression.value); } )?
            (
                IS
                {
                    Annotations.putAnnotation($value, OptionalIsFormat.class,
                        new OptionalIsFormat(true));
                }
            )?
            ( gc=generic_clause { $value.getGeneric().addAll($gc.value); } )?
            ( gma=generic_map_aspect { $value.getGenericMap().addAll($gma.value); } )?
            ( pc=port_clause { $value.getPort().addAll($pc.value); } )?
            ( pma=port_map_aspect { $value.getPortMap().addAll($pma.value); } )?
            ( bdi=block_declarative_item { $value.getDeclarations().add($bdi.value); } )*
            ( cs=concurrent_statement { $value.getStatements().add($cs.value); } )*
        )
    ;

case_statement[String label] returns [CaseStatement value]
@after { addAnnotations($value, $start); }
    :   ^( CASE 
            expression { $value = new CaseStatement($expression.value); }
            { $value.setLabel($label); }
            (
                choices 
                { CaseStatement.Alternative alt = $value.createAlternative($choices.value); }
                ( sequential_statement { alt.getStatements().add($sequential_statement.value); } )*
            )+
        )
    ;

choice returns [Choice value]
    :   discrete_range { $value = $discrete_range.value; }
    |   expression     { $value = $expression.value; }
    |   OTHERS         { $value = Choices.OTHERS; }
    ;

choices returns [List<Choice> value = new ArrayList<Choice>()]
    :   ^( CHOICES ( choice { $value.add($choice.value); } )+ )
    ;

component_configuration returns [ComponentConfiguration value]
@after { addAnnotations($value, $start); }
    :   ^( COMPONENT_CONFIGURATION
            component_specification
            { $value = new ComponentConfiguration($component_specification.value); }
            ( ea=entity_aspect { $value.setEntityAspect($ea.value); } )?
            ( gma=generic_map_aspect { $value.getGenericMap().addAll($gma.value); } )?
            ( pma=port_map_aspect { $value.getPortMap().addAll($pma.value); } )?
            ( bc=block_configuration { $value.setBlockConfiguration($bc.value); } )?
        )
    ;

component_declaration returns [Component value]
@init { DeclarativeRegion oldScope = currentScope; }
@after {
    currentScope = oldScope;
    addAnnotations($value, $start);
}
    :   ^( COMPONENT 
            identifier
            {
                $value = new Component($identifier.text);
                $value.setParent(oldScope);
                currentScope = $value;
            }
            (
                IS
                {
                    Annotations.putAnnotation($value, OptionalIsFormat.class,
                        new OptionalIsFormat(true));
                }
            )?
            ( gc=generic_clause { $value.getGeneric().addAll($gc.value); } )?
            ( pc=port_clause { $value.getPort().addAll($pc.value); } )?
        )
    ;

component_instantiation_statement[String label] returns [AbstractComponentInstantiation value]
@after { addAnnotations($value, $start); }
    :   ^( COMPONENT_INSTANTIATION_STATEMENT
            instantiated_unit[label] { $value = $instantiated_unit.value; }
            ( gma=generic_map_aspect { $value.getGenericMap().addAll($gma.value); } )?
            ( pma=port_map_aspect { $value.getPortMap().addAll($pma.value); } )?
        )
    ;

component_specification returns [ComponentSpecification value]
@init { List<String> identifiers = new ArrayList<String>(); }
@after { addAnnotations($value, $start); }
    :   ^( INSTANTIATION_LIST ( identifier { identifiers.add($identifier.text); } )+ )
        comp1=name
        {
            $value = ComponentSpecification.create($comp1.value.toComponent(currentScope),
                identifiers);
        }
    |   OTHERS comp2=name
        { $value = ComponentSpecification.createOthers($comp2.value.toComponent(currentScope)); }
    |   ALL comp3=name
        { $value = ComponentSpecification.createAll($comp3.value.toComponent(currentScope)); }
    ;

concurrent_assertion_statement[String label] returns [ConcurrentAssertionStatement value]
@after { addAnnotations($value, $start); }
    :   ^( ASSERT
            condition=expression
            {
                $value = new ConcurrentAssertionStatement($condition.value);
                $value.setLabel($label);
            }
            ^( REPORT rep=expression? ) { $value.setReportedExpression($rep.value); }
            ^( SEVERITY sev=expression? ) { $value.setSeverity($sev.value); }
        )
    ;

concurrent_procedure_call_statement[String label] returns [ConcurrentProcedureCall value]
@after { addAnnotations($value, $start); }
    :   ^( PROCEDURE_CALL
            name
            {
                $value = new ConcurrentProcedureCall($name.value.toProcedure(currentScope));
                $value.setLabel($label);
            }
            (
                parameters=association_list
                { $value.getParameters().addAll($parameters.value); }
            )?
        )
    ;

concurrent_signal_assignment_statement[String label] returns [AbstractPostponableConcurrentStatement value]
    :   ^( CONDITIONAL_SIGNAL_ASSIGNMENT_STATEMENT csa=conditional_signal_assignment )
        {
            $value = $csa.value;
            $value.setLabel($label);
        }
    |   ^( SELECTED_SIGNAL_ASSIGNMENT_STATEMENT ssa=selected_signal_assignment)
        {
            $value = $ssa.value;
            $value.setLabel($label);
        }
    ;

conditional_signal_assignment returns [ConditionalSignalAssignment value]
@init {
    boolean isGuarded = false;
}
@after { addAnnotations($value, $start); }
    :   ts=target_signal ( GUARDED { isGuarded = true; } )?
        delay_mechanism? cw=conditional_waveforms
        {
            $value = new ConditionalSignalAssignment($ts.value, $cw.value);
            $value.setGuarded(isGuarded);
            $value.setDelayMechanism($delay_mechanism.value);
        }
    ;

concurrent_statement returns [ConcurrentStatement value]
    :   ^( LABEL_STATEMENT identifier
            (
                    s1=concurrent_statement_optional_label[$identifier.text]
                    { $value = $s1.value; }
                |   s2=concurrent_statement_with_label[$identifier.text]
                    { $value = $s2.value; }
            )
        )
    |   s3=concurrent_statement_optional_label[null]
        { $value = $s3.value; }
    ;

concurrent_statement_optional_label[String label] returns [ConcurrentStatement value]
    :   statement1=concurrent_statement_optional_label2[$label]
        { $value = $statement1.value; }
    |   ^( POSTPONED statement2=concurrent_statement_optional_label2[$label] )
        {
            if ($statement2.value != null) {
                $statement2.value.setPostponed(true);
            }
            $value = $statement2.value;
        }
    ;

concurrent_statement_optional_label2[String label] returns [AbstractPostponableConcurrentStatement value]
    :   ps=process_statement[$label]                        { $value = $ps.value; }
    |   cpcs=concurrent_procedure_call_statement[$label]    { $value = $cpcs.value; }
    |   cas=concurrent_assertion_statement[$label]          { $value = $cas.value; }
    |   csas=concurrent_signal_assignment_statement[$label] { $value = $csas.value; }
    ;

concurrent_statement_with_label[String label] returns [ConcurrentStatement value]
    :   bs=block_statement[$label]                    { $value = $bs.value; }
    |   cis=component_instantiation_statement[$label] { $value = $cis.value; }
    |   gs=generate_statement[$label]                 { $value = $gs.value; }
    ;

//TODO: check
conditional_waveforms returns [List<ConditionalSignalAssignment.ConditionalWaveformElement> value = new ArrayList<ConditionalSignalAssignment.ConditionalWaveformElement>()]
    :   ^( CONDITIONAL_WAVEFORMS
            (
                w=waveform
                { ConditionalSignalAssignment.ConditionalWaveformElement element = new ConditionalSignalAssignment.ConditionalWaveformElement($w.value); }
                ( expression { element.setCondition($expression.value); } )?
                { $value.add(element); }
            )*
        )
    ;

configuration_declaration returns [Configuration value]
@init {
    List<ConfigurationDeclarativeItem> declarations =
        new ArrayList<ConfigurationDeclarativeItem>();
}
@after { addAnnotations($value, $start); }
    :   ^( CONFIGURATION identifier entity=name
            ( cdi=configuration_declarative_item { declarations.add($cdi.value); } )*
            bc=block_configuration
            {
                $value = new Configuration($identifier.text, $entity.value.toEntity(currentScope), $bc.value);
                $value.getDeclarations().addAll(declarations);
            }
        )
    ;

configuration_declarative_item returns [ConfigurationDeclarativeItem value]
    :   use_clause              { $value = $use_clause.value; }
    |   attribute_specification { $value = $attribute_specification.value; }
    |   group_declaration       { $value = $group_declaration.value; }
    ;

configuration_item returns [ConfigurationItem value]
    :   block_configuration     { $value = $block_configuration.value; }
    |   component_configuration { $value = $component_configuration.value; }
    ;

configuration_specification returns [ConfigurationSpecification value]
@after { addAnnotations($value, $start); }
    :   ^( CONFIGURATION_SPECIFICATION 
            cs=component_specification { $value = new ConfigurationSpecification($cs.value); }
            ( ea=entity_aspect { $value.setEntityAspect($ea.value); } )?
            ( gma=generic_map_aspect { $value.getGenericMap().addAll($gma.value); } )?
            ( pma=port_map_aspect { $value.getPortMap().addAll($pma.value); } )?
        )
    ;

constant_declaration returns [ConstantDeclaration value]
@after { addAnnotations($value, $start); }
    :   ^( CONSTANT identifier_list si=subtype_indication def=expression? )
        {
            List<Constant> constants = new ArrayList<Constant>();
            for (String identifier : $identifier_list.value) {
                constants.add(new Constant(identifier, $si.value, $def.value));
            }
            $value = new ConstantDeclaration(constants);
        }
    ;

constrained_array_definition[String ident] returns [ConstrainedArray value]
@after { addAnnotations($value, $start); }
    :   ^( CONSTRAINED_ARRAY_DEFINITION ic=index_constraint elementType=subtype_indication )
        { $value = new ConstrainedArray($ident, $elementType.value, $ic.value); }
    ;

context_item returns [LibraryUnit value]
    :   library_clause { $value = $library_clause.value; }
    |   use_clause     { $value = $use_clause.value; }
    ;

delay_mechanism returns [DelayMechanism value]
    :   TRANSPORT { $value = DelayMechanism.TRANSPORT; }
    |   ^( INERTIAL { $value = DelayMechanism.INERTIAL; }
            ( time=expression { $value = DelayMechanism.REJECT_INERTIAL($time.value); } )?
        )
    ;

design_file returns [VhdlFile value]
@init {
    $value = new VhdlFile();
    libraryScope.getFiles().add($value);
    currentScope = $value;
}
    :   (
            ( 
                context_item
                {
                    //TODO: replace hack
                    if ($context_item.value instanceof LibraryClause) {
                        LibraryClause lc = (LibraryClause)$context_item.value;
                        for (String library : lc.getLibraries()) {
                            if (library.equalsIgnoreCase("ieee")) {
                                rootScope.getLibraries().add(de.upb.hni.vmagic.builtin.Libraries.IEEE);
                            }
                        }
                    }
                    $value.getElements().add($context_item.value);
                }
            )*
            library_unit   { $value.getElements().add($library_unit.value); }
        )* EOF
    ;

designator
    :   identifier
    |   STRING_LITERAL
    ;

direction returns [Range.Direction value]
    :   TO     { $value = Range.Direction.TO; }
    |   DOWNTO { $value = Range.Direction.DOWNTO; }
    ;

disconnection_specification returns [DisconnectionSpecification value]
@after { addAnnotations($value, $start); }
    :   ^( DISCONNECT sl=signal_list type_mark=name after=expression )
        { $value = new DisconnectionSpecification($sl.value, $type_mark.value.toTypeMark(currentScope), $after.value); }
    ;

discrete_range returns [DiscreteRange value]
    :   ^( direction left=expression right=expression)
        { $value = new Range($left.value, $direction.value, $right.value); }
    |   ^( DISCRETE_RANGE
            type_mark_or_range_attribute=name
            (
                    range_constraint
                    {
                        $value = $type_mark_or_range_attribute.value.toDiscreteRange(
                            currentScope, $range_constraint.value);
                    }
                |   index_constraint
                    {
                        $value = $type_mark_or_range_attribute.value.toDiscreteRange(
                            currentScope, $index_constraint.value);
                    }
                |
                    { $value = $type_mark_or_range_attribute.value.toDiscreteRange(currentScope); }
            )
        )
    ;

//TODO: don't use dummy objects
entity_aspect returns [EntityAspect value]
    :   ^( ENTITY entity=name architecture=identifier? )
        {
            Entity dummyEntity = new Entity($entity.text);
            if ($architecture.text != null) {
                //TODO: remove dummy architecture
                Architecture dummy = new Architecture($architecture.text, dummyEntity);
                $value = EntityAspect.architecture(dummy);
            } else {
                $value = EntityAspect.entity(dummyEntity);
            }
        }
    |   ^( CONFIGURATION configuration=name )
        {
            Configuration dummy = new Configuration($configuration.text, null, null);
            $value = EntityAspect.configuration(dummy);
        }
    |   OPEN
        { $value = EntityAspect.OPEN; }
    ;

entity_class returns [EntityClass value]
    :   ENTITY        { $value = EntityClass.ENTITY; }
    |   ARCHITECTURE  { $value = EntityClass.ARCHITECTURE; }
    |   CONFIGURATION { $value = EntityClass.CONFIGURATION; }
    |   PROCEDURE     { $value = EntityClass.PROCEDURE; }
    |   FUNCTION      { $value = EntityClass.FUNCTION; }
    |   PACKAGE       { $value = EntityClass.PACKAGE; }
    |   TYPE          { $value = EntityClass.TYPE; }
    |   SUBTYPE       { $value = EntityClass.SUBTYPE; }
    |   CONSTANT      { $value = EntityClass.CONSTANT; }
    |   SIGNAL        { $value = EntityClass.SIGNAL; }
    |   VARIABLE      { $value = EntityClass.VARIABLE; }
    |   COMPONENT     { $value = EntityClass.COMPONENT; }
    |   LABEL         { $value = EntityClass.LABEL; }
    |   LITERAL       { $value = EntityClass.LITERAL; }
    |   UNITS         { $value = EntityClass.UNITS; }
    |   GROUP         { $value = EntityClass.GROUP; }
    |   FILE          { $value = EntityClass.FILE; }
    ;

entity_declaration returns [Entity value]
@init { DeclarativeRegion oldScope = currentScope; }
@after {
    currentScope = oldScope;
    addAnnotations($value, $start);
}
    :   ^( ENTITY
            identifier
            {
                $value = new Entity($identifier.text);
                $value.setParent(oldScope);
                currentScope = $value;
            }
            ( gc=generic_clause { $value.getGeneric().addAll($gc.value); } )?
            ( pc=port_clause { $value.getPort().addAll($pc.value); } )?
            ( edi=entity_declarative_item { $value.getDeclarations().add($edi.value); } )*
            ( es=entity_statement { $value.getStatements().add($es.value); } )*
        )
    ;

entity_declarative_item returns [EntityDeclarativeItem value]
    :   subprogram_declaration      { $value = $subprogram_declaration.value; }
    |   subprogram_body             { $value = $subprogram_body.value; }
    |   type_declaration            { $value = $type_declaration.value; }
    |   subtype_declaration         { $value = $subtype_declaration.value; }
    |   variable_declaration        { $value = $variable_declaration.value; }
    |   constant_declaration        { $value = $constant_declaration.value; }
    |   signal_declaration          { $value = $signal_declaration.value; }
    |   file_declaration            { $value = $file_declaration.value; }
    |   alias_declaration           { $value = $alias_declaration.value; }
    |   attribute_specification     { $value = $attribute_specification.value; }
    |   attribute_declaration       { $value = $attribute_declaration.value; }
    |   disconnection_specification { $value = $disconnection_specification.value; }
    |   use_clause                  { $value = $use_clause.value; }
    |   group_template_declaration  { $value = $group_template_declaration.value; }
    |   group_declaration           { $value = $group_declaration.value; }
    ;

//TODO: don't use entity_tag.text?
entity_designator returns [AttributeSpecification.EntityNameList.EntityDesignator value]
    :   entity_tag signature?
        { $value = new AttributeSpecification.EntityNameList.EntityDesignator($entity_tag.text, $signature.value); }
    ;

entity_name_list returns [AttributeSpecification.EntityNameList value]
    :   { $value = new AttributeSpecification.EntityNameList(); }
        (
            entity_designator { $value.getDesignators().add($entity_designator.value); }
        )*
    |   OTHERS { $value = AttributeSpecification.EntityNameList.OTHERS; }
    |   ALL { $value = AttributeSpecification.EntityNameList.ALL; }
    ;

entity_statement returns [EntityStatement value]
@init { boolean isPostponed = false; }
    :   ^( ENTITY_STATEMENT identifier? ( POSTPONED { isPostponed = true; } )?
            (
                    cas=concurrent_assertion_statement[$identifier.text]
                    { $value = $cas.value; }
                |   ps=process_statement[$identifier.text]
                    { $value = $ps.value; }
                |   cpcs=concurrent_procedure_call_statement[$identifier.text]
                    { $value = $cpcs.value; }
            )
            {
                if ($value != null) {
                    $value.setPostponed(isPostponed);
                }
            }
        )
    ;

entity_tag
    :   identifier
    |   CHARACTER_LITERAL
    |   STRING_LITERAL
    ;

enumeration_type_definition[String ident] returns [EnumerationType value]
@init { $value = new EnumerationType($ident); }
    :   ^( ENUMERATION_TYPE_DEFINITION
            (
                    identifier           { $value.createLiteral($identifier.text); }
                |   cl=CHARACTER_LITERAL { $value.createLiteral($cl.text); }
            )+
        )
    ;

exit_statement[String label] returns [ExitStatement value = new ExitStatement()]
@after { addAnnotations($value, $start); }
    :   ^( EXIT
            { $value.setLabel($label); }
            ( loop_label { $value.setLoop($loop_label.value); } )?
            ( expression { $value.setCondition($expression.value); } )?
        )
    ;

expression returns [Expression value]
@after { addAnnotations($value, $start); }
    :   ^( EXPRESSION expression2 ) { $value = $expression2.value; }
    ;

expression2 returns [Expression value]
    :   ^( lo=logical_operator l=expression2 r=expression2 )
        { $value = $lo.value.create($l.value, $r.value); }
    |   relation
        { $value = $relation.value; }
    ;

factor returns [Expression value]
    :   ^( DOUBLESTAR l=primary r=primary ) { $value = new Pow($l.value, $r.value); }
    |   ^( ABS primary )                    { $value = new Abs($primary.value); }
    |   ^( NOT primary )                    { $value = new Not($primary.value); }
    |   primary                             { $value = $primary.value; }
    ;

file_declaration returns [FileDeclaration value]
@after { addAnnotations($value, $start); }
    :   ^( FILE identifier_list si=subtype_indication
            ( ^( OPEN open_kind=expression) )? logical_name=expression?
            {
                List<FileObject> files = new ArrayList<FileObject>();
                for (String ident : $identifier_list.value) {
                    files.add(new FileObject(ident, $si.value, $open_kind.value,
                        $logical_name.value));
                }
                $value = new FileDeclaration(files);
            }
        )
    ;

file_type_definition[String ident] returns [FileType value]
@after { addAnnotations($value, $start); }
    :   ^( FILE_TYPE_DEFINITION type_mark=name )
        { $value = new FileType($ident, $type_mark.value.toTypeMark(currentScope)); }
    ;

generate_statement[String label] returns [AbstractGenerateStatement value]
@init { DeclarativeRegion oldScope = currentScope; }
@after {
    currentScope = oldScope;
    addAnnotations($value, $start);
}
    :   ^( GENERATE
            generation_scheme[label]
            {
                $value = $generation_scheme.value;
                $value.setParent(oldScope);
                currentScope = $value;
            }
            ( bdi=block_declarative_item { $value.getDeclarations().add($bdi.value); } )*
            ( cs=concurrent_statement { $value.getStatements().add($cs.value); } )*
        )
    ;

generation_scheme[String label] returns [AbstractGenerateStatement value]
    :   FOR identifier discrete_range
        { $value = new ForGenerateStatement($label, $identifier.text, $discrete_range.value); }
    |   IF expression
        { $value = new IfGenerateStatement($label, $expression.value); }
    ;

generic_clause returns [List<VhdlObjectProvider<Constant>> value]
@init { $value = new ArrayList<VhdlObjectProvider<Constant>>(); }
    :   ^( GENERIC
            ( icd=interface_constant_declaration { $value.add($icd.value); } )+
        )
    ;

generic_map_aspect returns [List<AssociationElement> value]
    :   ^( GENERIC_MAP association_list ) { $value = $association_list.value; }
    ;

group_constituent
    :   name
    |   CHARACTER_LITERAL
    ;

//TODO: remove dummy GroupTemplate
//TODO: handle group_constituent correctly
//TODO: fix name ambiguity (see VhdlAntlr.g)
group_declaration returns [Group value]
@after { addAnnotations($value, $start); }
    :   ^( GROUP_DECLARATION
            identifier group_template=name
            {
                GroupTemplate dummy = new GroupTemplate($group_template.text);
                $value = new Group($identifier.text, dummy);
            }
            ( gc=group_constituent { $value.getConstituents().add($gc.text); } )+
        )
    ;

group_template_declaration returns [GroupTemplate value]
@after { addAnnotations($value, $start); }
    :   ^( GROUP_TEMPLATE_DECLARATION
            identifier { $value = new GroupTemplate($identifier.text); }
            (
                ec=entity_class { $value.getEntityClasses().add($ec.value); }
                ( BOX { $value.setRepeatLast(true); } )?
            )+
        )
    ;

identifier
    :   BASIC_IDENTIFIER
    |   EXTENDED_IDENTIFIER
    ;

identifier_list returns [List<String> value = new ArrayList<String>()]
    :   ( identifier { $value.add($identifier.text); } )+
    ;

if_statement[String label] returns [IfStatement value]
@after { addAnnotations($value, $start); }
    :   ^( IF
            c1=expression
            {
                $value = new IfStatement($c1.value);
                $value.setLabel($label);
            }
            ( ss1=sequential_statement { $value.getStatements().add($ss1.value); } )*
            (
                ^( ELSIF
                    c2=expression
                    { IfStatement.ElsifPart part = $value.createElsifPart($c2.value); }
                    ( ss2=sequential_statement { part.getStatements().add($ss2.value); } )*
                )
            )*
            (
                ^( ELSE
                    ( ss3=sequential_statement { $value.getElseStatements().add($ss3.value); } )*
                )
            )?
        )
    ;

index_constraint returns [List<DiscreteRange> value = new ArrayList<DiscreteRange>()]
    :   ^( INDEX_CONSTRAINT ( discrete_range { $value.add($discrete_range.value); } )+ )
    ;

instantiated_unit[String label] returns [AbstractComponentInstantiation value]
@init { boolean optionalComponent = false; }
    :   ^( COMPONENT_INSTANCE
            ( COMPONENT { optionalComponent = true; } )?
            component=name
        )
        {
            $value = new ComponentInstantiation($label, $component.value.toComponent(currentScope));
            if (optionalComponent) {
                Annotations.putAnnotation(
                    $value, ComponentInstantiationFormat.class,
                    new ComponentInstantiationFormat(optionalComponent)
                );
            }
        }
    |   ^( ENTITY entity=name
            (
                    architecture=identifier
                    {
                        //TODO: remove dummy architecture
                        Architecture arch = new Architecture($architecture.text, $entity.value.toEntity(currentScope));
                        $value = new ArchitectureInstantiation($label, arch);
                    }
                |
                    { $value = new EntityInstantiation($label, $entity.value.toEntity(currentScope)); }
            )
        )
    |   ^( CONFIGURATION configuration=name )
        { $value = new ConfigurationInstantiation($label, $configuration.value.toConfiguration(currentScope)); }
    ;

//TODO: float range necessary?
integer_or_float_type_definition[String ident] returns [IntegerType value]
@after { addAnnotations($value, $start); }
    :   ^( INTEGER_OR_FLOAT_TYPE_DEFINITION range_constraint )
        { $value = new IntegerType($ident, $range_constraint.value); }
    ;

interface_constant_declaration returns [ConstantGroup value]
@init{
    boolean hasObjectClass = false;
    boolean hasMode = false;
}
@after {
    addAnnotations($value, $start);
}
    :   ^( INTERFACE_CONSTANT_DECLARATION
            ( CONSTANT { hasObjectClass = true; } )?
            identifier_list
            ( IN { hasMode = true;} )?
            si=subtype_indication
            def=expression?
        )
        {
            InterfaceDeclarationFormat format =
                new InterfaceDeclarationFormat(hasObjectClass, hasMode);

            $value = new ConstantGroup();
            for (String identifier : $identifier_list.value) {
                Constant c = new Constant(identifier, $si.value, $def.value);
                Annotations.putAnnotation(c, InterfaceDeclarationFormat.class, format);

                $value.getElements().add(c);
            }
        }
    ;

interface_declaration returns [VhdlObjectProvider<? extends VhdlObject> value]
    :   isd=interface_signal_declaration   { $value = $isd.value; }
    |   icd=interface_constant_declaration { $value = $icd.value; }
    |   ivd=interface_variable_declaration { $value = $ivd.value; }
    |   ifd=interface_file_declaration     { $value = $ifd.value; }
    ;

interface_file_declaration returns [FileGroup value]
    :   ^( INTERFACE_FILE_DECLARATION FILE identifier_list subtype_indication )
        {
            $value = new FileGroup();
            for (String identifier : $identifier_list.value) {
                $value.getElements().add(new FileObject(identifier, $subtype_indication.value));
            }
        }
    ;

interface_signal_declaration returns [SignalGroup value]
@init{
    boolean hasObjectClass = false;
    boolean hasMode = false;
    boolean isBus = false;
}
@after {
    addAnnotations($value, $start);
}
    :   ^( INTERFACE_SIGNAL_DECLARATION
            ( SIGNAL { hasObjectClass = true; } )?
            identifier_list
            ( mode { hasMode = true; } )?
            si=subtype_indication
            ( BUS { isBus = true; } )?
            def=expression?
        )
        {
            InterfaceDeclarationFormat format =
                new InterfaceDeclarationFormat(hasObjectClass, hasMode);

            $value = new SignalGroup();
            Signal.Mode m = ($mode.value == null ? Signal.Mode.IN : $mode.value);
            for (String identifier : $identifier_list.value) {
                Signal s = new Signal(identifier, m, $si.value, $def.value);
                if (isBus) {
                    s.setKind(Signal.Kind.BUS);
                }
                Annotations.putAnnotation(s, InterfaceDeclarationFormat.class, format);

                $value.getElements().add(s);
            }
        }
    ;

interface_variable_declaration returns [VariableGroup value]
@init{
    boolean hasObjectClass = false;
    boolean hasMode = false;
}
    :   ^( INTERFACE_VARIABLE_DECLARATION
            ( VARIABLE { hasObjectClass = true; } )?
            identifier_list
            ( mode { hasMode = true; } )?
            si=subtype_indication
            def=expression?
        )
        {
            InterfaceDeclarationFormat format =
                new InterfaceDeclarationFormat(hasObjectClass, hasMode);

            $value = new VariableGroup();
            for (String identifier : $identifier_list.value) {
                Variable v = new Variable(identifier, $si.value, $def.value);
                if (hasMode) {
                    v.setMode($mode.value);
                }
                Annotations.putAnnotation(v, InterfaceDeclarationFormat.class, format);

                $value.getElements().add(v);
            }
        }
    ;

iteration_scheme returns [LoopStatement value]
    :   ^( WHILE expression )
        { $value = new WhileStatement($expression.value); }
    |   ^( FOR identifier discrete_range )
        { $value = new ForStatement($identifier.text, $discrete_range.value); }
    |   UNCONDITIONAL_LOOP
        { $value = new LoopStatement(); }
    ;

library_clause returns [LibraryClause value]
@after { addAnnotations($value, $start); }
    :   ^( LIBRARY logical_name_list )
        { $value = new LibraryClause($logical_name_list.value); }
    ;

library_unit returns [LibraryUnit value]
    :   architecture_body         { $value = $architecture_body.value; }
    |   package_body              { $value = $package_body.value; }
    |   entity_declaration        { $value = $entity_declaration.value; }
    |   configuration_declaration { $value = $configuration_declaration.value; }
    |   package_declaration       { $value = $package_declaration.value; }
    ;

logical_name_list returns [List<String> value = new ArrayList<String>()]
    :   ( identifier { $value.add($identifier.text); } )+
    ;

logical_operator returns [ExpressionType value]
    :   AND  { $value = ExpressionType.AND; }
    |   OR   { $value = ExpressionType.OR; }
    |   NAND { $value = ExpressionType.NAND; }
    |   NOR  { $value = ExpressionType.NOR; }
    |   XOR  { $value = ExpressionType.XOR; }
    |   XNOR { $value = ExpressionType.XNOR; }
    ;

loop_statement[String label] returns [LoopStatement value]
@init { DeclarativeRegion oldScope = currentScope; }
@after {
    currentScope = oldScope;
    addAnnotations($value, $start);
}
    :   ^( LOOP iteration_scheme
            {
                $value = $iteration_scheme.value;
                $value.setLabel($label);
                $value.setParent(oldScope);
                currentScope = $value;
            }
            ( ss=sequential_statement { $value.getStatements().add($ss.value); } )*
        )
    ;

mode returns [VhdlObject.Mode value]
    :   IN      { $value = VhdlObject.Mode.IN; }
    |   OUT     { $value = VhdlObject.Mode.OUT; }
    |   INOUT   { $value = VhdlObject.Mode.INOUT; }
    |   BUFFER  { $value = VhdlObject.Mode.BUFFER; }
    |   LINKAGE { $value = VhdlObject.Mode.LINKAGE; }
    ;

multiplying_operator returns [ExpressionType value]
    :   MUL { $value = ExpressionType.MUL; }
    |   DIV { $value = ExpressionType.DIV; }
    |   MOD { $value = ExpressionType.MOD; }
    |   REM { $value = ExpressionType.REM; }
    ;

// was
//   name
//     : simple_name
//     | operator_symbol
//     | selected_name
//     | indexed_name
//     | slice_name
//     | attribute_name
//     ;
name returns [TemporaryName value]
    :   ^( NAME
            (       identifier
                    { $value = new TemporaryName(this, $name.start, $identifier.text); }
                |   STRING_LITERAL
                    {
                        String literal = $STRING_LITERAL.text;
                        literal = literal.substring(1, literal.length() - 1);
                        $value = new TemporaryName(this, $name.start, new StringLiteral(literal));
                    }
            )
            (
                    name_indexed_part   { $value.addPart($name_indexed_part.value); }
                |   name_slice_part     { $value.addPart($name_slice_part.value); }
                |   name_attribute_part { $value.addPart($name_attribute_part.value); }
                |   name_selected_part  { $value.addPart($name_selected_part.value); }
                |   association_list
                    {
                        TemporaryName.Part part =
                            TemporaryName.Part.createAssociation($association_list.value);
                        $value.addPart(part);
                    }
                |   name_indexed_or_slice_part
                    { $value.addPart($name_indexed_or_slice_part.value); }
            )*
        )
    ;

name_attribute_part returns [TemporaryName.Part value]
    :   ^( NAME_ATTRIBUTE_PART signature? identifier expression? )
        {
            $value = TemporaryName.Part.createAttribute($identifier.text, $expression.value,
                $signature.value);
        }
    ;

name_indexed_part returns [TemporaryName.Part value]
@init { List<Expression> indices = new ArrayList<Expression>(); }
    :   ^( NAME_INDEXED_PART
            ( expression { indices.add($expression.value); } )+
        )
        { $value = TemporaryName.Part.createIndexed(indices); }
    ;

//TODO: don't use suffix.text
name_selected_part returns [TemporaryName.Part value]
    :   ^( NAME_SELECTED_PART suffix )
        { $value = TemporaryName.Part.createSelected($suffix.text); }
    ;

name_slice_part returns [TemporaryName.Part value]
    :   ^( NAME_SLICE_PART discrete_range )
        { $value = TemporaryName.Part.createSlice($discrete_range.value); }
    ;

name_indexed_or_slice_part returns [TemporaryName.Part value]
    :   ^( NAME_INDEXED_OR_SLICE_PART name )
        { $value = TemporaryName.createIndexedOrSlicePart($name.value, currentScope); }
    ;

next_statement[String label] returns [NextStatement value = new NextStatement()]
    :   ^( NEXT
            { $value.setLabel($label); }
            ( loop_label { $value.setLoop($loop_label.value); } )?
            ( expression { $value.setCondition($expression.value); } )?
        )
    ;

null_statement[String label] returns [NullStatement value = new NullStatement()]
@after { addAnnotations($value, $start); }
    :   NULLTOK
        { $value.setLabel($label); }
    ;

package_body returns [PackageBody value]
@init { DeclarativeRegion oldScope = currentScope; }
@after {
    currentScope = oldScope;
    addAnnotations($value, $start);
}
    :   ^( PACKAGE_BODY
            psn=package_simple_name
            {
                $value = new PackageBody($psn.value);
                $value.setParent(oldScope);
                currentScope = $value;
            }
            ( pbdi=package_body_declarative_item { $value.getDeclarations().add($pbdi.value); } )*
        )
    ;

package_body_declarative_item returns [PackageBodyDeclarativeItem value]
    :   subprogram_declaration     { $value = $subprogram_declaration.value; }
    |   subprogram_body            { $value = $subprogram_body.value; }
    |   type_declaration           { $value = $type_declaration.value; }
    |   subtype_declaration        { $value = $subtype_declaration.value; }
    |   constant_declaration       { $value = $constant_declaration.value; }
    |   variable_declaration       { $value = $variable_declaration.value; }
    |   file_declaration           { $value = $file_declaration.value; }
    |   alias_declaration          { $value = $alias_declaration.value; }
    |   use_clause                 { $value = $use_clause.value; }
    |   group_template_declaration { $value = $group_template_declaration.value; }
    |   group_declaration          { $value = $group_declaration.value; }
    ;
    
package_declaration returns [PackageDeclaration value]
@init { DeclarativeRegion oldScope = currentScope; }
@after {
    currentScope = oldScope;
    addAnnotations($value, $start);
}
    :   ^( PACKAGE
            identifier
            {
                $value = new PackageDeclaration($identifier.text);
                $value.setParent(oldScope);
                currentScope = $value;
            }
            ( pdi=package_declarative_item { $value.getDeclarations().add($pdi.value); } )*
        )
    ;

package_declarative_item returns [PackageDeclarativeItem value]
    :   subprogram_declaration      { $value = $subprogram_declaration.value; }
    |   type_declaration            { $value = $type_declaration.value; }
    |   subtype_declaration         { $value = $subtype_declaration.value; }
    |   constant_declaration        { $value = $constant_declaration.value; }
    |   signal_declaration          { $value = $signal_declaration.value; }
    |   variable_declaration        { $value = $variable_declaration.value; }
    |   file_declaration            { $value = $file_declaration.value; }
    |   alias_declaration           { $value = $alias_declaration.value; }
    |   component_declaration       { $value = $component_declaration.value; }
    |   attribute_specification     { $value = $attribute_specification.value; }
    |   attribute_declaration       { $value = $attribute_declaration.value; }
    |   disconnection_specification { $value = $disconnection_specification.value; }
    |   use_clause                  { $value = $use_clause.value; }
    |   group_template_declaration  { $value = $group_template_declaration.value; }
    |   group_declaration           { $value = $group_declaration.value; }
    ;

//TODO: don't use name.text
physical_type_definition[String ident] returns [PhysicalType value]
@after { addAnnotations($value, $start); }
    :   ^( PHYSICAL_TYPE_DEFINITION
            range_constraint baseUnit=identifier
            { $value = new PhysicalType($ident, $range_constraint.value, $baseUnit.text); }
            (
                unit=identifier
                (
                        al=abstract_literal n1=name
                        { $value.createUnit($unit.text, $al.value, $n1.text); }
                    |   n2=name
                        { $value.createUnit($unit.text, $n2.text); }
                )
            )*
        )
    ;

port_clause returns [List<VhdlObjectProvider<Signal>> value]
@init { $value = new ArrayList<VhdlObjectProvider<Signal>>(); }
    :   ^( PORT
            ( isd=interface_signal_declaration { $value.add($isd.value); } )+
        )
    ;

port_map_aspect returns [List<AssociationElement> value]
    :   ^( PORT_MAP association_list )
        { $value = $association_list.value; }
    ;

primary returns [Expression value]
@after { addAnnotations($value, $start); }
    :   abstract_literal { $value = $abstract_literal.value; }
    |   ^( PHYSICAL_LITERAL abstract_literal unit=name )
        //TODO: don't use .text
        { $value = new PhysicalLiteral($abstract_literal.text, $name.text); }
    |   CHARACTER_LITERAL
        { $value = new CharacterLiteral($CHARACTER_LITERAL.text.charAt(1)); }
    |   BIT_STRING_LITERAL_BINARY
        {
            String v = $BIT_STRING_LITERAL_BINARY.text;
            v = v.substring(2, v.length() - 1);
            $value = new BinaryLiteral(v);
        }
    |   BIT_STRING_LITERAL_OCTAL
        {
            String v = $BIT_STRING_LITERAL_OCTAL.text;
            v = v.substring(2, v.length() - 1);
            $value = new OctalLiteral(v);
        }
    |   BIT_STRING_LITERAL_HEX
        {
            String v = $BIT_STRING_LITERAL_HEX.text;
            v = v.substring(2, v.length() - 1);
            $value = new HexLiteral(v);
        }
    |   NULLTOK
        { $value = Literals.NULL; }
    |   aggregate
        {
            Aggregate a = $aggregate.value;
            if (a.getAssociations().size() == 1 &&
                    a.getAssociations().get(0).getChoices().size() == 0) {
                $value = new Parentheses(a.getAssociations().get(0).getExpression());
            } else {
                $value = a;
            }
        }
    |   allocator
        { $value = $allocator.value; }
    |   name
        { $value = $name.value.toPrimary(currentScope, inElementAssociation); }
    |   qualified_expression
        { $value = $qualified_expression.value; }
    ;

procedure_call_statement[String label] returns [ProcedureCall value]
@after { addAnnotations($value, $start); }
   :   ^( PROCEDURE_CALL 
            procedure=name 
            {
                $value = new ProcedureCall($procedure.value.toProcedure(currentScope));
                $value.setLabel($label);
            }
            ( 
                parameters=association_list { $value.getParameters().addAll($parameters.value);}
            )?
        )
    ;

process_declarative_item returns [ProcessDeclarativeItem value]
    :   subprogram_declaration     { $value = $subprogram_declaration.value; }
    |   subprogram_body            { $value = $subprogram_body.value; }
    |   type_declaration           { $value = $type_declaration.value; }
    |   subtype_declaration        { $value = $subtype_declaration.value; }
    |   constant_declaration       { $value = $constant_declaration.value; }
    |   variable_declaration       { $value = $variable_declaration.value; }
    |   file_declaration           { $value = $file_declaration.value; }
    |   alias_declaration          { $value = $alias_declaration.value; }
    |   attribute_specification    { $value = $attribute_specification.value; }
    |   attribute_declaration      { $value = $attribute_declaration.value; }
    |   use_clause                 { $value = $use_clause.value; }
    |   group_template_declaration { $value = $group_template_declaration.value; }
    |   group_declaration          { $value = $group_declaration.value; }
    ;

process_statement[String label] returns [ProcessStatement value]
@init {
    $value = new ProcessStatement($label);

    DeclarativeRegion oldScope = currentScope;
    $value.setParent(oldScope);
    currentScope = $value;
}
@after {
    currentScope = oldScope;
    addAnnotations($value, $start);
}
    :   ^( PROCESS
            ( sl=sensitivity_list { $value.getSensitivityList().addAll($sl.value); } )?
            (
                IS
                {
                    Annotations.putAnnotation($value, OptionalIsFormat.class,
                        new OptionalIsFormat(true));
                }
            )?
            ( pdi=process_declarative_item { $value.getDeclarations().add($pdi.value); } )*
            ( ss=sequential_statement { $value.getStatements().add($ss.value); } )*
        )
    ;

//: ^( QUALIFIED_EXPRESSION type_mark ( aggregate | expression ) )
qualified_expression returns [QualifiedExpression value]
@after { addAnnotations($value, $start); }
    :   ^( QUALIFIED_EXPRESSION type_mark=name aggregate )
        {
            $value = new QualifiedExpression($type_mark.value.toTypeMark(currentScope), $aggregate.value);
        }
    ;

range returns [RangeProvider value]
    :   ^( direction left=expression right=expression)
        { $value = new Range($left.value, $direction.value, $right.value); }
    |   name
        { $value = $name.value.toRangeName(currentScope); }
    ;

range_constraint returns [RangeProvider value]
    :   ^( RANGETOK range )
        { $value = $range.value; }
    ;

record_type_definition[String ident] returns [RecordType value]
@init { $value = new RecordType($ident); }
@after { addAnnotations($value, $start); }
    :   ^( RECORD_TYPE_DEFINITION
            ( 
                identifier_list subtype_indication
                { $value.createElement($subtype_indication.value, $identifier_list.value); }
            )+
        )
    ;

relation returns [Expression value]
    :   ^( relational_operator l=relation r=relation )
        { $value = $relational_operator.value.create($l.value, $r.value); }
    |   shift_expression
        { $value = $shift_expression.value; }
    ;

relational_operator returns [ExpressionType value]
    :   EQ  { $value = ExpressionType.EQ; }
    |   NEQ { $value = ExpressionType.NEQ; }
    |   LT  { $value = ExpressionType.LT; }
    |   LE  { $value = ExpressionType.LE; }
    |   GT  { $value = ExpressionType.GT; }
    |   GE  { $value = ExpressionType.GE; }
    ;

report_statement[String label] returns [ReportStatement value]
@after { addAnnotations($value, $start); }
    :   ^( REPORT
            condition=expression
            {
                $value = new ReportStatement($condition.value);
                $value.setLabel($label);
            }
            ( severity=expression { $value.setSeverity($severity.value); } )?
        )
    ;

return_statement[String label] returns [ReturnStatement value = new ReturnStatement()]
@after { addAnnotations($value, $start); }
    :   ^( RETURN
            { $value.setLabel($label); }
            ( expression { $value.setReturnedExpression($expression.value); } )?
        )
    ;

selected_signal_assignment returns [SelectedSignalAssignment value]
@init { boolean isGuarded; }
@after { addAnnotations($value, $start); }
    :   expression target_signal
        { $value = new SelectedSignalAssignment($expression.value, $target_signal.value); }
        ( GUARDED { $value.setGuarded(true); } )?
        ( delay_mechanism { $value.setDelayMechanism($delay_mechanism.value); } )?
        selected_waveforms { $value.getSelectedWaveforms().addAll($selected_waveforms.value); }
    ;

selected_waveforms returns [List<SelectedSignalAssignment.SelectedWaveform> value]
@init { $value = new ArrayList<SelectedSignalAssignment.SelectedWaveform>(); }
    :   (
            waveform choices
            {
                $value.add(
                    new SelectedSignalAssignment.SelectedWaveform($waveform.value, $choices.value)
                );
            }
        )+
    ;

sensitivity_list returns [List<Signal> value = new ArrayList<Signal>()]
    :   ( signal=name { $value.add($signal.value.toSignal(currentScope)); } )+
    ;

sequential_statement returns [SequentialStatement value]
    :   ^( LABEL_STATEMENT identifier sequential_statement2[$identifier.text] )
        { $value = $sequential_statement2.value; }
    |   sequential_statement2[null]
        { $value = $sequential_statement2.value; }
    ;

sequential_statement2[String label] returns [SequentialStatement value]
    : s00=wait_statement[$label]                { $value = $s00.value; }
    | s01=assertion_statement[$label]           { $value = $s01.value; }
    | s02=report_statement[$label]              { $value = $s02.value; }
    | s03=signal_assignment_statement[$label]   { $value = $s03.value; }
    | s04=variable_assignment_statement[$label] { $value = $s04.value; }
    | s05=if_statement[$label]                  { $value = $s05.value; }
    | s06=case_statement[$label]                { $value = $s06.value; }
    | s07=loop_statement[$label]                { $value = $s07.value; }
    | s08=next_statement[$label]                { $value = $s08.value; }
    | s09=exit_statement[$label]                { $value = $s09.value; }
    | s10=return_statement[$label]              { $value = $s10.value; }
    | s11=null_statement[$label]                { $value = $s11.value; }
    | s12=procedure_call_statement[$label]      { $value = $s12.value; }
    ;

shift_expression returns [Expression value]
    :   ^( shift_operator l=shift_expression r=shift_expression )
        { $value = $shift_operator.value.create($l.value, $r.value); }
    |   simple_expression
        { $value = $simple_expression.value; }
    ;

shift_operator returns [ExpressionType value]
    :   SLL { $value = ExpressionType.SLL;}
    |   SRL { $value = ExpressionType.SRL;}
    |   SLA { $value = ExpressionType.SLA;}
    |   SRA { $value = ExpressionType.SRA;}
    |   ROL { $value = ExpressionType.ROL;}
    |   ROR { $value = ExpressionType.ROR;}
    ;

signal_assignment_statement[String label] returns [SignalAssignment value]
@after { addAnnotations($value, $start); }
    :   ^( SIGNAL_ASSIGNMENT_STATEMENT target_signal delay_mechanism? waveform )
        {
            $value = new SignalAssignment($target_signal.value, $waveform.value);
            $value.setLabel($label);
            $value.setDelayMechanism($delay_mechanism.value);
        }
    ;

signal_declaration returns [SignalDeclaration value]
@after { addAnnotations($value, $start); }
    :   ^( SIGNAL identifier_list subtype_indication signal_kind expression? )
        {
            List<Signal> signals = new ArrayList<Signal>();
            for (String identifier : $identifier_list.value) {
                Signal s = new Signal(identifier, $subtype_indication.value, $expression.value);
                s.setKind($signal_kind.value);
                signals.add(s);
            }
            $value = new SignalDeclaration(signals);
        }
    ;

signal_kind returns [Signal.Kind value]
    :   REGISTER { $value = Signal.Kind.REGISTER; }
    |   BUS      { $value = Signal.Kind.BUS; }
    |            { $value = Signal.Kind.DEFAULT; }
    ;

signal_list returns [DisconnectionSpecification.SignalList value]
@init { List<Signal> signals = new ArrayList<Signal>(); }
    :   ^( SIGNAL_LIST ( signal=name { signals.add($signal.value.toSignal(currentScope)); } )+ )
        { $value = new DisconnectionSpecification.SignalList(signals); }
    |   OTHERS { $value = DisconnectionSpecification.SignalList.OTHERS; }
    |   ALL    { $value = DisconnectionSpecification.SignalList.ALL; }
    ;

signature returns [Signature value = new Signature();]
    :   ^( SIGNATURE 
            ( type_mark1=name { $value.getParameterTypes().add($type_mark1.value.toTypeMark(currentScope)); } )*
            ( RETURN type_mark2=name { $value.setReturnType($type_mark2.value.toTypeMark(currentScope)); } )?
        )
    ;

simple_expression returns [Expression value]
@init {
    boolean addPlus = false;
    boolean addMinus = false;
}
@after {
    if (addPlus) {
        $value = new Plus($value);
    }
    if (addMinus) {
        $value = new Minus($value);
    }
}
    :   (
                PLUS  { addPlus = true; }
            |   MINUS { addMinus = true; }
        )?
        (
                ^( adding_operator l=simple_expression r=simple_expression )
                { $value = $adding_operator.value.create($l.value, $r.value); }
            |   t=term
                { $value = $t.value; }
        )
    ;

subprogram_body returns [SubprogramBody value]
@init { DeclarativeRegion oldScope = currentScope; }
@after {
    currentScope = oldScope;
    addAnnotations($value, $start);
}
    :   ^( SUBPROGRAM_BODY
            sp=subprogram_specification
            {
                if ($subprogram_specification.value instanceof FunctionDeclaration) {
                    $value = new FunctionBody((FunctionDeclaration) $sp.value);
                } else if ($subprogram_specification.value instanceof ProcedureDeclaration) {
                    $value = new ProcedureBody((ProcedureDeclaration) $sp.value);
                } else {
                    throw new IllegalStateException();
                }

                $value.setParent(oldScope);
                currentScope = $value;
            }
            ( sdi=subprogram_declarative_item { $value.getDeclarations().add($sdi.value); } )*
            ( ss=sequential_statement { $value.getStatements().add($ss.value); } )*
        )
    ;

subprogram_declaration returns [SubprogramDeclaration value]
@after { addAnnotations($value, $start); }
    :   ^( SUBPROGRAM_DECLARATION subprogram_specification )
        { $value = $subprogram_specification.value; }
    ;

subprogram_declarative_item returns [SubprogramDeclarativeItem value]
    :   subprogram_declaration     { $value = $subprogram_declaration.value; }
    |   subprogram_body            { $value = $subprogram_body.value; }
    |   type_declaration           { $value = $type_declaration.value; }
    |   subtype_declaration        { $value = $subtype_declaration.value; }
    |   constant_declaration       { $value = $constant_declaration.value; }
    |   variable_declaration       { $value = $variable_declaration.value; }
    |   file_declaration           { $value = $file_declaration.value; }
    |   alias_declaration          { $value = $alias_declaration.value; }
    |   attribute_specification    { $value = $attribute_specification.value; }
    |   attribute_declaration      { $value = $attribute_declaration.value; }
    |   use_clause                 { $value = $use_clause.value; }
    |   group_template_declaration { $value = $group_template_declaration.value; }
    |   group_declaration          { $value = $group_declaration.value; }
    ;

subprogram_specification returns [SubprogramDeclaration value]
@init { boolean impure = false; }
    :   ^( PROCEDURE
            designator { $value = new ProcedureDeclaration($designator.text); }
            ( id=interface_declaration { $value.getParameters().add($id.value); } )*
        )
    |   ^( FUNCTION
            PURE? ( IMPURE { impure = true; } )? designator type_mark=name
            {
                SubtypeIndication type = $type_mark.value.toTypeMark(currentScope);
                FunctionDeclaration fd = new FunctionDeclaration($designator.text, type);
                fd.setImpure(impure);
                $value = fd;
            }
            ( id=interface_declaration { $value.getParameters().add($id.value); } )*
        )
    ;

subtype_declaration returns [Subtype value]
@after { addAnnotations($value, $start); }
    :   ^( SUBTYPE identifier subtype_indication )
        { $value = new Subtype($identifier.text, $subtype_indication.value); }
    ;

//TODO: fix name/constraint ambiguity
//TODO: remove unresolved type
subtype_indication returns [SubtypeIndication value]
    :   ^( SUBTYPE_INDICATION
            type_mark=name { $value = $type_mark.value.toTypeMark(currentScope); }
            (
                resolution_function=name
                { $value = new ResolvedSubtypeIndication($resolution_function.value.toFunction(currentScope), $value); }
            )?
            (
                    range_constraint
                    { $value = new RangeSubtypeIndication($value, $range_constraint.value); }
                |   index_constraint
                    { $value = new IndexSubtypeIndication($value, $index_constraint.value); }
            )?
        )
    ;

suffix
    :   identifier
    |   CHARACTER_LITERAL
    |   STRING_LITERAL
    |   ALL
    ;

target_signal returns [Target<Signal> value]
    :   signal=name { $value = $signal.value.toSignalTarget(currentScope); }
    |   aggregate
        {
            //TODO: find type safe alternative
            @SuppressWarnings("unchecked")
            Target<Signal> tmp = $aggregate.value;
            $value = tmp;
        }
    ;

target_variable returns [Target<Variable> value]
    :   variable=name { $value = $variable.value.toVariableTarget(currentScope); }
    |   aggregate
        {
            //TODO: find type safe alternative
            @SuppressWarnings("unchecked")
            Target<Variable> tmp = $aggregate.value;
            $value = tmp;
        }
    ;

term returns [Expression value]
    :   ^( multiplying_operator l=term r=term )
        { $value = $multiplying_operator.value.create($l.value, $r.value); }
    |   factor
        { $value = $factor.value; }
    ;

type_declaration returns [Type value]
    :   ^( FULL_TYPE_DECLARATION identifier type_definition[$identifier.text] )
        { $value = $type_definition.value; }
    |   ^( INCOMPLETE_TYPE_DECLARATION identifier )
        { $value = new IncompleteType($identifier.text); }
    ;

type_definition[String identifier] returns [Type value]
    : ptd=physical_type_definition[$identifier]           { $value = $ptd.value; }
    | etd=enumeration_type_definition[$identifier]        { $value = $etd.value; }
    | ioftd=integer_or_float_type_definition[$identifier] { $value = $ioftd.value; }
    | uad=unconstrained_array_definition[$identifier]     { $value = $uad.value; }
    | cas=constrained_array_definition[$identifier]       { $value = $cas.value; }
    | rtd=record_type_definition[$identifier]             { $value = $rtd.value; }
    | atd=access_type_definition[$identifier]             { $value = $atd.value; }
    | ftd=file_type_definition[$identifier]               { $value = $ftd.value; }
    ;

unconstrained_array_definition[String ident] returns [UnconstrainedArray value]
@init { List<SubtypeIndication> indexSubtypes = new ArrayList<SubtypeIndication>(); }
@after { addAnnotations($value, $start); }
    :   ^( UNCONSTRAINED_ARRAY_DEFINITION
            ( index_subtype=name { indexSubtypes.add($index_subtype.value.toTypeMark(currentScope)); } )+
            element_type=subtype_indication
        )
        { $value = new UnconstrainedArray($ident, $element_type.value, indexSubtypes); }
    ;

//TODO: handle names differently
use_clause returns [UseClause value]
@init { List<String> names = new ArrayList<String>(); }
@after {
    $value = new UseClause(names);
    addAnnotations($value, $start);
}
    :   ^( USE ( selected_name=name { names.add($selected_name.value.toUseClauseName(currentScope)); } )+ )
    ;

variable_assignment_statement[String label] returns [VariableAssignment value]
@after { addAnnotations($value, $start); }
    :   ^( VARIABLE_ASSIGNMENT_STATEMENT target_variable expression )
        {
            $value = new VariableAssignment($target_variable.value, $expression.value);
            $value.setLabel($label);
        }
    ;

variable_declaration returns [VariableDeclaration value]
@init { boolean isShared = false; }
@after { addAnnotations($value, $start); }
    :   ^( VARIABLE
            ( SHARED { isShared = true; } )?
            identifier_list subtype_indication expression?
        )
        {
            List<Variable> variables = new ArrayList<Variable>();
            for (String identifier : $identifier_list.value) {
                Variable v = new Variable(identifier, $subtype_indication.value, $expression.value);
                v.setShared(isShared);
                variables.add(v);
            }
            $value = new VariableDeclaration(variables);
        }
    ;

wait_statement[String label] returns [WaitStatement value = new WaitStatement()]
@after { addAnnotations($value, $start); }
    :   ^( WAIT
            { $value.setLabel($label); }
            ( ^(ON sl=sensitivity_list) { $value.getSensitivityList().addAll($sl.value); } )?
            ( ^(UNTIL condition=expression) { $value.setCondition($condition.value); } )?
            ( ^(FOR timeout=expression) { $value.setTimeout($timeout.value); } )?
        )
    ;

waveform returns [List<WaveformElement> value = new ArrayList<WaveformElement>()]
    :   ^( WAVEFORM
            ( waveform_element { $value.add($waveform_element.value); } )+
        )
    |   UNAFFECTED //return empty list
    ;

waveform_element returns [WaveformElement value]
    :   ^( WAVEFORM_ELEMENT
            val=expression { $value = new WaveformElement($val.value); }
            ( after=expression { $value.setAfter($after.value); } )?
        )
    ;

//----------

loop_label returns [LoopStatement value]
    :   identifier
        {
            $value = resolve($identifier.text, LoopStatement.class);
            if ($value == null) {
                resolveError($identifier.start, ParseError.Type.UNKNOWN_LOOP, $identifier.text);
                $value = new LoopStatement();
                $value.setLabel($identifier.text);
            }
        }
    ;

package_simple_name returns [PackageDeclaration value]
    :   identifier
        {
            $value = resolve($identifier.text, PackageDeclaration.class);
            if ($value == null) {
                resolveError($identifier.start, ParseError.Type.UNKNOWN_PACKAGE, $identifier.text);
                $value = new PackageDeclaration($identifier.text);
            }
        }
    ;
