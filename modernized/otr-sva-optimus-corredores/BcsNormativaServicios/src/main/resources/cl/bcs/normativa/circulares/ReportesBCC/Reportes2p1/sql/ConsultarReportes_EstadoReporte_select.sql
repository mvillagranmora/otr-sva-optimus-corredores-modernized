SELECT * 
  FROM "BSCL"."BSCL.Normativa.BD::NOR.DatosREPO2P1"
 WHERE "RUTContraparte1" = :rutContraparte1
  AND "FechaNegociacion" = :fechaNegociacion
  AND "HoraNegociacion" = :horaNegociacion
  AND "Identificador" = :identificador
  AND "Estado" = 'ACT'
  AND "EstadoSICAM" IN ('ENV', '');
