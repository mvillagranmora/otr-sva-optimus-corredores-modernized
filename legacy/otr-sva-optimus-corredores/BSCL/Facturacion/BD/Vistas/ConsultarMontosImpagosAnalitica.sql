SET SCHEMA "BSCL";
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarMontosImpagosAnalitica";
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarMontosImpagosAnalitica"  AS
SELECT 
	"IdComprobante" AS "DealNumber",
	"DocumentoIdentidad" AS "Counterparty",
	"FechaLiquidacion" AS "SettlementDate",
	ABS("MontoComprobanteFinal") as "AmountCLP",
	CASE COALESCE("Indicador"."ValorIndicador",'N') 
		WHEN 'S' THEN 'Y' ELSE 'N' 
	END AS "Relacionado"
FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS "Comprobante"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" AS "Indicador" 
ON "Indicador"."IdCliente" = "Comprobante"."IdCliente" 
	AND "Indicador"."CodigoTipoIndicador" = 'RELCORR'
WITH READ ONLY;
GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarMontosImpagosAnalitica"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;