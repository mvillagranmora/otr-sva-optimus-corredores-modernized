SET SCHEMA "BSCL"; 
/*OPERACIONALES*/
DELETE FROM "BSCL"."BSCL.Plataforma.BD::EVT.EventoDetalle";
DELETE FROM "BSCL"."BSCL.Plataforma.BD::EVT.Evento";
DELETE FROM "BSCL"."BSCL.Plataforma.BD::EVT.NotificacionDetalle";
DELETE FROM "BSCL"."BSCL.Plataforma.BD::EVT.Notificacion";
DELETE FROM "BSCL"."BSCL.Plataforma.BD::EVT.Destinatario";
DELETE FROM "BSCL"."BSCL.Plataforma.BD::EVT.EventoDirecciones";
DELETE FROM "BSCL"."BSCL.Plataforma.BD::EVT.Mail";
DELETE FROM "BSCL"."BSCL.Plataforma.BD::EVT.EventoArchivoAdjunto";
COMMIT;
/*CONFIG*/
DELETE FROM "BSCL"."BSCL.Plataforma.BD::EVT.PersonalizacionEvento";
DELETE FROM "BSCL"."BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle";
DELETE FROM "BSCL"."BSCL.Plataforma.BD::EVT.ConfiguracionEvento";
DELETE FROM "BSCL"."BSCL.Plataforma.BD::EVT.ConfiguracionEventoCodigoTipoEvento";
DELETE FROM "BSCL"."BSCL.Plataforma.BD::EVT.ConfiguracionEventoCopiasCorreo";
DELETE FROM "BSCL"."BSCL.Plataforma.BD::EVT.ConfiguracionTipoEvento";
DELETE FROM "BSCL"."BSCL.Plataforma.BD::EVT.ConfiguracionUsuarioTipoEvento";
DELETE FROM "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle";
DELETE FROM "BSCL"."BSCL.Plataforma.BD::EVT.TipoEvento";

/*NO BORRAR*/
select '' as "asd" from dummy
DELETE FROM "BSCL"."BSCL.Plataforma.BD::EVT.TagGenerales";
DELETE FROM "BSCL"."BSCL.Plataforma.BD::EVT.TiempoDespacho";
DELETE FROM "BSCL"."BSCL.Plataforma.BD::EVT.FormaDespacho";
DELETE FROM "BSCL"."BSCL.Plataforma.BD::EVT.DireccionEvento";
DELETE FROM "BSCL"."BSCL.Plataforma.BD::EVT.Plantilla";

--TagsGenerales 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TagGenerales" ("IdTagGenerales","Llave","ValorDefault") VALUES (1,'Footer','<p><span style=font-size:8.0pt;font-family:Arial,sans-serif;color:#4d4d4d>La Bolsa 64, Santiago, Chile</span></p> <p><strong><a href=http://www.bolsadesantiago.com/ target=_blank>www.bolsadesantiago.com</a></strong></p><p><img alt= src=https://fbcdn-photos-b-a.akamaihd.net/hphotos-ak-xfp1/v/t1.0-0/p200x200/13606907_10209971355221972_3084385476925284781_n.jpg?oh=da2414ca5332aa51fd96199e890d8702&amp;oe=582760D0&amp;__gda__=1475021609_faa8ae56174d768b31c9bfd7638b70a1 style=width: 144px; height: 54px; /></p><p>(es) La información contenida en este correo electrónico es para el uso exclusivo de sus destinatarios. Su contenido puede ser confidencial y cualquier uso no autorizado está estrictamente prohibido. Si usted ha recibido esta comunicación por error, le rogamos notificar de inmediato al remitente y eliminar todas las copias que posea. (en) The information contained in this e-mail is intended for the exclusive use of the individual named above. The contents may be confidential and any unauthorized use of whatever kind is strictly prohibited. If you have received this communication in error, please notify the sender immediately and delete all copies of it.  </p>'); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TagGenerales" ("IdTagGenerales","Llave","ValorDefault") VALUES (2,'Cabecera','<p style=text-align: center;><img alt= src=https://fbcdn-photos-b-a.akamaihd.net/hphotos-ak-xfp1/v/t1.0-0/p200x200/13606907_10209971355221972_3084385476925284781_n.jpg?oh=da2414ca5332aa51fd96199e890d8702&amp;oe=582760D0&amp;__gda__=1475021609_faa8ae56174d768b31c9bfd7638b70a1 style=width: 500px; height: 117px; /></p>'); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TagGenerales" ("IdTagGenerales","Llave","ValorDefault") VALUES (3,'Confirmar','<#macro CONFIRMACION> <a href=::URL::> <#nested> </a></#macro>'); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TagGenerales" ("IdTagGenerales","Llave","ValorDefault") VALUES (4,'Codigo','Para acceder ingrese los últimos 4 digitos de su rut'); 

