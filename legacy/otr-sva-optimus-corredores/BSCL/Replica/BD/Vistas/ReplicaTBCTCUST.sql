SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTCUST"; 

-- select  *  from "BSCL.Replica.BD.Vistas::ReplicaTBCTCUST"

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTCUST" AS

        Select WST."IdStack" AS "IDSTACK", "Action" AS ACTION,
        	CC."FEC_MVTO" || ':' ||  LEFT(COALESCE(CASE WHEN CC."MERCADO" = 'RV' THEN   hpr."ValorOrigen"  
						    WHEN  CC.TIPO_INST = 'ETN' THEN   'EN'
   						    WHEN  CC.TIPO_INST = 'ETX' THEN   'ET'
					    		ELSE CC."MERCADO" END,CC."MERCADO"),2)  || ':' || CC."NEMO" || ':' || "EMISOR"   AS TABLE_KEY,
            *

        FROM "BSCL.Replica.BD.Vistas::Sebra.TBCTCUST" CC
        LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS hpr ON (hpr."TipoValor" = 'TipoInstrumentoSegunMercado' 
                                                              AND hpr."Entidad" = 'TipoInstrumentoSegunMercado' 
                                                              AND hpr."ValorDestino" = CC.TIPO_INST   AND hpr."ValorOrigen" NOT IN ('CM' , 'ET' , 'EN'))
        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'TBCTCUST' AND WST."Key" = CC."FEC_MVTO" || ':' ||  LEFT(COALESCE(CASE WHEN CC."MERCADO" = 'RV' THEN   hpr."ValorOrigen"  
																																			    WHEN  CC.TIPO_INST = 'ETN' THEN   'EN'
																													   						    WHEN  CC.TIPO_INST = 'ETX' THEN   'ET'
																																		    		ELSE CC."MERCADO" END,CC."MERCADO"),2)  || ':' || CC."NEMO" || ':' || "EMISOR"
		
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTBCTCUST" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
