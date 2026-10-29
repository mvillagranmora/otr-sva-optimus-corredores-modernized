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
WHERE (FWD."Estado" = 'VIG' OR FWD."Estado" = 'PEL' OR FWD."Estado" = 'PDA')
AND FWD."FechaVencimiento" > :FechaOperacion
AND (FWD."Nemotecnico" = 'ARBITR' OR (FWD."Nemotecnico" = 'SECANO' AND (FWD."IdMonedaPrincipal" = :IdMoneda)))
UNION
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
WHERE FWD."Estado" = 'VCD' 
AND FWD."FechaVencimiento" > :FechaOperacion
AND :FechaSistema != :FechaOperacion
AND (FWD."Nemotecnico" = 'ARBITR' OR (FWD."Nemotecnico" = 'SECANO' AND (FWD."IdMonedaPrincipal" = :IdMoneda)))