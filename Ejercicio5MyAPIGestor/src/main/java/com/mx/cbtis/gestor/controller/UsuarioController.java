package com.mx.cbtis.gestor.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mx.cbtis.gestor.modelo.Usuario;
import com.mx.cbtis.gestor.repository.UsuarioRepository;

@RestController
public class UsuarioController {
	
	@Autowired
	UsuarioRepository usuariotable;

	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	
	@PostMapping("/getUsuarioLogin")
	public Usuario getUsuarioLogin(@RequestBody Usuario userParam) {
		
		Optional<Usuario> optionaluser = usuariotable.findByCorreoElectronico(userParam.getCorreo_electronico());
		
		Usuario user = new Usuario();
		
		if(optionaluser.isPresent()) {
			Usuario dbUser = optionaluser.get();
			String storedPass = dbUser.getContrasena();
			boolean matches = false;
			if (storedPass != null && userParam.getContrasena() != null) {
				if (storedPass.startsWith("$2a$") || storedPass.startsWith("$2b$") || storedPass.startsWith("$2y$")) {
					matches = passwordEncoder.matches(userParam.getContrasena(), storedPass);
				} else {
					matches = storedPass.equals(userParam.getContrasena()) || passwordEncoder.matches(userParam.getContrasena(), storedPass);
				}
			}
			if (matches) {
				user = dbUser;
			} else {
				user.setNombre("000");
			}
		}else {
			user.setNombre("000");
		}
		
		return user;
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
	public int addUsuario(@Valid @RequestBody Usuario usuario) {
		int flag = 0;
		if(usuario != null) {
			if (usuario.getContrasena() != null && !usuario.getContrasena().isEmpty()) {
				if (!usuario.getContrasena().startsWith("$2a$") && !usuario.getContrasena().startsWith("$2b$")) {
					usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
				}
			}
			usuariotable.save(usuario);
			flag = 1;
		}
		
		return flag;
	}
	
	@PostMapping("/updateUsuario")
	public int updateUsuario(@RequestBody Usuario usuario) {
		int flag = 0;
		if(usuario != null) {
			if (usuario.getContrasena() != null && !usuario.getContrasena().isEmpty()) {
				if (!usuario.getContrasena().startsWith("$2a$") && !usuario.getContrasena().startsWith("$2b$")) {
					usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
				}
			}
			usuariotable.save(usuario);
			flag = 1;
		}
		
		return flag;
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
