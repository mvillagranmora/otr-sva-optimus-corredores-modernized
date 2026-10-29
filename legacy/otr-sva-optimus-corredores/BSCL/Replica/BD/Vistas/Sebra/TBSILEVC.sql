SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBSILEVC"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBSILEVC" AS

SELECT	
            COALESCE (CAST(CASE WHEN EC."IdTipoEventoCapital" = 'CC' THEN 'CC'
								WHEN EC."IdTipoEventoCapital" = 'SL' THEN 'SL'
								ELSE 'VC' END  AS VARCHAR(2)),'')  AS "TIPO_EVE_CAP", -- CHAR(2)
			CASE WHEN EC."IdClasificacionTipoInstrumento" IN ('RF', 'IF', 'FM') THEN EC."IdClasificacionTipoInstrumento"
            			WHEN EC."IdTipoInstrumento" IN ('OSA','SIM','ACN') THEN 'AC'
 	           			WHEN EC."IdTipoInstrumento" IN ('ETN') THEN 'EN'
 	           			WHEN EC."IdTipoInstrumento" IN ('ETX') THEN 'ET'
 	           			WHEN EC."IdTipoInstrumento" IN ('MIL') THEN 'ST'
 	           			WHEN EC."IdTipoInstrumento" IN ('ACX') THEN 'AO'
	           			WHEN EC."IdTipoInstrumento" IN ('CFX') THEN 'CO'
	           			WHEN EC."IdTipoInstrumento" IN ('CFM') THEN 'CM'
            			WHEN EC."IdClasificacionTipoInstrumento" = 'SP' THEN 'MO'
            			WHEN EC."IdTipoInstrumento" = 'FWD' THEN 'FW'
            			WHEN EC."IdTipoInstrumento" = 'CFI' THEN 'CF'
            			ELSE 'XX'END AS "MERCADO", -- CHAR(2)
            CASE WHEN EC."IdTipoEventoCapital" = 'SU' THEN COALESCE (RPAD(EC."NemoNuevo",16),'') ELSE COALESCE (RPAD(EC."Nemo",16),'') END AS "NEMO", -- CHAR(16)
            COALESCE (EC."Secuencia" , 0 )  AS "SEC_EVE", -- DECIMAL
            COALESCE (CAST(EC."IdTipoEventoCapital" AS VARCHAR(2)),'')  AS "TIPO_VC", -- CHAR(2)
            MAX(COALESCE (CAST(EC."FechaPago" AS VARCHAR(10)),''))  AS "FEC_PAGO", -- CHAR(10)
            COALESCE (CASE WHEN EC."NumeroDividendo" > 9999 THEN 9999 ELSE EC."NumeroDividendo" END , 0 )  AS "NUM_DIVDO", -- DECIMAL
            COALESCE (CAST(EC."Descripcion" AS VARCHAR(40)),'')  AS "DESCRIPCION", -- CHAR(40)
            CAST(EC."EsInscrito" AS VARCHAR(1))  AS "IND_INSCRITO", -- CHAR(1)
            ''  AS "LAMINA", -- CHAR(15)
            TO_DECIMAL(COALESCE (EC."Monto" , 0 ),17,7)  AS "MONTO_DIVDO", -- DECIMAL
            0  AS "NUM_CER_BCS", -- DECIMAL
            0  AS "NUM_CER_CIA", -- DECIMAL
            0  AS "NUM_CER_DCV", -- DECIMAL
            0  AS "NUM_REG_ACC", -- DECIMAL
            EC."IdMonedaLiquidacion" AS "MONEDA", -- CHAR(3)
            0 AS "CORRELATIVO", -- NUMERIC
            COALESCE (CAST(EC."Monto" AS VARCHAR(60)),'')  AS "MONTO_DIVDO_REAL", -- VARCHAR(60)
			EC."TasaEfectiva" AS "TASA_ISFUT",
			EC."ISFUT" AS "IND_ISFUT"
        FROM "BSCL.Custodia.BD::CUS.EventoCapital" EC
        WHERE EC."IdEstado" != 'ANU'
        GROUP BY "IdTipoEventoCapital","IdClasificacionTipoInstrumento","IdTipoInstrumento","NemoNuevo","Nemo","NumeroDividendo","Secuencia","Descripcion","EsInscrito","Monto","IdMonedaLiquidacion"
        ,"TasaEfectiva","ISFUT"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBSILEVC" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
