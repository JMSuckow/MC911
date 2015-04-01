 == Projeto 2 ==

Este segundo projeto abordará o tópico de geração de representação intermediária.

Descrição
Você deverá implementar uma classe na linguagem Java que recebe a Abstract Syntax Tree do programa e retorna um código em LLVM-IR. O pacote disponibilizado possui um jar (lib/projeto2.jar) contendo todo o código necessário para que a classe Codegen (src/llvm/Codegen.java) seja chamada. Nesta classe há exemplos e comentários que detalham o funcionamento e a implementação do pacote. A parte teórica necessária para a implementação deste laboratório foi vista no curso de MC910, e pode ser revista nos slides da disciplina ou capítulo 7 do Appel (2a edição).

Material
Pacote de apoio: minijava-llvm-v04.tar.bz2 . Sempre que este pacote receber uma atualização será comunicado via grupo e terá um aviso na página. No Makefile possui instruções de como compilar e executar o projeto. Maiores informações em breve.

Documentação das classes:
doc_ast.java : objetos referenciado em Codegen.java.
doc_env.java : classe da Tabela de Simbolos original do compilador.
doc_symbol.java : classes utilizadas pela Tabela de Simbolos Env.

Slides sobre o Projeto 2:
Um_pouco_sobre_llvm .
MiniJava/LLVM 