--Tipo Evento
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEvento" ("CodigoTipoEvento","DireccionEvento","Descripcion") VALUES ('NOTICUCLI','SYSEMP','Cumpleaños clientes'); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEvento" ("CodigoTipoEvento","DireccionEvento","Descripcion") VALUES ('INGRESO_ORDEN','EMPCLI','Notificacion de ordenes Agente -> Cliente'); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEvento" ("CodigoTipoEvento","DireccionEvento","Descripcion") VALUES ('DOCVENCIDOS','SYSEMP','Notificacion de documentos del cliente vencidos'); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEvento" ("CodigoTipoEvento","DireccionEvento","Descripcion") VALUES ('DOCAVENCER','SYSEMP','Notificacion de documentos del cliente por vencer'); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEvento" ("CodigoTipoEvento","DireccionEvento","Descripcion") VALUES ('CIRCULARIZACION_CLIENTE','SYSEMP','Notificacion de Circularizacion a Cliente');--pasar a EMPCLI cuando se arregle
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEvento" ("CodigoTipoEvento","DireccionEvento","Descripcion") VALUES ('CAMBIO_ESTADO_ORDEN','EMPCLI','Notificacion de cambio de estado ordenes');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEvento" ("CodigoTipoEvento","DireccionEvento","Descripcion") VALUES ('RESUMEN_FACTURACION_CLIENTE','EMPCLI','Resumen de operaciones facturadas del cliente');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEvento"	("CodigoTipoEvento","DireccionEvento","Descripcion") VALUES ('DIVIDENDOS','SYSEMP', 'Notificacion Evento Capital')
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEvento"	("CodigoTipoEvento","DireccionEvento","Descripcion") VALUES ('CORTE_CUPON', 'SYSEMP', 'Notificacion Evento Capital Corte Cupon')
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEvento"	("CodigoTipoEvento","DireccionEvento","Descripcion") VALUES ('CREACION_EVENTO_AUTORIZACION','SYSEMP', 'Notificación Creación Evento de Autorizacion');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEvento"	("CodigoTipoEvento","DireccionEvento","Descripcion") VALUES ('MODIFICACION_EVENTO_AUTORIZACION','SYSEMP', 'Notificación Modificación Evento de Autorizacion');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEvento"	("CodigoTipoEvento","DireccionEvento","Descripcion") VALUES ('RECUPERAR_CLAVE_CLIENTE','EMPCLI', 'Notificar token de recuperacion para clave de cliente');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEvento"	("CodigoTipoEvento","DireccionEvento","Descripcion") VALUES ('RECUPERAR_CLAVE_EMPLEADO','SYSEMP', 'Notificar token de recuperacion para clave de empleado');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEvento"	("CodigoTipoEvento","DireccionEvento","Descripcion") VALUES ('CLAVE_CLIENTE_RECUPERADA','EMPCLI', 'Notificar cambio de clave cliente recuperada por token');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEvento"	("CodigoTipoEvento","DireccionEvento","Descripcion") VALUES ('CLAVE_EMPLEADO_RECUPERADA','SYSEMP', 'Notificar cambio de clave empleado recuperada por token');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEvento" ("CodigoTipoEvento","DireccionEvento","Descripcion") VALUES ('CUMPLIMIENTO_SOI','SYSEMP','Notificacion al Oficial de Cumplimiento');
--
select * from "BSCL.Plataforma.BD::EVT.ConfiguracionEventoCodigoTipoEvento"
--insert into "BSCL.Plataforma.BD::EVT.ConfiguracionEventoCodigoTipoEvento"("IdConfiguracionCodigoTipoEvento", "IdConfiguracionEvento", "NotificaTodosUsuarios", "ExcluyeDestinatario", "TipoUsuario", "IdUsuarioRol", "CopiarRemitente")
--VALUES(64, 64,'N','N','R',-1,'N')
--select * from "BSCL"."BSCL.Plataforma.BD::EVT.ConfiguracionEvento"
--TipoEvento Detalle
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (1,'TAG1','VALOR1','DEMO'); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (2,'TAG2','VALOR2','DEMO'); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (3,'TAG3','VALOR3','DEMO'); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (4,'Glosa','Esto es una glosa','NOTICUCLI'); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (7,'GLOSA','TEST1','DOCVENCIDOS'); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (8,'GLOSA','TEST2','DOCVENCIDOS'); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (9,'GLOSA','TEST1','DOCAVENCER'); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (10,'GLOSA','TEST2','DOCAVENCER');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (11, 'FOLIO', '1234', 'CAMBIO_ESTADO_ORDEN');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (12, 'IdCliente', '1234', 'CAMBIO_ESTADO_ORDEN');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (13, 'IDCLIENTEPERSONA', '1234', 'CAMBIO_ESTADO_ORDEN');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (14, 'NEMO', 'LAN', 'CAMBIO_ESTADO_ORDEN');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (15, 'RUT', '11111111-1', 'CAMBIO_ESTADO_ORDEN');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (16, 'CLIENTE', 'TEST', 'CAMBIO_ESTADO_ORDEN');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (17, 'IdPortafolioCliente', '1', 'CAMBIO_ESTADO_ORDEN');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (18, 'CUENTA', 'PROGENERAL', 'CAMBIO_ESTADO_ORDEN');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (19, 'IdEmpleadoDigitador', '1', 'CAMBIO_ESTADO_ORDEN');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (20, 'AGENTE', '1', 'CAMBIO_ESTADO_ORDEN');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (21, 'IDEMPLEADOPERSONA', '1', 'CAMBIO_ESTADO_ORDEN');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (22, 'CORREO', 'test@bcs.cl', 'CAMBIO_ESTADO_ORDEN');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (23, 'TELEFONO', '+5697852421', 'CAMBIO_ESTADO_ORDEN');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (24, 'FECHA', '2016-11-18', 'CAMBIO_ESTADO_ORDEN');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (25, 'HORA', '19:00:00', 'CAMBIO_ESTADO_ORDEN');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (26, 'PRODUCTO', 'ACCION', 'CAMBIO_ESTADO_ORDEN');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (27, 'TIPO_OPERACION', 'COPRA', 'CAMBIO_ESTADO_ORDEN');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (28, 'CANTIDAD', '100', 'CAMBIO_ESTADO_ORDEN');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (29, 'PRECIO', '120', 'CAMBIO_ESTADO_ORDEN');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (30, 'MONEDA', 'CLP', 'CAMBIO_ESTADO_ORDEN');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (31, 'ESTADO', 'OFE', 'CAMBIO_ESTADO_ORDEN');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (32, 'CORREDORA', 'HANA', 'CAMBIO_ESTADO_ORDEN');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (33, 'CORREDORA', 'HANA', 'CAMBIO_ESTADO_ORDEN');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (34,'MONTO','100','CAMBIO_ESTADO_ORDEN');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (35, 'ATENCIONCLIENTE', '+56226589565', 'CAMBIO_ESTADO_ORDEN');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (36,'NombreCompleto','NOMBRE','CIRCULARIZACION_CLIENTE'); 


INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (37, 'RUT', '11111111-1', 'RESUMEN_FACTURACION_CLIENTE');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (38, 'CLIENTE', 'TEST', 'RESUMEN_FACTURACION_CLIENTE');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (39, 'CUENTA', 'PROGENERAL', 'RESUMEN_FACTURACION_CLIENTE');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (40, 'AGENTE', '1', 'RESUMEN_FACTURACION_CLIENTE');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (41, 'CORREO', 'test@bcs.cl', 'RESUMEN_FACTURACION_CLIENTE');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (42, 'TELEFONO', '+5697852421', 'RESUMEN_FACTURACION_CLIENTE');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (43, 'FECHA', '2016-11-18', 'RESUMEN_FACTURACION_CLIENTE');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (44, 'HORA', '19:00:00', 'RESUMEN_FACTURACION_CLIENTE');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (45, 'CORREDORA', 'HANA', 'RESUMEN_FACTURACION_CLIENTE');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (46, 'ATENCIONCLIENTE', '+56226589565', 'RESUMEN_FACTURACION_CLIENTE');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (47, 'TABLA', '<table><tr><td></td></tr></table>', 'RESUMEN_FACTURACION_CLIENTE');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (90, 'TipoDividendo', 	'DIVIDENDO', 	'DIVIDENDOS');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (91, 'Nombre', 			'TEST', 		'DIVIDENDOS');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (92, 'Nemo', 			'LAN', 			'DIVIDENDOS');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (93, 'Pago', 			'100', 			'DIVIDENDOS');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (94, 'Monto', 			'1000', 		'DIVIDENDOS');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (95, 'Agente', 			'1', 			'DIVIDENDOS');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (96, 'SocNeg', 			'11111111-1', 	'DIVIDENDOS');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (97, 'Port', 			'1', 			'DIVIDENDOS');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (98, 'Fono', 			'+56912345678', 'DIVIDENDOS');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (99, 'Mail', 			'test@bcs.cl', 	'DIVIDENDOS');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (100, 'ConDerecho', 	'1500', 		'DIVIDENDOS');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (101, 'Pago', 			'1001', 		'DIVIDENDOS');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (102, 'PagoMo', 		'$', 			'DIVIDENDOS');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (103, 'MontoMo', 		'$', 			'DIVIDENDOS');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (104, 'FechaPago', 		'2016-12-07',	'DIVIDENDOS');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (105, 'Estado', 		'ACT', 			'DIVIDENDOS');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (106, 'Fecha', 			'2016-12-07', 	'DIVIDENDOS');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (107, 'Hora', 			'13:00:00', 	'DIVIDENDOS');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (108, 'Texto', 			' se han generado',	'DIVIDENDOS');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (109, 'Corredora', 		'HANA',			'DIVIDENDOS');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (110, 'Tipo', 			'Dividendo en Acciones','DIVIDENDOS');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (111, 'TipoDividendo', 	'CORTE CUPON', 	'CORTE_CUPON');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (112, 'Nombre', 		'TEST',			'CORTE_CUPON');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (113, 'Nemo', 			'BACEN-A2',		'CORTE_CUPON');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (114, 'Pago', 			'100', 			'CORTE_CUPON');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (115, 'Monto', 			'1000', 		'CORTE_CUPON');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (116, 'Agente', 		'1', 			'CORTE_CUPON');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (117, 'SocNeg', 		'11111111-1', 	'CORTE_CUPON');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (118, 'Port', 			'1', 			'CORTE_CUPON');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (119, 'Fono', 			'+56912345678', 'CORTE_CUPON');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (120, 'Mail', 			'test@bcs.cl', 	'CORTE_CUPON');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (121, 'ConDerecho', 	'1500', 		'CORTE_CUPON');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (122, 'Pago', 			'1001', 		'CORTE_CUPON');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (123, 'PagoMo', 		'$', 			'CORTE_CUPON');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (124, 'MontoMo', 		'$', 			'CORTE_CUPON');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (125, 'FechaPago', 		'2016-12-07', 	'CORTE_CUPON');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (126, 'Estado', 		'ACT', 			'CORTE_CUPON');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (127, 'Fecha', 			'2016-12-07', 	'CORTE_CUPON');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (128, 'Hora', 			'13:00:00', 	'CORTE_CUPON');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (129, 'Texto', 			' se han generado',	'CORTE_CUPON');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (130, 'Corredora', 		'HANA',	'CORTE_CUPON');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (131, 'Tipo', 			'Corte Cupon',			'CORTE_CUPON');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (132, 'Familia', 		'Familia',			'CORTE_CUPON');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (133, 'Token', 'H4s#r', 'RECUPERAR_CLAVE_CLIENTE');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (134, 'Nombre', 'Pedro BCS', 'RECUPERAR_CLAVE_CLIENTE');
COMMIT
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (135, 'Token', 'H4s#r', 'RECUPERAR_CLAVE_EMPLEADO');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (136, 'Nombre', 'Pedro BCS', 'RECUPERAR_CLAVE_EMPLEADO');
COMMIT

INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (137, 'Nombre', 'Pedro BCS', 'CLAVE_CLIENTE_RECUPERADA');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (138, 'Nombre', 'Pedro BCS', 'CLAVE_EMPLEADO_RECUPERADA');


INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (139,'NombreDestinatario','NOMBRE','CUMPLIMIENTO_SOI'); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (140,'IdCliente','0','CUMPLIMIENTO_SOI'); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (141,'NombreCompleto','0','CUMPLIMIENTO_SOI'); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (142,'IdOperacion','0','CUMPLIMIENTO_SOI'); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (143,'TipoOperacion','0','CUMPLIMIENTO_SOI'); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (144,'TipoProducto','0','CUMPLIMIENTO_SOI'); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (145,'MontoUtilizado','0','CUMPLIMIENTO_SOI'); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (146,'Moneda','0','CUMPLIMIENTO_SOI'); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (147,'DocumentoIdentidad','0','CUMPLIMIENTO_SOI'); 

INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (148, 'Descripcion', 			'Descripcon Evento Capital','DIVIDENDOS');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle" ("IdTipoEventoDetalle","Llave","ValorDefault","CodigoTipoEvento") VALUES (149, 'Descripcion', 			'Descripcon Evento Capital','CORTE_CUPON');


COMMIT;


--MAESTRA
INSERT INTO  "BSCL"."BSCL.Plataforma.BD::EVT.DireccionEvento"("IdDireccionEvento", "Descripcion") VALUES('EMPCLI', 'EMPLEADO GENERA A CLIENTE');
INSERT INTO  "BSCL"."BSCL.Plataforma.BD::EVT.DireccionEvento"("IdDireccionEvento", "Descripcion") VALUES('CLIEMP', 'CLIENTE GENERA A EMPLEADO');
INSERT INTO  "BSCL"."BSCL.Plataforma.BD::EVT.DireccionEvento"("IdDireccionEvento", "Descripcion") VALUES('SYSEMP', 'SISTEMA GENERA A EMPLEADO');

