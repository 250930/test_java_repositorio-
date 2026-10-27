package test.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import test.model.MateriaAsignada;
import test.model.Profesor;

public interface ProfesorDao {
    Optional<Profesor> buscarPorId(int id) throws SQLException;

    List<MateriaAsignada> buscarMaterias(int idProfesor, int anio) throws SQLException;
}
