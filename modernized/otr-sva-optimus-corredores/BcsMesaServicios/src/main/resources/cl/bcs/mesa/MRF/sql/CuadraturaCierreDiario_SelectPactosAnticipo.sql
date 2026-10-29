
SELECT 
 OP."IdOperacion"
,OP."FolioOperacion"
,CLI."NombreCompleto" AS      "RazonSocial"
,OP."MontoOperacionMoneda"
,OP."IdMoneda"
,OP."IdMonedaLiquidacion"
,OP."IdMesaOrigen"
,OP."IdEstado"
,'' AS "Descripcion"
FROM "BSCL.Mesa.BD::MES.Operacion" AS OP
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON CAR."IdOperacion" = OP."IdOperacion"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI ON CLI."IdCliente" = CAR."IdCliente"
WHERE OP."IdEstado" = 'ANT'
AND   CAR."IdTipoInstrumento"='PAC'
AND   CAR."IdEmpresa"=:IdEmpresa
AND   OP."FechaCondicionLiquidacionAnticipo"=:FechaActual
