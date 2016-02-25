# Syntax of the exports description file #

The format of the package exports description file is very simple in order to be concise, human-friendly, suitable for using with version control system (where it is an often subject of reviewing differences and merging different versions) and for text manipulations (with tools like diff or grep). However, *.bnd* format allows too much freedom and yet does not provide means for tracking changes.


## Characteristics ##

The file is a text file in the UTF-8 encoding and consisting of printable characters only. Non-printable characters should be treated as an error.

The file contains a flat list of entries describing individual packages, which shall be exported, and a bundle-specific entry, which shares the syntax of the package entries, although not supporting all attributes (because they don't make sense for a bundle). An entry consists of one or two lines (the second line, if present, provides additional attributes for the package).


## Grammar ##

Whitespace not significant, except for line endings, and with this assumption, the grammar could be expressed as these few rules:

```
FILE            ::= (COMMENT | ENTRY | EOL)*
COMMENT         ::= '#' {printable character}* EOL
ENTRY           ::= BUNDLE | PACKAGE
EOL             ::= {line ending}

BUNDLE          ::= '$bundle-version' BASELINE CONSTRAINT? CHANGE? EOL
PACKAGE         ::= EXPORT ATTRIBUTES?

EXPORT          ::= {package name} ( BASELINE | 'inherit' ) CONSTRAINT? CHANGE? EOL
ATTRIBUTES      ::= '+' {attributes} EOL

BASELINE        ::= ':' VERSION
CONSTRAINT      ::= '<' VERSION
CHANGE          ::= '@' ( 'major' | 'minor' | 'micro' | 'none' )
```


## Example ##

```
$bundle-version: 1.2.3 < 2.0.0

foo.bar: 2.1.3 < 3.0.0 @ minor
foo.baz: 1.1.2         @ none
+ x-demo:=true
```


## Semantics ##

All directives may appear at most once. The `$bundle-version` directive is mandatory and must appear (once as implied by the previous sentence).

The `inherit` token within an export directive means that the package version shall be inherited from the bundle version after resolving.
