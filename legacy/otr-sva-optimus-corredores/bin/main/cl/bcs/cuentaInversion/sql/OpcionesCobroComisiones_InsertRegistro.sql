INSERT INTO "BSCL.CuentaInversion.BD::CTA.CobroComisionOpcionesCobro" (
 "IdOpcionCobro"
,"OpcionCobro"
,"PortafolioAPV"
,"IdClasificacionTipoInstrumento"
,"PorcentajeCobro"
,"IdEstado"
,"FechaVigencia"
,"FechaIngreso"
,"FechaModificacion"
)
VALUES (
 "BSCL.CuentaInversion.BD.Secuencias::CobroComisionOpcionesCobro".NEXTVAL
,:OpcionCobro
,:PortafolioAPV
,:Mercado
,:PorcentajeCobro
,:Estado
,:FechaVigencia
,:FechaIngreso
,'0001-01-01 00:00:00'
);