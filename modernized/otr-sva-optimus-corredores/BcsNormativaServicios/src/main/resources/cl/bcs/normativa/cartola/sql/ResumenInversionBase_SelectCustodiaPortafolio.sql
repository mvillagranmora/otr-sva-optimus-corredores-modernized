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
		   		  WHEN CAR."IdClasificacionTipoInstrumento" IN ('RV') THEN "INGCantidad" * COALESCE(Precio."PrecioCierre", "INGPrecioMercadoActual")
		   		  ELSE "INGCantidad" * "INGPrecioMercadoActual" END) AS "MontoMercadoOperacion"
		FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
		INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON CAR."IdPortafolio" = P."IdPortafolio"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
		LEFT JOIN "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio"  AS Precio ON CAR."IdInstrumento" = Precio."IdInstrumento" AND Precio."Fecha" = :FechaHasta AND CAR."IdBolsa" = Precio."IdBolsa"
		WHERE
		CAR."IdClasificacionTipoInstrumento" IN ('RF','IF','FM'/*,'DV'*/)
		AND CAR."IdTipoInstrumento" NOT IN ('FDO','FIP','FBC','FIC','FUF','IOA','IOE','IOI','IOD','IOB','IOC','IOU','IOM','IOF','FBO','DER')
		AND CAR."IdEstado" = 'VIG'
		AND CAR."IdEmpresa" = :IdEmpresa
		AND CAR."IdTipoOperacion" != 'VENPAC'
		AND CAR."INGFechaOperacion" <= (CASE WHEN CAR."IdTipoInstrumento" = 'FWD' THEN CAR."INGFechaLiquidacion" ELSE :FechaHasta END)
		AND CAR."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF')
		AND (:IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :IdProductoAgrupador)
		AND P."IdTrabajo" = :IdTrabajo

	UNION ALL 

		SELECT 
			'Cartera' AS "TipoCus"
			,CAR."IdCliente"										AS "IdCliente"
			,CAR."IdPortafolio"										AS "IdPortafolio"
			,'' AS "IdTipoOperacion"
			,'DV'													AS "IdClasificacionTipoInstrumento"			
			,GT."IdGrupoProducto"									AS "IdTipoInstrumento"			
			,CAR."IdInstrumento"									AS "IdInstrumento"
			,CAR."IdMonedaInstrumento"								AS "IdMonedaOperacion"	
			,MOV."Monto"											AS "MontoCompraOperacion"
			,MOV."Monto"											AS "MontoMercadoOperacion"		
		FROM 	  "BSCL.Custodia.BD::CUS.Cartera" 					AS CAR
		INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON CAR."IdPortafolio" = P."IdPortafolio"
		INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCartolaDeMovimientos" AS MOV
		ON (MOV."IdOperacion" = CAR."IdCartera")
		INNER JOIN "BSCL.Plataforma.BD::INS.GrupoTipoInstrumento"	AS GT ON (GT."IdTipoInstrumento" = CAR."IdTipoInstrumento")
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" 								AS Portafolio 
		ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" 						AS MatrizAgrupador 
		ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV" 
		WHERE 
			CAR."IdEstado" = 'VIG'
		AND CAR."IdClasificacionTipoInstrumento" = 'DV' 
		AND (CAR."FechaOperacion"	 <=:FechaHasta)
		AND MOV."IdEstado" = 'CRT'
		AND GT."IdGrupoProducto" IN ('FU', 'OP')
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
		FROM "BSCL.Custodia.BD::CUS.CarteraNegativa" CAR
		INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON CAR."IdPortafolio" = P."IdPortafolio"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS	ON INS."IdInstrumento" = CAR."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoInstrumento ON TipoInstrumento."IdTipoInstrumento" = INS."IdTipoInstrumento" 
		LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" as ClasInst ON ClasInst."IdClasificacionTipoInstrumento" = TipoInstrumento."IdClasificacionTipoInstrumento"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
		LEFT JOIN "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio"  AS Precio ON CAR."IdInstrumento" = Precio."IdInstrumento" AND Precio."Fecha" = :FechaHasta AND CAR."IdBolsa" = Precio."IdBolsa"
		WHERE
			ClasInst."IdClasificacionTipoInstrumento" IN ('RF', 'RV', 'IF')
		AND CAR."IdEstado" = 'VIG'
		AND CAR."IdEmpresa" = :IdEmpresa
		AND CAR."IdTipoOperacion" != 'VENPAC'
		AND CAR."FechaOperacion" <= ( :FechaHasta )
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
		FROM "BSCL.Custodia.BD::GTA.Garantia" GAR
		INNER JOIN
		(
			SELECT
				RANK() over (partition by CAR."FolioCartera" order by CAR."IdCartera" desc) as RANK
			   ,CAR."FolioCartera"
			   ,CAR."IdCliente"
			   ,CAR."IdPortafolio"
			   ,CAR."IdTipoOperacion"
			FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
			INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON CAR."IdPortafolio" = P."IdPortafolio"
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
			WHERE
			CAR."IdEmpresa" = :IdEmpresa
			AND (:IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :IdProductoAgrupador)
			AND P."IdTrabajo" = :IdTrabajo
		) AS CAR ON CAR."FolioCartera" = GAR."FolioCarteraOrigen"
		WHERE
			GAR."IdClasificacionTipoInstrumento" IN ('RV','RF','IF','FM','DV')
		AND GAR."IdEstado" = 'VIG'
		AND GAR."INGFechaOperacion" <= :FechaHasta
		AND CAR."IdTipoOperacion" != 'VENPAC'
		AND CAR.RANK = 1
	),
	Subyacente AS
	(
		(SELECT
	 'Subyacente' AS "TipoCus",
		PRE."IdCliente",
		PRE."IdPortafolio",
		'' AS "IdTipoOperacion",
		"IdClasificacionTipoInstrumentoSubyacente" as "IdClasificacionTipoInstrumento",
		CASE WHEN PRE."IdTipoPrestamo"  = 'PRTVTC' THEN 'VC' ELSE 'PT' END  AS "IdTipoInstrumento",
		"IdInstrumentoSubyacente" as "IdInstrumento",
		CASE WHEN PRE."IdTipoPrestamo"  = 'PRTVTC' THEN 'VENTA CORTA' ELSE 'PRESTAMOS' END  AS "NombreTipoInstrumento", 
		CASE WHEN PRE."IdTipoReajuste" <> '' THEN Pre."IdTipoReajuste" ELSE Pre."IdMonedaInstrumento" END AS "IdMonedaOperacion",
		"INGMontoInstrumento" as "MontoCompraOperacion",
		CASE WHEN COALESCE (RPBI."Precio",-1) > 0 THEN 
    		RPBI."Precio" * PRE."Cantidad"
    	ELSE
        CASE  
            WHEN COALESCE (INS."Precio", -1) > 0 THEN INS."Precio" * PRE."Cantidad"
        ELSE COALESCE (PRE."INGPrecioMercadoInstrumento", 0) * PRE."Cantidad" END
		END AS "MontoMercadoOperacion"
		 FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente" PRE
		INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON PRE."IdPortafolio" = P."IdPortafolio"
		LEFT JOIN "BSCL.Plataforma.BD::INS.RegistroPrecioBolsaInstrumento" AS RPBI ON PRE."IdInstrumentoSubyacente" = RPBI."IdInstrumento" AND RPBI."FechaSistema" = :FechaHasta
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON INS."IdInstrumento" = PRE."IdInstrumentoSubyacente"
		WHERE PRE."IdEmpresa" = :IdEmpresa 
		AND P."IdTrabajo" = :IdTrabajo
		AND "FechaPrestamo" <= :FechaHasta 
		AND "FechaVencimientoPrestamo" >= :FechaHasta 
		AND (("EGRCantidadPrestamo" = 0
		AND PRE."IdEstado" = 'VIG'
		AND "INGFechaOperacion" <= :FechaHasta )
		OR ("INGFechaOperacion" >= :FechaHasta and "EGRCantidadPrestamo" <> 0 and "EGRFechaOperacion" >  :FechaHasta )OR PRE."IdEstado" = 'ANT')
		AND "IdClasificacionTipoInstrumentoSubyacente" <>  '')

		UNION ALL
		(SELECT 
		 'Subyacente' AS "TipoCus",
		EMP."IdCliente",
		EMP."IdPortafolio",
		'' AS "IdTipoOperacion",
		"IdClasificacionTipoInstrumentoSubyacente" as "IdClasificacionTipoInstrumento",
		'EM' AS "IdTipoInstrumento",
		"IdInstrumentoSubyacente" as "IdInstrumento",
		'EMPRESTITOS' AS "NombreTipoInstrumento", 
		CASE WHEN EMP."IdTipoReajuste" <> '' THEN EMP."IdTipoReajuste" ELSE EMP."IdMonedaInstrumento"  END AS "IdMonedaOperacion",
		"INGMontoInstrumento" as "MontoCompraOperacion",
		CASE WHEN COALESCE (RPBI."Precio",-1) > 0 THEN 
    		RPBI."Precio" * EMP."Cantidad"
    	ELSE
        CASE  
            WHEN COALESCE (INS."Precio", -1) > 0 THEN INS."Precio" * EMP."Cantidad"
        ELSE COALESCE (EMP."INGPrecioMercadoInstrumento", 0) * EMP."Cantidad" END
		END AS "MontoMercadoOperacion"
		FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenEmprestitoVigente" EMP
		INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON EMP."IdPortafolio" = P."IdPortafolio"
		LEFT JOIN "BSCL.Plataforma.BD::INS.RegistroPrecioBolsaInstrumento" AS RPBI ON EMP."IdInstrumentoSubyacente" = RPBI."IdInstrumento" AND RPBI."FechaSistema" = :FechaHasta
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON INS."IdInstrumento" = EMP."IdInstrumentoSubyacente" 
		WHERE EMP."IdEmpresa" = :IdEmpresa 
		AND "FechaEmprestito" <= :FechaHasta
		AND "FechaVencimientoEmprestito" >= :FechaHasta
		AND P."IdTrabajo" = :IdTrabajo
		AND EMP."IdEstado" = 'VIG'
		AND "IdClasificacionTipoInstrumentoSubyacente" <>  '')
		UNION ALL 	
		(
		SELECT
		 'Subyacente' AS "TipoCus"
		   ,CAR."IdCliente"
		   ,CAR."IdPortafolio"
		   ,'' AS "IdTipoOperacion"
		   ,SUB."IdClasificacionTipoInstrumento"
		   ,INS."IdTipoInstrumento"
		   ,SUB."IdInstrumento"
		  ,INS."NombreTipoInstrumento" AS "NombreTipoInstrumento"
		   --------------------------------------------------------------------------------------------
		   ,CASE 
			   	WHEN SUB."IdTipoReajuste" <> '' THEN 
			   		SUB."IdTipoReajuste" 
			   	ELSE SUB."IdMonedaInstrumento" 
			END AS "IdMonedaOperacion"
		   ,CASE 
            WHEN SUB."INGMontoSubyacenteActualMonedaInstrumento" > 0 THEN 
                CASE WHEN CAR."IdTipoOperacion" = 'VENSIM' 
                     THEN -1 * SUB."INGMontoSubyacenteActualMonedaInstrumento" 
                     ELSE SUB."INGMontoSubyacenteActualMonedaInstrumento" 
                END
            ELSE
                CASE WHEN CAR."IdTipoOperacion" = 'VENSIM' 
                     THEN -1 * SUB."INGMontoMercadoActualMonedaInstrumento" 
                     ELSE SUB."INGMontoMercadoActualMonedaInstrumento" 
                END
         	END AS "MontoCompraOperacion"
         	,CASE 
            WHEN CAR."IdTipoOperacion" = 'VENSIM' 
                THEN -1 * (CAR."INGCantidad" * COALESCE(MovComprom."Precio",0))
            ELSE 
                CAR."INGCantidad" * COALESCE(MovComprom."Precio",0)
         	END AS "MontoMercadoOperacion"
		FROM "BSCL.Custodia.BD::CUS.Subyacente" SUB
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
		INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON CAR."IdPortafolio" = P."IdPortafolio"
		INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" 	AS ULTSUB 	
		ON (ULTSUB."FolioCarteraRelacionada" 	= CAR."FolioCartera"  AND SUB."IdSubyacente"	= ULTSUB."IdSubyacente")
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS ON INS."IdInstrumento" = CAR."IdInstrumento"
		LEFT JOIN (SELECT DISTINCT MovNormal."IdEmpresa", MovNormal."IdComprobante", MovNormal."NemotecnicoSubyacente", MovPlazo."Precio" FROM "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteSimple" MovNormal
			LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteSimple" as MovPlazo on 
		 	MovPlazo."IdMovimientoComprobante" = MovNormal."IdMovimientoRelacionado"
		 )as MovComprom ON
			MovComprom."IdEmpresa" = CAR."IdEmpresa" and 
		 	MovComprom."IdComprobante" = CAR."FolioComprobante" and 
		 	MovComprom."NemotecnicoSubyacente" = SUB."Nemotecnico"
		WHERE
		CAR."IdEmpresa" = :IdEmpresa
		AND CAR."IdTipoInstrumento" = 'SIM'
		AND (:IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :IdProductoAgrupador)
		AND P."IdTrabajo" = :IdTrabajo
		AND	SUB."IdClasificacionTipoInstrumento" IN ('RV','FM','DV')
		AND CAR."IdTipoOperacion" NOT IN ('COMRRV','COMRRF')
		AND SUB."IdEstado" = 'VIG'
		AND CAR."IdEstado" = 'VIG'
		AND CAR."INGFechaOperacion" <= :FechaHasta
		
		UNION ALL
		
		SELECT
		 'Subyacente' AS "TipoCus"
        ,CAR."IdCliente"
        ,CAR."IdPortafolio"
        ,CAR."IdTipoOperacion"
        ,SUB."IdClasificacionTipoInstrumento"
		,INS."IdTipoInstrumento"
		,SUB."IdInstrumento"
		,INS."NombreTipoInstrumento" AS "NombreTipoInstrumento"
		   --------------------------------------------------------------------------------------------
		,CAR."IdTipoReajuste" AS "IdMonedaOperacion"
		,CASE 
            WHEN SUB."INGMontoSubyacenteActualMonedaInstrumento" > 0 THEN 
                CASE WHEN CAR."IdTipoOperacion" = 'COMPAC' 
                     THEN -1 * SUB."INGMontoSubyacenteActualMonedaInstrumento" 
                     ELSE SUB."INGMontoSubyacenteActualMonedaInstrumento" 
                END
            ELSE
                CASE WHEN CAR."IdTipoOperacion" = 'COMPAC' 
                     THEN -1 * SUB."INGMontoMercadoActualMonedaInstrumento" 
                     ELSE SUB."INGMontoMercadoActualMonedaInstrumento" 
                END
         END AS "MontoCompraOperacion"
		 ,CASE 
            WHEN CAR."MontoAPlazo" > 0 THEN
                CASE 
                    WHEN CAR."IdTipoOperacion" = 'COMPAC' 
                        THEN -1 * COALESCE(RES."INGMontoAPlazoMonedaInstrumento",0)
                    WHEN CAR."IdTipoOperacion" = 'VENPAC' 
                        THEN COALESCE(RES."INGMontoAPlazoMonedaInstrumento",0)
                    ELSE 0
                END
            ELSE 
                CASE 
                    WHEN CAR."IdTipoOperacion" = 'COMPAC' 
                        THEN -1 * SUB."INGMontoSubyacenteActualMonedaInstrumento"
                    ELSE SUB."INGMontoSubyacenteActualMonedaInstrumento"
                END
         END AS "MontoMercadoOperacion"
		FROM "BSCL.Custodia.BD::CUS.Subyacente" SUB
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
		INNER JOIN "BSCL.Custodia.BD::CUS.Reserva" RES ON RES."IdOperacion" = CAR."IdOperacion" AND RES."FolioSubyacente" = SUB."FolioSubyacente"
		INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON CAR."IdPortafolio" = P."IdPortafolio"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS ON INS."IdInstrumento" = CAR."IdInstrumento"
		LEFT JOIN (SELECT DISTINCT MovNormal."IdEmpresa", MovNormal."IdComprobante", MovNormal."NemotecnicoSubyacente", MovPlazo."Precio" FROM "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteSimple" MovNormal
			LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteSimple" as MovPlazo on 
		 	MovPlazo."IdMovimientoComprobante" = MovNormal."IdMovimientoRelacionado"
		 )as MovComprom ON
			MovComprom."IdEmpresa" = CAR."IdEmpresa" and 
		 	MovComprom."IdComprobante" = CAR."FolioComprobante" and 
		 	MovComprom."NemotecnicoSubyacente" = SUB."Nemotecnico"
		WHERE
		CAR."IdEmpresa" = :IdEmpresa
		AND CAR."IdTipoInstrumento" IN ('PAC')
		AND (:IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :IdProductoAgrupador)
		AND P."IdTrabajo" = :IdTrabajo
		AND	SUB."IdClasificacionTipoInstrumento" IN ('RF','IF')
		AND CAR."IdTipoOperacion" NOT IN ('COMRRF')
		AND SUB."IdEstado" = 'VIG'
		AND CAR."IdEstado" = 'VIG'
		AND CAR."INGFechaOperacion" <= :FechaHasta	
		)
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
		FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" CAR
		INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON CAR."IdPortafolio" = P."IdPortafolio"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
		WHERE
		CAR."IdClasificacionTipoInstrumento" IN ('RV','RF','IF','FM','DV')
		AND CAR."IdEstado" = 'PEN'
		AND CAR."IdEmpresa" = :IdEmpresa
		AND CAR."FechaOperacion" <= :FechaHasta
		AND CAR."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF')
		--AND CAR."EsVentaDescubierta" = 'S'
		AND (:IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :IdProductoAgrupador)
		AND P."IdTrabajo" = :IdTrabajo
	)
	SELECT 
	CUS."IdCliente"
   ,CUS."IdPortafolio"
   ,TINS."IdTipoInstrumento"
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
	FROM (SELECT
		"TipoCus"
	   ,"IdCliente"
	   ,"IdPortafolio"
	   ,"IdTipoOperacion"
	   ,"IdClasificacionTipoInstrumento"
	   ,"IdTipoInstrumento"
       ,CASE WHEN "IdTipoInstrumento" = 'FU'	THEN 'FUTUROS'  ELSE '' END AS "NombreTipoInstrumento"
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
       ,CASE WHEN "IdTipoInstrumento" = 'FU'	THEN 'FUTUROS'  ELSE '' END AS "NombreTipoInstrumento"
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
	FROM Transito) AS CUS
	INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = CUS."IdInstrumento"
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TINS ON TINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
    INNER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS CTINS ON CTINS."IdClasificacionTipoInstrumento" = TINS."IdClasificacionTipoInstrumento"
	GROUP BY
		CUS."IdCliente"
	   ,CUS."IdPortafolio"
	   ,CUS."IdTipoOperacion"
	   ,CTINS."IdClasificacionTipoInstrumento"
	   ,CUS."IdTipoInstrumento"
	   ,TINS."IdTipoInstrumento"
	   ,CASE WHEN CUS."NombreTipoInstrumento" = '' THEN TINS."Nombre" ELSE CUS."NombreTipoInstrumento" END
	   ,TINS."Nombre"
	   ,CUS."IdMonedaOperacion"
	   ,CUS."NombreTipoInstrumento"
	   ,CTINS."Nombre";