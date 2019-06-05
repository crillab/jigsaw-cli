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

import java.lang.annotation.Annotation;
import java.util.Arrays;

import fr.cril.cli.CliOptionDefinitionException;
import fr.cril.cli.annotations.Params;

/**
 * An enumeration of all supported annotations related to classes used to define CLI arguments.
 * 
 * It is used to apply the annotation logic on {@link OptionMap} instance:
 * <ol>
 * <li>call {@link EClassAnnotation#hasForClass(Class)} to check if an annotation belongs to the user library;</li>
 * <li>call {@link EClassAnnotation#forClass(Class)} to get the enumeration constant related to a user library annotation;</li>
 * <li>call {@link EClassAnnotation#apply(Annotation, OptionMap)} to apply the annotation logic on the {@link OptionMap} instance.</li>
 * </ol>
 * 
 * @author Emmanuel Lonca - lonca@cril.fr
 */
public enum EClassAnnotation {
	
	/** enumeration constant related to options short names */
	PARAMS(Params.class, (a, o) -> o.setParamMultiplicity(((Params) a).value()));
	
	private final Class<? extends Annotation> annotationCl;
	
	private final OptionAnnotationApplier applier;
	
	private EClassAnnotation(final Class<? extends Annotation> annotationCl, final OptionAnnotationApplier applier) {
		this.annotationCl = annotationCl;
		this.applier = applier;
	}
	
	/**
	 * Returns the enumeration constant related to an annotation of the user library.
	 * 
	 * The class of an annotation must be provided.
	 * In case the class object does not belongs to the user library, an {@link IllegalArgumentException} is thrown.
	 * 
	 * @param cl the class of the annotation
	 * @return the corresponding enumeration constant
	 */
	public static EClassAnnotation forClass(final Class<? extends Annotation> cl) {
		for(final EClassAnnotation a : values()) {
			if(a.annotationCl.equals(cl)) {
				return a;
			}
		}
		throw new IllegalArgumentException(cl+" does not belong to the user library");
	}
	
	/**
	 * Checks if an annotation belongs to the user library.
	 * 
	 * @param cl the class of the annotation
	 * @return <code>true</code> iff the annotation belongs to the user library.
	 */
	public static boolean hasForClass(final Class<? extends Annotation> cl) {
		return Arrays.stream(values()).anyMatch(a -> a.annotationCl.equals(cl));
	}
	
	/**
	 * Applies the annotation logic.
	 * 
	 * In case an issue is found with the use of the library (e.g. a redefinition of the parameter multiplicity),
	 * a {@link CliOptionDefinitionException} is thrown.
	 * 
	 * @param annotation the annotation under consideration
	 * @param options the {@link OptionMap} instance
	 * @throws CliOptionDefinitionException in case an issue is found with the use of the library
	 */
	public void apply(final Annotation annotation, final OptionMap options) throws CliOptionDefinitionException {
		this.applier.apply(annotation, options);
	}
	
	@FunctionalInterface
	private interface OptionAnnotationApplier {
		
		void apply(final Annotation annotation, final OptionMap options) throws CliOptionDefinitionException;
		
	}

}
