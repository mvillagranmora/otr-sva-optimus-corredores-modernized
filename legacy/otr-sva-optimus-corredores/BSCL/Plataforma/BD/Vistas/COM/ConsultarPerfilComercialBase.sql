SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilComercialBase"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilComercialBase"  
AS

SELECT 
	PCB."IdPerfilComercialBase",
	PCB."DescripcionPerfilComercialBase",
	PCB."IdCategoriaInversionista",
	PCB."IdPerfilProductos",
	PCB."IdPerfilCobros",
	PCB."IdPerfilCobrosPeru",
	PCB."IdPerfilLimites",
	PCB."IdPerfilCondicionesOperativas",
	PCB."IdModeloFacturacion",
	PCB."FechaCreacion",
	PCB."FechaModificacion",
	PCB."IdEstado",
	PCB."PorDefecto",
	PCB."IdEmpresa",
    PCB."TipoEnrolamiento"
FROM 
	"BSCL.Plataforma.BD::COM.PerfilComercialBase" as PCB;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilComercialBase" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
