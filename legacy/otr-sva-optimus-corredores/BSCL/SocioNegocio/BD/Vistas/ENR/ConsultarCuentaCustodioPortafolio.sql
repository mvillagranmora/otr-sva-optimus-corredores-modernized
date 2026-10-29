SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarCuentaCustodioPortafolio"; 

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarCuentaCustodioPortafolio" AS 
SELECT  
CCP."IdCuentaCustodioPortafolio"
,CCP."IdEnrolamiento"
,CCP."IdBoveda"
,BOV."Nombre" AS "NombreBoveda"
,BOV."Nombre" || ' (' || CCP."Codigo" || ')' AS "NombreBovedaCodigo"
,CCP."Codigo"
,CCP."LiquidaVencimientos"
,CCP."LiquidaCupones"
,CCP."LiquidaPrepagos"
,CCP."LiquidaDividendos"
,CCP."IdModeloCustodia"
,MODELO."NombreModeloCustodia"
,CCP."IdTipoCustodia"
,TIPOCUSTODIA."NombreTipoCustodia"
,CCP."IdTipoCuentaCustodia"
,TIPOCUENTA."NombreTipoCuentaCustodia"
,CCP."IdPersonaBancoCustodio"
,BANCOCUSTODIO."Codigo" AS "CodigoBancoCustodio"
,CASE PERCUS."Tipo"
    WHEN 'N' THEN PERCUS."Nombres" ||' '|| PERCUS."ApellidoPaterno" || ' ' ||PERCUS."ApellidoMaterno" 
    WHEN 'M' THEN PERCUS."Nombres" ||' '|| PERCUS."ApellidoPaterno" || ' ' ||PERCUS."ApellidoMaterno" 
    ELSE PERCUS."RazonSocial"
END as "NombreCompletoBancoCustodio"
,CCP."IdPersonaBancoLiquidador"
,BANCOLIQ."Codigo" AS "CodigoBancoLiquidador"
,CASE PERLIQ."Tipo"
    WHEN 'N' THEN PERLIQ."Nombres" ||' '|| PERLIQ."ApellidoPaterno" || ' ' ||PERLIQ."ApellidoMaterno" 
    WHEN 'M' THEN PERLIQ."Nombres" ||' '|| PERLIQ."ApellidoPaterno" || ' ' ||PERLIQ."ApellidoMaterno" 
    ELSE PERLIQ."RazonSocial"
END  as "NombreCompletoBancoLiquidador"
,"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(CCP."LiquidaVencimientos") AS "LiquidaVencimientosUI"
,"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(CCP."LiquidaCupones") AS "LiquidaCuponesUI"
,"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(CCP."LiquidaPrepagos") AS "LiquidaPrepagosUI"
,"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(CCP."LiquidaDividendos") AS "LiquidaDividendosUI"
FROM "BSCL.SocioNegocio.BD::ENR.CuentaCustodioPortafolio" AS CCP
INNER JOIN "BSCL.Plataforma.BD::PLA.Boveda" AS BOV ON (BOV."IdBoveda" = CCP."IdBoveda")
LEFT JOIN "BSCL.Portafolio.BD::POR.ModeloCustodia" AS MODELO ON MODELO."IdModeloCustodia" = CCP."IdModeloCustodia"
LEFT JOIN "BSCL.Portafolio.BD::POR.TipoCustodia" AS TIPOCUSTODIA ON TIPOCUSTODIA."IdTipoCustodia" = CCP."IdTipoCustodia"
LEFT JOIN "BSCL.Portafolio.BD::POR.TipoCuentaCustodia" AS TIPOCUENTA ON TIPOCUENTA."IdTipoCuentaCustodia" = CCP."IdTipoCuentaCustodia"
LEFT JOIN "BSCL.Plataforma.BD::PLA.BancoCustodio" AS BANCOCUSTODIO ON BANCOCUSTODIO."IdPersona" = CCP."IdPersonaBancoCustodio"
LEFT JOIN "BSCL.Plataforma.BD::PLA.BancoCustodio" AS BANCOLIQ ON BANCOLIQ."IdPersona" = CCP."IdPersonaBancoLiquidador"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PERLIQ ON PERLIQ."IdPersona" = BANCOCUSTODIO."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PERCUS ON PERCUS."IdPersona" = BANCOLIQ."IdPersona";

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarCuentaCustodioPortafolio" TO  BCS_DESARROLLADOR WITH GRANT OPTION;  

