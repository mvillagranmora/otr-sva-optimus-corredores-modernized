SELECT 
    COALESCE(DIREC."Direccion", '') AS "Direccion"
FROM "BSCL.SocioNegocio.BD::PER.DireccionElectronica" DIREC
WHERE DIREC."IdDireccionElectronica" = (
    SELECT "IdDireccionElectronicaPrincipal"
    FROM "BSCL.SocioNegocio.BD::PER.Persona"
    WHERE "IdPersona" = :IdPersona
)
