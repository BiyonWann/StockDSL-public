grammar StockDSL;

@header {
package stockdsl;
}

program: strategyBlock+ EOF;

strategyBlock
    : 'strategy' STRING '{' statement* '}'
    ;

statement
    : configStmt
    | rule
    ;

rule
    : 'if' expr '{' action+ '}'
    ;

action
    : ('buy' | 'sell') SYMBOL
    ;


expr
    : logicalTerm ( 'or' logicalTerm )*
    ;

logicalTerm
    : comparison ( 'and' comparison )*
    ;

comparison
    : primary ( comparator primary )?
    ;


primary
    : functionCall
    | NUMBER
    | IDENTIFIER
    | SYMBOL
    | '(' expr ')'
    ;

functionCall
    : (IDENTIFIER | SYMBOL) '(' argList? ')'
    ;

argList
    : expr (',' expr)*
    ;


configStmt
    : 'use' STRING
    | 'symbols' ':' symbolList
    | 'capital' ':' DOLLAR
    | 'timeframe' ':' TIMEFRAME
    | 'period' ':' STRING 'to' STRING
    | 'risk_per_trade' ':' PERCENT
    ;


symbolList: SYMBOL (',' SYMBOL)*;

comparator: '<' | '>' | '<=' | '>=' | '==' | '!=';

// --- LEXER RULES ---

STRING      : '"' ( ~["\\] | '\\' . )* '"';
SYMBOL      : [A-Z]+ ('-' [A-Z]+)?;
DOLLAR      : '$' [0-9]+;
PERCENT     : [0-9]+ '%';
TIMEFRAME   : 'daily' | 'weekly' | 'monthly';
NUMBER      : '-'? [0-9]+ ('.' [0-9]+)?; // also can be negative


IDENTIFIER  : [a-zA-Z_][a-zA-Z0-9_]*;

COMMENT     : '//' ~[\r\n]* -> skip;

WS          : [ \t\r\n]+ -> skip;
