WITH CodigoPortafolioActivo AS (
	SELECT "DocumentoIdentidad", "IdPersona","IdCliente", "CodigoPortafolio", "IdPortafolio","NombreCompleto"
	    FROM (
	        SELECT "DocumentoIdentidad","IdCliente", "IdPersona", "CodigoPortafolio", "IdPortafolio","NombreCompleto",
	               ROW_NUMBER() OVER (PARTITION BY "DocumentoIdentidad", "IdPersona" ORDER BY "CodigoPortafolio" ASC) AS rn
	        FROM "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio"
	    ) 
    WHERE rn = 1
), ultimoRespaldo AS (
	SELECT 
		MAX(Cartera."FechaRespaldo") AS "FechaUltimoRespaldo"
	FROM 
		"BSCL.Custodia.BD::HIS.Cartera" AS Cartera
	WHERE 
		 Cartera."FechaRespaldo" BETWEEN ADD_DAYS(:FechaCorteCuponDesde, -7) AND ADD_DAYS(:FechaCorteCuponDesde, -1)
), CuponesCarteraHistorica AS (
	SELECT 
		CAR."FolioCartera", 
		CAR."IdEstado", 
		COALESCE(TO_VARCHAR(CAR."FechaRespaldo", 'YYYY-MM-DD'), '0001-01-01') AS "FechaUltimoRespaldo",
		CAR."Nemotecnico"
	FROM 
		"BSCL.Custodia.BD::HIS.Cartera" AS CAR
	INNER JOIN ultimoRespaldo UR ON  CAR."FechaRespaldo" = UR."FechaUltimoRespaldo"  AND CAR."IdEstado" = 'VIG'
	WHERE 
		CAR."IdClasificacionTipoInstrumento"   = 'RF'
		AND   CAR."INGFechaPagoCorteCupon"			BETWEEN :FechaCorteCuponDesde AND :FechaCorteCuponHasta	
		AND   CAR."FechaLiquidacion" 				<= :FechaCorteCuponDesde
		AND   CAR."IdEstado" 						= 'VIG'
		AND	  CAR."INGFechaCorteCupon"				<= CAR."INGFechaPagoCorteCupon"
		AND   CAR."IdInstrumento" IN (CASE WHEN (COALESCE(:IdInstrumento,'') = '') THEN CAR."IdInstrumento" ELSE :IdInstrumento END)
		AND   CAR."FechaRespaldo"					 < CAR."INGFechaCorteCupon"
		AND (CASE
				WHEN CAR."IdTipoInstrumento" = 'LTH' AND LOCATE(CAR."Nemotecnico", '*') > 0 THEN 'CAMBIO_NEMO' 
				WHEN CAR."IdTipoInstrumento" = 'LTH' AND LOCATE(CAR."Nemotecnico", '&') > 0 THEN 'CAMBIO_NEMO'
				ELSE 'CORTE_CUPON'
			END) = 'CORTE_CUPON' 
		AND   CAR."INGFechaPagoCorteCupon" BETWEEN :FechaCorteCuponDesde AND :FechaCorteCuponHasta		
)
 SELECT 
		'CC'															AS "IdTipoEventoCapital"
		,'N'															AS "EsCarteraNegativa"
		,CA."INGNumeroCupon"											AS "Secuencia"	
		,TO_BIGINT(0)													AS "IdSocioNegocio"
		,'CNT'			   												AS "TipoSocioNegocio"
		,COALESCE(CA."IdPortafolio",0)  								AS "IdPortafolio"
		,'R'															AS "IdTipoDocumentoIdentidad"
		,CASE 
		   WHEN :p_NombreEmisor = 'S' THEN IFNULL(Emisor."DocumentoIdentidad",'')
		 ELSE BO."DocumentoIdentidad"
		 END AS "DocumentoIdentidad"
		,CASE 
		   WHEN :p_NombreEmisor = 'S' THEN IFNULL(Emisor."NombreCompleto",'')
		 ELSE IFNULL(PE."RazonSocial",'')
		 END AS "NombreCliente"
		,CA."IdClasificacionTipoInstrumento"							AS "IdClasificacionTipoInstrumento"
		,CA."IdTipoInstrumento"											AS "IdTipoInstrumento"
		,CA."IdInstrumento"												AS "IdInstrumento"
		,CA."Nemotecnico"												AS "Nemo"
		,TO_BIGINT(0)													AS "IdInstrumentoNuevo"
		,''																AS "NemoNuevo"
		,CA."INGFechaPagoCorteCupon"									AS "FechaCierre"
		,CA."INGFechaPagoCorteCupon"									AS "FechaPago"
		,CASE WHEN CA."IdEstado" = 'VIG' 
				THEN SUM("Saldo") * -1 
				ELSE SUM("Cantidad") * -1								END AS "CantidadDerecho"
		,'CUS'															AS "TipoCartera"
		,TO_DECIMAL(0,26,8)												AS "CantidadNueva"
		--AMORTIZACION
		,CASE WHEN CA."IdTipoReajuste" != 'CLP'  AND "INGFechaPagoCorteCupon" != "INGFechaCorteCupon"
			THEN CASE WHEN CA."INGIdMonedaPagoCupon" = 'CLP' 
					THEN SUM("INGValorFactorMonedaInstrumentoProximo" * (CA."INGMontoCapitalCorteCupon"/"INGValorFactorMonedaInstrumentoActual")) * -1
					ELSE SUM("INGMontoCapitalCorteCuponMonedaInstrumento") * -1
				 END
		ELSE 
				CASE WHEN CA."INGIdMonedaPagoCupon" = 'CLP'
					THEN SUM(CA."INGMontoCapitalCorteCupon") * -1
					ELSE SUM(CA."INGMontoCapitalCorteCuponMonedaInstrumento") * -1
				END
		END	AS "MontoCapital" --AMORTIZACION
		--INTERES
		,CASE WHEN CA."IdTipoReajuste" != 'CLP'  AND "INGFechaPagoCorteCupon" != "INGFechaCorteCupon"
			THEN CASE WHEN CA."INGIdMonedaPagoCupon" = 'CLP' 
				THEN SUM("INGValorFactorMonedaInstrumentoProximo" * ("INGInteresesPagoCorteCupon"/"INGValorFactorMonedaInstrumentoActual")) * -1
				ELSE SUM("INGMontoInteresCorteCuponMonedaInstrumento") * -1
			END
		ELSE 
			CASE WHEN CA."INGIdMonedaPagoCupon" = 'CLP' 
				THEN SUM(CA."INGInteresesPagoCorteCupon") * -1
				ELSE SUM(CA."INGMontoInteresCorteCuponMonedaInstrumento") * -1
			END
		END	AS "MontoInteres" --INTERES
		--TOTAL
		,CASE WHEN CA."IdTipoReajuste" != 'CLP'  AND "INGFechaPagoCorteCupon" != "INGFechaCorteCupon"
			THEN CASE WHEN CA."INGIdMonedaPagoCupon" = 'CLP'
				THEN SUM("INGValorFactorMonedaInstrumentoProximo" * ("INGMontoCorteCupon"/"INGValorFactorMonedaInstrumentoActual")) *-1
				ELSE SUM("INGMontoCorteCuponMonedaInstrumento") *-1
			END
		 ELSE
		 	CASE WHEN CA."INGIdMonedaPagoCupon" = 'CLP'
		 		THEN SUM(CA."INGMontoCorteCupon") *-1
		 		ELSE SUM(CA."INGMontoCorteCuponMonedaInstrumento") *-1
		 	END
		END	AS "Total"
		,CA."INGIdMonedaPagoCupon"										AS "IdMoneda"
		,CA."INGNumeroCupon"											AS "SecuenciaCupon"	
		,TO_BIGINT(0)													AS "IdEmpleadoEjecutivoPortafolio"
		,''																AS "CodigoUsuarioAgente"	
		,''																AS "NombreAgente"
		,''																AS "CodigoSucursal"
		,CURRENT_UTCTIMESTAMP											AS "FechaHoraIngresoRegistro"
		,CURRENT_UTCTIMESTAMP											AS "FechaHoraUltimaActualizacion"		
		,'VIG'															AS "IdEstado"
		,CA."IdBoveda"													AS "IdBoveda"
		,CA."IdBolsa"													AS "IdBolsa"
		,''																AS "IdConceptoGarantia"
		,''																AS "IdTipoOperacionGarantia"
		,SUM(CA."INGMontoReajusteCorteCupon")							AS "MontoReajuste"
		,'SIS'															AS "Origen"
		,CA."IdTipoReajuste"											AS "IdTipoReajuste"
		,TO_DECIMAL(0,26,8)												AS "INGMontoInteresCorteCupon"  
		,''	AS "TipoOperacion"
		,CA."FechaLiquidacion"
		,CA."INGValorFactorMonedaInstrumentoProximo"
		,CA."INGFechaCorteCupon"
		,CA."IdTipoOperacion"
		,CA."INGFechaFactorMonedaAdicionalProximo"
		,CA."INGValorFactorMonedaInstrumentoActual"
		,CASE WHEN CA."IdTipoReajuste" != 'CLP'  AND "INGFechaPagoCorteCupon" != "INGFechaCorteCupon" THEN 'S' 
			ELSE 'N' END AS "UsaProximoFactorValorMoneda"
		,CA."INGIdTipoOperacion"
		,CA."IdCartera"
		,'CARTERA' as "QueryOrigen"
 FROM (
		SELECT 
		  		 CAR."IdEmpresa"
		  		,CAR."IdCartera"
				,CAR."INGNumeroCupon"	
				,CAR."IdClienteRelacionado"
				,CAR."IdTipoOperacion"
				,CAR."IdPortafolioRelacionado"
		  		,CPA."IdPortafolio"
		  		,CAR."IdCliente"
		  		,CAR."IdInstrumento"
		  		,CAR."IdTipoInstrumento"
		  		,CAR."INGFechaPagoCorteCupon"
		  		,CAR."IdClasificacionTipoInstrumento"
		  		,CAR."Nemotecnico"
		  		,CAR."IdEstado"
		  				  		,CASE WHEN (CAR."IdTipoOperacion" IN ('VENPAC','COMPAC') AND CAR."IdEstado" <> 'VIG') THEN 0
		  			ELSE (CAR."INGCantidad" - CAR."EGRCantidad") END as "Saldo"
		  		,CASE 
			  		WHEN (CAR."IdTipoOperacion" IN ('VENPAC','COMPAC') AND CARVCD."EGRIdTipoOperacion" = 'VCDTIT') THEN CARVCD."INGCantidad"
			  		WHEN (CAR."IdTipoOperacion" IN ('VENPAC','COMPAC') AND CAR."IdEstado" <> 'VIG') THEN 0 
			  		ELSE CAR."INGCantidad" END AS "Cantidad"
		  		,CASE 
			  		WHEN (CAR."IdTipoOperacion" IN ('VENPAC','COMPAC') AND CARVCD."EGRIdTipoOperacion" = 'VCDTIT' AND CAR."INGCantidad" > 0)
			  			THEN 
			  			CASE WHEN CAR."INGIdMonedaPagoCupon" = 'CLP'
			  				THEN ROUND((CAR."INGMontoCapitalCorteCupon" / CAR."INGCantidad") * CARVCD."INGCantidad")
			  				ELSE ROUND((CAR."INGMontoCapitalCorteCuponMonedaInstrumento" / CAR."INGCantidad") * CARVCD."INGCantidad")
			  			END 
			  		WHEN (CAR."IdTipoOperacion" IN ('VENPAC','COMPAC') AND CAR."IdEstado" <> 'VIG') 
			  			THEN 0
				  		ELSE CASE WHEN CAR."INGIdMonedaPagoCupon" = 'CLP' THEN CAR."INGMontoCapitalCorteCupon" ELSE CAR."INGMontoCapitalCorteCuponMonedaInstrumento" END 
				 END AS "INGMontoCapitalCorteCupon"
		  		,CASE 
			  		WHEN (CAR."IdTipoOperacion" IN ('VENPAC','COMPAC') AND CARVCD."EGRIdTipoOperacion" = 'VCDTIT' AND CAR."INGCantidad" > 0)
			  			THEN 
			  			CASE WHEN CAR."INGIdMonedaPagoCupon" = 'CLP'
			  				THEN ROUND((CAR."INGInteresesPagoCorteCupon" / CAR."INGCantidad") * CARVCD."INGCantidad")
			  				ELSE ROUND((CAR."INGMontoInteresCorteCuponMonedaInstrumento" / CAR."INGCantidad") * CARVCD."INGCantidad")
			  			END 
			  		WHEN (CAR."IdTipoOperacion" IN ('VENPAC','COMPAC') AND CAR."IdEstado" <> 'VIG')
			  			THEN 0
			  			ELSE CASE WHEN CAR."INGIdMonedaPagoCupon" = 'CLP' THEN CAR."INGInteresesPagoCorteCupon" ELSE CAR."INGMontoInteresCorteCuponMonedaInstrumento" END 
			  	 END AS "INGInteresesPagoCorteCupon"
		  		,CASE 
			  		WHEN (CAR."IdTipoOperacion" IN ('VENPAC','COMPAC') AND CARVCD."EGRIdTipoOperacion" = 'VCDTIT' AND CAR."INGCantidad" > 0)
			  			THEN 
			  			CASE WHEN  CAR."INGIdMonedaPagoCupon" = 'CLP'
			  				THEN ROUND((CAR."INGMontoCorteCupon" / CAR."INGCantidad") * CARVCD."INGCantidad")
			  				ELSE ROUND((CAR."INGMontoCorteCuponMonedaInstrumento" / CAR."INGCantidad") * CARVCD."INGCantidad") 
			  			END 
			  		WHEN (CAR."IdTipoOperacion" IN ('VENPAC','COMPAC') AND CAR."IdEstado" <> 'VIG')
			  			THEN 0
			  			ELSE CASE WHEN CAR."INGIdMonedaPagoCupon" = 'CLP' THEN CAR."INGMontoCorteCupon" ELSE CAR."INGMontoCorteCuponMonedaInstrumento" END 
			  	 END AS "INGMontoCorteCupon"
		  		,CAR."INGIdMonedaPagoCupon"
		  		,CAR."IdBoveda"
		  		,CAR."IdBolsa"
		  		,CAR."INGMontoReajusteCorteCupon"
		  		,CAR."INGMontoInteresCorteCupon"
		  		,CAR."INGMontoCapitalCorteCuponMonedaInstrumento"
		  		,CAR."INGMontoCorteCuponMonedaInstrumento"
		  		,CAR."IdTipoReajuste"
		  		,CAR."FechaLiquidacion"
				,CAR."INGFechaCorteCupon"
				,CAR."INGValorFactorMonedaInstrumentoProximo"
				,CAR."INGValorFactorMonedaInstrumentoActual"
				,CAR."INGFechaFactorMonedaAdicionalProximo"
				,CAR."INGIdTipoOperacion"
		  		,CAR."INGMontoInteresCorteCuponMonedaInstrumento"
		FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON CAR."IdInstrumento" = INS."IdInstrumento" 
		INNER JOIN "BSCL.Plataforma.BD::MER.Emisor"  				AS Emisor ON Emisor."IdEmisor" = INS."IdEmisor"
        LEFT JOIN CodigoPortafolioActivo CPA ON Emisor."IdPersona" = CPA."IdPersona"
		LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" CMBTE ON CMBTE."IdComprobante" = CAR."EGRFolioComprobante" AND CMBTE."IdCliente" = CAR."IdCliente" AND CAR."EGRFolioComprobante"!= 0
		LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CARVCD ON (
			CAR."FechaLiquidacion" = CARVCD."FechaLiquidacion"
			AND CAR."IdInstrumento" = CARVCD."IdInstrumento"
			AND CAR."IdPortafolioRelacionado" = CARVCD."IdPortafolio" 
			AND CAR."IdEstado" IN ('VIG','VCD') AND CAR."IdTipoOperacion" IN ('COMPAC','VENPAC') AND CARVCD."INGIdTipoOperacion" = 'VCDPAC'
		)
		WHERE CAR."IdClasificacionTipoInstrumento" = 'RF'
		AND (CAR."INGFechaPagoCorteCupon" BETWEEN :FechaCorteCuponDesde AND :FechaCorteCuponHasta)
		AND CAR."FechaComprobante" < CAR."INGFechaCorteCupon" 
		AND CAR."INGFechaOperacion" < CAR."INGFechaPagoCorteCupon"
		AND CAR."IdEstado" IN ('VCD','VIG')
		AND CAR."IdInstrumento" IN (CASE WHEN (COALESCE(:IdInstrumento,'') = '') THEN CAR."IdInstrumento" ELSE :IdInstrumento END)

		UNION ALL 

		SELECT --Ventas
		  		 CAR."IdEmpresa"
		  		,CAR."IdCartera"
				,CAR."INGNumeroCupon"	
				,CAR."IdClienteRelacionado"
				,CAR."IdTipoOperacion"
				,CAR."IdPortafolioRelacionado"
		  		,CAR."IdPortafolio"
		  		,CAR."IdCliente"
		  		,CAR."IdInstrumento"
		  		,CAR."IdTipoInstrumento"
		  		,CAR."INGFechaPagoCorteCupon"
		  		,CAR."IdClasificacionTipoInstrumento"
		  		,CAR."Nemotecnico"
		  		,'VIG' AS "IdEstado"
		  		,CAR."EGRCantidad" as "Saldo"
		  		,CAR."EGRCantidad" AS "Cantidad"
					-- Monto capital
				,CASE 
				    WHEN "IdEstado" = 'RPA' THEN 
				        CASE 
				            WHEN CAR."INGIdMonedaPagoCupon" = 'CLP' 
				                THEN CAST((("INGMontoCapitalCorteCupon" / CAR."INGCantidad") * "EGRCantidad") AS INT)
				                ELSE (CAR."INGMontoCapitalCorteCuponMonedaInstrumento" / CAR."INGCantidad") * "EGRCantidad"
				        END 
				    ELSE 
				        CASE 
				            WHEN CAR."INGIdMonedaPagoCupon" = 'CLP' 
				                THEN CAR."INGMontoCapitalCorteCupon"
				                ELSE CAR."INGMontoCapitalCorteCuponMonedaInstrumento"
				        END 
				END AS "INGMontoCapitalCorteCupon"

				,CASE WHEN "IdEstado" = 'RPA' THEN 
						CASE WHEN  CAR."INGIdMonedaPagoCupon" = 'CLP' 
							THEN CAST((("INGMontoInteresCorteCupon" / CAR."INGCantidad") * "EGRCantidad") AS INT)
							ELSE ("INGMontoInteresCorteCuponMonedaInstrumento"/CAR."INGCantidad")*"EGRCantidad"
						END 
				 ELSE CASE WHEN  CAR."INGIdMonedaPagoCupon" = 'CLP'
				 			THEN "INGMontoInteresCorteCupon" 
				 			ELSE "INGMontoInteresCorteCuponMonedaInstrumento"
				 	  END 
				 END  AS "INGInteresesPagoCorteCupon"	 
				,CASE WHEN "IdEstado" = 'RPA' THEN
						CASE WHEN  CAR."INGIdMonedaPagoCupon" = 'CLP'
							THEN CAST((("INGMontoCorteCupon"/CAR."INGCantidad")*"EGRCantidad") AS INT)
							ELSE ("INGMontoCorteCuponMonedaInstrumento"/CAR."INGCantidad")*"EGRCantidad"
						END 
				 ELSE CASE WHEN  CAR."INGIdMonedaPagoCupon" = 'CLP'
				 			THEN "INGMontoCorteCupon" 
				 			ELSE "INGMontoCorteCuponMonedaInstrumento" 
				 		END 
			     END AS "INGMontoCorteCupon"
		  		,CAR."INGIdMonedaPagoCupon"
		  		,CAR."IdBoveda"
		  		,CAR."IdBolsa"
		  		,CAR."INGMontoReajusteCorteCupon"
		  		,CAR."INGMontoInteresCorteCupon"
		  		,CAR."INGMontoCapitalCorteCuponMonedaInstrumento"
		  		,CAR."INGMontoCorteCuponMonedaInstrumento"
		  		,CAR."IdTipoReajuste"
		  		,CAR."FechaLiquidacion"
				,CAR."INGFechaCorteCupon"
				,CAR."INGValorFactorMonedaInstrumentoProximo"
				,CAR."INGValorFactorMonedaInstrumentoActual"
				,CAR."INGFechaFactorMonedaAdicionalProximo"
				,CAR."INGIdTipoOperacion"
		  		,CAR."INGMontoInteresCorteCuponMonedaInstrumento"
		FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
		INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" CMBTE ON CMBTE."IdComprobante" = CAR."EGRFolioComprobante" AND CMBTE."IdCliente" = CAR."IdCliente" AND CAR."EGRFolioComprobante"!= 0 AND CMBTE."IdEstadoComprobante" != 'ANU'
		WHERE CAR."IdClasificacionTipoInstrumento"   = 'RF'
		AND (CAR."EGRFechaLiquidacion" >= :FechaCorteCuponDesde AND CAR."EGRCantidad" <> 0 AND CAR."EGRRetiroPorFusion" = 'N')  
		AND CAR."INGFechaCorteCupon" < CAR."EGRFechaLiquidacion"
		AND CAR."IdTipoInstrumento" != 'SIM'
		AND CAR."IdClasificacionTipoInstrumento" != 'DV'
		AND CAR."IdEstado" != 'ANU'
		AND CAR."EGRIdTipoOperacion" = 'VENRF'
		AND CAR."IdInstrumento" IN (CASE WHEN (COALESCE(:IdInstrumento,'') = '') THEN CAR."IdInstrumento" ELSE :IdInstrumento END)
		
		UNION ALL
		
		SELECT --Devuelve cantidades retiradas cuando la fecha de liquidación del retiro es superior a la fecha de corte de cupón
		  		CAR."IdEmpresa"
		  		,CAR."IdCartera"
				,CAR."INGNumeroCupon"	
				,CAR."IdClienteRelacionado"
				,CAR."IdTipoOperacion"
				,CAR."IdPortafolioRelacionado"
		  		,CPA."IdPortafolio"
		  		,CAR."IdCliente"
		  		,CAR."IdInstrumento"
		  		,CAR."IdTipoInstrumento"
		  		,CAR."INGFechaPagoCorteCupon"
		  		,CAR."IdClasificacionTipoInstrumento"
		  		,CAR."Nemotecnico"
		  		,'VIG' AS "IdEstado"
		  		,CAR."EGRCantidad" as "Saldo"
		  		,CAR."EGRCantidad" AS "Cantidad"
				-- Monto capital
				,CASE 
				    WHEN CAR."IdEstado" = 'RPA' THEN 
				        CASE 
				            WHEN CAR."INGIdMonedaPagoCupon" = 'CLP' 
				                THEN CAST((("INGMontoCapitalCorteCupon" / CAR."INGCantidad") * "EGRCantidad") AS INT) 
				                ELSE (CAR."INGMontoCapitalCorteCuponMonedaInstrumento" / CAR."INGCantidad") * "EGRCantidad"
				        END 
				    ELSE 
				        CASE 
				            WHEN CAR."INGIdMonedaPagoCupon" = 'CLP' 
				                THEN CAR."INGMontoCapitalCorteCupon"
				                ELSE CAR."INGMontoCapitalCorteCuponMonedaInstrumento"
				        END 
				END AS "INGMontoCapitalCorteCupon"

				,CASE WHEN CAR."IdEstado" = 'RPA' THEN 
						CASE WHEN  CAR."INGIdMonedaPagoCupon" = 'CLP' 
							THEN CAST((("INGMontoInteresCorteCupon" / CAR."INGCantidad") * "EGRCantidad") AS INT)
							ELSE ("INGMontoInteresCorteCuponMonedaInstrumento"/CAR."INGCantidad")*"EGRCantidad"
						END 
				 ELSE CASE WHEN  CAR."INGIdMonedaPagoCupon" = 'CLP'
				 			THEN "INGMontoInteresCorteCupon" 
				 			ELSE "INGMontoInteresCorteCuponMonedaInstrumento"
				 	  END 
				 END  AS "INGInteresesPagoCorteCupon"	 
				,CASE WHEN CAR."IdEstado" = 'RPA' THEN
						CASE WHEN  CAR."INGIdMonedaPagoCupon" = 'CLP'
							THEN CAST((("INGMontoCorteCupon"/CAR."INGCantidad")*"EGRCantidad") AS INT)
							ELSE ("INGMontoCorteCuponMonedaInstrumento"/CAR."INGCantidad")*"EGRCantidad"
						END 
				 ELSE CASE WHEN  CAR."INGIdMonedaPagoCupon" = 'CLP'
				 			THEN "INGMontoCorteCupon" 
				 			ELSE "INGMontoCorteCuponMonedaInstrumento" 
				 		END 
			     END AS "INGMontoCorteCupon"
		  		,CAR."INGIdMonedaPagoCupon"
		  		,CAR."IdBoveda"
		  		,CAR."IdBolsa"
		  		,CAR."INGMontoReajusteCorteCupon"
		  		,CAR."INGMontoInteresCorteCupon"
		  		,CAR."INGMontoCapitalCorteCuponMonedaInstrumento"
		  		,CAR."INGMontoCorteCuponMonedaInstrumento"
		  		,CAR."IdTipoReajuste"
		  		,CAR."FechaLiquidacion"
				,CAR."INGFechaCorteCupon"
				,CAR."INGValorFactorMonedaInstrumentoProximo"
				,CAR."INGValorFactorMonedaInstrumentoActual"
				,CAR."INGFechaFactorMonedaAdicionalProximo"
				,CAR."INGIdTipoOperacion"
		  		,CAR."INGMontoInteresCorteCuponMonedaInstrumento"
		FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
		LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor"  				AS Emisor ON Emisor."IdEmisor" = CAR."IdEmisor"
		LEFT JOIN CodigoPortafolioActivo CPA ON Emisor."IdPersona" = CPA."IdPersona"
		WHERE CAR."IdClasificacionTipoInstrumento"   = 'RF'
		AND (CAR."INGFechaPagoCorteCupon" BETWEEN :FechaCorteCuponDesde AND :FechaCorteCuponHasta)
		AND (CAR."EGRFechaLiquidacion" >= :FechaCorteCuponDesde AND CAR."EGRCantidad" <> 0 AND CAR."EGRRetiroPorFusion" = 'N')  
		AND CAR."INGFechaCorteCupon" < CAR."EGRFechaLiquidacion"
		AND CAR."IdTipoInstrumento" != 'SIM'
		AND CAR."IdClasificacionTipoInstrumento" != 'DV'
		AND CAR."IdEstado" != 'ANU'
		AND CAR."EGRIdTipoOperacion" = 'RETTIT'
		AND CAR."IdInstrumento" IN (CASE WHEN (COALESCE(:IdInstrumento,'') = '') THEN CAR."IdInstrumento" ELSE :IdInstrumento END)
		)  AS CA
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBoveda"             	AS BO ON (CA."IdBoveda" = BO."IdBoveda")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 		AS Instru ON Instru."IdInstrumento" = CA."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" 				AS Emisor ON Emisor."IdEmisor" = Instru."IdEmisor"
LEFT JOIN CodigoPortafolioActivo CPA ON Emisor."IdPersona" = CPA."IdPersona"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"   AS SN ON (BO."IdPersona" = SN."IdPersona")
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona"							AS PE ON (BO."IdPersona" = PE."IdPersona")
LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapital" 						AS EC
ON 	(CA."IdInstrumento" 		 = EC."IdInstrumento" 			AND 
	 CA."IdTipoInstrumento" 	 = EC."IdTipoInstrumento"		AND
	 EC."IdTipoEventoCapital"	 = 'CC'                         AND
	 CA."INGFechaPagoCorteCupon"   = EC."FechaPago"           AND
	 EC."IdEstado"	            <> 'ANU'
	)

