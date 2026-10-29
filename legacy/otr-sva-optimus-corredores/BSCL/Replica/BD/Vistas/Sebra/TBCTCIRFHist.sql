SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTCIRFHist"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTCIRFHist" AS

SELECT
		TO_DATE("FechaRespaldo") AS "FechaRespaldo"
		, LEFT("FechaRespaldo",7) AS COD_PER			--char	7
		, SCUST."IdCliente",SCUST."IdPortafolio",SCUST."IdInstrumento"
		, SCUST."FolioCustodia" AS SEC_CAR_INVER								--decimal	0
		, COALESCE("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(DOC."DocumentoIdentidad"),'0-0') AS RUT_CLI
		, POR."CodigoPortafolio" AS SEC_RUT_CLI								--decimal	0
		, CAST(SCUST."Nemotecnico" AS VARCHAR(16)) AS NEMO										--char	16
		, min(TO_VARCHAR(SCUST."FechaOperacion", 'YYYY-MM-DD'))   AS FEC_INVER								--char	10
		, max(CASE WHEN SCUST."IdTipoInstrumento" IN ('BCP','BCU','BCD') THEN  'CE' WHEN SCUST."IdTipoInstrumento" IN ('BEM') THEN 'BE' ELSE 'DP' END) AS TIPO_FAM														--char	2
		--MONTO ACTUAL A TIR COMPRA
		, CAST(sum(SCUST."MontoCartera") AS DECIMAL(17,2)) AS MONTO_VAL_DISP											--decimal	0
		, max(SCUST."IdEmisor") AS EMISOR											--char	10
		, max(replace(CAST(SCUST."IdTipoReajuste" AS VARCHAR(2)), 'CL' , 'NR'))  AS TIPO_REAJ
		, sum(SCUST."Cantidad" ) AS CAN_ACTUAL								--decimal	0
		, sum(SCUST."Cantidad_Compra_Plazo") AS CAN_COM_PLAZO						--decimal	0
		, sum(SCUST."CantidadGarantia") AS CAN_GARANTIA							--decimal	0
		, 0 AS CAN_ORD_VEN													--decimal	0
		, 0 AS CAN_TRAMITE_COM												--decimal	0
		, 0 AS CAN_TRAMITE_VEN												--decimal	0
		, max(CASE WHEN SCUST."IdBoveda" = '' THEN 'COR' ELSE SCUST."IdBoveda" END )  AS COD_BOVEDA					--char	3
		,'' AS COD_RESERVA													--char	4
		, max(TO_VARCHAR(SCUST."FechaEmisionInstrumento", 'YYYY-MM-DD')) AS FEC_EMI							--char	10
		,'1900-01-01' AS FEC_RESERVA													--char	10
		, max(TO_VARCHAR("FechaVencimientoInstrumento", 'YYYY-MM-DD'))  AS FEC_VCTO														--char	10
		, max(TO_VARCHAR("FechaVencimientoInstrumento", 'YYYY-MM-DD'))  AS FEC_VCTO_COM													--char	10
		, max(SCUST."FolioComprobante" ) AS FOLIO_COMP_ADJ							--decimal	0
		,'S' AS IND_DIVISIBLE												--char	1
		,'N' AS IND_ING_CORTES												--char	1
		, 0 AS INT_ACUM_GAN_CP												--decimal	0
		, 0 AS INT_ACUM_GAN_VP												--decimal	0
		, sum(SCUST."MontoCartera") AS INT_DIA_INV_DIS												--decimal	0
		, 0 AS LIN_COMP_ADJ													--decimal	0-???? Pendiente
		--MONTO MERCADO
		, CAST(sum(SCUST."MontoCartera") AS DECIMAL(17,2)) AS MONTO_ACT_AJUS						--decimal	0
		, 0 AS MONTO_ACUM_COMP												--decimal	0
		, 0 AS MONTO_ACUM_SPRE												--decimal	0
		, 0 AS MONTO_ACUM_VEN												--decimal	0
		, 0 AS MONTO_CUP													--decimal	0
		, 0 AS MONTO_CUP_CP													--decimal	0
		--MONTO COMPRA
		, CAST(sum(SCUST."MontoCompra") AS DECIMAL(17,2)) AS MONTO_INI_AJUS												--decimal	0
		, 0 AS MONTO_VAL_DISP_MAS											--decimal	0
		, 0 AS MONTO_VAL_DISP_MEN											--decimal	0
		, max(CASE WHEN  DAYS_BETWEEN ("FechaOperacion", "FechaVencimientoInstrumento") > 9999 THEN 9999 ELSE  DAYS_BETWEEN ("FechaOperacion", "FechaVencimientoInstrumento") END)  AS PLAZO
		, avg(SCUST."INGPrecioMercadoActual") AS PRECIO_MED_CIE												--decimal	0
		,  CASE WHEN SUM("CantidadGarantia" + "Cantidad" + "Cantidad_Compra_Plazo") over (partition by DOC."DocumentoIdentidad",POR."CodigoPortafolio",SCUST."Nemotecnico")	 = 0 THEN 0 
			ELSE to_decimal(
	   	   ROUND(
	        SUM(("CantidadGarantia" + "Cantidad" + "Cantidad_Compra_Plazo") * "PrecioCompra") over (partition by DOC."DocumentoIdentidad",POR."CodigoPortafolio",SCUST."Nemotecnico")	
               /SUM("CantidadGarantia" + "Cantidad" + "Cantidad_Compra_Plazo") over (partition by DOC."DocumentoIdentidad",POR."CodigoPortafolio",SCUST."Nemotecnico") 
           ,4,ROUND_HALF_UP)
	     ,15,4) end
	     AS PRECIO_PROM_COMP
		, 0 AS QUIEN_RESERVA												--decimal	0
		, max("TipoCartera")  AS TIPO_CARTERA													--char	2
		, max(CASE SCUST."IdTipoComprobante" WHEN 'CUS' THEN 'IT' WHEN 'FAE' THEN 'FD' WHEN 'MES' THEN 'MA' ELSE 'IT' END) AS TIPO_COMP_ADJ	--char	2
		, avg(CASE WHEN SCUST."TasaMercado"  > 999 THEN 999 ELSE SCUST."TasaMercado"  END )  AS TIR_MED_CIE													--decimal	0
		, CASE WHEN SUM("CantidadGarantia" + "Cantidad" + "Cantidad_Compra_Plazo") over (partition by DOC."DocumentoIdentidad",POR."CodigoPortafolio",SCUST."Nemotecnico")	 = 0 THEN 0 
			ELSE
				to_decimal(
					ROUND(
	        			SUM(("CantidadGarantia" + "Cantidad" + "Cantidad_Compra_Plazo") * "TasaCompra") over (partition by DOC."DocumentoIdentidad",POR."CodigoPortafolio",SCUST."Nemotecnico")	
               			/SUM("CantidadGarantia" + "Cantidad" + "Cantidad_Compra_Plazo") over (partition by DOC."DocumentoIdentidad",POR."CodigoPortafolio",SCUST."Nemotecnico") 
           			,4,ROUND_HALF_UP)
	     		,15,4) 
	     	END as TIR_PROM_COMP
		, '' AS VTO_COMP_ADJ													--char	10
		, COALESCE(max("ValorClasificacionRiesgo"),'') AS CLASIF_RIESGO												--char	10
		, coalesce(max(CAST(TRIM(DOCEMI."DocumentoIdentidad") AS VARCHAR(11))),'') AS RUT_EMISOR					--char	11
		, 0 AS DURATION														--decimal	0
		, 0 AS SEC_CAR_HISTO												--decimal	0
		,'' AS ORIGEN_RECURSOS												--char	1
		,'' AS CLASIF_CARTERA 												--char	1
		, 0 AS CODCATIFRSID 												--tinyint	0
		, avg(CASE WHEN SCUST."TasaMercado"  > 999 THEN 999 ELSE SCUST."TasaMercado"  END ) AS TIR_MED_CIE_ANT												--decimal	0
		, sum("INGMontoATasaMercadoAnterior") AS MTO_VAL_DISP_ANT												--decimal	0
		, max("ValorParidadCierre") AS PARIDAD_MORIGEN												--decimal	0
		, sum("MontoCompraMonedaInstrumento")  AS CAPTACION_MORIGEN											--decimal	0
		, max(SCUST."IdMonedaInstrumento") AS MON_MORIGEN													--char	3
		, 0 AS CAPT_MORIGEN_MCDO											--decimal	0
		, 0 AS MTO_VAL_TASA_MCDO											--decimal	0
		, 0 AS MTO_VAL_TASA_COMP											--decimal	0
		, max("NUMERO_CUPON") AS "NUMERO_CUPON"
		, sum("MontoInteresCorteCupon") AS "MontoInteresCorteCupon"
	FROM (
		SELECT 
				"FechaRespaldo",
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
				"IdClasificacionTipoInstrumento",
				"CodigoIFRS",
				"FechaEmisionInstrumento",
				"FechaVencimientoInstrumento",
				"IdTipoComprobante",
				"FolioComprobante",
				"FechaComprobante",
				"INGCantidad" AS "Cantidad",
				"INGMontoATasaCompraActual" AS "MontoCompra",
				"INGTasaCompra" AS "TasaCompra", 
				"INGTasaMercadoActual" AS "TasaMercado",
				"INGMontoATasaMercadoActual" AS "MontoCartera",
				"ValorParidadCierre",
				"MontoCompraMonedaInstrumento",
				"IdMonedaInstrumento",
				"INGTasaMercadoAnterior",
				"INGMontoATasaMercadoAnterior",
				c."INGPrecioMercadoActual",
				c."INGPrecioPromedioCompra",
				c."INGTasaMercadoActual",
				c."INGTasaCompra",
				c."INGPrecioCompra" AS "PrecioCompra",
				0 AS "Cantidad_Compra_Plazo",
				0 AS "Cantidad_Venta_Plazo",
				0 AS "CantidadGarantia",
				CASE WHEN "IdTipoOperacion" = 'VENPAC' THEN 'CC' ELSE '' END  AS "TipoCartera",
				"INGNumeroCupon" AS "NUMERO_CUPON",
				 "INGMontoInteresCorteCupon"  AS  "MontoInteresCorteCupon"
			FROM "BSCL.Custodia.BD::HIS.Cartera" AS c
			WHERE "IdEstado" = 'VIG' AND "IdClasificacionTipoInstrumento" = 'RF'
			and "FolioCartera" != 0
			UNION ALL
			SELECT 
				"FechaRespaldo",
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
				"IdClasificacionTipoInstrumento",
				"CodigoIFRS",
				ins."FechaEmision" AS "FechaEmisionInstrumento",
				ins."FechaVencimiento" AS "FechaVencimientoInstrumento",
				"IdTipoComprobante",
				"FolioComprobante",
				'0001-01-01' AS "FechaComprobante",
				-1*"Cantidad" AS "Cantidad",
				0 AS "MontoCompra", --CLP
				"TasaOperacion" AS "TasaCompra", 
				"TasaMercado" AS "TasaMercado",
				-1*"MontoMercadoMonedaSistema" AS "MontoCartera", --CLP
				0 AS "ValorParidadCierre",
				-1*"MontoOperacionMonedaInstrumento" AS "MontoCompraMonedaInstrumento",
				"IdMonedaInstrumento",
				0 AS "INGTasaMercadoAnterior",
				0 AS "INGMontoATasaMercadoAnterior",
				car."PrecioMercado" AS "INGPrecioMercadoActual",
				0 AS "INGPrecioPromedioCompra",
				car."TasaMercado" AS "INGTasaMercadoActual",
				car."TasaOperacion" AS "INGTasaCompra",
				car."PrecioOperacion" AS "PrecioCompra",
				0 AS "Cantidad_Compra_Plazo",
				0 AS "Cantidad_Venta_Plazo",
				0 AS "CantidadGarantia",
				CASE WHEN "IdTipoOperacion" = 'VENPAC' THEN 'CC' ELSE '' END  AS "TipoCartera",
				0 AS "NUMERO_CUPON",
				0 AS  "MontoInteresCorteCupon"
			FROM "BSCL.Custodia.BD::HIS.CarteraNegativa" AS car 
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins on ins."IdInstrumento" = car."IdInstrumento"
			LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tins on tins."IdTipoInstrumento" = ins."IdTipoInstrumento"
			WHERE car."IdEstado" = 'VIG' AND "IdClasificacionTipoInstrumento" = 'RF'
			UNION ALL
			SELECT 
				s."FechaRespaldo",
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
				s."IdClasificacionTipoInstrumento",
				'' AS "CodigoIFRS",
				s."FechaEmisionInstrumento",
				s."FechaVencimientoInstrumento",
				c."IdTipoComprobante",
				c."FolioComprobante",
				c."FechaComprobante",
				CASE WHEN C."IdTipoOperacion" in ('VENPAC') THEN S."INGCantidad" ELSE 0  END  AS "Cantidad", ---"INGCantidad"
				s."INGMontoATasaCompraActual" AS "MontoCompra",
				c."INGTasaCompra" AS "TasaCompra", 
				s."INGTasaMercadoActual" AS "TasaMercado",
				S."INGMontoATasaSubyacenteActual" AS "MontoCartera",
				c."ValorParidadCierre",
				s."INGMontoCompraAnteriorMonedaInstrumento" AS "MontoCompraMonedaInstrumento",
				s."IdMonedaInstrumento",
				0 AS "INGTasaMercadoAnterior",
				0 AS "INGMontoATasaMercadoAnterior",
				s."INGPrecioMercadoActual",
				s."PrecioPromedioCompraOperacionRelacionada" AS "INGPrecioPromedioCompra",
				s."INGTasaMercadoActual",
				s."TasaCompraOperacionRelacionada" AS "INGTasaCompra",
				c."INGPrecioCompra" AS "PrecioCompra",
			   CASE WHEN C."IdTipoOperacion"  in ('VENPAC') THEN 0 ELSE S."INGCantidad" END AS "Cantidad_Compra_Plazo",
			   CASE WHEN C."IdTipoOperacion"  in ('COMPAC')  THEN 0 ELSE S."INGCantidad" END AS "Cantidad_Venta_Plazo",
			   0 AS "CantidadGarantia",-- , C."IdTipoOperacion"
			   '' AS "TipoCartera" ,-- , C."IdTipoOperacion"
			   c."INGNumeroCupon" AS "NUMERO_CUPON",
				 c."INGMontoInteresCorteCupon"  AS  "MontoInteresCorteCupon"
			FROM "BSCL.Custodia.BD::HIS.Subyacente" as S 
			INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" as C on S."FolioCarteraRelacionada" = C."FolioCartera"
				AND S."FechaRespaldo" = C."FechaRespaldo"
			WHERE S."IdEstado" = 'VIG' AND c."IdEstado" = 'VIG'
			  AND S."IdClasificacionTipoInstrumento" = 'RF' -- AND "IdClienteRelacionado" = 6069  
			  AND C."IdTipoInstrumento" IN ('PAC')	AND C."IdTipoOperacion" != 'VENPAC'	
			UNION all
			SELECT 
				s."FechaRespaldo",
				s."FolioSubyacente" AS "FolioCustodia",
				s."INGFechaOperacion",
				c."IdClienteRelacionado",
				c."IdPortafolioRelacionado",
				s."IdTipoInstrumento",
				s."IdInstrumento",
				s."IdEmisor",
				s."Nemotecnico",
				s."NemoSVS",
				'' AS "IdBoveda",
				s."IdTipoReajuste",
				c."PlazoEnDiasOperacion",
				s."IdClasificacionTipoInstrumento",
				'' AS "CodigoIFRS",
				s."FechaEmisionInstrumento",
				s."FechaVencimientoInstrumento",
				c."IdTipoComprobante",
				c."FolioComprobante",
				c."FechaComprobante",
				CASE WHEN C."IdTipoOperacion" in ('VENPAC') THEN 0 ELSE  S."INGCantidad" END  AS "Cantidad", ---"INGCantidad"
				s."INGMontoATasaCompraActual" AS "MontoCompra",
				c."INGTasaCompra" AS "TasaCompra", 
				s."INGTasaMercadoActual" AS "TasaMercado",
				S."INGMontoATasaSubyacenteActual" AS "MontoCartera",
				c."ValorParidadCierre",
				s."INGMontoCompraAnteriorMonedaInstrumento" AS "MontoCompraMonedaInstrumento",
				s."IdMonedaInstrumento",
				0 AS "INGTasaMercadoAnterior",
				0 AS "INGMontoATasaMercadoAnterior",
				s."INGPrecioMercadoActual",
				s."PrecioPromedioCompraOperacionRelacionada" AS "INGPrecioPromedioCompra",
				s."INGTasaMercadoActual",
				s."TasaCompraOperacionRelacionada" AS "INGTasaCompra",
				c."INGPrecioCompra" AS "PrecioCompra",
			   CASE WHEN C."IdTipoOperacion"  in ('VENSIM','VENPAC')THEN S."INGCantidad" ELSE 0 END AS "Cantidad_Compra_Plazo",
			   CASE WHEN C."IdTipoOperacion"  in ('COMSIM','COMPAC')  THEN S."INGCantidad" ELSE 0 END AS "Cantidad_Venta_Plazo",
			   0 AS "CantidadGarantia",
			   'CC' AS "TipoCartera", -- , C."IdTipoOperacion"
			   c."INGNumeroCupon" AS "NUMERO_CUPON",
				c."INGMontoInteresCorteCupon"  AS  "MontoInteresCorteCupon"
			FROM "BSCL.Custodia.BD::HIS.Subyacente" as S 
			INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" as C on S."FolioCarteraRelacionada" = C."FolioCartera"
				AND S."FechaRespaldo" = C."FechaRespaldo"
			WHERE S."IdEstado" = 'VIG' AND c."IdEstado" = 'VIG'
			  AND S."IdClasificacionTipoInstrumento" = 'RF' -- AND "IdClienteRelacionado" = 6069  
			  AND C."IdTipoOperacion"  in ('VENPAC')	
			UNION ALL
			SELECT 
				G."FechaRespaldo",
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
			   g."IdClasificacionTipoInstrumento",
			   g."CodigoIFRS",
			   g."FechaEmisionInstrumento",
			   g."FechaVencimientoInstrumento",
			   g."IdTipoComprobante",
			   g."FolioComprobante",
			   g."FechaComprobante",
				0 AS "Cantidad",
				g."INGMontoATasaCompraActual" AS "MontoCompra",
			    g."TasaCompraOperacionOrigen" AS "TasaCompra", 
			    g."INGTasaMercadoActual" AS "TasaMercado",
				g."INGMontoATasaMercadoActual" AS "MontoCartera",
			   	0 AS "ValorParidadCierre",
				g."INGMontoATasaCompraActual" AS "MontoCompraMonedaInstrumento",
				g."IdMonedaInstrumento",
				0 as "INGTasaMercadoAnterior",
				0 as "INGMontoATasaMercadoAnterior",
				"INGPrecioMercadoActual",
				"PrecioPromedioCompraOperacionOrigen" AS "INGPrecioPromedioCompra",
				"INGTasaMercadoActual",
				"TasaCompraOperacionOrigen" AS "INGTasaCompra",
				"PrecioCompraOperacionOrigen" AS "PrecioCompra",
				0 AS "Cantidad_Compra_Plazo",
			    0 AS "Cantidad_Venta_Plazo",
			    G."INGCantidad" AS "CantidadGarantia",
			      '' AS "TipoCartera" , 
			    g."INGNumeroCupon" AS "NUMERO_CUPON",
				 g."INGMontoInteresCorteCupon"  AS  "MontoInteresCorteCupon"
			FROM "BSCL.Custodia.BD::HIS.Garantia" AS G
			WHERE G."IdEstado" = 'VIG'
			  AND G."IdClasificacionTipoInstrumento" = 'RF'
		UNION ALL
		SELECT 
				t."FechaRespaldo",
				t."IdMovimientoCarteraEnTransito" AS "FolioCustodia",
				t."FechaOperacion" AS "FechaOperacion",
				t."IdCliente",
				t."IdPortafolio",
				t."IdTipoInstrumento",
				t."IdInstrumento",
				t."IdEmisor",
			    t."Nemotecnico",
			    t."NemoSVS",
			    '' AS "IdBoveda",
			    t."IdTipoReajuste",
			    0 AS "PlazoEnDiasOperacion",
			   t."IdClasificacionTipoInstrumento",
			   t."CodigoIFRS",
			   t."FechaEmisionInstrumento",
			   t."FechaVencimientoInstrumento",
			   t."IdTipoComprobante",
			   t."FolioComprobante",
			   t."FechaOperacion",
				t."Cantidad" * -1 AS "Cantidad",
				t."MontoCompra" AS "MontoCompra",
			    t."TasaCompra" AS "TasaCompra", 
			     t."TasaCompra" AS "TasaMercado",
				t."MontoCompra" AS "MontoCartera",
			   	0 AS "ValorParidadCierre",
				t."MontoCompra" AS "MontoCompraMonedaInstrumento",
				t."IdMonedaInstrumento",
				0 as "INGTasaMercadoAnterior",
				0 as "INGMontoATasaMercadoAnterior",
				t."PrecioCompra" AS "INGPrecioMercadoActual",
				t."PrecioCompra" AS "INGPrecioPromedioCompra",
				t."TasaCompra" AS "INGTasaMercadoActual",
				t."TasaCompra" AS "INGTasaCompra",
				"PrecioCompra" AS "PrecioCompra",
				0 AS "Cantidad_Compra_Plazo",
			    0 AS "Cantidad_Venta_Plazo",
			    0 AS "CantidadGarantia",
			      '' AS "TipoCartera", 
			    0 AS "NUMERO_CUPON",
				0  AS  "MontoInteresCorteCupon"
			FROM "BSCL.Custodia.BD::HIS.MovimientoCarteraEnTransito" AS t  JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS o
					ON (t."IdTipoOperacion" = o."IdTipoOperacion" AND "AccionCustodia" = 'RETIRO')
			WHERE  "IdEstado"  = 'PEN' AND "IdClasificacionTipoInstrumento" = 'RF' 
		) AS SCUST
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = SCUST."IdCliente"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = CLI."IdPersona"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON DOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS fec ON (fec."IdEmpresa" = 1 and "CodigoParametro" = 'PLA.FechaSistema')
		LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" AS EMI ON EMI."IdEmisor" = SCUST."IdEmisor"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PEREMI ON PEREMI."IdPersona" = EMI."IdPersona"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCEMI ON DOCEMI."IdDocumentoIdentidad" = PEREMI."IdDocumentoIdentidad"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdCliente" = CLI."IdCliente" AND SCUST."IdPortafolio" = por."IdPortafolio"
		LEFT JOIN (
			SELECT "IdInstrumento" ,STRING_AGG("ValorClasificacionRiesgo",' ; ') AS "ValorClasificacionRiesgo" 
			FROM "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" CLA 
			GROUP BY "IdInstrumento" 
			) CLA ON (CLA."IdInstrumento" = SCUST."IdInstrumento" AND SCUST."IdClasificacionTipoInstrumento" = 'RF')
		WHERE SCUST."FechaRespaldo" = '2025-03-31'
		GROUP BY LEFT(fec."Valor",7),
				SCUST."IdCliente",SCUST."IdPortafolio",SCUST."IdInstrumento",
				SCUST."FolioCustodia","CantidadGarantia" , "Cantidad","Cantidad_Compra_Plazo","PrecioCompra","TasaCompra",
				DOC."DocumentoIdentidad",
				POR."CodigoPortafolio" ,"FechaRespaldo",
				SCUST."Nemotecnico"--, "TipoCartera"--,"CantidadGarantia","Cantidad","PrecioCompra",SCUST."IdCliente",SCUST."IdPortafolio"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBCTCIRFHist" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
