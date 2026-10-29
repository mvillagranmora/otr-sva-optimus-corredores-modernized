SET SCHEMA "BSCL";

/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/

INSERT INTO "BSCL"."BSCL.Orden.BD::ORD.InstruccionMotivo" ("IdMotivo", "IdEstado", "CodigoEstado", "Motivo", "Tipo", "IdTipoCanalIngreso", "Activo" )
VALUES("BSCL"."BSCL.Orden.BD.Secuencias::InstruccionMotivo".NEXTVAL, 'ING', 'INCLI', upper('Ingreso Cliente'), 'A', 'CLI', 'S');

INSERT INTO "BSCL"."BSCL.Orden.BD::ORD.InstruccionMotivo" ("IdMotivo", "IdEstado", "CodigoEstado", "Motivo", "Tipo", "IdTipoCanalIngreso", "Activo" )
VALUES("BSCL"."BSCL.Orden.BD.Secuencias::InstruccionMotivo".NEXTVAL,'ING', 'INCUS', upper('Ingreso Custodio'), 'A', 'AGE', 'S');

INSERT INTO "BSCL"."BSCL.Orden.BD::ORD.InstruccionMotivo" ("IdMotivo", "IdEstado", "CodigoEstado", "Motivo", "Tipo", "IdTipoCanalIngreso", "Activo" )
VALUES("BSCL"."BSCL.Orden.BD.Secuencias::InstruccionMotivo".NEXTVAL,'ING', 'CMCLI', upper('Carga Masiva Cliente'), 'A', 'CLI', 'S');

INSERT INTO "BSCL"."BSCL.Orden.BD::ORD.InstruccionMotivo" ("IdMotivo", "IdEstado", "CodigoEstado", "Motivo", "Tipo", "IdTipoCanalIngreso", "Activo" )
VALUES("BSCL"."BSCL.Orden.BD.Secuencias::InstruccionMotivo".NEXTVAL,'ING', 'CMCUS', upper('Carga Masiva Custodio'), 'A', 'AGE', 'S');

INSERT INTO "BSCL"."BSCL.Orden.BD::ORD.InstruccionMotivo" ("IdMotivo", "IdEstado", "CodigoEstado", "Motivo", "Tipo", "IdTipoCanalIngreso", "Activo" )
VALUES("BSCL"."BSCL.Orden.BD.Secuencias::InstruccionMotivo".NEXTVAL,'CNF', 'PMEXT', upper('Pre-Match exitoso'), 'A', 'SIS', 'S');

INSERT INTO "BSCL"."BSCL.Orden.BD::ORD.InstruccionMotivo" ("IdMotivo", "IdEstado", "CodigoEstado", "Motivo", "Tipo", "IdTipoCanalIngreso", "Activo" )
VALUES("BSCL"."BSCL.Orden.BD.Secuencias::InstruccionMotivo".NEXTVAL,'LIQ', 'LIDCV', upper('Liquiddo en DCV'), 'A', 'SIS', 'S');

INSERT INTO "BSCL"."BSCL.Orden.BD::ORD.InstruccionMotivo" ("IdMotivo", "IdEstado", "CodigoEstado", "Motivo", "Tipo", "IdTipoCanalIngreso", "Activo" )
VALUES("BSCL"."BSCL.Orden.BD.Secuencias::InstruccionMotivo".NEXTVAL,'MOD', 'RECLI', upper('Reparo Cliente'), 'A', 'CLI', 'S');

INSERT INTO "BSCL"."BSCL.Orden.BD::ORD.InstruccionMotivo" ("IdMotivo", "IdEstado", "CodigoEstado", "Motivo", "Tipo", "IdTipoCanalIngreso", "Activo" )
VALUES("BSCL"."BSCL.Orden.BD.Secuencias::InstruccionMotivo".NEXTVAL,'RZD', null, upper('Discrepancia Rut Cliente'), 'M', 'SIS', 'S');

