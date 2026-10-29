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

SET schema BSCL;
--
--
-- LIBERACION SEMANA: 05-02-2016
--


delete from "BSCL.Tesoreria.BD::TES.EstadoTesoreria";

INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(1,'ING','TES.MovimientoIngresoEgreso');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(2,'CRT','TES.MovimientoIngresoEgreso');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(3,'BLO','TES.MovimientoIngresoEgreso');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(4,'ANU','TES.MovimientoIngresoEgreso');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(5,'EMI','TES.MovimientoIngresoEgreso');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(6,'ING','TES.Comprobante');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(7,'BLO','TES.Comprobante');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(8,'LIQ','TES.Comprobante');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(9,'ING','TES.ComprobanteFormaDePago');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(10,'CRT','TES.ComprobanteFormaDePago');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(13,'ANU','TES.ComprobanteFormaDePago'); --INTERNO
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(14,'LIQ','TES.ComprobanteFormaDePago');

--------ESTADO CAJA------
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(15,'ACT','TES.Caja');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(16,'INA','TES.Caja');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(17,'BLO','TES.Caja');

-------ESTADO CAJA-MONEDA------
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(18,'ACT','TES.CajaMoneda');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(19,'INA','TES.CajaMoneda');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(20,'BLO','TES.CajaMoneda');


-------ESTADO CAJA-USUARIO------
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(21,'ACT','TES.CajaUsuario');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(22,'INA','TES.CajaUsuario');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(23,'BLO','TES.CajaUsuario');

-------ESTADO CAJA-PRODUCTO------
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(24,'ACT','TES.CajaProducto');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(25,'INA','TES.CajaProducto');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(26,'BLO','TES.CajaProducto');

-------ESTADOS CUENTAS BANCARIAS ------
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(27,'ACT','TES.CuentaBancaria');
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
VALUES(28,'INA','TES.CuentaBancaria');


-- ########################################################################################
-- # JV: Mantenedor de Cuentas Bancarias
-- ########################################################################################

-- Modificación tabla "CuentaBanco"
ALTER TABLE "BSCL.Tesoreria.BD::TES.CuentaBanco"  DROP ("Dirección");
ALTER TABLE "BSCL.Tesoreria.BD::TES.CuentaBanco"  DROP ("MedioContacto");

ALTER TABLE "BSCL.Tesoreria.BD::TES.CuentaBanco"  ADD ("Direccion"	NVARCHAR(100)   NOT NULL DEFAULT '');
ALTER TABLE "BSCL.Tesoreria.BD::TES.CuentaBanco"  ADD ("TelefonoContacto"	NVARCHAR(100)   NOT NULL DEFAULT '');
ALTER TABLE "BSCL.Tesoreria.BD::TES.CuentaBanco"  ADD ("EmailContacto"	NVARCHAR(100)   NOT NULL DEFAULT '');

-- Secuencia 
DROP SEQUENCE "BSCL.Tesoreria.BD.Secuencias::CuentaBanco"	CASCADE;
CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::CuentaBanco"   START    WITH 1  INCREMENT BY 1  MINVALUE  1; 
       


-----------------------------------------
------------MANTENEDOR CAJAS ------------
-----------------------------------------
drop table "BSCL.Tesoreria.BD::TES.CajaProductoEmpresa" cascade;

CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.CajaProductoEmpresa" (
 "IdCajaProductoEmpresa" BIGINT 		NOT NULL,
 "IdEmpresa"  			BIGINT 			NOT NULL,
 "IdCaja"				BIGINT 			NOT NULL,
 "IdProductoEmpresa"	BIGINT			NOT NULL,
 "	"				NVARCHAR(3) 	NOT NULL,
PRIMARY KEY ("IdCajaProductoEmpresa") );

/* Table: */
	alter table "BSCL.Tesoreria.BD::TES.CajaProductoEmpresa" 
	   add constraint  FK_Caja_CajaProductoEmpresa foreign key ("IdCaja")
	      references "BSCL.Tesoreria.BD::TES.Caja"  ("IdCaja") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.CajaProductoEmpresa" 
	   add constraint  FK_ProductoEmpresa_CajaProductoEmpresa foreign key ("IdProductoEmpresa")
	      references "BSCL.Plataforma.BD::INS.ProductoEmpresa" ("IdProductoEmpresa") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.CajaProductoEmpresa"
	   add constraint  FK_Estado_CajaProductoEmpresa foreign key ("IdEstado")
		  references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

DROP SEQUENCE "BSCL.Tesoreria.BD.Secuencias::CajaProducto"	CASCADE;
CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::CajaProducto"
	   START     WITH 1
       INCREMENT BY 1
       MINVALUE  1; 

-------------------------------------------
-------- Agregar campo Saldo a CajaMoneda
--------------------------------------------
alter table "BSCL"."BSCL.Tesoreria.BD::TES.CajaMoneda"
       add ("Saldo" DECIMAL(26,8) DEFAULT 0);
-----------------------------------------------------------

-------------------------------------------
-------- Agregar campo SobreGiro a CajaUsuario
--------------------------------------------
alter table "BSCL"."BSCL.Tesoreria.BD::TES.CajaUsuario"
       add ("Sobregiro" NVARCHAR(1) NOT NULL default 'N');
