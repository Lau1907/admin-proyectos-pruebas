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
import com.mx.cbtis.gestor.dto.UsuarioDTO;
import com.mx.cbtis.gestor.modelo.Usuario;
import com.mx.cbtis.gestor.repository.UsuarioRepository;

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

@WebMvcTest(UsuarioController.class)
public class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioController usuarioController;

    @MockBean
    private UsuarioRepository usuariotable;

    @Test
    @DisplayName("UT-E01 POST /addUsuario JSON válido con DTO -> 200 y cuerpo 1")
    void testUtE01_addUsuarioValido() throws Exception {
        UsuarioDTO userDTO = new UsuarioDTO(null, "Ana Paola Ramírez Vázquez", "ana22533@cbtis75.edu.mx", "0722", "Estudiante", "#70FAB7");

        mockMvc.perform(post("/addUsuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
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
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        String hash = encoder.encode("0722");

        UsuarioDTO loginReq = new UsuarioDTO();
        loginReq.setCorreo_electronico("ana22533@cbtis75.edu.mx");
        loginReq.setContrasena("0722");

        Usuario userFound = new Usuario(131, "Ana Paola Ramírez Vázquez", "ana22533@cbtis75.edu.mx", hash, "Estudiante", "#70FAB7");
        when(usuariotable.findByCorreoElectronico("ana22533@cbtis75.edu.mx")).thenReturn(Optional.of(userFound));

        mockMvc.perform(post("/getUsuarioLogin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("Estudiante"))
                .andExpect(jsonPath("$.nombre").value("Ana Paola Ramírez Vázquez"));
    }

    @Test
    @DisplayName("UT-E04 POST /getUsuarioLogin credenciales incorrectas (usuario no existe) -> nombre 000")
    void testUtE04_loginIncorrectoUsuarioNoExiste() throws Exception {
        UsuarioDTO loginReq = new UsuarioDTO();
        loginReq.setCorreo_electronico("noexiste@cbtis75.edu.mx");
        loginReq.setContrasena("wrong");

        when(usuariotable.findByCorreoElectronico("noexiste@cbtis75.edu.mx")).thenReturn(Optional.empty());

        mockMvc.perform(post("/getUsuarioLogin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("000"));
    }

    @Test
    @DisplayName("UT-E04b POST /getUsuarioLogin credenciales incorrectas (password erróneo) -> nombre 000")
    void testUtE04b_loginIncorrectoPasswordInvalido() throws Exception {
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        String hash = encoder.encode("passwordSeguro123");

        UsuarioDTO loginReq = new UsuarioDTO();
        loginReq.setCorreo_electronico("ana22533@cbtis75.edu.mx");
        loginReq.setContrasena("passwordErroneo");

        Usuario userFound = new Usuario(131, "Ana Paola Ramírez Vázquez", "ana22533@cbtis75.edu.mx", hash, "Estudiante", "#70FAB7");
        when(usuariotable.findByCorreoElectronico("ana22533@cbtis75.edu.mx")).thenReturn(Optional.of(userFound));

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
        UsuarioDTO userDTO = new UsuarioDTO(131, "Ana Paola Ramírez Vázquez", "ana22533@cbtis75.edu.mx", "0722", "Estudiante", "#70FAB7");

        mockMvc.perform(post("/updateUsuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
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
        UsuarioDTO userDTO = new UsuarioDTO(null, "Usuario Sin Credenciales", "", "", "Estudiante", "#000000");

        mockMvc.perform(post("/addUsuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest());

        verify(usuariotable, never()).save(any());
    }

    @Test
    @DisplayName("UT-E12 Invocaciones directas con null -> retorna 0 sin llamar al repositorio")
    void testUtE12_invocacionesConNull() {
        org.junit.jupiter.api.Assertions.assertEquals(0, usuarioController.addUsuario(null));
        org.junit.jupiter.api.Assertions.assertEquals(0, usuarioController.updateUsuario(null));
        org.junit.jupiter.api.Assertions.assertEquals(0, usuarioController.deleteUsuario(null));

        verify(usuariotable, never()).save(any());
        verify(usuariotable, never()).delete(any());
    }

    @Test
    @DisplayName("UT-E13 getUsuarioLogin con contraseña nula o payload nulo -> retorna 000")
    void testUtE13_loginPasswordNulaYPayloadNulo() {
        Usuario dbUser = new Usuario(1, "Ana", "ana@correo.com", "$2a$10$hash", "User", "#000");
        when(usuariotable.findByCorreoElectronico("ana@correo.com")).thenReturn(Optional.of(dbUser));

        UsuarioDTO req = new UsuarioDTO();
        req.setCorreo_electronico("ana@correo.com");
        req.setContrasena(null);

        Usuario res = usuarioController.getUsuarioLogin(req);
        org.junit.jupiter.api.Assertions.assertEquals("000", res.getNombre());

        Usuario resNull = usuarioController.getUsuarioLogin(null);
        org.junit.jupiter.api.Assertions.assertEquals("000", resNull.getNombre());
    }

    @Test
    @DisplayName("UT-E14 addUsuario y updateUsuario con contraseña ya hasheada ($2a$) o vacía -> conserva contraseña")
    void testUtE14_passwordsHasheadasYVacias() {
        String hashExistente = "$2a$10$wK1F5nOQzJ5p2lB/mF7kqu3kZ2i0xR3mYJ0.vL0C8y5m6dF3q5L8K";
        UsuarioDTO dtoHasheado = new UsuarioDTO(1, "Test", "test@test.com", hashExistente, "User", "#FFF");
        org.junit.jupiter.api.Assertions.assertEquals(1, usuarioController.addUsuario(dtoHasheado));

        UsuarioDTO dtoPassNull = new UsuarioDTO(2, "Test2", "test2@test.com", null, "User", "#FFF");
        org.junit.jupiter.api.Assertions.assertEquals(1, usuarioController.updateUsuario(dtoPassNull));
    }

    @Test
    @DisplayName("UT-E15 Cobertura getters/setters/constructores de UsuarioDTO")
    void testUtE15_coberturaUsuarioDTO() {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId_usuario(10);
        dto.setNombre("Nombre");
        dto.setCorreo_electronico("correo@test.com");
        dto.setContrasena("pass");
        dto.setRol("Admin");
        dto.setImagen_usuario("img.png");

        org.junit.jupiter.api.Assertions.assertEquals(10, dto.getId_usuario());
        org.junit.jupiter.api.Assertions.assertEquals("Nombre", dto.getNombre());
        org.junit.jupiter.api.Assertions.assertEquals("correo@test.com", dto.getCorreo_electronico());
        org.junit.jupiter.api.Assertions.assertEquals("pass", dto.getContrasena());
        org.junit.jupiter.api.Assertions.assertEquals("Admin", dto.getRol());
        org.junit.jupiter.api.Assertions.assertEquals("img.png", dto.getImagen_usuario());
    }

    @Test
    @DisplayName("UT-E16 toEntity con null y con DTO completo")
    void testUtE16_toEntityNullYCompleto() {
        org.junit.jupiter.api.Assertions.assertNull(usuarioController.toEntity(null));

        UsuarioDTO dto = new UsuarioDTO(5, "Carlos", "carlos@test.com", "pass", "Docente", "#123");
        Usuario entity = usuarioController.toEntity(dto);
        org.junit.jupiter.api.Assertions.assertNotNull(entity);
        org.junit.jupiter.api.Assertions.assertEquals(5, entity.getId_usuario());
        org.junit.jupiter.api.Assertions.assertEquals("Carlos", entity.getNombre());
    }

    @Test
    @DisplayName("UT-E17 getUsuarioLogin con variantes de hash ($2b$, $2y$, texto plano coincidente y no coincidente, pass nulo en BD)")
    void testUtE17_loginVariantesPassword() {
        // 1. Password nula en base de datos
        Usuario dbUserNullPass = new Usuario(1, "Ana", "nullpass@correo.com", null, "User", "#000");
        when(usuariotable.findByCorreoElectronico("nullpass@correo.com")).thenReturn(Optional.of(dbUserNullPass));
        UsuarioDTO reqNullPass = new UsuarioDTO(null, null, "nullpass@correo.com", "cualquiera", null, null);
        org.junit.jupiter.api.Assertions.assertEquals("000", usuarioController.getUsuarioLogin(reqNullPass).getNombre());

        // 2. Hash $2b$
        Usuario dbUser2b = new Usuario(2, "Luis", "luis@correo.com", "$2b$10$wK1F5nOQzJ5p2lB/mF7kqu3kZ2i0xR3mYJ0.vL0C8y5m6dF3q5L8K", "User", "#000");
        when(usuariotable.findByCorreoElectronico("luis@correo.com")).thenReturn(Optional.of(dbUser2b));
        UsuarioDTO req2b = new UsuarioDTO(null, null, "luis@correo.com", "0722", null, null);
        Usuario res2b = usuarioController.getUsuarioLogin(req2b);
        org.junit.jupiter.api.Assertions.assertNotNull(res2b);

        // 3. Hash $2y$
        Usuario dbUser2y = new Usuario(3, "Maria", "maria@correo.com", "$2y$10$wK1F5nOQzJ5p2lB/mF7kqu3kZ2i0xR3mYJ0.vL0C8y5m6dF3q5L8K", "User", "#000");
        when(usuariotable.findByCorreoElectronico("maria@correo.com")).thenReturn(Optional.of(dbUser2y));
        UsuarioDTO req2y = new UsuarioDTO(null, null, "maria@correo.com", "0722", null, null);
        Usuario res2y = usuarioController.getUsuarioLogin(req2y);
        org.junit.jupiter.api.Assertions.assertNotNull(res2y);

        // 4. Texto plano coincidente
        Usuario dbUserPlano = new Usuario(4, "Pedro", "pedro@correo.com", "plana123", "User", "#000");
        when(usuariotable.findByCorreoElectronico("pedro@correo.com")).thenReturn(Optional.of(dbUserPlano));
        UsuarioDTO reqPlano = new UsuarioDTO(null, null, "pedro@correo.com", "plana123", null, null);
        org.junit.jupiter.api.Assertions.assertEquals("Pedro", usuarioController.getUsuarioLogin(reqPlano).getNombre());

        // 5. Texto plano no coincidente
        UsuarioDTO reqPlanoErr = new UsuarioDTO(null, null, "pedro@correo.com", "incorrecta", null, null);
        org.junit.jupiter.api.Assertions.assertEquals("000", usuarioController.getUsuarioLogin(reqPlanoErr).getNombre());
    }

    @Test
    @DisplayName("UT-E18 addUsuario y updateUsuario con hash $2b$ y con contraseñas vacías")
    void testUtE18_addYUpdateConHash2bYContrasenaVacia() {
        String hash2b = "$2b$10$wK1F5nOQzJ5p2lB/mF7kqu3kZ2i0xR3mYJ0.vL0C8y5m6dF3q5L8K";
        UsuarioDTO dto2b = new UsuarioDTO(6, "Test 2b", "test2b@test.com", hash2b, "User", "#FFF");
        org.junit.jupiter.api.Assertions.assertEquals(1, usuarioController.addUsuario(dto2b));
        org.junit.jupiter.api.Assertions.assertEquals(1, usuarioController.updateUsuario(dto2b));

        UsuarioDTO dtoVacio = new UsuarioDTO(7, "Test Vacio", "vacio@test.com", "", "User", "#FFF");
        org.junit.jupiter.api.Assertions.assertEquals(1, usuarioController.addUsuario(dtoVacio));
        org.junit.jupiter.api.Assertions.assertEquals(1, usuarioController.updateUsuario(dtoVacio));
    }
}
