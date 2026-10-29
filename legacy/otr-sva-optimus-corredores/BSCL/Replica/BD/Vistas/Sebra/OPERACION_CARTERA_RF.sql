SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.OPERACION_CARTERA_RF"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.OPERACION_CARTERA_RF" AS

Select 
			ASIG."IdAsignacion",
            COALESCE (ASIG."IdOrden" , 0 )  AS "OCR_SOL_SEC", -- NUMERIC
            0   AS "OCR_LINEA", -- NUMERIC
            COALESCE (ASIG."FolioCartera" , 0 )  AS "OCR_SEC_CAR_INVER", -- NUMERIC
            COALESCE (CAST(ORD."Nemotecnico" AS VARCHAR(16)),'')  AS "OCR_NEMO", -- CHAR(16)
            COALESCE (ASIG."CantidadAsignada" , 0 )  AS "OCR_CANT", -- NUMERIC
            COALESCE ( 0 , 0 )  AS "OCR_NUM_GRUPO", -- NUMERIC
            COALESCE ( 0 , 0 )  AS "OCR_SEC_DIST", -- NUMERIC
            COALESCE (ASIG."MontoAsignado", 0 )  AS "OCR_MONTO_VAL_DISP", -- DECIMAL
            COALESCE (ASIG."MontoAsignado" , 0 )  AS "OCR_MONTO_ACT_AJUS" -- DECIMAL

        FROM "BSCL.Orden.BD::ORD.Asignacion" ASIG
        INNER JOIN "BSCL.Orden.BD::ORD.Orden" ORD ON ASIG."IdOrden" = ORD."IdOrden"
        WHERE "IdClasificacionTipoInstrumento" = 'RF' and ASIG."FolioCartera" > 0

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.OPERACION_CARTERA_RF" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
