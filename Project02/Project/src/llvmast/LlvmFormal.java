package llvmast;

public class LlvmFormal extends LlvmValue{

	public LlvmValue name;
	
    public LlvmFormal(LlvmValue name, LlvmValue type){
    	this.name = name;
    	this.type = type.type;
    }
    
    public String toString(){
		return name.toString();
    }
}
