SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarAsignacionesParaDividendo";
GO
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarAsignacionesParaDividendo"
AS SELECT "Asignacion_$0"."IdAsignacion" 
, "Asignacion_$0"."IdTransaccion" 
, "Transaccion_$11"."IdTransaccionOd" 
, "Asignacion_$0"."IdOrden"
, "Asignacion_$0"."FechaAsignacion"
, "Asignacion_$0"."CantidadAsignada" 
, "Asignacion_$0"."MontoAsignado" 
, "Asignacion_$0"."MontoAsignadoLocal" 
, "Asignacion_$0"."MontoAsignadoLiquidacion" 
, "Asignacion_$0"."ValorRescateAsignado"
, "Asignacion_$0"."EsAPlazo"
, "Asignacion_$0"."IdAsignacionRef"
, "Asignacion_$0"."IdMovimientoComprobante"
, "Asignacion_$0"."IdEstadoAsignacion" AS "IdEstadoAsignacion" 
, "Instrumento_$6"."IdMoneda" AS "IdMonedaInstrumento" 
, "Transaccion_$11"."IdMonedaSubyacente" AS "IdMonedaInstrumentoSubyacente"
, "Transaccion_$11"."IdMoneda" AS "IdMonedaOperacion" 
, "Transaccion_$11"."IdTipoReajuste" AS "IdTipoReajuste" 
, "Orden_$3"."IdMonedaLiquidacionOrden" AS "IdMonedaLiquidacionOrden" 
, "Orden_$3"."IdFormaCustodia" AS "IdFormaCustodia"
, "Orden_$3"."IdEmpresa" AS "IdEmpresa"
, "Transaccion_$11"."IndicadorOrdenDirecta" 
, "Transaccion_$11"."IdEmisor" AS "IdEmisor"
, "Transaccion_$11"."FechaEmision" AS "FechaEmision"
, "Transaccion_$11"."EsCuentaPropia" AS "EsCuentaPropia"
, "Transaccion_$11"."EsFungible" AS "EsFungible"

, "Asignacion_$0"."FechaParidad" AS "FechaParidad"
, "Asignacion_$0"."Paridad" AS "ValorParidad"
, "Asignacion_$0"."FechaTipoCambio" AS "FechaTipoCambio"
, "Asignacion_$0"."TipoCambio" AS "ValorTipoCambio"
, "Transaccion_$11"."EsVentaCorta" AS "EsVentaCorta"
, "Transaccion_$11"."Precio" AS "PrecioAsignacion" 
, CASE "Transaccion_$11"."EsVentaCorta" WHEN 'S' THEN "Orden_$3"."TasaVentaCorta" ELSE "Transaccion_$11"."Tasa" END AS "Tasa"
, CASE WHEN "Transaccion_$11"."EsVentaCorta" = 'S' THEN "Orden_$3"."PlazoVentaCorta" WHEN "Transaccion_$11"."IdTipoInstrumento" = 'SIM' THEN "Transaccion_$11"."Plazo" ELSE 0 END AS "Plazo"      
, "Transaccion_$11"."IdCondicionDeLiquidacion" AS "IdCondicionDeLiquidacion" 
, "CondicionDeLiquidacion_$12"."Codigo" AS "CodigoCondicionDeLiquidacion" 
, "CondicionDeLiquidacion_$12"."Nombre" AS "NombreCondicionDeLiquidacion" 
, "Transaccion_$11"."FechaLiquidacion" AS "FechaLiquidacion" 
, "Transaccion_$11"."Folio" AS "FolioTransaccion" 
, "Transaccion_$11"."FechaOperacion" AS "FechaOperacionTransaccion" 
, "Transaccion_$11"."IdCorredorContraparte" 
, "Orden_$3"."IdLibroOrdenes" AS "IdLibroOrdenes"
, "Orden_$3"."Glosa" AS "GlosaOrden"
, "Orden_$3"."Folio" AS "FolioOrden"
, CASE 	WHEN "Transaccion_$11"."EsVentaCorta" = 'S' THEN  "Orden_$3"."FechaVencimiento"
		ELSE "Transaccion_$11"."FechaVencimiento" END AS "FechaVencimiento" 
