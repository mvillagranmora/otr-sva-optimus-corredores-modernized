SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTELEFONO"; 
--SELECT * FROM BSCL."BSCL.Replica.BD.Vistas::ReplicaTELEFONO"
CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTELEFONO" AS

	SELECT
		WST."IdStack" AS "IDSTACK","Action" AS ACTION,
		CAST(TEL."TEL_ID" AS varchar) AS TABLE_KEY,
		TEL.*
		
	FROM  "BSCL.Replica.BD.Vistas::Sebra.TELEFONO" TEL
	LEFT  JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'TELEFONO' AND WST."Key" = TEL."TEL_ID"
	

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTELEFONO" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
