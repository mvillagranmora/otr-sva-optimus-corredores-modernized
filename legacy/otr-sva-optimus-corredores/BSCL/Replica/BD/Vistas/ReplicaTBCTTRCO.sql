SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTTRCO"; 

--- SELECT *  from "BSCL.Replica.BD.Vistas::ReplicaTBCTTRCO"  WHERE SEC_MOVTO = 1316617
		

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTTRCO" AS
	
		SELECT
			WST."IdStack" AS IDSTACK,"Action" AS ACTION,
			COD_PER || ':' ||   FEC_VCTO_TP || ':' ||   rtrim(ltrim(NEMO)) || ':' ||  rtrim(ltrim(RUT_CLI)) || ':' ||   rtrim(ltrim(SEC_RUT_CLI)) || ':' ||  cast(SEC_MOVTO as varchar) || ':' ||   cast(FOLIO_PRESTAMO as varchar) || ':' ||   cast(Sec_Folio as varchar) || ':' ||   rtrim(ltrim(Tipo_Rel)) AS TABLE_KEY,
			*
		
		FROM "BSCL.Replica.BD.Vistas::Sebra.TBCTTRCO" CAR
		LEFT  JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'TBCTTRCO' AND WST."Key" = CAR.FOLIO_PRESTAMO
					

WITH READ ONLY; 

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTBCTTRCO" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
