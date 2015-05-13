// testando this - (OK)
class m336
{
   public static void main(String[] args)
   {
      System.out.println(new a().i());
   }
}

class a
{
	int i;
   public a A(){return this;}
   public int i(){
   	i = 0; 
   	while (i < 5 ){ 
   		i = i + 1; 
   	} 
   	return i; 
   }
}
