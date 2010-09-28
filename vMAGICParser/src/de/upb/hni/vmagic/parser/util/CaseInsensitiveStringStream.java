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

package de.upb.hni.vmagic.parser.util;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;

/**
 * A case insensitive version of ANTLRStringStream.
 * CaseInsensitiveStringStream modifies the lookahead to be lower case only.
 * The case inside the token text is preserved for further usage.
 */
public class CaseInsensitiveStringStream extends ANTLRStringStream {
	/**
	 * Create new stream by calling the super class constructor.
	 * @param input the input string
	 */
	public CaseInsensitiveStringStream(String input) {
		super(input);
	}
	
	/**
	 * Returns the lookahead as a lower case char.
	 * @param i the amount of lookahead
	 * @return the character at the position i
	 */
	@Override
	public int LA(int i) {
		if (i == 0) {
			return 0;
		}
		
		if (i < 0) { 
			i++;
			if ((p + i - 1) < 0) {
				return CharStream.EOF;
			}
		}
		
		if ((p + i - 1) >= n) {
			return CharStream.EOF;
		}		

		return Character.toLowerCase(data[p + i - 1]);
	}
}
