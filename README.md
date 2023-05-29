= PreLaTeX =

Converts LaTeX source(s) into a single equivalent LaTeX output file, while removing various constructs:

* \newcommand definitions
* \*def definitions
* various conditionals (\newif, \ifx, \ifcase, etc.)
* \input

External packages are not read and macros defined in them are not replaced. Macros defined in the file
or in local packages are expanded according to their definitions.

The environment variable TEXINPUTS is used  in the usual way as a search path
for additional source files.

The emulation of TeX and LaTeX is far from perfect (and can never be perfect).
I am hoping that people will contribute to improving its capabilities to make
this a better tool. Some known issues are:

* incorrect handling of \usepackage and \RequirePackage options
* no \newenvironment support
