SELECT 	 
TO_BIGINT(:IdTipoCentralizacion) 		AS "IdTipoCentralizacion"
,Solicitudes."IdSolicitudAporteRescate" AS "IdMovimiento"
,'MONTO_APORTE'							AS "IdEvento"
,'MONTO APORTE' 						AS "Nombre"
,Solicitudes."MontoMonedaSistema" 		AS "MontoEvento"
,Solicitudes."IdMonedaSistema" 			AS "CodigoMoneda"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudesAporteRescateCentralizacion" Solicitudes
WHERE Solicitudes."IdEmpresa" = :IdEmpresa 
AND (
	(Solicitudes."FechaCertificacion" = :FechaContable )
	)
AND (Solicitudes."TipoSolicitud" = 'APO' OR Solicitudes."IdTipoMovimientoFondoMutuo" IN ('RESTRANSF', 'RESTRASP', 'RESCAM')) 
AND Solicitudes."Estado" = 'CRT'
AND :DocumentoIdentidadCorredora = CASE 
                                       WHEN Solicitudes."IdTipoMovimientoFondoMutuo" IN ('RESTRANSF', 'RESTRASP') THEN Solicitudes."DocumentoIdentidadDes"
                                     ELSE Solicitudes."DocumentoIdentidad"
                                     END 
AND Solicitudes."IdMonedaSistema" = :IdMonedaSistema
UNION
SELECT 
TO_BIGINT(:IdTipoCentralizacion) 		AS "IdTipoCentralizacion"
,Solicitudes."IdSolicitudAporteRescate" AS "IdMovimiento"
,'VALOR_APORTE'							AS "IdEvento"
,'VALOR APORTE' 						AS "Nombre"
,Solicitudes."MontoPago" 				AS "MontoEvento"
,Solicitudes."IdMonedaFondo" 			AS "CodigoMoneda"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudesAporteRescateCentralizacion" Solicitudes
WHERE Solicitudes."IdEmpresa" = :IdEmpresa 
AND (
	(Solicitudes."FechaCertificacion" = :FechaContable )
	)
AND (Solicitudes."TipoSolicitud" = 'APO' OR Solicitudes."IdTipoMovimientoFondoMutuo" IN ('RESTRANSF', 'RESTRASP', 'RESCAM'))
AND Solicitudes."Estado" = 'CRT'
AND :DocumentoIdentidadCorredora = CASE 
                                       WHEN Solicitudes."IdTipoMovimientoFondoMutuo" IN ('RESTRANSF', 'RESTRASP') THEN Solicitudes."DocumentoIdentidadDes"
                                     ELSE Solicitudes."DocumentoIdentidad"
                                     END  
AND Solicitudes."IdMonedaFondo" <> :IdMonedaSistema
UNION
SELECT
 TO_BIGINT(:IdTipoCentralizacion) 	AS "IdTipoCentralizacion"
,Solicitudes."IdSolicitudAporteRescate"	AS "IdMovimiento"      
,'UTILIDAD'							AS "IdEvento"
,'Utilidad' 						AS "Nombre"			 
,ABS(Solicitudes."Spread")			AS "Monto"
,Solicitudes."IdMonedaFondo"		AS "IdMonedaInstrumento"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudesAporteRescateCentralizacion" Solicitudes
WHERE Solicitudes."IdEmpresa" = :IdEmpresa 
AND (
	(Solicitudes."FechaCertificacion" = :FechaContable )
	)
AND (Solicitudes."TipoSolicitud" = 'APO' OR Solicitudes."IdTipoMovimientoFondoMutuo" IN ('RESTRANSF', 'RESTRASP', 'RESCAM'))
AND Solicitudes."Estado" = 'CRT'
AND :DocumentoIdentidadCorredora = CASE 
                                       WHEN Solicitudes."IdTipoMovimientoFondoMutuo" IN ('RESTRANSF', 'RESTRASP') THEN Solicitudes."DocumentoIdentidadDes"
                                     ELSE Solicitudes."DocumentoIdentidad"
                                     END
AND Solicitudes."Spread" > 0
UNION
SELECT
TO_BIGINT(:IdTipoCentralizacion) 	AS "IdTipoCentralizacion"
,Solicitudes."IdSolicitudAporteRescate"	AS "IdMovimiento"      
,'PERDIDA'							AS "IdEvento"
,'Perdida' 							AS "Nombre"			 
,ABS(Solicitudes."Spread")			AS "Monto"
,Solicitudes."IdMonedaFondo"		AS "IdMonedaInstrumento"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudesAporteRescateCentralizacion" Solicitudes
WHERE Solicitudes."IdEmpresa" = :IdEmpresa 
AND (
	(Solicitudes."FechaCertificacion" = :FechaContable )
	)
AND (Solicitudes."TipoSolicitud" = 'APO' OR Solicitudes."IdTipoMovimientoFondoMutuo" IN ('RESTRANSF', 'RESTRASP', 'RESCAM'))
AND Solicitudes."Estado" = 'CRT'
AND :DocumentoIdentidadCorredora = CASE 
                                       WHEN Solicitudes."IdTipoMovimientoFondoMutuo" IN ('RESTRANSF', 'RESTRASP') THEN Solicitudes."DocumentoIdentidadDes"
                                     ELSE Solicitudes."DocumentoIdentidad"
                                     END
AND Solicitudes."Spread" < 0