WHERE CA."IdClasificacionTipoInstrumento" = 'RF'				
AND   CA."INGFechaPagoCorteCupon"			BETWEEN :FechaCorteCuponDesde AND :FechaCorteCuponHasta	
AND   CA."FechaLiquidacion" 				<= :FechaCorteCuponHasta
AND   CA."IdEstado" 						IN ( 'VIG','VCD')
AND   CA."IdBoveda" 					   <> 'CIA'				
AND   EC."IdTipoInstrumento" 				IS NULL	


AND     CA."IdInstrumento" IN (CASE WHEN (COALESCE(:IdInstrumento,'') = '') THEN CA."IdInstrumento" ELSE :IdInstrumento END)


AND (CASE -- Letras hipotecarias cuyo nemo tiene "*" o "&" no generan pago cupon sino cambio de nemo
		WHEN CA."IdTipoInstrumento" = 'LTH' AND LOCATE(CA."Nemotecnico", '*') > 0 THEN 'CAMBIO_NEMO' 
		WHEN CA."IdTipoInstrumento" = 'LTH' AND LOCATE(CA."Nemotecnico", '&') > 0 THEN 'CAMBIO_NEMO'
		ELSE 'CORTE_CUPON'
	END) = 'CORTE_CUPON'
	
GROUP BY
	CA."IdCartera",
	CA."IdPortafolio",	
	BO."DocumentoIdentidad",
	Emisor."DocumentoIdentidad",
	PE."RazonSocial",
	Emisor."NombreCompleto",
	CA."IdClasificacionTipoInstrumento",	
	CA."IdTipoInstrumento",		
	CA."IdInstrumento",
	CA."Nemotecnico",
	CA."INGFechaPagoCorteCupon",
	CA."INGFechaCorteCupon",
	CA."INGIdMonedaPagoCupon",
	CA."INGNumeroCupon",
	CA."IdBoveda",			
	CA."IdTipoReajuste",
	CA."IdBolsa",
	CA."IdEstado",
	CA."FechaLiquidacion",
	CA."INGValorFactorMonedaInstrumentoProximo",
	CA."INGFechaCorteCupon",
	CA."IdTipoOperacion",
	CA."INGFechaFactorMonedaAdicionalProximo",
	CA."INGValorFactorMonedaInstrumentoActual",
	CA."INGIdTipoOperacion"
	
	UNION ALL
