SELECT
 "IdMoneda"
,"TipoMovimiento"
,SUM ("Monto") AS "MontoSumado"
FROM  "BSCL.CuentaInversion.BD.Vistas::ConsultarCartolaDeMovimientos"
WHERE "IdCliente"    = :p_IdCliente 
AND   "IdPortafolio" = :p_IdPortafolio
AND   "FechaSistema"      BETWEEN  :p_FechaInicio AND :p_FechaFin  
AND   "IdModulo"          IN ('CTA','TES')
AND   "IdEstado"          = 'CRT'
AND   "TipoMovimiento"    = :p_TipoMovimiento
GROUP BY "IdMoneda","TipoMovimiento" 
UNION ALL
SELECT 
"IdMonedaSistema" AS "IdMoneda",
'CA' AS "TipoMovimiento",
SUM(ROUND("EGRCantidad" * "INGPrecioMercadoActual",0)) AS "MontoSumado" 
FROM "BSCL.Custodia.BD::CUS.Cartera" 
WHERE "IdPortafolio" = :p_IdPortafolio
AND "EGRIdTipoOperacion" = 'RETTIT'
AND "IdConcepto" NOT IN ('CIECCA')
AND "EGRFechaOperacion" BETWEEN :p_FechaInicio AND :p_FechaFin       
GROUP BY "IdMonedaSistema"
