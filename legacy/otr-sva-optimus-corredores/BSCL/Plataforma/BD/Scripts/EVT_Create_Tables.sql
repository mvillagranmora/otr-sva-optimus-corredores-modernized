SET SCHEMA "BSCL";
	
/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/
--drop table "BSCL.Plataforma.BD::ACC.Usuario" cascade;


select * from "BSCL.Plataforma.BD::EVT.TipoEventoDetalle"

drop table "BSCL.Plataforma.BD::EVT.TipoEvento"  cascade;
drop table "BSCL.Plataforma.BD::EVT.TipoEventoDetalle"  cascade;
drop table "BSCL.Plataforma.BD::EVT.Plantilla" cascade;
drop table "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" cascade;
drop table "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle" cascade;
drop table "BSCL.Plataforma.BD::EVT.PersonalizacionEvento" cascade;
drop table "BSCL.Plataforma.BD::EVT.Notificacion" cascade;
drop table "BSCL.Plataforma.BD::EVT.Evento" cascade;
drop table "BSCL.Plataforma.BD::EVT.FormaDespacho" cascade;
drop table "BSCL.Plataforma.BD::EVT.TiempoDespacho" cascade;
drop table "BSCL.Plataforma.BD::EVT.Mail" cascade;
drop table "BSCL.Plataforma.BD::EVT.Destinatario" cascade;
drop table "BSCL.Plataforma.BD::EVT.ArchivoAdjunto" cascade;
drop table "BSCL.Plataforma.BD::EVT.ConfiguracionTipoEvento" cascade;
drop table "BSCL.Plataforma.BD::EVT.ConfiguracionUsuarioTipoEvento" cascade; 
drop table "BSCL.Plataforma.BD::EVT.NotificacionDetalle" cascade;
drop table "BSCL.Plataforma.BD::EVT.EventoDetalle" cascade;
drop table "BSCL.Plataforma.BD::EVT.EventoArchivoAdjunto" cascade;
drop table "BSCL.Plataforma.BD::EVT.EventoDirecciones" cascade;
drop table "BSCL.Plataforma.BD::EVT.TagGenerales" cascade;
drop table "BSCL.Plataforma.BD::EVT.ConfiguracionEventoCopiasCorreo" cascade;
drop table "BSCL.Plataforma.BD::EVT.ConfiguracionEventoCodigoTipoEvento" cascade;

drop sequence "BSCL.Plataforma.BD.Secuencias::EVT.TipoEvento" cascade;
drop sequence "BSCL.Plataforma.BD.Secuencias::EVT.TipoEventoDetalle" cascade;   
drop sequence "BSCL.Plataforma.BD.Secuencias::EVT.Plantilla" cascade;  
drop sequence "BSCL.Plataforma.BD.Secuencias::EVT.Evento" cascade;
drop sequence "BSCL.Plataforma.BD.Secuencias::EVT.Notificacion" cascade;
drop sequence "BSCL.Plataforma.BD.Secuencias::EVT.ConfiguracionEvento" cascade;
drop sequence "BSCL.Plataforma.BD.Secuencias::EVT.PersonalizacionEvento" cascade;
drop sequence "BSCL.Plataforma.BD.Secuencias::EVT.ConfiguracionEventoDetalle" cascade;
drop sequence "BSCL.Plataforma.BD.Secuencias::EVT.Mail" cascade;
drop sequence "BSCL.Plataforma.BD.Secuencias::EVT.Destinatarios" cascade;
drop sequence "BSCL.Plataforma.BD.Secuencias::EVT.ArchivoAdjunto" cascade;
drop sequence "BSCL.Plataforma.BD.Secuencias::EVT.NotificacionDetalle" cascade;
drop sequence "BSCL.Plataforma.BD.Secuencias::EVT.ConfiguracionTipoEvento" cascade;
drop sequence "BSCL.Plataforma.BD.Secuencias::EVT.ConfiguracionUsuarioTipoEvento" cascade;
drop sequence "BSCL.Plataforma.BD.Secuencias::EVT.EventoDetalle" cascade;
drop sequence "BSCL.Plataforma.BD.Secuencias::EVT.EventoArchivoAdjunto" cascade;
drop sequence "BSCL.Plataforma.BD.Secuencias::EVT.EventoDirecciones" cascade;
drop sequence "BSCL.Plataforma.BD.Secuencias::EVT.ConfiguracionEventoCopiasCorreo" cascade;
drop sequence "BSCL.Plataforma.BD.Secuencias::EVT.TipoEventoDetalle" cascade;
drop sequence "BSCL.Plataforma.BD.Secuencias::EVT.ConfiguracionEventoCodigoTipoEvento" cascade;


