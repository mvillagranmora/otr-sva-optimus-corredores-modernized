SELECT
    DIR."IdDireccionFisica",
    DIR."IdPersona",
    DIR."CodigoProposito" AS "IdProposito",
    DIR."Sector" ,
    DIR."CodigoPostal",
    DIR."Calle" ,
    DIR."Numero" ,
    DIR."Departamento" ,
    DIR."Comentario" ,
    DIR."IdCiudad" ,
    DIR."IdDivisionPais" AS "IdComuna",
    DIR."CodigoCompuesto" AS "CodigoUnicoTerritorial",
    DIR."Predeterminado"
FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DIR
WHERE DIR."IdPersona" IN (:IdPersona);
