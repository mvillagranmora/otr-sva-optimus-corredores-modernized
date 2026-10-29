SELECT
    FW."IdCliente"					 	as "IdCliente",
    FW."IdPortafolio"					as "IdPortafolio",
    CASE FW."Accion" WHEN 'COMPRA' THEN 'V' ELSE 'C' END AS "tipo"  ,
    TO_INTEGER(FW."Plazo")	AS "plazo"  ,
    TO_VARCHAR(FW."FechaOperacion", 'DD-MM-YYYY')	AS "emision"  ,
 	TO_VARCHAR(FW."FechaVencimiento", 'DD-MM-YYYY') AS "vencimiento"  ,
    TO_INTEGER(FW."PlazoResidual") AS "saldoPlazo"  ,
    CASE FW."Nemotecnico" WHEN 'ARBITR' THEN FW."ParidadCierre" ELSE FW."TipoCambioCierre" END AS "precioCierre"  ,
    TO_VARCHAR(FW."FechaOperacion", 'DD-MM-YYYY') AS "Fecha"  ,
    TO_INTEGER(FW."IdOperacion") 	AS "folioOper"  ,
    FW."NombreInstrumento" AS "Operacion"  ,
    FW."MontoMonedaPrincipal" 			AS "monto"  ,
    FW."IdMonedaPrincipal"  || '/' || FW."IdMonedaSecundaria" AS "moneda"  ,
    FW."MontoValorRazonableActivo" - FW."MontoValorRazonablePasivo" AS "montoPeso"  ,
    FW."MontoValorRazonableActivo" AS "valorRazonableActivo"  ,
    FW."MontoValorRazonablePasivo" AS "valorRazonablePasivo"  ,
    FW."ModalidadLiquidacion" 			AS "liquidacion"  ,
    FW."IdMonedaPrincipal"  || '/' || FW."IdMonedaSecundaria" AS "OperacionMoneda"  ,
    TO_VARCHAR(FW."FechaVencimiento", 'DD-MM-YYYY') 	AS "WarningFechavalorRazonable"  ,
    TO_VARCHAR(FW."FechaOperacion", 'DD-MM-YYYY')		AS "FechavalorRazonable" ,
    FW."IdMonedaPrincipal" AS "monedaRecibe" ,
    FW."IdMonedaSecundaria" AS "monedaEntrega",
    MND."IdTipoMoneda",
    FW."Nemotecnico",
    FW."IdInstrumento", --GCMBIMECAR-1 Mejoramiento de Cartolas
    FW."MontoMonedaSecundaria", --GCMBIMECAR-1 Mejoramiento de Cartolas
    FW."TipoCambioCierre",  --GCMBIMECAR-1 Mejoramiento de Cartolas
    FW."TipoCambioCosto" --GCMBIMECAR-1 Mejoramiento de Cartolas
FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWD" FW
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON FW."IdPortafolio" = P."IdPortafolio"
INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" MND ON MND."IdMoneda" = FW."IdMonedaPrincipal"
WHERE FW."IdEstado" = 'VIG'
AND P."IdTrabajo" = :IdTrabajo
ORDER BY "IdCliente","IdPortafolio","OperacionMoneda"
