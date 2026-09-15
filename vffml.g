syntax             = [WS] { rule }
rule               = nt [WS] "=" [WS] alternation [WS] [";" WS]
alternation        = concatenation { WS "/" WS concatenation }
concatenation      = primary { WS primary }
suffix             = primary ["+" | "*" | "?"]
primary            = "[" WS alternation WS "]" | "{" WS alternation WS "}" | "(" WS alternation WS ")" | nt | terminal
nt                 = #"[a-zA-Z][a-zA-Z0-9_]*(?x) # NonTerminal"
terminal           = regexTerminal | stringTerminal | quasiRegexTerminal
stringTerminal     = "\"" #"[^\"]*" "\""
regexTerminal      = #"´[^\´\s]*´"
quasiRegexTerminal = qRegexPart { qRegexPart }
<qRegexPart>       = "[" range+ "]" #"[\+\*\?]?"
<range>            = char [ "-" char ]
<char>             = #"\\\\(u[0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F]|[nrt\"´\\\\])" | #"[^\\\s\-]"
comment            = <"#" { (" " | "\t" | #"[^\S]+") } (#"\\r?\\n" | EOF)>
<WS>               = <( " " | "\t" | "\n" )+>