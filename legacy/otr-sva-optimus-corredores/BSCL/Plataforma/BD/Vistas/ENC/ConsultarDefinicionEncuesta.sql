SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::ENC.ConsultarDefinicionEncuesta"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::ENC.ConsultarDefinicionEncuesta" 
AS 
SELECT E."IdEmpresa",E."IdDefinicionEncuesta",E."Nombre",E."IdEstado",O."Nombre" AS "Estado", U."CodigoUsuario" AS "CodigoUsuarioCreacion"
FROM "BSCL.Plataforma.BD::ENC.DefinicionEncuesta" E
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" O ON E."IdEstado" = O."IdEstado"
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" U ON U."IdUsuario" = E."IdUsuarioCreacion"
;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::ENC.ConsultarDefinicionEncuesta" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 