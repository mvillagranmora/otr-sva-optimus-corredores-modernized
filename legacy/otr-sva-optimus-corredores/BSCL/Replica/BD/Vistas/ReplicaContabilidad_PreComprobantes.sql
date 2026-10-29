SET SCHEMA "BSCL";

DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaContabilidad_PreComprobantes";

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaContabilidad_PreComprobantes"  AS
SELECT
	WST."IdStack" AS "IDSTACK",
	"IdPreComprobante" AS TABLE_KEY,
	"Action" AS ACTION,
	"IdPreComprobante" AS "IDPRECOMPROBANTE",
	"IdTipoContabilidad" AS "IDTIPOCONTABILIDAD",
	"Ejercicio" AS "EJERCICIO",
	"FechaContable" AS "FECHACONTABLE",
	"Glosa" AS "GLOSA",
	"FechaProceso" AS "FECHAPROCESO",
	"HoraProceso" AS "HORAPROCESO",
	"CodigoUsuario" AS "CODIGOUSUARIO",
	"Estado" AS "ESTADO"
FROM "_SYS_BIC"."BSCL.Contabilidad.BD.Analytics/PreComprobantes"
LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST 
ON  WST."Table" = 'Contabilidad_PreComprobantes' AND WST."Key" = "IdPreComprobante"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaContabilidad_PreComprobantes" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
