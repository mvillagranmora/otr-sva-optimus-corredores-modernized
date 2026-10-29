SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaCLIENTE"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaCLIENTE" AS

        Select WST."IdStack" AS "IDSTACK","Action" AS ACTION,
        	"PER_ID"  AS TABLE_KEY,
            "PER_ID", -- INT
            "FIL_ID", -- INT
            "EST_ID", -- VARCHAR(2)
            "CCA_CODIGO", -- CHAR(3)
            "CLI_MOD_OPERA", -- CHAR(4) -- 
            "CLI_ALIAS", -- VARCHAR(30) --
            "CLI_IND_FICHA", -- CHAR(1)
            "CLI_NUM_FICHA", -- INT
            "CLI_INF_PRIV", -- CHAR(1)
            "PAR_TIP_SVS", -- VARCHAR(10) --tipo relacion svs
            "PAR_REL_CORRE", -- VARCHAR(10) --relacion ng380
            "PAR_TIP_NCG", -- VARCHAR(10) --indicador ncg
            "CLI_EMI_DTE", -- CHAR(1)
            "CLI_DES_DTE", -- CHAR(5)
            "CLI_OBS_BLOQUEO", -- VARCHAR(200)
            "CLI_FEC_INGRESO", -- DATETIME
            "CLI_FEC_MODIFICA", -- DATETIME
            "EMP_CODIGO", -- CHAR(4)
            "FOLIOCONDGENERALESRF", -- NUMERIC
            "FECHACONDGENERALESRF", -- DATETIME
            "COD_BANCO", -- CHAR(10)
            "CLI_OBS_NCG", -- VARCHAR(255)
            "TIPO_CLIENTE", -- VARCHAR(10) --??
            "CLI_SUS_CON_INT", -- CHAR(1)	--ver si existe como indicador ??
            "PAR_REL_SII", -- VARCHAR(10)	--indicadores ??
            "PAR_REL_DCV", -- VARCHAR(10) --indicadores ??
            "PAR_REL_BCS", -- VARCHAR(10) --indicadores ??
            "COPER", -- DECIMAL
            "COPEROBO" -- DECIMAL
        FROM "BSCL.Replica.BD.Vistas::Sebra.CLIENTE" CLI
        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'CLIENTE' AND WST."Key" = CLI."PER_ID"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaCLIENTE" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
