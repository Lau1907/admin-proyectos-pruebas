package com.mx.cbtis.gestor.modelo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ModeloTest {

    @Test
    @DisplayName("Test modelo Usuario: constructores, getters y setters")
    void testModeloUsuario() {
        Usuario u1 = new Usuario();
        assertThat(u1).isNotNull();

        u1.setId_usuario(1);
        u1.setNombre("Juan Perez");
        u1.setCorreo_electronico("juan@correo.com");
        u1.setContrasena("pass123");
        u1.setRol("Admin");
        u1.setImagen_usuario("#123456");

        assertThat(u1.getId_usuario()).isEqualTo(1);
        assertThat(u1.getNombre()).isEqualTo("Juan Perez");
        assertThat(u1.getCorreo_electronico()).isEqualTo("juan@correo.com");
        assertThat(u1.getContrasena()).isEqualTo("pass123");
        assertThat(u1.getRol()).isEqualTo("Admin");
        assertThat(u1.getImagen_usuario()).isEqualTo("#123456");

        Usuario u2 = new Usuario(2, "Maria Lopez", "maria@correo.com", "pass456", "User", "#654321");
        assertThat(u2.getId_usuario()).isEqualTo(2);
        assertThat(u2.getNombre()).isEqualTo("Maria Lopez");
        assertThat(u2.getCorreo_electronico()).isEqualTo("maria@correo.com");
        assertThat(u2.getContrasena()).isEqualTo("pass456");
        assertThat(u2.getRol()).isEqualTo("User");
        assertThat(u2.getImagen_usuario()).isEqualTo("#654321");
    }

    @Test
    @DisplayName("Test modelo Tareas: constructores, getters y setters")
    void testModeloTareas() {
        Tareas t1 = new Tareas();
        assertThat(t1).isNotNull();

        t1.setId_tarea(10);
        t1.setNombre("Diseño BD");
        t1.setDescripcion("Creación de tablas");
        t1.setFecha_vencimiento("2024-07-01");
        t1.setLista("pendiente");
        t1.setEstado("warning");
        t1.setId_proyecto(100);

        assertThat(t1.getId_tarea()).isEqualTo(10);
        assertThat(t1.getNombre()).isEqualTo("Diseño BD");
        assertThat(t1.getDescripcion()).isEqualTo("Creación de tablas");
        assertThat(t1.getFecha_vencimiento()).isEqualTo("2024-07-01");
        assertThat(t1.getLista()).isEqualTo("pendiente");
        assertThat(t1.getEstado()).isEqualTo("warning");
        assertThat(t1.getId_proyecto()).isEqualTo(100);

        Tareas t2 = new Tareas(20, "Frontend", "Vistas HTML", "2024-07-10", "en proceso", "success", 200);
        assertThat(t2.getId_tarea()).isEqualTo(20);
        assertThat(t2.getNombre()).isEqualTo("Frontend");
        assertThat(t2.getDescripcion()).isEqualTo("Vistas HTML");
        assertThat(t2.getFecha_vencimiento()).isEqualTo("2024-07-10");
        assertThat(t2.getLista()).isEqualTo("en proceso");
        assertThat(t2.getEstado()).isEqualTo("success");
        assertThat(t2.getId_proyecto()).isEqualTo(200);
    }

    @Test
    @DisplayName("Test modelo Proyectos: constructores, getters y setters")
    void testModeloProyectos() {
        Proyectos p1 = new Proyectos();
        assertThat(p1).isNotNull();

        p1.setId_proyecto(50);
        p1.setNombre("Sistema ERP");
        p1.setDescripcion("Gestor integral");
        p1.setFecha_inicio("2024-01-01");
        p1.setFecha_finalizacion("2024-12-31");
        p1.setEstado_proyecto("En progreso");
        p1.setFondo("fondo.png");

        assertThat(p1.getId_proyecto()).isEqualTo(50);
        assertThat(p1.getNombre()).isEqualTo("Sistema ERP");
        assertThat(p1.getDescripcion()).isEqualTo("Gestor integral");
        assertThat(p1.getFecha_inicio()).isEqualTo("2024-01-01");
        assertThat(p1.getFecha_finalizacion()).isEqualTo("2024-12-31");
        assertThat(p1.getEstado_proyecto()).isEqualTo("En progreso");
        assertThat(p1.getFondo()).isEqualTo("fondo.png");

        Proyectos p2 = new Proyectos(60, "App Movil", "App iOS y Android", "2024-03-01", "2024-09-01", "Planeacion", "bg.jpg");
        assertThat(p2.getId_proyecto()).isEqualTo(60);
        assertThat(p2.getNombre()).isEqualTo("App Movil");
        assertThat(p2.getDescripcion()).isEqualTo("App iOS y Android");
        assertThat(p2.getFecha_inicio()).isEqualTo("2024-03-01");
        assertThat(p2.getFecha_finalizacion()).isEqualTo("2024-09-01");
        assertThat(p2.getEstado_proyecto()).isEqualTo("Planeacion");
        assertThat(p2.getFondo()).isEqualTo("bg.jpg");
    }

    @Test
    @DisplayName("Test modelo Comentarios: constructores, getters y setters")
    void testModeloComentarios() {
        Comentarios c1 = new Comentarios();
        assertThat(c1).isNotNull();

        c1.setId_comentario(300);
        c1.setId_usuario("Carlos Ruiz");
        c1.setId_tarea(10);
        c1.setTexto_comentario("Buen avance");
        c1.setFecha_hora("2024-06-26 12:00:00");

        assertThat(c1.getId_comentario()).isEqualTo(300);
        assertThat(c1.getId_usuario()).isEqualTo("Carlos Ruiz");
        assertThat(c1.getId_tarea()).isEqualTo(10);
        assertThat(c1.getTexto_comentario()).isEqualTo("Buen avance");
        assertThat(c1.getFecha_hora()).isEqualTo("2024-06-26 12:00:00");

        Comentarios c2 = new Comentarios(301, "Ana Paola", 20, "Revisar logs", "2024-06-26 12:30:00");
        assertThat(c2.getId_comentario()).isEqualTo(301);
        assertThat(c2.getId_usuario()).isEqualTo("Ana Paola");
        assertThat(c2.getId_tarea()).isEqualTo(20);
        assertThat(c2.getTexto_comentario()).isEqualTo("Revisar logs");
        assertThat(c2.getFecha_hora()).isEqualTo("2024-06-26 12:30:00");
    }

    @Test
    @DisplayName("Test modelo Asignaciones: constructores, getters y setters")
    void testModeloAsignaciones() {
        Asignaciones a1 = new Asignaciones();
        assertThat(a1).isNotNull();

        a1.setId_asignacion(500);
        a1.setId_usuario(1);
        a1.setId_tarea(10);
        a1.setFecha_asignacion("2024-06-26");

        assertThat(a1.getId_asignacion()).isEqualTo(500);
        assertThat(a1.getId_usuario()).isEqualTo(1);
        assertThat(a1.getId_tarea()).isEqualTo(10);
        assertThat(a1.getFecha_asignacion()).isEqualTo("2024-06-26");

        Asignaciones a2 = new Asignaciones(501, 2, 20, "2024-06-27");
        assertThat(a2.getId_asignacion()).isEqualTo(501);
        assertThat(a2.getId_usuario()).isEqualTo(2);
        assertThat(a2.getId_tarea()).isEqualTo(20);
        assertThat(a2.getFecha_asignacion()).isEqualTo("2024-06-27");
    }
}
