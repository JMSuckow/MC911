package llvmast;
public class LlvmString extends LlvmValue{
	public String s;
	
    public LlvmString(String s){
    	this.s = s;
    	LlvmCustomType ty = new LlvmCustomType(LlvmCustomType.CustomType.CUSTOM_CLASS);
    	ty.setString(s);
    	this.type = ty;
    }
    
    public LlvmString(String s, String t){
    	this.s = s;
    	LlvmCustomType ty = new LlvmCustomType(LlvmCustomType.CustomType.CUSTOM_CLASS);
     	ty.setString(t);
    	this.type = ty;
    }
    
    public LlvmString(String s, LlvmType t){
    	this.type = t;
    	this.s = s;
    }
    
    public LlvmString(LlvmType t){
    	this.type = t;
    	this.s = t.toString();
    }
    
    public String toString(){
    	return s;
    }
}
