SELECT
	CASE 
		WHEN GASTO."IdPais" IS NULL OR GASTO."AplicarGastoTransferencia" <> 'S' 
			THEN 'N' 
			ELSE 'S' 
		END AS "AplicaGasto",
	CASE 
		WHEN (IMP."IdPais" IS NULL) OR (IMP."IdEstado" <> 'ACT') OR (PER_INS."IdPaisResidencia" = PER_CLI."IdPaisResidencia" AND PER_CLI."Tipo"='J')
			THEN 'N' 
			ELSE 'S' 
		END AS "AplicaImpuesto",
	INS."IdInstrumento",
	CLI."IdCliente"
FROM "BSCL.Plataforma.BD::INS.Instrumento" INS
INNER JOIN "BSCL.Plataforma.BD::MER.Emisor" EMI ON INS."IdEmisor" = EMI."IdEmisor"
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER_INS ON PER_INS."IdPersona" = EMI."IdPersona"
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = :IdCliente
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER_CLI ON CLI."IdPersona" = PER_CLI."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD::PLA.PaisGastoTransferencia" GASTO	ON GASTO."IdPais" = PER_INS."IdPaisResidencia"
LEFT JOIN "BSCL.Plataforma.BD::PLA.PaisImpuestoDividendoExtranjero" IMP ON IMP."IdPais" = PER_INS."IdPaisResidencia"
WHERE INS."IdInstrumento" = :IdInstrumento;