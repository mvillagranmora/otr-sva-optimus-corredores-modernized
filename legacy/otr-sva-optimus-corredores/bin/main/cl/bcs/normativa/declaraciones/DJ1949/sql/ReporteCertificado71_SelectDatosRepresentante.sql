SELECT 
 EMPRESA."DocumentoIdentidadRepresentante" AS "Rep_Rut"
,PERSONA."NombreCompleto" AS "Rep_Nombre" 
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMPRESA
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PERSONA ON (PERSONA."DocumentoIdentidad" = EMPRESA."DocumentoIdentidadRepresentante" AND EMPRESA."IdEmpresa"=:IdEmpresa);