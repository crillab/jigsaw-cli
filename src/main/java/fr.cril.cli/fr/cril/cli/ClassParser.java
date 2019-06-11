package fr.cril.cli;

import java.io.PrintWriter;

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
	
	private final Class<T> cl;
	
	private OptionMap optMap;
	
	private boolean allowShortNamesMerging = true;

	/**
	 * Builds a parser given the class under consideration.
	 * 
	 * @param cl the class
	 */
	public ClassParser(final Class<T> cl) {
		this.cl = cl;
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
	OptionMap parse() throws CliOptionDefinitionException {
		this.optMap = new OptionMap();
		this.optMap.allowShortNamesMerging(this.allowShortNamesMerging);
		for(final Annotation annotation: this.cl.getAnnotations()) {
			final Class<? extends Annotation> annotationType = annotation.annotationType();
			if(!EClassAnnotation.hasForClass(annotationType)) {
				continue;
			}
			EClassAnnotation.forClass(annotationType).apply(annotation, this.optMap);
		}
		for(final Field f : this.cl.getDeclaredFields()) {
			parseField(this.optMap, f);
		}
		this.optMap.sanityChecks();
		return this.optMap;
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
	
	/**
	 * Prints the option list and their description into the provided {@link PrintWriter}.
	 * 
	 * @param out the {@link PrintWriter}
	 */
	public void printOptionUsage(final PrintWriter out) {
		this.optMap.printOptionUsage(out);
	}
	
	/**
	 * Allows short names merging in CLI arguments (<code>-ab</code> means <code>-a -b</code>).
	 * The merging is allowed only if the options take no parameter.
	 * 
	 * The default is <code>true</code>.
	 * 
	 * In case merging is allowed, a check is made to prevent ambiguity: declaring <code>-a</code>, <code>-b</code> and <code>-ab</code>
	 * will cause an exception to be thrown when {@link ClassParser#parse()} is called.
	 * Disabling merging allows the declaration of such option set.
	 * 
	 * @param allow <code>true</code> to allow
	 */
	void allowShortNamesMerging(final boolean allow) {
		this.allowShortNamesMerging = allow;
	}

}
