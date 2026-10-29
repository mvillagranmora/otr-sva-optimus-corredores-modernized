SET SCHEMA "BSCL";
	
/*==============================================================*/
/* Drop Sequence:                                               */
/*==============================================================*/
DROP SEQUENCE "BSCL.Homologacion.BD.Secuencias::CargaMasiva" CASCADE;

/*==============================================================*/
/* Create Sequence:                                             */
/*==============================================================*/

CREATE SEQUENCE "BSCL.Homologacion.BD.Secuencias::CargaMasiva" 
	START     WITH 1 
	INCREMENT BY 1
	MINVALUE  1;

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
COMMIT;
