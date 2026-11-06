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
import com.mx.cbtis.gestor.modelo.Tareas;
import com.mx.cbtis.gestor.repository.TareasRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TareasController.class)
public class TareasControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TareasController tareasController;

    @MockBean
    private TareasRepository tareastable;

    @Test
    @DisplayName("UT-E17 POST /addTareas válido -> 200 y 1 (Éxito)")
    void testUtE17_addTareasValido() throws Exception {
        Tareas tarea = new Tareas(null, "Base de datos", "Realizar la estructura de las tablas", "2024-06-13", "terminado", "danger", 197);

        mockMvc.perform(post("/addTareas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tarea)))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(tareastable).save(any(Tareas.class));
    }

    @Test
    @DisplayName("UT-E18 GET /buscarTarea?id_tarea=199 existente -> retorna tarea (Éxito)")
    void testUtE18_buscarTareaExistente() throws Exception {
        Tareas tarea = new Tareas(199, "Base de datos", "Realizar la estructura de las tablas", "2024-06-13", "terminado", "danger", 197);
        when(tareastable.findById(199)).thenReturn(Optional.of(tarea));

        mockMvc.perform(get("/buscarTarea").param("id_tarea", "199"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_tarea").value(199))
                .andExpect(jsonPath("$.nombre").value("Base de datos"));
    }

    @Test
    @DisplayName("UT-E18b GET /buscarTarea?id_tarea=999 inexistente -> objeto vacío controlado (Fracaso controlado)")
    void testUtE18b_buscarTareaInexistente() throws Exception {
        when(tareastable.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/buscarTarea").param("id_tarea", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_tarea").doesNotExist())
                .andExpect(jsonPath("$.nombre").doesNotExist());
    }

    @Test
    @DisplayName("UT-E19 POST /getTarea con id_proyecto=197 -> lista de 2 (Éxito)")
    void testUtE19_getTareaListaDeDos() throws Exception {
        Tareas req = new Tareas();
        req.setId_proyecto(197);

        Tareas t1 = new Tareas(199, "Base de datos", "Realizar la estructura de las tablas", "2024-06-13", "terminado", "danger", 197);
        Tareas t2 = new Tareas(200, "Endpoints API", "Desarrollar controladores", "2024-06-15", "en proceso", "warning", 197);

        when(tareastable.findByIdProyecto(197)).thenReturn(Arrays.asList(t1, t2));

        mockMvc.perform(post("/getTarea")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Base de datos"))
                .andExpect(jsonPath("$[1].nombre").value("Endpoints API"));
    }

    @Test
    @DisplayName("UT-E20 POST /getTarea de proyecto sin tareas -> lista vacía (Fracaso / Sin registros)")
    void testUtE20_getTareaProyectoSinTareas() throws Exception {
        Tareas req = new Tareas();
        req.setId_proyecto(999);

        when(tareastable.findByIdProyecto(999)).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/getTarea")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("UT-E21 GET /buscarTarea?id_tarea=abc inválido -> 400 (Fracaso)")
    void testUtE21_buscarTareaTipoInvalido() throws Exception {
        mockMvc.perform(get("/buscarTarea").param("id_tarea", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("UT-E22 POST /updateTareas válido -> 200 y 1 (Éxito)")
    void testUtE22_updateTareasValido() throws Exception {
        Tareas tarea = new Tareas(199, "Base de datos modificada", "Descripción actualizada", "2024-06-20", "en proceso", "warning", 197);

        mockMvc.perform(post("/updateTareas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tarea)))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(tareastable).save(any(Tareas.class));
    }

    @Test
    @DisplayName("UT-E23 DELETE /deleteTareas válido -> 200 y 1 (Éxito)")
    void testUtE23_deleteTareasValido() throws Exception {
        Tareas tarea = new Tareas(199, "Base de datos", "Realizar estructura", "2024-06-13", "terminado", "danger", 197);

        mockMvc.perform(delete("/deleteTareas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tarea)))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(tareastable).delete(any(Tareas.class));
    }

    @Test
    @DisplayName("UT-E24 POST /buscarTareas con elementos -> lista (Éxito)")
    void testUtE24_buscarTareasConElementos() throws Exception {
        Tareas t1 = new Tareas(199, "Tarea 1", "Desc 1", "2024-06-13", "terminado", "danger", 197);
        Tareas t2 = new Tareas(200, "Tarea 2", "Desc 2", "2024-06-15", "en proceso", "warning", 197);

        when(tareastable.findAll()).thenReturn(Arrays.asList(t1, t2));

        mockMvc.perform(post("/buscarTareas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Tarea 1"))
                .andExpect(jsonPath("$[1].nombre").value("Tarea 2"));
    }

    @Test
    @DisplayName("UT-E25 POST /buscarTareas sin registros -> lista vacía (Fracaso / Vacío)")
    void testUtE25_buscarTareasVacio() throws Exception {
        when(tareastable.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/buscarTareas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("UT-E26 Invocaciones directas con null -> retorna 0 (Fracaso)")
    void testUtE26_invocacionesConNull() {
        org.junit.jupiter.api.Assertions.assertEquals(0, tareasController.addTareas(null));
        org.junit.jupiter.api.Assertions.assertEquals(0, tareasController.updateTareas(null));
        org.junit.jupiter.api.Assertions.assertEquals(0, tareasController.deleteTareas(null));
    }
}
