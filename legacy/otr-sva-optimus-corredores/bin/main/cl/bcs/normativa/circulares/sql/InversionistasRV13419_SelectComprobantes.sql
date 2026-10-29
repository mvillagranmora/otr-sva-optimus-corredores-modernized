SELECT OPE.*  FROM
(
	SELECT 
		 CCU."MontoMovimientoComprobante" AS "MontoComprobante"
		,CCU."FolioComprobante"
		,CCU."FolioTransaccion" AS "Folio"
		,CCU."IdTipoComprobante"
		,TOPE."IdTipoOperacion"
		,TOPE."Accion"
		,CCU."IdCliente" 
		,CCU."IdTipoInstrumento"
		,CCU."DocumentoIdentidad" AS "RutCliente"
		,CASE WHEN DIR_EXT."IdPersona" IS NOT NULL THEN 'S' ELSE 'N' END AS "EsExtranjero"
		,CCU."FechaMovimientoComprobante" AS "FechaComprobante"
		,COALESCE(PA."NombrePais",'No indicada') AS "Nacionalidad"
		,COALESCE(CLI."IdTipoCliente",'') AS "IdTipoCliente"
		,CCU."IdComprobante"
		,CCU."IdMovimientoComprobante"
		,CCU."IdMoneda"
		,CASE WHEN COALESCE(EMP."IdSocioDeNegocio",0) = 0  THEN 'N' ELSE 'S' END AS "EsCorredor"
		,CASE WHEN COALESCE(CIC."ValorIndicador",'N') ='S' THEN 'S' ELSE 'N' END AS "EsRelacionado" 
		
	FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante"  CCU
	INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SDN
	ON CCU."IdCliente" = SDN."IdCliente"
	INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI
	ON CLI."IdCliente" = CCU."IdCliente"
	INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER
	ON CLI."IdPersona" = PER."IdPersona"
	LEFT JOIN 
		(
			SELECT DIR."IdPersona" FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica" DIR
			INNER JOIN "BSCL.Plataforma.BD::REG.Ciudad" as CIU
			ON DIR."IdCiudad" = CIU."IdCiudad" 
			AND COALESCE(CIU."IdPais",:p_Pais) != :p_Pais
			WHERE DIR."Borrado" = 'N'
			GROUP BY "IdPersona"
		) AS DIR_EXT
	ON PER."IdPersona" = DIR_EXT."IdPersona"
	LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" AS PA
	ON PER."IdPaisNacimiento" = PA."IdPais"
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TOPE
	ON CCU."IdTipoOperacion" = TOPE."IdTipoOperacion"
	LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Empresa" EMP
	ON EMP."IdSocioDeNegocio" = SDN."IdSocioDeNegocio"
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" CIC
	ON CLI."IdCliente" = CIC."IdCliente" AND CIC."CodigoTipoIndicador" = 'RELCORR'  --ARTICULO 100
	
	WHERE CCU."IdClasificacionTipoInstrumento" = 'RV'
	AND CCU."IdEstadoFacturacion" = 'FAC'
	AND	CCU."IdTipoComprobante" != 'FEE'
	AND	CCU."IdTipoInstrumento" != 'SIM'
	AND CCU."FechaMovimientoComprobante" BETWEEN :p_FechaInicio AND :p_FechaTermino
) AS OPE LEFT  OUTER JOIN --- Sin Notas de Credito 
	(
		SELECT NC."IdComprobanteReferencia"
		FROM "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" NC
		WHERE NC."IdEstadoFacturacion"='FAC' AND NC."IdTipoComprobante"='NCE' AND NC."IdComprobanteReferencia">0
	) AS NC
	ON (OPE."IdComprobante"=NC."IdComprobanteReferencia") 
		WHERE NC."IdComprobanteReferencia" IS NULL AND  OPE."IdTipoComprobante" NOT IN ('NCE')
;