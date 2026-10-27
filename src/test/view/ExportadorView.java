package test.view;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Scanner;
import test.model.Profesor;

public final class ExportadorView {
    private final Scanner entrada;
    private final PrintStream salida;

    public ExportadorView(Scanner entrada, PrintStream salida) {
        this.entrada = entrada;
        this.salida = salida;
    }

    public void mostrarTitulo() {
        salida.println("EXPORTADOR DE MATERIAS ASIGNADAS - AÑO 2025");
        salida.println();
    }

    public Integer pedirId() {
        while (true) {
            salida.print("Ingrese el ID del profesor: ");
            salida.flush();
            if (!entrada.hasNextLine()) {
                return null;
            }
            String texto = entrada.nextLine().strip();
            try {
                if (!texto.matches("[0-9]+")) {
                    throw new NumberFormatException();
                }
                return Integer.valueOf(texto);
            } catch (NumberFormatException e) {
                salida.println("Error: Ingrese un número válido.");
            }
        }
    }

    public void profesorNoEncontrado() {
        salida.println("Error: No se encontró ningún profesor con ese número.");
    }

    public void sinMaterias() {
        salida.println("El profesor no tiene materias asignadas en 2025.");
    }

    public void exportacionExitosa(Profesor profesor, Path archivo) {
        salida.println("Archivo generado exitosamente.");
        salida.println("Profesor: " + profesor.nombreCompleto());
        salida.println("Archivo: " + archivo.getFileName());
    }

    public void errorArchivo() {
        salida.println("Error: No se pudo crear el archivo de exportación.");
    }

    public void errorBaseDeDatos() {
        salida.println("Error: No fue posible consultar la base de datos.");
    }

    public void errorConexion() {
        salida.println("Error: No fue posible conectarse a la base de datos.");
        salida.println("Verifique MySQL y las credenciales de db.properties.");
    }
}
