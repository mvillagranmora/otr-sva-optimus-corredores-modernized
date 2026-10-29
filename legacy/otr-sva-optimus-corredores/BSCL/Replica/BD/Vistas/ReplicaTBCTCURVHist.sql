SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTCURVHist"; 
--SELECT count(1) FROM  "BSCL.Replica.BD.Vistas::ReplicaTBCTCURVHist"
--	0	      	TOT	4128.00	0.00	0.00	6228.00	2100.00	2100.00
-- SELECT  *  FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCURV" WHERE  ltrim(rtrim(rut_cli)) = '76529250-6' and nemo = 'ENELGXCH' and cod_per = '2019-04'
/*SELECT  "INGCantidad",* 
	FROM "BSCL.Custodia.BD::CUS.Cartera" AS  c
		WHERE "IdEstado" = 'VIG'
		  AND "IdClasificacionTipoInstrumento" = 'RV'
		  AND "IdTipoInstrumento" NOT IN ('SIM','PAC','PRT','EMP','FFM')
		  AND "INGFechaLiquidacion" <= TO_VARCHAR(CURRENT_TIMESTAMP, 'YYYY-MM-DD') AND "Nemotecnico" = 'CFIMBIRF-A' AND "IdCliente" = 9387
*/
/*

	SELECT "IdCarga",*
		FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS t  JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS o
				ON (t."IdTipoOperacion" = o."IdTipoOperacion" AND "AccionCustodia" = 'RETIRO')
		WHERE  "IdEstado"  = 'PEN'   AND "IdClasificacionTipoInstrumento" = 'RV'
		and "IdCliente" = 6596 and "Nemotecnico"= 'ITAUCORP'

		
				
	update t set "IdEstado" = 'ANU'
		from "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS t  JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS o
				ON (t."IdTipoOperacion" = o."IdTipoOperacion" AND "AccionCustodia" = 'RETIRO')
		WHERE  "IdEstado"  = 'PEN'   AND "IdClasificacionTipoInstrumento" = 'RV'
			and "IdCliente" = 6078 and "Nemotecnico"= 'ENELGXCH'
		
select  count(1)  from	"BSCL.Replica.BD.Vistas::ReplicaTBCTCURVHist"
						
		*/

	/* 
	
	
	
SELECT  c.rut_cli , c.SEC_RUT_CLI , c.nemo , c.cod_boveda, a.can_custodia as canProd , c.can_custodia as canReplica, a.can_com_plazo , c.can_com_plazo, a.can_garantia , c.can_garantia, a.can_ven_plazo , c.can_ven_plazo,
			 (c.can_custodia  + c.can_com_plazo + c.can_garantia ) - (a.can_custodia  + a.can_com_plazo + a.can_garantia ),
			 a.PRECIO_PROM , c.PRECIO_PROM
FROM BSCL_VIRTUAL.GES_TBCTCURV  A join "BSCL.Replica.BD.Vistas::ReplicaTBCTCURVHist"  C  
		on (
			 rtrim(ltrim(c.rut_cli)) =  rtrim(ltrim(a.rut_cli ))  and 
			 c.SEC_RUT_CLI =  a.SEC_RUT_CLI    and 
			 rtrim(ltrim(c.nemo)) =  rtrim(ltrim(a.nemo))    and 
			 c.cod_boveda=  a.cod_boveda     
		)
where c.cod_per = '2019-03' and c.cod_boveda = 'TOT' and c.rut_cli != 'oficina' and 
 a.cod_per = '2019-03' and a.cod_boveda = 'TOT' and a.rut_cli != 'oficina'
 and (a.can_custodia != c.can_custodia  or a.can_com_plazo != c.can_com_plazo  or a.can_garantia != c.can_garantia  or a.can_ven_plazo != c.can_ven_plazo
 --or (a.PRECIO_PROM != c.PRECIO_PROM  and a.PRECIO_PROM != 0 ) 
 )
 
	*/

