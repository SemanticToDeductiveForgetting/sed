/*
 * Grammer file of ALC DL
 *
 */

grammar alc;

 file
   :axiom (ENDLINE axiom)* ENDLINE* EOF
   ;

 axiom
   : LPAREN axiom RPAREN
   | formula SUBSET formula
   | formula EQUIV formula
   ;

 formula
   : NOT formula
   | LPAREN formula RPAREN
   | formula bin_connective formula
   | FORALL role DOT formula
   | EXISTS role DOT formula
   | atomic
   ;

atomic
    : (CHARACTER | '_' | '-')*
    | TOP
    | BOTTOM
    ;

bin_connective
   : CONJ
   | DISJ
   ;
//used in FORALL|EXISTS and following predicates
role
   : (CHARACTER | '_' | '-')*
   ;

DOT
   : '.'
   ;
LPAREN
   :'('
   ;
RPAREN
   :')'
   ;
NOT
   :'!' | '*NOT*' | '¬' | '*not*' | '*Not*'
   ;
FORALL
   :'*Forall*' | '*forall*' | '*FORALL*'
   ;
EXISTS
   :'*Exists*' | '*exists*' | '*EXISTS*'
   ;
CHARACTER
   :('0' .. '9' | 'a' .. 'z' | 'A' .. 'Z')
   ;
CONJ
   :'/\\' | '&'
   ;
DISJ
   :'^' | '\\/' | '|'
   ;
SUBSET
   :'->'
   ;
EQUIV
   :'='
   ;
ENDLINE
   :('\r'|'\n')+
   ;
WHITESPACE
   :(' '|'\t')+->skip
   ;
TOP
   :'*Top*' | '*TOP*' | '*top*'
   ;
BOTTOM
   :'*Bottom*' | '*BOTTOM*' | '*bottom*'
   ;