SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCPDTPC"; 
CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCPDTPC" AS



		SELECT
            COD_PER || ':' ||    cast(SEC_COMPROM as varchar) || ':' ||    cast(LIN_COMPROM as varchar) AS TABLE_KEY,
			"IdStack" AS IDSTACK,"Action" AS ACTION,
			COD_PER, -- TIPO :char
			SEC_COMPROM, -- TIPO :decimal 
			LIN_COMPROM, -- TIPO :decimal 
			EMISOR, -- TIPO :char -- 
			FEC_LIQUID, -- TIPO :char   
			TIPO_REAJ, -- TIPO :char
			NEMO, -- TIPO :char
			SEC_CAR_CORR, -- TIPO :decimal
			CANT_PC, -- TIPO :decimal  
			CANT_PC_COMPROM, -- TIPO :decimal  
			CANTIDAD, -- TIPO :decimal  
			COD_CLASIF_RIESGO, -- TIPO :decimal
			EST_LIN_COMPROM, -- TIPO :char
			FEC_EMI, -- TIPO :char
			FEC_VCTO, -- TIPO :char
			IND_COBERTURA, -- TIPO :char
			IND_CORTES, -- TIPO :char	
			MERCADO, -- TIPO :char  
			MONTO_ACUM_CUP, -- TIPO :decimal
			MONTO_CARTERA, -- TIPO :decimal 
			MONTO_CARTERA_MAS, -- TIPO :decimal  
			MONTO_CARTERA_MEN, -- TIPO :decimal 
			MONTO_COMPROM, -- TIPO :decimal 
			MONTO_INI_CAP, -- TIPO :decimal  
			MONTO_INI_CAP_MAS, -- TIPO :decimal  
			MONTO_INI_CAP_MEN, -- TIPO :decimal  
			PRECIO, -- TIPO :decimal
			RUT_CLI, -- TIPO :char
			SEC_CAR_CLI, -- TIPO :decimal
			SEC_RUT_CLI, -- TIPO :decimal
			TASA_COMPROM, -- TIPO :decimal   
			TIPO_FAM, -- TIPO :char
			TIR, -- TIPO :decimal  --
			VAL_RESC, -- TIPO :decimal
			VALOR_PTE, -- TIPO :decimal -- 
			COD_BOVEDA, -- TIPO :char
			DURATION, -- TIPO :decimal  -- 
			TIPO_CARTERA, -- TIPO :char
			IND_FUNGIBILIDAD, -- TIPO :char
			MONTO_INI_CAP_TOT, -- TIPO :decimal  
			INTRADAY, -- TIPO :char
			TASA_SELECCIONADA, -- TIPO :char  
			SEC_DETALLE -- TIPO :decimal 
		
		FROM "BSCL.Replica.BD.Vistas::Sebra.TBCPDTPC" dtpc		
		LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'TBCPDTPC' 
			AND WST."Key" = COD_PER || ':' ||    cast(SEC_COMPROM as varchar) || ':' ||    cast(LIN_COMPROM as varchar)
		WITH READ ONLY;
	 
GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTBCPDTPC" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
