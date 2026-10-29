SET SCHEMA "BSCL";
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTCIIF";

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTCIIF" AS

SELECT
		max(CLI."IdCliente") AS "IdCliente",
		LEFT(fec."Valor",7) AS COD_PER
		,fec."Valor" AS "FechaSistema"
		,SCUST."FolioCustodia" AS SEC_CAR_INVER
		,COALESCE("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(IDENT."DocumentoIdentidad"),'0-0') AS RUT_CLI
		,POR."CodigoPortafolio" AS SEC_RUT_CLI
		,max(SCUST."IdEmisor") AS EMISOR
		,max(replace(CAST(SCUST."IdTipoReajuste" AS VARCHAR(2)), 'CL' , 'NR'))  AS TIPO_REAJ
		,max(TO_VARCHAR("FechaVencimientoInstrumento", 'YYYY-MM-DD')) AS FEC_VCTO
		,sum(SCUST."MontoCartera") AS MONTO_VAL_DISP
		,max(CASE WHEN SCUST."IdEmisor" = 'CENTRAL' THEN  'CE' ELSE 'DP' END) AS TIPO_FAM
		,TO_DECIMAL(max(CASE WHEN SCUST."Nemotecnico" = 'PDBC' THEN SCUST."Cantidad" WHEN SCUST."Cantidad" != 0 THEN 1 ELSE 0 END),8,0)   AS CAN_ACTUAL --  "Cantidad"
		,max(CASE WHEN SCUST."IdBoveda" = '' THEN 'DCV' ELSE SCUST."IdBoveda" END ) AS COD_BOVEDA
		, 0.0 AS CAN_TRAMITE_COM
		,max(CASE WHEN SCUST."Nemotecnico" = 'PDBC' THEN SCUST."Cantidad_Compra_Plazo" WHEN SCUST."Cantidad_Compra_Plazo" != 0 THEN 1 ELSE 0 END)  AS CAN_COM_PLAZO
		,sum("CantidadGarantia")  AS CAN_GARANTIA --SCUST."Cantidad_Garantia"
		, 0.0 AS CAN_ORD_VEN
		, 0.0 AS CAN_TRAMITE_VEN
		,'' AS COD_RESERVA
		,max(TO_VARCHAR(SCUST."FechaEmisionInstrumento", 'YYYY-MM-DD')) AS FEC_EMI
		,min(TO_VARCHAR(SCUST."FechaOperacion", 'YYYY-MM-DD'))  AS FEC_INVER
		,'' AS FEC_RESERVA
		,max(TO_VARCHAR("FechaVencimientoInstrumento", 'YYYY-MM-DD')) AS FEC_VCTO_COM
		,min(SCUST."FolioComprobante" ) AS FOLIO_COMP_ADJ
		, 0.0 AS INT_ACUM_GAN_CP
		, 0.0 AS INT_ACUM_GAN_VP
		, sum(SCUST."MontoCartera")  AS INT_DIA_INV_DIS
		, COALESCE(ROW_NUM,0) AS LIN_COMP_ADJ
		,TO_DECIMAL(sum(SCUST."MontoCartera"),17,2) AS MONTO_ACT_AJUS
		,TO_DECIMAL(sum(SCUST."MontoCompra"),17,2) AS MONTO_INI_AJUS
		, 0.0 AS MONTO_VAL_DISP_MAS
		, 0.0 AS MONTO_VAL_DISP_MEN
		,trim(CAST(SCUST."Nemotecnico" AS VARCHAR(16))) AS NEMO
		,max(CASE 
			WHEN  ABS(DAYS_BETWEEN ("FechaOperacion", "FechaVencimientoInstrumento")) > 9999 THEN 9999 
			ELSE  ABS(DAYS_BETWEEN ("FechaOperacion", "FechaVencimientoInstrumento")) END)  AS PLAZO
		, 0.0 AS QUIEN_RESERVA
		, max("TasaMercado") AS TASA_MED_CIE
		, max("TasaCompra") AS TASA_PROM_COMP
		, "TipoCartera" AS TIPO_CARTERA
		,max(CASE SCUST."IdTipoComprobante" WHEN 'CUS' THEN 'IT' WHEN 'FAE' THEN 'FD' WHEN 'MES' THEN 'MA' ELSE 'IT' END) AS TIPO_COMP_ADJ
		,sum(SCUST."ValorRescate") AS VAL_RESC
		,min(TO_VARCHAR(SCUST."FechaOperacion", 'YYYY-MM-DD')) AS VTO_COMP_ADJ
		,CAST(SCUST."NemoSVS" AS VARCHAR(16)) AS NEMO_SVS
		,max(COALESCE( CAST(TRIM(IDENTEMI."DocumentoIdentidad") AS VARCHAR(11)) , '')) AS RUT_EMISOR
		, 0.0 AS SEC_CAR_HISTO
		, 0.0 AS SEC_CAR_ORIGEN
		,sum(SCUST."ValorRescate_Compra_Plazo") AS VAL_RESC_PLAZO
		,max(CAST(SCUST."EsFungible" AS VARCHAR(1))) AS IND_FUNGIBILIDAD
		, max("TasaCompra") AS TASA_EMISION
		, 0.0 AS SPREAD_TASA_EMISION
		,'' AS MODALIDAD_PAGO
		,'' AS PERIODICIDAD_PAGO
		,max(TO_VARCHAR(SCUST."FechaEmisionInstrumento", 'YYYY-MM-DD')) AS FECHA_EMISION
		,'' AS ORIGEN_RECURSOS
		,'' AS CLASIF_CARTERA -- SE DEBE HOMOLOGAR SCUST."IdClasificacionTipoInstrumento" CHAR (1)
		, 0.0 AS CODCATIFRSID -- SE DEBE HOMOLOGAR SCUST."CodigoIFRS" tinyint
		, max("ValorParidadCierre") AS PARIDAD_MORIGEN  
		, sum("MontoCompraMonedaInstrumento") AS CAPTACION_MORIGEN  
		, max("IdMonedaInstrumento") AS MON_MORIGEN   
		, max("INGTasaMercadoAnterior") AS TASA_MED_CIE_ANT  
		, sum("INGMontoATasaMercadoAnterior") AS MTO_VAL_DISP_ANT 
		, 0.0 AS CAPT_MORIGEN_MCDO 
		, 0.0 AS VAL_RESC_DIF_ORIG
		, 0.0 AS MTO_VAL_TASA_MCDO
		, 0.0 AS MTO_VAL_TASA_COMP
		, 0.0 AS PRECIO_PROM_COMP
		,max("EsFungible") AS COD_FUNGIBLE  
		,'' AS COD_EMISION
		,'' AS COD_TITULO
		,0.0 AS PRECIO_MED_CIE
	FROM (
		--CARTERA LIBRE
		SELECT 
				"FolioCartera" AS "FolioCustodia",
				"FechaOperacion",
				"IdCliente",
				"IdPortafolio",
				"IdTipoInstrumento",
				"IdInstrumento",
				"IdEmisor",
			    "Nemotecnico",
			    "NemoSVS",
			    "IdBoveda",
			    "IdTipoReajuste",
			    "PlazoEnDiasOperacion",
			    "EsFungible",
			    "IdClasificacionTipoInstrumento",
			    "CodigoIFRS",
			    "FechaEmisionInstrumento",
			    "FechaVencimientoInstrumento",
			    "IdTipoComprobante",
			    "FolioComprobante",
			    "FechaComprobante",
				CASE WHEN "Nemotecnico" = 'PDBC' THEN "INGValorRescate"/5000000 ELSE "INGCantidad" END AS "Cantidad",
				CASE WHEN "Nemotecnico" = 'PDBC' THEN 5000000 ELSE "INGValorRescate" END AS "ValorRescate",
				CASE WHEN "Nemotecnico" = 'PDBC' THEN "INGMontoATasaCompraActual"/("INGValorRescate"/5000000) ELSE "INGMontoATasaCompraActual" END AS "MontoCompra",
			    "INGTasaCompra" AS "TasaCompra", 
			    "INGTasaMercadoActual" AS "TasaMercado",
				--INGMontoATasaMercadoActual" AS "MontoCartera",
				CASE WHEN "Nemotecnico" = 'PDBC' THEN "INGMontoATasaMercadoActual"/("INGValorRescate"/5000000) ELSE "INGMontoATasaMercadoActual" END  AS "MontoCartera",
				"ValorParidadCierre",
				"MontoCompraMonedaInstrumento",
				"IdMonedaInstrumento",
				"INGTasaMercadoAnterior",
				"INGMontoATasaMercadoAnterior",
				0 AS "Cantidad_Compra_Plazo",
			    0 AS "ValorRescate_Compra_Plazo",
			    0 AS "Cantidad_Venta_Plazo",
			    0 AS "ValorRescate_Venta_Plazo",
			    0 AS "CantidadGarantia",
			    CASE WHEN "IdTipoOperacion" = 'VENPAC' THEN 'CC' ELSE '' END  AS "TipoCartera",
			    linea.ROW_NUM AS ROW_NUM
			FROM "BSCL.Custodia.BD::CUS.Cartera" car
			left join (
				select ROW_NUMBER() OVER (PARTITION BY "IdOperacion" ORDER BY "IdReserva") AS ROW_NUM,
				"IdReserva","IdOperacion" 
				from "BSCL.Custodia.BD::CUS.Reserva" 
				) as linea on linea."IdReserva" = car."IdOperacion"
			WHERE "IdEstado" = 'VIG'
			  AND "IdClasificacionTipoInstrumento" = 'IF' AND "IdTipoInstrumento" NOT IN ('SIM','PAC','PRT','EMP','FFM')-- AND "TipoCarteraCorredor"  != 'CC' 
			  AND "FolioCartera"  != 0
			UNION ALL
			--VENTA A PLAZO
			SELECT --C."IdTipoOperacion",
				s."FolioSubyacente" AS "FolioCustodia",
				s."INGFechaOperacion",
				c."IdCliente",
				c."IdPortafolio",
				s."IdTipoInstrumento",
				s."IdInstrumento",
				s."IdEmisor",
				s."Nemotecnico",
				s."NemoSVS",
				'' AS "IdBoveda",
				s."IdTipoReajuste",
				c."PlazoEnDiasOperacion",
				s."EsFungible",
				s."IdClasificacionTipoInstrumento",
				'' AS "CodigoIFRS",
				s."FechaEmisionInstrumento",
				s."FechaVencimientoInstrumento",
				c."IdTipoComprobante",
--			   CASE WHEN COALESCE ("FolioExternoReferencia",0) = 0 THEN  c."FolioComprobante" ELSE c."FolioExternoReferencia" END AS "FolioComprobante",
				"FolioComprobante",
				c."FechaComprobante",
				CASE WHEN C."IdTipoOperacion" in ('VENPAC','COMPAC') THEN CASE WHEN S."Nemotecnico" = 'PDBC' THEN S."INGValorRescate"/5000000 ELSE S."INGCantidad" END ELSE 0  END  AS "Cantidad", ---"INGCantidad"
				CASE WHEN C."IdTipoOperacion" in ('VENPAC','COMPAC') THEN CASE WHEN S."Nemotecnico" = 'PDBC' THEN 5000000 ELSE S."INGValorRescate" END ELSE 0 END  AS "ValorRescate",	--"INGValorRescate"
				CASE WHEN S."Nemotecnico" = 'PDBC' THEN s."INGMontoATasaCompraActual"/(s."INGValorRescate"/5000000) ELSE s."INGMontoATasaCompraActual" END AS "MontoCompra",
				c."INGTasaCompra" AS "TasaCompra", 
				s."INGTasaMercadoActual" AS "TasaMercado",
				CASE WHEN S."Nemotecnico" = 'PDBC' THEN S."INGMontoATasaSubyacenteActual"/(S."INGValorRescate"/5000000) ELSE S."INGMontoATasaSubyacenteActual" END  AS "MontoCartera",
				c."ValorParidadCierre",
				s."INGMontoCompraActualMonedaInstrumento" AS "MontoCompraMonedaInstrumento",
				s."IdMonedaInstrumento",
				0 AS "INGTasaMercadoAnterior",
				0 AS "INGMontoATasaMercadoAnterior",
			   CASE WHEN C."IdTipoOperacion"  in ('VENSIM','VENPAC')THEN 0 ELSE CASE WHEN S."Nemotecnico" = 'PDBC' THEN S."INGValorRescate"/5000000 ELSE S."INGCantidad" END END AS "Cantidad_Compra_Plazo",
			   CASE WHEN C."IdTipoOperacion"  in ('VENSIM','VENPAC')  THEN 0 ELSE CASE WHEN S."Nemotecnico" = 'PDBC' THEN 5000000 ELSE S."INGValorRescate" END END AS "ValorRescate_Compra_Plazo",
			   CASE WHEN C."IdTipoOperacion"  in ('COMSIM','COMPAC')  THEN 0 ELSE CASE WHEN S."Nemotecnico" = 'PDBC' THEN S."INGValorRescate"/5000000 ELSE S."INGCantidad" END END AS "Cantidad_Venta_Plazo",
			   CASE WHEN C."IdTipoOperacion" in ('COMSIM','COMPAC') THEN 0 ELSE CASE WHEN S."Nemotecnico" = 'PDBC' THEN 5000000 ELSE S."INGValorRescate" END END AS "ValorRescate_Venta_Plazo",
			   0 AS "CantidadGarantia",
			   '' AS "TipoCartera",
			   linea.ROW_NUM as "Linea"
			FROM "BSCL.Custodia.BD::CUS.Subyacente" as S 
			left JOIN "BSCL.Custodia.BD::CUS.Cartera" as C on S."FolioCarteraRelacionada" = C."FolioCartera" and C."EsPrimeraCartera" = 'S'
			LEFT join (
				select ROW_NUMBER() OVER (PARTITION BY "IdOperacion" ORDER BY "IdReserva") AS ROW_NUM,
				"IdCarteraOrigen","IdOperacion","FolioSubyacente" from "BSCL.Custodia.BD::CUS.Reserva" bmbmo
				) as linea on linea."IdCarteraOrigen" = S."IdCarteraRetirada"
				and linea."IdOperacion" = C."IdOperacion" and s."FolioSubyacente" = linea."FolioSubyacente"
			WHERE S."IdEstado" = 'VIG' AND c."IdEstado" = 'VIG'
			  AND S."IdClasificacionTipoInstrumento" = 'IF' -- AND "IdCliente" = 7672  
			  AND C."IdTipoInstrumento" IN ('PAC') AND C."IdTipoOperacion" != 'VENPAC'
	UNION ALL
			--COMPRA A PLAZO
			SELECT 
				s."FolioSubyacente" AS "FolioCustodia",
				s."INGFechaOperacion",
				c."IdClienteRelacionado" AS "IdCliente",
				c."IdPortafolioRelacionado",
				s."IdTipoInstrumento",
				s."IdInstrumento",
				s."IdEmisor",
				s."Nemotecnico",
				s."NemoSVS",
				'' AS "IdBoveda",
				s."IdTipoReajuste",
				c."PlazoEnDiasOperacion",
				s."EsFungible",
				s."IdClasificacionTipoInstrumento",
				'' AS "CodigoIFRS",
				s."FechaEmisionInstrumento",
				s."FechaVencimientoInstrumento",
				c."IdTipoComprobante",
				"FolioComprobante",
				c."FechaComprobante",
				0  AS "Cantidad", ---"INGCantidad"
				case when c."IdCliente" = CORR."IdCliente" THEN S."INGValorRescate" ELSE 0 END AS "ValorRescate",	--"INGValorRescate"
				CASE WHEN s."Nemotecnico" = 'PDBC' THEN s."INGMontoATasaCompraActual"/(s."INGValorRescate"/5000000) ELSE s."INGMontoATasaCompraActual" END AS "MontoCompra",
				c."INGTasaCompra" AS "TasaCompra", 
				s."INGTasaMercadoActual" AS "TasaMercado",
				CASE WHEN s."Nemotecnico" = 'PDBC' THEN S."INGMontoATasaSubyacenteActual"/(S."INGValorRescate"/5000000) ELSE S."INGMontoATasaSubyacenteActual" END  AS "MontoCartera",
				c."ValorParidadCierre",
				s."INGMontoCompraActualMonedaInstrumento" AS "MontoCompraMonedaInstrumento",
				s."IdMonedaInstrumento",
				0 AS "INGTasaMercadoAnterior",
				0 AS "INGMontoATasaMercadoAnterior",
				CASE WHEN C."IdTipoOperacion"  in ('VENSIM','VENPAC')  THEN CASE WHEN S."Nemotecnico" = 'PDBC' THEN S."INGValorRescate"/5000000 ELSE S."INGCantidad" END ELSE 0 END AS "Cantidad_Compra_Plazo",
			    CASE WHEN C."IdTipoOperacion"  in ('VENSIM','VENPAC') AND c."IdCliente" != CORR."IdCliente" THEN 
			   	CASE WHEN S."Nemotecnico" = 'PDBC' 
			   		THEN 5000000 
			   		ELSE S."INGValorRescate" END 
			   	ELSE 0 END AS "ValorRescate_Compra_Plazo",
			    CASE WHEN C."IdTipoOperacion"  in ('COMPAC')  THEN CASE WHEN S."Nemotecnico" = 'PDBC' THEN S."INGValorRescate"/5000000 ELSE S."INGCantidad" END ELSE 0 END AS "Cantidad_Venta_Plazo",
			   CASE WHEN C."IdTipoOperacion" in ('COMPAC') THEN CASE WHEN S."Nemotecnico" = 'PDBC' THEN 5000000 ELSE S."INGValorRescate" END ELSE 0 END AS "ValorRescate_Venta_Plazo",
			   0 AS "CantidadGarantia",
			   case when c."IdCliente" = CORR."IdCliente" THEN '' ELSE 'CC' END AS "TipoCartera",
			   linea.ROW_NUM AS ROW_NUM
			FROM "BSCL.Custodia.BD::CUS.Subyacente" as S 
			INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" as C on S."FolioCarteraRelacionada" = C."FolioCartera" AND C."EsPrimeraCartera" = 'S'
			LEFT join (
				select ROW_NUMBER() OVER (PARTITION BY "IdOperacion" ORDER BY "IdReserva") AS ROW_NUM,
				"IdCarteraOrigen","IdOperacion" from "BSCL.Custodia.BD::CUS.Reserva" bmbmo
				) as linea on linea."IdCarteraOrigen" = S."IdCarteraRetirada"
				and linea."IdOperacion" = C."IdOperacion"
			LEFT JOIN "BSCL.Plataforma.BD::PLA.Empresa" EMPRESA ON C."IdEmpresa" = EMPRESA."IdEmpresa"
			LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON SOC."IdSocioDeNegocio" = EMPRESA."IdSocioDeNegocio"
			LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CORR ON CORR."IdPersona" = SOC."IdPersona"
			WHERE S."IdEstado" = 'VIG' AND c."IdEstado" = 'VIG'
			  AND S."IdClasificacionTipoInstrumento" = 'IF' -- AND "IdClienteRelacionado" =  6069
			  AND C."IdTipoInstrumento" IN ('PAC')	AND C."IdTipoOperacion"  in ('VENPAC')		
		UNION ALL
			--CARTERA NEGATIVA
			SELECT 
				"IdCarteraNegativa" AS "FolioCustodia",
				"FechaOperacion",
				"IdCliente",
				"IdPortafolio",
				ins."IdTipoInstrumento",
				car."IdInstrumento",
				car."IdEmisor",
				car."Nemotecnico",
				"NemoSVS",
				"IdBoveda",
				car."IdTipoReajuste",
				0 AS "PlazoEnDiasOperacion",
				"EsFungible",
			   "IdClasificacionTipoInstrumento",
			   "CodigoIFRS",
				ins."FechaEmision" AS "FechaEmisionInstrumento",
				ins."FechaVencimiento" AS "FechaVencimientoInstrumento",
				"IdTipoComprobante",
				"FolioComprobante",
				'0001-01-01' AS "FechaComprobante",
				CASE WHEN car."Nemotecnico" = 'PDBC' THEN -1*"ValorRescate"/5000000 ELSE -1*"Cantidad" END AS "Cantidad",
				CASE WHEN car."Nemotecnico" = 'PDBC' THEN -5000000 ELSE -1*"ValorRescate" END AS "ValorRescate",
				0 AS "MontoCompra", --CLP
				"TasaOperacion" AS "TasaCompra", 
				"TasaMercado" AS "TasaMercado",
				CASE WHEN car."Nemotecnico" = 'PDBC' THEN -1*"MontoMercadoMonedaSistema"/("ValorRescate"/5000000) ELSE "MontoMercadoMonedaSistema" END  AS "MontoCartera",
				0 AS "ValorParidadCierre",
				-1*"MontoOperacionMonedaInstrumento" AS "MontoCompraMonedaInstrumento",
				"IdMonedaInstrumento",
				0 AS "INGTasaMercadoAnterior",
				0 AS "INGMontoATasaMercadoAnterior",
				0 AS "Cantidad_Compra_Plazo",
				0 AS "ValorRescate_Compra_Plazo",
				0 AS "Cantidad_Venta_Plazo",
				0 AS "ValorRescate_Venta_Plazo",
				0 AS "CantidadGarantia",
				CASE WHEN "IdTipoOperacion" = 'VENPAC' THEN 'CC' ELSE '' END  AS "TipoCartera",
				0 AS ROW_NUM
			FROM "BSCL.Custodia.BD::CUS.CarteraNegativa" AS car 
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins on ins."IdInstrumento" = car."IdInstrumento"
			LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tins on tins."IdTipoInstrumento" = ins."IdTipoInstrumento"
			WHERE car."IdEstado" = 'VIG' AND "IdClasificacionTipoInstrumento" = 'IF'
				AND tins."IdTipoInstrumento" NOT IN ('SIM','PAC','PRT','EMP','FFM') 
			UNION ALL
			SELECT 
				G."FolioGarantia" AS "FolioCustodia",
				g."INGFechaOperacion" AS "FechaOperacion",
				g."IdCliente",
				g."IdPortafolio",
				g."IdTipoInstrumento",
				g."IdInstrumento",
				g."IdEmisor",
			    g."Nemotecnico",
			    g."NemoSVS",
			    '' AS "IdBoveda",
			    g."IdTipoReajuste",
			    0 AS "PlazoEnDiasOperacion",
			    g."EsFungible",
			   g."IdClasificacionTipoInstrumento",
			   g."CodigoIFRS",
			   g."FechaEmisionInstrumento",
			   g."FechaVencimientoInstrumento",
			   g."IdTipoComprobante",
			   --CASE WHEN COALESCE ("FolioExternoReferencia",0) = 0 THEN  g."FolioComprobante" ELSE g."FolioExternoReferencia" END AS "FolioComprobante",
			   "FolioComprobante",
			   g."FechaComprobante",
			 	0 AS "Cantidad", 
				CASE WHEN g."Nemotecnico" = 'PDBC' THEN 5000000 ELSE g."INGValorRescate" END AS "ValorRescate",
				CASE WHEN g."Nemotecnico" = 'PDBC' THEN g."INGMontoATasaCompraActual"/(g."INGValorRescate"/5000000) ELSE g."INGMontoATasaCompraActual" END AS "MontoCompra",
			    g."TasaCompraOperacionOrigen" AS "TasaCompra", 
			    g."INGTasaMercadoActual" AS "TasaMercado",
				CASE WHEN g."Nemotecnico" = 'PDBC' THEN g."INGMontoATasaMercadoActual"/(g."INGValorRescate"/5000000) ELSE g."INGMontoATasaMercadoActual" END  AS "MontoCartera",
			   	0 AS "ValorParidadCierre",
				g."INGMontoATasaCompraActual" AS "MontoCompraMonedaInstrumento",
				g."IdMonedaInstrumento",
				0 AS "INGTasaMercadoAnterior",
				0 AS "INGMontoATasaMercadoAnterior",
				0 AS "Cantidad_Compra",
			    0 AS "ValorRescate_Compra",
			    0 AS "Cantidad_Venta",
			    0 AS "ValorRescate_Venta",
			   CASE WHEN g."Nemotecnico" = 'PDBC' THEN g."INGValorRescate"/5000000 ELSE g."INGCantidad" END AS "CantidadGarantia",
			   '' AS "TipoCartera",
			   0 AS ROW_NUM
			FROM "BSCL.Custodia.BD::GTA.Garantia" AS G
			--INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS C ON C."FolioCartera" = G."FolioCarteraOrigen"
			WHERE G."IdEstado" = 'VIG'
			  AND G."IdClasificacionTipoInstrumento" = 'IF'
			  AND G."IdTipoInstrumento" <> 'FFM'
			  AND G."INGFechaLiquidacion" <= (SELECT "Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.FechaSistema')
		) AS SCUST
			LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = SCUST."IdCliente"
			LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = CLI."IdPersona"
			LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
			LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS fec ON (fec."IdEmpresa" = 1 and "CodigoParametro" = 'PLA.FechaSistema')
			LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" AS EMI ON EMI."IdEmisor" = SCUST."IdEmisor"
			LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PEREMI ON PEREMI."IdPersona" = EMI."IdPersona"
			LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENTEMI ON PEREMI."IdDocumentoIdentidad" = IDENTEMI."IdDocumentoIdentidad"
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdCliente" = CLI."IdCliente" AND SCUST."IdPortafolio" = por."IdPortafolio"			
		WHERE SCUST."ValorRescate"  <= 99999999999
		GROUP BY "TipoCartera" , LEFT(fec."Valor",7) ,
				SCUST."FolioCustodia",fec."Valor",
				COALESCE("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(IDENT."DocumentoIdentidad"),'0-0') ,
				POR."CodigoPortafolio" ,
				trim(CAST(SCUST."Nemotecnico" AS VARCHAR(16))) ,
				CAST(SCUST."NemoSVS" AS VARCHAR(16)),ROW_NUM
		WITH READ ONLY;

	
GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBCTCIIF" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
