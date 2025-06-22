package com.tpo.dao;

import com.tpo.modelo.usuario.Usuario;
import java.util.List;
import java.util.Optional;

public interface UsuarioDao {
    Usuario guardar(Usuario usuario) throws Exception;
    void actualizar(Usuario usuario) throws Exception;
    void eliminarPorId(Long id) throws Exception;
    Optional<Usuario> buscarPorId(Long id) throws Exception;
    Optional<Usuario> buscarPorDocIdentidad(String docIdentidad) throws Exception;
    List<Usuario> listarTodos() throws Exception;
    long contarUsuarios() throws Exception;
    void incrementarMinutosActividad(Long usuarioId, long minutosMinutos) throws Exception;
}
