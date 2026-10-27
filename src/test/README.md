# Exportador de materias de un profesor

Aplicación independiente del examen. Punto de entrada: `test.Main`. Requiere JDK 17 y MySQL 8. El año académico consultado siempre es 2025; la fecha del documento corresponde al día de ejecución.

## Conexión

Desde la raíz del proyecto, copia `src/test/db.properties.example` a `db.properties` y completa la contraseña proporcionada por el trainer. El archivo local está excluido de Git.

La configuración de ejemplo usa el servidor `localhost`, la base `Campuslands` y el usuario `camper`. Ajusta la URL si tu servidor o puerto son diferentes.

Se pueden usar las variables `DB_URL`, `DB_USER` y `DB_PASSWORD`, o las propiedades de Java `-Ddb.url=...`, `-Ddb.user=...` y `-Ddb.password=...`. La prioridad es: propiedades de Java, variables de entorno, archivo local y valores predeterminados.

Para leer otro archivo: `-Dtest.db.config=ruta/al/archivo.properties`.

## Datos de prueba

El exportador consulta las tablas `profesores`, `asignaciones_profesor`, `materias` y `grados` de la base `Campuslands`. Con los datos originales del proyecto:

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

## Salida

El archivo `materias_profesor_<id>_2025.txt` se crea en UTF-8 en la raíz del proyecto. Si existe, se reemplaza. Las materias se consultan sin repetir una misma materia por sus distintas secciones.

La solución añade archivos nuevos; las clases existentes del ERP se conservan.

## Entrega

Entrega la URL del repositorio privado y el hash completo del último commit en `main`. El trainer debe figurar como colaborador.
