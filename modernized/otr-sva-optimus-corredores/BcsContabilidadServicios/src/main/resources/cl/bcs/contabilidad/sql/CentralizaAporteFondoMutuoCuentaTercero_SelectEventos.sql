SELECT 	 
	TO_BIGINT(:IdTipoCentralizacion) 		AS "IdTipoCentralizacion"
	,Solicitudes."IdSolicitudAporteRescate" AS "IdMovimiento"
	,'MONTO_APORTE'							AS "IdEvento"
	,'MONTO APORTE' 						AS "Nombre"
	,Solicitudes."MontoMonedaSistema" 		AS "MontoEvento"
	,Solicitudes."IdMonedaSistema" 			AS "CodigoMoneda"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudesAporteRescateCentralizacion" Solicitudes
WHERE Solicitudes."IdEmpresa" = :IdEmpresa 
  AND (Solicitudes."FechaCertificacion" = :FechaContable )
  AND (Solicitudes."TipoSolicitud" = 'APO' OR Solicitudes."IdTipoMovimientoFondoMutuo" IN ('RESTRASP', 'RESCAM')) 
  AND Solicitudes."Estado" = 'CRT' 
  AND :DocumentoIdentidadCorredora != CASE 
                                        WHEN Solicitudes."IdTipoMovimientoFondoMutuo" IN ('RESTRASP') THEN Solicitudes."DocumentoIdentidadDes"
                                      ELSE Solicitudes."DocumentoIdentidad"
                                      END    
  AND Solicitudes."IdMonedaSistema" = :IdMonedaSistema
  AND Solicitudes."IdTipoMovimientoFondoMutuo" NOT IN ('RESTRANSF')
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
  AND (Solicitudes."FechaCertificacion" = :FechaContable )
  AND (Solicitudes."TipoSolicitud" = 'APO' OR Solicitudes."IdTipoMovimientoFondoMutuo" IN ('RESTRASP', 'RESCAM')) 
  AND Solicitudes."Estado" = 'CRT'
  AND :DocumentoIdentidadCorredora != CASE 
                                        WHEN Solicitudes."IdTipoMovimientoFondoMutuo" IN ('RESTRASP') THEN Solicitudes."DocumentoIdentidadDes"
                                      ELSE Solicitudes."DocumentoIdentidad"
                                      END
  AND Solicitudes."IdMonedaFondo" <> :IdMonedaSistema
  AND Solicitudes."IdTipoMovimientoFondoMutuo" NOT IN ('RESTRANSF')
UNION
SELECT
 	TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion"
	,Solicitudes."IdSolicitudAporteRescate"	AS "IdMovimiento"      
	,'UTILIDAD'							AS "IdEvento"
	,'Utilidad' 						AS "Nombre"			 
	,ABS(Solicitudes."Spread")			AS "Monto"
	,Solicitudes."IdMonedaFondo"		AS "IdMonedaInstrumento"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudesAporteRescateCentralizacion" Solicitudes
WHERE Solicitudes."IdEmpresa" = :IdEmpresa 
  AND (Solicitudes."FechaCertificacion" = :FechaContable )
  AND (Solicitudes."TipoSolicitud" = 'APO' OR Solicitudes."IdTipoMovimientoFondoMutuo" IN ('RESTRASP', 'RESCAM'))
  AND Solicitudes."Estado" = 'CRT'
  AND :DocumentoIdentidadCorredora != CASE 
                                        WHEN Solicitudes."IdTipoMovimientoFondoMutuo" IN ('RESTRASP') THEN Solicitudes."DocumentoIdentidadDes"
                                      ELSE Solicitudes."DocumentoIdentidad"
                                      END
  AND Solicitudes."Spread" > 0
  AND Solicitudes."IdTipoMovimientoFondoMutuo" NOT IN ('RESTRANSF')
UNION
SELECT
	TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion"
	,Solicitudes."IdSolicitudAporteRescate"	AS "IdMovimiento"      
	,'PERDIDA'							AS "IdEvento"
	,'Perdida' 							AS "Nombre"			 
	,ABS(Solicitudes."Spread")			AS "Monto"
	,Solicitudes."IdMonedaFondo"		AS "IdMonedaInstrumento"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudesAporteRescateCentralizacion" Solicitudes
WHERE Solicitudes."IdEmpresa" = :IdEmpresa 
  AND (Solicitudes."FechaCertificacion" = :FechaContable )
  AND (Solicitudes."TipoSolicitud" = 'APO' OR Solicitudes."IdTipoMovimientoFondoMutuo" IN ('RESTRASP', 'RESCAM'))  
  AND Solicitudes."Estado" = 'CRT'
  AND :DocumentoIdentidadCorredora != CASE 
                                        WHEN Solicitudes."IdTipoMovimientoFondoMutuo" IN ('RESTRASP') THEN Solicitudes."DocumentoIdentidadDes"
                                      ELSE Solicitudes."DocumentoIdentidad"
                                      END
  AND Solicitudes."Spread" < 0
  AND Solicitudes."IdTipoMovimientoFondoMutuo" NOT IN ('RESTRANSF')
  -- 
UNION  
SELECT 	 
	TO_BIGINT(:IdTipoCentralizacion) 		AS "IdTipoCentralizacion"
	,Solicitudes."IdSolicitudAporteRescate" AS "IdMovimiento"
	,'MONTO_APORTE_CLP_INICIAL'				AS "IdEvento"
	,'MONTO APORTE CLP INICIAL' 			AS "Nombre"
	,CASE 
    	WHEN Spot."IdOperacionSpot" IS NOT NULL THEN Spot."MontoMonedaSecundaria"
    	ELSE Solicitudes."MontoMonedaSistema"
    END										AS "MontoEvento"
	,Solicitudes."IdMonedaSistema" 			AS "CodigoMoneda"
	
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudesAporteRescateCentralizacion" Solicitudes
LEFT OUTER  JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" AS Spot ON (Spot."IdOperacionSpotOrigen" = Solicitudes."IdOperacionSpot" AND Spot."Estado" != 'MOD' AND Spot."IdOperacionSpotOrigen" != 0)

WHERE Solicitudes."IdEmpresa" = :IdEmpresa 
  AND (Solicitudes."FechaCertificacion" = :FechaContable )
  AND (Solicitudes."TipoSolicitud" = 'APO' OR Solicitudes."IdTipoMovimientoFondoMutuo" IN ('RESTRASP', 'RESCAM')) 
  AND Solicitudes."Estado" = 'CRT' 
  AND :DocumentoIdentidadCorredora != CASE 
                                        WHEN Solicitudes."IdTipoMovimientoFondoMutuo" IN ('RESTRASP') THEN Solicitudes."DocumentoIdentidadDes"
                                      ELSE Solicitudes."DocumentoIdentidad"
                                      END    
  AND Solicitudes."IdMonedaSistema" = :IdMonedaSistema
  AND Solicitudes."IdTipoMovimientoFondoMutuo" NOT IN ('RESTRANSF')  