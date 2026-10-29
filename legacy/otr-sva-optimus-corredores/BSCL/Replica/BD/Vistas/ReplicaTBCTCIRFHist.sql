SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTCIRFHist"; 

/*
 SELECT count(1), cod_per
  FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCIRFHist" GROUP BY cod_per
*/
CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTCIRFHist" AS

	WITH
	v_FechasCierre as (
		SELECT TO_VARCHAR("FechaRespaldo", 'YYYY-MM'),   max("FechaRespaldo" ) AS "FechaCierre"
						FROM "BSCL.Custodia.BD::HIS.Cartera" AS  c
								WHERE "IdEstado" = 'VIG'
								  AND "IdClasificacionTipoInstrumento" = 'RF'
								  AND "IdTipoInstrumento" NOT IN ('SIM','PAC','PRT','EMP','FFM')
								  GROUP BY TO_VARCHAR("FechaRespaldo", 'YYYY-MM')
	)
	SELECT
		max(WST."IdStack") AS "IDSTACK",
		
		"Periodo"  || ':' ||
		cast(SCUST."FolioCustodia" AS varchar)  || ':' || 
		trim(COALESCE("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(PER."DocumentoIdentidad"),'0-0') ) || ':' ||
		trim(POR."CodigoPortafolio" ) || ':' || 
		trim(CAST(SCUST."Nemotecnico" AS VARCHAR(16)))  AS TABLE_KEY, 
		max(CLI."IdCliente") AS "IdCliente",
		
		
		"Periodo" AS COD_PER			--char	7
		 
		,SCUST."FolioCustodia" AS SEC_CAR_INVER								--decimal	0
		,COALESCE("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(PER."DocumentoIdentidad"),'0-0') AS RUT_CLI
		,POR."CodigoPortafolio" AS SEC_RUT_CLI								--decimal	0
		,trim(CAST(SCUST."Nemotecnico" AS VARCHAR(16))) AS NEMO										--char	16
		,min(TO_VARCHAR(SCUST."FechaOperacion", 'YYYY-MM-DD'))   AS FEC_INVER								--char	10
		,max(CASE WHEN SCUST."IdEmisor" = 'CENTRAL' THEN  'CE' ELSE 'DP' END) AS TIPO_FAM														--char	2
		,sum(SCUST."MontoCartera") AS MONTO_VAL_DISP												--decimal	0
		,max(SCUST."IdEmisor") AS EMISOR											--char	10
		
		,max(replace(CAST(SCUST."IdTipoReajuste" AS VARCHAR(2)), 'CL' , 'NR'))  AS TIPO_REAJ
		
		
		,sum(SCUST."Cantidad" ) AS CAN_ACTUAL								--decimal	0
		,sum(SCUST."Cantidad_Compra_Plazo") AS CAN_COM_PLAZO						--decimal	0
		,sum(SCUST."CantidadGarantia") AS CAN_GARANTIA							--decimal	0
		
		, 0 AS CAN_ORD_VEN													--decimal	0
		, 0 AS CAN_TRAMITE_COM												--decimal	0
		, 0 AS CAN_TRAMITE_VEN												--decimal	0
		,max(CASE WHEN SCUST."IdBoveda" = '' THEN 'DCV' ELSE SCUST."IdBoveda" END )  AS COD_BOVEDA					--char	3
		,'' AS COD_RESERVA													--char	4
		,max(TO_VARCHAR(SCUST."FechaEmisionInstrumento", 'YYYY-MM-DD')) AS FEC_EMI							--char	10
		,'1900-01-01' AS FEC_RESERVA													--char	10
		,max(TO_VARCHAR("FechaVencimientoInstrumento", 'YYYY-MM-DD'))  AS FEC_VCTO														--char	10
		,max(TO_VARCHAR("FechaVencimientoInstrumento", 'YYYY-MM-DD'))  AS FEC_VCTO_COM													--char	10
		,max(SCUST."FolioComprobante" ) AS FOLIO_COMP_ADJ							--decimal	0
		,'S' AS IND_DIVISIBLE												--char	1
		,'N' AS IND_ING_CORTES												--char	1
		, 0 AS INT_ACUM_GAN_CP												--decimal	0
		, 0 AS INT_ACUM_GAN_VP												--decimal	0
		, sum(SCUST."MontoCartera") AS INT_DIA_INV_DIS												--decimal	0
		, 0 AS LIN_COMP_ADJ													--decimal	0-???? Pendiente
		,sum(SCUST."MontoCartera") AS MONTO_ACT_AJUS							--decimal	0
		, 0 AS MONTO_ACUM_COMP												--decimal	0
		, 0 AS MONTO_ACUM_SPRE												--decimal	0
		, 0 AS MONTO_ACUM_VEN												--decimal	0
		, 0 AS MONTO_CUP													--decimal	0
		, 0 AS MONTO_CUP_CP													--decimal	0
		, sum(SCUST."MontoCompra") AS MONTO_INI_AJUS												--decimal	0
		, 0 AS MONTO_VAL_DISP_MAS											--decimal	0
		, 0 AS MONTO_VAL_DISP_MEN											--decimal	0
		,max(CASE WHEN  DAYS_BETWEEN ("FechaOperacion", "FechaVencimientoInstrumento") > 9999 THEN 9999 ELSE  DAYS_BETWEEN ("FechaOperacion", "FechaVencimientoInstrumento") END)  AS PLAZO
		,avg(SCUST."INGPrecioMercadoActual") AS PRECIO_MED_CIE												--decimal	0
		,avg(SCUST."INGPrecioPromedioCompra") AS PRECIO_PROM_COMP												--decimal	0
		, 0 AS QUIEN_RESERVA												--decimal	0
		, "TipoCartera"  AS TIPO_CARTERA													--char	2
		,max(CAST(SUBSTRING(SCUST."IdTipoComprobante",0,2) AS VARCHAR(2))) AS TIPO_COMP_ADJ			--char	2
		
		,avg(CASE WHEN SCUST."TasaMercado"  > 999 THEN 999 ELSE SCUST."TasaMercado"  END )  AS TIR_MED_CIE													--decimal	0
		
		,avg(CASE WHEN SCUST."TasaCompra"  > 999 THEN 999 ELSE SCUST."TasaCompra"  END)  AS TIR_PROM_COMP												--decimal	0
		,'' AS VTO_COMP_ADJ													--char	10
		,'' AS CLASIF_RIESGO												--char	10
		,max(CAST(TRIM(PEREMI."DocumentoIdentidad") AS VARCHAR(11))) AS RUT_EMISOR					--char	11
		, 0 AS DURATION														--decimal	0
		, 0 AS SEC_CAR_HISTO												--decimal	0
		,'' AS ORIGEN_RECURSOS												--char	1
		,'' AS CLASIF_CARTERA --SE DEBE HOMOLOGAR SCUST."IdClasificacionTipoInstrumento" 			--char	1
		, 0 AS CODCATIFRSID -- SE DEBE HOMOLOGAR SCUST."CodigoIFRS"								--tinyint	0
		, avg(CASE WHEN SCUST."TasaMercado"  > 999 THEN 999 ELSE SCUST."TasaMercado"  END ) AS TIR_MED_CIE_ANT												--decimal	0
		, sum("INGMontoATasaMercadoAnterior") AS MTO_VAL_DISP_ANT												--decimal	0
		, max("ValorParidadCierre") AS PARIDAD_MORIGEN												--decimal	0
		,  sum("MontoCompraMonedaInstrumento")  AS CAPTACION_MORIGEN											--decimal	0
		,max(SCUST."IdMonedaInstrumento") AS MON_MORIGEN													--char	3
		, 0 AS CAPT_MORIGEN_MCDO											--decimal	0
		, 0 AS MTO_VAL_TASA_MCDO											--decimal	0
		, 0 AS MTO_VAL_TASA_COMP											--decimal	0
	
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
				
				0 AS "Cantidad_Compra_Plazo",
			    0 AS "Cantidad_Venta_Plazo",
			    
			   0 AS "CantidadGarantia",
			   CASE WHEN "IdTipoOperacion" = 'VENPAC' THEN 'CC' ELSE '' END  AS "TipoCartera"
				
			FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" AS c
					INNER JOIN v_FechasCierre AS t 	ON ( t."FechaCierre" =   c."FechaRespaldo")
			WHERE "IdEstado" = 'VIG'
			  AND "IdClasificacionTipoInstrumento" = 'RF'
			  AND "IdTipoInstrumento" NOT IN ('SIM','PAC','PRT','EMP','FFM') -- AND "IdCliente" = 6082
			  AND "TipoCarteraCorredor"  != 'CC'
	
			UNION ALL
			
			
		SELECT 
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
				
				s."IdClasificacionTipoInstrumento",
				'' AS "CodigoIFRS",
				s."FechaEmisionInstrumento",
				s."FechaVencimientoInstrumento",
				   
				c."IdTipoComprobante",
				c."FolioComprobante",
				c."FechaComprobante",
				   
				CASE WHEN C."IdTipoOperacion" in ('VENSIM','VENPAC') THEN S."INGCantidad" ELSE 0  END  AS "Cantidad", ---"INGCantidad"
				
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
				
				
			   CASE WHEN C."IdTipoOperacion"  in ('VENSIM','VENPAC')THEN 0 ELSE S."INGCantidad" END AS "Cantidad_Compra_Plazo",
			   
			   CASE WHEN C."IdTipoOperacion"  in ('COMSIM','COMPAC')  THEN 0 ELSE S."INGCantidad" END AS "Cantidad_Venta_Plazo",
			   
			   0 AS "CantidadGarantia",-- , C."IdTipoOperacion"
			 '' AS "TipoCartera"-- , C."IdTipoOperacion"
			 
			FROM bscl."BSCL.Custodia.BD::HIS.Subyacente" as S 
				INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" as C on S."FolioCarteraRelacionada" = C."FolioCartera"
				INNER JOIN v_FechasCierre AS t 	ON ( t."FechaCierre" =   c."FechaRespaldo" AND t."FechaCierre" =   s."FechaRespaldo")
			WHERE S."IdEstado" = 'VIG' AND c."IdEstado" = 'VIG'
			  AND S."IdClasificacionTipoInstrumento" = 'RF' -- AND "IdClienteRelacionado" = 6069  
			  AND C."IdTipoInstrumento" IN ('SIM','PAC')	AND C."IdTipoOperacion" != 'VENPAC'	
			  
			  UNION all
			
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
				
				s."IdClasificacionTipoInstrumento",
				'' AS "CodigoIFRS",
				s."FechaEmisionInstrumento",
				s."FechaVencimientoInstrumento",
				   
				c."IdTipoComprobante",
				c."FolioComprobante",
				c."FechaComprobante",
				   
				CASE WHEN C."IdTipoOperacion" in ('VENSIM','VENPAC') THEN 0 ELSE  S."INGCantidad" END  AS "Cantidad", ---"INGCantidad"
				
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
				
				
			   CASE WHEN C."IdTipoOperacion"  in ('VENSIM','VENPAC')THEN S."INGCantidad" ELSE 0 END AS "Cantidad_Compra_Plazo",
			   
			   CASE WHEN C."IdTipoOperacion"  in ('COMSIM','COMPAC')  THEN S."INGCantidad" ELSE 0 END AS "Cantidad_Venta_Plazo",
			   
			   0 AS "CantidadGarantia",
			   'CC' AS "TipoCartera"-- , C."IdTipoOperacion"
			
			FROM "BSCL.Custodia.BD::HIS.Subyacente" as S 
				INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" as C on S."FolioCarteraRelacionada" = C."FolioCartera"
				INNER JOIN v_FechasCierre AS t 	ON ( t."FechaCierre" =   c."FechaRespaldo" AND t."FechaCierre" =   s."FechaRespaldo")
			WHERE S."IdEstado" = 'VIG' AND c."IdEstado" = 'VIG'
			  AND S."IdClasificacionTipoInstrumento" = 'RF' -- AND "IdClienteRelacionado" = 6069  
			  AND C."IdTipoInstrumento" IN ('SIM','PAC')	AND C."IdTipoOperacion"  in ('VENSIM','VENPAC')	
			  
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
			  
			   g."IdClasificacionTipoInstrumento",
			   g."CodigoIFRS",
			   g."FechaEmisionInstrumento",
			   g."FechaVencimientoInstrumento",
			   
			   g."IdTipoComprobante",
			   g."FolioComprobante",
			   g."FechaComprobante",
			   
				g."INGCantidad" AS "Cantidad",
				
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
	
				0 AS "Cantidad_Compra_Plazo",
			    0 AS "Cantidad_Venta_Plazo",

			    G."INGCantidad" AS "CantidadGarantia",
			      '' AS "TipoCartera"

			FROM "BSCL.Custodia.BD::HIS.Garantia" AS G
						INNER JOIN v_FechasCierre AS t 	ON ( t."FechaCierre" =   g."FechaRespaldo" )
			WHERE G."IdEstado" = 'VIG'
			  AND G."IdClasificacionTipoInstrumento" = 'RF'
			  AND G."IdTipoInstrumento" <> 'FFM'
			  AND G."INGFechaLiquidacion" <= TO_VARCHAR(CURRENT_TIMESTAMP, 'YYYY-MM-DD')
			  
			  
		UNION ALL
	
		
		SELECT 
				TO_VARCHAR(t."FechaRespaldo", 'YYYY-MM') AS  "Periodo",
				t."FolioCustodia" AS "FolioCustodia",
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
	
				0 AS "Cantidad_Compra_Plazo",
			    0 AS "Cantidad_Venta_Plazo",

			    0 AS "canGaran",
			      '' AS "TipoCartera"
				  
			FROM "BSCL.Custodia.BD::HIS.MovimientoCarteraEnTransito" AS t  
					JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS o
						ON (t."IdTipoOperacion" = o."IdTipoOperacion" AND "AccionCustodia" = 'RETIRO')
					INNER JOIN v_FechasCierre AS t1 	ON ( t1."FechaCierre" =   t."FechaRespaldo" )
					
			WHERE  "IdEstado"  = 'PEN' AND "IdClasificacionTipoInstrumento" = 'RF' 
		
			
		) AS SCUST
		
		INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = SCUST."IdCliente"
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER ON PER."IdPersona" = CLI."IdPersona"
		INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS fec ON (fec."IdEmpresa" = 1 and "CodigoParametro" = 'PLA.FechaSistema')
		
		LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" AS EMI ON EMI."IdEmisor" = SCUST."IdEmisor"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PEREMI ON PEREMI."IdPersona" = EMI."IdPersona"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdCliente" = CLI."IdCliente" AND SCUST."IdPortafolio" = por."IdPortafolio"
		LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'TBCTCIRF' AND WST."Key" = SCUST."FolioCustodia"

		WHERE POR."CodigoPortafolio"  IS NOT NULL AND   "Periodo"  !=  LEFT(fec."Valor",7)
		GROUP BY  "Periodo",
				SCUST."FolioCustodia",
				COALESCE("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(PER."DocumentoIdentidad"),'0-0') ,
				POR."CodigoPortafolio" ,
				trim(CAST(SCUST."Nemotecnico" AS VARCHAR(16))), "TipoCartera"


WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTBCTCIRF" TO  BCS_DESARROLLADOR WITH GRANT OPTION;