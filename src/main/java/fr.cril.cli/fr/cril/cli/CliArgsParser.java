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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

import fr.cril.cli.utils.EFieldType;
import fr.cril.cli.utils.Multiplicity;
import fr.cril.cli.utils.OptionMap;

/**
 * The class used to parse command line arguments and make the field associations taking advantage of a {@link ClassParser}.
 * 
 * @author Emmanuel Lonca - lonca@cril.fr
 */
public class CliArgsParser<T> {
	
	private final ClassParser<T> optParser;
	
	private final List<String> parameters = new ArrayList<>();
	
	private final List<Field> seen = new ArrayList<>();

	/**
	 * Builds a new CLI arguments parser using a {@link ClassParser}.
	 * 
	 * @param optParser
	 */
	public CliArgsParser(final ClassParser<T> optParser) {
		this.optParser = optParser;
	}
	
	/**
	 * Parses the CLI arguments and associates the values to the fields as described by the {@link ClassParser}.
	 * The object on which the fields should be updated must be passed in addition to the CLI arguments.
	 * 
	 * The class parser is invoked by this method; if an error is found at this step, a {@link CliOptionDefinitionException} is thrown.
	 * Then, the arguments themselves are parsed. At this step, an error in the usage implies a {@link CliUsageException} is thrown.
	 * 
	 * This functions must be called prior to any other interrogation function, like {@link CliArgsParser#getParameters()}.
	 * 
	 * @param obj the object on which the fields should be updated
	 * @param argArray the CLI arguments
	 * @throws CliUsageException if there is a error in the definition of the options (usage of the annotations)
	 * @throws CliOptionDefinitionException if the CLI arguments are invalid for the {@link ClassParser} (arguments given by the final user)
	 */
	public void parse(final Object obj, final String[] argArray) throws CliUsageException, CliOptionDefinitionException {
		final OptionMap optionMap = this.optParser.parse();
		this.parameters.clear();
		this.seen.clear();
		final Queue<String> args = new LinkedList<>(Arrays.asList(argArray));
		while(!args.isEmpty()) {
			final String arg = args.poll();
			if("--".equals(arg)) {
				break;
			}
			if(arg.startsWith("--")) {
				parseLongNamedOpt(obj, optionMap, arg, args);
			} else if(arg.startsWith("-")) {
				parseShortNamedOpt(obj, optionMap, arg, args);
			} else {
				this.parameters.add(arg);
			}
		}
		this.parameters.addAll(args);
		checkRequired(optionMap);
		parseParams(obj, optionMap);
	}

	private void parseShortNamedOpt(final Object obj, final OptionMap optionMap, final String current, final Queue<String> others) throws CliUsageException {
		final String cur = current.substring(1);
		switch(cur.length()) {
		case 0:
			throw new CliUsageException("empty option: \"-\"");
		case 1:
			parseShortNamedOpt(obj, optionMap, cur.charAt(0), others);
			break;
		default:
			final Queue<String> emptyQueue = new LinkedList<>();
			for(int i=0; i<cur.length(); ++i) {
				parseShortNamedOpt(obj, optionMap, (char) cur.charAt(i), emptyQueue);
			}
		}
	}

	private void parseShortNamedOpt(final Object obj, final OptionMap optionMap, final char current, final Queue<String> others) throws CliUsageException {
		final Field field = optionMap.getField(current);
		readFieldParams(obj, optionMap, field, Character.toString(current), others);
	}

	private void readFieldParams(final Object obj, final OptionMap optionMap, final Field field, final String optName, final Queue<String> others) throws CliUsageException {
		final int multiplicity = optionMap.getArgMultiplicity(field);
		final List<String> optParams = new ArrayList<>(multiplicity);
		for(int i=0; i<multiplicity; ++i) {
			if(others.isEmpty()) {
				throw new CliUsageException("not enough parameters for option \""+optName+"\" (expected "+multiplicity+")");
			}
			optParams.add(others.poll());
		}
		EFieldType.forClass(field.getType(), multiplicity).apply(field, obj, optParams);
		this.seen.add(field);
	}

	private void parseLongNamedOpt(final Object obj, final OptionMap optionMap, final String current, final Queue<String> others) throws CliUsageException {
		final String cur = current.substring(2);
		final Field field = optionMap.getField(cur);
		readFieldParams(obj, optionMap, field, cur, others);
	}
	
	private void checkRequired(final OptionMap optionMap) throws CliUsageException {
		final Optional<String> required = optionMap.getRequiredFields().stream().filter(f -> !this.seen.contains(f)).map(optionMap::fieldToString).reduce((a,b) -> a+", "+b);
		if(required.isPresent()) {
			throw new CliUsageException("the following fields have no value (although they have to): "+required.get());
		}
	}
	
	private void parseParams(final Object obj, final OptionMap optionMap) throws CliUsageException {
		final int nAdditional = this.parameters.size();
		final Multiplicity paramMult = optionMap.getParamMultiplicity();
		if(nAdditional < paramMult.getMin() || nAdditional > paramMult.getMax()) {
			throw new CliUsageException("wrong parameter provided: expected between "+paramMult.getMin()+" and "+paramMult.getMax());
		}
		for(int i=0; i<Math.min(nAdditional, optionMap.nParams()); ++i) {
			final Field field = optionMap.getParamField(i);
			if(field == null) {
				continue;
			}
			EFieldType.forClass(field.getType(), 1).apply(field, obj, Collections.singletonList(this.parameters.get(i)));
		}
	}
	
	/**
	 * Returns the list of the parameters (i.e. non-option arguments in the CLI arguments)
	 * 
	 * @return the list of the parameters
	 */
	public List<String> getParameters() {
		return Collections.unmodifiableList(this.parameters);
	}

}
