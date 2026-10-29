SELECT
"TipoCuenta"
,"TipoCuentaArchivo"
,"TipoMovimiento"
,"IdEmisor"
,"FondoExtranjero"
,SUM(CASE "TipoTransaccion" WHEN 1 THEN "Monto" ELSE "NumeroCuotas" END) AS "Monto"
,"TipoTransaccion"
,"IdMoneda"
FROM
(
SELECT 
CASE  
WHEN RFM."IdRegimenFondoMutuo" IN ('APV-AN','BF','DC-AL','DC-DL','DC-R','APV-B','APV-A') THEN '02'
WHEN RFM."IdRegimenFondoMutuo" = '57B' THEN '03'
WHEN RFM."IdRegimenFondoMutuo" = '57T' THEN '04'
ELSE '01' END AS "TipoCuenta"
,CASE 
WHEN RFM."IdRegimenFondoMutuo" IN ('APV-AN','BF','DC-AL','DC-DL','DC-R','APV-B','APV-A') THEN 'APV'
WHEN RFM."IdRegimenFondoMutuo" IN ('57B','57T') THEN '57B'
ELSE 'FMM' END AS "TipoCuentaArchivo"
,CASE CAR."TipoSolicitud"
WHEN 'APO' THEN 'A'
ELSE 'R'  END AS "TipoMovimiento"
,CAR."IdEmisor"
,FONDO."FondoExtranjero"
,COALESCE(CAR."MontoPagoLocal",0) AS "Monto"
,COALESCE(CAR."NumeroCuotas",0) AS "NumeroCuotas"
, CASE CAR."CalculoPorMontoCuota"
  WHEN 'C' THEN TO_BIGINT(2) 
  WHEN 'P' THEN TO_BIGINT(2)
  WHEN 'T' THEN TO_BIGINT(2)
  WHEN 'M' THEN TO_BIGINT(1)
  END AS "TipoTransaccion"
,CAR."IdMoneda"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" AS CAR
INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS FONDO ON FONDO."IdFondo" = CAR."IdFondo"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS RFM ON RFM."IdRegimenFondoMutuo" = CAR."IdRegimenTributario"
WHERE "IdSolicitudAporteRescate" IN (:Solicitudes) AND CAR."FolioOmnibus" = 0 AND CAR."EstadoSolicitud" IN ('ING','PEN')
)
GROUP BY 
"TipoCuenta"
,"TipoCuentaArchivo"
,"TipoMovimiento"
,"IdEmisor"
,"FondoExtranjero"
,"TipoTransaccion"
,"IdMoneda"
  