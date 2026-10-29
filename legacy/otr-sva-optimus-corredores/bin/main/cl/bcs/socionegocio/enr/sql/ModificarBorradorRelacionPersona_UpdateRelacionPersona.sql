UPDATE "BSCL.SocioNegocio.BD::ENR.RelacionPersona"
SET "IdTipoRelacion" = :IdTipoRelacion
,"IdPersonaDestino" = :IdPersonaDestino
,"FechaInicioRelacion" = :FechaInicioRelacion
,"FechaFinRelacion" = :FechaFinRelacion
,"PorcentajeParticipacion" = :PorcentajeParticipacion
,"RetiraDocumentos" = :RetiraDocumentos
,"FinalidadRelacion" = :FinalidadRelacion
,"CondicionesRelacion" = :CondicionesRelacion
,"IdGradoVinculacionParental" = :IdGradoVinculacionParental
,"IdCiudad" = :IdCiudad
,"Calle" = :Calle
,"Numero" = :Numero
,"Email" = :Email
,"Telefono" = :Telefono
,"Complemento" = :Complemento
,"Ciudad" = :Ciudad
WHERE "IdRelacionPersona" = :IdRelacionPersona