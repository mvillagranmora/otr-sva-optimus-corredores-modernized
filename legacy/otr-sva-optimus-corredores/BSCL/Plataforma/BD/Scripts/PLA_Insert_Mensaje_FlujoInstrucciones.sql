SET SCHEMA "BSCL";

/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/
INSERT INTO BSCL."BSCL.Plataforma.BD::EVT.Mensaje"
("IdMensaje", "Titulo", "IdTipoMensaje", "IdTipoEntidadDestinatario", "FechaVigenciaDesde", "FechaVigenciaHasta", "Habilitado", "MensajeLargo", "MuestraMensajeCorto", "UrlAviso", "IdTipoCanal", "MensajeCorto")
VALUES(39, 'RECORDATORIO_INSTRUCCIONES_RECHAZADAS', 'RCDTR', 'CLI', '2021-01-01', '2099-12-30', 'S', 'Estimado cliente, posee solicitudes de cancelación de instrucciones rechazadas sin revisar', 'N', '', '', '');

/*==============================================================*/
/* ROLLBACK:                                                    */
/*==============================================================*/
/*
DELETE FROM  BSCL."BSCL.Plataforma.BD::EVT.Mensaje"
WHERE "Titulo" IN (
'RECORDATORIO_INSTRUCCIONES_RECHAZADAS'
); 
*/

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;