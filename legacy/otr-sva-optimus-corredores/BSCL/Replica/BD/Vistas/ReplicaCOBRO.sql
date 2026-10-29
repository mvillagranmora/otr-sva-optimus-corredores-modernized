SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaCOBRO"; 
--SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaCOBRO" 
CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaCOBRO" AS

        Select WST."IdStack" AS "IDSTACK", "Action" AS ACTION,
        	COB."COB_ID"  AS TABLE_KEY,
            "COB_ID", -- CHAR(4)
            "PAR_TCO_ID", -- VARCHAR(10)
            "COB_NOMBRE", -- VARCHAR(50)
            "COB_DIF_CANAL", -- CHAR(1)
            "PAR_TIP_CARGO", -- VARCHAR(10)
            "PAR_TIP_EVENTO", -- VARCHAR(10)
            "COB_PERIODO", -- CHAR(3)
            "PAR_BAS_CARGO", -- VARCHAR(10)
            "PAR_BEN_TIPO", -- VARCHAR(10)
            "COB_BEN_META", -- NUMERIC
            "COB_BEN_UNIDAD", -- VARCHAR(3)
            "COB_BEN_MONEDA", -- VARCHAR(3)
            "COB_INC_IVA", -- CHAR(1)
            "COB_COM_ESP" -- CHAR(1)

        FROM "BSCL.Replica.BD.Vistas::Sebra.COBRO" COB
        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'COBRO' AND WST."Key" = COB."COB_ID"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaCOBRO" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
