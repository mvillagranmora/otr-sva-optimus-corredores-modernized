-- case seed on top of seed/base-fmm.sql (fake data)
-- REAL types (cat_columnas_tablas.csv)
CREATE TABLE "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" ("IdMedioDePagoYModalidadDePago" BIGINT NOT NULL, "IdMedioDePago" NVARCHAR(3) NOT NULL, "IdModalidadDePago" NVARCHAR(6) NOT NULL, "IdEstado" NVARCHAR(3) NOT NULL);
-- ASSUMED types (no catalog: legacy row class ProcesarIngresoEgresoTesoreria.PreferenciaPagoPortafolio)
CREATE TABLE "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio" ("IdPreferenciaPagoPortafolio" BIGINT, "IdCuentaBancaria" BIGINT, "IdBancoCuentaBancaria" BIGINT, "NumeroCuentaBancaria" NVARCHAR(100), "IdTipoCuentaBancaria" NVARCHAR(4), "NombreTipoCuentaBancaria" NVARCHAR(100));
-- ASSUMED types (no catalog: legacy row class ProcesarIngresoEgresoTesoreria.PreferenciaPagoCorredor + the filter columns of the in-line SQL)
CREATE TABLE "BSCL.Tesoreria.BD::TES.PreferenciaPagoCorredor" ("IdTipoMovimiento" NVARCHAR(6), "IdConcepto" NVARCHAR(6), "IdMedioDePago" NVARCHAR(3), "IdCuentaBanco" BIGINT, "IdMoneda" NVARCHAR(3), "NombreEntidad" NVARCHAR(100), "IdEstado" NVARCHAR(3), "IdEmpresa" BIGINT);
UPDATE "BSCL.Plataforma.BD::ACC.Usuario" SET "TipoEntidad" = 'CLI' WHERE "IdUsuario" = 7;
