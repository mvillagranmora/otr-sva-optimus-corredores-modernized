SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaCOBRO_CANAL"; 

-- select *  from "BSCL.Replica.BD.Vistas::ReplicaCOBRO_CANAL"

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaCOBRO_CANAL" AS


		SELECT
			par_tpc_id || ':' ||  MERCADO || ':' ||  pro_id || ':' ||  ope_id || ':' || cob_id || ':' ||  cca_codigo AS TABLE_KEY,
				*
		FROM (
	        Select WST."IdStack" AS "IDSTACK", "Action" AS ACTION,
				PAR_TPC_ID -- 
				,MERCADO
				,PRO_ID
				,OPE_ID
				,COB_ID -- 
				,PCO_IND_AMB
				,PCO_ESP_SIMPLAZO
				,PCO_ESP_VALPLAZO
				,PCO_ESP_EMISOR
				,CCA_CODIGO
				,COC_VAL_CARGO
				,COC_VAL_MIN
			FROM "BSCL.Replica.BD.Vistas::Sebra.COBRO_CANAL" DPC
	        LEFT  JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'COBRO_CANAL' AND WST."Key" = 
	        	par_tpc_id || ':' ||  MERCADO || ':' ||  pro_id || ':' ||  ope_id || ':' || cob_id || ':' ||  cca_codigo
		) AS tb1
		
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaCOBRO_CANAL" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
