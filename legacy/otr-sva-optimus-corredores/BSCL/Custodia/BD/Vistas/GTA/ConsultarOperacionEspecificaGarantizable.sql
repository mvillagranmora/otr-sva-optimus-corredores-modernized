SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::GTA.ConsultarOperacionEspecificaGarantizable";
GO
CREATE VIEW "BSCL.Custodia.BD.Vistas::GTA.ConsultarOperacionEspecificaGarantizable"
AS
SELECT	'CARTERA' AS "OrigenOperacion"
		,CAR."FolioComprobante" AS  "IdOperacion" -- CAR."IdOperacion" 
		,CAR."IdCartera"
		,CAR."FolioCartera"
		,CAR."IdCliente"
		,CLI."NombreCompleto"
		,CLI."DocumentoIdentidad"
		,CAR."IdPortafolio"
		,POR."CodigoPortafolio"
		,POR."DescripcionPortafolio"
		,POR."Proposito"
		,CAR."IdConcepto"
		,CPT."Nombre" AS "NombreConcepto"
		,CAR."IdTipoInstrumento"
		,CAR."IdInstrumento"
		,CAR."IdTipoOperacion"	
		,OPE."Nombre" AS "NombreTipoOperacion"
		,CAR."INGFechaOperacion"
		,CAR."INGFechaLiquidacion"
		,CAR."IdMonedaSistema" as "IdMoneda"
		,CAR."INGCantidad"
		,CAR."INGValorRescate"
		,CAR."INGMontoATasaCompraActual"
		,CAR."INGMontoATasaMercadoActual"
		,CAR."IdBolsa"
		,CAR."IdEmpresa" --, OPE.*
		,CAR."IdTipoPrestamo"
	FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
	INNER JOIN "BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia" as CMC ON CMC."IdTipoOperacion" = CAR."IdTipoOperacion" AND CMC."PoseeInstrumentoSubyacente" 	= 'S'
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS OPE ON OPE."IdTipoOperacion" = CAR."IdTipoOperacion"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS CPT ON CPT."IdConcepto" = CAR."IdConcepto" 
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI ON CLI."IdCliente" = CAR."IdCliente"
	LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS POR ON POR."IdPortafolio" = CAR."IdPortafolio"
	WHERE CAR."IdEstado" = 'VIG'
	UNION
	SELECT
		'ORDEN' AS "OrigenOperacion"
		, ORD."IdOrden"  AS "IdOperacion"
		,ORD."IdOrden" AS "IdCartera"
		,ORD."Folio"   AS "FolioCartera"
		,ORD."IdCliente"
		,CLI."NombreCompleto"
		,CLI."DocumentoIdentidad"
		,ORD."IdPortafolioCliente"
		,POR."CodigoPortafolio"
		,POR."DescripcionPortafolio"
		,POR."Proposito"
		,CPTORD."IdConcepto"
		,CPT."Nombre" AS "NombreConcepto"
		,ORD."IdTipoInstrumento"
		,ORD."IdInstrumento"
		,ORD."IdTipoOperacion"	
		,OPE."Nombre" AS "NombreTipoOperacion"
		,ORD."FechaOperacion"
		,ORD."FechaLiquidacion"
		,ORD."IdMonedaOrden" as "IdMoneda"
		,ORD."Cantidad" AS "INGCantidad"
		,ORD."ValorRescate" AS "INGValorRescate"
		,ORD."MontoOrden" AS "INGMontoATasaCompraActual"
		,ORD."MontoOrden" AS "INGMontoATasaMercadoActual"
		,ORD."IdBolsa"
		,ORD."IdEmpresa" --, OPE.*
		,'' AS "IdTipoPrestamo"
	FROM "BSCL.Orden.BD::ORD.Orden" ORD
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS OPE ON OPE."IdTipoOperacion" = ORD."IdTipoOperacion"
	LEFT JOIN "BSCL.Orden.BD::ORD.ConceptoOrden" CPTORD ON CPTORD."IdTipoOperacion" = ORD."IdTipoOperacion" AND "Uso" = 'INGRESOORDEN'
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS CPT ON CPT."IdConcepto" = CPTORD."IdConcepto" 
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI ON CLI."IdCliente" = ORD."IdCliente"
	LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS POR ON POR."IdPortafolio" = ORD."IdPortafolioCliente"
	WHERE ORD."IdEstadoOrden" IN ('ING','OFE') AND ORD."IdTipoOperacion" = 'VENRRV'

WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::GTA.ConsultarOperacionEspecificaGarantizable" TO BCS_DESARROLLADOR WITH GRANT OPTION;
GO