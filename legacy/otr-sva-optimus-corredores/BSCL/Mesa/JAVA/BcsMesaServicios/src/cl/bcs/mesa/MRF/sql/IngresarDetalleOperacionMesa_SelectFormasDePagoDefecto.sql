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
,PP."IdMedioDePago"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio" AS PP
LEFT JOIN  "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" AS  MP ON MP."IdMedioDePago" = PP."IdMedioDePago"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" AS CBANC ON PP."IdCuentaBancaria" = CBANC."IdCuentaBancaria"
INNER JOIN "BSCL.Tesoreria.BD::TES.CuentaBanco" AS CUENTABANCO  ON CUENTABANCO."IdBanco" = CBANC."IdBanco" 
  																AND	CUENTABANCO."IdMoneda" = CBANC."IdMoneda"
  																AND	CUENTABANCO."NumeroCuentaBancaria" = CBANC."NumeroCuentaBancaria" 

WHERE PP."IdPortafolio" = :IdPortafolio
AND PP."PorDefecto" = 'S'
AND PP."IdMoneda" =:IdMoneda
AND PP."IdTipoProducto"='MERF'
AND PP."IdEstado" = 'ACT'
AND PP."IdTipoOperacion" = :IdTipoOperacion;



 
 