/*


 SELECT  a.rut_cli , a.SEC_RUT_CLI , a.nemo , a.cod_boveda, a.can_custodia , c.can_custodia, a.can_com_plazo , c.can_com_plazo, a.can_garantia , c.can_garantia
FROM BSCL_VIRTUAL.GES_TBCTCURV  a    left join "BSCL.Replica.BD.Vistas::ReplicaTBCTCURVHist"  c
		on (
			 rtrim(ltrim(c.rut_cli)) =  rtrim(ltrim(a.rut_cli ))  and 
			 c.SEC_RUT_CLI =  a.SEC_RUT_CLI    and 
			 rtrim(ltrim(c.nemo)) =  rtrim(ltrim(a.nemo))    and 
			 c.cod_boveda=  a.cod_boveda    and
			  c.cod_per = '2019-03' and c.cod_boveda = 'TOT' and c.rut_cli != 'oficina'  

		)
where
 a.cod_per = '2019-03' and a.cod_boveda = 'TOT' and TRIM(a.rut_cli) != 'OFICINA' 
 and  c.can_garantia is NULL
 
 

 SELECT  a.rut_cli , a.SEC_RUT_CLI , a.nemo , a.cod_boveda, a.can_custodia , c.can_custodia, a.can_com_plazo , c.can_com_plazo, a.can_garantia , c.can_garantia
FROM  "BSCL.Replica.BD.Vistas::ReplicaTBCTCURVHist"  a    left join BSCL_VIRTUAL.GES_TBCTCURV  c
		on (
			 rtrim(ltrim(c.rut_cli)) =  rtrim(ltrim(a.rut_cli ))  and 
			 c.SEC_RUT_CLI =  a.SEC_RUT_CLI    and 
			 rtrim(ltrim(c.nemo)) =  rtrim(ltrim(a.nemo))    and 
			 c.cod_boveda=  a.cod_boveda    and
			  c.cod_per = '2019-03' and c.cod_boveda = 'TOT' and c.rut_cli != 'oficina'  

		)
where
 a.cod_per = '2019-03' and a.cod_boveda = 'TOT' and a.rut_cli != 'oficina' 
 and  c.can_garantia is NULL
 
 */

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTCURVHist" AS
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
		SELECT 
			TO_VARCHAR("FechaRespaldo", 'YYYY-MM') AS "Periodo", "FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico",CASE WHEN trim(c."IdBoveda") = '' THEN NULL ELSE trim(c."IdBoveda") END AS "IdBoveda",
			"INGMontoPrecioPromedioMonedaInstrumento","INGMontoMercadoActualMonedaInstrumento","INGPrecioMercadoActual","INGPrecioPromedioCompra","IdMonedaInstrumento",
			SUM("INGCantidad") AS "Cantidad",SUM("INGMontoATasaCompraActual") AS "Monto"
			
		FROM "BSCL.Custodia.BD::HIS.Cartera" AS  c	 JOIN 
					v_FechasCierre AS t 	ON ( t."FechaCierre" =   c."FechaRespaldo")
								  
		
		WHERE "IdEstado" = 'VIG'
		  AND "IdClasificacionTipoInstrumento" = 'RV'
		  AND "IdTipoInstrumento" NOT IN ('SIM','PAC','PRT','EMP','FFM')
		  AND "INGFechaLiquidacion" <= TO_VARCHAR(CURRENT_TIMESTAMP, 'YYYY-MM-DD')
		GROUP BY 
			TO_VARCHAR("FechaRespaldo", 'YYYY-MM'),"FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico",CASE WHEN trim(c."IdBoveda") = '' THEN NULL ELSE trim(c."IdBoveda") END ,
			"INGMontoPrecioPromedioMonedaInstrumento","INGMontoMercadoActualMonedaInstrumento","INGPrecioMercadoActual","INGPrecioPromedioCompra","IdMonedaInstrumento"
	),
	v_Saldo_Plazo as (
		SELECT 
			TO_VARCHAR(s."FechaRespaldo", 'YYYY-MM')  AS "Periodo", C."FolioCartera",C."IdCliente",C."IdPortafolio",S."IdTipoInstrumento",S."IdInstrumento",S."Nemotecnico",CASE WHEN trim(c."IdBoveda") = '' THEN NULL ELSE trim(c."IdBoveda") END AS "IdBoveda",
			C."INGMontoPrecioPromedioMonedaInstrumento",C."INGMontoMercadoActualMonedaInstrumento",C."INGPrecioMercadoActual",C."INGPrecioPromedioCompra",C."IdMonedaInstrumento",
			
			SUM(CASE WHEN C."IdTipoOperacion"  in ('VENSIM','VENPAC') THEN S."INGCantidad" ELSE 0 END) AS "Cantidad_Compra",
			SUM(CASE WHEN C."IdTipoOperacion" in ('COMSIM','COMPAC')  THEN S."INGCantidad" ELSE 0 END) AS "Cantidad_Venta",
			
			SUM(S."INGMontoATasaSubyacenteActual") AS "Monto"
		FROM "BSCL.Custodia.BD::HIS.Subyacente" as S 
		INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" as C on S."FolioCarteraRelacionada" = C."FolioCartera" AND  c."FechaRespaldo" =  s."FechaRespaldo"
		INNER JOIN v_FechasCierre t ON  ( t."FechaCierre" =   c."FechaRespaldo")
		WHERE S."IdEstado" = 'VIG'
		  AND C."IdEstado" = 'VIG'
		  AND S."IdClasificacionTipoInstrumento" = 'RV'
		  AND C."IdTipoInstrumento" IN ('SIM','PAC')
		  AND C."INGFechaLiquidacion" <= TO_VARCHAR(CURRENT_TIMESTAMP, 'YYYY-MM-DD')
		GROUP BY 
			TO_VARCHAR(s."FechaRespaldo", 'YYYY-MM'),C."FolioCartera",C."IdCliente",C."IdPortafolio",S."IdTipoInstrumento",S."IdInstrumento",S."Nemotecnico",CASE WHEN trim(c."IdBoveda") = '' THEN NULL ELSE trim(c."IdBoveda") END ,
			C."INGMontoPrecioPromedioMonedaInstrumento",C."INGMontoMercadoActualMonedaInstrumento",C."INGPrecioMercadoActual",C."INGPrecioPromedioCompra",C."IdMonedaInstrumento"
	),
	v_Saldo_Garantia as (
		SELECT 
			TO_VARCHAR("FechaRespaldo", 'YYYY-MM')  AS "Periodo" ,G."FolioCarteraOrigen" AS "FolioCartera",G."IdCliente",G."IdPortafolio",G."IdTipoInstrumento",G."IdInstrumento",G."Nemotecnico",'DCV'  AS "IdBoveda",
			g."PrecioPromedioCompraOperacionOrigen" AS "INGMontoPrecioPromedioMonedaInstrumento","MontoPrecioPromedioCompraOperacionOrigen" AS "INGMontoMercadoActualMonedaInstrumento",
			g."PrecioGarantia" AS "INGPrecioMercadoActual","PrecioPromedioCompraOperacionOrigen" AS "INGPrecioPromedioCompra","IdMonedaSistema" AS "IdMonedaInstrumento",
			SUM(G."INGCantidad") AS "Cantidad",SUM(G."INGMontoATasaGarantiaActual") AS "Monto"
		FROM "BSCL.Custodia.BD::HIS.Garantia" AS G
				INNER JOIN v_FechasCierre t ON  ( t."FechaCierre" =   g."FechaRespaldo")
		WHERE G."IdEstado" = 'VIG'
		  AND G."IdClasificacionTipoInstrumento" = 'RV'
		  AND G."IdTipoInstrumento" <> 'FFM'
		  AND G."INGFechaLiquidacion" <= TO_VARCHAR(CURRENT_TIMESTAMP, 'YYYY-MM-DD')
		GROUP BY 
			TO_VARCHAR("FechaRespaldo", 'YYYY-MM'),"FolioCarteraOrigen",G."IdCliente",G."IdPortafolio",G."IdTipoInstrumento",G."IdInstrumento",G."Nemotecnico",
			"PrecioPromedioCompraOperacionOrigen","MontoPrecioPromedioCompraOperacionOrigen","PrecioGarantia","PrecioPromedioCompraOperacionOrigen","IdMonedaSistema"
	),
	v_Saldo_Prestamo AS (
		SELECT 
			TO_VARCHAR(s."FechaRespaldo", 'YYYY-MM')  AS "Periodo" ,C."FolioCartera",C."IdCliente",C."IdPortafolio",
			S."IdTipoInstrumento",S."IdInstrumento",S."Nemotecnico",CASE WHEN trim(c."IdBoveda") = '' THEN NULL ELSE trim(c."IdBoveda") END AS "IdBoveda",
			C."INGMontoPrecioPromedioMonedaInstrumento",C."INGMontoMercadoActualMonedaInstrumento",C."INGPrecioMercadoActual",C."INGPrecioPromedioCompra",C."IdMonedaInstrumento",
			SUM(CASE WHEN C."IdTipoOperacion"  in ('VENSIM','VENPAC') THEN S."INGCantidad" ELSE 0 END) AS "Cantidad_Compra",
			SUM(CASE WHEN C."IdTipoOperacion" in ('COMSIM','COMPAC')  THEN S."INGCantidad" ELSE 0 END) AS "Cantidad_Venta",
			SUM(S."INGMontoATasaSubyacenteActual") AS "Monto"
		FROM "BSCL.Custodia.BD::HIS.Subyacente" as S 
		INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" as C on S."FolioCarteraRelacionada" = C."FolioCartera" AND  c."FechaRespaldo" =  s."FechaRespaldo"
		INNER JOIN v_FechasCierre t ON  ( t."FechaCierre" =   c."FechaRespaldo")
		
		WHERE S."IdEstado" = 'VIG' AND c."IdEstado" = 'VIG' AND c."IdEstado" = 'VIG'
		  AND S."IdClasificacionTipoInstrumento" = 'RV'
		  AND C."IdTipoInstrumento" IN ('PRT','EMP')
		  AND C."INGFechaLiquidacion" <= TO_VARCHAR(CURRENT_TIMESTAMP, 'YYYY-MM-DD')
		GROUP BY TO_VARCHAR(s."FechaRespaldo", 'YYYY-MM') ,
			C."FolioCartera",C."IdCliente",C."IdPortafolio",S."IdTipoInstrumento",S."IdInstrumento",S."Nemotecnico",CASE WHEN trim(c."IdBoveda") = '' THEN NULL ELSE trim(c."IdBoveda") END,
			C."INGMontoPrecioPromedioMonedaInstrumento",C."INGMontoMercadoActualMonedaInstrumento",C."INGPrecioMercadoActual",C."INGPrecioPromedioCompra",C."IdMonedaInstrumento"
	),
	v_Saldo_Transito AS (
		SELECT TO_VARCHAR(t."FechaRespaldo", 'YYYY-MM')  AS "Periodo","FolioCustodia" AS "FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico",null AS "IdBoveda",
				sum("MontoCompra") AS "INGMontoPrecioPromedioMonedaInstrumento",sum("MontoCompra") AS  "INGMontoMercadoActualMonedaInstrumento",min("PrecioCompra") AS "INGPrecioMercadoActual", min("PrecioCompra") AS "INGPrecioPromedioCompra", 
				max("IdMonedaSistema") AS "IdMonedaInstrumento",
				SUM("Cantidad") * - 1 AS "Cantidad",SUM("MontoCompra") AS "Monto"
 
		FROM "BSCL.Custodia.BD::HIS.MovimientoCarteraEnTransito" AS t  
				INNER JOIN v_FechasCierre t1 ON  ( t1."FechaCierre" =   t."FechaRespaldo")
				JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS o
					ON (t."IdTipoOperacion" = o."IdTipoOperacion" AND "AccionCustodia" = 'RETIRO')
		
		WHERE  "IdEstado"  = 'PEN'   AND "IdClasificacionTipoInstrumento" = 'RV'
		GROUP BY TO_VARCHAR(t."FechaRespaldo", 'YYYY-MM') ,"FolioCustodia","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico"
	)
