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

parser grammar ConventionDefinitionParser;

@header
{
  package org.luossfi.gen.antlr.fnwd;
}

options
{
  tokenVocab = ConventionDefinitionLexer;
}

conventionDefinition
  : packageDefinition*
  ;

packageDefinition
  : PACKAGE alternatives LCURLY fileDefinition+ RCURLY
  ;

fileDefinition
  : FILE alternatives
  ;

alternatives
  : compositeExpression ( PIPE compositeExpression )*
  ;

compositeExpression
  : multipliableExpression+
  ;

multipliableExpression
  : expression MULTIPLIER?
  ;

expression
  : simpleExpression            #simpleExpr
  | LPAREN alternatives RPAREN  #groupedExpr
  ;

simpleExpression
  : STRING_OPEN ( stringContent )* STRING_CLOSE                     #stringExpr
  | WILDCARD                                                        #wildcardExpr
  | CHAR_GROUP_OPEN ( CHAR_RANGE | SINGLE_CHAR )* CHAR_GROUP_CLOSE  #charGroupExpr
  ;

stringContent
  : STRING_LITERAL | PLACEHOLDER
  ;
