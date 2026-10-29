SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarOrdenAsociada"; 
 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarOrdenAsociada" 
AS SELECT 
	ORDEN_ASOCIADA."IdOrdenAsociada",
	REPORTADO."IdOrden" AS "IdOrdenReportado",
	REPORTANTE."IdOrden" AS "IdOrdenReportante",
	REPORTADO."MontoOrden" AS "MontoOrdenReportado",
	REPORTANTE."MontoOrden" AS "MontoOrdenReportante",
	REPORTANTE."MontoLiquidacion" AS "MontoLiquidacion",
	REPORTANTE."MontoOrdenPlazo" AS "MontoOrdenPlazo",
	REPORTANTE."MontoLiquidacionPlazo" AS "MontoLiquidacionPlazo",
	CLIENTE_REPORTADO."NombreCompleto" AS "NombreCompletoReportado",
	PORTAFOLIO_REPORTADO."CodigoPortafolio" AS "CodigoPortafolioReportado",
	REPORTADO."IdMonedaOrden" AS "IdMonedaOrdenReportado",
	REPORTADO."IdPortafolioCliente" AS "IdPortafolioReportado",
	CLIENTE_REPORTANTE."NombreCompleto" AS "NombreCompletoReportante",
	PORTAFOLIO_REPORTANTE."CodigoPortafolio" AS "CodigoPortafolioReportante",
	REPORTANTE."IdPortafolioCliente" AS "IdPortafolioReportante",
	REPORTANTE."IdMonedaOrden" AS "IdMonedaOrdenReportante",
	REPORTADO."TasaReporte"   AS "TasaReportado",
	REPORTANTE."TasaReporte"  AS "TasaReportante",
	ORDEN_ASOCIADA."FechaIngresoRegistroCorta" AS "FechaRegistro",
	REPORTADO."IdEstadoOrden" AS "IdEstadoOrdenReportado",
	REPORTANTE."IdEstadoOrden" AS "IdEstadoOrdenReportante",
	ORDEN_ASOCIADA."Monto" AS "MontoAsociado",
	ORDEN_ASOCIADA."Cantidad" AS "CantidadAsociada",
	REPORTADO."IdTipoInstrumento",
	REPORTANTE."IdInstrumento" AS "IdInstrumentoReportante",
	REPORTANTE."PlazoReporte" AS "PlazoReportante",
	REPORTADO."PrecioOrden" AS "PrecioOrden",
	REPORTADO."TasaPromedioReporte",
	REPORTANTE."IdMonedaLiquidacionOrden" AS "IdMonedaLiquidacionOrdenReportante",
	REPORTANTE."IdTipoOperacion" AS "IdTipoOperacionReportante",
	REPORTANTE."IdTipoCanalIngreso" AS "IdTipoCanalIngresoReportante",
	REPORTADO."IdBolsa",
	REPORTANTE."FechaOperacion" AS "FechaOperacionReportante",
	TIPO_OPERACION."IdTipoProducto",
	REPORTADO."IdGrupoCobro" AS "IdGrupoCobroReportado",
	REPORTANTE."IdGrupoCobro" AS "IdGrupoCobroReportante",
	REPORTADO."BaseTasa" AS "BaseReporteReportado",
	REPORTANTE."BaseTasa" AS "BaseReporteReportante",
	ORDEN_ASOCIADA."IdOrdenAsociada" || ' - ' || CLIENTE_REPORTANTE."NombreCompleto" AS "IdOrdenAsociadaNombreReportanteUI",
	ORDEN_ASOCIADA."IdOrdenAsociada" || ' - ' || CLIENTE_REPORTADO."NombreCompleto" AS "IdOrdenAsociadaNombreReportadoUI",
	REPORTADO."EsConfirmada",
	REPORTADO."IdInstrumentoSubyacente"
FROM "BSCL.Orden.BD::ORD.OrdenAsociada" ORDEN_ASOCIADA
INNER JOIN "BSCL.Orden.BD::ORD.Orden" REPORTADO ON (REPORTADO."IdOrden" = ORDEN_ASOCIADA."IdOrdenN")
INNER JOIN "BSCL.Orden.BD::ORD.Orden" REPORTANTE ON (REPORTANTE."IdOrden" = ORDEN_ASOCIADA."IdOrdenM")
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS PORTAFOLIO_REPORTADO ON REPORTADO."IdPortafolioCliente" = PORTAFOLIO_REPORTADO."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLIENTE_REPORTADO ON CLIENTE_REPORTADO."IdCliente" = PORTAFOLIO_REPORTADO."IdCliente"
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS PORTAFOLIO_REPORTANTE ON REPORTANTE."IdPortafolioCliente" = PORTAFOLIO_REPORTANTE."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLIENTE_REPORTANTE ON CLIENTE_REPORTANTE."IdCliente" = PORTAFOLIO_REPORTANTE."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPO_OPERACION ON  TIPO_OPERACION."IdTipoOperacion" = REPORTADO."IdTipoOperacion";
GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarOrdenAsociada" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
