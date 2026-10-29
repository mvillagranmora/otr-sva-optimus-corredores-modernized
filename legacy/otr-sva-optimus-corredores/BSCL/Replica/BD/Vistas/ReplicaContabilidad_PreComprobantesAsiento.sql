SET SCHEMA "BSCL";

DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaContabilidad_PreComprobantesAsiento";

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaContabilidad_PreComprobantesAsiento"  AS
SELECT
	WST."IdStack" AS "IDSTACK",
	"IdPreComprobanteAsiento" AS TABLE_KEY,
	"Action" AS ACTION,
	"IdPreComprobanteAsiento" AS "IDPRECOMPROBANTEASIENTO",
	"IdPreComprobante" AS "IDPRECOMPROBANTE",
	"IdCuentaContable" AS "IDCUENTACONTABLE",
	"Ejercicio" AS "EJERCICIO",
	"Linea" AS "LINEA",
	"MontoDebe" AS "MONTODEBE",
	"MontoHaber" AS "MONTOHABER",
	"FechaContable" AS "FECHACONTABLE",
	"Glosa" AS "GLOSA",
	"Estado" AS "ESTADO"
FROM "_SYS_BIC"."BSCL.Contabilidad.BD.Analytics/PreComprobantesAsiento"
LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST 
ON  WST."Table" = 'Contabilidad_PreComprobantesAsiento' AND WST."Key" = "IdPreComprobanteAsiento"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaContabilidad_PreComprobantesAsiento" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
