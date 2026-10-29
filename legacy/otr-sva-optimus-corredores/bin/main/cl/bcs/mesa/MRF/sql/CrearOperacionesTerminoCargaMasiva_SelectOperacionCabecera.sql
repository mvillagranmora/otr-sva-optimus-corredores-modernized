SELECT
DETALLE."IdCargaMasivaDetalle"
,PORTAFOLIO."IdCliente"
,DETALLE."IdPortafolio"
,DETALLE."TipoOperacion"
,CASE DETALLE."TipoOperacion" WHEN 'CT' THEN 'COMTER'
WHEN 'VT' THEN 'VENTER'
END AS "TipoOperacionReal"
,DETALLE."TipoReajuste"
,DETALLE."ValorInicial"
,DETALLE."CondicionLiquidacion"
,CABECERA."IdMesa"
,DETALLE."FolioAgrupadorCartera"
,MESA."IdCliente" AS "IdClienteMesa"
,MESA."IdPortafolio" AS "IdPortafolioMesa"
,CLIENTE."IdPersonaEjecutivo"
,DETALLE."MonedaLiquidacion"
,COALESCE(CONFIGURACION_PORTAFOLIO_MESA."ValorIndicador", 'N') AS "AfectaCustodia"
FROM "BSCL.Mesa.BD::CAR.CargaMasivaDetalle" AS DETALLE 
INNER JOIN "BSCL.Mesa.BD::CAR.CargaMasiva" AS CABECERA ON CABECERA."IdCargaMasiva" = DETALLE."IdCargaMasiva"
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO ON PORTAFOLIO."IdPortafolio" = DETALLE."IdPortafolio"
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLIENTE ON CLIENTE."IdCliente" = PORTAFOLIO."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS CONFIGURACION_PORTAFOLIO_MESA ON CONFIGURACION_PORTAFOLIO_MESA."CodigoTipoIndicador" = 'MES_CUSTRF' AND CONFIGURACION_PORTAFOLIO_MESA."IdPortafolio" = PORTAFOLIO."IdPortafolio"
LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" AS MESA ON MESA."IdMesa" = CABECERA."IdMesa"
WHERE DETALLE."IdCargaMasivaDetalle" IN (:IdCargaMasivaDetalle) AND DETALLE."TipoRegistro" = 'C' AND DETALLE."IdEstado" = 'ING';