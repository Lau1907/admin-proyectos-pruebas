package com.mx.cbtis.modelo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ClienteModeloTest {

    @Test
    @DisplayName("Test modelo Usuario en Cliente: constructores, getters y setters")
    void testModeloUsuario() {
        Usuario u1 = new Usuario();
        assertThat(u1).isNotNull();

        u1.setId_usuario(1);
        u1.setNombre("Ana");
        u1.setCorreo_electronico("ana@correo.com");
        u1.setContrasena("pass");
        u1.setRol("Estudiante");
        u1.setImagen_usuario("#FFF");

        assertThat(u1.getId_usuario()).isEqualTo(1);
        assertThat(u1.getNombre()).isEqualTo("Ana");
        assertThat(u1.getCorreo_electronico()).isEqualTo("ana@correo.com");
        assertThat(u1.getContrasena()).isEqualTo("pass");
        assertThat(u1.getRol()).isEqualTo("Estudiante");
        assertThat(u1.getImagen_usuario()).isEqualTo("#FFF");

        Usuario u2 = new Usuario(2, "Luis", "luis@correo.com", "pass2", "Docente", "#000");
        assertThat(u2.getId_usuario()).isEqualTo(2);
        assertThat(u2.getNombre()).isEqualTo("Luis");
        assertThat(u2.getCorreo_electronico()).isEqualTo("luis@correo.com");
        assertThat(u2.getContrasena()).isEqualTo("pass2");
        assertThat(u2.getRol()).isEqualTo("Docente");
        assertThat(u2.getImagen_usuario()).isEqualTo("#000");
    }

    @Test
    @DisplayName("Test modelo Tareas en Cliente: constructores, getters y setters")
    void testModeloTareas() {
        Tareas t1 = new Tareas();
        assertThat(t1).isNotNull();

        t1.setId_tarea(10);
        t1.setNombre("Tarea 1");
        t1.setDescripcion("Desc");
        t1.setFecha_vencimiento("2024-06-30");
        t1.setLista("pendiente");
        t1.setEstado("danger");
        t1.setId_proyecto(5);

        assertThat(t1.getId_tarea()).isEqualTo(10);
        assertThat(t1.getNombre()).isEqualTo("Tarea 1");
        assertThat(t1.getDescripcion()).isEqualTo("Desc");
        assertThat(t1.getFecha_vencimiento()).isEqualTo("2024-06-30");
        assertThat(t1.getLista()).isEqualTo("pendiente");
        assertThat(t1.getEstado()).isEqualTo("danger");
        assertThat(t1.getId_proyecto()).isEqualTo(5);

        Tareas t2 = new Tareas(20, "Tarea 2", "Desc 2", "2024-07-01", "terminado", "success", 6);
        assertThat(t2.getId_tarea()).isEqualTo(20);
        assertThat(t2.getNombre()).isEqualTo("Tarea 2");
        assertThat(t2.getDescripcion()).isEqualTo("Desc 2");
        assertThat(t2.getFecha_vencimiento()).isEqualTo("2024-07-01");
        assertThat(t2.getLista()).isEqualTo("terminado");
        assertThat(t2.getEstado()).isEqualTo("success");
        assertThat(t2.getId_proyecto()).isEqualTo(6);
    }

    @Test
    @DisplayName("Test modelo Proyectos en Cliente: constructores, getters y setters")
    void testModeloProyectos() {
        Proyectos p1 = new Proyectos();
        assertThat(p1).isNotNull();

        p1.setId_proyecto(100);
        p1.setNombre("Proyecto 1");
        p1.setDescripcion("Desc");
        p1.setFecha_inicio("2024-01-01");
        p1.setFecha_finalizacion("2024-12-31");
        p1.setEstado_proyecto("En proceso");
        p1.setFondo("fondo.png");

        assertThat(p1.getId_proyecto()).isEqualTo(100);
        assertThat(p1.getNombre()).isEqualTo("Proyecto 1");
        assertThat(p1.getDescripcion()).isEqualTo("Desc");
        assertThat(p1.getFecha_inicio()).isEqualTo("2024-01-01");
        assertThat(p1.getFecha_finalizacion()).isEqualTo("2024-12-31");
        assertThat(p1.getEstado_proyecto()).isEqualTo("En proceso");
        assertThat(p1.getFondo()).isEqualTo("fondo.png");

        Proyectos p2 = new Proyectos(101, "Proyecto 2", "Desc 2", "2024-02-01", "2024-08-31", "Finalizado", "bg.jpg");
        assertThat(p2.getId_proyecto()).isEqualTo(101);
        assertThat(p2.getNombre()).isEqualTo("Proyecto 2");
        assertThat(p2.getDescripcion()).isEqualTo("Desc 2");
        assertThat(p2.getFecha_inicio()).isEqualTo("2024-02-01");
        assertThat(p2.getFecha_finalizacion()).isEqualTo("2024-08-31");
        assertThat(p2.getEstado_proyecto()).isEqualTo("Finalizado");
        assertThat(p2.getFondo()).isEqualTo("bg.jpg");
    }

    @Test
    @DisplayName("Test modelo Comentarios en Cliente: constructores, getters y setters")
    void testModeloComentarios() {
        Comentarios c1 = new Comentarios();
        assertThat(c1).isNotNull();

        c1.setId_comentario(201);
        c1.setId_usuario("Perla");
        c1.setId_tarea(10);
        c1.setTexto_comentario("Texto");
        c1.setFecha_hora("2024-06-26 13:48:00");

        assertThat(c1.getId_comentario()).isEqualTo(201);
        assertThat(c1.getId_usuario()).isEqualTo("Perla");
        assertThat(c1.getId_tarea()).isEqualTo(10);
        assertThat(c1.getTexto_comentario()).isEqualTo("Texto");
        assertThat(c1.getFecha_hora()).isEqualTo("2024-06-26 13:48:00");

        Comentarios c2 = new Comentarios(202, "Liliana", 20, "Texto 2", "2024-06-26 13:49:00");
        assertThat(c2.getId_comentario()).isEqualTo(202);
        assertThat(c2.getId_usuario()).isEqualTo("Liliana");
        assertThat(c2.getId_tarea()).isEqualTo(20);
        assertThat(c2.getTexto_comentario()).isEqualTo("Texto 2");
        assertThat(c2.getFecha_hora()).isEqualTo("2024-06-26 13:49:00");
    }

    @Test
    @DisplayName("Test modelo Asignaciones en Cliente: constructores, getters y setters")
    void testModeloAsignaciones() {
        Asignaciones a1 = new Asignaciones();
        assertThat(a1).isNotNull();

        a1.setId_asignacion(301);
        a1.setId_usuario(1);
        a1.setId_tarea(10);
        a1.setFecha_asignacion("2024-06-26");

        assertThat(a1.getId_asignacion()).isEqualTo(301);
        assertThat(a1.getId_usuario()).isEqualTo(1);
        assertThat(a1.getId_tarea()).isEqualTo(10);
        assertThat(a1.getFecha_asignacion()).isEqualTo("2024-06-26");

        Asignaciones a2 = new Asignaciones(302, 2, 20, "2024-06-27");
        assertThat(a2.getId_asignacion()).isEqualTo(302);
        assertThat(a2.getId_usuario()).isEqualTo(2);
        assertThat(a2.getId_tarea()).isEqualTo(20);
        assertThat(a2.getFecha_asignacion()).isEqualTo("2024-06-27");
    }
}

