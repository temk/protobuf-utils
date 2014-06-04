grammar Grep;

tokens {
    UNARY_MINUS,
    UNARY_PLUS,
    CAST_LONG,
    CAST_DOUBLE,
    CAST_BOOL,
    CAST_STRING
}
// ==============================================
// =================== rules ====================
// ==============================================


query
    : expr EOF
    ;

print
    : print_expr EOF
    ;

print_expr
    : expr (',' expr)* 
    | scope '[' print_expr ']'
    | scope '(' print_expr ')'
    | scope '{' print_expr '}'
    ;

expr
    : cond_or 
    ;

cond_or
    : cond_and (COND_OR cond_and)*
    ;

cond_and
    : bit_or (COND_AND bit_or)*
    ;

bit_or
    : bit_xor (BIT_OR bit_xor)*
    ;

bit_xor
    : bit_and (BIT_XOR bit_and)*
    ;

bit_and
    : cond_eq (BIT_AND cond_eq)*
    ;

cond_eq
    : cond_cmp ((COND_EQ|COND_NEQ|REGEXP) cond_cmp)*
    ;

cond_cmp
    : bit_shift ((COND_GE|COND_LE|COND_GEQ|COND_LEQ) bit_shift)*
    ;

bit_shift
    : add_op ((BIT_SHIFT_LEFT|BIT_SHIFT_RIGHT) add_op)*
    ;

add_op
    : prod_op ((ADD_OP|SUB_OP) prod_op)*
    ;

prod_op
    : unary ((MUL_OP|DIV_OP|REM_OP) unary)*
    ;

unary
    : atomic
    | ADD_OP  unary 
    | SUB_OP  unary 
    | NOT     unary
    | BIT_NOT unary
    | COUNT   unary
    | cast    unary
    ;

cast
    : '(' type = ('long' |'double'|'string'|'bool') ')' 
    ;

    
atomic
    : constant
    | VARIABLE
    | '(' expr ')'
    | reference
    | scoped_expr
    ;

 scoped_expr
    : scope '[' expr ']'
    | scope '(' expr ')'
    | scope '{' expr '}'
    ;

scope
    : reference DOT 
    ;

reference
    : root (DOT field)*
    ;

root
    : ROOT
    | field
    ;

field 
    : (ID|ext) index?  
    ;

index
    : '[' ID ']' 
    | '[' expr ']' 
    ;

ext locals [String extName]
    :  ID '@' ID 
    |  ID '@' '[' ID (DOT ID)* ']'
    ;

constant
    : INTEGER 
    | STRING 
    | FLOAT
    | BOOL
    ;
      
// ==============================================
// ================== tokens ====================
// ==============================================

COND_OR
    : '||'
    ;

COND_AND
    : '&&'
    ;

BIT_OR
    : '|'
    ;

BIT_NOT
    : '~'
    ;

BIT_XOR
    : '^'
    ;

BIT_AND
    : '&'
    ;

COND_EQ
    : '=='
    ;

COND_NEQ
    : '!='
    ;

REGEXP
    : '~='
    ;

COND_GE
    : '>'
    ;

COND_LE
    : '<'
    ;

COND_GEQ
    : '>='
    ;

COND_LEQ
    : '<='
    ;

BIT_SHIFT_LEFT
    : '<<'
    ;

BIT_SHIFT_RIGHT
    : '>>'
    ;

ADD_OP
    : '+'
    ;

SUB_OP
    : '-'
    ;

MUL_OP
    : '*'
    ;

DIV_OP
    : '/'
    ;
REM_OP
    : '%'
    ;

NOT
    : '!'
    ;

COUNT
    : '#'
    ;

ID
    : ('a'..'z' | 'A'..'Z' | '_') ('a'..'z' | 'A'..'Z' | '_' | '0'..'9')*
    ;

VARIABLE
    : '$' ID
    ;

ROOT
    : '$'+
    ;

DOT
    : '.'
    ;

BOOL
    : 'true'
    | 'false'
    ;

INTEGER 
    : ('0'..'9')+ 
    ;

FLOAT	
    : INTEGER
    | INTEGER  '.'
    | INTEGER? '.' INTEGER+
    ;

STRING	
    : '"' ( ~["] )* '"' 
    | '\'' ( ~[\'] )* '\''
    ;

WS
    : [ \t\f\r\n]+ -> skip 
    ;