--TiempoDespacho
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TiempoDespacho" ("IdTiempoDespacho","Nombre","CodigoTiempoDespacho") VALUES (1,'INMEDIATO','I'); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TiempoDespacho" ("IdTiempoDespacho","Nombre","CodigoTiempoDespacho") VALUES (2,'HORA','H'); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.TiempoDespacho" ("IdTiempoDespacho","Nombre","CodigoTiempoDespacho") VALUES (3,'MANUAL','M'); 
--Forma Despacho
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.FormaDespacho" ("IdFormaDespacho","Nombre","CodigoFormaDespacho") VALUES (1,'CORREO','C'); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.FormaDespacho" ("IdFormaDespacho","Nombre","CodigoFormaDespacho") VALUES (2,'UI','U'); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::EVT.FormaDespacho" ("IdFormaDespacho","Nombre","CodigoFormaDespacho") VALUES (3,'AMBOS','T'); 


SELECT * FROM "BSCL"."BSCL.Plataforma.BD::EVT.ConfiguracionTipoEvento";
SELECT * FROM "BSCL"."BSCL.Plataforma.BD::EVT.ConfiguracionEvento";
SELECT * FROM "BSCL"."BSCL.Plataforma.BD::EVT.ConfiguracionEventoCodigoTipoEvento";
SELECT * FROM "BSCL"."BSCL.Plataforma.BD::EVT.ConfiguracionEventoCopiasCorreo";

SELECT * FROM "BSCL"."BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle";
select * from "BSCL"."BSCL.Plataforma.BD.Vistas::EVT.ConsultarEventoDetalleNoAgrupados"

SELECT * FROM "BSCL"."BSCL.Plataforma.BD::EVT.ConfiguracionUsuarioTipoEvento";
SELECT * FROM "BSCL"."BSCL.Plataforma.BD::EVT.TipoEvento";
SELECT * FROM "BSCL"."BSCL.Plataforma.BD::EVT.TipoEventoDetalle";
SELECT * FROM "BSCL"."BSCL.Plataforma.BD::EVT.Plantilla";

SELECT top 20 * FROM "BSCL"."BSCL.Plataforma.BD::EVT.Evento" 
ORDER BY "IdEvento" DESC;
--Where "Procesado" = 'N' 

SELECT top 20 * FROM "BSCL"."BSCL.Plataforma.BD::EVT.Notificacion" ORDER BY "IdNotificacion" DESC;
SELECT top 20 * FROM "BSCL"."BSCL.Plataforma.BD::EVT.NotificacionDetalle";
SELECT * FROM "BSCL"."BSCL.Plataforma.BD::EVT.EventoDetalle" ORDER BY "IdEventoDetalle" DESC;
SELECT * FROM "BSCL"."BSCL.Plataforma.BD::EVT.Destinatario";
SELECT * FROM "BSCL"."BSCL.Plataforma.BD::EVT.Evento" Where "CodigoTipoEvento" = 'RESUMEN_FACTURACION_CLIENTE'
SELECT * FROM "BSCL"."BSCL.Plataforma.BD::EVT.EventoArchivoAdjunto";

SELECT * FROM "BSCL"."BSCL.Plataforma.BD::EVT.EventoDirecciones";
SELECT * FROM "BSCL"."BSCL.Plataforma.BD::EVT.Mail";
SELECT * FROM "BSCL"."BSCL.Plataforma.BD::EVT.PersonalizacionEvento";
/*NO BORRAR*/
SELECT * FROM "BSCL"."BSCL.Plataforma.BD::EVT.TagGenerales";
SELECT * FROM "BSCL"."BSCL.Plataforma.BD::EVT.TiempoDespacho";
SELECT * FROM "BSCL"."BSCL.Plataforma.BD::EVT.FormaDespacho";
SELECT * FROM "BSCL"."BSCL.Plataforma.BD::EVT.DireccionEvento";
