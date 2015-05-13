@.formatting.string = private constant [4 x i8] c"%d\0A\00"
%type.Array = type {i32, i32* }
define i32 @main() {
entry:
  %tmp0 = alloca i32
  store i32 0, i32 * %tmp0
  %tmp1 = mul i32 0, 1
  %tmp2 = call i8*  @malloc(i32 %tmp1)
  %tmp3 = bitcast i8* %tmp2 to %class.Fac* 
  %tmp4 = call i32  @_ComputeFac__Fac(%class.Fac*  %tmp3, i32 10)
  %tmp5 = getelementptr [4 x i8] * @.formatting.string, i32 0, i32 0
  %tmp6 = call i32 (i8 *, ...)* @printf(i8 * %tmp5, i32 %tmp4)
  %tmp7 = load i32 * %tmp0
  ret i32 %tmp7
}
%class.Fac = type {}
define i32 @_ComputeFac__Fac(%class.Fac*  %this, i32 %num) {
entry_ComputeFac__Fac:
  %Fac_ComputeFac_num_tmp = alloca i32
  store i32 %num, i32 * %Fac_ComputeFac_num_tmp
  %Fac_ComputeFac_num_aux_tmp = alloca i32
%tmp8 = icmp slt i32 %num, 1
br i1 %tmp8, label %then0, label %else0
then0:
  %tmp9 = alloca i32
  store i32 1, i32 * %tmp9
  %tmp10 = load i32 * %tmp9
  store i32 %tmp10, i32 * %Fac_ComputeFac_num_aux_tmp
br label %end0
else0:
  %tmp11 = sub i32 %num, 1
  %tmp12 = call i32  @_ComputeFac__Fac(%class.Fac*  %this, i32 %tmp11)
  %tmp13 = mul i32 %num, %tmp12
  store i32 %tmp13, i32 * %Fac_ComputeFac_num_aux_tmp
br label %end0
end0:
  %tmp14 = load i32 * %Fac_ComputeFac_num_aux_tmp
  ret i32 %tmp14
}
declare i32 @printf (i8 *, ...)
declare i8 * @malloc (i32)