/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/

create column table "BSCL.Plataforma.BD::EVT.TipoEvento" (
    "CodigoTipoEvento"    NVARCHAR(40)        NOT NULL, 
    "DireccionEvento"     NVARCHAR(6)         NOT NULL,  --EMPCLI:Empleado genera a cliente  CLIEMP: Cliente genera a empleado  SYSEMP: Systema a Empleado   EMPEMP: Sistema a Empleado
    "Descripcion"         NVARCHAR(520)       NOT NULL,
   primary key ("CodigoTipoEvento")
);

create column table "BSCL.Plataforma.BD::EVT.TipoEventoDetalle"(
	"IdTipoEventoDetalle" BIGINT	    NOT NULL,
	"Llave"			      NVARCHAR(40)  NOT NULL,
	"ValorDefault"		  NVARCHAR(100)	NOT NULL,		
	"CodigoTipoEvento"	  NVARCHAR(40)  NOT NULL,
   primary key ("IdTipoEventoDetalle")
);

create column table "BSCL.Plataforma.BD::EVT.TagGenerales"(
	"IdTagGenerales" 		  BIGINT	    NOT NULL,
	"Llave"			      NVARCHAR(40)  	NOT NULL,
	"ValorDefault"		  NVARCHAR(2000)	NOT NULL,
   primary key ("IdTagGenerales")	
 );

create column table "BSCL.Plataforma.BD::EVT.ConfiguracionTipoEvento" (
    "IdConfiguracionTipoEvento"   BIGINT          NOT NULL,
    "IdEmpresa"                   BIGINT          NOT NULL,  --Empresa que pertenece.
    "CodigoTipoEvento"            NVARCHAR(40)    NOT NULL, --fk 
    "NotificaTodosUsuarios"       NVARCHAR(1)     NOT NULL DEFAULT 'N', --SI se van a notificar todos los usuarios del sistema  aplica para SYSEMP, EMPEMP
   	"ExcluyeDestinatario" 		  NVARCHAR(1)	  NOT NULL DEFAULT 'N',
   	"CopiarRemitente"			  NVARCHAR(1)	  NOT NULL DEFAULT 'N',
   primary key ("IdConfiguracionTipoEvento")
);
ALTER TABLE "BSCL.Plataforma.BD::EVT.ConfiguracionTipoEvento" ADD CONSTRAINT "UNQ01_ConfiguracionTipoEvento" UNIQUE ("IdEmpresa","CodigoTipoEvento");



