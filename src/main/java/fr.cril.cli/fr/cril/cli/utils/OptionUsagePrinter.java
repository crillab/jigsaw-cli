package fr.cril.cli.utils;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
 * A class used to display the available options to the final user.
 * 
 * @author Emmanuel Lonca - lonca@cril.fr
 */
public class OptionUsagePrinter {
	
	private final OptionMap options;

	/**
	 * Builds a usage printer given the {@link OptionMap} instance under consideration.
	 * 
	 * @param options the options
	 */
	public OptionUsagePrinter(final OptionMap options) {
		this.options = options;
	}
	
	/**
	 * Prints the option list and their description into the provided {@link PrintWriter}.
	 * 
	 * @param out the {@link PrintWriter}
	 */
	public void print(final PrintWriter out) {
		final List<Field> fields = this.options.namedFields().stream().sorted((f1, f2) -> {
			final String short1 = this.options.getShortName(f1);
			final String name1 = short1 == null ? this.options.getLongName(f1) : short1;
			final String short2 = this.options.getShortName(f2);
			final String name2 = short2 == null ? this.options.getLongName(f2) : short2;
			final int ignCaseCmp = name1.toLowerCase().compareTo(name2.toLowerCase());
			return ignCaseCmp == 0 ? name1.compareTo(name2) : ignCaseCmp;
		}).collect(Collectors.toList());
		final String[][] matrix = buildWordMatrix(fields);
		printMatrix(out, fields, matrix);
		out.flush();
	}

	private String[][] buildWordMatrix(final List<Field> fields) {
		final String[][] matrix = new String[fields.size()][4];
		for(int i=0; i<fields.size(); ++i) {
			final Field f = fields.get(i);
			final String shortOpt = this.options.getShortName(f);
			if(shortOpt != null) {
				matrix[i][0] = "-"+shortOpt;
			}
			final String longOpt = this.options.getLongName(f);
			if(longOpt != null) {
				matrix[i][1] = "--"+longOpt;
			}
			final Optional<String> args = Arrays.stream(this.options.getArgNames(f)).map(s -> "<"+s+">").reduce((a,b) -> a+" "+b);
			if(args.isPresent()) {
				matrix[i][2] = args.get();
			}
			String descr = this.options.getDescription(f);
			if(this.options.isRequired(f)) {
				descr = descr.isEmpty() ? "[required]" : descr + " [required]";
			}
			matrix[i][3] = descr;
		}
		return matrix;
	}
	
	private void printMatrix(final PrintWriter out, final List<Field> fields, final String[][] matrix) {
		final int maxShortOptSize = IntStream.range(0, fields.size()).mapToObj(i -> matrix[i][0]).filter(Objects::nonNull).mapToInt(String::length).max().orElse(0);
		final int maxLongOptSize = IntStream.range(0, fields.size()).mapToObj(i -> matrix[i][1]).filter(Objects::nonNull).mapToInt(String::length).max().orElse(0);
		final int maxArgOptSize = IntStream.range(0, fields.size()).mapToObj(i -> matrix[i][2]).filter(Objects::nonNull).mapToInt(String::length).max().orElse(0);
		if(maxShortOptSize != 0) {
			if(maxLongOptSize != 0) {
				printMatrixBothShortAndLongOpts(out, matrix, maxShortOptSize, maxLongOptSize, maxArgOptSize);
			} else {
				printMatrixShortOptsOnly(out, matrix, maxShortOptSize, maxArgOptSize);
			}
		} else if(maxLongOptSize != 0) {
			printMatrixLongOptsOnly(out, matrix, maxLongOptSize, maxArgOptSize);
		}
		out.flush();
	}

	private void printMatrixShortOptsOnly(final PrintWriter out, final String[][] matrix, final int maxShortOptSize, final int maxArgOptSize) {
		for(int i=0; i<matrix.length; ++i) {
			final String format = " %"+maxShortOptSize+"s";
			out.printf(format, matrix[i][0]);
			printArgs(out, matrix, i, maxArgOptSize);
			printDescr(out, matrix, i);
		}
	}

	private void printArgs(final PrintWriter out, final String[][] matrix, final int fieldIndex, final int maxArgOptSize) {
		final String args = matrix[fieldIndex][2];
		if(maxArgOptSize > 0) {
			if(args == null) {
				out.print(IntStream.range(0, 1+maxArgOptSize).mapToObj(i -> " ").reduce((a,b) -> a+b).orElse(""));
			} else {
				final String format = " %-"+maxArgOptSize+"s";
				out.printf(format, args);
			}
		}
	}

	private void printDescr(final PrintWriter out, final String[][] matrix, final int fieldIndex) {
		final String descr = matrix[fieldIndex][3];
		if(!descr.isEmpty()) {
			out.printf("   %s", descr);
		}
		out.print('\n');
	}

	private void printMatrixLongOptsOnly(final PrintWriter out, final String[][] matrix, final int maxLongOptSize, final int maxArgOptSize) {
		for(int i=0; i<matrix.length; ++i) {
			final String format = " %-"+maxLongOptSize+"s";
			out.printf(format, matrix[i][1]);
			printArgs(out, matrix, i, maxArgOptSize);
			printDescr(out, matrix, i);
		}
	}

	private void printMatrixBothShortAndLongOpts(final PrintWriter out, final String[][] matrix, final int maxShortOptSize, final int maxLongOptSize, final int maxArgOptSize) {
		final String emptyShortOpt = IntStream.range(0, maxShortOptSize).mapToObj(i -> " ").reduce((a,b) -> a+b).orElse("");
		final String shortOptFormat = "%"+maxShortOptSize+"s";
		final String emptyLongOpt = IntStream.range(0, maxLongOptSize).mapToObj(i -> " ").reduce((a,b) -> a+b).orElse("");
		final String longOptFormat = "%-"+maxLongOptSize+"s";
		for(int i=0; i<matrix.length; ++i) {
			out.print(' ');
			final String shortOpt = matrix[i][0];
			out.print(shortOpt == null ? emptyShortOpt : String.format(shortOptFormat, shortOpt));
			final String longOpt = matrix[i][1];
			out.print(shortOpt != null && longOpt != null ? ',' : ' ');
			out.print(longOpt == null ? emptyLongOpt : String.format(longOptFormat, longOpt));
			printArgs(out, matrix, i, maxArgOptSize);
			printDescr(out, matrix, i);
		}
	}

}
