SET SCHEMA "BSCL"; 
GO
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarTransaccionPorAsignar"; 
GO

CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarTransaccionPorAsignar"  
AS SELECT 
	tran."IdEmpresa" ,
	tran."IdTransaccion" ,
	tran."Folio" as "FolioTransaccion" ,
	tran."FechaSistemaCreacion" ,
	tran."HoraSistemaCreacion" ,
	TO_DATE(TO_VARCHAR(tran."FechaOperacion",'YYYY-MM-DD')) as "FechaOperacion",
	tran."FechaOperacion" as "FechaOperacionTransaccion" ,
	tran."FechaLiquidacion" ,
	tran."FechaEmision" ,
	tran."FechaModificacionOperacion",
	tran."IdEmisor" ,
	tran."EsVentaCorta" ,
	"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(tran."EsVentaCorta") as "EsVentaCortaUI",
	tran."FechaVencimiento" ,
	tran."IdSistemaBursatil" ,
	tran."IndicadorOrdenDirecta" ,
	tran."EsCuentaPropia" ,
	tran."EsFungible" ,
	"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(tran."EsFungible") AS "EsFungibleUI",
	tran."EsAPlazo" ,
	"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(tran."EsAPlazo") AS "EsAPlazoUI",
	tran."CodigoOperadorContraparte" ,
	tran."CodigoOperadorCorredor" ,
	--tran."Cantidad" as "CantidadTransaccion",
	CASE
	  WHEN tipoins."IdClasificacionTipoInstrumento" = 'IF' THEN tran."ValorRescate"
	ELSE tran."Cantidad"
	END AS "CantidadTransaccion",
	tran."Monto" as "MontoTransaccion" ,
	tran."Precio" as "PrecioTransaccion" ,
	tran."Tasa" ,
	tran."Reservado" ,
	tran."Plazo",
	tran."IdInstrumento" ,
	tran."Nemotecnico" as "Instrumento" ,
	tran."IdMoneda" AS "IdMonedaInstrumento" ,
	tran."IdMoneda" AS "IdMonedaTransaccion" ,
--SUBYACENTE,
	tran."IdInstrumentoSubyacente" ,
	insasoc."Nemotecnico" AS "InstrumentoSubyacente",
--NEGOCIACION,
	"Instrumento_NEG"."Nemotecnico" AS "InstrumentoNegociacionTran",
--MEZCLADO
	CASE 
		WHEN tipoins."IdTipoInstrumento" = 'SIM' THEN tran."IdInstrumentoSubyacente" 
		WHEN tipoins."IdClasificacionTipoInstrumento" = 'RE' THEN tran."IdInstrumentoSubyacente" 
		ELSE tran."IdInstrumento" 
		END AS "IdInstrumentoUI",
	CASE 
		WHEN tipoins."IdTipoInstrumento" = 'SIM' THEN insasoc."Nemotecnico" 
		WHEN tipoins."IdClasificacionTipoInstrumento" = 'RE' THEN insasoc."Nemotecnico"  
		ELSE ins."Nemotecnico" 
		END AS "InstrumentoUI",
	
	
