SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTCIIFHist";
-- select  * from  "BSCL.Replica.BD.Vistas::ReplicaTBCTCIIFHist"  where SEC_CAR_INVER = 2188745

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTCIIFHist" AS

	WITH
	v_FechasCierre as (
		SELECT TO_VARCHAR("FechaRespaldo", 'YYYY-MM'),   max("FechaRespaldo" ) AS "FechaCierre"
						FROM "BSCL.Custodia.BD::HIS.Cartera" AS  c
								WHERE "IdEstado" = 'VIG'
								  AND "IdClasificacionTipoInstrumento" = 'IF'
								  AND "IdTipoInstrumento" NOT IN ('SIM','PAC','PRT','EMP','FFM')
								  GROUP BY TO_VARCHAR("FechaRespaldo", 'YYYY-MM')
	)
	SELECT
		DISTINCT max(WST."IdStack") AS "IDSTACK",
		 "Periodo"  || ':' || cast(SCUST."FolioCustodia" AS varchar)  || ':' ||
		trim(COALESCE("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(PER."DocumentoIdentidad"),'0-0')) || ':' || 
		rtrim(ltrim(POR."CodigoPortafolio" )) || ':' || 
		trim(CAST(SCUST."Nemotecnico" AS VARCHAR(16))) || ':' || trim(CAST(SCUST."NemoSVS" AS VARCHAR(16))) AS TABLE_KEY,
		max(CLI."IdCliente") AS "IdCliente",

		 "Periodo" AS COD_PER
		
		,SCUST."FolioCustodia" AS SEC_CAR_INVER
		,COALESCE("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(PER."DocumentoIdentidad"),'0-0') AS RUT_CLI
		
		,POR."CodigoPortafolio" AS SEC_RUT_CLI
		
		,max(SCUST."IdEmisor") AS EMISOR
		
		,max(replace(CAST(SCUST."IdTipoReajuste" AS VARCHAR(2)), 'CL' , 'NR'))  AS TIPO_REAJ
		,max(TO_VARCHAR("FechaVencimientoInstrumento", 'YYYY-MM-DD')) AS FEC_VCTO
		,sum(SCUST."MontoCartera") AS MONTO_VAL_DISP
		
		,max(CASE WHEN SCUST."IdEmisor" = 'CENTRAL' THEN  'CE' ELSE 'DP' END) AS TIPO_FAM
		,max(CASE WHEN SCUST."Cantidad" != 0 THEN 1 ELSE 0 END)   AS CAN_ACTUAL --  "Cantidad"
		
		,max(CASE WHEN SCUST."IdBoveda" = '' THEN 'DCV' ELSE SCUST."IdBoveda" END ) AS COD_BOVEDA
		, 0.0 AS CAN_TRAMITE_COM
		
		,max(CASE WHEN SCUST."Cantidad_Compra_Plazo" != 0 THEN 1 ELSE 0 END)  AS CAN_COM_PLAZO
		
		
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
		, 0.0 AS LIN_COMP_ADJ
		
		,sum(SCUST."MontoCartera") AS MONTO_ACT_AJUS
		
		,sum(SCUST."MontoCompra") AS MONTO_INI_AJUS
		, 0.0 AS MONTO_VAL_DISP_MAS
		, 0.0 AS MONTO_VAL_DISP_MEN
		
		,trim(CAST(SCUST."Nemotecnico" AS VARCHAR(16))) AS NEMO
		
		,max(CASE WHEN  DAYS_BETWEEN ("FechaOperacion", "FechaVencimientoInstrumento") > 9999 THEN 9999 ELSE  DAYS_BETWEEN ("FechaOperacion", "FechaVencimientoInstrumento") END)  AS PLAZO
		
		, 0.0 AS QUIEN_RESERVA
		, max("TasaMercado") AS TASA_MED_CIE
		, max("TasaCompra") AS TASA_PROM_COMP
		, "TipoCartera" AS TIPO_CARTERA
		
		,max(CAST(SUBSTRING(SCUST."IdTipoComprobante",0,2) AS VARCHAR(2))) AS TIPO_COMP_ADJ
		
		,sum(SCUST."ValorRescate") AS VAL_RESC
		
		,min(TO_VARCHAR(SCUST."FechaOperacion", 'YYYY-MM-DD')) AS VTO_COMP_ADJ
		,CAST(SCUST."NemoSVS" AS VARCHAR(16)) AS NEMO_SVS
		
		,max(COALESCE( CAST(TRIM(PEREMI."DocumentoIdentidad") AS VARCHAR(11)) , '')) AS RUT_EMISOR
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
		SELECT 
				TO_VARCHAR(c."FechaRespaldo", 'YYYY-MM') AS  "Periodo",
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
			  -- CASE WHEN COALESCE ("FolioExternoReferencia",0) = 0 THEN  "FolioComprobante" ELSE "FolioExternoReferencia" END AS "FolioComprobante",
			  "FolioComprobante",
			   "FechaComprobante",
			   
				"INGCantidad" AS "Cantidad",
				"INGValorRescate" AS "ValorRescate",
				"INGMontoATasaCompraActual" AS "MontoCompra",
			    "INGTasaCompra" AS "TasaCompra", 
			    "INGTasaMercadoActual" AS "TasaMercado",
				"INGMontoATasaMercadoActual" AS "MontoCartera",
				   
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
			   CASE WHEN "IdTipoOperacion" = 'VENPAC' THEN 'CC' ELSE '' END  AS "TipoCartera"
			
			FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" AS c
					INNER JOIN v_FechasCierre AS t 	ON ( t."FechaCierre" =   c."FechaRespaldo")
			WHERE "IdEstado" = 'VIG'
			  AND "IdClasificacionTipoInstrumento" = 'IF'
			  AND "IdTipoInstrumento" NOT IN ('SIM','PAC','PRT','EMP','FFM') -- AND "IdCliente" = 7672
			  AND "TipoCarteraCorredor"  != 'CC'
	
			UNION ALL
			
			SELECT --C."IdTipoOperacion",
				TO_VARCHAR(c."FechaRespaldo", 'YYYY-MM') AS  "Periodo",
				s."FolioCarteraRelacionada" AS "FolioCustodia",
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
				   
				CASE WHEN C."IdTipoOperacion" in ('VENSIM','VENPAC') THEN S."INGCantidad" ELSE 0  END  AS "Cantidad", ---"INGCantidad"
				CASE WHEN C."IdTipoOperacion" in ('VENSIM','VENPAC') THEN S."INGValorRescate" ELSE 0 END  AS "ValorRescate",	--"INGValorRescate"
				
				s."INGMontoATasaCompraActual" AS "MontoCompra",
				c."INGTasaCompra" AS "TasaCompra", 
				s."INGTasaMercadoActual" AS "TasaMercado",
				S."INGMontoATasaSubyacenteActual" AS "MontoCartera",
				   
				c."ValorParidadCierre",
				s."INGMontoCompraAnteriorMonedaInstrumento" AS "MontoCompraMonedaInstrumento",
				s."IdMonedaInstrumento",
				
				0 AS "INGTasaMercadoAnterior",
				0 AS "INGMontoATasaMercadoAnterior",
				
			   CASE WHEN C."IdTipoOperacion"  in ('VENSIM','VENPAC')THEN 0 ELSE S."INGCantidad" END AS "Cantidad_Compra_Plazo",
			   CASE WHEN C."IdTipoOperacion"  in ('VENSIM','VENPAC')  THEN 0 ELSE S."INGValorRescate" END AS "ValorRescate_Compra_Plazo",
			   
			   CASE WHEN C."IdTipoOperacion"  in ('COMSIM','COMPAC')  THEN 0 ELSE S."INGCantidad" END AS "Cantidad_Venta_Plazo",
			   CASE WHEN C."IdTipoOperacion" in ('COMSIM','COMPAC') THEN 0 ELSE S."INGValorRescate" END AS "ValorRescate_Venta_Plazo",
			   
			   0 AS "CantidadGarantia",
			   '' AS "TipoCartera"-- , C."IdTipoOperacion"
			
			FROM bscl."BSCL.Custodia.BD::HIS.Subyacente" as S 
					INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" as C on S."FolioCarteraRelacionada" = C."FolioCartera"
					INNER JOIN v_FechasCierre AS t 	ON ( t."FechaCierre" =   c."FechaRespaldo" AND t."FechaCierre" =   s."FechaRespaldo")
			WHERE S."IdEstado" = 'VIG' AND c."IdEstado" = 'VIG'
			  AND S."IdClasificacionTipoInstrumento" = 'IF' -- AND "IdCliente" = 7672  
			  AND C."IdTipoInstrumento" IN ('SIM','PAC') AND C."IdTipoOperacion" != 'VENPAC'
			  
			  
	UNION ALL
			
			SELECT 
				TO_VARCHAR(s."FechaRespaldo", 'YYYY-MM') AS  "Periodo",
				s."FolioCarteraRelacionada" AS "FolioCustodia",
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
				s."EsFungible",
				s."IdClasificacionTipoInstrumento",
				'' AS "CodigoIFRS",
				s."FechaEmisionInstrumento",
				s."FechaVencimientoInstrumento",
				   
				c."IdTipoComprobante",
--				CASE WHEN COALESCE ("FolioExternoReferencia",0) = 0 THEN  c."FolioComprobante" ELSE c."FolioExternoReferencia" END AS "FolioComprobante",
				"FolioComprobante",
				c."FechaComprobante",
				   
				CASE WHEN C."IdTipoOperacion" in ('VENSIM','VENPAC') THEN 0 ELSE  S."INGCantidad" END  AS "Cantidad", ---"INGCantidad"
				CASE WHEN C."IdTipoOperacion" in ('VENSIM','VENPAC') THEN 0 ELSE S."INGValorRescate" END  AS "ValorRescate",	--"INGValorRescate"
				
				s."INGMontoATasaCompraActual" AS "MontoCompra",
				c."INGTasaCompra" AS "TasaCompra", 
				s."INGTasaMercadoActual" AS "TasaMercado",
				S."INGMontoATasaSubyacenteActual" AS "MontoCartera",
				   
				c."ValorParidadCierre",
				s."INGMontoCompraAnteriorMonedaInstrumento" AS "MontoCompraMonedaInstrumento",
				s."IdMonedaInstrumento",
				
				0 AS "INGTasaMercadoAnterior",
				0 AS "INGMontoATasaMercadoAnterior",
				
			   CASE WHEN C."IdTipoOperacion"  in ('VENSIM','VENPAC')THEN S."INGCantidad" ELSE 0 END AS "Cantidad_Compra_Plazo",
			   CASE WHEN C."IdTipoOperacion"  in ('VENSIM','VENPAC')  THEN S."INGValorRescate" ELSE 0 END AS "ValorRescate_Compra_Plazo",
			   
			   CASE WHEN C."IdTipoOperacion"  in ('COMSIM','COMPAC')  THEN S."INGCantidad" ELSE 0 END AS "Cantidad_Venta_Plazo",
			   CASE WHEN C."IdTipoOperacion" in ('COMSIM','COMPAC') THEN S."INGValorRescate" ELSE 0 END AS "ValorRescate_Venta_Plazo",
			   
			   0 AS "CantidadGarantia",-- , C."IdTipoOperacion",
			   'CC' AS "TipoCartera"
			
			FROM bscl."BSCL.Custodia.BD::HIS.Subyacente" as S 
				INNER JOIN bscl."BSCL.Custodia.BD::HIS.Cartera" as C on S."FolioCarteraRelacionada" = C."FolioCartera"
				INNER JOIN v_FechasCierre AS t 	ON ( t."FechaCierre" =   c."FechaRespaldo" AND t."FechaCierre" =   s."FechaRespaldo")
			WHERE S."IdEstado" = 'VIG' AND c."IdEstado" = 'VIG'
			  AND S."IdClasificacionTipoInstrumento" = 'IF' -- AND "IdClienteRelacionado" =  6069
			  AND C."IdTipoInstrumento" IN ('SIM','PAC')	AND C."IdTipoOperacion"  in ('VENSIM','VENPAC')		
		--	  AND s."NemoSVS" = 'FNBCI-040619'
			    
				
	UNION ALL
			
	
			SELECT 
				TO_VARCHAR(g."FechaRespaldo", 'YYYY-MM') AS  "Periodo",
				G."FolioCarteraOrigen" AS "FolioCustodia",
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
			   
				g."INGCantidad" AS "Cantidad", 
				g."INGValorRescate" AS "ValorRescate",
				
				g."INGMontoATasaCompraActual" AS "MontoCompra",
			    g."TasaCompraOperacionOrigen" AS "TasaCompra", 
			    g."INGTasaMercadoActual" AS "TasaMercado",
				g."INGMontoATasaMercadoActual" AS "MontoCartera",
				   
			   	0 AS "ValorParidadCierre",
				g."INGMontoATasaCompraActual" AS "MontoCompraMonedaInstrumento",
				g."IdMonedaInstrumento",
				
				0 AS "INGTasaMercadoAnterior",
				0 AS "INGMontoATasaMercadoAnterior",
	
				0 AS "Cantidad_Compra",
			    0 AS "ValorRescate_Compra",
			    0 AS "Cantidad_Venta",
			    0 AS "ValorRescate_Venta",
			   
			   1 AS "CantidadGarantia",
			   '' AS "TipoCartera"
		
			FROM "BSCL.Custodia.BD::HIS.Garantia" AS G
			--INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS C ON C."FolioCartera" = G."FolioCarteraOrigen"
				INNER JOIN v_FechasCierre AS t 	ON ( t."FechaCierre" =   g."FechaRespaldo" )
			WHERE G."IdEstado" = 'VIG'
			  AND G."IdClasificacionTipoInstrumento" = 'IF'
			  AND G."IdTipoInstrumento" <> 'FFM'
			  AND G."INGFechaLiquidacion" <= TO_VARCHAR(CURRENT_TIMESTAMP, 'YYYY-MM-DD')
			
		) AS SCUST
			INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = SCUST."IdCliente"
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER ON PER."IdPersona" = CLI."IdPersona"
			INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS fec ON (fec."IdEmpresa" = 1 and "CodigoParametro" = 'PLA.FechaSistema')
			
			LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" AS EMI ON EMI."IdEmisor" = SCUST."IdEmisor"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PEREMI ON PEREMI."IdPersona" = EMI."IdPersona"
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdCliente" = CLI."IdCliente" AND SCUST."IdPortafolio" = por."IdPortafolio"
			left JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'TBCTCIIF' AND WST."Key" = SCUST."FolioCustodia"
		WHERE SCUST."ValorRescate"  <= 99999999999 AND  "Periodo"  !=  LEFT(fec."Valor",7)
		GROUP BY "TipoCartera" ,  "Periodo" ,
				SCUST."FolioCustodia",
				COALESCE("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(PER."DocumentoIdentidad"),'0-0') ,
				POR."CodigoPortafolio" ,
				trim(CAST(SCUST."Nemotecnico" AS VARCHAR(16))) ,
				CAST(SCUST."NemoSVS" AS VARCHAR(16)) 

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTBCTCIIF" TO  BCS_DESARROLLADOR WITH GRANT OPTION;