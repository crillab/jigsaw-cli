package fr.cril.cli.utils;

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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import fr.cril.cli.CliOptionDefinitionException;
import fr.cril.cli.CliUsageException;
import fr.cril.cli.annotations.Args;
import fr.cril.cli.annotations.LongName;
import fr.cril.cli.annotations.Required;
import fr.cril.cli.annotations.ShortName;

/**
 * A class used to register all the defined CLI options and their properties (related field, argument multiplicity, ...).
 * 
 * Most methods of this class performs some checks of the use of the user library and throw a {@link CliOptionDefinitionException} if an error is detected,
 * like two declarations of the same long name. However, some checks cannot be done "on the fly"; at the end of the option registration phase,
 * you must call {@link OptionMap#sanityChecks()} to perform them.
 * 
 * @author Emmanuel Lonca - lonca@cril.fr
 */
public class OptionMap {
	
	private final Map<String, Field> shortOpts = new HashMap<>();
	
	private final Map<Field, String> revShortOpts = new HashMap<>();
	
	private final Map<String, Field> longOpts = new HashMap<>();
	
	private final Map<Field, String> revLongOpts = new HashMap<>();
	
	private final Map<Field, Integer> multiplicities = new HashMap<>();
	
	private final Map<Field, Boolean> required = new HashMap<>();
	
	private final Map<Field, String> descriptions = new HashMap<>();
	
	private static final int DEFAULT_OPT_MULT = 0;
	
	private static final String DEFAULT_DESCRIPTION = "";
	
	private Multiplicity paramMultiplicity = null;
	
	private final List<Field> parameters = new ArrayList<>();
	
	private static final Multiplicity DEFAULT_PARAM_MULTIPLICITY = new Multiplicity(0);
	
	private boolean allowShortNamesMerging = true;
	
	/**
	 * Associates a short name to a field.
	 * 
	 * In case this short name is already in use (even if it is associated to the same field than the one provided),
	 * a {@link CliOptionDefinitionException} is thrown.
	 * 
	 * @param field the field
	 * @param shortName the short name
	 * @throws CliOptionDefinitionException in case this short name is already in use
	 */
	public void setShortName(final Field field, final String shortName) throws CliOptionDefinitionException {
		checkNullField(field);
		try {
			checkOptionName(shortName);
		} catch(CliOptionDefinitionException e) {
			throw new CliOptionDefinitionException(field.toString()+": short "+e.getMessage());
		}
		if(this.shortOpts.containsKey(shortName)) {
			throw new CliOptionDefinitionException(field+": short option \""+shortName+"\" used more than once");
		}
		if(revShortOpts.containsKey(field)) {
			throw new CliOptionDefinitionException(field+": multiple short names");
		}
		this.shortOpts.put(shortName, field);
		this.revShortOpts.put(field, shortName);
	}
	
	/**
	 * Returns the field associated to the provided short name.
	 * 
	 * In case there is no such field, a {@link CliUsageException} is thrown.
	 * 
	 * @param shortName a short name
	 * @return the field associated to the short name
	 * @throws CliUsageException if no field is associated to this short name
	 */
	public Field getFieldByShortName(final String shortName) throws CliUsageException {
		if(shortName == null) {
			throw new IllegalArgumentException();
		}
		final Field f = this.shortOpts.get(shortName);
		if(f == null) {
			throw new CliUsageException("no short option \"-"+shortName+"\"");
		}
		return f;
	}
	
	/**
	 * Returns <code>true</code> iff an option with the provided short name exists.
	 * 
	 * @param shortName the short name
	 * @return <code>true</code> iff an option with the provided short name exists
	 */
	public boolean hasShortName(final String shortName) {
		if(shortName == null) {
			throw new IllegalArgumentException();
		}
		return this.shortOpts.containsKey(shortName);
	}
	
	/**
	 * Associated a long name to a field.
	 * 
	 * In case this long name is already in use (even if it is associated to the same field than the one provided),
	 * a {@link CliOptionDefinitionException} is thrown.
	 * 
	 * @param field the field
	 * @param longName the long name
	 * @throws CliOptionDefinitionException in case this long name is already in use
	 */
	public void setLongName(final Field field, final String longName) throws CliOptionDefinitionException {
		checkNullField(field);
		try {
			checkOptionName(longName);
		} catch(CliOptionDefinitionException e) {
			throw new CliOptionDefinitionException(field.toString()+": long "+e.getMessage());
		}
		if(this.longOpts.containsKey(longName)) {
			throw new CliOptionDefinitionException(field+": long option name \""+longName+"\" is already in use by "+this.longOpts.get(longName));
		}
		if(this.revLongOpts.containsKey(field)) {
			throw new CliOptionDefinitionException(field+": multiple long names");
		}
		this.longOpts.put(longName, field);
		this.revLongOpts.put(field, longName);
	}

