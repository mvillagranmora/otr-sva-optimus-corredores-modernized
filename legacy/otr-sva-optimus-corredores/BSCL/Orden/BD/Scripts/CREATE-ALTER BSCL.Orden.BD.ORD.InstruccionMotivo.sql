SET SCHEMA "BSCL";

/*========================================================================================*/
/* CREATE: Almacena los motivos de ingreso/actualización/eliminación de las instrucciones */
/*========================================================================================*/

CREATE COLUMN TABLE "BSCL"."BSCL.Orden.BD::ORD.InstruccionMotivo" ("IdMotivo" BIGINT NOT NULL ,
"IdEstado" NVARCHAR(3),
"CodigoEstado" NVARCHAR(5),
"Motivo" NVARCHAR(100),
"Tipo" NVARCHAR(1),
"IdTipoCanalIngreso" NVARCHAR(3),
"Activo" NVARCHAR(1),
PRIMARY KEY ("IdMotivo"));

CREATE SEQUENCE "BSCL"."BSCL.Orden.BD.Secuencias::InstruccionMotivo" START WITH 1;

ALTER TABLE "BSCL"."BSCL.Orden.BD::ORD.InstruccionMotivo" ADD CONSTRAINT "fk_IdEstado" FOREIGN KEY ( "IdEstado") 
REFERENCES "BSCL"."BSCL.Plataforma.BD::PLA.Estado" ("IdEstado");

ALTER TABLE "BSCL"."BSCL.Orden.BD::ORD.InstruccionMotivo" ADD CONSTRAINT "fk_IdTipoCanalIngreso" FOREIGN KEY ( "IdTipoCanalIngreso") 
REFERENCES "BSCL"."BSCL.Plataforma.BD::PLA.TipoCanalIngreso"("IdTipoCanalIngreso");

/*==============================================================*/
/* ROLLBACK:                                                    */
/*==============================================================*/
--DROP TABLE "BSCL"."BSCL.Orden.BD::ORD.InstruccionMotivo";