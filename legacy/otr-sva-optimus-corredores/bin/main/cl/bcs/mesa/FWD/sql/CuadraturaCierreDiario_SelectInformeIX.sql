SELECT COUNT(1) AS "Estado"
FROM "BSCL.Mesa.BD::FWD.LogInforme"
WHERE 
"Informe" =  'ANEXO_IX'
AND "FechaEjecucion" = :Fecha
AND "FechaInforme" = :Fecha
AND "IdEmpresa" = :IdEmpresa;