create column table "BSCL.Plataforma.BD::EVT.ConfiguracionUsuarioTipoEvento" (
    "IdConfiguracionUsuarioTipoEvento"   BIGINT          NOT NULL,
    "IdConfiguracionTipoEvento"  		 BIGINT          NOT NULL,  --FK  ConfiguracionTipoEvento
	"TipoUsuario"                   	 NVARCHAR(40)    NOT NULL,  --U Usuario  R: Rol 
    "IdUsuarioRol"            			 BIGINT     	 NOT NULL, 
    primary key ("IdConfiguracionUsuarioTipoEvento")
); 

 
create column table "BSCL.Plataforma.BD::EVT.Plantilla" (
   "IdPlantilla"            BIGINT     			NOT NULL,
   "IdEmpresa"              BIGINT    			NOT NULL,  --Empresa que pertenece.
   "IdUsuario"              BIGINT     			NOT NULL,  --Usuario que creo la notif.
   "FechaCreacion"          SECONDDATE 			NOT NULL,  --fecha creacion
   "Template"               TEXT      			NOT NULL,  --Plantilla, podrian haber tags de sistemas, y tags asociados a event
   "Asunto" 				NVARCHAR(520)       NOT NULL,
   "CodigoTipoEvento"       NVARCHAR(40)		NOT NULL,
   primary key ("IdPlantilla")

);

ALTER TABLE  "BSCL.Plataforma.BD::EVT.Plantilla" ADD UNIQUE ("Nombre");




create column table "BSCL.Plataforma.BD::EVT.Evento" (
    "IdEmpresa"                   BIGINT              NOT NULL, --Empresa que pertenece el evento.
    "IdEvento"                    BIGINT              NOT NULL, --ID Unico del evento
    "CodigoTipoEvento"            NVARCHAR(40)        NOT NULL, --FK TipoEvento
    "IdUsuario"                   BIGINT              NOT NULL, --Usuario creador
    "FechaCreacion"               SECONDDATE          NOT NULL, --fecha creacion
    "Procesado"                   NVARCHAR(1)         NOT NULL DEFAULT 'N', --S/N Evento fue procesado
    "TipoEntidadOrigen"           NVARCHAR(3)         NOT NULL DEFAULT '', --Tipo de entidad CLI, EMP, POR
    "IdEntidadOrigen"             BIGINT              NOT NULL DEFAULT -1, --Id Entidad que gatillo el evento (Cliente/Empleado)
    "TipoEntidadDestino"          NVARCHAR(3)         NOT NULL DEFAULT '', --Tipo de entidad CLI, EMP, POR
    "MensajeBody" 				  TEXT				  ,
    "IdEntidadDestino"            BIGINT              NOT NULL DEFAULT -1, --Id Entidad destino del evento (Cliente/Empleado/Portafolio)
   primary key ("IdEvento")

);

alter table "BSCL"."BSCL.Plataforma.BD::EVT.Evento" add ("IdPersonaRemitente"   	  BIGINT       NOT NULL DEFAULT 0);
alter table "BSCL"."BSCL.Plataforma.BD::EVT.Evento" add ("IdPersonaDestinatario"     BIGINT       NOT NULL DEFAULT 0);


create column table "BSCL.Plataforma.BD::EVT.EventoDetalle" (
    "IdEventoDetalle"             BIGINT              NOT NULL, --PK IdEventoDetalle
    "IdEvento"                    BIGINT              NOT NULL, --FK IdEvento
    "Llave"       			      NVARCHAR(40)        NOT NULL, --Campo
    "Valor"                  	  NVARCHAR(5000)        NOT NULL, --Valor del campo
   primary key ("IdEventoDetalle")
);


create column table "BSCL"."BSCL.Plataforma.BD::EVT.EventoArchivoAdjunto"(
 	"IdEventoArchivoAdjunto"      BIGINT              NOT NULL, --PK IdEventoDetalle
    "IdEvento"                    BIGINT              NOT NULL, --FK IdEvento
    "NombreArchivo"				  NVARCHAR(520)		  NOT NULL,
    "Archivo"       			  BLOB      		  NOT NULL, --Campo
	"Contrasenia"				  NVARCHAR(520)				  ,
 	primary key ("IdEventoArchivoAdjunto")
);


create column table "BSCL.Plataforma.BD::EVT.EventoDirecciones"(
 	"IdEventoEventoDireccion"   	  BIGINT              NOT NULL, --PK IdEventoDetalle
    "IdEvento"                  	  BIGINT              NOT NULL, --FK IdEvento
    "Direccion"       		    	  NVARCHAR(520)		  NOT NULL, --Campo
	"TipoDestinatario" 				  NVARCHAR(6) 	      NOT NULL,
 	primary key ("IdEventoEventoDireccion")
);




