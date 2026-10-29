SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaINDICADORES_CUENTA"; 
CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaINDICADORES_CUENTA" AS

	SELECT 
			"IdStack"  AS "IDSTACK" , "Action" AS ACTION,
			(PER_ID || CUENTA || IND_ID) AS TABLE_KEY,
			"PER_ID", -- TIPO :int
			"CUENTA", -- TIPO :int
			"FIL_ID", -- TIPO :int
			"IND_ID", -- TIPO :int
			"DIN_ID", -- TIPO :char
			"INC_FEC_INICIO" -- TIPO :datetime
		FROM "BSCL.Replica.BD.Vistas::Sebra.INDICADORES_CUENTA" i
		left  JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion"   AS s	ON (s."Table" ='INDICADORES_CUENTA' AND (PER_ID || CUENTA || IND_ID) = s."Key") 

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaINDICADORES_CUENTA" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

