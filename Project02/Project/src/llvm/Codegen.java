/*****************************************************
Esta classe Codegen é a responsável por emitir LLVM-IR. 
Ela possui o mesmo método 'visit' sobrecarregado de
acordo com o tipo do parâmetro. Se o parâmentro for
do tipo 'While', o 'visit' emitirá código LLVM-IR que 
representa este comportamento. 
Alguns métodos 'visit' já estão prontos e, por isso,
a compilação do código abaixo já é possível.

class a{
    public static void main(String[] args){
    	System.out.println(1+2);
    }
}

O pacote 'llvmast' possui estruturas simples 
que auxiliam a geração de código em LLVM-IR. Quase todas 
as classes estão prontas; apenas as seguintes precisam ser 
implementadas: 

// llvmasm/LlvmBranch.java
// llvmasm/LlvmIcmp.java
// llvmasm/LlvmMinus.java
// llvmasm/LlvmTimes.java


Todas as assinaturas de métodos e construtores 
necessárias já estão lá. 


Observem todos os métodos e classes já implementados
e o manual do LLVM-IR (http://llvm.org/docs/LangRef.html) 
como guia no desenvolvimento deste projeto. 

****************************************************/
package llvm;

import semant.Env;
import syntaxtree.*;
import llvmast.*;

import java.util.*;
import java.lang.Math;

public class Codegen extends VisitorAdapter{
	private List<LlvmInstruction> assembler;
	private Codegen codeGenerator;

  	private SymTab symTab;
	//private ClassNode classEnv; 	// Aponta para a classe atualmente em uso em symTab
	//private MethodNode methodEnv; 	// Aponta para a metodo atualmente em uso em symTab
	
	private SimpleClass currentClass;
	private Method currentMethod;
	private int labelNumber;


	public Codegen(){
		assembler = new LinkedList<LlvmInstruction>();
		symTab = new SymTab();
	}

	// Método de entrada do Codegen
	public String translate(Program p, Env env){	
		codeGenerator = new Codegen();
		
		// Preenchendo a Tabela de Símbolos
		// Quem quiser usar 'env', apenas comente essa linha

		 codeGenerator.symTab.FillTabSymbol(p);
		
		// Formato da String para o System.out.printlnijava "%d\n"
		codeGenerator.assembler.add(new LlvmConstantDeclaration("@.formatting.string", "private constant [4 x i8] c\"%d\\0A\\00\""));
		
		//Formato de Array
		codeGenerator.assembler.add(new LlvmConstantDeclaration("%type.Array", "type {i32, i32* }"));	

		// NOTA: sempre que X.accept(Y), então Y.visit(X);
		// NOTA: Logo, o comando abaixo irá chamar codeGenerator.visit(Program), linha 75
		p.accept(codeGenerator);

		// Link do printf
		List<LlvmType> pts = new LinkedList<LlvmType>();
		pts.add(new LlvmPointer(LlvmPrimitiveType.I8));
		pts.add(LlvmPrimitiveType.DOTDOTDOT);
		codeGenerator.assembler.add(new LlvmExternalDeclaration("@printf", LlvmPrimitiveType.I32, pts)); 
		List<LlvmType> mallocpts = new LinkedList<LlvmType>();
		mallocpts.add(LlvmPrimitiveType.I32);
		codeGenerator.assembler.add(new LlvmExternalDeclaration("@malloc", new LlvmPointer(LlvmPrimitiveType.I8),mallocpts)); 


		String r = new String();
		for(LlvmInstruction instr : codeGenerator.assembler)
			r += instr+"\n";
		return r;
	}

	public LlvmValue visit(Program n){
		n.mainClass.accept(this);

		for (util.List<ClassDecl> c = n.classList; c != null; c = c.tail){
			currentClass = this.symTab.nextClass();
			c.head.accept(this);
		}

		return null;
	}

	public LlvmValue visit(MainClass n){
		
		// definicao do main 
		assembler.add(new LlvmDefine("@main", LlvmPrimitiveType.I32, new LinkedList<LlvmValue>()));
		assembler.add(new LlvmLabel(new LlvmLabelValue("entry")));
		LlvmRegister R1 = new LlvmRegister(new LlvmPointer(LlvmPrimitiveType.I32));
		assembler.add(new LlvmAlloca(R1, LlvmPrimitiveType.I32, new LinkedList<LlvmValue>()));
		assembler.add(new LlvmStore(new LlvmIntegerLiteral(0), R1));

		// Statement é uma classe abstrata
		// Portanto, o accept chamado é da classe que implementa Statement, por exemplo,  a classe "Print". 
		n.stm.accept(this);  

		// Final do Main
		LlvmRegister R2 = new LlvmRegister(LlvmPrimitiveType.I32);
		assembler.add(new LlvmLoad(R2,R1));
		assembler.add(new LlvmRet(R2));
		assembler.add(new LlvmCloseDefinition());
		return null;
	}
	
