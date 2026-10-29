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
