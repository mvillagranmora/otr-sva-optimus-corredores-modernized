/****************************************************************************
propósito		: CONSULTA CONFIGURACION DE BLOQUEO AUTOMÁTICO DE CLIENTES
autor			: jg
fecha creación	: 30/01/2017
*****************************************************************************/
SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarBloqueoAutomaticoClientes"; 
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarBloqueoAutomaticoClientes" as
	select
		emp."IdEmpresa",
		bac."IdEstado",
		bac."BloqueoCumplirMayoriaEdad",
		bac."BloqueoFichaDesactualizada",
		bac."BloqueoPortafolioSinOperaciones",
	    bac."FechaCreacion",
	   	bac."FechaModificacion",
	   	td."IdTipoDocumento",
	   	coalesce(dbac."IdEstado",'INA') as "IdEstadoTipoDocumento",
	   	est."Nombre" as "NombreEstadoTipoDocumento", 
	   	td."NombreDocumento"
	from "BSCL.Plataforma.BD::DOC.TipoDocumento" td
	join "BSCL.Plataforma.BD::PLA.Empresa" emp
		on (emp."IdEmpresa" = td."IdEmpresa" and emp."IdEstado" = 'ACT')
	left join "BSCL.SocioNegocio.BD::CLI.BloqueoAutomaticoClientes" bac
		on (bac."IdEmpresa" = td."IdEmpresa")
	left join "BSCL.SocioNegocio.BD::CLI.DetalleBloqueoAutomaticoClientes" dbac
		on (dbac."IdEmpresa" = td."IdEmpresa" and dbac."IdTipoDocumento" = td."IdTipoDocumento")
	left join "BSCL.Plataforma.BD::PLA.Estado" est 
		on (est."IdEstado" = coalesce(dbac."IdEstado",'INA'))
	order by td."NombreDocumento";
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarBloqueoAutomaticoClientes" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
