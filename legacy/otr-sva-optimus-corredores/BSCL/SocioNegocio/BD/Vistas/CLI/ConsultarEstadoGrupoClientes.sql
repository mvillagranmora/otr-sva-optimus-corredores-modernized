SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarEstadoGrupoClientes";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarEstadoGrupoClientes" AS 
SELECT   "ESTORD"."IdEstado",  "ESTADO"."Nombre" as "NombreEstado"  
FROM "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" "ESTORD" 
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "ESTADO" ON "ESTORD"."IdEstado" = "ESTADO"."IdEstado" 
WHERE "ESTORD"."NombreEntidad" = 'GRUPOCLIENTES';

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarEstadoGrupoClientes" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 