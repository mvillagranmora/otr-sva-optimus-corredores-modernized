WITH CodigoPortafolioActivo AS (
	SELECT "DocumentoIdentidad", "IdPersona","IdCliente", "CodigoPortafolio", "IdPortafolio","NombreCompleto"
	    FROM (
	        SELECT "DocumentoIdentidad","IdCliente", "IdPersona", "CodigoPortafolio", "IdPortafolio","NombreCompleto",
	               ROW_NUMBER() OVER (PARTITION BY "DocumentoIdentidad", "IdPersona" ORDER BY "CodigoPortafolio" ASC) AS rn
	        FROM "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio"
	    ) 
    WHERE rn = 1
)
SELECT DISTINCT 
		'CC'															AS "IdTipoEventoCapital"
		,'N'															AS "EsCarteraNegativa"
		,MOV."IdTipoOperacion"                                          AS "IdTipoOperacion"
		,TipoOperacion."AccionCustodia"
		,TipoOperacion."AccionOperacion" 
		,CMBTE."Tasa" 
		,MOV."MueveCustodia" 
		,CMBTE."IdEmpleadoEjecutivoPortafolio"                          AS "IdEmpleadoEjecutivoPortafolio" 
		,COALESCE(EJE."CodigoEmpleado",'')								AS "CodigoUsuarioAgente"
		,COALESCE(EJE."CodigoSucursal",'')								AS "CodigoSucursal"
		,TO_BIGINT(0)													AS "IdInstrumentoNuevo"
		,''																AS "NemoNuevo"
		,COALESCE(CPA."DocumentoIdentidad", BO."DocumentoIdentidad")    AS "DocumentoIdentidad"
		,COALESCE(CPA."IdPortafolio",0)                                 AS "IdPortafolio"
		,COALESCE (CPA."CodigoPortafolio",0)                            AS "CodigoPortafolio"
		,COALESCE(CPA."NombreCompleto",BO."Nombre")                                          AS "NombreCliente"
		,MOV."IdEstado"                                                 AS "IdEstado"
		,MOV."IdMonedaAdicional"                                        AS "IdMonedaAdicional"  
		,MOV."IdTipoReajuste" 
		,MOV."IdMonedaSistema" 
		,MOV."FechaIngresoRegistro"                                     AS "FechaHoraIngresoRegistro"
		,MOV."IdAgente" 
		,MOV."IdEmisor" 
		,MOV."FechaOperacion"                                           AS "FechaOperacion" 
		,MOV."TasaCompra" 
		,MOV."ValorFactorMonedaInstrumento" 
		,MOV."IdTipoInstrumento" 
		,MOV."OrigenCustodia" 
		,MOV."IdEmisor"                                                 AS "Emisor"
		,SOC."IdSocioDeNegocio"                                         AS "IdSocioDeNegocio"
		,CMBTE."IdEmpleadoEjecutivoPortafolio"                          AS "IdEmpleadoEjecutivoPortafolio"
		,CMBTE."NombreSucursalEmpleadoEjecutivoPortafolio"              AS "NombreAgente"
		,CMBTE."IdSucursalEmpleadoEjecutivoPortafolio" 
		,CMBTE."IdCondicionLiquidacion"                                 AS "IdCondicionDeLiquidacion"
		,CMBTE."MayorMenorValor" 
	    ,inst4."FechaVencimiento"                                       AS "FechaVencimiento" 
	    ,inst4."FechaEmision"                                           AS "FechaEmision"   
	    ,MOV."IdMonedaInstrumento"                                      AS "IdMonedaInstrumento"
	    ,MOV."IdTipoReajuste"                                           AS "IdMonedaEmisionInstrumento"
	    ,inst4."IdTipoReajuste"                                         AS "IdTipoReajusteInstrumento"
		,CMBTE."Precio"                                                 AS "PrecioCompra"
		,MOV."TasaCompra"  
		,MOV."IdMovimientoCarteraEnTransitoRelacionado"                 AS "IdCarteraRelacionado"
		,MOV."IdBolsa"                                                  AS "IdBolsa" 
		,MOV."IdBoveda"                                                 AS "IdBoveda" 
		,''																AS "IdConceptoGarantia"
		,''																AS "IdTipoOperacionGarantia"
		,'SIS'															AS "Origen"
		,MOV."IdCliente" 										
		,'R'															AS "IdTipoDocumentoIdentidad"
		,'CNT'	   												        AS "TipoSocioNegocio"
		,MOV."FechaLiquidacion"                                         AS "FechaLiquidacion" 
		,MOV."IdClasificacionTipoInstrumento"							AS "IdClasificacionTipoInstrumento"
		,MOV."IdTipoInstrumento"										AS "IdTipoInstrumento"
		,MOV."IdInstrumento"											AS "IdInstrumento"
		,MOV."Nemotecnico"												AS "Nemotecnico"
		,TO_BIGINT(0)													AS "IdInstrumentoNuevo"
		,MOV."Cantidad" * -1                                            AS "Cantidad"
		,MOV."FolioExternoReferencia"
		,'CUS'															AS "TipoCartera"
		,TO_DECIMAL(0,26,8)												AS "CantidadNueva"
		,'NOMINAL' as "QueryOrigen"
	FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOV 
		INNER JOIN "BSCL.Plataforma.BD::MER.Emisor" AS Emisor ON Emisor."IdEmisor" = MOV."IdEmisor"
		LEFT JOIN CodigoPortafolioActivo CPA ON Emisor."IdPersona" = CPA."IdPersona"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBoveda"  AS BO ON (MOV."IdBoveda" = BO."IdBoveda")
		LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = MOV."IdCuentaCustodioPortafolio"		
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON MOV."IdTipoOperacion" = TipoOperacion."IdTipoOperacion"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst4 ON inst4."IdInstrumento" = MOV."IdInstrumento"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = MOV."IdPortafolio" AND Portafolio."IdCliente" = MOV."IdCliente"
		LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" CMBTE ON CMBTE."IdComprobante" = MOV."FolioComprobante" AND CMBTE."IdCliente" = MOV."IdCliente" AND MOV."FolioComprobante"!= 0
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolio"	AS EJE ON EJE."IdPortafolio" = CMBTE."IdPortafolio" 
		LEFT JOIN  "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC ON (SOC."IdCliente" = MOV."IdCliente")
	    WHERE (
          (MOV."IdConcepto"='CMINRF' AND MOV."FechaLiquidacion" BETWEEN :FechaCorteCuponDesde AND :FechaCorteCuponHasta AND MOV."FechaOperacion" <:FechaPago)
        OR
          (MOV."IdConcepto"='VNINRF' AND MOV."FechaLiquidacion" >:FechaCorteCuponHasta)
          )
		AND MOV."IdInstrumento" IN (:IdInstrumento)
			AND	MOV."IdEstado" = 'PEN'
		AND COALESCE(TipoOperacion."AccionCustodia",'') <> ''
		AND MOV."IdClasificacionTipoInstrumento" = 'RF'
		AND MOV."IdTipoOperacion" IN ('COMRF','VENRF')
		AND TipoOperacion."AccionCustodia" IN('INGRESO','RETIRO') AND MOV."EsNominal" = 'S'
		AND MOV."IdMovimientoCarteraEnTransitoRelacionado" = 0 
