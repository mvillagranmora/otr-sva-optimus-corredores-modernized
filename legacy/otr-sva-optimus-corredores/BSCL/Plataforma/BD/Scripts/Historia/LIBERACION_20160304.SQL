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
-- LIBERACION SEMANA: 04-03-2016
--



--################################################
--       KHELLMAN.TransaccionSimultanea requiere Moneda de Liquidacion
--################################################
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion") 
SELECT MAX("IdTipoInstrumentoMonedaLiquidacion")+1,'SIM', 'CLP','CLP' FROM "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" UNION ALL


--------------------------------------------------
--      FIN <MOTIVO>
--------------------------------------------------



--################################################
--       MMACHMAR - Inscripcion accesos cambio estado Soc Negocio
--################################################

-- Inscripcion Accesos pantalla modal Bloqueo-Desbloqueo de Socios de Negocio
-- PERSONA
UPDATE ACC SET ACC."IdServicio" = 0 FROM "BSCL"."BSCL.Plataforma.BD::ACC.Acceso" ACC WHERE ACC."IdAcceso" = 47;
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(248,'Botón Aceptar Bloquear Datos Generales', 'BTN', 47, 1, 108, 'ModalSocioNegocioBloqueo_PERSONA_AceptarBLO', 'S');

UPDATE ACC SET ACC."IdServicio" = 0 FROM "BSCL"."BSCL.Plataforma.BD::ACC.Acceso" ACC WHERE ACC."IdAcceso" = 48;
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(249,'Botón Aceptar Desbloquear Datos Generales', 'BTN', 48, 1, 108, 'ModalSocioNegocioBloqueo_PERSONA_AceptarACT', 'S');

-- CLIENTE
UPDATE ACC SET ACC."NombreAcceso" = 'Botón Aceptar Bloquear Datos Generales', ACC."IdElementoHtml" = 'ModalSocioNegocioBloqueo_CLIENTE_AceptarBLO'
FROM "BSCL"."BSCL.Plataforma.BD::ACC.Acceso" ACC WHERE ACC."IdAcceso" = 115;

INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(250,'Botón Aceptar Desbloquear Datos Generales', 'BTN', 113, 1, 96, 'ModalSocioNegocioBloqueo_CLIENTE_AceptarACT', 'S');

-- EMPLEADO
UPDATE ACC SET ACC."NombreAcceso" = 'Botón Aceptar Bloquear Datos Generales', ACC."IdElementoHtml" = 'ModalSocioNegocioBloqueo_EMPLEADO_AceptarBLO'
FROM "BSCL"."BSCL.Plataforma.BD::ACC.Acceso" ACC WHERE ACC."IdAcceso" = 164;

UPDATE ACC SET ACC."NombreAcceso" = 'Botón Aceptar Desbloquear Datos Generales', ACC."IdElementoHtml" ='ModalSocioNegocioBloqueo_EMPLEADO_AceptarACT'
FROM "BSCL"."BSCL.Plataforma.BD::ACC.Acceso" ACC WHERE ACC."IdAcceso" = 165;

-- CONTRAPARTE
UPDATE ACC SET ACC."NombreAcceso" = 'Botón Aceptar Bloquear Datos Generales', ACC."IdElementoHtml" ='ModalSocioNegocioBloqueo_CONTRAPARTE_AceptarBLO'
FROM "BSCL"."BSCL.Plataforma.BD::ACC.Acceso" ACC WHERE ACC."IdAcceso" = 175;

UPDATE ACC SET ACC."NombreAcceso"='Botón Aceptar Desbloquear Datos Generales', ACC."IdElementoHtml"='ModalSocioNegocioBloqueo_CONTRAPARTE_AceptarACT'
FROM "BSCL"."BSCL.Plataforma.BD::ACC.Acceso" ACC WHERE ACC."IdAcceso" = 176;

-- PROVEEDOR
UPDATE ACC SET ACC."NombreAcceso" = 'Botón Aceptar Bloquear Datos Generales', ACC."IdElementoHtml" = 'ModalSocioNegocioBloqueo_PROVEEDOR_AceptarBLO'
FROM "BSCL"."BSCL.Plataforma.BD::ACC.Acceso" ACC WHERE ACC."IdAcceso" = 184;

