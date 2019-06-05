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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import fr.cril.cli.CliOptionDefinitionException;
import fr.cril.cli.annotations.Param;
import fr.cril.cli.annotations.Params;

public class EClassAnnotationTest {
	
	@ParameterizedTest
	@ValueSource(classes = {Params.class})
	public void testHasForClass(final Class<? extends Annotation> cl) {
		assertTrue(EClassAnnotation.hasForClass(cl));
	}
	
	@Test
	public void testHasNotForClass() {
		assertFalse(EClassAnnotation.hasForClass(Annotation.class));
	}
	
	@ParameterizedTest
	@ValueSource(classes = {Params.class})
	public void testForClass(final Class<? extends Annotation> cl) {
		assertTrue(EClassAnnotation.forClass(cl) instanceof EClassAnnotation);
	}
	
	@Test
	public void testForWrongClass() {
		assertThrows(IllegalArgumentException.class, () -> EClassAnnotation.forClass(Annotation.class));
	}
	
	@Test
	public void testSetMultiplicity() throws CliOptionDefinitionException {
		final OptionMap options = new OptionMap();
		EClassAnnotation.PARAMS.apply(TestClass.class.getAnnotation(Params.class), options);
		assertEquals(new Multiplicity(1, 2), options.getParamMultiplicity());
	}
	
	@Params("1..2")
	public class TestClass {
		
		@Param(1)
		private String s1;
		
		@Param(2)
		private String s2;
	}

}
