package fr.cril.cli.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.cril.cli.CliOptionDefinitionException;

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

public class OptionUsagePrinterTest {

	private OptionMap options;

	private Field field;

	@SuppressWarnings("unused")
	private Object obj2 = new Object();

	private Field field2;

	@SuppressWarnings("unused")
	private Object obj3 = new Object();

	private Field field3;
	
	@BeforeEach
	public void setUp() throws NoSuchFieldException, SecurityException {
		this.options = new OptionMap();
		this.options.allowShortNamesMerging(true);
		this.field = OptionMapTest.class.getDeclaredField("options");
		this.field2 = OptionMapTest.class.getDeclaredField("obj2");
		this.field3 = OptionMapTest.class.getDeclaredField("obj3");
	}

	@Test
	public void testPrintOptionFull() throws CliOptionDefinitionException {
		this.options.setShortName(this.field, "a");
		this.options.setLongName(this.field, "a");
		this.options.setDescription(this.field, "descr1");
		this.options.setShortName(this.field2, "b");
		this.options.setLongName(this.field2, "ab");
		this.options.setDescription(this.field2, "descr2");
		this.options.setShortName(this.field3, "c");
		this.options.setLongName(this.field3, "abc");
		this.options.setDescription(this.field3, "descr3");
		assertEquals(" -a,--a     descr1\n -b,--ab    descr2\n -c,--abc   descr3\n", optUsage());
	}
	
	private String optUsage() {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		final PrintWriter pw = new PrintWriter(os);
		this.options.printOptionUsage(pw);
		return new String(os.toByteArray());
	}

	@Test
	public void testPrintOptionNoShort() throws CliOptionDefinitionException {
		this.options.setLongName(this.field, "a");
		this.options.setDescription(this.field, "descr1");
		this.options.setLongName(this.field2, "ab");
		this.options.setDescription(this.field2, "descr2");
		this.options.setLongName(this.field3, "abc");
		this.options.setDescription(this.field3, "descr3");
		assertEquals(" --a     descr1\n --ab    descr2\n --abc   descr3\n", optUsage());
	}

	@Test
	public void testPrintOptionNoShortButArgs() throws CliOptionDefinitionException {
		this.options.setLongName(this.field, "a");
		this.options.setMultiplicity(this.field, 2);
		this.options.setDescription(this.field, "descr1");
		this.options.setLongName(this.field2, "b");
		this.options.setDescription(this.field2, "descr2");
		this.options.setLongName(this.field3, "c");
		this.options.setDescription(this.field3, "descr3");
		assertEquals(" --a <arg0> <arg1>   descr1\n --b                 descr2\n --c                 descr3\n", optUsage());
	}

	@Test
	public void testPrintOptionNoLong() throws CliOptionDefinitionException {
		this.options.setShortName(this.field, "a");
		this.options.setDescription(this.field, "descr1");
		this.options.setShortName(this.field2, "b");
		this.options.setDescription(this.field2, "descr2");
		this.options.setShortName(this.field3, "c");
		this.options.setDescription(this.field3, "descr3");
		assertEquals(" -a   descr1\n -b   descr2\n -c   descr3\n", optUsage());
	}

	@Test
	public void testPrintOptionNoLongButArgs() throws CliOptionDefinitionException {
		this.options.setShortName(this.field, "a");
		this.options.setMultiplicity(this.field, 2);
		this.options.setDescription(this.field, "descr1");
		this.options.setShortName(this.field2, "b");
		this.options.setDescription(this.field2, "descr2");
		this.options.setShortName(this.field3, "c");
		this.options.setDescription(this.field3, "descr3");
		assertEquals(" -a <arg0> <arg1>   descr1\n -b                 descr2\n -c                 descr3\n", optUsage());
	}

	@Test
	public void testPrintOptionMixed() throws CliOptionDefinitionException {
		this.options.setLongName(this.field, "a");
		this.options.setDescription(this.field, "descr1");
		this.options.setShortName(this.field2, "b");
		this.options.setDescription(this.field2, "descr2");
		this.options.setShortName(this.field3, "c");
		this.options.setLongName(this.field3, "abc");
		assertEquals("    --a     descr1\n -b         descr2\n -c,--abc\n", optUsage());
	}

	@Test
	public void testPrintOptionFullMultiCharShortOpts() throws CliOptionDefinitionException {
		this.options.setShortName(this.field, "aaa");
		this.options.setLongName(this.field, "a");
		this.options.setDescription(this.field, "descr1");
		this.options.setShortName(this.field2, "b");
		this.options.setLongName(this.field2, "ab");
		this.options.setDescription(this.field2, "descr2");
		this.options.setShortName(this.field3, "c");
		this.options.setLongName(this.field3, "abc");
		this.options.setDescription(this.field3, "descr3");
		assertEquals(" -aaa,--a     descr1\n   -b,--ab    descr2\n   -c,--abc   descr3\n", optUsage());
	}

	@Test
	public void testPrintOptionNoOpts() {
		assertEquals("", optUsage());
	}

	@Test
	public void testPrintOptionUsageCaseCmp() throws CliOptionDefinitionException {
		this.options.setShortName(this.field, "B");
		this.options.setShortName(this.field2, "a");
		this.options.setShortName(this.field3, "A");
		assertEquals(" -A\n -a\n -B\n", optUsage());
	}
	
	@Test
	public void testRequired() throws CliOptionDefinitionException {
		this.options.setLongName(this.field, "a");
		this.options.setDescription(this.field, "descr1");
		this.options.setShortName(this.field2, "b");
		this.options.setDescription(this.field2, "descr2");
		this.options.setShortName(this.field3, "c");
		this.options.setLongName(this.field3, "abc");
		this.options.setRequired(this.field, true);
		this.options.setRequired(this.field2, true);
		this.options.setRequired(this.field3, true);
		assertEquals("    --a     descr1 [required]\n -b         descr2 [required]\n -c,--abc   [required]\n", optUsage());
	}

}
