SELECT
	-- INFO COMPROBANTE
	OPE."FolioOperacion" AS "FolioOperacion",
	TO_VARCHAR(OPE."FechaOperacion",'YYYY-MM-DD') AS "FechaOperacion",
	CASE Tope."AccionOperacion" WHEN 'COMPRA' THEN 'C' ELSE 'V' END AS "CompraVenta",
	-- INFO CLIENTE
	POR."CodigoPortafolio" AS "Portafolio",
	CASE "Tipo" WHEN 'J' THEN PER."RazonSocial" ELSE PER."Nombres" || ' ' || PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno" END AS "Nombre",
	DOC."DocumentoIdentidad" AS "DocumentoIdentidad",
	-- INFO INSTRUMENTO
	OPE."IdTipoOperacion",
	OPE."MontoOperacionInicial" AS "MontoInicial",
	OPE."MontoOperacionFinal" AS "MontoFinal",
	TI."IdTipoInstrumento",
	TO_VARCHAR(ope."FechaVencimiento",'YYYY-MM-DD') AS "FechaInicio",
	TO_VARCHAR(ope."FechaVencimiento",'YYYY-MM-DD') AS "FechaTermino",
	ope."IdTipoOperacion",
	CASE WHEN OPE."MontoOperacionInicial" - OPE."MontoOperacionFinal" < 0 THEN OPE."MontoOperacionInicial" - OPE."MontoOperacionFinal" ELSE 0 END as "Perdida",
	CASE WHEN OPE."MontoOperacionInicial" - OPE."MontoOperacionFinal" > 0 THEN OPE."MontoOperacionInicial" - OPE."MontoOperacionFinal" ELSE 0 END as "Ganancia"
FROM "BSCL.Mesa.BD::MES.Operacion" OPE 
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TOPE ON TOPE."IdTipoOperacion" = ope."IdTipoOperacion" 
	INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = OPE."IdPortafolio"
	INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = OPE."IdCliente"
	INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPersona" = CLI."IdPersona"
	INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC ON doc."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
	INNER JOIN "BSCL.Plataforma.BD::IND.Indicador" IND ON PER."IdPersona"  = ind."IdEntidad" 
	INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TIND ON TIND."IdTipoIndicador" = IND."IdTipoIndicador"
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" TI ON OPE."IdInstrumento" = TI."IdInstrumento"
WHERE tind."CodigoTipoIndicador" = 'RELCORRPER' 
	AND ind."ValorIndicador" = 'S' 
	AND TO_VARCHAR(ope."FechaOperacion",'YYYY-MM-DD') BETWEEN :FechaInicio AND :FechaFin
	AND OPE."IdTipoOperacion" IN ('COMTER', 'VENTER')
	AND OPE."IdEstado" IN ('LIQ', 'VIG', 'VCD');