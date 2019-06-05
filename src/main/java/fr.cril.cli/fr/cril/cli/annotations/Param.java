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

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * An annotation used for non option arguments (parameters) in the command line.
 * 
 * Parameters have no name and are ordered according to their appearance in the command line.
 * A unique index must be provided for each of the <code>n</code> parameters, starting from <code>0</code> to <code>n-1</code>
 * 
 * The default index associated with the annotation is zero.
 * 
 * A parameter cannot be associated to a name, be set as required, or have arguments.
 * 
 * @author Emmanuel Lonca - lonca@cril.fr
 */
@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface Param {
	
	int value() default 0;

}
