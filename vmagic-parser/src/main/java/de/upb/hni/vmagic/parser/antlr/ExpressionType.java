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

import de.upb.hni.vmagic.expression.Add;
import de.upb.hni.vmagic.expression.And;
import de.upb.hni.vmagic.expression.Concatenate;
import de.upb.hni.vmagic.expression.Divide;
import de.upb.hni.vmagic.expression.Equals;
import de.upb.hni.vmagic.expression.Expression;
import de.upb.hni.vmagic.expression.GreaterEquals;
import de.upb.hni.vmagic.expression.GreaterThan;
import de.upb.hni.vmagic.expression.LessEquals;
import de.upb.hni.vmagic.expression.LessThan;
import de.upb.hni.vmagic.expression.Mod;
import de.upb.hni.vmagic.expression.Multiply;
import de.upb.hni.vmagic.expression.Nand;
import de.upb.hni.vmagic.expression.Nor;
import de.upb.hni.vmagic.expression.NotEquals;
import de.upb.hni.vmagic.expression.Or;
import de.upb.hni.vmagic.expression.Rem;
import de.upb.hni.vmagic.expression.Rol;
import de.upb.hni.vmagic.expression.Ror;
import de.upb.hni.vmagic.expression.Sla;
import de.upb.hni.vmagic.expression.Sll;
import de.upb.hni.vmagic.expression.Sra;
import de.upb.hni.vmagic.expression.Srl;
import de.upb.hni.vmagic.expression.Subtract;
import de.upb.hni.vmagic.expression.Xnor;
import de.upb.hni.vmagic.expression.Xor;

/**
 * Expression type.
 */
enum ExpressionType {

    AND {

        Expression create(Expression l, Expression r) {
            return new And(l, r);
        }
    },
    OR {

        Expression create(Expression l, Expression r) {
            return new Or(l, r);
        }
    },
    NAND {

        Expression create(Expression l, Expression r) {
            return new Nand(l, r);
        }
    },
    NOR {

        Expression create(Expression l, Expression r) {
            return new Nor(l, r);
        }
    },
    XOR {

        Expression create(Expression l, Expression r) {
            return new Xor(l, r);
        }
    },
    XNOR {

        Expression create(Expression l, Expression r) {
            return new Xnor(l, r);
        }
    },
    EQ {

        Expression create(Expression l, Expression r) {
            return new Equals(l, r);
        }
    },
    NEQ {

        Expression create(Expression l, Expression r) {
            return new NotEquals(l, r);
        }
    },
    LT {

        Expression create(Expression l, Expression r) {
            return new LessThan(l, r);
        }
    },
    LE {

        Expression create(Expression l, Expression r) {
            return new LessEquals(l, r);
        }
    },
    GT {

        Expression create(Expression l, Expression r) {
            return new GreaterThan(l, r);
        }
    },
    GE {

        Expression create(Expression l, Expression r) {
            return new GreaterEquals(l, r);
        }
    },
    SLL {

        Expression create(Expression l, Expression r) {
            return new Sll(l, r);
        }
    },
    SRL {

        Expression create(Expression l, Expression r) {
            return new Srl(l, r);
        }
    },
    SLA {

        Expression create(Expression l, Expression r) {
            return new Sla(l, r);
        }
    },
    SRA {

        Expression create(Expression l, Expression r) {
            return new Sra(l, r);
        }
    },
    ROL {

        Expression create(Expression l, Expression r) {
            return new Rol(l, r);
        }
    },
    ROR {

        Expression create(Expression l, Expression r) {
            return new Ror(l, r);
        }
    },
    ADD {

        Expression create(Expression l, Expression r) {
            return new Add(l, r);
        }
    },
    SUB {

        Expression create(Expression l, Expression r) {
            return new Subtract(l, r);
        }
    },
    CONCAT {

        Expression create(Expression l, Expression r) {
            return new Concatenate(l, r);
        }
    },
    MUL {

        Expression create(Expression l, Expression r) {
            return new Multiply(l, r);
        }
    },
    DIV {

        Expression create(Expression l, Expression r) {
            return new Divide(l, r);
        }
    },
    MOD {

        Expression create(Expression l, Expression r) {
            return new Mod(l, r);
        }
    },
    REM {

        Expression create(Expression l, Expression r) {
            return new Rem(l, r);
        }
    };

    abstract Expression create(Expression l, Expression r);
}
