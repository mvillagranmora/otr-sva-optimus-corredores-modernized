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


--####################################################
--			JG.PLANTILLA EMPLEADO
--####################################################

-- Definición de Tablas.
DROP TABLE "BSCL.Plataforma.BD::COM.PlantillaEmpleado";
DROP TABLE "BSCL.Plataforma.BD::COM.DetallePlantillaEmpleado";

create column table "BSCL.Plataforma.BD::COM.PlantillaEmpleado" ( 
	"IdPlantillaEmpleado"				NVARCHAR(40)		NOT NULL,
	"Descripcion"						NVARCHAR(100)       NOT NULL DEFAULT '',
	"FechaCreacion"						DATE				NOT NULL DEFAULT '0001-01-01 00:00:00',
	"FechaModificacion"					DATE				NOT NULL DEFAULT '0001-01-01 00:00:00',
	"IdEstado"					   		NVARCHAR(3)       	NOT NULL DEFAULT 'ACT',
	"IdTipoCargoEmpleado" 				BIGINT				NOT NULL DEFAULT 0,
	--"PorDefecto"						NVARCHAR(1)       	NOT NULL DEFAULT ,
	"IdEmpresa"							BIGINT              NOT NULL DEFAULT 0,
	"IdUsuarioCreador"					BIGINT				NOT NULL DEFAULT 0,
	primary key ("IdPlantillaEmpleado")
);

create column table "BSCL.Plataforma.BD::COM.DetallePlantillaEmpleado" (
	"IdDetallePlantillaEmpleado"		BIGINT              NOT NULL, 
	"IdPlantillaEmpleado"				NVARCHAR(40)        NOT NULL DEFAULT '', 
	"IdTipoProducto"					NVARCHAR(4)         NOT NULL DEFAULT '', 
	"IdTipoOperacion"					NVARCHAR(6)         NOT NULL DEFAULT '', 
	"MontoMinimo"						DECIMAL(26,8) 		NOT NULL DEFAULT 0,
	"MontoMaximo"						DECIMAL(26,8) 		NOT NULL DEFAULT 0,
	"IdMoneda"							NVARCHAR(3)    		NOT NULL DEFAULT '',
	"PorcentajeDescuentoComision"		DECIMAL(26,8) 		NOT NULL DEFAULT 0, -- valor en %
	--
	"PorcentajeMargenEmpleado"			DECIMAL(26,8) 		NOT NULL DEFAULT 0,
	"Ruteo"								NVARCHAR(1)        	NOT NULL DEFAULT 'N', 
	"MontoMaximoRuteo"					DECIMAL(26,8) 		NOT NULL DEFAULT 0,
	"OrdenDirecta"						NVARCHAR(1)        	NOT NULL DEFAULT 'N', 
	"FechaCreacion"						DATE				NOT NULL DEFAULT '0001-01-01 00:00:00',
	"IdEstado"					   		NVARCHAR(3)       	NOT NULL DEFAULT 'ACT', 
	primary key ("IdDetallePlantillaEmpleado")
);

-- Secuencias.
drop sequence "BSCL.Plataforma.BD.Secuencias::DetallePlantillaEmpleado";
create sequence "BSCL.Plataforma.BD.Secuencias::DetallePlantillaEmpleado" increment by 1 start with 1000 minvalue 1000 maxvalue 4611686018427387903 no cycle ;


-- Inscripción de servicio de creación:
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(143,'BSCL.Plataforma.BD.Servicios::COM.CrearPlantillaEmpleado',0,'P','');



--Una plantilla de ejemplo.
DELETE FROM "BSCL.Plataforma.BD::COM.PlantillaEmpleado";
DELETE FROM "BSCL.Plataforma.BD::COM.DetallePlantillaEmpleado";

