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

import java.util.Objects;

/**
 * A class used to handle argument multiplicity of CLI arguments.
 * 
 * Multiplicity is the number of command line arguments allowed, except the options.
 * It is internally represented as an integer interval.
 * 
 * @author Emmanuel Lonca - lonca@cril.fr
 */
public class Multiplicity {
	
	private final int min;
	
	private final int max;

	/**
	 * Builds an instance of {@link Multiplicity} given the allowed interval for the number of arguments.
	 * 
	 * The interval is given by its lower and upper bounds. Both bounds are inclusive.
	 * 
	 * @param min the minimal number of arguments allowed
	 * @param max the maximal number of arguments allowed
	 */
	public Multiplicity(final int min, final int max) {
		this.min = min;
		this.max = max;
		checkBounds();
	}
	
	/**
	 * Builds an instance of {@link Multiplicity} given a string representing the allowed interval for the number of arguments.
	 * 
	 * The interval must follow the pattern <code>[x..y]</code> where:
	 * <ul>
	 * <li><code>x</code> is an integer;</li>
	 * <li><code>y</code> is an integer or <code>*</code> (which means {@link Integer#MAX_VALUE});</li>
	 * <li><code>y</code> is greater than or equal to <code>x</code> and must be positive or null.
	 * </ul>
	 * 
	 * In case the interval is invalid, an {@link IllegalArgumentException} is thrown.
	 * 
	 * @param interval the interval, as a string
	 */
	public Multiplicity(final String interval) {
		try {
			final String[] bounds = interval.split("\\.\\.");
			this.min = Integer.parseInt(bounds[0]);
			this.max = "*".equals(bounds[1]) ? Integer.MAX_VALUE : Integer.parseInt(bounds[1]);
			checkBounds();
		} catch(NullPointerException | ArrayIndexOutOfBoundsException | NumberFormatException e) {
			throw new IllegalArgumentException("invalid argument multiplicity pattern: "+interval);
		}
	}
	
	/**
	 * Builds an instance of {@link Multiplicity} for an interval containing only one value.
	 * 
	 * @param exactly the only value of the interval
	 */
	public Multiplicity(final int exactly) {
		this(exactly, exactly);
	}

	private void checkBounds() {
		if(this.max < this.min || this.max < 0) {
			throw new IllegalArgumentException("invalid bounds: min="+this.min+" max="+this.max);
		}
	}
	
	/**
	 * Returns the minimal number of arguments allowed.
	 * 
	 * @return the minimal number of arguments allowed
	 */
	public int getMin() {
		return this.min;
	}
	
	/**
	 * Returns the maximal number of arguments allowed.
	 * 
	 * @return the maximal number of arguments allowed
	 */
	public int getMax() {
		return this.max;
	}

	@Override
	public final int hashCode() {
		return Objects.hash(max, min);
	}

	@Override
	public final boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Multiplicity)) {
			return false;
		}
		final Multiplicity other = (Multiplicity) obj;
		return max == other.max && min == other.min;
	}
	
	@Override
	public String toString() {
		return "["+this.min+".."+(this.max == Integer.MAX_VALUE ? "*" : this.max)+"]";
	}
	
}
