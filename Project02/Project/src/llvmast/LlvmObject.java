package llvmast;
public class LlvmObject extends LlvmValue{
	public String className;
	public LlvmRegister register;
	
    public LlvmObject(String className, LlvmRegister reg){
    	this.className = className;
    	this.register = reg;
    	this.type = reg.type;
    }
    
    public String toString(){
    	return register.toString();
    }
}
