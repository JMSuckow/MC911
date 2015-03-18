%{
#include "parser.h"
%}

STRING [a-z][a-z0-9_]*
TABSPACE [\r\t]+
WHITESPACE [ ]+
BREAK_LINE [\n]+
DOLLAR \\$
ANY_CHAR .

%option case-insensitive

%%

{TABSPACE} /* ignore */ ;

"$"			{return T_DOLLARMATH;}
"\\"			{return '\\';}
"{"			{return '{';}
"}"			{return '}';}
"\\documentclass"		{return T_DOCUMENTCLASS;}
"\\usepackage"		{return T_USEPACKAGE;}
"\\title"			{return T_TITLE;}
"\\author"		{return T_AUTHOR;}
"\\maketitle" 		{return T_MAKETITLE;}
"\\textbf" 		{return T_TEXTBF;}
"\\textit"		{return T_TEXTIT;}
"\\begin"			{return T_BEGIN;}
"document"		{return T_DOCUMENT;}
"itemize"		{return T_ITEMIZE;}
"\\item"			{return T_ITEM;}
"\\end"			{return T_END;}
"\\includegraphics"	{return T_INCLUDEGRAPHICS;}
"\\cite"			{return T_CITE;} 
"thebibliography" 	{return T_THEBIBLIOGRAPHY;}
"\\bibitem"		{return T_BIBITEM;}

{DOLLAR} {	
	yylval.str = (char *) strdup(yytext);
	return T_DOLLAR;
}


{WHITESPACE} {	
	yylval.str = (char *) strdup(yytext);
	return T_WHITESPACE;
}

{BREAK_LINE} {	
	yylval.str = (char *) strdup(yytext);
	return T_BREAKLINE;
}


{STRING} {	
	yylval.str = (char *) strdup(yytext);
	return T_STRING;
}

{ANY_CHAR} {
	printf ("Caracter %c (ASCII=%d) inesperado\n", yytext[0], yytext[0]);
}


