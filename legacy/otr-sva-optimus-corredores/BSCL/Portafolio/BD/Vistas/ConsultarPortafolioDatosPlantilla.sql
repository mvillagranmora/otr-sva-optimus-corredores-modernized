SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioDatosPlantilla"; 
 
CREATE VIEW "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioDatosPlantilla" AS 
 
SELECT   
	PORT."IdPortafolio", 
	PORT."CodigoPortafolio", 	
	PORT."DescripcionPortafolio",
	PORT."Proposito", 
	PORT."IdCliente", 
	PORT."IdEstado", 
	"EST"."Nombre" as "Estado", 
	PORT."IdPerfilComercialBaseOrigen",
	PORT."IdPerfilProductos",
	PORT."IdPerfilProductosExtra",
	PORT."IdPerfilCobros",
	PORT."IdPerfilCobrosExtra",
	PORT."IdPerfilLimites",
	PORT."IdPerfilLimitesExtra",
	PORT."IdPerfilCondicionesOperativas",
	PORT."IdPerfilCondicionesOperativasExtra",
	PORT."IdEstadoAutorizacion",
	PL."IdPlantillaLimite",
	PL."DescripcionPlantillaLimite",
	PL."IdEstado" AS "EstadoPlantilla",
	PL."PorDefecto",
	PL."IdEmpresa",
	PL."EsPerfilPersonalizado"
FROM 
	"BSCL.Portafolio.BD::POR.Portafolio" AS PORT
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "EST" ON "EST"."IdEstado" = (CASE WHEN "PORT"."IdEstado" IN ('ACT','VIG') THEN "PORT"."IdEstado" ELSE 'BLO' END)
LEFT JOIN "BSCL.Plataforma.BD.Vistas::LIM.ConsultarPlantillaLimites" AS PL ON (PORT."IdPerfilLimites" = PL."IdPlantillaLimite");

GRANT SELECT ON "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioDatosPlantilla" TO BCS_DESARROLLADOR WITH GRANT OPTION;
