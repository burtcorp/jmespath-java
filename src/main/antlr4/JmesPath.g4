grammar JmesPath;

import JSON;

query : expression EOF ;

expression
  : expression '.' (IDENTIFIER | multi_select_list | multi_select_hash | function_expression | '*') # chain_expression
  | expression bracket_specifier # bracketed_expression
  | bracket_specifier # bracket_expression
  | expression '||' expression # or_expression
  | IDENTIFIER # identifier_expression
  | expression '&&' expression # and_expression
  | expression COMPARATOR expression # comparison_expression
  | '!' expression # not_expression
  | '(' expression ')' # paren_expression
  | '*' # wildcard_expression
  | multi_select_list # multi_select_list_expression
  | multi_select_hash # multi_select_hash_expression
  | literal # literal_expression
  | function_expression # function_call_expression
  | expression '|' expression # pipe_expression
  | RAW_STRING # raw_string_expression
  | current_node # current_node_expression
  ;

multi_select_list : '[' expression (',' expression)* ']' ;

multi_select_hash : '{' keyval_expr (',' keyval_expr)* '}' ;

keyval_expr : IDENTIFIER ':' expression ;

bracket_specifier
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
function_expression : IDENTIFIER (no_args | one_or_more_args) ;

no_args : '(' ')' ;

one_or_more_args : '(' function_arg (',' function_arg)* ')' ;

function_arg
  : expression
  | expression_type
  ;

current_node : '@' ;

expression_type : '&' expression ;

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