--cabecera
INSERT INTO "BSCL.Plataforma.BD::COM.PlantillaEmpleado" ("IdPlantillaEmpleado","Descripcion","FechaCreacion","FechaModificacion","IdEstado","IdEmpresa","IdUsuarioCreador")
VALUES ('PLANT_A','Esta es una descripción',NOW(),NOW(),'ACT',1,1);	
--detallle
INSERT INTO "BSCL.Plataforma.BD::COM.DetallePlantillaEmpleado"("IdDetallePlantillaEmpleado","IdPlantillaEmpleado","IdTipoProducto","IdTipoOperacion","MontoMinimo","MontoMaximo","IdMoneda","PorcentajeDescuentoComision","PorcentajeMargenEmpleado","Ruteo","MontoMaximoRuteo","OrdenDirecta","FechaCreacion","IdEstado")
VALUES(1,'PLANT_A','INAC','COMACN',0,999999999,'CLP',0,0,'N',999999999,'N',NOW(),'ACT');
INSERT INTO "BSCL.Plataforma.BD::COM.DetallePlantillaEmpleado"("IdDetallePlantillaEmpleado","IdPlantillaEmpleado","IdTipoProducto","IdTipoOperacion","MontoMinimo","MontoMaximo","IdMoneda","PorcentajeDescuentoComision","PorcentajeMargenEmpleado","Ruteo","MontoMaximoRuteo","OrdenDirecta","FechaCreacion","IdEstado")
VALUES(2,'PLANT_A','INAC','VENACN',0,999999999,'CLP',0,0,'N',999999999,'N',NOW(),'ACT');
INSERT INTO "BSCL.Plataforma.BD::COM.DetallePlantillaEmpleado"("IdDetallePlantillaEmpleado","IdPlantillaEmpleado","IdTipoProducto","IdTipoOperacion","MontoMinimo","MontoMaximo","IdMoneda","PorcentajeDescuentoComision","PorcentajeMargenEmpleado","Ruteo","MontoMaximoRuteo","OrdenDirecta","FechaCreacion","IdEstado")
VALUES(3,'PLANT_A','INMI','0',0,999999999,'CLP',0,0,'N',999999999,'N',NOW(),'ACT');


-------------------------------------------------------
--			FIN PLANTILLA EMPLEADO
-------------------------------------------------------


