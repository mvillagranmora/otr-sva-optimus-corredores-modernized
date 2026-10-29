SET SCHEMA "BSCL";

-- Fuentes de Precios
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("FuentePreciosPlazo" NVARCHAR(100) DEFAULT '');

-- Cantidades
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("CantidadPlazoCompra" Decimal(26,8) DEFAULT 0);
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("CantidadPlazoVenta" Decimal(26,8) DEFAULT 0);

-- Clasificacion de Instrumentos
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("ClasificacionInstrumentoCustodia" NVARCHAR(8) DEFAULT ''); 
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("ClasificacionInstrumentoGarantia" NVARCHAR(8) DEFAULT ''); 
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("ClasificacionInstrumentoPrestamo" NVARCHAR(8) DEFAULT ''); 
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("ClasificacionInstrumentoPlazo" NVARCHAR(8) DEFAULT ''); 

-- Valorizacion
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("ValorizacionGarantia" DECIMAL(26,8) DEFAULT 0); 
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("ValorizacionPrestamo" DECIMAL(26,8) DEFAULT 0); 
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("ValorizacionPlazoCompra" DECIMAL(26,8) DEFAULT 0); 
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("ValorizacionPlazoVenta" DECIMAL(26,8) DEFAULT 0); 

-- TipoInstrumento
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("IdTipoInstrumentoPlazoCompra" NVARCHAR(6) DEFAULT '');
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("IdTipoInstrumentoPlazoVenta" NVARCHAR(6) DEFAULT '');  

-- Cambiar tipos de datos
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ALTER ("ValorMercadoCustodia" DECIMAL(26,8) DEFAULT 0);
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ALTER ("MontoMercadoCustodia" DECIMAL(26,8) DEFAULT 0);
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ALTER ("PrecioMercadoCustodia" DECIMAL(26,8) DEFAULT 0);
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ALTER ("PrecioCompraCustodia" DECIMAL(26,8) DEFAULT 0);
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ALTER ("TasaMercadoCustodia" DECIMAL(26,8) DEFAULT 0);
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ALTER ("CantidadTotal" DECIMAL(26,8) DEFAULT 0);
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ALTER ("ValorCompra" DECIMAL(26,8) DEFAULT 0);
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ALTER ("MontoCompra" DECIMAL(26,8) DEFAULT 0);
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ALTER ("CantidadGarantia" DECIMAL(26,8) DEFAULT 0);
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ALTER ("CantidadCustodia" DECIMAL(26,8) DEFAULT 0);

COMMIT;