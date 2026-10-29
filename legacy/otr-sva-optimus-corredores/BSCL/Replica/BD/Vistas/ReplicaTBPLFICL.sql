SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTBPLFICL"; 


-- select *  from  "BSCL.Replica.BD.Vistas::ReplicaTBPLFICL"

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTBPLFICL" AS

SELECT
	WST."IdStack" AS "IDSTACK","Action" AS ACTION,
	trim(coalesce ( POR."RUT_CLI" ,'')) || ':' || trim(coalesce (POR."SEC_RUT_CLI",'-1')) AS TABLE_KEY,
	POR.*

FROM  "BSCL.Replica.BD.Vistas::Sebra.TBPLFICL" POR
LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'TBPLFICL' AND WST."Key" = trim(coalesce ( POR."RUT_CLI" ,'')) || ':' || trim(coalesce (POR."SEC_RUT_CLI",'-1'))


WITH READ ONLY; 

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTBPLFICL" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
