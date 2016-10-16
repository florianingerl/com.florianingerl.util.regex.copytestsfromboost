package com.florianingerl.util.regex.copytestsfromboost;

import com.florianingerl.util.regex.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.io.IOUtils;
import java.io.PrintWriter;
import java.io.FileInputStream;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class MainTest 
{
	
    @Test
	public void functionRegexTest()
	{
		Matcher m = Main.pTestFunctions.matcher("void testSomething(){\n // This } is commented out \n //This is } commented out too \r\n /*This } is commented out too */ These \" } \\\" } \" were inside a string. Hello my name is Florian {} some nested { open brackets } }");
	
		assertTrue(m.find() );
		
		assertEquals("testSomething", m.group("functionName") );
		assertEquals("{\n // This } is commented out \n //This is } commented out too \r\n /*This } is commented out too */ These \" } \\\" } \" were inside a string. Hello my name is Florian {} some nested { open brackets } }", m.group("functionBody") );
		
	}
	
	@Test
	public void TEST_REGEX_SEARCH_RegexTest()
	{
		Matcher m = Main.p.matcher("TEST_REGEX_SEARCH(\"(a)\", perl, \"zzzaazz\", match_default, make_array(3, 4, 3, 4, -2, 4, 5, 4, 5, -2, -2));");
		
		assertTrue(m.find() );
		
		assertEquals("\"(a)\"", m.group("regex") );
		assertEquals("perl", m.group("options") );
		assertEquals("\"zzzaazz\"", m.group("input") );
		assertEquals("match_default", m.group("otheroptions") );
		assertEquals("3, 4, 3, 4, -2, 4, 5, 4, 5, -2, -2", m.group("array") );
		
		
		
	}	
	
	@Test
	public void test3()
	{
		String cpp_file_content = null;
		try {
			cpp_file_content = IOUtils.toString( getClass().getClassLoader().getResourceAsStream("basic_tests.cpp") ); 
		} catch(Exception e) { e.printStackTrace(); assertTrue(false); }
	
		Matcher m = Main.pTestFunctions.matcher(cpp_file_content);
		int i=0;
		while(m.find() )
		{
			++i;
		}
		
		System.out.println("com.florianingerl.util.regex.Matcher found " + i + " matches!");
		assertTrue(i == 4 );
		
	}
    
}