UPDATE ACC SET ACC."NombreAcceso" = 'Botón Aceptar Desbloquear Datos Generales', ACC."IdElementoHtml"='ModalSocioNegocioBloqueo_PROVEEDOR_AceptarACT'
FROM "BSCL"."BSCL.Plataforma.BD::ACC.Acceso" ACC WHERE ACC."IdAcceso" = 185;

--------------------------------------------------
--      FIN Inscripcion Accesos
--------------------------------------------------


insert into "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato"
select 84,'CLP','ORD','','',4,'T',4,'T',4,'T',0,'T',4,'T',4,'T',4,'T','ACT' from dummy;

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(204,'BSCL.Orden.BD.Servicios::CrearLoteOrdenPostrera',0,'P','');   





--################################################
--       NROJAS Se cambia nombre de servicio que retorna información de Saldo de CTA
--################################################

update "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" 
set "RutaServicio" = 'BSCL.CuentaInversion.BD.Servicios::ConsultarSaldoCuentaInversion'
WHERE "IdServicio" = 21


--################################################
--       JVILLAGRAN parámetro para identificar CCLV
--################################################

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor")
VALUES(1, 'IdPersonaCCLV', 'Indica el IdPersona de CCLV', '20033');

--################################################
--       OSCAR CARREÑO - fueron borrados
--################################################
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(85,'BSCL.Custodia.BD.Servicios::PRT.AnularPrestamo',0,'P','');
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(86,'BSCL.Custodia.BD.Servicios::PRT.AnularEmprestito',0,'P','');

--################################################
--       MMACHMAR - INSERT tabla Servicios
--################################################
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(211,'BSCL.Orden.BD.Servicios::CrearLibroOrdenesAUX',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(212,'BSCL.Orden.BD.Servicios::ModificarLibroOrdenesAUX',0,'P','');
--------------------------------------------------
--      FIN INSERT tabla Servicios
--------------------------------------------------

--################################################
--       CAPONTE - DOCUMENTOS
--################################################

CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::Documento"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
       
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::Anexo"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
	   
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(206,'BSCL.Plataforma.BD.Servicios::DOC.AgregarTipoDocumento',0,'P',''); 	   
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (210, 'BSCL.Plataforma.BD.Servicios::DOC.AgregarDocumento' , 1,  'P', '');

--################################################
--       CHERNANDEZ - CARGA_ARCHIVOS
--################################################
set schema "BSCL";
CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.Archivo" (
   "IdArchivo" 				BIGINT              not null,
   "IdEmpresa"				BIGINT				not null,
   "CodigoArchivo"     		NVARCHAR(50)        not null unique,
   "TieneSeparador"			NVARCHAR(1)			not null,--S/N, en caso de ser 'N', de tomara el campo tamaño de la tabla formato para separar los campos.
   "SeparadorColumnas"		NVARCHAR(5)			not null,--Ej: . o , o ; o etc.			
   PRIMARY KEY("IdArchivo")
);

