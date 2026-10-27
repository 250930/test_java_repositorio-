package test;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Clock;
import java.util.Scanner;
import test.controller.ExportadorController;
import test.dao.Conexion;
import test.dao.JdbcProfesorDao;
import test.view.ExportadorView;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        ExportadorView vista = new ExportadorView(new Scanner(System.in), System.out);
        try (Connection conexion = Conexion.abrir()) {
            new ExportadorController(new JdbcProfesorDao(conexion), vista,
                    Path.of("."), Clock.systemDefaultZone()).ejecutar();
        } catch (IOException | SQLException | SecurityException e) {
            vista.errorConexion();
        }
    }
}
