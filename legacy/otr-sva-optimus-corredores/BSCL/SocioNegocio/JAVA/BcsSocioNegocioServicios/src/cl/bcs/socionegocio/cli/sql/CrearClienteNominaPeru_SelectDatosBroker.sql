SELECT TOP 1 
 	DF."Calle" AS "CALLE",
    DF."Numero" AS "NUMERO",
    DF."IdDivisionPais" AS "DIVISIONPAIS",
    DF."IdCiudad" AS "CIUDAD",
    DE."Direccion" AS "DIRECCIONELECTRONICA",
    TE."IdTipoTelefono" AS "TIPOTELEFONO",
    TE."Numero" AS "TELEFONO",
    BE."Codigo" AS "BROKER" 
FROM "BSCL.Plataforma.BD::PLA.BrokerExtranjero" BE 
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" DF ON DF."IdPersona" = :IdPersona
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" DE ON DE."IdPersona" = :IdPersona
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Telefono" TE ON TE."IdPersona" = :IdPersona
ORDER BY DF."Predeterminado" DESC
