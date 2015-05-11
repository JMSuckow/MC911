package llvmast;

public class LlvmAssign extends LlvmInstruction{

	public LlvmValue name;
	public LlvmValue exp;
	
    public LlvmAssign(LlvmValue name, LlvmValue exp){
    	this.name = name;
    	this.exp = exp;
    }
    
    public String toString(){
		return name + " = " + exp;
    }
}
