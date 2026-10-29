--Mrojas
--05-01-2024
--Columnas Nuevas Para Edicion de Registros
-----ALETER NUEVAS COLUMNAS-------------
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("TendenciaDeRiesgo" NVARCHAR(50));

ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("BaseTasa" DECIMAL(26, 8));

ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("Duration" DECIMAL(26, 8));

ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("CodigoPais" NVARCHAR(2));

ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("CodigoPaisEmisionInstrumento" NVARCHAR(2));

ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("FuentePrecios" NVARCHAR(100));

ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD ("CantidadPrestamo" Decimal(26,8));