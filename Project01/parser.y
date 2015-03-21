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
void print(char* string);
void printReferencias(char* ref);

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
%token T_BREAKLINE


%type <str> string_text aditional_string item_list bibitem_list bibitem option_item_list blank itemize bibitemize formatted_text

%start text_list

%error-verbose
 
%%

text_list: 	text_list text 
	 |	text 
;

text: ignore
		| T_WHITESPACE command
		| command
		| string_text T_DOLLARMATH string_text T_DOLLARMATH {print(concat(4, $1, " <span class=\"tex2jax_process\">$", $3, "$</span>"));}
		| T_DOLLARMATH string_text T_DOLLARMATH {print(concat(3, " <span class=\"tex2jax_process\">$", $2, "$</span>"));}
		| string_text T_BREAKLINE {print(concat(2, $1," "));}
		| string_text T_CITE '{' T_STRING '}' {print(concat(5, " <a href=\"#", $4, "\">", $4, "</a>"));}
		| T_BREAKLINE {if(doc_status!=FINISHED)print("<br/>");}

;

formatted_text:  string_text T_BREAKLINE formatted_text {$$ = concat(3, $1, " ",$3);}
					| string_text {$$ = $1;}
					| string_text T_BREAKLINE {$$ = $1;}
;
 
string_text: aditional_string { $$ = $1; }
		| string_text aditional_string { $$ = concat(2, $1, $2); }

;

aditional_string: T_DOLLAR { $$ = $1; }
		| T_WHITESPACE { $$ = $1; }
		| T_STRING {  $$ = $1; }
;

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
		| itemize {print($1);}
		| bibitemize {print($1);}
		| T_MAKETITLE { makeTitle(); }
		| T_TEXTBF '{' string_text '}' { textBold($3); }
		| T_TEXTIT '{' string_text '}' { textItalic($3); }
		| T_INCLUDEGRAPHICS '{' T_STRING '}' { insertImage($3); }
;

itemize: T_BEGIN '{' T_ITEMIZE '}' item_list T_END '{' T_ITEMIZE '}' {$$ = concat(3, "\n<ul>", $5, "\n</ul>");}
	| T_BEGIN '{' T_ITEMIZE '}' item_list  blank T_END '{' T_ITEMIZE '}' {$$ = concat(3, "\n<ul>", $5, "\n</ul>");}
;

item_list: item_list option_item_list { $$ = concat(2, $1, $2); }
		| item_list blank option_item_list { $$ = concat(2, $1, $3); }
		| option_item_list { $$ = $1; }
		| blank option_item_list { $$ = $2;}
		| T_WHITESPACE option_item_list { $$ = $2;}
		| item_list itemize { $$ = concat(2, $1, $2); }
		| item_list blank itemize { $$ = concat(2, $1, $3); }
;

blank:	T_BREAKLINE T_WHITESPACE {$$ = $2;}
		| T_BREAKLINE {$$ = "";} 
;				

option_item_list: T_ITEM formatted_text { $$ = concat(3, "\n<li>", $2, "</li>"); }
					| T_ITEM '[' string_text ']' formatted_text { $$ = concat(5, "\n<table cellspacing=10><tr><td align=\"center\">", $3,"</td><td align=\"center\">", $5,"</td></tr></table>"); }
;

bibitemize:  T_BEGIN '{' T_THEBIBLIOGRAPHY '}' bibitem_list T_END '{' T_THEBIBLIOGRAPHY '}' {printReferencias(concat(3, "\n<ul>",$5,"</ul>"));}
	| T_BEGIN '{' T_THEBIBLIOGRAPHY '}' bibitem_list blank T_END '{' T_THEBIBLIOGRAPHY '}' {printReferencias(concat(3, "\n<ul>",$5,"</ul>"));}
;

bibitem_list: bibitem_list bibitem { $$ = concat(2, $1, $2); }
		| bibitem_list blank bibitem { $$ = concat(2, $1, $3); }
		| bibitem { $$ = $1; }
		| blank bibitem { $$ = $2;}
;

bibitem: T_BIBITEM '{' T_STRING '}' formatted_text { $$ = concat(7, "\n<a name=\"", $3 ,"\"><table cellspacing=10><tr><td align=\"center\">", $3,"</td><td align=\"center\">", $5,"</td></tr></table></a>"); }
; 
%%

void printReferencias(char* ref){

	if(bib_status == NSTARTED){
		FILE *F = fopen(filename, "a");
		fprintf(F, "<b>Referencias</b><br/>");
		fprintf(F, "%s", ref);
		fclose(F);
		bib_status = STARTED;	
	}

}

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
		fprintf(F, "<br/><b>%s</b>", text);
		fclose(F);
}

void textItalic(char* text){
		startBody();
		FILE *F = fopen(filename, "a"); 
		fprintf(F, "<br/><i>%s</i>", text);
		fclose(F);
}

void insertImage(char* imageName){
		startBody();
		FILE *F = fopen(filename, "a"); 
		fprintf(F, "<center><img src=\"%s\"></center>", imageName);
		fclose(F);
}

void print(char* string){
	startBody();
	FILE *F = fopen(filename, "a"); 
	fprintf(F, "%s", string);
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


