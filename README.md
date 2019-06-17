# Jigsaw-CLI

Jigsaw-CLI is a Java command-line arguments parsing library based on annotations.
Contrary to most of this kind of library, Jigsaw-CLI implements the Java module system introduced in Java 9.

## Example of use

```java
@Params("0..*") // any number of parameters (excluding options) is allowed
public class MyAwesomeApp {
	
	@ShortName("n")
	@LongName("number")
	@Args(1)
	@Description("a number")
	@Required
	private int number;
	
	@LongName("text")
	@Args(1)
	@Description("an optional text")
	private String text = "default text";
	
	@ShortName("a")
	@Description("an optional flag")
	private boolean flag1;
	
	@ShortName("b")
	@Description("another optional flag")
	private boolean flag2;

	/* non-option parameters are stored here */
	private List<String> parameters;
	
	public static void main(final String args[]) {
		new MyAwesomeApp(args).launch();
	}
	
	private MyAwesomeApp(final String args[]) {
		final ClassParser<MyAwesomeApp> classParser = new ClassParser<>(MyAwesomeApp.class); // the annotation parser
		final CliArgsParser<MyAwesomeApp> argsParser = new CliArgsParser<>(classParser); // the CLI args parser
		try {
			argsParser.parse(this, args); // fields (number, text, flag1, flag2) are set here
			this.parameters = argsParser.getParameters(); // no-option arguments are stored in the parameter field here
		} catch (CliUsageException e) {
			System.out.println("error in the CLI arguments: "+e.getMessage());
			System.out.println("available options:");
			classParser.printOptionUsage(System.out);
			throw new IllegalArgumentException("error in the provided CLI arguments: "+e.getMessage());
		} catch (CliOptionDefinitionException e) {
			throw new IllegalArgumentException("error in the CLI arguments definition: "+e.getMessage());
		}
	}
	
	private void launch() {
		// main app method
	}

}
```

In this example, the following options are defined :

* `-n` (or `--number`) which takes one integer argument (this option is required),
* `--text` which takes one string argument (its default value is `"default text"`),
* `-a`, a Boolean flag which takes no argument (default value is `false`, the value is `true` if the option is set),
* `-b`, another Boolean flag.

Excluding these options, an arbitrary number of arguments is allowed (defined by the `@Params("0..*")` annotation).

Starting from the `launch()` method, the value of the options are accessed through the fields they are associated to (thanks to the annotations),
while the program arguments are accessed through the `parameters` list. If the provided CLI arguments does not follow the requirements (`-n`
must be provided with a argument that can be cast to an integer and `--text`, if set, must be followed by its value), a `CliUsageException` is thrown
in the `MyAwesomeApp` constructor and the program exits.

## Software requirements

Jigsaw-CLI requires Java 11. `module-info.java` files must require the `fr.cril.cli` module in order to use Jigsaw-CLI.

## Field Annotations

Options are associated to fields using annotations.
A field is considered an option as soon as an annotation from the Jigsaw-CLI library annotates it.
Any option must be given a name.

### Option names

Options must be given at least one short or one long name (and may be given both).
It is forbidden to associate more than one short (or long) name to a single option
or to associate the same short (or long) name to more than one option. A same string may be given for both a short and a long name.

Short names are given by `@ShortName` annotations which values are non-empty string constants made of letters, digits and hyphens (the first character cannot be an hyphen).
Short-named options (e.g. `f`) are set when a CLI argument is the concatenation of an hyphen and the short name (`-f` for the short name `f`).

Short names may contain an arbitrary number of characters (except zero). In case they are single-charactered, they can be concatenated in the CLI arguments:
if both `-b` and `-f` are valid CLI options, `-bf` (or `-fb`) will set both options (except if `-fb` or `-bf` is itself a short name associated to an option;
see the "Short names disambiguation" section below). Merged options cannot have arguments.

Long names are given by `@LongName` annotations which values are non-empty string constants made of letters, digits and hyphens (the first character cannot be an hyphen).
Long-named options (e.g. `f`) are set when a CLI argument is the concatenation of two hyphens and the long name (`--foo` for the long name `foo`).
Long names may contain an arbitrary number of characters (except zero).

The special empty long option `--` specifies the end of the options. Any argument appearing after it in the CLI arguments will be treated as a program parameter.


### Option arguments

Options may themselves have arguments. The number of arguments is fixed by adding the `@Args` annotation to the field under consideration.
Depending on the type of the field, only some values are allowed for `@Args`: see the "Field types" section below for more information.
The default number of arguments is zero.

Options arguments can be named in order to get improved display when option usage is printed (see below). 

### Required options

Options may be set as required using the `@Required` annotation. If CLI arguments do not set a required option, a `CliUsageException` is thrown during their parsing.
The default value is `not required`.

### Option descriptions

