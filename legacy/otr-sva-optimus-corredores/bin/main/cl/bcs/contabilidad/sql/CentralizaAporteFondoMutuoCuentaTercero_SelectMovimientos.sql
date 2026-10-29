SELECT 
	TO_BIGINT(:IdTipoCentralizacion) 					AS "IdTipoCentralizacion"
	,Solicitudes."IdSolicitudAporteRescate"				AS "IdMovimiento"
	,CASE 
	   WHEN Solicitudes."IdTipoOperacion" = 'RESCFM' THEN  'APORFM'
	 ELSE Solicitudes."IdTipoOperacion"	
	 END                                                AS "IdTipoMovimiento"	
	,Solicitudes."IdEmpresa"         					AS "IdEmpresa"							    
	,Solicitudes."IdSolicitudAporteRescate"				AS "Numero"
	,Solicitudes."IdSolicitudAporteRescate" 			AS "Linea"
	,YEAR(Solicitudes."FechaCertificacion") ||'-'|| RIGHT('0' || MONTH(Solicitudes."FechaCertificacion"),2) ||'-'|| RIGHT('0' || DAYOFMONTH(Solicitudes."FechaCertificacion"),2) AS "Fecha"
	,CASE 
	   WHEN Solicitudes."IdTipoMovimientoFondoMutuo" IN ('RESTRASP')  THEN Solicitudes."DocumentoIdentidadDes"
	 ELSE Solicitudes."DocumentoIdentidad"
	 END                                                AS "DocumentoIdentidad"
	,CASE 
	   WHEN Solicitudes."IdTipoMovimientoFondoMutuo" IN ('RESTRASP')  THEN Solicitudes."IdPortafolioDes"
	 ELSE Solicitudes."IdPortafolio"
	 END                                                AS "IdPortafolio"
	,TO_BIGINT(YEAR(Solicitudes."FechaCertificacion"))	AS "Ejercicio"
	,RIGHT('0' || MONTH(Solicitudes."FechaCertificacion"),2) ||'-'|| YEAR(Solicitudes."FechaCertificacion") 	AS "Periodo"
	,ABS(Solicitudes."MontoPago")						AS "Monto"
	,Solicitudes."IdMonedaFondo"                       	AS "IdMoneda"                                  
	,Solicitudes."Estado"								AS "Estado"
	

	,Solicitudes."IdFondo"

FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudesAporteRescateCentralizacion" Solicitudes
WHERE Solicitudes."IdEmpresa" = :IdEmpresa 
  AND (Solicitudes."FechaCertificacion" = :FechaContable )
  AND (Solicitudes."TipoSolicitud" = 'APO' OR Solicitudes."IdTipoMovimientoFondoMutuo" IN ('RESTRASP', 'RESCAM'))
  AND Solicitudes."Estado" = 'CRT'
  AND :DocumentoIdentidadCorredora != CASE 
                                        WHEN Solicitudes."IdTipoMovimientoFondoMutuo" IN ('RESTRASP') THEN Solicitudes."DocumentoIdentidadDes"
                                      ELSE Solicitudes."DocumentoIdentidad"
                                      END
  AND Solicitudes."IdTipoMovimientoFondoMutuo" NOT IN ('RESTRANSF');