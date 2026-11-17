package com.mx.cbtis.gestor.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mx.cbtis.gestor.modelo.Comentarios;
import com.mx.cbtis.gestor.repository.ComentariosRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ComentariosController.class)
public class ComentariosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ComentariosController comentariosController;

    @MockBean
    private ComentariosRepository comentariostable;

    @Test
    @DisplayName("UT-C01 POST /addComentario válido -> 200 y 1 (Éxito)")
    void testUtC01_addComentarioValido() throws Exception {
        Comentarios c = new Comentarios(null, "Liliana Gómez Martínez", 199, "Revisar endpoints", "2024-06-26 14:00:00");

        mockMvc.perform(post("/addComentario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(c)))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(comentariostable).save(any(Comentarios.class));
    }

    @Test
    @DisplayName("UT-C02 GET /buscarComentario?id=201 existente -> comentario encontrado (Éxito)")
    void testUtC02_buscarComentarioExistente() throws Exception {
        Comentarios c = new Comentarios(201, "Perla Joceline", 199, "Subirlo YA!!!", "2024-06-26 13:48:00");
        when(comentariostable.findById(201)).thenReturn(Optional.of(c));

        mockMvc.perform(get("/buscarComentario").param("id", "201"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_comentario").value(201))
                .andExpect(jsonPath("$.texto_comentario").value("Subirlo YA!!!"));
    }

    @Test
    @DisplayName("UT-C03 GET /buscarComentario?id=99999 inexistente -> objeto vacío controlado (Fracaso controlado)")
    void testUtC03_buscarComentarioInexistente() throws Exception {
        when(comentariostable.findById(99999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/buscarComentario").param("id", "99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_comentario").doesNotExist())
                .andExpect(jsonPath("$.texto_comentario").doesNotExist());
    }

    @Test
    @DisplayName("UT-C04 POST /getComentario por tarea -> lista con comentarios (Éxito)")
    void testUtC04_getComentarioPorTarea() throws Exception {
        Comentarios req = new Comentarios();
        req.setId_tarea(199);

        Comentarios c1 = new Comentarios(201, "Perla Joceline", 199, "Subirlo YA!!!", "2024-06-26 13:48:00");
        Comentarios c2 = new Comentarios(202, "Liliana Gómez", 199, "hola", "2024-06-26 13:49:00");
        when(comentariostable.findByTarea(199)).thenReturn(Arrays.asList(c1, c2));

        mockMvc.perform(post("/getComentario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].texto_comentario").value("Subirlo YA!!!"))
                .andExpect(jsonPath("$[1].texto_comentario").value("hola"));
    }

    @Test
    @DisplayName("UT-C05 POST /getComentario tarea sin comentarios -> lista vacía (Fracaso / Vacío)")
    void testUtC05_getComentarioTareaSinComentarios() throws Exception {
        Comentarios req = new Comentarios();
        req.setId_tarea(888);
        when(comentariostable.findByTarea(888)).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/getComentario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("UT-C06 POST /updateComentario válido -> 200 y 1 (Éxito)")
    void testUtC06_updateComentario() throws Exception {
        Comentarios c = new Comentarios(201, "Perla Joceline", 199, "Texto actualizado", "2024-06-26 15:00:00");

        mockMvc.perform(post("/updateComentario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(c)))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(comentariostable).save(any(Comentarios.class));
    }

    @Test
    @DisplayName("UT-C07 DELETE /deleteComentario válido -> 200 y 1 (Éxito)")
    void testUtC07_deleteComentario() throws Exception {
        Comentarios c = new Comentarios(201, "Perla Joceline", 199, "Subirlo YA!!!", "2024-06-26 13:48:00");

        mockMvc.perform(delete("/deleteComentario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(c)))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(comentariostable).delete(any(Comentarios.class));
    }

    @Test
    @DisplayName("UT-C08 POST /buscarComentarios con datos -> lista (Éxito)")
    void testUtC08_buscarComentariosConDatos() throws Exception {
        Comentarios c1 = new Comentarios(201, "Perla", 199, "Comentario 1", "2024-06-26 13:48:00");
        Comentarios c2 = new Comentarios(202, "Liliana", 199, "Comentario 2", "2024-06-26 13:49:00");
        when(comentariostable.findAll()).thenReturn(Arrays.asList(c1, c2));

        mockMvc.perform(post("/buscarComentarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].texto_comentario").value("Comentario 1"))
                .andExpect(jsonPath("$[1].texto_comentario").value("Comentario 2"));
    }

    @Test
    @DisplayName("UT-C09 POST /buscarComentarios vacío -> lista vacía (Fracaso / Vacío)")
    void testUtC09_buscarComentariosVacio() throws Exception {
        when(comentariostable.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/buscarComentarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("UT-C10 Invocaciones directas con null -> retorna 0 sin llamar al repositorio (Fracaso)")
    void testUtC10_invocacionesConNull() {
        org.junit.jupiter.api.Assertions.assertEquals(0, comentariosController.addComentario(null));
        org.junit.jupiter.api.Assertions.assertEquals(0, comentariosController.updateComentario(null));
        org.junit.jupiter.api.Assertions.assertEquals(0, comentariosController.deleteComentario(null));

        verify(comentariostable, never()).save(any());
        verify(comentariostable, never()).delete(any());
    }
}
