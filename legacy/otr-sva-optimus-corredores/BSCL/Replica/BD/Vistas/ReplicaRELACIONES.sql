SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaRELACIONES"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaRELACIONES" AS

	SELECT  
		cast(per_id_per as varchar)  || ':' ||   cast(per_id_rel as varchar)  || ':' ||  par_tre_id as "TABLE_KEY",		
		"IdStack" as "IDSTACK", "Action" AS ACTION,
		"PER_ID_PER", 
		"PER_ID_REL", 
		"PAR_TRE_ID", 
			
		"REL_FEC_INICIO" AS "REL_FEC_INICIO", 
		"REL_FEC_TERMINO" AS "REL_FEC_TERMINO",
		"REL_VIGENCIA" AS "REL_VIGENCIA",
		'DESCONOCID' AS "REL_FATCA", 
		'0' AS "REL_IND_BENEF", 
		'' AS "REL_RUT_CONYUGE",
		'' AS "REL_NOMBRE_CONYUGE", 
		'' AS "REL_PATERNO_CONYUGE",
		'' AS "REL_MATERNO_CONYUGE",
		'' AS "TIPO_REPRESENTANTE", 
		CURRENT_TIMESTAMP AS "REL_FEC_MODIFICA"
		
	FROM "BSCL.Replica.BD.Vistas::Sebra.RELACIONES" AS TB2
	left JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST
		ON  (WST."Table" = 'RELACIONES' AND WST."Key" = cast(per_id_per as varchar)  || ':' ||   cast(per_id_rel as varchar)  || ':' ||  par_tre_id ) 
	
		WHERE "PER_ID_PER" > 0 AND 
			"PER_ID_REL" > 0 
			
			

WITH READ ONLY;
		
GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaRELACIONES" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
 



