SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Normativa.BD.Vistas::NCG380.ConsultarInstruccionesJuntaAccionista";

CREATE VIEW "BSCL.Normativa.BD.Vistas::NCG380.ConsultarInstruccionesJuntaAccionista"
AS
SELECT
	 "Cliente"."IdClienteConDerechoJuntaAccionista"
	,"Cliente"."DocumentoIdentidad"
	,"Cliente"."CodigoISIN"
	,"Cliente"."Nemotecnico"
	,"Cliente"."IdEmisor"
	,"Evento"."TipoJunta" 
	,"Evento"."Lugar"
	,"Evento"."FechaRealizacion"
	,"Evento"."Materias"
	,"Cliente"."Instruccion"
	,"Cliente"."CantidadDerecho"
FROM  "BSCL.Custodia.BD.Vistas::CUS.ConsultarClientesConDerechoJuntaAccionista" AS "Cliente"
INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarJuntaAccionistas" AS "Evento" ON ("Cliente"."IdEventoJuntaAccionista" = "Evento"."IdEventoJuntaAccionista");


GRANT SELECT ON "BSCL.Normativa.BD.Vistas::NCG380.ConsultarInstruccionesJuntaAccionista" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
