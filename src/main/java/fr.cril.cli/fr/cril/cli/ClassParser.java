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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import fr.cril.cli.utils.EClassAnnotation;
import fr.cril.cli.utils.EFieldAnnotation;
import fr.cril.cli.utils.OptionMap;

/**
 * The class used to parse a class instance, looking for the CLI annotations.
 * 
 * It must be parameterized by the class type of the instance under consideration.
 * 
 * @author Emmanuel Lonca - lonca@cril.fr
 */
public class ClassParser<T> {
	
	private final T instance;
	
	/**
	 * Builds a parser given the class instance under consideration.
	 * 
	 * @param instance the class instance
	 */
	public ClassParser(final T instance) {
		this.instance = instance;
	}
	
	/**
	 * Launches the parsing process.
	 * 
	 * An {@link OptionMap} describing the options set by annotations is returned.
	 * 
	 * In case errors are detected in the definition of the options, a {@link CliOptionDefinitionException} is thrown.
	 * 
	 * @return an {@link OptionMap} describing the options
	 * @throws CliOptionDefinitionException in case errors are detected in the definition of the options
	 */
	public OptionMap parse() throws CliOptionDefinitionException {
		final OptionMap optMap = new OptionMap();
		for(final Annotation annotation: this.instance.getClass().getAnnotations()) {
			final Class<? extends Annotation> annotationType = annotation.annotationType();
			if(!EClassAnnotation.hasForClass(annotationType)) {
				continue;
			}
			EClassAnnotation.forClass(annotationType).apply(annotation, optMap);
		}
		for(final Field f : this.instance.getClass().getDeclaredFields()) {
			parseField(optMap, f);
		}
		optMap.sanityChecks();
		return optMap;
	}
	
	private void parseField(final OptionMap optMap, final Field field) throws CliOptionDefinitionException {
		for(final Annotation annotation : field.getAnnotations()) {
			final Class<? extends Annotation> annotationType = annotation.annotationType();
			if(!EFieldAnnotation.hasForClass(annotationType)) {
				continue;
			}
			EFieldAnnotation.forClass(annotationType).apply(field, annotation, optMap);
		}
	}

}