	private void checkOptionName(final String name) throws CliOptionDefinitionException {
		if(name == null) {
			throw new CliOptionDefinitionException("option name is null");
		}
		if(name.isEmpty()) {
			throw new CliOptionDefinitionException("option name is empty");
		}
		if(!Character.isLetterOrDigit(name.charAt(0))) {
			throw new CliOptionDefinitionException("option name \""+name+"\" must start with a letter or a digit");
		}
		if(name.chars().anyMatch(OptionMap::isForbiddenInOptionNames)) {
			throw new CliOptionDefinitionException("option name \""+name+"\" contains a character which is not a letter, a digit or an hyphen");
		}
	}
	
	/**
	 * Returns the field associated to the provided long name.
	 * 
	 * @param longName a long name
	 * @return the field associated to the long name
	 * @throws CliUsageException if no field is associated to this long name
	 */
	public Field getFieldByLongName(final String longName) throws CliUsageException {
		if(longName == null) {
			throw new IllegalArgumentException();
		}
		final Field f = this.longOpts.get(longName);
		if(f == null) {
			throw new CliUsageException("no field linked to long option \""+longName+"\"");
		}
		return f;
	}
	
	private static final boolean isForbiddenInOptionNames(final int c) {
		return !(Character.isLetterOrDigit(c) || c == '-');
	}
	
	/**
	 * Sets the parameter multiplicity of a CLI option associated to a field.
	 * 
	 * The multiplicity must be nonnegative, and defined at most once.
	 * The default multiplicity is zero (the option has no parameter).
	 * 
	 * Argument multiplicities must be associated to named fields (with short or long names).
	 * The check is not processed in this method, allowing to define the option name after the multiplicity;
	 * the check is done in the {@link OptionMap#sanityChecks()} method.
	 * 
	 * @param field the field associated to the CLI option
	 * @param multiplicity the multiplicity
	 * @throws CliOptionDefinitionException if the multiplicity is invalid or defined twice
	 */
	public void setMultiplicity(final Field field, final int multiplicity) throws CliOptionDefinitionException {
		checkNullField(field);
		if(multiplicity < 0) {
			throw new CliOptionDefinitionException("multiplicity must be a nonnegative integer");
		}
		if(this.multiplicities.containsKey(field)) {
			throw new CliOptionDefinitionException(field+": multiple definitions of multiplicity");
		}
		this.multiplicities.put(field, multiplicity);
	}
	
	/**
	 * Returns the multiplicity of an option given by its associated field.
	 * 
	 * In case no multiplicity was set, the default one (zero parameter) is returned.
	 * 
	 * @param field the field
	 * @return the related multiplicity, or the default one if none was set
	 */
	public int getArgMultiplicity(final Field field) {
		checkNullField(field);
		final Integer mult = this.multiplicities.get(field);
		return mult == null ? DEFAULT_OPT_MULT : mult;
	}
	
	/**
	 * Sets the requirement flag of an option given by its associated field.
	 * 
	 * An option which requirement flag is set to <code>true</code> must be given by the user.
	 * 
	 * The flag may be set only once. In case it is added twice, a {@link CliOptionDefinitionException} is thrown.
	 * 
	 * @param field the field
	 * @param value the value of the flag
	 * @throws CliOptionDefinitionException if the flag is set twice
	 */
	public void setRequired(final Field field, final boolean value) throws CliOptionDefinitionException {
		checkNullField(field);
		if(this.required.containsKey(field)) {
			throw new CliOptionDefinitionException(field+": multiple occurrences of the required flag");
		}
		this.required.put(field, value);
	}
	
	private void checkNullField(final Field field) {
		if(field == null) {
			throw new IllegalArgumentException("null field provided");
		}
	}

	/**
	 * Returns the value of the <code>required</code> flag of an option given by its associated field.
	 * 
	 * In case it has not been set, its value is <code>false</code>.
	 * 
	 * @param field the field
	 * @return the value of the requirement flag
	 */
	public boolean isRequired(final Field field) {
		checkNullField(field);
		final Boolean result = this.required.get(field);
		return result != null && result;
	}
	
	/**
	 * Returns a set containing the fields which have the <code>required</code> flag.
	 * 
	 * @return a set containing the fields which have the <code>required</code> flag
	 */
	public Set<Field> getRequiredFields() {
		return Collections.unmodifiableSet(this.required.keySet());
	}
	
