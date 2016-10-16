package com.florianingerl.util.regex.copytestsfromboost;

import com.florianingerl.util.regex.*;
import java.nio.file.Files;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import java.io.PrintWriter;
import java.io.FileInputStream;
/**
 * Hello world!
 *
 */
public class Main
{
	static Pattern pTestFunctions = Pattern.compile("\\bvoid\\s+(?=[a-zA-Z])(?<functionName>\\w*?test\\w*)\\s*\\(\\s*\\)\\s*(?<functionBody>\\{(//.*(\r)?\n|/\\*[\\s\\S]*?\\*/|\"(\\\\.|[^\"\\\\]+)*\"|[^\"{}/]+|(?functionBody))*+\\})");

	static Pattern p = Pattern.compile("TEST_REGEX_SEARCH\\s*\\(\\s*(?<regex>(?<javaString>\"(\\\\.|[^\"])*\"))\\s*,\\s*(?<options>((perl|icase|nosubs)\\s*(\\||(?=,)))+),\\s*(?<input>(?javaString))\\s*,\\s*(?<otheroptions>((match_default|match_not_dot_newline|match_single_line|match_no_subs)\\s*(\\||(?=,)))+),\\s*make_array\\s*\\((?<array>[^)]+)\\)\\s*\\)\\s*;");
	
    public static void main( String[] args )
    {
		try{
			
			String cpp_file_content = IOUtils.toString( new FileInputStream("C:/Software/boost.regex/test/regress/basic_tests.cpp") ); 
			System.out.println(cpp_file_content);
			
			StringBuilder sbMain = new StringBuilder("\tpublic static void main(String [] args){\n");
			
			StringBuilder sbFunctions = new StringBuilder();
			
			Matcher mTestFunctions = pTestFunctions.matcher(cpp_file_content);
		
			while(mTestFunctions.find() ){
				System.out.println(mTestFunctions.group() );
		
				sbMain.append("\t\t"+ mTestFunctions.group("functionName") + "();\n");
				
				sbFunctions.append("\tprivate static void " + mTestFunctions.group("functionName") + "(){\n");
				String functionBody = mTestFunctions.group("functionBody");
				System.out.println(mTestFunctions.group("functionBody") );
				
				Matcher m = p.matcher(functionBody);
				
				while(m.find() ){
					sbFunctions.append("\t\tcheck(").append( m.group("regex") ).append(",0");
					if( m.group("options").contains("icase") )
						sbFunctions.append("|Pattern.CASE_INSENSITIVE");
					if( !m.group("otheroptions").contains("match_not_dot_newline") )
						sbFunctions.append("|Pattern.DOTALL");
					sbFunctions.append(",").append(m.group("input") ).append(", new int[]{").append( m.group("array") ).append("});\n");
				}
				
				sbFunctions.append("\n\t\treport(\"" + mTestFunctions.group("functionName") + "\");\n");
				sbFunctions.append("\t}\n\n");
			}
			
			sbMain.append("\t\tif(failure){throw new RuntimeException(\"RegExTest failed, 1st failure: \" + firstFailure); }\n");
			sbMain.append("\t\telse{System.err.println(\"OKAY: All tests passed.\");}\n\t}\n");
			
			StringBuilder sbClass = new StringBuilder("package com.florianingerl.util.regex;\n");
			sbClass.append("public class BoostRegExTest {\n\n");
			sbClass.append("\tprivate static boolean failure = false;\n");
			sbClass.append("\tprivate static int failCount = 0;\n");
			sbClass.append("\tprivate static String firstFailure = null;\n\n");
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
	
		
	
	
}
