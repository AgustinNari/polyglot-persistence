package com.tpo.dao;

import com.tpo.modelo.usuario.Usuario;
import java.util.List;
import java.util.Optional;

public interface UsuarioDao {
    Usuario guardar(Usuario usuario) throws Exception; // inserta y retorna con id asignado
    void actualizar(Usuario usuario) throws Exception; // actualiza datos (requiere id no nulo)
    void eliminarPorId(Long id) throws Exception;
    Optional<Usuario> buscarPorId(Long id) throws Exception;
    Optional<Usuario> buscarPorDocIdentidad(String docIdentidad) throws Exception;
    List<Usuario> listarTodos() throws Exception;
    long contarUsuarios() throws Exception; // contar usuarios totales
    void incrementarMinutosActividad(Long usuarioId, long minutosMinutos) throws Exception;
}