INSERT INTO "BSCL"."BSCL.Orden.BD::ORD.InstruccionMotivo" ("IdMotivo", "IdEstado", "CodigoEstado", "Motivo", "Tipo", "IdTipoCanalIngreso", "Activo" )
VALUES("BSCL"."BSCL.Orden.BD.Secuencias::InstruccionMotivo".NEXTVAL,'RZD', null, upper('Discrepancia Monto Operación'), 'M', 'SIS', 'S');

INSERT INTO "BSCL"."BSCL.Orden.BD::ORD.InstruccionMotivo" ("IdMotivo", "IdEstado", "CodigoEstado", "Motivo", "Tipo", "IdTipoCanalIngreso", "Activo" )
VALUES("BSCL"."BSCL.Orden.BD.Secuencias::InstruccionMotivo".NEXTVAL,'RZD', null, upper('Discrepancia Cantidad de Instrumentos'), 'M', 'SIS', 'S');

INSERT INTO "BSCL"."BSCL.Orden.BD::ORD.InstruccionMotivo" ("IdMotivo", "IdEstado", "CodigoEstado", "Motivo", "Tipo", "IdTipoCanalIngreso", "Activo" )
VALUES("BSCL"."BSCL.Orden.BD.Secuencias::InstruccionMotivo".NEXTVAL,'RZD', null, upper('Discrepancia en Fecha Liquidación'), 'M', 'SIS', 'S');

INSERT INTO "BSCL"."BSCL.Orden.BD::ORD.InstruccionMotivo" ("IdMotivo", "IdEstado", "CodigoEstado", "Motivo", "Tipo", "IdTipoCanalIngreso", "Activo" )
VALUES("BSCL"."BSCL.Orden.BD.Secuencias::InstruccionMotivo".NEXTVAL,'RZD', null, upper('Discrepancia en Nemotécnico'), 'M', 'SIS', 'S');

INSERT INTO "BSCL"."BSCL.Orden.BD::ORD.InstruccionMotivo" ("IdMotivo", "IdEstado", "CodigoEstado", "Motivo", "Tipo", "IdTipoCanalIngreso", "Activo" )
VALUES("BSCL"."BSCL.Orden.BD.Secuencias::InstruccionMotivo".NEXTVAL,'RZD', null, upper('Discrepancia de Contraparte'), 'M', 'SIS', 'S');

INSERT INTO "BSCL"."BSCL.Orden.BD::ORD.InstruccionMotivo" ("IdMotivo", "IdEstado", "CodigoEstado", "Motivo", "Tipo", "IdTipoCanalIngreso", "Activo" )
VALUES("BSCL"."BSCL.Orden.BD.Secuencias::InstruccionMotivo".NEXTVAL,'SCN', null, upper('Sin Instrucción Contraparte'), 'M', 'SIS', 'S');

INSERT INTO "BSCL"."BSCL.Orden.BD::ORD.InstruccionMotivo" ("IdMotivo", "IdEstado", "CodigoEstado", "Motivo", "Tipo", "IdTipoCanalIngreso", "Activo" )
VALUES("BSCL"."BSCL.Orden.BD.Secuencias::InstruccionMotivo".NEXTVAL,'SCN', null, upper('Fecha de Settlement Discrepante'), 'M', 'SIS', 'S');

INSERT INTO "BSCL"."BSCL.Orden.BD::ORD.InstruccionMotivo" ("IdMotivo", "IdEstado", "CodigoEstado", "Motivo", "Tipo", "IdTipoCanalIngreso", "Activo" )
VALUES("BSCL"."BSCL.Orden.BD.Secuencias::InstruccionMotivo".NEXTVAL,'SCN', null, upper('Reparo Contraparte DCV'), 'M', 'SIS', 'S');

INSERT INTO "BSCL"."BSCL.Orden.BD::ORD.InstruccionMotivo" ("IdMotivo", "IdEstado", "CodigoEstado", "Motivo", "Tipo", "IdTipoCanalIngreso", "Activo" )
VALUES("BSCL"."BSCL.Orden.BD.Secuencias::InstruccionMotivo".NEXTVAL,'CAN', null, upper('Discrepancia con Contraparte'), 'M', 'SIS', 'S');

