SET SCHEMA "BSCL";
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTECCL"; 

--select *  from bscl."BSCL.Replica.BD.Vistas::ReplicaTBCTECCL" 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTECCL" AS
        
	 Select WST."IdStack" AS "IDSTACK","Action" AS ACTION,
			TIPO_EVE_CAP || ':' ||  MERCADO || ':' ||  NEMO || ':' ||  SEC_EVE || ':' || TIPO_CARTERA || ':' ||  RUT_CLI || ':' || SEC_RUT_CLI  AS TABLE_KEY,
			*
			
		FROM "BSCL.Replica.BD.Vistas::Sebra.TBCTECCL" EC
	    LEFT  JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'TBCTECCL' AND WST."Key" = TIPO_EVE_CAP || ':' ||  MERCADO || ':' ||  NEMO || ':' ||  SEC_EVE || ':' || TIPO_CARTERA || ':' ||  RUT_CLI || ':' || SEC_RUT_CLI
	 
	    
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTBCTECCL" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
