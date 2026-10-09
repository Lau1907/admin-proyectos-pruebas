package com.mx.cbtis.gestor.repository;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.mx.cbtis.gestor.modelo.Usuario;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UsuarioRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("UT-01 save de usuario válido genera ID")
    void testUt01_saveUsuarioValido() {
        Usuario usuario = new Usuario(null, "Ana Paola Ramírez Vázquez", "ana22533@cbtis75.edu.mx", "0722", "Estudiante", "#70FAB7");
        Usuario guardado = usuarioRepository.save(usuario);
        assertThat(guardado.getId_usuario()).isNotNull();
    }

    @Test
    @DisplayName("UT-02 findById existente")
    void testUt02_findByIdExistente() {
        Usuario usuario = new Usuario(null, "Liliana Gómez Martínez", "liliana22512@cbtis75.edu.mx", "ada", "Estudiante", "#54EA01");
        Usuario persistido = entityManager.persistAndFlush(usuario);
        entityManager.clear();

        Optional<Usuario> encontrado = usuarioRepository.findById(persistido.getId_usuario());
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNombre()).isEqualTo("Liliana Gómez Martínez");
    }

    @Test
    @DisplayName("UT-03 findById inexistente")
    void testUt03_findByIdInexistente() {
        Optional<Usuario> encontrado = usuarioRepository.findById(99999);
        assertThat(encontrado).isEmpty();
    }

    @Test
    @DisplayName("UT-04 findByCorreoPass con credenciales correctas")
    void testUt04_findByCorreoPassCorrecto() {
        Usuario usuario = new Usuario(null, "Perla Joceline Martínez Meza", "perla22522@cbtis75.edu.mx", "12345", "Estudiante", "#805611");
        entityManager.persistAndFlush(usuario);
        entityManager.clear();

        Optional<Usuario> encontrado = usuarioRepository.findByCorreoPass("perla22522@cbtis75.edu.mx", "12345");
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNombre()).isEqualTo("Perla Joceline Martínez Meza");
    }

    @Test
    @DisplayName("UT-05 findByCorreoPass con contraseña incorrecta -> vacío")
    void testUt05_findByCorreoPassIncorrecto() {
        Usuario usuario = new Usuario(null, "Perla Joceline Martínez Meza", "perla22522@cbtis75.edu.mx", "12345", "Estudiante", "#805611");
        entityManager.persistAndFlush(usuario);
        entityManager.clear();

        Optional<Usuario> encontrado = usuarioRepository.findByCorreoPass("perla22522@cbtis75.edu.mx", "pass_incorrecta");
        assertThat(encontrado).isEmpty();
    }

    @Test
    @DisplayName("UT-06 actualizar nombre persiste")
    void testUt06_actualizarNombrePersiste() {
        Usuario usuario = new Usuario(null, "Nombre Original", "original@cbtis75.edu.mx", "123", "Estudiante", "#000000");
        Usuario persistido = entityManager.persistAndFlush(usuario);
        entityManager.clear();

        Usuario paraActualizar = usuarioRepository.findById(persistido.getId_usuario()).orElseThrow();
        paraActualizar.setNombre("Nombre Modificado");
        usuarioRepository.save(paraActualizar);
        entityManager.flush();
        entityManager.clear();

        Usuario actualizado = usuarioRepository.findById(persistido.getId_usuario()).orElseThrow();
        assertThat(actualizado.getNombre()).isEqualTo("Nombre Modificado");
    }

    @Test
    @DisplayName("UT-07 delete deja la tabla en 0")
    void testUt07_deleteDejaTablaEnZero() {
        Usuario usuario = new Usuario(null, "Usuario A Borrar", "borrar@cbtis75.edu.mx", "123", "Estudiante", "#000000");
        Usuario persistido = entityManager.persistAndFlush(usuario);
        entityManager.clear();

        usuarioRepository.deleteById(persistido.getId_usuario());
        entityManager.flush();
        entityManager.clear();

        assertThat(usuarioRepository.count()).isEqualTo(0);
    }
}