	/**
	 * Checks is the option map is consistent. In case it is not, a {@link CliOptionDefinitionException} is thrown.
	 * 
	 * Most of the checks are processed during the modification of the {@link OptionMap}.
	 * However, some of them can only be realized when the option building phase is over; this method calls them.
	 * 
	 * @throws CliOptionDefinitionException if the option map is not consistent
	 */
	public void sanityChecks() throws CliOptionDefinitionException {
		final Optional<String> unnamedWithMultiplicities = unnamedIn(this.multiplicities.keySet());
		if(unnamedWithMultiplicities.isPresent()) {
			throw new CliOptionDefinitionException("the following fields have multiplicities but no name: "+unnamedWithMultiplicities.get());
		}
		final Optional<String> unnamedWithRequiredFlag = unnamedIn(this.required.keySet());
		if(unnamedWithRequiredFlag.isPresent()) {
			throw new CliOptionDefinitionException("the following fields have \"required\" flag but no name: "+unnamedWithRequiredFlag.get());
		}
		final Optional<String> unnamedWithDescription = unnamedIn(this.descriptions.keySet());
		if(unnamedWithDescription.isPresent()) {
			throw new CliOptionDefinitionException("the following fields have descriptions but no name: "+unnamedWithDescription.get());
		}
		int nParams = this.parameters.size();
		final Optional<String> namedParams = namedIn(this.parameters);
		if(namedParams.isPresent()) {
			throw new CliOptionDefinitionException("the following fields are both set as parameters and named: "+namedParams);
		}
		final Multiplicity mult = getParamMultiplicity();
		if(nParams > mult.getMax()) {
			throw new CliOptionDefinitionException("number of declared parameters does not match the max parameter multiplicity ("+nParams+" parameters for a multiplicity of "+mult+")");
		}
		if(this.allowShortNamesMerging) {
			final List<String> multicharShortNames = this.shortOpts.keySet().stream().filter(s -> s.length() > 1).collect(Collectors.toList());
			for(final String multicharShortName : multicharShortNames) {
				if(multicharShortName.chars().allMatch(c -> this.shortOpts.containsKey(Character.toString(c)))) {
					throw new CliOptionDefinitionException("amgiguity: \""+multicharShortName+"\" may be seen as the concatenation of single-charactered options");
				}
			}
		}
	}
	
	private Optional<String> unnamedIn(final Collection<Field> fields) {
		return fields.stream().filter(f -> !this.revShortOpts.containsKey(f)).filter(f -> !this.revLongOpts.containsKey(f)).map(Object::toString).reduce((a,b) -> a+","+b);
	}
	
	private Optional<String> namedIn(final Collection<Field> fields) {
		return fields.stream().filter(f -> this.revShortOpts.containsKey(f) || this.revLongOpts.containsKey(f)).map(Object::toString).reduce((a,b) -> a+","+b);
	}
	
	/**
	 * Returns a string describing an option using its name(s).
	 * 
	 * The option is given by the field.
	 * 
	 * @param field the field corresponding to the option
	 * @return the string describing the option
	 */
	public String fieldToString(final Field field) {
		final String shortName = this.revShortOpts.get(field);
		final String longName = this.revLongOpts.get(field);
		if(shortName != null) {
			final String shortNameStr = "-"+shortName;
			if(longName != null) {
				return "--"+longName+" ("+shortNameStr+")";
			}
			return shortNameStr;
		} else {
			if(longName != null) {
				return "--"+longName;
			}
			throw new IllegalArgumentException(field+": one of short or long name must be defined");
		}
	}
	
	/**
	 * Sets a field as a parameter.
	 * 
	 * A parameter cannot be annotated by {@link ShortName}, {@link LongName}, {@link Args} or {@link Required}. 
	 * 
	 * Each parameter must have a unique index (between <code>0</code> and <code>n-1</code> for <code>n</code> parameters).
	 * The order in which the parameters are declared does not mandatory follow their indexes.
	 * 
	 * Some checks are performed in this method (throwing a {@link CliOptionDefinitionException} if an error is found),
	 * but most of them are included in {@link OptionMap#sanityChecks()}.
	 * 
	 * @param field the field corresponding to the parameter
	 * @param paramIndex the parameter index.
	 * @throws CliOptionDefinitionException if an error is detected while setting the field as a parameter
	 */
	public void setParam(final Field field, final int paramIndex) throws CliOptionDefinitionException {
		checkNullField(field);
		while(this.parameters.size() < paramIndex+1) {
			this.parameters.add(null);
		}
		if(this.parameters.get(paramIndex) != null) {
			throw new CliOptionDefinitionException(field+": parameter index already in use (by "+this.parameters.get(paramIndex)+")");
		}
		this.parameters.set(paramIndex, field);
	}
	
	/**
	 * Returns the number of parameters which have been defined.
	 * 
	 * @return the number of parameters which have been defined
	 */
	public int nParams() {
		return this.parameters.size();
	}
	
	/**
	 * Returns the field associated with the parameter at the given index.
	 * 
	 * The index must be included between 0 (inclusive) and {@link OptionMap#nParams()} (exclusive).
	 * 
	 * @param paramIndex the parameter index
	 * @return the corresponding field
	 */
	public Field getParamField(final int paramIndex) {
		return this.parameters.get(paramIndex);
	}

