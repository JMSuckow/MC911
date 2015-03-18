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
void startDocument();
void makeTitle();
void startBody();
void endDocument();
void textBold(char* text);
void textItalic(char* text);
void insertImage(char* imageName);
void newTitle(char* t);
void printList(char* list);

char* title;
char* filename = "Tests/out.html";
int doc_status = NSTARTED;
int head_status = NSTARTED;
int bib_status = NSTARTED;
int body_status = NSTARTED;

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


%type <str> string_text aditional_string item_list bibitem_list bibitem option_item_list blank itemize

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
		| T_BREAKLINE

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
		| T_BEGIN '{' T_DOCUMENT '}'	{ startDocument(); }
		| T_END '{' T_DOCUMENT '}'		{ endDocument(); }
		| itemize {printList($1);}
		| T_BEGIN '{' T_THEBIBLIOGRAPHY '}' bibitem_list T_END '{' T_BIBITEM '}'
		| T_MAKETITLE { makeTitle(); }
		| T_TEXTBF '{' string_text '}' { textBold($3); }
		| T_TEXTIT '{' string_text '}' { textItalic($3); }
		| T_INCLUDEGRAPHICS '{' T_STRING '}' { insertImage($3); }
		| T_CITE '{' T_STRING '}'
;

itemize: T_BEGIN '{' T_ITEMIZE '}' item_list T_END '{' T_ITEMIZE '}' {$$ = concat(3, "\n<ul>", $5, "\n</ul>");}
	| T_BEGIN '{' T_ITEMIZE '}' item_list  T_BREAKLINE T_END '{' T_ITEMIZE '}' {$$ = concat(3, "\n<ul>", $5, "\n</ul>");}
	| T_BEGIN '{' T_ITEMIZE '}' item_list  T_BREAKLINE blank T_END '{' T_ITEMIZE '}' {$$ = concat(3, "\n<ul>", $5, "\n</ul>");}
;

item_list: item_list option_item_list { $$ = concat(2, $1, $2); }
		| item_list T_BREAKLINE option_item_list { $$ = concat(2, $1, $3); }
		| item_list T_BREAKLINE blank option_item_list { $$ = concat(2, $1, $4); }
		| option_item_list { $$ = $1; }
		| T_BREAKLINE option_item_list { $$ = $2; }
		| T_BREAKLINE blank option_item_list { $$ = $3; }
		| item_list itemize { $$ = concat(2, $1, $2); }
		| item_list T_BREAKLINE itemize { $$ = concat(2, $1, $3); }
		| item_list T_BREAKLINE blank itemize { $$ = concat(2, $1, $4); }
;

blank: T_WHITESPACE blank
		| T_WHITESPACE
;

option_item_list: T_ITEM string_text { $$ = concat(3, "\n<li>", $2, "</li>\n"); }
;

bibitem_list: bibitem_list bibitem { $$ = concat(3, $1, "\n", $2); }
		| bibitem 
;

bibitem: T_BIBITEM '{' T_STRING '}' string_text  { $$ = concat(3, "<li>", $5, "</li>"); } //MUDAR
; 
%%

void startDocument(){

	if(doc_status == NSTARTED){
		FILE *F = fopen(filename, "w+"); 
		fprintf(F, "<html>\n<head>\n");
		fprintf(F, "<script type=\"text/x-mathjax-config\"> \nMathJax.Hub.Config({tex2jax: {inlineMath: [['$','$'], ['\\(','\\)']]}});\n </script>");
		fprintf(F, "\n<script type=\"text/javascript\"  \nsrc=\"http://cdn.mathjax.org/mathjax/latest/MathJax.js?config=TeX-AMS-MML_HTMLorMML\"> \n </script>");
		fclose(F);
		doc_status = STARTED;	
		head_status = STARTED;
	}

}

void makeTitle(){

	if(head_status == STARTED){
		FILE *F = fopen(filename, "a"); 
		fprintf(F, "\n<title>%s</title>\n</head>",title);
		fclose(F);
		head_status = FINISHED;
		startBody(); 
		F = fopen(filename, "a"); 
		fprintf(F, "\n<h1 align=\"center\">%s</h1>",title);
		fclose(F);
	}
	else if(head_status == FINISHED && body_status == STARTED){
		FILE *F = fopen(filename, "a"); 
		fprintf(F, "\n<script>document.title = \"%s \";</script>",title);
		fprintf(F, "\n<h1 align=\"center\">%s</h1>",title);
		fclose(F);	
	}

}

void startBody(){

	if(body_status == NSTARTED){
		FILE *F = fopen(filename, "a"); 
		fprintf(F, "\n<body  class=\"tex2jax_ignore\">");
		fclose(F);
		body_status = STARTED;	
	}

}

void endDocument(){

	startDocument();
	
	if(head_status == STARTED){
		FILE *F = fopen(filename, "a"); 
		fprintf(F, "\n</head>");
		fclose(F);
		head_status = FINISHED;	
	}
	
	startBody();
	
	if(body_status == STARTED){
		FILE *F = fopen(filename, "a"); 
		fprintf(F, "\n</body>\n</html>");
		fclose(F);
		body_status = FINISHED;
		doc_status = FINISHED;	
	}

}

void textBold(char* text){
		startBody();
		FILE *F = fopen(filename, "a"); 
		fprintf(F, "<b>%s</b>", text);
		fclose(F);
}

void textItalic(char* text){
		startBody();
		FILE *F = fopen(filename, "a"); 
		fprintf(F, "<i>%s</i>", text);
		fclose(F);
}

void insertImage(char* imageName){
		startBody();
		FILE *F = fopen(filename, "a"); 
		fprintf(F, "<center><img src=\"%s\"></center>", imageName);
		fclose(F);
}

void printList(char* list){
	startBody();
	FILE *F = fopen(filename, "a"); 
	fprintf(F, "%s", list);
	fclose(F);
}

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


