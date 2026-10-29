SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTCIFM";


CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTCIFM" AS


	SELECT
		WST."IdStack" AS "IDSTACK","Action" AS ACTION,
		COD_PER  || ':' || SEC_CAR_INVER  || ':' || RUT_CLI || ':' || SEC_RUT_CLI || ':' || NEMO  AS TABLE_KEY
	  ,COD_PER --char(7)
      ,SEC_CAR_INVER --decimal(8,0)
      ,RUT_CLI --char(11)
      ,SEC_RUT_CLI --decimal(2,0)
      ,NEMO --char(16)
      ,COD_BOVEDA --char(3)
      ,FEC_INVER --char(10)
      ,CAN_ACTUAL --decimal(17,6)
      ,CAN_COM_PLAZO --decimal(17,6)
      ,CAN_GARANTIA --decimal(17,6)
      ,CAN_ORD_VEN --decimal(17,6)
      ,CAN_TRAM_COM --decimal(17,6)
      ,CAN_TRAM_VEN --decimal(17,6)
      ,COD_RUBRO --char(4)
      ,FEC_VCTO_TP --char(10)
      ,FOLIO_COMP_ADJ --decimal(8,0)
      ,INT_ACUM_GAN_CP --decimal(17,2)
      ,INT_ACUM_GAN_VP --decimal(17,2)
      ,LIN_COMP_ADJ --decimal(2,0)
      ,LIN_ORDEN --decimal(2,0)
      ,MONTO_ACT_AJUS --decimal(17,2)
      ,MONTO_ACUM_SPRE --decimal(17,2)
      ,MONTO_ACUM_VC --decimal(17,2)
      ,MONTO_ACUM_VC_CE --decimal(17,2)
      ,MONTO_ACUM_VC_CP --decimal(17,2)
      ,MONTO_ACUM_VC_VE --decimal(17,2)
      ,MONTO_ACUM_VEN --decimal(17,2)
      ,MONTO_INI_AJUS --decimal(17,2)
      ,PRECIO_CIERRE--decimal(17,6)
      ,PRECIO_PROM_COMP --decimal(17,6)
      ,SEC_ORDEN --decimal(8,0)
      ,TIPO_CARTERA --char(2)
      ,TIPO_COMP_ADJ --char(2)
      ,VTO_COMP_ADJ --char(10)

	FROM "BSCL.Replica.BD.Vistas::Sebra.TBCTCIFM" 
	left JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'TBCTCIFM' 
		AND WST."Key" = COD_PER  || ':' || SEC_CAR_INVER  || ':' || RUT_CLI || ':' || SEC_RUT_CLI || ':' || NEMO

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTBCTCIFM" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
