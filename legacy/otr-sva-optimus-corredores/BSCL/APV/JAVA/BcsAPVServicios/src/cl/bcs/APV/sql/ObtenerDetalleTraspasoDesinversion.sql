SELECT 
DetalleTraspaso."MontoTraspaso",
DetalleTraspaso."IdInstrumento",
DetalleTraspaso."PorcentajeTraspaso",
DetalleTraspaso."CodigoRegimenTributario",
DetalleTraspaso."IdPortafolio",
Instrumento."IdMoneda"
FROM "BSCL.APV.BD::TRA.DetalleOrigenTraspaso"  AS DetalleTraspaso
INNER JOIN  "BSCL.APV.BD::TRA.Traspaso" AS Traspaso
ON Traspaso."IdTraspaso"= DetalleTraspaso."IdTraspaso"
INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Instrumento
ON Instrumento."IdInstrumento" = DetalleTraspaso."IdInstrumento"
WHERE DetalleTraspaso."IdTraspaso"= :IdTraspaso;