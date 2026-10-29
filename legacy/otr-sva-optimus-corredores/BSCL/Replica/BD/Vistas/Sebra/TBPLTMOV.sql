SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBPLTMOV"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBPLTMOV" AS

	SELECT 
		tab."TIPO_REG" 
		,tab."CODIGO"
		,tab."COD_MOVTO_CONT"
		,tab."CONCEPTO_MVTO"
		,tab."CTA_CONTAB"		
		,tab."DESCRIP_MOVTO"
		,tab."FPT_CAN_ACUM_VC"	--decimal	0
		,tab."FPT_CAN_COM_APLAZO"	--decimal	0
		,tab."FPT_CAN_CUST"	--decimal	0
		,tab."FPT_CAN_GTIA"	--decimal	0
		,tab."FPT_CAN_TRAM_COM"	--decimal	0
		,tab."FPT_CAN_TRAM_VTA"	--decimal	0
		,tab."FPT_CAN_VTA_APLAZO"	--decimal	0
		,tab."FPT_GASTO_APLAZO"	--decimal	0
		,tab."FPT_GASTO_CTDO"	--decimal	0
		,tab."FPT_MTO_ACUM_CUP"	--decimal	0
		,tab."FPT_MTO_REAL_ACUM_VC"	--decimal	0
		,tab."FPT_REAL_ACUM_VC"	--decimal	0
		,tab."FPT_REAL_ACUM_VTA"	--decimal	0
		,tab."FPT_SDO_APLAZO"	--decimal	0
		,tab."FPT_SDO_COMPROM"	--decimal	0
		,tab."FPT_SDO_DISP"	--decimal	0
		,tab."FPT_SDO_RETEN"	--decimal	0
		,tab."FSP_CAN_ACUM_VC"	--decimal	0
		,tab."FSP_CAN_COM_APLAZO"	--decimal	0
		,tab."FSP_CAN_CUST"	--decimal	0
		,tab."FSP_CAN_GTIA"	--decimal	0
		,tab."FSP_CAN_TRAM_COM"	--decimal	0
		,tab."FSP_CAN_TRAM_VTA"	--decimal	0
		,tab."FSP_CAN_VTA_APLAZO"	--decimal	0
		,tab."FSP_GASTO_APLAZO"	--decimal	0
		,tab."FSP_GASTO_CTDO"	--decimal	0
		,tab."FSP_MTO_ACUM_CUP"	--decimal	0
		,tab."FSP_MTO_REAL_ACUM_VC"	--decimal	0
		,tab."FSP_REAL_ACUM_VC"	--decimal	0
		,tab."FSP_REAL_ACUM_VTA"	--decimal	0
		,tab."FSP_SDO_APLAZO"	--decimal	0
		,tab."FSP_SDO_COMPROM"	--decimal	0
		,tab."FSP_SDO_DISP"	--decimal	0
		,tab."FSP_SDO_RETEN"	--decimal	0
		,tab."IND_CPDTPC"	--char	1
		,tab."IND_CTCIFM"	--char	1
		,tab."IND_CTCIIF"	--char	1
		,tab."IND_CTCIRF"	--char	1
		,tab."IND_CTCIRV"	--char	1
		,tab."IND_CTCTCL"	--char	1
		,tab."IND_CTCUFM"	--char	1
		,tab."IND_CTCURF"	--char	1
		,tab."IND_CTCURV"	--char	1
		,tab."IND_CTTRAS"	--char	1
		,tab."IND_CTTRTP"	--char	1
		,tab."IND_DEB_HAB"	--char	1
		,tab."IND_ENV_MVTO"	--char	1
		,tab."IND_ORSACL"	--char	1
		,tab."IND_PLLINR"	--char	1
		,tab."IND_TIT_CCTE"	--char	1
		,tab."IND_TRASPASO"	--char	1
		,tab."TIPO_CARTERA"	--char	2
		,tab."FAM_OPERAC"	--char	2
		,tab."CONCEP_MVTO_EX1"	--char	2
		,tab."CONCEP_MVTO_EX2"	--char	2
		,tab."VALUTA"	--decimal	0
		,tab."CONTRA_CTA"	--char	12
		,tab."IND_USO_GARANTIA"	--char	1
		,tab."IND_AFECTO_GMF"	--char	1
		,tab."IND_CTRFTP"	--char	2
		,tab."INDICADOR_RENDIMIENTOS"	--char	1
		,tab."IND_VENTA"	--char	1
		,tab."GENERA_MOVTO_APT"	--char	1
		,tab."RUT_CLI"	--char	11
		,tab."CTA_CONTAB_RELA"	--char	12
		,tab."IND_CENTRAL"	--char	1
			
	FROM "BSCL.Sebra.BD::MIG.TBPLTMOV" tab
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBPLTMOV" TO  BCS_DESARROLLADOR WITH GRANT OPTION;