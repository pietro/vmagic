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

import de.upb.hni.vmagic.parser.annotation.PositionInformation;

/**
 * Parser settings.
 */
public class VhdlParserSettings {

    private boolean createDummyObjects;
    private boolean printErrors;
    private boolean emitResolveErrors;
    private boolean addPositionInformation;

    public VhdlParserSettings() {
        printErrors = true;
        createDummyObjects = true;
        emitResolveErrors = false;
        addPositionInformation = false;
    }

    /**
     * Returns if the parser should create dummy objects.
     * @return <code>true</code>, if the parser should create dummy objects
     */
    public boolean isCreateDummyObjects() {
        return createDummyObjects;
    }

    /**
     * Sets if the parser should create dummy objects.
     * @param createDummyObjects <code>true</code>, if the parser should create dummy objects
     */
    public void setCreateDummyObjects(boolean createDummyObjects) {
        this.createDummyObjects = createDummyObjects;
    }

    /**
     * Returns if resolve errors are emitted.
     * @return <code>true</code>, if resolve errors are emitted
     */
    public boolean isEmitResolveErrors() {
        return emitResolveErrors;
    }

    /**
     * Sets if resolve errors should be emitted.
     * @param emitResolveErrors <code>true<code>, if resolve errors should be emitted
     */
    public void setEmitResolveErrors(boolean emitResolveErrors) {
        this.emitResolveErrors = emitResolveErrors;
    }

    /**
     * Returns if informations about the position in the source file should be stored in the meta
     * class instances.
     * @return <code>true</code>, if the position information should be stored
     */
    public boolean isAddPositionInformation() {
        return addPositionInformation;
    }

    /**
     * Sets if informations about the position in the source file should be stored in the meta
     * class instances. A <code>PositionInformation</code> annotation is used to store this
     * information.
     * @param addPositionInformation <code>true</code>, if the position should be stores
     * @see PositionInformation
     */
    public void setAddPositionInformation(boolean addPositionInformation) {
        this.addPositionInformation = addPositionInformation;
    }

    /**
     * Returns if error messages should be printed to stderr.
     * @return <code>true</code>, if error messages should be printed to stderr
     */
    public boolean isPrintErrors() {
        return printErrors;
    }

    /**
     * Sets if error messages should be printed to stderr.
     * @param printErrors <code>true</code>, if error messages should be printed to stderr
     */
    public void setPrintErrors(boolean printErrors) {
        this.printErrors = printErrors;
    }
}
