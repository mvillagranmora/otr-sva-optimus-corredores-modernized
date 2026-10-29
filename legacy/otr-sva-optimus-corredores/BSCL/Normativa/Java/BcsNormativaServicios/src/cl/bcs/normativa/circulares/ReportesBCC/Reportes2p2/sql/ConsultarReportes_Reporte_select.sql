SELECT *
  FROM "BSCL.Normativa.BD::NOR.DatosREPO2P2"
 WHERE (:rutEntidadMCF IS NULL OR "RUTEntidadMCF"  = :rutEntidadMCF)
  AND (:fecha IS NULL OR "Fecha" = :fecha)
  AND (:monedaSaldo IS NULL OR "MonedaSaldo" = :monedaSaldo);