SET SCHEMA "BSCL";
	
/*==============================================================*/
/* Drop Sequence:                                               */
/*==============================================================*/
DROP SEQUENCE "BSCL.CuentaInversion.BD.Secuencias::CAR.CargaMasiva"                 CASCADE;
DROP SEQUENCE "BSCL.CuentaInversion.BD.Secuencias::CAR.CargaMasivaDetalle" 		    CASCADE;


/*==============================================================*/
/* Create Sequence:                                             */
/*==============================================================*/
	
--CELZORRO-264 - MBI Manejo de Activos Internacionales - Carga masiva Abonos y Cargos
CREATE SEQUENCE "BSCL.CuentaInversion.BD.Secuencias::CAR.CargaMasiva"
	   START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;

CREATE SEQUENCE "BSCL.CuentaInversion.BD.Secuencias::CAR.CargaMasivaDetalle"
	   START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
--CELZORRO-264 - MBI Manejo de Activos Internacionales - Carga masiva Abonos y Cargos

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
COMMIT;
