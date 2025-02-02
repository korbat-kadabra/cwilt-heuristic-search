/**
 * 
 * Author: Christopher Wilt
 *         University of New Hampshire
 *         Artificial Intelligence Research Group
 * 
 */
package org.cwilt.search.utils.basic;
import java.util.Arrays;

public class ArgParser {
	public enum ParamType {
		STRING, FLOAT, INTEGER, FLAG, STRINGARR
	};

	public static class ArgTypes {
		public class InvalidFlag extends Error{

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;};
		public ParamType getPt() {
			return pt;
		}

		public String getFlagName() {
			return flagName;
		}

		private ParamType pt;
		private String flagName;

		public ArgTypes(String flagName, ParamType pt) throws InvalidFlag {
			if(flagName.charAt(0) != '-'){
				throw new InvalidFlag();
			}
			this.pt = pt;
			this.flagName = flagName;
		}
	}

	public static class InvalidCommandLineInput extends Exception {

		/**
		*
		*/
		private static final long serialVersionUID = 1L;

	};

	private static int findLastIndex(int index, String[] args, ArgTypes[] flags){
		int max = args.length;
		for (int i = 0; i < flags.length; i++) {
			for (int j = 0; j < args.length; j++) {
				if (args[j].compareTo(flags[i].getFlagName()) == 0) {
					if(j > index && j < max)
						max = j;
				}
			}
		}
		return max;
	}
	
	public static Object extractArgument(ParamType pt, String[] value, int index, ArgTypes[] pts)
			throws InvalidCommandLineInput {
		if (pt == ParamType.FLAG)
			return true;
		if (value.length <= index + 1 && pt != ParamType.STRINGARR)
			throw new InvalidCommandLineInput();
		switch (pt) {
		case STRING:
			return value[index + 1];
		case FLOAT:
			return (Double.parseDouble(value[index + 1]));
		case INTEGER:
			return (Long.parseLong(value[index + 1]));
		case STRINGARR:
			return Arrays.copyOfRange(value, index + 1, findLastIndex(index, value, pts));
		default:
			return null;
		}

	}

	public static Object[] parseArgs(ArgTypes[] flags, String[] args)
			throws InvalidCommandLineInput {
		Object[] argsToReturn = new Object[flags.length];

		// search for each flag
		for (int i = 0; i < flags.length; i++) {
			for (int j = 0; j < args.length; j++) {
				if (args[j].compareTo(flags[i].getFlagName()) == 0) {
					argsToReturn[i] = extractArgument(flags[i].getPt(), args, j, flags);
				}
			}
		}
		return argsToReturn;
	}

	private static final ArgParser.ArgTypes commandArgs[] = {
			new ArgParser.ArgTypes("-alg", ArgParser.ParamType.STRING),
			new ArgParser.ArgTypes("-time", ArgParser.ParamType.FLOAT),
			new ArgParser.ArgTypes("-problem", ArgParser.ParamType.STRING),
			new ArgParser.ArgTypes("-rest", ArgParser.ParamType.STRINGARR),
	};

	public static void main(String[] args) throws InvalidCommandLineInput {
		if (args.length != 0) {
			Object[] s = ArgParser.parseArgs(commandArgs, args);
			System.err.println("alg is " + s[0]);
			System.err.println("time is " + s[1]);
			System.err.println("problem is " + s[2]);
			System.err.println("rest is " + Arrays.deepToString((Object[])s[3]));
		}

	}

}