--Cartera Historica
SELECT 
		'CC'															AS "IdTipoEventoCapital"
		,'N'															AS "EsCarteraNegativa"
		,CA."INGNumeroCupon"											AS "Secuencia"	
		,TO_BIGINT(0)													AS "IdSocioNegocio"
		,'CNT'			   												AS "TipoSocioNegocio"
		,COALESCE(CPA."IdPortafolio",0)									AS "IdPortafolio"
		,'R'															AS "IdTipoDocumentoIdentidad"
		,CASE 
		   WHEN :p_NombreEmisor = 'S' THEN IFNULL(CPA."DocumentoIdentidad",'')
		 ELSE BO."DocumentoIdentidad"
		 END AS "DocumentoIdentidad"
		,CASE 
		   WHEN :p_NombreEmisor = 'S' THEN IFNULL(CPA."NombreCompleto",'')
		 ELSE IFNULL(PE."RazonSocial",'')
		 END AS "NombreCliente"
		,CA."IdClasificacionTipoInstrumento"							AS "IdClasificacionTipoInstrumento"
		,CA."IdTipoInstrumento"											AS "IdTipoInstrumento"
		,CA."IdInstrumento"												AS "IdInstrumento"
		,CA."Nemotecnico"												AS "Nemo"
		,TO_BIGINT(0)													AS "IdInstrumentoNuevo"
		,''																AS "NemoNuevo"
		,CA."INGFechaPagoCorteCupon"									AS "FechaCierre"
		,CA."INGFechaPagoCorteCupon"									AS "FechaPago"
		,CASE WHEN CA."IdEstado" = 'VIG' 
				THEN SUM(CA."INGCantidad" - CA."EGRCantidad") * -1 
				ELSE SUM("Cantidad") * -1								END AS "CantidadDerecho"
		,'CUS'															AS "TipoCartera"
		,TO_DECIMAL(0,26,8)												AS "CantidadNueva"
		-- AMORTIZACION
		,CASE WHEN CA."IdTipoReajuste" != 'CLP'  AND "INGFechaPagoCorteCupon" != "INGFechaCorteCupon"
			THEN CASE WHEN CA."INGIdMonedaPagoCupon" = 'CLP' 
					THEN SUM("INGValorFactorMonedaInstrumentoProximo" * ("INGMontoCapitalCorteCupon"/"INGValorFactorMonedaInstrumentoActual")) * -1
					ELSE SUM("INGMontoCapitalCorteCuponMonedaInstrumento") * -1
				 END
		ELSE 
				CASE WHEN CA."INGIdMonedaPagoCupon" = 'CLP'
					THEN SUM(CA."INGMontoCapitalCorteCupon") *-1
					ELSE SUM(CA."INGMontoCapitalCorteCuponMonedaInstrumento") * -1
				END
		END	AS "MontoCapital" --AMORTIZACION
		--INTERES
		,CASE WHEN CA."IdTipoReajuste" != 'CLP'  AND "INGFechaPagoCorteCupon" != "INGFechaCorteCupon"
			THEN CASE WHEN CA."INGIdMonedaPagoCupon" = 'CLP' 
				THEN SUM("INGValorFactorMonedaInstrumentoProximo" * ("INGInteresesPagoCorteCupon"/"INGValorFactorMonedaInstrumentoActual")) * -1
				ELSE SUM("INGMontoInteresCorteCuponMonedaInstrumento") * -1
			END
		ELSE 
			CASE WHEN CA."INGIdMonedaPagoCupon" = 'CLP' 
				THEN SUM(CA."INGInteresesPagoCorteCupon") * -1
				ELSE SUM(CA."INGMontoInteresCorteCuponMonedaInstrumento") * -1
			END
		END	AS "MontoInteres" --INTERES
		--TOTAL
		,CASE WHEN CA."IdTipoReajuste" != 'CLP'  AND "INGFechaPagoCorteCupon" != "INGFechaCorteCupon"
			THEN CASE WHEN CA."INGIdMonedaPagoCupon" = 'CLP'
				THEN SUM("INGValorFactorMonedaInstrumentoProximo" * ("INGMontoCorteCupon"/"INGValorFactorMonedaInstrumentoActual")) * -1
				ELSE SUM("INGMontoCorteCuponMonedaInstrumento") * -1 
			END
		 ELSE
		 	CASE WHEN CA."INGIdMonedaPagoCupon" = 'CLP'
		 		THEN SUM(CA."INGMontoCorteCupon") * -1
		 		ELSE SUM(CA."INGMontoCorteCuponMonedaInstrumento") * -1
		 	END
		END	AS "Total"
		,CA."INGIdMonedaPagoCupon"										AS "IdMoneda"
		,CA."INGNumeroCupon"											AS "SecuenciaCupon"	
		,TO_BIGINT(0)													AS "IdEmpleadoEjecutivoPortafolio"
		,''																AS "CodigoUsuarioAgente"	
		,''																AS "NombreAgente"
		,''																AS "CodigoSucursal"
		,CURRENT_UTCTIMESTAMP											AS "FechaHoraIngresoRegistro"
		,CURRENT_UTCTIMESTAMP											AS "FechaHoraUltimaActualizacion"		
		,'VIG'															AS "IdEstado"
		,CA."IdBoveda"													AS "IdBoveda"
		,CA."IdBolsa"													AS "IdBolsa"
		,''																AS "IdConceptoGarantia"
		,''																AS "IdTipoOperacionGarantia"
		,SUM(CA."INGMontoReajusteCorteCupon")							AS "MontoReajuste"
		,'SIS'															AS "Origen"
		,CA."IdTipoReajuste"											AS "IdTipoReajuste"
		,TO_DECIMAL(0,26,8)												AS "INGMontoInteresCorteCupon"  
		,''	AS "TipoOperacion"
		,CA."FechaLiquidacion"
		,CA."INGValorFactorMonedaInstrumentoProximo"
		,CA."INGFechaCorteCupon"
		,CA."IdTipoOperacion"
		,'0001-01-01' AS "INGFechaFactorMonedaAdicionalProximo"
		,CA."INGValorFactorMonedaInstrumentoActual"
		,CASE WHEN CA."IdTipoReajuste" != 'CLP'  AND ("INGFechaPagoCorteCupon" != "INGFechaCorteCupon" OR "FechaRespaldo" < "INGFechaCorteCupon") THEN 'S' 
			ELSE 'N' END AS "UsaProximoFactorValorMoneda"
		,CA."IdTipoOperacion" AS "INGIdTipoOperacion"
		,CA."IdCartera"
		,'CARTERA_HISTORICA' as "QueryOrigen"
