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

/**
 * An exception thrown while an issue is discovered in the <b>definition</b> of the options (i.e. when looking at the option annotations).
 * 
 * @author Emmanuel Lonca - lonca@cril.fr
 */
public class CliOptionDefinitionException extends Exception {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Builds a new exception given its reason.
	 * 
	 * @param reason the reason
	 */
	public CliOptionDefinitionException(final String reason) {
		super(reason);
	}

}
