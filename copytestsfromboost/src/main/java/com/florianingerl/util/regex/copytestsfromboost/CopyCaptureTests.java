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
public class CopyCaptureTests 
{
	

    public static void main( String[] args )
    {
	
	try{
        String captures_cpp = IOUtils.toString( CopyCaptureTests.class.getClassLoader().getResourceAsStream("captures_test.cpp") );
		
		Pattern p = Pattern.compile("pchar\\s+(e\\d+)(\\s*\\[\\s*\\d+\\s*\\]){2}\\s*=\\s*(?<expected>[^;]+);\\s*test_captures\\s*\\(\\s*(?<regex>(?<javaString>\"(\\\\.|[^\"])*\"))\\s*,\\s*(?<input>(?javaString))\\s*,\\s*\\1\\s*\\)\\s*;");
		Matcher m = p.matcher(captures_cpp);
		
		 StringBuffer sb = new StringBuffer();
		 int i = 0;
		 while (m.find()) {
			++i;
			m.appendReplacement(sb, "String[][] $1 = " + m.group("expected").replaceAll(",(?=\\s*\\})", "") + ";\ncheck(${regex},${input},$1);");
		 }	
		 m.appendTail(sb);
		
		String output = sb.toString();
		
		try(  PrintWriter out = new PrintWriter( "output.txt" )  ){
			out.println( output );
		}
		
		System.out.println(""+ i +" tests were copied!");
		System.out.println("File ouput.txt was successfully written!");
		
		}catch(Exception e){e.printStackTrace(); }
    }
}
