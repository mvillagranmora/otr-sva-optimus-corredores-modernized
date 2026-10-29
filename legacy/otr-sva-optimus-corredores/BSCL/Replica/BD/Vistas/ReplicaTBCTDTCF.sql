SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTDTCF"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTDTCF" AS

		SELECT
			 
			WST."IdStack" AS "IDSTACK", "Action" AS ACTION,
			TIPO_COMP_FAC || ':' ||   cast(FOLIO_COMP_FAC as varchar) || ':' ||   cast(LIN_COMP_FAC as varchar)  AS TABLE_KEY,
			*
		
		FROM "BSCL.Replica.BD.Vistas::Sebra.TBCTDTCF" DCOM
		LEFT  JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'TBCTDTCF' AND WST."Key" = TIPO_COMP_FAC || ':' ||   cast(FOLIO_COMP_FAC as varchar) || ':' ||   cast(LIN_COMP_FAC as varchar)

		
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTBCTDTCF" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
