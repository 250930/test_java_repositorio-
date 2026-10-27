package test.verification;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import test.controller.ExportadorController;
import test.dao.ProfesorDao;
import test.model.MateriaAsignada;
import test.model.Profesor;
import test.view.ExportadorView;

public final class ExportadorVerificacion {
    private static final Clock RELOJ = Clock.fixed(
            Instant.parse("2026-10-27T15:00:00Z"), ZoneOffset.UTC);
    private static final Profesor PROFESOR = new Profesor(
            1, "Roberto", "Andrés", "Martínez", "Torres", "CC", "80123456");
    private static final List<MateriaAsignada> MATERIAS = List.of(
            new MateriaAsignada("MAT-10", "Matemáticas", "Décimo"),
            new MateriaAsignada("MAT-11", "Matemáticas", "Once"),
            new MateriaAsignada("FIS-10", "Física", "Décimo"));

    private ExportadorVerificacion() {
    }

    public static void main(String[] args) throws Exception {
        Path raiz = Files.createTempDirectory("exportador-materias-");
        try {
            verificarExportacion(Files.createDirectory(raiz.resolve("exportacion")));
            verificarSinMaterias(Files.createDirectory(raiz.resolve("sin-materias")));
            verificarErrorArchivo(Files.createDirectory(raiz.resolve("error-archivo")));
            verificarErrorSql(Files.createDirectory(raiz.resolve("error-sql")));
            verificarFinEntrada(Files.createDirectory(raiz.resolve("fin-entrada")));
            verificarNombre();
            System.out.println("Verificaciones correctas: exportación, validaciones, año, UTF-8, "
                    + "columnas, fecha, reemplazo, nombres opcionales y errores.");
        } finally {
            try (var rutas = Files.walk(raiz)) {
                for (Path ruta : rutas.sorted(Comparator.reverseOrder()).toList()) {
                    Files.delete(ruta);
                }
            }
        }
    }

    private static void verificarExportacion(Path directorio) throws Exception {
        DaoPrueba dao = new DaoPrueba(MATERIAS);
        String salida = ejecutar("abc\n\n2147483648\n999\n1\n", dao, directorio);
        exigir(salida.contains("Error: Ingrese un número válido."), "Entrada inválida");
        exigir(salida.contains("Error: No se encontró ningún profesor con ese número."),
                "Profesor inexistente");
        exigir(salida.contains("Archivo generado exitosamente."), "Confirmación");
        exigir(salida.contains("Profesor: Roberto Andrés Martínez Torres"), "Nombre en pantalla");
        exigir(salida.contains("Archivo: materias_profesor_1_2025.txt"), "Nombre del archivo");
        exigir(dao.anioConsultado == 2025, "Año académico fijo");
        Path archivo = directorio.resolve("materias_profesor_1_2025.txt");
        String contenido = Files.readString(archivo, StandardCharsets.UTF_8);
        exigir(contenido.contains("Nombre completo: Roberto Andrés Martínez Torres"), "Nombre");
        exigir(contenido.contains("Documento: CC 80123456"), "Documento");
        exigir(contenido.contains("Documento generado el: 27/10/2026"), "Fecha de generación");
        exigir(contenido.contains("Física") && contenido.contains("Décimo"), "Tildes UTF-8");
        exigir(!contenido.contains("\t"), "Columnas sin tabuladores");
        String fila = contenido.lines().filter(linea -> linea.startsWith("MAT-10"))
                .findFirst().orElseThrow();
        exigir(fila.length() == 65, "Anchos 10, 35 y 20");
        exigir(fila.substring(10, 45).strip().equals("Matemáticas"), "Columna Materia");
        exigir(fila.substring(45).strip().equals("Décimo"), "Columna Grado");
        Files.writeString(archivo, "CONTENIDO ANTERIOR QUE DEBE DESAPARECER\n".repeat(100));
        ejecutar("1\n", dao, directorio);
        exigir(Files.readString(archivo, StandardCharsets.UTF_8).equals(contenido),
                "Reemplazo completo del archivo");
        exigir(dao.busquedas == 3, "Repetir ID solo tras profesor inexistente");
    }

    private static void verificarSinMaterias(Path directorio) throws Exception {
        DaoPrueba dao = new DaoPrueba(List.of());
        String salida = ejecutar("1\n1\n", dao, directorio);
        exigir(salida.contains("El profesor no tiene materias asignadas en 2025."), "Sin materias");
        exigir(!Files.exists(directorio.resolve("materias_profesor_1_2025.txt")),
                "No generar archivo vacío");
        exigir(dao.busquedas == 1, "Terminar cuando no hay materias");
    }

    private static void verificarErrorArchivo(Path directorio) throws Exception {
        Files.createDirectory(directorio.resolve("materias_profesor_1_2025.txt"));
        String salida = ejecutar("1\n", new DaoPrueba(MATERIAS), directorio);
        exigir(salida.contains("Error: No se pudo crear el archivo de exportación."),
                "Error de escritura");
        exigir(!salida.contains("Archivo generado exitosamente."), "Sin falso éxito");
    }

    private static void verificarErrorSql(Path directorio) throws Exception {
        ProfesorDao dao = new ProfesorDao() {
            @Override
            public Optional<Profesor> buscarPorId(int id) throws SQLException {
                throw new SQLException("Error de prueba");
            }

            @Override
            public List<MateriaAsignada> buscarMaterias(int idProfesor, int anio) {
                throw new AssertionError("No debe consultar materias");
            }
        };
        exigir(ejecutar("1\n", dao, directorio)
                .contains("Error: No fue posible consultar la base de datos."), "Error SQL");
    }

    private static void verificarFinEntrada(Path directorio) throws Exception {
        DaoPrueba dao = new DaoPrueba(MATERIAS);
        ejecutar("", dao, directorio);
        exigir(dao.busquedas == 0, "Fin de entrada");
    }

    private static void verificarNombre() {
        Profesor profesor = new Profesor(2, " Ana ", null, " Pérez ", " ", "CE", " 123 ");
        exigir(profesor.nombreCompleto().equals("Ana Pérez"), "Nombres opcionales");
        exigir(profesor.documento().equals("CE 123"), "Espacio entre tipo y documento");
    }

    private static String ejecutar(String entrada, ProfesorDao dao, Path directorio)
            throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (PrintStream salida = new PrintStream(buffer, true, StandardCharsets.UTF_8);
             Scanner lector = new Scanner(entrada)) {
            ExportadorView vista = new ExportadorView(lector, salida);
            new ExportadorController(dao, vista, directorio, RELOJ).ejecutar();
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static void exigir(boolean condicion, String caso) {
        if (!condicion) {
            throw new AssertionError(caso);
        }
    }

    private static final class DaoPrueba implements ProfesorDao {
        private final List<MateriaAsignada> materias;
        private int busquedas;
        private int anioConsultado;

        private DaoPrueba(List<MateriaAsignada> materias) {
            this.materias = materias;
        }

        @Override
        public Optional<Profesor> buscarPorId(int id) {
            busquedas++;
            return id == 1 ? Optional.of(PROFESOR) : Optional.empty();
        }

        @Override
        public List<MateriaAsignada> buscarMaterias(int idProfesor, int anio) {
            exigir(idProfesor == 1, "ID consultado");
            anioConsultado = anio;
            return materias;
        }
    }
}