SELECT 
	MAX(WST."IdStack") AS "IDSTACK",
	SCUST."Periodo"	  || ':' || COALESCE(rtrim(ltrim(TRIM(PER."DocumentoIdentidad"))),'') || ':' ||  rtrim(ltrim(POR."CodigoPortafolio" )) || ':' ||  rtrim(ltrim(SCUST."Nemotecnico")) || ':' ||  COALESCE(BOVEDA , SCUST."IdBoveda",'DCV' ) AS TABLE_KEY,
	SCUST."Periodo"	  AS "COD_PER" 
	,CLI."IdCliente"
	,SUM(SCUST."Cantidad_Cartera") /* + SUM(SCUST."Cantidad_Venta_Plazo")*/ AS "CAN_CUSTODIA"
	,COALESCE("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(PER."DocumentoIdentidad"),'0-0') AS "RUT_CLI"
	,CAST(POR."CodigoPortafolio" AS VARCHAR(100)) AS "SEC_RUT_CLI"
--	,scust."IdTipoInstrumento"
	,LEFT(CASE COALESCE( hpr."ValorOrigen", scust."IdTipoInstrumento") 
						WHEN   'OSA' 			THEN 'AC' 
						WHEN   'SIM' 			THEN 'AC'
						WHEN   'ETN' 			THEN 'EN'
						WHEN   'ETX' 			THEN 'ET'
						ELSE COALESCE( hpr."ValorOrigen", scust."IdTipoInstrumento") END,2) AS "MERCADO"
						
	,CAST(SCUST."Nemotecnico" AS VARCHAR(40)) AS "NEMO"
	
	,COALESCE(BOVEDA , SCUST."IdBoveda",'DCV' ) AS "COD_BOVEDA"
	
	,SUM(SCUST."Cantidad_Compra_Plazo") AS "CAN_COM_PLAZO"
	,SUM(SCUST."Cantiddad_Garantia") AS "CAN_GARANTIA"
	,0 AS "CAN_LIBRE_01"
	,0 AS "CAN_LIBRE_02"
	,0 AS "CAN_LIBRE_03"
	,0 AS "CAN_TRAM_COM"
	,0 AS "CAN_TRAM_VEN"
	,SUM(SCUST."Cantidad_Venta_Plazo") AS "CAN_VEN_PLAZO"
	,SUM(SCUST."INGMontoPrecioPromedioMonedaInstrumento") AS "MONTO_INI_AJUS"
	,SUM(SCUST."INGMontoMercadoActualMonedaInstrumento") AS "MONTO_VALORIZ"
	,avg(SCUST."INGPrecioMercadoActual") AS "PRECIO_CIERRE"
	,avg(SCUST."INGPrecioPromedioCompra") AS "PRECIO_PROM"
	,CAST('' AS VARCHAR(50)) AS "COD_MANDATO"
	,CAST('' AS VARCHAR(50)) AS "COD_OBSERVAC" 
	,(SUM(SCUST."Cantidad_Compra_Prestamo") + SUM(SCUST."Cantidad_Venta_Prestamo")) AS "CAN_PRESTAMO"
	,0 AS "CAN_VENTA_CORTA"
	,CAST(SCUST."IdMonedaInstrumento" AS VARCHAR(2)) AS "MON_MORIGEN"
	,SUM(SCUST."INGMontoMercadoActualMonedaInstrumento") AS "VALOR_MERCADO_MORIGE"
FROM (
	SELECT 	
			P."Periodo"	,
			P."FolioCartera",
			P."IdCliente",
			P."IdPortafolio",
			P."IdTipoInstrumento",
			P."IdInstrumento",
			P."Nemotecnico",
			P."IdBoveda",
			P."INGMontoPrecioPromedioMonedaInstrumento",
			P."INGMontoMercadoActualMonedaInstrumento",
			P."INGPrecioMercadoActual",
			P."INGPrecioPromedioCompra",
			P."IdMonedaInstrumento",
			COALESCE(C."Cantidad",0) + COALESCE(T."Cantidad",0)  AS "Cantidad_Cartera",
			COALESCE(C."Monto",0) - COALESCE(T."Monto",0)  AS "Monto_Cartera",
			COALESCE(Z."Cantidad_Compra",0)    AS "Cantidad_Compra_Plazo",
			COALESCE(Z."Cantidad_Venta",0) AS "Cantidad_Venta_Plazo",
			COALESCE(Z."Monto",0) AS "Monto_Plazo",
			COALESCE(G."Cantidad",0) AS "Cantiddad_Garantia",
			COALESCE(G."Monto",0) AS "Monto_Garantia",
			COALESCE(E."Cantidad_Compra",0) AS "Cantidad_Compra_Prestamo",
			COALESCE(E."Cantidad_Venta",0) AS "Cantidad_Venta_Prestamo"
	FROM (
		SELECT DISTINCT 
			"Periodo" , "FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
			"INGMontoPrecioPromedioMonedaInstrumento","INGMontoMercadoActualMonedaInstrumento","INGPrecioMercadoActual","INGPrecioPromedioCompra","IdMonedaInstrumento"
		FROM (
			SELECT 
				"Periodo" , "FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
				"INGMontoPrecioPromedioMonedaInstrumento","INGMontoMercadoActualMonedaInstrumento","INGPrecioMercadoActual","INGPrecioPromedioCompra","IdMonedaInstrumento"
			FROM v_Saldo_Cartera
			UNION ALL
			SELECT 
				"Periodo" , "FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
				"INGMontoPrecioPromedioMonedaInstrumento","INGMontoMercadoActualMonedaInstrumento","INGPrecioMercadoActual","INGPrecioPromedioCompra","IdMonedaInstrumento"
			FROM v_Saldo_Plazo
			UNION ALL
			SELECT 
				"Periodo" , "FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
				"INGMontoPrecioPromedioMonedaInstrumento","INGMontoMercadoActualMonedaInstrumento","INGPrecioMercadoActual","INGPrecioPromedioCompra","IdMonedaInstrumento"
			FROM v_Saldo_Garantia
			UNION ALL
			SELECT
				"Periodo" , "FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
				"INGMontoPrecioPromedioMonedaInstrumento","INGMontoMercadoActualMonedaInstrumento","INGPrecioMercadoActual","INGPrecioPromedioCompra","IdMonedaInstrumento"
			FROM v_Saldo_Prestamo
			
			UNION ALL
			
			SELECT 
				"Periodo" , "FolioCartera","IdCliente","IdPortafolio","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdBoveda",
				"INGMontoPrecioPromedioMonedaInstrumento","INGMontoMercadoActualMonedaInstrumento","INGPrecioMercadoActual","INGPrecioPromedioCompra","IdMonedaInstrumento"
			FROM v_Saldo_Transito
				
		)
	) as P
	LEFT JOIN v_Saldo_Cartera as C 
			ON C."IdCliente" = P."IdCliente"
			AND P."Periodo" = C."Periodo"  
			AND C."FolioCartera" = P."FolioCartera"
			AND C."IdPortafolio" = P."IdPortafolio"
			AND C."IdTipoInstrumento" = P."IdTipoInstrumento"
			AND C."IdInstrumento" = P."IdInstrumento"
			AND C."IdBoveda" = P."IdBoveda"
			AND C."INGMontoPrecioPromedioMonedaInstrumento" = P."INGMontoPrecioPromedioMonedaInstrumento"
			AND C."INGMontoMercadoActualMonedaInstrumento" = P."INGMontoMercadoActualMonedaInstrumento"
			AND C."INGPrecioMercadoActual" = P."INGPrecioMercadoActual"
			AND C."INGPrecioPromedioCompra" = P."INGPrecioPromedioCompra"
			AND C."IdMonedaInstrumento" = P."IdMonedaInstrumento"
			
	LEFT JOIN v_Saldo_Plazo as Z
			ON Z."IdCliente" = P."IdCliente"
			AND P."Periodo" = z."Periodo"			
			AND Z."FolioCartera" = P."FolioCartera"
			AND Z."IdPortafolio" = P."IdPortafolio"
			AND Z."IdTipoInstrumento" = P."IdTipoInstrumento"
			AND Z."IdInstrumento" = P."IdInstrumento"
			AND Z."IdBoveda" = P."IdBoveda"
			AND Z."INGMontoPrecioPromedioMonedaInstrumento" = P."INGMontoPrecioPromedioMonedaInstrumento"
			AND Z."INGMontoMercadoActualMonedaInstrumento" = P."INGMontoMercadoActualMonedaInstrumento"
			AND Z."INGPrecioMercadoActual" = P."INGPrecioMercadoActual"
			AND Z."INGPrecioPromedioCompra" = P."INGPrecioPromedioCompra"
			AND Z."IdMonedaInstrumento" = P."IdMonedaInstrumento"
	LEFT JOIN v_Saldo_Garantia as G 
			ON G."IdCliente" = P."IdCliente"
			AND P."Periodo" = g."Periodo"			
			AND G."FolioCartera" = P."FolioCartera"
			AND G."IdPortafolio" = P."IdPortafolio"
			AND G."IdTipoInstrumento" = P."IdTipoInstrumento"
			AND G."IdInstrumento" = P."IdInstrumento"
			AND G."IdBoveda" = P."IdBoveda"
			AND G."INGMontoPrecioPromedioMonedaInstrumento" = P."INGMontoPrecioPromedioMonedaInstrumento"
			AND G."INGMontoMercadoActualMonedaInstrumento" = P."INGMontoMercadoActualMonedaInstrumento"
			AND G."INGPrecioMercadoActual" = P."INGPrecioMercadoActual"
			AND G."INGPrecioPromedioCompra" = P."INGPrecioPromedioCompra"
			AND G."IdMonedaInstrumento" = P."IdMonedaInstrumento"
	LEFT JOIN v_Saldo_Prestamo as E 
			ON E."IdCliente" = P."IdCliente"
			AND e."Periodo" = C."Periodo"			
			AND E."FolioCartera" = P."FolioCartera"
			AND E."IdPortafolio" = P."IdPortafolio"
			AND E."IdTipoInstrumento" = P."IdTipoInstrumento"
			AND E."IdInstrumento" = P."IdInstrumento"
			AND E."IdBoveda" = P."IdBoveda"
			AND E."INGMontoPrecioPromedioMonedaInstrumento" = P."INGMontoPrecioPromedioMonedaInstrumento"
			AND E."INGMontoMercadoActualMonedaInstrumento" = P."INGMontoMercadoActualMonedaInstrumento"
			AND E."INGPrecioMercadoActual" = P."INGPrecioMercadoActual"
			AND E."INGPrecioPromedioCompra" = P."INGPrecioPromedioCompra"
			AND E."IdMonedaInstrumento" = P."IdMonedaInstrumento"
			
	LEFT JOIN v_Saldo_Transito as T
			ON P."IdCliente" = T."IdCliente"
			AND P."Periodo" = t."Periodo"			
			AND P."FolioCartera" = T."FolioCartera"
			AND P."IdPortafolio" = T."IdPortafolio"
			AND P."IdTipoInstrumento" = T."IdTipoInstrumento"
			AND P."IdInstrumento" = T."IdInstrumento"
		--	AND P."IdBoveda" = T."IdBoveda"
			AND P."INGMontoPrecioPromedioMonedaInstrumento" = T."INGMontoPrecioPromedioMonedaInstrumento"
			AND P."INGMontoMercadoActualMonedaInstrumento" = T."INGMontoMercadoActualMonedaInstrumento"
			AND P."INGPrecioMercadoActual" = T."INGPrecioMercadoActual"
			AND P."INGPrecioPromedioCompra" = T."INGPrecioPromedioCompra"
			AND P."IdMonedaInstrumento" = T."IdMonedaInstrumento"
			

) AS SCUST
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = SCUST."IdCliente"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER ON PER."IdPersona" = CLI."IdPersona"
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON POR."IdCliente" = CLI."IdCliente"  AND SCUST."IdPortafolio" = por."IdPortafolio"
INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS fec ON (fec."IdEmpresa" = 1 and "CodigoParametro" = 'PLA.FechaSistema')

LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS hpr ON (hpr."TipoValor" = 'TipoInstrumentoSegunMercado' 
                                                                   AND hpr."Entidad" = 'TipoInstrumentoSegunMercado' 
                                                                   AND hpr."ValorDestino" = SCUST."IdTipoInstrumento" 
                                                                   AND hpr."ValorOrigen" NOT IN ('CM' , 'EN' , 'ET'))


left JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'TBCTCURV' AND WST."Key" = SCUST."FolioCartera"
	cross JOIN (SELECT 'TOT' AS BOVEDA  FROM dummy UNION 
				SELECT null AS BOVEDA  FROM dummy 
								)
WHERE "BSCL.Replica.BD.Servicios::func_IsNumeric"(POR."CodigoPortafolio") != 0								
	AND LEFT(fec."Valor",7) !=  SCUST."Periodo"	
GROUP BY SCUST."Periodo"		, CLI."IdCliente", LEFT(CASE COALESCE( hpr."ValorOrigen", scust."IdTipoInstrumento") 
						WHEN   'OSA' 			THEN 'AC' 
						WHEN   'SIM' 			THEN 'AC'
						WHEN   'ETN' 			THEN 'EN'
						WHEN   'ETX' 			THEN 'ET'
						ELSE COALESCE( hpr."ValorOrigen", scust."IdTipoInstrumento") END,2) , LEFT(fec."Valor",7),PER."DocumentoIdentidad",POR."CodigoPortafolio", SCUST."Nemotecnico",COALESCE(BOVEDA , SCUST."IdBoveda",'DCV' ) ,SCUST."IdMonedaInstrumento"


WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTBCTCURV" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

