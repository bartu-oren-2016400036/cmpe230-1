import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
		Scanner in = null;
		try {
			in = new Scanner(new FileReader(args[0]));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		String line = "";
		int lineNumber = 0;
		
		// Process line by line
		while(in.hasNext()) {
			line = in.nextLine();
			line = line.replaceAll("\\s+",""); // Delete whitespace
			lineNumber++;

			if(line.contains("=")) {
				if(line.length() - line.replace("(", "").length() == line.length() - line.replace(")", "").length()) { // Check paranthesis usage.
					if(!line.contains("pow") && !line.contains("*") && !line.contains("+")) {
						// Assignment
					} else {
						// Evaluate
					}
				} else {
					System.out.println("Line " + lineNumber + ": Syntax error.");
				}
			} else {
				// Output
			}
		}
	}

}
