 SELECT
    d."ID",
    d."IdDocumento",
    d."FuenteDatos",
    doc."Version",
    ----
    to_date(d."FechaOperacion") AS "FechaOperacion",
    TO_CHAR(ADD_MONTHS(d."FechaOperacion",-1),'YYYY-MM') AS "AgnoMes",
    ---Aporte Empleador
    COALESCE(d."RetiroEmpleadorActualizadoMonedaDJ",d."RetiroEmpleadorActualizado",0) AS "RetiroEmpleadorActualizado",--($)
    COALESCE(d."MontoRetencionActualizadoMonedaDJ",d."MontoRetencionActualizado",0) AS "MontoRetencionActualizado",--15%($)
    --Monto del retiro proveniente de Ahorros Acogidos al Beneficio Tributario del inciso primero
    --del Art. 42 bis de la L.I.R y/o retiros del trabajador efectuados con cargo a
    --los aportes del empleador, actualizados en $
    COALESCE(d."MontoRetiroPorAhorrosIncisoPrimeroMonedaDJ",d."MontoRetiroPorAhorrosIncisoPrimero",0) AS "MontoRetiroPorAhorrosIncisoPrimero",
    --Retención de Impto de 15% practicada sobre retiros registrados en la columna anterior actualizados en $
    COALESCE(d."RetencionImpuestoSobreRetirosMonedaDJ",d."RetencionImpuestoSobreRetiros",0) AS "RetencionImpuestoSobreRetiros" --15%($)
FROM "BSCL.Normativa.BD::TRI.DatosDJ1899" AS d
INNER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarDocumento" AS doc ON (doc."IdDocumento" = d."IdDocumento")
WHERE doc."Version" = :Version;