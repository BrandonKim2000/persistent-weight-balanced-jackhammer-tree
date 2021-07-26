package cmsc420_f20;

// YOU SHOULD NOT MODIFY THIS FILE

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

/**
 * Command handler. This inputs a single command line, processes the command (by
 * invoking the appropriate method(s) on the PBJ Tree), and returns the result as
 * a string.
 */

public class CommandHandler {

	private PBJTree<String, Airport> tree; // the PBJ Tree

	/**
	 * Construct from a PBJ Tree.
	 * 
	 * @param tree The PBJ Tree
	 */
	public CommandHandler(PBJTree<String, Airport> tree) {
		this.tree = new PBJTree<String, Airport>();
	}

	/**
	 * Process a single command and return the string output. Each command begins
	 * with a timestamp and a command (e.g., find, insert, get-min) followed by a
	 * list of arguments. The arguments are separated by colons (":"). For example,
	 * the command to find the LAX airport is "find:LAX".
	 *
	 * Note that the insert command both first perform a find to avoid generating
	 * exceptions.
	 *
	 * The preorder command gets a listing of the airports in preorder. It prints
	 * this list and also generates a nicely indented (inorder) presentation of the
	 * tree's contents.
	 * 
	 * @param inputLine The input line with the command and parameters.
	 * @return A short summary of the command's execution/result.
	 */

	public String processCommand(String inputLine) throws Exception {
		String output = new String(); // for storing summary output
		Scanner line = new Scanner(inputLine);
		try {
			line.useDelimiter(":"); // use ":" to separate arguments
			int time = (line.hasNext() ? line.nextInt() : 0); // next command
			if (time < 0) {
				throw new Exception("Invalid input: Time value is negative");
			}
			String cmd = (line.hasNext() ? line.next() : ""); // next command

			// -----------------------------------------------------
			// INSERT code name city country x y
			// -----------------------------------------------------
			if (cmd.compareTo("insert") == 0) {
				String code = line.next(); // get parameters
				String name = line.next();
				String city = line.next();
				String country = line.next();
				float x = line.nextFloat();
				float y = line.nextFloat();
				float weight = line.nextFloat();
				if (weight <= 0) {
					throw new Exception("Invalid input: Insert weight is not strictly positive");
				}
				if (tree.find(code, time) != null) { // code already in dictionary?
					output += "insert(" + code + ") at time = " + time + " : failure - duplicate code"
							+ System.lineSeparator();
				} else {
					Airport ap = new Airport(code, name, city, country, x, y); // create airport object
					tree.insert(code, ap, weight, time); // insert into tree
					output += "insert(" + code + ") at time = " + time + " : successful {" + ap.getString("attributes")
							+ "}" + System.lineSeparator();
				}
			}
			// -----------------------------------------------------
			// DELETE code - Not supported!
			// -----------------------------------------------------
			else if (cmd.compareTo("delete") == 0) {
				String code = line.next(); // get parameters
				output += "delete(" + code + ") at time = " + time + " : failure - deletion is not supported"
							+ System.lineSeparator();
			}
			// -----------------------------------------------------
			// CLEAR - Not supported!
			// -----------------------------------------------------
			else if (cmd.compareTo("clear") == 0) {
				output += "clear at time = " + time + " : failure - clear is not supported" + System.lineSeparator();
			}
			// -----------------------------------------------------
			// FIND code
			// -----------------------------------------------------
			else if (cmd.compareTo("find") == 0) {
				String code = line.next(); // get parameters
				Airport result = tree.find(code, time); // find in tree
				output += summarizeFind(cmd, code, result, time); // summarize result
			}
			// -----------------------------------------------------
			// FIND-DOWN code
			// -----------------------------------------------------
			else if (cmd.compareTo("find-down") == 0) {
				String code = line.next(); // get parameters
				Airport result = tree.findDown(code, time); // find in tree
				output += summarizeFind(cmd, code, result, time); // summarize result
			}
			// -----------------------------------------------------
			// FIND-UP code
			// -----------------------------------------------------
			else if (cmd.compareTo("find-up") == 0) {
				String code = line.next(); // get parameters
				Airport result = tree.findUp(code, time); // find in tree
				output += summarizeFind(cmd, code, result, time); // summarize result
			}
			// -----------------------------------------------------
			// GET-MIN
			// -----------------------------------------------------
			else if (cmd.compareTo("get-min") == 0) {
				Airport result = tree.getMin(time); // get min in tree
				output += summarizeGet(cmd, result, time); // summarize result
			}
			// -----------------------------------------------------
			// GET-MAX
			// -----------------------------------------------------
			else if (cmd.compareTo("get-max") == 0) {
				Airport result = tree.getMax(time); // get max in tree
				output += summarizeGet(cmd, result, time); // summarize result
			}
			// -----------------------------------------------------
			// PREORDER - get a preorder list of entries at the
			// current time, and print the tree with indentation
			// -----------------------------------------------------
			else if (cmd.compareTo("preorder") == 0) {
				ArrayList<String> entries = tree.getPreorderList(time);
				Iterator<String> iter = entries.iterator(); // iterator for the list
				output += "Preorder list at time = " + time + ":" + System.lineSeparator();
				while (iter.hasNext()) { // output the preorder list (flat)
					output += "  " + iter.next() + System.lineSeparator();
				}
				output += treeStructure(entries); // summarize tree contents (indented)
			}
			// -----------------------------------------------------
			// FULL-PREORDER - get a preorder list of the entire
			// tree and print with indentation
			// -----------------------------------------------------
			else if (cmd.compareTo("full-preorder") == 0) {
				ArrayList<String> entries = tree.getFullPreorderList();
				Iterator<String> iter = entries.iterator(); // iterator for the list
				output += "Full-Preorder list at time = " + time + ":" + System.lineSeparator();
				while (iter.hasNext()) { // output the preorder list (flat)
					output += "  " + iter.next() + System.lineSeparator();
				}
				output += treeStructure(entries); // summarize tree contents (indented)
			}
			// -----------------------------------------------------
			// Invalid command or empty
			// -----------------------------------------------------
			else {
				if (cmd.compareTo("") == 0)
					System.err.println("Error: Empty line");
				else
					System.err.println("Error: Invalid command - \"" + cmd + "\"");
			}
		} catch (Exception | Error e) { // something went wrong?
			output = "Unexpected error encountered: " + e.getMessage() + System.lineSeparator();
			System.err.print(output);
			e.printStackTrace(System.err);
		} finally { // always executed
			line.close(); // close the input scanner
		}
		return output; // return summary output
	}

