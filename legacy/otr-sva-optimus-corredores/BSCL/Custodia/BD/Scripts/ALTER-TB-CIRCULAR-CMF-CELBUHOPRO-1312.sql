SET SCHEMA "BSCL";

ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("IndicadorValorizado" NVARCHAR(1) DEFAULT 'N');
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("ValResultado" NVARCHAR(400) DEFAULT '');

/*
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("ValMontoMonedaInstrumento" DECIMAL(26,8) DEFAULT 0);
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("ValMontoMonedaSistema" DECIMAL(26,8) DEFAULT 0);
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("ValMontoMonedaInstrumentoFechaVenta" DECIMAL(26,8) DEFAULT 0);
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("ValMontoMonedaSistemaFechaVenta" DECIMAL(26,8) DEFAULT 0);
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("ValMontoDevengadoAMonedaInstrumento" DECIMAL(26,8) DEFAULT 0);
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("ValMontoDevengadoAMonedaSistema" DECIMAL(26,8) DEFAULT 0);
*/

ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("IdInstrumento" BIGINT DEFAULT 0);

ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("PrecioMercadoGarantia" DECIMAL(26,8) DEFAULT 0);
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("PrecioMercadoPrestamo" DECIMAL(26,8) DEFAULT 0);
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("PrecioMercadoPactosC" DECIMAL(26,8) DEFAULT 0);
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("PrecioMercadoPactosV" DECIMAL(26,8) DEFAULT 0);
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("PrecioMercadoSimulCom" DECIMAL(26,8) DEFAULT 0);
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("PrecioMercadoSimulVen" DECIMAL(26,8) DEFAULT 0);

ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("TasaMercadoGarantia" DECIMAL(26,8) DEFAULT 0);
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("TasaMercadoPrestamo" DECIMAL(26,8) DEFAULT 0);
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("TasaMercadoPactosC" DECIMAL(26,8) DEFAULT 0);
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("TasaMercadoPactosV" DECIMAL(26,8) DEFAULT 0);
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("TasaMercadoSimulCom" DECIMAL(26,8) DEFAULT 0);
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("TasaMercadoSimulVen" DECIMAL(26,8) DEFAULT 0);

COMMIT;
