package test.dao;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class Conexion {
    private Conexion() {
    }

    public static Connection abrir() throws IOException, SQLException {
        Properties propiedades = new Properties();
        Path archivo = Path.of(System.getProperty("test.db.config", "db.properties"));
        if (Files.exists(archivo)) {
            try (Reader lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
                propiedades.load(lector);
            }
        }
        String url = configurar(propiedades, "db.url", "DB_URL",
                "jdbc:mysql://localhost:3306/Campuslands"
                + "?useSSL=false&serverTimezone=America/Bogota"
                + "&allowPublicKeyRetrieval=true&characterEncoding=UTF-8");
        String usuario = configurar(propiedades, "db.user", "DB_USER", "camper");
        String clave = configurar(propiedades, "db.password", "DB_PASSWORD", "");
        return DriverManager.getConnection(url, usuario, clave);
    }

    private static String configurar(Properties propiedades, String clave,
                                      String variable, String valorPredeterminado) {
        String valor = System.getProperty(clave);
        if (valor == null) {
            valor = System.getenv(variable);
        }
        return valor != null ? valor : propiedades.getProperty(clave, valorPredeterminado);
    }
}
