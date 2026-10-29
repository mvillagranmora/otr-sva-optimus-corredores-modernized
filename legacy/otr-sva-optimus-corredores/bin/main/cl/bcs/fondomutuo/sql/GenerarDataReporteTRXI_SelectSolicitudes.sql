SELECT 
CAR."IdSerieFondo"
,CAR."IdSolicitudAporteRescate"
,CASE  
WHEN RFM."IdRegimenFondoMutuo" IN ('APV-AN','BF','DC-AL','DC-DL','DC-R','APV-B','APV-A') THEN '02'
WHEN RFM."IdRegimenFondoMutuo" = '57B' THEN '03'
WHEN RFM."IdRegimenFondoMutuo" = '57T' THEN '04'
ELSE '01' END AS "TipoCuenta"
,CASE WHEN CAR."TipoSolicitud" = 'APO' THEN 'A'
	  WHEN DESTINO."Switch" = 'S' THEN 'S'
      ELSE 'R'  END AS "TipoMovimiento"
,CASE CAR."CalculoPorMontoCuota"
  WHEN 'C' THEN TO_BIGINT(2) 
  WHEN 'P' THEN TO_BIGINT(2)
  WHEN 'T' THEN TO_BIGINT(2)
  WHEN 'M' THEN TO_BIGINT(1)
  END 	 AS "TipoTransaccion"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" AS CAR
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS RFM ON RFM."IdRegimenFondoMutuo" = CAR."IdRegimenTributario"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.ReinversionSerieFondoDestino" AS DESTINO ON CAR."IdSolicitudAporteRescate" = DESTINO."IdSolicitudAporteRescate"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS TIP ON TIP."IdTipoMovimientoFondoMutuo" = CAR."IdTipoMovimientoFondoMutuo"
WHERE CAR."IdSolicitudAporteRescate" IN (:Solicitudes) AND CAR."FolioOmnibus" = 0 AND CAR."EstadoSolicitud" = 'ING' AND TIP."GeneraOmnibus" = 'S'
UNION ALL
SELECT 
CAR."IdSerieFondo"
,CAR."IdSolicitudAporteRescate"
,CASE  
WHEN RFM."IdRegimenFondoMutuo" IN ('APV-AN','BF','DC-AL','DC-DL','DC-R','APV-B','APV-A') THEN '02'
WHEN RFM."IdRegimenFondoMutuo" = '57B' THEN '03'
WHEN RFM."IdRegimenFondoMutuo" = '57T' THEN '04'
ELSE '01' END AS "TipoCuenta"
,CASE WHEN CAR."TipoSolicitud" = 'APO' THEN 'A'
	  WHEN DESTINO."Switch" = 'S' THEN 'S'
      ELSE 'R'  END AS "TipoMovimiento"
,CASE CAR."CalculoPorMontoCuota"
  WHEN 'C' THEN TO_BIGINT(2) 
  WHEN 'P' THEN TO_BIGINT(2)
  WHEN 'T' THEN TO_BIGINT(2)
  WHEN 'M' THEN TO_BIGINT(1)
  END 	 AS "TipoTransaccion"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" AS CAR
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS RFM ON RFM."IdRegimenFondoMutuo" = CAR."IdRegimenTributario"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.ReinversionSerieFondoDestino" AS DESTINO ON CAR."IdSolicitudAporteRescate" = DESTINO."IdSolicitudAporteRescate"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS TIP ON TIP."IdTipoMovimientoFondoMutuo" = CAR."IdTipoMovimientoFondoMutuo"
WHERE CAR."IdSolicitudAporteRescateRelacionado" IN (:Solicitudes) AND CAR."FolioOmnibus" = 0 AND CAR."EstadoSolicitud" = 'ING' AND TIP."GeneraOmnibus" = 'S' AND CAR."IdTipoMovimientoFondoMutuo" = 'RESCNN';