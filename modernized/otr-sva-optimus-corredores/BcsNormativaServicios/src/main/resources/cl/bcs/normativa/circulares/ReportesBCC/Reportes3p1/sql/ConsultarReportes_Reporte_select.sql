SELECT *
FROM "BSCL"."BSCL.Normativa.BD::NOR.DatosREPO3P1"
WHERE (:rutReportante IS NULL OR "RUTReportante"  = :rutReportante)
AND (:fechaOperacion IS NULL OR "FechaOperacion" = :fechaOperacion)
AND (:horaOperacion IS NULL OR "HoraOperacion" = :horaOperacion)
AND (:identificador IS NULL OR "Identificador"  = :identificador);