
SELECT 
 OP."IdOperacion"
,RS."FolioCarteraOrigen" AS "FolioOperacion"
,CLI."NombreCompleto" AS      "RazonSocial"
,OP."MontoOperacionMoneda"
,OP."IdMoneda"
,OP."IdMonedaLiquidacion"
,OP."IdMesaOrigen"
,OP."IdEstado"
,'' AS "Descripcion"
FROM "BSCL.Mesa.BD::MES.Operacion" AS OP
INNER JOIN  "BSCL.Custodia.BD::CUS.Reserva" AS RS ON OP."IdOperacion" = RS."IdOperacion"
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON CAR."IdOperacion" = RS."IdReserva"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI ON CLI."IdCliente" = OP."IdCliente"
WHERE OP."IdEstado" = 'VIG'
AND   OP."IdTipoOperacion"='VENTER'
AND   CAR."IdEmpresa"=:IdEmpresa
AND   OP."FechaOperacion"=:FechaActual

