SELECT
    d."ID",
    d."IdDocumento",
    d."FuenteDatos",
    doc."Version",
    ----
    to_date(d."FechaOperacion") AS "FechaOperacion",
    TO_CHAR(ADD_MONTHS(d."FechaOperacion",-1),'YYYY-MM') AS "AgnoMes",
    d."AhorrosTrabajador",
    d."AhorrosDeposito",
    d."AhorrosCotizaciones",
    --Retiros
    d."RetirosTrabajador",
    d."RetirosDeposito",
    d."RetirosCotizaciones"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1871" AS d
INNER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarDocumento" AS doc 
ON (doc."IdDocumento" = d."IdDocumento")
AND doc."Version" = :Version;