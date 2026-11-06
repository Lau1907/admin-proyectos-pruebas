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

    @Autowired
    private ProyectosController proyectosController;

    @MockBean
    private ProtectosRepository proyectostable;

    @Test
    @DisplayName("UT-E12 POST /addProyecto válido -> 200 y 1 (Éxito)")
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
    @DisplayName("UT-E13 GET /buscarProyecto?id_proyecto=197 -> proyecto encontrado (Éxito)")
    void testUtE13_buscarProyectoExistente() throws Exception {
        Proyectos p = new Proyectos(197, "My API Rest", "Desarrollaran un Api Rest", "2024-05-31", "2024-06-29", "En proceso", "/Images/animacion1.gif");
        when(proyectostable.findById(197)).thenReturn(Optional.of(p));

        mockMvc.perform(get("/buscarProyecto").param("id_proyecto", "197"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_proyecto").value(197))
                .andExpect(jsonPath("$.nombre").value("My API Rest"));
    }

    @Test
    @DisplayName("UT-E13b GET /buscarProyecto?id_proyecto=9999 -> objeto vacío controlado (Fracaso controlado)")
    void testUtE13b_buscarProyectoInexistente() throws Exception {
        when(proyectostable.findById(9999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/buscarProyecto").param("id_proyecto", "9999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_proyecto").doesNotExist())
                .andExpect(jsonPath("$.nombre").doesNotExist());
    }

    @Test
    @DisplayName("UT-E14 POST /buscarProyectos con tabla vacía -> lista vacía (Fracaso / Vacío)")
    void testUtE14_buscarProyectosVacio() throws Exception {
        when(proyectostable.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/buscarProyectos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("UT-E14b POST /buscarProyectos con datos -> lista con proyectos (Éxito)")
    void testUtE14b_buscarProyectosConDatos() throws Exception {
        Proyectos p1 = new Proyectos(197, "API Rest", "Desc 1", "2024-05-31", "2024-06-29", "En proceso", "/Images/animacion1.gif");
        Proyectos p2 = new Proyectos(198, "Frontend Web", "Desc 2", "2024-06-01", "2024-06-30", "Terminado", "/Images/animacion2.gif");

        when(proyectostable.findAll()).thenReturn(Arrays.asList(p1, p2));

        mockMvc.perform(post("/buscarProyectos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("API Rest"))
                .andExpect(jsonPath("$[1].nombre").value("Frontend Web"));
    }

    @Test
    @DisplayName("UT-E15 DELETE /deleteProyecto -> 200 y 1 (Éxito)")
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
    @DisplayName("UT-E15b POST /updateProyecto -> 200 y 1 (Éxito)")
    void testUtE15b_updateProyecto() throws Exception {
        Proyectos p = new Proyectos(197, "My API Rest Actualizada", "Nueva descripción", "2024-05-31", "2024-07-15", "En revisión", "/Images/animacion1.gif");

        mockMvc.perform(post("/updateProyecto")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(proyectostable).save(any(Proyectos.class));
    }

    @Test
    @DisplayName("UT-E16 GET /buscarProyecto sin parámetro -> 400 (Fracaso)")
    void testUtE16_buscarProyectoSinParametro() throws Exception {
        mockMvc.perform(get("/buscarProyecto"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("UT-E17 Invocaciones directas con null -> retorna 0 (Fracaso)")
    void testUtE17_invocacionesConNull() {
        org.junit.jupiter.api.Assertions.assertEquals(0, proyectosController.addProyecto(null));
        org.junit.jupiter.api.Assertions.assertEquals(0, proyectosController.updateProyecto(null));
        org.junit.jupiter.api.Assertions.assertEquals(0, proyectosController.deleteProyecto(null));
    }
}
