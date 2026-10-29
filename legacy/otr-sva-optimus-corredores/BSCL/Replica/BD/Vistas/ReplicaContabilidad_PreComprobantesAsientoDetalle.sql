SET SCHEMA "BSCL";

DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaContabilidad_PreComprobantesAsientoDetalle";


CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaContabilidad_PreComprobantesAsientoDetalle"  AS
SELECT
	WST."IdStack" AS "IDSTACK",
	"IdPreComprobante" || ':' || "IdPreComprobanteAsiento" || ':' || "IdEvento" AS TABLE_KEY,
	"Action" AS ACTION,
	"IdPreComprobante" AS "IDPRECOMPROBANTE",
	"IdPreComprobanteAsiento" AS "IDPRECOMPROBANTEASIENTO",
	"IdEvento" AS "IDEVENTO",
	"IdMovimiento" AS "IDMOVIMIENTO",
	"Operador" AS "OPERADOR",
	"Linea" AS "LINEA",
	"Estado" AS "ESTADO",
	"GlosaDetalle" AS "GLOSADETALLE",
	"IdTipoEvento" AS "IDTIPOEVENTO",
	"Monto" AS "MONTO",
	"Numero" AS "NUMERO"
FROM "_SYS_BIC"."BSCL.Contabilidad.BD.Analytics/PreComprobantesAsientoDetalle"
LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST 
ON  WST."Table" = 'Contabilidad_PreComprobantesAsientoDetalle' AND WST."Key" = ("IdPreComprobante" || ':' || "IdPreComprobanteAsiento" || ':' || "IdEvento")

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaContabilidad_PreComprobantesAsientoDetalle" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
