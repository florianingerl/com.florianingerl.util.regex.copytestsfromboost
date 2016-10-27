package com.florianingerl.util.regex.copytestsfromboost;

import com.florianingerl.util.regex.*;
import java.nio.file.Files;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import java.io.PrintWriter;
import java.io.FileInputStream;
import java.io.File;
/**
 * Hello world!
 *
 */
public class Main
{
	private static final String NO_ESCAPE = "(?<!(?<!\\\\)\\\\(\\\\{2}){0,10})";

	static Pattern pTestFunctions = Pattern.compile("\\bvoid\\s++(?=[a-zA-Z])(?<functionName>(?>\\w*?test\\w*))\\s*+\\(\\s*+\\)\\s*+(?<functionBody>\\{(//.*+(\r)?\n|/\\*[\\s\\S]*?\\*/|\"(?:\\\\.|[^\"\\\\]++)*+\"|[^\"{}/]++|(?functionBody))*+\\})");

	static Pattern p = Pattern.compile("TEST_REGEX_SEARCH\\s*\\(\\s*(?<regex>(?<javaString>\"(\\\\.|[^\"])*\"))\\s*,\\s*(?<options>((perl|icase|nosubs)\\s*(\\||(?=,)))+),\\s*(?<input>(?javaString))\\s*,\\s*(?<otheroptions>((match_default|match_not_dot_newline|match_single_line|match_no_subs)\\s*(\\||(?=,)))+),\\s*make_array\\s*\\((?<array>[^)]+)\\)\\s*\\)\\s*;");
	
	private static final File boostDir = new File("C:/Software/boost.regex");
	
	private static final String [] testFiles = new String[]{ "test/regress/basic_tests.cpp", "test/regress/test_tricky_cases.cpp", "test/regress/test_alt.cpp" };
	
    public static void main( String[] args )
    {
		try{
			
			
			
			StringBuilder sbMain = new StringBuilder("\tpublic static void main(String [] args){\n");
			
			StringBuilder sbFunctions = new StringBuilder();
			
			for(String testFile : testFiles){
			
			String cpp_file_content = IOUtils.toString( new FileInputStream(new File(boostDir, testFile) ) ); 
			System.out.println(cpp_file_content);
			
			Matcher mTestFunctions = pTestFunctions.matcher(cpp_file_content);
		
			while(mTestFunctions.find() ){
				System.out.println(mTestFunctions.group() );
		
				sbMain.append("\t\t"+ mTestFunctions.group("functionName") + "();\n");
				
				sbFunctions.append("\tprivate static void " + mTestFunctions.group("functionName") + "(){\n");
				String functionBody = mTestFunctions.group("functionBody");
				System.out.println(mTestFunctions.group("functionBody") );
				
				Matcher m = p.matcher(functionBody);
				
				while(m.find() ){
					String regex = Main.adaptRegex(m.group("regex") );
					if(regex == null) continue;
					sbFunctions.append("\t\tcheck(").append( regex ).append(",0");
					if( m.group("options").contains("icase") )
						sbFunctions.append("|Pattern.CASE_INSENSITIVE");
					if( !m.group("otheroptions").contains("match_not_dot_newline") )
						sbFunctions.append("|Pattern.DOTALL");
					sbFunctions.append(",").append(m.group("input") ).append(", new int[]{").append( m.group("array") ).append("});\n");
				}
				
				sbFunctions.append("\n\t\treport(\"" + mTestFunctions.group("functionName") + "\");\n");
				sbFunctions.append("\t}\n\n");
			}
			
			}
			
			sbMain.append("\t\tif(failure){throw new RuntimeException(\"RegExTest failed, 1st failure: \" + firstFailure); }\n");
			sbMain.append("\t\telse{System.err.println(\"OKAY: All tests passed.\");}\n\t}\n");
			
			StringBuilder sbClass = new StringBuilder("package com.florianingerl.util.regex;\n");
			sbClass.append("import static org.junit.Assert.assertTrue;\n");
			sbClass.append("import org.junit.Test;\n");
			sbClass.append("public class BoostRegExTest {\n\n");
			sbClass.append("\tprivate static boolean failure = false;\n");
			sbClass.append("\tprivate static int failCount = 0;\n");
			sbClass.append("\tprivate static String firstFailure = null;\n\n");
			appendCallMainFunction(sbClass);
			sbClass.append(sbMain);
			appendReportFunction(sbClass);
			appendCheckFunction(sbClass);
			sbClass.append(sbFunctions);
			sbClass.append("}");
			
			try(  PrintWriter out = new PrintWriter( "BoostRegExTest.java" )  ){
				out.println( sbClass.toString() );
			}
		
			System.out.println("File BoostRegExTest.java was successfully written!");
			
		}catch(Exception e){
			System.out.println(e.getMessage() );
		}
    }
	
