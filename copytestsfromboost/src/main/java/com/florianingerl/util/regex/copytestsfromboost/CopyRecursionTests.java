package com.florianingerl.util.regex.copytestsfromboost;

import com.florianingerl.util.regex.*;
import java.nio.file.Files;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import java.io.PrintWriter;
/**
 * Hello world!
 *
 */
public class CopyRecursionTests 
{
	

    public static void main( String[] args )
    {
	
	try{
        String recursion_cpp = IOUtils.toString( CopyRecursionTests.class.getClassLoader().getResourceAsStream("recursion_test.cpp") );
		
		System.out.println(recursion_cpp);
		
		Pattern p = Pattern.compile("TEST_REGEX_SEARCH\\s*\\(\\s*(?<regex>(?<javaString>\"(\\\\.|[^\"])*\"))\\s*,\\s*(?<options>((perl|icase|nosubs)\\s*(\\||(?=,)))+),\\s*(?<input>(?javaString))\\s*,\\s*(?<otheroptions>((match_default|match_not_dot_newline|match_single_line|match_no_subs)\\s*(\\||(?=,)))+),\\s*make_array\\s*\\((?<array>[^)]+)\\)\\s*\\)\\s*;");
		Matcher m = p.matcher(recursion_cpp);
		
		StringBuffer sb = new StringBuffer();
		
		int i = 0;
		while(m.find() ){
			++i;
			System.out.println(m.group(0));
			sb.append("check(").append( m.group("regex") ).append(",0");
			if( m.group("options").contains("icase") )
				sb.append("|Pattern.CASE_INSENSITIVE");
			if( !m.group("otheroptions").contains("match_not_dot_newline") )
				sb.append("|Pattern.DOTALL");
			sb.append(",").append(m.group("input") ).append(", new int[]{").append( m.group("array") ).append("});\n");
		}
		
		String output = sb.toString();
		
		try(  PrintWriter out = new PrintWriter( "output.txt" )  ){
			out.println( output );
		}
		
		System.out.println(""+ i +" tests were copied!");
		System.out.println("File ouput.txt was successfully written!");
		
		}catch(Exception e){e.printStackTrace(); }
    }
}
