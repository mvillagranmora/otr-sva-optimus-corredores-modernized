SET SCHEMA "BSCL";

/*==============================================================*/
/* Drop Sequence:                                               */
/*==============================================================*/
DROP SEQUENCE "BSCL.Orden.BD.Secuencias::Asignacion"      CASCADE;
DROP SEQUENCE "BSCL.Orden.BD.Secuencias::CobroAsignacion"      CASCADE;
DROP SEQUENCE "BSCL.Orden.BD.Secuencias::CobrosOrden"      CASCADE;
DROP SEQUENCE "BSCL.Orden.BD.Secuencias::CobrosRangoOrden" CASCADE;
DROP SEQUENCE "BSCL.Orden.BD.Secuencias::GrupoCobroOrden"      CASCADE;
DROP SEQUENCE "BSCL.Orden.BD.Secuencias::LibroOrdenes"      CASCADE;
DROP SEQUENCE "BSCL.Orden.BD.Secuencias::LibroOrdenesDefinicion"      CASCADE;
DROP SEQUENCE "BSCL.Orden.BD.Secuencias::PorcentajeVisibilidad"      CASCADE;
DROP SEQUENCE "BSCL.Orden.BD.Secuencias::TipoCustodiaOrden"      CASCADE;
DROP SEQUENCE "BSCL.Orden.BD.Secuencias::TipoFechaVigenciaOrden"      CASCADE;
DROP SEQUENCE "BSCL.Orden.BD.Secuencias::TipoFormaCustodiaOrden"      CASCADE;
DROP SEQUENCE "BSCL.Orden.BD.Secuencias::TipoPrecioOrden"      CASCADE;
DROP SEQUENCE "BSCL.Orden.BD.Secuencias::TipoRuteoOrden"      CASCADE;
DROP SEQUENCE "BSCL.Orden.BD.Secuencias::TipoVigenciaOrden"      CASCADE;
DROP SEQUENCE "BSCL.Orden.BD.Secuencias::Transaccion"      CASCADE;
--Simulacion Asignacion
DROP SEQUENCE "BSCL.Orden.BD.Secuencias::SimulacionAsignacion" CASCADE;
DROP SEQUENCE "BSCL.Orden.BD.Secuencias::ParametrosSimulacionAsignacion" CASCADE;
DROP SEQUENCE "BSCL.Orden.BD.Secuencias::DetalleSimulacionAsignacion" CASCADE;
--DROP SEQUENCE "BSCL.Orden.BD.Secuencias::CobroSimulacionAsignacion" CASCADE;

DROP SEQUENCE "BSCL.Orden.BD.Secuencias::Grupo" CASCADE;
DROP SEQUENCE "BSCL.Orden.BD.Secuencias::GrupoPortafolio" CASCADE;

DROP SEQUENCE "BSCL"."BSCL.Orden.BD.Secuencias::LibroOrdenesOperador";
DROP SEQUENCE "BSCL"."BSCL.Orden.BD.Secuencias::LibroOrdenesSegmento";
DROP SEQUENCE "BSCL"."BSCL.Orden.BD.Secuencias::LibroOrdenesMarcaNegociacion";
DROP SEQUENCE "BSCL"."BSCL.Orden.BD.Secuencias::LibroPorDefecto";	

DROP SEQUENCE "BSCL"."BSCL.Orden.BD.Secuencias::FolioOrdenes";

/*==============================================================*/
/* Create Sequence:                                             */
/*==============================================================*/

CREATE SEQUENCE "BSCL.Orden.BD.Secuencias::Asignacion" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
	   
CREATE SEQUENCE "BSCL.Orden.BD.Secuencias::CobroAsignacion" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
	   
CREATE SEQUENCE "BSCL.Orden.BD.Secuencias::CobrosOrden" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
       
CREATE SEQUENCE "BSCL.Orden.BD.Secuencias::CobrosRangoOrden" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;

