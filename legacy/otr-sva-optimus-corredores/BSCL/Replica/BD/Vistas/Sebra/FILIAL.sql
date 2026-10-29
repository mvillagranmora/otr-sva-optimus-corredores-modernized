SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.FILIAL"; 

-- select * from "BSCL.Replica.BD.Vistas::FILIAL"

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.FILIAL" AS
        Select 
            "IdEmpresa"  AS "FIL_ID", -- INT
            COALESCE ( CAST(
							CASE WHEN instr(EMP."DocumentoIdentidad" , '-') = 0 THEN -1
								 ELSE trim(LEFT(EMP."DocumentoIdentidad"  , instr(EMP."DocumentoIdentidad" , '-')-1)) END AS INT) , 0 )  AS "PER_ID", -- INT
            COALESCE (CAST(EMP."RazonSocial" AS VARCHAR(70)),'')  AS "FIL_ALIAS", -- VARCHAR(70)
            CAST('S' AS VARCHAR(1))  AS "FIL_MES_MONE", -- CHAR(1)
            CAST('S' AS VARCHAR(1))  AS "FIL_MES_PESO" -- CHAR(1)
        FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP
		WHERE EMP."IdEstado" = 'ACT'
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::FILIAL" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
