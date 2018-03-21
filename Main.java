import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Scanner;
import java.util.Stack;

public class Main {

	public static void main(String[] args) {
		Scanner in = null;
		PrintWriter writer = null;
		
		try {
			in = new Scanner(new FileReader(args[0]));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
		    writer = new PrintWriter(new FileWriter(args[0].substring(0, args[0].indexOf('.')) + ".asm"));
		    writer.print("ags");
		    writer.print("ags");
		} 
		catch (IOException ex) {
			ex.printStackTrace();
		} 
		
		String line = "";
		int lineNumber = 0;
		String[] knownVars;
		
		// Process line by line
		while(in.hasNext()) {
			line = in.nextLine();
			line = line.replaceAll("\\s+"," "); // Reduce whitespace to 1
			lineNumber++;

			if(line.contains("=")) {
				if(paranthesisCheck(line)) { // Check parenthesis usage.
					String[] inputs = line.split("=");
					if(!line.contains("pow") && !line.contains("*") && !line.contains("+")) {
						// Assignment
						
					} else {
						String postFixRep = postfix(inputs[1]);
						if(postFixRep.equals("error"))
							System.out.println("Line " + lineNumber + ": Syntax error.");
					}
				} else {
					System.out.println("Line " + lineNumber + ": Syntax error.");
				}
			} else {
				// Output
			}
		}
		
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
	
	public static String postfix(String line) {
		Stack<Character> operator = new Stack<Character>(); 
		String postf = "";
		boolean first = true;
		for(int i = 0; i<line.length(); i++) {
			if(line.charAt(i) == ' ') {
				if (first !=true && (line.charAt(i+1)!='+' || line.charAt(i+1)!='*' || line.charAt(i+1)!='(' || line.charAt(i)!=')')) {
					return "error";
				}
				line = line.substring(i+1);
				i=-1;
				
			}
			else if(line.charAt(i)=='+'||line.charAt(i)=='*'||line.charAt(i)=='('||line.charAt(i)==')') {
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

				else if(operator.isEmpty() || operator.peek() == '(' || operator.peek() == '+' || line.charAt(i) == '(' || (line.charAt(i) == '*' && operator.peek() == '*')) {
					operator.push(line.charAt(i));
				}

				else if (line.charAt(i) == '+' && operator.peek() == '*'){
					while(operator.peek() == '*') {
						postf += operator.pop();
						postf += " ";
					}
					operator.push(line.charAt(i));
				}					
				line = line.substring(i+1);
				first = false;
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

}