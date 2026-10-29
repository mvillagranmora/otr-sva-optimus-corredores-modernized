SELECT
CPG."IdLibroOrdenes"
FROM
"BSCL.Orden.BD.Vistas::ConsultarPortafoliosGrupo" CPG
WHERE CPG."IdGrupo" = :IdGrupo
AND CPG."IdEstadoGrupo" = 'ACT'
