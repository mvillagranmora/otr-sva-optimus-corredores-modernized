SET SCHEMA "BSCL";

/*=================================================================================*/
/* Drop Sequence:                                                                  */
/*=================================================================================*/
DROP SEQUENCE "BSCL.Normativa.BD.Secuencias::ILS.IdTipoInforme";
DROP SEQUENCE "BSCL.Normativa.BD.Secuencias::ILS.IdItem";
DROP SEQUENCE "BSCL.Normativa.BD.Secuencias::ILS.IdConcepto";
DROP SEQUENCE "BSCL.Normativa.BD.Secuencias::ILS.InformeTipo";

/*=================================================================================*/
/* Create Sequence:                                                                */
/*=================================================================================*/

         
/*  "BSCL.Normativa.BD.Secuencias::ILS.IdTipoInforme";	*/
CREATE SEQUENCE "BSCL.Normativa.BD.Secuencias::ILS.IdTipoInforme" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
       
/*  "BSCL.Normativa.BD.Secuencias::ILS.IdItem";	 */
CREATE SEQUENCE "BSCL.Normativa.BD.Secuencias::ILS.IdItem" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;

       /*  "BSCL.Normativa.BD.Secuencias::ILS.IdConcepto";	*/
CREATE SEQUENCE "BSCL.Normativa.BD.Secuencias::ILS.IdConcepto" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;   
       
             /*  "BSCL.Normativa.BD.Secuencias::ILS.InformeTipo";	*/
CREATE SEQUENCE "BSCL.Normativa.BD.Secuencias::ILS.InformeTipo" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
      
CREATE SEQUENCE "BSCL.Normativa.BD.Secuencias::ILS.InformeResultadoIndice" 
	   START	 WITH 1 
	   INCREMENT BY 1
	   MINVALUE  1;

CREATE SEQUENCE "BSCL.Normativa.BD.Secuencias::ILS.InformeResultadoConcepto"
	   START	 WITH 1 
	   INCREMENT BY 1
	   MINVALUE  1;

CREATE SEQUENCE "BSCL.Normativa.BD.Secuencias::ILS.InformeResultadoItem"
	   START	 WITH 1 
	   INCREMENT BY 1
	   MINVALUE  1;

CREATE SEQUENCE "BSCL.Normativa.BD.Secuencias::ILS.InformeResultadoItemDetalle"
	   START	 WITH 1 
	   INCREMENT BY 1
	   MINVALUE  1;
