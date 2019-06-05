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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import fr.cril.cli.CliUsageException;

public class EFieldTypeTest {
	
	private TestClass obj;
	
	@BeforeEach
	public void setUp() {
		EFieldType.resetBooleanConstants();
		this.obj = new TestClass();
	}
	
	@Test
	public void testBoolean() throws CliUsageException, NoSuchFieldException, SecurityException {
		final Field f = TestClass.class.getDeclaredField("b0");
		EFieldType.forClass(f.getType(), 0).apply(f, this.obj, Collections.emptyList());
		assertTrue(this.obj.b0);
	}
	
	@Test
	public void testBool() throws CliUsageException, NoSuchFieldException, SecurityException {
		final Field f = TestClass.class.getDeclaredField("b1");
		EFieldType.forClass(f.getType(), 0).apply(f, this.obj, Collections.emptyList());
		assertTrue(this.obj.b1);
	}
	
	@Test
	public void testBooleanWithArg() throws CliUsageException, NoSuchFieldException, SecurityException {
		final Field f = TestClass.class.getDeclaredField("b0");
		EFieldType.forClass(f.getType(), 1).apply(f, this.obj, Collections.singletonList("true"));
		assertTrue(this.obj.b0);
		EFieldType.forClass(f.getType(), 1).apply(f, this.obj, Collections.singletonList("false"));
		assertFalse(this.obj.b0);
	}
	
	@Test
	public void testBooleanWithWrongArg() throws CliUsageException, NoSuchFieldException, SecurityException {
		final Field f = TestClass.class.getDeclaredField("b0");
		final EFieldType type = EFieldType.forClass(f.getType(), 1);
		assertThrows(CliUsageException.class, () -> type.apply(f, this.obj, Collections.singletonList("foo")));
	}
	
	@Test
	public void testBoolWithArg() throws CliUsageException, NoSuchFieldException, SecurityException {
		final Field f = TestClass.class.getDeclaredField("b1");
		EFieldType.forClass(f.getType(), 1).apply(f, this.obj, Collections.singletonList("true"));
		assertTrue(this.obj.b1);
		EFieldType.forClass(f.getType(), 1).apply(f, this.obj, Collections.singletonList("false"));
		assertFalse(this.obj.b1);
	}
	
	@Test
	public void testBoolWithWrongArg() throws CliUsageException, NoSuchFieldException, SecurityException {
		final Field f = TestClass.class.getDeclaredField("b1");
		final EFieldType type = EFieldType.forClass(f.getType(), 1);
		assertThrows(CliUsageException.class, () -> type.apply(f, this.obj, Collections.singletonList("foo")));
	}
	
	@Test
	public void testInteger() throws NoSuchFieldException, SecurityException, CliUsageException {
		final Field f = TestClass.class.getDeclaredField("i0");
		EFieldType.forClass(f.getType(), 1).apply(f, this.obj, Collections.singletonList("1"));
		assertEquals(1, this.obj.i0);
	}
	
	@Test
	public void testInt() throws NoSuchFieldException, SecurityException, CliUsageException {
		final Field f = TestClass.class.getDeclaredField("i1");
		EFieldType.forClass(f.getType(), 1).apply(f, this.obj, Collections.singletonList("1"));
		assertEquals(1, this.obj.i1);
	}
	
	@Test
	public void testLong() throws NoSuchFieldException, SecurityException, CliUsageException {
		final Field f = TestClass.class.getDeclaredField("l0");
		EFieldType.forClass(f.getType(), 1).apply(f, this.obj, Collections.singletonList("1"));
		assertEquals(1, this.obj.l0);
	}
	
	@Test
	public void testLg() throws NoSuchFieldException, SecurityException, CliUsageException {
		final Field f = TestClass.class.getDeclaredField("l1");
		EFieldType.forClass(f.getType(), 1).apply(f, this.obj, Collections.singletonList("1"));
		assertEquals(1, this.obj.l1);
	}
	
	@Test
	public void testString() throws NoSuchFieldException, SecurityException, CliUsageException {
		final Field f = TestClass.class.getDeclaredField("s");
		EFieldType.forClass(f.getType(), 1).apply(f, this.obj, Collections.singletonList("foo"));
		assertEquals("foo", this.obj.s);
	}
	
	@Test
	public void testRetrieveByUnknownClass() {
		assertThrows(IllegalArgumentException.class, () -> EFieldType.forClass(TestClass.class, 1));
	}
	
	@Test
	public void testRetrieveByUnknownMultiplicity() {
		assertThrows(IllegalArgumentException.class, () -> EFieldType.forClass(Boolean.class, 2));
	}
	