--####################################################
--			JV.INSCRIPCION DE SERVICIOS
--####################################################
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(146,'BSCL.Tesoreria.BD.Servicios::CrearCuentaBancaria',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(147,'BSCL.Tesoreria.BD.Servicios::ModificarCuentaBancaria',0,'P','');


--####################################################

--Conceptos
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" ) 
VALUES('MOVTRN','','CTA','MOVIMIENTO DE TRANSFERENCIA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" ) 
VALUES('CAGTSM','CA','CTA','INGRESO GARANTIA POR SIMULTANEA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" ) 
VALUES('CAGTFT','CA','CTA','INGRESO GARANTIA POR FUTURO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" ) 
VALUES('CAGTFW','CA','CTA','INGRESO GARANTIA POR FORWARD','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" ) 
VALUES('CAGTRF','CA','CTA','INGRESO GARANTIA POR RENTA FIJA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" ) 
VALUES('CAGTVC','CA','CTA','INGRESO GARANTIA POR VENTA CORTA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" ) 
VALUES('ABGTFT','AB','CTA','RETIRO GARANTIA POR FUTURO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" ) 
VALUES('ABGTFW','AB','CTA','RETIRO GARANTIA POR FORWARD','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" ) 
VALUES('ABGTRF','AB','CTA','RETIRO GARANTIA POR RENTA FIJA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" ) 
VALUES('ABGTVC','AB','CTA','RETIRO GARANTIA POR VENTA CORTA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" ) 
VALUES('ABGTSM','AB','CTA','RETIRO GARANTIA POR SIMULTANEA','VIG');
DELETE FROM "BSCL.Plataforma.BD::PLA.Concepto" 
WHERE "IdConcepto" in ('CAEGTE','ABIGTE','VCTSAB','VCTSCA','VENSIM','VCTSIA','CATPRE','ANTSIC','VCTSIC');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CAEGTE','CA','CTA','COMPROBANTE EGRESO TESORERIA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ABIGTE','AB','CTA','COMPROBANTE INGRESO TESORERIA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ABVCTS','AB','CTA','VENCIMIENTO SIMULTANEA COMPRA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CAVCTS','AB','CTA','VENCIMIENTO SIMULTANEA VENTA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('CAANSV','CA','CTA','ANTICIPO SIMULTANEA VENTA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('ABANSC','AB','CTA','ANTICIPO SIMULTANEA COMPRA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ABTPRE','AB','CTA','TOTAL FACTURA PREMIO','VIG');
DELETE FROM "BSCL.Plataforma.BD::PLA.Concepto" 
WHERE "IdConcepto" in ('CACCAC','CACCAN','CANCAC','CANCAN','CATCAC','CATCAN','ABTVAC','ABTVAN');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CACCAX','CA','CTA','COBROS FACTURA COMPRA ACCIONES EXTRANJERAS','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CACCAN','CA','CTA','COBROS FACTURA COMPRA ACCIONES NACIONALES','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CANCAX','CA','CTA','NETO FACTURA COMPRA ACCIONES EXTRANJERAS','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CANCAN','CA','CTA','NETO FACTURA COMPRA ACCIONES NACIONALES','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CATCAX','CA','CTA','TOTAL FACTURA COMPRA ACCIONES EXTRANJERAS','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CATCAN','CA','CTA','TOTAL FACTURA COMPRA ACCIONES NACIONALES','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ABTVAX','AB','CTA','TOTAL FACTURA VENTA ACCIONES EXTRANJERAS','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ABTVAN','AB','CTA','TOTAL FACTURA VENTA ACCIONES NACIONALES','VIG');
DELETE FROM "BSCL.Plataforma.BD::PLA.Concepto" 
WHERE "IdConcepto" in ('CACVAC','CACVAN','ABNVAC','ABNVAN','COMFUT','VENFUT');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CACVAX','CA','CTA','COBROS FACTURA VENTA ACCIONES EXTRANJERAS','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CACVAN','CA','CTA','COBROS FACTURA VENTA ACCIONES NACIONALES','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ABNVAX','AB','CTA','NETO FACTURA VENTA ACCIONES EXTRANJERAS','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ABNVAN','AB','CTA','NETO FACTURA VENTA ACCIONES NACIONALES','VIG');


--------------------PERFILES-----------------------------------------------
SET schema "BSCL";
--Secuencia tabla Perfil
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::Perfil"
       START     WITH 3
       INCREMENT BY 1
       MINVALUE  1; 

CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::PerfilAcceso"
       START     WITH 9
       INCREMENT BY 1
       MINVALUE  1; 

--Agregar a servicios
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(138,'BSCL.Plataforma.BD.Servicios::ACC.CrearPerfil',0,'P','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(148,'BSCL.Plataforma.BD.Servicios::ACC.ActualizarPerfilAcceso',0,'P','');

--Agregar estados Perfiles
insert into "BSCL.Plataforma.BD::PLA.EstadoPla"("IdEstadoPla", "IdEstado", "NombreEntidad", "Descripcion") 
	values(8, 'ACT', 'Perfil', 'Activo');
insert into "BSCL.Plataforma.BD::PLA.EstadoPla"("IdEstadoPla", "IdEstado", "NombreEntidad", "Descripcion") 
	values(9, 'INA', 'Perfil', 'Inactivo');

--Crear Nuevas Tablas accesos
drop table "BSCL"."BSCL.Plataforma.BD::ACC.Acceso" cascade;
create column table "BSCL.Plataforma.BD::ACC.Acceso" (
   "IdAcceso"  				BIGINT              not null, -- Id del acceso
   "NombreAcceso"			NVARCHAR(100)		not null, -- Nombre a desplegar en la pantalla de asignacion de accesos
   "TipoAcceso"				NVARCHAR(5)			not null, -- MENU1,MENU2,MENU3,BOTON,CMBX, INPT,SRV 
   "Padre"					BIGINT				not null, -- 0: Opcion de menu, IdAcceso en caso de ser hijo de algun nodo
   "Orden"			       	INT          	    not null, -- Orden por el cual se desplegaran las opciones
   "IdServicio"			  	BIGINT			    not null, -- el id del servicio que necesita el componente (entidad servicio)
   "EsVisibleMenu"			NVARCHAR(1)			not null, -- solo para opciones de menu, valores 'S' o 'N'					
   primary key ("IdAcceso")
);

drop table "BSCL.Plataforma.BD::ACC.PerfilAcceso" cascade;
create column table "BSCL.Plataforma.BD::ACC.PerfilAcceso" (
   "IdAccesoPerfil"  		BIGINT              not null,
   "IdEmpresa"				BIGINT				not null,
   "IdPerfil"  				BIGINT              not null, 
   "IdAcceso"				BIGINT				not null,
   "IdEstado"		 		NVARCHAR(3)			not null,
   primary key ("IdAccesoPerfil")
);

--Accesos Pantalla Consulta Usuarios
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "EsVisibleMenu")
VALUES(1,'PLATAFORMA', 'MENU1', 0, 13, 0, 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "EsVisibleMenu")
VALUES(2,'Usuarios', 'MENU2', 1, 1, 0, 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "EsVisibleMenu")
VALUES(3,'Pantalla Crear Usuario', 'BTN', 2, 1 ,0 ,'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "EsVisibleMenu")
VALUES(4,'Pantalla Modificar Usuario', 'BTN', 2 , 2 ,0 , 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso","Padre","Orden","IdServicio", "EsVisibleMenu")
VALUES(5,'Pantalla Administrar Roles', 'BTN', 2 , 3, 0, 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso","Padre","Orden","IdServicio", "EsVisibleMenu")
VALUES(6,'Pantalla Administrar Perfiles', 'BTN', 2 ,4 ,0 , 'S');

--Accesos pantalla creacion usuario
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "EsVisibleMenu")
VALUES(7,'Crear Usuario', 'BTN',3, 1, 1, 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "EsVisibleMenu")
VALUES(8,'Modificar Usuario', 'BTN', 4, 2, 2, 'S');

--OPCIONES MENU PADRE
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" 
( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "EsVisibleMenu")
VALUES(9,'MERCADO', 'MENU1', 0, 1, 0, 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" 
( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "EsVisibleMenu")
VALUES(10,'SOCIO DE NEGOCIO', 'MENU1', 0, 2, 0, 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" 
( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "EsVisibleMenu")
VALUES(11,'OPERACIONES EN RUEDA', 'MENU1', 0, 3, 0, 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" 
( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "EsVisibleMenu")
VALUES(12,'CUENTA INVERSION', 'MENU1', 0, 4, 0, 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" 
( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "EsVisibleMenu")
VALUES(13,'CUSTODIA', 'MENU1', 0, 5, 0, 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" 
( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "EsVisibleMenu")
VALUES(14,'FACTURACION', 'MENU1', 0, 6, 0, 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" 
( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "EsVisibleMenu")
VALUES(15,'TESORERIA', 'MENU1', 0, 7, 0, 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" 
( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "EsVisibleMenu")
VALUES(16,'CONTABILIDAD', 'MENU1', 0, 8, 0, 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" 
( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "EsVisibleMenu")
VALUES(17,'RENTA FIJA E IIF', 'MENU1', 0, 9, 0, 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" 
( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "EsVisibleMenu")
VALUES(18,'SPOT', 'MENU1', 0, 10, 0, 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" 
( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "EsVisibleMenu")
VALUES(19,'FORWARD', 'MENU1', 0, 11, 0, 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" 
( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "EsVisibleMenu")
VALUES(20,'NORMATIVO	', 'MENU1', 0, 12, 0, 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" 
( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "EsVisibleMenu")
VALUES(21,'DESARROLLO', 'MENU1', 0, 14, 0, 'S');

--Relaciones Perfil-Acceso
INSERT INTO "BSCL.Plataforma.BD::ACC.PerfilAcceso"("IdAccesoPerfil", "IdEmpresa", "IdPerfil", "IdAcceso", "IdEstado")
VALUES (1,1,1,1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::ACC.PerfilAcceso"("IdAccesoPerfil", "IdEmpresa", "IdPerfil", "IdAcceso", "IdEstado")
VALUES (2,1,1,2,'ACT');
INSERT INTO "BSCL.Plataforma.BD::ACC.PerfilAcceso"("IdAccesoPerfil", "IdEmpresa", "IdPerfil", "IdAcceso", "IdEstado")
VALUES (3,1,1,3,'ACT');
INSERT INTO "BSCL.Plataforma.BD::ACC.PerfilAcceso"("IdAccesoPerfil", "IdEmpresa", "IdPerfil", "IdAcceso", "IdEstado")
VALUES (4,1,1,4,'ACT');
INSERT INTO "BSCL.Plataforma.BD::ACC.PerfilAcceso"("IdAccesoPerfil", "IdEmpresa", "IdPerfil", "IdAcceso", "IdEstado")
VALUES (5,1,1,5,'ACT');
INSERT INTO "BSCL.Plataforma.BD::ACC.PerfilAcceso"("IdAccesoPerfil", "IdEmpresa", "IdPerfil", "IdAcceso", "IdEstado")
VALUES (6,1,1,6,'ACT');
INSERT INTO "BSCL.Plataforma.BD::ACC.PerfilAcceso"("IdAccesoPerfil", "IdEmpresa", "IdPerfil", "IdAcceso", "IdEstado")
VALUES (7,1,1,7,'ACT');
INSERT INTO "BSCL.Plataforma.BD::ACC.PerfilAcceso"("IdAccesoPerfil", "IdEmpresa", "IdPerfil", "IdAcceso", "IdEstado")
VALUES (8,1,1,8,'ACT');
--------------------FIN PERFILES-----------------------------------------------
--
--
-- LIBERACION SEMANA: 12-02-2016
--

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(157,'BSCL.Mercado.BD.Servicios::ValorizarRV',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(158,'BSCL.Orden.BD.Servicios::ModificarTransaccion',0,'P','');

----------------------------------------------------------------------------------------------------
--------------------------------------MODIFICACION USUARIO------------------------------------------
----------------------------------------------------------------------------------------------------
set schema "BSCL";
UPDATE "BSCL.Plataforma.BD::PLA.Modulo" SET "Codigo" = 'PLA', "Nombre" = 'Plataforma'
WHERE "IdModulo" = 'PLA';
UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.EstadoPla"
SET "IdEstado" = 'INA', "Descripcion" = 'Inactivo'
WHERE "IdEstadoPla" = 9;

UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.EstadoPla"
SET "IdEstado" = 'INA', "Descripcion" = 'Inactivo'
WHERE "IdEstadoPla" = 7;

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(159,'BSCL.Plataforma.BD.Servicios::ACC.ActualizarUsuario',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(160,'BSCL.Plataforma.BD.Servicios::ACC.DesbloqueoTemporalUsuario',0,'P','');

--Actualizar fechas de vigencia de los usuarios al maximo del widget datepicker.
UPDATE "BSCL"."BSCL.Plataforma.BD::ACC.Usuario" SET "FechaVigencia" = '2099-12-30';
UPDATE "BSCL"."BSCL.Plataforma.BD::ACC.Usuario" SET "FechaBloqueoTemporal" = '0001-01-01 00:00:00 000';
----------------------------------------------------------------------------------------------------
----------------------------------FIN MODIFICACION USUARIO------------------------------------------
----------------------------------------------------------------------------------------------------
