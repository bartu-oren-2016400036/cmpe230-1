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
		
		boolean operationsStarted = false;
		int startingLine = 0;
		String line = "";
		int lineNumber = 0;
		knownVars = new ArrayList<String>();
		
		writer.println("jmp start");
		writer.println();
		
		// Get variables and syntax error check
		while(in.hasNext()) {
			line = in.nextLine();
			lineNumber++;
			line = line.replaceAll("\\s+"," ");
			if(line.contains("=") && !line.contains("pow") && !line.contains("*") && !line.contains("+")) {
				String[] inputs = line.split("=");
				String[] syn = null;
				List<String> syntax = null; 
				
				if(inputs.length > 2) {
					System.out.println("Line " + lineNumber + ": Syntax error.");
					System.exit(0);
				}
				
				for(String s : inputs) {
					syn = s.split("\\s+");
					syntax = new LinkedList<>(Arrays.asList(syn));		
					syntax.removeAll(Arrays.asList("", null));
					if(syntax.size() != 1) {
						System.out.println("Line " + lineNumber + ": Syntax error.");
						System.exit(0);
					}
				}
				
				line = line.replaceAll("\\s+","");
				inputs = line.split("=");
				if(!operationsStarted) {
					if(inputs[1].matches("^[0-9a-fA-F]+$")) {
						if(inputs[1].length() <= 4) {
							writer.println(inputs[0] + ": dw " + "00000h, 0" + inputs[1] + "h");
						} else if (inputs[1].length() <= 8) {
							writer.println(inputs[0] + ": dw " + "0"+ inputs[1].substring(0, inputs[1].length() - 4) + 
									"h, 0" + inputs[1].substring(inputs[1].length() - 4) + "h");
						} else if(inputs[1].length() == 9 && inputs[1].charAt(0) == '0'){
							writer.println(inputs[0] + ": dw " + "0"+ inputs[1].substring(0, inputs[1].length() - 4) + 
									"h, 0" + inputs[1].substring(inputs[1].length() - 4) + "h");
						} else {
							System.out.println("Line " + lineNumber + ": Syntax error. Number type is wrong.");
							System.exit(0);
						}
					} else if (!knownVars.contains(inputs[1])){
						System.out.println("Line " + lineNumber + ": Syntax error. Undefined Variable.");
						System.exit(0);
					}
				} else if(!knownVars.contains(inputs[0])){
					writer.println(inputs[0] + ": dw 00000h, 00000h");
				}
				
				knownVars.add(inputs[0]);
				
			} else if(!line.contains("pow") && !line.contains("*") && !line.contains("+") && !line.equals(" ") && !line.equals("")){
				String[] syn = null;
				syn = line.split("\\s+");
				List<String> syntax = new LinkedList<>(Arrays.asList(syn));
				syntax.removeAll(Arrays.asList("", null));
				if(syntax.size() != 1) {
					System.out.println("Line " + lineNumber + ": Syntax error.");
					System.exit(0);
				}
			} else {
				operationsStarted = true;
				startingLine = lineNumber - 1;
			}
		}
		
		//Initialize helper variables
		writer.println("const: dw 00000h,00000h");
		writer.println("const2: dw 00000h,00000h");
		writer.println("helper: dw 00000h,00000h");
		writer.println("digit: dw 00000h,00000h,00000h,00000h");
		writer.println();
		
		knownVars.add("const");	
		knownVars.add("const2");
		knownVars.add("helper");
		
		// Writes print function for assembly
		printFunction();
		
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
		
		// Pass to operations
		while(lineNumber < startingLine) {
			line = in.nextLine();
			line = line.replaceAll("\\s+","");
			lineNumber++;
			if(!line.contains("=")) {
				printOut(line);
			}
		}
		
		// Process line by line
		while(in.hasNext()) {
			line = in.nextLine();
			line = line.replaceAll("\\s+"," "); // Reduce whitespace to 1
			lineNumber++;	
			//Get info about what to do in current line.
			if(line.contains("=")) {
				// Check parenthesis usage.
				if(paranthesisCheck(line)) { 
					String[] inputs = line.split("=");
					if(line.contains("pow") || line.contains("*") || line.contains("+")) {
						String postFixRep = postfix(inputs[1]);
						
						List<String> postList = null; 
						String[] postArray = null;
						postArray = postFixRep.split("\\s+");
						postList = new LinkedList<>(Arrays.asList(postArray));		
						postList.removeAll(Arrays.asList("", null));
						
						Stack<String> post = new Stack<String>();
						for(String s: postList) {
							if(s.equals("*")) {
								String val1 = post.pop();
								String val2 = post.pop();
								mult(val1, val2, "helper");
								post.push("helper");
								
							} else if(s.equals("+")) {
								String val1 = post.pop();
								String val2 = post.pop();
								sum(val1, val2, "helper");
								post.push("helper");
								
							} else { 
								post.push(s);
							}
						}
						inputs[0] = inputs[0].replaceAll("\\s+","");
						varAssign(inputs[0], "helper");
						
						if(postFixRep.equals("error")) {
							System.out.println("Line " + lineNumber + ": Syntax error.");
							System.exit(0);
						}
					} else {
						inputs[0] = inputs[0].replaceAll("\\s+","");
						inputs[1] = inputs[1].replaceAll("\\s+","");
						varAssign(inputs[0], inputs[1]);
					}
				} else {
					System.out.println("Line " + lineNumber + ": Syntax error. Paranthesis do not match.");
					System.exit(0);
				}
			} else {
				line = line.replaceAll("\\s+","");
				printOut(line);
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

	// 2 operator yan yana kontrol
	public static String postfix(String line) {
		Stack<Character> operator = new Stack<Character>();
		String postf = "";
		boolean first = true;
		for(int i = 0; i<line.length(); i++) {
			if(line.charAt(i) == ' ') {
				if (first !=true && line.charAt(i+1)!='+' && line.charAt(i+1)!='*'   && line.charAt(i)!=')') {
					return "error";
				}
				postf += line.substring(0, i);
				if(i!=0)
					postf += " ";
				line = line.substring(i+1);
				i=-1;
				first =false;

			}
			else  if(line.charAt(i)=='+'||line.charAt(i)=='*'||line.charAt(i)=='('||line.charAt(i)==')')  {
				postf += line.substring(0, i);
				if(i!=0)
					postf += " ";


				if(line.charAt(i) == ')') {
					while(operator.peek() != '(') {
						postf += operator.pop();
						postf += " ";
					}
					operator.pop();
				}

				else if(operator.isEmpty() || operator.peek() == '(' ||  operator.peek() == '+' || line.charAt(i) == '(' || (line.charAt(i) ==  '*' && operator.peek() == '*')) {
					operator.push(line.charAt(i));
					first = true;
				}

				else if (line.charAt(i) == '+' && operator.peek() == '*'){
					while(!operator.isEmpty() &&  operator.peek() == '*') {
						postf += operator.pop();
						postf += " ";
					}
					operator.push(line.charAt(i));
					first=true;
				}
				line = line.substring(i+1);
				i = -1;
			}
		}
		postf += line;
		postf += " ";

        while(!operator.isEmpty()) {
            postf +=operator.pop();
            postf += " ";
        }

        return postf;

    }

	private static void constAssign(String con) {
		if(con.length() <= 4) {
			writer.println("mov ax, 0" + con + "h");
			writer.println("mov [const+2], ax");
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
	
	private static void endProgram() {
		writer.println("mov ax, 0");
		writer.println("mov ah, 4ch");
		writer.println("int 21h");
		writer.println();
	}
}