SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarDetalleCarteraEjecutivo"; 
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarDetalleCarteraEjecutivo" as
	select 
		ci."NombreCarteraEjecutivo",
		ci."IdEmpresa",
		ci."Descripcion",
		ci."IdEstado",
		dci."IdEmpleadoEjecutivo",
		i."NombreCompleto"
    from  "BSCL.SocioNegocio.BD::EJE.CarteraEjecutivo" ci
	inner join "BSCL.SocioNegocio.BD::EJE.DetalleCarteraEjecutivo" dci
		on ci."NombreCarteraEjecutivo" = dci."NombreCarteraEjecutivo"
		and ci."IdEmpresa" = dci."IdEmpresa"
	left join "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" i--"BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolio" i
		on i."IdEmpleadoEjecutivo" = dci."IdEmpleadoEjecutivo";
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarDetalleCarteraEjecutivo" 
TO BCS_DESARROLLADOR WITH GRANT OPTION; 

