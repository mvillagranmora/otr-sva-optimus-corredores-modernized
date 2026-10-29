SELECT 
    COALESCE(DIR."Calle", '')||' '||COALESCE(DIR."Numero", '')||' '||COALESCE(CIU."NombreCiudad", '') AS "DireccionCompleta"
FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
INNER JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" DIR
    ON (PER."IdDireccionFisicaPrincipal" = DIR."IdDireccionFisica")
LEFT JOIN "BSCL.Plataforma.BD::REG.Ciudad" CIU
    ON (COALESCE(DIR."IdCiudad",0) = CIU."IdCiudad")
WHERE PER."IdPersona" = :IdPersona
    AND PER."IdDireccionFisicaPrincipal" IS NOT NULL
    AND PER."IdDireccionFisicaPrincipal" <> 0