	@ParameterizedTest
	@CsvSource({
		"b0, 0",
		"b1, 0",
		"i0, 1",
		"i1, 1",
		"l0, 1",
		"l1, 1",
		"s, 1"
	})
	public void testNullParams(final String fieldName, final int multiplicity) throws NoSuchFieldException, SecurityException {
		final Field f = TestClass.class.getDeclaredField(fieldName);
		final EFieldType type = EFieldType.forClass(f.getType(), multiplicity);
		assertThrows(IllegalArgumentException.class, () -> type.apply(f, this.obj, null));
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"i0", "i1", "l0", "l1", "s"})
	public void testNotEnoughParams(final String fieldName) throws NoSuchFieldException, SecurityException {
		final Field f = TestClass.class.getDeclaredField(fieldName);
		final EFieldType type = EFieldType.forClass(f.getType(), 1);
		assertThrows(IllegalArgumentException.class, () -> type.apply(f, this.obj, Collections.emptyList()));
	}
	
	@ParameterizedTest
	@CsvSource({
		"b0, 0",
		"b1, 0",
		"i0, 1",
		"i1, 1",
		"l0, 1",
		"l1, 1",
		"s, 1"
	})
	public void testTooMuchParams(final String fieldName, final int multiplicity) throws NoSuchFieldException, SecurityException {
		final Field f = TestClass.class.getDeclaredField(fieldName);
		final EFieldType type = EFieldType.forClass(f.getType(), multiplicity);
		assertThrows(IllegalArgumentException.class, () -> type.apply(f, this.obj, Stream.of("1", "2").collect(Collectors.toList())));
	}
	
	@Test
	public void testApplyWrongInteger() throws NoSuchFieldException, SecurityException {
		final Field f = TestClass.class.getDeclaredField("i0");
		final EFieldType type = EFieldType.forClass(f.getType(), 1);
		assertThrows(CliUsageException.class, () -> type.apply(f, this.obj, Collections.singletonList("a")));
	}
	
	@Test
	public void testApplyWrongInt() throws NoSuchFieldException, SecurityException {
		final Field f = TestClass.class.getDeclaredField("i1");
		final EFieldType type = EFieldType.forClass(f.getType(), 1);
		assertThrows(CliUsageException.class, () -> type.apply(f, this.obj, Collections.singletonList("a")));
	}
	
	@Test
	public void testApplyWrongLong() throws NoSuchFieldException, SecurityException {
		final Field f = TestClass.class.getDeclaredField("l0");
		final EFieldType type = EFieldType.forClass(f.getType(), 1);
		assertThrows(CliUsageException.class, () -> type.apply(f, this.obj, Collections.singletonList("a")));
	}
	
	@Test
	public void testApplyWrongLg() throws NoSuchFieldException, SecurityException {
		final Field f = TestClass.class.getDeclaredField("l1");
		final EFieldType type = EFieldType.forClass(f.getType(), 1);
		assertThrows(CliUsageException.class, () -> type.apply(f, this.obj, Collections.singletonList("a")));
	}
	
	@Test
	public void testWrongFieldType() throws NoSuchFieldException, SecurityException {
		final EFieldType type = EFieldType.forClass(TestClass.class.getDeclaredField("s").getType(), 1);
		assertThrows(IllegalArgumentException.class, () -> type.apply(TestClass.class.getDeclaredField("i0"), this.obj, Collections.singletonList("a")));
	}
	
	@Test
	public void testSetBooleanConstants() throws NoSuchFieldException, SecurityException, CliUsageException {
		EFieldType.setBooleanConstants(new String[] {"no"}, new String[] {"yes"});
		final Field f = TestClass.class.getDeclaredField("b1");
		EFieldType.forClass(f.getType(), 1).apply(f, this.obj, Collections.singletonList("yes"));
		assertTrue(this.obj.b1);
		EFieldType.forClass(f.getType(), 1).apply(f, this.obj, Collections.singletonList("no"));
		assertFalse(this.obj.b1);
	}
	
	@ParameterizedTest
	@CsvSource({
		",",
		"a, ",
		", a",
		"'', ''",
		"'', a",
		"a, ''",
		"a, a"
	})
	public void testSetWrongBooleanConstants(final String falseValue, final String trueValue) {
		final String[] f = falseValue == null ? null : new String[] {falseValue};
		final String[] t = trueValue == null ? null : new String[] {trueValue};
		assertThrows(IllegalArgumentException.class, () -> EFieldType.setBooleanConstants(f, t));
	}
	
	@ParameterizedTest
	@CsvSource({
		",",
		"a, ",
		", a"
	})
	public void testSetWrongBooleanConstantsEmptyArrays(final String falseValue, final String trueValue) {
		final String[] f = falseValue == null ? new String[] {} : new String[] {falseValue};
		final String[] t = trueValue == null ? new String[] {} : new String[] {trueValue};
		assertThrows(IllegalArgumentException.class, () -> EFieldType.setBooleanConstants(f, t));
	}
	
	@ParameterizedTest
	@CsvSource({
		",",
		"a, ",
		", a"
	})
	public void testSetWrongBooleanConstantsNullStrings(final String falseValue, final String trueValue) {
		final String[] f = falseValue == null ? new String[] {null} : new String[] {falseValue};
		final String[] t = trueValue == null ? new String[] {null} : new String[] {trueValue};
		assertThrows(IllegalArgumentException.class, () -> EFieldType.setBooleanConstants(f, t));
	}
	
	private class TestClass {
		
		private Boolean b0 = false;
		
		private boolean b1 = false;
		
		private Integer i0 = 0;
		
		private int i1 = 0;
		
		private Long l0 = 0l;
		
		private long l1 = 0l;
		
		private String s;
	}

}
