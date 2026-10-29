set schema "BSCL";

-- Claves de la ventana Datatec Monitor Automatico
-- (Mesa/UI/SPT/DatatecMonitorAutomatico)

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Diccionario"
select 'UI_DATATEC_MONITOR_AUTOMATICO','CL','Datatec Monitor Automatico','' from dummy;

-- Bloqueo de la interfaz Datatec (parametro de mesa USUARIOBLOQUEODATATEC)

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Diccionario"
select 'UI_DATATEC_BLOQUEADA','CL','Carga Online Datatec Bloqueada','' from dummy;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Diccionario"
select 'UI_DATATEC_BLOQUEAR','CL','Bloquear','' from dummy;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Diccionario"
select 'UI_DATATEC_DESBLOQUEAR','CL','Desbloquear','' from dummy;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Diccionario"
select 'UI_DATATEC_ESTADO_BLOQUEO','CL','Bloqueo','' from dummy;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Diccionario"
select 'UI_DATATEC_BLOQUEO_DESCONOCIDO','CL','Sin determinar','' from dummy;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Diccionario"
select 'UI_DATATEC_BLOQUEO_LIBRE','CL','Liberado','' from dummy;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Diccionario"
select 'UI_DATATEC_BLOQUEO_BLOQUEADO_POR','CL','Bloqueado por','' from dummy;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Diccionario"
select 'UI_DATATEC_BLOQUEO_OK','CL','Interfaz Datatec bloqueada','' from dummy;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Diccionario"
select 'UI_DATATEC_DESBLOQUEO_OK','CL','Interfaz Datatec liberada','' from dummy;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Diccionario"
select 'UI_DATATEC_YA_BLOQUEADA_POR','CL','La interfaz Datatec ya esta bloqueada por XXX. Verifique antes de conectar el archivo.','' from dummy;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Diccionario"
select 'UI_ERROR_DESBLOQUEAR_INTERFAZ_DATATEC','CL','No se pudo liberar la interfaz Datatec.','' from dummy;

-- Monitor de archivo local (el operador elige el archivo directamente, no una carpeta)

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Diccionario"
select 'UI_DATATEC_CONECTAR_ARCHIVO','CL','Conectar archivo','' from dummy;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Diccionario"
select 'UI_DATATEC_DESCONECTAR_ARCHIVO','CL','Desconectar','' from dummy;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Diccionario"
select 'UI_DATATEC_ESTADO_MONITOR','CL','Estado del monitor','' from dummy;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Diccionario"
select 'UI_DATATEC_ARCHIVO_CONECTADO','CL','Archivo conectado','' from dummy;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Diccionario"
select 'UI_DATATEC_ULTIMA_LECTURA','CL','Ultima lectura','' from dummy;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Diccionario"
select 'UI_DATATEC_FS_DESCONECTADO','CL','Desconectado','' from dummy;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Diccionario"
select 'UI_DATATEC_FS_CONECTADO','CL','Conectado','' from dummy;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Diccionario"
select 'UI_DATATEC_FS_SIN_CAMBIO','CL','Conectado, sin cambios','' from dummy;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Diccionario"
select 'UI_DATATEC_FS_ESPERANDO_CONFIG','CL','Cargando configuracion de la mesa','' from dummy;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Diccionario"
select 'UI_DATATEC_FS_INESTABLE','CL','Archivo en escritura, esperando','' from dummy;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Diccionario"
select 'UI_DATATEC_FS_NO_ENCONTRADO','CL','Archivo no encontrado (fue movido o eliminado)','' from dummy;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Diccionario"
select 'UI_DATATEC_FS_BLOQUEADO','CL','Archivo bloqueado por otra aplicacion','' from dummy;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Diccionario"
select 'UI_DATATEC_FS_ERROR','CL','Error de lectura','' from dummy;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Diccionario"
select 'UI_DATATEC_FS_NO_SOPORTADO','CL','El navegador no permite conectar el archivo automaticamente. Use Chrome o Edge sobre HTTPS.','' from dummy;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Diccionario"
select 'UI_DATATEC_ARCHIVO_NO_AUTORIZADO','CL','No se pudo obtener acceso al archivo Datatec.','' from dummy;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Diccionario"
select 'UI_DATATEC_PERMISO_REVOCADO','CL','Se perdio el permiso sobre el archivo Datatec. Vuelva a conectarlo.','' from dummy;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Diccionario"
select 'UI_DATATEC_ERROR_LECTURA','CL','Error al leer el archivo Datatec conectado.','' from dummy;

commit;
