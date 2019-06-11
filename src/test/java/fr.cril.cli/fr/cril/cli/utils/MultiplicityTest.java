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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import fr.cril.cli.CliOptionDefinitionException;
import nl.jqno.equalsverifier.EqualsVerifier;

public class MultiplicityTest {
	
	@Test
	public void testOk() {
		final Multiplicity multiplicity = new Multiplicity(1, 2);
		assertEquals(1, multiplicity.getMin());
		assertEquals(2, multiplicity.getMax());
	}
	
	@Test
	public void testStar() {
		final Multiplicity multiplicity = new Multiplicity("1..*");
		assertEquals(1, multiplicity.getMin());
		assertEquals(Integer.MAX_VALUE, multiplicity.getMax());
	}
	
	@ParameterizedTest
	@CsvSource({"2, 1", "-2, -1"})
	public void testIllegalParams(final int min, final int max) {
		assertThrows(IllegalArgumentException.class, () -> new Multiplicity(min, max));
	}
	
	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {"0", "0.", "1.", "0.1", "0..", "..1", "1..0", "-2..-1", "0..a", "a..1", " 0..1", "0..1 ", "0 .. 1", "0 ..1", "0.. 1", "0. .1"})
	public void testSetArgMultiplicityWrongMult(final String pattern) throws CliOptionDefinitionException {
		assertThrows(IllegalArgumentException.class, () -> new Multiplicity(pattern));
	}
	
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(Multiplicity.class).verify();
	}
	
	@Test
	public void testToString() {
		assertEquals("[1..2]", new Multiplicity(1, 2).toString());
	}
	
	@Test
	public void testToStringStar() {
		assertEquals("[1..*]", new Multiplicity("1..*").toString());
	}
	
	@ParameterizedTest
	@CsvSource({
		"2, 2, 'exactly 2'",
		"1, 3, 'between 1 and 3'",
		"0, 2, 'at most 2'",
		"1, *, 'at least 1'",
		"0, *, 'any'"
	})
	public void testToHumanReadableString(final String min, final String max, final String expected) {
		assertEquals(expected, new Multiplicity(min+".."+max).toHumanReadableString());
	}

}
