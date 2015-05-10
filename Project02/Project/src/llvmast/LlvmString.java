package llvmast;
public class LlvmString extends LlvmValue{
	public String s;
	
    public LlvmString(String s){
    	this.s = s;
    	this.type = LlvmCustomType.CUSTOM_CLASS;
    }
    
    public LlvmString(LlvmType t){
    	this.type = t;
    	this.s = t.toString();
    }
    
    public String toString(){
    	return s;
    }
}
