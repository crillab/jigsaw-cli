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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import fr.cril.cli.CliUsageException;

/**
 * An enumeration used to cast string values according to a field type,
 * and associate this value to the field.
 * 
 * @author Emmanuel Lonca - lonca@cril.fr
 */
public enum EFieldType {
	
	/** handles the {@link Boolean} class; always sets the value of the flag to <code>true</code> */
	BOOLEAN(Boolean.class, 0, EFieldType::applyBool),
	
	/** handles the <code>boolean</code> primitive type; always sets the value of the flag to <code>true</code> */
	BOOL(boolean.class, 0, EFieldType::applyBool),
	
	/** handles the {@link Boolean} class; sets the value according to a string parameter (see {@link EFieldType#setBooleanConstants(String[], String[])}) */
	BOOLEAN_ARG(Boolean.class, 1, EFieldType::applyBoolArg),
	
	/** handles the <code>boolean</code> primitive type; sets the value according to a string parameter (see {@link EFieldType#setBooleanConstants(String[], String[])}) */
	BOOL_ARG(boolean.class, 1, EFieldType::applyBoolArg),
	
	/** handles the {@link String} type; just copy the first parameter */
	STRING(String.class, 1, EFieldType::applyString),
	
	/** handles the {@link Integer} class; casts the first parameter using {@link Integer#parseInt(String)} */
	INTEGER(Integer.class, 1, EFieldType::applyInt),
	
	/** handles the <code>int</code> primitive type; casts the first parameter using {@link Integer#parseInt(String)} */
	INT(int.class, 1, EFieldType::applyInt),
	
	/** handles the {@link Long} class; casts the first parameter using {@link Long#parseLong(String)} */
	LONG(Long.class, 1, EFieldType::applyLong),
	
	/** handles the <code>long</code> primitive type; casts the first parameter using {@link Long#parseLong(String)} */
	LG(long.class, 1, EFieldType::applyLong);
	
	private final Class<?> cl;
	
	private final int multiplicity;
	
	private final OptParamApplier applier;
	
	private static final String[] DEFAULT_BOOL_FALSE_STR = {"false"};
	
	private static String[] boolFalseStr = DEFAULT_BOOL_FALSE_STR;
	
	private static final String[] DEFAULT_BOOL_TRUE_STR = {"true"};
	
	private static String[] boolTrueStr = DEFAULT_BOOL_TRUE_STR;

	private EFieldType(final Class<?> cl, final int multiplicity, final OptParamApplier applier) {
		this.cl = cl;
		this.multiplicity = multiplicity;
		this.applier = applier;
	}
	
	/**
	 * Retrieves the {@link EFieldType} constant corresponding to the provided field class and the multiplicity.
	 * 
	 * If no such {@link EFieldType} constant exists, an {@link IllegalArgumentException} is thrown.
	 * 
	 * @param cl the field class
	 * @param multiplicity the multiplicity 
	 * @return the corresponding {@link EFieldType} constant
	 */
	public static EFieldType forClass(final Class<?> cl, final int multiplicity) {
		for(final EFieldType type : EFieldType.values()) {
			if(type.cl.equals(cl) && type.multiplicity == multiplicity) {
				return type;
			}
		}
		throw new IllegalArgumentException("no enum constant for class "+cl);
	}
	
	/**
	 * Given a list of string parameters, associates the corresponding value to a field.
	 * 
	 * Depending on the {@link EFieldType} constant under consideration, the number of parameters (i.e. the size of the list of parameters)
	 * may vary. In case the number of parameters is different than expected, an {@link IllegalArgumentException} is thrown.
	 * 
	 * The algorithm used to translate strings into the field type depends on the field type and may require some properties.
	 * In case such properties are not satisfied, a {@link CliUsageException} is thrown.
	 * 
	 * For some field types, there are different translation algorithms, depending on the multiplicity associated with the enumeration constant.
	 * As an example, for Boolean types:
	 * <ul>
	 * <li>for a multiplicity of 0 (no parameters), the field value is set to {@link Boolean#TRUE};</li>
	 * <li>for a multiplicity of 1, the field value is set according to the string value (by default, "true" and "false" are accepted).</li>
	 * </ul>
	 * 
	 * @param field the field which value must to set
	 * @param obj the object which the field value is to set
	 * @param params the list of string parameters
	 * @throws CliUsageException if the string values are incorrect for this enumeration constant
	 */
	public void apply(final Field field, final Object obj, final List<String> params) throws CliUsageException {
		this.applier.apply(field, obj, params);
	}
	