	/**
	 * Summarize the results of the find commands.
	 * 
	 * @param cmd    The command (find, find-down, find-up)
	 * @param result The find result (null, if not found)
	 * @return String encoding the results
	 */
	static String summarizeFind(String cmd, String code, Airport result, int time) {
		String output = new String(cmd + "(" + code + ") at time = " + time + ": ");
		if (result != null) {
			output += "found [" + result + "]" + System.lineSeparator();
		} else {
			output += "not found" + System.lineSeparator();
		}
		return output;
	}

	/**
	 * Summarize the results of the get command.
	 * 
	 * @param cmd    The command (get-min or get-max)
	 * @param result The find result (null, if not found)
	 * @return String encoding the results
	 */
	static String summarizeGet(String cmd, Airport result, int time) {
		String output = new String(cmd + " at time = " + time + ": ");
		if (result != null) {
			output += "found [" + result + "]" + System.lineSeparator();
		} else {
			output += "not found" + System.lineSeparator();
		}
		return output;
	}

	/**
	 * Print the tree contents with indentation.
	 * 
	 * @param entries List of entries in preorder
	 * @return String encoding the tree structure
	 */
	static String treeStructure(ArrayList<String> entries) {
		String output = "Tree structure:" + System.lineSeparator();
		Iterator<String> iter = entries.iterator(); // iterator for the list
		if (iter.hasNext()) { // tree is nonempty
			output += treeStructureHelper(iter, "  "); // print everything
		}
		return output;
	}

	/**
	 * Recursive helper for treeStructure. The argument iterator specifies the next
	 * node from the preorder list to be printed, and the argument indent indicates
	 * the indentation to be performed (of the form "| | | ...").
	 * 
	 * @param iter   Iterator for the entries in the list
	 * @param indent String indentation for the current line
	 */
	static String treeStructureHelper(Iterator<String> iter, String indent) {
		final String levelIndent = "| "; // the indentation for each level of the tree
		String output = "";
		if (iter.hasNext()) {
			String entry = iter.next(); // get the next entry
			Boolean isExtern = (entry.length() > 0 && entry.charAt(0) == '['); // external?
			if (isExtern) { // print external node entry
				output += indent + entry + System.lineSeparator();
			} else {
				output += treeStructureHelper(iter, indent + levelIndent); // print left subtree
				output += indent + entry + System.lineSeparator(); // print this node
				output += treeStructureHelper(iter, indent + levelIndent); // print right subtree
			}
		} else {
			System.err.println("Unexpected trailing elements in entries list"); // shouldn't get here!
		}
		return output;
	}
}
