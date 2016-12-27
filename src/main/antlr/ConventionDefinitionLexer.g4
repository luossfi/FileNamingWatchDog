/*
 * FileNamingWatchDog, a library for checking Java packages and source file
 * names for compliance to naming conventions.
 *
 * Copyright (C) 2016++ Steff Lukas <steff.lukas@luossfi.org>
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

lexer grammar ConventionDefinitionLexer;

@header
{
  package org.luossfi.gen.antlr.fnwd;
}

PACKAGE
  : P A C K A G E
  ;

FILE
  : F I L E
  ;

STRING_OPEN
  : '"' -> mode( STRING_MODE )
  ;

CHAR_GROUP_OPEN
  : '[' -> mode( CHAR_GROUP_MODE )
  ;

LCURLY
  : '{'
  ;


RCURLY
  : '}'
  ;

LPAREN
  : '('
  ;

RPAREN
  : ')'
  ;

PIPE
  : '|'
  ;

MULTIPLIER
  : ( '*' | '+' | '?' ) '?'?
  ;

WILDCARD
  : '.'
  ;

COMMENT
  : '#'.*?'\r'?'\n' -> skip
  ;

WS
  : [ \t\n\r]+ -> skip
  ;

fragment A: [aA];
fragment B: [bB];
fragment C: [cC];
fragment D: [dD];
fragment E: [eE];
fragment F: [fF];
fragment G: [gG];
fragment H: [hH];
fragment I: [iI];
fragment J: [jJ];
fragment K: [kK];
fragment L: [lL];
fragment M: [mM];
fragment N: [nN];
fragment O: [oO];
fragment P: [pP];
fragment Q: [qQ];
fragment R: [rR];
fragment S: [sS];
fragment T: [tT];
fragment U: [uU];
fragment V: [vV];
fragment W: [wW];
fragment X: [xX];
fragment Y: [yY];
fragment Z: [zZ];

mode STRING_MODE;

STRING_CLOSE
  : '"' -> mode( DEFAULT_MODE )
  ;

PLACEHOLDER
  : '%' .*? '%'
  ;

STRING_LITERAL
  : ~[%"]+
  ;

mode CHAR_GROUP_MODE;

CHAR_GROUP_CLOSE
  : ']' -> mode ( DEFAULT_MODE )
  ;

CHAR_RANGE
  : '\\'?. '-' '\\'?.
  ;

SINGLE_CHAR
  : '\\'?.
  ;


