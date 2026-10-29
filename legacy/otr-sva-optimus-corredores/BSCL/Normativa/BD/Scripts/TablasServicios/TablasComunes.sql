SET SCHEMA "BSCL"; 

/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/
DROP TABLE  "BSCL.Normativa.BD::GBL.Parametros";
		   


/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Normativa.BD::GBL.Parametros"(
	"IdInterno"				BIGINT,
	"Tipo" 					NVARCHAR(40), ---BATCH/SOLICITUD/ESPECIFICO
	"NombreParametro" 		NVARCHAR(100), 
	"Valor" 				NVARCHAR(100)
) ON COMMIT DELETE ROWS;


	     									     
 /*==============================================================*/
/* Sequences:       DROP & CREATE                               */
/*==============================================================*/

DROP sequence "BSCL.Normativa.BD.Secuencias::GBL.IdTrabajoParametros" cascade;


CREATE SEQUENCE "BSCL.Normativa.BD.Secuencias::GBL.IdTrabajoParametros" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;




commit;


 
 

 

 

 





 






 

 
 