	private static void appendCallMainFunction(StringBuilder sb){
		sb.append("	@Test\n");
		sb.append("	public void callMain() {\n");
		sb.append("		try {\n");
		sb.append("			main(null);\n");
		sb.append("		} catch (Exception e) {\n");
		sb.append("			assertTrue(e.getMessage(), false);\n");
		sb.append("		}\n");
		sb.append("	}\n");	
	}
	
	private static void appendReportFunction(StringBuilder sb){
		sb.append("	private static void report(String testName) {\n");
		sb.append("		int spacesToAdd = 30 - testName.length();\n");
		sb.append("		StringBuffer paddedNameBuffer = new StringBuffer(testName);\n");
		sb.append("		for (int i = 0; i < spacesToAdd; i++)\n");
		sb.append("			paddedNameBuffer.append(\" \");\n");
		sb.append("		String paddedName = paddedNameBuffer.toString();\n");
		sb.append("		System.err.println(paddedName + \": \" + (failCount == 0 ? \"Passed\" : \"Failed(\" + failCount + \")\"));\n");
		sb.append("		if (failCount > 0) {\n");
		sb.append("			failure = true;\n");
		sb.append("			if (firstFailure == null) {\n");
		sb.append("				firstFailure = testName;\n");
		sb.append("			}\n");
		sb.append("		}\n");
		sb.append("		failCount = 0;\n");
		sb.append("	}\n");
	}
	
	private static void appendCheckFunction(StringBuilder sb){
		sb.append("	private static void check(String regex, int flags, String s, int [] data ){\n");
		sb.append("		Pattern p = Pattern.compile(regex, flags );\n");
		sb.append("		Matcher m = p.matcher(s);\n");
		sb.append("		int i = 0;\n");
		sb.append("		while(data[i] != -2 ){\n");
		sb.append("			if(!m.find() ){\n");
		sb.append("				++failCount;\n");
		sb.append("				return;\n");
		sb.append("			}\n");
		sb.append("			int j = 0;\n");
		sb.append("			while( data[i] != -2 ){\n");
		sb.append("				if(m.start(j) != data[i++] || m.end(j) != data[i++] ){\n");
		sb.append("					++failCount;\n");
		sb.append("				}\n");
		sb.append("				++j;\n");
		sb.append("			}\n");
		sb.append("			++i;\n");
		sb.append("		}\n");
		sb.append("		if( m.find() )++failCount;\n");
		sb.append("	}\n");
	}
	
	static String adaptRegex(String regex){
		if(containsRelativeBackReference(regex) ) return null;
		if(containsUnsupportedRecursion(regex) ) return null;
		if(containsUnsupportedConditional(regex) ) return null;
		regex = adaptWrongBackReferences(regex);
		regex = adaptWrongNamedGroups(regex);
		regex = adaptWrongCommentaries(regex);
		regex = adaptWordBoundaries(regex);
		regex = escapeGreaterAndSmallerSigns(regex);
		regex = adaptWrongRecursion(regex);
		regex = adaptWrongConditionalBasedOnValidGroupCapture(regex);
		regex = adaptCharacterClasses(regex);
		if(containsRecursionToGroupThatsNotYetDeclared(regex) ) return null;
		return regex;
	}
	
	private static boolean containsRelativeBackReference(String regex){
		return Pattern.compile(NO_ESCAPE + "\\\\g(-\\d+|\\{-\\d+\\})").matcher(regex).find();
	}
	
	private static String adaptWrongBackReferences(String regex){
		Pattern p = Pattern.compile(NO_ESCAPE + "\\\\g((?<groupNumber>\\d+)|\\{(?groupNumber)\\})");
		Matcher m = p.matcher(regex);
		regex = m.replaceAll("\\\\${groupNumber}");
		p = Pattern.compile(NO_ESCAPE + "\\\\g\\{(?<groupName>[a-zA-Z][a-zA-Z0-9]*)\\}");
		m = p.matcher(regex);
		return m.replaceAll("\\\\k<${groupName}>");
	}
	
	private static String adaptWrongNamedGroups(String regex){
		Pattern p = Pattern.compile(NO_ESCAPE + "\\(\\?'(?<groupName>[a-zA-Z][a-zA-Z0-9]*)'");
		Matcher m = p.matcher(regex);
		return m.replaceAll("(?<${groupName}>");
	}
	
	private static String adaptWrongCommentaries(String regex){
		Pattern p = Pattern.compile(NO_ESCAPE + "\\(\\?#(?<comment>[\\s\\S]*?)\\)");
		Matcher m = p.matcher(regex);
		return m.replaceAll("(?x:#${comment}\\\\n)");
	}
	
	private static String adaptWordBoundaries(String regex){
		return Pattern.compile(NO_ESCAPE + "\\\\[><]").matcher(regex).replaceAll("\\\\b");
	}
	
