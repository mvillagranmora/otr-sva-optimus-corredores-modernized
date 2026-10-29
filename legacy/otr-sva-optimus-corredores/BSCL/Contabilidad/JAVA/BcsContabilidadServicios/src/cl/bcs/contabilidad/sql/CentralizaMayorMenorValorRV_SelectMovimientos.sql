SELECT
    TO_BIGINT(:p_IdTipoCentralizacion)              AS "IdTipoCentralizacion",
    CVIFRF."IdValorizacion"                       	AS "IdMovimiento",
    'MAMEVALRV'                                     AS "IdTipoMovimiento",
    CVIFRF."IdEmpresa"                        		AS "IdEmpresa",                                     
    CVIFRF."FolioCartera"                     		AS "Numero",
    CVIFRF."FolioCartera"                     		AS "Linea",
    TO_NVARCHAR(:p_FechaContable, 'YYYY-MM-DD') 	AS "Fecha",
    DI."DocumentoIdentidad"							AS "DocumentoIdentidad",
    CVIFRF."IdPortafolio"							AS "IdPortafolio",
    TO_BIGINT(YEAR(CVIFRF."FechaOperacion"))        AS "Ejercicio",
    TO_NVARCHAR(:p_FechaContable, 'MM-YYYY') 		AS "Periodo",
    ABS(CVIFRF."MontoActual")   					AS "Monto",
    CONF."Valor" 									AS "IdMoneda",
    ''                         						AS "Estado"
FROM "BSCL.Custodia.BD::VAL.CarteraValorizadaIFRF" CVIFRF
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = CVIFRF."IdCliente" 
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI ON DI."IdPersona" = CLI."IdPersona" 
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" CONF ON CONF."CodigoParametro" = 'PLA.MonedaSistema'
WHERE CVIFRF."IdEmpresa" = :p_IdEmpresa 
    AND "IdInforme" = :p_IdInforme
