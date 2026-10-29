SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaFILIAL"; 

-- select * from "BSCL.Replica.BD.Vistas::ReplicaFILIAL"

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaFILIAL" AS

        Select WST."IdStack" AS "IDSTACK", "Action" AS ACTION,
        	COALESCE ( CAST(
							CASE WHEN instr(EMP."DocumentoIdentidad" , '-') = 0 THEN -1
								 ELSE trim(LEFT(EMP."DocumentoIdentidad"  , instr(EMP."DocumentoIdentidad" , '-')-1)) END AS INT) , 0 )  AS TABLE_KEY,
            COALESCE ( 0 , 0 )  AS "FIL_ID", -- INT
            COALESCE ( CAST(
							CASE WHEN instr(EMP."DocumentoIdentidad" , '-') = 0 THEN -1
								 ELSE trim(LEFT(EMP."DocumentoIdentidad"  , instr(EMP."DocumentoIdentidad" , '-')-1)) END AS INT) , 0 )  AS "PER_ID", -- INT
            COALESCE (CAST(EMP."RazonSocial" AS VARCHAR(70)),'')  AS "FIL_ALIAS", -- VARCHAR(70)
            COALESCE (CAST('S' AS VARCHAR(1)),'')  AS "FIL_MES_MONE", -- CHAR(1)
            COALESCE (CAST('S' AS VARCHAR(1)),'')  AS "FIL_MES_PESO" -- CHAR(1)
        FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP
        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'FILIAL' AND WST."Key" = COALESCE ( CAST(
							CASE WHEN instr(EMP."DocumentoIdentidad" , '-') = 0 THEN -1
								 ELSE trim(LEFT(EMP."DocumentoIdentidad"  , instr(EMP."DocumentoIdentidad" , '-')-1)) END AS INT) , 0 )
		WHERE EMP."IdEstado" = 'ACT'

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaFILIAL" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
