SELECT 
FW."IdCliente"
,FW."IdPortafolio"
,CASE FW."Accion" WHEN 'COMPRA' THEN 'V' ELSE 'C' END AS "Tipo"
,FW."IdOperacion" AS "Folio"
,FW."NombreInstrumento" AS "Operacion"
,FW."Nemotecnico"
,FW."IdMonedaPrincipal"  || '/' || FW."IdMonedaSecundaria" AS "Moneda"
,CASE FW."Accion" WHEN 'COMPRA' THEN FW."MontoMonedaPrincipal" ELSE TO_DECIMAL(0,26,8) END AS "MontoVenta"
,CASE FW."Accion" WHEN 'VENTA' THEN FW."MontoMonedaPrincipal" ELSE TO_DECIMAL(0,26,8) END AS "MontoCompra"
,CASE FW."Nemotecnico" WHEN 'ARBITR' THEN FW."ParidadCierre" ELSE FW."TipoCambioCierre" END AS "TipoCambioCierre"
,FW."IdMonedaPrincipal" AS "MonedaRecibe"
,FW."IdMonedaSecundaria" AS "MonedaEntrega"
,MND."IdTipoMoneda"
FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWD" FW
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON FW."IdPortafolio" = P."IdPortafolio"
INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" MND ON MND."IdMoneda" = FW."IdMonedaPrincipal"
WHERE FW."IdEstado" = 'VIG'