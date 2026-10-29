SELECT DISTINCT 
		'CC'															AS "IdTipoEventoCapital"
		,'N'															AS "EsCarteraNegativa"
		,MOV."IdTipoOperacion"                                          AS "IdTipoOperacion"
		,MOV."IdEstado"                                                 AS "IdEstado"
		,MOV."IdPortafolioRelacionado" 
		,MOV."IdMonedaSistema" 
		,Portafolio."Alias"                                             AS "NombreCliente"
		,SOC."IdSocioDeNegocio"                                         AS "IdSocioDeNegocio" 
		,CMBTE."IdEmpleadoEjecutivoPortafolio"                          AS "IdEmpleadoEjecutivoPortafolio"
		,COALESCE(EJE."CodigoEmpleado",'')								AS "CodigoUsuarioAgente"
		,COALESCE(EJE."CodigoSucursal",'')								AS "CodigoSucursal"
		,CMBTE."NombreSucursalEmpleadoEjecutivoPortafolio"              AS "NombreAgente"
		,CMBTE."IdSucursalEmpleadoEjecutivoPortafolio" 
		,Portafolio."CodigoPortafolio" 
		,CMBTE."IdBoveda"                                               as "IdBoveda"
		,MOV."IdBolsa"                                                  AS "IdBolsa" 
		,''																AS "IdConceptoGarantia"
		,''																AS "IdTipoOperacionGarantia"
		,'SIS'															AS "Origen"
		,MOV."IdTipoInstrumento" 
		,CMBTE."Precio"                                                 AS "PrecioCompra"
		,CMBTE."IdEmpleadoEjecutivoPortafolio"                          AS "IdEmpleadoEjecutivoPortafolio" 
		,TO_BIGINT(0)													AS "IdInstrumentoNuevo"
		,''																AS "NemoNuevo"
		,MOV."OrigenCustodia" 
		,MOV."IdEmisor"                                                 AS "Emisor"
		,CMBTE."IdCondicionLiquidacion"                                 AS "IdCondicionDeLiquidacion"
	    ,inst4."FechaVencimiento"                                       AS "FechaVencimiento" 
	    ,inst4."FechaEmision"                                           AS "FechaEmision"   
	    ,MOV."FechaOperacion"                                           AS "FechaOperacion"
	    ,MOV."FechaIngresoRegistro"                                     AS "FechaHoraIngresoRegistro"
	    ,MOV."IdMonedaInstrumento"                                      AS "IdMonedaInstrumento"
	    ,MOV."IdMonedaAdicional"                                        AS "IdMonedaAdicional"  
	    ,MOV."IdTipoReajuste"                                           AS "IdMonedaEmisionInstrumento"
	    ,inst4."IdTipoReajuste"                                         AS "IdTipoReajusteInstrumento"
		,CMBTE."DocumentoIdentidad"                                     AS "DocumentoIdentidad"
		,CMBTE."Precio"                                                 AS "PrecioCompra"
		,MOV."IdMovimientoCarteraEnTransitoRelacionado"                 AS "IdCartera"
		,MOV."IdCliente" 										
		,'R'															AS "IdTipoDocumentoIdentidad"
		,'CLI'	   												        AS "TipoSocioNegocio"
		,COALESCE(MOV."IdPortafolio",0)  								AS "IdPortafolio"
		,MOV."FechaLiquidacion"                                         AS "FechaLiquidacion" 
		,MOV."IdClasificacionTipoInstrumento"							AS "IdClasificacionTipoInstrumento"
		,MOV."IdTipoInstrumento"										AS "IdTipoInstrumento"
		,MOV."IdInstrumento"											AS "IdInstrumento"
		,MOV."Nemotecnico"												AS "Nemotecnico"
		,TO_BIGINT(0)													AS "IdInstrumentoNuevo"
		,MOV."Cantidad"                                                 AS "Cantidad"
		,MOV."FolioExternoReferencia"
		,'CUS'																AS "TipoCartera"
		,TO_DECIMAL(0,26,8)												AS "CantidadNueva"
		,'NOMINAL' as "QueryOrigen"
	FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOV 
		LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapital" AS ECLI ON ECLI."IdInstrumento"=MOV."IdInstrumento" 
		LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS rel ON rel."IdMovimientoCarteraEnTransitoRelacionado" = MOV."IdMovimientoCarteraEnTransito"
		LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = MOV."IdCuentaCustodioPortafolio"		
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON MOV."IdTipoOperacion" = TipoOperacion."IdTipoOperacion"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst4 ON inst4."IdInstrumento" = COALESCE(rel."IdInstrumento",MOV."IdInstrumento")
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = MOV."IdPortafolio" AND Portafolio."IdCliente" = MOV."IdCliente"
		LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" CMBTE ON CMBTE."IdComprobante" = MOV."FolioComprobante" AND CMBTE."IdCliente" = MOV."IdCliente" AND MOV."FolioComprobante"!= 0
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolio"	AS EJE ON EJE."IdPortafolio" = CMBTE."IdPortafolio" 
		LEFT JOIN  "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC ON (SOC."IdCliente" = MOV."IdCliente")
		WHERE (
          (MOV."IdConcepto"='CMINRF' AND MOV."FechaLiquidacion" BETWEEN :FechaCorteCuponDesde AND :FechaCorteCuponHasta AND MOV."FechaOperacion" < :FechaPago)
        OR
          (MOV."IdConcepto"='VNINRF' AND MOV."FechaLiquidacion" >:FechaCorteCuponHasta)
          )
		AND MOV."IdInstrumento" IN (:IdInstrumento)
			AND	MOV."IdEstado" = 'PEN'
		AND COALESCE(TipoOperacion."AccionCustodia",'') <> ''
		AND MOV."IdClasificacionTipoInstrumento" = 'RF'
		AND MOV."IdTipoOperacion" in ('COMRF','VENRF')
		AND TipoOperacion."AccionCustodia" IN('INGRESO','RETIRO') AND MOV."EsNominal" = 'S'
		AND MOV."IdMovimientoCarteraEnTransitoRelacionado" = 0 