
SELECT 
 "IdBonificacion"
,"AnoAhorro"
,"AnoProceso"
,"MontoPagadoNeto"
,"MontoSaldoNeto"
,"MontoPagadoReajustado"
,"IdPersona"
,"IdCliente"
,"MontoBonificacionAfiliadoNeto"
,CASE WHEN "PA1" > 0 THEN 'PA1'
      WHEN "PA2" > 0 THEN 'PA2'
      WHEN "PA3" > 0 THEN 'PA3'
 END AS "TipoAhorro"
,"MontoAfiliadoNeto"
,"MontoReajustadoPagadoAfiliado"
,"IdEstado"
FROM "BSCL.APV.BD.Vistas::INV.ConsultarBonificacion"
WHERE "IdBonificacion" in (:IdsBonificacion);