create column table "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" (
   "IdEmpresa"                    BIGINT        	NOT NULL,  --Empresa que pertenece la configuracion del evento.
   "IdConfiguracionEvento"        BIGINT            NOT NULL,  --ID Unico
   "Descripcion"                  NVARCHAR(520)     NOT NULL,  --Descipción de la configuracion
   "IdUsuario"                    BIGINT            NOT NULL,  --Usuario creador
   "Agrupa"						  NVARCHAR(1)		NOT NULL DEFAULT 'N',
   "FechaCreacion"                SECONDDATE        NOT NULL,  --fecha creacion
   "IdEstado"                     NVARCHAR(3)   	NOT NULL, --ACT/INA
   "PermitePersonalizar"          NVARCHAR(1)       NOT NULL DEFAULT 'N',--S/N sobreescribe IdEstado desde el portafolio
   "FormaDespacho"                NVARCHAR(1)   	NOT NULL DEFAULT 'U',--U=UI C=Correo  T=Ambos
   "TiempoDespacho"               NVARCHAR(2)       NOT NULL DEFAULT 'M',-- I=Inmediata,H=Hora UTC, M=Manual 
   "FechaDespacho"                NVARCHAR(20)      NOT NULL DEFAULT ' ',--H ->  20:00    
   "ConfirmacionDespacho"         NVARCHAR(1)       NOT NULL DEFAULT 'N',--S/N  va depender de la forma de despacho
   "FechaVencimiento"             DATE              NOT NULL DEFAULT '0001-01-01',  --fecha de vencimiento para la confirmacion
   "FechaRecordatorio"            DATE              NOT NULL DEFAULT '0001-01-01', --Si requiere confirmacion puede re-enviarse la notificación antes de su vencimiento
   "IdPlantilla"                  BIGINT            NOT NULL,  --ID plantilla que va ser usado
   "IdPlantillaRecordatorio"      BIGINT            NOT NULL,  --ID plantillaRecordatorio que va ser usado
   primary key ("IdConfiguracionEvento")
);

-- TABLA DE PASO DE LA ConfiguracionTipoEvento Y ConfiguracionUsuarioTipoEvento
 create column table BSCL."BSCL.Plataforma.BD::EVT.ConfiguracionEventoCodigoTipoEvento" (
	"IdConfiguracionCodigoTipoEvento" 		BIGINT 		 NOT NULL,
    "IdConfiguracionEvento"       		    BIGINT       NOT NULL,  --ID Unico
    "NotificaTodosUsuarios"      		    NVARCHAR(1)  NOT NULL DEFAULT 'N', --SI se van a notificar todos los usuarios del sistema  aplica para SYSEMP, EMPEMP
   	"ExcluyeDestinatario" 		 		    NVARCHAR(1)	 NOT NULL DEFAULT 'N',
 	"TipoUsuario"                           NVARCHAR(40) NOT NULL,  --U Usuario  R: Rol 
    "IdUsuarioRol"            			    BIGINT     	 NOT NULL, 
    "CopiarRemitente"   NVARCHAR(1)	 NOT NULL DEFAULT 'N',
    primary key ("IdConfiguracionCodigoTipoEvento")
); 


 
alter table "BSCL"."BSCL.Plataforma.BD::EVT.ConfiguracionEvento" add ("CopiasAdicionales"     NVARCHAR(1)       NOT NULL DEFAULT 'N');
alter table "BSCL"."BSCL.Plataforma.BD::EVT.ConfiguracionEvento" add ("RecordarDespacho"     NVARCHAR(1)       NOT NULL DEFAULT 'N');
alter table "BSCL"."BSCL.Plataforma.BD::EVT.ConfiguracionEvento" add ("DiasVencimiento"     BIGINT       NOT NULL DEFAULT 0);
alter table "BSCL"."BSCL.Plataforma.BD::EVT.ConfiguracionEvento" add ("DiasRecordatorio"    BIGINT       NOT NULL DEFAULT 0);

 
create column table bscl."BSCL.Plataforma.BD::EVT.ConfiguracionEventoCopiasCorreo"(
   "IdConfiguracionEventoCopiasCorreo"       BIGINT             NOT NULL,  --ID Unico
   "IdConfiguracionEvento"              	 BIGINT             NOT NULL,  --ID Referencia al Agrupador
   "Correo"									 NVARCHAR(520) 		NOT NULL,  -- correo asociado
   "IdUsuario"                         	 	 BIGINT             NOT NULL,  --Usuario agredo detalle
   "FechaCreacion"                     		 SECONDDATE         NOT NULL,  --fecha detalle
     primary key ("IdConfiguracionEventoCopiasCorreo")
);

 

