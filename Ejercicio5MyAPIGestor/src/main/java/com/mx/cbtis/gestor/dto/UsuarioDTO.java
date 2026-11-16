package com.mx.cbtis.gestor.dto;

import javax.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UsuarioDTO {

	@JsonProperty("id_usuario")
	private Integer idUsuario;

	@JsonProperty("nombre")
	private String nombre;

	@NotBlank
	@JsonProperty("correo_electronico")
	private String correoElectronico;

	@NotBlank
	@JsonProperty("contrasena")
	private String contrasena;

	@JsonProperty("rol")
	private String rol;

	@JsonProperty("imagen_usuario")
	private String imagenUsuario;

	public UsuarioDTO() {
	}

	public UsuarioDTO(Integer idUsuario, String nombre, String correoElectronico, String contrasena, String rol,
			String imagenUsuario) {
		this.idUsuario = idUsuario;
		this.nombre = nombre;
		this.correoElectronico = correoElectronico;
		this.contrasena = contrasena;
		this.rol = rol;
		this.imagenUsuario = imagenUsuario;
	}

	public Integer getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(Integer idUsuario) {
		this.idUsuario = idUsuario;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getCorreoElectronico() {
		return correoElectronico;
	}

	public void setCorreoElectronico(String correoElectronico) {
		this.correoElectronico = correoElectronico;
	}

	public String getContrasena() {
		return contrasena;
	}

	public void setContrasena(String contrasena) {
		this.contrasena = contrasena;
	}

	public String getRol() {
		return rol;
	}

	public void setRol(String rol) {
		this.rol = rol;
	}

	public String getImagenUsuario() {
		return imagenUsuario;
	}

	public void setImagenUsuario(String imagenUsuario) {
		this.imagenUsuario = imagenUsuario;
	}
}
