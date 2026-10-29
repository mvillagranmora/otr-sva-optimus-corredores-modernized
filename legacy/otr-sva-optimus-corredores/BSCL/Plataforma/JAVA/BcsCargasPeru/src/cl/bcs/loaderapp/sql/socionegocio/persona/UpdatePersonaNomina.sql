UPDATE PER
SET 
	 PER."IdPaisNacionalidadPrincipal" = :PaisNacionalidadPrincipal
	,PER."IdPaisNacionalidadSecundaria" = :PaisNacionalidadSecundaria
	,PER."IdPaisResidenciaSecundaria" = :PaisResidenciaSecundaria
	,PER."IdProfesion" = :Profesion
	,PER."IdGradoInstruccion" = :GradoInstruccion
	,PER."IdGiroCIIU" = :CodigoGiro
	,PER."IdGiroPrincipal" = :CodigoGiro
    ,PER."FechaDefuncion" = TO_DATE(:FechaDefuncion,'DD-MM-YYYY')
    ,PER."CodigoTipoCargoEmpleado" = IFNULL(:TipoCargoPersona,'')
    ,PER."AgnosServicio" = :AniosServicio
    ,PER."IngresoPromedioMensual" = :IngresoPromedioMensual
    ,PER."IdRangoPatrimonio" = :IdRangoPatrimonio
    ,PER."EsExtranjero" = :EsExtranjero
    ,PER."IdDocumentoIdentidadAdicional" = DOCAD."IdDocumentoIdentidad"
    ,PER."IdCarga" = 166--DEBUG PE
FROM 
    "BSCL.SocioNegocio.BD::PER.Persona" PER, 
    "BSCL.SocioNegocio.BD::CLI.Cliente" CLI,
    (
        SELECT DI."IdDocumentoIdentidad"
        FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI
        LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CI ON CI."IdCliente" = :IdCliente
        LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PE ON PE."IdPersona" = CI."IdPersona"
        WHERE DI."IdPersona" = CI."IdPersona" AND PE."IdDocumentoIdentidad" <> DI."IdDocumentoIdentidad"
    ) DOCAD
where PER."IdPersona" = CLI."IdPersona" AND CLI."IdCliente" = :IdCliente;