INSERT INTO "BSCL"."BSCL.Orden.BD::ORD.InstruccionMotivo" ("IdMotivo", "IdEstado", "CodigoEstado", "Motivo", "Tipo", "IdTipoCanalIngreso", "Activo" )
VALUES("BSCL"."BSCL.Orden.BD.Secuencias::InstruccionMotivo".NEXTVAL,'CAN', null, upper('No llegan los Fondos'), 'M', 'SIS', 'S');

INSERT INTO "BSCL"."BSCL.Orden.BD::ORD.InstruccionMotivo" ("IdMotivo", "IdEstado", "CodigoEstado", "Motivo", "Tipo", "IdTipoCanalIngreso", "Activo" )
VALUES("BSCL"."BSCL.Orden.BD.Secuencias::InstruccionMotivo".NEXTVAL,'CAN', null, upper('No existen los papeles'), 'M', 'SIS', 'S');

INSERT INTO "BSCL"."BSCL.Orden.BD::ORD.InstruccionMotivo" ("IdMotivo", "IdEstado", "CodigoEstado", "Motivo", "Tipo", "IdTipoCanalIngreso", "Activo" )
VALUES("BSCL"."BSCL.Orden.BD.Secuencias::InstruccionMotivo".NEXTVAL,'CAN', null, upper('Discrepancia en DCV'), 'M', 'SIS', 'S');

INSERT INTO "BSCL"."BSCL.Orden.BD::ORD.InstruccionMotivo" ("IdMotivo", "IdEstado", "CodigoEstado", "Motivo", "Tipo", "IdTipoCanalIngreso", "Activo" )
VALUES("BSCL"."BSCL.Orden.BD.Secuencias::InstruccionMotivo".NEXTVAL,'NLQ', null, upper('Sin Fondos Cliente'), 'M', 'SIS', 'S');

INSERT INTO "BSCL"."BSCL.Orden.BD::ORD.InstruccionMotivo" ("IdMotivo", "IdEstado", "CodigoEstado", "Motivo", "Tipo", "IdTipoCanalIngreso", "Activo" )
VALUES("BSCL"."BSCL.Orden.BD.Secuencias::InstruccionMotivo".NEXTVAL,'NLQ', null, upper('Sin Posición Cliente'), 'M', 'SIS', 'S');

INSERT INTO "BSCL"."BSCL.Orden.BD::ORD.InstruccionMotivo" ("IdMotivo", "IdEstado", "CodigoEstado", "Motivo", "Tipo", "IdTipoCanalIngreso", "Activo" )
VALUES("BSCL"."BSCL.Orden.BD.Secuencias::InstruccionMotivo".NEXTVAL,'NLQ', null, upper('Sin Fondos Contraparte'), 'M', 'SIS', 'S');

INSERT INTO "BSCL"."BSCL.Orden.BD::ORD.InstruccionMotivo" ("IdMotivo", "IdEstado", "CodigoEstado", "Motivo", "Tipo", "IdTipoCanalIngreso", "Activo" )
VALUES("BSCL"."BSCL.Orden.BD.Secuencias::InstruccionMotivo".NEXTVAL,'NLQ', null, upper('Sin Posición Contraparte'), 'M', 'SIS', 'S');

INSERT INTO "BSCL"."BSCL.Orden.BD::ORD.InstruccionMotivo" ("IdMotivo", "IdEstado", "CodigoEstado", "Motivo", "Tipo", "IdTipoCanalIngreso", "Activo" )
VALUES("BSCL"."BSCL.Orden.BD.Secuencias::InstruccionMotivo".NEXTVAL,'NLQ', null, upper('Rechazado por Ingreso Fuera de Horario'), 'M', 'SIS', 'S');

/*==============================================================*/
/* ROLLBACK:                                                    */
/*==============================================================*/
/*
DELETE FROM "BSCL"."BSCL.Orden.BD::ORD.InstruccionMotivo";
*/

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;