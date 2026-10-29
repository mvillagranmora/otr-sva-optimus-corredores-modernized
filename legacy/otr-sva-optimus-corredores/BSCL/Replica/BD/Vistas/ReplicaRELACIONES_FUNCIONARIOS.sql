SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaRELACIONES_FUNCIONARIOS"; 
-- SELECT *  FROM "BSCL.Replica.BD.Vistas::ReplicaRELACIONES_FUNCIONARIOS" 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaRELACIONES_FUNCIONARIOS" AS

        Select WST."IdStack" AS "IDSTACK","Action" AS ACTION,
        	EMP."EMP_CODIGO" || ':' ||  EMP."EMP_CODIGO_REL"  || ':' || 'TRFUNSUPER' AS TABLE_KEY,
			EMP.*
           
		FROM  "BSCL.Replica.BD.Vistas::Sebra.RELACIONES_FUNCIONARIOS" EMP
       	LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'RELACIONES_FUNCIONARIOS' 
       		AND WST."Key" = EMP."EMP_CODIGO" || ':' ||  EMP."EMP_CODIGO_REL"  || ':' || 'TRFUNSUPER'

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaRELACIONES_FUNCIONARIOS" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                       
