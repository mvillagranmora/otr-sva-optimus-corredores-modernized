SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::TRA.ConsultarOperadorCarteraEjecutivo"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::TRA.ConsultarOperadorCarteraEjecutivo" as
	select 
		oci."CodigoOperador",
		oci."NombreCarteraEjecutivo",
		oci."IdEmpresa",
		oci."IdEstado"
	from  "BSCL.Plataforma.BD::TRA.OperadorCarteraEjecutivo" oci;
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::TRA.ConsultarOperadorCarteraEjecutivo" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
