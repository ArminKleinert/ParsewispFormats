package de.kleinert.parsewisp.ebnf;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EBNFAmbiguityTest {

    String grammar = """
            program = WS , { ( function_decl | statement ) , WS } ;
            
            function_decl = 'fun' , WS , identifier , WS , function_args , WS , block ;
            function_args = '(' , WS , identifier , WS , { ',' , WS , identifier } , WS ,')' | '(' , WS , ')' ;
            
            block_stmt = block | statement ;
            block = '{' , WS , { statement , WS } , '}' ;
            
            statement = if_statement | while_statement | expression_statement ;
            
            if_statement = 'if' , WS , condition , WS , block_stmt , WS , [ 'else' , WS , block_stmt ] ;
            
            while_statement = 'while' , WS , condition , WS , block_stmt ;
            expression_statement = expression , WS , ';' ;
            
            condition = '(' , WS , expression , WS , ')' ;
            
            expression = number | identifier ;
            number = #'[0-9]+' ;
            identifier = #'[a-zA-Z][a-zA-Z0-9]+' ;
            
            <WS> = <#'\\s*'> ;
            """;

    @Test
    void test0() {
        var p = EBNF.parser(grammar);
        var text = """
                if (abc)
                  if (ghi)
                    jkl;
                else
                  mni;
                """;
        Assertions.assertEquals(2, p.parses(text).size());
    }
}