	public LlvmValue visit(Plus n){
		System.out.println("Plus");
		LlvmValue v1 = n.lhs.accept(this);
		LlvmValue v2 = n.rhs.accept(this);
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I32);
		assembler.add(new LlvmPlus(lhs,LlvmPrimitiveType.I32,v1,v2));
		return lhs;
	}
	
	public LlvmValue visit(Minus n){
		System.out.println("Minus");
		LlvmValue v1 = n.lhs.accept(this);
		LlvmValue v2 = n.rhs.accept(this);
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I32);
		assembler.add(new LlvmMinus(lhs,LlvmPrimitiveType.I32,v1,v2));
		return lhs;
	}
	
	public LlvmValue visit(Times n){
		System.out.println("Times");
		LlvmValue v1 = n.lhs.accept(this);
		LlvmValue v2 = n.rhs.accept(this);
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I32);
		assembler.add(new LlvmTimes(lhs,LlvmPrimitiveType.I32,v1,v2));
		return lhs;
	}
	
	public LlvmValue visit(If n){
		System.out.println("If");
		LlvmValue b = n.condition.accept(this);
		
		LlvmLabel thenClause = new LlvmLabel(new LlvmLabelValue("then"+labelNumber));
		LlvmLabel elseClause = new LlvmLabel(new LlvmLabelValue("else"+labelNumber));
		LlvmLabel endClause = new LlvmLabel(new LlvmLabelValue("end"+labelNumber));
		labelNumber++;
		
		assembler.add(new LlvmBranch(b,thenClause.label,elseClause.label));
		
		assembler.add(thenClause);
		LlvmValue v1 = n.thenClause.accept(this);
		assembler.add(new LlvmBranch(endClause.label));

		LlvmValue v2;
		
		assembler.add(elseClause);
		if(n.elseClause != null){
			v2 = n.elseClause.accept(this);
		}
		assembler.add(new LlvmBranch(endClause.label));
		
		assembler.add(endClause);
		
		return null;

	}

	public LlvmValue visit(True n){
		System.out.println("True");
		return new LlvmBool(LlvmBool.TRUE);
	}

	public LlvmValue visit(False n){
		System.out.println("False");
		return new LlvmBool(LlvmBool.FALSE);
	}
	
	public LlvmValue visit(LessThan n){
		System.out.println("LessThan");
		LlvmValue v1 = n.lhs.accept(this);
		LlvmValue v2 = n.rhs.accept(this);
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I1);
		assembler.add(new LlvmIcmp(lhs,Condition.slt,LlvmPrimitiveType.I32,v1,v2));
		return lhs;
	}
	
	public LlvmValue visit(Equal n){
		System.out.println("Equal");
		LlvmValue v1 = n.lhs.accept(this);
		LlvmValue v2 = n.rhs.accept(this);
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I1);
		assembler.add(new LlvmIcmp(lhs,Condition.eq,LlvmPrimitiveType.I32,v1,v2));
		return lhs;
	}

	public LlvmValue visit(And n){
		System.out.println("And");
		LlvmValue v1 = n.lhs.accept(this);
		LlvmValue v2 = n.rhs.accept(this);
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I1);
		assembler.add(new LlvmAnd(lhs,LlvmPrimitiveType.I1,v1,v2));
		return lhs;
	}

	public LlvmValue visit(Not n){
		System.out.println("Not");
		LlvmValue b = n.exp.accept(this);
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I1);
		assembler.add(new LlvmNot(lhs,LlvmPrimitiveType.I1,b));
		return lhs;
	}
	
	public LlvmValue visit(NewArray n){
		System.out.println("NewArray");
		
		LlvmValue size = n.size.accept(this);
		
		
		//Calculate Array type size: 12
		LlvmRegister sizeReg = new LlvmRegister(LlvmPrimitiveType.I32);
		assembler.add(new LlvmTimes(sizeReg,LlvmPrimitiveType.I32,new LlvmIntegerLiteral(12),new LlvmIntegerLiteral(1)));
		//Malloc Array type size
		LlvmRegister arrayReg = new LlvmRegister(LlvmPrimitiveType.I8P);
		List<LlvmValue> args = new ArrayList<LlvmValue>();
		args.add(sizeReg);
		assembler.add(new LlvmCall(arrayReg, LlvmPrimitiveType.I8P, "@malloc", args));
		//Bitcast array type
		LlvmRegister array = new LlvmRegister(LlvmCustomType.ARRAYP);
		assembler.add(new LlvmBitcast(array, arrayReg, LlvmCustomType.ARRAYP));
		//store size
		LlvmRegister sizePtr = new LlvmRegister(LlvmPrimitiveType.I32P);
		List<LlvmValue> offsets = new ArrayList<LlvmValue>();
		offsets.add(new LlvmIntegerLiteral(0));
		offsets.add(new LlvmIntegerLiteral(0));
		assembler.add(new LlvmGetElementPointer(sizePtr, array, offsets));
		assembler.add(new LlvmStore(size, sizePtr));		
		//alloc array
		LlvmRegister arrayA = new LlvmRegister(LlvmPrimitiveType.I32P);
		List<LlvmValue> numbers = new ArrayList<LlvmValue>();
		numbers.add(size);
		assembler.add(new LlvmAlloca(arrayA, LlvmPrimitiveType.I32, numbers));
		//store array
		LlvmRegister arrayPtr = new LlvmRegister(LlvmPrimitiveType.I32PP);
		List<LlvmValue> offsetsArray = new ArrayList<LlvmValue>();
		offsetsArray.add(new LlvmIntegerLiteral(0));
		offsetsArray.add(new LlvmIntegerLiteral(1));
		assembler.add(new LlvmGetElementPointer(arrayPtr, array, offsetsArray));
		assembler.add(new LlvmStore(arrayA, arrayPtr));
		
		return array;		
	}
	
	public LlvmValue visit(ClassDeclSimple n){
		System.out.println("ClassDeclSimple");
		
		LlvmValue name = n.name.accept(this);
		
		List<LlvmValue> varList = new ArrayList<LlvmValue>();
		for (util.List<VarDecl> c = n.varList; c != null; c = c.tail){
			varList.add(c.head.accept(this));
		}
		
		String s = "";
		for(int i =0; i<varList.size(); i++){
			LlvmVariable v = (LlvmVariable) varList.get(i);
			if(v.type instanceof LlvmCustomType && ((LlvmCustomType)v.type).custom == LlvmCustomType.CustomType.CUSTOM_CLASS)
				s = s + "%class."+v.type+"*";
			else
				s = s + v.type;
			if(i+1 < varList.size())
				s = s + ", ";
		}
		
		assembler.add(new LlvmConstantDeclaration("%class."+name, "type {" + s + "}"));	

		
		List<LlvmValue> methodList = new ArrayList<LlvmValue>();
		for (util.List<MethodDecl> c = n.methodList; c != null; c = c.tail){
			currentMethod = currentClass.nextMethod();
			methodList.add(c.head.accept(this));
		}
		currentMethod = null;

		return name;
	}
	
	public LlvmValue visit(VarDecl n){
		System.out.println("VarDecl");
		
		LlvmValue type = n.type.accept(this);
		LlvmValue name = n.name.accept(this);
		
		return new LlvmVariable(name,type);
	}
	
	public LlvmValue visit(MethodDecl n){
		System.out.println("MethodDecl");
		
		LlvmValue returnType = n.returnType.accept(this);
		LlvmValue name = n.name.accept(this);
		
		List<LlvmValue> formals = new ArrayList<LlvmValue>();
		formals.add(new LlvmString("this","%class."+currentClass.name+"* "));
		for (util.List<Formal> c = n.formals; c != null; c = c.tail){
			formals.add(c.head.accept(this));
		}
		
		//System.out.println("Current Class"+currentClass.name);
		
		// definicao do main 
		assembler.add(new LlvmDefine("@_"+name+"__"+currentClass.name, returnType.type, formals));
		assembler.add(new LlvmLabel(new LlvmLabelValue("entry_"+name+"__"+currentClass.name)));
		
		//Alloc formal variables
		for(int i =1; i < formals.size(); i++){
			LlvmRegister reg = new LlvmRegister("%" + currentClass.name + "_" + name + "_" + ((LlvmFormal)formals.get(i)).name + "_tmp",new LlvmPointer(formals.get(i).type));
			LlvmRegister formal = new LlvmRegister("%" + ((LlvmFormal)formals.get(i)).name, formals.get(i).type);
			assembler.add(new LlvmAlloca(reg, formals.get(i).type, new ArrayList<LlvmValue>()));
			assembler.add(new LlvmStore(formal, reg));
		}
		
		//Alloc new variables
		List<LlvmValue> locals = new ArrayList<LlvmValue>();
		for (util.List<VarDecl> c = n.locals; c != null; c = c.tail){
			locals.add(c.head.accept(this));	
		}
		
		for(int i =0; i < locals.size(); i++){
			LlvmRegister reg = new LlvmRegister("%" + currentClass.name + "_" + name + "_" + ((LlvmVariable)locals.get(i)).name + "_tmp",new LlvmPointer(locals.get(i).type));
			assembler.add(new LlvmAlloca(reg, locals.get(i).type, new ArrayList<LlvmValue>()));
		}
		
		//Body
		List<LlvmValue> body = new ArrayList<LlvmValue>();
		for (util.List<Statement> c = n.body; c != null; c = c.tail){
			body.add(c.head.accept(this));
		}
		
		
		//Return
		LlvmValue returnExp = n.returnExp.accept(this);
		
		if(!(returnExp.type.toString().equals(currentMethod.getReturnType().toString()))){
			LlvmRegister reg = new LlvmRegister(currentMethod.getReturnType());
			assembler.add(new LlvmLoad(reg,returnExp));
			assembler.add(new LlvmRet(reg));
		}else{
			assembler.add(new LlvmRet(returnExp));
		}
		assembler.add(new LlvmCloseDefinition());

		
		if(returnType.type instanceof LlvmCustomType)
				((LlvmCustomType) returnType.type).setString("%class."+ returnType.type+"* ");
		
		
		return new LlvmMethod(name,returnType,returnExp,formals,body,locals);
	}
	
	public LlvmValue visit(Formal n){
		System.out.println("Formal");
		
		LlvmValue type = n.type.accept(this);
		LlvmValue name = n.name.accept(this);
		
		return new LlvmFormal(name,type);
	}
	
	public LlvmValue visit(Assign n){
		System.out.println("Assign");
		
		LlvmValue var = n.var.accept(this);
		LlvmValue exp = n.exp.accept(this);
		
		Variable v = this.symTab.getVariable(currentClass, currentMethod, n.var.s);
		
		LlvmRegister reg = null;
		
		if(v.place == Variable.Place.FORMAL || v.place == Variable.Place.LOCAL){
			if(v.place == Variable.Place.FORMAL)
				reg = new LlvmRegister("%" + v.name ,new LlvmPointer(v.type));
			else
				reg = new LlvmRegister("%" + currentClass.name + "_" + currentMethod.name + "_" + v.name + "_tmp",new LlvmPointer(v.type));
			
			if(!(exp instanceof LlvmRegister)){
				LlvmRegister regExp = new LlvmRegister (new LlvmPointer(exp.type));
				LlvmRegister regAux = new LlvmRegister(exp.type);
				assembler.add(new LlvmAlloca(regExp, exp.type, new ArrayList<LlvmValue>()));
				assembler.add(new LlvmStore(exp, regExp));
				assembler.add(new LlvmLoad(regAux,regExp));
				assembler.add(new LlvmStore(regAux, reg));
			}else{			
				assembler.add(new LlvmStore(exp,reg));
			}
		}
		else if(v.place == Variable.Place.GLOBAL){
			
			LlvmRegister variablePtr;
			if(v.type instanceof LlvmCustomType && ((LlvmCustomType) v.type).custom == LlvmCustomType.CustomType.CUSTOM_CLASS){
				variablePtr = new LlvmRegister(new LlvmString("%class."+currentClass.name+"* * ").type);
			}else
				variablePtr = new LlvmRegister(new LlvmPointer(v.type));
				
			List<LlvmValue> offsets = this.symTab.getOffsets(currentClass,v);
			
			LlvmRegister thisReg = new LlvmRegister("%this", new LlvmString("%class."+currentClass.name+"* ").type);
			
			assembler.add(new LlvmGetElementPointer(variablePtr, thisReg, offsets));
		
			if(v.type instanceof LlvmCustomType && ((LlvmCustomType) v.type).custom == LlvmCustomType.CustomType.CUSTOM_CLASS){
				reg = new LlvmRegister(new LlvmString("%class."+currentClass.name+"* ").type);
			}else
				reg = new LlvmRegister(v.type);
				
			assembler.add(new LlvmStore(exp, variablePtr));
			assembler.add(new LlvmLoad(reg, variablePtr));

		}

		return reg;
	}
	
	public LlvmValue visit(Identifier n){
		System.out.println("Identifier - "+ n.s);
		
		
		return new LlvmString(n.s);
	}
	
	public LlvmValue visit(IntArrayType n){
		System.out.println("IntArrayType");
		
		return new LlvmString(new LlvmPointer(LlvmCustomType.ARRAY));
	}
	
	public LlvmValue visit(BooleanType n){
		System.out.println("BooleanType");
		
		return new LlvmString(LlvmPrimitiveType.I1);
	}
	
	public LlvmValue visit(IntegerType n){
		System.out.println("IntegerType");
		
		return new LlvmString(LlvmPrimitiveType.I32);
	}
	
	public LlvmValue visit(IdentifierType n){
		System.out.println("IdentifierType");
		
		return new LlvmString(n.name);
	}
	
	public LlvmValue visit(IdentifierExp n){
		System.out.println("IdentifierExp"+n.name);
		
		LlvmValue name = n.name.accept(this);
		
		Variable v = this.symTab.getVariable(currentClass, currentMethod, n.name.s);
		
		LlvmRegister reg = null;
		
		if(v.place == Variable.Place.LOCAL){
			reg = new LlvmRegister("%" + currentClass.name + "_" + currentMethod.name + "_" + v.name + "_tmp",new LlvmPointer(v.type));
		}
		else if(v.place == Variable.Place.FORMAL){
			reg = new LlvmRegister("%" + v.name,new LlvmPointer(v.type));
		}
		else if(v.place == Variable.Place.GLOBAL){
			LlvmRegister variablePtr = new LlvmRegister(new LlvmPointer(v.type));
			List<LlvmValue> offsets = this.symTab.getOffsets(currentClass,v);
			
			LlvmRegister thisReg = new LlvmRegister("%this", new LlvmString("%class."+currentClass.name+"* ").type);
			
			assembler.add(new LlvmGetElementPointer(variablePtr, thisReg, offsets));
		
			if(v.type instanceof LlvmCustomType && ((LlvmCustomType) v.type).custom == LlvmCustomType.CustomType.CUSTOM_CLASS)
				reg = new LlvmRegister(new LlvmString("%class."+currentClass.name+"* ").type);
			else
				reg = new LlvmRegister(v.type);
			assembler.add(new LlvmLoad(reg, variablePtr));
			
		}
		
		return reg;
	}
	
	public LlvmValue visit(ArrayLength n){
		System.out.println("ArrayLength");
		
		LlvmValue array = n.array.accept(this);
		
		LlvmRegister sizePtr = new LlvmRegister(LlvmPrimitiveType.I32P);
		List<LlvmValue> offsets = new ArrayList<LlvmValue>();
		offsets.add(new LlvmIntegerLiteral(0));
		offsets.add(new LlvmIntegerLiteral(0));
		assembler.add(new LlvmGetElementPointer(sizePtr, array, offsets));
		
		LlvmRegister size = new LlvmRegister(LlvmPrimitiveType.I32);
		assembler.add(new LlvmLoad(size, sizePtr));
		
		
		return size;
	}
	
	//FIXME: Doesn't Work!
	public LlvmValue visit(ArrayLookup n){
		System.out.println("ArrayLookup");
		
		LlvmValue array = n.array.accept(this);
		LlvmValue index = n.index.accept(this);
		
		LlvmRegister valuePtr = new LlvmRegister(LlvmPrimitiveType.I32PP);
		List<LlvmValue> offsets = new ArrayList<LlvmValue>();
		offsets.add(new LlvmIntegerLiteral(0));
		offsets.add(new LlvmIntegerLiteral(1));
		offsets.add(index);
		assembler.add(new LlvmGetElementPointer(valuePtr, array, offsets));
		
		LlvmRegister value = new LlvmRegister(LlvmPrimitiveType.I32);
		assembler.add(new LlvmLoad(value, valuePtr));
		
		return value;
	}
	
	//TODO
	public LlvmValue visit(ArrayAssign n){
		System.out.println("ArrayAssign");
		
		LlvmValue var = n.var.accept(this);
		LlvmValue index = n.index.accept(this);
		LlvmValue value = n.value.accept(this);
		
		return null;
	}
	
	public LlvmValue visit(Call n){
		System.out.println("Call");
		
		LlvmValue object = n.object.accept(this);
		LlvmValue method = n.method.accept(this);
		
		List<LlvmValue> actuals = new ArrayList<LlvmValue>();
		actuals.add(object);
		for (util.List<Exp> c = n.actuals; c != null; c = c.tail){
			actuals.add(c.head.accept(this));
		}
		
		LlvmRegister retValue = null;
		if(object instanceof LlvmObject){
			Method m = this.symTab.getMethod(((LlvmObject) object).className,n.method.s);
			
			retValue = new LlvmRegister(this.symTab.getMethod(((LlvmObject) object).className,n.method.s).getReturnType());
			
			assembler.add(new LlvmCall(retValue, retValue.type, "@_"+method+"__"+ ((LlvmObject) object).className, actuals));
		}else if(object.type instanceof LlvmCustomType && ((LlvmCustomType)object.type).custom == LlvmCustomType.CustomType.CUSTOM_CLASS){
			String className = this.symTab.findClassName(object.type.toString());
			
			retValue = new LlvmRegister(this.symTab.getMethod(className,n.method.s).getReturnType());
			assembler.add(new LlvmCall(retValue, retValue.type, "@_"+method+"__"+ className, actuals));			
		}

		return retValue;
	}
	
	public LlvmValue visit(NewObject n){
		System.out.println("NewObject");
		
		LlvmValue className = n.className.accept(this);
		
		//Calculate Object type size
		LlvmRegister sizeReg = new LlvmRegister(LlvmPrimitiveType.I32);
		assembler.add(new LlvmTimes(sizeReg,LlvmPrimitiveType.I32,new LlvmIntegerLiteral(this.symTab.getClass(className.toString()).getSizeDeclaration()),new LlvmIntegerLiteral(1)));
		//Malloc Obj type size
		LlvmRegister objReg = new LlvmRegister(LlvmPrimitiveType.I8P);
		List<LlvmValue> args = new ArrayList<LlvmValue>();
		args.add(sizeReg);
		assembler.add(new LlvmCall(objReg, LlvmPrimitiveType.I8P, "@malloc", args));
		//Bitcast Obj type
		LlvmRegister obj = new LlvmRegister(new LlvmString("%class."+className+"* ").type);
		assembler.add(new LlvmBitcast(obj, objReg, obj.type));
		
		return new LlvmObject(n.className.s, obj);
	}
	
	public LlvmValue visit(Print n){

		LlvmValue v =  n.exp.accept(this);

		// getelementptr:
		LlvmRegister lhs = new LlvmRegister(new LlvmPointer(LlvmPrimitiveType.I8));
		LlvmRegister src = new LlvmNamedValue("@.formatting.string",new LlvmPointer(new LlvmArray(4,LlvmPrimitiveType.I8)));
		List<LlvmValue> offsets = new LinkedList<LlvmValue>();
		offsets.add(new LlvmIntegerLiteral(0));
		offsets.add(new LlvmIntegerLiteral(0));
		List<LlvmType> pts = new LinkedList<LlvmType>();
		pts.add(new LlvmPointer(LlvmPrimitiveType.I8));
		List<LlvmValue> args = new LinkedList<LlvmValue>();
		args.add(lhs);
		args.add(v);
		assembler.add(new LlvmGetElementPointer(lhs,src,offsets));

		pts = new LinkedList<LlvmType>();
		pts.add(new LlvmPointer(LlvmPrimitiveType.I8));
		pts.add(LlvmPrimitiveType.DOTDOTDOT);
		
		// printf:
		assembler.add(new LlvmCall(new LlvmRegister(LlvmPrimitiveType.I32),
				LlvmPrimitiveType.I32,
				pts,				 
				"@printf",
				args
				));
		return null;
	}
	
	public LlvmValue visit(IntegerLiteral n){
		return new LlvmIntegerLiteral(n.value);
	};
	
	public LlvmValue visit(This n){
		System.out.println("This");
		
		LlvmRegister thisReg = new LlvmRegister("%this", new LlvmString("%class."+currentClass.name+"* ").type);
		
		return thisReg;
	}
	
	public LlvmValue visit(While n){
		System.out.println("While");
		
		LlvmLabel beginClause = new LlvmLabel(new LlvmLabelValue("begin"+labelNumber));
		assembler.add(new LlvmBranch(beginClause.label));
		assembler.add(beginClause);
		
		LlvmValue condition = n.condition.accept(this);
		
		LlvmLabel trueClause = new LlvmLabel(new LlvmLabelValue("true"+labelNumber));
		LlvmLabel endClause = new LlvmLabel(new LlvmLabelValue("end"+labelNumber));
		labelNumber++;
		
		assembler.add(new LlvmBranch(condition,trueClause.label,endClause.label));
		
		assembler.add(trueClause);
		
		LlvmValue body = n.body.accept(this);
		
		assembler.add(new LlvmBranch(beginClause.label));
		
		assembler.add(endClause);
		
		
		
		return null;
	}
	
	public LlvmValue visit(Block n){
		System.out.println("Block");
		
		List<LlvmValue> body = new ArrayList<LlvmValue>();
		for (util.List<Statement> c = n.body; c != null; c = c.tail){
			body.add(c.head.accept(this));
		}
		
		return null;
	}
	
	// Todos os visit's que devem ser implementados	
	//public LlvmValue visit(ClassDeclSimple n){System.out.println("ClassDeclSimple");return null;}
	public LlvmValue visit(ClassDeclExtends n){System.out.println("ClassDeclExtends");return null;}
	//public LlvmValue visit(VarDecl n){System.out.println("VarDecl");return null;}
	//public LlvmValue visit(MethodDecl n){System.out.println("MethodDecl");return null;}
	//public LlvmValue visit(Formal n){System.out.println("Formal");return null;}
	//public LlvmValue visit(IntArrayType n){System.out.println("IntArrayType");return null;}
	//public LlvmValue visit(BooleanType n){System.out.println("BooleanType");return null;}
	//public LlvmValue visit(IntegerType n){System.out.println("IntegerType");return null;}
	//public LlvmValue visit(IdentifierType n){System.out.println("IdentifierType");return null;}
	//public LlvmValue visit(Block n){System.out.println("Block");return null;}
	//public LlvmValue visit(If n){return null;}
	//public LlvmValue visit(While n){System.out.println("While");return null;}
	//public LlvmValue visit(Assign n){System.out.println("Assign");return null;}
	//public LlvmValue visit(ArrayAssign n){System.out.println("ArrayAssign");return null;}
	//public LlvmValue visit(And n){return null;}
	//public LlvmValue visit(LessThan n){return null;}
	//public LlvmValue visit(Equal n){System.out.println("Equal");return null;}
	//public LlvmValue visit(Minus n){return null;}
	//public LlvmValue visit(Times n){return null;}
	//public LlvmValue visit(ArrayLookup n){System.out.println("ArrayLookup");return null;}
	//public LlvmValue visit(ArrayLength n){System.out.println("ArrayLength");return null;}
	//public LlvmValue visit(Call n){System.out.println("Call");return null;}
	//public LlvmValue visit(True n){return null;}
	//public LlvmValue visit(False n){return null;}
	//public LlvmValue visit(IdentifierExp n){System.out.println("IdentifierExp");return null;}
	//public LlvmValue visit(This n){System.out.println("This");return null;}
	//public LlvmValue visit(NewArray n){System.out.println("NewArray");return null;}
	//public LlvmValue visit(NewObject n){System.out.println("NewObject");return null;}
	//public LlvmValue visit(Not n){return null;}
	//public LlvmValue visit(Identifier n){System.out.println("Identifier");return null;}
}


