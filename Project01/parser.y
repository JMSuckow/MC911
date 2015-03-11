%{
#include <stdio.h>
#include <stdarg.h>
#include <string.h>
#include <stdlib.h>

char *concat(int count, ...);
char* search_table(char* table, char* columns);
char* inputString(FILE* F, size_t size, char ch);
char** split (char* s, char* c, int* n);

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


%type <str> text

%start stmt_list

%error-verbose
 
%%

stmt_list: 	stmt_list stmt 
	 |	stmt 
;

stmt:
	

;
 
%%

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


