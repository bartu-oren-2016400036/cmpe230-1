import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

public class Main {
	static PrintWriter writer = null;
	static List<String> knownVars;
	public static void main(String[] args) {
		Scanner in = null;
		try {
			in = new Scanner(new FileReader(args[0]));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		try {
			writer = new PrintWriter(new FileWriter(args[0].substring(0, args[0].indexOf('.')) + ".asm"));
		} 
		catch (IOException ex) {
			ex.printStackTrace();
		} 

		String line = "";
		int lineNumber = 0;
		knownVars = new ArrayList<String>();

		writer.println("jmp start");
		writer.println();
		writer.println("msg1 db \"Line $\"");
		writer.println("msg2 db \" : Syntax error.\",10,13,\"$\"");

		int maxOperations = 0; //holds max count of operations per line in file 
		int errorNum = 0;
		boolean error = false;

		// process every word, open variables
		while(in.hasNext()) {

			int countOperations = 0; //holds count of operations per line

			line = in.nextLine();

			line = line.replaceAll("\\+", " + ");
			line = line.replaceAll("\\*", " * ");
			line = line.replaceAll("\\(", " ( ");
			line = line.replaceAll("\\)", " ) ");
			line = line.replaceAll("\\=", " = ");
			line = line.replaceAll("pow", " pow ");
			line = line.replaceAll(",", " , ");

			line = line.replaceAll("\\s+"," ");
			
			lineNumber++;

			String [] lineArr = line.split("\\s+");
			List<String> lineList = new LinkedList<>(Arrays.asList(lineArr));
			lineList.removeAll(Arrays.asList("", null));

			for(String s: lineList) {
				if(s.equals("pow") || s.equals("+") || s.equals("*")) {
					countOperations++;
				}
				else if(!s.equals("pow") && !s.equals("+") && !s.equals("*") && !s.equals("(") && !s.equals(")")
						&& !s.equals(",") && !s.equals("=") && !knownVars.contains(s)) {
					if(s.matches("^[0-9a-fA-F]+$") && (s.length() > 9 || (s.length() == 9 && s.charAt(0)!='0'))) {
						if(!error){
							errorNum = lineNumber;
							error = true;
						}
							
					}
					else if(!s.matches("^[0-9a-fA-F]+$")) {
						writer.println(s + ": dw 00000h,00000h");
						knownVars.add(s);
					}
				}
			}
			maxOperations = Math.max(maxOperations, countOperations);
		}
		if(error) {
			writer.println("start:");
			writer.println();
			errorFunction(errorNum);
			in.close();
			writer.close();
			System.exit(0);
		}

		//Initialize helper variables
		for(int i=0; i<maxOperations; i++) {
			writer.println("operation"+ i +": dw 00000h,00000h");
			knownVars.add("operation"+i);
		}
		writer.println("const: dw 00000h,00000h");
		writer.println("const2: dw 00000h,00000h");
		writer.println("helper: dw 00000h,00000h");
		writer.println("digit: dw 00000h,00000h,00000h,00000h");
		writer.println("powerbase: dw 00000h,00000h");
		writer.println("powerexp: dw 00000h,00000h");
		writer.println("powerres: dw 00000h,00000h");

		writer.println();

		knownVars.add("const");	
		knownVars.add("const2");
		knownVars.add("helper");
		knownVars.add("powerbase");
		knownVars.add("powerexp");
		knownVars.add("powerres");

		// Writes print and power function for assembly
		printFunction();
		powerFunction();


		writer.println("start:");
		writer.println();

		// Start second parse
		try {
			lineNumber = 0;
			in.close();
			in = new Scanner(new FileReader(args[0]));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		lineNumber = 0;

		// Process line by line
		while(in.hasNext()) {
			line = in.nextLine();

			line = line.replaceAll("\\+", " + ");
			line = line.replaceAll("\\*", " * ");
			line = line.replaceAll("\\(", " ( ");
			line = line.replaceAll("\\)", " ) ");
			line = line.replaceAll("\\=", " = ");
			line = line.replaceAll("pow", " pow ");
			line = line.replaceAll(",", " , ");

			line = line.replaceAll("\\s+"," ");
			lineNumber++;

			if(!paranthesisCheck(line)) {
				errorFunction(lineNumber);
				in.close();
				writer.close();
				System.exit(0);
			}

			//Get info about what to do in current line.
			if(line.contains("=") && !line.contains("pow") && !line.contains("*") && !line.contains("+")) {
				String[] inputs = line.split("=");
				String[] syn = null;
				List<String> syntax = null; 

				if(inputs.length > 2) {
					errorFunction(lineNumber);
					in.close();
					writer.close();
					System.exit(0);
				}

				for(String s : inputs) {
					syn = s.split("\\s+");
					syntax = new LinkedList<>(Arrays.asList(syn));		
					syntax.removeAll(Arrays.asList("", null));
					if(syntax.size() != 1) {
						errorFunction(lineNumber);
						in.close();
						writer.close();
						System.exit(0);
					}
				}

				line = line.replaceAll("\\s+","");
				inputs = line.split("=");
				varAssign(inputs[0], inputs[1]);
			}
			else if(!line.contains("pow") && !line.contains("*") && !line.contains("+") && !line.equals(" ") && !line.equals("")){
				String[] syn = null;
				syn = line.split("\\s+");
				List<String> syntax = new LinkedList<>(Arrays.asList(syn));
				syntax.removeAll(Arrays.asList("", null));
				if(syntax.size() != 1) {
					errorFunction(lineNumber);
					in.close();
					writer.close();
					System.exit(0);
				}

				line = line.replaceAll("\\s+","");
				printOut(line);
			}
			else if(line.contains("pow") || line.contains("*") || line.contains("+")) {
				String[] inputs = line.split("=");
				String[] syn = null;
				List<String> syntax = null; 

				if(inputs.length > 2) {
					errorFunction(lineNumber);
					in.close();
					writer.close();
					System.exit(0);
				}

				syn = inputs[0].split("\\s+");
				syntax = new LinkedList<>(Arrays.asList(syn));		
				syntax.removeAll(Arrays.asList("", null));
				if(syntax.size() != 1) {
					errorFunction(lineNumber);
					in.close();
					writer.close();
					System.exit(0);
				}


				if(line.contains("pow")) {
					int openParCount = 0;
					boolean lastPow = false;
					boolean inPowBase = false;
					boolean inPowExp = false;
					String powerBase = "";
					String powerExp = "";

					String [] powArr = null;
					powArr = inputs[1].split("\\s+");
					List<String> powList = new LinkedList<>(Arrays.asList(powArr));
					powList.removeAll(Arrays.asList("", null));

					for(String s: powList) {
						if(s.equals("pow")) {
							if(lastPow) {
								errorFunction(lineNumber);
								in.close();
								writer.close();
								System.exit(0);
							}
							lastPow = true;
							inPowBase = true;
						}
						else if(lastPow && !s.equals("(")) {
							errorFunction(lineNumber);
							in.close();
							writer.close();
							System.exit(0);
						}
						else if(lastPow) {
							lastPow = false;
						}
						else if(inPowBase && s.equals(",")) {
							inPowBase = false;
							inPowExp = true;
							powerBase.replaceAll("\\s+"," ");
							if(!powerBase.equals("") && !powerBase.equals(" ") && paranthesisCheck(powerBase)) {
								List<String> post = postFix(powerBase);
								if(post == null) {
									errorFunction(lineNumber);
									in.close();
									writer.close();
									System.exit(0);
								}
							}else {
								errorFunction(lineNumber);
								in.close();
								writer.close();
								System.exit(0);
							}
						}
						else if(inPowBase) {
							powerBase += s;
							powerBase += " ";

						}
						else if(inPowExp && s.equals("(")) {
							powerExp += s;
							powerExp += " ";
							openParCount++;
						}
						else if(inPowExp && s.equals(")") && openParCount != 0) {
							powerExp += s;
							powerExp += " ";
							openParCount--;
						}
						else if(inPowExp && s.equals(")") && openParCount == 0) {
							inPowExp = false;
							powerExp.replaceAll("\\s+"," ");
							if(!powerExp.equals("") && !powerExp.equals(" ") && paranthesisCheck(powerExp)) {
								List<String> post = postFix(powerExp);
								if(post == null) {
									errorFunction(lineNumber);
									in.close();
									writer.close();
									System.exit(0);
								}
							}else {
								errorFunction(lineNumber);
								in.close();
								writer.close();
								System.exit(0);
							}
						}
						else if(inPowExp) {
							powerExp += s;
							powerExp += " ";
						}
					}
					inputs[1] = inputs[1].replaceAll("pow", "");
					inputs[1] = inputs[1].replaceAll(",", ") ^ (");
				}
				List<String> postList = postFix(inputs[1]);

				if(postList == null) {
					errorFunction(lineNumber);
					in.close();
					writer.close();
					System.exit(0);
				}

				postList.removeAll(Arrays.asList("", null));

				Stack<String> post = new Stack<String>();
				int operCount = 0;
				
				for(String s: postList) {
					if(s.equals("*")) {
						String val2 = post.pop();
						String val1 = post.pop();
						String resultName = "operation" + operCount;
						mult(val1, val2, resultName);
						post.push(resultName);
						operCount++;

					} else if(s.equals("+")) {
						String val2 = post.pop();
						String val1 = post.pop();
						String resultName = "operation" + operCount;
						sum(val1, val2, resultName);
						post.push(resultName);
						operCount++;

					} else if(s.equals("^")) {
						String val2 = post.pop();
						String val1 = post.pop();
						String resultName = "operation" + operCount;
						power(val1, val2, resultName);
						post.push(resultName);
						operCount++;

					} else { 
						post.push(s);
					}
				}

				inputs[0] = inputs[0].replaceAll("\\s+","");
				operCount--;
				String last = "operation" + operCount;
				varAssign(inputs[0], last);
			}

		}

		endProgram();
		in.close();
		writer.close();
	}

	public static boolean paranthesisCheck(String line) {
		int par1o = 0;
		int par1c = 0;
		int par2o = 0;
		int par2c = 0;
		int par3o = 0;
		int par3c = 0;

		for (int i = 0; i < line.length(); i++) {
			if(line.charAt(i) == '('){
				par1o++;
			}
			else if(line.charAt(i) == ')'){
				par1c++;
			}
			else if(line.charAt(i) == '['){
				par2o++;
			}
			else if(line.charAt(i) == ']'){
				par2c++;
			}
			else if(line.charAt(i) == '{'){
				par3o++;
			}
			else if(line.charAt(i) == '}'){
				par3c++;
			}
			if(par1c > par1o || par2c > par2o || par3c > par3o) {
				return false;
			}
		}

		return (par1o == par1c && par2o == par2c && par3o == par3c);
	}

	public static List<String> postFix(String line) {

		Stack<String> operator = new Stack<String>();
		List<String> postF = new LinkedList<String>();
		boolean lastOpt = true;

		line = line.replaceAll("\\+", " + ");
		line = line.replaceAll("\\*", " * ");
		line = line.replaceAll("\\(", " ( ");
		line = line.replaceAll("\\)", " ) ");
		line = line.replaceAll("\\^", " ^ ");


		line = line.replaceAll("\\s+"," ");

		String [] lineArr = null;
		lineArr = line.split("\\s+");
		List<String> lineList = new LinkedList<>(Arrays.asList(lineArr));

		lineList.removeAll(Arrays.asList("", null));

		for(String s: lineList) {
			if(s.equals("+") || s.equals("*") || s.equals("^") || s.equals("(") || s.equals(")")) {

				if(lastOpt == true && !s.equals("("))
					return null;

				lastOpt = true;

				if(s.equals(")")) {
					while(!operator.peek().equals("(")) {
						postF.add(operator.pop());
					}
					operator.pop();
					lastOpt = false;
				}

				else if(operator.isEmpty() || operator.peek().equals("(") || operator.peek().equals("+") || s.equals("(") || 
						(operator.peek().equals("*") && !s.equals("+")) || (operator.peek().equals("^") && s.equals("^"))) {
					operator.push(s);
				}

				else if (s.equals("+") && (operator.peek().equals("*") || operator.peek().equals("^"))){
					while(!operator.isEmpty() && (operator.peek().equals("*") || operator.peek().equals("^"))) {
						postF.add(operator.pop());
					}
					operator.push(s);
				}
				else if (s.equals("*") && operator.peek().equals("^")){
					while(!operator.isEmpty() &&  operator.peek().equals("^")) {
						postF.add(operator.pop());
					}
					operator.push(s);
				}	
			}
			else if(!s.equals("")) {
				if(lastOpt == false) {
					return null;
				}
				postF.add(s);
				lastOpt = false;
			}
		}
		while(!operator.isEmpty()) {
			postF.add(operator.pop());
		}

		return postF;
	}

	private static void constAssign(String con) {
		if(con.length() <= 4) {
			writer.println("mov ax, 0" + con + "h");
			writer.println("mov [const+2], ax");
			writer.println("mov ax, 0");
			writer.println("mov [const], ax");
			writer.println();

		} else {
			writer.println("mov ax, 0" + con.substring(con.length()-4) + "h");
			writer.println("mov [const+2], ax");
			writer.println("mov ax, 0" + con.substring(0, con.length()-4) + "h");
			writer.println("mov [const], ax");
			writer.println();
		}
	}

	private static void const2Assign(String con) {
		if(con.length() <= 4) {
			writer.println("mov ax, 0" + con + "h");
			writer.println("mov [const2+2], ax");
			writer.println("mov ax, 0");
			writer.println("mov [const2], ax");
			writer.println();

		} else {
			writer.println("mov ax, 0" + con.substring(con.length()-4) + "h");
			writer.println("mov [const2+2], ax");
			writer.println("mov ax, 0" + con.substring(0, con.length()-4) + "h");
			writer.println("mov [const2], ax");
			writer.println();
		}
	}

	private static void varAssign(String left, String right) {
		if(!knownVars.contains(right)) {
			const2Assign(right);
			varAssign(left, "const2");
			return;
		}
		writer.println("mov ax, [" + right + "]");
		writer.println("mov " + "[" + left + "], ax");
		writer.println("mov ax, [" + right + "+2]");
		writer.println("mov " + "[" + left + "+2], ax");
		writer.println();
	}

	private static boolean isAnyConst(String var1, String var2) {
		if(!knownVars.contains(var1) || !knownVars.contains(var2)) {
			if(!knownVars.contains(var1))
				constAssign(var1);
			if(!knownVars.contains(var2))
				const2Assign(var2);
			return true;
		}
		return false;
	}

	private static void sum(String var1, String var2, String result) {
		if(isAnyConst(var1, var2)) {
			if(!knownVars.contains(var1) && !knownVars.contains(var2)) {
				sum("const", "const2", result);
				return;
			} else if(!knownVars.contains(var1)) {
				sum("const", var2, result);
				return;
			} else {
				sum(var1, "const2", result);
				return;
			}
		}
		writer.println("mov ax, [" + var2 + "+2]");
		writer.println("add ax, [" + var1 + "+2]");
		writer.println("mov dx, [" + var2 + "]");
		writer.println("adc dx, [" + var1 + "]");
		writer.println("mov [helper], dx");
		writer.println("mov [helper+2], ax");
		writer.println();
		varAssign(result, "helper");
	}

	private static void mult(String var1, String var2, String result) {
		if(isAnyConst(var1, var2)) {
			if(!knownVars.contains(var1) && !knownVars.contains(var2)) {
				mult("const", "const2", result);
				return;
			} else if(!knownVars.contains(var1)) {
				mult("const", var2, result);
				return;
			} else {
				mult(var1, "const2", result);
				return;
			}
		}
		writer.println("mov ax, [" + var2 + "+2]");
		writer.println("mov bx, [" + var1 + "+2]");
		writer.println("mul bx");
		writer.println("mov [helper], dx");
		writer.println("mov [helper+2], ax");
		writer.println();
		writer.println("mov ax, [" + var2 + "]");
		writer.println("mov bx, [" + var1 + "+2]");
		writer.println("mul bx");
		writer.println("add [helper], ax");
		writer.println();
		writer.println("mov ax, [" + var2 + "+2]");
		writer.println("mov bx, [" + var1 + "]");
		writer.println("mul bx");
		writer.println("add [helper], ax");
		writer.println();
		varAssign(result, "helper");
	}


	private static void printOut(String output) {
		varAssign("helper", output);
		writer.println("call print");
		writer.println("mov ax, [helper+2]");
		writer.println("mov [helper], ax");
		writer.println("call print");
		writer.println("mov ah, 02h");
		writer.println("mov dx, 13");
		writer.println("int 21h");
		writer.println("mov dx, 10");
		writer.println("int 21h");
		writer.println();
	}

	private static void power(String base, String exp, String result) {
		if(isAnyConst(base, exp)) {
			if(!knownVars.contains(base) && !knownVars.contains(exp)) {
				power("const", "const2", result);
				return;
			} else if(!knownVars.contains(base)) {
				power("const", exp, result);
				return;
			} else {
				power(base, "const2", result);
				return;
			}
		}
		varAssign("powerbase", base);
		varAssign("powerexp", exp);
		writer.println("call powerloop");
		varAssign(result, "helper");
	}

	private static void printFunction() {
		writer.println("print:");
		writer.println("mov ax, [helper]");
		writer.println("shr ax, 12");
		writer.println("mov [digit], ax");
		writer.println();
		writer.println("mov ax, [helper]");
		writer.println("shl ax, 4");
		writer.println("shr ax, 12");
		writer.println("mov [digit+2], ax");
		writer.println();
		writer.println("mov ax, [helper]");
		writer.println("shl ax, 8");
		writer.println("shr ax, 12");
		writer.println("mov [digit+4], ax");
		writer.println();
		writer.println("mov ax, [helper]");
		writer.println("shl ax, 12");
		writer.println("shr ax, 12");
		writer.println("mov [digit+6], ax");
		writer.println();
		writer.println("mov ax, 0");
		writer.println();
		writer.println("mov ah, 02h");
		writer.println("mov dx, [digit]");
		writer.println("add dx, 30h");
		writer.println("cmp dx, 57");
		writer.println("jle less");
		writer.println("add dx, 27h");
		writer.println("less:");
		writer.println("int 21h");
		writer.println();
		writer.println("mov dx, [digit+2]");
		writer.println("add dx, 30h");
		writer.println("cmp dx, 57");
		writer.println("jle less2");
		writer.println("add dx, 27h");
		writer.println("less2:");
		writer.println("int 21h");
		writer.println();
		writer.println("mov dx, [digit+4]");
		writer.println("add dx, 30h");
		writer.println("cmp dx, 57");
		writer.println("jle less3");
		writer.println("add dx, 27h");
		writer.println("less3:");
		writer.println("int 21h");
		writer.println();
		writer.println("mov dx, [digit+6]");
		writer.println("add dx, 30h");
		writer.println("cmp dx, 57");
		writer.println("jle less4");
		writer.println("add dx, 27h");
		writer.println("less4:");
		writer.println("int 21h");
		writer.println("ret");
		writer.println();
	}
	private static void powerFunction() {
		writer.println("powerloop:");
		writer.println("");
		writer.println("mov ax, [powerbase]");
		writer.println("mov [powerres], ax");
		writer.println("mov [helper], ax");
		writer.println("mov ax, [powerbase+2]");
		writer.println("mov [powerres+2], ax");
		writer.println("mov [helper+2], ax");
		writer.println("");
		writer.println("control0:");
		writer.println("");
		writer.println("mov ax, [powerexp]");
		writer.println("cmp ax, 0");
		writer.println("jnz control1");
		writer.println("mov ax, [powerexp+2]");
		writer.println("cmp ax, 0");
		writer.println("jnz control1");
		writer.println("mov ax, 0");
		writer.println("mov [helper], ax");
		writer.println("mov ax, 1");
		writer.println("mov [helper+2], ax");
		writer.println("ret");
		writer.println("");
		writer.println("control1:");
		writer.println("");
		writer.println("mov ax, [powerexp]");
		writer.println("cmp ax, 0");
		writer.println("jnz controlbase0");
		writer.println("mov ax, [powerexp+2]");
		writer.println("cmp ax, 1");
		writer.println("jnz controlbase0");
		writer.println("ret");
		writer.println("");
		writer.println("controlbase0:");
		writer.println("");
		writer.println("mov ax, [powerbase]");
		writer.println("cmp ax, 0");
		writer.println("jnz controlbase1");
		writer.println("mov ax, [powerbase+2]");
		writer.println("cmp ax, 0");
		writer.println("jnz controlbase1");
		writer.println("mov ax, 0");
		writer.println("mov [helper], ax");
		writer.println("mov [helper+2], ax");
		writer.println("ret");
		writer.println("");
		writer.println("controlbase1: ");
		writer.println("");
		writer.println("mov ax, [powerbase]");
		writer.println("cmp ax, 0");
		writer.println("jnz operation");
		writer.println("mov ax, [powerbase+2]");
		writer.println("cmp ax, 1");
		writer.println("jnz operation");
		writer.println("mov ax, 0");
		writer.println("mov [helper], ax");
		writer.println("mov ax,1");
		writer.println("mov [helper+2], ax");
		writer.println("ret");
		writer.println("");
		writer.println("operation:");
		writer.println("");
		writer.println("mov ax, [powerbase+2]");
		writer.println("mov bx, [powerres+2]");
		writer.println("mul bx");
		writer.println("mov [helper], dx");
		writer.println("mov [helper+2], ax");
		writer.println("mov ax, [powerres]");
		writer.println("mov bx, [powerbase+2]");
		writer.println("mul bx");
		writer.println("add [helper], ax");
		writer.println("mov ax, [powerres+2]");
		writer.println("mov bx, [powerbase]");
		writer.println("mul bx");
		writer.println("add [helper], ax");
		writer.println("mov ax, [helper]");
		writer.println("mov [powerres], ax");
		writer.println("mov ax, [helper+2]");
		writer.println("mov [powerres+2], ax");
		writer.println("");
		writer.println("mov ax, [powerexp+2]");
		writer.println("dec ax");
		writer.println("mov [powerexp+2], ax");
		writer.println("jmp control0");
		writer.println("");
	}
	private static void errorFunction(int errorNum) {
		Stack<Integer> reverseOrder = new Stack<Integer>();
		while(errorNum != 0) {
			reverseOrder.add(errorNum % 10);
			errorNum = errorNum / 10;
		}
		writer.println("");
		writer.println("mov ah,09");
		writer.println("mov dx,offset msg1");
		writer.println("int 21h");
		writer.println("");
		while(!reverseOrder.isEmpty()) {
			writer.println("mov dl," + "\"" + reverseOrder.pop() + "\"");
			writer.println("mov ah,02");
			writer.println("int 21h");
		}
		writer.println("mov ah,09");
		writer.println("mov dx,offset msg2");
		writer.println("int 21h");
		writer.println("");
		endProgram();
		
	}

	private static void endProgram() {
		writer.println("mov ax, 0");
		writer.println("mov ah, 4ch");
		writer.println("int 21h");
		writer.println();
	}
}