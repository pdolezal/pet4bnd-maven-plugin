# Syntax of the exports description file #

The format of the package exports description file is very simple in order to be concise, human-friendly, suitable for using with version control system (where it is an often subject of reviewing differences and merging different versions) and for text manipulations (with tools like diff or grep). However, *.bnd* format allows too much freedom and yet does not provide means for tracking changes.


## Characteristics ##

The file is a text file in the UTF-8 encoding and consisting of printable characters only. Non-printable characters should be treated as an error.

The file contains a flat list of entries that define either package groups (with `$bundle` being a special case of a package group, implicitly binding all packages), or a package exports. The file may yet contain comments and blank lines (both not significant), whitespace is not significant except for line endings as the file format is line-oriented.


## Grammar ##

With the assumption of leaving the insignificant whitespace the grammar could be expressed as these few rules:

```
FILE            ::= (COMMENT | DEFINITION | EOL)*
COMMENT         ::= '#' {printable character}* EOL
DEFINITION      ::= GROUP | EXPORT
EOL             ::= {line ending}

GROUP           ::= GNAME ':' VERSION CONSTRAINT? CHANGE? EOL
EXPORT          ::= PACKAGE ATTRIBUTES?

PACKAGE         ::= PNAME ':' ( VERSION | GNAME ) CONSTRAINT? CHANGE? EOL
ATTRIBUTES      ::= '+' {attributes} EOL

GNAME           ::= {name starting with $}
PNAME           ::= {package name}

CONSTRAINT      ::= '<' VERSION
CHANGE          ::= '@' ( 'major' | 'minor' | 'micro' | 'none' )
VERSION         ::= {version in the format major(.minor(.micro(.qualifier)?)?)?}
```


## Example ##

```
$bundle: 1.2.3 < 2.0.0

foo.bar: 2.1.3 < 3.0.0 @ minor
foo.baz: 1.1.2         @ none
+ x-demo:=true

foo.boo: $bundle
```


## Semantics ##

All definitions may appear at most once. The `$bundle` directive is mandatory and must appear (once as implied by the previous sentence). A group name reference in an export must refer to a group that has been defined already, it means that group definitions must precede the points of their use.
