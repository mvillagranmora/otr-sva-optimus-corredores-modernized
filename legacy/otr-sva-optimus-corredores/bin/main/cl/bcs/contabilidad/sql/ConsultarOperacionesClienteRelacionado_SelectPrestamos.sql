SELECT 
	CAR."FolioCartera",
	TO_VARCHAR(CAR."FechaOperacion", 'YYYY-MM-DD') AS "FechaOperacion",
	CAR."IdTipoComprobante" AS "TipoDocumento",
	CASE Tope."AccionOperacion" WHEN 'COMPRA' THEN 'C' ELSE 'V' END AS "CompraVenta",
	-- INFO CLIENTE
	POR."CodigoPortafolio" AS "Portafolio",
	CASE "Tipo" WHEN 'J' THEN PER."RazonSocial" ELSE PER."Nombres" || ' ' || PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno" END AS "Nombre",
	DOC."DocumentoIdentidad",
	-- INFO INSTRUMENTO
	CAR."IdTipoInstrumento",
	-- MONTO
	CAR."IdClasificacionTipoInstrumento",
	CAR."MontoCompra" AS "MontoOperado"
FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" Tope ON Tope."IdTipoOperacion" = CAR."IdTipoOperacion"
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = CAR."IdCliente"
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = CAR."IdPortafolio"
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPersona" = CLI."IdPersona"
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC ON doc."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
INNER JOIN "BSCL.Plataforma.BD::IND.Indicador" IND ON PER."IdPersona"  = ind."IdEntidad" 
INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TIND ON TIND."IdTipoIndicador" = IND."IdTipoIndicador"
WHERE CAR."IdTipoInstrumento" IN ('PRT', 'PRF', 'EMP', 'ERF') AND TIND."CodigoTipoIndicador" = 'RELCORRPER'
AND IND."ValorIndicador" = 'S' AND car."IdEstado" IN ('VIG', 'LIQ', 'VCD')
AND TO_VARCHAR(CAR."FechaOperacion",'YYYY-MM-DD') BETWEEN :FechaInicio AND :FechaFin