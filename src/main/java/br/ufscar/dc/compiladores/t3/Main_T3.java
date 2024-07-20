package br.ufscar.dc.compiladores.t3;

// Importações
import br.ufscar.dc.compiladores.t3.LAParser.ProgramaContext;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public class Main_T3 {

    public static void main(String[] args) throws IOException {

        // Tratando os parâmetros da main
        try (PrintWriter pw = new PrintWriter(new FileWriter(args[1]))) {
            try {
                CharStream cs = CharStreams.fromFileName(args[0]);
                
                // Analisador léxico
                LALexer lexer = new LALexer(cs);
                CommonTokenStream tokens = new CommonTokenStream(lexer);
                
                // Analisador sintático
                LAParser parser = new LAParser(tokens);
                ProgramaContext arvore = parser.programa();
                Parser_T3 my_parser = new Parser_T3();

                my_parser.visitPrograma(arvore);
                
                // Erros encontrados
                ParserUtils_T3.errosSemanticos.forEach(pw::println);
                pw.println("Fim da compilacao");
                pw.close();                
            } catch (RuntimeException e) {
                System.out.println("Erro: " + e);
            }
        }
    }
}
