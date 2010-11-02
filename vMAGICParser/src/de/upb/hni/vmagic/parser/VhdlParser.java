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

import de.upb.hni.vmagic.Annotations;
import de.upb.hni.vmagic.LibraryDeclarativeRegion;
import de.upb.hni.vmagic.RootDeclarativeRegion;
import de.upb.hni.vmagic.parser.util.CaseInsensitiveInputStream;
import de.upb.hni.vmagic.parser.util.CaseInsensitiveStringStream;
import de.upb.hni.vmagic.parser.util.CaseInsensitiveFileStream;
import de.upb.hni.vmagic.VhdlFile;
import de.upb.hni.vmagic.parser.annotation.ParseErrors;
import de.upb.hni.vmagic.parser.antlr.MetaClassCreator;
import de.upb.hni.vmagic.parser.antlr.VhdlAntlrLexer;
import de.upb.hni.vmagic.parser.antlr.VhdlAntlrParser;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.antlr.runtime.tree.CommonTreeNodeStream;

/**
 * VHDL parser.
 */
public class VhdlParser {

    private static final VhdlParserSettings DEFAULT_SETTINGS = new VhdlParserSettings();

    /**
     * Prevent instantiation.
     */
    private VhdlParser() {
    }

    private static VhdlFile parse(VhdlParserSettings settings, CharStream stream,
            RootDeclarativeRegion rootScope, LibraryDeclarativeRegion libraryScope)
            throws VhdlParserException {
        VhdlAntlrLexer lexer = new VhdlAntlrLexer(stream);
        CommonTokenStream ts = new CommonTokenStream(lexer);

        VhdlAntlrParser parser = new VhdlAntlrParser(ts);
        parser.setTreeAdaptor(new TreeAdaptorWithoutErrorNodes());

        VhdlAntlrParser.design_file_return result;
        try {
            result = parser.design_file();
        } catch (RecognitionException ex) {
            throw new VhdlParserException(ex.getMessage());
        }

        CommonTreeNodeStream nodes = new CommonTreeNodeStream(result.getTree());
        nodes.setTokenStream(ts);

        MetaClassCreator mcc = new MetaClassCreator(nodes, settings, rootScope, libraryScope);

        VhdlFile file = null;
        try {
            file = mcc.design_file();
        } catch (RecognitionException ex) {
            throw new VhdlParserException(ex.getMessage());
        }

        if (!mcc.getErrors().isEmpty()) {
            List<ParseError> errors = mcc.getErrors();
            Annotations.putAnnotation(file, ParseErrors.class, new ParseErrors(errors));
            if (settings.isPrintErrors()) {
                reportErrors(errors);
            }
        }

        return file;
    }

    private static VhdlFile parse(VhdlParserSettings settings, CharStream stream) throws VhdlParserException {
        RootDeclarativeRegion rootScope = new RootDeclarativeRegion();
        LibraryDeclarativeRegion libraryScope = new LibraryDeclarativeRegion("work");
        rootScope.getLibraries().add(libraryScope);

        return parse(settings, stream, rootScope, libraryScope);
    }

    public static VhdlFile parseFile(String fileName) throws IOException, VhdlParserException {
        return parseFile(fileName, DEFAULT_SETTINGS);
    }

    public static VhdlFile parseFile(String fileName, VhdlParserSettings settings)
            throws IOException, VhdlParserException {
        return parse(settings, new CaseInsensitiveFileStream(fileName));
    }

    public static VhdlFile parseFile(String fileName, VhdlParserSettings settings, RootDeclarativeRegion rootScope, LibraryDeclarativeRegion libray)
            throws IOException, VhdlParserException {
        return parse(settings, new CaseInsensitiveFileStream(fileName), rootScope, libray);
    }

    public static VhdlFile parseString(String str) throws IOException, VhdlParserException {
        return parseString(str, DEFAULT_SETTINGS);
    }

    public static VhdlFile parseString(String str, VhdlParserSettings settings)
            throws IOException, VhdlParserException {
        return parse(settings, new CaseInsensitiveStringStream(str));
    }

    public static VhdlFile parseStream(InputStream stream) throws IOException, VhdlParserException {
        return parseStream(stream, DEFAULT_SETTINGS);
    }

    public static VhdlFile parseStream(InputStream stream, VhdlParserSettings settings)
            throws IOException, VhdlParserException {
        return parse(settings, new CaseInsensitiveInputStream(stream));
    }

    public static VhdlFile parseStream(InputStream stream, VhdlParserSettings settings, RootDeclarativeRegion rootScope, LibraryDeclarativeRegion libray)
            throws IOException, VhdlParserException {
        return parse(settings, new CaseInsensitiveInputStream(stream), rootScope, libray);
    }

    public static boolean hasParseErrors(VhdlFile file) {
        return Annotations.getAnnotation(file, ParseErrors.class) != null;
    }

    public static List<ParseError> getParseErrors(VhdlFile file) {
        ParseErrors errors = Annotations.getAnnotation(file, ParseErrors.class);
        if (errors == null) {
            return Collections.emptyList();
        } else {
            return errors.getErrors();
        }
    }

    private static String errorToMessage(ParseError error) {
        switch (error.getType()) {
            case UNKNOWN_COMPONENT:
                return "unknown component: " + error.getMessage();
            case UNKNOWN_CONFIGURATION:
                return "unknown configuration: " + error.getMessage();
            case UNKNOWN_CONSTANT:
                return "unknown constant: " + error.getMessage();
            case UNKNOWN_ENTITY:
                return "unknown entity: " + error.getMessage();
            case UNKNOWN_FILE:
                return "unknown file: " + error.getMessage();
            case UNKNOWN_LOOP:
                return "unknown loop: " + error.getMessage();
            case UNKNOWN_OTHER:
                return "unknown identifier: " + error.getMessage();
            case UNKNOWN_PACKAGE:
                return "unknown pacakge: " + error.getMessage();
            case UNKNOWN_SIGNAL:
                return "unknown signal: " + error.getMessage();
            case UNKNOWN_SIGNAL_ASSIGNMENT_TARGET:
                return "unknown signal assignment target: " + error.getMessage();
            case UNKNOWN_TYPE:
                return "unknown type: " + error.getMessage();
            case UNKNOWN_VARIABLE:
                return "unknown variable: " + error.getMessage();
            case UNKNOWN_VARIABLE_ASSIGNMENT_TARGET:
                return "unknown variable assignment target: " + error.getMessage();
            default:
                return "unknown error";
        }
    }

    private static void reportErrors(List<ParseError> errors) {
        for (ParseError error : errors) {
            System.err.println("line " + error.getPosition().getBegin().getLine() + ": "
                    + errorToMessage(error));
        }

    }

    private static class TreeAdaptorWithoutErrorNodes extends CommonTreeAdaptor {

        @Override
        public Object errorNode(TokenStream input, Token start, Token stop, RecognitionException e) {
            return null;
        }
    }
}
