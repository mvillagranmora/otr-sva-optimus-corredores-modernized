SET SCHEMA "BSCL";

/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/

INSERT INTO BSCL."BSCL.Orden.BD::ORD.EstadoOrden" ("IdEstadoOrden", "IdEstado", "NombreEntidad")
VALUES(58, 'NLQ', 'INSTRUCCION');

INSERT INTO BSCL."BSCL.Orden.BD::ORD.EstadoOrden" ("IdEstadoOrden", "IdEstado", "NombreEntidad")
VALUES(59, 'SCN', 'INSTRUCCION');

INSERT INTO BSCL."BSCL.Orden.BD::ORD.EstadoOrden" ("IdEstadoOrden", "IdEstado", "NombreEntidad")
VALUES(60, 'CAN', 'INSTRUCCION');

INSERT INTO BSCL."BSCL.Orden.BD::ORD.EstadoOrden" ("IdEstadoOrden", "IdEstado", "NombreEntidad")
VALUES(61, 'MOD', 'INSTRUCCION');

/*==============================================================*/
/* ROLLBACK:                                                    */
/*==============================================================*/
/*
DELETE FROM  BSCL."BSCL.Orden.BD::ORD.EstadoOrden"
WHERE "IdEstadoOrden" IN (58,59,60,61); 
*/

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;