SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaBANCO"; 
-- SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaBANCO" 
CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaBANCO" AS

SELECT
	WST."IdStack" AS "IDSTACK", "Action" AS ACTION,cast(BAN."BAN_ID" AS varchar) AS TABLE_KEY,
	cast("BAN_ID" as varchar) as BAN_ID,
	"BAN_CODIGO",
	"BAN_NOMBRE",
	"BAN_EMISOR",
	"BAN_EQUIVALE",
	"BAN_PAG_RESCATE",
	"BAN_IND_INT",
	"BAN_IND_TIPO",
	"BAN_COD_SWIFT",
	"BAN_COD_SWIFTH",
	"BAN_COD_ABA",
	"BAN_COD_IBAN",
	"BAN_RUT_BANCO"
FROM "BSCL.Replica.BD.Vistas::Sebra.BANCO" BAN
LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'BANCO' AND WST."Key" = BAN."BAN_ID"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaBANCO" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
