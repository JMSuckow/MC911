Compiled from "ClassInfo.java"
public class symbol.ClassInfo {
  public temp.Label vtable;
  public symbol.Symbol name;
  public symbol.ClassInfo base;
  public java.util.Hashtable<symbol.Symbol, symbol.VarInfo> attributes;
  public java.util.Hashtable<symbol.Symbol, symbol.MethodInfo> methods;
  public java.util.Vector<symbol.Symbol> attributesOrder;
  public java.util.Vector<symbol.Symbol> vtableIndex;
  public symbol.ClassInfo(symbol.Symbol);
  public void setBase(symbol.ClassInfo);
  public symbol.ClassInfo(symbol.Symbol, symbol.ClassInfo);
  public boolean addAttribute(symbol.VarInfo);
  public int getAttributeOffset(symbol.Symbol);
  public int getMethodOffset(symbol.Symbol);
  public boolean addMethod(symbol.MethodInfo);
}
Compiled from "MethodInfo.java"
public class symbol.MethodInfo {
  public syntaxtree.Type type;
  public symbol.Symbol name;
  public symbol.Symbol parent;
  public util.List<symbol.VarInfo> formals;
  public util.List<symbol.VarInfo> locals;
  public java.util.Hashtable<symbol.Symbol, symbol.VarInfo> formalsTable;
  public java.util.Hashtable<symbol.Symbol, symbol.VarInfo> localsTable;
  public frame.Access thisPtr;
  public frame.Frame frame;
  public symbol.MethodInfo(syntaxtree.Type, symbol.Symbol, symbol.Symbol);
  public java.lang.String decorateName();
  public boolean addFormal(symbol.VarInfo);
  public boolean addLocal(symbol.VarInfo);
}
Compiled from "Symbol.java"
public class symbol.Symbol {
  public static symbol.Symbol symbol(java.lang.String);
  public java.lang.String toString();
  static {};
}
Compiled from "Table.java"
public class symbol.Table<B> {
  public symbol.Table();
  public boolean put(symbol.Symbol, B);
  public B get(symbol.Symbol);
  public void beginScope();
  public void endScope();
  public java.util.Enumeration<symbol.Symbol> keys();
}
Compiled from "VarInfo.java"
public class symbol.VarInfo {
  public syntaxtree.Type type;
  public symbol.Symbol name;
  public frame.Access access;
  public symbol.VarInfo(syntaxtree.Type, symbol.Symbol);
}
