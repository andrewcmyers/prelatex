# preLaTeX

This tool converts LaTeX source(s) into a single equivalent LaTeX output file, while removing various constructs by expanding them:

* \newcommand definitions
* \*def definitions
* various conditionals (\newif, \ifx, \ifcase, etc.)
* file read using \input

External packages are not read and macros defined in them are not replaced. Macros defined in the file
or in local packages are expanded according to their definitions.

Comments can be optionally removed.

The environment variable TEXINPUTS is used  in the usual way as a search path
for additional source files.

The emulation of TeX and LaTeX is far from perfect (and can never be perfect).
I am hoping that people will contribute to improving its capabilities to make
this a better tool. Some known issues are:

* incorrect handling of \usepackage and \RequirePackage options
* Several variant forms of \def are not supported yet, like \edef and \gdef
* No support for \catcode manipulation

## Configuration

The default behavior of preLaTeX can be modified by using a configuration file, specified using the `--config`
option. The configuration file uses [LWON dictionary syntax](https://github.com/andrewcmyers/lwon)
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

    Macro `name` is expanded to empty text.

- `keep macro: <name>`

    This macro is not expanded even if its definition is known.

- `TEXINPUTS <directory list>`

    The list of directories to use as the path for finding included packages and other files is
    specified as an array in LWON syntax: surrounded by brackets and separated by commas.
 
    *Example:*

    ```
    TEXINPUTS [ paper/macros, paper/sections ]
    ```

## Building

To build you can use Gradle:

    gradle jar

Then the script `bin/prelatex` can be used to run the program:

    bin/prelatex myfile.tex ...

preLaTeX has a number of options for controlling how the file is interpreted. Typically, you
will want a configuration file (the --config option) to define how to handle various packages. You may
also want to define a prefix .tex file to override some of the definitions. So a typical invocation might
look like:

    prelatex --config config.pltx overrides.tex top.tex

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
