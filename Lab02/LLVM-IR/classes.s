%class.Conta = type { i32, i32 }
%class.Poupanca = type { %class.Conta, i32 }

define void @__sacar_conta(%class.Conta * %this, i32 %val) {
 entry0:
 %ptr_saldo = getelementptr %class.Conta * %this, i32 0, i32 1
	%saldo = load i32* %ptr_saldo
 %tmp0 = sub i32 %saldo, %val
 store i32 %tmp0, i32 * %ptr_saldo
	ret void
}

define void @__depositar_conta(%class.Conta * %this, i32 %val) {
 entry0:
 %ptr_saldo = getelementptr %class.Conta * %this, i32 0, i32 1
	%saldo = load i32* %ptr_saldo
 %tmp0 = add i32 %saldo, %val
 store i32 %tmp0, i32 * %ptr_saldo
	ret void
}

define i32 @__consultar_conta(%class.Conta * %this) {
 entry0:
 %ptr_saldo = getelementptr %class.Conta * %this, i32 0, i32 1
	%saldo = load i32* %ptr_saldo
 ret i32 %saldo
}


define void @__atualizarSaldo_poupanca(%class.Poupanca* %this, i32 %tax){
 entry0:
 %addr2 = alloca i32
 store i32 %tax, i32 * %addr2
 %addr3 = alloca i32
 store i32 100, i32 * %addr3
 
 %saldo_tmp = getelementptr %class.Poupanca * %this, i32 0, i32 0
 %ptr_saldo = getelementptr %class.Conta * %saldo_tmp, i32 0, i32 1
	%saldo = load i32* %ptr_saldo

 %tmp0 = mul i32 %saldo, %tax
 %tmp1 = sdiv i32 %tmp0, 100
 %tmp2 = add i32 %tmp1, %saldo

 store i32 %tmp2, i32 * %ptr_saldo

	ret void

}

