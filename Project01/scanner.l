%{
#include "parser.h"
%}

STRING [a-z][a-z0-9_]*
WHITESPACE [ \r\t]+
BREAK_LINE [\n]
ANY_CHAR .

%option case-insensitive

%%

{WHITESPACE} /* ignore */ ;

"$"			{return '$';}
"\"			{return '\';}
"{"			{return '{';}
"}"			{return '}';}
"documentclass"		{return T_DOCUMENTCLASS;}
"usepackage"		{return T_USEPACKAGE;}
"title"			{return T_TITLE;}
"author"		{return T_AUTHOR;}
"maketitle" 		{return T_MAKETITLE;}
"textbf" 		{return T_TEXTBF;}
"textit"		{return T_TEXTIT;}
"begin"			{return T_BEGIN;}
"document"		{return T_DOCUMENT;}
"itemize"		{return T_ITEMIZE;}
"item"			{return T_ITEM;}
"end"			{return T_END;}
"includegraphics"	{return T_INCLUDEGRAPHICS;}
"cite"			{return T_CITE;} 
"thebibliography" 	{return T_THEBIBLIOGRAPHY;}
"bibitem"		{return T_BIBITEM;}


{STRING} {	
	yylval.str = (char *) strdup(yytext);
	return T_STRING;
}

{ANY_CHAR} {
	printf ("Caracter %c (ASCII=%d) inesperado\n", yytext[0], yytext[0]);
}


