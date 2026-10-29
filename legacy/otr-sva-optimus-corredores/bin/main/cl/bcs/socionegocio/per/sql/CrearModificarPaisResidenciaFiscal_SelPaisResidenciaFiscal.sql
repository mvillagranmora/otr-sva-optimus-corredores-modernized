SELECT		
	PRF."IdPaisResidenciaFiscal",
	PER."IdPersona",
	PAI."IdPais",
	PRF."FechaInicioTributacionPais",
	PRF."IdEstado",
	PRF."IdEmpresa",
	-- PRF."FechaCreacion",
	-- PRF."FechaModificacion",
	PRF."IdUsuarioCreador",
	PRF."IdUsuarioModificador"
FROM "BSCL.SocioNegocio.BD::PER.PaisResidenciaFiscal" AS PRF
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = PRF."IdPersona")
LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" AS PAI ON (PAI."IdPais" = PRF."IdPais")
WHERE PRF."IdEmpresa" = :IdEmpresa AND  PRF."IdPaisResidenciaFiscal" IN (:PaisResidenciaFiscal);