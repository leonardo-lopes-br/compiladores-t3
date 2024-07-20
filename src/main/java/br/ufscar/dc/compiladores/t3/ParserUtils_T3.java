package br.ufscar.dc.compiladores.t3;

// Importações
import br.ufscar.dc.compiladores.t3.TabelaDeSimbolos.Tipos;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.Token;

public class ParserUtils_T3 {

    public static List<String> errosSemanticos = new ArrayList<>();

    // Método para adicionar um novo erro
    public static void adicionaErroSemantico(Token token, String msg) {
        int linha = token.getLine();
        
        // Adiciona o novo erro apenas se ele não tiver sido identificado
        if (!errosSemanticos.contains("Linha " + linha + ": " + msg))
            errosSemanticos.add(String.format("Linha %d: %s", linha, msg));
    }
    
    // Método para verificar a compatibilidade dos tipos: Inteiro x Real
    public static boolean verificaCompatibilidade(Tipos T1, Tipos T2) {
        return (
                T1 == Tipos.INTEIRO && T2 == Tipos.REAL
                        || T1 == Tipos.REAL && T2 == Tipos.INTEIRO
                        || T1 == Tipos.REAL && T2 == Tipos.REAL
                        || T1 == Tipos.INTEIRO && T2 == Tipos.INTEIRO
        );
    }
    
    // Método para verificação de compatibilidade lógica entre os valores
    public static boolean verificaCompatibilidadeLogica(Tipos T1, Tipos T2) {
        return (
                T1 == Tipos.INTEIRO && T2 == Tipos.REAL ||
                        T1 == Tipos.REAL && T2 == Tipos.INTEIRO
        );
    }
                    
    public static Tipos verificarTipo(TabelaDeSimbolos tabela, LAParser.Exp_aritmeticaContext ctx) {
        // A variável que será retornada ao fim da execução é inicializada com o tipo
        // do primeiro elemento que será verificado, para fins de comparação
        Tipos tipoRetorno = verificarTipo(tabela, ctx.termo().get(0));

        // Separa o caso especial dos termos numéricos do restante
        for (var termoArit : ctx.termo()) {
            Tipos tipoAtual = verificarTipo(tabela, termoArit);
            if ((verificaCompatibilidade(tipoAtual, tipoRetorno)) && (tipoAtual != Tipos.INVALIDO))
                tipoRetorno = Tipos.REAL;
            else
                tipoRetorno = tipoAtual;
        }
        return tipoRetorno;
    }

    public static Tipos verificarTipo(TabelaDeSimbolos tabela, LAParser.TermoContext ctx) {
        // A variável que será retornada ao fim da execução é inicializada com o tipo
        // do primeiro elemento que será verificado, para fins de comparação
        Tipos tipoRetorno = verificarTipo(tabela, ctx.fator().get(0));

        // Separa o caso especial dos termos numéricos do restante
        for (LAParser.FatorContext fatorArit : ctx.fator()) {
            Tipos tipoAtual = verificarTipo(tabela, fatorArit);
            if ((verificaCompatibilidade(tipoAtual, tipoRetorno)) && (tipoAtual != Tipos.INVALIDO))
                tipoRetorno = Tipos.REAL;
            else
                tipoRetorno = tipoAtual;
        }
        
        return tipoRetorno;
    }

    public static Tipos verificarTipo(TabelaDeSimbolos tabela, LAParser.FatorContext ctx) {
        Tipos tipoRetorno = null;
        for (LAParser.ParcelaContext parcela : ctx.parcela())
            tipoRetorno = verificarTipo(tabela, parcela);
        return tipoRetorno;
    }

    // Dintingue entre parcela unária e não unária
    public static Tipos verificarTipo(TabelaDeSimbolos tabela, LAParser.ParcelaContext ctx) {
        if (ctx.parcela_unario() != null)
            return verificarTipo(tabela, ctx.parcela_unario());
        else
            return verificarTipo(tabela, ctx.parcela_nao_unario());
    }

