SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarDeudoresAcreedoresPorIntermediacionCorredor";
GO
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarDeudoresAcreedoresPorIntermediacionCorredor" As 

	SELECT 	"IdEmpresa",
			"IdTransaccion",
			"FolioTransaccion",
			"CodigoOperadorContraparte", 
			"CodigoOperadorCorredor", 
			"IdCorredorContraparte", 
			"NombreCorredorContraparte",
			"IndicadorOrdenDirecta", 
--			"EsODUI", 
			"IdClasificacionTipoInstrumento", 
			"NombreClasificacionTipoInstrumento", 
			"IdTipoInstrumento",  
			"NombreTipoInstrumento",
			"IdEmisor", 
			"IdInstrumentoUI", 
			"InstrumentoUI",
			"FechaOperacion",
			"FechaOperacionTransaccion",
			"FechaLiquidacion", 
			"IdCondicionDeLiquidacion", 
			"CodigoCondicionDeLiquidacion", 
			"NombreCondicionDeLiquidacion",
			"IdMonedaTransaccion",
			"IdMonedaInstrumento",
			CASE 
				WHEN "IdClasificacionTipoInstrumento" = 'IF'
					THEN "ValorRescateTransaccion"
				ELSE
					"CantidadTransaccion"
			END AS "CantidadTransaccion",
--			"CantidadTransaccion", 
			"PrecioTransaccion", 
			"Tasa",
			CASE 
				WHEN "IdClasificacionTipoInstrumento" = 'IF'
					THEN "Tasa"
				ELSE
					"PrecioTransaccion"
			END AS "PrecioTasa",
			"MontoTransaccion",
			"ValorRescateTransaccion",
			"IdTipoOperacion", 
			"AccionOrden", 
			"IndicadorDCV"
	FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccion"
	ORDER BY "IdTransaccion" DESC

GO
GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarDeudoresAcreedoresPorIntermediacionCorredor" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
