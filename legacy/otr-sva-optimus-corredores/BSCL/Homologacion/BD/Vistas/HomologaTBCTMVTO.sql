SET SCHEMA "BSCL";
DROP VIEW "BSCL.Homologacion.BD.Vistas::HomologaTBCTMVTO";

CREATE VIEW "BSCL.Homologacion.BD.Vistas::HomologaTBCTMVTO" AS
WITH
	v_Custodia as (
		SELECT 	'CUS' AS "Modulo","FolioCartera" AS "Folio","FechaIngresoRegistro","FechaOperacion","FechaVencimientoOperacion",
				"INGFechaLiquidacion" AS "FechaLiquidacion",0 AS "Plazo","IdCondicionLiquidacion","IdEstado",
				"IdCliente","IdPortafolio",0 AS "IdClienteCorredora",0 AS "IdPortafolioCorredora",
				"IdTipoOperacion","IdTipoProducto","IdConcepto","IdTipoComprobante","FolioComprobante","IdMesa",
				"IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","IdEmisor","FechaEmisionInstrumento","IdMonedaEmision",
				'' AS "IdMonedaOperacion","Nemotecnico","IdBolsa","IdBoveda","EsFungible","IdAgente","IdSucursal",
			   	"INGCantidad" AS "Cantidad","INGValorRescate" AS "ValorRescate","IdTipoReajuste",
			   	"INGPrecioCompra" AS "PrecioCompra","INGTasaCompra" AS "TasaCompra","INGTasaAgente" AS "TasaAgente",0 AS "TasaEmision",
			   	"INGMontoATasaCompraActual" AS "MontoCompra","INGPrecioMercadoActual" AS "PrecioMercado",
			   	"CodigoIFRS",'' AS "FolioTransaccion",'' AS "IdMedioDePago",'' AS "EmitidoA",0 AS "Comision",0 AS "Gastos"
		FROM "BSCL.Custodia.BD::CUS.Cartera"
		WHERE "IdTipoProducto" not in ('INSI','MERF','MEFW','PRES')
	),
	v_TitulosNominales as (
		SELECT 	'CUSNOM' AS "Modulo","IdMovimientoCarteraEnTransito" AS "Folio","FechaIngresoRegistro","FechaOperacion","FechaVencimientoOperacion",
				"FechaLiquidacion",0 AS "Plazo","IdCondicionLiquidacion","IdEstado",
				"IdCliente","IdPortafolio",0 AS "IdClienteCorredora",0 AS "IdPortafolioCorredora",
				"IdTipoOperacion","IdTipoProducto","IdConcepto","IdTipoComprobante","FolioComprobante","IdMesa",
				"IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","IdEmisor","FechaEmisionInstrumento","IdMonedaEmision",
				'' AS "IdMonedaOperacion","Nemotecnico","IdBolsa","IdBoveda","EsFungible","IdAgente","IdSucursal",
			   	"Cantidad" AS "Cantidad","ValorRescate" AS "ValorRescate","IdTipoReajuste",
			   	"PrecioCompra","TasaCompra",0 AS "TasaAgente",0 AS "TasaEmision","MontoCompra",0 AS "PrecioMercado",
			   	'' AS "CodigoIFRS",'' AS "FolioTransaccion",'' AS "IdMedioDePago",'' AS "EmitidoA",0 AS "Comision",0 AS "Gastos"
		FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito"
		WHERE "EsNominal" = 'S'
	),
	v_OperacionesAPlazo as (
		SELECT 	'PLZ' AS "Modulo","FolioCartera" AS "Folio","FechaIngresoRegistro","FechaOperacion","FechaVencimientoOperacion",
				"INGFechaLiquidacion" AS "FechaLiquidacion",0 AS "Plazo","IdCondicionLiquidacion","IdEstado",
				"IdCliente","IdPortafolio","IdClienteRelacionado" AS "IdClienteCorredora","IdPortafolioRelacionado" AS "IdPortafolioCorredora",
				"IdTipoOperacion","IdTipoProducto","IdConcepto","IdTipoComprobante","FolioComprobante","IdMesa",
				"IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","IdEmisor","FechaEmisionInstrumento","IdMonedaEmision",
				'' AS "IdMonedaOperacion","Nemotecnico","IdBolsa","IdBoveda","EsFungible","IdAgente","IdSucursal",
			   	"INGCantidad" AS "Cantidad","INGValorRescate" AS "ValorRescate","IdTipoReajuste",
			   	"INGPrecioCompra" AS "PrecioCompra","INGTasaCompra" AS "TasaCompra","INGTasaAgente" AS "TasaAgente",0 AS "TasaEmision",
			   	"INGMontoATasaCompraActual" AS "MontoCompra","INGPrecioMercadoActual" AS "PrecioMercado",'' AS "CodigoIFRS",
			   	'' AS "FolioTransaccion",'' AS "IdMedioDePago",'' AS "EmitidoA",0 AS "Comision",0 AS "Gastos"
		FROM "BSCL.Custodia.BD::CUS.Cartera"
		WHERE "IdTipoProducto" in ('INSI','MERF','MEFW','PRES')
	),
	v_Tesoreria as (
		SELECT 	'TES' AS "Modulo",MOV."IdIngresoEgreso" AS "Folio",MOV."FechaIngresoRegistro",'0001-01-01' AS "FechaOperacion",
				'0001-01-01' AS "FechaVencimientoOperacion",MOV."FechaLiquidacion",0 AS "Plazo",
				MOV."IdCondicionLiquidacion",MOV."IdEstado",CLI."IdCliente" AS "IdCliente",MOV."IdPortafolioTitularPago" AS "IdPortafolio",
				0 AS "IdClienteCorredora",0 AS "IdPortafolioCorredora",
				MOV."IdTipoOperacion",MOV."IdTipoProducto",MOV."IdConcepto",'' AS "IdTipoComprobante",0 AS "FolioComprobante",0 AS "IdMesa",
				MOV."IdClasificacionTipoInstrumento",'' AS "IdTipoInstrumento",0 AS "IdInstrumento",'' AS "IdEmisor",
				'0001-01-01' AS "FechaEmisionInstrumento",'' AS "IdMonedaEmision",IE."IdMoneda" AS "IdMonedaOperacion",
				'' AS "Nemotecnico",MOV."IdBolsa",'' AS "IdBoveda",'' AS "EsFungible",0 AS "IdAgente",0 AS "IdSucursal",
			   	0 AS "Cantidad",0 AS "ValorRescate",'' AS "IdTipoReajuste",
			   	0 AS "PrecioCompra",0 AS "TasaCompra",0 AS "TasaAgente",0 AS "TasaEmision",
			   	IE."Monto" AS "MontoCompra",0 AS "PrecioMercado",'' AS "CodigoIFRS",'' AS "FolioTransaccion",
			   	IE."IdMedioDePago",MOV."NombrePersonaTitularPago" AS "EmitidoA",0 AS "Comision",0 AS "Gastos"
		FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS MOV
		INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago" AS IE ON (IE."IdIngresoEgreso" = MOV."IdIngresoEgreso")
		INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (MOV."IdTipoPersonaTitularPago" = 'CLI' AND CLI."IdPersona" = MOV."IdPersonaTitularPago")
	),
	v_CuentaInversion as (
		SELECT 	'CTA' AS "Modulo",MOV."IdCargoAbonoMovimiento" AS "Folio",MOV."FechaIngresoRegistro",'0001-01-01' AS "FechaOperacion",
				'0001-01-01' AS "FechaVencimientoOperacion",MOV."FechaEfectivaMovimiento" AS "FechaLiquidacion",0 AS "Plazo",
				MOV."IdCondicionDeLiquidacion" AS "IdCondicionLiquidacion",MOV."IdEstado",CI."IdCliente" AS "IdCliente",CI."IdPortafolio",0 AS "IdClienteCorredora",0 AS "IdPortafolioCorredora",
				MOV."IdTipoOperacion",MOV."IdTipoProducto",MOV."IdConcepto",MOV."IdTipoMovimiento" AS "IdTipoComprobante",0 AS "FolioComprobante",0 AS "IdMesa",
				'' AS "IdClasificacionTipoInstrumento",'' AS "IdTipoInstrumento",0 AS "IdInstrumento",'' AS "IdEmisor",
				'0001-01-01' AS "FechaEmisionInstrumento",'' AS "IdMonedaEmision",CI."IdMoneda" AS "IdMonedaOperacion",
				'' AS "Nemotecnico",'' AS "IdBolsa",'' AS "IdBoveda",'' AS "EsFungible",0 AS "IdAgente",0 AS "IdSucursal",
			   	0 AS "Cantidad",0 AS "ValorRescate",'' AS "IdTipoReajuste",
			   	0 AS "PrecioCompra",0 AS "TasaCompra",0 AS "TasaAgente",0 AS "TasaEmision",
			   	MOV."Monto" AS "MontoCompra",0 AS "PrecioMercado",'' AS "CodigoIFRS",'' AS "FolioTransaccion",
			   	'' AS "IdMedioDePago",'' AS "EmitidoA",0 AS "Comision",0 AS "Gastos"
		FROM  "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS MOV
		INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CI ON CI."IdCuentaInversion" = MOV."IdCuentaInversion"
	),
	v_Facturacion as (
		SELECT 	'MFAC' AS "Modulo",COM."FolioComprobante" AS "Folio",COM."FechaIngresoRegistro",'0001-01-01' AS "FechaOperacion",
				'0001-01-01' AS "FechaVencimientoOperacion",COM."FechaLiquidacion",COM."DiasPlazo" AS "Plazo",
				COM."IdCondicionLiquidacion",COM."IdEstadoComprobante" AS "IdEstado",COM."IdCliente",COM."IdPortafolio",0 AS "IdClienteCorredora",0 AS "IdPortafolioCorredora",
				COM."IdTipoOperacion",COM."IdTipoProducto",COM."IdConcepto" AS "IdConcepto",COM."IdTipoComprobante",COM."FolioComprobante",
				0 AS "IdMesa",COM."IdClasificacionTipoInstrumento",COM."IdTipoInstrumento",0 AS "IdInstrumento",'' AS "IdEmisor",
				'0001-01-01' AS "FechaEmisionInstrumento",'' AS "IdMonedaEmision",'' AS "IdMonedaOperacion",
				'' AS "Nemotecnico",COM."IdBolsa",COM."IdBoveda",'' AS "EsFungible",COM."IdEmpleadoEjecutivoPortafolio" AS "IdAgente",
				COM."IdSucursalEmpleadoEjecutivoPortafolio" AS "IdSucursal",
			   	COM."Cantidad",0 AS "ValorRescate",'' AS "IdTipoReajuste",
			   	0 AS "PrecioCompra","Tasa" AS "TasaCompra",0 AS "TasaAgente",0 AS "TasaEmision",
			   	COM."Monto" AS "MontoCompra",0 AS "PrecioMercado",'' AS "CodigoIFRS",'' AS "FolioTransaccion",
			   	'' AS "IdMedioDePago",'' AS "EmitidoA", CBS."Comision", CBS."Gastos"
		FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS COM
		LEFT JOIN (
			SELECT "IdComprobante", SUM("COFI"+"COSI"+"COVA"+"COAI"+"COIF"+"CCOR") AS "Comision",SUM("GFOD"+"GFFA") AS "Gastos"
			FROM (
				SELECT  "IdComprobante",
					--COMISIONES
					COALESCE(MAX(CASE WHEN "IdCobro" = 'COFI' THEN "MontoCobro" END),0) AS "COFI",
					COALESCE(MAX(CASE WHEN "IdCobro" = 'COSI' THEN "MontoCobro" END),0) AS "COSI",
					COALESCE(MAX(CASE WHEN "IdCobro" = 'COVA' THEN "MontoCobro" END),0) AS "COVA",
					COALESCE(MAX(CASE WHEN "IdCobro" = 'COAI' THEN "MontoCobro" END),0) AS "COAI",
					COALESCE(MAX(CASE WHEN "IdCobro" = 'COIF' THEN "MontoCobro" END),0) AS "COIF",
					COALESCE(MAX(CASE WHEN "IdCobro" = 'CCOR' THEN "MontoCobro" END),0) AS "CCOR",
					--GASTOS
					COALESCE(MAX(CASE WHEN "IdCobro" = 'GFOD' THEN "MontoCobro" END),0) AS "GFOD",
					COALESCE(MAX(CASE WHEN "IdCobro" = 'GFFA' THEN "MontoCobro" END),0) AS "GFFA"
				FROM (
						SELECT "IdComprobante", "IdCobro", "MontoCobro"
						FROM "BSCL.Facturacion.BD::FAC.ComprobanteCobro"
				     )
				GROUP BY "IdComprobante"
				)
			GROUP BY "IdComprobante"
		) AS CBS ON CBS."IdComprobante" = COM."IdComprobante"
	)
