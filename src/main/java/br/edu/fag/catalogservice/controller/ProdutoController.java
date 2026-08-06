package br.edu.fag.catalogservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private static final double PRECO_DE_PRODUTO_DE_ALTO_VALOR = 1000.00;

    private final JdbcTemplate jdbcTemplate;

    public ProdutoController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> criar(@RequestBody Map<String, Object> dados) {
        String nome = texto(dados.get("nome"));
        String descricao = texto(dados.get("descricao"));
        Double preco = numeroDecimal(dados.get("preco"));

        // Validações básicas dos dados recebidos
        if (nome == null || nome.isBlank()) {
            return ResponseEntity.badRequest().body(erro("O nome do produto é obrigatório."));
        }

        if (preco == null) {
            return ResponseEntity.badRequest().body(erro("O preço do produto deve ser um número válido."));
        }

        if (preco <= 0) {
            return ResponseEntity.badRequest().body(erro("O preço do produto deve ser maior que zero."));
        }

        nome = nome.trim();
        descricao = descricao == null ? null : descricao.trim();

        // Regra de negócio 1: nomes muito curtos não identificam bem um produto.
        if (nome.length() < 3) {
            return ResponseEntity.badRequest().body(
                    erro("O nome do produto deve ter pelo menos 3 caracteres."));
        }

        // Regra de negócio 2: produtos de alto valor precisam ser bem descritos.
        if (preco >= PRECO_DE_PRODUTO_DE_ALTO_VALOR
                && (descricao == null || descricao.isBlank())) {
            return ResponseEntity.badRequest().body(
                    erro("Produtos a partir de R$ 1.000,00 devem possuir uma descrição."));
        }

        preco = Math.round(preco * 100.0) / 100.0;
        LocalDateTime criadoEm = LocalDateTime.now();

        KeyHolder chaveGerada = new GeneratedKeyHolder();
        String nomeParaSalvar = nome;
        String descricaoParaSalvar = descricao;
        Double precoParaSalvar = preco;

        jdbcTemplate.update(conexao -> {
            PreparedStatement comando = conexao.prepareStatement(
                    "INSERT INTO produtos (nome, descricao, preco, criado_em) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            comando.setString(1, nomeParaSalvar);
            comando.setString(2, descricaoParaSalvar);
            comando.setDouble(3, precoParaSalvar);
            comando.setTimestamp(4, Timestamp.valueOf(criadoEm));
            return comando;
        }, chaveGerada);

        Number chave = chaveGerada.getKey();

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("id", chave.longValue());
        resposta.put("nome", nome);
        resposta.put("descricao", descricao);
        resposta.put("preco", preco);
        resposta.put("criadoEm", criadoEm);

        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    private String texto(Object valor) {
        return valor instanceof String ? (String) valor : null;
    }

    private Double numeroDecimal(Object valor) {
        if (valor == null) {
            return null;
        }

        try {
            String numero = valor.toString().trim().replace(',', '.');
            double resultado = Double.parseDouble(numero);
            return Double.isFinite(resultado) ? resultado : null;
        } catch (NumberFormatException excecao) {
            return null;
        }
    }

    private Map<String, Object> erro(String mensagem) {
        return Map.of("erro", mensagem);
    }
}
