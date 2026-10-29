SET SCHEMA "BSCL";

/*==============================================================*/
/* Drop Sequence:                                               */
/*==============================================================*/
DROP SEQUENCE "BSCL.APV.BD.Secuencias::SCS.Empleador";
DROP SEQUENCE "BSCL.APV.BD.Secuencias::SCS.IdContratoSAAPV"; 
DROP SEQUENCE "BSCL.APV.BD.Secuencias::SCS.IdDistribucionInversion";
DROP SEQUENCE "BSCL.APV.BD.Secuencia::APV.IdCargaMasiva";
DROP SEQUENCE "BSCL.APV.BD.Secuencia::REC.IdRecaudacion";
DROP SEQUENCE "BSCL.APV.BD.Secuencia::SCS.IdRelacion";
DROP SEQUENCE "BSCL.APV.BD.Secuencia::REC.IdRecaudacionEmpleador";
DROP SEQUENCE "BSCL.APV.BD.Secuencia::REC.IdRecaudacionCliente";
DROP SEQUENCE "BSCL.APV.BD.Secuencia::SCS.IdPortafolioContratoSAAPV";
DROP SEQUENCE "BSCL.APV.BD.Secuencia::APVC.IdEstadoTransicion";
DROP SEQUENCE "BSCL.APV.BD.Secuencias::TRA.IdDetalleTraspaso";
DROP SEQUENCE "BSCL.APV.BD.Secuencias::REC.IdAnotacion";
  
/*==============================================================*/
/* Create Sequence:                                             */
/*==============================================================*/

CREATE SEQUENCE "BSCL.APV.BD.Secuencias::SCS.Empleador" 
       	START     WITH 1
       	INCREMENT BY 1
       	MINVALUE  1;

CREATE SEQUENCE "BSCL.APV.BD.Secuencias::SCS.IdContratoSAAPV" 
		START     WITH 1
       	INCREMENT BY 1
       	MINVALUE  1;

CREATE SEQUENCE "BSCL.APV.BD.Secuencias::SCS.IdDistribucionInversion" 
		START     WITH 1
       	INCREMENT BY 1
       	MINVALUE  1;
       	
CREATE SEQUENCE "BSCL.APV.BD.Secuencia::APV.IdCargaMasiva"       	
		START     WITH 1
       	INCREMENT BY 1
       	MINVALUE  1;
       	
CREATE SEQUENCE "BSCL.APV.BD.Secuencia::REC.IdRecaudacion"       	
		START     WITH 1
       	INCREMENT BY 1
       	MINVALUE  1;
       	
CREATE SEQUENCE "BSCL.APV.BD.Secuencia::SCS.IdRelacion"       	
		START     WITH 1
       	INCREMENT BY 1
       	MINVALUE  1; 
       	
CREATE SEQUENCE "BSCL.APV.BD.Secuencia::REC.IdRecaudacionEmpleador"       	
		START     WITH 1
       	INCREMENT BY 1
       	MINVALUE  1; 
       	
CREATE SEQUENCE "BSCL.APV.BD.Secuencia::REC.IdRecaudacionCliente"       	
		START     WITH 1
       	INCREMENT BY 1
       	MINVALUE  1; 
  
CREATE SEQUENCE "BSCL.APV.BD.Secuencia::SCS.IdPortafolioContratoSAAPV"       	
		START     WITH 1
       	INCREMENT BY 1
       	MINVALUE  1;  
       	
CREATE SEQUENCE "BSCL.APV.BD.Secuencia::APVC.IdEstadoTransicion"       	
		START     WITH 1
       	INCREMENT BY 1
       	MINVALUE  1;        	      	

CREATE SEQUENCE "BSCL.APV.BD.Secuencias::TRA.IdDetalleTraspaso"       	
		START     WITH 1
       	INCREMENT BY 1
       	MINVALUE  1;  
 	
CREATE SEQUENCE "BSCL.APV.BD.Secuencias::REC.IdAnotacion"       	
		START     WITH 1
       	INCREMENT BY 1
       	MINVALUE  1;