SELECT
	 CASE WHEN SINFO."Modulo" = 'CUS' THEN 'N'
		  WHEN SINFO."Modulo" = 'MFAC' AND SINFO."Plazo" > 0 THEN 'S'
		  ELSE '' 
	  END AS IND_CUST_APLAZO 										--char](1) NOT NULL
	,CASE WHEN SINFO."Modulo" in ('CUS','PLZ','PAC') AND SINFO."IdEstado" ='VIG' THEN 'I'
		  WHEN SINFO."Modulo" = 'CTA' AND SINFO."IdEstado" ='CRT' THEN 'I'
		  WHEN SINFO."Modulo" = 'TES' AND SINFO."IdEstado" ='ING' THEN 'I'
		  WHEN SINFO."IdEstado" ='ANU' THEN 'A'
		  ELSE ''
	  END AS IND_ANULADO 											--char](1) NOT NULL
	,SINFO."IdEstado" AS EST_MOVTO 									--char](1) NOT NULL
	,SINFO."FechaOperacion" AS FEC_MVTO
	,SINFO."Folio" AS SEC_MOVTO 									--decimal](8 0) NOT NULL
	,CASE WHEN SINFO."Modulo" = 'CTA' THEN 'S' 
		  ELSE ''
	  END AS IND_SALDO_CTDO 										--char](1) NOT NULL
	,CASE WHEN SINFO."Modulo" = 'CTA' THEN 'S' 
		  ELSE ''
	  END AS IND_SALDO_APLAZO 										--char](1) NOT NULL
	,CASE WHEN SINFO."Modulo" = 'CUS' AND SINFO."IdTipoOperacion" = 'INGTIT' THEN 'E'
		  WHEN SINFO."Modulo" = 'CUS' AND SINFO."IdTipoOperacion" = 'EGRTIT' THEN 'S'
		  ELSE ''
	  END AS IND_CUST_CTDO 											--char](1) NOT NULL
	,TRIM(PER."DocumentoIdentidad") AS RUT_CLI
	,PORT."CodigoPortafolio" AS SEC_RUT_CLI
	,SUBSTRING (SINFO."IdTipoComprobante",0,2) AS TIPO_COMP_ADJ		--char](2) NOT NULL
	,SINFO."FolioComprobante" AS FOLIO_COMP_ADJ 					--decimal](8 0) NOT NULL
	, 0 AS SEC_TRASPASO 											--decimal](8 0) NOT NULL
	,CASE WHEN SINFO."Modulo" = 'CUS' OR SINFO."Modulo" = 'CUSNOM' THEN 'CU'
		  WHEN SINFO."Modulo" = 'TES' THEN 'TE'
	      ELSE ''
	  END AS UNIDAD_DES 											--char](2) NOT NULL ???
	,CASE WHEN SINFO."IdTipoOperacion" = 'APORFM' THEN 'VIFM'
		  WHEN SINFO."IdTipoOperacion" = 'RESCFM' THEN 'CIFM'
		  WHEN SINFO."IdTipoOperacion" = 'VCRF'   THEN 'VCRF'
		  WHEN SINFO."IdTipoOperacion" = 'VENOSA' THEN 'VOSA'
		  ELSE ''
	  END AS COD_MOVTO 												--char](4) NOT NULL
	,SINFO."Nemotecnico" AS NEMO 									--char](16) NOT NULL
	,'' AS CALC_MTO_APLAZO 											--char](1) NOT NULL ???
	,'' AS CALC_MTO_CTDO 											--char](1) NOT NULL ???
	,SINFO."Cantidad" AS CANTIDAD									--decimal](17 6) NOT NULL
	,SINFO."IdAgente" AS COD_AGENTE 								--char](4) NOT NULL
	,'' AS COD_BCO_PAGO 											--char](4) NOT NULL ???
	,SINFO."IdBoveda" AS COD_BOVEDA 								--char](3) NOT NULL
	,0 AS COD_CORR_CONTRA 											--decimal](3 0) NOT NULL
	,MES."Codigo" AS COD_MESA 										--char](4) NOT NULL
	,'' AS COD_OPE 													--char](4) NOT NULL
	,SINFO."IdClasificacionTipoInstrumento" AS COD_RUBRO 			--char](4) NOT NULL
	,SINFO."IdSucursal" AS COD_SUC 									--char](4) NOT NULL
	,SINFO."Comision" AS COMISION 									--decimal](14 4) NOT NULL
	,SINFO."IdConcepto" AS CONCEPTO_MVTO 							--char](2) NOT NULL
	,0 AS DERECHOS 													--decimal](14 4) NOT NULL
	,SINFO."IdEmisor" AS EMISOR 									--char](10) NOT NULL
	,SINFO."EmitidoA" AS EMITIDO_A 									--char](40)NOT NULL ???
	,0 AS FAC_AJUSTE 												--decimal](15 10) NOT NULL
	,SINFO."FechaEmisionInstrumento" AS FEC_EMI 					--char](10) NOT NULL
	,SINFO."FechaIngresoRegistro" AS FEC_ING_MVTO 					--char](10) NOT NULL
	,SINFO."FechaLiquidacion" AS FEC_LIQUID 						--char](10) NOT NULL
	,CASE WHEN SINFO."Modulo" = 'MFAC' AND SINFO."FechaVencimientoOperacion" != '0001-01-01' THEN add_days(SINFO."FechaVencimientoOperacion",-1*SINFO."Plazo")
		  ELSE '0001-01-01'
	  END AS FEC_TRANS 												--char](10) NOT NULL
	,SINFO."FechaVencimientoOperacion" AS FEC_VCTO 					--char](10) NOT NULL
	,0 AS FOLIO_COMP_CONT 											--decimal](8 0) NOT NULL
	,SINFO."FolioTransaccion" AS FOLIO_TRANS 						--char](8) NOT NULL
	,CASE WHEN SINFO."IdMedioDePago" = 'EBA' THEN 'ET'
		  WHEN SINFO."IdMedioDePago" = 'CHE' THEN 'CH'
		  WHEN SINFO."IdMedioDePago" = 'BDE' THEN 'BD'
		  WHEN SINFO."IdMedioDePago" = 'EFE' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'EF'
		  WHEN SINFO."IdMedioDePago" = 'EFE' AND SINFO."IdMonedaOperacion" = 'EUR' THEN 'EU'
		  WHEN SINFO."IdMedioDePago" = 'EFE' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'US'
		  WHEN SINFO."IdMedioDePago" = 'CIN' THEN ''	--CUENTA INVERSION
		  WHEN SINFO."IdMedioDePago" = 'VVI' THEN 'VV'
		  WHEN SINFO."IdMedioDePago" = 'VVE' THEN 'VE'
		  WHEN SINFO."IdMedioDePago" = 'TLR' THEN 'TH'
		  WHEN SINFO."IdMedioDePago" = 'TSW' THEN 'TH'
		  WHEN SINFO."IdMedioDePago" = 'TDP' THEN 'TH'
		  WHEN SINFO."IdMedioDePago" = 'TRA' THEN 'TH'
		  WHEN SINFO."IdMedioDePago" = 'AJU' THEN ''	--AJUSTE CUENTA CORRIENTE
		  ELSE ''
	  END AS FORMA_PAGO 											--char](2) NOT NULL
	,SINFO."Gastos" AS GASTOS 										--decimal](14 4) NOT NULL
	,'' AS IDENTIFIC_DOCTO 											--char](20) NOT NULL ???
	,SINFO."IdBolsa" AS IND_BOLSA 									--char](3) NOT NULL
	,CASE WHEN SINFO."Modulo" = 'CTA' AND SINFO."IdTipoComprobante" = 'MOVABN' THEN 'A'
		  WHEN SINFO."Modulo" = 'CTA' AND SINFO."IdTipoComprobante" = 'MOVCAR' THEN 'C'
		  ELSE ''
	  END AS IND_CCTE_APLAZO 										--char](1) NOT NULL
	,CASE WHEN SINFO."Modulo" = 'CTA' AND SINFO."IdTipoComprobante" = 'MOVABN' THEN 'A'
		  WHEN SINFO."Modulo" = 'CTA' AND SINFO."IdTipoComprobante" = 'MOVCAR' THEN 'C'
		  ELSE ''
	  END AS IND_CCTE_CTDO 											--char](1) NOT NULL
	,'' AS IND_CHEQUE 												--char](1) NOT NULL ???
	,'' AS IND_COBRO_SEP 											--char](1) NOT NULL ???
	,'' AS IND_CORREC 												--char](1) NOT NULL ???
	,'' AS IND_EXENCION 											--char](1) NOT NULL ???
	,CASE WHEN SINFO."Modulo" = 'CUS' THEN 'N'
		  ELSE ''
	  END AS IND_FUS_CAR_DIA 										--char](1) NOT NULL
	,CASE WHEN SINFO."Modulo" = 'CUS' THEN 'N'
		  ELSE ''
	  END AS IND_FUSION_TRAS 										--char](1) NOT NULL
	,'' AS IND_GASTO_FAC 											--char](1) NOT NULL ???
	,'' AS IND_HAB_CARTERA 											--char](1) NOT NULL ???
	,'' AS IND_INC_GAS_CAR 											--char](1) NOT NULL ???
	,COALESCE(CODLIQ."Codigo",'') AS IND_LIQUID 					--char](2) NOT NULL
	,'' AS IND_PAGO_COBRO 											--char](1) NOT NULL ???
	,'' AS IND_REBAJ_CAR 											--char](1) NOT NULL ???
	,'' AS IND_RECAM_CAR 											--char](1) NOT NULL ???
	,'' AS IND_REPAR_CAR 											--char](1) NOT NULL ???
	,'' AS IND_RETENIDO 											--char](1) NOT NULL ???
	,'' AS IND_RIESGO_CLI 											--char](1) NOT NULL ???
	,CASE WHEN SINFO."Modulo" = 'PLZ' AND SINFO."IdTipoProducto" in ('MERF','MEFW') THEN 'F'
		  ELSE ''
	  END AS IND_RUEDA 												--char](1) NOT NULL
	,CASE WHEN SINFO."Modulo" = 'CTA' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'C'
    	  WHEN SINFO."Modulo" = 'CTA' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'U'
    	  WHEN SINFO."Modulo" = 'CTA' AND SINFO."IdMonedaOperacion" = 'EUR' THEN 'V'
    	  ELSE ''
      END AS IND_TIT_CCTE 											--char](1) NOT NULL
	,CASE WHEN SINFO."Modulo" = 'CUS' THEN 'C'
		  WHEN SINFO."Modulo" = 'CUSNOM' THEN 'N'
		  ELSE ''
	  END AS IND_TIT_CUS 											--char](1) NOT NULL
	,'' AS IND_TRANSF 												--char](1) NOT NULL ???
	,'' AS IND_TRASPASO 											--char](1) NOT NULL ???
	, 0 AS LIN_COMP_ADJ 											--decimal](2 0) NOT NULL  ???
	, 0 AS LIN_ORDEN 												--decimal](2 0) NOT NULL  
	,SUBSTRING (SINFO."IdTipoInstrumento",0,2) AS MERCADO			--char](2) NOT NULL
	,SINFO."MontoCompra" AS MONTO 									--decimal](17 2) NOT NULL
	,CASE WHEN SINFO."Modulo" = 'PLZ' THEN SINFO."MontoCompra"
		  ELSE 0 
	  END AS MONTO_APLAZO 											--decimal](17 2) NOT NULL
    , 0 AS MONTO_CAP_COMP 											--decimal](17 2) NOT NULL
	, 0 AS MONTO_INTERES 											--decimal](17 2) NOT NULL
	,SINFO."PrecioCompra" AS PRECIO 								--decimal](17 6) NOT NULL
	,SINFO."IdTipoProducto" AS PRODUCTO 							--char](4) NOT NULL
	,TRIM(PCORR."DocumentoIdentidad") AS RUT_CORR 					--char](11) NOT NULL
	, 0 AS SEC_CAR_COMPROM 											--decimal](8 0) NOT NULL
	, 0 AS SEC_EVE 													--decimal](8 0) NOT NULL
	, 0 AS SEC_MOVTO_ASOC 											--decimal](8 0) NOT NULL
	, 0 AS SEC_ORDEN 												--decimal](8 0) NOT NULL
	,SINFO."IdPortafolioCorredora" AS SEC_RUT_CORR 					--decimal](2 0) NOT NULL
	, 0 AS SPREAD 													--decimal](17 2) NOT NULL
	,SINFO."TasaCompra" AS TASA 									--decimal](7 4) NOT NULL
	,SINFO."TasaAgente" AS TASA_AGE 								--decimal](7 4) NOT NULL
	, 0 AS TASA_COMPROM 											--decimal](7 4) NOT NULL
	,'' AS TIPO_FAM 												--char](2) NOT NULL
	,SINFO."IdTipoReajuste" AS TIPO_REAJ 							--char](2) NOT NULL
	,'' AS UNIDAD_SOL 												--char](2) NOT NULL ???
	, 0 AS USUARIO_CER 												--decimal](7 0) NOT NULL
	, 0 AS USUARIO_SOL 												--decimal](7 0) NOT NULL
	,SINFO."ValorRescate" AS VAL_RESC 								--decimal](15 4) NOT NULL
	, 0 AS VALOR_UNIT 												--decimal](17 2) NOT NULL
	,'' AS VTO_COMP_ADJ 											--char](10) NOT NULL ???
	,CLI."IdTipoCliente" AS TIPO_CLI 								--char](2) NOT NULL
	,'' AS CLAVE_DCV_CLI 											--char](15) NOT NULL
	,'' AS ENV_DCV_CLI 												--char](1) NOT NULL
	,'' AS CLAVE_DCV_COR 											--char](15) NOT NULL
	,'' AS ENV_DCV_COR												--char](1) NOT NULL
	,SINFO."IdPortafolio" AS PORTAFOLIO 							--char](10) NOT NULL
	, 0 AS RETENCION_FUENTE 										--decimal](17 4) NOT NULL ???
	,'' AS CODIGO_SER 												--char](3) NOT NULL
	,SINFO."FolioComprobante" AS FOLIO_COMPROBANTE 					--decimal](5 0) NOT NULL
	,'' AS FPA_ID 													--char](2) NOT NULL ???
	,SINFO."EsFungible" AS IND_FUNGIBILIDAD 						--char](1) NOT NULL
	,SINFO."TasaEmision" AS TASA_EMISION 							--decimal](10 4) NOT NULL
	, 0 AS SPREAD_TASA_EMISION 										--decimal](10 4) NOT NULL
	,'' AS MODALIDAD_PAGO 											--char](1) NOT NULL
    ,'' AS PERIODICIDAD_PAGO 										--char](1) NOT NULL ???
	,CASE WHEN SINFO."Modulo" = 'PLZ' AND SINFO."IdTipoProducto" = 'INSI' THEN 'TS'
		  ELSE 'NOR'
	  END AS IND_SIMUL 												--char](2) NOT NULL
    ,'' AS ORIGEN_RECURSOS 											--char](1) NOT NULL
	,SINFO."IdClasificacionTipoInstrumento" AS CLASIF_CARTERA 		--char](1) NOT NULL
	,SINFO."PrecioMercado" AS PRECIO_MED_CIE 						--decimal](5 2) NOT NULL
	, 0 AS PRECIO_PROM_COMP 										--decimal](5 2) NOT NULL
	,SINFO."EsFungible" AS COD_FUNGIBLE								--char](20) NOT NULL
	,SINFO."IdMonedaEmision" AS COD_EMISION 						--char](20) NOT NULL
	,SINFO."IdInstrumento" AS COD_TITULO							--char](10) NOT NULL
	,'' AS GEN_MOVTO 												--char](1) NOT NULL ???	
	,'' AS IND_EXIMIDO 												--char](1) NOT NULL ???
	,CASE WHEN SINFO."CodigoIFRS" = 'CUS' THEN 1			
		  ELSE 0
	  END AS CODCATIFRSID											--tinyint] NOT NULL (no sé si va 1)
	