	private static void applyBool(final Field field, final Object obj, final List<String> params) throws CliUsageException {
		apply(field, obj, computeValue(new Multiplicity(0), l -> Boolean.TRUE, params));
	}
	
	private static void applyBoolArg(final Field field, final Object obj, final List<String> params) throws CliUsageException {
		apply(field, obj, computeValue(new Multiplicity(1), l -> {
			if(Arrays.stream(boolFalseStr).anyMatch(l.get(0)::equals)) {
				return Boolean.FALSE;
			}
			if(Arrays.stream(boolTrueStr).anyMatch(l.get(0)::equals)) {
				return Boolean.TRUE;
			}
			throw new CliUsageException("\""+l.get(0)+"\" cannot be converted to Boolean");
		}, params));
	}
	
	private static void applyString(final Field field, final Object obj, final List<String> params) throws CliUsageException {
		apply(field, obj, computeValue(new Multiplicity(1), l -> l.get(0), params));
	}
	
	private static void applyInt(final Field field, final Object obj, final List<String> params) throws CliUsageException {
		apply(field, obj, computeValue(new Multiplicity(1), l -> {
			try {
				return Integer.parseInt(l.get(0));
			} catch (NumberFormatException e) {
				throw new CliUsageException("expected an integer, found \""+params.get(0)+"\"");
			}
		}, params));
	}
	
	private static void applyLong(final Field field, final Object obj, final List<String> params) throws CliUsageException {
		apply(field, obj, computeValue(new Multiplicity(1), l -> {
			try {
				return Long.parseLong(l.get(0));
			} catch (NumberFormatException e) {
				throw new CliUsageException("expected an integer, found \""+params.get(0)+"\"");
			}
		}, params));
	}
	
	private static Object computeValue(final Multiplicity expectedMultiplicity, final OptParamComputer valueComputer, final List<String> params) throws CliUsageException {
		if(params == null || params.size() < expectedMultiplicity.getMin() || params.size() > expectedMultiplicity.getMax()) {
			throw new IllegalArgumentException();
		}
		return valueComputer.compute(params);
	}
	
	private static void apply(final Field field, final Object obj, final Object value) {
		final boolean canAccess = field.canAccess(obj);
		field.setAccessible(true);
		try {
			field.set(obj, value);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new IllegalArgumentException(e);
		}
		field.setAccessible(canAccess);
	}
	
	/**
	 * Changes the set of string literals seen as Boolean constants.
	 * 
	 * By default, the only recognized values are "true" and "false".
	 * The new values are passed as two string arrays (the first one containing the values which stand for <code>false</code>,
	 * the second one containing the values which stand for <code>true</code>).
	 * 
	 * Both arrays must be non null, contain at least one string, and strings must be non-null and nonempty.
	 * No string can be shared by the two arrays.
	 * 
	 * @param falseValues the false values
	 * @param trueValues the true values
	 */
	public static void setBooleanConstants(final String[] falseValues, final String[] trueValues) {
		if(
			falseValues == null || trueValues == null || falseValues.length == 0 || trueValues.length == 0 ||
			Arrays.stream(falseValues).anyMatch(Objects::isNull) || Arrays.stream(trueValues).anyMatch(Objects::isNull) ||
			Arrays.stream(falseValues).anyMatch(String::isEmpty) || Arrays.stream(trueValues).anyMatch(String::isEmpty) ||
			Arrays.stream(falseValues).anyMatch(f -> Arrays.stream(trueValues).anyMatch(f::equals))
		) {
			throw new IllegalArgumentException("unexpected values provided as Boolean constants (got \""+Arrays.toString(falseValues)+"\" and \""+Arrays.toString(trueValues)+"\")");
		}
		EFieldType.boolFalseStr = Arrays.copyOf(falseValues, falseValues.length);
		EFieldType.boolTrueStr = Arrays.copyOf(trueValues, falseValues.length);
	}
	
	/**
	 * Resets the set of string literals seen as Boolean constants.
	 * 
	 * See {@link EFieldType#setBooleanConstants(String[], String[])}.
	 */
	public static void resetBooleanConstants() {
		boolFalseStr = DEFAULT_BOOL_FALSE_STR;
		boolTrueStr = DEFAULT_BOOL_TRUE_STR;
	}
	
	@FunctionalInterface
	private interface OptParamApplier {
		
		void apply(final Field field, final Object obj, final List<String> params) throws CliUsageException;
	}
	
	@FunctionalInterface
	private interface OptParamComputer {
		
		Object compute(final List<String> params) throws CliUsageException;
	}

}
