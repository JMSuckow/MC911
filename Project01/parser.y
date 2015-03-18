%{
#include <stdio.h>
#include <stdarg.h>
#include <string.h>
#include <stdlib.h>


#define NSTARTED 0
#define STARTED 1
#define FINISHED 2

char *concat(int count, ...);
char* search_table(char* table, char* columns);
char* inputString(FILE* F, size_t size, char ch);
char** split (char* s, char* c, int* n);
void newTitle(char* t);

char* title;

%}
 
%union{
	char *str;
	int  *intval;
}


%token <str> T_STRING
%token T_DOCUMENTCLASS
%token T_USEPACKAGE
%token T_TITLE
%token T_AUTHOR
%token T_MAKETITLE
%token T_TEXTBF
%token T_TEXTIT
%token T_BEGIN
%token T_DOCUMENT
%token T_ITEMIZE
%token T_ITEM
%token T_END
%token T_INCLUDEGRAPHICS
%token T_CITE 
%token T_THEBIBLIOGRAPHY
%token T_BIBITEM
%token T_DOLLARMATH
%token <str> T_DOLLAR
%token <str> T_WHITESPACE
%token <str> T_BREAKLINE


%type <str> string_text aditional_string item_list bibitem_list bibitem

%start text_list

%error-verbose
 
%%

text_list: 	text_list text 
	 |	text 
;

text: ignore
		| command
		| T_DOLLARMATH string_text T_DOLLARMATH
		| T_BREAKLINE string_text 
;
 
string_text: aditional_string { $$ = $1; }
		| string_text aditional_string { $$ = concat(2, $1, $2); }

;

aditional_string: T_DOLLAR { $$ = $1; }
		| T_WHITESPACE { $$ = $1; }
		| T_STRING { $$ = $1; }


ignore: T_DOCUMENTCLASS '[' string_text ']' '{' string_text '}'
		| T_DOCUMENTCLASS '{' string_text '}' '[' string_text ']'
		| T_DOCUMENTCLASS '{' string_text '}'
		| T_DOCUMENTCLASS '[' string_text ']'
		| T_USEPACKAGE '[' string_text ']' '{' string_text '}'
		| T_USEPACKAGE '{' string_text '}' '[' string_text ']'
		| T_USEPACKAGE '{' string_text '}'
		| T_USEPACKAGE '[' string_text ']'
		| T_AUTHOR '{' string_text '}'
; 

command: T_TITLE '{' string_text '}' {	newTitle($3);}
		| T_BEGIN '{' T_DOCUMENT '}'
		| T_END '{' T_DOCUMENT '}'
		| T_BEGIN '{' T_ITEMIZE '}' item_list T_END '{' T_ITEMIZE '}'
		| T_BEGIN '{' T_THEBIBLIOGRAPHY '}' bibitem_list T_END '{' T_BIBITEM '}'
		| T_MAKETITLE
		| T_TEXTBF '{' string_text '}'
		| T_TEXTIT '{' string_text '}'
		| T_INCLUDEGRAPHICS '{' T_STRING '}'
		| T_CITE '{' T_STRING '}'
;


item_list: item_list T_ITEM string_text { $$ = concat(4, $1, "\n<li>", $3, "<\\li>"); }
		| T_ITEM string_text { $$ = concat(3, "<li>", $2, "<\\li>"); }
;

bibitem_list: bibitem_list bibitem { $$ = concat(3, $1, "\n", $2); }
		| bibitem 
;

bibitem: T_BIBITEM '{' T_STRING '}' string_text  { $$ = concat(3, "<li>", $5, "<\\li>"); } //MUDAR
; 
%%

void newTitle(char* t){
	
	title = (char*) calloc(sizeof(char),strlen(t));
	strcpy(title,t);
	
}

char* concat(int count, ...)
{
    va_list ap;
    int len = 1, i;

    va_start(ap, count);
    for(i=0 ; i<count ; i++)
        len += strlen(va_arg(ap, char*));
    va_end(ap);

    char *result = (char*) calloc(sizeof(char),len);
    int pos = 0;

    // Actually concatenate strings
    va_start(ap, count);
    for(i=0 ; i<count ; i++)
    {
        char *s = va_arg(ap, char*);
        strcpy(result+pos, s);
        pos += strlen(s);
    }
    va_end(ap);

    return result;
}


int yyerror(const char* errmsg)
{
	printf("\n*** Erro: %s\n", errmsg);
}
 
int yywrap(void) { return 1; }
 
int main(int argc, char** argv)
{
     yyparse();
     return 0;
}


