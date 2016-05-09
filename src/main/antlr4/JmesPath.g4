grammar JmesPath;

import JSON;

query : expression EOF ;

expression
  : expression '.' (IDENTIFIER | multiSelectList | multiSelectHash | functionExpression | '*') # chainExpression
  | expression bracketSpecifier # bracketedExpression
  | bracketSpecifier # bracketExpression
  | expression '||' expression # orExpression
  | IDENTIFIER # identifierExpression
  | expression '&&' expression # andExpression
  | expression COMPARATOR expression # comparisonExpression
  | '!' expression # notExpression
  | '(' expression ')' # parenExpression
  | '*' # wildcardExpression
  | multiSelectList # multiSelectListExpression
  | multiSelectHash # multiSelectHashExpression
  | literal # literalExpression
  | functionExpression # functionCallExpression
  | expression '|' expression # pipeExpression
  | RAW_STRING # rawStringExpression
  | currentNode # currentNodeExpression
  ;

multiSelectList : '[' expression (',' expression)* ']' ;

multiSelectHash : '{' keyvalExpr (',' keyvalExpr)* '}' ;

keyvalExpr : IDENTIFIER ':' expression ;

bracketSpecifier
  : '[' (SIGNED_INT | '*' | SLICE_EXPRESSION) ']'
  | '[]'
  | '[' '?' expression ']'
  ;

SLICE_EXPRESSION : SIGNED_INT? ':' SIGNED_INT? (':' SIGNED_INT?)? ;

COMPARATOR
  : '<'
  | '<='
  | '=='
  | '>='
  | '>'
  | '!='
  ;

// TODO: should be NAME and not IDENTIFIER, but that doesn't work
functionExpression : IDENTIFIER (noArgs | oneOrMoreArgs) ;

noArgs : '(' ')' ;

oneOrMoreArgs : '(' functionArg (',' functionArg)* ')' ;

functionArg
  : expression
  | expressionType
  ;

currentNode : '@' ;

expressionType : '&' expression ;

RAW_STRING : '\'' (RAW_ESC | ~['\\])* '\'' ;

fragment RAW_ESC : '\\' ['\\] ;

literal : '`' value '`' ;

SIGNED_INT : '-'? DIGIT+ ;

DIGIT : [0-9] ;

LETTER : [a-zA-Z] ;

IDENTIFIER
  : NAME
  | STRING
  ;

NAME : LETTER (LETTER | DIGIT | '_')* ;
