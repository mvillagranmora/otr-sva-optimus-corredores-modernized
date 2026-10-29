SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.OPERACION_CARTERA_IF"; 
--select OCI_SOL_SEC,OCI_SEC_CAR_INVER,count(1) from "BSCL.Replica.BD.Vistas::Sebra.OPERACION_CARTERA_IF" group by OCI_SOL_SEC,OCI_SEC_CAR_INVER having count(1) > 1
--select * from  "BSCL.Replica.BD.Vistas::Sebra.OPERACION_CARTERA_IF" where OCI_SOL_SEC =  507515

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.OPERACION_CARTERA_IF" AS

Select 
			ASIG."IdAsignacion",
			COALESCE (ASIG."IdOrden" , 0 )  AS "OCI_SOL_SEC", -- NUMERIC
            0   AS "OCI_LINEA_IF", -- NUMERIC
            COALESCE (ASIG."FolioCartera" , 0 )  AS "OCI_SEC_CAR_INVER", -- NUMERIC
            COALESCE (CAST(ORD."Nemotecnico" AS VARCHAR(16)),'')  AS "OCI_NEMO", -- CHAR(16)
            COALESCE (CAST('' AS VARCHAR(16)),'')  AS "OCI_EMISOR", -- CHAR(16)
            COALESCE (CAST('' AS VARCHAR(2)),'')  AS "OCI_TIPO_REAJ", -- CHAR(2)
            COALESCE (ORD."FechaVencimiento" , '1900-01-01' )  AS "OCI_FEC_VCTO", -- DATETIME
            COALESCE (ASIG."CantidadAsignada" , 0 )  AS "OCI_CANTIDAD", -- NUMERIC
            COALESCE ("ValorRescateAsignado" , 0 )  AS "OCI_VAL_RESC_ASIG", -- NUMERIC
            COALESCE (ASIG."MontoAsignado", 0 )  AS "OCI_MONTO_VAL_DISP", -- DECIMAL
            COALESCE (ASIG."MontoAsignado", 0 )  AS "OCI_MONTO_ACT_AJUS" -- DECIMAL
        FROM "BSCL.Orden.BD::ORD.Asignacion" ASIG
        INNER JOIN "BSCL.Orden.BD::ORD.Orden" ORD ON ASIG."IdOrden" = ORD."IdOrden"
		
        WHERE "IdClasificacionTipoInstrumento" = 'IF' and ASIG."FolioCartera" > 0

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.OPERACION_CARTERA_IF" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
