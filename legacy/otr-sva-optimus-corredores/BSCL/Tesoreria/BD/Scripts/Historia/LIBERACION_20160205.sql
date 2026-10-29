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
-- LIBERACION SEMANA: 12-02-2016
--

-- Nuevos campos para cuentas bancarias
ALTER TABLE "BSCL"."BSCL.Tesoreria.BD::TES.CuentaBanco" ADD ("SaldoInicial"			   DECIMAL(26,8)   DEFAULT 0);
ALTER TABLE "BSCL"."BSCL.Tesoreria.BD::TES.CuentaBanco" ADD ("AfectaPosicionInicial"    NVARCHAR(1)     NOT NULL DEFAULT 'N');

------------------------------------------------------------------------------------------------------------
/*Tabla Caja Producto Empresa */
drop table "BSCL.Tesoreria.BD::TES.CajaProductoEmpresa" cascade;
CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.CajaProductoEmpresa" (
 "IdCajaProductoEmpresa" BIGINT 		NOT NULL,
 "IdEmpresa"  			BIGINT 			NOT NULL,
 "IdCaja"				BIGINT 			NOT NULL,
 "IdProductoEmpresa"	BIGINT			NOT NULL,
 "IdEstado"				NVARCHAR(3) 	NOT NULL,
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
---------------------------------------------------------------------------------------------------------
/* Secuencia Chequera */
CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::Chequera"
	   START     WITH 1
       INCREMENT BY 1
       MINVALUE  1; 
       
       
   
