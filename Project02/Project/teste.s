@.formatting.string = private constant [4 x i8] c"%d\0A\00"
%type.Array = type {i32, i32* }
declare i32 @printf (i8 *, ...)
declare i8 * @malloc (i32)
