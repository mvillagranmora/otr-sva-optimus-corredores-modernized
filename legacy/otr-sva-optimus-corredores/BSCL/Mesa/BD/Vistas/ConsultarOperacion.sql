SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarOperacion";

CREATE VIEW "BSCL.Mesa.BD.Vistas::ConsultarOperacion" AS

SELECT
	OPER."FechaOperacion" AS "FechaMovimiento",
	OPER."FolioOperacion" AS "FolioTransacion",
	''AS "SecuenciaOrden",
	'' AS "IdTipoComprobante",
	OPER."FolioOperacion" AS "FolioComprobante",
	OPER."IdOperacion" AS "IdOperacion",
	OPER."IdTipoOperacion" AS "IdTipoOperacion",
	0 AS "IdOperadorContraparte",
	0 AS "IdOperadorCorredor",
	OPER."IdCliente",
	OPER."IdPortafolio",
	
	INS."IdTipoInstrumento" AS "Mercado",
	
	INS."IdEmisor" AS "IdEmisor",
	INS."Nemotecnico" AS "Nemotecnico",
	'' AS "RelacionInstrumento", -- revisar
	CASE LEFT(OPER."IdTipoOperacion", 1) WHEN 'C' THEN RES."Tasa"
	ELSE 0 END AS "TasaCompra",
	RES."MontoValorizado" AS "ValorPresenteCompraInst",
	
	INS."FechaVencimiento" AS "FechaVencimiento",
	RES."IngCantidad" AS "Cantidad",
	RES."Precio" as "Precio",
	RES."Tasa" as "Tasa",
	RES."IdTipoReajuste" AS "IdMoneda",
	CASE LEFT(OPER."IdTipoOperacion", 1) WHEN 'C' THEN RES."ValorRescateInicial"
	ELSE 0 END AS "MontoCompra", 
	CASE LEFT(OPER."IdTipoOperacion", 1) WHEN 'V' THEN RES."ValorRescateInicial"
	ELSE 0 END AS "MontoVenta", 
	0 AS "Comision",
	0 AS "Derechos",
	0 AS "CargoFijos",
	0 AS "IVA",
	OPER."IdCondicionLiquidacion",
	0 AS "IdDocumentoRelacion",
	OPER."FechaVencimiento" AS "FechaVencimientoPacto",
	0 AS "PrecioPlazo",
	CASE WHEN OPER."IdTipoOperacion" IN ('COMPAC', 'VENPAC') THEN OPER."Tasa" 
	ELSE 0 END AS "TasaPlazo",
	CASE WHEN OPER."IdTipoOperacion" IN ('COMPAC', 'VENPAC') THEN OPER."IdTipoReajuste"
	ELSE '' END AS "IdMonedaPlazo",
	CASE LEFT(OPER."IdTipoOperacion", 1) WHEN 'C' THEN RES."MontoValorizado" 
	ELSE 0 END AS "TotalCompraVencimiento",
	CASE LEFT(OPER."IdTipoOperacion", 1) WHEN 'V' THEN RES."MontoValorizado" 
	ELSE 0 END AS  "TotalVentaVencimiento", 
	'C' as  "Custodia",
	'' AS "IdBoveda",
	0 AS "CantidadGarantia",
	'' AS "NemoSVS", -- IdIntrumento
	CASE LEFT(OPER."IdTipoOperacion", 1) WHEN 'V' THEN RES."Tasa"
	ELSE 0 END AS "TasaVenta", --HACER CASE
	RES."MontoValorizado" AS "ValorPresente",
	CASE LEFT(OPER."IdTipoOperacion", 1) WHEN 'C' THEN (RES."ValorRescateInicial" - RES."MontoValorizado") 
										 WHEN 'V' THEN (RES."MontoValorizado" - RES."ValorRescateInicial") 
										 ELSE 0 END AS "UtilidadPerdida",
	'' AS "FechaOrigenPacto",
	OPER."IdOperacion" AS "DescripcionOperacion",
	''  AS "IdEstadoFactura",
	'' AS "Canal",
	0 AS "ValorIVA",
	0 AS "FolioComprobanteAsociado",
	RES."IdInstrumento",
	OPER."IdTipoProducto",
	COALESCE(RES."IdClasificacionTipoInstrumento",'') AS "IdClasificacionTipoInstrumento",
	COALESCE(RES."IdTipoInstrumento",'') AS "IdTipoInstrumento"
FROM "BSCL.Mesa.BD::MES.Operacion" AS OPER
INNER JOIN "BSCL.Custodia.BD::CUS.Reserva" RES ON OPER."IdOperacion" = RES."IdOperacion"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON RES."IdInstrumento" = INS."IdInstrumento" 
WHERE OPER."IdEstado" = 'VIG'
AND OPER."IdTipoProducto" IN ('MERF', 'IF');

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::ConsultarOperacion" TO BCS_DESARROLLADOR WITH GRANT OPTION;