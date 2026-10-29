SET SCHEMA "BSCL";

/*==============================================================*/
/* Drop Sequence:                                               */
/*==============================================================*/
DROP SEQUENCE "BSCL.Orden.BD.Secuencias::AsignacionPaso"      CASCADE;
DROP SEQUENCE "BSCL.Orden.BD.Secuencias::OrdenPaso"      CASCADE;
DROP SEQUENCE "BSCL.Orden.BD.Secuencias::TransaccionPaso"      CASCADE;
DROP SEQUENCE "BSCL.Orden.BD.Secuencias::AsignacionResultado"      CASCADE;

/*==============================================================*/
/* Create Sequence:                                             */
/*==============================================================*/

CREATE SEQUENCE "BSCL.Orden.BD.Secuencias::AsignacionPaso" 
       START     WITH 2000
       INCREMENT BY 1
       MINVALUE  1;
	   
CREATE SEQUENCE "BSCL.Orden.BD.Secuencias::OrdenPaso" 
       START     WITH 2000
       INCREMENT BY 1
       MINVALUE  1;
	   
CREATE SEQUENCE "BSCL.Orden.BD.Secuencias::TransaccionPaso" 
       START     WITH 2000
       INCREMENT BY 1
       MINVALUE  1;

CREATE SEQUENCE "BSCL.Orden.BD.Secuencias::AsignacionResultado" 
       START     WITH 2000
       INCREMENT BY 1
       MINVALUE  1;
	   

	   