FROM CuponesCarteraHistorica AS CarteraHistorica
INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" AS CA ON CA."FolioCartera" = CarteraHistorica."FolioCartera" AND CA."FechaRespaldo" = CarteraHistorica."FechaUltimoRespaldo" AND CA."IdEstado" = CarteraHistorica."IdEstado"
LEFT JOIN BSCL."BSCL.Plataforma.BD::INS.Instrumento" AS INS ON CA."IdInstrumento" = INS."IdInstrumento" 
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBoveda"             	AS BO ON (CA."IdBoveda" = BO."IdBoveda")
INNER JOIN "BSCL.Plataforma.BD::MER.Emisor"  				AS Emisor ON Emisor."IdEmisor" = INS."IdEmisor"
LEFT JOIN CodigoPortafolioActivo CPA ON Emisor."IdPersona" = CPA."IdPersona" 
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"   AS SN ON (BO."IdPersona" = SN."IdPersona")
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona"							AS PE ON (BO."IdPersona" = PE."IdPersona")
LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapital" 						AS EC
ON 	(CA."IdInstrumento" 		 = EC."IdInstrumento" 			AND 
	 CA."IdTipoInstrumento" 	 = EC."IdTipoInstrumento"		AND
	 EC."IdTipoEventoCapital"	 = 'CC'                         AND
	 CA."INGFechaPagoCorteCupon" = EC."FechaPago"          		AND
	 EC."IdEstado"	            <> 'ANU'						AND
	 CA."INGFechaCorteCupon"	<= CA."INGFechaPagoCorteCupon" 
	)
