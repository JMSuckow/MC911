package llvmast;
public class LlvmCustomType extends LlvmType{
    public static final LlvmType ARRAY  = new LlvmCustomType();
    public static final LlvmType ARRAYP  = new LlvmCustomType();
    //public static final LlvmCustomType CUSTOM_CLASS  = new LlvmCustomType();
    
    public enum CustomType {
       CUSTOM_CLASS, NO;
	}
    
    private String s;
    public CustomType custom;
    
    public LlvmCustomType(CustomType t){
    	this.custom = t;
    }
	
	 public LlvmCustomType(){
    	this.custom = CustomType.NO;
    }	
	    
    public void setString(String s){
    	this.s = s;
    }

   public String toString(){
	if(this == ARRAY) return "%type.Array";
	if(this == ARRAYP) return "%type.Array*";
	if(this.custom == CustomType.CUSTOM_CLASS) return s;
	return null;
    }
}
