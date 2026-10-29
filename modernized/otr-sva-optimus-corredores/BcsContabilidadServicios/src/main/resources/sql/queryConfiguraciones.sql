SELECT
    C."IdTipoCentralizacion"        AS "IdTipoCentralizacion",
    C."Nombre"                      AS "Nombre",
    CT."Servicio"                   AS "Servicio",
    CT."Tipo"                       AS "Tipo",
    NULL                            AS "IdTipoCentralizacionOrigen",
    C."RequiereFechaConsulta"       AS "RequiereFechaConsulta"
FROM "BSCL.Contabilidad.BD::ITZ.CentralizacionTipo" C
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.ConfiguracionCentralizacionTipo" CT
    ON C."IdTipoCentralizacion" = CT."IdTipoCentralizacion"
WHERE C."IdTipoCentralizacion" IN (:IdTipoCentralizacion)
UNION ALL
SELECT
    CR."IdTipoCentralizacionRelacionada"    AS "IdTipoCentralizacion",
    COALESCE(C."Nombre", '')               AS "Nombre",
    CT."Servicio"                          AS "Servicio",
    CT."Tipo"                              AS "Tipo",
    CR."IdTipoCentralizacion"              AS "IdTipoCentralizacionOrigen",
    C."RequiereFechaConsulta"              AS "RequiereFechaConsulta"
FROM "BSCL.Contabilidad.BD::ITZ.ConfiguracionCentralizacionTipoRelacionada" CR
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.CentralizacionTipo" C
    ON CR."IdTipoCentralizacionRelacionada" = C."IdTipoCentralizacion"
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.ConfiguracionCentralizacionTipo" CT
    ON CR."IdTipoCentralizacionRelacionada" = CT."IdTipoCentralizacion"
WHERE CR."IdTipoCentralizacion" IN (:IdTipoCentralizacion)
