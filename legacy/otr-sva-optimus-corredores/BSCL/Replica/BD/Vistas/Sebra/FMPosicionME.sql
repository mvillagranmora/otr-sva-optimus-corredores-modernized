SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.FMPosicionME"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.FMPosicionME" AS

WITH
	v_posicionIni as (
		 Select --FecPosicion = @Fecha,
		           1 AS CodProducto              ,
		           1 AS CodSubProducto           ,
		           13 AS CodMonPrinc             ,
		           13 AS CodMonSecu              ,
		           0 AS PosicInicMonPrinc        ,
		           0 AS PrecioPosicionInic       ,
		           0 AS PosicionInicMonSecu      ,
		           0 AS PrecioPosicionCierre     ,
		           0 AS MtoHedgeIniFuturo        ,
		           0 AS MtoHedgeActFuturo        ,
		           0 AS MtoHedgeIniSpot          ,
		           0 AS MtoHedgeActualSpot       ,
		           'NO SE HAN REGISTRADO OPERACIONES' AS UltimaOper,
		           '09:00' AS FechaHoraCreacion ,
		           0 AS PrecioPosicionCierreIng ,
		           1 AS TipoPosicion            ,
		           0 AS MtoVentasArb            ,
		           0 AS MtoComprasArb           
				FROM dummy
		    Union all
		
		    Select --FecPosicion              = @Fecha,
		           1 AS CodProducto              ,
		           1 AS CodSubProducto           ,
		           13 AS CodMonPrinc             ,
		           13 AS CodMonSecu              ,
		           0 AS  PosicInicMonPrinc       ,
		            0 AS PrecioPosicionInic      ,
		            0 AS PosicionInicMonSecu     ,
		            0 AS PrecioPosicionCierre    ,
		            0 AS MtoHedgeIniFuturo       ,
		            0 AS MtoHedgeActFuturo       ,
		            0 AS MtoHedgeIniSpot         ,
		            0 AS MtoHedgeActualSpot      ,
  					'NO SE HAN REGISTRADO OPERACIONES' AS UltimaOper,
		           '09:00' AS FechaHoraCreacion ,
		            0 AS PrecioPosicionCierreIng ,
		           2 AS TipoPosicion             ,
		            0 AS MtoVentasArb            ,
		            0 AS MtoComprasArb           
				FROM dummy
		    Union all
		
		    SELECT-- FecPosicion              = @Fecha,
		          1 as CodProducto              ,
		          1 AS  CodSubProducto          ,
		           0 AS  CodMonPrinc            ,
		            0 AS CodMonSecu             ,
		            0 AS PosicInicMonPrinc      ,
		            0 AS PrecioPosicionInic     ,
		            0 AS PosicionInicMonSecu    ,
		            0 AS PrecioPosicionCierre   ,
		            0 AS MtoHedgeIniFuturo      ,
		            0 AS MtoHedgeActFuturo      ,
		            0 AS MtoHedgeIniSpot        ,
		            0 AS MtoHedgeActualSpot     ,
  					'NO SE HAN REGISTRADO OPERACIONES' AS UltimaOper,
		           '09:00' AS FechaHoraCreacion ,
		            0 AS PrecioPosicionCierreIng  ,
		           1 AS TipoPosicion             ,
		            0 AS MtoVentasArb            ,
		            0 AS MtoComprasArb           
				FROM dummy
		    Union all 
		
		    SELECT-- FecPosicion              = @Fecha,
		          1 as CodProducto              ,
		           1 AS CodSubProducto           ,
		            0 AS CodMonPrinc            ,
		            0 AS CodMonSecu             ,
		            0 AS PosicInicMonPrinc      ,
		            0 AS PrecioPosicionInic     ,
		            0 AS PosicionInicMonSecu    ,
		            0 AS PrecioPosicionCierre   ,
		            0 AS MtoHedgeIniFuturo      ,
		            0 AS MtoHedgeActFuturo      ,
		            0 AS MtoHedgeIniSpot        ,
		            0 AS MtoHedgeActualSpot     ,
  					'NO SE HAN REGISTRADO OPERACIONES' AS UltimaOper,
		           '09:00' AS FechaHoraCreacion ,
		            0 AS PrecioPosicionCierreIng  ,
		           2 AS TipoPosicion             ,
		            0 AS MtoVentasArb            ,
		            0 AS MtoComprasArb           
		           FROM dummy
	),
	v_SpotOperaciones AS (
		select  fec."Valor" ,spottt.*  
		from "BSCL.Replica.BD.Vistas::Sebra.FMMovtosSpot" AS spottt
			INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS fec ON (fec."IdEmpresa" = 1 and "CodigoParametro" = 'PLA.FechaSistema')
		 where FechaOperacion >= '2022-05-27' and FechaOperacion <= '2022-05-27 23:00' -- fec."Valor"
	)

	
