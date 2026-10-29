SET SCHEMA "BSCL"; 

DROP TABLE "BSCL.Orden.BD::ORD.AnularAsignacionMasivaOptimizada_Entrada_Asignaciones";

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.AnularAsignacionMasivaOptimizada_Entrada_Asignaciones"(
	"IdAsignacion"	 						BIGINT
) ON COMMIT DELETE ROWS;
 
 
