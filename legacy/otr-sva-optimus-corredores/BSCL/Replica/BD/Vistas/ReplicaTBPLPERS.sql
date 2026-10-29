SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTBPLPERS"; 

-- select *  from "BSCL.Replica.BD.Vistas::ReplicaTBPLPERS"

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTBPLPERS" AS

        SELECT 
        	WST."IdStack" AS "IDSTACK","Action" AS ACTION,
        	PER."RUT_PERSONA"  AS TABLE_KEY,
			PER."TIPO_REG",
			PER."RUT_PERSONA",
			PER."SEC_RUT_PERSONA",
			PER."NOMBRE",
			PER."CASILLA",
			PER."CENTRO_COSTO",
			PER."CIUDAD",
			PER."COMUNA",
			PER."DIRECCION",
			PER."FAX",
			PER."OBSERVACION",
			PER."PAIS",
			PER."TELEFONO",
			PER."COD_BCO",
			PER."CTA_BCO",
			PER."IND_COMIS_ACC",
			PER."TIPO_COMIS_ACC",
			PER."IND_COMIS_IRF",
			PER."TIPO_COMIS_IRF",
			PER."IND_COMIS_IIF",
			PER."TIPO_COMIS_IIF",
			PER."IND_COBRANZA",
			PER."TIPO_MON_COMIS",
			PER."MONTO_COMISION",
			PER."TIPO_PROV",
			PER."EMAIL"
        
		FROM  "BSCL.Replica.BD.Vistas::Sebra.TBPLPERS" PER
        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'TBPLPERS' AND WST."Key" = PER."RUT_PERSONA"
		
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTBPLPERS" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
