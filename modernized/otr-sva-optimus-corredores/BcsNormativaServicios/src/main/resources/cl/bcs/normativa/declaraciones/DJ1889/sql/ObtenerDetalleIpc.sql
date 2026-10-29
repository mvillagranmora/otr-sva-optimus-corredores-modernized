SELECT
    d."ID",
    d."IdDocumento",
    d."FuenteDatos",
    doc."Version",
    ----
    to_date(d."FechaOperacion") AS "FechaOperacion",
    TO_CHAR(d."FechaOperacion",'YYYY-MM') AS "AgnoMes",
    d."RentabilidadPositiva",
    d."RentabilidadNegativa"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1889" AS d
INNER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarDocumento" AS doc ON (doc."IdDocumento" = d."IdDocumento")
WHERE doc."Version" = :Version;