SELECT
	fec."Valor" AS FECPOSICION, 	--DATETIME
	v_posicionIni.CODPRODUCTO AS CODPRODUCTO, 						--INT
	v_posicionIni.CODSUBPRODUCTO AS CODSUBPRODUCTO, 					--INT
	v_posicionIni.CODMONPRINC AS CODMONPRINC, 						--INT
	v_posicionIni.CODMONSECU AS CODMONSECU, 						--INT
	v_posicionIni.TipoPosicion AS TIPOPOSICION, 						--INT
	v_posicionIni.PosicInicMonPrinc AS POSICINICMONPRINC, 				--DECIMAL(18, 4)
	v_posicionIni.PrecioPosicionInic AS PRECIOPOSICIONINIC, 				--DECIMAL(20, 10)
	v_posicionIni.PosicionInicMonSecu AS POSICIONINICMONSECU, 				--DECIMAL(18, 4)
	
	mtoventasmonprincCompras AS MTOCOMPRASMONPRINC, 				--DECIMAL(18, 4)
	precioPromCompras AS PRECIOPROMCOMPRAS, 				--DECIMAL(20, 10)
	'' AS INDPRECIOPROMCOMPRAS, 			--CHAR(1)
	mtoventasmonSecuCompras AS MTOCOMPRASMONSECU, 				--DECIMAL(18, 4)
	
	mtoventasmonprincVentas AS MTOVENTASMONPRINC, 				--DECIMAL(18, 4)
	precioPromVentas AS PRECIOPROMVENTAS, 					--DECIMAL(20, 10)
	'' AS INDPRECIOPROMVENTAS, 				--CHAR(1)
	mtoventasmonSecuVentas AS MTOVENTASMONSECU, 					--DECIMAL(18, 4)
	
	PosicCierreMonPrincTotal AS POSICCIERREMONPRINC, 				--DECIMAL(18, 4)
	CASE WHEN precioAnterior > ultimaOperSpot.TCCIERRE THEN '-' ELSE '+' END AS INDPOSICCIERREMONPRINC, 			--CHAR(1)
	ultimaOperSpot.TCCIERRE AS PRECIOPOSICIONCIERRE, 				--DECIMAL(20, 10)
	CASE WHEN precioAnterior > ultimaOperSpot.TCCIERRE THEN '-' ELSE '+' END  AS INDPRECIOPOSICIONCIERRE, 			--CHAR(1)
	ultimaOperSpot.TCCIERRE AS PRECIOPOSICIONCIERREING, 			--DECIMAL(20, 10)
	CASE WHEN precioAnterior > ultimaOperSpot.TCCIERRE THEN '-' ELSE '+' END  AS INDPRECIOPOSICIONCIERREING, 		--CHAR(1)
	
	PosicCierreMonMtoMonSecuTotal AS POSICIONCIERREMONSECU, 			--DECIMAL(18, 4)
	
	v_posicionIni.MtoHedgeIniFuturo AS MTOHEDGEINIFUTURO, 				--DECIMAL(18, 4)
	'' AS INDHEDGEINIFUTURO, 				--CHAR(1)
	0 AS MTOHEDGEINISPOT, 					--DECIMAL(18, 4)
	'' AS INDHEDGEINISPOT, 					--CHAR(1)
	0 AS MTOHEDGEINIPRECIO, 				--DECIMAL(18, 4)
	'' AS INDHEDGEINIPRECIO, 				--CHAR(1)
	v_posicionIni.MtoHedgeActFuturo AS MTOHEDGEACTFUTURO,  				--DECIMAL(18, 4)
	'' AS INDHEDGEACTFUTURO, 				--CHAR(1)
	0 AS MTOHEDGEACTUALSPOT, 				--DECIMAL(18, 4)
	'' AS INDHEDGEACTUALSPOT,				--CHAR(1)
	0 AS MTOHEDGEACTVCTOSFUT, 				--DECIMAL(18, 4)
	'' AS INDHEDGEACTVCTOSFUT, 				--CHAR(1)
	0 AS MTOHEDGEACTDESCALCE, 				--DECIMAL(18, 4)
	'' AS INDHEDGEACTDESCALCE, 				--CHAR(1)
	0 AS MTOHEDGEUTILIDAD, 					--DECIMAL(18, 4)
	'' AS INDHEDGEUTILIDAD, 				--CHAR(1)
	0 AS MTOTRADINGRESULTDIA, 				--DECIMAL(18, 4)
	'' AS INDTRADINGRESULTDIA, 				--CHAR(1)
	0 AS MTOTRADINGRESULTACUMDIA, 			--DECIMAL(18, 4)
	'' AS INDTRADINGRESULTACUMDIA, 			--CHAR(1)
	0 AS MTOTRADINGRESULTACUMMES, 			--DECIMAL(18, 4)
	'' AS INDTRADINGRESULTACUMMES, 			--CHAR(1)
	0 AS MTOTRADINGRESULTACUMMESANT, 		--DECIMAL(18, 4)
	'' AS INDTRADINGRESULTACUMMESANT, 		--CHAR(1)
	0 AS MTORESERVADO1, 					--DECIMAL(18, 4)
	'' AS INDRESERVADO1, 					--CHAR(1)
	0 AS MTORESERVADO2, 					--DECIMAL(18, 4)
	'' AS INDRESERVADO2, 					--CHAR(1)
	0 AS MTORESERVADO3, 					--DECIMAL(18, 4)
	'' AS INDRESERVADO3, 					--CHAR(1)
	0 AS MTORESERVADO4, 					--DECIMAL(18, 4)
	'' AS INDRESERVADO4, 					--CHAR(1)
	0 AS MTORESERVADO5, 					--DECIMAL(18, 4)
	'' AS INDRESERVADO5, 					--CHAR(1)
	cantidadCompras AS NUMOPERCOMPRAS, 					--INT
	cantidadVentas AS NUMOPERVENTAS, 					--INT
	cantidadAnulaciones AS NUMOPERANULACIONES, 				--INT
	ultimaOperSpot.CodTipoMovto	 AS CODTIPOMOVTO, 						--INT
	ultimaOperSpot.FolioOperacion AS FOLIOOPERACION, 					--NUMERIC(10, 0)
	COALESCE(CASE WHEN ultimaOperSpot.CodTipoMovto = 2 THEN 'Venta Spot ' ELSE 'Compra Spot ' END || 
					CASE WHEN ultimaOperSpot.CodMonPrinc = 13 THEN 'USD' ELSE 'CLP' END || ' a ' || ultimaOperSpot.per_raz_social , UltimaOper ) AS ULTIMAOPER, 						--VARCHAR(200)
	0 AS MTORENTABCOMPRAS, 					--DECIMAL(18, 4)
	'' AS INDRENTABCOMPRAS, 				--CHAR(1)
	0 AS MTORENTABVENTAS, 					--DECIMAL(18, 4)
	'' AS INDRENTABVENTAS, 					--CHAR(1)
	v_posicionIni.FechaHoraCreacion AS FECHAHORACREACION, 	--DATETIME
	0 AS MTOCOMPRASARB, 					--DECIMAL(18, 4)
	0 AS MTOVENTASARB, 						--DECIMAL(18, 4)
	0 AS MTOUTILIDADCV, 					--NUMERIC(18, 4)
	0 AS MTOUTILIDADACUMDIACV, 				--NUMERIC(18, 4)
	0 AS MTOUTILIDADACUMMESCV, 				--NUMERIC(18, 4)
	0 AS MTOUTILIDADARB, 					--NUMERIC(18, 4)
	0 AS MTOUTILIDADACUMDIAARB, 			--NUMERIC(18, 4)
	0 AS MTOUTILIDADACUMMESARB, 			--NUMERIC(18, 4)
	0 AS MTOFWDCOMPRAS, 					--NUMERIC(18, 4)
	0 AS MTOFWDVENTAS, 						--NUMERIC(18, 4)
	0 AS MTOFWDPOSICION, 					--NUMERIC(18, 4)
	'' AS IDRFWDPOSICION, 					--CHAR(1)
	0 AS MTOCOMMONPRINCCONS,				--DECIMAL(18, 4)
	0 AS MTOCOMMONSECUCONS, 				--DECIMAL(18, 4)
	0 AS PRECIOPROMCOMCONS, 				--DECIMAL(20, 10)
	0 AS MTOVENMONPRINCCONS,				--DECIMAL(18, 4,
	0 AS MTOVENMONSECUCONS, 				--DECIMAL(18, 4)
	0 AS PRECIOPROMVENCONS 					--DECIMAL(20, 10)


FROM v_posicionIni 
	INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS fec ON (fec."IdEmpresa" = 1 and "CodigoParametro" = 'PLA.FechaSistema')
	, (
		SELECT sum(CASE WHEN CodTipoMovto = 2 THEN 1 ELSE 0 end ) AS cantidadVentas, 
				sum(CASE WHEN CodTipoMovto = 1 THEN 1 ELSE 0 end ) AS cantidadCompras,
				sum(CASE WHEN estado = 'A' THEN 1 ELSE 0 end) AS cantidadAnulaciones,
				
				sum(CASE WHEN CodTipoMovto = 2 THEN MtoMonPrinc ELSE 0 end ) AS mtoventasmonprincVentas,
				sum(CASE WHEN CodTipoMovto = 1 THEN MtoMonPrinc ELSE 0 end ) AS mtoventasmonprincCompras,
				
				sum(CASE WHEN CodTipoMovto = 2 THEN MtoMonSecu ELSE 0 end ) AS mtoventasmonSecuVentas,
				sum(CASE WHEN CodTipoMovto = 1 THEN MtoMonSecu ELSE 0 end ) AS mtoventasmonSecuCompras,
				
				avg(CASE WHEN CodTipoMovto = 2 THEN TCCIERRE ELSE 0 end ) AS precioPromVentas,
				avg(CASE WHEN CodTipoMovto = 1 THEN TCCIERRE ELSE 0 end ) AS precioPromCompras,
				
				
				sum( Case When CodTipoMovto = 1 then 1 Else -1 End * MtoMonSecu )   AS PosicCierreMonPrincTotal,
				sum( Case When CodTipoMovto = 1 then 1 Else -1 End * MtoMonSecu )   AS PosicCierreMonMtoMonSecuTotal
				
				
			FROM  v_SpotOperaciones
	) AS resumenOperSpot, (
		SELECT  top 1 p.per_raz_social,spott.*,  LAG(spott.TCCIERRE) OVER (PARTITION BY FolioTransaccion ORDER BY FolioTransaccion desc) AS precioAnterior
			FROM  v_SpotOperaciones spott join "BSCL.Replica.BD.Vistas::Sebra.PERSONA" AS p
					ON ( rtrim(ltrim(p.per_rutdv)) = ltrim(rtrim(spott.RutCliente)))
			
			ORDER BY FolioTransaccion desc
	) AS ultimaOperSpot
	

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.FMPosicionME" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
