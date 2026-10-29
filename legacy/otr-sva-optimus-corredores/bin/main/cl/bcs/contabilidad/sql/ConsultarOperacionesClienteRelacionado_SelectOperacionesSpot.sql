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
	CASE COM."IdTipoOperacion" WHEN 'ANCSPT' THEN  0 ELSE COM."Monto" END  AS "MontoCompra",
	CASE COM."IdTipoOperacion" WHEN 'ANVSPT' THEN 0 ELSE COM."Monto" END AS "MontoVenta",
	Tope."IdTipoOperacion"
FROM "BSCL.Facturacion.BD::FAC.Comprobante" COM
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TOPE ON TOPE."IdTipoOperacion" = COM."IdTipoOperacion" 
	INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = COM."IdCliente"
	INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = COM."IdPortafolio"
	INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPersona" = CLI."IdPersona"
	INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC ON doc."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
	INNER JOIN "BSCL.Plataforma.BD::IND.Indicador" IND ON PER."IdPersona"  = ind."IdEntidad" 
	INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TIND ON TIND."IdTipoIndicador" = IND."IdTipoIndicador" 
WHERE tind."CodigoTipoIndicador" = 'RELCORRPER' 
	AND ind."ValorIndicador" = 'S'
	AND COM."IdClasificacionTipoInstrumento" = 'SP'
	AND com."IdEstadoComprobante" != 'ANU'
	AND TO_VARCHAR(COM."FechaComprobante",'YYYY-MM-DD') BETWEEN :FechaInicio AND :FechaFin
ORDER BY com."IdComprobante" desc;