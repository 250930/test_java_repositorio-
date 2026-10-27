USE Campuslands;

SELECT COUNT(*) AS profesores FROM profesores;
SELECT COUNT(*) AS materias FROM materias;
SELECT COUNT(*) AS grados FROM grados;
SELECT año_academico, COUNT(*) AS asignaciones
FROM asignaciones_profesor
GROUP BY año_academico
ORDER BY año_academico;

SELECT p.id_profesor,
       CONCAT_WS(' ', NULLIF(TRIM(p.primer_nombre), ''),
                      NULLIF(TRIM(p.segundo_nombre), ''),
                      NULLIF(TRIM(p.primer_apellido), ''),
                      NULLIF(TRIM(p.segundo_apellido), '')) AS nombre_completo,
       CONCAT(p.tipo_documento, ' ', p.numero_documento) AS documento
FROM profesores p
WHERE p.id_profesor IN (1, 3, 29);

SELECT DISTINCT m.codigo_materia, m.nombre_materia, g.nombre_grado
FROM asignaciones_profesor a
JOIN materias m ON m.id_materia = a.id_materia
JOIN grados g ON g.id_grado = m.id_grado
WHERE a.id_profesor = 1 AND a.año_academico = 2025
ORDER BY m.codigo_materia, m.nombre_materia, g.nombre_grado;

SELECT COUNT(*) AS materias_profesor_29_en_2025
FROM asignaciones_profesor
WHERE id_profesor = 29 AND año_academico = 2025;
