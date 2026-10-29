
SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/ 
DELETE FROM  "BSCL.Plataforma.BD::PLA.Configuracion"; 

/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Configuracion" ("IdEmpresa", "CodigoParametro", "Descripcion", "Valor")
VALUES(1, 'ACC.IdRolAdmin', 'Rol del usuario admininstrador', '1');

INSERT INTO "BSCL.Plataforma.BD::PLA.Configuracion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'PLA.FechaSistema','Fecha en que esta posicionada el sistema','2015-09-18');
INSERT INTO "BSCL.Plataforma.BD::PLA.Configuracion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'PLA.IdiomaSistema','Idioma que usará el diccionario','CL');
INSERT INTO "BSCL.Plataforma.BD::PLA.Configuracion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'PLA.LocalizacionSistema','Formato que usará el Sistema','CL');
INSERT INTO "BSCL.Plataforma.BD::PLA.Configuracion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'PLA.ZonaHoraria','Zona horaria del sistema','CLT');
INSERT INTO "BSCL.Plataforma.BD::PLA.Configuracion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'PLA.SegundosZonaHoraria','Segundos Zona horaria del sistema respecto UTC','-10800');
INSERT INTO "BSCL.Plataforma.BD::PLA.Configuracion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'PLA.ClaveUsuarioIntentos','Maximo número de intentos antes de bloquear la cuenta','5');
INSERT INTO "BSCL.Plataforma.BD::PLA.Configuracion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'PLA.ClaveUsuarioTiempoBloqueo','Minutos que la clave será bloqueada temporalmente','15');
INSERT INTO "BSCL.Plataforma.BD::PLA.Configuracion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'PLA.MonedaSistema','Moneda del sistema','CLP');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Configuracion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'PLA.CoberturaEfectivoEspecies','Porcentaje de Cobertura para Efectivo ó Especies','100');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Configuracion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'PLA.CoberturaSinDefinir','Porcentaje de Cobertura cuendo esta no esté definida','0');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Configuracion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'PLA.BolsaDefectoSistema','Bolsa por defecto del sistema','BCS'); 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor")
VALUES(1,'PLA.ConflictoAccesoDefecto', 'Decide si en conflicto de accesos si manda el habilitado o no', 'S');
INSERT INTO "BSCL.Plataforma.BD::PLA.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor")
VALUES(1,'PLA.ConcurrenciaValorizador', 'Concurrencia de Valorizador', '4');
INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor")
VALUES(1, 'PLA.Version', 'Version Sistema', '20160406'); 
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Configuracion" 
VALUES(1, 'PLA.DiferenciaMontoAsignacion', 'Es el monto que puede tener de diferencia el monto asignado a una transaccion, y no ser considerado error de cuadratura', '200');
INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor")
VALUES(1, 'PLA.Ambiente', 'Tipo de Ambiente DES QA UAT PRO', 'DES');   
INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Configuracion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'PLA.ServidorWebHana','Servidor Web Hana','http://webweb:Bolsa1234@161.238.34.39:8002'); 
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Configuracion"("IdEmpresa","CodigoParametro","Descripcion","Valor")
VALUES(1,'PLA.RutaBaseArchivos','Ruta base para almacenar archivos','/mnt/optimus/des/empresa1/');

INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Configuracion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'PLA.EmisorNotificacion','Emisor de las notificaciones','no-reply@bolsadesantiago.com');
INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Configuracion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'PLA.SMTP','SMTP usado para el envio de mail','mail.smtp.host');
INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Configuracion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'PLA.AliasEmisorNotificacion','Alias del emisor de la notificación','Optimus');
INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Configuracion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'PLA.HostMailing','Host usado para el envio de notificaciones','mail.bcs.cl');
INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Configuracion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'PLA.UrlHana','Url de hana','http://161.238.34.90:9025');
INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Configuracion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'PLA.UrlPortalConfirmacion','URL del portal de confirmacion del correo','http://161.238.34.90:9025/BSCL/Plataforma/GWJAVA/PORTAL/validate');
INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Configuracion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'PLA.RangoDiasDefault','Rango por defecto de dias entre filtro fechaDesde - fechaHasta','31');
INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Configuracion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'PLA.DiasCambioClave','CANTIDAD DE DIAS MAXIMO DE VIGENCIA DE CLAVE','60');
INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Configuracion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'PLA.AccesosPerfilesRepetidos','Configura si dos perfiles pueden tener los mismos accesos','N');

INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Configuracion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'ACC.TiempoTokenValido','Minutos que un token de recuperacion clave se mantiene activo','1');

INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Configuracion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'PLA.RutaArchivosWeb','Ruta de archivos web estaticos usados por optimus para crear los formularios y la interfaz gráfica en general','/home/optimusDES/web/');


INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Configuracion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'PLA.PibMas','% PibMas utilizado en valorizacion','0.05');
INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Configuracion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'PLA.PibMenos','% PibMas utilizado en valorizacion','0.05');
INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Configuracion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'PLA.PibTipoTasa','Tipo tasa Pib utilizado en valorizacion','COMPRA'); --COMPRA,MERCADO,CORREDORA
/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
