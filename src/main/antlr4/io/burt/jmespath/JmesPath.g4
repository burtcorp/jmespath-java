grammar JmesPath;

query : expression EOF ;

expression
  : expression '.' (identifier | multiSelectList | multiSelectHash | functionExpression | wildcard='*') # chainExpression
  | expression bracketSpecifier # bracketedExpression
  | bracketSpecifier # bracketExpression
  | expression COMPARATOR expression # comparisonExpression
  | expression '&&' expression # andExpression
  | expression '||' expression # orExpression
  | identifier # identifierExpression
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

DIGIT : [0-9] ;

LETTER : [a-zA-Z] ;

identifier
  : NAME
  | STRING
  ;

NAME : LETTER (LETTER | DIGIT | '_')* ;

json
  : object
  | array
  ;

object
  : '{' pair (',' pair)* '}'
  | '{' '}'
  ;

pair
  : STRING ':' value
  ;

array
  : '[' value (',' value)* ']'
  | '[' ']'
  ;

value
  : STRING
  | (REAL_OR_EXPONENT_NUMBER | SIGNED_INT)
  | object
  | array
  | 'true'
  | 'false'
  | 'null'
  ;

STRING
  : '"' (ESC | ~ ["\\])* '"'
  ;

fragment ESC
  : '\\' (["\\/bfnrt] | UNICODE)
  ;

fragment UNICODE
  : 'u' HEX HEX HEX HEX
  ;

fragment HEX
  : [0-9a-fA-F]
  ;

REAL_OR_EXPONENT_NUMBER
  : '-'? INT '.' [0-9] + EXP?
  | '-'? INT EXP
  ;

SIGNED_INT : '-'? INT ;

fragment INT
  : '0'
  | [1-9] [0-9]*
  ;

fragment EXP
  : [Ee] [+\-]? INT
  ;

WS
  : [ \t\n\r] + -> skip
  ;
