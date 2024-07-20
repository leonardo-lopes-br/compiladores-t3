package br.ufscar.dc.compiladores.t3;

// Importações
import static br.ufscar.dc.compiladores.t3.ParserUtils_T3.verificarTipo;
import static br.ufscar.dc.compiladores.t3.ParserUtils_T3.adicionaErroSemantico;
import static br.ufscar.dc.compiladores.t3.ParserUtils_T3.verificaCompatibilidade;
import br.ufscar.dc.compiladores.t3.TabelaDeSimbolos.Tipos;
import org.antlr.v4.runtime.Token;

// Classe do Parser para visitar a árvore da gramática
public class Parser_T3 extends LABaseVisitor<Void> {
    static Escopos escoposAninhados = new Escopos();
    TabelaDeSimbolos tabela;
    TabelaDeSimbolos tabelaEscopo;

    // Método auxiliar para adicionar a variável que está sendo analisada na tabela de escopos
    public void addVariableIntoScopeTable(String nome, String tipo, Token nomeT, Token tipoT) {
        tabelaEscopo = escoposAninhados.obterEscopoAtual();
        Tipos tipoItem;
        switch (tipo) {
            case "inteiro":
                tipoItem = Tipos.INTEIRO;
                break;
            case "real":
                tipoItem = Tipos.REAL;
                break;
            case "logico":
                tipoItem = Tipos.LOGICO;
                break;
            case "literal":
                tipoItem = Tipos.LITERAL;
                break;
            default:
                tipoItem = Tipos.INVALIDO;
                break;
        }
        // Tipo não declarado
        if (tipoItem == Tipos.INVALIDO)
            adicionaErroSemantico(tipoT, "tipo " + tipo + " nao declarado");

        // Caso a variavel não tenha sido declarada anteriormente no escopo atual, adiciona ela
        if (!tabelaEscopo.existe(nome))
            tabelaEscopo.adicionar(nome, tipoItem);
        // Caso contrário, gera o erro de variável já declarada
        else
            adicionaErroSemantico(nomeT, "identificador " + nome + " ja declarado anteriormente");
    }

    // Inicio da análise sintática
    @Override
    public Void visitPrograma(LAParser.ProgramaContext ctx) {
        tabela = new TabelaDeSimbolos();
        return super.visitPrograma(ctx);
    }

    @Override
    public Void visitDeclaracoes(LAParser.DeclaracoesContext ctx) {
        tabela = escoposAninhados.obterEscopoAtual();

        for (LAParser.Decl_local_globalContext declaracao : ctx.decl_local_global())
            visitDecl_local_global(declaracao);
        
        return super.visitDeclaracoes(ctx);
    }

    // Distingue entre declaração local e global
    @Override
    public Void visitDecl_local_global(LAParser.Decl_local_globalContext ctx) {
        tabela = escoposAninhados.obterEscopoAtual();

        if (ctx.declaracao_local() != null)
            visitDeclaracao_local(ctx.declaracao_local());
        else if (ctx.declaracao_global() != null)
            visitDeclaracao_global(ctx.declaracao_global());

        return super.visitDecl_local_global(ctx);
    }

    @Override
    public Void visitDeclaracao_local(LAParser.Declaracao_localContext ctx) {
        tabela = escoposAninhados.obterEscopoAtual();
        // Verifica se existe uma declaração
        if (ctx.getText().contains("declare")) {
            String tipoVariavel, identVariavel;
            tipoVariavel = ctx.variavel().tipo().getText();

            // Adiciona a variabel no escopo atual caso ela não exista
            for (LAParser.IdentificadorContext ident : ctx.variavel().identificador()) {
                identVariavel = ident.getText();
                addVariableIntoScopeTable(identVariavel, tipoVariavel, ident.getStart(), ctx.variavel().tipo().getStart());
            }
        }
        return super.visitDeclaracao_local(ctx);
    }

    @Override
    public Void visitCmdLeia(LAParser.CmdLeiaContext ctx) {
        tabela = escoposAninhados.obterEscopoAtual();

        for (LAParser.IdentificadorContext id : ctx.identificador())
            // Verifica se a variável já foi declarada anteriormente, caso não, gera erro
            if (!tabela.existe(id.getText()))
                adicionaErroSemantico(id.getStart(), "identificador " + id.getText() + " nao declarado");

        return super.visitCmdLeia(ctx);
    }

    @Override
    public Void visitCmdEscreva(LAParser.CmdEscrevaContext ctx) {
        tabela = escoposAninhados.obterEscopoAtual();

        for (LAParser.ExpressaoContext expressao : ctx.expressao())
            verificarTipo(tabela, expressao);

        return super.visitCmdEscreva(ctx);
    }

    @Override
    public Void visitCmdEnquanto(LAParser.CmdEnquantoContext ctx) {
        tabela = escoposAninhados.obterEscopoAtual();
        verificarTipo(tabela, ctx.expressao());
        return super.visitCmdEnquanto(ctx);
    }

    @Override
    public Void visitCmdAtribuicao(LAParser.CmdAtribuicaoContext ctx) {
        tabela = escoposAninhados.obterEscopoAtual();
        Tipos tipoExpressao = verificarTipo(tabela, ctx.expressao());
        String nomeVar = ctx.identificador().getText();
        
        if (tipoExpressao != Tipos.INVALIDO) {
            // Variavel não declarada
            if (!tabela.existe(nomeVar)) {
                adicionaErroSemantico(ctx.identificador().getStart(), "identificador " + ctx.identificador().getText() + " nao declarado");
            } else {
                // Verificação de tipos numéricos
                Tipos varTipo = verificarTipo(tabela, nomeVar);
                if (varTipo == Tipos.INTEIRO || varTipo == Tipos.REAL) {
                    if (!verificaCompatibilidade(varTipo, tipoExpressao)) {
                        adicionaErroSemantico(ctx.identificador().getStart(), "atribuicao nao compativel para " + ctx.identificador().getText());
                    }
                // Se não for o caso especial dos números, retorna o erro de incompatibilidade
                } else if (varTipo != tipoExpressao)
                    adicionaErroSemantico(ctx.identificador().getStart(), "atribuicao nao compativel para " + ctx.identificador().getText());
            }
        }
        return super.visitCmdAtribuicao(ctx);
    }
}
