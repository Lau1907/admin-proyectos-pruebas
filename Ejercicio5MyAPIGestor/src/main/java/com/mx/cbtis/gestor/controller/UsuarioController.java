package com.mx.cbtis.gestor.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mx.cbtis.gestor.dto.UsuarioDTO;
import com.mx.cbtis.gestor.modelo.Usuario;
import com.mx.cbtis.gestor.repository.UsuarioRepository;

@RestController
public class UsuarioController {
	
	private final UsuarioRepository usuariotable;

	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	public UsuarioController(UsuarioRepository usuariotable) {
		this.usuariotable = usuariotable;
	}

	Usuario toEntity(UsuarioDTO dto) {
		if (dto == null) {
			return null;
		}
		Usuario usuario = new Usuario();
		usuario.setId_usuario(dto.getIdUsuario());
		usuario.setNombre(dto.getNombre());
		usuario.setCorreo_electronico(dto.getCorreoElectronico());
		usuario.setContrasena(dto.getContrasena());
		usuario.setRol(dto.getRol());
		usuario.setImagen_usuario(dto.getImagenUsuario());
		return usuario;
	}
	
	private boolean verificarPassword(String storedPass, String rawPass) {
		if (storedPass == null || rawPass == null) {
			return false;
		}
		if (storedPass.startsWith("$2a$") || storedPass.startsWith("$2b$") || storedPass.startsWith("$2y$")) {
			return passwordEncoder.matches(rawPass, storedPass);
		}
		return storedPass.equals(rawPass) || passwordEncoder.matches(rawPass, storedPass);
	}

	private String hashContrasena(String rawPass) {
		if (rawPass != null && !rawPass.isEmpty() && !rawPass.startsWith("$2a$") && !rawPass.startsWith("$2b$")) {
			return passwordEncoder.encode(rawPass);
		}
		return rawPass;
	}

	private int guardarUsuario(UsuarioDTO usuarioDTO) {
		if (usuarioDTO == null) {
			return 0;
		}
		Usuario usuario = toEntity(usuarioDTO);
		usuario.setContrasena(hashContrasena(usuario.getContrasena()));
		usuariotable.save(usuario);
		return 1;
	}
	
	@PostMapping("/getUsuarioLogin")
	public Usuario getUsuarioLogin(@RequestBody UsuarioDTO userParam) {
		Usuario fallback = new Usuario();
		fallback.setNombre("000");

		if (userParam == null || userParam.getCorreoElectronico() == null) {
			return fallback;
		}
		
		Optional<Usuario> optionaluser = usuariotable.findByCorreoElectronico(userParam.getCorreoElectronico());
		if (optionaluser.isPresent()) {
			Usuario dbUser = optionaluser.get();
			if (verificarPassword(dbUser.getContrasena(), userParam.getContrasena())) {
				return dbUser;
			}
		}
		return fallback;
	}
	
	@GetMapping("/buscarUsuario")
	public ResponseEntity<Usuario> getUsuario(@RequestParam int id) {
		Optional<Usuario> usuario = usuariotable.findById(id);
		if(usuario.isPresent()) {
			return ResponseEntity.ok(usuario.get());
		}
		return ResponseEntity.notFound().build();
	}
	
	@PostMapping("/addUsuario")
	public int addUsuario(@Valid @RequestBody UsuarioDTO usuarioDTO) {
		return guardarUsuario(usuarioDTO);
	}
	
	@PostMapping("/updateUsuario")
	public int updateUsuario(@RequestBody UsuarioDTO usuarioDTO) {
		return guardarUsuario(usuarioDTO);
	}
	
	@DeleteMapping("/deleteUsuario")
	public int deleteUsuario(@RequestBody Usuario usuario) {
		int flag = 0;
		if(usuario != null) {
			usuariotable.delete(usuario);
			flag = 1;
		}
		
		return flag;
	}
	
	@PostMapping("/buscarUsuarios")
	public List<Usuario> getUsuarios(){
		List<Usuario> listaUsuarios = new ArrayList<>(); 
		usuariotable.findAll().forEach(listaUsuarios::add);
		return listaUsuarios;
	}		
}
