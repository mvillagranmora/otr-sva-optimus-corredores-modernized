SET SCHEMA "BSCL";

/*=============================================================*/
/* CREATE: Almacena los cambios sufridos por las instrucciones */
/*=============================================================*/


CREATE SEQUENCE "BSCL"."BSCL.Orden.BD.Secuencias::InstruccionHistorica" START WITH 1;

CREATE COLUMN TABLE "BSCL"."BSCL.Orden.BD::ORD.InstruccionHistorica" (
"IdInstruccionHistoria" BIGINT NOT NULL,
"IdInstruccion" BIGINT NOT NULL,
"IdEstadoInstruccion" NVARCHAR(3) NOT NULL,
"IdMotivo" BIGINT,
"FechaIngreso" TIMESTAMP  NOT NULL,
PRIMARY KEY ("IdInstruccionHistoria"));

ALTER TABLE "BSCL"."BSCL.Orden.BD::ORD.InstruccionHistorica" ADD CONSTRAINT "fk_IdInstruccion" FOREIGN KEY ( "IdInstruccion")  
REFERENCES "BSCL"."BSCL.Orden.BD::ORD.Instruccion" ("IdInstruccion");


/*==============================================================*/
/* ROLLBACK:                                                    */
/*==============================================================*/
--DROP TABLE "BSCL"."BSCL.Orden.BD.Secuencias::InstruccionHistorica";