SET SCHEMA "BSCL";

/*==============================================================*/
/* Drop Sequence:                                               */
/*==============================================================*/
DROP SEQUENCE "BSCL.Tesoreria.BD.Secuencias::IngresoEgreso"           	 	CASCADE;
DROP SEQUENCE "BSCL.Tesoreria.BD.Secuencias::IngresoEgresoFormaDePago" 		CASCADE;
DROP SEQUENCE "BSCL.Tesoreria.BD.Secuencias::Comprobante" 					CASCADE;
DROP SEQUENCE "BSCL.Tesoreria.BD.Secuencias::ComprobanteDetalle"       		CASCADE;
DROP SEQUENCE "BSCL.Tesoreria.BD.Secuencias::ComprobanteDetalleIngresoEgreso" CASCADE;
DROP SEQUENCE "BSCL.Tesoreria.BD.Secuencias::ComprobanteFormaDePago"   		CASCADE;
DROP SEQUENCE "BSCL.Tesoreria.BD.Secuencias::Caja"					   		CASCADE;
DROP SEQUENCE "BSCL.Tesoreria.BD.Secuencias::CajaMoneda"			   		CASCADE;
DROP SEQUENCE "BSCL.Tesoreria.BD.Secuencias::CajaUsuario"			   		CASCADE;
DROP SEQUENCE "BSCL.Tesoreria.BD.Secuencias::CuentaBanco"			   		CASCADE;
DROP SEQUENCE "BSCL.Tesoreria.BD.Secuencias::PreferenciaGiro"				CASCADE;
DROP SEQUENCE "BSCL.Tesoreria.BD.Secuencias::NominaTransferencia"			CASCADE;
DROP SEQUENCE "BSCL.Tesoreria.BD.Secuencias::NominaTransferenciaDetalle"	CASCADE;
DROP SEQUENCE "BSCL.Tesoreria.BD.Secuencias::Deposito"						CASCADE;
DROP SEQUENCE "BSCL.Tesoreria.BD.Secuencias::DepositoDetalle"				CASCADE;
DROP SEQUENCE "BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago"		CASCADE;
DROP SEQUENCE "BSCL.Tesoreria.BD.Secuencias::SecuenciaRelacionada"			CASCADE;
DROP SEQUENCE "BSCL.Tesoreria.BD.Secuencias::CAR.CargaMasiva"               CASCADE;
DROP SEQUENCE "BSCL.Tesoreria.BD.Secuencias::CAR.CargaMasivaDetalle"        CASCADE;
DROP SEQUENCE "BSCL.Tesoreria.BD.Secuencias::CIE.RevisionCierreDiario"      CASCADE;
DROP SEQUENCE "BSCL.Tesoreria.BD.Secuencias::TES.EnvioDocumento"        	CASCADE;
DROP SEQUENCE "BSCL.Tesoreria.BD.Secuencias::TES.EnvioDocumentoDetalle"     CASCADE;




/*==============================================================*/
/* Create Sequence:                                             */
/*==============================================================*/

CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::IngresoEgreso"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1; 

CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::IngresoEgresoFormaDePago"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1; 
       
CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::Comprobante"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
       
CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::ComprobanteDetalle"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;       
       
CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::ComprobanteDetalleIngresoEgreso"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;          

CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::ComprobanteFormaDePago"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;

CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::Caja"
	   START     WITH 1
       INCREMENT BY 1
       MINVALUE  1; 
       
CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::CajaMoneda"
	   START     WITH 1
       INCREMENT BY 1
       MINVALUE  1; 

CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::CajaUsuario"
	   START     WITH 1
       INCREMENT BY 1
       MINVALUE  1; 

                
CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::CuentaBanco"
	   START     WITH 1
       INCREMENT BY 1
       MINVALUE  1; 

CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::Chequera"
	   START     WITH 1
       INCREMENT BY 1
       MINVALUE  1; 
       

CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::ConfiguracionChequera"
	   START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;

CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::PreferenciaGiro"
	   START     WITH 1
       INCREMENT BY 1
       MINVALUE  1; 
       
CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::NominaTransferencia"
	   START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
       
CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::NominaTransferenciaDetalle"
	   START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
       
CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::Deposito"
	   START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
       
CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::DepositoDetalle"
	   START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
              
CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::TipoMovimientoMedioDePago"
	   START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
       
CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::SecuenciaRelacionada"
	   START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;

CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::CAR.CargaMasiva"
	   START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;

CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::CAR.CargaMasivaDetalle"
	   START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
       
CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::CIE.RevisionCierreDiario"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;        

CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::TES.EnvioDocumento"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1; 
       
CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::TES.EnvioDocumentoDetalle"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1; 
       
       