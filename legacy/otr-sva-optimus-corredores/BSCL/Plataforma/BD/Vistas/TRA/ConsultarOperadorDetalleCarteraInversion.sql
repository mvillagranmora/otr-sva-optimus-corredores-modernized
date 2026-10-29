SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::TRA.ConsultarOperadorDetalleCarteraInversion"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::TRA.ConsultarOperadorDetalleCarteraInversion" as
	select 
		oci."CodigoOperador",
		oci."NombreCarteraInversion",
		oci."IdEmpresa",
		dci."IdInstrumento"
	from  "BSCL.Plataforma.BD::TRA.OperadorCarteraInversion" oci
	left join "BSCL.Plataforma.BD::INS.DetalleCarteraInversion" dci
		on(oci."IdEmpresa"=dci."IdEmpresa" and oci."NombreCarteraInversion"=dci."NombreCarteraInversion");
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::TRA.ConsultarOperadorDetalleCarteraInversion" 
TO BCS_DESARROLLADOR WITH GRANT OPTION; 
