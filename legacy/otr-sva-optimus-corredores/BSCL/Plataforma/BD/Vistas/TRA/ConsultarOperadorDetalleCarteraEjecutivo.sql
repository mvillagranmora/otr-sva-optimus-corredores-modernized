SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::TRA.ConsultarOperadorDetalleCarteraEjecutivo"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::TRA.ConsultarOperadorDetalleCarteraEjecutivo" as
	select 
		oci."CodigoOperador",
		oci."NombreCarteraEjecutivo",
		oci."IdEmpresa",
		dci."IdEmpleadoEjecutivo"
	from  "BSCL.Plataforma.BD::TRA.OperadorCarteraEjecutivo" oci
	left join "BSCL.SocioNegocio.BD::EJE.DetalleCarteraEjecutivo" dci
		on(oci."IdEmpresa"=dci."IdEmpresa" and oci."NombreCarteraEjecutivo" = dci."NombreCarteraEjecutivo");
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::TRA.ConsultarOperadorDetalleCarteraEjecutivo" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
