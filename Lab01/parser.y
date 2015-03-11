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
%token T_SELECT
%token T_FROM
%token T_CREATE
%token T_TABLE
%token T_INSERT
%token T_INTO
%token T_VALUES


%type <str> create_stmt insert_stmt select_stmt col_list  values_list

%start stmt_list

%error-verbose
 
%%

stmt_list: 	stmt_list stmt 
	 |	stmt 
;

stmt:
		create_stmt ';'	{printf("%s",$1);}
	|	insert_stmt ';'	{printf("%s",$1);}
	|	select_stmt ';' {printf("%s",$1);}

;

create_stmt:
	   T_CREATE T_TABLE T_STRING '(' col_list ')' 	{	FILE *F = fopen($3, "w"); 
								fprintf(F, "%s\n", $5);
								fclose(F);
								$$ = concat(5, "\nCREATE TABLE: ", $3, "\nCOL_NAME: ", $5, "\n\n");
							}
;

col_list:
		T_STRING 		{ $$ = $1; }
	| 	col_list ',' T_STRING 	{ $$ = concat(3, $1, ";", $3); }
;


insert_stmt:
	   T_INSERT T_INTO T_STRING T_VALUES '(' values_list ')' { FILE *F = fopen($3, "a"); 
								  fprintf(F, "%s\n", $6);
								  fclose(F);
								  $$ = concat(5, "\nINSERT INTO TABLE: ", $3, "\nVALUES: ", $6, "\n\n");
							 	}
;

values_list:
		T_STRING 		{ $$ = $1; }
	| 	col_list ',' T_STRING 	{ $$ = concat(3, $1, ";", $3); }
;

select_stmt:
		T_SELECT '*' T_FROM T_STRING { 	char *s = search_table($4, "*");
						$$ = concat(5, "\nSELECT '*' FROM ", $4, ":\n", s, "\n\n");
					     }
	|	T_SELECT col_list T_FROM T_STRING {char *s = search_table($4, $2);
						$$ = concat(6, "\nSELECT ", $2, " FROM ", $4, ":\n", s, "\n\n");
					     }
;
 
%%

char* inputString(FILE* F, size_t size, char ch){
    char *str;
    int c;
    size_t len = 0;
    str = realloc(NULL, sizeof(char)*size);//size is start size
    if(!str)return str;
    while(EOF!=(c=fgetc(F)) && c != ch){
        str[len++]=c;
        if(len==size){
            str = realloc(str, sizeof(char)*(size+=16));
            if(!str)return str;
        }
    }
    str[len++]='\0';

    return realloc(str, sizeof(char)*len);
}

char** split (char* s, char* c, int* n){
		char ** array  = NULL;
		char *  p    = strtok (s, c);
		int n_spaces = 0;

		while (p) {
  			array  = realloc (array , sizeof (char*) * ++n_spaces);

  			array [n_spaces-1] = p;

  			p = strtok (NULL, c);
		}

		array  = realloc (array , sizeof (char*) * (n_spaces+1));
		array [n_spaces] = 0;
		*n = n_spaces;
		return array;
}

char* search_table(char* table, char* columns)
{
	FILE *F = fopen(table, "r"); 
	
	char* columns_all = inputString(F, 1, '\n');
	char* values = "";
	char* val_tmp;
	int n_total=0, n_req=0, n_val=0;

	if(strlen(columns)== 1 && columns[0] == '*'){
		values = inputString(F, 1, EOF); 
	}
	else{
		char** col_array  = split(columns_all, ";", &n_total);
		char** req_col_array = split(columns, ";",&n_req);
		int col_order[n_req];
		int i, j;


		for(i=0;i<n_req;i++){
			for(j=0;j<n_total;j++){
				if(strcmp(col_array[j],req_col_array[i]) == 0)
					col_order[i] = j;
			}
		}

		val_tmp =  inputString(F, 1, EOF);

			char** val_tmp_array = split(val_tmp, "\n", &n_val);
			char** val_array;

			for(i=0; i< n_val;i++){
				val_array = split(val_tmp_array[i], ";", &n_total);

				for(j=0; j < n_req;j++){
										
					if(j>0)
						values = concat(3, values, ";", val_array[col_order[j]]);
					else
						values = concat(2, values,val_array[col_order[j]]);
				}

				values = concat(2, values, "\n");
			}		

	}
	
	
	fclose(F);

	return values;
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


