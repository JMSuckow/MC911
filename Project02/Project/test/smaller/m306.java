// retorno de metodo
class m306
{
    public static void main(String[] args)
    {
    	System.out.println(new b().Init(1).a().a().a().a().a().i());
    }
}

class a
{
   int i;
   
   public a Init(int ii){ i = ii; return this; }
   public a a(){ i = i + 1; return this; }
   public int i() { return i; }
}

class b extends a
{
    public int i() { return 2 * i - 1; }
    public a a(){ return this; }
}
