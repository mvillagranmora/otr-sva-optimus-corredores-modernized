SELECT
L."Idlog",
L."CodigoFormulario",
L."CampoBackend",
L."IdEmpresa",
L."IdCliente",
L."IdPersona",
L."IdUsuario",
L."FechaSistema",
L."IdPrograma",
L."Estado",
L."Mensaje"
FROM "BSCL.SocioNegocio.BD::CLI.LogGenerarContratos" AS L
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cli ON (Cli."IdCliente" = L."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Per ON (L."IdPersona" = Per."IdPersona")
WHERE L."Estado" = 'PEN'
AND (
    :IdCliente = 0
    OR (
        :IdCliente != 0
        AND (
            Cli."IdCliente" = :IdCliente
            OR Per."IdCliente" = :IdCliente
        )
    )
);