create column table "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle" (
   "IdConfiguracionEventoDetalle"       BIGINT             NOT NULL,  --ID Unico
   "IdConfiguracionEvento"              BIGINT             NOT NULL,  --ID Referencia al Agrupador
   "IdUsuario"                          BIGINT             NOT NULL,  --Usuario agredo detalle
   "FechaCreacion"                      SECONDDATE         NOT NULL,  --fecha detalle
   "CodigoTipoEvento"                   NVARCHAR(40)       NOT NULL,  --FK Referencia al tipo evento usado por el agrupador
   "IdEstado"							NVARCHAR(3)		   NOT NULL, --ACT/INA
   primary key ("IdConfiguracionEventoDetalle")

);
ALTER TABLE "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle"
ADD CONSTRAINT "UNQ01_ConfiguracionEventoDetalle" UNIQUE ("IdConfiguracionEvento","CodigoTipoEvento");

DROP TABLE bscl."BSCL.Plataforma.BD::EVT.PersonalizacionEvento";
create column table bscl."BSCL.Plataforma.BD::EVT.PersonalizacionEvento" (
   "IdPortafolio"                 BIGINT              NOT NULL,  --ID Unico
   "IdConfiguracionEvento"        BIGINT              NOT NULL,  --FK  ConfiguracionEvento
   "IdEstado"                     NVARCHAR(3)         NOT NULL,  --ACT/INA
   "FechaCreacion"                SECONDDATE          NOT NULL,  --fecha creacion
   "IdUsuario"                    BIGINT              NOT NULL,  --Usuario creador
   "SuspensionTemporal" 		  NVARCHAR(1)		  NOT NULL DEFAULT 'N',
   "FechaInicioSuspension"		  DATE		  NOT NULL DEFAULT '0001-01-01',
   "FechaTerminoSuspension"		  DATE		  NOT NULL DEFAULT '0001-01-01',
   primary key ("IdPortafolio","IdConfiguracionEvento")
);




create column table "BSCL.Plataforma.BD::EVT.FormaDespacho" (
   "IdFormaDespacho"                       BIGINT              NOT NULL,  --ID Unico
   "Nombre"                                 NVARCHAR(40)        NOT NULL,  --FK  ConfiguracionEvento
   "CodigoFormaDespacho"  					NVARCHAR(3)         NOT NULL,  --ACT/INA
   primary key ("IdFormaDespacho")

);

create column table "BSCL.Plataforma.BD::EVT.TiempoDespacho" (
   "IdTiempoDespacho"                       BIGINT              NOT NULL,  --ID Unico
   "Nombre"                                 NVARCHAR(40)        NOT NULL,  --FK  ConfiguracionEvento
   "CodigoTiempoDespacho"  					NVARCHAR(3)         NOT NULL,  --ACT/INA
   primary key ("IdTiempoDespacho")

);