    public static Tipos verificarTipo(TabelaDeSimbolos tabela, LAParser.Parcela_unarioContext ctx) {
        Tipos tipoRetorno;
        String nome;
        
        if (ctx.identificador() != null) {
            nome = ctx.identificador().getText();
            
            // Caso a variável já tenha sido declarada, retorna o tipo dela
            if (tabela.existe(nome))
                tipoRetorno = tabela.verificar(nome);
            // Verifica a existencia da variavel com uma tabela auxiliar e adiciona o erro se ele não existir
            else {
                TabelaDeSimbolos tabelaAux = Parser_T3.escoposAninhados.percorrerEscoposAninhados().get(Parser_T3.escoposAninhados.percorrerEscoposAninhados().size() - 1);
                if (!tabelaAux.existe(nome)) {
                    adicionaErroSemantico(ctx.identificador().getStart(), "identificador " + ctx.identificador().getText() + " nao declarado");
                    tipoRetorno = Tipos.INVALIDO;
                } else 
                    tipoRetorno = tabelaAux.verificar(nome);
            }
        } else if (ctx.NUM_INT() != null)
            tipoRetorno = Tipos.INTEIRO;
        else if (ctx.NUM_REAL() != null)
            tipoRetorno = Tipos.REAL;
        else
            tipoRetorno = verificarTipo(tabela, ctx.exp_aritmetica().get(0));

        return tipoRetorno;
    }

    public static Tipos verificarTipo(TabelaDeSimbolos tabela, LAParser.Parcela_nao_unarioContext ctx) {
        Tipos tipoRetorno;
        String nome;

        // Novamente, verifica se a variável existe e adiciona um erro conforme necessário
        if (ctx.identificador() != null) {
            nome = ctx.identificador().getText();
        
            if (!tabela.existe(nome)) {
                adicionaErroSemantico(ctx.identificador().getStart(), "identificador " + ctx.identificador().getText() + " nao declarado");
                tipoRetorno = Tipos.INVALIDO;
            } else 
                tipoRetorno = tabela.verificar(ctx.identificador().getText());
        } else
            tipoRetorno = Tipos.LITERAL;

        return tipoRetorno;
    }

    public static Tipos verificarTipo(TabelaDeSimbolos tabela, LAParser.ExpressaoContext ctx) {
        Tipos tipoRetorno = verificarTipo(tabela, ctx.termo_logico(0));

        // Aqui basta verificar se os tipos são diferentes
        for (LAParser.Termo_logicoContext termoLogico : ctx.termo_logico()) {
            Tipos tipoAtual = verificarTipo(tabela, termoLogico);
            if (tipoRetorno != tipoAtual && tipoAtual != Tipos.INVALIDO)
                tipoRetorno = Tipos.INVALIDO;
        }

        return tipoRetorno;
    }

    public static Tipos verificarTipo(TabelaDeSimbolos tabela, LAParser.Termo_logicoContext ctx) {
        Tipos tipoRetorno = verificarTipo(tabela, ctx.fator_logico(0));
        // Aqui basta verificar se os tipos são diferentes
        for (LAParser.Fator_logicoContext fatorLogico : ctx.fator_logico()) {
            Tipos tipoAtual = verificarTipo(tabela, fatorLogico);
            if (tipoRetorno != tipoAtual && tipoAtual != Tipos.INVALIDO)
                tipoRetorno = Tipos.INVALIDO;
        }
        return tipoRetorno;
    }

    public static Tipos verificarTipo(TabelaDeSimbolos tabela, LAParser.Fator_logicoContext ctx) {
        return verificarTipo(tabela, ctx.parcela_logica());
    }

    public static Tipos verificarTipo(TabelaDeSimbolos tabela, LAParser.Parcela_logicaContext ctx) {
        Tipos tipoRetorno;

        if (ctx.exp_relacional() != null)
            tipoRetorno = verificarTipo(tabela, ctx.exp_relacional());
         else
            tipoRetorno = Tipos.LOGICO;

        return tipoRetorno;

    }

    public static Tipos verificarTipo(TabelaDeSimbolos tabela, LAParser.Exp_relacionalContext ctx) {
        Tipos tipoRetorno = verificarTipo(tabela, ctx.exp_aritmetica().get(0));

        if (ctx.exp_aritmetica().size() > 1) {
            Tipos tipoAtual = verificarTipo(tabela, ctx.exp_aritmetica().get(1));

            if (tipoRetorno == tipoAtual || verificaCompatibilidadeLogica(tipoRetorno, tipoAtual))
                tipoRetorno = Tipos.LOGICO;
            else
                tipoRetorno = Tipos.INVALIDO;
        }

        return tipoRetorno;

    }

    // Verificação padrão de tipos de variáveis a partir da tabela
    public static Tipos verificarTipo(TabelaDeSimbolos tabela, String nomeVar) {
        return tabela.verificar(nomeVar);
    }
}