SELECT
 "IdMoneda"
,"TipoMovimiento"
,SUM ("Monto") AS "MontoSumado"
FROM  "BSCL.CuentaInversion.BD.Vistas::ConsultarCartolaDeMovimientos"
WHERE "IdCliente"    = :p_IdCliente 
AND   "IdPortafolio" = :p_IdPortafolio
AND   "FechaSistema"      BETWEEN  :p_FechaInicio AND :p_FechaFin  
AND   "IdModulo"          = 'TES'
AND   "IdEstado"          = 'CRT'
AND   "TipoMovimiento"    = :p_TipoMovimiento
GROUP BY "IdMoneda","TipoMovimiento" 
