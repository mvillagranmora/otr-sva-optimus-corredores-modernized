SELECT 
PP."IdPortafolio"
,PP."IdPreferenciaPago"
,CUENTABANCO."IdCuentaBanco" AS "IdCuentaBancaria"
,PP."IdBancoCuentaBancaria"
,PP."NombreBancoCuentaBancaria"
,PP."NumeroCuentaBancaria"
,PP."IdTipoCuentaBancaria"
,PP."NombreTipoCuentaBancaria"
,PP."IdTarjetaBancaria"
,PP."IdTipoTarjetaBancaria"
,PP."NombreBancoTarjetaBancaria"
,PP."NumeroTarjetaBancaria"
,MP."IdModalidadDePago"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio" AS PP
LEFT JOIN  "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" AS  MP ON MP."IdMedioDePago" = PP."IdMedioDePago"
INNER JOIN "BSCL.Tesoreria.BD::TES.CuentaBanco" AS CUENTABANCO  ON CUENTABANCO."IdBanco" = PP."IdBancoCuentaBancaria" 
  																AND	CUENTABANCO."IdMoneda" = PP."IdMoneda"
  																AND	CUENTABANCO."NumeroCuentaBancaria" = PP."NumeroCuentaBancaria" 
WHERE PP."IdPortafolio" = :IdPortafolio
AND PP."IdTipoProducto"='MERF'
AND PP."IdEstado" = 'ACT'
UNION ALL
SELECT 
PP."IdPortafolio"
,PP."IdPreferenciaPago"
,PP."IdCuentaBancaria"
,PP."IdBancoCuentaBancaria"
,PP."NombreBancoCuentaBancaria"
,PP."NumeroCuentaBancaria"
,PP."IdTipoCuentaBancaria"
,PP."NombreTipoCuentaBancaria"
,PP."IdTarjetaBancaria"
,PP."IdTipoTarjetaBancaria"
,PP."NombreBancoTarjetaBancaria"
,PP."NumeroTarjetaBancaria"
,MP."IdModalidadDePago"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio" AS PP
LEFT JOIN  "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" AS  MP ON MP."IdMedioDePago" = PP."IdMedioDePago"
WHERE PP."IdPortafolio" = :IdPortafolio
AND PP."IdTipoProducto"='MERF'
AND PP."IdEstado" = 'ACT'
AND MP."IdModalidadDePago"= 'MODCAJ'
UNION ALL
SELECT 
PP."IdPortafolio"
,PP."IdPreferenciaPago"
,PP."IdCuentaBancaria"
,PP."IdBancoCuentaBancaria"
,PP."NombreBancoCuentaBancaria"
,PP."NumeroCuentaBancaria"
,PP."IdTipoCuentaBancaria"
,PP."NombreTipoCuentaBancaria"
,PP."IdTarjetaBancaria"
,PP."IdTipoTarjetaBancaria"
,PP."NombreBancoTarjetaBancaria"
,PP."NumeroTarjetaBancaria"
,MP."IdModalidadDePago"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio" AS PP
LEFT JOIN  "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" AS  MP ON MP."IdMedioDePago" = PP."IdMedioDePago"
WHERE PP."IdPortafolio" = :IdPortafolio
AND PP."IdTipoProducto"='MERF'
AND PP."IdEstado" = 'ACT'
AND MP."IdMedioDePago" = 'CIN'
