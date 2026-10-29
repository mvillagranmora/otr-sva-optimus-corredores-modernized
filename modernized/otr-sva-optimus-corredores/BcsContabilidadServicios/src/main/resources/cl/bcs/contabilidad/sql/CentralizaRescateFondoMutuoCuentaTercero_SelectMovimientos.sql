SELECT 
	TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion"
	,Solicitudes."IdSolicitudAporteRescate"				AS "IdMovimiento"
	,Solicitudes."IdTipoOperacion"						AS "IdTipoMovimiento"
	,Solicitudes."IdEmpresa"         					AS "IdEmpresa"							    
	,Solicitudes."IdSolicitudAporteRescate"				AS "Numero"
	,Solicitudes."IdSolicitudAporteRescate" 				AS "Linea"
	,YEAR(Solicitudes."FechaCertificacion") ||'-'|| RIGHT('0' || MONTH(Solicitudes."FechaCertificacion"),2) ||'-'|| RIGHT('0' || DAYOFMONTH(Solicitudes."FechaCertificacion"),2) AS "Fecha"
	,Solicitudes."DocumentoIdentidad"					AS "DocumentoIdentidad"
	,Solicitudes."IdPortafolio"							AS "IdPortafolio"
	,TO_BIGINT(YEAR(Solicitudes."FechaCertificacion"))	AS "Ejercicio"
	,RIGHT('0' || MONTH(Solicitudes."FechaCertificacion"),2) ||'-'|| YEAR(Solicitudes."FechaCertificacion") 	AS "Periodo"
	,ABS(Solicitudes."MontoPago")							AS "Monto"
	,Solicitudes."IdMonedaFondo"                       		AS "IdMoneda"                                   
	,Solicitudes."Estado"								AS "Estado"
	

	,Solicitudes."IdFondo"
	
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudesAporteRescateCentralizacion" Solicitudes
WHERE Solicitudes."IdEmpresa" = :IdEmpresa 
AND (Solicitudes."FechaCertificacion" = :FechaContable )
AND Solicitudes."TipoSolicitud" = 'RES'
AND Solicitudes."Estado" = 'CRT'
AND Solicitudes."DocumentoIdentidad" != :DocumentoIdentidadCorredora;