create column table "BSCL.Plataforma.BD::EVT.Notificacion" 
(
   "IdEmpresa"                   BIGINT              NOT NULL,  --Empresa que pertenece la notificación.
   "IdNotificacion"              BIGINT              NOT NULL,  --ID Unico Notificacion
   "IdConfiguracionEvento"       BIGINT              NOT NULL,  --FK ConfiguracionEvento que le corresponde
   "FechaCreacion"               SECONDDATE          NOT NULL,  --Fecha creacion
   "FormaDespacho"               NVARCHAR(1)         NOT NULL,  --Forma de despacho de ConfiguracionEvento
   "TiempoDespacho"              NVARCHAR(1)         NOT NULL,  --Tiempo de despacho de ConfiguracionEvento
   "FechaDespachoOrginal"        SECONDDATE          NOT NULL,  --Fecha despacho programado original
   "FechaDespachoRecordatorio"   SECONDDATE          NOT NULL,  --Fecha despacho Recordatorio
    --
   "Enviar" 					 NVARCHAR(1)         NOT NULL DEFAULT 'N',  --S/N si quiere enviarse, luego de enviar cambia a N
   "Enviado"                     NVARCHAR(1)         NOT NULL DEFAULT 'N',  --S/N si fue enviado
   "FechaEnviado"                SECONDDATE          NOT NULL DEFAULT '0001-01-01',  --fecha en que fue enviado
   "IdUsuarioEnviado"            BIGINT              NOT NULL DEFAULT 0,  --Usuario que envio, aplica para MANUAL   
   --
   "ReEnviar"                    NVARCHAR(1)         NOT NULL DEFAULT 'N',  --S/N si quiere enviarse, luego de reenviar cambia a N
   "ReEnviado"                   NVARCHAR(1)         NOT NULL DEFAULT 'N',  --S/N si fue re-enviado
   "FechaReEnviado"              SECONDDATE          NOT NULL DEFAULT '0001-01-01',  --fecha en que fue enviado
   "IdUsuarioReEnviado"          BIGINT              NOT NULL DEFAULT 0,  --Usuario que envio, aplica para MANUAL   
    --
   "ConfirmacionDespacho"        NVARCHAR(1)         NOT NULL  ,  --Confirmacion Despacho de ConfiguracionEvento
   "TokenConfirmacion"           NVARCHAR(80)        NOT NULL DEFAULT ' ',  --token unico usado para identificar la confirmacion
   "Confirmado"                  NVARCHAR(1)         NOT NULL DEFAULT 'N',  --S/N si fue confirmado
   "FechaConfirmado"             SECONDDATE          NOT NULL DEFAULT '0001-01-01',  --fecha en que fue confirmado
   "AccionConfirmada"            NVARCHAR(3)         NOT NULL DEFAULT 'PEN',   -- PEN Pendiente, RZD Rechazado, APD Aprobado , VCD Vencido  
   "IdUsuarioConfimado"          BIGINT              NOT NULL DEFAULT 0,  --Usuario que aplico la confirmacion en caso
   "FechaVencimiento"            DATE                NOT NULL DEFAULT '0001-01-01',  --fecha de vencimiento para la confirmacion de ConfiguracionEvento
   "FechaRecordatorio"           DATE                NOT NULL DEFAULT '0001-01-01', --Si requiere confirmacion puede re-enviarse la notificación antes de su vencimiento
   "Observacion"                 NVARCHAR(520)       NOT NULL DEFAULT 'PEN', --Observación extra al confirmar/rechazar
   --
   "Descartado" 				 NVARCHAR(1)         NOT NULL DEFAULT 'N',
   "IdUsuarioDescarte"			 BIGINT				 NOT NULL DEFAULT 0,
   "FechaDescarte"				 SECONDDATE			 NOT NULL DEFAULT '0001-01-01',

   primary key ("IdNotificacion")

);