FROM (
	SELECT  "Modulo","Folio","FechaIngresoRegistro","FechaOperacion","FechaVencimientoOperacion","FechaLiquidacion","Plazo",
			"IdCondicionLiquidacion","IdEstado","IdCliente","IdPortafolio","IdClienteCorredora","IdPortafolioCorredora",
			"IdTipoOperacion","IdTipoProducto","IdConcepto","IdTipoComprobante","FolioComprobante","IdMesa",
			"IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","FechaEmisionInstrumento","IdMonedaEmision",
			"IdMonedaOperacion","IdEmisor","Nemotecnico","EsFungible","IdBolsa","IdBoveda","IdAgente","IdSucursal",
			"IdTipoReajuste","Cantidad","ValorRescate",
			"PrecioCompra","TasaCompra","TasaAgente","TasaEmision","MontoCompra","PrecioMercado","CodigoIFRS",
			"FolioTransaccion","IdMedioDePago","EmitidoA","Comision","Gastos"
	FROM v_Custodia
	UNION
	SELECT  "Modulo","Folio","FechaIngresoRegistro","FechaOperacion","FechaVencimientoOperacion","FechaLiquidacion","Plazo",
			"IdCondicionLiquidacion","IdEstado","IdCliente","IdPortafolio","IdClienteCorredora","IdPortafolioCorredora",
			"IdTipoOperacion","IdTipoProducto","IdConcepto","IdTipoComprobante","FolioComprobante","IdMesa",
			"IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","FechaEmisionInstrumento","IdMonedaEmision",
			"IdMonedaOperacion","IdEmisor","Nemotecnico","EsFungible","IdBolsa","IdBoveda","IdAgente","IdSucursal",
			"IdTipoReajuste","Cantidad","ValorRescate",
			"PrecioCompra","TasaCompra","TasaAgente","TasaEmision","MontoCompra","PrecioMercado","CodigoIFRS",
			"FolioTransaccion","IdMedioDePago","EmitidoA","Comision","Gastos"
	FROM v_TitulosNominales
	UNION
	SELECT  "Modulo","Folio","FechaIngresoRegistro","FechaOperacion","FechaVencimientoOperacion","FechaLiquidacion","Plazo",
			"IdCondicionLiquidacion","IdEstado","IdCliente","IdPortafolio","IdClienteCorredora","IdPortafolioCorredora",
			"IdTipoOperacion","IdTipoProducto","IdConcepto","IdTipoComprobante","FolioComprobante","IdMesa",
			"IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","FechaEmisionInstrumento","IdMonedaEmision",
			"IdMonedaOperacion","IdEmisor","Nemotecnico","EsFungible","IdBolsa","IdBoveda","IdAgente","IdSucursal",
			"IdTipoReajuste","Cantidad","ValorRescate",
			"PrecioCompra","TasaCompra","TasaAgente","TasaEmision","MontoCompra","PrecioMercado","CodigoIFRS",
			"FolioTransaccion","IdMedioDePago","EmitidoA","Comision","Gastos"
	FROM v_OperacionesAPlazo
	UNION
	SELECT  "Modulo","Folio","FechaIngresoRegistro","FechaOperacion","FechaVencimientoOperacion","FechaLiquidacion","Plazo",
			"IdCondicionLiquidacion","IdEstado","IdCliente","IdPortafolio","IdClienteCorredora","IdPortafolioCorredora",
			"IdTipoOperacion","IdTipoProducto","IdConcepto","IdTipoComprobante","FolioComprobante","IdMesa",
			"IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","FechaEmisionInstrumento","IdMonedaEmision",
			"IdMonedaOperacion","IdEmisor","Nemotecnico","EsFungible","IdBolsa","IdBoveda","IdAgente","IdSucursal",
			"IdTipoReajuste","Cantidad","ValorRescate",
			"PrecioCompra","TasaCompra","TasaAgente","TasaEmision","MontoCompra","PrecioMercado","CodigoIFRS",
			"FolioTransaccion","IdMedioDePago","EmitidoA","Comision","Gastos"
	FROM v_Tesoreria
	UNION
	SELECT  "Modulo","Folio","FechaIngresoRegistro","FechaOperacion","FechaVencimientoOperacion","FechaLiquidacion","Plazo",
			"IdCondicionLiquidacion","IdEstado","IdCliente","IdPortafolio","IdClienteCorredora","IdPortafolioCorredora",
			"IdTipoOperacion","IdTipoProducto","IdConcepto","IdTipoComprobante","FolioComprobante","IdMesa",
			"IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","FechaEmisionInstrumento","IdMonedaEmision",
			"IdMonedaOperacion","IdEmisor","Nemotecnico","EsFungible","IdBolsa","IdBoveda","IdAgente","IdSucursal",
			"IdTipoReajuste","Cantidad","ValorRescate",
			"PrecioCompra","TasaCompra","TasaAgente","TasaEmision","MontoCompra","PrecioMercado","CodigoIFRS",
			"FolioTransaccion","IdMedioDePago","EmitidoA","Comision","Gastos"
	FROM v_CuentaInversion
	UNION
	SELECT  "Modulo","Folio","FechaIngresoRegistro","FechaOperacion","FechaVencimientoOperacion","FechaLiquidacion","Plazo",
			"IdCondicionLiquidacion","IdEstado","IdCliente","IdPortafolio","IdClienteCorredora","IdPortafolioCorredora",
			"IdTipoOperacion","IdTipoProducto","IdConcepto","IdTipoComprobante","FolioComprobante","IdMesa",
			"IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","FechaEmisionInstrumento","IdMonedaEmision",
			"IdMonedaOperacion","IdEmisor","Nemotecnico","EsFungible","IdBolsa","IdBoveda","IdAgente","IdSucursal",
			"IdTipoReajuste","Cantidad","ValorRescate",
			"PrecioCompra","TasaCompra","TasaAgente","TasaEmision","MontoCompra","PrecioMercado","CodigoIFRS",
			"FolioTransaccion","IdMedioDePago","EmitidoA","Comision","Gastos"
	FROM v_Facturacion
) AS SINFO
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = SINFO."IdCliente"
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORT ON PORT."IdPortafolio" = SINFO."IdPortafolio"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER ON PER."IdPersona" = CLI."IdPersona"

LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CORR ON CORR."IdCliente" = SINFO."IdClienteCorredora"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PCORR ON PCORR."IdPersona" = CORR."IdPersona"
LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" AS MES ON MES."IdMesa" = SINFO."IdMesa"
LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS CODLIQ ON  CODLIQ."IdCondicionDeLiquidacion" = SINFO."IdCondicionLiquidacion"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Homologacion.BD.Vistas::HomologaTBCTMVTO" TO  BCS_DESARROLLADOR WITH GRANT OPTION;