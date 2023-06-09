# preLaTeX

This tool converts LaTeX source(s) into a single equivalent LaTeX output file, while removing various constructs by expanding them.
This transformation is useful for producing LaTeX source files that are accepted by picky publishers like arXiv and ACM.

Eliminated constructs include:

* \newcommand and \newenvironment definitions
* \*def definitions (TeX definitions)
* various conditionals (\newif, \ifx, \ifcase, \ifmmode, etc.)
* files read using \input
* some more advanced constructs: \csname, \expandafter, ...

External packages are not read and macros defined in them are not replaced. Macros defined in the file
or in local packages are expanded according to their definitions. Macros not known to preLaTeX, or
specified as "kept", are left unexpanded.

Comments can be optionally removed.

The environment variable TEXINPUTS is used in the usual way as a search path
for additional source files.

The emulation of TeX and LaTeX is far from perfect (and can never be perfect,
since in general we do not want to expand packages). I am hoping that people
will contribute to improving its capabilities to make this a better tool. Some
known (and fixable) issues are:

* Various other built-in TeX macros are not yet supported, like \noexpand, \string, \uppercase, \lowercase, \number,
  \message, \afterassignment, \aftergroup, \ignorespaces, \futurelet, etc. These are of course macros that
  should not appear in pure LaTeX documents, but are useful for expanding some packages.

preLaTeX is not integrated with BibTeX, which should not be a problem for most uses.

## Configuration

The default behavior of preLaTeX can be modified by using a configuration file, specified using the `--config`
option. The configuration file uses [LWON dictionary syntax](https://github.com/andrewcmyers/lwon).
The following options may be specified:

- `comments: false`

    Remove TeX comments from the output if set to false; otherwise,
    preserve them. Comments inside macro definitions are not preserved,
    however.

- `drop package: <pkg>`

    Remove the package entirely from the output

- `expand package: <pkg>`

    Read and process the package contents: useful for local packages.

- `drop macro: <name>`

    Macro `name` is expanded to empty text rather than as defined.
    
- `keep macro: <name>`

    This macro is not expanded even if its definition is known. Its arguments are still expanded.

- `TEXINPUTS <directory list>`

    The list of directories to use as the path for finding included packages and other files is
    specified as an array in LWON syntax: surrounded by brackets and separated by commas.
 
    *Example:*

    ```
    TEXINPUTS [ paper/macros, paper/sections ]
    ```

It is also possible to specify some of these options on the command line.

## Command-line options

preLaTeX has a number of options for controlling how the file is interpreted:

  * `--nocomments`: remove comments from the output
  * `--dump_macros`: print out all macros known by preLaTeX
  * `--config <config file>`: specify configuration file
  * `--drop <pkg>`: equivalent to `drop package: <pkg>` in the configuration file
  * `--expand <pkg>`: equivalent to `expand package: <pkg>` in the configuration file

After the command line options, one or more input LaTeX files may be specified. These are read in sequence, so the default behavior can
also be modified by creating files of macro definitions to be read before the main document file. A typical invocation might
look as follows:

    prelatex --config config.pltx overrides.tex top.tex

## Building

To build you can use Gradle:

    gradle jar

Then the script `bin/prelatex` can be used to run the program:

    bin/prelatex myfile.tex ...

If you run into Java version incompatibility issues, probably Gradle is building for a different version
of Java than your default installation of Java. Try setting the environment variable `JAVA_HOME` to
the appropriate Java installation (which must be at least version 17).



You can also install the prelatex script on your system by running `gradle install`. This needs to
be run using `sudo` so the necessary files can be copied. It installs into `/usr/local/bin` and `/usr/local/share/prelatex`.

### IntelliJ

If you want to run the program inside IntellJ or another IDE, the source code
you need is found in the following directories, where the top-level directory
is `prelatex`:

    prelatex/src
    prelatex/easyIO/src
    prelatex/lwon/src

Mark all of them as "source roots" and build. Note that you will need to enable preview features
in the Java compiler and the Java executable itself, with the `--enable-preview` option.

## Contributing

Contributions as PRs are welcome. Discussion in the Issues section is probably a good idea before trying to get involved.
