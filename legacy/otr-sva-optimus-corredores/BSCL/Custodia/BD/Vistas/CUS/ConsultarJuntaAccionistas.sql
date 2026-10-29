SET SCHEMA "BSCL";

DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarJuntaAccionistas";
 
CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarJuntaAccionistas" 
AS

SELECT 
"JuntaAccionista"."IdEventoJuntaAccionista"
, "JuntaAccionista"."IdEmisor"
, "JuntaAccionista"."FechaCierre"
, "JuntaAccionista"."FechaRealizacion"
, "Emisor"."NombreCompleto" AS "NombreEmisor"
, "TipoJunta"."Descripcion" AS "TipoJunta"
, "JuntaAccionista"."IdTipoJuntaAccionista"
, "JuntaAccionista"."Lugar"
, "JuntaAccionista"."HoraRealizacion"
, "EstadoCustodia"."Nombre" AS "NombreEstadoJunta"
, "EstadoJunta"."Nombre" AS "NombreEstadoCustodia"
, "JuntaAccionista"."IdEstadoCustodia"
, "JuntaAccionista"."IdEstadoJunta"
, "JuntaAccionista"."Instruccion"
, "JuntaAccionista"."CuadraturaDCV"
, "JuntaAccionista"."Materias"
, CASE "JuntaAccionista"."IdEstadoCustodia" WHEN 'PEN' THEN 'S' ELSE 'N' END AS "FechaCierreEditable"
, "JuntaAccionista"."IdUsuario"
, "JuntaAccionista"."IdEmpresa"
, COALESCE("Totales"."TotalCantidadDerecho",0) AS "TotalCantidadDerecho"
, COALESCE("Totales"."TotalCantidadDerechoCalculado",0) AS "TotalCantidadDerechoCalculado"
FROM "BSCL.Custodia.BD::CUS.EventoJuntaAccionista" AS "JuntaAccionista"
INNER JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS "Emisor" ON ("Emisor"."IdEmisor" = "JuntaAccionista"."IdEmisor")
INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" AS "EstadoCustodia" ON ("EstadoCustodia"."IdEstado" = "JuntaAccionista"."IdEstadoCustodia")
INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" AS "EstadoJunta" ON ("EstadoJunta"."IdEstado" = "JuntaAccionista"."IdEstadoJunta")
INNER JOIN "BSCL.Custodia.BD::CUS.TipoJuntaAccionista" AS "TipoJunta" ON ("TipoJunta"."Codigo" = "JuntaAccionista"."IdTipoJuntaAccionista" AND "TipoJunta"."Entidad" = 'TIPOJUNTA')
LEFT OUTER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTotalDerechoJuntaAccionista" AS "Totales" ON ("Totales"."IdEventoJuntaAccionista" = "JuntaAccionista"."IdEventoJuntaAccionista");

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarJuntaAccionistas" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


