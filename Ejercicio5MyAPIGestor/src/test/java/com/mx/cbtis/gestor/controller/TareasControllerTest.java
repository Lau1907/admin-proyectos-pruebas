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

    @MockBean
    private TareasRepository tareastable;

    @Test
    @DisplayName("UT-E17 POST /addTareas válido -> 200 y 1")
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
    @DisplayName("UT-E18 GET /buscarTarea?id_tarea=199 -> tarea")
    void testUtE18_buscarTareaExistente() throws Exception {
        Tareas tarea = new Tareas(199, "Base de datos", "Realizar la estructura de las tablas", "2024-06-13", "terminado", "danger", 197);
        when(tareastable.findById(199)).thenReturn(Optional.of(tarea));

        mockMvc.perform(get("/buscarTarea").param("id_tarea", "199"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_tarea").value(199))
                .andExpect(jsonPath("$.nombre").value("Base de datos"));
    }

    @Test
    @DisplayName("UT-E19 POST /getTarea con id_proyecto=197 -> lista de 2")
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
    @DisplayName("UT-E20 POST /getTarea de proyecto sin tareas -> lista vacía")
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
    @DisplayName("UT-E21 GET /buscarTarea?id_tarea=abc -> 400")
    void testUtE21_buscarTareaTipoInvalido() throws Exception {
        mockMvc.perform(get("/buscarTarea").param("id_tarea", "abc"))
                .andExpect(status().isBadRequest());
    }
}
