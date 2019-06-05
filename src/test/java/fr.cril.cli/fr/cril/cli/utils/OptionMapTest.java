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
	
	@BeforeEach
	public void setUp() throws NoSuchFieldException, SecurityException {
		this.options = new OptionMap();
		this.field = OptionMapTest.class.getDeclaredField("options");
	}
	
	@ParameterizedTest
	@ValueSource(chars = {' ', '-', '?'})
	public void testSetShortNameWrongChar(final char c) throws CliOptionDefinitionException {
		assertThrows(CliOptionDefinitionException.class, () -> this.options.setShortName(this.field, c));
	}
	
	@Test
	public void testSetShortNameMultOcc() throws CliOptionDefinitionException {
		this.options.setShortName(this.field, 'a');
		assertThrows(CliOptionDefinitionException.class, () -> this.options.setShortName(this.field, 'a'));
	}
	
	@Test
	public void testSetShortName() throws CliOptionDefinitionException, CliUsageException {
		this.options.setShortName(this.field, 'a');
		assertEquals(this.field, this.options.getField('a'));
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
		assertEquals(this.field, this.options.getField("a"));
	}
	
	@Test
	public void testSetLongNameComposed() throws CliOptionDefinitionException, CliUsageException {
		this.options.setLongName(this.field, "a-b");
		assertEquals(this.field, this.options.getField("a-b"));
	}
	
	@Test
	public void testSetMultiplicity() throws CliOptionDefinitionException {
		this.options.setShortName(this.field, 'a');
		this.options.setMultiplicity(this.field, 1);
		final int multiplicity = this.options.getArgMultiplicity(this.field);
		assertEquals(1, multiplicity);
	}
	
	@Test
	public void testDefaultMultiplicity() throws CliOptionDefinitionException {
		this.options.setShortName(this.field, 'a');
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
		this.options.setShortName(this.field, 'a');
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
		this.options.setShortName(this.field, 'a');
		this.options.setLongName(this.field, "a");
		this.options.setMultiplicity(this.field, 1);
		this.options.sanityChecks();
	}
	
	@Test
	public void testGetFieldWrongShortName() {
		assertThrows(CliUsageException.class, () -> this.options.getField('a'));
	}
	
	@Test
	public void testGetFieldWrongLongName() {
		assertThrows(CliUsageException.class, () -> this.options.getField("arg"));
	}
	
	@Test
	public void testRequiredTrue() throws CliOptionDefinitionException {
		this.options.setShortName(this.field, 'a');
		this.options.setRequired(this.field, true);
		assertTrue(this.options.isRequired(this.field));
	}
	
	@Test
	public void testRequiredSet() throws CliOptionDefinitionException {
		this.options.setShortName(this.field, 'a');
		this.options.setRequired(this.field, true);
		assertEquals(Collections.singleton(this.field), this.options.getRequiredFields());
	}
	
	@Test
	public void testRequiredFalse() throws CliOptionDefinitionException {
		this.options.setShortName(this.field, 'a');
		this.options.setRequired(this.field, false);
		assertFalse(this.options.isRequired(this.field));
	}
	
	@Test
	public void testRequiredDefaultValue() throws CliOptionDefinitionException {
		this.options.setShortName(this.field, 'a');
		assertFalse(this.options.isRequired(this.field));
	}
	
	@Test
	public void testSetRequiredTwice() throws CliOptionDefinitionException {
		this.options.setShortName(this.field, 'a');
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
		this.options.setShortName(this.field, 'a');
		this.options.setLongName(this.field, "abc");
		assertEquals("--abc (-a)", this.options.fieldToString(this.field));
	}
	
	@Test
	public void testFieldToStringShort() throws CliOptionDefinitionException {
		this.options.setShortName(this.field, 'a');
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
		this.options.setShortName(this.field, 'a');
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
		this.options.setShortName(this.field, 'a');
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
		this.options.setShortName(this.field, 'a');
		assertEquals("foobar", this.options.getDescription(this.field));
	}
	
	@Test
	public void testGetDefaultDescription() throws CliOptionDefinitionException {
		this.options.setShortName(this.field, 'a');
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
		this.options.setShortName(this.field, 'a');
		assertThrows(CliOptionDefinitionException.class, () -> this.options.setShortName(this.field, 'b'));
	}
	
	@Test
	public void testSharedLongName() throws CliOptionDefinitionException {
		this.options.setLongName(this.field, "a");
		assertThrows(CliOptionDefinitionException.class, () -> this.options.setLongName(this.field, "a"));
	}

}