--tran."IdInstrumentoUI" ,
--tran."InstrumentoUI",
	
	tipoinsasoc."IdTipoInstrumento" AS "IdTipoInstrumentoSubyacente",
	tipoinsasoc."Nombre" AS "NombreTipoInstrumentoSubyacente" ,
	ctiasoc."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumentoSubyacente"  ,
	ctiasoc."Nombre" AS "NombreClasificacionTipoInstrumentoSubyacente" ,
	tran."IdMonedaSubyacente" AS "IdMonedaInstrumentoSubyacente",
	tran."IdTipoInstrumento" ,
	tipoins."Nombre" AS "NombreTipoInstrumento"  ,
	tran."IdClasificacionTipoInstrumento" ,
	"ClasificacionTipoInstrumento_$7"."Codigo" AS "CodigoClasificacionTipoInstrumento" ,
	"ClasificacionTipoInstrumento_$7"."Nombre" AS "NombreClasificacionTipoInstrumento" ,
	tran."ValorRescate"  as "ValorRescateTransaccion" ,
	trandisp."ValorRescateDisponible"  as "ValorRescateDisponibleTransaccion",
	tran."IdEstado"  as "IdEstadoTransaccion" ,
	est."Nombre" AS "EstadoTransaccion" ,
	tran."IdTransaccionPlazo",
	tran."IdTransaccionOd" ,
	tran."IdEmpleado" ,
	tran."IdTipoOperacion" ,
	tipooper."Nombre" AS "NombreTipoOperacion" ,
	tipooper."AccionOperacion" AS "AccionTipoOperacion" ,
	tipooper."AccionOperacion" AS "AccionOrden" ,
	tipooper."IdTipoProducto" AS "IdTipoProducto" ,
	tran."IdCondicionDeLiquidacion" ,
	condliq."Codigo" AS "CodigoCondicionDeLiquidacion" ,
	condliq."Nombre" AS "NombreCondicionDeLiquidacion" ,
	tran."FechaParidad" ,
	tran."ValorParidad" ,
	tran."FechaTipoCambio" ,
	tran."IdLibroOrdenes" ,
	"LibroOrden"."NombreLibroOrdenes" ,
	"LibroOrden"."IdTipoLibroOrden" ,
	tran."ValorTipoCambio" ,
	tran."IdCorredorContraparte" ,
	tran."IdTipoReajuste" ,
	tran."IdUsuarioReserva" ,
	tran."FechaIngresoOferta" ,
	tran."IdBolsa" ,
	bol."Nombre" AS "NombreBolsa"  ,
	CASE
	  WHEN tipoins."IdClasificacionTipoInstrumento" = 'IF' THEN trandisp."ValorRescateDisponible"
	ELSE trandisp."CantidadDisponible"
	END AS "CantidadDisponibleTransaccion",
	
	trandisp."CantidadAsignada"  as "CantidadAsignadaTransaccion" ,
	trandisp."ValorRescateAsignado"  as "ValorRescateAsignadoTransaccion",
	tran."NemotecnicoSVS",
	tran."MarcaNegociacion",
	tran."IndicadorDCV"
	
	--OPCIONES CL
	,ins."PrecioStrike"
	,ins."EsDivisible" AS "IdModalidadLiquidacion"
	,CASE WHEN ins."ModalidadLiquidacion" = 'P' THEN 'FISICA' WHEN ins."ModalidadLiquidacion" = 'C' THEN 'FINANCIERA' ELSE '' END AS "ModalidadLiquidacion"
	,ins."ModalidadEjercicio" AS "IdModalidadEjercicio"
	,CASE WHEN ins."ModalidadEjercicio" = 'A' THEN 'AUTOMATICA' WHEN ins."LargoPlazo" = 'M' THEN 'MANUAL' ELSE '' END AS "ModalidadEjercicio"
	,ins."ISIN" AS "IdTipoOpcion"
	,ins."EsPutEsCall" AS "TipoOpcion"
	,ins."EstiloOpcion" AS "IdEstiloOpcion"
	,CASE WHEN ins."EstiloOpcion" = 'A' THEN 'AMERICANA' WHEN ins."EstiloOpcion" = 'E' THEN 'EUROPEA' ELSE '' END AS "EstiloOpcion"
	,ins."FechaVencimiento" AS "FechaVencimientoOpciones"
	,ins."Tamano"
	,cor."NombreCompleto" as "NombreCorredorContraparte"
	,tran."MarcaAsignacion"
	
FROM "BSCL.Orden.BD::ORD.Transaccion" AS tran
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Bolsa" AS bol ON bol."IdBolsa" = tran."IdBolsa"
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS condliq ON condliq."IdCondicionDeLiquidacion" = tran."IdCondicionDeLiquidacion"
LEFT OUTER JOIN "BSCL.Orden.BD.Vistas::ConsultarDisponibleTransaccion" AS trandisp ON trandisp."IdTransaccion" = tran."IdTransaccion"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS ins ON ins."IdInstrumento" = tran."IdInstrumento"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS tipoins ON tipoins."IdTipoInstrumento" = ins."IdTipoInstrumento"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS "ClasificacionTipoInstrumento_$7" ON "ClasificacionTipoInstrumento_$7"."IdClasificacionTipoInstrumento" = tipoins."IdClasificacionTipoInstrumento"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS tipooper ON tipooper."IdTipoOperacion" = tran."IdTipoOperacion"
LEFT OUTER JOIN "BSCL.Orden.BD::ORD.LibroOrdenes" AS "LibroOrden" ON "LibroOrden"."IdLibroOrdenes" = tran."IdLibroOrdenes"
	
--SUBYACENTE
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS insasoc ON insasoc."IdInstrumento" = tran."IdInstrumentoSubyacente"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS tipoinsasoc ON tipoinsasoc."IdTipoInstrumento" = insasoc."IdTipoInstrumento"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS ctiasoc ON ctiasoc."IdClasificacionTipoInstrumento" = tipoinsasoc."IdClasificacionTipoInstrumento"
LEFT OUTER JOIN "BSCL.Orden.BD::ORD.EstadoOrden" AS estorden ON  estorden."IdEstado" = tran."IdEstado" AND estorden."NombreEntidad" = 'TRANSACCION'
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS est ON est."IdEstado" = estorden."IdEstado"
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" cor on cor."IdCorredora" = tran."IdCorredorContraparte"
--NEGOCIACION
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento_NEG" ON  "Instrumento_NEG"."IdInstrumento" = tran."IdInstrumentoNegociacion"

WHERE tran."IdEstado" = 'ING' AND tran."EsAPlazo" = 'N' AND (trandisp."CantidadDisponible" > 0 OR trandisp."ValorRescateDisponible" > 0); 
GO

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarTransaccionPorAsignar" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO