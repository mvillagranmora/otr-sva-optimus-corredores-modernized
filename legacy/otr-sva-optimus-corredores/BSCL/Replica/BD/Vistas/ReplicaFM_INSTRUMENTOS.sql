SET SCHEMA "BSCL"; 
go
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaFM_INSTRUMENTOS"; go

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaFM_INSTRUMENTOS" AS

SELECT
	WST."IdStack"  AS "IDSTACK" ,
	"Action" AS ACTION,
	A.Codigo_Ins   AS TABLE_KEY,
	a.*

FROM "BSCL.Replica.BD.Vistas::Sebra.FM_INSTRUMENTOS" A
LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'FM_INSTRUMENTOS' AND WST."Key" = A.Codigo_Ins
						
WITH READ ONLY;
go
GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaFM_INSTRUMENTOS" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

