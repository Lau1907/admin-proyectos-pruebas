package com.mx.cbtis.gestor.controller;

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
import com.mx.cbtis.gestor.modelo.Proyectos;
import com.mx.cbtis.gestor.repository.ProtectosRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProyectosController.class)
public class ProyectosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProtectosRepository proyectostable;

    @Test
    @DisplayName("UT-E12 POST /addProyecto válido -> 200 y 1")
    void testUtE12_addProyectoValido() throws Exception {
        Proyectos p = new Proyectos(null, "My API Rest", "Desarrollaran un Api Rest", "2024-05-31", "2024-06-29", "En proceso", "/Images/animacion1.gif");

        mockMvc.perform(post("/addProyecto")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(proyectostable).save(any(Proyectos.class));
    }

    @Test
    @DisplayName("UT-E13 GET /buscarProyecto?id_proyecto=197 -> proyecto")
    void testUtE13_buscarProyectoExistente() throws Exception {
        Proyectos p = new Proyectos(197, "My API Rest", "Desarrollaran un Api Rest", "2024-05-31", "2024-06-29", "En proceso", "/Images/animacion1.gif");
        when(proyectostable.findById(197)).thenReturn(Optional.of(p));

        mockMvc.perform(get("/buscarProyecto").param("id_proyecto", "197"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_proyecto").value(197))
                .andExpect(jsonPath("$.nombre").value("My API Rest"));
    }

    @Test
    @DisplayName("UT-E14 POST /buscarProyectos con tabla vacía -> lista vacía")
    void testUtE14_buscarProyectosVacio() throws Exception {
        when(proyectostable.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/buscarProyectos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("UT-E15 DELETE /deleteProyecto -> 200 y 1")
    void testUtE15_deleteProyecto() throws Exception {
        Proyectos p = new Proyectos(197, "My API Rest", "Desarrollaran un Api Rest", "2024-05-31", "2024-06-29", "En proceso", "/Images/animacion1.gif");

        mockMvc.perform(delete("/deleteProyecto")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(proyectostable).delete(any(Proyectos.class));
    }

    @Test
    @DisplayName("UT-E16 GET /buscarProyecto sin parámetro -> 400")
    void testUtE16_buscarProyectoSinParametro() throws Exception {
        mockMvc.perform(get("/buscarProyecto"))
                .andExpect(status().isBadRequest());
    }
}
