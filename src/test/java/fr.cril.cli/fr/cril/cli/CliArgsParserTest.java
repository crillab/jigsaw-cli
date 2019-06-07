package fr.cril.cli;

/*-
 * #%L
 * Jigsaw-cli
 * %%
 * Copyright (C) 2019 Artois University and CNRS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *   *   CRIL - initial API and implementation
 * #L%
 */

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import fr.cril.cli.annotations.LongName;
import fr.cril.cli.annotations.Param;
import fr.cril.cli.annotations.Params;
import fr.cril.cli.annotations.Args;
import fr.cril.cli.annotations.Required;
import fr.cril.cli.annotations.ShortName;

public class CliArgsParserTest {
	
	@Params("0..1")
	private class TestClassOkOptions {
		
		@ShortName("f")
		@LongName("foo")
		private boolean foo;
		
		@ShortName("b")
		@LongName("bar")
		private boolean bar;
		
		@ShortName("m")
		@LongName("mandatory")
		@Args(1)
		@Required
		private String mandatory;
		
		@ShortName("multi")
		private boolean multicharShortNamed;
		
		@Param
		private String param;
	}
	
	@Test
	public void testOk() throws CliUsageException, CliOptionDefinitionException {
		final TestClassOkOptions obj = new TestClassOkOptions();
		final ClassParser<TestClassOkOptions> optParser = new ClassParser<>(obj);
		final CliArgsParser<TestClassOkOptions> cliParser = new CliArgsParser<>(optParser);
		cliParser.parse(obj, new String[] {"-m", "foobar", "-f"});
		assertEquals("foobar", obj.mandatory);
		assertTrue(obj.foo);
		assertFalse(obj.bar);
	}
	
	@Test
	public void testMergedShortNames() throws CliUsageException, CliOptionDefinitionException {
		final TestClassOkOptions obj = new TestClassOkOptions();
		final ClassParser<TestClassOkOptions> optParser = new ClassParser<>(obj);
		final CliArgsParser<TestClassOkOptions> cliParser = new CliArgsParser<>(optParser);
		cliParser.parse(obj, new String[] {"-m", "foobar", "-fb"});
		assertEquals("foobar", obj.mandatory);
		assertTrue(obj.foo);
		assertTrue(obj.bar);
	}
	
	@Test
	public void testMultiplicityError() {
		final TestClassOkOptions obj = new TestClassOkOptions();
		final ClassParser<TestClassOkOptions> optParser = new ClassParser<>(obj);
		final CliArgsParser<TestClassOkOptions> cliParser = new CliArgsParser<>(optParser);
		assertThrows(CliUsageException.class, () -> cliParser.parse(obj, new String[] {"foobar", "-f", "-m"}));
	}
	
	@Test
	public void testNoMandatory() {
		final TestClassOkOptions obj = new TestClassOkOptions();
		final ClassParser<TestClassOkOptions> optParser = new ClassParser<>(obj);
		final CliArgsParser<TestClassOkOptions> cliParser = new CliArgsParser<>(optParser);
		assertThrows(CliUsageException.class, () -> cliParser.parse(obj, new String[] {"foobar", "-f"}));
	}
	
	@Test
	public void testLongName() throws CliUsageException, CliOptionDefinitionException {
		final TestClassOkOptions obj = new TestClassOkOptions();
		final ClassParser<TestClassOkOptions> optParser = new ClassParser<>(obj);
		final CliArgsParser<TestClassOkOptions> cliParser = new CliArgsParser<>(optParser);
		cliParser.parse(obj, new String[] {"--mandatory", "foobar", "--foo"});
		assertEquals("foobar", obj.mandatory);
		assertTrue(obj.foo);
		assertFalse(obj.bar);
	}
	
	@Test
	public void testEndOfOptions() throws CliUsageException, CliOptionDefinitionException {
		final TestClassOkOptions obj = new TestClassOkOptions();
		final ClassParser<TestClassOkOptions> optParser = new ClassParser<>(obj);
		final CliArgsParser<TestClassOkOptions> cliParser = new CliArgsParser<>(optParser);
		cliParser.parse(obj, new String[] {"-m", "foobar", "--", "-fb"});
		assertEquals("foobar", obj.mandatory);
		assertFalse(obj.foo);
		assertFalse(obj.bar);
	}
	
	@Test
	public void testEmptyOption() {
		final TestClassOkOptions obj = new TestClassOkOptions();
		final ClassParser<TestClassOkOptions> optParser = new ClassParser<>(obj);
		final CliArgsParser<TestClassOkOptions> cliParser = new CliArgsParser<>(optParser);
		assertThrows(CliUsageException.class, () -> cliParser.parse(obj, new String[] {"-m", "foobar", "-"}));
	}
	