ALTER TABLE "BSCL"."BSCL.Plataforma.BD::EVT.Notificacion" DROP ("DireccionAlternativa");
ALTER TABLE "BSCL"."BSCL.Plataforma.BD::EVT.Notificacion" DROP ("Leida");




 create column table "BSCL.Plataforma.BD::EVT.NotificacionDetalle" 
(  "IdNotificacionDetalle"            BIGINT         NOT NULL,  --Id Notificacion Detalle
   "IdNotificacion"               	  BIGINT  	     NOT NULL,  --Id Notificacion FK 
   "IdEvento"                         BIGINT		 NOT NULL,  --Id Evento
   "Despachado"                    	  NVARCHAR(1)    NOT NULL,  --S/N 
   primary key ("IdNotificacionDetalle")
);


drop table "BSCL.Plataforma.BD::EVT.Mail" 
create column table "BSCL.Plataforma.BD::EVT.Mail" 
(  "IdEmail"                   		   BIGINT         NOT NULL,  --ID Unico Mail
   "FechaCreacion"               	   SECONDDATE     NOT NULL,  --Fecha creacion
   "Remitente"                         NVARCHAR(200)  NOT NULL,  --Remitente
   "Asunto"                    		   NVARCHAR(200)  NOT NULL,  --Asuto
   "MensajeCuerpo"                     TEXT			  NOT NULL,  --Mensaje
   "EstadoEnviado" 					   NVARCHAR(1)    NOT NULL DEFAULT 'N',
   "FechaEnviado" 					   SECONDDATE     NOT NULL DEFAULT '0001-01-01', 
   primary key ("IdEmail")
);

create column table "BSCL.Plataforma.BD::EVT.Destinatario" 
( 
   "IdDestinatario"                    BIGINT         NOT NULL,  --ID Grupo Destinatario
   "IdEmail"						   BIGINT 		  NOT NULL, -- FK IdEmail
   "TipoDestinatario" 				   NVARCHAR(5)    NOT NULL, -- TO, CC, BCC, REPLY
   "Destinatario"               	   NVARCHAR(200)  NOT NULL,  -- Mail, puede ser con alias. Ej: "Juanito Corp" <juanito@mail.com>
   primary key ("IdDestinatario")
);

 
create column table "BSCL.Plataforma.BD::EVT.ArchivoAdjunto" 
(  
   "IdArchivoAdjunto"               BIGINT         NOT NULL,  --ID Grupo ArchivoAdjunto
   "IdEmail"               	  	    BIGINT     	   NOT NULL,  --FK IdEmail
   "Nombre"                         NVARCHAR(200)   ,  --Nombre del archivo adjunto
   "Archivo"                    	BLOB,  --Archivo en binario
   primary key ("IdArchivoAdjunto")
);

         

/*==============================================================*/
/* constraint:                                                  */
/*==============================================================*/
/* Table: */
/*==============================================================*/
/* SECUENCES:                                                       */
/*==============================================================*/

create sequence "BSCL.Plataforma.BD.Secuencias::EVT.TipoEvento" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::EVT.TipoEventoDetalle" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::EVT.Plantilla" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::EVT.Evento" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::EVT.Notificacion" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::EVT.ConfiguracionEvento" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::EVT.PersonalizacionEvento" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::EVT.ConfiguracionEventoDetalle" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::EVT.Mail" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::EVT.Destinatarios" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::EVT.ArchivoAdjunto" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::EVT.NotificacionDetalle" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::EVT.ConfiguracionUsuarioTipoEvento" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::EVT.ConfiguracionTipoEvento" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::EVT.EventoDetalle" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::EVT.EventoArchivoAdjunto" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::EVT.EventoDirecciones" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::EVT.ConfiguracionEventoCopiasCorreo" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::EVT.TipoEventoDetalle" increment by 1 start with 2 minvalue 1 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::EVT.ConfiguracionEventoCodigoTipoEvento" increment by 1 start with 2 minvalue 1 maxvalue 4611686018427387903 no cycle;