WHERE   EC."IdTipoInstrumento" 				IS NULL	
GROUP BY 
	CA."IdCartera",
	BO."DocumentoIdentidad",
	CPA."IdPortafolio",
	CPA."DocumentoIdentidad",
	PE."RazonSocial",
	CPA."NombreCompleto",
	CA."IdClasificacionTipoInstrumento",	
	CA."IdTipoInstrumento",		
	CA."IdInstrumento",
	CA."Nemotecnico",
	CA."INGFechaPagoCorteCupon",
	CA."INGFechaCorteCupon",
	CA."INGIdMonedaPagoCupon",
	CA."INGNumeroCupon",
	CA."IdBoveda",					
	CA."IdBolsa",
	CA."IdTipoReajuste",
	CA."IdEstado",
	CA."IdTipoReajuste",
	CA."FechaLiquidacion",
	CA."INGValorFactorMonedaInstrumentoProximo",
	CA."INGFechaCorteCupon",
	CA."IdTipoOperacion",
	CA."INGValorFactorMonedaInstrumentoActual",
	CA."FechaRespaldo"

	UNION ALL
-- Cartera Negativa
SELECT 
		'CC'															AS "IdTipoEventoCapital"
		,'S'															AS "EsCarteraNegativa"
		,CA."NumeroCupon"												AS "Secuencia"	
		,TO_BIGINT(0)													AS "IdSocioNegocio"
		,'CNT'			   												AS "TipoSocioNegocio"
		,TO_BIGINT(0)													AS "IdPortafolio"
		,'R'															AS "IdTipoDocumentoIdentidad"
		,CASE 
		   WHEN :p_NombreEmisor = 'S' THEN IFNULL(Emisor."DocumentoIdentidad",'')
		 ELSE BO."DocumentoIdentidad"
		 END AS "DocumentoIdentidad"
		,CASE 
		   WHEN :p_NombreEmisor = 'S' THEN IFNULL(Emisor."NombreCompleto",'')
		 ELSE IFNULL(PE."RazonSocial",'')
		 END AS "NombreCliente"
		,TIP."IdClasificacionTipoInstrumento"							AS "IdClasificacionTipoInstrumento"
		,TIP."IdTipoInstrumento"										AS "IdTipoInstrumento"
		,INS."IdInstrumento"											AS "IdInstrumento"
		,CA."Nemotecnico"												AS "Nemo"
		,TO_BIGINT(0)													AS "IdInstrumentoNuevo"
		,''																AS "NemoNuevo"
		,CA."FechaCorteCupon"											AS "FechaCierre"
		,CA."FechaCorteCupon"											AS "FechaPago"
		,SUM("Cantidad")												AS "CantidadDerecho"
		,'CUS'															AS "TipoCartera"
		,TO_DECIMAL(0,26,8)												AS "CantidadNueva"
		,SUM(CA."MontoCapitalCorteCupon")								AS "MontoCapital"	
		,SUM(CA."InteresesPagoCorteCupon")								AS "MontoInteres"
		,SUM(CA."MontoCorteCupon")										AS "Total"
		,CA."IdMonedaPagoCupon"											AS "IdMoneda"
		,CA."NumeroCupon"												AS "SecuenciaCupon"	
		,TO_BIGINT(0)													AS "IdEmpleadoEjecutivoPortafolio"
		,''																AS "CodigoUsuarioAgente"	
		,''																AS "NombreAgente"
		,''																AS "CodigoSucursal"
		,CURRENT_UTCTIMESTAMP											AS "FechaHoraIngresoRegistro"
		,CURRENT_UTCTIMESTAMP											AS "FechaHoraUltimaActualizacion"		
		,'VIG'															AS "IdEstado"
		,CA."IdBoveda"													AS "IdBoveda"
		,CA."IdBolsa"													AS "IdBolsa"
		,''																AS "IdConceptoGarantia"
		,''																AS "IdTipoOperacionGarantia"
		,SUM(CA."MontoReajusteCorteCupon")								AS "MontoReajuste"
		,'SIS'															AS "Origen"
		,CA."IdTipoReajuste"											AS "IdTipoReajuste"
		,TO_DECIMAL(0,26,8)												AS "INGMontoInteresCorteCupon"  
		,''	AS "TipoOperacion"
		,CA."FechaLiquidacion"
		,0 AS "INGValorFactorMonedaInstrumentoProximo"
		,'0001-01-01' AS "INGFechaCorteCupon"
		,CA."IdTipoOperacion"
		,'0001-01-01' AS "INGFechaFactorMonedaAdicionalProximo"
		,0 AS "INGValorFactorMonedaInstrumentoActual"
		,'N' AS "UsaProximoFactorValorMoneda"
		,'' AS "INGIdTipoOperacion"
		,CA."IdCarteraNegativa"
		,'CARTERA_NEGATIVA' as "QueryOrigen"
