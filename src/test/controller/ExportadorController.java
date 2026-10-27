package test.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import test.dao.ProfesorDao;
import test.model.MateriaAsignada;
import test.model.Profesor;
import test.view.ExportadorView;

public final class ExportadorController {
    private static final int ANIO_ACADEMICO = 2025;
    private static final String FORMATO_FILA = "%-10s%-35s%-20s%n";
    private final ProfesorDao profesores;
    private final ExportadorView vista;
    private final Path directorio;
    private final Clock reloj;

    public ExportadorController(ProfesorDao profesores, ExportadorView vista,
                                Path directorio, Clock reloj) {
        this.profesores = profesores;
        this.vista = vista;
        this.directorio = directorio;
        this.reloj = reloj;
    }

    public void ejecutar() {
        vista.mostrarTitulo();
        try {
            while (true) {
                Integer id = vista.pedirId();
                if (id == null) {
                    return;
                }
                Optional<Profesor> resultado = profesores.buscarPorId(id);
                if (resultado.isEmpty()) {
                    vista.profesorNoEncontrado(id);
                    continue;
                }
                Profesor profesor = resultado.get();
                List<MateriaAsignada> materias = profesores.buscarMaterias(id, ANIO_ACADEMICO);
                if (materias.isEmpty()) {
                    vista.sinMaterias(profesor);
                    return;
                }
                Path archivo = directorio.resolve("materias_profesor_" + id + "_2025.txt");
                try {
                    Files.writeString(archivo, crearReporte(profesor, materias),
                            StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
                    vista.exportacionExitosa(profesor, archivo);
                } catch (IOException | SecurityException e) {
                    vista.errorArchivo();
                }
                return;
            }
        } catch (SQLException e) {
            vista.errorBaseDeDatos();
        }
    }

    private String crearReporte(Profesor profesor, List<MateriaAsignada> materias) {
        String salto = System.lineSeparator();
        StringBuilder reporte = new StringBuilder();
        String dobleLinea = "=".repeat(50);
        String linea = "-".repeat(50);
        reporte.append(dobleLinea).append(salto);
        reporte.append("COLEGIO INTERNACIONAL \"LITTLE HORIZONS\"").append(salto);
        reporte.append("REPORTE DE MATERIAS ASIGNADAS - AÑO 2025").append(salto);
        reporte.append(dobleLinea).append(salto);
        reporte.append("Nombre completo : ").append(profesor.nombreCompleto()).append(salto);
        reporte.append("Documento       : ").append(profesor.documento()).append(salto);
        reporte.append(linea).append(salto);
        reporte.append(String.format(Locale.ROOT, FORMATO_FILA, "Código", "Materia", "Grado"));
        for (MateriaAsignada materia : materias) {
            reporte.append(String.format(Locale.ROOT, FORMATO_FILA,
                    materia.codigo(), materia.nombre(), materia.grado()));
        }
        reporte.append(linea).append(salto);
        reporte.append("Documento generado el: ")
                .append(LocalDate.now(reloj).format(DateTimeFormatter.ofPattern("dd/MM/uuuu")))
                .append(salto);
        return reporte.toString();
    }
}
