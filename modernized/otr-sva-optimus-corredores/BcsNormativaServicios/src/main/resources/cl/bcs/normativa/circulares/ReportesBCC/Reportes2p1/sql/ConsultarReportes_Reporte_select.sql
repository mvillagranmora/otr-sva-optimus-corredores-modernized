SELECT *
  FROM "BSCL.Normativa.BD::NOR.DatosREPO2P1"
 WHERE (:rutContraparte1 IS NULL OR "RUTContraparte1"  = :rutContraparte1)
  AND (:fechaNegociacion IS NULL OR "FechaNegociacion" = :fechaNegociacion)
  AND (:horaNegociacion IS NULL OR "HoraNegociacion" = :horaNegociacion)
  AND (:identificador IS NULL OR "Identificador"  = :identificador);