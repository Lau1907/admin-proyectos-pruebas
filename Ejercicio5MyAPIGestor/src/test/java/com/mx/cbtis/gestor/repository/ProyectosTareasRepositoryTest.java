package com.mx.cbtis.gestor.repository;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.mx.cbtis.gestor.modelo.Proyectos;
import com.mx.cbtis.gestor.modelo.Tareas;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ProyectosTareasRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProtectosRepository protectosRepository;

    @Autowired
    private TareasRepository tareasRepository;

    @Test
    @DisplayName("UT-08 save de proyecto válido")
    void testUt08_saveProyectoValido() {
        Proyectos proyecto = new Proyectos(null, "My API Rest", "Desarrollaran un Api Rest", "2024-05-31", "2024-06-29", "En proceso", "/Images/animacion1.gif");
        Proyectos guardado = protectosRepository.save(proyecto);
        assertThat(guardado.getId_proyecto()).isNotNull();
    }

    @Test
    @DisplayName("UT-09 findById de proyecto")
    void testUt09_findByIdProyecto() {
        Proyectos proyecto = new Proyectos(null, "My API Rest", "Desarrollaran un Api Rest", "2024-05-31", "2024-06-29", "En proceso", "/Images/animacion1.gif");
        Proyectos persistido = entityManager.persistAndFlush(proyecto);
        entityManager.clear();

        Optional<Proyectos> encontrado = protectosRepository.findById(persistido.getId_proyecto());
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNombre()).isEqualTo("My API Rest");
    }

    @Test
    @DisplayName("UT-10 findByIdProyecto devuelve solo las tareas de ese proyecto")
    void testUt10_findByIdProyectoDevuelveTareasDelProyecto() {
        Proyectos p1 = entityManager.persistAndFlush(new Proyectos(null, "Proyecto 1", "Desc 1", "2024-01-01", "2024-01-31", "Activo", "fondo1.png"));
        Proyectos p2 = entityManager.persistAndFlush(new Proyectos(null, "Proyecto 2", "Desc 2", "2024-02-01", "2024-02-28", "Activo", "fondo2.png"));

        Tareas t1 = new Tareas(null, "Tarea 1 P1", "Desc 1", "2024-06-13", "terminado", "danger", p1.getId_proyecto());
        Tareas t2 = new Tareas(null, "Tarea 2 P1", "Desc 2", "2024-06-14", "en proceso", "warning", p1.getId_proyecto());
        Tareas t3 = new Tareas(null, "Tarea 1 P2", "Desc 3", "2024-06-15", "pendiente", "info", p2.getId_proyecto());

        entityManager.persistAndFlush(t1);
        entityManager.persistAndFlush(t2);
        entityManager.persistAndFlush(t3);
        entityManager.clear();

        List<Tareas> tareasP1 = tareasRepository.findByIdProyecto(p1.getId_proyecto());
        assertThat(tareasP1).hasSize(2);
        assertThat(tareasP1).extracting(Tareas::getNombre).containsExactlyInAnyOrder("Tarea 1 P1", "Tarea 2 P1");
    }

    @Test
    @DisplayName("UT-11 findByIdProyecto de proyecto sin tareas -> lista vacía")
    void testUt11_findByIdProyectoSinTareas() {
        Proyectos p = entityManager.persistAndFlush(new Proyectos(null, "Proyecto Sin Tareas", "Desc", "2024-01-01", "2024-01-31", "Activo", "fondo.png"));
        entityManager.clear();

        List<Tareas> tareas = tareasRepository.findByIdProyecto(p.getId_proyecto());
        assertThat(tareas).isEmpty();
    }

    @Test
    @DisplayName("UT-12 delete de tarea")
    void testUt12_deleteTarea() {
        Tareas tarea = new Tareas(null, "Base de datos", "Realizar la estructura", "2024-06-13", "terminado", "danger", 197);
        Tareas persistida = entityManager.persistAndFlush(tarea);
        entityManager.clear();

        tareasRepository.deleteById(persistida.getId_tarea());
        entityManager.flush();
        entityManager.clear();

        assertThat(tareasRepository.findById(persistida.getId_tarea())).isEmpty();
    }
}
