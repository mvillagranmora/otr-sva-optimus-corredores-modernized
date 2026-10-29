/***********************************************************************************************
 * Creado : EME
 * Fecha  : 07/05/2018                                      
 * Descripcion : Consulta aportes y rescates de fondos mutuos
 ***********************************************************************************************/


SELECT 
  '' AS "IdIng"             
, T."Rut"
, T."TipoMovimiento"
, T."IdInstrumento"
, T."NombreFFMM"
, COUNT(1) AS "CantidadOperaciones"
, SUM(COALESCE(T."Valor",0)) AS "Valor"
, T."TipoTransaccion"
, T."FechaTransaccion"
, T."MonedaTransaccion"
, T."TipoCuenta"
,TO_BIGINT(0)                           AS "Lote"
,T."ValorCuota"
,SUM(COALESCE(T."NumeroDeCuotas",0)) AS "NumeroDeCuotas"
, T."FechaLiquidez"
, TO_BIGINT(0)   						  AS "IdAdministradora"
, TO_BIGINT(0)   						  AS "Flag"
, ''				 		  AS "Descripcion"
, T."TipoCambio"
, SUM(COALESCE(T."Monto",0)) AS "Monto"
, T."FechaCuota"
, T."Serie"
, T."IdEmisor"
, T."FondoExtranjero"
, T."IdSerieFondo"
, T."ISIN"
, T."ISINDestino"
FROM  (
SELECT             
  REPLACE(REPLACE(EM."DocumentoIdentidad",'-', ''), '.', '') AS "Rut"
, CASE WHEN CAR."TipoSolicitud" = 'APO' THEN 'A' 
  WHEN DESTINO."Switch" = 'S' THEN 'S'
  ELSE 'R'  END    AS "TipoMovimiento"
, REPLACE(REPLACE(CAR."CodigoSVS",'-', ''), '.', '') AS "IdInstrumento"
, CAR."NombreFondo"		   AS "NombreFFMM"
, CASE WHEN COALESCE(DESTINO."IdSolicitudAporteRescate",0) = 0 THEN COALESCE(CAR."Monto",0) ELSE (COALESCE(CAR."Monto",0) * DESTINO."Porcentaje")/100 END AS "Valor"
, CASE CAR."CalculoPorMontoCuota"
  WHEN 'C' THEN TO_BIGINT(2) 
  WHEN 'P' THEN TO_BIGINT(2)
  WHEN 'T' THEN TO_BIGINT(2)
  WHEN 'M' THEN TO_BIGINT(1)
  END 						  AS "TipoTransaccion"
, CAR."FechaAceptacion"		  AS "FechaTransaccion"
, CAR."IdMoneda"			  AS "MonedaTransaccion"
, CASE  
	WHEN FONDO."FondoExtranjero" = 'S' THEN ''
	WHEN RFM."IdRegimenFondoMutuo" IN ('APV-AN','BF','DC-AL','DC-DL','DC-R','APV-B','APV-A') THEN '02'
	WHEN RFM."IdRegimenFondoMutuo" = '57B' THEN '03'
	WHEN RFM."IdRegimenFondoMutuo" = '57T' THEN '04'
	ELSE '01' END AS "TipoCuenta"
, CAR."ValorCuota"
, CASE WHEN COALESCE(DESTINO."IdSolicitudAporteRescate",0) = 0 THEN COALESCE(CAR."NumeroCuotas",0) ELSE (COALESCE(CAR."NumeroCuotas",0) * DESTINO."Porcentaje")/100 END AS "NumeroDeCuotas"
, CAR."FechaLiquidacion"		  AS "FechaLiquidez"
, CAR."ParidadFechaInversion" AS "TipoCambio"
, CASE WHEN COALESCE(DESTINO."IdSolicitudAporteRescate",0) = 0 THEN COALESCE(CAR."CostoMonedaLocal",0) ELSE (COALESCE(CAR."CostoMonedaLocal",0) * DESTINO."Porcentaje")/100 END AS "Monto"
, CAR."FechaCertificacion"	  AS "FechaCuota"
, CAR."CodigoSerie"			  AS "Serie"
, CAR."IdEmisor"			  AS "IdEmisor"
, FONDO."FondoExtranjero"
, CAR."IdSerieFondo"
, SERIE."ISIN"
, CASE WHEN DESTINO."Switch" = 'S' THEN COALESCE(SERIE_DESTINO."ISIN",'') ELSE '' END AS "ISINDestino"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" AS CAR
INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS FONDO ON FONDO."IdFondo" = CAR."IdFondo"
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SERIE ON SERIE."IdSerieFondo" = CAR."IdSerieFondo"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.ReinversionSerieFondoDestino" AS DESTINO ON DESTINO."IdSolicitudAporteRescate" = CAR."IdSolicitudAporteRescate"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SERIE_DESTINO ON SERIE_DESTINO."IdSerieFondo" = DESTINO."IdSerieFondoDestino"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS  EM ON EM."IdEmisor" = CAR."IdEmisor"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS RFM ON RFM."IdRegimenFondoMutuo" = CAR."IdRegimenTributario"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS TIP ON TIP."IdTipoMovimientoFondoMutuo" = CAR."IdTipoMovimientoFondoMutuo"
WHERE CAR."IdSolicitudAporteRescate" IN (:Solicitudes) AND CAR."FolioOmnibus" = 0 AND CAR."EstadoSolicitud" = 'ING' AND TIP."GeneraOmnibus" = 'S'
UNION ALL 
SELECT             
  REPLACE(REPLACE(EM."DocumentoIdentidad",'-', ''), '.', '') AS "Rut"
, CASE WHEN CAR."TipoSolicitud" = 'APO' THEN 'A' 
  WHEN DESTINO."Switch" = 'S' THEN 'S'
  ELSE 'R'  END    AS "TipoMovimiento"
, REPLACE(REPLACE(CAR."CodigoSVS",'-', ''), '.', '') AS "IdInstrumento"
, CAR."NombreFondo"		   AS "NombreFFMM"
, CASE WHEN COALESCE(DESTINO."IdSolicitudAporteRescate",0) = 0 THEN COALESCE(CAR."Monto",0) ELSE (COALESCE(CAR."Monto",0) * DESTINO."Porcentaje")/100 END AS "Valor"
, CASE CAR."CalculoPorMontoCuota"
  WHEN 'C' THEN TO_BIGINT(2) 
  WHEN 'P' THEN TO_BIGINT(2)
  WHEN 'T' THEN TO_BIGINT(2)
  WHEN 'M' THEN TO_BIGINT(1)
  END 						  AS "TipoTransaccion"
, CAR."FechaAceptacion"		  AS "FechaTransaccion"
, CAR."IdMoneda"			  AS "MonedaTransaccion"
, CASE  
	WHEN FONDO."FondoExtranjero" = 'S' THEN ''
	WHEN RFM."IdRegimenFondoMutuo" IN ('APV-AN','BF','DC-AL','DC-DL','DC-R','APV-B','APV-A') THEN '02'
	WHEN RFM."IdRegimenFondoMutuo" = '57B' THEN '03'
	WHEN RFM."IdRegimenFondoMutuo" = '57T' THEN '04'
	ELSE '01' END AS "TipoCuenta"
, CAR."ValorCuota"
, CASE WHEN COALESCE(DESTINO."IdSolicitudAporteRescate",0) = 0 THEN COALESCE(CAR."NumeroCuotas",0) ELSE (COALESCE(CAR."NumeroCuotas",0) * DESTINO."Porcentaje")/100 END AS "NumeroDeCuotas"
, CAR."FechaLiquidacion"		  AS "FechaLiquidez"
, CAR."ParidadFechaInversion" AS "TipoCambio"
, CASE WHEN COALESCE(DESTINO."IdSolicitudAporteRescate",0) = 0 THEN COALESCE(CAR."CostoMonedaLocal",0) ELSE (COALESCE(CAR."CostoMonedaLocal",0) * DESTINO."Porcentaje")/100 END AS "Monto"
, CAR."FechaCertificacion"	  AS "FechaCuota"
, CAR."CodigoSerie"			  AS "Serie"
, CAR."IdEmisor"			  AS "IdEmisor"
, FONDO."FondoExtranjero"
, CAR."IdSerieFondo"
, SERIE."ISIN"
, CASE WHEN DESTINO."Switch" = 'S' THEN COALESCE(SERIE_DESTINO."ISIN",'') ELSE '' END AS "ISINDestino"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" AS CAR
INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS FONDO ON FONDO."IdFondo" = CAR."IdFondo"
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SERIE ON SERIE."IdSerieFondo" = CAR."IdSerieFondo"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.ReinversionSerieFondoDestino" AS DESTINO ON DESTINO."IdSolicitudAporteRescate" = CAR."IdSolicitudAporteRescate"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SERIE_DESTINO ON SERIE_DESTINO."IdSerieFondo" = DESTINO."IdSerieFondoDestino"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS  EM ON EM."IdEmisor" = CAR."IdEmisor"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS RFM ON RFM."IdRegimenFondoMutuo" = CAR."IdRegimenTributario"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS TIP ON TIP."IdTipoMovimientoFondoMutuo" = CAR."IdTipoMovimientoFondoMutuo"
WHERE (CAR."IdSolicitudAporteRescateRelacionado" IN (:Solicitudes) ) AND CAR."FolioOmnibus" = 0 AND CAR."EstadoSolicitud" = 'ING' AND TIP."GeneraOmnibus" = 'S' AND CAR."IdTipoMovimientoFondoMutuo" = 'RESCNN'

) T
GROUP BY 
  T."Rut"
, T."TipoMovimiento"
, T."IdInstrumento"
, T."NombreFFMM"
, T."TipoTransaccion"
, T."FechaTransaccion"
, T."MonedaTransaccion"
, T."TipoCuenta"
, T."ValorCuota"
, T."FechaLiquidez"
, T."TipoCambio"
, T."FechaCuota"
, T."Serie"
, T."IdEmisor"
, T."FondoExtranjero"
, T."IdSerieFondo"
, T."ISIN"
, T."ISINDestino"
ORDER BY T."TipoTransaccion",T."TipoMovimiento"  DESC;

