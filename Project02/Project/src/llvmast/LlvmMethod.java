package llvmast;
import java.util.*;

public class LlvmMethod extends LlvmValue{

	public LlvmValue name;
	public LlvmValue returnExp;
	public List<LlvmValue> locals;
	public List<LlvmValue> formals;
	public List<LlvmValue> body;
	
   public LlvmMethod(LlvmValue name, LlvmValue returnType, LlvmValue returnExp,
   						List<LlvmValue> formals, List<LlvmValue> body, List<LlvmValue> locals){
    	this.name = name;
    	this.type = returnType.type;
    	this.returnExp = returnExp;
    	this.locals = locals;
    	this.body = body;
    	this.formals = formals;
    }
    
    public String toString(){
		return name.toString();
    }
}
