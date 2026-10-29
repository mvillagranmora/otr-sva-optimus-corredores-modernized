SELECT
TO_VARCHAR(NOM."FechaDesde",'YYYY-MM-DD') AS "FechaDesde"
,TO_VARCHAR(NOM."FechaHasta",'YYYY-MM-DD') AS "FechaHasta"
,CAR."CartolaEncoded"
,CAR."IdCliente"
,CAR."IdPortafolio"
,CAR."NombreArchivo"
,CAR."Origen"
FROM "BSCL.Normativa.BD::CAR.Cartola" CAR
LEFT JOIN "BSCL.Normativa.BD::CAR.Nomina" NOM ON CAR."IdNomina" = NOM."IdNomina"
WHERE CAR."IdCartola" = ?;