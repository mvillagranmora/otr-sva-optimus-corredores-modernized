SELECT TOP 1
	SOC."IdSocioDeNegocio",
	SOC."IdPersona",
	SOC."IdEmpleado",
	SOC."IdCliente"
FROM "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" as SOC
	LEFT JOIN (	SELECT DISTINCT "IdPersonaDestino" FROM "BSCL.SocioNegocio.BD::REL.RelacionPersona" rp
					INNER JOIN "BSCL.SocioNegocio.BD::REL.TipoRelacion" tp ON (rp."IdTipoRelacion" = tp."IdTipoRelacion" AND "AccesoOptimusCliente"='S')
				) AS rel ON  rel."IdPersonaDestino"= SOC."IdPersona"
WHERE "IdEmpresa" = :IdEmpresa AND (("IdCliente" = :IdEntidad AND :TipoEntidad = 'CLI') OR ("IdEmpleado" = :IdEntidad AND :TipoEntidad = 'EMP') OR ("IdPersonaDestino" = :IdEntidad AND :TipoEntidad = 'PER') );