package fr.cril.cli.annotations;

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

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import fr.cril.cli.utils.Multiplicity;

/**
 * An annotation used to define the allowed number of arguments (except options) allowed in the CLI arguments.
 * 
 * The interval is given by an string; see {@link Multiplicity#Multiplicity(String)} for details.
 * 
 * This annotations must annotate a class.
 * If <code>n</code> is the upper bound of the interval, the class may declare <code>n</code> fields as parameters using the {@link Param} annotation.
 * 
 * The default value for this annotation is <i>exactly zero arguments</i>.
 * 
 * @author Emmanuel Lonca - lonca@cril.fr
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface Params {
	
	String value() default "0..0";

}
