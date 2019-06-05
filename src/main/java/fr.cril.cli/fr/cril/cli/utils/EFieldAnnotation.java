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
import java.lang.reflect.Field;
import java.util.Arrays;

import fr.cril.cli.CliOptionDefinitionException;
import fr.cril.cli.annotations.Args;
import fr.cril.cli.annotations.Description;
import fr.cril.cli.annotations.LongName;
import fr.cril.cli.annotations.Param;
import fr.cril.cli.annotations.Required;
import fr.cril.cli.annotations.ShortName;

/**
 * An enumeration of all supported annotations related to fields used to define CLI arguments.
 * 
 * It is used to apply the annotation logic on {@link OptionMap} instance:
 * <ol>
 * <li>call {@link EFieldAnnotation#hasForClass(Class)} to check if an annotation belongs to the user library;</li>
 * <li>call {@link EFieldAnnotation#forClass(Class)} to get the enumeration constant related to a user library annotation;</li>
 * <li>call {@link EFieldAnnotation#apply(Field, Annotation, OptionMap)} to apply the annotation logic on the {@link OptionMap} instance.</li>
 * </ol>
 * 
 * @author Emmanuel Lonca - lonca@cril.fr
 */
public enum EFieldAnnotation {
	
	/** enumeration constant related to options short names */
	SHORT_NAME(ShortName.class, (f, a, o) -> o.setShortName(f, ((ShortName) a).value())),
	
	/** enumeration constant related to options long names */
	LONG_NAME(LongName.class, (f, a, o) -> o.setLongName(f, ((LongName) a).value())),
	
	/** enumeration constant related to options parameter multiplicities */
	OPT_ARG_MULTIPLICITY(Args.class, (f, a, o) -> o.setMultiplicity(f, ((Args) a).value())),
	
	/** enumeration constant related to the <code>required</code> flag */
	REQUIRED(Required.class, (f, a, o) -> o.setRequired(f, ((Required) a).value())),
	
	/** enumeration constant related to parameters (non option arguments) */
	PARAM(Param.class, (f, a, o) -> o.setParam(f, ((Param) a).value())),
	
	/** enumeration constant related to parameters (non option arguments) */
	DESCRIPTION(Description.class, (f, a, o) -> o.setDescription(f, ((Description) a).value()));
	
	private final Class<? extends Annotation> annotationCl;
	
	private final OptionAnnotationApplier applier;
	
	private EFieldAnnotation(final Class<? extends Annotation> annotationCl, final OptionAnnotationApplier applier) {
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
	public static EFieldAnnotation forClass(final Class<? extends Annotation> cl) {
		for(final EFieldAnnotation a : values()) {
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
	 * Given an annotated field, the annotation under consideration and an {@link OptionMap} instance,
	 * the {@link OptionMap} is modified to reflect the fact the field is annotated.
	 * 
	 * The provided annotation must annotate the provided field (no check of this fact is made by this method).
	 * 
	 * In case an issue is found with the use of the library (e.g. two uses of the same short name),
	 * a {@link CliOptionDefinitionException} is thrown.
	 * 
	 * @param field the annotated field
	 * @param annotation the annotation under consideration
	 * @param options the {@link OptionMap} instance
	 * @throws CliOptionDefinitionException in case an issue is found with the use of the library
	 */
	public void apply(final Field field, final Annotation annotation, final OptionMap options) throws CliOptionDefinitionException {
		this.applier.apply(field, annotation, options);
	}
	
	@FunctionalInterface
	private interface OptionAnnotationApplier {
		
		void apply(final Field field, final Annotation annotation, final OptionMap options) throws CliOptionDefinitionException;
		
	}

}
