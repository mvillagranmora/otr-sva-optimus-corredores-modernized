UPDATE "BSCL.CuentaInversion.BD::CTA.CobroComisionOpcionesCobro" SET
 "OpcionCobro" =:OpcionCobro
,"PortafolioAPV" =:PortafolioAPV
,"IdClasificacionTipoInstrumento" = :Mercado
,"PorcentajeCobro" =:PorcentajeCobro
,"IdEstado" =:Estado
,"FechaVigencia" =:FechaVigencia
,"FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE
"IdOpcionCobro" =:IdOpcionCobro;