FROM  "BSCL.Custodia.BD::CUS.CarteraNegativa" 							AS CA
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" 					AS INS ON CA."IdInstrumento" = INS."IdInstrumento" 
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento"				AS TIP ON INS."IdTipoInstrumento"  = TIP."IdTipoInstrumento" 
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBoveda"             	AS BO ON (CA."IdBoveda" = BO."IdBoveda")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 		AS Instru ON Instru."IdInstrumento" = CA."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" 				AS Emisor ON Emisor."IdEmisor" = Instru."IdEmisor"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"   AS SN ON (BO."IdPersona" = SN."IdPersona")
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona"							AS PE ON (BO."IdPersona" = PE."IdPersona")
LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapital" 						AS EC
ON 	(INS."IdInstrumento" 		 = EC."IdInstrumento" 			AND 
	 TIP."IdTipoInstrumento" 	 = EC."IdTipoInstrumento"		AND
	 EC."IdTipoEventoCapital"	 = 'CC'                         AND
	 CA."FechaCorteCupon"		 = EC."FechaPago"          		AND
	 EC."IdEstado"	            <> 'ANU'
	)

WHERE TIP."IdClasificacionTipoInstrumento" = 'RF'				
AND   CA."FechaCorteCupon"					BETWEEN :FechaCorteCuponDesde AND :FechaCorteCuponHasta	
AND   CA."FechaLiquidacion" 				<= :FechaCorteCuponDesde
AND   CA."IdEstado" 						IN ('VIG','VCD')
AND   CA."IdBoveda" 					   <> 'CIA'				
AND   EC."IdTipoInstrumento" 				IS NULL	
AND   CA."FechaCorteCupon"					= CA."FechaPagoCorteCupon"

