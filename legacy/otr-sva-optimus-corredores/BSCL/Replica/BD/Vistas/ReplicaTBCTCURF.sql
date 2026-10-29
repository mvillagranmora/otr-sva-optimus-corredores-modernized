SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTCURF"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTCURF" AS

SELECT
	--TOP 1
	WST."IdStack" AS "IDSTACK","Action" AS ACTION,
	 COD_PER  || ':' || RUT_CLI || ':' ||  SEC_RUT_CLI || ':' ||  NEMO || ':' ||  COD_BOVEDA   AS TABLE_KEY,
	 "COD_PER"
	,"IdCliente"
	,"RUT_CLI"
	,"SEC_RUT_CLI"
	,"NEMO"
	,"COD_BOVEDA"
	,"CAN_COM_PLAZO"
	,"CAN_VEN_PLAZO"
	,"CAN_CUSTODIA"
	,"CAN_GARANTIA"
	,"CAN_LIBRE_01"
	,"CAN_LIBRE_02"
	,"CAN_LIBRE_03"
	,"CAN_TRAMITE_COM"
	,"CAN_TRAMITE_VEN"
	,"MONTO_INI_AJUS"
	,"MONTO_VALORIZ"
	,"PRECIO_MED_CIE"
	,"PRECIO_PROM"
	,"TIR_MED_CIE"
	,"TIR_PROM"
FROM "BSCL.Replica.BD.Vistas::Sebra.TBCTCURF" curf
--FROM "BSCL.Replica.BD.Vistas::Sebra.TBCTCURFHist"('PLACEHOLDER' = ('$$p_fecha$$','2022-06-30')) curf
LEFT  JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'TBCTCURF'
	and wst."Key" = (COD_PER  || ':' || RUT_CLI || ':' ||  SEC_RUT_CLI || ':' ||  NEMO || ':' ||  COD_BOVEDA)
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTBCTCURF" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
