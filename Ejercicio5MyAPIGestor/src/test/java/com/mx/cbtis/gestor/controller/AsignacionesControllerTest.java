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
import com.mx.cbtis.gestor.modelo.Asignaciones;
import com.mx.cbtis.gestor.repository.AsignacionesRepository;

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

@WebMvcTest(AsignacionesController.class)
public class AsignacionesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AsignacionesController asignacionesController;

    @MockBean
    private AsignacionesRepository asignacionestable;

    @Test
    @DisplayName("UT-A01 POST /addAsignacion válido -> 200 y 1 (Éxito)")
    void testUtA01_addAsignacionValido() throws Exception {
        Asignaciones asig = new Asignaciones(null, 131, 199, "2024-06-26");

        mockMvc.perform(post("/addAsignacion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(asig)))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(asignacionestable).save(any(Asignaciones.class));
    }

    @Test
    @DisplayName("UT-A02 GET /buscarAsignacion?id=1 existente -> asignacion encontrada (Éxito)")
    void testUtA02_buscarAsignacionExistente() throws Exception {
        Asignaciones asig = new Asignaciones(1, 131, 199, "2024-06-26");
        when(asignacionestable.findById(1)).thenReturn(Optional.of(asig));

        mockMvc.perform(get("/buscarAsignacion").param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_asignacion").value(1))
                .andExpect(jsonPath("$.id_usuario").value(131))
                .andExpect(jsonPath("$.id_tarea").value(199));
    }

    @Test
    @DisplayName("UT-A03 GET /buscarAsignacion?id=999 inexistente -> objeto vacío controlado (Fracaso controlado)")
    void testUtA03_buscarAsignacionInexistente() throws Exception {
        when(asignacionestable.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/buscarAsignacion").param("id", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_asignacion").doesNotExist())
                .andExpect(jsonPath("$.id_usuario").doesNotExist());
    }

    @Test
    @DisplayName("UT-A04 POST /updateAsignacion válido -> 200 y 1 (Éxito)")
    void testUtA04_updateAsignacionValido() throws Exception {
        Asignaciones asig = new Asignaciones(1, 172, 199, "2024-06-27");

        mockMvc.perform(post("/updateAsignacion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(asig)))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(asignacionestable).save(any(Asignaciones.class));
    }

    @Test
    @DisplayName("UT-A05 DELETE /deleteAsignacion válido -> 200 y 1 (Éxito)")
    void testUtA05_deleteAsignacionValido() throws Exception {
        Asignaciones asig = new Asignaciones(1, 131, 199, "2024-06-26");

        mockMvc.perform(delete("/deleteAsignacion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(asig)))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(asignacionestable).delete(any(Asignaciones.class));
    }

    @Test
    @DisplayName("UT-A06 POST /buscarAsignaciones con datos -> lista (Éxito)")
    void testUtA06_buscarAsignacionesConDatos() throws Exception {
        Asignaciones a1 = new Asignaciones(1, 131, 199, "2024-06-26");
        Asignaciones a2 = new Asignaciones(2, 172, 199, "2024-06-27");
        when(asignacionestable.findAll()).thenReturn(Arrays.asList(a1, a2));

        mockMvc.perform(post("/buscarAsignaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id_asignacion").value(1))
                .andExpect(jsonPath("$[1].id_asignacion").value(2));
    }

    @Test
    @DisplayName("UT-A07 POST /buscarAsignaciones vacía -> lista vacía (Fracaso / Vacío)")
    void testUtA07_buscarAsignacionesVacio() throws Exception {
        when(asignacionestable.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/buscarAsignaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("UT-A08 Invocaciones directas con null -> retorna 0 sin llamar al repositorio (Fracaso)")
    void testUtA08_invocacionesConNull() {
        org.junit.jupiter.api.Assertions.assertEquals(0, asignacionesController.addAsignacion(null));
        org.junit.jupiter.api.Assertions.assertEquals(0, asignacionesController.updateAsignacion(null));
        org.junit.jupiter.api.Assertions.assertEquals(0, asignacionesController.deleteAsignacion(null));

        verify(asignacionestable, never()).save(any());
        verify(asignacionestable, never()).delete(any());
    }
}
