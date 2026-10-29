SET SCHEMA "BSCL";


/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/
DELETE FROM BSCL."BSCL.Orden.BD::ORD.TipoEventoBitacora" WHERE "IdTipoEventoBitacora" = 'INSTRUCCION_SOLICITUD_CANCELACION' AND "DescripcionTipoEventoBitacora" = 'INSTRUCCION SOLICITUD CANCELACION';
DELETE FROM BSCL."BSCL.Orden.BD::ORD.TipoEventoBitacora" WHERE "IdTipoEventoBitacora" = 'INSTRUCCION_MODIFICADA' AND "DescripcionTipoEventoBitacora" = 'INSTRUCCION MODIFICADA';
DELETE FROM BSCL."BSCL.Orden.BD::ORD.TipoEventoBitacora" WHERE "IdTipoEventoBitacora" = 'INSTRUCCION_ACEPTACION_SOL_CANCELACION' AND "DescripcionTipoEventoBitacora" = 'INSTRUCCION ACEPTACION SOLICITUD CANCELACION';
DELETE FROM BSCL."BSCL.Orden.BD::ORD.TipoEventoBitacora" WHERE "IdTipoEventoBitacora" = 'INSTRUCCION_RECHAZO_SOL_CANCELACION' AND "DescripcionTipoEventoBitacora" = 'INSTRUCCION RECHAZO SOLICITUD CANCELACION';


INSERT INTO BSCL."BSCL.Orden.BD::ORD.TipoEventoBitacora" ("IdTipoEventoBitacora", "DescripcionTipoEventoBitacora")
VALUES('INSTRUCCION_SOLICITUD_CANCELACION', 'INSTRUCCION SOLICITUD CANCELACION');

INSERT INTO BSCL."BSCL.Orden.BD::ORD.TipoEventoBitacora" ("IdTipoEventoBitacora", "DescripcionTipoEventoBitacora")
VALUES('INSTRUCCION_MODIFICADA', 'INSTRUCCION MODIFICADA');

INSERT INTO BSCL."BSCL.Orden.BD::ORD.TipoEventoBitacora" ("IdTipoEventoBitacora", "DescripcionTipoEventoBitacora")
VALUES('INSTRUCCION_ACEPTACION_SOL_CANCELACION', 'INSTRUCCION ACEPTACION SOLICITUD CANCELACION');

INSERT INTO BSCL."BSCL.Orden.BD::ORD.TipoEventoBitacora" ("IdTipoEventoBitacora", "DescripcionTipoEventoBitacora")
VALUES('INSTRUCCION_RECHAZO_SOL_CANCELACION', 'INSTRUCCION RECHAZO SOLICITUD CANCELACION');


/*==============================================================*/
/* ROLLBACK:                                                    */
/*==============================================================*/
/*
DELETE FROM BSCL."BSCL.Orden.BD::ORD.TipoEventoBitacora" 
WHERE "IdTipoEventoBitacora" = 'INSTRUCCION_SOLICITUD_CANCELACION' AND "DescripcionTipoEventoBitacora" = 'INSTRUCCION SOLICITUD CANCELACION';

DELETE FROM BSCL."BSCL.Orden.BD::ORD.TipoEventoBitacora" 
WHERE "IdTipoEventoBitacora" = 'INSTRUCCION_MODIFICADA' AND "DescripcionTipoEventoBitacora" = 'INSTRUCCION MODIFICADA';

DELETE FROM BSCL."BSCL.Orden.BD::ORD.TipoEventoBitacora" 
WHERE "IdTipoEventoBitacora" = 'INSTRUCCION_ACEPTACION_SOL_CANCELACION' AND "DescripcionTipoEventoBitacora" = 'INSTRUCCION ACEPTACION SOLICITUD CANCELACION';

DELETE FROM BSCL."BSCL.Orden.BD::ORD.TipoEventoBitacora" 
WHERE "IdTipoEventoBitacora" = 'INSTRUCCION_RECHAZO_SOL_CANCELACION' AND "DescripcionTipoEventoBitacora" = 'INSTRUCCION RECHAZO SOLICITUD CANCELACION';
*/

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;