	@Test
	public void testHyphenAsArg() throws CliUsageException, CliOptionDefinitionException {
		final TestClassOkOptions obj = new TestClassOkOptions();
		final ClassParser<TestClassOkOptions> optParser = new ClassParser<>(obj);
		final CliArgsParser<TestClassOkOptions> cliParser = new CliArgsParser<>(optParser);
		cliParser.parse(obj, new String[] {"-m", "-", "--"});
		assertEquals("-", obj.mandatory);
	}
	
	@Test
	public void testHyphenHyphenAsArg() throws CliUsageException, CliOptionDefinitionException {
		final TestClassOkOptions obj = new TestClassOkOptions();
		final ClassParser<TestClassOkOptions> optParser = new ClassParser<>(obj);
		final CliArgsParser<TestClassOkOptions> cliParser = new CliArgsParser<>(optParser);
		cliParser.parse(obj, new String[] {"-m", "--", "--"});
		assertEquals("--", obj.mandatory);
	}
	
	@Test
	public void testMergedWithArg() {
		final TestClassOkOptions obj = new TestClassOkOptions();
		final ClassParser<TestClassOkOptions> optParser = new ClassParser<>(obj);
		final CliArgsParser<TestClassOkOptions> cliParser = new CliArgsParser<>(optParser);
		assertThrows(CliUsageException.class, () -> cliParser.parse(obj, new String[] {"-mfb", "foobar"}));
	}
	
	@Test
	public void testMulticharShortOpt() throws CliUsageException, CliOptionDefinitionException {
		final TestClassOkOptions obj = new TestClassOkOptions();
		final ClassParser<TestClassOkOptions> optParser = new ClassParser<>(obj);
		final CliArgsParser<TestClassOkOptions> cliParser = new CliArgsParser<>(optParser);
		cliParser.parse(obj, new String[] {"-m", "foobar", "-multi"});
		assertEquals("foobar", obj.mandatory);
		assertTrue(obj.multicharShortNamed);
	}
	
	@Params("1..2")
	private class TestClassOkClass {
		
		@Param(0)
		private String s = "";
		
		@Param(1)
		private int i = 0;
	}
	
	@Test
	public void test2Params() throws CliUsageException, CliOptionDefinitionException {
		final TestClassOkClass obj = new TestClassOkClass();
		final ClassParser<TestClassOkClass> optParser = new ClassParser<>(obj);
		final CliArgsParser<TestClassOkClass> cliParser = new CliArgsParser<>(optParser);
		cliParser.parse(obj, new String[] {"a", "1"});
		assertEquals("a", obj.s);
		assertEquals(1, obj.i);
	}
	
	@Test
	public void test1Param() throws CliUsageException, CliOptionDefinitionException {
		final TestClassOkClass obj = new TestClassOkClass();
		final ClassParser<TestClassOkClass> optParser = new ClassParser<>(obj);
		final CliArgsParser<TestClassOkClass> cliParser = new CliArgsParser<>(optParser);
		cliParser.parse(obj, new String[] {"a"});
		assertEquals("a", obj.s);
		assertEquals(0, obj.i);
	}
	
	@Test
	public void test0Params() {
		final TestClassOkClass obj = new TestClassOkClass();
		final ClassParser<TestClassOkClass> optParser = new ClassParser<>(obj);
		final CliArgsParser<TestClassOkClass> cliParser = new CliArgsParser<>(optParser);
		assertThrows(CliUsageException.class, () -> cliParser.parse(obj, new String[] {}));
	}
	
	@Test
	public void test3Params() throws CliUsageException, CliOptionDefinitionException {
		final TestClassOkClass obj = new TestClassOkClass();
		final ClassParser<TestClassOkClass> optParser = new ClassParser<>(obj);
		final CliArgsParser<TestClassOkClass> cliParser = new CliArgsParser<>(optParser);
		assertThrows(CliUsageException.class, () -> cliParser.parse(obj, new String[] {"a", "1", "foo"}));
	}
	
	@Params("0..*")
	private class TestClassStar {
		
		@Param(1)
		private String arg1;
		
	}
	
	@Test
	public void testGetParameters() throws CliUsageException, CliOptionDefinitionException {
		final TestClassStar obj = new TestClassStar();
		final ClassParser<TestClassStar> optParser = new ClassParser<>(obj);
		final CliArgsParser<TestClassStar> cliParser = new CliArgsParser<>(optParser);
		cliParser.parse(obj, new String[] {"a", "b", "c"});
		assertEquals(Stream.of("a", "b", "c").collect(Collectors.toList()), cliParser.getParameters());
		assertEquals("b", obj.arg1);
	}

}