/**********************************************************************************/
/* === Tabela de Símbolos ==== 
 * 
 * 
 */
/**********************************************************************************/

class SymTab extends VisitorAdapter{
    //public Map<String, ClassNode> classes;     
    //private ClassNode classEnv;    //aponta para a classe em uso
    public ArrayList<SimpleClass> classes;
    private int currentClass;
	
	public SymTab(){
		classes = new ArrayList<SimpleClass>();
		currentClass = 0;
	}
	
    public LlvmValue FillTabSymbol(Program n){
		n.accept(this);
		return null;
	}
	
	public SimpleClass getClass(String name){

		for(SimpleClass sc : classes){
			
			if(sc.name.equals(name)){
				return sc;
			}
		}
		
		return null;	
	}
	
	public SimpleClass nextClass(){
		return classes.get(++currentClass);
	}
	
	public Method getMethod(String className, String method){

		for(SimpleClass sc : classes){			
			if(sc.name.equals(className)){
				for(Method m : sc.methods){
					if(m.name.equals(method)){
						return m;
					}
				}
			}
		}
		
		return null;	
	}
	
	public Variable getVariable(SimpleClass c, Method method, String varName){

		for(Variable v : c.variables){
			if(v.name.equals(varName))
				return v;
		}
		
		if(method != null){
			for(Variable v : method.formals){
				if(v.name.equals(varName))
					return v;
			}
			for(Variable v : method.locals){
				if(v.name.equals(varName))
					return v;
			}
		}
		
		return null;	
	}
	
