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

public class Codegen extends VisitorAdapter{
	private List<LlvmInstruction> assembler;
	private Codegen codeGenerator;

  	private SymTab symTab;
	private ClassNode classEnv; 	// Aponta para a classe atualmente em uso em symTab
	private MethodNode methodEnv; 	// Aponta para a metodo atualmente em uso em symTab


	public Codegen(){
		assembler = new LinkedList<LlvmInstruction>();
	}

	// Método de entrada do Codegen
	public String translate(Program p, Env env){	
		codeGenerator = new Codegen();
		
		// Preenchendo a Tabela de Símbolos
		// Quem quiser usar 'env', apenas comente essa linha
		// codeGenerator.symTab.FillTabSymbol(p);
		
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

		for (util.List<ClassDecl> c = n.classList; c != null; c = c.tail)
			c.head.accept(this);

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
		
		LlvmLabel thenClause = new LlvmLabel(new LlvmLabelValue("then"));
		LlvmLabel elseClause = new LlvmLabel(new LlvmLabelValue("else"));
		LlvmLabel endClause = new LlvmLabel(new LlvmLabelValue("end"));
		
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
		
		List<LlvmValue> methodList = new ArrayList<LlvmValue>();
		for (util.List<MethodDecl> c = n.methodList; c != null; c = c.tail){
			methodList.add(c.head.accept(this));
		}
		
		String s = "";
		for(int i =0; i<varList.size(); i++){
			LlvmValue v = varList.get(i);
			s = s + v.type;
			if(i+1 < varList.size())
				s = s + ", ";
		}
		
		assembler.add(new LlvmConstantDeclaration("%class."+name, "type {" + s + "}"));

		return name;
	}
	
	public LlvmValue visit(VarDecl n){
		System.out.println("VarDecl");
		
		return null;
	}
	
	public LlvmValue visit(MethodDecl n){
		System.out.println("MethodDecl");
		
		return null;
	}
	
	public LlvmValue visit(Identifier n){
		System.out.println("Identifier");
		
		
		return new LlvmString(n.s);
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
	
	// Todos os visit's que devem ser implementados	
	//public LlvmValue visit(ClassDeclSimple n){System.out.println("ClassDeclSimple");return null;}
	public LlvmValue visit(ClassDeclExtends n){System.out.println("ClassDeclExtends");return null;}
	//public LlvmValue visit(VarDecl n){System.out.println("VarDecl");return null;}
	//public LlvmValue visit(MethodDecl n){System.out.println("MethodDecl");return null;}
	public LlvmValue visit(Formal n){System.out.println("Formal");return null;}
	public LlvmValue visit(IntArrayType n){System.out.println("IntArrayType");return null;}
	public LlvmValue visit(BooleanType n){System.out.println("BooleanType");return null;}
	public LlvmValue visit(IntegerType n){System.out.println("IntegerType");return null;}
	public LlvmValue visit(IdentifierType n){System.out.println("IdentifierType");return null;}
	public LlvmValue visit(Block n){System.out.println("Block");return null;}
	//public LlvmValue visit(If n){return null;}
	public LlvmValue visit(While n){System.out.println("While");return null;}
	public LlvmValue visit(Assign n){System.out.println("Assign");return null;}
	public LlvmValue visit(ArrayAssign n){System.out.println("ArrayAssign");return null;}
	//public LlvmValue visit(And n){return null;}
	//public LlvmValue visit(LessThan n){return null;}
	public LlvmValue visit(Equal n){System.out.println("Equal");return null;}
	//public LlvmValue visit(Minus n){return null;}
	//public LlvmValue visit(Times n){return null;}
	public LlvmValue visit(ArrayLookup n){System.out.println("ArrayLookup");return null;}
	//public LlvmValue visit(ArrayLength n){System.out.println("ArrayLength");return null;}
	public LlvmValue visit(Call n){System.out.println("Call");return null;}
	//public LlvmValue visit(True n){return null;}
	//public LlvmValue visit(False n){return null;}
	public LlvmValue visit(IdentifierExp n){System.out.println("IdentifierExp");return null;}
	public LlvmValue visit(This n){System.out.println("this");return null;}
	//public LlvmValue visit(NewArray n){System.out.println("NewArray");return null;}
	public LlvmValue visit(NewObject n){System.out.println("NewObject");return null;}
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
    public Map<String, ClassNode> classes;     
    private ClassNode classEnv;    //aponta para a classe em uso

    public LlvmValue FillTabSymbol(Program n){
	n.accept(this);
	return null;
}
public LlvmValue visit(Program n){
	n.mainClass.accept(this);

	for (util.List<ClassDecl> c = n.classList; c != null; c = c.tail)
		c.head.accept(this);

	return null;
}

public LlvmValue visit(MainClass n){
	classes.put(n.className.s, new ClassNode(n.className.s, null, null));
	return null;
}

public LlvmValue visit(ClassDeclSimple n){
	List<LlvmType> typeList = null;
	// Constroi TypeList com os tipos das variáveis da Classe (vai formar a Struct da classe)
	
	List<LlvmValue> varList = null;
	// Constroi VarList com as Variáveis da Classe

	classes.put(n.name.s, new ClassNode(n.name.s, 
										new LlvmStructure(typeList), 
										varList)
      			);
    	// Percorre n.methodList visitando cada método
	return null;
}

	public LlvmValue visit(ClassDeclExtends n){return null;}
	public LlvmValue visit(VarDecl n){return null;}
	public LlvmValue visit(Formal n){return null;}
	public LlvmValue visit(MethodDecl n){return null;}
	public LlvmValue visit(IdentifierType n){return null;}
	public LlvmValue visit(IntArrayType n){return null;}
	public LlvmValue visit(BooleanType n){return null;}
	public LlvmValue visit(IntegerType n){return null;}
}

class ClassNode extends LlvmType {
	ClassNode (String nameClass, LlvmStructure classType, List<LlvmValue> varList){
	}
}

class MethodNode {
}




