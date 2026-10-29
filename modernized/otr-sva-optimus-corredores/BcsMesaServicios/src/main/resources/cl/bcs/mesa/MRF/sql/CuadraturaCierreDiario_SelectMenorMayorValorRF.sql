SELECT
 CAR."IdOperacion"
,CAR."FolioCartera" AS "FolioOperacion"
,CLI."NombreCompleto" AS      "RazonSocial"
,CAR."MontoCompra" AS "MontoOperacionMoneda"
,CAR."IdTipoReajuste" AS  "IdMoneda"
,CAR."IdMonedaInstrumento" AS "IdMonedaLiquidacion"
,TO_BIGINT (0) AS "IdMesaOrigen"
,CAR."IdEstado"
,'' AS "Descripcion"
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR 
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafoliosCuentaPropia" AS CP ON CP."IdCliente" = CAR."IdCliente" AND CP."IdEstadoEmpresa"='ACT' AND CP."EsPortafolioDefecto"='S'
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI ON CLI."IdCliente" = CAR."IdCliente"
WHERE CAR."IdEstado" = 'VIG'
AND   CAR."IdClasificacionTipoInstrumento"IN('RF','IF')
AND   CAR."IdTipoInstrumento" != 'PAC'
AND   CAR."IdEmpresa" = :IdEmpresa
