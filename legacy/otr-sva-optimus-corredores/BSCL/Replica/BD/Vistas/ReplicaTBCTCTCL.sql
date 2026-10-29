SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTCTCL"; 
	
CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTCTCL" AS

SELECT   
	 "IdStack" AS "IDSTACK"	,"Action" AS ACTION
	 ,COD_PER  ||':'|| TRIM(RUT_CLI) ||':'|| SEC_RUT_CLI ||':'|| CTA_CLI AS TABLE_KEY
	,COD_PER								--char(7)
	,RUT_CLI
	,SEC_RUT_CLI				--decimal(2, 0)
	,CTA_CLI										--char(4)
	,FEC_COBRO_GASTO					--char(10)
	,FEC_SALDO_NEG						--char(10)
	,FEC_ULT_OPE 					--char(10)
	,MONTO_OPE_ACC 					--decimal(17, 2) 
	,SALDO_COMIS 					--decimal(17, 2) 
	,SALDO_COMIS_DIA 				--decimal(17, 2) 
	,SALDO_COMIS_DIA_CALC			--decimal(17, 2) 
	,SALDO_COMIS_DIA_MIN 			--decimal(17, 2) 
	,SALDO_COMPROM 					--decimal(17, 2) 
	,SALDO_DERECHOS 					--decimal(17, 2) 
	,SALDO_DISP 			--decimal(17, 2)
	,SALDO_GASTOS 					--decimal(17, 2) 
	,SALDO_LIBRE_01 					--decimal(17, 2)   -- JK saldo_garantia
	,SALDO_LIBRE_02 					--decimal(17, 2)  
	,SALDO_LIBRE_03 					--decimal(17, 2)  
	,SALDO_NEG_INST 					--decimal(15, 4) 
	,SALDO_RETEN 	--decimal(17, 2)
	,SALDO_TP 				--decimal(17, 2) -- saldo en simultaneas compra - ventas
	,COMPRAS_24 						--decimal(17, 2)  
	,VENTAS_24 						--decimal(17, 2)  
	,COMPRAS_48 						--decimal(17, 2)  
	,VENTAS_48 						--decimal(17, 2)  
	,RECIBIR_24 						--decimal(17, 2)  
	,TRANSF_24 						--decimal(17, 2)  
	,TRANSF_48 						--decimal(17, 2)  
	,APORTES_24 						--decimal(17, 2)  
	,APORTES_48 						--decimal(17, 2)  
	,RESCATE_24 						--decimal(17, 2)  
	,RESCATE_48 						--decimal(17, 2)  
	,RESCATE_72 						--decimal(17, 2)  
	,RESCATE_96 						--decimal(17, 2)  
	,RESCATE_120 					--decimal(17, 2)  
	,SALDO_AFECTO_GMF 				--decimal(13, 2)  
	,MONTO_RETENIDO 					--decimal(18, 4)  
	,COMPRAS_72 						--decimal(17, 2)  
	,VENTAS_72 						--decimal(17, 2)  
	,TRANSF_72 						--decimal(17, 2)  
	,SALDO_HABERES 					--decimal(17, 2)
FROM "BSCL.Replica.BD.Vistas::Sebra.TBCTCTCL"  AS CTCL
--FROM (
--	SELECT * FROM "BSCL.Replica.BD.Vistas::Sebra.TBCTCTCL" UNION
--	SELECT * FROM "BSCL.Replica.BD.Vistas::Sebra.TBCTCTCLHIST"
--	) AS CTCL
LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'TBCTCTCL' 
	AND WST."Key" = COD_PER  ||':'|| TRIM(RUT_CLI) ||':'|| SEC_RUT_CLI ||':'|| CTA_CLI
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTBCTCTCL" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
