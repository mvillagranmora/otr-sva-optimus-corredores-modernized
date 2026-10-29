SET SCHEMA "BSCL"; 


DROP TABLE "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearIngresoEgreso_Entrada_IngresoEgreso"  cascade;
DROP TABLE "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearIngresoEgreso_Entrada_FormaDePago"    cascade;
DROP TABLE "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearIngresoEgreso_Salida_Resultado"    	  cascade;


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearIngresoEgreso_Entrada_IngresoEgreso"
LIKE "BSCL.Tesoreria.BD.Tipos::TES.IngresoEgresoEditar"  ON COMMIT DELETE ROWS;

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearIngresoEgreso_Entrada_FormaDePago"
 LIKE "BSCL.Tesoreria.BD.Tipos::TES.FormaDePago"  ON COMMIT DELETE ROWS;

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearIngresoEgreso_Salida_Resultado" 
 LIKE "BSCL.Tesoreria.BD.Tipos::TES.CTA.ResultadoReglas"  ON COMMIT DELETE ROWS;
 							
commit

