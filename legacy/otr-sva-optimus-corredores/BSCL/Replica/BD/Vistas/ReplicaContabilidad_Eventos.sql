SET SCHEMA "BSCL";

DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaContabilidad_Eventos";

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaContabilidad_Eventos"  AS
SELECT
	WST."IdStack" AS "IDSTACK",
	"IdMovimiento" || ':' || "IdTipoDatoEvento" AS TABLE_KEY,
	"Action" AS ACTION,
	"IdMovimiento" AS "IDMOVIMIENTO",
	"IdTipoDatoEvento" AS "IDTIPODATOEVENTO",
	"TipoEvento" AS "TIPOEVENTO",
	"ValorVarChar" AS "VALORVARCHAR",
	"ValorDecimal" AS "VALORDECIMAL",
	"Descripcion" AS "DESCRIPCION",
	"Estado" AS "ESTADO",
	"NombreEstado" AS "NOMBREESTADO"
FROM "_SYS_BIC"."BSCL.Contabilidad.BD.Analytics/Eventos"
LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST 
ON  WST."Table" = 'Contabilidad_Eventos' AND WST."Key" = ("IdMovimiento" || ':' || "IdTipoDatoEvento")

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaContabilidad_Eventos" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