CREATE SEQUENCE "BSCL"."BSCL.Plataforma.BD.Secuencias::EventoNotificacion"
	START     WITH 1
    INCREMENT BY 1
    MINVALUE  1;
 
 
/*==============================================================*/
/* constraint:                                                  */
/*==============================================================*/

/* Table: */


alter table "BSCL.Plataforma.BD::EVT.ConfiguracionTipoEvento"
add constraint "FK_TipoEvento_ConfiguracionTipoEvento" foreign key ("CodigoTipoEvento")
references "BSCL.Plataforma.BD::EVT.TipoEvento" ("CodigoTipoEvento") on delete restrict on update restrict;


alter table "BSCL.Plataforma.BD::EVT.TipoEventoDetalle"
add constraint "FK_TipoEventoDetalle_TipoEvento" foreign key ("CodigoTipoEvento")
references "BSCL.Plataforma.BD::EVT.TipoEvento" ("CodigoTipoEvento") on delete restrict on update restrict;


alter table "BSCL.Plataforma.BD::EVT.ConfiguracionUsuarioTipoEvento"
add constraint "FK_ConfiguracionTipoEvento_ConfiguracionTipoEvento" foreign key ("IdConfiguracionTipoEvento")
references "BSCL.Plataforma.BD::EVT.ConfiguracionTipoEvento" ("IdConfiguracionTipoEvento") on delete restrict on update restrict;

alter table "BSCL.Plataforma.BD::EVT.Evento"
add constraint "FK_Evento_TipoEvento" foreign key ("CodigoTipoEvento")
references "BSCL.Plataforma.BD::EVT.TipoEvento" ("CodigoTipoEvento") on delete restrict on update restrict;


alter table "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle"
add constraint "FK_ConfiguracionEventoDetalle_TipoEvento" foreign key ("CodigoTipoEvento")
references "BSCL.Plataforma.BD::EVT.TipoEvento" ("CodigoTipoEvento") on delete restrict on update restrict;

alter table "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle"
add constraint "FK_ConfiguracionEventoDetalle_ConfiguracionEvento" foreign key ("IdConfiguracionEvento")
references "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" ("IdConfiguracionEvento") on delete restrict on update restrict;


alter table "BSCL.Plataforma.BD::EVT.PersonalizacionEvento"
add constraint "FK_PersonalizacionEvento_ConfiguracionEvento" foreign key ("IdConfiguracionEvento")
references "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" ("IdConfiguracionEvento") on delete restrict on update restrict;

alter table "BSCL.Plataforma.BD::EVT.Notificacion"
add constraint "FK_Notificacion_ConfiguracionEvento" foreign key ("IdConfiguracionEvento")
references "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" ("IdConfiguracionEvento") on delete restrict on update restrict;

alter table "BSCL.Plataforma.BD::EVT.NotificacionDetalle"
add constraint "FK_NotificacionDetalle_Notificacion" foreign key ("IdNotificacion")
references "BSCL.Plataforma.BD::EVT.Notificacion" ("IdNotificacion") on delete restrict on update restrict;


alter table bscl."BSCL.Plataforma.BD::EVT.Destinatario"
add constraint "FK_Destinatario_Mail" foreign key ("IdEmail")
references bscl."BSCL.Plataforma.BD::EVT.Mail" ("IdEmail") on delete restrict on update restrict;

 
alter table bscl."BSCL.Plataforma.BD::EVT.ArchivoAdjunto"
add constraint "FK_ArchivoAdjunto_Mail" foreign key ("IdEmail")
references bscl."BSCL.Plataforma.BD::EVT.Mail" ("IdEmail") on delete restrict on update restrict;

alter table "BSCL.Plataforma.BD::EVT.EventoDetalle"
add constraint "FK_EventoDetalle_Evento" foreign key ("IdEvento")
references "BSCL.Plataforma.BD::EVT.Evento" ("IdEvento") on delete restrict on update restrict;
 
commit
