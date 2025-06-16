SELECT 
    MIN(CAST(documento AS BIGINT)) AS min_documento,
    MAX(CAST(documento AS BIGINT)) AS max_documento,
    (MAX(CAST(documento AS BIGINT)) - MIN(CAST(documento AS BIGINT))) AS rango
FROM
    ciudadano;

SELECT ciu.nombre, ciu.apellido, puesto.consecutive, puesto.nombre as "Nombre de lugar", puesto.direccion, muni.nombre as "Muni", dep.nombre as "Depar"
FROM ciudadano AS ciu
JOIN mesa_votacion AS mesa ON mesa.id = ciu.mesa_id
JOIN puesto_votacion as puesto on puesto.consecutive = mesa.consecutive
JOIN municipio as muni on muni.id = puesto.municipio_id
JOIN departamento as dep on dep.id = muni.departamento_id;
LIMIT 10;