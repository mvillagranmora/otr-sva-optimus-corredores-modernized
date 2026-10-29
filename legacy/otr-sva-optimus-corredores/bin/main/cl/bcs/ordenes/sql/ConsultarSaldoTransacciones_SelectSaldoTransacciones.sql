
SELECT  
	tran."IdTransaccion",
	tran."CantidadTransaccion",	
	tran."MontoTransaccion",
	tran."FechaSistemaCreacion",
	tran."HoraSistemaCreacion",
	tran."FechaOperacionTransaccion",
	tran."FolioTransaccion",
	tran."PrecioTransaccion",
	tran."InstrumentoUI",
	tran."IdTipoOperacion",
	tran."NombreTipoOperacion",
	tran."IdCondicionDeLiquidacion",
	tran."NombreCondicionDeLiquidacion",
	tran."IdBolsa",
	tran."IdMonedaTransaccion",
	tran."FechaVencimiento",
	tran."IdEmisor",
	tran."Tasa",
	tran."IdTipoReajuste",
	tran."NemotecnicoSVS",
	tran."EsFungible",
	"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(tran."EsFungible") as "EsFungibleUI",
	tran."IndicadorOrdenDirecta",
	tran."EsVentaCorta",
	"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(tran."EsVentaCorta") as "EsVentaCortaUI",
	tran."NombreTipoInstrumento",
	tran."NombreClasificacionTipoInstrumento",
	
	tran."NombreCortoContraparte" AS "NombreCorredorContraparte",
	
	CASE	
		WHEN tran."EsFungible" ='S' THEN ASIG."ValorRescateAsignado"
		WHEN tran."EsFungible" ='N' THEN ASIG."CantidadAsignada"
	ELSE 0 END AS "CantidadAsignada",
		
		
	ASIG."IdAsignacion",
	ASIG."IdEstadoAsignacion",
	
	tran."AccionOrden",
	tran."Reservado",
	tran."IdUsuarioReserva",

	CASE
		WHEN tran."IdUsuarioReserva" = 0 THEN ''
		ELSE USU."CodigoUsuario"
	END AS "CodigoUsuarioReserva",

	tran."IdGrupoReserva",	
	
	CAST(0 AS BIGINT) As "IdComprobante",
	CASE
		WHEN tran."IdInstrumentoSubyacente" = 0 THEN tran."IdMonedaTransaccion"
		ELSE tran."IdMonedaInstrumentoSubyacente"
	END AS "IdMonedaInstrumento",
	tran."IdTipoInstrumento",
	tran."InstrumentoNegociacionTran"

FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccionUI" AS tran
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS USU ON (USU."IdUsuario" = tran."IdUsuarioReserva")
LEFT JOIN "BSCL.Orden.BD::ORD.Asignacion" ASIG ON ASIG."IdTransaccion" = tran."IdTransaccion" AND ASIG."IdEstadoAsignacion" != 'ANU'
WHERE tran."IdEmpresa" = :IdEmpresa
AND tran."CantidadDisponibleTransaccion" > 0
--AND tran."FechaOperacionTransaccion" >= '2020-08-21T04:00:00' AND tran."FechaOperacionTransaccion" <= '2020-08-22T03:59:59'
AND tran."FechaOperacionTransaccion" >= :FechaTransaccionInicio AND tran."FechaOperacionTransaccion" <= :FechaTransaccionTermino;