	/**
	 * Sets the multiplicity of the command line parameters (except options).
	 * 
	 * The multiplicity is given as a string that must follow the format described at {@link Multiplicity#Multiplicity(String)}.
	 * 
	 * It is forbidden to redefine the multiplicity
	 * 
	 * @param multiplicity the multiplicity
	 * @throws CliOptionDefinitionException if the multiplicity is redefined by this call
	 */
	public void setParamMultiplicity(final String multiplicity) throws CliOptionDefinitionException {
		if(multiplicity == null) {
			throw new IllegalArgumentException();
		}
		if(this.paramMultiplicity != null) {
			throw new CliOptionDefinitionException("multiple definition of parameter multiplicity");
		}
		this.paramMultiplicity = new Multiplicity(multiplicity);
	}
	
	/**
	 * Returns the multiplicity of the command line parameters (except options).
	 * 
	 * In case it has not been define, the default value is returned (exactly zero parameters are allowed).
	 * 
	 * @return the multiplicity of the command line parameters
	 */
	public Multiplicity getParamMultiplicity() {
		return this.paramMultiplicity == null ? DEFAULT_PARAM_MULTIPLICITY : this.paramMultiplicity;
	}
	
	/**
	 * Sets the description associated to an option given by its field.
	 * This description is used while printing usage.
	 * 
	 * The description must be non null and nonempty.
	 * 
	 * A same field cannot be associated a description twice.
	 * 
	 * Descriptions must be associated to named fields (with short or long names).
	 * The check is not processed in this method, allowing to define the option name after the description;
	 * the check is done in the {@link OptionMap#sanityChecks()} method.
	 * 
	 * @param field the field
	 * @param description the description
	 * @throws CliOptionDefinitionException if a description is associated twice to a field
	 */
	public void setDescription(final Field field, final String description) throws CliOptionDefinitionException {
		checkNullField(field);
		if(description == null) {
			throw new CliOptionDefinitionException(field+": null description provided");
		}
		if(description.isEmpty()) {
			throw new CliOptionDefinitionException(field+": empty description provided");
		}
		if(this.descriptions.containsKey(field)) {
			throw new CliOptionDefinitionException(field+": multiple definition of the description");
		}
		this.descriptions.put(field, description);
	}
	
	/**
	 * Returns the description associated to an option, given by its field.
	 * 
	 * In case no description has been set, an empty one is returned.
	 * 
	 * @param field the field
	 * @return the description, or an empty string if none.
	 */
	public String getDescription(final Field field) {
		checkNullField(field);
		final String descr = this.descriptions.get(field);
		return descr == null ? DEFAULT_DESCRIPTION : descr;
	}
	
	/**
	 * Prints the option list and their description into the provided {@link PrintWriter}.
	 * 
	 * @param out the {@link PrintWriter}
	 */
	public void printOptionUsage(final PrintWriter out) {
		final List<Field> fields = Stream.concat(this.revShortOpts.keySet().stream(), this.revLongOpts.keySet().stream()).distinct().sorted((f1, f2) -> {
			final String short1 = this.revShortOpts.get(f1);
			final String name1 = short1 == null ? this.revLongOpts.get(f1) : short1;
			final String short2 = this.revShortOpts.get(f2);
			final String name2 = short2 == null ? this.revLongOpts.get(f2) : short2;
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
			final String shortOpt = this.revShortOpts.get(f);
			if(shortOpt != null) {
				matrix[i][0] = "-"+shortOpt;
			}
			final String longOpt = this.revLongOpts.get(f);
			if(longOpt != null) {
				matrix[i][1] = "--"+longOpt;
			}
			final Optional<String> args = IntStream.range(0, this.getArgMultiplicity(f)).mapToObj(j -> "<arg"+j+">").reduce((a,b) -> a+" "+b);
			if(args.isPresent()) {
				matrix[i][2] = args.get();
			}
			matrix[i][3] = this.descriptions.get(f);
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
		if(descr != null) {
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
	
	/**
	 * Allows short names merging in CLI arguments (<code>-ab</code> means <code>-a -b</code>).
	 * The merging is allowed only if the options take no parameter.
	 * 
	 * The default is <code>true</code>.
	 * 
	 * In case merging is allowed, a check is made to prevent ambiguity: declaring <code>-a</code>, <code>-b</code> and <code>-ab</code>
	 * will cause an exception to be thrown when {@link OptionMap#sanityChecks()} is called.
	 * Disabling merging allows the declaration of such option set.
	 * 
	 * @param allow <code>true</code> to allow
	 */
	public void allowShortNamesMerging(final boolean allow) {
		this.allowShortNamesMerging = allow;
	}

}
