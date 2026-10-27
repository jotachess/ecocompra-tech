package com.ecocompra.tech;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import com.ecocompra.tech.entity.Categoria;
import com.ecocompra.tech.repository.CategoriaRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RevisionHttpTest {
    @Autowired MockMvc mvc;
    @Autowired CategoriaRepository categorias;

    @Test void jsonMalformadoDebeSer400() throws Exception {
        mvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
    }
    @Test void parametroObligatorioAusenteDebeSer400() throws Exception {
        mvc.perform(get("/api/productos/buscar/consumo"))
                .andExpect(status().isBadRequest());
    }
    @Test void tipoDeParametroInvalidoDebeSer400() throws Exception {
        mvc.perform(get("/api/productos/buscar/consumo").param("max", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test void aceptaIdCategoriaPlanoYConservaElValorPersistido() throws Exception {
        Categoria categoria = categorias.saveAndFlush(Categoria.builder().nombre("Categoría HTTP plana").build());
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"idCategoria\":" + categoria.getIdCategoria()
                        + ",\"nombre\":\"Producto HTTP plano\",\"precio\":25.50}"))
                .andExpect(status().isCreated()).andExpect(header().exists("Location"))
                .andDo(resultado -> mvc.perform(get(resultado.getResponse().getHeader("Location")))
                        .andExpect(status().isOk()).andExpect(jsonPath("$.precio").value(25.50))
                        .andExpect(jsonPath("$.stock").value(0))
                        .andExpect(jsonPath("$.categoria.nombre").value("Categoría HTTP plana")));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "\"precio\":10.123", "\"precio\":100000000", "\"precio\":10,\"stock\":null",
        "\"precio\":10,\"consumoKwh\":1.12345,\"condicionesConsumo\":\"8 h\",\"fuenteTecnica\":\"Ficha\"",
        "\"precio\":10,\"vidaUtilAnios\":1000,\"fuenteTecnica\":\"Ficha\""
    })
    void rechazaPrecisionIncompatibleAntesDeEscribirEnSql(String campos) throws Exception {
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"idCategoria\":1,\"nombre\":\"Inválido\"," + campos + "}"))
                .andExpect(status().isBadRequest());
    }

    @Test void parametrosConComillasNoAlteranLasConsultas() throws Exception {
        mvc.perform(get("/api/productos/buscar/nombre").param("nombre", "' OR 1=1 --"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
        mvc.perform(get("/api/productos/buscar/reparabilidad").param("criterio", "' OR 1=1 --"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test void categoriasContradictoriasSon400() throws Exception {
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"idCategoria\":1,\"categoria\":{\"idCategoria\":2},\"nombre\":\"Inválido\",\"precio\":10}"))
                .andExpect(status().isBadRequest());
    }

    @Test void metodoYTipoDeContenidoIncorrectosConservanSuEstadoHttp() throws Exception {
        mvc.perform(patch("/api/clientes/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isMethodNotAllowed());
        mvc.perform(post("/api/clientes").contentType(MediaType.TEXT_PLAIN).content("texto"))
                .andExpect(status().isUnsupportedMediaType());
    }
}
