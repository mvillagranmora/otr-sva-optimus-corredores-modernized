SET SCHEMA "BSCL";

/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Estado" ("IdEstado", "Nombre")
VALUES('NLQ', 'NO LIQUIDADO');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Estado" ("IdEstado", "Nombre")
VALUES('SCN', 'SOLICITUD CANCELACIÓN');


/*==============================================================*/
/* ROLLBACK */
/*==============================================================*/
/*
DELETE FROM BSCL."BSCL.Plataforma.BD::PLA.Estado"
WHERE "IdEstado" IN ('NLQ','SCN');

*/
commit;
