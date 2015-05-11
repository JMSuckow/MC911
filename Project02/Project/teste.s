@.formatting.string = private constant [4 x i8] c"%d\0A\00"
%type.Array = type {i32, i32* }
define i32 @main() {
entry:
  %tmp0 = alloca i32
  store i32 0, i32 * %tmp0
  %tmp1 = getelementptr [4 x i8] * @.formatting.string, i32 0, i32 0
  %tmp2 = call i32 (i8 *, ...)* @printf(i8 * %tmp1, i32 0)
  %tmp3 = load i32 * %tmp0
  ret i32 %tmp3
}
%class.a = type {i32*}
define i32 @_i__a(%class.a* %this) {
entry_i__a:
  %tmp4 = mul i32 12, 1
  %tmp5 = call i8*  @malloc(i32 %tmp4)
  %tmp6 = bitcast i8* %tmp5 to %type.Array*
  %tmp7 = getelementptr %type.Array* %tmp6, i32 0, i32 0
  store i32 10, i32* %tmp7
  %tmp8 = alloca i32, i32 10
  %tmp9 = getelementptr %type.Array* %tmp6, i32 0, i32 1
  store i32* %tmp8, i32** %tmp9
  %tmp10 = load %type.Array* %tmp6
  ret i32 0
}
declare i32 @printf (i8 *, ...)
declare i8 * @malloc (i32)
