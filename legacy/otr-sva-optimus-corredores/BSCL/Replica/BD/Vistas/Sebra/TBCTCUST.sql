SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTCUST"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTCUST" AS

Select 					    		
			CC."IdCuadratura",
            COALESCE (CAST(CC."FechaCuadraturaCustodia" AS VARCHAR(10)),'')  AS "FEC_MVTO", -- CHAR(10)
            
            LEFT(COALESCE(CASE WHEN CC."IdClasificacionTipoInstrumento" = 'RV' THEN   hpr."ValorOrigen"  
						    WHEN  CC."IdTipoInstrumento" = 'ETN' THEN   'EN'
   						    WHEN  CC."IdTipoInstrumento" = 'ETX' THEN   'ET'
					    		ELSE CC."IdClasificacionTipoInstrumento" END,CC."IdClasificacionTipoInstrumento"),2)  AS "MERCADO",
					    		
            
            
            COALESCE (CAST(CC."Nemotecnico" AS VARCHAR(16)),'')  AS "NEMO_SVS", -- CHAR(16)
            COALESCE (CAST(INS."IdEmisor" AS VARCHAR(10)),'')  AS "EMISOR", -- CHAR(10)
            COALESCE (CAST(CC."Nemotecnico" AS VARCHAR(16)),'')  AS "NEMO", -- CHAR(16)
            COALESCE(REPLACE(CAST(INS."IdTipoReajuste" AS VARCHAR(2)), 'CL' , 'NR'),'')  AS "TIPO_REAJ", -- CHAR(2)
            COALESCE (CAST(INS."FechaVencimiento" AS VARCHAR(10)),'1900-01-01')  AS "FEC_VCTO", -- CHAR(10)
            COALESCE (NOR."CantidadUnidadesNominal" , CC."CantidadCustodia")  AS "CANTIDAD", -- DECIMAL
            COALESCE (NOR."Valorizacion", CC."Valorizacion")  AS "MONTO_VALORIZ", -- DECIMAL
            COALESCE (NOR."CarteraPropia", CC."CantidadCuentaPropia")  AS "CAN_CP", -- DECIMAL
            COALESCE (NOR."CustodiaTerceros", (CC."CantidadTerceros" + CC."CantidadPactoVenta"))  AS "CAN_CT", -- DECIMAL
            COALESCE (NOR."CustodiaPropiaDCV", CC."CantidadDCVCuentaPropia")  AS "CAN_CP_DCV", -- DECIMAL
            COALESCE (NOR."CustodiaTercerosDCV", CC."CantidadDCVTerceros")  AS "CAN_CT_DCV", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "CAN_CIA", -- DECIMAL 
            0 AS CAN_CORREDOR,
            COALESCE (NOR."CustodiaBolsas" ,(CC."CantidadBCS"+ CC."CantidadBEC"+ CC."CantidadBOVALPO"))  AS "CAN_BOLSAS", -- DECIMAL
            COALESCE (NOR."CustodiaOtros", (CC."CantidadCustodia" - (CC."CantidadDCVCuentaPropia" + CC."CantidadDCVTerceros" + CC."CantidadEmisores" + CC."CantidadIntermediario")))  AS "CAN_OTROS", -- DECIMAL
            COALESCE (CAST(INS."IdTipoInstrumento" AS VARCHAR(10)),'')  AS "TIPO_INST", -- CHAR(10)
            COALESCE (CAST(NOR."TipoUnidades" AS VARCHAR(15)),'')  AS "TIPO_UNID", -- CHAR(15)
            COALESCE (CAST('9999999' AS VARCHAR(100)),'')  AS "CUENTA", -- VARCHAR(100)
            COALESCE (CAST('' AS VARCHAR(26)),'')  AS "FEC_EVE", -- CHAR(26)
            COALESCE (CAST('OK' AS VARCHAR(2)),'')  AS "TIPO_REG" -- CHAR(2)

        FROM "BSCL.Normativa.BD::PRO.CuadraturaCustodia" CC
        LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS hpr ON (hpr."TipoValor" = 'TipoInstrumentoSegunMercado' 
                                                              AND hpr."Entidad" = 'TipoInstrumentoSegunMercado' 
                                                              AND hpr."ValorDestino" = CC."IdTipoInstrumento"   AND hpr."ValorOrigen" NOT IN ('CM' , 'ET' , 'EN'))
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON CC."IdInstrumento" = INS."IdInstrumento"
		LEFT JOIN "BSCL.Normativa.BD::NOR.DatosCIR1720" NOR ON CC."IdCuadratura" = NOR."IdCuadratura" 
		
		WHERE  rtrim(COALESCE (CC."Nemotecnico" ,'')) != ''

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBCTCUST" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
