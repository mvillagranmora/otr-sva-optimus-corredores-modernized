SET SCHEMA "BSCL";
	
/*==============================================================*/
/* Drop Sequence:                                               */
/*==============================================================*/
DROP SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::PreSolicitudAporteRescate"  						CASCADE;
DROP SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::SolicitudAporteRescate"      						CASCADE;
DROP SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::ValorCuotaHistorico"         						CASCADE;
DROP SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::LogAperturaCierreBloqueo"    						CASCADE;
DROP SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::Comision"    				   						CASCADE;
DROP SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::TramoComision"    		   						CASCADE;
DROP SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::LogCertificacionAporteRescate"    				CASCADE;
DROP SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::FolioCartaLiquidacionAporteRescate"   			CASCADE;
DROP SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::SecuenciaComprobanteLiquidacion"   				CASCADE;
DROP SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::ValorTipoCambioHistorico"							CASCADE;
DROP SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::DetalleCargaSolicitudesAporteRescate" 			CASCADE;
DROP SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::CargaSolicitudesAporteRescate" 					CASCADE;
DROP SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::InterfazAgf" 										CASCADE;
DROP SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::ConfiguracionPortafolioAdministracionLiquidez"	CASCADE;
DROP SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::LogAdministracionLiquidez"						CASCADE;
DROP SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::CarteraOriginalDeInversion"						CASCADE;
DROP SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::CuadraturaCustodia"								CASCADE;
DROP SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::IdReinversionSerieFondo"							CASCADE;
DROP SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::IdAgrupador"										CASCADE;

/*==============================================================*/
/* Create Sequence:                                             */
/*==============================================================*/

CREATE SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::PreSolicitudAporteRescate" 
	START     WITH 1 
	INCREMENT BY 1
	MINVALUE  1;

CREATE SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::SolicitudAporteRescate" 
	START     WITH 1 
	INCREMENT BY 1
	MINVALUE  1;

CREATE SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::ValorCuotaHistorico"
	START     WITH 1 
	INCREMENT BY 1
	MINVALUE  1;

CREATE SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::LogAperturaCierreBloqueo"
	START     WITH 1 
	INCREMENT BY 1
	MINVALUE  1;

CREATE SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::Comision"
	START     WITH 1 
	INCREMENT BY 1
	MINVALUE  1;

CREATE SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::TramoComision"
	START     WITH 1 
	INCREMENT BY 1
	MINVALUE  1;

CREATE SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::LogCertificacionAporteRescate" 
	START	  WITH 1
	INCREMENT BY 1
	MINVALUE  1;

CREATE SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::FolioCartaLiquidacionAporteRescate"
	START 	  WITH 1
	INCREMENT BY 1
	MINVALUE  1;
	
CREATE SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::SecuenciaComprobanteLiquidacion"
	START 	  WITH 1
	INCREMENT BY 1
	MINVALUE  1;

CREATE SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::ValorTipoCambioHistorico"
	START 	 WITH 1
	INCREMENT BY 1
	MINVALUE 1;

CREATE SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::DetalleCargaSolicitudesAporteRescate"
	START WITH 1
	INCREMENT BY 1
	MINVALUE 1;

CREATE SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::CargaSolicitudesAporteRescate"
	START WITH 1
	INCREMENT BY 1
	MINVALUE 1;
	
CREATE SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::InterfazAgf"
	START WITH 1
	INCREMENT BY 1
	MINVALUE 1;

CREATE SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::ConfiguracionPortafolioAdministracionLiquidez"
	START WITH 1
	INCREMENT BY 1
	MINVALUE 1;

CREATE SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::LogAdministracionLiquidez"
	START WITH 1
	INCREMENT BY 1
	MINVALUE 1;

CREATE SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::CarteraOriginalDeInversion"
	START WITH 1
	INCREMENT BY 1
	MINVALUE 1;

CREATE SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::CuadraturaCustodia"
	START WITH 1
	INCREMENT BY 1
	MINVALUE 1;
CREATE SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::IdReinversionSerieFondo"
	START WITH 1
	INCREMENT BY 1
	MINVALUE 1;
	
CREATE SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::IdAgrupador"
	START WITH 1
	INCREMENT BY 1
	MINVALUE 1;
/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
COMMIT;