	public List<LlvmValue> getOffsets(SimpleClass currentClass, Variable v){
		
		List<LlvmValue> offsets = new ArrayList<LlvmValue>();
		offsets.add(new LlvmIntegerLiteral(0));
		
		for(int i =0; i< currentClass.variables.size(); i++){
		
			if(currentClass.variables.get(i) == v){
				offsets.add(new LlvmIntegerLiteral(i));
			}
		
		}
		
		return offsets;
		
	} 
	
	public String findClassName(String regType){
		if(regType.charAt(0) == '%')
			return regType.substring(7,Math.min(regType.indexOf(' '),regType.indexOf('*')));
		else
			return regType;
	}
	
	public LlvmValue visit(Program n){
	
		classes.add((SimpleClass)n.mainClass.accept(this));
	
		for (util.List<ClassDecl> c = n.classList; c != null; c = c.tail)
			classes.add((SimpleClass)c.head.accept(this));

	
		return null;
	}
	
	public LlvmValue visit(MainClass n){
		System.out.println("ST-MainSimple");
		//classes.put(n.className.s, new ClassNode(n.className.s, null, null));
		
		return new SimpleClass(n.className.s, new ArrayList<Variable>(),  new ArrayList<Method>());
	}
	
	public LlvmValue visit(ClassDeclSimple n){
		System.out.println("ST-ClassDeclSimple");
		
			//LlvmValue name = n.name.accept(this);
			
			List<Variable> varList = new ArrayList<Variable>();
			for (util.List<VarDecl> c = n.varList; c != null; c = c.tail){
				Variable v = (Variable)c.head.accept(this);
				v.place = Variable.Place.GLOBAL; 
				varList.add(v);
			}
			
			List<Method> methodList = new ArrayList<Method>();
			for (util.List<MethodDecl> c = n.methodList; c != null; c = c.tail){
				Method m = (Method)c.head.accept(this);
				methodList.add(m);
			}
	
		return new SimpleClass(n.name.s, varList, methodList);
	}

