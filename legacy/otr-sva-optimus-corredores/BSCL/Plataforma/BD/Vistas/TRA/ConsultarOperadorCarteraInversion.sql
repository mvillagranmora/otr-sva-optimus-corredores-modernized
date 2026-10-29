SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::TRA.ConsultarOperadorCarteraInversion"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::TRA.ConsultarOperadorCarteraInversion" as
	select 
		oci."CodigoOperador",
		oci."NombreCarteraInversion",
		oci."IdEmpresa",
		oci."IdEstado"
	from  "BSCL.Plataforma.BD::TRA.OperadorCarteraInversion" oci;
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::TRA.ConsultarOperadorCarteraInversion" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
