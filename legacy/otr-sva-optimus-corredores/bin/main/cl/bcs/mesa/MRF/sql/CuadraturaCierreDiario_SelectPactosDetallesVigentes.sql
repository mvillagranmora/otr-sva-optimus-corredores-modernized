SELECT 
 OP."IdOperacion"
,CAR."FolioCartera" AS "FolioOperacion"
,CLI."NombreCompleto" AS      "RazonSocial"
,RS."MontoValorizado" AS "MontoOperacionMoneda"
,RS."IdTipoReajuste"  AS "IdMoneda"
,CAR."IdMonedaInstrumento" AS "IdMonedaLiquidacion"
,OP."IdMesaOrigen"
,CAR."IdEstado"
,'' AS "Descripcion"
FROM "BSCL.Mesa.BD::MES.Operacion" AS OP
INNER JOIN  "BSCL.Custodia.BD::CUS.Reserva" AS RS ON OP."IdOperacion" = RS."IdOperacion"
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON CAR."IdOperacion" = RS."IdReserva"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI ON CLI."IdCliente" = OP."IdCliente"
WHERE CAR."IdEstado" = 'VIG'
AND CAR."IdTipoProducto" = 'MERF'
AND OP."IdTipoOperacion" IN ('COMPAC','VENPAC')
AND CAR."IdEmpresa"=:IdEmpresa
