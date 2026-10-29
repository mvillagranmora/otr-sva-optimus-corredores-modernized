WITH
	Cartera AS
	(
		SELECT
			'Cartera' AS "TipoCus"
		   ,CAR."IdCliente"
		   ,CAR."IdPortafolio"
		   ,'' AS "IdTipoOperacion"
		   ,CAR."IdClasificacionTipoInstrumento"
		   ,CAR."IdTipoInstrumento"
		   ,CAR."IdInstrumento"
		   --------------------------------------------------------------------------------------------
		   ,CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'DV' AND CAR."IdTipoInstrumento" = 'FWD' THEN CAR."IdMonedaSistema" ELSE CAR."IdMonedaInstrumento" END AS "IdMonedaOperacion"
		   ,CAR."INGMontoCompraActualMonedaInstrumento" AS "MontoCompraOperacion"
		   ,(CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'DV' AND CAR."IdTipoInstrumento" = 'FWD' THEN CAR."INGMontoValorRazonablePasivo" - CAR."INGMontoValorRazonableActivo" 
		   		  WHEN CAR."IdClasificacionTipoInstrumento" IN ('IF','RF') THEN CAR."INGMontoMercadoActualMonedaInstrumento"
		   		  WHEN CAR."IdClasificacionTipoInstrumento" IN ('RV') THEN COALESCE(ROUND(CAR."INGCantidad" * COALESCE(PREMER."PrecioCierre", CAR."INGPrecioMercadoActual"),0),0)
		   		  ELSE COALESCE(ROUND(CAR."INGCantidad" * COALESCE(PREMER."PrecioCierre", CAR."INGPrecioMercadoActual"),0),0) END) AS "MontoMercadoOperacion"
		FROM "BSCL.Custodia.BD::HIS.Cartera" CAR
		INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON CAR."IdPortafolio" = P."IdPortafolio"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
		LEFT JOIN 
		(
				SELECT PRE."IdInstrumento",PRE."PrecioCierre",PRE."Fecha",PRE."IdEmpresa"	
				FROM
					(	
						SELECT row_number() over (partition by prec."IdInstrumento",SIS."IdEmpresa" order by prec."IdInstrumento",prec."Fecha" desc) AS row_num
								,prec."IdInstrumento",prec."Fecha",	prec."IdBolsa",prec."PrecioCierre",SIS."IdEmpresa"
						FROM "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" prec
							INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" SIS ON  ( SIS."CodigoParametro" = 'PLA.FechaSistema' AND TO_DATE(prec."Fecha") <= TO_DATE(:FechaHasta)) 
				 		WHERE prec."IdBolsa" = 'BCS'
				 		AND prec."PrecioCierre"  > 0
					) PRE	
				WHERE PRE.row_num = 1 
				) 
				PREMER ON (CAR."IdInstrumento"=PREMER."IdInstrumento"  AND CAR."IdEmpresa"= PREMER."IdEmpresa"
		)			
		WHERE
		CAR."FechaRespaldo" = :FechaHasta
		AND	CAR."IdClasificacionTipoInstrumento" IN ('RF','IF','FM'/*,'DV'*/)
		AND CAR."IdEstado" = 'VIG'
		AND CAR."IdEmpresa" = :IdEmpresa
		AND CAR."IdTipoOperacion" != 'VENPAC'
		AND CAR."INGFechaOperacion" <= (CASE WHEN CAR."IdTipoInstrumento" = 'FWD' THEN CAR."INGFechaLiquidacion" ELSE :FechaHasta END)
		AND CAR."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF')
		AND (:IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :IdProductoAgrupador)
		AND P."IdTrabajo" = :IdTrabajo
	),
	CarteraNegativa AS -- (Por ahora solo mercado RF)
	(
		SELECT
			'CarteraNegativa' AS "TipoCus"
		   ,CAR."IdCliente"
		   ,CAR."IdPortafolio"
		   ,'' AS "IdTipoOperacion"
		   ,ClasInst."IdClasificacionTipoInstrumento"
		   ,TipoInstrumento."IdTipoInstrumento"
		   ,CAR."IdInstrumento"
		   ,CASE WHEN IFNULL(CAR."IdMonedaInstrumento",'') <> '' THEN CAR."IdMonedaInstrumento" ELSE IFNULL(CAR."IdTipoReajuste",'') END AS "IdMonedaOperacion"
		   ,CAR."MontoOperacionMonedaInstrumento" * -1 AS "MontoCompraOperacion" -- CAR."INGMontoCompraActualMonedaInstrumento" AS "MontoCompraOperacion"
		   ,CAR."MontoMercadoMonedaInstrumento" * -1 AS "MontoMercadoOperacion" --,CAR."INGMontoATasaMercadoActual" AS "MontoMercadoOperacion"
		FROM "BSCL.Custodia.BD::HIS.CarteraNegativa" CAR
		INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON CAR."IdPortafolio" = P."IdPortafolio"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS	ON INS."IdInstrumento" = CAR."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoInstrumento ON TipoInstrumento."IdTipoInstrumento" = INS."IdTipoInstrumento" 
		LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" as ClasInst ON ClasInst."IdClasificacionTipoInstrumento" = TipoInstrumento."IdClasificacionTipoInstrumento"		
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
		LEFT JOIN 
		(
				SELECT PRE."IdInstrumento",PRE."PrecioCierre",PRE."Fecha",PRE."IdEmpresa"	
				FROM
					(	
						SELECT row_number() over (partition by prec."IdInstrumento",SIS."IdEmpresa" order by prec."IdInstrumento",prec."Fecha" desc) AS row_num
								,prec."IdInstrumento",prec."Fecha",	prec."IdBolsa",prec."PrecioCierre",SIS."IdEmpresa"
						FROM "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" prec
							INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" SIS ON  ( SIS."CodigoParametro" = 'PLA.FechaSistema' AND TO_DATE(prec."Fecha") <= TO_DATE(:FechaHasta)) 
				 		WHERE prec."IdBolsa" = 'BCS'
				 		AND prec."PrecioCierre"  > 0
					) PRE	
				WHERE PRE.row_num = 1 
		) PREMER ON (CAR."IdInstrumento"=PREMER."IdInstrumento"  AND CAR."IdEmpresa"= PREMER."IdEmpresa"
		)			
		WHERE
			ClasInst."IdClasificacionTipoInstrumento" IN ('RF', 'RV', 'IF')
		AND CAR."IdEstado" = 'VIG'
		AND CAR."IdEmpresa" = :IdEmpresa
		AND CAR."IdTipoOperacion" != 'VENPAC'
		AND CAR."FechaRespaldo" = :FechaHasta
		AND CAR."FechaOperacion" <= :FechaHasta 
		AND (:IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :IdProductoAgrupador)
		AND P."IdTrabajo" = :IdTrabajo
	),
	Garantia AS
	(
		SELECT
			'Garantia' AS "TipoCus"
		   ,CAR."IdCliente"
		   ,CAR."IdPortafolio"
		   ,'' AS "IdTipoOperacion"
		   ,GAR."IdClasificacionTipoInstrumento"
		   ,GAR."IdTipoInstrumento"
		   ,GAR."IdInstrumento"
		   --------------------------------------------------------------------------------------------
		   ,GAR."IdMonedaInstrumento" AS "IdMonedaOperacion"
		   ,GAR."INGMontoGarantiaActualMonedaInstrumento" AS "MontoCompraOperacion"
		   ,GAR."INGMontoMercadoActualMonedaInstrumento" AS "MontoMercadoOperacion"
		FROM "BSCL.Custodia.BD::HIS.Garantia" GAR
		INNER JOIN
		(
			SELECT
				CCAR."EsPrimeraCartera"
			   ,CAR."FolioCartera"
			   ,CAR."IdCliente"
			   ,CAR."IdPortafolio"
			   ,CAR."FechaRespaldo"
			   ,CAR."IdTipoOperacion"
			FROM "BSCL.Custodia.BD::HIS.Cartera" CAR
			INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON CAR."IdPortafolio" = P."IdPortafolio"
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
			LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CCAR ON CCAR."FolioCartera" = CAR."FolioCartera" AND CCAR."FechaOperacion" = CAR."FechaOperacion" 
			WHERE 
			CAR."FechaRespaldo" = :FechaHasta
			AND	CAR."IdEmpresa" = :IdEmpresa
			AND CCAR."EsPrimeraCartera" = 'S'
			AND (:IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :IdProductoAgrupador)
			AND P."IdTrabajo" = :IdTrabajo		
		) AS CAR ON CAR."FolioCartera" = GAR."FolioCarteraOrigen" AND CAR."FechaRespaldo" = GAR."FechaRespaldo"
		WHERE
			GAR."IdClasificacionTipoInstrumento" IN ('RV','RF','IF','FM','DV')
		AND GAR."IdEstado" = 'VIG'
		AND GAR."INGFechaOperacion" <= :FechaHasta
		AND CAR."IdTipoOperacion" != 'VENPAC'
	),
	Subyacente AS
	(	
	SELECT
			'Subyacente' AS "TipoCus"
		   ,CAR."IdCliente"
		   ,CAR."IdPortafolio"
		   ,'' AS "IdTipoOperacion"
		   ,SUB."IdClasificacionTipoInstrumento"
		   ,TINS."IdTipoInstrumento"
		   ,SUB."IdInstrumento"
		   ,TINS."Nombre" AS "NombreTipoInstrumento"
		   --------------------------------------------------------------------------------------------
		   ,SUB."IdMonedaInstrumento"  AS "IdMonedaOperacion"--CASE WHEN SUB."IdTipoReajuste" <> '' THEN SUB."IdTipoReajuste" ELSE SUB."IdMonedaInstrumento" END AS "IdMonedaOperacion"
		   ,SUM(CASE WHEN SUB."INGMontoSubyacenteActualMonedaInstrumento" > 0 THEN 
		   															CASE WHEN CAR."IdTipoOperacion" IN ('VENSIM','COMPAC') THEN -1*SUB."INGMontoSubyacenteActualMonedaInstrumento" ELSE SUB."INGMontoSubyacenteActualMonedaInstrumento" END 
		   													  ELSE 	
		   													  		CASE WHEN CAR."IdTipoOperacion" IN ('VENSIM','COMPAC') THEN -1*SUB."INGMontoMercadoActualMonedaInstrumento" ELSE SUB."INGMontoMercadoActualMonedaInstrumento" END
		   													  END) AS "MontoCompraOperacion"
		   ,SUM(CASE WHEN CAR."MontoAPlazo" > 0 	THEN 
		   														--CASE WHEN CAR."IdTipoOperacion" IN ('VENSIM','COMPAC') THEN -1*CAR."MontoAPlazo" ELSE CAR."MontoAPlazo" END
		   														CASE WHEN CAR."IdTipoOperacion" IN ('VENSIM','COMPAC') THEN -1*(CAR."INGCantidad" * coalesce(MovComprom."Precio",0)) ELSE CAR."INGCantidad" * coalesce(MovComprom."Precio",0) END
		   													ELSE 
		   														CASE WHEN CAR."IdTipoOperacion" IN ('VENSIM','COMPAC') THEN -1*SUB."INGMontoSubyacenteActualMonedaInstrumento" ELSE SUB."INGMontoSubyacenteActualMonedaInstrumento" END 
		   												   	END) AS "MontoMercadoOperacion"
		FROM 
		(
		SELECT 
			"FechaRespaldo"
			,"FolioCarteraRelacionada"
			,"IdClasificacionTipoInstrumento"
			,"IdInstrumento"
			,"Nemotecnico"
			,"IdMonedaInstrumento"
			,"IdEstado"
			,SUM("INGMontoMercadoActualMonedaInstrumento") AS "INGMontoMercadoActualMonedaInstrumento"
			,SUM("INGMontoSubyacenteActualMonedaInstrumento") AS "INGMontoSubyacenteActualMonedaInstrumento"
		FROM "BSCL.Custodia.BD::HIS.Subyacente" SUB
		WHERE SUB."IdEstado" = 'VIG' AND "IdClasificacionTipoInstrumento" IN ('RV','RF','IF','FM','DV')
		AND SUB."FechaRespaldo" = :FechaHasta
		GROUP BY 
			"FechaRespaldo"
			,"FolioCarteraRelacionada"
			,"IdClasificacionTipoInstrumento"
			,"IdInstrumento"
			,"Nemotecnico"
			,"IdMonedaInstrumento"
			,"IdEstado"
		) AS SUB
		INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" CAR ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera" AND SUB."FechaRespaldo" = CAR."FechaRespaldo"
		INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON CAR."IdPortafolio" = P."IdPortafolio"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = CAR."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TINS ON TINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS CTINS ON CTINS."IdClasificacionTipoInstrumento" = TINS."IdClasificacionTipoInstrumento"
		LEFT JOIN (SELECT DISTINCT MovNormal."IdEmpresa", MovNormal."IdComprobante", MovNormal."NemotecnicoSubyacente", MovPlazo."Precio" FROM "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteSimple" MovNormal
			LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteSimple" as MovPlazo on 
		 	MovPlazo."IdMovimientoComprobante" = MovNormal."IdMovimientoRelacionado"
		 )as MovComprom ON
			MovComprom."IdEmpresa" = CAR."IdEmpresa" and 
		 	MovComprom."IdComprobante" = CAR."FolioComprobante" and 
		 	MovComprom."NemotecnicoSubyacente" = SUB."Nemotecnico" 
		WHERE
		CAR."IdEmpresa" = :IdEmpresa
		AND (:IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :IdProductoAgrupador)
		AND P."IdTrabajo" = :IdTrabajo
		AND	SUB."IdClasificacionTipoInstrumento" IN ('RV','RF','IF','FM','DV')
		AND CAR."IdTipoOperacion" NOT IN ('COMRRV','COMRRF','PRESTA', 'EMPRES')
		AND CAR."IdTipoInstrumento" <> 'PAC'
		AND SUB."IdEstado" = 'VIG'
		AND CAR."IdEstado" = 'VIG'
		GROUP BY 
			CAR."IdCliente"
		   ,CAR."IdPortafolio"
		   ,SUB."IdClasificacionTipoInstrumento"
		   ,TINS."IdTipoInstrumento"
		   ,SUB."IdInstrumento"
		   ,TINS."Nombre"
		   ,SUB."IdMonedaInstrumento"
	UNION ALL 
		SELECT
			'Subyacente' AS "TipoCus"
		   ,CAR."IdCliente"
		   ,CAR."IdPortafolio"
		   ,CAR."IdTipoOperacion"
		   ,SUB."IdClasificacionTipoInstrumento"
		   ,TINS."IdTipoInstrumento"
		   ,SUB."IdInstrumento"
		   ,TINS."Nombre" AS "NombreTipoInstrumento"
		   --------------------------------------------------------------------------------------------
		  ,CASE WHEN CAR."IdTipoInstrumento" = 'PAC' THEN 
		   			CAR."IdTipoReajuste" 
		   		ELSE CASE WHEN SUB."IdTipoReajuste" <> '' THEN 
		   					SUB."IdTipoReajuste" 
		   				ELSE SUB."IdMonedaInstrumento" END 
		   		END AS "IdMonedaOperacion"
		   ,CASE WHEN SUB."INGMontoSubyacenteActualMonedaInstrumento" > 0 THEN 
		   															CASE WHEN CAR."IdTipoOperacion" = 'COMPAC' THEN -1*SUB."INGMontoSubyacenteActualMonedaInstrumento" ELSE SUB."INGMontoSubyacenteActualMonedaInstrumento" END 
		   													  ELSE 	
		   													  		CASE WHEN CAR."IdTipoOperacion" = 'COMPAC' THEN -1*SUB."INGMontoMercadoActualMonedaInstrumento" ELSE SUB."INGMontoMercadoActualMonedaInstrumento" END
		   													  END AS "MontoCompraOperacion"
		   ,CASE WHEN CAR."MontoAPlazo" > 0 	THEN 
		   														CASE WHEN CAR."IdTipoOperacion" IN ('COMPAC') THEN -1*RES."INGMontoAPlazoMonedaInstrumento"
		   														 ELSE  RES."INGMontoAPlazoMonedaInstrumento"
		   												   	END END AS "MontoMercadoOperacion"
		FROM "BSCL.Custodia.BD::HIS.Subyacente" SUB
		INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" CAR ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera" AND SUB."FechaRespaldo" = CAR."FechaRespaldo"
		INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON CAR."IdPortafolio" = P."IdPortafolio"
		INNER JOIN "BSCL.Custodia.BD::CUS.Reserva" RES ON RES."IdOperacion" = CAR."IdOperacion" AND RES."FolioSubyacente" = SUB."FolioSubyacente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = CAR."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TINS ON TINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS CTINS ON CTINS."IdClasificacionTipoInstrumento" = TINS."IdClasificacionTipoInstrumento"
		WHERE
		CAR."IdEmpresa" = :IdEmpresa
		AND (:IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :IdProductoAgrupador)
		AND P."IdTrabajo" = :IdTrabajo
		AND	SUB."IdClasificacionTipoInstrumento" IN ('RF','IF')
		AND CAR."IdTipoOperacion" NOT IN ('COMRRV','COMRRF')
		AND CAR."IdTipoInstrumento" = 'PAC'
		AND SUB."IdEstado" = 'VIG'
		AND CAR."IdEstado" = 'VIG'
		AND SUB."FechaRespaldo" = :FechaHasta
		GROUP BY CAR."IdCliente"
		   ,CAR."IdPortafolio"
		   ,SUB."IdClasificacionTipoInstrumento"
		   ,TINS."IdTipoInstrumento"
		   ,SUB."IdInstrumento"
		   ,TINS."Nombre"
		   ,SUB."IdMonedaInstrumento"
		   ,CAR."MontoAPlazo"
		   ,CAR."IdTipoOperacion"
		   ,CAR."IdTipoInstrumento"
		   ,CAR."IdTipoReajuste"
		   ,SUB."IdTipoReajuste"
		   ,SUB."INGMontoSubyacenteActualMonedaInstrumento"
		   ,SUB."INGMontoMercadoActualMonedaInstrumento"
		   ,RES."INGMontoAPlazoMonedaInstrumento"
		   
	UNION ALL 
		--PRESTAMOS Y VENTA CORTA
		SELECT
	        'Subyacente' AS "TipoCus"
		    ,CAR."IdCliente"
		    ,CAR."IdPortafolio"
		    ,'' AS "IdTipoOperacion"
		    ,SUB."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
		    ,CASE WHEN TP."IdTipoPrestamo"  = 'PRTVTC' THEN 'VC' ELSE 'PT' END  AS "IdTipoInstrumento"
		    ,SUB."IdInstrumento" as "IdInstrumento"
		    ,CASE WHEN TP."IdTipoPrestamo"  = 'PRTVTC' THEN 'VENTA CORTA' ELSE 'PRESTAMOS' END  AS "NombreTipoInstrumento" 
		    ,CASE WHEN CAR."IdTipoReajuste" <> '' THEN CAR."IdTipoReajuste" ELSE CAR."IdMonedaInstrumento" END AS "IdMonedaOperacion"
		    ,CAR."INGMontoATasaCompraActual" AS "MontoCompraOperacion"
			,CASE 
			    WHEN COALESCE (PRECIO_TASA."Precio", -1) > 0 THEN 
			        PRECIO_TASA."Precio" * CAR."Cantidad"
			    ELSE 
			        COALESCE (CAR."INGPrecioMercadoActual", 0) * CAR."Cantidad"
			END AS "MontoMercadoOperacion"
		FROM "BSCL.Custodia.BD::HIS.Cartera" CAR
		INNER JOIN "BSCL.Custodia.BD::HIS.Subyacente" SUB ON CAR."FolioCartera" = SUB."FolioCarteraRelacionada" AND CAR."FechaRespaldo" = SUB."FechaRespaldo" 
		INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON CAR."IdPortafolio" = P."IdPortafolio"
		INNER JOIN "BSCL.Custodia.BD::PRT.TipoPrestamo" TP ON CAR."IdInstrumento" = TP."IdInstrumentoPrestamo" 
		LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoReajuste" as TipoReajuste ON (car."IdTipoReajuste" = TipoReajuste."IdTipoReajuste")
		LEFT JOIN "BSCL.Normativa.BD::CAR.PrecioTasaPaso" AS PRECIO_TASA ON SUB."IdInstrumento" = PRECIO_TASA."IdInstrumento" AND P."IdPortafolio" = PRECIO_TASA."IdPortafolio" AND PRECIO_TASA."IdTrabajo" = :IdTrabajo
		WHERE CAR."IdEmpresa" = :IdEmpresa 
		AND P."IdTrabajo" = :IdTrabajo
		AND CAR."FechaRespaldo" = :FechaHasta
		AND CAR."FechaOperacion" <= :FechaHasta 
		AND CAR."FechaVencimientoOperacion" >= :FechaHasta 
		AND ((CAR."EGRCantidad" = 0
		AND CAR."IdEstado" = 'VIG'
		AND CAR."INGFechaOperacion" <= :FechaHasta )
		OR (CAR."INGFechaOperacion" >= :FechaHasta and CAR."EGRCantidad" <> 0 and CAR."EGRFechaOperacion" >  :FechaHasta )OR CAR."IdEstado" = 'ANT')
		AND SUB."IdClasificacionTipoInstrumento" <>  ''
	UNION ALL 
		--EMPRESTITOS
		SELECT 
			 'Subyacente' AS "TipoCus"
			,CAR."IdCliente"
			,CAR."IdPortafolio"
			,'' AS "IdTipoOperacion"
			,SUB."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
			,'EM' AS "IdTipoInstrumento"
			,SUB."IdInstrumento" as "IdInstrumento"
			,'EMPRESTITOS' AS "NombreTipoInstrumento" 
			,CASE WHEN CAR."IdTipoReajuste" <> '' THEN CAR."IdTipoReajuste" ELSE CAR."IdMonedaInstrumento"  END AS "IdMonedaOperacion"
			,CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' AND CAR."EsFungible" = 'S' THEN
      	   	    CASE WHEN CAR."INGValorRescate" > resTot."IngValorRescate" THEN
      	        	 ( ( CAR."INGValorRescate"-resTot."IngValorRescate")  * CAR."INGMontoATasaMercadoActual" ) 
      	        	 / CAR."INGValorRescate"
      	        	 ELSE CAR."INGMontoATasaMercadoActual" 
      	        END
			  	   --* RF's
			  	   ELSE CASE WHEN resTot."IngCantidad" > 0 AND CAR."INGCantidad" > 0 THEN
			  	   	 		 ( ( CAR."INGCantidad"-resTot."IngCantidad")  * CAR."INGMontoATasaMercadoActual" ) 
			  	        	 / CAR."INGCantidad" 
			  	   			 ELSE CAR."INGMontoATasaMercadoActual" 
			  	   		END
			  END AS "MontoCompraOperacion"	
			,CASE WHEN COALESCE (PRECIO_TASA."Precio",-1) > 0 THEN 
	    		PRECIO_TASA."Precio" * CAR."INGCantidad"
	    	ELSE
	        	 COALESCE (CAR."INGPrecioMercadoActual", 0) * CAR."INGCantidad"
			END AS "MontoMercadoOperacion"	
		FROM "BSCL.Custodia.BD::HIS.Cartera" CAR
		INNER JOIN "BSCL.Custodia.BD::HIS.Subyacente" SUB ON CAR."FolioCartera" = SUB."FolioCarteraRelacionada" AND CAR."FechaRespaldo" = SUB."FechaRespaldo" 
		INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON CAR."IdPortafolio" = P."IdPortafolio"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoReajuste" as TipoReajuste ON (CAR."IdTipoReajuste" = TipoReajuste."IdTipoReajuste")
		LEFT JOIN "BSCL.Normativa.BD::CAR.PrecioTasaPaso" AS PRECIO_TASA ON SUB."IdInstrumento" = PRECIO_TASA."IdInstrumento" AND P."IdPortafolio" = PRECIO_TASA."IdPortafolio" AND PRECIO_TASA."IdTrabajo" = :IdTrabajo
		LEFT JOIN (
		    SELECT res."FolioCarteraOrigen"
		        ,SUM("IngCantidad") AS "IngCantidad"
		        ,SUM("IngValorRescate") AS "IngValorRescate"
		        ,SUM("MontoValorizado") AS "IngMontoValorizado"
		    FROM "BSCL.Custodia.BD::CUS.Reserva" AS res 
		    WHERE res."IdEstado"='VIG' AND res."EgrCantidad" = 0
		    GROUP BY res."FolioCarteraOrigen") AS resTot ON resTot."FolioCarteraOrigen" = CAR."FolioCartera"  
		WHERE CAR."IdEmpresa" = :IdEmpresa 
			AND CAR."IdTipoInstrumento" IN ('EMP','ERF')
			AND CAR."FechaRespaldo" = :FechaHasta
			AND CAR."FechaOperacion" <= :FechaHasta
			AND CAR."FechaVencimientoOperacion" >= :FechaHasta
			AND P."IdTrabajo" = :IdTrabajo
			AND CAR."IdEstado" = 'VIG'
			AND SUB."IdClasificacionTipoInstrumento" <>  ''
	),
	Transito AS
	(
		SELECT
			'Transito' AS "TipoCus"
		   ,CAR."IdCliente"
		   ,CAR."IdPortafolio"
		   ,'' AS "IdTipoOperacion"
		   ,CAR."IdClasificacionTipoInstrumento"
		   ,CAR."IdTipoInstrumento"
		   ,CAR."IdInstrumento"
		   --------------------------------------------------------------------------------------------
		   ,CAR."IdMonedaInstrumento" AS "IdMonedaOperacion"
		   ,CAR."MontoCompra" AS "MontoCompraOperacion"
		   ,CAR."MontoCompra" AS "MontoMercadoOperacion"
		FROM "BSCL.Custodia.BD::HIS.MovimientoCarteraEnTransito" CAR
		INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON CAR."IdPortafolio" = P."IdPortafolio"
		INNER JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" CARHIS ON CAR."IdMovimientoCarteraEnTransito" = CARHIS."IdMovimientoCarteraEnTransito"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
		WHERE
		CAR."FechaRespaldo" = :FechaHasta
		AND	CAR."IdClasificacionTipoInstrumento" IN ('RV','RF','IF','FM','DV')
		AND CAR."IdEstado" = 'PEN'
		AND CAR."IdEmpresa" = :IdEmpresa
		AND CAR."FechaOperacion" <= :FechaHasta
		AND CAR."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF')
		AND (:IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :IdProductoAgrupador)
		AND P."IdTrabajo" = :IdTrabajo
	)
	SELECT 
	CUS."IdCliente"
   ,CUS."IdPortafolio"
   ,CASE
   		WHEN CTINS."IdClasificacionTipoInstrumento" = 'RV' AND TINS."IdTipoInstrumento" = 'SIM' AND :LocalizacionSistema = 'PE' THEN TINS."IdTipoInstrumento"
   		WHEN CTINS."IdClasificacionTipoInstrumento" = 'RV' THEN CTINS."IdClasificacionTipoInstrumento"
		WHEN CTINS."IdClasificacionTipoInstrumento" = 'RF' AND TINS."IdTipoInstrumento" IN ('PRT','EMP') THEN TINS."IdTipoInstrumento"
		WHEN CTINS."IdClasificacionTipoInstrumento" = 'RF' AND TINS."IdTipoInstrumento" NOT IN ('PRT','EMP') THEN CTINS."IdClasificacionTipoInstrumento"
   		WHEN CTINS."IdClasificacionTipoInstrumento" = 'IF' AND :LocalizacionSistema = 'CL' THEN CTINS."IdClasificacionTipoInstrumento"
   		WHEN CTINS."IdClasificacionTipoInstrumento" = 'FM' AND :LocalizacionSistema = 'CL' THEN CTINS."IdClasificacionTipoInstrumento"
   		WHEN CTINS."IdClasificacionTipoInstrumento" = 'DV' AND TINS."IdTipoInstrumento" = 'FWD' THEN TINS."IdTipoInstrumento"
   		WHEN CTINS."IdClasificacionTipoInstrumento" = 'DV' THEN CTINS."IdClasificacionTipoInstrumento"
	END AS "Grupo"
   ,CASE 
	   WHEN CTINS."IdClasificacionTipoInstrumento" IN ('RF', 'IF') AND CUS."IdTipoInstrumento" IN ('PAC') THEN
	   		CASE WHEN CUS."IdTipoOperacion" = 'VENPAC' THEN 'Pacto Compra' ELSE 'Pacto Venta'
	   		END 
	  WHEN CUS."NombreTipoInstrumento" = '' THEN TINS."Nombre" ELSE CUS."NombreTipoInstrumento" END as "Item"
   ,CUS."IdTipoInstrumento"
   ,CUS."IdMonedaOperacion"
   ,CASE WHEN CUS."IdTipoInstrumento" IN ('VC','PT') THEN SUM("MontoMercadoOperacion"*-1) ELSE SUM("MontoMercadoOperacion") END AS "MontoMonedaOperacion"
	FROM (
	SELECT
		"TipoCus"
	   ,"IdCliente"
	   ,"IdPortafolio"
	   ,"IdTipoOperacion"
	   ,"IdClasificacionTipoInstrumento"
	   ,"IdTipoInstrumento"
	   ,'' AS "NombreTipoInstrumento"
	   ,"IdInstrumento"
	   ,"IdMonedaOperacion"
	   ,"MontoCompraOperacion"
	   ,"MontoMercadoOperacion"
	FROM Cartera
	UNION ALL
	SELECT
		"TipoCus"
	   ,"IdCliente"
	   ,"IdPortafolio"
	   ,"IdTipoOperacion"
	   ,"IdClasificacionTipoInstrumento"
	   ,"IdTipoInstrumento"
	   ,'' AS "NombreTipoInstrumento"
	   ,"IdInstrumento"
	   ,"IdMonedaOperacion"
	   ,"MontoCompraOperacion"
	   ,"MontoMercadoOperacion"
	FROM CarteraNegativa
	UNION ALL 
	SELECT
		"TipoCus"
	   ,"IdCliente"
	   ,"IdPortafolio"
	   ,"IdTipoOperacion"
	   ,"IdClasificacionTipoInstrumento"
	   ,"IdTipoInstrumento"
	   ,'' AS "NombreTipoInstrumento"
	   ,"IdInstrumento"
	   ,"IdMonedaOperacion"
	   ,"MontoCompraOperacion"
	   ,"MontoMercadoOperacion"
	FROM Garantia
	UNION ALL 
	SELECT
		"TipoCus"
	   ,"IdCliente"
	   ,"IdPortafolio"
	   ,"IdTipoOperacion"
	   ,"IdClasificacionTipoInstrumento"
	   ,"IdTipoInstrumento"
	   ,"NombreTipoInstrumento" AS "NombreTipoInstrumento"
	   ,"IdInstrumento"
	   ,"IdMonedaOperacion"
	   ,"MontoCompraOperacion"
	   ,"MontoMercadoOperacion"
	FROM Subyacente
	UNION ALL 
	SELECT
		"TipoCus"
	   ,"IdCliente"
	   ,"IdPortafolio"
	   ,"IdTipoOperacion"
	   ,"IdClasificacionTipoInstrumento"
	   ,"IdTipoInstrumento"
	   ,'' AS "NombreTipoInstrumento"
	   ,"IdInstrumento"
	   ,"IdMonedaOperacion"
	   ,"MontoCompraOperacion"
	   ,"MontoMercadoOperacion"
	FROM Transito
	) AS CUS
	INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = CUS."IdInstrumento"
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TINS ON TINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
    INNER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS CTINS ON CTINS."IdClasificacionTipoInstrumento" = TINS."IdClasificacionTipoInstrumento"
	GROUP BY
		CUS."IdCliente"
	   ,CUS."IdPortafolio"
	   ,CTINS."IdClasificacionTipoInstrumento"
	   ,CUS."IdTipoInstrumento"
	   ,TINS."IdTipoInstrumento"
	   ,CASE WHEN CUS."NombreTipoInstrumento" = '' THEN TINS."Nombre" ELSE CUS."NombreTipoInstrumento" END
	   ,TINS."Nombre"
	   ,CUS."IdMonedaOperacion"
	   ,CUS."IdTipoOperacion"
	   ,CUS."NombreTipoInstrumento";