		public LlvmValue visit(ClassDeclExtends n){System.out.println("ST-ClassDeclSimple");return null;}
		
		public LlvmValue visit(VarDecl n){
			System.out.println("ST-VarDecl");
			
			LlvmValue type = n.type.accept(this);
			//LlvmValue name = n.name.accept(this);
			
			return new Variable(n.name.s,type);
		}
		
		public LlvmValue visit(Formal n){
			System.out.println("ST-Formal");
			
			LlvmValue type = n.type.accept(this);
			//LlvmValue name = n.name.accept(this);
			
			return new Variable(n.name.s,type);
		}
		
		public LlvmValue visit(MethodDecl n){
			System.out.println("ST-MethodDecl");
			
			LlvmValue returnType = n.returnType.accept(this);
			LlvmValue name = n.name.accept(this);
			
			List<Variable> formals = new ArrayList<Variable>();
			for (util.List<Formal> c = n.formals; c != null; c = c.tail){
				Variable v = (Variable)c.head.accept(this);
				v.place = Variable.Place.FORMAL; 
				formals.add(v);
			}
			
			List<Variable> locals = new ArrayList<Variable>();
			for (util.List<VarDecl> c = n.locals; c != null; c = c.tail){
				Variable v = (Variable)c.head.accept(this);
				v.place = Variable.Place.LOCAL; 
				locals.add(v);	
			}
			
			if(returnType.type instanceof LlvmCustomType){
				((LlvmCustomType) returnType.type).setString("%class."+ returnType.type+"* ");
				
			}
			
			return new Method(returnType,n.name.s,formals,locals);
		}
		
