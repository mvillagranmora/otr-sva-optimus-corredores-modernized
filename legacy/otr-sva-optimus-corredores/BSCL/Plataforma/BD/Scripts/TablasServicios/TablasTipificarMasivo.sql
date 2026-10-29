SET SCHEMA "BSCL"; 


drop table "BSCL.Plataforma.BD::PLA.TipificarMasivo_Proceso_TipVALJOBS";--TIP_VALJOBS


CREATE GLOBAL TEMPORARY TABLE "BSCL.Plataforma.BD::PLA.TipificarMasivo_Proceso_TipVALJOBS"(
	GRUPO  		bigint,
	PROGRAMA  	bigint,
	TAREA 		bigint,
	ORDEN 		bigint,
	OPCION 		nvarchar(4),
	TIPOUNIDAD 	int			
)ON COMMIT DELETE ROWS;

commit;
 

 

 

 





 






 

 
 

