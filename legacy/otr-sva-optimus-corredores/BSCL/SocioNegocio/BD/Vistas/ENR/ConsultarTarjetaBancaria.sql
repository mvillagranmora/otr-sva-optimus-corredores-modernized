SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarTarjetaBancaria";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarTarjetaBancaria" AS
SELECT
TARJETA_BANCARIA."IdTarjetaBancaria"
,TARJETA_BANCARIA."IdBanco"
,TARJETA_BANCARIA."IdTipoTarjetaBancaria"
,TARJETA_BANCARIA."NumeroTarjetaBancaria"
,LEFT(TARJETA_BANCARIA."NumeroTarjetaBancaria",0) || 'XXXXXXX' || RIGHT(TARJETA_BANCARIA."NumeroTarjetaBancaria",4) AS "NumeroTarjetaBancariaEnmasc"
,TARJETA_BANCARIA."IdEnrolamiento"
,TARJETA_BANCARIA."FechaVencimiento"
,TARJETA_BANCARIA."CodigoVerificacion"
,BANCO."NombreBanco"
,TIPO_TARJETA_BANCARIA."NombreTipoTarjetaBancaria"
,'['|| BANCO."NombreBanco" ||']'||  ' N°' || TARJETA_BANCARIA."NumeroTarjetaBancaria" 	AS "NombreTarjetaBancariaFull"
FROM "BSCL.SocioNegocio.BD::ENR.TarjetaBancaria" TARJETA_BANCARIA
LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" AS BANCO ON TARJETA_BANCARIA."IdBanco" = BANCO."IdBanco"
LEFT JOIN "BSCL.Plataforma.BD::BAN.TipoTarjetaBancaria" AS TIPO_TARJETA_BANCARIA ON TIPO_TARJETA_BANCARIA."IdTipoTarjetaBancaria" = TARJETA_BANCARIA."IdTipoTarjetaBancaria";

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarTarjetaBancaria" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
