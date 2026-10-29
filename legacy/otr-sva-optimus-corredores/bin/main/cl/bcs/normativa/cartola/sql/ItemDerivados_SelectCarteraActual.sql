SELECT
    FW."IdCliente"
    ,FW."IdPortafolio"
    ,FW."IdOperacion" AS "Folio"
    ,CASE FW."Accion" WHEN 'COMPRA' THEN 'V' ELSE 'C' END AS "Tipo"
    ,'FW' AS "Grupo"
    ,'Forward' AS "Item"
    ,FW."MontoMonedaPrincipal" AS "Monto"
    ,FW."IdMonedaSecundaria" AS "Moneda"
    ,FW."IdMonedaPrincipal" AS "MonedaRecibe"
    ,FW."IdMonedaSecundaria" AS "MonedaEntrega"
	,FW."TipoCambioCierre"
	,FW."MontoValorRazonableActivo" AS "ValorRazonableActivo"
	,FW."MontoValorRazonablePasivo" AS "ValorRazonablePasivo"
FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWD" FW
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON FW."IdPortafolio" = P."IdPortafolio"
WHERE FW."IdEstado" = 'VIG'