SET SCHEMA "BSCL";
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCPDTPC";

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCPDTPC" AS
SELECT * FROM (

	SELECT 
	MAX(COD_PER) AS COD_PER,
	SEC_COMPROM AS SEC_COMPROM,
	LIN_COMPROM AS LIN_COMPROM,
	MAX(EMISOR) AS EMISOR,
	MAX(FEC_LIQUID) AS FEC_LIQUID,
	TIPO_REAJ AS TIPO_REAJ,
	NEMO AS NEMO,
	MIN(SEC_CAR_CORR) AS SEC_CAR_CORR,
	CAST(CASE WHEN LIN_COMPROM = 1 THEN MAX(CANT_PC) ELSE 0 END AS DECIMAL(15,4)) AS CANT_PC,
	CAST(SUM(CANT_PC_COMPROM) AS DECIMAL(15,4)) AS CANT_PC_COMPROM,
	CAST(SUM(CANTIDAD) AS DECIMAL(14,2)) AS CANTIDAD,
	MIN(COD_CLASIF_RIESGO) AS COD_CLASIF_RIESGO,
	MAX(EST_LIN_COMPROM) AS EST_LIN_COMPROM,
	MAX(FEC_EMI) AS FEC_EMI,
	MAX(FEC_VCTO) AS FEC_VCTO,
	MAX(IND_COBERTURA) AS IND_COBERTURA,
	MAX(IND_CORTES) AS IND_CORTES,
	MERCADO AS MERCADO,
	MAX(MONTO_ACUM_CUP) AS MONTO_ACUM_CUP,
	SUM(MONTO_CARTERA) AS MONTO_CARTERA,
	MAX(MONTO_CARTERA_MAS) AS MONTO_CARTERA_MAS,
	MAX(MONTO_CARTERA_MEN) AS MONTO_CARTERA_MEN,
	SUM(MONTO_COMPROM) AS MONTO_COMPROM,
	SUM(MONTO_INI_CAP) AS MONTO_INI_CAP,
	SUM(MONTO_INI_CAP_MAS) AS MONTO_INI_CAP_MAS,
	SUM(MONTO_INI_CAP_MEN) AS MONTO_INI_CAP_MEN,
	MAX(PRECIO) AS PRECIO,
	RUT_CLI AS RUT_CLI,
	MIN(SEC_CAR_CLI) AS SEC_CAR_CLI,
	SEC_RUT_CLI AS SEC_RUT_CLI,
	MAX(TASA_COMPROM) AS TASA_COMPROM,
	MAX(TIPO_FAM) AS TIPO_FAM,
	MAX(TIR) AS TIR,
	SUM(VAL_RESC) AS VAL_RESC,
	CAST(SUM(VALOR_PTE) AS DECIMAL(17,2)) AS VALOR_PTE,
	MAX(COD_BOVEDA) AS COD_BOVEDA,
	MAX(DURATION) AS DURATION,
	MAX(TIPO_CARTERA) AS TIPO_CARTERA,
	MAX(IND_FUNGIBILIDAD) AS IND_FUNGIBILIDAD,
	MAX(MONTO_INI_CAP_TOT) AS MONTO_INI_CAP_TOT,
	MAX(INTRADAY) AS INTRADAY,
	MAX(TASA_SELECCIONADA) AS TASA_SELECCIONADA,
	MAX(SEC_DETALLE) AS SEC_DETALLE
	FROM (
		SELECT
				LEFT(fec."Valor",7) AS COD_PER, -- TIPO :char
				res."IdReserva",
				o."IdOperacion" AS SEC_COMPROM, -- TIPO :decimal 
				RANK() OVER (PARTITION BY res."IdOperacion" ORDER BY res."IdOperacion", res."IdReserva", res."Nemotecnico", res."FechaVencimiento" )   AS LIN_COMPROM, -- TIPO :decimal 
				res."IdEmisor" 	 AS EMISOR, -- TIPO :char -- 
				TO_VARCHAR(CASE o."IdTipoOperacion" 
					WHEN 'COMPAC' THEN CASE WHEN o."IdEstado" = 'ANT' THEN o."FechaLiquidacion" ELSE o."FechaVencimiento" END
					WHEN 'VENPAC' THEN CASE WHEN o."IdEstado" = 'ANT' THEN o."FechaLiquidacion" ELSE o."FechaVencimiento" END
					WHEN 'COMTER' THEN o."FechaOperacion" 
					WHEN 'VENTER' THEN o."FechaOperacion"
					ELSE o."FechaLiquidacion"
					END
				, 'YYYY-MM-DD')  AS FEC_LIQUID, -- TIPO :char 
				replace(CAST(RES."IdTipoReajuste" AS VARCHAR(2)), 'CL' , 'NR') AS TIPO_REAJ, -- TIPO :char
				COALESCE (CAST(RES."Nemotecnico" AS VARCHAR(100)),''   )  AS NEMO, -- TIPO :char
				COALESCE (RES."FolioCarteraCustodia" , 0    )  AS SEC_CAR_CORR, -- TIPO :decimal
				--REs."CantidadInicial"   AS CANT_PC, -- TIPO :decimal  
				CASE o."IdTipoOperacion" 
			    WHEN 'COMPAC' THEN COALESCE(o."MontoOperacionMoneda",0)
			    WHEN 'VENPAC' THEN COALESCE(o."MontoOperacionMoneda",0)
			    ELSE 0 
			    END AS CANT_PC, -- TIPO :decimal
				(CASE o."IdTipoOperacion" 
					WHEN 'VENPAC' THEN 
				    	CASE WHEN TRIM(RES."Nemotecnico") = 'PDBC' AND O."IdEstado" = 'ANT' THEN CAR_CLI."EGRMonto" / COALESCE(CAR_CLI."EGRCantidad", 1) 
				    		WHEN TRIM(RES."Nemotecnico") != 'PDBC' AND O."IdEstado" = 'ANT' THEN CAR_CLI."EGRMonto" 
				    		WHEN TRIM(RES."Nemotecnico") = 'PDBC' AND O."IdEstado" != 'ANT' THEN RES."INGMontoAPlazo" / COALESCE(RES."IngCantidad", 1) 
				    		ELSE RES."INGMontoAPlazo" END
			    	WHEN 'COMPAC' THEN 
			    		CASE WHEN TRIM(RES."Nemotecnico") = 'PDBC' AND O."IdEstado" = 'ANT' THEN CAR_CLI."EGRMonto" / COALESCE(CAR_CLI."EGRCantidad", 1) 
				    		WHEN TRIM(RES."Nemotecnico") != 'PDBC' AND O."IdEstado" = 'ANT' THEN CAR_CLI."EGRMonto" 
				    		WHEN TRIM(RES."Nemotecnico") = 'PDBC' AND O."IdEstado" != 'ANT' THEN RES."INGMontoAPlazo" / COALESCE(RES."IngCantidad", 1) 
				    		ELSE RES."INGMontoAPlazo" END
					ELSE RES."MontoLiquidacionOperacion" 
				END) / COALESCE(dolar."Valor", 1) AS CANT_PC_COMPROM, -- TIPO :decimal  
				RES."CantidadInicial"   AS CANTIDAD, -- TIPO :decimal  
				0  AS COD_CLASIF_RIESGO, -- TIPO :decimal
				'I'  AS EST_LIN_COMPROM, -- TIPO :char
				'0001-01-01'  AS FEC_EMI, -- TIPO :char
				TO_VARCHAR (CASE WHEN res."FechaVencimiento" < '1900-01-01' THEN '1900-01-01' ELSE res."FechaVencimiento" end ,'YYYY-MM-DD') AS FEC_VCTO, -- TIPO :char
				''  AS IND_COBERTURA, -- TIPO :char
				CASE WHEN res."IdClasificacionTipoInstrumento" = 'RF' THEN 'N' ELSE '' END AS IND_CORTES, -- TIPO :char	
				res."IdClasificacionTipoInstrumento"  AS MERCADO, -- TIPO :char  
				TO_DECIMAL(COALESCE (CAR_PAC."INGMontoCorteCupon", COALESCE (CAR_CVT."INGMontoCorteCupon", 0    )),17,2)  AS MONTO_ACUM_CUP, -- TIPO :decimal
				CASE o."IdTipoOperacion" 
				WHEN 'VENPAC' THEN CASE WHEN TRIM(RES."Nemotecnico") = 'PDBC' THEN RES."MontoValorizado" / COALESCE(RES."IngCantidad", 1) ELSE RES."MontoValorizado" END
			    ELSE 0 
			    END AS MONTO_CARTERA, -- TIPO :decimal 
				COALESCE (CAR_PAC."MontoCompra" ,COALESCE (CAR_CVT."MontoCompra" , 0    ))  AS MONTO_CARTERA_MAS, -- TIPO :decimal  
				COALESCE (CAR_PAC."MontoCompra" ,COALESCE (CAR_CVT."MontoCompra" , 0    ))  AS MONTO_CARTERA_MEN, -- TIPO :decimal 
				(CASE o."IdTipoOperacion" 
				    WHEN 'VENPAC' THEN 
				    	CASE WHEN TRIM(RES."Nemotecnico") = 'PDBC' AND O."IdEstado" = 'ANT' THEN CAR_CLI."EGRMonto" / COALESCE(CAR_CLI."EGRCantidad", 1) 
				    		WHEN TRIM(RES."Nemotecnico") != 'PDBC' AND O."IdEstado" = 'ANT' THEN CAR_CLI."EGRMonto" 
				    		WHEN TRIM(RES."Nemotecnico") = 'PDBC' AND O."IdEstado" != 'ANT' THEN RES."INGMontoAPlazo" / COALESCE(RES."IngCantidad", 1) 
				    		ELSE RES."INGMontoAPlazo" END
			    	WHEN 'COMPAC' THEN 
			    		CASE WHEN TRIM(RES."Nemotecnico") = 'PDBC' AND O."IdEstado" = 'ANT' THEN CAR_CLI."EGRMonto" / COALESCE(CAR_CLI."EGRCantidad", 1) 
				    		WHEN TRIM(RES."Nemotecnico") != 'PDBC' AND O."IdEstado" = 'ANT' THEN CAR_CLI."EGRMonto" 
				    		WHEN TRIM(RES."Nemotecnico") = 'PDBC' AND O."IdEstado" != 'ANT' THEN RES."INGMontoAPlazo" / COALESCE(RES."IngCantidad", 1) 
				    		ELSE RES."INGMontoAPlazo" END
				    ELSE RES."MontoLiquidacion"
			    END) / COALESCE(dolar."Valor", 1) AS MONTO_COMPROM, -- TIPO :decimal 
				COALESCE (RES."IngMontoValorizado" , 0    )  AS MONTO_INI_CAP, -- TIPO :decimal  
				COALESCE (RES."IngMontoValorizado" , 0    )  AS MONTO_INI_CAP_MAS, -- TIPO :decimal  
				COALESCE (RES."IngMontoValorizado" , 0    )  AS MONTO_INI_CAP_MEN, -- TIPO :decimal  
				COALESCE (RES."Precio",0)  AS PRECIO, -- TIPO :decimal
				COALESCE (CAST("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(CAST(IDENT."DocumentoIdentidad" AS VARCHAR(11))) AS VARCHAR(100)),''   )  AS RUT_CLI, -- TIPO :char
				COALESCE (CAR_PAC."IdCartera" ,COALESCE (CAR_CVT."IdCartera" , 0    ))  AS SEC_CAR_CLI, -- TIPO :decimal
				COALESCE (CAST(POR."CodigoPortafolio" AS VARCHAR(100)), '')  AS SEC_RUT_CLI, -- TIPO :decimal
				COALESCE (RES."TasaMercado", 0    )  AS TASA_COMPROM, -- TIPO :decimal   
				--CASE WHEN RES."IdEmisor" = 'CENTRAL' THEN  'CE' ELSE 'DP' END  AS TIPO_FAM, -- TIPO :char
				CASE 
					WHEN res."IdTipoInstrumento" = 'PAG' THEN 'DP'
					WHEN res."IdTipoInstrumento" = 'BEM' THEN 'BE'
					WHEN res."IdTipoInstrumento" = 'LTH' THEN 'LH'
					WHEN res."IdTipoInstrumento" = 'BBC' THEN 'BE'
					WHEN res."IdTipoInstrumento" = 'BTU' THEN 'CE'
					ELSE 'DP'
				END  AS TIPO_FAM, -- TIPO :char
				COALESCE (RES."Tasa", 0    )  AS TIR, -- TIPO :decimal  --
				COALESCE(RES."IngValorRescate", 0) / COALESCE(dolar."Valor", 1) AS VAL_RESC, -- TIPO :decimal
				COALESCE(RES."MontoValorizadoReserva", 0) / COALESCE(dolar."Valor", 1) AS VALOR_PTE, -- TIPO :decimal -- 
				COALESCE (CAST(RES."IdBoveda" AS VARCHAR(100)),''   )  AS COD_BOVEDA, -- TIPO :char
				CASE WHEN COALESCE (CAR_PAC."INGDurationCompraActual" ,COALESCE (CAR_CVT."INGDurationCompraActual" , 0    )) > 999 THEN 999
				    WHEN COALESCE (CAR_PAC."INGDurationCompraActual" ,COALESCE (CAR_CVT."INGDurationCompraActual" , 0    )) < -999 THEN 0
					ELSE COALESCE (CAR_PAC."INGDurationCompraActual" ,COALESCE (CAR_CVT."INGDurationCompraActual" , 0    )) END  AS DURATION, -- TIPO :decimal  --   INGDurationSubyacenteActual
				COALESCE (CAST('' AS VARCHAR(100)),''   )  AS TIPO_CARTERA, -- TIPO :char
				COALESCE (CAST(RES."EsFungible" AS VARCHAR(100)),''   )  AS IND_FUNGIBILIDAD, -- TIPO :char
				COALESCE (CAR_PAC."MontoCompra" ,COALESCE (CAR_CVT."MontoCompra" , 0    ))  AS MONTO_INI_CAP_TOT, -- TIPO :decimal  
				COALESCE (CAST(CAR_PAC."EsIntraday" AS VARCHAR(100)),COALESCE (CAST(CAR_CVT."EsIntraday" AS VARCHAR(100)),''   ))  AS INTRADAY, -- TIPO :char
				''  AS TASA_SELECCIONADA, -- TIPO :char  
				COALESCE (RES."IdReserva" , 0    )  AS SEC_DETALLE -- TIPO :decimal
			--SELECT  CAR."IdTipoInstrumento" ,* 
			FROM "BSCL.Custodia.BD::CUS.Reserva" RES
			INNER JOIN  "BSCL.Mesa.BD::MES.Operacion" o ON (res."IdOperacion" = o."IdOperacion")
			LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR_PAC ON o."FolioCartera" = CAR_PAC."FolioCartera"
			LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR_CVT ON RES."IdCarteraOrigen" = CAR_CVT."IdCartera"
			LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR_CLI ON RES."IdCarteraCustodia" = CAR_CLI."IdCartera" AND O."IdEstado" = 'ANT'
			LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON o."IdCliente" = CLI."IdCliente"
			LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON CLI."IdPersona" = PER."IdPersona"
			LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdCliente" = CLI."IdCliente" AND  o."IdPortafolio" = POR."IdPortafolio"
			INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS fec ON (fec."IdEmpresa" = 1 and "CodigoParametro" = 'PLA.FechaSistema')
			LEFT JOIN (
				SELECT CASE WHEN COALESCE(MAX(conf_rep."Valor"), 'N') = 'S' THEN COALESCE(MAX(dol."Valor"), 1) ELSE 1 END AS "Valor"
				FROM "BSCL.Replica.BD::ConfiguracionReplica" conf_rep
				LEFT JOIN (
					SELECT TOP 1 f."Valor"
					FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" f
					LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" conf ON conf."CodigoParametro" = 'PLA.FechaSistema'
					WHERE f."IdParametroFinanciero" = 'DO'
					AND f."Fecha" <= conf."Valor"
					ORDER BY f."Fecha" DESC
				) dol ON 1 = 1
				WHERE conf_rep."IdEmpresa" = 1 AND conf_rep."CodigoParametro" = 'TBCPXXPC_Montos_en_USD'
			) AS dolar ON o."IdMoneda" = 'USD'
			WHERE POR."CodigoPortafolio" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R') AND
			RES."IdClasificacionTipoInstrumento" IN ('IF' , 'RF') AND COALESCE (o."FolioOperacion", 0) <> 0
			AND (
				(o."IdTipoOperacion" IN ('COMPAC','VENPAC') 
					AND 
					(CASE o."IdTipoOperacion" 
							WHEN 'COMPAC' THEN o."FechaVencimiento"
							WHEN 'VENPAC' THEN o."FechaVencimiento" 
							WHEN 'COMTER' THEN o."FechaOperacion" 
							WHEN 'VENTER' THEN o."FechaOperacion"
							ELSE o."FechaLiquidacion"
							END >= LEFT(fec."Valor",7))
					AND o."IdEstado" <> 'ANU') 
				OR (
					o."IdTipoOperacion" IN ('COMTER','VENTER') AND LEFT(fec."Valor",7) = LEFT(o."FechaOperacion",7)
				)
			)
			--operaciones del mes o pactos vigentes a la fecha de sistema
		)
		GROUP BY SEC_COMPROM, LIN_COMPROM, TIPO_REAJ, NEMO, MERCADO, RUT_CLI, SEC_RUT_CLI
		
		UNION ALL
		
		SELECT COD_PER, SEC_COMPROM, LIN_COMPROM, EMISOR, FEC_LIQUID, TIPO_REAJ, NEMO, SEC_CAR_CORR, CANT_PC, CANT_PC_COMPROM, CANTIDAD, COD_CLASIF_RIESGO, EST_LIN_COMPROM, FEC_EMI, FEC_VCTO, IND_COBERTURA, IND_CORTES, MERCADO, MONTO_ACUM_CUP, MONTO_CARTERA, MONTO_CARTERA_MAS, MONTO_CARTERA_MEN, MONTO_COMPROM, MONTO_INI_CAP, MONTO_INI_CAP_MAS, MONTO_INI_CAP_MEN, PRECIO, RUT_CLI, SEC_CAR_CLI, SEC_RUT_CLI, TASA_COMPROM, TIPO_FAM, TIR, VAL_RESC, VALOR_PTE, COD_BOVEDA, DURATION, TIPO_CARTERA, IND_FUNGIBILIDAD, MONTO_INI_CAP_TOT, INTRADAY, TASA_SELECCIONADA, SEC_DETALLE
		FROM "BSCL.Replica.BD::MIG.TBCPDTPC"		
);
	
GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBCPDTPC" TO  BCS_DESARROLLADOR WITH GRANT OPTION;