Options may be given a description. This description is displayed when the list of options is displayed (see the "Parsers" section below).

## Parameters

CLI arguments not related to options (called parameters is this documentation) can be accessed by two ways:

* by calling `CliArgsParser.getParameters()`, which returns the list of strings corresponding to the arguments;
* using the `@Param` annotation.

This annotation is used on fields (like options annotations), but cannot be set on options. It takes one argument (the parameter index), which default value is 0.

A field for which the `@Param` annotation is set with a parameter index of `i` will be set to the value of the `i`-th parameter.
The parameter index must have a value between 0 and `n` (excluded), where `n` is the number of parameters.
The number of parameters can be bounded using the `@Params` class annotation (see below).

## Field types

During the CLI arguments parsing phase, the fields corresponding to options and parameters are updated with the values provided by the arguments. In case an option does not appear in the CLI, the field keeps its default value.

The algorithm used to assign the (string) value to a field depends on the field type and the `@Args` value (which default is zero):


| `Type/Args` 	| algorithm                                                                                                                 	|
|-------------	|---------------------------------------------------------------------------------------------------------------------------	|
| `boolean/0` 	| if the flag is present, sets the value to `true`                                                                          	|
| `Boolean/0` 	| if the flag is present, sets the value to `Boolean.TRUE`                                                                  	|
| `boolean/1` 	| sets the value to `true` (resp. `false`) if the argument is equal to a *true value* (resp. *false value*)                 	|
| `Boolean/1` 	| sets the value to `Boolean.TRUE` (resp. `Boolean.FALSE`) if the argument is equal to a *true value* (resp. *false value*) 	|
| `String/1`  	| sets the value given by the argument                                                                                      	|
| `int/1`     	| sets the value as the result of `Integer.valueOf(String).intValue()` applied on the argument                              	|
| `Integer/1` 	| sets the value as the result of `Integer.valueOf(String)` applied on the argument                                         	|
| `long/1`    	| sets the value as the result of `Long.valueOf(String).longValue()` applied on the argument                                	|
| `Long/1`    	| sets the value as the result of `Long.valueOf(String)` applied on the argument                                            	|
|             	|                                                                                                                           	|

The default *false values* and *true values* involved in `boolean/1` and `Boolean/1` are set to `"false"` and `"true"` as a default.
They can be changed by a call to `CliArgsParser.setBooleanConstants(String[], String[])` and reset by `CliArgsParser.resetBooleanConstants()`.

More types will be handled in the future.

## Class annotations

Class annotation are used to define configurations that are global to the full list of command-line arguments.
At this time, there is only one class annotation, used to define the number of allowed parameters: `@Params`.

The number of allowed parameters is given by a string of the form `"X..Y"` denoting an interval in which `X` is the lower bound and `Y` is the upper bound.
`X` and `Y` must be integers, `Y` must be greater than or equal to `X`, and `Y` must be at least 0. `Y` can take the special value `*` which means `unbounded`.

## Parsers

Jigsaw-CLI uses two parsers: one for the annotations and one for the CLI arguments.

The first one, the class parser, must be instantiated with a class object and parameterized by this class: `ClassParser<T> classParser = new ClassParser<>(T.class)`.
The class object is the one of the class in which the annotations are set. This parser is responsible of the correct usage of the annotations and is used to build the internal option map.

The second one, the CLI arguments parser, is instantiated using a class parser (and must be parameterized by the same type): `CliArgsParser<T> argsParser = new CliArgsParser<>(classParser)`.

The parsing process begins when the `CliArgsParser<T>.parse(T, String[])` method is called. Its first parameter is the object in which we want the fields to be updated according to the CLI arguments provided by the second parameter. This method can throw two kinds of exceptions:

* a `CliOptionDefinitionException` when the class parser detects an error due to a wrong usage of the annotations (an option has no name, a parameter has a wrong index, ...),
* a `CliUsageException` when the CLI arguments are incorrect according to the annotations (missing required field, wrong value type, ...).

When a `CliUsageException` is thrown, it may be useful to tell the user what is wrong (the reason is accessible through the `getMessage()` method of the exception) and what options are allowed using `ClassParser<T>.printOptionUsage(PrintWriter)`.

## Short names disambiguation

By default, Jigsaw-CLI prevents the declaration of a short name option composed by multiple characters if each character corresponds itself to a short name to prevents ambiguity (if `a`, `b` and `ab` are short names, using `-ab` may activate `-a` and `-b`, or `-ab`). In case you need to define such names, you can deactivate the merging behavior by calling `ClassParser<T>.allowShortNamesMerging(false)` before launching the CLI arguments parsing.

## License

Jigsaw-CLI is developed at CRIL (Centre de Recherche en Informatique de Lens) as a part of other projects.
It is made available under the terms of the GNU GPLv3 license.
