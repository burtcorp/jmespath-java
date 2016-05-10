grammar JmesPath;

import JSON;

query : expression EOF ;

expression
  : expression '.' (identifier | multiSelectList | multiSelectHash | functionExpression | '*') # chainExpression
  | expression bracketSpecifier # bracketedExpression
  | bracketSpecifier # bracketExpression
  | expression '||' expression # orExpression
  | identifier # identifierExpression
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

keyvalExpr : identifier ':' expression ;

bracketSpecifier
  : '[' SIGNED_INT ']' # bracketIndex
  | '[' '*' ']' # bracketStar
  | '[' slice ']' # bracketSlice
  | '[' ']' # bracketFlatten
  | '[' '?' expression ']' # select
  ;

slice : start=SIGNED_INT? ':' stop=SIGNED_INT? (':' step=SIGNED_INT?)? ;

COMPARATOR
  : '<'
  | '<='
  | '=='
  | '>='
  | '>'
  | '!='
  ;

functionExpression : NAME (noArgs | oneOrMoreArgs) ;

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

identifier
  : NAME
  | STRING
  ;

NAME : LETTER (LETTER | DIGIT | '_')* ;
