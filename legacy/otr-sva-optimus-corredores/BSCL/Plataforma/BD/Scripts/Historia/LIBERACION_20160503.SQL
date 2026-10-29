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

--**** NO BORRAR - AUMENTAR EN CADA LIBERACION ***
UPDATE "BSCL.Plataforma.BD::PLA.Configuracion" SET "Valor" ='20160414' WHERE "CodigoParametro" =  'Version';
commit;


--
--
-- LIBERACION SEMANA: xx-05-2016 (Regreso de Peru)
--

--################################################
--    JG - REQUISITOS OPERATIVOS
--################################################
DELETE FROM "BSCL.Plataforma.BD::REQ.CondicionOperativa";

INSERT INTO "BSCL.Plataforma.BD::REQ.CondicionOperativa" ("IdCondicionOperativa","Nombre","RequiereAtributo1","RequiereAtributo2","IdTipoComportamientoDefecto","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('PERNATFALLJURDIS','PERSONA NATURAL FALLECIDA/JURÍDICA DISUELTA','N','N','AUTORIZACION',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.CondicionOperativa" ("IdCondicionOperativa","Nombre","RequiereAtributo1","RequiereAtributo2","IdTipoComportamientoDefecto","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('PORTBLOQ','PORTAFOLIO BLOQUEADO','N','N','AUTORIZACION',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.CondicionOperativa" ("IdCondicionOperativa","Nombre","RequiereAtributo1","RequiereAtributo2","IdTipoComportamientoDefecto","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('CONTCONDGENFUT','CONTRATO CONDICIONES GENERALES FUTURO','N','N','AUTORIZACION',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.CondicionOperativa" ("IdCondicionOperativa","Nombre","RequiereAtributo1","RequiereAtributo2","IdTipoComportamientoDefecto","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('CONTSIM','CONTRATO DE SIMULTANEAS','N','N','AUTORIZACION',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.CondicionOperativa" ("IdCondicionOperativa","Nombre","RequiereAtributo1","RequiereAtributo2","IdTipoComportamientoDefecto","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('CONTVENCOR','CONTRATO DE VENTA CORTA','N','N','AUTORIZACION',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.CondicionOperativa" ("IdCondicionOperativa","Nombre","RequiereAtributo1","RequiereAtributo2","IdTipoComportamientoDefecto","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('CONTCONDFORW','CONTRATO CONDICIONES DE FORWARD','N','N','AUTORIZACION',1,NOW(),NOW(),1,'ACT');


INSERT INTO "BSCL.Plataforma.BD::REQ.EstadoCondicionOperativa" ("IdEstadoCondicionOperativa","IdEstado")
VALUES(1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.EstadoCondicionOperativa" ("IdEstadoCondicionOperativa","IdEstado")
VALUES(2,'INA');


DELETE FROM "BSCL.Plataforma.BD::REQ.TipoEtapa";
/*
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoEtapa" ("IdTipoEtapa","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('INGRESO','INGRESO',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoEtapa" ("IdTipoEtapa","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('ASINACION','ASIGNACIÓN',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoEtapa" ("IdTipoEtapa","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('ENVFACT','ENVÍO A FACTURAR',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoEtapa" ("IdTipoEtapa","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('GCOMFACT','GENERACIÓN DE COMPROBANTE DE FACTURACIÓN',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoEtapa" ("IdTipoEtapa","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('FACTDTE','FACTURACIÓN DTE',1,NOW(),NOW(),1,'ACT');
*/
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoEtapa" ("IdTipoEtapa","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('INGRESOORDEN','INGRESO ORDEN',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoEtapa" ("IdTipoEtapa","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('INGRESOORDENSIM','INGRESO ORDEN SIMULTANEAS',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoEtapa" ("IdTipoEtapa","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('INGRESOORDENVC','INGRESO ORDEN VENTA CORTA',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoEtapa" ("IdTipoEtapa","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('INGRESOFORWARD','INGRESO FORWARD',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoEtapa" ("IdTipoEtapa","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('ASIGNARORDEN','ASIGNAR ORDEN',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoEtapa" ("IdTipoEtapa","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('FACTURAR','FACTURAR',1,NOW(),NOW(),1,'ACT');


INSERT INTO "BSCL.Plataforma.BD::REQ.TipoComportamiento" ("IdTipoComportamiento","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('ADVERTENCIA','ADVERTENCIA',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoComportamiento" ("IdTipoComportamiento","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('RECHAZO','RECHAZO',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoComportamiento" ("IdTipoComportamiento","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('AUTORIZACION','AUTORIZACION',1,NOW(),NOW(),1,'ACT');

INSERT INTO "BSCL.Plataforma.BD::REQ.TipoOperacionUsuario" ("IdTipoOperacionUsuario","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('ENROCLI','Enrolamiento Cliente',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoOperacionUsuario" ("IdTipoOperacionUsuario","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('ENROPER','Enrolamiento Persona',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoOperacionUsuario" ("IdTipoOperacionUsuario","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('NUEPORTF','Creación Portafolio',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoOperacionUsuario" ("IdTipoOperacionUsuario","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('COMACN','Compra Acciones RV',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoOperacionUsuario" ("IdTipoOperacionUsuario","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('VENACN','Venta Acciones RV',1,NOW(),NOW(),1,'ACT');


INSERT INTO "BSCL.Plataforma.BD::REQ.ModeloRequisitoOperativo"
("IdModeloRequisitoOperativo","Descripcion","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('CORREDORA','MODELO DE CORREDORA',1,NOW(),NOW(),1,'ACT');


ALTER TABLE "BSCL.Plataforma.BD::REQ.RequisitoOperativo" DROP ("IdTipoOperacionUsuario");


--################################################
--    MMACHMAR - Inscripcion Servicios REQUISITOS OPERATIVOS
--################################################
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(270,'BSCL.Plataforma.BD.Servicios::REQ.CrearRequisitoOperativo',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(271,'BSCL.Plataforma.BD.Servicios::REQ.ModificarRequisitoOperativo',0,'P','');


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (299, 'BSCL.Custodia.BD.Servicios::EVC.CerrarEventoCapitalDV' , 1,  'P', 'Cierra un Evento Capital Cliente (DV)');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (300, 'BSCL.Custodia.BD.Servicios::EVC.CerrarEventoCapitalDA' , 1,  'P', 'Cierra un Evento Capital Cliente (DA)');      


insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('INGGTA','INGGTA','INGRESO GARANTIA','INGRESO','CUST','N','N','N','N','N','INGRESO','INGRESO','N','');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('EGRGTA','EGRGTA','EGRESO GARANTIA','EGRESO','CUST','N','N','N','N','N','EGRESO','EGRESO','N','');


--################################################
--    JG - MODELO COMERCIAL
--################################################
SET SCHEMA BSCL;
DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::COM.ModificarPerfilCobro";
DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::COM.ConfirmarModificarPerfilCobro";
DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::COM.RequiereAutorizacionPerfilCobro";
DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::COM.ModificarPerfilCobroDetalle";
DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::COM.ConfirmarModificarPerfilCobroDetalle";
DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::COM.CrearPerfilCobro";
DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::COM.CrearPerfilCobroDetalle"  ;
DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::COM.ConfirmarCrearPerfilCobroDetalle";

UPDATE "BSCL.Plataforma.BD::PLA.Servicio"
SET "RutaServicio" = 'BSCL.Plataforma.BD.Servicios::COM.ModificarPerfilCobros'
WHERE "IdServicio" = 87  and "RutaServicio" = 'BSCL.Plataforma.BD.Servicios::COM.ModificarPerfilCobro';

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(272,'BSCL.Plataforma.BD.Servicios::COM.ModificarPerfilCobrosDetalle',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(273,'BSCL.Plataforma.BD.Servicios::COM.CrearPerfilCobrosDetalle',0,'P','');


--################################################
--    CFLORES - Modificacion de Limites con flujo de autorizaciones
--################################################

SET SCHEMA "BSCL"; --Agregar campos

ALTER TABLE "BSCL.Plataforma.BD::LIM.Limite"
ADD("IdPlantillaLimite"			NVARCHAR(40)		default '' not null,
	"FechaIngreso"       		SECONDDATE          default '0001-01-01 00:00:00' not null,
    "FechaCreacion"     		SECONDDATE          default '0001-01-01 00:00:00' not null,
    "FechaModificacion"  		SECONDDATE          default '0001-01-01 00:00:00' not null,
	"IdEstadoAutorizacion" 		NVARCHAR(3)         default 'PEN' not null,
	"Borrado" 					NVARCHAR(1) DEFAULT 'N',
    "IdUsuarioCreador"   		BIGINT  DEFAULT 0,
    "IdTipoProducto"			NVARCHAR(4)         not null default '');


DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilLimites"; 

SET SCHEMA "BSCL"; --crear tabla
create column table "BSCL.Plataforma.BD::LIM.PlantillaLimite" (
	"IdPlantillaLimite"					NVARCHAR(40)		not null,
	"DescripcionPlantillaLimite"		NVARCHAR(100)		not null,
	"IdCategoriaInversionista"			NVARCHAR(40)		not null,
	"FechaCreacion"						DATE				not null,
	"FechaModificacion"					DATE				not null,
	"FechaIngreso" 						SECONDDATE CS_SECONDDATE	 DEFAULT '0001-01-01 00:00:00' NOT NULL ,
	"IdEstadoAutorizacion"				NVARCHAR(3) 				 DEFAULT '' NOT NULL ,--*
	"IdUsuarioCreador"  				BIGINT 						 DEFAULT 0,
	"IdEstado"					   		NVARCHAR(3)			not null,
	"PorDefecto"						NVARCHAR(1)			DEFAULT 'N' not null,
	"IdEmpresa"							BIGINT				not null,
	primary key ("IdPlantillaLimite")
);

SET SCHEMA "BSCL"; --renombrar columna
RENAME COLUMN "BSCL.Plataforma.BD::LIM.Limite"."IdCanal" 	TO "IdCanalIngreso";

SET SCHEMA "BSCL"; --borrar columna
ALTER TABLE "BSCL.Plataforma.BD::LIM.Limite" DROP ("IdTipoInstrumento");

SET SCHEMA "BSCL"; --borrar columna
ALTER TABLE "BSCL.Plataforma.BD::LIM.Limite" DROP ("IdUsuario",	"IdCliente","IdPortafolio");
						


SET SCHEMA "BSCL"; --crear secuencia
create sequence "BSCL.Plataforma.BD.Secuencias::Limite" increment by 1 start with 10000 minvalue 10000 maxvalue 4611686018427387903 no cycle;


INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(292,'BSCL.Plataforma.BD.Servicios::LIM.CrearPlantillaLimite',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(293,'BSCL.Plataforma.BD.Servicios::LIM.CrearPlantillaLimiteDetalle',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(294,'BSCL.Plataforma.BD.Servicios::LIM.ModificarPlantillaLimite',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(295,'BSCL.Plataforma.BD.Servicios::LIM.ModificarPlantillaLimiteDetalle',0,'P','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(306,'BSCL.Plataforma.BD.Servicios::LIM.ModificarLimite',0,'P','');


INSERT INTO "BSCL.Plataforma.BD::LIM.GrupoLimite"("IdGrupoLimite","Descripcion")
VALUES('PORTAFOLIO','PORTAFOLIO');

INSERT INTO "BSCL.Plataforma.BD::LIM.TipoLimite"("IdTipoLimite","IdEmpresa","Nombre","EsCuentaPropia","IdComportamiento","IdGrupoLimite","Categoria","IdEstado")
VALUES('LIMPORTPRODUCTO',1,'LÍMITE PORTAFOLIO PRODUCTO','N','RZD','PORTAFOLIO','OPERACION','ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NCPLIM','PC','PLA','NUEVA PLANTILLA LMITES','VIG');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NCPLIM','N');
INSERT INTO "BSCL.Plataforma.BD::AUT.TipoEvento" 
VALUES('NCPLIM',1,'PLA','AUT_PLALIM_NUE','Nueva Plantilla Límites',1,'S','' ,'' ,0,0,'VIG');

----FIN DE LIMITES


--################################################
--    NROJAS - Conceptos
--################################################							
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('FEPRES','','FAC','FACTURA ELECTRONICA DE PRESTAMO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('FOPRES','','FAC','FACTURA OTROS CONCEPTOS DE PRESTAMO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('FECMAX','','FAC','FACTURA ELECTRONICA DE COMPRA DE ACCIONES EXTRANJERAS','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('FEVNAX','','FAC','FACTURA ELECTRONICA DE VENTA DE ACCIONES EXTRANJERAS','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('FECMMI','','FAC','FACTURA ELECTRONICA DE COMPRA DE ACCIONES MILA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('FEVNMI','','FAC','FACTURA ELECTRONICA DE VENTA DE ACCIONES MILA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('FECMRF','','FAC','FACTURA ELECTRONICA DE COMPRA DE RF','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('FEVNRF','','FAC','FACTURA ELECTRONICA DE VENTA DE RF','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('FECMIF','','FAC','FACTURA ELECTRONICA DE COMPRA DE IF','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('FEVNIF','','FAC','FACTURA ELECTRONICA DE VENTA DE IF','VIG');

--################################################
--    JPZ - Conceptos
--################################################							
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" ) 
VALUES ('ABDIMO','AB','CTA','ABONO DIVIDENDO EN DINERO','VIG');

INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES ('ABDIMT','AB','TES','DIVIDENDO EN DINERO','VIG');


--################################################
--    CHERNANDEZ - Integracion cierre diario ordenes, con modulo de cierre.
--################################################	
INSERT INTO "BSCL.Plataforma.BD::PLA.CREModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (261, 1, 'UI_CUADRAR_ORDENES',  'UI_CUADRAR_ORDENES', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.CREModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (262, 1, 'UI_VENCER_ORDENES',  'UI_VENCER_ORDENES', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.CREModuloFuncionProceso" ("IdModuloFuncionProceso", "IdCREModuloEmpresa", "NombreFuncionProceso", "Descripcion", "IdEstado")
VALUES (263, 1, 'UI_OFICIALIZAR_ORDENES',  'UI_OFICIALIZAR_ORDENES', 'ACT');

--################################################
--    CESPINOSA - Cierre modulos.
--################################################	
alter table "BSCL.Plataforma.BD::PLA.CREModuloFuncionProceso"
	alter (
		"NombreFuncionProceso" 			NVARCHAR(100)
	);


--################################################
--    CSTANGE - VALORIZADOR.
--################################################	
DROP TYPE BSCL."BSCL.Plataforma.BD.Tipos::INS.TareaValorizacion";

INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor")
VALUES(1, 'RutinaValorizador', 'Rutina usada en el valorizador', 'BCS');   

drop TABLE BSCL."BSCL.Plataforma.BD::INS.TareaValorizacion";
CREATE COLUMN TABLE BSCL."BSCL.Plataforma.BD::INS.TareaValorizacion" (
	 --- DATOS TAREA
	 "IdTarea" 						BIGINT 			not null,
	 "GrupoValorizacion"			BIGINT			not null,	 
	 "FechaSolicitud" 				SECONDDATE		not null,
 	 "IdEstado" 					NVARCHAR(3) 	not null,
 	 
 	 -- DATOS VALORIZADOR
	 "RutinaValorizacion" 			NVARCHAR(4) 	not null, ----BCS  
	 "OpcionValorizacion" 			NVARCHAR(4) 	not null, ----Las mismas que el valorizador  
	 "TipoValorizacion"				NVARCHAR(4) 	not null, ----Tipo de valorizacion a usar x valorizador 

	 -- DATOS ENTRADA / SALIDA
	 "FechaValorizacion" 			DATE			not null,
	 "IdTipoInstrumento"			NVARCHAR(3) 	,
	 "IdInstrumento" 				BIGINT 			,
	 "FolioCartera"       			BIGINT			,	 	 
	 "Nemotecnico" 					NVARCHAR(40)	,		 
	 "IdMoneda"						NVARCHAR(10)	, -- IF Moneda Reajuste
	 "Cantidad"						DECIMAL(26,8) 	,	
	 "Precio"						DECIMAL(26,8) 	,
	 "Tasa"							DECIMAL(26,8) 	,	
	 "TasaEstimada"					DECIMAL(26,8) 	,
	 "Rescate"						DECIMAL(26,8) 	, -- IF Rescate
	 "Captacion"					DECIMAL(26,8) 	, -- IF Captacion
	 "MontoMonedaInstrumento"		DECIMAL(26,8)   , -- IF captacion, Monto VP RF moneda instrumento
     "MontoMonedaLocal"				DECIMAL(26,8)   , -- IF captacion moneda local
	 "ValorPar"						DECIMAL(26,8) 	,
	 "Duration"						DECIMAL(26,8) 	,
	 "Convexidad"					DECIMAL(26,8) 	,
	 "TipoUnidad"       			INT			    ,	 	 
	 "ValorMoneda"       			DECIMAL(26,8)	,	 		
	 "ValorReferencial"       		CHAR(1)			, 
	 "TipoTasaUtilizada"       		INT			    ,
	 "TasaPago"       				DECIMAL(26,8)	,
	 "TasaSVS"       				DECIMAL(26,8)	,			 
	 "FechaVencimiento"				DATE 			, -- IF
	 PRIMARY KEY ("IdTarea")
);

--################################################
--    OCARRENO - ANTICIPO DE SIMULTANEAS Y PRESTAMOS
--################################################
alter table "BSCL.Plataforma.BD::INS.TipoOperacion"
	ADD (
		"EsAnticipo" 			NVARCHAR(1) default 'N' not null,
		"EsVencimiento" 		NVARCHAR(1) default 'N' not null,
		"EsAnulacion" 			NVARCHAR(1) default 'N' not null
	);

ALTER TABLE "BSCL.Plataforma.BD::INS.TipoOperacion" DROP ("EsIngresoEgresoTitulo");

insert into "BSCL"."BSCL.Plataforma.BD::INS.TipoOperacion" (
"IdTipoOperacion","Codigo","Nombre","Accion","IdTipoProducto","Comprobante","EsOrden","EsSimultanea",
"EsVentaCorta","EsTransaccion","AccionOperacion","AccionCustodia","IdTipoOperacionRelacionada",
"RecibePagaPortafolio","EsAnticipo","EsVencimiento","EsAnulacion" )
values ('ANSIMC','ANSIMC','ANTICIPO SIMULTANEA DE COMPRA', 'VENTA','INSI','N','N','N','N','N','VENTA','RETIRO','ANSIMV','','S','N','N');

insert into "BSCL"."BSCL.Plataforma.BD::INS.TipoOperacion" (
"IdTipoOperacion","Codigo","Nombre","Accion","IdTipoProducto","Comprobante","EsOrden","EsSimultanea",
"EsVentaCorta","EsTransaccion","AccionOperacion","AccionCustodia","IdTipoOperacionRelacionada",
"RecibePagaPortafolio","EsAnticipo","EsVencimiento","EsAnulacion")
values ('ANSIMV','ANSIMV','ANTICIPO SIMULTANEA DE VENTA', 'COMPRA','INSI','N','N','N','N','N','COMPRA','INGRESO','ANSIMC','','S','N','N');

insert into "BSCL"."BSCL.Plataforma.BD::INS.TipoOperacion" (
"IdTipoOperacion","Codigo","Nombre","Accion","IdTipoProducto","Comprobante","EsOrden","EsSimultanea",
"EsVentaCorta","EsTransaccion","AccionOperacion","AccionCustodia","IdTipoOperacionRelacionada",
"RecibePagaPortafolio","EsAnticipo","EsVencimiento","EsAnulacion")
values ('ANTPRT','ANTPRT','ANTICIPO DE PRESTAMO', 'VENTA','INAC','N','N','N','N','N','VENTA','RETIRO','ANTEMP','','S','N','N');

insert into "BSCL"."BSCL.Plataforma.BD::INS.TipoOperacion" (
"IdTipoOperacion","Codigo","Nombre","Accion","IdTipoProducto","Comprobante","EsOrden","EsSimultanea",
"EsVentaCorta","EsTransaccion","AccionOperacion","AccionCustodia","IdTipoOperacionRelacionada",
"RecibePagaPortafolio","EsAnticipo","EsVencimiento","EsAnulacion")
values ('ANTEMP','ANTEMP','ANTICIPO DE EMPRESTITO', 'VENTA','INAC','N','N','N','N','N','VENTA','RETIRO','ANTPRT','','S','N','N');

---------------------
insert into "BSCL"."BSCL.Plataforma.BD::INS.TipoOperacion" (
"IdTipoOperacion","Codigo","Nombre","Accion","IdTipoProducto","Comprobante","EsOrden","EsSimultanea",
"EsVentaCorta","EsTransaccion","AccionOperacion","AccionCustodia","IdTipoOperacionRelacionada",
"RecibePagaPortafolio","EsAnticipo","EsVencimiento","EsAnulacion" )
values ('VESIMC','VESIMC','VENCIMIENTO SIMULTANEA DE COMPRA', 'VENTA','INSI','N','N','N','N','N','VENTA','RETIRO','VESIMV','','N','S','N');

insert into "BSCL"."BSCL.Plataforma.BD::INS.TipoOperacion" (
"IdTipoOperacion","Codigo","Nombre","Accion","IdTipoProducto","Comprobante","EsOrden","EsSimultanea",
"EsVentaCorta","EsTransaccion","AccionOperacion","AccionCustodia","IdTipoOperacionRelacionada",
"RecibePagaPortafolio","EsAnticipo","EsVencimiento","EsAnulacion")
values ('VESIMV','VESIMV','VENCIMIENTO SIMULTANEA DE VENTA', 'COMPRA','INSI','N','N','N','N','N','COMPRA','INGRESO','VESIMC','','N','S','N');

insert into "BSCL"."BSCL.Plataforma.BD::INS.TipoOperacion" (
"IdTipoOperacion","Codigo","Nombre","Accion","IdTipoProducto","Comprobante","EsOrden","EsSimultanea",
"EsVentaCorta","EsTransaccion","AccionOperacion","AccionCustodia","IdTipoOperacionRelacionada",
"RecibePagaPortafolio","EsAnticipo","EsVencimiento","EsAnulacion")
values ('VENPRT','VENPRT','VENCIMIENTO DE PRESTAMO', 'VENTA','INAC','N','N','N','N','N','VENTA','RETIRO','VENEMP','','N','S','N');

insert into "BSCL"."BSCL.Plataforma.BD::INS.TipoOperacion" (
"IdTipoOperacion","Codigo","Nombre","Accion","IdTipoProducto","Comprobante","EsOrden","EsSimultanea",
"EsVentaCorta","EsTransaccion","AccionOperacion","AccionCustodia","IdTipoOperacionRelacionada",
"RecibePagaPortafolio","EsAnticipo","EsVencimiento","EsAnulacion")
values ('VENEMP','VENEMP','VENCIMIENTO DE EMPRESTITO', 'VENTA','INAC','N','N','N','N','N','VENTA','RETIRO','VENPRT','','N','S','N');


UPDATE tiope 
SET
	"AccionCustodia" = CASE WHEN tiope."AccionCustodia" = 'COMPRA' THEN 'INGRESO'
							WHEN tiope."AccionCustodia" = 'INGRESO' THEN 'INGRESO'
							WHEN tiope."AccionCustodia" = 'VENTA' THEN 'RETIRO'
							WHEN tiope."AccionCustodia" = 'EGRESO' THEN 'RETIRO'
							WHEN tiope."AccionCustodia" = 'RETIRO' THEN 'RETIRO'
							WHEN tiope."Accion" = 'INGRESO' THEN 'INGRESO'
							WHEN tiope."Accion" = 'EGRESO' THEN 'RETIRO'
							WHEN tiope."Accion" = 'TRASPASO' THEN 'TRASPASO'
					   ELSE ''
					   END
FROM "BSCL"."BSCL.Plataforma.BD::INS.TipoOperacion" tiope;


--################################################
--    MMACHMAR - Valores Defecto Sistema
--################################################

CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.ValorDefectoSistema"(
	"IdValorDefectoSistema"	NVARCHAR(40)	NOT NULL,
	"Nombre"				NVARCHAR(255)	NOT NULL DEFAULT '',
	"Valor"					NVARCHAR(255)	NOT NULL DEFAULT '',
	"IdEmpresa"				BIGINT			NOT NULL DEFAULT 0,
	"IdUsuarioCreador"		BIGINT			NOT NULL DEFAULT 0,
	"FechaCreacion"			SECONDDATE		NOT NULL DEFAULT '0001-01-01 00:00:00',
	"FechaModificacion"		SECONDDATE		NOT NULL DEFAULT '0001-01-01 00:00:00',
	"IdEstado"				NVARCHAR(3)		DEFAULT 'ACT' NOT NULL,
  	PRIMARY KEY ("IdValorDefectoSistema")
);

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdTipoCliente','Tipo Cliente','RETAIL',1,1,NOW(),NOW(),'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdTipoDocumentoIdentidad','Tipo Documento Identidad','R',1,1,NOW(),NOW(),'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdTipoCuentaBancaria','Tipo Cuenta Bancaria','CUCO',1,1,NOW(),NOW(),'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdTipoLibroOrden','Tipo Libro','NORMAL',1,1,NOW(),NOW(),'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdEstadoLibroOrden','Estado Libro Orden','ACT',1,1,NOW(),NOW(),'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdEstadoPlantillaLimitesDetalle','Estado Plantilla Límites Detalle','ACT',1,1,NOW(),NOW(),'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ValorDefectoSistema" ("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado") 
VALUES('IdMonedaPlantillaLimitesDetalle','Moneda Plantilla Límites Detalle','CLP',1,1,NOW(),NOW(),'ACT');


--PROJO: REFACTURACION
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES ('REF','REFACTURADO');
Insert into "BSCL.Plataforma.BD::PLA.Servicio" values (296,'BSCL.Orden.BD.Servicios::ObtenerDetalleRefacturacion',0,'P','Servicio obtencion detalle de Refacturacion');
Insert into "BSCL.Plataforma.BD::PLA.Servicio" values (297,'BSCL.Orden.BD.Servicios::RefacturarMovimiento',0,'P','Servicio Refacturacion de movimientos');
--PROJO: ESTADO ORDEN
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('ACP','ACEPTADO');


--################################################
--   JG (n_n) Cambios Operador.
--################################################

SET SCHEMA "BSCL";
ALTER TABLE "BSCL.Plataforma.BD::TRA.ConfiguracionOperador" DROP ("AsignMRV");
ALTER TABLE "BSCL.Plataforma.BD::TRA.ConfiguracionOperador" DROP ("MvtoFactMRV");
ALTER TABLE "BSCL.Plataforma.BD::TRA.ConfiguracionOperador" DROP ("AsignMMILA");
ALTER TABLE "BSCL.Plataforma.BD::TRA.ConfiguracionOperador" DROP ("MvtoFactMMILA");
ALTER TABLE "BSCL.Plataforma.BD::TRA.ConfiguracionOperador" DROP ("AsignMDEUDA");
ALTER TABLE "BSCL.Plataforma.BD::TRA.ConfiguracionOperador" DROP ("MvtoFactMDEUDA");
ALTER TABLE "BSCL.Plataforma.BD::TRA.ConfiguracionOperador" DROP ("AsignMDERIV");
ALTER TABLE "BSCL.Plataforma.BD::TRA.ConfiguracionOperador" DROP ("MvtoFactMDERIV");
ALTER TABLE "BSCL.Plataforma.BD::TRA.ConfiguracionOperador" DROP ("AsignMOTC");
ALTER TABLE "BSCL.Plataforma.BD::TRA.ConfiguracionOperador" DROP ("MvtoFactMOTC");


create column table "BSCL.Plataforma.BD::TRA.DetalleConfiguracionOperador" ( 
	"CodigoOperador"					NVARCHAR(10)	not null,
	"IdTipoInstrumento" 				NVARCHAR(3) 	not null,
	"AsignacionAuto"					NVARCHAR(1)		not null default 'N',
	"MovimientoFacturarAuto"			NVARCHAR(1)		not null default 'N',
	"IdEstado"							NVARCHAR(3)    	not null default 'ACT',
	"FechaCreacion"						DATE			not null default '0001-01-01',
	"FechaModificacion"					DATE			not null default '0001-01-01',
	"IdUsuarioCrea"						BIGINT			not null default 0, 
	"IdUsuarioModifica"					BIGINT			not null default 0,
	"IdEmpresa"							BIGINT			not null default 0,
	primary key ("CodigoOperador","IdTipoInstrumento","IdEmpresa")
);


create column table "BSCL.Plataforma.BD::TRA.TipoInstrumentoTransaccion" ( 
	"IdTipoInstrumento" 				NVARCHAR(3) 	not null,
	"IdEstado"							NVARCHAR(3)    	not null default 'ACT',
	"FechaCreacion"						DATE			not null default '0001-01-01',
	"FechaModificacion"					DATE			not null default '0001-01-01',
	"IdUsuarioCrea"						BIGINT			not null default 0, 
	"IdUsuarioModifica"					BIGINT			not null default 0,
	"IdEmpresa"							BIGINT			not null default 0,
	primary key ("IdTipoInstrumento","IdEmpresa")
);

DELETE FROM "BSCL.Plataforma.BD::TRA.TipoInstrumentoTransaccion";
INSERT INTO "BSCL.Plataforma.BD::TRA.TipoInstrumentoTransaccion" ("IdTipoInstrumento","IdEstado","FechaCreacion","FechaModificacion","IdUsuarioCrea","IdUsuarioModifica","IdEmpresa")	
VALUES('ACN','ACT',NOW(),NOW(),1,1,1);
INSERT INTO "BSCL.Plataforma.BD::TRA.TipoInstrumentoTransaccion" ("IdTipoInstrumento","IdEstado","FechaCreacion","FechaModificacion","IdUsuarioCrea","IdUsuarioModifica","IdEmpresa")	
VALUES('MIL','ACT',NOW(),NOW(),1,1,1);
INSERT INTO "BSCL.Plataforma.BD::TRA.TipoInstrumentoTransaccion" ("IdTipoInstrumento","IdEstado","FechaCreacion","FechaModificacion","IdUsuarioCrea","IdUsuarioModifica","IdEmpresa")	
VALUES('PAG','ACT',NOW(),NOW(),1,1,1);


INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
select max("IdTipoOperacionYCondicionDeLiquidacion")+1,'S','T0','DIVDIN','ACT' from "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion";

--################################################
--   JG (^_^) Placeholderssss Socio Negocio
--################################################
INSERT INTO "BSCL.Plataforma.BD::PLA.ValorDefectoSistema"
("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado")
VALUES('PLACEHOLDER_RUT','PLACEHOLDER_RUT','12345678-9',1,1,NOW(),NOW(),'ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.ValorDefectoSistema"
("IdValorDefectoSistema","Nombre","Valor","IdEmpresa","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEstado")
VALUES('PLACEHOLDER_TELEFONO','PLACEHOLDER_TELEFONO','+56 1 2345 6789',1,1,NOW(),NOW(),'ACT');

--################################################
--   JG (X_x) FATCA
--################################################
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MODFAT','N');

INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MODFAT','MP','SOC','MODIFICACION FATCA','VIG');

insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MODFAT',1,'SOC','AUT_FAT_MOD','Modificación Datos FATCA de la Persona',1,'S','' ,'' ,0,0,'VIG');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (304, 'BSCL.SocioNegocio.BD.Servicios::PER.ModificarPersonaFatca' , 1,  'P', ''); 
--################################################
--   JG Matriz Operacional como servicio
--################################################
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (305, 'BSCL.Plataforma.BD.Servicios::COM.ConsultarMatrizOperacional' , 1,  'P', ''); 
--################################################
--   JG-Incorpora acuerdo unión civil. SAP-3213
--################################################
INSERT INTO "BSCL.Plataforma.BD::REG.TipoEstadoCivil" ( "IdTipoEstadoCivil","Nombre","IdPais","IdEstado" )  
VALUES('CC','CONVIVIENTE CIVIL',44,'ACT');
--################################################
--   JG-Se agranda tamaño Codigo Operador
--################################################
ALTER TABLE "BSCL.Plataforma.BD::TRA.ConfiguracionOperador" 
alter ("CodigoOperador"			NVARCHAR(40)		not null);
ALTER TABLE "BSCL.Plataforma.BD::TRA.DetalleConfiguracionOperador" 
alter ("CodigoOperador"			NVARCHAR(40)		not null);

--################################################
--   KH - Clasificacion de Riesgo
--################################################
drop table "BSCL"."BSCL.Plataforma.BD::INS.EntidadClasificadoraRiesgo" cascade;
drop table "BSCL"."BSCL.Plataforma.BD::INS.TipoClasificacionRiesgo" cascade;
drop table "BSCL"."BSCL.Plataforma.BD::INS.ClasificacionRiesgo" cascade;
drop table "BSCL"."BSCL.Plataforma.BD::INS.EquivalenciaClasificacion"  cascade;
drop table "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo"  cascade;

create column table "BSCL"."BSCL.Plataforma.BD::INS.EntidadClasificadoraRiesgo" (
   "IdEntidadClasificadoraRiesgo"       NVARCHAR(10)   	not null,   
   "Nombre"                             NVARCHAR(100) 	default '' 	not null,
   primary key ("IdEntidadClasificadoraRiesgo")
);

create column table "BSCL"."BSCL.Plataforma.BD::INS.TipoClasificacionRiesgo" (
   "IdTipoClasificacionRiesgo"          NVARCHAR(4)   	not null,   
   "IdClasificacionTipoInstrumento" 	NVARCHAR(3)		default '' 	not null,
   "Codigo"                             NVARCHAR(40) 	default '' 	not null,
   "Nombre"                             NVARCHAR(100) 	default '' 	not null,
   "IdEstado"                           NVARCHAR(3) 		default '' 	not null,
   primary key ("IdTipoClasificacionRiesgo")
);

--"BSCL.Plataforma.BD.Vistas::ConsultarCoberturaGarantia";
create column table "BSCL"."BSCL.Plataforma.BD::INS.ClasificacionRiesgo" (
   "IdClasificacionRiesgo"        	BIGINT      		not null,
   "IdTipoClasificacionRiesgo"     	NVARCHAR(4)  		not null,
   "IdEntidadClasificadoraRiesgo"  	NVARCHAR(10)   		not null,   
   "ClasificacionRiesgo"     	NVARCHAR(20) 		default '' 	not null,   
   "Nivel"                         	INTEGER 			default '0' not null,
   "IdEstado"                      	NVARCHAR(3) 		default '' 	not null,
   primary key ("IdClasificacionRiesgo")
);

-- CONFIGURACION PARA EXIGIBILIDAD, COBERTURA Y PRESTAMOS: 
-- LISTA DE TIPOS DE INSTRUMENTOS agrupados por TIPO DE CLASIFICACION de Riesgo, los cuales deteminan papeles aconsiderar como exigibilidad de una simultanea
create column table "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" (
   "IdTipoInstrumentoClasificacionRiesgo" BIGINT      not null,
   "IdTipoClasificacionRiesgo"            NVARCHAR(4) default '' 	not null,
   "IdTipoInstrumento"                    NVARCHAR(3) default '' 	not null,
   "Nivel"                                INTEGER  	  default '0' not null, -- ELIMINAR
   "IdEstado"                             NVARCHAR(3) default '' 	not null,
   primary key ("IdTipoInstrumentoClasificacionRiesgo")
);


create column table "BSCL"."BSCL.Plataforma.BD::INS.EquivalenciaClasificacion" (
   "IdEquivalenciaClasificacion"    BIGINT      	not null,
   "IdClasificacionRiesgoOrigen"  	BIGINT      	not null,
   "IdClasificacionRiesgoDestino"  	BIGINT      	not null,
   primary key ("IdEquivalenciaClasificacion")
);

alter table "BSCL"."BSCL.Plataforma.BD::INS.TipoClasificacionRiesgo"
   add constraint "FK_Estado_TipoClasificacionRiesgo" foreign key ("IdEstado")
      references "BSCL"."BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

alter table "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo"
   add constraint "FK_Estado_TipoInstrumentoClasificacionRiesgo" foreign key ("IdEstado")
      references "BSCL"."BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

alter table "BSCL"."BSCL.Plataforma.BD::INS.ClasificacionRiesgo"
   add constraint "FK_Estado_ClasificacionRiesgo" foreign key ("IdEstado")
      references "BSCL"."BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

alter table "BSCL"."BSCL.Plataforma.BD::INS.EquivalenciaClasificacion"
   add constraint "FK_EquivalenciaClasificacion_ClasificacionOrigen" foreign key ("IdClasificacionRiesgoOrigen")
      references "BSCL"."BSCL.Plataforma.BD::INS.ClasificacionRiesgo"  ("IdClasificacionRiesgo") on delete restrict on update restrict;

alter table "BSCL"."BSCL.Plataforma.BD::INS.EquivalenciaClasificacion"
   add constraint "FK_EquivalenciaClasificacion_ClasificacionDestino" foreign key ("IdClasificacionRiesgoDestino")
      references "BSCL"."BSCL.Plataforma.BD::INS.ClasificacionRiesgo"  ("IdClasificacionRiesgo") on delete restrict on update restrict;

alter table "BSCL"."BSCL.Plataforma.BD::INS.ClasificacionRiesgo"
   add constraint "FK_ClasificacionRiesgo_TipoClasificacionRiesgo" foreign key ("IdTipoClasificacionRiesgo")
      references "BSCL"."BSCL.Plataforma.BD::INS.TipoClasificacionRiesgo"  ("IdTipoClasificacionRiesgo") on delete restrict on update restrict;
      
alter table "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo"
   add constraint "FK_TipoInstrumentoClasificacionRiesgo_TipoClasificacionRiesgo" foreign key ("IdTipoClasificacionRiesgo")
      references "BSCL"."BSCL.Plataforma.BD::INS.TipoClasificacionRiesgo"  ("IdTipoClasificacionRiesgo") on delete restrict on update restrict;
      
alter table "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo"
   add constraint "FK_TipoInstrumentoClasificacionRiesgo_TipoInstrumento" foreign key ("IdTipoInstrumento")
      references "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento"  ("IdTipoInstrumento") on delete restrict on update restrict;

ALTER TABLE "BSCL"."BSCL.Plataforma.BD::INS.Instrumento"  	ADD  ("IdEntidadClasificadoraRiesgo" NVARCHAR(10) not null default '');
ALTER TABLE "BSCL"."BSCL.Plataforma.BD::INS.Instrumento"  	ADD  ("ValorClasificacionRiesgo"     NVARCHAR(20) not null default ''); -- puede ser cualquier nomenclatura, depende de la entidad

ALTER TABLE "BSCL"."BSCL.Plataforma.BD::REG.Pais"  			ADD  ("IdEntidadClasificadoraRiesgo" NVARCHAR(10) not null default '');
ALTER TABLE "BSCL"."BSCL.Plataforma.BD::REG.Pais"  			ADD  ("ValorClasificacionRiesgo"     NVARCHAR(20) not null default ''); -- puede ser cualquier nomenclatura, depende de la entidad

Delete from "BSCL"."BSCL.Plataforma.BD::INS.EntidadClasificadoraRiesgo";
insert into "BSCL"."BSCL.Plataforma.BD::INS.EntidadClasificadoraRiesgo" ("IdEntidadClasificadoraRiesgo", "Nombre") values ('HUMPHREYS','CLASIFICADORA DE RIESGO HUMPHREYS LIMITADA');
insert into "BSCL"."BSCL.Plataforma.BD::INS.EntidadClasificadoraRiesgo" ("IdEntidadClasificadoraRiesgo", "Nombre") values ('FELLER','FELLER-RATE CLASIFICADORA DE RIESGO LIMITADA');
insert into "BSCL"."BSCL.Plataforma.BD::INS.EntidadClasificadoraRiesgo" ("IdEntidadClasificadoraRiesgo", "Nombre") values ('FITCH','FITCH CHILE CLASIFICADORA DE RIESGO LIMITADA');
insert into "BSCL"."BSCL.Plataforma.BD::INS.EntidadClasificadoraRiesgo" ("IdEntidadClasificadoraRiesgo", "Nombre") values ('INTRATING','INTERNATIONAL CREDIT RATING COMPAÑIA CLASIFICADORA DE RIESGO LIMITADA');
insert into "BSCL"."BSCL.Plataforma.BD::INS.EntidadClasificadoraRiesgo" ("IdEntidadClasificadoraRiesgo", "Nombre") values ('STDANDPOOR','STANDARD & POOR S RATINGS CHILE CLASIFICADORA DE RIESGO LIMITADA');
insert into "BSCL"."BSCL.Plataforma.BD::INS.EntidadClasificadoraRiesgo" ("IdEntidadClasificadoraRiesgo", "Nombre") values ('CCR','COMISION CLASIFICADORA DE RIESGO');

Delete from "BSCL"."BSCL.Plataforma.BD::INS.TipoClasificacionRiesgo";
INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.TipoClasificacionRiesgo" ("IdTipoClasificacionRiesgo","IdClasificacionTipoInstrumento","Codigo","Nombre","IdEstado") 
select 'CLRF','RF','','Clasificación de Riesgo para mercado RF','ACT' 		from dummy union
select 'CLIF','IF','','Clasificación de Riesgo para mercado IF','ACT' 		from dummy union
select 'CLRV','RV','','Clasificación de Riesgo para mercado RV','ACT' 		from dummy union
select 'PAIS','','','Clasificación de Riesgo para Paises','ACT' 		from dummy;

Delete from "BSCL"."BSCL.Plataforma.BD::INS.ClasificacionRiesgo";    
INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.ClasificacionRiesgo" ("IdClasificacionRiesgo","IdTipoClasificacionRiesgo","IdEntidadClasificadoraRiesgo","ClasificacionRiesgo","Nivel","IdEstado")
select  1,'CLRV','STDANDPOOR','C'		,1,'ACT' from dummy union
select  2,'CLRV','STDANDPOOR','B'		,2,'ACT' from dummy union
select  3,'CLRV','STDANDPOOR','A'		,3,'ACT' from dummy union

select  4,'CLRV','FITCH','NIVEL 1'		,5,'ACT' from dummy union -- equivalentes
select  5,'CLRV','FITCH','NIVEL 2'		,4,'ACT' from dummy union
select  6,'CLRV','FITCH','NIVEL 3'		,3,'ACT' from dummy union
select  7,'CLRV','FITCH','NIVEL 4'		,2,'ACT' from dummy union
select  8,'CLRV','FITCH','NIVEL 5'		,1,'ACT' from dummy union

select  9,'CLRV','HUMPHREYS','NIVEL 1'	,5,'ACT' from dummy union -- equivalentes
select  10,'CLRV','HUMPHREYS','NIVEL 2'	,4,'ACT' from dummy union
select  11,'CLRV','HUMPHREYS','NIVEL 3'	,3,'ACT' from dummy union
select  12,'CLRV','HUMPHREYS','NIVEL 4'	,2,'ACT' from dummy union
select  13,'CLRV','HUMPHREYS','NIVEL 5'	,1,'ACT' from dummy union

select  14,'CLRV','FELLER','NIVEL 1'		,5,'ACT' from dummy union -- equivalentes
select  15,'CLRV','FELLER','NIVEL 2'		,4,'ACT' from dummy union
select  16,'CLRV','FELLER','NIVEL 3'		,3,'ACT' from dummy union
select  17,'CLRV','FELLER','NIVEL 4'		,2,'ACT' from dummy union
select  18,'CLRV','FELLER','NIVEL 5'		,1,'ACT' from dummy union

select  19,'CLIF','STDANDPOOR','N5'				,1,'ACT' from dummy union
select  20,'CLIF','STDANDPOOR','N4'				,2,'ACT' from dummy union
select  21,'CLIF','STDANDPOOR','N3'				,3,'ACT' from dummy union
select  22,'CLIF','STDANDPOOR','N2'				,4,'ACT' from dummy union
select  23,'CLIF','STDANDPOOR','N1'				,5,'ACT' from dummy union

select 24,'CLRF','STDANDPOOR','B'				, 1,'ACT' from dummy union
select 25,'CLRF','STDANDPOOR','B-'				, 2,'ACT' from dummy union
select 26,'CLRF','STDANDPOOR','B+'				, 3,'ACT' from dummy union
select 27,'CLRF','STDANDPOOR','BB'				, 4,'ACT' from dummy union
select 28,'CLRF','STDANDPOOR','BB-'				, 5,'ACT' from dummy union
select 29,'CLRF','STDANDPOOR','BB+'				, 6,'ACT' from dummy union
select 30,'CLRF','STDANDPOOR','BBB'				, 7,'ACT' from dummy union
select 31,'CLRF','STDANDPOOR','BBB-'			, 8,'ACT' from dummy union
select 32,'CLRF','STDANDPOOR','BBB+'			, 9,'ACT' from dummy union
select 33,'CLRF','STDANDPOOR','A'				,10,'ACT' from dummy union
select 34,'CLRF','STDANDPOOR','A-'				,11,'ACT' from dummy union
select 35,'CLRF','STDANDPOOR','A+'				,12,'ACT' from dummy union
select 36,'CLRF','STDANDPOOR','AA'				,13,'ACT' from dummy union
select 37,'CLRF','STDANDPOOR','AA-'				,14,'ACT' from dummy union
select 38,'CLRF','STDANDPOOR','AA+'				,15,'ACT' from dummy union
select 39,'CLRF','STDANDPOOR','AAA'				,16,'ACT' from dummy union
select 40,'CLRF','STDANDPOOR','AAA-'			,17,'ACT' from dummy union
select 41,'CLRF','STDANDPOOR','AAA+'			,18,'ACT' from dummy ;

Delete from "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo";
INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" ("IdTipoInstrumentoClasificacionRiesgo","IdTipoClasificacionRiesgo","IdTipoInstrumento","Nivel","IdEstado") VALUES (1,'CLIF','PAG',0,'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" ("IdTipoInstrumentoClasificacionRiesgo","IdTipoClasificacionRiesgo","IdTipoInstrumento","Nivel","IdEstado") VALUES (3,'CLRV','ACN',0,'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" ("IdTipoInstrumentoClasificacionRiesgo","IdTipoClasificacionRiesgo","IdTipoInstrumento","Nivel","IdEstado") VALUES (4,'CLRV','ACX',0,'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" ("IdTipoInstrumentoClasificacionRiesgo","IdTipoClasificacionRiesgo","IdTipoInstrumento","Nivel","IdEstado") VALUES (5,'CLRV','CFI',0,'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" ("IdTipoInstrumentoClasificacionRiesgo","IdTipoClasificacionRiesgo","IdTipoInstrumento","Nivel","IdEstado") VALUES (6,'CLRV','CFM',0,'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" ("IdTipoInstrumentoClasificacionRiesgo","IdTipoClasificacionRiesgo","IdTipoInstrumento","Nivel","IdEstado") VALUES (7,'CLRV','EMP',0,'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" ("IdTipoInstrumentoClasificacionRiesgo","IdTipoClasificacionRiesgo","IdTipoInstrumento","Nivel","IdEstado") VALUES (8,'CLRV','ETN',0,'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" ("IdTipoInstrumentoClasificacionRiesgo","IdTipoClasificacionRiesgo","IdTipoInstrumento","Nivel","IdEstado") VALUES (9,'CLRV','ETX',0,'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" ("IdTipoInstrumentoClasificacionRiesgo","IdTipoClasificacionRiesgo","IdTipoInstrumento","Nivel","IdEstado") VALUES (10,'CLRV','PRT',0,'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" ("IdTipoInstrumentoClasificacionRiesgo","IdTipoClasificacionRiesgo","IdTipoInstrumento","Nivel","IdEstado") VALUES (11,'CLRV','SIM',0,'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" ("IdTipoInstrumentoClasificacionRiesgo","IdTipoClasificacionRiesgo","IdTipoInstrumento","Nivel","IdEstado") VALUES (12,'CLRF','BBC',0,'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" ("IdTipoInstrumentoClasificacionRiesgo","IdTipoClasificacionRiesgo","IdTipoInstrumento","Nivel","IdEstado") VALUES (13,'CLRF','BCD',0,'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" ("IdTipoInstrumentoClasificacionRiesgo","IdTipoClasificacionRiesgo","IdTipoInstrumento","Nivel","IdEstado") VALUES (14,'CLRF','BCP',0,'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" ("IdTipoInstrumentoClasificacionRiesgo","IdTipoClasificacionRiesgo","IdTipoInstrumento","Nivel","IdEstado") VALUES (15,'CLRF','BCU',0,'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" ("IdTipoInstrumentoClasificacionRiesgo","IdTipoClasificacionRiesgo","IdTipoInstrumento","Nivel","IdEstado") VALUES (16,'CLRF','BEM',0,'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" ("IdTipoInstrumentoClasificacionRiesgo","IdTipoClasificacionRiesgo","IdTipoInstrumento","Nivel","IdEstado") VALUES (17,'CLRF','CER',0,'ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" ("IdTipoInstrumentoClasificacionRiesgo","IdTipoClasificacionRiesgo","IdTipoInstrumento","Nivel","IdEstado") VALUES (18,'CLRF','LTH',0,'ACT');

--- se deben relacionar por mantenedor
INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.EquivalenciaClasificacion" ("IdEquivalenciaClasificacion", "IdClasificacionRiesgoOrigen", "IdClasificacionRiesgoDestino")
select 1,4,9 from dummy union 
select 2,4,14 from dummy union 
select 3,3,4 from dummy;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Configuracion" ("IdEmpresa", "CodigoParametro" , "Descripcion", "Valor")
select '1','EntidadClasificadora', 'Entidad Clasificadora de Riesgo Predeterminada', 'STDANDPOOR' from dummy;

------------------------- AGREGAR A LIBERACION MERCADO
ALTER TABLE "BSCL"."BSCL.Mercado.BD::MER.Emisor"  			ADD  ("IdEntidadClasificadoraRiesgo" NVARCHAR(10) not null default '');
ALTER TABLE "BSCL"."BSCL.Mercado.BD::MER.Emisor"  			ADD  ("ValorClasificacionRiesgo"     NVARCHAR(20) not null default ''); -- puede ser cualquier nomenclatura, depende de la entidad
  
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('CLASIFICACION_OK','CL','Clasificacion Actualizada Correctamente','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('CLASIFICACION_ERROR','CL','Clasificacion no se pudo Actualizar','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('CLASIFICACION_ERROR_ENTIDAD','CL','Debe ingresar datos de entidad clasificadora e instrumento','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('CLASIFICACION_FALTADEFAULT','CL','No existe Entidad Clasificadora Predeterminada','');

