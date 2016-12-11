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
	private static final String NO_ESCAPE = "(?<!(?<!\\\\{2})\\\\{2}(\\\\{4}){0,10})";

	static Pattern pTestFunctions = Pattern.compile("\\bvoid\\s++(?=[a-zA-Z])(?<functionName>(?>\\w*?test\\w*))\\s*+\\(\\s*+\\)\\s*+(?<functionBody>\\{(//.*+(\r)?\n|/\\*[\\s\\S]*?\\*/|\"(?:\\\\.|[^\"\\\\]++)*+\"|[^\"{}/]++|(?functionBody))*+\\})");

	static Pattern pTestRegexSearch = Pattern.compile("/\\*[\\s\\S]*?\\*/|//.*(\r)?\n|TEST_REGEX_SEARCH\\s*\\(\\s*(?<regex>(?<javaString>\"(\\\\.|[^\"])*\"))\\s*,\\s*(?<options>((perl|icase|nosubs|no_mod_m|no_mod_s|mod_s|mod_x)\\s*(\\||(?=,)))+),\\s*(?<input>(?javaString))\\s*,\\s*(?<otheroptions>((match_default|match_not_dot_newline|match_single_line|match_no_subs)\\s*(\\||(?=,)))+),\\s*make_array\\s*\\((?<array>[^)]+)\\)\\s*\\)\\s*;");
	static Pattern pInvalidRegex = Pattern.compile("/\\*[\\s\\S]*?\\*/|//.*(\r)?\n|TEST_INVALID_REGEX\\s*\\(\\s*(?<regex>(?<javaString>\"(\\\\.|[^\"])*\"))\\s*,\\s*(?<options>((perl|icase|nosubs|no_mod_m|no_mod_s|mod_s|mod_x)\\s*(\\||(?=\\))))+)\\)\\s*;");
	
	private static final File boostDir = new File("C:/Software/boost.regex");
	
	private static final String [] testFiles = new String[]{ "test/regress/basic_tests.cpp", "test/regress/test_tricky_cases.cpp", "test/regress/test_alt.cpp", "test/regress/test_simple_repeats.cpp", "test/regress/test_perl_ex.cpp", "test/regress/test_non_greedy_repeats.cpp", "test/regress/test_grep.cpp", "test/regress/test_backrefs.cpp" };
	
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
				if( mTestFunctions.group("functionName").equals("test_verbs") ) continue;
				System.out.println(mTestFunctions.group() );
		
				sbMain.append("\t\t"+ mTestFunctions.group("functionName") + "();\n");
				
				sbFunctions.append("\tprivate static void " + mTestFunctions.group("functionName") + "(){\n");
				String functionBody = mTestFunctions.group("functionBody");
				System.out.println(mTestFunctions.group("functionBody") );
				
				Matcher m = pTestRegexSearch.matcher(functionBody);
				
				while(m.find() ){
					if(! m.group().startsWith("TEST") ) continue;
					if(! isValidArray( m.group("array") ) ) continue;
					String regex = Main.adaptRegex(m.group("regex") );
					if(regex == null) continue;
					sbFunctions.append("\t\tcheck(").append( regex ).append(",");
					appendOptions( m.group("options"), m.group("otheroptions"), sbFunctions);
					String array = m.group("array");
					if( containsRecursion(regex) ) {
						array = adaptWrongRecursionGroups(array);
					}
					sbFunctions.append(",").append(m.group("input") ).append(", new int[]{").append( array ).append("});\n");
				}
				/*
				Matcher m2 = pInvalidRegex.matcher(functionBody);
				
				while(m2.find() ){
					if(! m2.group().startsWith("TEST") ) continue;
					String regex = Main.adaptRegex(m2.group("regex") );
					if(regex == null) continue;
					sbFunctions.append("\t\tcheckExpectedFail(").append( regex ).append(",");
					appendOptions(m2.group("options"), null, sbFunctions );
					sbFunctions.append(");\n");
				}*/
				
				sbFunctions.append("\n\t\treport(\"" + mTestFunctions.group("functionName") + "\");\n");
				sbFunctions.append("\t}\n\n");
			}
			
			}
			
			sbMain.append("\t\tif(failure){throw new RuntimeException(\"BoostRegExTest failed, 1st failure: \" + firstFailure); }\n");
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
			appendCheckExpectedFailFunction(sbClass);
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
	
	private static void appendOptions(String options, String otherOptions, StringBuilder sb){
		sb.append("0");
		if(  options.contains("icase") )
			sb.append("|Pattern.CASE_INSENSITIVE");
		if( !options.contains("no_mod_m") )
			sb.append("|Pattern.MULTILINE");
		if( !options.contains("no_mod_s") && (otherOptions == null || !otherOptions.contains("match_not_dot_newline") ) )
			sb.append("|Pattern.DOTALL");
		if(options.contains("mod_x") )
			sb.append("|Pattern.COMMENTS");
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
	
	private static void appendCheckExpectedFailFunction(StringBuilder sb){
		sb.append("	private static void checkExpectedFail(String p, int flags) {\n");
		sb.append("		try {\n");
		sb.append("			Pattern.compile(p, flags);\n");
		sb.append("		} catch (PatternSyntaxException pse) {\n");
		sb.append("			return;\n");
		sb.append("		}\n");
		sb.append("		failCount++;\n");
		sb.append("	}\n");
	}

	
	static String adaptRegex(String regex){
		if(isSpecialRegex(regex) ) return null;
		if( regex.contains("(?i:(?1)") ) return null;
		if( regex.contains("(?:(?<A>a+)|(?<A>b+)") ) return null;
		if(containsRelativeBackReference(regex) ) return null;
		if(containsUnsupportedRecursion(regex) ) return null;
		if(containsUnsupportedConditional(regex) ) return null;
		if(containsBranchReset(regex) ) return null;
		regex = adaptWrongBackReferences(regex);
		regex = adaptWrongNamedGroups(regex);
		regex = adaptWrongCommentaries(regex);
		regex = adaptWordBoundaries(regex);
		regex = escapeGreaterAndSmallerSigns(regex);
		regex = adaptWrongRecursion(regex);
		regex = adaptWrongConditionalBasedOnValidGroupCapture(regex);
		regex = adaptCharacterClasses(regex);
		regex = adaptWrongUpperAndLowerCase(regex);
		if(containsRecursionToGroupThatsNotYetDeclared(regex) ) return null;
		return regex;
	}
	
	private static final Pattern pRelativeBackReference = Pattern.compile(NO_ESCAPE + "\\\\{2}g(-\\d+|\\{-\\d+\\})");
	private static boolean containsRelativeBackReference(String regex){
		return pRelativeBackReference.matcher(regex).find();
	}
	
	private static final Pattern pWrongBackReference = Pattern.compile(NO_ESCAPE + "\\\\{2}g((?<groupNumber>\\d+)|\\{(?groupNumber)\\})");
	private static final Pattern pWrongBackReference2 = Pattern.compile(NO_ESCAPE + "\\\\{2}g\\{(?<groupName>[a-zA-Z][a-zA-Z0-9]*)\\}");
	private static String adaptWrongBackReferences(String regex){
		Matcher m = pWrongBackReference.matcher(regex);
		regex = m.replaceAll( "\\\\\\\\${groupNumber}" );
		m = pWrongBackReference2.matcher(regex);
		return m.replaceAll(  "\\\\\\\\k<${groupName}>" );
	}
	
	private static final Pattern pWrongNamedGroups = Pattern.compile(NO_ESCAPE + "\\(\\?'(?<groupName>[a-zA-Z][a-zA-Z0-9]*)'");
	private static String adaptWrongNamedGroups(String regex){ 
		Matcher m = pWrongNamedGroups.matcher(regex);
		return m.replaceAll("(?<${groupName}>");
	}
	
	private static final Pattern pWrongCommentaries = Pattern.compile(NO_ESCAPE + "\\(\\?#(?<comment>[\\s\\S]*?)\\)");
	private static String adaptWrongCommentaries(String regex){
		Matcher m = pWrongCommentaries.matcher(regex);
		return m.replaceAll( (Matcher matcher) -> { return "(?x:#" + matcher.group("comment").replaceAll("\\\\n", "") + "\\n)"; } );
	}
	
	private static final Pattern pWrongWordBoundaries = Pattern.compile(NO_ESCAPE + "\\\\{2}[><]"); 
	private static String adaptWordBoundaries(String regex){
		return pWrongWordBoundaries.matcher(regex).replaceAll( (Matcher matcher) -> { return "\\\\b"; } );
	}
	
	private static Pattern pGreaterAndSmallerSigns = null;
	static {
		String namedGroup = "\\(\\?\\<(?<groupName>[a-zA-Z][a-zA-Z0-9]*)\\>";
		String conditionBasedOnValidGroupCapture = "\\(\\?\\(\\<(?groupName)\\>";
		String backReference = "\\\\{2}k\\<(?groupName)\\>";
		String characterClass = "\\[(\\\\{2}.|[^\\]])*\\]";
		String lookbehind = "\\(\\?\\<[!=]";
		String independentGroup = "\\Q(?>\\E";
		String recursion = "\\Q(?P>\\E(?groupName)\\)";
		pGreaterAndSmallerSigns = Pattern.compile(NO_ESCAPE + "(" + namedGroup + "|" + conditionBasedOnValidGroupCapture + "|" + backReference + "|" + characterClass + "|" + lookbehind + "|" + independentGroup + "|" + recursion + "|[><])");
	}
	
	private static String escapeGreaterAndSmallerSigns(String regex){
		
		Matcher m = pGreaterAndSmallerSigns.matcher(regex);
		
		return m.replaceAll( (Matcher matcher) -> { 
			if(matcher.group().startsWith("<") || matcher.group().startsWith(">") ){
				return "\\\\" + matcher.group();
			}
			return matcher.group();
			} );
	}
	
	private static final Pattern pRecursion = Pattern.compile(NO_ESCAPE + "\\(\\?([a-zA-Z][a-zA-Z0-9]*|\\d+)\\)");
	private static boolean containsRecursion(String regex){
		return pRecursion.matcher(regex).find();
	}
	
	private static final Pattern pWrongRecursionGroups = Pattern.compile("(\\d+\\s*,\\s*\\d+\\s*,)([\\s\\S]*?)(-\\s*2)");
	private static String adaptWrongRecursionGroups(String array){
		return pWrongRecursionGroups.matcher(array).replaceAll("$1$3");
	}
	
	private static final Pattern pWrongRecursion = Pattern.compile(NO_ESCAPE + "\\(\\?(&|P\\>)(?<groupName>[a-zA-Z][a-zA-Z0-9]*)\\)");
	private static String adaptWrongRecursion(String regex){
		return pWrongRecursion.matcher(regex).replaceAll("(?${groupName})");
	}
	
	private static final Pattern pWrongConditionalBasedOnValidGroupCapture = Pattern.compile(NO_ESCAPE + "\\(\\?\\((?:'|(?<cond>[<]))(?<groupName>[a-zA-Z][a-zA-Z0-9]*)(?(cond)[>]|')\\)");
	private static String adaptWrongConditionalBasedOnValidGroupCapture(String regex){
		return pWrongConditionalBasedOnValidGroupCapture.matcher(regex).replaceAll("(?(${groupName})");
	}
	
	private static final Pattern pUnsupportedRecursion = Pattern.compile(NO_ESCAPE + "\\(\\?(-\\d+|\\+\\d+|R|0)\\)");
	private static boolean containsUnsupportedRecursion(String regex){
		return pUnsupportedRecursion.matcher(regex).find();
	}
	
	private static final Pattern pUnsupportedConditional = Pattern.compile(NO_ESCAPE + "\\Q(?(\\E(\\?\\!|DEFINE(?=\\))|\\Q?<=\\E|\\Q?<!\\E|R&(?<groupName>[a-zA-Z][a-zA-Z0-9]*)(?=\\))|R\\d+(?=\\)))");
	private static boolean containsUnsupportedConditional(String regex){
		return pUnsupportedConditional.matcher(regex).find();
	}
	
	private static final Pattern pCharacterClasses = Pattern.compile(NO_ESCAPE + "\\[(?<not>\\^)?\\[:(?<class>lower|upper|alpha|digit|alnum|punct|graph|print|blank|cntrl|space):\\]\\]");
	private static final Pattern pWordAndXDigitCharacterClasses = Pattern.compile(NO_ESCAPE + "\\[(?<not>\\^)?\\[:(?<class>word|xdigit):\\]\\]");
	private static String adaptCharacterClasses(String regex){
		Matcher m = pCharacterClasses .matcher(regex);
		
		regex = m.replaceAll( (Matcher matcher) -> {
			
			String r = "\\\\p{" + Character.toUpperCase( matcher.group("class").charAt(0) ) + matcher.group("class").substring(1) + "}"; 
			if( matcher.start("not") != -1 ) r = "[^" + r + "]";
			return r;
			});
			
		return pWordAndXDigitCharacterClasses.matcher(regex).replaceAll( (Matcher matcher) -> {
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
	
	private static final Pattern pRecursionToNumberedGroup = Pattern.compile(NO_ESCAPE + "(\\(\\?(?<groupNumber>\\d+)\\)|\\Q(?(\\E(?groupNumber)\\))");
	private static final Pattern pGroupCounter = Pattern.compile( "^(" + NO_ESCAPE + "(?<capturingGroup>\\((?:\\?\\<[a-zA-Z][a-zA-Z0-9]*\\>|(?!\\?)))|[\\s\\S])*$" );
	private static boolean containsRecursionToNumberedGroupThatsNotYetDeclared(String regex){
		Matcher m = pRecursionToNumberedGroup.matcher(regex);

		while(m.find() ){
			if(!isGroupAlreadyDeclared(Integer.parseInt( m.group("groupNumber") ), regex.substring(0, m.start() ) ) )
				return true;
		}
	
		return false;
	}
	
	
	private static boolean isGroupAlreadyDeclared(int group, String subregex){
		Matcher m = pGroupCounter.matcher(subregex);
		if(! m.find() ) throw new RuntimeException("The regex should always match!");
		return m.captures("capturingGroup").size() >= group ;
	}
	
	private static final Pattern pRecursionToNamedGroup = Pattern.compile(NO_ESCAPE + "(\\(\\?(?<groupName>[a-zA-Z][a-zA-Z0-9]*)\\)|\\Q(?(\\E(?groupName)\\))");
	private static boolean containsRecursionToNamedGroupThatsNotYetDeclared(String regex){
		Matcher m = pRecursionToNamedGroup.matcher(regex);
		
		while(m.find() ){
			if(!isNamedGroupAlreadyDeclared(m.group("groupName"), regex.substring(0, m.start() ) ) ) 
				return true;
		}
		
		return false;
	}
	
	private static final boolean isNamedGroupAlreadyDeclared(String groupName, String subregex){
		Pattern p = Pattern.compile(NO_ESCAPE + "\\(\\?\\<"+ groupName + "\\>"); 
		return p.matcher(subregex).find();
	}
	
	private static final String [] SPECIAL_PATTERNS = new String[]{ "\"[a-Z]+\"", "\"[[:lower:]]+\"", "\"[[:upper:]]+\"", "\"\\\\l+\"","\"\\\\u+\"", "\"a{ 2 , 4 }\"", "\"a{ 2 , }\"", "\"a{ 2 }\"", "\"a{12b\"" };
	
	private static boolean isSpecialRegex(String regex){
		for(int i=0; i < SPECIAL_PATTERNS.length; ++i){
			if( SPECIAL_PATTERNS[i].equals(regex) ) return true;
		}
		return false;
	}
	
	private static final Pattern pWrongUpperAndLowerCase = Pattern.compile(NO_ESCAPE + "\\\\{2}(l|u)");
	private static String adaptWrongUpperAndLowerCase(String regex){
		return pWrongUpperAndLowerCase.matcher(regex).replaceAll( (Matcher matcher) -> {
			if(matcher.group().contains("l") ) return "\\\\p{Lower}";
			else return "\\\\p{Upper}";
		} );
	}
	
	private static final Pattern pBranchReset = Pattern.compile(NO_ESCAPE + "\\Q(?|\\E");
	private static boolean containsBranchReset(String regex){
		return pBranchReset.matcher(regex).find();
	}
	
	private static final Pattern pValidArray = Pattern.compile("(^|(?<=-\\s{0,10}2\\s{0,10},))\\s*(?<first>\\d+)\\s*,\\s*\\k<first>\\s*,[\\s\\S]*?-\\s*2\\s*,\\s*\\k<first>");
	static boolean isValidArray(String array){
		return !pValidArray.matcher(array).find();
	}
	
	
}
