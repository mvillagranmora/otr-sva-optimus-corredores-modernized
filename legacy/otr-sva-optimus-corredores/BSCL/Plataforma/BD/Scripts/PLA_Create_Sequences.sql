SET SCHEMA "BSCL";

/*==============================================================*/
/* Drop Sequence:                                               */
/*==============================================================*/
DROP SEQUENCE "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio"                   CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::AUTSecuenciasGenerales"      CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::CoberturaGarantia"      CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::CuentaBancaria"      CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::DiaFeriado"      CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::Empresa"      CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::Estado"      CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::INSInstrumento"      CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::INSMercado"      CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::LIMSecuenciasGenerales"      CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::Log"      CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::Pais"      CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::PLAConcepto"      CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::PLAModulo"      CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::REGDireccion"      CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::ReglaEjecucion"      CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::Sucursal"      CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::TipoCustodiaOrden"      CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::TipoFormaCustodiaOrden"      CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::TipoIndicadorFacturacionOrden"      CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::TipoPrecioOrden"      CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::TipoRuteoOrden"      CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::TipoVigenciaOrden"      CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::CoberturaGarantia"      CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::ExigibilidadGarantia"      CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio"  CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::PLA.IdSecuenciaCambio"  CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::BAN.IdCuentaBancaria"  CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::PLA.ConfiguracionFiltroInterfaz"  CASCADE;       
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::DetalleLog"	CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::TipoAtributo"	CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::Atributo"	CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::DetalleAtributo"	CASCADE;  
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::LogMasivo"	CASCADE; 
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::BolsaInstrumentoPrecios" CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::RelacionInstrumento" CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::ArbolClasificacionActivos" CASCADE;       

/*==============================================================*/
/* Create Sequence:                                             */
/*==============================================================*/

CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::AUTSecuenciasGenerales" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
	   
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::CoberturaGarantia" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
	   
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::CuentaBancaria" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
	   
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::DiaFeriado" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
	   
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::Empresa" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
	   
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::Estado" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
	   
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::INSInstrumento" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
	   
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::INSMercado" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
       
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::INS.InstrumentoSubyacente";
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::INS.InstrumentoSubyacente" 
INCREMENT BY 1
START WITH 1645
RESET BY SELECT IFNULL(MAX("IdInstrumentoSubyacente"), 0) + 1 FROM "BSCL.Plataforma.BD::INS.InstrumentoSubyacente";
SELECT MAX("IdInstrumentoSubyacente") from "BSCL.Plataforma.BD::INS.InstrumentoSubyacente";

CREATE SEQUENCE s1 

CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::LIMSecuenciasGenerales" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
	   
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::Log" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
	   
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::Pais" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
	   
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::PLAConcepto" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
	   
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::PLAModulo" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
	   
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::REGDireccion" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
	   
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::ReglaEjecucion" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
	   
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::Sucursal" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
	   
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::TipoCustodiaOrden" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
	   
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::TipoFormaCustodiaOrden" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
	   
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::TipoIndicadorFacturacionOrden" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
	   
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::TipoPrecioOrden" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
	   
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::TipoRuteoOrden" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;

CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::TipoVigenciaOrden" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
       
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::ExigibilidadGarantia"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;	

CREATE SEQUENCE "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;	

CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;	
       
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::PLA.IdSecuenciaCambio"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;	
       
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::BAN.IdCuentaBancaria"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;	

CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::PLA.ConfiguracionFiltroInterfaz"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;	       

CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::DetalleLog" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;

CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::TipoAtributo"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
       
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::Atributo"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
              
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::DetalleAtributo"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
       
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::LogMasivo"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;       
       
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::BolsaInstrumentoPrecios"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;       

CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::RelacionInstrumento"

       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;       
       
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::AUT.IdGrupo"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
	   
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::ArbolClasificacionActivos"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;	   