AND     CA."IdInstrumento" IN (CASE WHEN (COALESCE(:IdInstrumento,'') = '') THEN CA."IdInstrumento" ELSE :IdInstrumento END)


AND (CASE -- Letras hipotecarias cuyo nemo tiene "*" o "&" no generan pago cupon sino cambio de nemo
		WHEN TIP."IdTipoInstrumento" = 'LTH' AND LOCATE(INS."Nemotecnico", '*') > 0 THEN 'CAMBIO_NEMO' 
		WHEN TIP."IdTipoInstrumento" = 'LTH' AND LOCATE(INS."Nemotecnico", '&') > 0 THEN 'CAMBIO_NEMO'
		ELSE 'CORTE_CUPON'
	END) = 'CORTE_CUPON'
	
GROUP BY 
	CA."IdCarteraNegativa",
	BO."DocumentoIdentidad",
	Emisor."DocumentoIdentidad",
	PE."RazonSocial",
	Emisor."NombreCompleto",
	TIP."IdClasificacionTipoInstrumento",	
	TIP."IdTipoInstrumento",		
	INS."IdInstrumento",
	CA."Nemotecnico",
	CA."FechaCorteCupon",
	CA."IdMonedaPagoCupon",
	CA."NumeroCupon",
	CA."IdBoveda",					
	CA."IdBolsa",
	CA."IdTipoReajuste",
	CA."IdEstado",
	CA."FechaLiquidacion",
	CA."IdTipoOperacion"

	UNION ALL
