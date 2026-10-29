SELECT
FW."IdCliente"
,FW."IdPortafolio"
,CASE FW."Accion" WHEN 'COMPRA' THEN 'VENTA FORWARD' ELSE 'COMPRA FORWARD' END AS "tipoOper"
,CASE FW."Accion" WHEN 'COMPRA' THEN 'V' ELSE 'C' END AS "tipo"
,TO_DECIMAL(FW."IdOperacion",26,0)  AS "SEC_OPER"
,FW."Glosa" AS "GLOSA" 
,FW."ModalidadLiquidacion" AS "TIPO_LIQUID"
,TO_VARCHAR(FW."FechaOperacion", 'YYYY-MM-DD') AS "FEC_OPER"
,TO_VARCHAR(FW."FechaVencimiento", 'YYYY-MM-DD') AS "FEC_VCTO"
,FW."IdMonedaPrincipal" || '/' || "IdMonedaSecundaria" AS "moneda"
,CASE FW."Nemotecnico" WHEN 'ARBITR' THEN FW."ParidadCierre" ELSE FW."TipoCambioCierre" END AS "PRECIO_PESOS"
,FW."IdMonedaPrincipal" AS "monedaRecibe"
,FW."MontoMonedaPrincipal" AS "montoRecibe"
,FW."IdMonedaSecundaria" AS "monedaEntrega"
,FW."MontoMonedaSecundaria" AS "montoEntrega"
,FW."Margen" AS "rentabilidad"
,MND."IdTipoMoneda"
,FW."Nemotecnico"
FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWDHistorico" FW
INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" MND ON MND."IdMoneda" = FW."IdMonedaPrincipal"
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" AS PORTAFOLIO_PASO ON (FW."IdPortafolio" = PORTAFOLIO_PASO."IdPortafolio")
WHERE PORTAFOLIO_PASO."IdTrabajo" = :IdTrabajo
AND FW."FechaRespaldo" = :FechaHasta
AND FW."IdEstado" = 'VIG'