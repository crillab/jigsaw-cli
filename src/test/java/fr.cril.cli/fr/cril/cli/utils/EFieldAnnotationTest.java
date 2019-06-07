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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import fr.cril.cli.CliOptionDefinitionException;
import fr.cril.cli.CliUsageException;
import fr.cril.cli.annotations.Args;
import fr.cril.cli.annotations.Description;
import fr.cril.cli.annotations.LongName;
import fr.cril.cli.annotations.Param;
import fr.cril.cli.annotations.Required;
import fr.cril.cli.annotations.ShortName;

public class EFieldAnnotationTest {
	
	private OptionMap options;
	
	@ShortName("o")
	@LongName("opt")
	@Args(1)
	@Description("I'm the option")
	@Required
	private int option;
	
	private Field optField;
	
	@Param
	private String param;
	
	private Field paramField;
	
	@BeforeEach
	public void setUp() throws NoSuchFieldException, SecurityException {
		this.options = new OptionMap();
		this.optField = EFieldAnnotationTest.class.getDeclaredField("option");
		this.paramField = EFieldAnnotationTest.class.getDeclaredField("param");
	}
	
	@ParameterizedTest
	@ValueSource(classes = {ShortName.class, LongName.class, Args.class})
	public void testHasForClass(final Class<? extends Annotation> cl) {
		assertTrue(EFieldAnnotation.hasForClass(cl));
	}
	
	@Test
	public void testHasNotForClass() {
		assertFalse(EFieldAnnotation.hasForClass(Annotation.class));
	}
	
	@ParameterizedTest
	@ValueSource(classes = {ShortName.class, LongName.class, Args.class})
	public void testForClass(final Class<? extends Annotation> cl) {
		assertTrue(EFieldAnnotation.forClass(cl) instanceof EFieldAnnotation);
	}
	
	@Test
	public void testForWrongClass() {
		assertThrows(IllegalArgumentException.class, () -> EFieldAnnotation.forClass(Annotation.class));
	}
	
	@Test
	public void testApplyShortName() throws CliOptionDefinitionException, CliUsageException {
		EFieldAnnotation.SHORT_NAME.apply(this.optField, this.optField.getAnnotation(ShortName.class), this.options);
		assertEquals(this.optField, this.options.getFieldByShortName("o"));
	}
	
	@Test
	public void testApplyLongName() throws CliOptionDefinitionException, CliUsageException {
		EFieldAnnotation.LONG_NAME.apply(this.optField, this.optField.getAnnotation(LongName.class), this.options);
		assertEquals(this.optField, this.options.getFieldByLongName("opt"));
	}
	
	@Test
	public void testApplyArgMultiplicity() throws CliOptionDefinitionException {
		EFieldAnnotation.OPT_ARG_MULTIPLICITY.apply(this.optField, this.optField.getAnnotation(Args.class), this.options);
		assertEquals(1, this.options.getArgMultiplicity(this.optField));
	}
	
	@Test
	public void testApplyRequired() throws CliOptionDefinitionException {
		EFieldAnnotation.REQUIRED.apply(this.optField, this.optField.getAnnotation(Required.class), this.options);
		assertTrue(this.options.isRequired(this.optField));
	}
	
	@Test
	public void testApplyParam() throws CliOptionDefinitionException {
		EFieldAnnotation.PARAM.apply(this.paramField, this.paramField.getAnnotation(Param.class), this.options);
		assertEquals(1, this.options.nParams());
	}
	
	@Test
	public void testApplyDescription() throws CliOptionDefinitionException {
		EFieldAnnotation.DESCRIPTION.apply(this.optField, this.optField.getAnnotation(Description.class), this.options);
		assertEquals("I'm the option", this.options.getDescription(this.optField));
	}
	
	@ParameterizedTest
	@EnumSource(EFieldAnnotation.class)
	public void testApplyNullField(final EFieldAnnotation a) {
		assertThrows(IllegalArgumentException.class, () -> a.apply(null, this.paramField.getAnnotation(a.getAnnotationClass()), this.options));
	}
	
	@ParameterizedTest
	@EnumSource(EFieldAnnotation.class)
	public void testApplyNullAnnotation(final EFieldAnnotation a) {
		assertThrows(IllegalArgumentException.class, () -> a.apply(this.optField, null, this.options));
	}
	
	@ParameterizedTest
	@EnumSource(EFieldAnnotation.class)
	public void testApplyNullOptions(final EFieldAnnotation a) {
		assertThrows(IllegalArgumentException.class, () -> a.apply(this.optField, this.paramField.getAnnotation(a.getAnnotationClass()), null));
	}
	
}