, "TipoOperacion_$9"."IdTipoOperacion" AS "IdTipoOperacion" 
, "TipoOperacion_$9"."AccionOperacion" AS "AccionTipoOperacion" 
, "TipoOperacion_$9"."Nombre" AS "NombreTipoOperacion" 
, "Orden_$3"."IdTipoCanalIngreso" AS "IdTipoCanalIngreso" 
, "TipoProducto_$10"."IdTipoProducto" AS "IdTipoProducto" 
, "Transaccion_$11"."IdInstrumento" AS "IdInstrumento"
--, "Instrumento_$6"."ValorClasificacionRiesgo" AS "ValorClasificacionRiesgo" 
, "Transaccion_$11"."IdInstrumentoSubyacente" AS "IdInstrumentoSubyacente"
, "Instrumento_ASOC"."Nemotecnico" AS "InstrumentoSubyacente"  
, "TipoInstrumento_ASOC"."IdTipoInstrumento" AS "IdTipoInstrumentoSubyacente" 
, "TipoInstrumento_ASOC"."Nombre" AS "NombreTipoInstrumentoSubyacente"
, "TipoInstrumento_ASOC"."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumentoSubyacente"
, "ClasificacionTipoInstrumento_ASOC"."Nombre" AS "NombreClasificacionTipoInstrumentoSubyacente"
, "ConsultarPortafolio_$0"."CodigoPortafolio" 
, "Transaccion_$11"."IdBolsa" AS "IdBolsa" 
, "Orden_$3"."FechaOperacion" AS "FechaOperacionOrden" 
, "Instrumento_$6"."Nemotecnico" AS "Instrumento" 
, "TipoInstrumento_$7"."IdTipoInstrumento" AS "IdTipoInstrumento" 
, "TipoInstrumento_$7"."Nombre" AS "NombreTipoInstrumento" 
, "ClasificacionTipoInstrumento_$8"."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento" 
, "ClasificacionTipoInstrumento_$8"."Nombre" AS "NombreClasificacionTipoInstrumento" 
, "Orden_$3"."IdCliente" AS "IdCliente" 
, "ConsultarCliente_$5"."DocumentoIdentidad" AS "DocumentoIdentidad" 
, "ConsultarCliente_$5"."RazonSocial" AS "RazonSocial" 
, "ConsultarCliente_$5"."NombreCompleto" AS "NombreCompleto" 
, "Orden_$3"."IdPortafolioCliente" AS "IdPortafolio"

FROM  "BSCL.Orden.BD::ORD.Asignacion" AS "Asignacion_$0"

LEFT OUTER JOIN "BSCL.Orden.BD::ORD.Orden" AS "Orden_$3" ON  ( "Orden_$3"."IdOrden" = "Asignacion_$0"."IdOrden" )  
LEFT OUTER JOIN "BSCL.Orden.BD::ORD.Transaccion" AS "Transaccion_$11" ON  ( "Transaccion_$11"."IdTransaccion" = "Asignacion_$0"."IdTransaccion" ) 
LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS "ConsultarPortafolio_$0" ON  ( "Orden_$3"."IdPortafolioCliente" = "ConsultarPortafolio_$0"."IdPortafolio" )  
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "ConsultarCliente_$5" ON  ( "ConsultarCliente_$5"."IdCliente" = "ConsultarPortafolio_$0"."IdCliente" )  

LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento_$6" ON  ( "Instrumento_$6"."IdInstrumento" = "Transaccion_$11"."IdInstrumento" )  
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS "TipoInstrumento_$7" ON  ( "TipoInstrumento_$7"."IdTipoInstrumento" = "Instrumento_$6"."IdTipoInstrumento" )  
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS "ClasificacionTipoInstrumento_$8" ON  ( "ClasificacionTipoInstrumento_$8"."IdClasificacionTipoInstrumento" = "TipoInstrumento_$7"."IdClasificacionTipoInstrumento" )  

LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento_ASOC" ON  ( "Instrumento_ASOC"."IdInstrumento" = "Transaccion_$11"."IdInstrumentoSubyacente" )  
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS "TipoInstrumento_ASOC" ON  ( "TipoInstrumento_ASOC"."IdTipoInstrumento" = "Instrumento_ASOC"."IdTipoInstrumento" )  
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS "ClasificacionTipoInstrumento_ASOC" ON  ( "ClasificacionTipoInstrumento_ASOC"."IdClasificacionTipoInstrumento" = "TipoInstrumento_ASOC"."IdClasificacionTipoInstrumento" )  

LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS "TipoOperacion_$9" ON  ( "TipoOperacion_$9"."IdTipoOperacion" = "Transaccion_$11"."IdTipoOperacion" )  
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoProducto" AS "TipoProducto_$10" ON  ( "TipoProducto_$10"."IdTipoProducto" = "TipoOperacion_$9"."IdTipoProducto" )  
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS "CondicionDeLiquidacion_$12" ON  ( "CondicionDeLiquidacion_$12"."IdCondicionDeLiquidacion" = "Transaccion_$11"."IdCondicionDeLiquidacion" )
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" conf on conf."IdEmpresa" = "Transaccion_$11"."IdEmpresa" AND conf."CodigoParametro" = 'PLA.FechaSistema'
WHERE "ClasificacionTipoInstrumento_$8"."IdClasificacionTipoInstrumento" = 'RV'
AND "Transaccion_$11"."IndicadorOrdenDirecta" = '' AND "Transaccion_$11"."FechaLiquidacion" > TO_DATE(conf."Valor");

GO
GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarAsignacionesParaDividendo" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO