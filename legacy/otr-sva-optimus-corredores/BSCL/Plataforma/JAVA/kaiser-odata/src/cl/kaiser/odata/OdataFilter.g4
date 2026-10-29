grammar OdataFilter;

prog: boolexpr EOF;								

/*----------------------------------------
  Expresiones Booleanas
  - (A or B) and (C) and (D or B)
------------------------------------------*/
boolexpr	:'(' boolexpr ')'							# Group
			|boolexpr 'or' boolexpr						# OperatorOr
			|boolexpr 'and' boolexpr 					# OperatorAnd
			|condboolexpr								# OperatorBool
			; 

/*----------------------------------------
  Condiciones Booleanas
  - a eq b
  - substringof(a,b)
------------------------------------------*/
condboolexpr	: condexpr  'eq' condexpr										#Eq			
				| condexpr  'ne' condexpr										#Ne	
				| condexpr  'ge' condexpr										#Qe	
				| condexpr  'gt' condexpr										#Qt	
				| condexpr  'le' condexpr										#Le	
				| condexpr  'lt' condexpr										#Lt	
				| condexpr  'eq null'  											#EqNull	
				| condexpr  'ne null' 											#NqNull
				| condexpr  'eq \'\''  											#EqEmpty
				| condexpr  'ne \'\'' 											#NqEmpty
				| 'startswith' '(' condexpr  ','  condexpr ')'					#Startswith 
				| 'startswith' '(' condexpr  ','  condexpr ')' 'eq false'		#NqStartswith 
				| 'endswith' '(' condexpr  ','  condexpr ')'					#Endswith				
				| 'substringof' '(' condexpr  ','  condexpr ')'					#Substringof				
				| 'substringof' '(' condexpr  ','  condexpr ')'	'eq false'		#NqSubstringof				
				;
				
condexpr		: 'datetime' field									#toDatetime
				| 'tolower' '(' field ')'							#toLower
				| 'toupper' '(' field ')'							#toUpper
				| field												#toField  
				;

field:StringLiteral|CharacterLiteral|IdentName|FloatingPointLiteral|Number|Variable;		

/*----------------------------------------
Token normales
------------------------------------------*/
Number 	   : '-'? Digit+;
Variable   : (Letter)+ Digit*  ;
IdentName  : (Letter|'_')+ (Letter|Digit|'_'|'-')*;
CharacterLiteral :   '\'' ( EscapeSequence | ~('\''|'\\') )* '\'';
StringLiteral :  '"' ( EscapeSequence | ~('\\'|'"') )* '"';
FloatingPointLiteral :   '-'? ('0'..'9')+ '.' ('0'..'9')* Exponent? FloatTypeSuffix? |   '-'? '.' ('0'..'9')+ Exponent? FloatTypeSuffix? |   '-'? ('0'..'9')+ Exponent FloatTypeSuffix? |   '-'? ('0'..'9')+ FloatTypeSuffix ;
fragment Digit : '0'..'9';
fragment Letter : 'a'..'z'|'A'..'Z';
fragment EscapeSequence :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')| UnicodeEscape|   OctalEscape;
fragment OctalEscape :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')|   '\\' ('0'..'7') ('0'..'7')|   '\\' ('0'..'7');
fragment UnicodeEscape :   '\\' 'u' HexDigit HexDigit HexDigit HexDigit;
fragment HexDigit : ('0'..'9'|'a'..'f'|'A'..'F') ;
fragment Exponent : ('e'|'E') ('+'|'-')? ('0'..'9')+ ;
fragment FloatTypeSuffix : ('f'|'F'|'d'|'D') ;
WS  :  (' '|'\r'|'\t'|'\u000C'|'\n') -> skip;