CREATE COLUMN TABLE "BSCL"."BSCL.Plataforma.BD::PLA.Formato" (
   "IdFormato"				BIGINT              not null,
   "CodigoCampo"			NVARCHAR(100)		not null,--EJ: FECHA_TRANSACCION
   "TipoDato"  	    		NVARCHAR(50)        not null,--INT,STRING,DATE,TIMESTAMP,ETC
   "Posicion"				INT					not null,--En el caso de no tener separador, se debe especificar la posicion del campo en el archivo; caso contrario dejar en 0
   "LargoCampo"				INT					not null,--Largo en caracteres del campo a formatear (solo en caso de no tener separador el archivo)
   "EsObligatorio"			NVARCHAR(1)			not null, --S/N
   "Validacion"	     		NVARCHAR(50)        not null,--REGEX OPCIONAL, EJ: (.*)
   "MensajeValidacion"		NVARCHAR(150)		not null,--EJ:La fecha debe estar en formato YYYY-MM.	 
   PRIMARY KEY("IdFormato")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.ArchivoFormato" (
   "IdArchivoFormato"		BIGINT              not null,
   "IdArchivo"				BIGINT				not null,
   "IdFormato"     	    	BIGINT      		not null,
   PRIMARY KEY("IdArchivoFormato")
);
--Registro del archivo de pruebas
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Archivo"("IdArchivo", "IdEmpresa","CodigoArchivo", "TieneSeparador", "SeparadorColumnas") 
VALUES(1,1,'TEST_ARCHIVO_SEPARADOR','S',';');
--Formatos de archivo con separacion ;
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Formato" VALUES(1,'CODIGO', 'string',0,0,'S','','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Formato" VALUES(2,'PAIS', 'string',0,0,'S','','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Formato" VALUES(3,'DESCRIPCION', 'string',0,0,'S','','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Formato" VALUES(4,'ABREVIACION', 'string',0,0,'S','','');
--relaciones archivo con separacion y sus formatos
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(1,1,1);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(2,1,2);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(3,1,3);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(4,1,4);

--################################################
--       EESPINOZA - NOTIFICACION
--################################################

--################################################
--		 MODELO DATOS - NOTIFICACIÓN
--################################################

create column table "BSCL.Plataforma.BD::EVT.TipoEvento" (
"CodigoTipoEvento"            NVARCHAR(40)       not null, 
"DireccionEvento"     NVARCHAR(6)         not null,  --EMPCLI:Empleado genera a cliente  CLIEMP: Cliente genera a empleado  SYSEMP: Systema a Empleado
"Descripcion"                 NVARCHAR(520)       not null,
"CamposPlantilla"     NVARCHAR(5000)        not null, -- json describe campo de plantilla con un ejemplo 
primary key ("CodigoTipoEvento"));

create column table "BSCL.Plataforma.BD::EVT.Plantilla" (
"IdPlantilla"            BIGINT      not null,
"IdEmpresa"                    BIGINT      not null,  --Empresa que pertenece.
"IdUsuario"             BIGINT      not null,  --Usuario que creo la notif.
"FechaCreacion"          SECONDDATE      not null,  --fecha creacion
"Template"                     NCLOB       not null,  --Plantilla, podrian haber tags de sistemas, y tags asociados a event
primary key ("IdPlantilla"));

create column table "BSCL.Plataforma.BD::EVT.Evento" (
"IdEmpresa"                   BIGINT              not null,  --Empresa que pertenece el evento.
"IdEvento"                    BIGINT              not null,  --ID Unico del evento
"CodigoTipoEvento"                  NVARCHAR(40)       not null,  --FK TipoEvento
"IdUsuario"                         BIGINT              not null,  --Usuario creador
"FechaCreacion"               SECONDDATE          not null,  --fecha creacion
"Procesado"                         NVARCHAR(1)         not null,  --S/N Evento fue procesado
"TipoEntidadOrigen"                 NVARCHAR(3)         not null,  --Tipo de entidad CLI, EMP, POR
"IdEntidadOrigen"                   BIGINT              not null,  --Id Entidad que gatillo el evento (Cliente/Empleado)
"TipoEntidadDestino"          NVARCHAR(3)         not null,  --Tipo de entidad CLI, EMP, POR
"IdEntidadDestino"                  BIGINT              not null,  --Id Entidad destino del evento (Cliente/Empleado/Portafolio)
"DatosEvento"     TEXT      not null, --Datos que se usaran por la planilla
primary key ("IdEvento"));

create column table "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" (
"IdEmpresa"                          BIGINT              not null,  --Empresa que pertenece la configuracion del evento.
"IdConfiguracionEvento"      BIGINT              not null,  --ID Unico
"Descripcion"                  NVARCHAR(520)       not null,  --Descipción de la configuracion
"IdUsuario"                    BIGINT              not null,  --Usuario creador
"FechaCreacion"                SECONDDATE          not null,  --fecha creacion
"IdEstado"                           NVARCHAR(3)         not null, --ACT/INA
"PermitePersonalizar"          NVARCHAR(1)                not null default 'N',--S/N sobreescribe IdEstado desde el portafolio
"FormaDespacho"                      NVARCHAR(1)                not null default 'U',--U=UI C=Correo  T=Ambos
"TiempoDespacho"               NVARCHAR(1)                not null default 'M',-- I=Inmediata,C=Al Cierre día, N=Apertura al día siguiente, M=Manual
"ConfirmacionDespacho"               NVARCHAR(1)                not null default 'N',--S/N  va depender de la forma de despacho
"FechaVencimiento"             DATE                not null default '0001-01-01',  --fecha de vencimiento para la confirmacion
"FechaRecordatorio"            DATE                       not null default '0001-01-01', --Si requiere confirmacion puede re-enviarse la notificación antes de su vencimiento
"IdPlantilla"                BIGINT              not null,  --ID plantilla que va ser usado
"IdPlantillaRecordatorio"    BIGINT              not null,  --ID plantillaRecordatorio que va ser usado
primary key ("IdConfiguracionEvento"));

 
create column table "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle" (
"IdConfiguracionEventoDetalle"       BIGINT              not null,  --ID Unico
"IdConfiguracionEvento"              BIGINT              not null,  --ID Referencia al Agrupador
"IdUsuario"                                 BIGINT              not null,  --Usuario agredo detalle
"FechaCreacion"                             SECONDDATE          not null,  --fecha detalle
"CodigoTipoEvento"                   NVARCHAR(40)       not null,  --FK Referencia al tipo evento usado por el agrupador
primary key ("IdConfiguracionEventoDetalle"));


create column table "BSCL.Plataforma.BD::EVT.PersonalizacionEvento" (
"IdPortafolio"                       BIGINT              not null,  --ID Unico
"IdConfiguracionEvento"      BIGINT              not null,  --FK  ConfiguracionEvento
"IdEstado"                           NVARCHAR(3)         not null,  --ACT/INA
"FechaCreacion"                SECONDDATE          not null,  --fecha creacion
"IdUsuario"                    BIGINT              not null,  --Usuario creador   
primary key ("IdPortafolio"));


create column table "BSCL.Plataforma.BD::EVT.Notificacion" (
"IdEmpresa"                          BIGINT              not null,  --Empresa que pertenece la notificación.
"IdNotificacion"               BIGINT              not null,  --ID Unico Notificacion
"IdConfiguracionEvento"        BIGINT              not null,  --FK ConfiguracionEvento que le corresponde
"FechaCreacion"                SECONDDATE          not null,  --Fecha creacion
"FormaDespacho"                      NVARCHAR(1)                not null,  --Forma de despacho de ConfiguracionEvento
"TiempoDespacho"                     NVARCHAR(1)                not null,  --Tiempo de despacho de ConfiguracionEvento
"FechaDespachoOrginal"       SECONDDATE          not null,  --Fecha despacho programado original
"FechaDespachoReintento"     SECONDDATE          not null,  --Fecha despacho programado reintento Ejem: 15min
"NumeroIntento"                      INT                               not null,  --Maximo numero de intentos.
"Enviado"                                   NVARCHAR(1)                not null default 'N',  --S/N si fue enviado
"FechaEnviado"                       SECONDDATE          not null default '0001-01-01',  --fecha en que fue enviado
"IdUsuarioEnviado"                   BIGINT              not null,  --Usuario que envio, aplica para MANUAL   
"ReEnviado"                          NVARCHAR(1)                not null default 'N',  --S/N si fue re-enviado
"FechaReEnviado"               SECONDDATE          not null default '0001-01-01',  --fecha en que fue enviado
"IdUsuarioReEnviado"           BIGINT              not null    ,  --Usuario que envio, aplica para MANUAL   
"ConfirmacionDespacho"               NVARCHAR(1)                not null,  --Confirmacion Despacho de ConfiguracionEvento
"TokenConfirmacion"          NVARCHAR(80)          not null,  --token unico usado para identificar la confirmacion
"Confirmado"                         NVARCHAR(1)                not null,  --S/N si fue confirmado
"FechaConfirmado"              SECONDDATE          not null default '0001-01-01',  --fecha en que fue confirmado
"AccionConfirmada"                   NVARCHAR(3)                not null default 'PEN',   -- PEN Pendiente, RZD Rechazado, APD Aprobado , VCD Vencido  
"IdUsuarioConfimado"           BIGINT              not null,  --Usuario que aplico la confirmacion en caso
"FechaVencimiento"             DATE                not null default '0001-01-01',  --fecha de vencimiento para la confirmacion de ConfiguracionEvento
"FechaRecordatorio"            DATE                       not null default '0001-01-01', --Si requiere confirmacion puede re-enviarse la notificación antes de su vencimiento
"Observacion"                  NVARCHAR(520)       not null default 'PEN', --Observación extra al confirmar/rechazar
primary key ("IdNotificacion"));

create row table "BSCL.Plataforma.BD::EVT.FormaDespacho" (
"IdFormaDespacho"                       BIGINT              not null,  --ID Unico
"NombreAbreviado"                           NVARCHAR(1)         not null,  -- nombre abreviado de la forma de despacho
"Descripcion"                 NVARCHAR(200)           not null,  --Descripcion de la forma de despacho
primary key ("IdFormaDespacho"));

create row table "BSCL.Plataforma.BD::EVT.TiempoDespacho" (
"IdTiempoDespacho"                       BIGINT              not null,  --ID Unico
"NombreAbreviado"                           NVARCHAR(1)         not null,  --ACT/INA
"Descripcion"                 NVARCHAR(200)           not null,  --Descripcion del tiempo de despacho
primary key ("IdTiempoDespacho"));

create column table "BSCL.Plataforma.BD::EVT.DireccionEvento" 
(
	"IdDireccionEvento" NVARCHAR(6),
	"Descripcion" 		NVARCHAR(200)
	primary key ("IdDireccionEvento")
);


create sequence "BSCL.Plataforma.BD.Secuencias::EVT.TipoEvento" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::EVT.Plantilla" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::EVT.Evento" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::EVT.Notificacion" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::EVT.ConfiguracionEvento" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::EVT.PersonalizacionEvento" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::EVT.ConfiguracionEventoDetalle" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;
--################################################
--    FIN MODELO DATOS - NOTIFICACIÓN
--################################################



--ComboBox TiempoDespacho
INSERT INTO  "BSCL.Plataforma.BD::EVT.TiempoDespacho" VALUES (1,'I','Inmediata');
INSERT INTO  "BSCL.Plataforma.BD::EVT.TiempoDespacho" VALUES (2,'C','Al cierre del día');
INSERT INTO  "BSCL.Plataforma.BD::EVT.TiempoDespacho" VALUES (3,'N','Apertura al dia siguiente');
INSERT INTO  "BSCL.Plataforma.BD::EVT.TiempoDespacho" VALUES (4,'M','Manual');

--ComboBox  FormaDespacho
INSERT INTO  "BSCL.Plataforma.BD::EVT.FormaDespacho" VALUES (1,'C','Correo');
INSERT INTO  "BSCL.Plataforma.BD::EVT.FormaDespacho" VALUES (2,'U','UI');
INSERT INTO  "BSCL.Plataforma.BD::EVT.FormaDespacho" VALUES (3,'T','Todas');

--SP NOTIFICACIÓN
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(203,'BSCL.Plataforma.BD.Servicios::NOT.AgregarPlantilla',0,'P',' ');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(205,'BSCL.Plataforma.BD.Servicios::NOT.AgregarConfiguracionEvento',0,'P',' ');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(207,'BSCL.Plataforma.BD.Servicios::NOT.AgregarTipoEvento',0,'P',' ');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(209,'BSCL.Plataforma.BD.Servicios::NOT.AsocicarTipoEventoConConfiguracion',0,'P',' ');


INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(213,'BSCL.Orden.BD.Servicios::CrearSimulacionAsignacion',0,'P',' ');


