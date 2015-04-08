package llvmast;
public  class LlvmIcmp extends LlvmInstruction{
	
	LlvmRegister lhs;
	Condition cond;
	LlvmType type;
	LlvmValue op1, op2;
    
    public LlvmIcmp(LlvmRegister lhs,  Condition conditionCode, LlvmType type, LlvmValue op1, LlvmValue op2){
		this.lhs = lhs;
		this.cond = conditionCode;
		this.type = type;
		this.op1 = op1;
		this.op2 = op2;
    }

    public String toString(){
		return lhs + " = icmp "+ this.cond.name() + " " + this.type + " " + this.op1 + ", " + this.op2;
    }
}
