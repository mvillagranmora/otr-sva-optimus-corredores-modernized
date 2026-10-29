SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTCIRVHist"; 

-- select  count(1) from  "BSCL.Replica.BD.Vistas::ReplicaTBCTCIRVHist" where sec_car_inver = 1211472
-- select * from  "BSCL.Custodia.BD::CUS.Cartera" where "FolioCartera" = 1211472  and  "IdEstado" = 'VIG'
---select * from  "BSCL.Custodia.BD::GTA.Garantia" AS G where "FolioCarteraOrigen" = 1211472  

--1211472

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTCIRVHist" AS

WITH
	v_FechasCierre as (
		SELECT TO_VARCHAR("FechaRespaldo", 'YYYY-MM'),   max("FechaRespaldo" ) AS "FechaCierre"
						FROM "BSCL.Custodia.BD::HIS.Cartera" AS  c
								WHERE "IdEstado" = 'VIG'
								  AND "IdClasificacionTipoInstrumento" = 'RV'
								  AND "IdTipoInstrumento" NOT IN ('SIM','PAC','PRT','EMP','FFM')
								  GROUP BY TO_VARCHAR("FechaRespaldo", 'YYYY-MM')
	),
	v_Saldo_Cartera as (
		SELECT TO_VARCHAR("FechaRespaldo", 'YYYY-MM') AS "Periodo","FolioCartera" AS "FolioCustodia","FechaOperacion","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","IdEmisor",
			   "Nemotecnico","NemoSVS","IdBoveda","IdTipoReajuste","PlazoEnDiasOperacion","EsFungible",
			   "IdClasificacionTipoInstrumento","CodigoIFRS","FechaEmisionInstrumento",
			   "IdTipoComprobante","FolioComprobante","FechaComprobante",
			   
			   sum("INGCantidad") AS "Cantidad",sum("INGMontoMercadoActualMonedaInstrumento") AS "Monto",sum("INGPrecioMercadoActual") AS "PrecioMercado",
			   avg("PrecioCompra") AS "PrecioCompra", max("IdMonedaInstrumento") AS "IdMonedaInstrumento",sum("INGMontoATasaCompraActual") AS "MontoCompraAjus"
			   
		FROM "BSCL.Custodia.BD::HIS.Cartera" c JOIN 
					v_FechasCierre AS t 	ON ( t."FechaCierre" =   c."FechaRespaldo")
		WHERE "IdEstado" = 'VIG'
		  AND "IdClasificacionTipoInstrumento" = 'RV'
		  AND "IdTipoInstrumento" NOT IN ('SIM','PAC','PRT','EMP','FFM')
		 GROUP BY TO_VARCHAR(c."FechaRespaldo", 'YYYY-MM'),"FolioCartera","FechaOperacion","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","IdEmisor",
			   "Nemotecnico","NemoSVS","IdBoveda","IdTipoReajuste","PlazoEnDiasOperacion","EsFungible",
			   "IdClasificacionTipoInstrumento","CodigoIFRS","FechaEmisionInstrumento",
			   "IdTipoComprobante","FolioComprobante","FechaComprobante"
	),
	v_Saldo_Plazo as (
		SELECT TO_VARCHAR(c."FechaRespaldo", 'YYYY-MM') AS "Periodo",C."FolioCartera" AS "FolioCustodia",C."FechaOperacion",C."IdCliente",C."IdPortafolio",S."IdTipoInstrumento",S."IdInstrumento",
			   S."IdEmisor",S."Nemotecnico",S."NemoSVS",CASE WHEN trim(c."IdBoveda") = '' THEN NULL ELSE trim(c."IdBoveda") END AS "IdBoveda",
			   S."IdTipoReajuste",C."PlazoEnDiasOperacion",C."EsFungible",
			   S."IdClasificacionTipoInstrumento",C."CodigoIFRS",S."FechaEmisionInstrumento",
			   C."IdTipoComprobante",C."FolioComprobante",C."FechaComprobante",
			   
				SUM(CASE WHEN C."IdTipoOperacion"  in ('VENSIM','VENPAC') THEN S."INGCantidad" ELSE 0 END) AS "Cantidad_Compra",
				SUM(CASE WHEN C."IdTipoOperacion" in ('COMSIM','COMPAC')  THEN S."INGCantidad" ELSE 0 END) AS "Cantidad_Venta",		
				
			   
			   SUM(S."INGMontoATasaSubyacenteActual") AS "Monto",
			   avg(c."INGPrecioMercadoActual") AS "PrecioMercado",
			   sum(c."INGMontoATasaCompraActual") AS "MontoCompraAjus",
			   avg(c."PrecioCompra") AS "PrecioCompra",			   
			   max(c."IdMonedaInstrumento") AS "IdMonedaInstrumento"
			   
		FROM bscl."BSCL.Custodia.BD::HIS.Subyacente" as S 
		INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" as C on S."FolioCarteraRelacionada" = C."FolioCartera" AND  c."FechaRespaldo" =  s."FechaRespaldo"
					INNER JOIN v_FechasCierre t ON  ( t."FechaCierre" =   c."FechaRespaldo")
		WHERE S."IdEstado" = 'VIG'
		 AND C."IdEstado" = 'VIG'
		  AND S."IdClasificacionTipoInstrumento" = 'RV'
		  AND C."IdTipoInstrumento" IN ('SIM','PAC')
		GROUP BY TO_VARCHAR(c."FechaRespaldo", 'YYYY-MM') ,C."FolioCartera",C."FechaOperacion",C."IdCliente",C."IdPortafolio",S."IdTipoInstrumento",S."IdInstrumento",
			   	 S."IdEmisor",S."Nemotecnico",S."NemoSVS",CASE WHEN trim(c."IdBoveda") = '' THEN NULL ELSE trim(c."IdBoveda") END ,S."IdTipoReajuste",C."PlazoEnDiasOperacion",C."EsFungible",
			   	 S."IdClasificacionTipoInstrumento",C."CodigoIFRS",S."FechaEmisionInstrumento",
			   	 C."IdTipoComprobante",C."FolioComprobante",C."FechaComprobante"
	),
	v_Saldo_Prestamo as (
		SELECT  TO_VARCHAR(s."FechaRespaldo", 'YYYY-MM') AS "Periodo",C."FolioCartera" AS "FolioCustodia",min(C."FechaOperacion") AS "FechaOperacion",C."IdCliente",C."IdPortafolio",
				S."IdTipoInstrumento",S."IdInstrumento",
			   min(S."IdEmisor") AS "IdEmisor",S."Nemotecnico",min(S."NemoSVS") AS "NemoSVS",min(CASE WHEN trim(c."IdBoveda") = '' THEN NULL ELSE trim(c."IdBoveda") END ) AS "IdBoveda",
			   min(S."IdTipoReajuste") AS "IdTipoReajuste",min(C."PlazoEnDiasOperacion") AS "PlazoEnDiasOperacion",min(C."EsFungible") AS "EsFungible",
			   min(S."IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento",min(C."CodigoIFRS") AS "CodigoIFRS",min(S."FechaEmisionInstrumento") AS "FechaEmisionInstrumento",
			   min(C."IdTipoComprobante") AS "IdTipoComprobante",min(C."FolioComprobante") AS "FolioComprobante",min(C."FechaComprobante") AS "FechaComprobante",
			   SUM(CASE WHEN C."IdTipoOperacion" in ('VENSIM','VENPAC') THEN S."INGCantidad" ELSE 0 END) AS "Cantidad_Compra",
			   SUM(CASE WHEN C."IdTipoOperacion" in ('COMSIM','COMPAC') THEN S."INGCantidad" ELSE 0 END) AS "Cantidad_Venta",
			   SUM(S."INGMontoATasaSubyacenteActual") AS "Monto",
			   
			   avg(c."INGPrecioMercadoActual") AS "PrecioMercado",
			   sum(c."INGMontoATasaCompraActual") AS "MontoCompraAjus",
			   avg(c."PrecioCompra") AS "PrecioCompra",			   
			   max(c."IdMonedaInstrumento") AS "IdMonedaInstrumento"
		FROM bscl."BSCL.Custodia.BD::HIS.Subyacente" as S 
		INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" as C on S."FolioCarteraRelacionada" = C."FolioCartera" AND  c."FechaRespaldo" =  s."FechaRespaldo"
		INNER JOIN v_FechasCierre t ON  ( t."FechaCierre" =   c."FechaRespaldo")
		
		WHERE S."IdEstado" = 'VIG' AND c."IdEstado" = 'VIG'
		  AND S."IdClasificacionTipoInstrumento" = 'RV'
		  AND C."IdTipoInstrumento" IN ('PRT','EMP')
		GROUP BY  TO_VARCHAR(s."FechaRespaldo", 'YYYY-MM') ,C."FolioCartera",C."IdCliente",C."IdPortafolio",S."IdTipoInstrumento",S."IdInstrumento",
			   	 S."Nemotecnico"
	),
	v_Saldo_Garantia as (
		SELECT 
			TO_VARCHAR(g."FechaRespaldo", 'YYYY-MM') AS "Periodo",G."FolioCarteraOrigen" AS "FolioCustodia",G."IdCliente",G."IdPortafolio", max(G."FechaOperacion") AS "FechaOperacion",
			G."IdTipoInstrumento",G."IdInstrumento",G."Nemotecnico",
			g."IdEmisor",g."NemoSVS",g."IdTipoReajuste",
			'DCV'  AS "IdBoveda",0 AS "PlazoEnDiasOperacion",max(g."EsFungible") AS "EsFungible",
			max(G."IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento",
			sum(g."PrecioPromedioCompraOperacionOrigen") AS "INGMontoPrecioPromedioMonedaInstrumento",
			sum("MontoPrecioPromedioCompraOperacionOrigen") AS "INGMontoMercadoActualMonedaInstrumento",
			avg(g."PrecioGarantia") AS "INGPrecioMercadoActual",
			avg(g."PrecioGarantia") AS "PrecioMercado",
			avg("PrecioPromedioCompraOperacionOrigen") AS "INGPrecioPromedioCompra",max("IdMonedaSistema") AS "IdMonedaInstrumento",max(g."CodigoIFRS") AS "CodigoIFRS",
			SUM(G."INGCantidad") AS "Cantidad",SUM(G."INGMontoATasaGarantiaActual") AS "Monto" , max(g."FechaEmisionInstrumento") AS "FechaEmisionInstrumento",
			 max(g."IdTipoComprobante") AS "IdTipoComprobante",max(g."FolioComprobante") AS "FolioComprobante",max(g."FechaComprobante") AS "FechaComprobante",sum(g."MontoPrecioPromedioCompraOperacionOrigen" ) AS "MontoPrecioPromedioCompraOperacionOrigen"
		
		FROM "BSCL.Custodia.BD::HIS.Garantia" AS G
				INNER JOIN v_FechasCierre t ON  ( t."FechaCierre" =   g."FechaRespaldo")
		WHERE G."IdEstado" = 'VIG'
		  AND G."IdClasificacionTipoInstrumento" = 'RV'
		  AND G."IdTipoInstrumento" <> 'FFM'
		  AND G."INGFechaLiquidacion" <= TO_VARCHAR(CURRENT_TIMESTAMP, 'YYYY-MM-DD')
		GROUP BY 
			TO_VARCHAR(g."FechaRespaldo", 'YYYY-MM') ,"FolioCarteraOrigen",G."IdCliente",G."IdPortafolio",G."IdTipoInstrumento",G."IdInstrumento",g."IdEmisor", G."Nemotecnico",g."NemoSVS",g."IdTipoReajuste"
	),
	v_Saldo_Transito AS (
		SELECT TO_VARCHAR(t."FechaRespaldo", 'YYYY-MM') AS "Periodo","FolioCustodia" AS "FolioCustodia","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico",null AS "IdBoveda",
				sum("MontoCompra") AS "INGMontoPrecioPromedioMonedaInstrumento",sum("MontoCompra") AS  "INGMontoMercadoActualMonedaInstrumento",min("PrecioCompra") AS "INGPrecioMercadoActual", min("PrecioCompra") AS "INGPrecioPromedioCompra", 
				max("IdMonedaSistema") AS "IdMonedaInstrumento",
				SUM("Cantidad") * - 1 AS "Cantidad",SUM("MontoCompra") AS "Monto"
 
		FROM "BSCL.Custodia.BD::HIS.MovimientoCarteraEnTransito" AS t  JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS o
				ON (t."IdTipoOperacion" = o."IdTipoOperacion" AND "AccionCustodia" = 'RETIRO')
				INNER JOIN v_FechasCierre t1 ON  ( t1."FechaCierre" =   t."FechaRespaldo")
		WHERE  "IdEstado"  = 'PEN'  AND "IdClasificacionTipoInstrumento" = 'RV'
		GROUP BY TO_VARCHAR(t."FechaRespaldo", 'YYYY-MM') ,"FolioCustodia","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico"
	)
SELECT

	WST."IdStack" AS "IDSTACK",
	SCUST."Periodo"	  || ':' || cast(SCUST."FolioCustodia" AS varchar)  || ':' ||  COALESCE(rtrim(ltrim(TRIM(PER."DocumentoIdentidad"))),'') || ':' ||  rtrim(ltrim(POR."CodigoPortafolio" )) || ':' ||  rtrim(ltrim(SCUST."Nemotecnico"))  AS TABLE_KEY,
	SCUST."Periodo"	 AS COD_PER								--char	7
	,SCUST."FolioCustodia" AS SEC_CAR_INVER													--decimal	0
	,COALESCE("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(PER."DocumentoIdentidad"),'0-0') AS RUT_CLI
	,POR."CodigoPortafolio" AS SEC_RUT_CLI													--decimal	0

	
	,LEFT(CASE COALESCE( hpr."ValorOrigen", scust."IdTipoInstrumento") 
						WHEN   'OSA' 			THEN 'AC' 
						WHEN   'SIM' 			THEN 'AC'
						WHEN   'ETN' 			THEN 'EN'
						WHEN   'ETX' 			THEN 'ET'
						ELSE COALESCE( hpr."ValorOrigen", scust."IdTipoInstrumento") END,2) AS "MERCADO"
						
	,CAST(SCUST."Nemotecnico" AS VARCHAR(16)) AS NEMO										--char	16 
	,CAST(COALESCE(SCUST."IdBoveda",'DCV') AS VARCHAR(3)) AS COD_BOVEDA										--char	3
	,SCUST."FechaOperacion" AS FEC_INVER													--char	10
	,SCUST."Cantidad_Cartera"  AS CAN_ACTUAL													--decimal	0
	,SCUST."Cantidad_Compra_Plazo" AS CAN_COM_PLAZO											--decimal	0
	,SCUST."Cantidad_Garantia" AS CAN_GARANTIA												--decimal	0
	, 0 AS CAN_ORD_VEN																		--decimal	0
	, 0 AS CAN_TRAM_COM																		--decimal	0
	, 0 AS CAN_TRAM_VEN																		--decimal	0
	,'' AS COD_RUBRO																		--char	4
	,'1900-01-01' AS FEC_VCTO_TP																		--char	10
	,SCUST."FolioComprobante" AS FOLIO_COMP_ADJ												--decimal	0
	, 0 AS INT_ACUM_GAN_CP																	--decimal	0
	, 0 AS INT_ACUM_GAN_VP																	--decimal	0
	, 0 AS LIN_COMP_ADJ																		--decimal	0
	, 0 AS LIN_ORDEN																		--decimal	0
	,SCUST."Monto_Cartera" AS MONTO_ACT_AJUS												--decimal	0
	, 0 AS MONTO_ACUM_SPRE																	--decimal	0
	, 0 AS MONTO_ACUM_VC																	--decimal	0
	, 0 AS MONTO_ACUM_VC_CE																	--decimal	0
	, 0 AS MONTO_ACUM_VC_CP																	--decimal	0
	, 0 AS MONTO_ACUM_VC_VE																	--decimal	0
	, 0 AS MONTO_ACUM_VEN																	--decimal	0
	,"MontoCompraAjus" AS MONTO_INI_AJUS																	--decimal	0
	,SCUST."PrecioMercado"   AS PRECIO_CIERRE													--decimal	0
	, "PrecioCompra"		 AS PRECIO_PROM_COMP																	--decimal	0
	, 0 AS SEC_ORDEN																		--decimal	0
	,'' AS TIPO_CARTERA																		--char	2
	,SUBSTRING(SCUST."IdTipoComprobante",0,2) AS TIPO_COMP_ADJ								--char	2
	,'' AS VTO_COMP_ADJ																		--char	10
	,'' AS ORIGEN_RECURSOS																	--char	1
	,'' AS CLASIF_CARTERA		--SE DEBE HOMOLOGAR SCUST."IdClasificacionTipoInstrumento"	--char	1
	, SCUST."PrecioCompra" AS PRECIO_PROM_CALC																	--decimal	0
	,(SCUST."Cantidad_Compra_Prestamo" + SCUST."Cantidad_Venta_Prestamo") AS CAN_PRESTAMO	--decimal	0
	, 0 AS CAN_VENTA_CORTA																	--decimal	0
	,CASE WHEN SCUST."CodigoIFRS" = 'CUS' THEN 1			
		  ELSE 0
	  END AS CODCATIFRSID																	--tinyint	0
	,"IdMonedaInstrumento"  AS MON_MORIGEN																		--char	3
	, 0 AS CAPTACION_MORIGEN																--decimal	0
	, 0 AS TCAMBIO_INICIO																	--decimal	0
	, 0 AS VALOR_INI_MORIGEN																--decimal	0
FROM (
	SELECT 	P."Periodo"	, P."FolioCustodia",
			COALESCE(C."FechaOperacion",Z."FechaOperacion",G."FechaOperacion",E."FechaOperacion") AS "FechaOperacion",
			COALESCE(C."IdCliente",Z."IdCliente",G."IdCliente",E."IdCliente") AS "IdCliente",
			COALESCE(C."IdPortafolio",Z."IdPortafolio",G."IdPortafolio",E."IdPortafolio") AS "IdPortafolio",
			COALESCE(C."IdTipoInstrumento",Z."IdTipoInstrumento",G."IdTipoInstrumento",E."IdTipoInstrumento") AS "IdTipoInstrumento",
			COALESCE(C."IdInstrumento",Z."IdInstrumento",G."IdInstrumento",E."IdInstrumento") AS "IdInstrumento",
			COALESCE(C."IdEmisor",Z."IdEmisor",G."IdEmisor",E."IdEmisor") AS "IdEmisor",
			COALESCE(C."Nemotecnico",Z."Nemotecnico",G."Nemotecnico",E."Nemotecnico") AS "Nemotecnico",
			COALESCE(C."NemoSVS",Z."NemoSVS",G."NemoSVS",E."NemoSVS") AS "NemoSVS",
			COALESCE(C."IdBoveda",Z."IdBoveda",G."IdBoveda",E."IdBoveda") AS "IdBoveda",
			COALESCE(C."IdTipoReajuste",Z."IdTipoReajuste",G."IdTipoReajuste",E."IdTipoReajuste") AS "IdTipoReajuste",
			COALESCE(C."PlazoEnDiasOperacion",Z."PlazoEnDiasOperacion",G."PlazoEnDiasOperacion",E."PlazoEnDiasOperacion") AS "PlazoEnDiasOperacion",
			COALESCE(C."EsFungible",Z."EsFungible",G."EsFungible",E."EsFungible") AS "EsFungible",
			COALESCE(C."IdClasificacionTipoInstrumento",Z."IdClasificacionTipoInstrumento",G."IdClasificacionTipoInstrumento",E."IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento",
			COALESCE(C."CodigoIFRS",Z."CodigoIFRS",G."CodigoIFRS",E."CodigoIFRS") AS "CodigoIFRS",
			COALESCE(C."FechaEmisionInstrumento",Z."FechaEmisionInstrumento",G."FechaEmisionInstrumento",E."FechaEmisionInstrumento") AS "FechaEmisionInstrumento",
			COALESCE(C."IdTipoComprobante",Z."IdTipoComprobante",G."IdTipoComprobante",E."IdTipoComprobante") AS "IdTipoComprobante",
			COALESCE(C."FolioComprobante",Z."FolioComprobante",G."FolioComprobante",E."FolioComprobante") AS "FolioComprobante",
			COALESCE(C."FechaComprobante",Z."FechaComprobante",G."FechaComprobante",E."FechaComprobante") AS "FechaComprobante",
			------------------------------------------------------------------------------------------------------------

			COALESCE(C."Cantidad",0) + COALESCE(T."Cantidad",0)  AS "Cantidad_Cartera",
			COALESCE(C."Monto",0) - COALESCE(T."Monto",0)  AS "Monto_Cartera",
			COALESCE(Z."Cantidad_Compra",0) AS "Cantidad_Compra_Plazo",
			COALESCE(Z."Cantidad_Venta",0) AS "Cantidad_Venta_Plazo",
			COALESCE(Z."Monto",0) AS "Monto_Plazo",
			COALESCE(G."Cantidad",0) AS "Cantidad_Garantia",
			COALESCE(G."Monto",0) AS "Monto_Garantia",
			COALESCE(E."Cantidad_Compra",0) AS "Cantidad_Compra_Prestamo",
			COALESCE(E."Cantidad_Venta",0) AS "Cantidad_Venta_Prestamo",
			COALESCE(E."Monto",0) AS "Monto_Prestamo",
			
			COALESCE(C."PrecioMercado",Z."PrecioMercado",G."PrecioMercado",E."PrecioMercado",0) AS "PrecioMercado",
			
			COALESCE(c."MontoCompraAjus",Z."MontoCompraAjus",G."MontoPrecioPromedioCompraOperacionOrigen",E."MontoCompraAjus",0) AS "MontoCompraAjus",
			COALESCE(c."PrecioCompra" ,Z."PrecioCompra",G."INGPrecioPromedioCompra",E."PrecioCompra", 0) AS "PrecioCompra",
			COALESCE(c."IdMonedaInstrumento",Z."IdMonedaInstrumento",G."IdMonedaInstrumento",E."IdMonedaInstrumento" , 'CLP') AS "IdMonedaInstrumento" 
			
	
			
	FROM (
		SELECT DISTINCT "FolioCustodia", "Periodo" 
		FROM (
			SELECT "FolioCustodia", "Periodo" FROM v_Saldo_Cartera
				UNION ALL
			SELECT "FolioCustodia", "Periodo" FROM v_Saldo_Plazo
				UNION ALL
			SELECT "FolioCustodia", "Periodo" FROM v_Saldo_Prestamo
				UNION ALL
			SELECT "FolioCustodia", "Periodo" FROM v_Saldo_Garantia
				UNION ALL
			SELECT "FolioCustodia", "Periodo" FROM v_Saldo_Transito
		)
	) as P
	LEFT JOIN v_Saldo_Cartera as C 
			ON  C."FolioCustodia" = P."FolioCustodia" AND c."Periodo" = p."Periodo"
	LEFT JOIN v_Saldo_Plazo as Z
			ON Z."FolioCustodia" = P."FolioCustodia" AND z."Periodo" = p."Periodo"
	LEFT JOIN v_Saldo_Prestamo as E
			ON E."FolioCustodia" = P."FolioCustodia"	 AND e."Periodo" = p."Periodo"	
	LEFT JOIN v_Saldo_Garantia as G 
			ON G."FolioCustodia" = P."FolioCustodia" AND g."Periodo" = p."Periodo"

	LEFT JOIN v_Saldo_Transito as T
			ON T."FolioCustodia" = P."FolioCustodia" AND t."Periodo" = p."Periodo"

			
) AS SCUST
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = SCUST."IdCliente"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER ON PER."IdPersona" = CLI."IdPersona"
INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS fec ON (fec."IdEmpresa" = 1 and "CodigoParametro" = 'PLA.FechaSistema')

LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" AS EMI ON EMI."IdEmisor" = SCUST."IdEmisor"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PEREMI ON PEREMI."IdPersona" = EMI."IdPersona"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdCliente" = CLI."IdCliente" AND SCUST."IdPortafolio" = por."IdPortafolio"

LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS hpr ON (hpr."TipoValor" = 'TipoInstrumentoSegunMercado' 
                                                                   AND hpr."Entidad" = 'TipoInstrumentoSegunMercado' 
                                                                   AND hpr."ValorDestino" = SCUST."IdTipoInstrumento"    AND hpr."ValorOrigen" NOT IN ('CM' , 'EN' , 'ET'))


left JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'TBCTCIRV' AND WST."Key" = SCUST."FolioCustodia"
WHERE "BSCL.Replica.BD.Servicios::func_IsNumeric"(POR."CodigoPortafolio") != 0
AND LEFT(fec."Valor",7) !=  SCUST."Periodo"	
WITH READ ONLY; 

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTBCTCIRV" TO  BCS_DESARROLLADOR WITH GRANT OPTION;