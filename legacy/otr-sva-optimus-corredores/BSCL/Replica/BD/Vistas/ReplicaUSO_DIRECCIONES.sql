SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaUSO_DIRECCIONES"; 


-- SELECT  *  FROM "BSCL.Replica.BD.Vistas::ReplicaUSO_DIRECCIONES" where per_id = 31395515

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaUSO_DIRECCIONES" AS

	SELECT DISTINCT 
			WST."IdStack" AS "IDSTACK", "Action" AS ACTION,
			cast(PER."PER_ID" as varchar) || ':' || rtrim(ltrim(PER."CUENTA")) || ':'|| cast(PER."DIR_ID" as varchar) AS  TABLE_KEY,
			PER."PER_ID",
			PER."CUENTA",
			PER."FIL_ID",
			PER."DIR_ID",
			PER."UDI_FEC_INICIO",
			PER."UDI_PARTICULAR",
			PER."UDI_COMERCIAL",
			PER."UDI_LEGAL",
			PER."UDI_CORRESP",
			PER."UDI_MAIL_CORR",
			PER."UDI_MAIL_DTE"

		FROM  "BSCL.Replica.BD.Vistas::Sebra.USO_DIRECCIONES" PER
		LEFT JOIN  "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion"   AS WST
				 ON (WST."Table" ='USO_DIRECCIONES' AND cast(PER."PER_ID" as varchar) || ':' || rtrim(ltrim(PER."CUENTA")) || ':'|| cast(PER."DIR_ID" as varchar) = WST."Key")


WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaUSO_DIRECCIONES" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
