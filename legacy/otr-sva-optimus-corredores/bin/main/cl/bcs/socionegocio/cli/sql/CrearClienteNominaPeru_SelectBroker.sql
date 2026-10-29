SELECT
   	BE."Codigo" AS "BROKER",
    CC."IdPersona" AS "IDPERSONA",
    CPA."CodigoPais" AS "PAISNACIMIENTO",
    CC."CodigoEmpleadoEjecutivo" AS "EJECUTIVO",
    CC."IdCategoriaInversionista" AS "CATINV",
    CP."IdPerfilComercialBaseOrigen" AS "PERCOMER",
    CC."IdTipoCliente" AS "TIPOCLIENTE",
    CC."IdClaseCliente" AS "CLASECLIENTE",
    CC."DocumentoIdentidadAdicional" AS "CODIGORUT",
    CC."IdSegmentoCliente" AS "SEGMENTOCLIENTE",
    TO_NVARCHAR(CC."FechaNacimiento", 'YYYY-MM-DD') AS "FECHANACIMIENTO",
    
	--datos adicionales
    DF."Calle" AS "CALLE",
    DF."Numero" AS "NUMERO",
    DF."IdDivisionPais" AS "DIVISIONPAIS",
    DF."IdCiudad" AS "CIUDAD",
    DE."Direccion" AS "DIRECCIONELECTRONICA",
    TE."IdTipoTelefono" AS "TIPOTELEFONO",
    TE."Numero" AS "TELEFONO",
    CC.*
FROM "BSCL.Plataforma.BD::PLA.BrokerExtranjero" BE 
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CC ON CC."IdPersona" = BE."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::REG.ConsultarPais" CPA ON CPA."IdPais" = CC."IdPaisNacimiento"
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" CP ON CP."IdPortafolio" = CC."IdPortafolioDefecto"

LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" DF ON DF."IdDireccionFisica" = CC."IdDireccionFisicaPrincipal"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" DE ON DE."IdDireccionElectronica" = CC."IdDireccionElectronicaPrincipal"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Telefono" TE ON TE."IdTelefono" = CC."IdTelefonoPrincipal"
WHERE CC."IdEmpresa" = :IdEmpresa; 
