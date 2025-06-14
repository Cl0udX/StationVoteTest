SELECT 
    MIN(CAST(documento AS BIGINT)) AS min_documento,
    MAX(CAST(documento AS BIGINT)) AS max_documento,
    (MAX(CAST(documento AS BIGINT)) - MIN(CAST(documento AS BIGINT))) AS rango
FROM
    ciudadano;