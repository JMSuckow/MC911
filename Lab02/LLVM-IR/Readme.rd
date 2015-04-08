Construam em LLVM-IR uma classe Conta, com as seguintes características:

    atributo num_conta: inteiro, não precisa inicializar
    atributo saldo: inteiro, não precisa inicializar
    método sacar, que recebe o valor do saque. Não precisa checar saldo negativo.
    método depositar, que recebe o valor do depósito.
    método consultar, que retorna o valor do saldo

Acrescente agora a classe Poupança herdada de Conta com as seguintes características:

    atributo dia_rendimento: inteiro, não precisa inicializar
    método atualizarSaldo, que recebe a taxa de rendimento (de 0 a 100) e atualiza o saldo.

Seu arquivo “classes.s” deve conter apenas a implementação dessas classes solicitadas. A função “main”, necessária para a execução, será fornecida no conjunto de testes. O pacote disponível possui um "main.s" de exemplo e um "Makefile". Adicione seu "classes.s" no mesmo diretório e use o comando “make”. O executável resultante será “banco”. A saída esperada para esse "main.s" é 330.

Observações:

    A fim de padronizar o nome das funções, utilize os nomes “mangling” que estão declarados em "main.s."
    Além das instruções aritméticas conhecidas mul, sub e add, no método atualizarSaldo deve-se utilizar a instrução sdiv , que possui a sintaxe idêntica à mul .
    A entrada deste laboratório é apenas um arquivo denominado "classes.s" que contém código em LLVM-IR.
    O Susy apenas receberá o pacote; não realizará correções

