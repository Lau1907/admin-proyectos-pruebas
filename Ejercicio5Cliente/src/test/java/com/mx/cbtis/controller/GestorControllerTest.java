package com.mx.cbtis.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import com.mx.cbtis.modelo.Comentarios;
import com.mx.cbtis.modelo.Proyectos;
import com.mx.cbtis.modelo.Tareas;
import com.mx.cbtis.modelo.Usuario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GestorControllerTest {

    @Mock
    private RestTemplate restTemplate;

    private GestorController controller;

    @BeforeEach
    void setUp() {
        controller = new GestorController(restTemplate);
    }

    @Test
    @DisplayName("FT-01 GET / -> retorna vista home (Éxito)")
    void testGetHome() {
        String view = controller.getHome();
        assertThat(view).isEqualTo("home");
    }

    @Test
    @DisplayName("FT-02 GET /tutorial -> retorna vista tutorial (Éxito)")
    void testGetTutorial() {
        String view = controller.getTutorial();
        assertThat(view).isEqualTo("tutorial");
    }

    @Test
    @DisplayName("FT-03 GET /manual -> retorna vista manual (Éxito)")
    void testGetManual() {
        String view = controller.getManual();
        assertThat(view).isEqualTo("manual");
    }

    @Test
    @DisplayName("FT-04 GET /login -> retorna vista login (Éxito)")
    void testGetLogin() {
        String view = controller.getLogin();
        assertThat(view).isEqualTo("login");
    }

    @Test
    @DisplayName("FT-05 GET /register -> retorna vista register y carga modelo (Éxito)")
    void testGetRegister() {
        Model model = new ExtendedModelMap();
        String view = controller.getRegister(model);
        assertThat(view).isEqualTo("register");
        assertThat(model.containsAttribute("usuario")).isTrue();
    }

    @Test
    @DisplayName("FT-06 POST /saveUsuario con formulario válido -> redirige a /login (Éxito)")
    void testSaveUsuarioValido() {
        Usuario usuario = new Usuario(null, "Ana", "ana@correo.com", "pass123", "Estudiante", "#FFFFFF");
        when(restTemplate.postForEntity(eq("http://localhost:8081/addUsuario"), eq(usuario), eq(Integer.class)))
                .thenReturn(new ResponseEntity<>(1, HttpStatus.OK));

        ModelAndView mav = controller.setSaveUsuario(usuario);
        assertThat(mav.getViewName()).isEqualTo("redirect:/login");
        verify(restTemplate).postForEntity(eq("http://localhost:8081/addUsuario"), eq(usuario), eq(Integer.class));
    }

    @Test
    @DisplayName("FT-07 GET /inicio -> retorna vista inicio y carga modelo (Éxito)")
    void testGetInicio() {
        Model model = new ExtendedModelMap();
        String view = controller.getInicio(model);
        assertThat(view).isEqualTo("inicio");
        assertThat(model.containsAttribute("proyectos")).isTrue();
    }

    @Test
    @DisplayName("FT-08 POST /saveProyecto con formulario válido -> redirige a /inicio (Éxito)")
    void testSaveProyectoValido() {
        Proyectos proyecto = new Proyectos(null, "Proyecto API", "Desc", "2024-01-01", "2024-12-31", "En proceso", "fondo.gif");
        when(restTemplate.postForEntity(eq("http://localhost:8081/addProyecto"), eq(proyecto), eq(Integer.class)))
                .thenReturn(new ResponseEntity<>(1, HttpStatus.OK));

        ModelAndView mav = controller.setSaveProyecto(proyecto);
        assertThat(mav.getViewName()).isEqualTo("redirect:/inicio");
        verify(restTemplate).postForEntity(eq("http://localhost:8081/addProyecto"), eq(proyecto), eq(Integer.class));
    }

    @Test
    @DisplayName("FT-09 POST /consultarProyectos con respuesta válida -> 200 y lista (Éxito)")
    void testConsultarProyectosValido() {
        Proyectos p1 = new Proyectos(1, "P1", "D1", "2024-01-01", "2024-12-31", "Activo", "f1.png");
        Proyectos[] array = new Proyectos[]{p1};
        when(restTemplate.postForObject(eq("http://localhost:8081/buscarProyectos"), any(HttpHeaders.class), eq(Proyectos[].class)))
                .thenReturn(array);

        Model model = new ExtendedModelMap();
        ResponseEntity<Object> response = controller.consultarProyectos(model);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        List<?> lista = (List<?>) response.getBody();
        assertThat(lista).hasSize(1);
        assertThat(model.containsAttribute("listaProyectos")).isTrue();
    }

    @Test
    @DisplayName("FT-10 POST /consultarProyectos cuando backend devuelve null -> lista vacía (Fracaso controlado)")
    void testConsultarProyectosBackendNull() {
        when(restTemplate.postForObject(eq("http://localhost:8081/buscarProyectos"), any(HttpHeaders.class), eq(Proyectos[].class)))
                .thenReturn(null);

        Model model = new ExtendedModelMap();
        ResponseEntity<Object> response = controller.consultarProyectos(model);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        List<?> lista = (List<?>) response.getBody();
        assertThat(lista).isEmpty();
    }

    @Test
    @DisplayName("FT-11 POST /eliminarProyecto -> llama DELETE y redirige a /inicio (Éxito)")
    void testEliminarProyecto() {
        Proyectos proyecto = new Proyectos();
        proyecto.setId_proyecto(10);
        when(restTemplate.exchange(eq("http://localhost:8081/deleteProyecto"), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Integer.class)))
                .thenReturn(new ResponseEntity<>(1, HttpStatus.OK));

        ModelAndView mav = controller.eliminarProyecto(proyecto);
        assertThat(mav.getViewName()).isEqualTo("redirect:/inicio");
        verify(restTemplate).exchange(eq("http://localhost:8081/deleteProyecto"), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Integer.class));
    }

    @Test
    @DisplayName("FT-12 POST /actualizarProyecto recurso existente -> 200 con proyecto (Éxito)")
    void testActualizarProyectoExistente() {
        Proyectos p = new Proyectos(10, "P10", "D10", "2024-01-01", "2024-12-31", "Activo", "f.png");
        when(restTemplate.getForObject(eq("http://localhost:8081/buscarProyecto?id_proyecto={id_proyecto}"), eq(Proyectos.class), anyMap()))
                .thenReturn(p);

        Proyectos req = new Proyectos();
        req.setId_proyecto(10);
        ResponseEntity<Object> response = controller.updateProyecto(req);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(p);
    }

    @Test
    @DisplayName("FT-13 POST /actualizarProyecto recurso inexistente -> 200 con null (Fracaso / Inexistente)")
    void testActualizarProyectoInexistente() {
        when(restTemplate.getForObject(eq("http://localhost:8081/buscarProyecto?id_proyecto={id_proyecto}"), eq(Proyectos.class), anyMap()))
                .thenReturn(null);

        Proyectos req = new Proyectos();
        req.setId_proyecto(9999);
        ResponseEntity<Object> response = controller.updateProyecto(req);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNull();
    }

    @Test
    @DisplayName("FT-14 POST /verificarLogin con credenciales correctas -> usuario válido (Éxito)")
    void testVerificarLoginCorrecto() {
        Usuario req = new Usuario(null, null, "ana@correo.com", "0722", null, null);
        Usuario res = new Usuario(1, "Ana", "ana@correo.com", "hash", "Estudiante", "#FFF");
        when(restTemplate.postForEntity(eq("http://localhost:8081/getUsuarioLogin"), eq(req), eq(Usuario.class)))
                .thenReturn(new ResponseEntity<>(res, HttpStatus.OK));

        ResponseEntity<Usuario> response = controller.verificarUsuario(req);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getNombre()).isEqualTo("Ana");
    }

    @Test
    @DisplayName("FT-15 POST /verificarLogin con credenciales incorrectas -> nombre 000 (Fracaso / Login inválido)")
    void testVerificarLoginIncorrecto() {
        Usuario req = new Usuario(null, null, "ana@correo.com", "badpass", null, null);
        Usuario res = new Usuario();
        res.setNombre("000");
        when(restTemplate.postForEntity(eq("http://localhost:8081/getUsuarioLogin"), eq(req), eq(Usuario.class)))
                .thenReturn(new ResponseEntity<>(res, HttpStatus.OK));

        ResponseEntity<Usuario> response = controller.verificarUsuario(req);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getNombre()).isEqualTo("000");
    }

    @Test
    @DisplayName("FT-16 POST /consultarUsuarios con respuesta válida -> 200 y lista (Éxito)")
    void testConsultarUsuariosValido() {
        Usuario u1 = new Usuario(1, "Ana", "ana@cbtis.com", "p", "Rol", "#000");
        Usuario[] array = new Usuario[]{u1};
        when(restTemplate.postForObject(eq("http://localhost:8081/buscarUsuarios"), any(HttpHeaders.class), eq(Usuario[].class)))
                .thenReturn(array);

        Model model = new ExtendedModelMap();
        ResponseEntity<Object> response = controller.consultarUsuarios(model);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        List<?> lista = (List<?>) response.getBody();
        assertThat(lista).hasSize(1);
    }

    @Test
    @DisplayName("FT-17 POST /obtenerTareas con respuesta válida -> 200 y lista (Éxito)")
    void testObtenerTareasValido() {
        Tareas t1 = new Tareas(1, "T1", "D1", "2024-01-01", "p", "ok", 10);
        Tareas[] array = new Tareas[]{t1};
        when(restTemplate.postForObject(eq("http://localhost:8081/getTarea"), any(Tareas.class), eq(Tareas[].class)))
                .thenReturn(array);

        Model model = new ExtendedModelMap();
        ResponseEntity<List<Tareas>> response = controller.obtenerTareas(new Tareas(), model);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("FT-18 POST /obtenerComentarios con respuesta válida -> 200 y lista (Éxito)")
    void testObtenerComentariosValido() {
        Comentarios c1 = new Comentarios(1, "User1", 1, "Comentario 1", "2024-01-01");
        Comentarios[] array = new Comentarios[]{c1};
        when(restTemplate.postForObject(eq("http://localhost:8081/getComentario"), any(Comentarios.class), eq(Comentarios[].class)))
                .thenReturn(array);

        Model model = new ExtendedModelMap();
        ResponseEntity<List<Comentarios>> response = controller.obtenerComentarios(new Comentarios(), model);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("FT-19 GET /proyecto -> carga tarea, comentarios y usuarios (Éxito)")
    void testGetProyecto() {
        Usuario[] array = new Usuario[]{new Usuario(1, "Ana", "a@c.com", "p", "r", "#0")};
        when(restTemplate.postForObject(eq("http://localhost:8081/buscarUsuarios"), any(HttpHeaders.class), eq(Usuario[].class)))
                .thenReturn(array);

        Model model = new ExtendedModelMap();
        String view = controller.getProyecto(model);

        assertThat(view).isEqualTo("proyecto");
        assertThat(model.containsAttribute("tarea")).isTrue();
        assertThat(model.containsAttribute("comentarios")).isTrue();
        assertThat(model.containsAttribute("listaUsuarios")).isTrue();
    }

    @Test
    @DisplayName("FT-20 GET /tarea -> carga modelo y retorna proyecto (Éxito)")
    void testGetTarea() {
        Model model = new ExtendedModelMap();
        String view = controller.getTarea(model);
        assertThat(view).isEqualTo("proyecto");
        assertThat(model.containsAttribute("tareas")).isTrue();
    }

    @Test
    @DisplayName("FT-21 POST /saveTarea válido -> redirige a /proyecto con id_proyecto (Éxito)")
    void testSaveTareaValido() {
        Tareas tarea = new Tareas(null, "T1", "D1", "2024-01-01", "p", "ok", 15);
        when(restTemplate.postForEntity(eq("http://localhost:8081/addTareas"), eq(tarea), eq(Integer.class)))
                .thenReturn(new ResponseEntity<>(1, HttpStatus.OK));

        ModelAndView mav = controller.setSaveTareas(tarea);
        assertThat(mav.getViewName()).isEqualTo("redirect:/proyecto");
        assertThat(mav.getModel().get("id_proyecto")).isEqualTo(15);
    }

    @Test
    @DisplayName("FT-22 POST /saveComentario válido -> retorna vista proyecto (Éxito)")
    void testSaveComentarioValido() {
        Comentarios c = new Comentarios(null, "User", 1, "Texto", "2024-01-01");
        when(restTemplate.postForEntity(eq("http://localhost:8081/addComentario"), eq(c), eq(Integer.class)))
                .thenReturn(new ResponseEntity<>(1, HttpStatus.OK));

        String view = controller.setSaveComentario(c);
        assertThat(view).isEqualTo("proyecto");
    }

    @Test
    @DisplayName("FT-23 POST /actualizarTarea -> retorna tarea del backend (Éxito)")
    void testActualizarTareaValido() {
        Tareas t = new Tareas(5, "T5", "D5", "2024-01-01", "p", "ok", 1);
        when(restTemplate.getForObject(eq("http://localhost:8081/buscarTarea?id_tarea={id_tarea}"), eq(Tareas.class), anyMap()))
                .thenReturn(t);

        Tareas req = new Tareas();
        req.setId_tarea(5);
        ResponseEntity<Object> response = controller.updateTarea(req);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(t);
    }

    @Test
    @DisplayName("FT-24 POST /eliminarTarea -> exchange DELETE y redirect:/proyecto (Éxito)")
    void testEliminarTareaValido() {
        Tareas t = new Tareas();
        t.setId_tarea(5);
        when(restTemplate.exchange(eq("http://localhost:8081/deleteTareas"), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Integer.class)))
                .thenReturn(new ResponseEntity<>(1, HttpStatus.OK));

        ModelAndView mav = controller.eliminarTarea(t);
        assertThat(mav.getViewName()).isEqualTo("redirect:/proyecto");
    }

    @Test
    @DisplayName("FT-25 POST /verificarTarea y /cambiosTareas -> responde estado (Éxito)")
    void testVerificarYCambiosTareas() {
        Tareas t = new Tareas();
        when(restTemplate.postForEntity(eq("http://localhost:8081/addTareas"), eq(t), eq(Integer.class)))
                .thenReturn(new ResponseEntity<>(1, HttpStatus.OK));

        ResponseEntity<Integer> r1 = controller.verificarTarea(t);
        ResponseEntity<Integer> r2 = controller.cambiosTareas(t);

        assertThat(r1.getBody()).isEqualTo(1);
        assertThat(r2.getBody()).isEqualTo(1);
    }

    @Test
    @DisplayName("FT-26 POST /consultarTareas y /consultarComentarios -> listas (Éxito)")
    void testConsultarTareasYComentarios() {
        when(restTemplate.postForObject(eq("http://localhost:8081/buscarTareas"), any(HttpHeaders.class), eq(Tareas[].class)))
                .thenReturn(new Tareas[]{new Tareas()});
        when(restTemplate.postForObject(eq("http://localhost:8081/buscarComentarios"), any(HttpHeaders.class), eq(Comentarios[].class)))
                .thenReturn(new Comentarios[]{new Comentarios()});

        ResponseEntity<Object> r1 = controller.consultarTareas();
        ResponseEntity<Object> r2 = controller.consultarComentarios();

        assertThat(((List<?>) r1.getBody())).hasSize(1);
        assertThat(((List<?>) r2.getBody())).hasSize(1);
    }

    @Test
    @DisplayName("FT-27 Error de backend capturado -> propaga RestClientException (Fracaso / Error backend)")
    void testErrorDeBackendCapturado() {
        when(restTemplate.postForEntity(anyString(), any(), eq(Integer.class)))
                .thenThrow(new RestClientException("Connection refused"));

        assertThrows(RestClientException.class, () -> controller.setSaveUsuario(new Usuario()));
    }

    @Test
    @DisplayName("FT-28 Constructores y setter de GestorController")
    void testConstructoresYSetter() {
        GestorController c1 = new GestorController();
        assertThat(c1.getTemplate()).isNotNull();

        GestorController c2 = new GestorController(null);
        assertThat(c2.getTemplate()).isNotNull();

        c2.setTemplate(restTemplate);
        assertThat(c2.getTemplate()).isEqualTo(restTemplate);
    }
}