		public LlvmValue visit(IntArrayType n){
			System.out.println("ST-IntArrayType");
			
			return new LlvmString(new LlvmPointer(LlvmCustomType.ARRAY));
		}
		
		public LlvmValue visit(BooleanType n){
			System.out.println("ST-BooleanType");
			
			return new LlvmString(LlvmPrimitiveType.I1);
		}
		
		public LlvmValue visit(IntegerType n){
			System.out.println("ST-IntegerType");
			
			return new LlvmString(LlvmPrimitiveType.I32);
		}
		
		public LlvmValue visit(IdentifierType n){
			System.out.println("ST-IdentifierType");
			
			return new LlvmString(n.name);
		}
}
	
class SimpleClass extends LlvmValue{
	
	public String name;
	public List<Variable> variables;
	public List<Method> methods;
	private int currentMethod;
	
	public SimpleClass(String name, List<Variable> variables, List<Method> methods){
		this.name = name;
		this.variables = variables;
		this.methods = methods;
		this.currentMethod = 0;
	}
	
	public int getSizeDeclaration(){
		int totalSize = 0;
		for(Variable v : this.variables){
			totalSize += v.size;
		}
	
		return totalSize;
	}
	
			
	public Method nextMethod(){
		return methods.get(currentMethod++);
	}
	
}

class Variable extends LlvmValue{

	public String name;
	public int size;
	
	public enum Place{
		GLOBAL, FORMAL, LOCAL;
	}
	
	public Place place;
	
    public Variable(String name, LlvmValue type){
    	this.name = name;
    	this.type = type.type;
    	if((type.type instanceof LlvmCustomType && ((LlvmCustomType)type.type).custom == LlvmCustomType.CustomType.CUSTOM_CLASS) || type.type instanceof LlvmPointer){
    		size = 8;
    	}else if(type.type == LlvmPrimitiveType.I32){
    		size = 4;
    	}else if(type.type == LlvmPrimitiveType.I1){
    		size = 1;
    	}
    }

}

class Method extends LlvmValue{

	private LlvmType returnType;
	public String name;
	public List<Variable> formals;
	public List<Variable> locals;
	
	public Method(LlvmValue returnType, String name, List<Variable> formals, List<Variable> locals){
		this.returnType = returnType.type;
		this.name = name;
		this.formals = formals;
		this.locals = locals;
	}
	
	public LlvmType getReturnType(){
		return this.returnType;
	}

}

/*class ClassNode extends LlvmType {
	ClassNode (String nameClass, LlvmStructure classType, ){
	}
}

class MethodNode {
}*/




