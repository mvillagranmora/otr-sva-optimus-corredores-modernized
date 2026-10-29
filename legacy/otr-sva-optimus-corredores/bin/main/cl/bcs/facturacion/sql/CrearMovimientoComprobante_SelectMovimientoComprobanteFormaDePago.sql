SELECT PAG."NumeroMvtoCmbte",PAG."IdMedioDePago",PAG."IdModalidadDePago",PAG."IdMoneda",PAG."IdTipoCuentaBancaria",PAG."NumeroMvtoCmbteFormaDePago",
PAG."IdTarjetaBancaria",PAG."IdTipoTarjetaBancaria",

MEDPAG."IdMedioDePago" AS "IdMedioDePagoReal",
MODPAG."IdModalidadDePago" AS "IdModalidadDePagoReal",
MON."IdMoneda" AS "IdMonedaFormaDePagoReal",
TIPCTA."IdTipoCuentaBancaria" AS "IdTipoCuentaBancariaReal",
PERTJTA."IdTarjetaBancaria" AS "IdTarjetaBancariaReal",
TIPTAR."IdTipoTarjetaBancaria" AS "IdTipoTarjetaBancariaReal",

"BSCL.Facturacion.BD.Secuencias::MovimientoComprobanteFormaDePago".NEXTVAL AS "IdMovimientoComprobanteFormaDePago",
MEDPAG."Nombre" AS "NombreMedioDePago",
TIPCTA."NombreTipoCuentaBancaria" AS "NombreTipoCuentaBancaria",
TIPTAR."NombreTipoTarjetaBancaria" AS "NombreTipoTarjetaBancaria",
MODPAG."Nombre" AS "NombreModalidadDePago",
BAN."CodigoBanco" AS "CodigoBanco",
BAN."NombreBanco" AS "NombreBanco"


FROM "BSCL.Facturacion.BD::FAC.CrearMovimientoComprobanteFormaDePago_Proceso" PAG
LEFT JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" AS MEDPAG ON MEDPAG."IdMedioDePago" = PAG."IdMedioDePago"
LEFT JOIN "BSCL.Plataforma.BD::PAG.ModalidadDePago" AS MODPAG ON MODPAG."IdModalidadDePago" = PAG."IdModalidadDePago"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON ON MON."IdMoneda" = PAG."IdMoneda"
LEFT JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" AS TIPCTA ON TIPCTA."IdTipoCuentaBancaria" = PAG."IdTipoCuentaBancaria"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTarjetasBancarias" AS PERTJTA ON PERTJTA."IdTarjetaBancaria" = PAG."IdTarjetaBancaria"
LEFT JOIN "BSCL.Plataforma.BD::BAN.TipoTarjetaBancaria" AS TIPTAR ON TIPTAR."IdTipoTarjetaBancaria" = PAG."IdTipoTarjetaBancaria"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::BAN.ConsultarBanco" BAN ON BAN."IdBanco" = PAG."IdBanco"                       
/*SET SCHEMA BSCL

INSERT INTO "BSCL.Facturacion.BD::FAC.CrearMovimientoComprobante_Proceso"
("NumeroMvtoCmbte","IdConcepto","IdTipoComprobante","IdCondicionDeLiquidacion","IdTipoReajuste","IdTipoConvenio",
"IdEmisor","IdMonedaLiquidacion","IdInstrumento","IdInstrumentoRelacionado","IdEmpleadoDigitador","IdInstrumentoSubyacente",
"IdTipoProducto","IdMoneda","IdMonedaSubyacente","IdTipoCanalIngreso","IdTipoOperacion","IdBoveda","IdCorredoraContraparte",
"IdBolsa","IdOrden")
VALUES
(3729332,'CMACNA','FAE','T0','','SCV',
'FALABELLA','CLP',916,0,7307,0,
'INAC','CLP','','TRA','COMACN','DCV',2001,
'BCS',315357)*/
