UPDATE "BSCL.Plataforma.BD::PLA.Configuracion" 
SET "Valor" = :Valor, "FechaModificacion" = 
CASE 
 WHEN :Valor = 'S' THEN CURRENT_UTCTIMESTAMP
  ELSE
 "FechaModificacion"
END 
WHERE "CodigoParametro" = 'PLA.FlagEjecucionProcesoRecPAC';