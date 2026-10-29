SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarOrdenSinAsignar"; 
 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarOrdenSinAsignar" 
AS SELECT 
"Orden_$0"."IdOrden"
, "Orden_$0"."FechaOperacion"
, "Orden_$0"."FechaLiquidacion" 
, "Orden_$0"."IdCondicionDeLiquidacion" 
, "Orden_$0"."Folio" as "FolioOrden" 
, "Orden_$0"."FechaVencimiento"
, "Orden_$0"."FechaVigencia"
, "Orden_$0"."IdTipoVigenciaOrden"
, "Orden_$0"."IdEstadoOrden" 
, "Orden_$0"."Cantidad"
, "Orden_$0"."MontoOrden" 
, "Orden_$0"."ValorRescate" 
, "Orden_$0"."IdTipoReajuste" 
, "Orden_$0"."PrecioOrden" 
, "Orden_$0"."PrecioMercado" 
, "Orden_$0"."EsVentaCorta"
, "Orden_$0"."ValorCaptacion" 
, "Orden_$0"."IdTipoOperacion" 
, "Orden_$0"."IdGrupoCobro"
, "Orden_$0"."MontoLiquidacion"
, "TipoOperacion_$12"."IdTipoProducto" 
, "Orden_$0"."IdCliente" 
, "Orden_$0"."IdPortafolioCliente" as "IdPortafolio"
, "Orden_$0"."IdTipoCanalIngreso"
, "Orden_$0"."IdEmisor" 
, "Orden_$0"."IdEmpresa"
, "Orden_$0"."IdBolsa" 
, "Orden_$0"."IdInstrumento"
, "Instrumento_$8"."Nemotecnico"
, "TipoInstrumento_$9"."IdTipoInstrumento"
, "TipoInstrumento_$9"."IdClasificacionTipoInstrumento"

, "Orden_$0"."IdInstrumentoSubyacente" as "IdInstrumentoSubyacente"
, "Instrumento_ASOC"."Nemotecnico" AS "NemotecnicoSubyacente"
, "Instrumento_ASOC"."IdEmisor" AS "IdEmisorSubyacente"
, "Instrumento_ASOC"."IdTipoReajuste" AS "IdTipoReajusteSubyacente"
, "Instrumento_ASOC"."FechaVencimiento" AS "FechaVencimientoSubyacente"
, "Instrumento_ASOC"."FechaEmision" AS "FechaEmisionSubyacente"
, "Instrumento_ASOC"."IdMoneda" AS "IdMonedaSubyacente"
, "TipoInstrumento_ASOC"."IdTipoInstrumento" AS "IdTipoInstrumentoSubyacente"
, "TipoInstrumento_ASOC"."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumentoSubyacente"

, "Orden_$0"."OrdenVigente"

, estAsig."IdEstadoAsignacion"

, "Instrumento_$8"."Tamano"
, "Instrumento_$8"."IdMoneda" AS "IdMonedaInstrumento" 
, "Instrumento_$8"."FechaEmision" as "FechaEmisionInstrumento"
, "Instrumento_$8"."FechaVencimiento" as "FechaVencimientoInstrumento"
, "Orden_$0"."IdMonedaOrden" AS "IdMonedaOrden"
, "Orden_$0"."IdMonedaLiquidacionOrden" AS "IdMonedaLiquidacionOrden"
 
, "TipoOperacion_$12"."Accion" AS "AccionTipoOperacion" 
, "ConsultarDisponibleOrden_$4"."CantidadDisponible" AS "CantidadDisponibleOrden" 
, "ConsultarDisponibleOrden_$4"."CantidadAsignada" AS "CantidadAsignadaOrden"
, "ConsultarDisponibleOrden_$4"."MontoDisponible" AS "MontoDisponibleOrden" 
, "ConsultarDisponibleOrden_$4"."MontoAsignado" AS "MontoAsignadoOrden"

, "Orden_$0"."IdEmpleadoPortafolio" as "IdAgente"

FROM "BSCL.Orden.BD::ORD.Orden" AS "Orden_$0"
LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarEstadoAsignacionOrden" as estAsig on estAsig."IdOrden" = "Orden_$0"."IdOrden"
LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarDisponibleOrden" AS "ConsultarDisponibleOrden_$4" ON  ( "ConsultarDisponibleOrden_$4"."IdOrden" = "Orden_$0"."IdOrden" )  

LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento_$8" ON  ( "Instrumento_$8"."IdInstrumento" = "Orden_$0"."IdInstrumento" )
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS "TipoInstrumento_$9" ON  ( "TipoInstrumento_$9"."IdTipoInstrumento" = "Instrumento_$8"."IdTipoInstrumento" ) 
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS "ClasificacionTipoInstrumento_$10" ON  ( "ClasificacionTipoInstrumento_$10"."IdClasificacionTipoInstrumento" = "TipoInstrumento_$9"."IdClasificacionTipoInstrumento" )  

--SUBYACENTE
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento_ASOC" ON  "Instrumento_ASOC"."IdInstrumento" = "Orden_$0"."IdInstrumentoSubyacente"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS "TipoInstrumento_ASOC" ON "TipoInstrumento_ASOC"."IdTipoInstrumento" = "Instrumento_ASOC"."IdTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS "ClasificacionTipoInstrumento_ASOC" ON "ClasificacionTipoInstrumento_ASOC"."IdClasificacionTipoInstrumento" = "TipoInstrumento_ASOC"."IdClasificacionTipoInstrumento"

LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS "TipoOperacion_$12" ON  ( "TipoOperacion_$12"."IdTipoOperacion" = "Orden_$0"."IdTipoOperacion" )

WHERE "ConsultarDisponibleOrden_$4"."CantidadAsignada" = 0 and "Orden_$0"."OrdenVigente" = 'S' and "Orden_$0"."IdEstadoOrden" = 'ING';

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarOrdenSinAsignar" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 