SELECT 
	MOV."IdMovimientoComprobante" 				AS "SecuenciaMovimiento",
	MOV."IdClasificacionTipoInstrumento"		AS "IdClasificacionTipoInstrumento",
	MOV."IdTipoComprobante"						AS "TipoComprobante",
	CONC."Nombre"								AS "Concepto",
	EST."Nombre"								AS "Estado",
	TRA."Folio"									AS "FolioTransaccion",
	
	TO_CHAR(TRA."FechaOperacion", 'YYYY/MM/DD')	AS "FechaTransaccion",
	ASI."IdOrden"								AS "SecuenciaOrden",
	CASE 
		WHEN CLIPER."Tipo" = 'J'
			THEN CLIPER."RazonSocial"
		ELSE
			CLIPER."Nombres" || ' ' || CLIPER."ApellidoPaterno" || ' ' || CLIPER."ApellidoMaterno"
	END											AS "Cliente",
	PORT."CodigoPortafolio"						AS "CodigoPortafolio",
	TIPOP."AccionOrden"							AS "Operacion",
	TIPOP."Nombre"								AS "TipoOperacion",
	CASE 
	WHEN MOV."IdTipoInstrumento" = 'SIM'
		THEN MOV."NemotecnicoSubyacente"
	ELSE MOV."NemotecnicoInstrumento"
	END											AS "Nemotecnico",
	CASE 
	WHEN MOV."IdTipoInstrumento" = 'SIM'
		THEN EMISUBPER."RazonSocial"
	ELSE EMIPER."RazonSocial"
	END											AS "Emisor",
	CASE
	WHEN MOV."EsFungible" = 'S' AND MOV."IdClasificacionTipoInstrumento" = 'IF' 
		THEN MOV."ValorRescate" 
	ELSE MOV."Cantidad"								
	END											AS "Cantidad",
	COND."Nombre"								AS "CondicionLiquidacion",
	TRA."MarcaAsignacion"						AS "MarcaAsignacion",
	ASI."IdAsignacionMasiva"					AS "IdAsignacionMasiva",
	MOV."IdMoneda"								AS "IdMoneda",
	MOV."IdTipoInstrumento"						AS "IdTipoInstrumento",
	ASI."IdAsignacion"							AS "SecuenciaAsignacion"
FROM "BSCL.Orden.BD::ORD.Asignacion" AS ASI
INNER JOIN "BSCL.Orden.BD::ORD.Transaccion" TRA ON TRA."IdTransaccion" = ASI."IdTransaccion"
INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS MOV ON MOV."IdTransaccion" = ASI."IdTransaccion" 
	AND MOV."IdAsignacion" = ASI."IdAsignacion"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS CONC ON CONC."IdConcepto" = MOV."IdConceptoMovimiento"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EST ON EST."IdEstado" = MOV."IdEstadoFacturacion"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPOP ON TIPOP."IdTipoOperacion" = MOV."IdTipoOperacion"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORT ON PORT."IdPortafolio" = MOV."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = MOV."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS CLIPER ON CLIPER."IdPersona" = CLI."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" AS EMI ON EMI."IdEmisor" = MOV."IdEmisor"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS EMIPER ON EMIPER."IdPersona" = EMI."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS COND ON COND."IdCondicionDeLiquidacion" = MOV."IdCondicionLiquidacion"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUSUB ON INSTRUSUB."IdInstrumento" = MOV."IdInstrumentoSubyacente"
LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" AS EMISUB ON EMISUB."IdEmisor" = INSTRUSUB."IdEmisor"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS EMISUBPER ON EMISUBPER."IdPersona" = EMISUB."IdPersona"
WHERE
	(
		(:FiltraPorAsignacionMasiva) = 'S'
	    AND ASI."IdAsignacionMasiva" IN (:IdAsignacionMasiva)
	    AND MOV."IdClasificacionTipoInstrumento" = 'RV'
	    AND MOV."IdEstadoFacturacion" = 'ING'
    )
    OR 
    (
	    (:FiltraPorAsignacionMasiva) = 'N'
	    AND (
	    	MOV."FechaHoraMovimientoComprobante" >= (:FechaDesde) AND MOV."FechaHoraMovimientoComprobante" <= (:FechaHasta)
	    )
	    AND COALESCE(:IdClasificacionTipoInstrumento, MOV."IdClasificacionTipoInstrumento") = MOV."IdClasificacionTipoInstrumento"
	    AND COALESCE(:Operacion, TIPOP."AccionOrden") = TIPOP."AccionOrden"
	    AND COALESCE(:IdLibroOrdenes, ASI."IdLibroOrdenes") = ASI."IdLibroOrdenes"
	    AND COALESCE(:SecuenciaOrden, ASI."IdOrden") = ASI."IdOrden"
	    AND COALESCE(:IdTipoInstrumento, MOV."IdTipoInstrumento") = MOV."IdTipoInstrumento"
	    AND COALESCE(:IdCondicionDeLiquidacion, MOV."IdCondicionLiquidacion") = MOV."IdCondicionLiquidacion"
	    AND COALESCE(:CodigoBolsa, MOV."CodigoBolsa") = MOV."CodigoBolsa"
	    AND COALESCE(:FolioTransaccion, TRA."Folio") = TRA."Folio"
	    AND COALESCE(:IdPortafolio, MOV."IdPortafolio") = MOV."IdPortafolio"
	    AND (
			COALESCE(:IdInstrumento, MOV."IdInstrumento") = MOV."IdInstrumento"
			OR
			COALESCE(:IdInstrumento, MOV."IdInstrumentoSubyacente") = MOV."IdInstrumentoSubyacente"
		)
	    AND COALESCE(:IdTipoCanalIngreso, MOV."IdTipoCanalIngreso") = MOV."IdTipoCanalIngreso"
	    AND COALESCE(:MarcaAsignacion, TRA."MarcaAsignacion") = TRA."MarcaAsignacion"
	    AND MOV."IdEstadoFacturacion" = 'ING'
	    AND ASI."IdEstadoAsignacion" = 'EAF'
    )
    