CREATE SEQUENCE "BSCL.Orden.BD.Secuencias::GrupoCobroOrden" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
	   
CREATE SEQUENCE "BSCL.Orden.BD.Secuencias::LibroOrdenes" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
	   
CREATE SEQUENCE "BSCL.Orden.BD.Secuencias::LibroOrdenesDefinicion" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;

CREATE SEQUENCE "BSCL.Orden.BD.Secuencias::Orden" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1
       reset by select ifnull(max("IdOrden"), 0) +1 from "BSCL.Orden.BD::ORD.Orden";

CREATE SEQUENCE "BSCL.Orden.BD.Secuencias::CorredorCorresponsal" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1
       reset by select ifnull(max("IdCorredorCorresponsal"), 0) +1 from "BSCL.Orden.BD::ORD.CorredorCorresponsal";


CREATE SEQUENCE "BSCL.Orden.BD.Secuencias::PorcentajeVisibilidad" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
	   
CREATE SEQUENCE "BSCL.Orden.BD.Secuencias::TipoCustodiaOrden" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
	   
CREATE SEQUENCE "BSCL.Orden.BD.Secuencias::TipoFechaVigenciaOrden" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
	   
CREATE SEQUENCE "BSCL.Orden.BD.Secuencias::TipoFormaCustodiaOrden" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
	   
CREATE SEQUENCE "BSCL.Orden.BD.Secuencias::TipoPrecioOrden" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
	   
CREATE SEQUENCE "BSCL.Orden.BD.Secuencias::TipoRuteoOrden" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
	   
CREATE SEQUENCE "BSCL.Orden.BD.Secuencias::TipoVigenciaOrden" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
	   
CREATE SEQUENCE "BSCL.Orden.BD.Secuencias::Transaccion" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
--Simulacion Asignacion
CREATE SEQUENCE "BSCL.Orden.BD.Secuencias::SimulacionAsignacion"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
CREATE SEQUENCE "BSCL.Orden.BD.Secuencias::ParametrosSimulacionAsignacion"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;

CREATE SEQUENCE "BSCL.Orden.BD.Secuencias::DetalleSimulacionAsignacion"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
/*
CREATE SEQUENCE "BSCL.Orden.BD.Secuencias::CobroSimulacionAsignacion"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
*/       

--Secuencia tabla Grupo
CREATE SEQUENCE "BSCL.Orden.BD.Secuencias::Grupo"
   START     WITH 1
   INCREMENT BY 1
   MINVALUE  1; 
   
CREATE SEQUENCE "BSCL.Orden.BD.Secuencias::GrupoPortafolio"
   START     WITH 1
   INCREMENT BY 1
   MINVALUE  1;

--secuencias para tabla Operadores, Segmento/Canal y MarcasNegociacion de LibroOrdenes
CREATE SEQUENCE "BSCL"."BSCL.Orden.BD.Secuencias::LibroOrdenesOperador"
	START WITH 1 
	INCREMENT BY 1 
	MINVALUE 1;

CREATE SEQUENCE "BSCL"."BSCL.Orden.BD.Secuencias::LibroOrdenesSegmento" 
	START WITH 1 
	INCREMENT BY 1 
	MINVALUE 1;
	
CREATE SEQUENCE "BSCL"."BSCL.Orden.BD.Secuencias::LibroOrdenesMarcaNegociacion" 
	START WITH 1 
	INCREMENT BY 1 
	MINVALUE 1;

--secuencia para tabla LibroPorDefecto
CREATE SEQUENCE "BSCL"."BSCL.Orden.BD.Secuencias::LibroPorDefecto" 
	START WITH 1 
	INCREMENT BY 1 
	MINVALUE 1;

--secuencia para la foliacion de ordenes
CREATE SEQUENCE "BSCL"."BSCL.Orden.BD.Secuencias::FolioOrdenes" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;


	
	