	private static String escapeGreaterAndSmallerSigns(String regex){
		String namedGroup = "\\(\\?\\<(?<groupName>[a-zA-Z][a-zA-Z0-9]*)\\>";
		String conditionBasedOnValidGroupCapture = "\\(\\?\\(\\<(?groupName)\\>";
		String backReference = "\\\\k\\<(?groupName)\\>";
		String characterClass = "\\[(\\\\.|[^\\]])*\\]";
		String lookbehind = "\\(\\?\\<[!=]";
		Pattern p = Pattern.compile(NO_ESCAPE + "(" + namedGroup + "|" + conditionBasedOnValidGroupCapture + "|" + backReference + "|" + characterClass + "|" + lookbehind + "|[><])");
		
		Matcher m = p.matcher(regex);
		
		return m.replaceAll( (Matcher matcher) -> { 
			if(matcher.group().startsWith("<") || matcher.group().startsWith(">") ){
				return "\\\\" + matcher.group();
			}
			return matcher.group();
			} );
	}
	
	private static String adaptWrongRecursion(String regex){
		return Pattern.compile(NO_ESCAPE + "\\(\\?&(?<groupName>[a-zA-Z][a-zA-Z0-9]*)\\)").matcher(regex).replaceAll("(?${groupName})");
	}
	
	private static String adaptWrongConditionalBasedOnValidGroupCapture(String regex){
		return Pattern.compile(NO_ESCAPE + "\\(\\?\\((?:'|(?<cond>[<]))(?<groupName>[a-zA-Z][a-zA-Z0-9]*)(?(cond)[>]|')\\)").matcher(regex).replaceAll("(?(${groupName})");
	}
	
	private static boolean containsUnsupportedRecursion(String regex){
		return Pattern.compile(NO_ESCAPE + "\\(\\?(-\\d+|\\+\\d+|R|0)\\)").matcher(regex).find();
	}
	
	private static boolean containsUnsupportedConditional(String regex){
		return Pattern.compile(NO_ESCAPE + "\\(\\?\\((\\?\\!|DEFINE(?=\\))|R&(?<groupName>[a-zA-Z][a-zA-Z0-9]*)(?=\\))|R\\d+(?=\\)))").matcher(regex).find();
	}
		
	private static String adaptCharacterClasses(String regex){
		Pattern p = Pattern.compile(NO_ESCAPE + "\\[(?<not>\\^)?\\[:(?<class>lower|upper|alpha|digit|alnum|punct|graph|print|blank|cntrl|space):\\]\\]");
		Matcher m = p.matcher(regex);
		
		regex = m.replaceAll( (Matcher matcher) -> {
			
			String r = "\\\\p{" + Character.toUpperCase( matcher.group("class").charAt(0) ) + matcher.group("class").substring(1) + "}"; 
			if( matcher.start("not") != -1 ) r = "[^" + r + "]";
			return r;
			});
			
		p = Pattern.compile(NO_ESCAPE + "\\[(?<not>\\^)?\\[:(?<class>word|xdigit):\\]\\]");
		return p.matcher(regex).replaceAll( (Matcher matcher) -> {
			if(matcher.group().contains("word") ){
			if(matcher.start("not") != -1)
				return "\\\\W";
			else
				return "\\\\w"; }
			else{
				if(matcher.start("not") != -1)
				return "[^\\\\p{XDigit}]";
			else
				return "\\\\p{XDigit}"; 
			}
		} );
	}
	
	private static boolean containsRecursionToGroupThatsNotYetDeclared(String regex){
		if( containsRecursionToNumberedGroupThatsNotYetDeclared(regex) ) return true;
		if( containsRecursionToNamedGroupThatsNotYetDeclared(regex) ) return true;
		return false;
	}
	
	private static boolean containsRecursionToNumberedGroupThatsNotYetDeclared(String regex){
		Pattern p = Pattern.compile(NO_ESCAPE + "\\(\\?(?<groupNumber>\\d+)\\)");
		Matcher m = p.matcher(regex);
		
		Pattern p2 = Pattern.compile( "^(" + NO_ESCAPE + "(?<capturingGroup>\\((?:\\?\\<[a-zA-Z][a-zA-Z0-9]*\\>|(?!\\?)))|[\\s\\S])*$" );
		while(m.find() ){
			String subregex = regex.substring(0, m.start() );
			Matcher m2 = p2.matcher(subregex);
			if(! m2.find() ) throw new RuntimeException("The regex should always match!");
			if( m2.captures("capturingGroup").size() < Integer.parseInt( m.group("groupNumber") ) )
				return true;
			
		}
	
		return false;
	}
	
	private static boolean containsRecursionToNamedGroupThatsNotYetDeclared(String regex){
		Pattern p = Pattern.compile(NO_ESCAPE + "\\(\\?(?<groupName>[a-zA-Z][a-zA-Z0-9]*)\\)");
		Matcher m = p.matcher(regex);
		
		while(m.find() ){
			String subregex = regex.substring(0, m.start() );
			Pattern p2 = Pattern.compile(NO_ESCAPE + "\\(\\?\\<"+ m.group("groupName") + "\\>"); 
			if(! p2.matcher(subregex).find() ) return true;
		}
		
		return false;
	}
	
	
}
