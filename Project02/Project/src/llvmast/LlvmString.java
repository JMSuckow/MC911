package llvmast;
public class LlvmString extends LlvmValue{
	public String s;
    public LlvmString(String s){
    	this.s = s;
    }
    
    public String toString(){
    	return s;
    }
}
