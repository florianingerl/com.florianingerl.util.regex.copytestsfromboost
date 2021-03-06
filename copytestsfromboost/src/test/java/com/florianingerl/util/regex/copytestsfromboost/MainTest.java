package com.florianingerl.util.regex.copytestsfromboost;

import com.florianingerl.util.regex.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

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
		Matcher m = Main.pTestRegexSearch.matcher("TEST_REGEX_SEARCH(\"(a)\", perl, \"zzzaazz\", match_default, make_array(3, 4, 3, 4, -2, 4, 5, 4, 5, -2, -2));");
		
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
		
		assertTrue(i == 4 );
		
	}
	
	@Test
	public void test4()
	{
		java.util.regex.Pattern p = java.util.regex.Pattern.compile("^(([^b])|([^b]{2})){2}b$");
		java.util.regex.Matcher m = p.matcher("xyzb");
		
		assertTrue(m.find() );
		
		assertEquals("yz", m.group(1));
		assertEquals("x", m.group(2) );
		assertEquals("yz", m.group(3) );
	
	}
    
	@Test
	public void test5()
	{
		Pattern p = Pattern.compile("^(([^b])|([^b]{2})){2}b$");
		Matcher m = p.matcher("xyzb");
		
		assertTrue(m.find() );
		
		assertEquals("yz", m.group(1));
		assertEquals("x", m.group(2) );
		assertEquals("yz", m.group(3) );
	
	}
	
	@Test
	public void test6(){
		String regex = "([\"'])a\\\\g1";
		assertEquals("([\"'])a\\\\1", Main.adaptRegex(regex) );
		regex = "([\"'])a\\\\g{1}";
		assertEquals("([\"'])a\\\\1", Main.adaptRegex(regex) );
		regex = "(?<h83group>[\"'])a\\\\g{h83group}";
		assertEquals("(?<h83group>[\"'])a\\\\k<h83group>", Main.adaptRegex(regex) );
	}
	
	@Test
	public void test7(){
		String regex = "([\"'])a\\\\g-1";
		assertTrue( Main.adaptRegex(regex) == null );
		regex = "([\"'])a\\\\g{-1}";
		assertTrue( Main.adaptRegex(regex) == null );
		regex = "([\"'])a\\\\g1";
		assertTrue( Main.adaptRegex(regex) != null );
	}
	
	@Test
	public void test8(){
		String regex = "(?'h83group'a|b)";
		assertEquals("(?<h83group>a|b)", Main.adaptRegex(regex) );
	}
	
	@Test
	public void test9(){
		String regex = "(?# this is the first alternative)a|(?# this is the second alternative)b";
		assertEquals("(?x:# this is the first alternative\\n)a|(?x:# this is the second alternative\\n)b", Main.adaptRegex(regex) );
		regex = "(?# this is a \\n comment that spans \\n multiple lines)";
		assertEquals("(?x:# this is a  comment that spans  multiple lines\\n)", Main.adaptRegex(regex) );
	}
	
	@Test
	public void test10(){
		String regex = "\\\\<Beginning End\\\\>";
		assertEquals("\\\\bBeginning End\\\\b", Main.adaptRegex(regex) );
	}
	
	@Test
	public void test11(){
		String regex = "[><a-z]";
		assertEquals(regex, Main.adaptRegex(regex) );
		regex = "(?>an independent group)";
		assertEquals(regex, Main.adaptRegex(regex) );
		regex = "(?<!negative lookbehind)(?<=positive lookbehind)";
		assertEquals(regex, Main.adaptRegex(regex) );
		regex = "(?<namedGroup>This is a named group)";
		assertEquals(regex, Main.adaptRegex(regex) ); 
		regex = "\\\\k<namedGroup>";
		assertEquals(regex, Main.adaptRegex(regex) ); 
		regex = "(?<namedGroup>is declared here)(?(<namedGroup>)condition based on valid group capture)";
		assertEquals("(?<namedGroup>is declared here)(?(namedGroup)condition based on valid group capture)", Main.adaptRegex(regex) );
		regex = "some free < and > signs";
		assertEquals("some free \\\\< and \\\\> signs", Main.adaptRegex(regex) );
	}
	
	@Test
	public void test12(){
		String regex = "(?<someName>hello)(?&someName) and escaped \\\\(?&someName) and not escaped \\\\\\\\(?&someName)";
		assertEquals("(?<someName>hello)(?someName) and escaped \\\\(?&someName) and not escaped \\\\\\\\(?someName)", Main.adaptRegex(regex) );
		regex = "(?'abc'a|b)(?<doe>d|e)(?P>abc){2}";
		assertEquals("(?<abc>a|b)(?<doe>d|e)(?abc){2}", Main.adaptRegex(regex) );
	}
	
	@Test
	public void test13(){
		String regex = "(?-24)";
		assertTrue( Main.adaptRegex(regex) == null );
		regex = "(?+345)";
		assertTrue( Main.adaptRegex(regex) == null );
		regex = "(?R)";
		assertTrue( Main.adaptRegex(regex) == null );
		regex = "(?0)";
		assertTrue( Main.adaptRegex(regex) == null );
	}
	
	@Test
	public void test14(){
		String regex = "(?<groupname>is declared here)(?('groupname')condA|condB)";
		assertEquals("(?<groupname>is declared here)(?(groupname)condA|condB)", Main.adaptRegex(regex) );
		regex = "(?<groupname>is declared here)(?(<groupname>)condA|condB)";
		assertEquals("(?<groupname>is declared here)(?(groupname)condA|condB)", Main.adaptRegex(regex) );
	}
	
	@Test
	public void test15(){
		String regex = "(?(?!assert)yes|no)";
		assertTrue( Main.adaptRegex(regex) == null );
		regex = "(?(DEFINE)(?<groupName>[a-zA-Z][a-zA-Z0-9]*))";
		assertTrue( Main.adaptRegex(regex) == null );
		regex = "(?(R&groupName)yes|no)";
		assertTrue( Main.adaptRegex(regex) == null );
		regex = "(?(R345)yes|no)";
		assertTrue( Main.adaptRegex(regex) == null );
		regex = "(?(?<=hello)yes|no)";
		assertTrue( Main.adaptRegex(regex) == null );
		regex = "(?(?<!hello)yes|no)";
		assertTrue( Main.adaptRegex(regex) == null );
		
	}
	
	
	@Test
	public void test16(){
		String regex = "[[:xdigit:]]";
		assertEquals("\\\\p{XDigit}", Main.adaptRegex(regex) );
		regex = "[[:word:]]";
		assertEquals("\\\\w", Main.adaptRegex(regex) );
		regex = "[^[:word:]]";
		assertEquals("\\\\W", Main.adaptRegex(regex) );
		regex = "[^[:alnum:]]";
		assertEquals("[^\\\\p{Alnum}]", Main.adaptRegex(regex) );
		regex = "[[:lower:]]";
		assertEquals("\\\\p{Lower}", Main.adaptRegex(regex) );
		regex = "[[:blank:]] [[:print:]]";
		assertEquals("\\\\p{Blank} \\\\p{Print}", Main.adaptRegex(regex));
	}
	
	@Test
	public void test17(){
		String regex = "(?1)\\s*\\+\\s*(\"(\\\\.|[^\"])*\")";
		
		assertTrue( Main.adaptRegex(regex) == null );
		
		regex = "(?javaString)\\s*\\+\\s*(?<javaString>\"(\\\\.|[^\"])*\")";
		
		assertTrue( Main.adaptRegex(regex) == null );
	}
	
	@Test
	public void testEmptyAlternative(){
		Pattern.compile("|c");
	}
	
	@Test
	public void test18(){
		assertTrue( Main.adaptRegex("\"[a-Z]+\"") == null );
	}
	
	@Test
	public void test19(){
		assertTrue( Main.adaptRegex("(a)(?|x(y)z|(p(q)r)|(t)u(v))(z)") == null );
	}
	
	@Test
	public void test20(){
		assertTrue( Main.isValidArray("4, 5, -2, 5, 6, -2, -2") );
		assertFalse( Main.isValidArray("0, 0, 0, 0, -2, 1, 1, 1, 1, -2, 1, 2, 1, 2, -2, 2, 2, 2, 2, -2, -2") );
	}
	
	@Test
	public void test21(){
		String regex = "\\\\l++";
		assertEquals( "\\\\p{Lower}++", Main.adaptRegex(regex) );
		regex = "\\\\u++";
		assertEquals( "\\\\p{Upper}++", Main.adaptRegex(regex) );
	}
	
	@Test
	public void test22(){
		assertTrue( Main.adaptRegex("\"\\\\l+\"") == null );
		assertTrue( Main.adaptRegex("\"\\\\u+\"") == null );
	}
	
	@Test
	public void test23(){
		String regex = "^(?(2)a|(1)(2))+$";
		assertTrue( Main.adaptRegex(regex) == null );
		regex = "^(?(notDeclared)a|(1)(?<notDeclared>2))+$";
		assertTrue( Main.adaptRegex(regex) == null );
	}
}
