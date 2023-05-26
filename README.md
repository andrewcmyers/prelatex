= PreLaTeX =

Converts LaTeX source into a single equivalent LaTeX output file, while removing various constructs:

* \newcommand definitions
* \*def definitions
* various conditionals (\newif, \ifx, \ifcase, etc.)
* \newenvironment definitions
* \input

External packages are not read and macros defined in them are not replaced. Macros defined in the file
or in local packages are expanded according to their definitions.

