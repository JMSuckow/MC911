package llvmast;

public  class LlvmNot extends LlvmInstruction{

	public LlvmRegister lhs;
	public LlvmType type;
   	public LlvmValue b;
	private LlvmBool trueValue;

	public LlvmNot(LlvmRegister lhs, LlvmType type, LlvmValue b){
		this.lhs = lhs;
		this.type = type;
		this.b = b;
		this.trueValue = new LlvmBool(LlvmBool.TRUE);

   	}

    	public String toString(){
		return "  " +lhs + " = xor " + type + " " + ((LlvmBool)b).val + ", " + trueValue.val;
    	}
}
