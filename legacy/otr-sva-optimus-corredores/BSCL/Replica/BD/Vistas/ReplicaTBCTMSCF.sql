SET SCHEMA "BSCL";
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTMSCF"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTMSCF" AS

SELECT 
	WST."IdStack" AS "IDSTACK", "Action" AS ACTION,
	TIPO_COMP_FAC || cast(FOLIO_COMP_FAC as nvarchar) AS TABLE_KEY,
	*
FROM "BSCL.Replica.BD.Vistas::Sebra.TBCTMSCF" COM
LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'TBCTMSCF' AND WST."Key" = TIPO_COMP_FAC || cast(FOLIO_COMP_FAC as nvarchar)


WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTBCTMSCF" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
