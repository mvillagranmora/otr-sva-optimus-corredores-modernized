SELECT 	 
	TO_BIGINT(:IdTipoCentralizacion) 		AS "IdTipoCentralizacion"
	,Solicitudes."IdSolicitudAporteRescate" AS "IdMovimiento"
	,'MONTO_RESCATE'						AS "IdEvento"
	,'MONTO RESCATE' 						AS "Nombre"
	,Solicitudes."MontoMonedaSistema" 		AS "MontoEvento"
	,Solicitudes."IdMonedaSistema" 			AS "CodigoMoneda"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudesAporteRescateCentralizacion" Solicitudes
WHERE Solicitudes."IdEmpresa" = :IdEmpresa 
  AND (Solicitudes."FechaCertificacion" = :FechaContable )
  AND Solicitudes."TipoSolicitud" = 'RES'
  AND Solicitudes."Estado" = 'CRT'
  AND Solicitudes."DocumentoIdentidad" = :DocumentoIdentidadCorredora  
  AND Solicitudes."IdMonedaSistema" = :IdMonedaSistema
UNION
SELECT 
	TO_BIGINT(:IdTipoCentralizacion) 		AS "IdTipoCentralizacion"
	,Solicitudes."IdSolicitudAporteRescate" AS "IdMovimiento"
	,'VALOR_RESCATE'						AS "IdEvento"
	,'VALOR RESCATE' 						AS "Nombre"
	,Solicitudes."MontoPago" 				AS "MontoEvento"
	,Solicitudes."IdMonedaFondo" 			AS "CodigoMoneda"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudesAporteRescateCentralizacion" Solicitudes
WHERE Solicitudes."IdEmpresa" = :IdEmpresa 
  AND (Solicitudes."FechaCertificacion" = :FechaContable )
  AND Solicitudes."TipoSolicitud" = 'RES' 
  AND Solicitudes."Estado" = 'CRT'
  AND Solicitudes."DocumentoIdentidad" = :DocumentoIdentidadCorredora  
  AND Solicitudes."IdMonedaFondo" <> :IdMonedaSistema
UNION
SELECT
	 TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion"
	,"IdSolicitudAporteRescate"			AS "IdMovimiento"      
	,'UTILIDAD'							AS "IdEvento"
	,'Utilidad' 						AS "Nombre"			 
	,ABS("Spread")						AS "Monto"
	,"IdMonedaFondo"					AS "IdMonedaInstrumento"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudesAporteRescateCentralizacion" Solicitudes
WHERE Solicitudes."IdEmpresa" = :IdEmpresa 
  AND (Solicitudes."FechaCertificacion" = :FechaContable )
  AND Solicitudes."TipoSolicitud" = 'RES'
  AND Solicitudes."Estado" = 'CRT'
  AND Solicitudes."DocumentoIdentidad" = :DocumentoIdentidadCorredora
  AND Solicitudes."Spread" > 0
UNION
SELECT
	TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion"
	,"IdSolicitudAporteRescate"			AS "IdMovimiento"      
	,'PERDIDA'							AS "IdEvento"
	,'Perdida' 							AS "Nombre"			 
	,ABS("Spread")						AS "Monto"
	,"IdMonedaFondo"					AS "IdMonedaInstrumento"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudesAporteRescateCentralizacion" Solicitudes
WHERE Solicitudes."IdEmpresa" = :IdEmpresa 
  AND (Solicitudes."FechaCertificacion" = :FechaContable )
  AND Solicitudes."TipoSolicitud" = 'RES'
  AND Solicitudes."Estado" = 'CRT'
  AND Solicitudes."DocumentoIdentidad" = :DocumentoIdentidadCorredora
  AND Solicitudes."Spread" < 0