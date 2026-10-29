SELECT *
  FROM "BSCL"."BSCL.Normativa.BD::NOR.DatosREPO3P1"
 WHERE "Identificador" = :identificador
  AND "RUTReportante" = :rutReportante
  AND "FechaOperacion" = :fechaOperacion
  AND "HoraOperacion" = :horaOperacion
  AND "Estado" = 'ACT'
  AND "EstadoSICAM" IN ('ENV', '');
