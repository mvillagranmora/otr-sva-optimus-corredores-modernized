SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTGTIA"; 

-- SELECT  *  FROM  "BSCL.Replica.BD.Vistas::ReplicaTBCTGTIA" where sec_car_inver = 286550



CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTGTIA" AS

	SELECT
			"IdStack" AS "IDSTACK","Action" AS ACTION,
			COD_PER || ':' ||  rtrim(ltrim(RUT_CLI)) || ':' ||  rtrim(ltrim(SEC_RUT_CLI)) || ':' ||  MERCADO || ':' ||  rtrim(ltrim(NEMO)) || ':' ||  COD_BOVEDA || ':' ||  cast(SEC_CAR_INVER as varchar) || ':' ||  CONCEPTO_MVTO AS TABLE_KEY,
			*
		
		FROM "BSCL.Replica.BD.Vistas::Sebra.TBCTGTIA" GAR
		LEFT  JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'TBCTGTIA' AND WST."Key" = COD_PER || ':' ||  rtrim(ltrim(RUT_CLI)) || ':' ||  rtrim(ltrim(SEC_RUT_CLI)) || ':' ||  MERCADO || ':' ||  rtrim(ltrim(NEMO)) || ':' ||  COD_BOVEDA || ':' ||  cast(SEC_CAR_INVER as varchar) || ':' ||  CONCEPTO_MVTO
		
WITH READ ONLY; 

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTBCTGTIA" TO  BCS_DESARROLLADOR WITH GRANT OPTION;




