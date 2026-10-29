SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarCorredorCorresponsal";
GO 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarCorredorCorresponsal"
AS 
SELECT 
	c1."IdCorredorCorresponsal",
	c1."IdPais",
	p1."NombrePais" as "NombrePais",
	c1."CodigoCorredor",
	c1."IdContraparte",
	c1."IdPersona",
	c1."IdSocioDeNegocio",
	c1."IdEmpresa",
	c2."DocumentoIdentidad",
	c2."NombreCompleto",
	COALESCE(pf."IdPerfilCobros",pf2."IdPerfilCobros") AS "IdPerfilCobros",
	COALESCE(pf."DescripcionPerfilCobros",pf2."DescripcionPerfilCobros") AS "DescripcionPerfilCobros",
	c1."IdEstado",
	e1."Nombre" as "NombreEstado",
	c1."DefaultPais",
	"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(c1."DefaultPais") AS "DefaultPaisUI"
FROM "BSCL.Orden.BD::ORD.CorredorCorresponsal" c1
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" c2 on c1."IdPersona" = c2."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD::COM.PerfilCobros" pf on c1."IdPerfilCobros" = pf."IdPerfilCobros"
LEFT JOIN "BSCL.Plataforma.BD::COM_2.PerfilCobros" pf2 on c1."IdPerfilCobros" = pf2."IdPerfilCobros"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" e1 on c1."IdEstado" = e1."IdEstado"
LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" p1 on p1."IdPais" = c1."IdPais";
GO
GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarCorredorCorresponsal" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
