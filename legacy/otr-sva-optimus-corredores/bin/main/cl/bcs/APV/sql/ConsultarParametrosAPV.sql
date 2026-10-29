SELECT			
  "IdParametro"			
, "NombreParametro"			
, "ValorParametro"		
FROM "BSCL.APV.BD::APV.Parametro"
WHERE "TipoParametro" = :TipoParametro;