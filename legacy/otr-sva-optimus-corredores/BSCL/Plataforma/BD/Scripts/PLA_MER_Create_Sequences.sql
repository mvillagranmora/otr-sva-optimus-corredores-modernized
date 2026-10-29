SET SCHEMA "BSCL";

/*==============================================================*/
/* Drop Sequence:                                               */
/*==============================================================*/
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::MER.IdTrabajoParidad"	CASCADE;

/*==============================================================*/
/* Create Sequence:                                             */
/*==============================================================*/

CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::MER.IdTrabajoParidad" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
	   
