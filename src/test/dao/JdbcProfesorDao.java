package test.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import test.model.MateriaAsignada;
import test.model.Profesor;

public final class JdbcProfesorDao implements ProfesorDao {
    private final Connection conexion;

    public JdbcProfesorDao(Connection conexion) {
        this.conexion = conexion;
    }

    @Override
    public Optional<Profesor> buscarPorId(int id) throws SQLException {
        String sql = "SELECT id_profesor, primer_nombre, segundo_nombre, primer_apellido, "
                + "segundo_apellido, tipo_documento, numero_documento "
                + "FROM profesores WHERE id_profesor = ?";
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            try (ResultSet resultado = consulta.executeQuery()) {
                if (!resultado.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Profesor(
                        resultado.getInt("id_profesor"),
                        resultado.getString("primer_nombre"),
                        resultado.getString("segundo_nombre"),
                        resultado.getString("primer_apellido"),
                        resultado.getString("segundo_apellido"),
                        resultado.getString("tipo_documento"),
                        resultado.getString("numero_documento")));
            }
        }
    }

    @Override
    public List<MateriaAsignada> buscarMaterias(int idProfesor, int anio) throws SQLException {
        String sql = "SELECT DISTINCT m.codigo_materia, m.nombre_materia, g.nombre_grado "
                + "FROM asignaciones_profesor a "
                + "JOIN materias m ON m.id_materia = a.id_materia "
                + "JOIN grados g ON g.id_grado = m.id_grado "
                + "WHERE a.id_profesor = ? AND a.`año_academico` = ? "
                + "ORDER BY m.codigo_materia, m.nombre_materia, g.nombre_grado";
        List<MateriaAsignada> materias = new ArrayList<>();
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, idProfesor);
            consulta.setInt(2, anio);
            try (ResultSet resultado = consulta.executeQuery()) {
                while (resultado.next()) {
                    materias.add(new MateriaAsignada(
                            resultado.getString("codigo_materia"),
                            resultado.getString("nombre_materia"),
                            resultado.getString("nombre_grado")));
                }
            }
        }
        return materias;
    }
}