-----------------------------------------------------------


-----FK
	alter table "BSCL.Tesoreria.BD::TES.IngresoEgreso"
	   add constraint  FK_Empresa_IngresoEgreso foreign key ("IdEmpresa")
		      references "BSCL.Plataforma.BD::PLA.Empresa" ("IdEmpresa") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.IngresoEgreso"
	   add constraint  FK_Modulo_IngresoEgreso foreign key ("IdModulo")
		      references "BSCL.Plataforma.BD::PLA.Modulo" ("IdModulo") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.IngresoEgreso"
	   add constraint  FK_Usuario_IngresoEgreso foreign key ("IdUsuario")
		      references "BSCL.Plataforma.BD::ACC.Usuario" ("IdUsuario") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.IngresoEgreso"
	   add constraint  FK_Concepto_IngresoEgreso foreign key ("IdConcepto")
		      references "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.IngresoEgreso"
	   add constraint  FK_Moneda_IngresoEgreso foreign key ("IdMoneda")
		      references "BSCL.Plataforma.BD::PLA.Moneda" ("IdMoneda") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.IngresoEgreso"
	   add constraint  FK_TipoDocumentoIdentidad_IngresoEgreso foreign key ("IdTipoDocumentoIdentidad")
		      references "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" ("IdTipoDocumentoIdentidad") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.IngresoEgreso"
	   add constraint  FK_Caja_IngresoEgreso foreign key ("IdCaja")
		      references "BSCL.Tesoreria.BD::TES.Caja" ("IdCaja") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.IngresoEgreso"
	   add constraint  FK_Estado_IngresoEgreso foreign key ("IdEstado")
		      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.IngresoEgreso"
	   add constraint  FK_TipoSocioDeNegocio_IngresoEgreso foreign key ("IdTipoPersona")
		      references "BSCL.SocioNegocio.BD::SOC.TipoSocioDeNegocio" ("IdTipoSocioDeNegocio") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago"
	   add constraint  FK_MedioDePago_IngresoEgresoFormaDePago foreign key ("IdMedioDePago")
		      references "BSCL.Plataforma.BD::PAG.MedioDePago" ("IdMedioDePago") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago"
	   add constraint  FK_Estado_IngresoEgresoFormaDePago foreign key ("IdEstado")
		      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago"
	   add constraint  FK_IngresoEgreso_IngresoEgresoFormaDePago foreign key ("IdIngresoEgreso")
		      references "BSCL.Tesoreria.BD::TES.IngresoEgreso" ("IdIngresoEgreso") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.Comprobante"
	   add constraint  FK_Empresa_Comprobante foreign key ("IdEmpresa")
		      references "BSCL.Plataforma.BD::PLA.Empresa" ("IdEmpresa") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.Comprobante" 
	   add constraint  FK_Usuario_Comprobante foreign key ("IdUsuario")
	      references "BSCL.Plataforma.BD::ACC.Usuario" ("IdUsuario") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.Comprobante" 
	   add constraint  FK_Concepto_Comprobante foreign key ("IdConcepto")
	      references "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.Comprobante" 
	   add constraint  FK_Moneda_Comprobante foreign key ("IdMoneda")
	      references "BSCL.Plataforma.BD::PLA.Moneda" ("IdMoneda") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.Comprobante" 
	   add constraint  FK_Persona_Comprobante foreign key ("IdPersona")
	      references "BSCL.SocioNegocio.BD::PER.Persona" ("IdPersona") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.Comprobante"
	   add constraint  FK_TipoDocumentoIdentidad_Comprobante foreign key ("IdTipoDocumentoIdentidad")
	      references "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" ("IdTipoDocumentoIdentidad") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.Comprobante"
	   add constraint  FK_Estado_Comprobante foreign key ("IdEstado")
		      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago"
	   add constraint  FK_Comprobante_ComprobanteFormaDePago foreign key ("IdComprobante")
		      references "BSCL.Tesoreria.BD::TES.Comprobante" ("IdComprobante") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago"
	   add constraint  FK_MedioDePago_ComprobanteFormaDePago foreign key ("IdMedioDePago")
		      references "BSCL.Plataforma.BD::PAG.MedioDePago" ("IdMedioDePago") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago"
	   add constraint  FK_Estado_ComprobanteFormaDePago foreign key ("IdEstado")
		      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.ComprobanteDetalle"
	   add constraint  FK_Estado_ComprobanteDetalle foreign key ("IdEstado")
		      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.ComprobanteDetalleIngresoEgreso"
	   add constraint  FK_IngresoEgreso_ComprobanteDetalleIngresoEgreso foreign key ("IdIngresoEgreso")
		      references "BSCL.Tesoreria.BD::TES.IngresoEgreso" ("IdIngresoEgreso") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.ComprobanteDetalle"
	   add constraint  FK_Estado_ComprobanteDetalleIngresoEgreso foreign key ("IdEstado")
		      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.ConceptoTesoreria"
	   add constraint "FK_Concepto_ConceptoTesoreria" foreign key ("IdConcepto")
	      references "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.EstadoTesoreria"
	   add constraint "FK_Estado_EstadoTesoreria" foreign key ("IdEstado")
	      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;


