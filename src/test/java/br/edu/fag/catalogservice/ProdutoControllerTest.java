package br.edu.fag.catalogservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProdutoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void limparBanco() {
        jdbcTemplate.update("DELETE FROM produtos");
    }

    @Test
    void deveCriarUmProdutoValido() throws Exception {
        mockMvc.perform(post("/api/produtos")
                        .contentType("application/json")
                        .content("""
                                {
                                  "nome": "Teclado mecânico",
                                  "descricao": "Teclado ABNT2",
                                  "preco": 349.90
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Teclado mecânico"))
                .andExpect(jsonPath("$.descricao").value("Teclado ABNT2"))
                .andExpect(jsonPath("$.preco").value(349.90));

        Integer quantidade = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM produtos WHERE nome = ? AND preco = ?",
                Integer.class,
                "Teclado mecânico",
                349.90);

        assertThat(quantidade).isEqualTo(1);
    }

    @Test
    void naoDeveCriarProdutoSemNome() throws Exception {
        mockMvc.perform(post("/api/produtos")
                        .contentType("application/json")
                        .content("""
                                { "nome": "  ", "preco": 10.00 }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("O nome do produto é obrigatório."));

        assertThat(totalDeProdutos()).isZero();
    }

    @Test
    void naoDeveCriarProdutoComPrecoInvalido() throws Exception {
        mockMvc.perform(post("/api/produtos")
                        .contentType("application/json")
                        .content("""
                                { "nome": "Mouse", "preco": 0 }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("O preço do produto deve ser maior que zero."));

        assertThat(totalDeProdutos()).isZero();
    }

    @Test
    void naoDeveCriarProdutoComNomeMenorQueTresCaracteres() throws Exception {
        mockMvc.perform(post("/api/produtos")
                        .contentType("application/json")
                        .content("""
                                { "nome": "TV", "preco": 900.00 }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro")
                        .value("O nome do produto deve ter pelo menos 3 caracteres."));

        assertThat(totalDeProdutos()).isZero();
    }

    @Test
    void naoDeveCriarProdutoDeAltoValorSemDescricao() throws Exception {
        mockMvc.perform(post("/api/produtos")
                        .contentType("application/json")
                        .content("""
                                { "nome": "Notebook", "preco": 4500.00 }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro")
                        .value("Produtos a partir de R$ 1.000,00 devem possuir uma descrição."));

        assertThat(totalDeProdutos()).isZero();
    }

    private int totalDeProdutos() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM produtos", Integer.class);
    }
}
