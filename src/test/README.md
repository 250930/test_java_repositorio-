# Exportador de materias de un profesor

Aplicación independiente del examen. Punto de entrada: `test.Main`. Requiere JDK 17 y MySQL 8. El año académico consultado siempre es 2025; la fecha del documento corresponde al día de ejecución.

## Conexión

Desde la raíz del proyecto, copia `src/test/db.properties.example` a `db.properties` y completa la contraseña proporcionada por el trainer. El archivo local está excluido de Git.

La configuración de ejemplo usa el servidor `localhost`, la base `Campuslands` y el usuario `camper`. Ajusta la URL si tu servidor o puerto son diferentes.

Se pueden usar las variables `DB_URL`, `DB_USER` y `DB_PASSWORD`, o las propiedades de Java `-Ddb.url=...`, `-Ddb.user=...` y `-Ddb.password=...`. La prioridad es: propiedades de Java, variables de entorno, archivo local y valores predeterminados.

Para leer otro archivo: `-Dtest.db.config=ruta/al/archivo.properties`.

## SQL

`src/test/sql/Campuslands.sql` contiene las cuatro tablas utilizadas por el exportador, con la estructura del proyecto original: grados, profesores, materias y asignaciones_profesor. Incluye los 32 profesores, las 48 materias y las asignaciones de 2025 y 2026 del SQL original.

Importa este archivo únicamente para preparar una instalación nueva de prueba. Si ya tienes la base oficial del examen, utiliza sus datos directamente. El script no crea usuarios ni modifica permisos.

Para una instalación local sin el usuario del trainer, copia `src/test/sql/usuario_local.sql.example`, cambia `CAMBIAR_CONTRASENA_LOCAL` por tu contraseña local y ejecútalo como administrador de MySQL. Concede únicamente lectura sobre `Campuslands`. Usa la misma contraseña en tu `db.properties` local. Si el usuario ya existe, conserva su contraseña actual.

`src/test/sql/verificar.sql` permite revisar los datos y la consulta de materias desde MySQL.

Con los datos incluidos:

| ID | Resultado |
| --- | --- |
| 1 | Exporta Matemáticas de Primero y Séptimo |
| 3 | Exporta Matemáticas de Tercero y Noveno; el profesor no tiene segundo nombre |
| 29 | Profesor existente sin materias asignadas en 2025 |
| 999 | Profesor inexistente; vuelve a pedir el ID |

## Compilar y ejecutar

Ejecuta los comandos desde la raíz del repositorio, donde se guardará el TXT.

En PowerShell:

```powershell
New-Item -ItemType Directory -Force build/examen | Out-Null
$fuentesExamen = Get-ChildItem src/test -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 --release 17 -d build/examen $fuentesExamen
java "-Dfile.encoding=UTF-8" -cp "build/examen;lib/*" test.Main
```

En Linux o macOS:

```bash
mkdir -p build/examen
find src/test -name '*.java' > build/examen/fuentes.txt
javac -encoding UTF-8 --release 17 -d build/examen @build/examen/fuentes.txt
java "-Dfile.encoding=UTF-8" -cp "build/examen:lib/*" test.Main
```

En NetBeans, abre `src/test/Main.java` y usa **Ejecutar archivo**. En IntelliJ, ejecuta el método `main` de `test.Main` y establece la raíz del proyecto como directorio de trabajo.

## Comprobaciones

Después de compilar, ejecuta:

```powershell
java "-Dfile.encoding=UTF-8" -cp "build/examen;lib/*" test.verification.ExportadorVerificacion
```

En Linux o macOS, cambia el separador del classpath a `:`.

La verificación usa datos en memoria y una carpeta temporal. Comprueba entradas inválidas, ID inexistente, fin de entrada, profesor sin materias, consulta exclusiva de 2025, nombres opcionales, tildes UTF-8, columnas de 10/35/20 caracteres, fecha de generación, reemplazo de archivo y manejo de errores de escritura y SQL. La conexión real y las consultas JDBC deben verificarse adicionalmente contra MySQL.

La compilación y la verificación anterior pasaron con destino Java 17. El SQL se importó correctamente en una instancia temporal de MySQL 26.7: 32 profesores, 48 materias, 12 grados y 48 asignaciones para cada año, 2025 y 2026. También se ejecutaron las consultas extraídas de `JdbcProfesorDao.java`: profesor existente, inexistente, sin materias, segundo nombre nulo, exclusión de otros años y eliminación de duplicados por sección. El acceso SQL con el usuario `camper` y permiso `SELECT` funcionó. La prueba completa Java-JDBC queda pendiente en el servidor de destino con su configuración y el conector incluido en `lib/`.

## Salida

El archivo `materias_profesor_<id>_2025.txt` se crea en UTF-8 en la raíz del proyecto. Si existe, se reemplaza. Las materias se consultan sin repetir una misma materia por sus distintas secciones.

La solución añade archivos nuevos; las clases existentes del ERP se conservan.

## Entrega

Entrega la URL del repositorio privado y el hash completo del último commit en `main`. El trainer debe figurar como colaborador.