-- Cartera Negativa Historica
SELECT 
		'CC'															AS "IdTipoEventoCapital"
		,'S'															AS "EsCarteraNegativa"
		,CA."NumeroCupon"												AS "Secuencia"	
		,TO_BIGINT(0)													AS "IdSocioNegocio"
		,'CNT'			   												AS "TipoSocioNegocio"
		,TO_BIGINT(0)													AS "IdPortafolio"
		,'R'															AS "IdTipoDocumentoIdentidad"
		,CASE 
		   WHEN :p_NombreEmisor = 'S' THEN IFNULL(Emisor."DocumentoIdentidad",'')
		 ELSE BO."DocumentoIdentidad"
		 END AS "DocumentoIdentidad"
		,CASE 
		   WHEN :p_NombreEmisor = 'S' THEN IFNULL(Emisor."NombreCompleto",'')
		 ELSE IFNULL(PE."RazonSocial",'')
		 END AS "NombreCliente"
		,TIP."IdClasificacionTipoInstrumento"							AS "IdClasificacionTipoInstrumento"
		,TIP."IdTipoInstrumento"										AS "IdTipoInstrumento"
		,INS."IdInstrumento"											AS "IdInstrumento"
		,CA."Nemotecnico"												AS "Nemo"
		,TO_BIGINT(0)													AS "IdInstrumentoNuevo"
		,''																AS "NemoNuevo"
		,CA."FechaCorteCupon"											AS "FechaCierre"
		,CA."FechaCorteCupon"											AS "FechaPago"
		,SUM("Cantidad")												AS "CantidadDerecho"
		,'CUS'															AS "TipoCartera"
		,TO_DECIMAL(0,26,8)												AS "CantidadNueva"
		,SUM(CA."MontoCapitalCorteCupon")								AS "MontoCapital"	
		,SUM(CA."InteresesPagoCorteCupon")								AS "MontoInteres"
		,SUM(CA."MontoCorteCupon")										AS "Total"
		,CA."IdMonedaPagoCupon"											AS "IdMoneda"
		,CA."NumeroCupon"												AS "SecuenciaCupon"	
		,TO_BIGINT(0)													AS "IdEmpleadoEjecutivoPortafolio"
		,''																AS "CodigoUsuarioAgente"	
		,''																AS "NombreAgente"
		,''																AS "CodigoSucursal"
		,CURRENT_UTCTIMESTAMP											AS "FechaHoraIngresoRegistro"
		,CURRENT_UTCTIMESTAMP											AS "FechaHoraUltimaActualizacion"		
		,'VIG'															AS "IdEstado"
		,CA."IdBoveda"													AS "IdBoveda"
		,CA."IdBolsa"													AS "IdBolsa"
		,''																AS "IdConceptoGarantia"
		,''																AS "IdTipoOperacionGarantia"
		,SUM(CA."MontoReajusteCorteCupon")								AS "MontoReajuste"
		,'SIS'															AS "Origen"
		,CA."IdTipoReajuste"											AS "IdTipoReajuste"
		,TO_DECIMAL(0,26,8)												AS "INGMontoInteresCorteCupon"  
		,''	AS "TipoOperacion"
		,CA."FechaLiquidacion"
		,0 AS "INGValorFactorMonedaInstrumentoProximo"
		,'0001-01-01' AS "INGFechaCorteCupon"
		,CA."IdTipoOperacion"
		,'0001-01-01' AS "INGFechaFactorMonedaAdicionalProximo"
		,0 AS "INGValorFactorMonedaInstrumentoActual"	
		,'N' AS "UsaProximoFactorValorMoneda"
		,'' AS "INGIdTipoOperacion"
		,CA."IdCarteraNegativa"
		,'CARTERA_NEGATIVA_HISTORICA' as "QueryOrigen"
FROM  "BSCL.Custodia.BD::HIS.CarteraNegativa" 							AS CA
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" 					AS INS ON CA."IdInstrumento" = INS."IdInstrumento" 
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento"				AS TIP ON INS."IdTipoInstrumento"  = TIP."IdTipoInstrumento" 
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBoveda"             	AS BO ON (CA."IdBoveda" = BO."IdBoveda")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 		AS Instru ON Instru."IdInstrumento" = CA."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" 				AS Emisor ON Emisor."IdEmisor" = Instru."IdEmisor"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"   AS SN ON (BO."IdPersona" = SN."IdPersona")
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona"							AS PE ON (BO."IdPersona" = PE."IdPersona")
LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapital" 						AS EC
ON 	(INS."IdInstrumento" 		 = EC."IdInstrumento" 			AND 
	 TIP."IdTipoInstrumento" 	 = EC."IdTipoInstrumento"		AND
	 EC."IdTipoEventoCapital"	 = 'CC'                         AND
	 CA."FechaCorteCupon"		 = EC."FechaPago"          		AND
	 EC."IdEstado"	            <> 'ANU'
	)

WHERE TIP."IdClasificacionTipoInstrumento" = 'RF'				
AND   CA."FechaCorteCupon"					BETWEEN :FechaCorteCuponDesde AND :FechaCorteCuponHasta	
AND   CA."FechaLiquidacion" 				<= :FechaCorteCuponDesde
AND   CA."IdEstado" 						IN ('VIG','VCD')
AND   CA."IdBoveda" 					   <> 'CIA'				
AND   EC."IdTipoInstrumento" 				IS NULL	
AND	  CA."FechaCorteCupon"					<> CA."FechaPagoCorteCupon"
AND   CA."FechaRespaldo"					 = CA."FechaCorteCupon" 

AND     CA."IdInstrumento" IN (CASE WHEN (COALESCE(:IdInstrumento,'') = '') THEN CA."IdInstrumento" ELSE :IdInstrumento END)


AND (CASE -- Letras hipotecarias cuyo nemo tiene "*" o "&" no generan pago cupon sino cambio de nemo
		WHEN TIP."IdTipoInstrumento" = 'LTH' AND LOCATE(INS."Nemotecnico", '*') > 0 THEN 'CAMBIO_NEMO' 
		WHEN TIP."IdTipoInstrumento" = 'LTH' AND LOCATE(INS."Nemotecnico", '&') > 0 THEN 'CAMBIO_NEMO'
		ELSE 'CORTE_CUPON'
	END) = 'CORTE_CUPON'
	
GROUP BY 
	CA."IdCarteraNegativa",
	BO."DocumentoIdentidad",
	Emisor."DocumentoIdentidad",
	PE."RazonSocial",
	Emisor."NombreCompleto",
	TIP."IdClasificacionTipoInstrumento",	
	TIP."IdTipoInstrumento",		
	INS."IdInstrumento",
	CA."Nemotecnico",
	CA."FechaCorteCupon",
	CA."IdMonedaPagoCupon",
	CA."NumeroCupon",
	CA."IdBoveda",					
	CA."IdBolsa",
	CA."IdTipoReajuste",
	CA."IdEstado",
	CA."FechaLiquidacion",
	CA."IdTipoOperacion";
