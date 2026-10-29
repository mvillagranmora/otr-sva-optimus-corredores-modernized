SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarMailPorRol"; 
 
 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarMailPorRol"  
AS
SELECT DISTINCT
"IdDireccionElectronica",
"Direccion",
CR."IdRol",
USU."IdUsuario",   
CPDE."IdPersona",
USU."IdEntidad",
USU."TipoEntidad",
CP."DocumentoIdentidad",
cr."IdEmpresa"
FROM "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRol"  CR
LEFT JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesPerfilesUsuario" CRPU
ON CR."IdRol" = CRPU."IdRol"
LEFT JOIN  "BSCL.Plataforma.BD::ACC.Usuario" USU
ON USU."IdUsuario" = CRPU."IdUsuario"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" CSN
ON (CSN."IdCliente" = USU."IdEntidad" and "TipoEntidad" = 'CLI') OR
   (CSN."IdEmpleado" = USU."IdEntidad" and "TipoEntidad" = 'EMP') OR
   (CSN."IdProveedor" = USU."IdEntidad" and "TipoEntidad" = 'PRO') OR
   (CSN."IdContraparte" = USU."IdEntidad" and "TipoEntidad" = 'CNT') 
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" CPDE
ON CSN."IdPersona" = CPDE."IdPersona"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" CP
ON CP."IdPersona" = CPDE."IdPersona"
WHERE "Direccion" is not null and LENGTH(trim("Direccion"))>0;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarMailPorRol" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 



 
