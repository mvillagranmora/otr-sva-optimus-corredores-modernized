SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::ENC.ConsultarDefinicionEncuestaDetalle"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::ENC.ConsultarDefinicionEncuestaDetalle" 
AS 
SELECT E."IdEmpresa",E."IdDefinicionEncuesta",E."Nombre",E."IdEstado",O."Nombre" AS "Estado", U."CodigoUsuario" AS "CodigoUsuarioCreacion",
P."IdDefinicionPregunta",P."TextoPregunta",R."IdDefinicionRespuesta",R."TextoRespuesta",R."Puntaje"
FROM "BSCL.Plataforma.BD::ENC.DefinicionEncuesta" E
LEFT JOIN "BSCL.Plataforma.BD::ENC.DefinicionPregunta" P on E."IdDefinicionEncuesta" = P."IdDefinicionEncuesta" AND P."Borrado" = 'N'
LEFT JOIN "BSCL.Plataforma.BD::ENC.DefinicionRespuesta" R on P."IdDefinicionPregunta" = R."IdDefinicionPregunta" AND R."Borrado" = 'N'
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" O ON E."IdEstado" = O."IdEstado"
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" U ON U."IdUsuario" = E."IdUsuarioCreacion"
ORDER BY "IdDefinicionPregunta","IdDefinicionRespuesta";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::ENC.ConsultarDefinicionEncuestaDetalle" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 