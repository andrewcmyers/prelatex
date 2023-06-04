# PreLaTeX

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

## Configuration

The default behavior of PreLaTeX can be modified by using a configuration file, specified using the `--config`
option. The configuration file uses LWON dictionary syntax. The following options may be specified:

- `nocomments: true`

    Remove TeX comments from the output.

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

    Example:

    ```
    TEXINPUTS [ paper/macros, paper/sections ]
    ```
