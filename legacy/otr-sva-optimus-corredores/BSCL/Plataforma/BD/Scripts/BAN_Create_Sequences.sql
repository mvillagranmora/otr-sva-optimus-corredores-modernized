SET SCHEMA "BSCL";

/*==============================================================*/
/* Drop Sequence:                                               */
/*==============================================================*/
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::BAN.Plaza"	CASCADE;

/*==============================================================*/
/* Create Sequence:                                             */
/*==============================================================*/

CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::BAN.Plaza" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
	   
