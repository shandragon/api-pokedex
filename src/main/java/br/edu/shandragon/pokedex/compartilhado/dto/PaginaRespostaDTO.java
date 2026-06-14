package br.edu.shandragon.pokedex.compartilhado.dto;

import java.util.List;

public record PaginaRespostaDTO<T>(
    long totalItens,
    int itensPorPagina,
    int paginaAtual,
    List<T> itens
) {}
