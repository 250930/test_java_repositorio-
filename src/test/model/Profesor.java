package test.model;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record Profesor(int id, String primerNombre, String segundoNombre,
                       String primerApellido, String segundoApellido,
                       String tipoDocumento, String numeroDocumento) {
    public String nombreCompleto() {
        return Stream.of(primerNombre, segundoNombre, primerApellido, segundoApellido)
                .filter(Objects::nonNull)
                .map(String::strip)
                .filter(parte -> !parte.isEmpty())
                .collect(Collectors.joining(" "));
    }

    public String documento() {
        return tipoDocumento.strip() + " " + numeroDocumento.strip();
    }
}
