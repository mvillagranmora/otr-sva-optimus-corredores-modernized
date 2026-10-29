SELECT --18Q 
 sol."IdSolicitudAporteRescate"
,sol."FolioContrato"
,sol."RentabilidadReal"
,sol."CostoUF"
,sol."MontoMonedaSistema"
,sol."ValorCuota"
,sol."NumeroCuotas" 
,sol."FechaCertificacion"
,sol."FechaValorCuota"
,sol."RazonSocial"
,sol."NombreCompleto"
,sol."DocumentoIdentidad"
,sol."IdCliente"
,sol."CodigoPortafolio"
,sol."DescripcionPortafolio"
,sol."PropositoPortafolio"
,sol."IndicadorMLT"
,sol."EsIndicadorMLTUI"
,sol."IdSerieFondoOrigen"
,sol."TipoSolicitud"
,sol."DescripcionTipoSolicitud"
,sol."IdEmpresa"
, '18Q' AS "Regimen"
--, 'APV' AS "Regimen"
,COALESCE(TR."FolioMLT",0)  AS "FolioMandato"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" sol 
LEFT JOIN "BSCL.FondoMutuo.BD::TRA.TraspasoDetalleDestino" DT ON (DT."IdSolicitudAporteRescate"=sol."IdSolicitudAporteRescate")
LEFT JOIN "BSCL.FondoMutuo.BD::TRA.Traspaso"  				TR ON (TR."IdTraspaso"=DT."IdTraspaso")
WHERE 	sol."IdTipoMovimientoFondoMutuo" IN  ('APOTRASP')
		AND 	sol."IdRegimenTributario" IN ('18Q')
		AND 	sol."EstadoSolicitud" = 'CRT'
		AND 	sol."IdSolicitudAporteRescate" IN (:IdSolicitudAporteRescate)
		AND 	sol."IdEmpresa"=:IdEmpresa 
UNION ALL --APV		 
		
SELECT 
 sol."IdSolicitudAporteRescate"
,sol."FolioContrato" 
,sol."RentabilidadReal"
,sol."CostoUF"
,sol."MontoMonedaSistema"
,sol."ValorCuota"
,sol."NumeroCuotas" 
,sol."FechaCertificacion"
,sol."FechaValorCuota"
,sol."RazonSocial"
,sol."NombreCompleto"
,sol."DocumentoIdentidad"
,sol."IdCliente"
,sol."CodigoPortafolio"
,sol."DescripcionPortafolio"
,sol."PropositoPortafolio"
,sol."IndicadorMLT"
,sol."EsIndicadorMLTUI"
,sol."IdSerieFondoOrigen"
,sol."TipoSolicitud"
,sol."DescripcionTipoSolicitud"
,sol."IdEmpresa"
,'APV' AS "Regimen"
,0 AS "FolioMandato"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" sol 
WHERE 	sol."IdTipoMovimientoFondoMutuo" IN  ('APOTRASP')
		AND 	sol."IdRegimenTributario" IN ('APV-A','APV-AN','APV-B')
		AND 	sol."EstadoSolicitud" = 'CRT'
		AND 	sol."IdSolicitudAporteRescate" IN (:IdSolicitudAporteRescate)
		AND 	sol."IdEmpresa"=:IdEmpresa