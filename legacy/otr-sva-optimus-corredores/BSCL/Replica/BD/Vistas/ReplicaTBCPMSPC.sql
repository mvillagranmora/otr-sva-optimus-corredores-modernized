SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCPMSPC"; 

-- select * from "BSCL.Replica.BD.Vistas::ReplicaTBCPMSPC" where SEC_COMPROM  = 6891 
--SELECT  * 	FROM "BSCL.Replica.BD.Vistas::ReplicaTBCPMSPC"  


CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCPMSPC" AS

		SELECT
			COD_PER || ':' ||    cast(SEC_COMPROM as varchar) AS TABLE_KEY,
			"IdStack" AS IDSTACK,"Action" AS ACTION,
			COD_PER, -- TIPO :char
			SEC_COMPROM, -- TIPO :decimal --
			RUT_CORR, -- TIPO :char -- rut de la corredora
			SEC_RUT_CORR, -- TIPO :decimal -- 1
			TIPO_COMPROM, -- TIPO :char -- VT CT CP VP
			RUT_CLI, -- TIPO :char
			SEC_RUT_CLI, -- TIPO :decimal
			FEC_LIQUID, -- TIPO :char
			FEC_OPERAC, -- TIPO :char
			CAN_LIN, -- TIPO :decimal  > 1 
			CANTIDAD_CAPITAL, -- TIPO :decimal   
			CANTIDAD_COMPROM, -- TIPO :decimal 
			COD_AGENTE, -- TIPO :char 
			COD_BCO_PAGO, -- TIPO :char
			COD_BCO_RECIBO, -- TIPO :char
			COD_MESA, -- TIPO :char 
			COD_SUC, -- TIPO :char
			CTA_BCO_PAGO, -- TIPO :char
			CTA_BCO_RECIBO, -- TIPO :char
			DONDE_PAGO, -- TIPO :char
			DONDE_RECIBO, -- TIPO :char
			ESTADO, -- TIPO :char  -- Idestado   > I / E / T 
			FEC_VCTO_COM, -- TIPO :char
			FOLIO_DOC_ADJ, -- TIPO :decimal  
			FORMA_PAGO, -- TIPO :char
			FORMA_RECIBO, -- TIPO :char
			GLOSA, -- TIPO :char  
			IND_EMI_CERTIF, -- TIPO :char   
			IND_PAGADO, -- TIPO :char 
			IND_TIT_CUST_CLI, -- TIPO :char  > validar
			IND_TIT_CUST_CORR, -- TIPO :char 
			INTERES_DIA, -- TIPO :decimal
			MONTO_ASIG, -- TIPO :decimal > monto contado
			MONTO_ASIG_MAS, -- TIPO :decimal   > monto contado
			MONTO_ASIG_MEN, -- TIPO :decimal> monto contado
			MONTO_CAPITAL, -- TIPO :decimal> monto contado
			MONTO_COMPROM, -- TIPO :decimal > monto a plazo
			MONTO_INI_AJUS, -- TIPO :decimal > monto compra / monto contado
			PLAZO, -- TIPO :decimal
			SEC_COMP_ASOC, -- TIPO :decimal  
			SPREAD, -- TIPO :decimal 
			TASA_AGE, -- TIPO :decimal
			TASA_COMPROM, -- TIPO :decimal
			TIPO_FAM, -- TIPO :char
			TIPO_REAJ_COMP, -- TIPO :char  > 
			USUARIO, -- TIPO :decimal
			VALOR_PTE, -- TIPO :decimal 
			IND_SOL_ANUL, -- TIPO :char
			SEC_MOVTO_CTDO, -- TIPO :decimal   > validar
			SEC_MOVTO_APLAZO, -- TIPO :decimal  > validar
			COD_BOVEDA, -- TIPO :char  
			HORA, -- TIPO :char 
			TASA_CAM_APLAZO, -- TIPO :decimal > tasa 
			TASA_CAM_CTDO, -- TIPO :decimal
			CANTIDAD_CAPITAL_H, -- TIPO :decimal   MOnto contado  
			CANTIDAD_COMPROM_H, -- TIPO :decimal  monto a plazo
			TASA_COMPROM_H, -- TIPO :decimal >
			MONTO_CAPITAL_H, -- TIPO :decimal    MOnto contado  
			MONTO_COMPROM_H, -- TIPO :decimal   monto a plazo
			IND_LIQUID,
			FEC_IND_LIQUID, -- TIPO :char
			FOLIO_OPE_INI, -- TIPO :char
			FOLIO_OPE_FIN, -- TIPO :char
			FEC_ANULACION, -- TIPO :char
			USR_ANULACION, -- TIPO :char
			CODCATIFRSID -- TIPO :tinyint
		FROM "BSCL.Replica.BD.Vistas::Sebra.TBCPMSPC" MSPC
		LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'TBCPMSPC' AND WST."Key" = COD_PER || ':' ||    cast(SEC_COMPROM as varchar)
WITH READ ONLY;
 
GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTBCPMSPC" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
