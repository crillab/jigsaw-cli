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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

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
	
	private final Map<Character, Field> shortOpts = new HashMap<>();
	
	private final Map<String, Field> longOpts = new HashMap<>();
	
	private final Map<Field, Integer> multiplicities = new HashMap<>();
	
	private final Map<Field, Boolean> required = new HashMap<>();
	
	private static final int DEFAULT_OPT_MULT = 0;
	
	private Multiplicity paramMultiplicity = null;
	
	private final List<Field> parameters = new ArrayList<>();
	
	private static final Multiplicity DEFAULT_PARAM_MULTIPLICITY = new Multiplicity(0);
	
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
	public void setShortName(final Field field, final char shortName) throws CliOptionDefinitionException {
		if(!Character.isLetterOrDigit(shortName)) {
			throw new CliOptionDefinitionException(field+": short option character \""+shortName+"\" is not a letter or digit");
		}
		if(this.shortOpts.containsKey(shortName)) {
			throw new CliOptionDefinitionException(field+": short option \""+shortName+"\" used more than once");
		}
		this.shortOpts.put(shortName, field);
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
	public Field getField(final char shortName) throws CliUsageException {
		final Field f = this.shortOpts.get(shortName);
		if(f == null) {
			throw new CliUsageException("no field linked to short option \""+shortName+"\"");
		}
		return f;
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
		if(longName == null) {
			throw new CliOptionDefinitionException(field+": long option string is null");
		}
		if(longName.isEmpty()) {
			throw new CliOptionDefinitionException(field+": long option string is empty");
		}
		final String longOptStr = ": long option string \"";
		if(!Character.isLetterOrDigit(longName.charAt(0))) {
			throw new CliOptionDefinitionException(field+longOptStr+longName+"\" must start with a letter");
		}
		if(longName.chars().anyMatch(OptionMap::isForbiddenInOptionNames)) {
			throw new CliOptionDefinitionException(field+longOptStr+longName+"\" contains a character which is not a lettre or a digit");
		}
		if(this.longOpts.containsKey(longName)) {
			throw new CliOptionDefinitionException(field+longOptStr+longName+"\" is already in use by "+this.longOpts.get(longName));
		}
		this.longOpts.put(longName, field);
	}
	
	/**
	 * Returns the field associated to the provided long name.
	 * 
	 * @param longName a long name
	 * @return the field associated to the long name
	 * @throws CliUsageException if no field is associated to this long name
	 */
	public Field getField(final String longName) throws CliUsageException {
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
		if(this.required.containsKey(field)) {
			throw new CliOptionDefinitionException(field+": multiple occurrences of the required flag");
		}
		this.required.put(field, value);
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
		int nParams = this.parameters.size();
		final Optional<String> namedParams = namedIn(this.parameters);
		if(namedParams.isPresent()) {
			throw new CliOptionDefinitionException("the following fields are both set as parameters and named: "+namedParams);
		}
		final Multiplicity mult = getParamMultiplicity();
		if(nParams > mult.getMax()) {
			throw new CliOptionDefinitionException("number of declared parameters does not match the max parameter multiplicity ("+nParams+" parameters for a multiplicity of "+mult+")");
		}
	}
	
	private Optional<String> unnamedIn(final Collection<Field> fields) {
		return fields.stream().filter(f -> !this.shortOpts.containsValue(f)).filter(f -> !this.longOpts.containsValue(f)).map(Object::toString).reduce((a,b) -> a+","+b);
	}
	
	private Optional<String> namedIn(final Collection<Field> fields) {
		return fields.stream().filter(f -> this.shortOpts.containsValue(f) || this.longOpts.containsValue(f)).map(Object::toString).reduce((a,b) -> a+","+b);
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
		final Optional<Character> shortName = this.shortOpts.entrySet().stream().filter(e -> e.getValue().equals(field)).map(Entry::getKey).findAny();
		final Optional<String> longName = this.longOpts.entrySet().stream().filter(e -> e.getValue().equals(field)).map(Entry::getKey).findAny();
		if(shortName.isPresent()) {
			final String shortNameStr = "-"+shortName.get();
			if(longName.isPresent()) {
				return "--"+longName.get()+" ("+shortNameStr+")";
			}
			return shortNameStr;
		} else {
			if(longName.isPresent()) {
				return "--"+longName.get();
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
	
}
