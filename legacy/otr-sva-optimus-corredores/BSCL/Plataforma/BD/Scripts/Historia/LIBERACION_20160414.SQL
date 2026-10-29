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
-- LIBERACION SEMANA: 15-04-2016
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
--    CFLORES - Indicadores
--################################################

SET SCHEMA "BSCL"; --Agregar campo
alter table "BSCL.Plataforma.BD::IND.TipoIndicador" add ("CodigoTipoIndicador"   	NVARCHAR(10) 	NOT NULL );

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(274,'BSCL.Plataforma.BD.Servicios::IND.ConsultarIndicador',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(275,'BSCL.Plataforma.BD.Servicios::IND.ConsultarPreferencia',0,'P','');


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


--################################################
--    JG - PREFEERENCIAS DE PAGO.
--################################################	

ALTER TABLE "BSCL.Plataforma.BD::PAG.MedioDePago" 
ADD( "UsaCuentaBancaria"	NVARCHAR(1)	NOT NULL DEFAULT 'N');
ALTER TABLE "BSCL.Plataforma.BD::PAG.MedioDePago" 
ADD( "UsaTarjetaBancaria"	NVARCHAR(1)	NOT NULL DEFAULT 'N');
UPDATE "BSCL.Plataforma.BD::PAG.MedioDePago"
SET "UsaCuentaBancaria" = 'S' WHERE "IdMedioDePago" in ('TDP','TLR','TSW');

ALTER TABLE "BSCL.Plataforma.BD::PAG.PreferenciaPago" 
DROP ("UsaCuentaBancaria");
ALTER TABLE "BSCL.Plataforma.BD::PAG.PreferenciaPago" 
DROP ("UsaTarjetaBancaria");
 
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(290,'BSCL.Portafolio.BD.Servicios::ModificarPreferenciaPago',0,'P','');

 
--################################################
--    JG - Indicador por NG380
--################################################

 insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
	"IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
	"GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (40, 'CLIENTE', 1, '', 'COMBO', 'Clase de Orden','CLASEORDEN', 'Generales', 'IND', 'ACT', 0, 0, 100, 'N' );
	--DETALLE
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","DetalleIndicador")
	VALUES (1,40,'Orden Escrita');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","DetalleIndicador")
	VALUES (2,40,'Orden Verbal');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","DetalleIndicador")
	VALUES (3,40,'Operador Directo');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","DetalleIndicador")
	VALUES (4,40,'Ruteo');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","DetalleIndicador")
	VALUES (5,40,'Administración Cartera');
	INSERT INTO "BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","DetalleIndicador")
	VALUES (6,40,'Orden por otro medio');
 
