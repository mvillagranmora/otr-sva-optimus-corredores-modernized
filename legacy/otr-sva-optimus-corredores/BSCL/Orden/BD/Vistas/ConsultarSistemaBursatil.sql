SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarSistemaBursatil";
 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarSistemaBursatil" AS 
	select
		"IdSistemaBursatil",
		"NombreSistemaBursatil",
		"IdClasificacionTipoInstrumento",
		"IdEmpresa"
	from "BSCL.Orden.BD::ORD.SistemaBursatil";

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarSistemaBursatil" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
