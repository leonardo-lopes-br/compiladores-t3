package br.ufscar.dc.compiladores.t3;

import java.util.HashMap;
import java.util.Map;

// Classe baseada em código disponibilizado de exemplo pelo professor
public class TabelaDeSimbolos {

    private final Map<String, EntradaTabelaDeSimbolos> tabela;
    
    public TabelaDeSimbolos() {
        this.tabela = new HashMap<>();
    }

    public enum Tipos {
        INTEIRO,
        REAL,
        LITERAL,
        LOGICO,
        INVALIDO
    }

    static class EntradaTabelaDeSimbolos {
        String nome;
        Tipos tipo;

        private EntradaTabelaDeSimbolos(String nome, Tipos tipo) {
            this.nome = nome;
            this.tipo = tipo;
        }
    }

    public Tipos verificar(String nome) {
        return tabela.get(nome).tipo;
    }

    public void adicionar(String nome, Tipos tipo) {
        tabela.put(nome, new EntradaTabelaDeSimbolos(nome, tipo));
    }

    public boolean existe(String nome) {
        return tabela.containsKey(nome);
    }
}
