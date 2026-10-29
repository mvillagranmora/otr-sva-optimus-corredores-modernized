SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTCIRV"; 

-- select  count(1) from  "BSCL.Replica.BD.Vistas::ReplicaTBCTCIRV" where sec_car_inver = 1211472
-- select * from  "BSCL.Custodia.BD::CUS.Cartera" where "FolioCartera" = 1211472  and  "IdEstado" = 'VIG'
---select * from  "BSCL.Custodia.BD::GTA.Garantia" AS G where "FolioCarteraOrigen" = 1211472  

--1211472

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTCIRV" AS

SELECT

	WST."IdStack" AS "IDSTACK","Action" AS ACTION,
	COD_PER  || ':' || SEC_CAR_INVER  || ':' ||  TRIM(RUT_CLI) || ':' ||  SEC_RUT_CLI || ':' ||  rtrim(NEMO)  AS TABLE_KEY,
	*

FROM "BSCL.Replica.BD.Vistas::Sebra.TBCTCIRV" SCUST
left JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'TBCTCIRV' 
	AND WST."Key" = COD_PER  || ':' || SEC_CAR_INVER  || ':' ||  TRIM(RUT_CLI) || ':' ||  SEC_RUT_CLI || ':' ||  rtrim(NEMO)

WITH READ ONLY; 

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTBCTCIRV" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
