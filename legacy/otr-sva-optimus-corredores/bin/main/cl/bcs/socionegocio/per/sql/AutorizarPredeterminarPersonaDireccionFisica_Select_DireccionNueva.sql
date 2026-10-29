SELECT 
    COALESCE(DIR."Calle", '')||' '||COALESCE(DIR."Numero", '')||' '||COALESCE(CIU."NombreCiudad", '') AS "DireccionCompleta"
FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica" DIR
LEFT JOIN "BSCL.Plataforma.BD::REG.Ciudad" CIU
    ON (COALESCE(DIR."IdCiudad",0) = CIU."IdCiudad")
WHERE DIR."IdDireccionFisica" = :IdDireccionFisica
