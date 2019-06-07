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

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import fr.cril.cli.annotations.Args;
import fr.cril.cli.annotations.LongName;
import fr.cril.cli.annotations.Param;
import fr.cril.cli.annotations.Params;
import fr.cril.cli.annotations.ShortName;
import fr.cril.cli.utils.Multiplicity;
import fr.cril.cli.utils.OptionMap;

public class ClassParserTest {
	
	@Test
	public void testOk() throws CliOptionDefinitionException, NoSuchFieldException, SecurityException, CliUsageException {
		final OptionParserTestClassOk testCl = new OptionParserTestClassOk();
		final ClassParser<OptionParserTestClassOk> parser = new ClassParser<>(testCl);
		final OptionMap optionMap = parser.parse();
		assertEquals(new Multiplicity(1), optionMap.getParamMultiplicity());
		final Field p = OptionParserTestClassOk.class.getDeclaredField("param");
		assertEquals(1, optionMap.nParams());
		assertEquals(p, optionMap.getParamField(0));
		final Field f = OptionParserTestClassOk.class.getDeclaredField("field");
		assertEquals(f, optionMap.getFieldByShortName("f"));
		assertEquals(f, optionMap.getFieldByLongName("field"));
		assertEquals(1, optionMap.getArgMultiplicity(f));
	}
	
	@Test
	public void testNotOk() throws CliOptionDefinitionException, NoSuchFieldException, SecurityException, CliUsageException {
		final OptionParserTestClassNotOk testCl = new OptionParserTestClassNotOk();
		final ClassParser<OptionParserTestClassNotOk> parser = new ClassParser<>(testCl);
		assertThrows(CliOptionDefinitionException.class, () -> parser.parse());
	}
	
	@Test
	public void testPrintOptionUsage() throws CliOptionDefinitionException {
		final OptionParserTestClassOk testCl = new OptionParserTestClassOk();
		final ClassParser<OptionParserTestClassOk> parser = new ClassParser<>(testCl);
		parser.parse();
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		final PrintWriter pw = new PrintWriter(os);
		parser.printOptionUsage(pw);
		assertEquals(" -f,--field <arg0>\n", new String(os.toByteArray()));
	}
	
	@Test
	public void testShortNamesAmbiguity() {
		final OptionParserTestWithAmbiguity testCl = new OptionParserTestWithAmbiguity();
		final ClassParser<OptionParserTestWithAmbiguity> parser = new ClassParser<>(testCl);
		assertThrows(CliOptionDefinitionException.class, () -> parser.parse());
	}
	
	@Test
	public void testShortNamesAmbiguityNoMerging() throws CliOptionDefinitionException {
		final OptionParserTestWithAmbiguity testCl = new OptionParserTestWithAmbiguity();
		final ClassParser<OptionParserTestWithAmbiguity> parser = new ClassParser<>(testCl);
		parser.allowShortNamesMerging(false);
		parser.parse();
	}
	
	@Retention(RUNTIME)
	@Target(FIELD)
	private @interface NoOpForField{}
	
	@Retention(RUNTIME)
	@Target(TYPE)
	private @interface NoOpForClass{}
	
	@Params("1..1")
	@NoOpForClass
	private class OptionParserTestClassOk {
		
		@ShortName("f")
		@LongName("field")
		@Args(1)
		@NoOpForField
		private String field;
		
		@Param
		private String param;
	}
	
	@Params("1..*")
	@NoOpForClass
	private class OptionParserTestClassNotOk {
		
		@ShortName("f")
		@LongName("field")
		@Args(-1)
		@NoOpForField
		private String field;
	}
	
	private class OptionParserTestWithAmbiguity {
		
		@ShortName("a")
		private String a;
		
		@ShortName("b")
		private String b;
		
		@ShortName("ab")
		private String ab;
	}
	
}
