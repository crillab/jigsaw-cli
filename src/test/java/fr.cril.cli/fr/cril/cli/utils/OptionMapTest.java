package fr.cril.cli.utils;

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

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import fr.cril.cli.CliOptionDefinitionException;
import fr.cril.cli.CliUsageException;

public class OptionMapTest {
	
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
	
	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "  ", "???", "-abc", "abc?"})
	public void testSetShortNameWrongVals(final String s) {
		assertThrows(CliOptionDefinitionException.class, () -> this.options.setShortName(this.field, s));
	}
	
	@Test
	public void testSetShortNameMultOcc() throws CliOptionDefinitionException {
		this.options.setShortName(this.field, "a");
		assertThrows(CliOptionDefinitionException.class, () -> this.options.setShortName(this.field, "a"));
	}
	
	@Test
	public void testSetShortName() throws CliOptionDefinitionException, CliUsageException {
		this.options.setShortName(this.field, "a");
		assertEquals(this.field, this.options.getFieldByShortName("a"));
	}
	
	@Test
	public void testSetShortNameComposed() throws CliOptionDefinitionException, CliUsageException {
		this.options.setShortName(this.field, "a-b");
		assertEquals(this.field, this.options.getFieldByShortName("a-b"));
	}
	
	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "  ", "???", "-abc", "abc?"})
	public void testSetLongNameWrongVals(final String s) {
		assertThrows(CliOptionDefinitionException.class, () -> this.options.setLongName(this.field, s));
	}
	
	@Test
	public void testSetLongNameMultOcc() throws CliOptionDefinitionException {
		this.options.setLongName(this.field, "a");
		assertThrows(CliOptionDefinitionException.class, () -> this.options.setLongName(this.field, "a"));
	}
	
	@Test
	public void testSetLongName() throws CliOptionDefinitionException, CliUsageException {
		this.options.setLongName(this.field, "a");
		assertEquals(this.field, this.options.getFieldByLongName("a"));
	}
	
	@Test
	public void testSetLongNameComposed() throws CliOptionDefinitionException, CliUsageException {
		this.options.setLongName(this.field, "a-b");
		assertEquals(this.field, this.options.getFieldByLongName("a-b"));
	}
	
	@Test
	public void testSetMultiplicity() throws CliOptionDefinitionException {
		this.options.setShortName(this.field, "a");
		this.options.setMultiplicity(this.field, 1);
		final int multiplicity = this.options.getArgMultiplicity(this.field);
		assertEquals(1, multiplicity);
	}
	
	@Test
	public void testDefaultMultiplicity() throws CliOptionDefinitionException {
		this.options.setShortName(this.field, "a");
		final int multiplicity = this.options.getArgMultiplicity(this.field);
		assertEquals(0, multiplicity);
	}
	
	@Test
	public void testSetWrongMult() {
		assertThrows(CliOptionDefinitionException.class, () -> this.options.setMultiplicity(this.field, -1));
	}
	
	@Test
	public void testSetMultTwice() throws CliOptionDefinitionException {
		this.options.setMultiplicity(this.field, 0);
		assertThrows(CliOptionDefinitionException.class, () -> this.options.setMultiplicity(this.field, 0));
	}
	
	@Test
	public void testSetMultiplicityButNoName() throws CliOptionDefinitionException {
		this.options.setMultiplicity(this.field, 1);
		assertThrows(CliOptionDefinitionException.class, () -> this.options.sanityChecks());
	}
	
	@Test
	public void testSanityNoParams() throws CliOptionDefinitionException {
		this.options.sanityChecks();
	}
	
	@Test
	public void testMultSanityOkShort() throws CliOptionDefinitionException {
		this.options.setShortName(this.field, "a");
		this.options.setMultiplicity(this.field, 1);
		this.options.sanityChecks();
	}
	
	@Test
	public void testMultSanityOkLong() throws CliOptionDefinitionException {
		this.options.setLongName(this.field, "a");
		this.options.setMultiplicity(this.field, 1);
		this.options.sanityChecks();
	}
	
	@Test
	public void testMultSanityOkBoth() throws CliOptionDefinitionException {
		this.options.setShortName(this.field, "a");
		this.options.setLongName(this.field, "a");
		this.options.setMultiplicity(this.field, 1);
		this.options.sanityChecks();
	}
	
	@Test
	public void testGetFieldWrongShortName() {
		assertThrows(CliUsageException.class, () -> this.options.getFieldByShortName("a"));
	}
	
	@Test
	public void testGetFieldWrongLongName() {
		assertThrows(CliUsageException.class, () -> this.options.getFieldByLongName("arg"));
	}
	
	@Test
	public void testRequiredTrue() throws CliOptionDefinitionException {
		this.options.setShortName(this.field, "a");
		this.options.setRequired(this.field, true);
		assertTrue(this.options.isRequired(this.field));
	}
	
	@Test
	public void testRequiredSet() throws CliOptionDefinitionException {
		this.options.setShortName(this.field, "a");
		this.options.setRequired(this.field, true);
		assertEquals(Collections.singleton(this.field), this.options.getRequiredFields());
	}
	
	@Test
	public void testRequiredFalse() throws CliOptionDefinitionException {
		this.options.setShortName(this.field, "a");
		this.options.setRequired(this.field, false);
		assertFalse(this.options.isRequired(this.field));
	}
	
	@Test
	public void testRequiredDefaultValue() throws CliOptionDefinitionException {
		this.options.setShortName(this.field, "a");
		assertFalse(this.options.isRequired(this.field));
	}
	
	@Test
	public void testSetRequiredTwice() throws CliOptionDefinitionException {
		this.options.setShortName(this.field, "a");
		this.options.setRequired(this.field, true);
		assertThrows(CliOptionDefinitionException.class, () -> this.options.setRequired(this.field, true));
	}
	
	@Test
	public void testSanityRequiredButUnnamed() throws CliOptionDefinitionException {
		this.options.setRequired(this.field, true);
		assertThrows(CliOptionDefinitionException.class, () -> this.options.sanityChecks());
	}
	
	@Test
	public void testFieldToStringBoth() throws CliOptionDefinitionException {
		this.options.setShortName(this.field, "a");
		this.options.setLongName(this.field, "abc");
		assertEquals("--abc (-a)", this.options.fieldToString(this.field));
	}
	
	@Test
	public void testFieldToStringShort() throws CliOptionDefinitionException {
		this.options.setShortName(this.field, "a");
		assertEquals("-a", this.options.fieldToString(this.field));
	}
	
	@Test
	public void testFieldToStringLong() throws CliOptionDefinitionException {
		this.options.setLongName(this.field, "abc");
		assertEquals("--abc", this.options.fieldToString(this.field));
	}
	
	@Test
	public void testFieldToStringNone() throws CliOptionDefinitionException {
		assertThrows(IllegalArgumentException.class, () -> this.options.fieldToString(this.field));
	}
	
	@Test
	public void testSetParam() throws CliOptionDefinitionException {
		this.options.setParamMultiplicity("0..1");
		this.options.setParam(this.field, 0);
		this.options.sanityChecks();
		assertEquals(1, this.options.nParams());
		assertEquals(this.field, this.options.getParamField(0));
	}
	
	@Test
	public void testSetParamTwice() throws CliOptionDefinitionException {
		this.options.setParamMultiplicity("0..*");
		this.options.setParam(this.field, 0);
		assertThrows(CliOptionDefinitionException.class, () -> this.options.setParam(this.field, 0));
	}
	
	@Test
	public void testSanityShortNamedParam() throws CliOptionDefinitionException {
		this.options.setParamMultiplicity("0..*");
		this.options.setParam(this.field, 0);
		this.options.setShortName(this.field, "a");
		assertThrows(CliOptionDefinitionException.class, () -> this.options.sanityChecks());
	}
	
	@Test
	public void testSanityLongNamedParam() throws CliOptionDefinitionException {
		this.options.setParamMultiplicity("0..*");
		this.options.setParam(this.field, 0);
		this.options.setLongName(this.field, "a");
		assertThrows(CliOptionDefinitionException.class, () -> this.options.sanityChecks());
	}
	
	@Test
	public void testSanityBothNamesParam() throws CliOptionDefinitionException {
		this.options.setParamMultiplicity("0..*");
		this.options.setParam(this.field, 0);
		this.options.setShortName(this.field, "a");
		this.options.setLongName(this.field, "a");
		assertThrows(CliOptionDefinitionException.class, () -> this.options.sanityChecks());
	}
	
	@Test
	public void testSetParamMult() throws CliOptionDefinitionException {
		this.options.setParamMultiplicity("1..2");
		assertEquals(new Multiplicity(1, 2), this.options.getParamMultiplicity());
	}
	
	@Test
	public void testSetParamMultTwice() throws CliOptionDefinitionException {
		this.options.setParamMultiplicity("1..2");
		assertThrows(CliOptionDefinitionException.class, () -> this.options.setParamMultiplicity("1..2"));
	}
	
	@Test
	public void testGetDefaultParamMult() {
		assertEquals(new Multiplicity(0), this.options.getParamMultiplicity());
	}
	
	@Test
	public void testSanityTooMuchParamsForClassMult() throws CliOptionDefinitionException {
		this.options.setParamMultiplicity("0..0");
		this.options.setParam(this.field, 0);
		assertThrows(CliOptionDefinitionException.class, () -> this.options.sanityChecks());
	}
	
	@Test
	public void testSanityRequiredParam() throws CliOptionDefinitionException {
		this.options.setParamMultiplicity("0..1");
		this.options.setParam(this.field, 0);
		this.options.setRequired(this.field, true);
		assertThrows(CliOptionDefinitionException.class, () -> this.options.sanityChecks());
	}
	
	@Test
	public void testSanityParamWithArgs() throws CliOptionDefinitionException {
		this.options.setParamMultiplicity("0..1");
		this.options.setParam(this.field, 0);
		this.options.setMultiplicity(this.field, 1);
		assertThrows(CliOptionDefinitionException.class, () -> this.options.sanityChecks());
	}
	
	@Test
	public void testSetDescription() throws CliOptionDefinitionException {
		this.options.setDescription(this.field, "foobar");
		this.options.setShortName(this.field, "a");
		assertEquals("foobar", this.options.getDescription(this.field));
	}
	
	@Test
	public void testGetDefaultDescription() throws CliOptionDefinitionException {
		this.options.setShortName(this.field, "a");
		assertEquals("", this.options.getDescription(this.field));
	}
	
	@Test
	public void testSetNullDescription() {
		assertThrows(CliOptionDefinitionException.class, () -> this.options.setDescription(this.field, null));
	}
	
	@Test
	public void testSetEmptyDescription() {
		assertThrows(CliOptionDefinitionException.class, () -> this.options.setDescription(this.field, ""));
	}
	
	@Test
	public void testSetDescriptionTwice() throws CliOptionDefinitionException {
		this.options.setDescription(this.field, "foobar");
		assertThrows(CliOptionDefinitionException.class, () -> this.options.setDescription(this.field, "foobar"));
	}
	
	@Test
	public void testSanityUnnamedWithDescription() throws CliOptionDefinitionException {
		this.options.setDescription(this.field, "foobar");
		assertThrows(CliOptionDefinitionException.class, () -> this.options.sanityChecks());
	}
	
	@Test
	public void testSharedShortName() throws CliOptionDefinitionException {
		this.options.setShortName(this.field, "a");
		assertThrows(CliOptionDefinitionException.class, () -> this.options.setShortName(this.field, "b"));
	}
	
	@Test
	public void testSharedLongName() throws CliOptionDefinitionException {
		this.options.setLongName(this.field, "a");
		assertThrows(CliOptionDefinitionException.class, () -> this.options.setLongName(this.field, "b"));
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
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		final PrintWriter pw = new PrintWriter(os);
		this.options.printOptionUsage(pw);
		assertEquals(" -a,--a     descr1\n -b,--ab    descr2\n -c,--abc   descr3\n", new String(os.toByteArray()));
	}
	
	@Test
	public void testPrintOptionNoShort() throws CliOptionDefinitionException {
		this.options.setLongName(this.field, "a");
		this.options.setDescription(this.field, "descr1");
		this.options.setLongName(this.field2, "ab");
		this.options.setDescription(this.field2, "descr2");
		this.options.setLongName(this.field3, "abc");
		this.options.setDescription(this.field3, "descr3");
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		final PrintWriter pw = new PrintWriter(os);
		this.options.printOptionUsage(pw);
		assertEquals(" --a     descr1\n --ab    descr2\n --abc   descr3\n", new String(os.toByteArray()));
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
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		final PrintWriter pw = new PrintWriter(os);
		this.options.printOptionUsage(pw);
		assertEquals(" --a <arg0> <arg1>   descr1\n --b                 descr2\n --c                 descr3\n", new String(os.toByteArray()));
	}
	
	@Test
	public void testPrintOptionNoLong() throws CliOptionDefinitionException {
		this.options.setShortName(this.field, "a");
		this.options.setDescription(this.field, "descr1");
		this.options.setShortName(this.field2, "b");
		this.options.setDescription(this.field2, "descr2");
		this.options.setShortName(this.field3, "c");
		this.options.setDescription(this.field3, "descr3");
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		final PrintWriter pw = new PrintWriter(os);
		this.options.printOptionUsage(pw);
		assertEquals(" -a   descr1\n -b   descr2\n -c   descr3\n", new String(os.toByteArray()));
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
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		final PrintWriter pw = new PrintWriter(os);
		this.options.printOptionUsage(pw);
		assertEquals(" -a <arg0> <arg1>   descr1\n -b                 descr2\n -c                 descr3\n", new String(os.toByteArray()));
	}
	
	@Test
	public void testPrintOptionMixed() throws CliOptionDefinitionException {
		this.options.setLongName(this.field, "a");
		this.options.setDescription(this.field, "descr1");
		this.options.setShortName(this.field2, "b");
		this.options.setDescription(this.field2, "descr2");
		this.options.setShortName(this.field3, "c");
		this.options.setLongName(this.field3, "abc");
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		final PrintWriter pw = new PrintWriter(os);
		this.options.printOptionUsage(pw);
		assertEquals("    --a     descr1\n -b         descr2\n -c,--abc\n", new String(os.toByteArray()));
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
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		final PrintWriter pw = new PrintWriter(os);
		this.options.printOptionUsage(pw);
		assertEquals(" -aaa,--a     descr1\n   -b,--ab    descr2\n   -c,--abc   descr3\n", new String(os.toByteArray()));
	}
	
	@Test
	public void testPrintOptionNoOpts() {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		final PrintWriter pw = new PrintWriter(os);
		this.options.printOptionUsage(pw);
		assertEquals("", new String(os.toByteArray()));
	}
	
	@Test
	public void testSetNullShortName() throws CliOptionDefinitionException, CliUsageException {
		assertThrows(CliOptionDefinitionException.class, () -> this.options.setShortName(this.field, null));
	}
	
	@Test
	public void testSetNullLongName() throws CliOptionDefinitionException, CliUsageException {
		assertThrows(CliOptionDefinitionException.class, () -> this.options.setLongName(this.field, null));
	}
	
	@Test
	public void testSetNullParamMultiplicity() {
		assertThrows(IllegalArgumentException.class, () -> this.options.setParamMultiplicity(null));
	}
	
	@Test
	public void testSetDescriptionNullField() {
		assertThrows(IllegalArgumentException.class, () -> this.options.setDescription(null, "foo"));
	}
	
	@Test
	public void testSetLongNameNullField() {
		assertThrows(IllegalArgumentException.class, () -> this.options.setLongName(null, "foo"));
	}
	
	@Test
	public void testSetMultiplicityNullField() {
		assertThrows(IllegalArgumentException.class, () -> this.options.setMultiplicity(null, 1));
	}
	
	@Test
	public void testSetParamNullField() {
		assertThrows(IllegalArgumentException.class, () -> this.options.setParam(null, 0));
	}
	
	@Test
	public void testSetRequiredNullField() {
		assertThrows(IllegalArgumentException.class, () -> this.options.setRequired(null, true));
	}
	
	@Test
	public void testSetShortNameNullField() {
		assertThrows(IllegalArgumentException.class, () -> this.options.setShortName(null, "a"));
	}
	
	@Test
	public void testGetMultiplityNullArg() {
		assertThrows(IllegalArgumentException.class, () -> this.options.getArgMultiplicity(null));
	}
	
	@Test
	public void testGetDescriptionNullArg() {
		assertThrows(IllegalArgumentException.class, () -> this.options.getDescription(null));
	}
	
	@Test
	public void testGetFieldByShortNameNullArg() {
		assertThrows(IllegalArgumentException.class, () -> this.options.getFieldByShortName(null));
	}
	
	@Test
	public void testGetFieldByLongNameNullArg() {
		assertThrows(IllegalArgumentException.class, () -> this.options.getFieldByLongName(null));
	}
	
	@Test
	public void testIsRequiredNullArg() {
		assertThrows(IllegalArgumentException.class, () -> this.options.isRequired(null));
	}
	
	@Test
	public void testFieldToStringNullArg() {
		assertThrows(IllegalArgumentException.class, () -> this.options.fieldToString(null));
	}
	
	@Test
	public void testSanityShortOptsMergingAmbiguity() throws CliOptionDefinitionException {
		this.options.setShortName(this.field, "a");
		this.options.setShortName(this.field2, "b");
		assertThrows(CliOptionDefinitionException.class, () -> {
			this.options.setShortName(this.field3, "ab");
			this.options.sanityChecks();
		});
	}
	
	@Test
	public void testSanityShortOptsMergingNoAmbiguity() throws CliOptionDefinitionException {
		this.options.setShortName(this.field, "a");
		this.options.setShortName(this.field2, "b");
		this.options.setShortName(this.field3, "abc");
		this.options.sanityChecks();
	}
	
	@Test
	public void testHasShortName() throws CliOptionDefinitionException {
		this.options.setShortName(this.field, "a");
		assertTrue(this.options.hasShortName("a"));
	}
	
	@Test
	public void testHasNotShortName() throws CliOptionDefinitionException {
		assertFalse(this.options.hasShortName("a"));
	}
	
	@Test
	public void testHasNullShortName() {
		assertThrows(IllegalArgumentException.class, () -> this.options.hasShortName(null));
	}
	
	@Test
	public void testDisallowShortNamesMerging() throws CliOptionDefinitionException {
		this.options.allowShortNamesMerging(false);
		this.options.setShortName(this.field, "a");
		this.options.setShortName(this.field2, "b");
		this.options.setShortName(this.field3, "ab");
		this.options.sanityChecks();
	}
}
