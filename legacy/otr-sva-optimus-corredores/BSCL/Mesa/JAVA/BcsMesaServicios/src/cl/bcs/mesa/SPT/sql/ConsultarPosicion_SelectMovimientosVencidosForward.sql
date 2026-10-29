SELECT
FWD."IdTipoOperacion"
,FWD."IdMonedaPrincipal"
,FWD."IdMonedaSecundaria"
,FWD."Nemotecnico"
,FWD."Modalidad"
,FWD."Estado"
,PAR."ValorParametro"
,FWD."MontoMonedaPrincipal"
,FWD."MontoMonedaSecundaria"
FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWD" AS FWD
INNER JOIN "BSCL.Mesa.BD::MES.Parametro" AS PAR ON PAR."IdParametro" = FWD."Modalidad"
WHERE "FechaVencimiento"  = :FechaOperacion
AND (FWD."Nemotecnico" = 'ARBITR' OR (FWD."Nemotecnico" = 'SECANO' AND (FWD."IdMonedaPrincipal" = :IdMoneda OR FWD."IdMonedaSecundaria" = :IdMoneda)))
AND (
(PAR."ValorParametro" = 'ENTREGA_FISICA' AND (FWD."Estado" = 'VIG' OR FWD."Estado" = 'VCD') AND :FechaSistema != :FechaOperacion)
OR (PAR."ValorParametro" = 'ENTREGA_FISICA' AND FWD."Estado" = 'VIG' AND :FechaSistema = :FechaOperacion)
)
;