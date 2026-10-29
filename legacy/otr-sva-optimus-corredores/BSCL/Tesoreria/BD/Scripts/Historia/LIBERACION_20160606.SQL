-- ########################################################################################
--
--  =========================================================
--  ESTE ARCHIVO CONTIEN LOS SCRIPT HISTORICOS DE LIBERACION
--  =========================================================
--  * Debe incluir los ALTER de Tablas
--  * Debe incluir nuevas tablas, indices, secuencuias, etc.
--  * Debe incluir insert de datos maestros necesarios para el funcionamiento 
--  NOTA: Este archivo no reemplaza los scripts de creación que deben ir en cada módulo.
-- 
-- ########################################################################################

SET SCHEMA BSCL;
--
--
-- LIBERACION SEMANA: 03-06-2016
--
drop table "BSCL.Tesoreria.BD::TES.CajaUsuario" cascade;

CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.CajaUsuario" (
 "IdCajaUsuario"		BIGINT 			NOT NULL,
 "IdEmpresa"  			BIGINT 			NOT NULL,
 "IdCaja"				BIGINT 			NOT NULL,
 "IdUsuario"			BIGINT			NOT NULL,
 "Sobregiro"            NVARCHAR(1) 	NOT NULL default 'N',  -- S / N 
 "FechaInicio"			DATE     		NOT NULL,
 "FechaTermino"			DATE 	    	NOT NULL,
 "IdEstado"				NVARCHAR(3) 	NOT NULL,
PRIMARY KEY ("IdCajaUsuario") );



alter table "BSCL.Tesoreria.BD::TES.CajaUsuario" 
   add constraint  FK_Caja_CajaUsuario foreign key ("IdCaja")
      references "BSCL.Tesoreria.BD::TES.Caja"  ("IdCaja") on delete restrict on update restrict;

alter table "BSCL.Tesoreria.BD::TES.CajaUsuario" 
   add constraint  FK_Usuario_CajaUsuario foreign key ("IdUsuario")
      references "BSCL.Plataforma.BD::ACC.Usuario" ("IdUsuario") on delete restrict on update restrict;

alter table "BSCL.Tesoreria.BD::TES.CajaUsuario"
   add constraint  FK_Estado_CajaUsuario foreign key ("IdEstado")
      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;
      
--####################################################################################
-- <NRM> 
--####################################################################################
DROP TABLE "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago" CASCADE;
CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago" (
 "IdTipoMovimientoMedioDePago"		BIGINT		NOT NULL,
 "IdTipoMovimiento"				    NVARCHAR(6)	NOT NULL,
 "IdMedioDePago" 					NVARCHAR(3) NOT NULL,
 "AfectaSaldoEnEstado"				NVARCHAR(40),
 "DesplegarEnCombobox"				NVARCHAR(1),
PRIMARY KEY ("IdTipoMovimientoMedioDePago") );


INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','CHE','CRT;DEP;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','VVI','CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','BDE','CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','EFE','CRT;DEP;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','TLR','CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','TSW','CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','TDP','CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','EBA','CRT;DEP;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','TRA','CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','VVE','CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','CHE','EMI;CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','VVI','EMI;CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','BDE',NULL,'N');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','EFE','ING;CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','TLR','ING;CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','TSW','ING;CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','TDP','ING;CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','EBA',NULL,'N');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','TRA','ING;CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','VVE','ING;CRT;LIQ','S');




DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios::CrearComprobanteFormaDePago";
UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" SET "RutaServicio" = 'BSCL.Tesoreria.BD.Servicios::GestionFormaDePago' WHERE "IdServicio" = 91;






DELETE FROM "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago";
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','CHE','LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','VVI','LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','BDE','CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','EFE','CRT;DEP;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','TLR','CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','TSW','CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','TDP','CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','EBA','CRT;DEP;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','TRA','CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVING','VVE','LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','CHE','EMI;CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','VVI','EMI;CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','BDE',NULL,'N');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','EFE','ING;CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','TLR','ING;CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','TSW','ING;CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','TDP','ING;CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','EBA',NULL,'N');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','TRA','ING;CRT;LIQ','S');
INSERT INTO "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago"("IdTipoMovimientoMedioDePago","IdTipoMovimiento","IdMedioDePago","AfectaSaldoEnEstado","DesplegarEnCombobox")
VALUES("BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago".NEXTVAL,'MOVEGR','VVE','ING;CRT;LIQ','S');




DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios::AnularNominasTransferencias";
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios::RecepcionarNominasTransferencias";
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios::ReprocesarNominasTransferencias";
UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" SET "RutaServicio" = 'BSCL.Tesoreria.BD.Servicios.NOM::AnularNominasTransferencias' WHERE "IdServicio" = 230;
UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" SET "RutaServicio" = 'BSCL.Tesoreria.BD.Servicios.NOM::RecepcionarNominasTransferencias' WHERE "IdServicio" = 225;
UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" SET "RutaServicio" = 'BSCL.Tesoreria.BD.Servicios.NOM::ReprocesarNominasTransferencias' WHERE "IdServicio" = 231;


DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios::ConsultarSaldoCuentaBanco";
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios::ActualizarSaldosCuentaBanco";
UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" SET "RutaServicio" = 'BSCL.Tesoreria.BD.Servicios.BAN::ConsultarSaldoCuentaBanco' WHERE "IdServicio" = 314;


DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios::CrearDeposito";
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios::CertificarDepositos";
UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" SET "RutaServicio" = 'BSCL.Tesoreria.BD.Servicios.DEP::CertificarDepositos' WHERE "IdServicio" = 259;
UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" SET "RutaServicio" = 'BSCL.Tesoreria.BD.Servicios.DEP::CrearDeposito' WHERE "IdServicio" = 255;


DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios::ModificarCuentaBancaria";
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios::CrearCuentaBancaria";
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios::EliminarCuentaBancaria";
UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" SET "RutaServicio" = 'BSCL.Tesoreria.BD.Servicios.BAN::ModificarCuentaBancaria' WHERE "IdServicio" = 147;
UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" SET "RutaServicio" = 'BSCL.Tesoreria.BD.Servicios.BAN::CrearCuentaBancaria' WHERE "IdServicio" = 146;
UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" SET "RutaServicio" = 'BSCL.Tesoreria.BD.Servicios.BAN::CrearCuentaBancaria' WHERE "IdServicio" = 163;




DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios::CrearCajaUsuario";
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios::EliminarCaja";
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios::CrearCaja";
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios::CrearCajaMoneda";
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios::CrearCajaProducto";
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios::ModificarMantenedorCaja";
UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" SET "RutaServicio" = 'BSCL.Tesoreria.BD.Servicios.CAJ::EliminarCaja' WHERE "IdServicio" = 155;
UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" SET "RutaServicio" = 'BSCL.Tesoreria.BD.Servicios.CAJ::CrearCaja' WHERE "IdServicio" = 127;
UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" SET "RutaServicio" = 'BSCL.Tesoreria.BD.Servicios.CAJ::CrearCajaMoneda' WHERE "IdServicio" = 131;
UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" SET "RutaServicio" = 'BSCL.Tesoreria.BD.Servicios.CAJ::CrearCajaUsuario' WHERE "IdServicio" = 135;
UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" SET "RutaServicio" = 'BSCL.Tesoreria.BD.Servicios.CAJ::ModificarMantenedorCaja' WHERE "IdServicio" = 144;


DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios::BloquearDesbloquearChequera";
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios::CrearChequera";
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios::CrearConfiguracionChequera";
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios::EliminarConfiguracionChequera";
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios::ModificarChequera";
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios::ModificarConfiguracionChequera";
UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" SET "RutaServicio" = 'BSCL.Tesoreria.BD.Servicios.CHE::BloquearDesbloquearChequera' WHERE "IdServicio" = 177;
UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" SET "RutaServicio" = 'BSCL.Tesoreria.BD.Servicios.CHE::CrearChequera' WHERE "IdServicio" = 161;
UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" SET "RutaServicio" = 'BSCL.Tesoreria.BD.Servicios.CHE::CrearConfiguracionChequera' WHERE "IdServicio" = 185;
UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" SET "RutaServicio" = 'BSCL.Tesoreria.BD.Servicios.CHE::EliminarConfiguracionChequera' WHERE "IdServicio" = 192;
UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" SET "RutaServicio" = 'BSCL.Tesoreria.BD.Servicios.CHE::ModificarChequera' WHERE "IdServicio" = 166;
UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" SET "RutaServicio" = 'BSCL.Tesoreria.BD.Servicios.CHE::ModificarConfiguracionChequera' WHERE "IdServicio" = 189;


DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios::ProcesarClearing";
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios::ConsultarClearing";
UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" SET "RutaServicio" = 'BSCL.Tesoreria.BD.Servicios.CLG::ProcesarClearing' WHERE "IdServicio" = 216;
UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" SET "RutaServicio" = 'BSCL.Tesoreria.BD.Servicios.CLG::ConsultarClearing' WHERE "IdServicio" = 193;




