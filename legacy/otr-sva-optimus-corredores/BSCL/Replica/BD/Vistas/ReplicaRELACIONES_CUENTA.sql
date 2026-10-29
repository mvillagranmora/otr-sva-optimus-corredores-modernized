SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaRELACIONES_CUENTA"; 

--select  *  from  "BSCL.Replica.BD.Vistas::ReplicaRELACIONES_CUENTA" WHERE PER_ID = 16660955

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaRELACIONES_CUENTA" AS

	SELECT 
		WST."IdStack" AS IdStack ,"Action" AS ACTION,
		cast(PER_ID as varchar)  || ':' ||   cast(PER_ID_REL as varchar)  || ':' ||  PAR_TRE_ID || ':' || "CUENTA"  AS TABLE_KEY,	
		AUT."PER_ID",
		AUT."CUENTA",
		AUT."FIL_ID",
		AUT."PER_ID_REL",
		AUT."PAR_TRE_ID",
		TO_NVARCHAR(AUT."REL_FEC_INICIO",'YYYY-MM-DD HH24:MI:SS')AS REL_FEC_INICIO

		FROM  "BSCL.Replica.BD.Vistas::Sebra.RELACIONES_CUENTA" AUT
		LEFT  JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON 
		(WST."Table" = 'RELACIONES_CUENTA' AND WST."Key" = cast(PER_ID as varchar)  || ':' ||   cast(PER_ID_REL as varchar)  || ':' ||  PAR_TRE_ID || ':' || "CUENTA" ) 

		
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaRELACIONES_CUENTA" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
