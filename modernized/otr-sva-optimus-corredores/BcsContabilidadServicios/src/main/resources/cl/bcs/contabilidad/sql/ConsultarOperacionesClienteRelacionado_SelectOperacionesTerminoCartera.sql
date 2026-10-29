SELECT 	
--	 INFO COMPROBANTE
	OPE."FolioOperacion" AS "FolioOperacion",
	TO_VARCHAR(OPE."FechaOperacion",'YYYY-MM-DD') AS "FechaOperacion",
	OPE."IdTipoOperacion" AS "TipoDocumento",
	CASE Tope."AccionOperacion" WHEN 'COMPRA' THEN 'C' ELSE 'V' END AS "CompraVenta",
--	 INFO CLIENTE
	POR."CodigoPortafolio" AS "Portafolio",
	CASE "Tipo" WHEN 'J' THEN PER."RazonSocial" ELSE PER."Nombres" || ' ' || PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno" END AS "Nombre",
	DOC."DocumentoIdentidad",
--	 INFO INSTRUMENTO
	res."IdTipoInstrumento",
--	 MONTO
	res."MontoValorizado" AS "MontoInicial",
	res."MontoValorizado" AS "MontoFinal",
	TO_VARCHAR(ope."FechaCondicionLiquidacion",'YYYY-MM-DD') AS "FechaInicio",
	TO_VARCHAR(ope."FechaVencimiento",'YYYY-MM-DD') AS "FechFin",
	OPE."IdTipoOperacion"
FROM "BSCL.Mesa.BD::MES.Operacion" OPE
INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" Tope ON Tope."IdTipoOperacion" = OPE."IdTipoOperacion"
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = OPE."IdCliente"
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = OPE."IdPortafolio"
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPersona" = CLI."IdPersona"
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC ON doc."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
INNER JOIN "BSCL.Plataforma.BD::IND.Indicador" IND ON PER."IdPersona"  = ind."IdEntidad" 
INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TIND ON TIND."IdTipoIndicador" = IND."IdTipoIndicador" 
LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" res ON (OPE."IdOperacion" = res."IdOperacion")
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" carr ON (res."IdCarteraOrigen" = carr."IdCartera") 
WHERE tind."CodigoTipoIndicador" = 'RELCORRPER' 
	AND ind."ValorIndicador" = 'S'
	AND OPE."IdTipoOperacion" IN ('COMTER', 'VENTER')
	AND res."IdClasificacionTipoInstrumento" IN ('RF', 'IF')
	AND TO_VARCHAR(ope."FechaOperacion",'YYYY-MM-DD') BETWEEN :FechaInicio AND :FechaFin
ORDER BY OPE."FechaOperacion" DESC
