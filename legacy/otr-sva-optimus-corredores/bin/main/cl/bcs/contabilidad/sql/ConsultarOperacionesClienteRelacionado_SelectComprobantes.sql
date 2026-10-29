-- COMPROBANTES
WITH CobrosComprobantes AS (
	SELECT  "IdComprobante",
	    MAX(CASE WHEN "CodigoCobroClasificacion" = 'COMISION' THEN "MontoCobro" ELSE 0 END) AS "TotalComision",
	    MAX(CASE WHEN "CodigoCobroClasificacion" = 'DERECHO' THEN "MontoCobro" ELSE 0 END) AS "TotalDerecho",
	    MAX(CASE WHEN "CodigoCobroClasificacion" = 'GASTO' THEN "MontoCobro" ELSE 0 END) AS "TotalGasto",
	    MAX(CASE WHEN "CodigoCobroClasificacion" = 'IMPUESTO' THEN "MontoCobro" ELSE 0 END) AS "TotalImpto",
	    MAX(CASE WHEN "CodigoCobroClasificacion" = 'RETENCION' THEN "MontoCobro" ELSE 0 END) AS "TotalRetencion",
	    MAX("IdMonedaCobro") AS "IdMonedaCobro"
	 FROM (SELECT CmbteCobro."IdComprobante",
		Cobro."IdCobroClasificacion", 
		CobroClasificacion."CodigoCobroClasificacion",
		IFNULL(SUM(CmbteCobro."MontoComprobanteCobro"),0) as "MontoCobro",
		CmbteCobro."IdMonedaCobro"
	  FROM "BSCL.Facturacion.BD::FAC.ComprobanteCobro" CmbteCobro
		  LEFT OUTER JOIN "BSCL.Plataforma.BD::COM.Cobro" As Cobro On Cobro."IdCobro" = CmbteCobro."IdCobro"
		  LEFT OUTER JOIN "BSCL.Plataforma.BD::COM.CobroClasificacion" As CobroClasificacion On CobroClasificacion."IdCobroClasificacion" = Cobro."IdCobroClasificacion"
		GROUP BY CmbteCobro."IdComprobante", Cobro."IdCobroClasificacion",CobroClasificacion."CodigoCobroClasificacion", CmbteCobro."IdMonedaCobro"
		ORDER BY Cobro."IdCobroClasificacion"
	 )
	 GROUP BY "IdComprobante"
)
SELECT 
	-- INFO COMPROBANTE
	COM."IdComprobante" AS "FolioOperacion",
	TO_VARCHAR(COM."FechaComprobante",'YYYY-MM-DD') AS "FechaOperacion",
	COM."IdTipoComprobante" AS "TipoDocumento",
	CASE Tope."AccionOperacion" WHEN 'COMPRA' THEN 'C' ELSE 'V' END AS "CompraVenta",
	-- INFO CLIENTE
	POR."CodigoPortafolio" AS "Portafolio",
	CASE "Tipo" WHEN 'J' THEN PER."RazonSocial" ELSE PER."Nombres" || ' ' || PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno" END AS "Nombre",
	DOC."DocumentoIdentidad",
	-- INFO INSTRUMENTO
	COM."IdTipoInstrumento",
	-- MONTO
	ABS(COALESCE(COM."MontoComprobante"       , 0)) AS "MontoOperado",
	ABS(COALESCE(COM."MontoComprobanteFinal"  , 0)) AS "MontoTotal",
	ABS(COALESCE(COM."MontoComprobanteCobro"  , 0)) AS "MontoCobro",
	ABS(COALESCE(CBR."TotalComision"          , 0)) AS "TotalComision",
	ABS(COALESCE(CBR."TotalDerecho"           , 0)) AS "TotalDerecho",
	ABS(COALESCE(CBR."TotalGasto"             , 0)) AS "TotalGasto",
	ABS(COALESCE(CBR."TotalImpto"             , 0)) AS "TotalImpto",
	ABS(COALESCE(CBR."TotalRetencion"         , 0)) AS "TotalRetencion",
	Tope."IdTipoOperacion",
	COM."IdClasificacionTipoInstrumento",
	com."IdMoneda"
FROM "BSCL.Facturacion.BD::FAC.Comprobante" COM
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" Tope ON Tope."IdTipoOperacion" = COM."IdTipoOperacion"
	INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = COM."IdCliente"
	INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = COM."IdPortafolio"
	INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPersona" = CLI."IdPersona"
	INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC ON doc."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
	INNER JOIN "BSCL.Plataforma.BD::IND.Indicador" IND ON PER."IdPersona"  = ind."IdEntidad" 
	INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TIND ON TIND."IdTipoIndicador" = IND."IdTipoIndicador" 
	LEFT JOIN CobrosComprobantes CBR ON CBR."IdComprobante" = COM."IdComprobante"
WHERE tind."CodigoTipoIndicador" = 'RELCORRPER' 
	AND ind."ValorIndicador" = 'S'
	AND (COM."IdClasificacionTipoInstrumento" IN ('RV','RF', 'IF') OR COM."IdClasificacionTipoInstrumento" IS null)
	AND com."IdEstadoComprobante" != 'ANU'
	AND TO_VARCHAR(COM."FechaComprobante",'YYYY-MM-DD') BETWEEN :FechaInicio AND :FechaFin
ORDER BY com."IdComprobante" desc;