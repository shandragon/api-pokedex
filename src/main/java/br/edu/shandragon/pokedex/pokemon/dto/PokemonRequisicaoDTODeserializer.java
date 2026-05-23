package br.edu.shandragon.pokedex.pokemon.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PokemonRequisicaoDTODeserializer extends StdDeserializer<PokemonRequisicaoDTO> {

    private static final Set<String> CAMPOS_FIXOS = Set.of("numeroPokdex", "nome", "tipos", "evolucoes");

    public PokemonRequisicaoDTODeserializer() {
        super(PokemonRequisicaoDTO.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public PokemonRequisicaoDTO deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        ObjectNode node = mapper.readTree(p);

        Integer numeroPokdex = node.has("numeroPokdex") ? node.get("numeroPokdex").intValue() : null;
        String nome = node.has("nome") ? node.get("nome").textValue() : null;

        List<String> tipos = new ArrayList<>();
        if (node.has("tipos")) {
            node.get("tipos").forEach(t -> tipos.add(t.textValue()));
        }

        List<Map<String, Object>> evolucoes = new ArrayList<>();
        if (node.has("evolucoes")) {
            node.get("evolucoes").forEach(e -> evolucoes.add(
                    mapper.convertValue(e, new TypeReference<>() {})));
        }

        Map<String, Object> atributosExtras = new HashMap<>();
        node.fields().forEachRemaining(entry -> {
            if (!CAMPOS_FIXOS.contains(entry.getKey())) {
                atributosExtras.put(entry.getKey(), mapper.convertValue(entry.getValue(), Object.class));
            }
        });

        return new PokemonRequisicaoDTO(numeroPokdex, nome, tipos, evolucoes, atributosExtras);
    }
}
