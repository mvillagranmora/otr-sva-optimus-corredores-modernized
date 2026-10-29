SET SCHEMA BSCL;
drop table "BSCL.Plataforma.BD::EVT.TipoEvento"  cascade;
drop table "BSCL.Plataforma.BD::EVT.Plantilla" cascade;
drop table "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" cascade;
drop table "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle" cascade;
drop table "BSCL.Plataforma.BD::EVT.PersonalizacionEvento" cascade;
drop table "BSCL.Plataforma.BD::EVT.Notificacion" cascade;
drop table "BSCL.Plataforma.BD::EVT.Evento" cascade;
drop table "BSCL.Plataforma.BD::EVT.FormaDespacho" cascade;
drop table "BSCL.Plataforma.BD::EVT.TiempoDespacho" cascade;

drop sequence "BSCL.Plataforma.BD.Secuencias::EVT.TipoEvento" cascade;  
drop sequence "BSCL.Plataforma.BD.Secuencias::EVT.Plantilla" cascade;  
drop sequence "BSCL.Plataforma.BD.Secuencias::EVT.Evento" cascade;
drop sequence "BSCL.Plataforma.BD.Secuencias::EVT.Notificacion" cascade;
drop sequence "BSCL.Plataforma.BD.Secuencias::EVT.ConfiguracionEvento" cascade;
drop sequence "BSCL.Plataforma.BD.Secuencias::EVT.PersonalizacionEvento" cascade;
drop sequence "BSCL.Plataforma.BD.Secuencias::EVT.ConfiguracionEventoDetalle" cascade;




/*==============================================================*/

/* Create Table:                                                */

/*==============================================================*/
create column table "BSCL.Plataforma.BD::EVT.TipoEvento" (

    "CodigoTipoEvento"            NVARCHAR(40)       not null, 

    "DireccionEvento"     NVARCHAR(6)         not null,  --EMPCLI:Empleado genera a cliente  CLIEMP: Cliente genera a empleado  SYSEMP: Systema a Empleado

    "Descripcion"                 NVARCHAR(520)       not null,

    "CamposPlantilla"     NVARCHAR(5000)        not null, -- json describe campo de plantilla con un ejemplo 

   primary key ("CodigoTipoEvento")

);

 

 

create column table "BSCL.Plataforma.BD::EVT.Plantilla" (


   "IdPlantilla"            BIGINT      not null,
   
   "Descripcion"			NVARCHAR(200) NOT NULL,

   "IdEmpresa"                    BIGINT      not null,  --Empresa que pertenece.

   "IdUsuario"             BIGINT      not null,  --Usuario que creo la notif.

   "FechaCreacion"          SECONDDATE      not null,  --fecha creacion

   "Template"                     NCLOB       not null,  --Plantilla, podrian haber tags de sistemas, y tags asociados a event

   primary key ("IdPlantilla")

);

 

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



   primary key ("IdEvento")

);

 

 

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

   "SuspencionTemporal"			 NVARCHAR(1) 	  not null default 'S', -- S/N

   "FechaInicioSuspencion" 	 DATE 		NOT NULL  default '0001-01-01',

   "FechaFinalSuspencion" 	 	 DATE 		NOT NULL  default '0001-01-01'
  
   "IdPlantilla"                BIGINT              not null,  --ID plantilla que va ser usado

   "IdPlantillaRecordatorio"    BIGINT              not null,  --ID plantillaRecordatorio que va ser usado

   primary key ("IdConfiguracionEvento")

);

 

create column table "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle" (

   "IdConfiguracionEventoDetalle"       BIGINT              not null,  --ID Unico

   "IdConfiguracionEvento"              BIGINT              not null,  --ID Referencia al Agrupador

   "IdUsuario"                                 BIGINT              not null,  --Usuario agredo detalle

   "FechaCreacion"                             SECONDDATE          not null,  --fecha detalle

   "CodigoTipoEvento"                   NVARCHAR(40)       not null,  --FK Referencia al tipo evento usado por el agrupador

   primary key ("IdConfiguracionEventoDetalle")

);

 

 

create column table "BSCL.Plataforma.BD::EVT.PersonalizacionEvento" (

   "IdPortafolio"                       BIGINT              not null,  --ID Unico

   "IdConfiguracionEvento"      BIGINT              not null,  --FK  ConfiguracionEvento

   "IdEstado"                           NVARCHAR(3)         not null,  --ACT/INA

   "FechaCreacion"                SECONDDATE          not null,  --fecha creacion

   "IdUsuario"                    BIGINT              not null,  --Usuario creador   

    primary key ("IdPortafolio")

);

 

 

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

    --

   "ConfirmacionDespacho"               NVARCHAR(1)                not null,  --Confirmacion Despacho de ConfiguracionEvento

   "TokenConfirmacion"          NVARCHAR(80)          not null,  --token unico usado para identificar la confirmacion

   "Confirmado"                         NVARCHAR(1)                not null,  --S/N si fue confirmado

   "FechaConfirmado"              SECONDDATE          not null default '0001-01-01',  --fecha en que fue confirmado

   "AccionConfirmada"                   NVARCHAR(3)                not null default 'PEN',   -- PEN Pendiente, RZD Rechazado, APD Aprobado , VCD Vencido  

   "IdUsuarioConfimado"           BIGINT              not null,  --Usuario que aplico la confirmacion en caso

   "FechaVencimiento"             DATE                not null default '0001-01-01',  --fecha de vencimiento para la confirmacion de ConfiguracionEvento

   "FechaRecordatorio"            DATE                       not null default '0001-01-01', --Si requiere confirmacion puede re-enviarse la notificación antes de su vencimiento

   "Observacion"                  NVARCHAR(520)       not null default 'PEN', --Observación extra al confirmar/rechazar

   --

   primary key ("IdNotificacion")

);


create row table "BSCL.Plataforma.BD::EVT.FormaDespacho" (

   "IdFormaDespacho"                       BIGINT              not null,  --ID Unico

   "NombreAbreviado"                           NVARCHAR(1)         not null,  -- nombre abreviado de la forma de despacho

   "Descripcion"                 NVARCHAR(200)           not null,  --Descripcion de la forma de despacho

 
   primary key ("IdFormaDespacho")

);

create row table "BSCL.Plataforma.BD::EVT.TiempoDespacho" (

   "IdTiempoDespacho"                       BIGINT              not null,  --ID Unico

   "NombreAbreviado"                           NVARCHAR(1)         not null,  --ACT/INA

   "Descripcion"                 NVARCHAR(200)           not null,  --Descripcion del tiempo de despacho

 
   primary key ("IdTiempoDespacho")

);



create sequence "BSCL.Plataforma.BD.Secuencias::EVT.TipoEvento" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::EVT.Plantilla" increment by 1 start with 2 minvalue 2 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::EVT.Evento" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::EVT.Notificacion" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::EVT.ConfiguracionEvento" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::EVT.PersonalizacionEvento" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::EVT.ConfiguracionEventoDetalle" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;
 


ALTER TABLE "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle"
ADD CONSTRAINT "UNQ01_ConfiguracionEventoDetalle" UNIQUE ("IdConfiguracionEvento","CodigoTipoEvento")

