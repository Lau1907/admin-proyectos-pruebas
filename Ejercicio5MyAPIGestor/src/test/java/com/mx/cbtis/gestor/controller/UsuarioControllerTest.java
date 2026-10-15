package com.mx.cbtis.gestor.controller;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mx.cbtis.gestor.modelo.Usuario;
import com.mx.cbtis.gestor.repository.UsuarioRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
public class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioRepository usuariotable;

    @Test
    @DisplayName("UT-E01 POST /addUsuario JSON válido -> 200 y cuerpo 1")
    void testUtE01_addUsuarioValido() throws Exception {
        Usuario user = new Usuario(null, "Ana Paola Ramírez Vázquez", "ana22533@cbtis75.edu.mx", "0722", "Estudiante", "#70FAB7");

        mockMvc.perform(post("/addUsuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(usuariotable).save(any(Usuario.class));
    }

    @Test
    @DisplayName("UT-E02 GET /buscarUsuario?id=131 -> 200 con el usuario")
    void testUtE02_buscarUsuarioExistente() throws Exception {
        Usuario user = new Usuario(131, "Ana Paola Ramírez Vázquez", "ana22533@cbtis75.edu.mx", "0722", "Estudiante", "#70FAB7");
        when(usuariotable.findById(131)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/buscarUsuario").param("id", "131"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_usuario").value(131))
                .andExpect(jsonPath("$.nombre").value("Ana Paola Ramírez Vázquez"));
    }

    @Test
    @DisplayName("UT-E03 POST /getUsuarioLogin credenciales correctas -> usuario con su rol")
    void testUtE03_loginCorrecto() throws Exception {
        Usuario loginReq = new Usuario();
        loginReq.setCorreo_electronico("ana22533@cbtis75.edu.mx");
        loginReq.setContrasena("0722");

        Usuario userFound = new Usuario(131, "Ana Paola Ramírez Vázquez", "ana22533@cbtis75.edu.mx", "0722", "Estudiante", "#70FAB7");
        when(usuariotable.findByCorreoPass("ana22533@cbtis75.edu.mx", "0722")).thenReturn(Optional.of(userFound));

        mockMvc.perform(post("/getUsuarioLogin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("Estudiante"))
                .andExpect(jsonPath("$.nombre").value("Ana Paola Ramírez Vázquez"));
    }

    @Test
    @DisplayName("UT-E04 POST /getUsuarioLogin credenciales incorrectas -> nombre 000")
    void testUtE04_loginIncorrecto() throws Exception {
        Usuario loginReq = new Usuario();
        loginReq.setCorreo_electronico("ana22533@cbtis75.edu.mx");
        loginReq.setContrasena("wrong");

        when(usuariotable.findByCorreoPass("ana22533@cbtis75.edu.mx", "wrong")).thenReturn(Optional.empty());

        mockMvc.perform(post("/getUsuarioLogin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("000"));
    }

    @Test
    @DisplayName("UT-E05 POST /buscarUsuarios -> lista de 2")
    void testUtE05_buscarUsuarios() throws Exception {
        Usuario u1 = new Usuario(131, "Ana Paola Ramírez Vázquez", "ana22533@cbtis75.edu.mx", "0722", "Estudiante", "#70FAB7");
        Usuario u2 = new Usuario(172, "Liliana Gómez Martínez", "liliana22512@cbtis75.edu.mx", "ada", "Estudiante", "#54EA01");

        when(usuariotable.findAll()).thenReturn(Arrays.asList(u1, u2));

        mockMvc.perform(post("/buscarUsuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Ana Paola Ramírez Vázquez"))
                .andExpect(jsonPath("$[1].nombre").value("Liliana Gómez Martínez"));
    }

    @Test
    @DisplayName("UT-E06 POST /updateUsuario -> 200 y 1")
    void testUtE06_updateUsuario() throws Exception {
        Usuario user = new Usuario(131, "Ana Paola Ramírez Vázquez", "ana22533@cbtis75.edu.mx", "0722", "Estudiante", "#70FAB7");

        mockMvc.perform(post("/updateUsuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(usuariotable).save(any(Usuario.class));
    }

    @Test
    @DisplayName("UT-E07 DELETE /deleteUsuario con cuerpo JSON -> 200 y 1")
    void testUtE07_deleteUsuario() throws Exception {
        Usuario user = new Usuario(131, "Ana Paola Ramírez Vázquez", "ana22533@cbtis75.edu.mx", "0722", "Estudiante", "#70FAB7");

        mockMvc.perform(delete("/deleteUsuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(usuariotable).delete(any(Usuario.class));
    }

    @Test
    @DisplayName("UT-E08 POST /addUsuario con JSON mal formado -> 400 y nunca llama a save")
    void testUtE08_addUsuarioJsonMalFormado() throws Exception {
        mockMvc.perform(post("/addUsuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{nombre: 'sin comillas' bad_json}"))
                .andExpect(status().isBadRequest());

        verify(usuariotable, never()).save(any());
    }

    @Test
    @DisplayName("UT-E09 POST /addUsuario sin cuerpo -> 400")
    void testUtE09_addUsuarioSinCuerpo() throws Exception {
        mockMvc.perform(post("/addUsuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("UT-E10 GET /buscarUsuario?id=99999 (inexistente) -> debe responder 404")
    void testUtE10_buscarUsuarioInexistente404() throws Exception {
        when(usuariotable.findById(99999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/buscarUsuario").param("id", "99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("UT-E11 POST /addUsuario con correo y contraseña vacíos -> debe responder 400 y nunca llamar a save")
    void testUtE11_addUsuarioCorreoPassVacios400() throws Exception {
        Usuario user = new Usuario(null, "Usuario Sin Credenciales", "", "", "Estudiante", "#000000");

        mockMvc.perform(post("/addUsuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());

        verify(usuariotable, never()).save(any());
    }
}
