SET SCHEMA "BSCL";

DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaFacturacion_ConsultarMontosImpagos";

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaFacturacion_ConsultarMontosImpagos"  AS
SELECT
	WST."IdStack" AS "IDSTACK",
	"DealNumber" AS TABLE_KEY,
	"Action" AS ACTION,
	"DealNumber" AS "DEALNUMBER",
	"Counterparty" AS "COUNTERPARTY",
	"SettlementDate" AS "SETTLEMENTDATE",
	"AmountCLP" AS "AMOUNTCLP",
	"Relacionado" AS "RELACIONADO"
FROM "BSCL.Facturacion.BD.Vistas::ConsultarMontosImpagosAnalitica"
LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST 
ON  WST."Table" = 'Facturacion_ConsultarMontosImpagos' AND WST."Key" = "DealNumber"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaFacturacion_ConsultarMontosImpagos" TO  BCS_DESARROLLADOR WITH GRANT OPTION;