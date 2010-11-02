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

import de.upb.hni.vmagic.Annotations;
import de.upb.hni.vmagic.DeclarativeRegion;
import de.upb.hni.vmagic.LibraryDeclarativeRegion;
import de.upb.hni.vmagic.RootDeclarativeRegion;
import de.upb.hni.vmagic.VhdlElement;
import de.upb.hni.vmagic.parser.VhdlParserSettings;
import de.upb.hni.vmagic.parser.ParseError;
import de.upb.hni.vmagic.parser.annotation.PositionInformation;
import de.upb.hni.vmagic.parser.annotation.SourcePosition;
import de.upb.hni.vmagic.util.Comments;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.antlr.runtime.tree.TreeNodeStream;
import org.antlr.runtime.tree.TreeParser;

/**
 * Abstract base class for the meta class creator.
 */
class AbstractMetaClassCreator extends TreeParser {

    private final List<ParseError> errors = new ArrayList<ParseError>();
    protected DeclarativeRegion currentScope;
    protected final VhdlParserSettings settings;
    protected final LibraryDeclarativeRegion libraryScope;
    protected final RootDeclarativeRegion rootScope;

    public AbstractMetaClassCreator(TreeNodeStream input, RecognizerSharedState state) {
        super(input, state);
        throw new IllegalStateException("Don't call the default ANTLR constructors");
    }

    public AbstractMetaClassCreator(TreeNodeStream input) {
        super(input);
        throw new IllegalStateException("Don't call the default ANTLR constructors");
    }

    public AbstractMetaClassCreator(TreeNodeStream input, VhdlParserSettings settings,
            RootDeclarativeRegion rootScope, LibraryDeclarativeRegion libraryScope) {
        super(input);
        this.settings = settings;
        this.rootScope = rootScope;
        this.libraryScope = libraryScope;
    }

    protected VhdlParserSettings getSettings() {
        return settings;
    }

    protected <T> T resolve(String identifier, Class<T> clazz) {
        if (currentScope != null) {
            return currentScope.getScope().resolve(identifier, clazz);
        }

        return null;
    }

    private SourcePosition tokenToPosition(Token token, boolean start) {
        CommonToken t = (CommonToken) token;
        int index = start ? t.getStartIndex() : t.getStopIndex();
        return new SourcePosition(t.getLine(), t.getCharPositionInLine(), index);
    }

    private PositionInformation treeToPosition(Tree tree) {
        TokenStream tokens = input.getTokenStream();
        CommonToken start = (CommonToken) tokens.get(tree.getTokenStartIndex());
        CommonToken stop = (CommonToken) tokens.get(tree.getTokenStopIndex());

        return new PositionInformation(tokenToPosition(start, true),
                tokenToPosition(stop, false));
    }

    protected void resolveError(Tree tree, ParseError.Type type, String identifier) {
        if (settings.isEmitResolveErrors()) {
            PositionInformation pos = treeToPosition(tree);
            errors.add(new ParseError(pos, type, identifier));
        }
    }

    public List<ParseError> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    private void addPositionAnnotation(VhdlElement element, CommonTree tree) {
        PositionInformation info = treeToPosition(tree);
        Annotations.putAnnotation(element, PositionInformation.class, info);
    }

    private void addCommentAnnotation(VhdlElement element, CommonTree tree) {
        LinkedList<String> comments = new LinkedList<String>();

        for (int i = tree.getTokenStartIndex() - 1; i >= 0; i--) {
            Token t = input.getTokenStream().get(i);

            if (t.getChannel() == VhdlAntlrLexer.CHANNEL_COMMENT) {
                String text = t.getText().substring(2); //strip leading "--"
                comments.addFirst(text);
            } else if (t.getChannel() != VhdlAntlrLexer.HIDDEN) {
                break;
            }
        }

        if (!comments.isEmpty()) {
            Comments.setComments(element, comments);
        }
    }

    protected void addAnnotations(VhdlElement element, CommonTree tree) {
        if (element == null || tree == null) {
            return;
        }

        if (settings.isAddPositionInformation()) {
            addPositionAnnotation(element, tree);
        }

        if (settings.isParseComments()) {
            addCommentAnnotation(element, tree);
        }
    }
}
