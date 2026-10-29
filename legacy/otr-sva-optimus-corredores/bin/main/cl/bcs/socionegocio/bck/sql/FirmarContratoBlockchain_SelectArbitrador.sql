SELECT  REL."IdTipoRelacion",
		CASE PER2."Tipo" 
            WHEN 'N' THEN concat(concat(concat(concat(PER2."Nombres",' '),PER2."ApellidoPaterno"),' '), PER2."ApellidoMaterno")
            ELSE PER2."RazonSocial"
        END  as "NombreCompleto"
FROM "BSCL.SocioNegocio.BD::PER.Persona" AS PER
INNER JOIN "BSCL.SocioNegocio.BD::REL.RelacionPersona" REL ON REL."IdPersonaOrigen" = PER."IdPersona" AND REL."IdEstado" = 'ACT'
INNER JOIN "BSCL.SocioNegocio.BD::REL.TipoRelacion" TREL ON TREL."IdTipoRelacion" =  REL."IdTipoRelacion" AND TREL."IdEstado" = 'ACT'
INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC ON SOC."IdPersona" = REL."IdPersonaOrigen" AND SOC."IdEmpresa" = :p_IdEmpresa
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER2 ON PER2."IdPersona" = REL."IdPersonaDestino" AND PER2."IdEstado" = 'ACT'
INNER JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP ON EMP."IdPersona" = PER2."IdPersona" AND EMP."IdEstado" = 'ACT'
INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC2 ON SOC2."IdPersona" = REL."IdPersonaDestino" AND SOC2."IdEmpresa" = :p_IdEmpresa
WHERE REL."IdTipoRelacion" in (:p_IdTipoRelacion)
AND PER."IdPersona" = :p_IdPersona