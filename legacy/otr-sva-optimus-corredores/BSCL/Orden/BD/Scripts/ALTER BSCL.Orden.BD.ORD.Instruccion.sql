SET SCHEMA "BSCL";

/*==============================================*/
/* CREATE:  se agrega nueva referencia          */
/*==============================================*/
SET SCHEMA BSCL;

ALTER TABLE "BSCL"."BSCL.Orden.BD::ORD.Instruccion" ADD ("IdMotivo" BIGINT ); 
 
ALTER TABLE "BSCL"."BSCL.Orden.BD::ORD.Instruccion" ADD CONSTRAINT "fk_IdMotivo" FOREIGN KEY ( "IdMotivo")  
REFERENCES "BSCL"."BSCL.Orden.BD::ORD.InstruccionMotivo" ("IdMotivo");

/*==============================================================*/
/* ROLLBACK:                                                    */
/*==============================================================*/
--ALTER TABLE "BSCL"."BSCL.Orden.BD::ORD.Instruccion" DROP COLUMN "IdMotivo";