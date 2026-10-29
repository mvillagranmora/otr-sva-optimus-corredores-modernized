SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCPMSPC"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCPMSPC" AS

		SELECT * FROM (
			SELECT
			LEFT(fec."Valor",7)   AS COD_PER, -- TIPO :char
			COALESCE (OPE."IdOperacion" , 0    )  AS SEC_COMPROM, -- TIPO :decimal --
			"BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(IDENTCOR."DocumentoIdentidad") AS RUT_CORR, -- TIPO :char -- rut de la corredora
			COALESCE (LEFT(COR."CodigoPortafolio", 1), '0'    )  AS SEC_RUT_CORR, -- TIPO :decimal -- 1
			CASE WHEN OPE."IdTipoOperacion" = 'COMPAC' THEN 'CP'
				 WHEN OPE."IdTipoOperacion" = 'VENPAC' THEN 'VP'
				 WHEN OPE."IdTipoOperacion" = 'COMTER' THEN 'CT'
				 WHEN OPE."IdTipoOperacion" = 'VENTER' THEN 'VT'
				 ELSE ''
				 END AS TIPO_COMPROM, -- TIPO :char -- VT CT CP VP
			"BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(IDENT."DocumentoIdentidad")  AS RUT_CLI, -- TIPO :char
			CAST(POR."CodigoPortafolio" AS VARCHAR(100))  AS SEC_RUT_CLI, -- TIPO :decimal			
			TO_VARCHAR(CASE OPE."IdTipoOperacion" 
				WHEN 'COMPAC' THEN CASE WHEN OPE."IdEstado" = 'ANT' THEN OPE."FechaLiquidacion" ELSE OPE."FechaVencimiento" END
				WHEN 'VENPAC' THEN CASE WHEN OPE."IdEstado" = 'ANT' THEN OPE."FechaLiquidacion" ELSE OPE."FechaVencimiento" END
				WHEN 'COMTER' THEN OPE."FechaOperacion" 
				WHEN 'VENTER' THEN OPE."FechaOperacion"
				ELSE OPE."FechaLiquidacion"
				END
			, 'YYYY-MM-DD')  AS FEC_LIQUID, -- TIPO :char
			TO_VARCHAR(OPE."FechaOperacion", 'YYYY-MM-DD')  AS FEC_OPERAC, -- TIPO :char
			COALESCE (RES."CAN_LIN" , 0    )  AS CAN_LIN, -- TIPO :decimal  > 1 
			CAST(COALESCE(CASE WHEN COALESCE(conf."Valor", 'N') = 'S' THEN OPE."MontoOperacionInicialMoneda" ELSE OPE."MontoOperacionInicial" END, 0) AS DECIMAL(15,4)) AS CANTIDAD_CAPITAL, -- TIPO :decimal   
			CAST(CASE WHEN OPE."IdEstado" = 'ANT' AND OPE."IdTipoOperacion" IN ('COMPAC','VENPAC') THEN COALESCE(CASE WHEN COALESCE(conf."Valor", 'N') = 'S' THEN OPE."MontoOperacionFinalMonedaAnticipo" ELSE OPE."MontoOperacionFinalAnticipo" END, 0) ELSE COALESCE(CASE WHEN COALESCE(conf."Valor", 'N') = 'S' THEN OPE."MontoOperacionFinalMoneda" ELSE OPE."MontoOperacionFinal" END, 0) END AS DECIMAL(15,4)) AS CANTIDAD_COMPROM, -- TIPO :decimal 
			LEFT(COALESCE (CAST(COD_EMP_TRUN4."CodigoEmpleado" AS VARCHAR(100)),''   ),40)  AS COD_AGENTE, -- TIPO :char
			''  AS COD_BCO_PAGO, -- TIPO :char
			''  AS COD_BCO_RECIBO, -- TIPO :char
			LEFT(COALESCE (CAST(MES."Codigo" AS VARCHAR(4)),''   ),4)  AS COD_MESA, -- TIPO :char 
			''  AS COD_SUC, -- TIPO :char
			''  AS CTA_BCO_PAGO, -- TIPO :char
			''  AS CTA_BCO_RECIBO, -- TIPO :char
			''  AS DONDE_PAGO, -- TIPO :char
			''  AS DONDE_RECIBO, -- TIPO :char
			--COALESCE (CAST('' AS VARCHAR(100)),''   )  AS ESTADO, -- TIPO :char  -- Idestado   > I / E / T
			CASE WHEN OPE."IdEstado" = 'PEN' THEN 'I'
				 WHEN OPE."IdEstado" = 'VIG' THEN 'E'
				 WHEN OPE."IdEstado" = 'ANU' THEN 'N'
				 WHEN OPE."IdEstado" = 'LIQ' THEN 'T'
				 ELSE 'T' END AS ESTADO, -- TIPO :char  -- Idestado   > I / E / T 
				 
			COALESCE (TO_VARCHAR(OPE."FechaVencimiento" , 'YYYY-MM-DD'),''   )  AS FEC_VCTO_COM, -- TIPO :char
			0  AS FOLIO_DOC_ADJ, -- TIPO :decimal  
			''  AS FORMA_PAGO, -- TIPO :char
			'' AS FORMA_RECIBO, -- TIPO :char
			COALESCE (CAST(OPE."Glosa" AS VARCHAR(100)),''   )  AS GLOSA, -- TIPO :char  
			'S'  AS IND_EMI_CERTIF, -- TIPO :char   
			'N'  AS IND_PAGADO, -- TIPO :char 
			COALESCE (CAST('' AS VARCHAR(100)),''   )  AS IND_TIT_CUST_CLI, -- TIPO :char  > validar
			'C'  AS IND_TIT_CUST_CORR, -- TIPO :char 
			COALESCE (0 , 0    )  AS INTERES_DIA, -- TIPO :decimal
			COALESCE(CASE WHEN COALESCE(conf."Valor", 'N') = 'S' THEN OPE."MontoOperacionInicialMoneda" ELSE OPE."MontoOperacionInicial" END, 0) AS MONTO_ASIG, -- TIPO :decimal > monto contado
			COALESCE(CASE WHEN COALESCE(conf."Valor", 'N') = 'S' THEN OPE."MontoOperacionInicialMoneda" ELSE OPE."MontoOperacionInicial" END, 0) AS MONTO_ASIG_MAS, -- TIPO :decimal   > monto contado
			COALESCE(CASE WHEN COALESCE(conf."Valor", 'N') = 'S' THEN OPE."MontoOperacionInicialMoneda" ELSE OPE."MontoOperacionInicial" END, 0) AS MONTO_ASIG_MEN, -- TIPO :decimal> monto contado
			COALESCE(CASE WHEN COALESCE(conf."Valor", 'N') = 'S' THEN OPE."MontoOperacionInicialMoneda" ELSE OPE."MontoOperacionInicial" END, 0) AS MONTO_CAPITAL, -- TIPO :decimal> monto contado
			CAST(CASE WHEN OPE."IdEstado" = 'ANT' AND OPE."IdTipoOperacion" IN ('COMPAC','VENPAC') THEN COALESCE(CASE WHEN COALESCE(conf."Valor", 'N') = 'S' THEN OPE."MontoOperacionFinalMonedaAnticipo" ELSE OPE."MontoOperacionFinalAnticipo" END, 0) ELSE COALESCE(CASE WHEN COALESCE(conf."Valor", 'N') = 'S' THEN OPE."MontoOperacionFinalMoneda" ELSE OPE."MontoOperacionFinal" END, 0) END AS DECIMAL(17,2)) AS MONTO_COMPROM, -- TIPO :decimal > monto a plazo
			CAST(COALESCE (OPE."MontoOperacionInicial" , 0    ) AS DECIMAL(17,2)) AS MONTO_INI_AJUS, -- TIPO :decimal > monto compra / monto contado
			COALESCE (CASE WHEN OPE."IdEstado" = 'ANT' THEN DAYS_BETWEEN(OPE."FechaOperacion", OPE."FechaLiquidacion")
						ELSE OPE."PlazoEnDias"
						END, 0    )  AS PLAZO, -- TIPO :decimal
			0  AS SEC_COMP_ASOC, -- TIPO :decimal  
			0  AS SPREAD, -- TIPO :decimal 
			COALESCE (OPE."TasaAgente" , 0    )  AS TASA_AGE, -- TIPO :decimal
			COALESCE (OPE."Tasa" , 0    )  AS TASA_COMPROM, -- TIPO :decimal
			CASE WHEN OPE."IdEmisor" = 'CENTRAL' THEN  'CE' ELSE 'DP' END  AS TIPO_FAM, -- TIPO :char
			replace(CAST(OPE."IdTipoReajuste" AS VARCHAR(2)), 'CL' , 'NR')  AS TIPO_REAJ_COMP, -- TIPO :char  > 
			COALESCE (0 , 0    )  AS USUARIO, -- TIPO :decimal
			COALESCE (OPE."MontoOperacionInicialMoneda" , 0    )  AS VALOR_PTE, -- TIPO :decimal 
			''  AS IND_SOL_ANUL, -- TIPO :char
			COALESCE(CTA."SecuenciaMovimiento",0) as SEC_MOVTO_CTDO, -- TIPO :decimal   > validar
			COALESCE (0 , 0    )  AS SEC_MOVTO_APLAZO, -- TIPO :decimal  > validar
			''  AS COD_BOVEDA, -- TIPO :char  
			COALESCE (TO_VARCHAR(OPE."FechaCreacion", 'YYYY-MM-DD HH:MI:SS') ,'1900-01-01'   )  AS HORA, -- TIPO :char 
			COALESCE (OPE."Tasa" , 0    )  AS TASA_CAM_APLAZO, -- TIPO :decimal > tasa 
			COALESCE (OPE."Tasa" , 0    )  AS TASA_CAM_CTDO, -- TIPO :decimal
			COALESCE(CASE WHEN COALESCE(conf."Valor", 'N') = 'S' THEN OPE."MontoOperacionInicialMoneda" ELSE OPE."MontoOperacionInicial" END, 0) AS CANTIDAD_CAPITAL_H, -- TIPO :decimal   MOnto contado  
			COALESCE(CASE WHEN COALESCE(conf."Valor", 'N') = 'S' THEN OPE."MontoOperacionFinalMoneda" ELSE OPE."MontoOperacionFinal" END, 0) AS CANTIDAD_COMPROM_H, -- TIPO :decimal  monto a plazo
			COALESCE (OPE."Tasa" , 0    )   AS TASA_COMPROM_H, -- TIPO :decimal >
			COALESCE(CASE WHEN COALESCE(conf."Valor", 'N') = 'S' THEN OPE."MontoOperacionInicialMoneda" ELSE OPE."MontoOperacionInicial" END, 0) AS MONTO_CAPITAL_H, -- TIPO :decimal    MOnto contado  
			COALESCE(CASE WHEN COALESCE(conf."Valor", 'N') = 'S' THEN OPE."MontoOperacionFinalMoneda" ELSE OPE."MontoOperacionFinal" END, 0) AS MONTO_COMPROM_H, -- TIPO :decimal   monto a plazo
			COALESCE(CASE 
				WHEN OPE."IdTipoOperacion" = 'COMPAC' THEN ''
				WHEN OPE."IdTipoOperacion" = 'VENPAC' THEN ''
				WHEN CL."Codigo" = 'CN' THEN '' ELSE CL."Codigo" END,'') AS IND_LIQUID,
			COALESCE (CASE 
				WHEN OPE."IdTipoOperacion" = 'COMPAC' THEN '0001-01-01'
				WHEN OPE."IdTipoOperacion" = 'VENPAC' THEN '0001-01-01'
				ELSE CAST(OPE."FechaCondicionLiquidacion" AS VARCHAR(10)) END,'1900-01-01')  AS FEC_IND_LIQUID, -- TIPO :char
			0  AS FOLIO_OPE_INI, -- TIPO :char
			0  AS FOLIO_OPE_FIN, -- TIPO :char
			'0001-01-01:00:00:00.000000'  AS FEC_ANULACION, -- TIPO :char 
			COALESCE (CAST('' AS VARCHAR(100)),''   )  AS USR_ANULACION, -- TIPO :char  
			'0' AS CODCATIFRSID -- TIPO :tinyint
		
		
		FROM "BSCL.Mesa.BD::MES.Operacion" OPE
		INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON OPE."IdCliente" = CLI."IdCliente"
		INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON CLI."IdPersona" = PER."IdPersona"
		INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
		INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdCliente" = CLI."IdCliente" AND por."IdPortafolio"  = OPE."IdPortafolio"
		INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS fec ON (fec."IdEmpresa" = 1 and "CodigoParametro" = 'PLA.FechaSistema')
		LEFT JOIN "BSCL.Replica.BD::ConfiguracionReplica" AS conf ON (conf."IdEmpresa" = 1 AND conf."CodigoParametro" = 'TBCPXXPC_Montos_en_USD')
		
		LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" EMP ON POR."IdEmpleadoEjecutivo" = EMP."IdEmpleado"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" COR ON COR."IdPortafolio"  = OPE."IdPortafolioCorredor"
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLICOR ON COR."IdCliente" = CLICOR."IdCliente"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PERCOR ON CLICOR."IdPersona" = PERCOR."IdPersona"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" IDENTCOR ON IDENTCOR."IdDocumentoIdentidad" = PERCOR."IdDocumentoIdentidad"
		LEFT JOIN (
			SELECT "IdEmpleado", CASE WHEN RANK > 1 THEN CONCAT(RPAD("CodigoEmpleado",3),RANK) ELSE "CodigoEmpleado" END AS "CodigoEmpleado" FROM (
				SELECT RANK() OVER (PARTITION BY RPAD("CodigoEmpleado",4) ORDER BY "CodigoEmpleado" ASC)  as RANK, "IdEmpleado", RPAD("CodigoEmpleado",4) AS "CodigoEmpleado" FROM "BSCL.SocioNegocio.BD::EMP.Empleado"
			) WHERE RANK < 10
		) AS COD_EMP_TRUN4 ON COD_EMP_TRUN4."IdEmpleado" = EMP."IdEmpleado"
		LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" MES ON MES."IdMesa" = OPE."IdMesaOrigen"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" CL ON  CL."IdCondicionDeLiquidacion" = OPE."IdCondicionLiquidacion"
		LEFT JOIN (SELECT res."IdOperacion", count(1) AS CAN_LIN FROM "BSCL.Custodia.BD::CUS.Reserva" RES GROUP BY res."IdOperacion") RES ON RES."IdOperacion" = OPE."IdOperacion"
		
		LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" CTA ON CTA."IdComprobante" = OPE."IdOperacion" AND CTA."IdModulo" = 'MRF'
		AND CASE OPE."IdTipoOperacion" WHEN 'VENPAC' THEN 'VNPCME' 
										WHEN 'COMPAC' THEN 'CPPCME' 
										WHEN 'COMTER' THEN 'CPTRME' 
										WHEN 'VENTER' THEN 'VNTRME' 
									END = CTA."IdConcepto"
		AND CASE WHEN OPE."IdTipoOperacion" in ('VENPAC','VENTER') THEN 'MOVABN' ELSE 'MOVCAR' END = CTA."IdTipoMovimiento" AND CTA."IdEstado" = 'CRT'
		
		WHERE 
		COR."CodigoPortafolio" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R') AND 
		POR."CodigoPortafolio" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R') AND 
		OPE."IdTipoProducto" <> 'MEFW' and COALESCE (OPE."FolioOperacion", 0) <> 0
		AND (
			(OPE."IdTipoOperacion" IN ('COMPAC','VENPAC') AND 
				(CASE OPE."IdTipoOperacion" 
						WHEN 'COMPAC' THEN OPE."FechaVencimiento"
						WHEN 'VENPAC' THEN OPE."FechaVencimiento" 
						WHEN 'COMTER' THEN OPE."FechaOperacion" 
						WHEN 'VENTER' THEN OPE."FechaOperacion"
						ELSE OPE."FechaLiquidacion"
						END >= LEFT(fec."Valor",7))
			) OR (
				OPE."IdTipoOperacion" IN ('COMTER','VENTER') AND LEFT(fec."Valor",7) = LEFT(OPE."FechaOperacion",7)
			)
		)
		) UNION ALL
	SELECT 
		COD_PER, SEC_COMPROM, RUT_CORR, SEC_RUT_CORR, TIPO_COMPROM, RUT_CLI, SEC_RUT_CLI, FEC_LIQUID, FEC_OPERAC, CAN_LIN, CANTIDAD_CAPITAL, 
		CANTIDAD_COMPROM, COD_AGENTE, COD_BCO_PAGO, COD_BCO_RECIBO, COD_MESA, COD_SUC, CTA_BCO_PAGO, CTA_BCO_RECIBO, DONDE_PAGO, 
		DONDE_RECIBO, ESTADO, FEC_VCTO_COM, FOLIO_DOC_ADJ, FORMA_PAGO, FORMA_RECIBO, GLOSA, IND_EMI_CERTIF, IND_PAGADO, 
		IND_TIT_CUST_CLI, IND_TIT_CUST_CORR, INTERES_DIA, MONTO_ASIG, MONTO_ASIG_MAS, MONTO_ASIG_MEN, MONTO_CAPITAL, 
		MONTO_COMPROM, MONTO_INI_AJUS, PLAZO, SEC_COMP_ASOC, SPREAD, TASA_AGE, TASA_COMPROM, TIPO_FAM, TIPO_REAJ_COMP, 
		USUARIO, VALOR_PTE, IND_SOL_ANUL, SEC_MOVTO_CTDO, SEC_MOVTO_APLAZO, COD_BOVEDA, HORA, TASA_CAM_APLAZO, TASA_CAM_CTDO, 
		CANTIDAD_CAPITAL_H, CANTIDAD_COMPROM_H, TASA_COMPROM_H, MONTO_CAPITAL_H, MONTO_COMPROM_H, IND_LIQUID, FEC_IND_LIQUID, 
		0 AS FOLIO_OPE_INI, 0 AS FOLIO_OPE_FIN, FEC_ANULACION, USR_ANULACION, CODCATIFRSID
		FROM "BSCL.Replica.BD::MIG.TBCPMSPC" MSPC_SEBRA;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBCPMSPC" TO  BCS_DESARROLLADOR WITH GRANT OPTION;