-- Raw CarteraValorizadaIFRF rows behind the legacy movement query (same FROM / JOIN / WHERE as
-- the legacy _SelectMovimientos.sql of this type); the CASE, ABS, YEAR, TO_CHAR and the type's row
-- filter are applied by DevengoCalculator.
SELECT
	CVIFRF."IdValorizacion"
	,CVIFRF."IdCartera"
	,CVIFRF."IdEmpresa"
	,CVIFRF."IdTipoOperacion"
	,CVIFRF."IdClasificacionTipoInstrumento"
	,CVIFRF."FolioOperacion"
	,CVIFRF."FolioCartera"
	,DI."DocumentoIdentidad"
	,CVIFRF."IdPortafolio"
	,CVIFRF."FechaOperacion"
	,CVIFRF."IdMonedaInstrumento"
	,CONF."Valor" AS "IdMonedaSistema"
	,CVIFRF."MontoCompra"
	,CVIFRF."MontoPactado"
	,CVIFRF."MontoActual"
	,CVIFRF."MontoProximo"
	,CVIFRF."MontoInteres"
	,CVIFRF."MontoInteresCorrido"
	,CVIFRF."MontoReajuste"
FROM "BSCL.Custodia.BD::VAL.CarteraValorizadaIFRF" CVIFRF
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = CVIFRF."IdCliente"
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI ON DI."IdPersona" = CLI."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" CONF ON CONF."CodigoParametro" = 'PLA.MonedaSistema'
WHERE CVIFRF."IdInforme" = :p_IdInforme
AND CVIFRF."IdEmpresa" = :p_IdEmpresa
