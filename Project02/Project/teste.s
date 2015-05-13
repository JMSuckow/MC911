@.formatting.string = private constant [4 x i8] c"%d\0A\00"
%type.Array = type {i32, i32* }
define i32 @main() {
entry:
  %tmp0 = alloca i32
  store i32 0, i32 * %tmp0
  %tmp1 = mul i32 8, 1
  %tmp2 = call i8*  @malloc(i32 %tmp1)
  %tmp3 = bitcast i8* %tmp2 to %class.a* 
  %tmp4 = call i32  @_i__a(%class.a*  %tmp3)
  %tmp5 = getelementptr [4 x i8] * @.formatting.string, i32 0, i32 0
  %tmp6 = call i32 (i8 *, ...)* @printf(i8 * %tmp5, i32 %tmp4)
  %tmp7 = load i32 * %tmp0
  ret i32 %tmp7
}
%class.a = type {%class.a*}
define %class.a*  @_a__a(%class.a*  %this) {
entry_a__a:
  %tmp8 = getelementptr [4 x i8] * @.formatting.string, i32 0, i32 0
  %tmp9 = call i32 (i8 *, ...)* @printf(i8 * %tmp8, i32 1)
  %tmp10 = mul i32 8, 1
  %tmp11 = call i8*  @malloc(i32 %tmp10)
  %tmp12 = bitcast i8* %tmp11 to %class.a* 
  %a_a_tmp = load %class.a*  %tmp12
  ret %class.a*  %this
}
define i32 @_i__a(%class.a*  %this) {
entry_i__a:
  ret i32 0
}
declare i32 @printf (i8 *, ...)
declare i8 * @malloc (i32)
