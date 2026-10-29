SET SCHEMA "BSCL";

DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarClientesConDerechoJuntaAccionista";
 
CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarClientesConDerechoJuntaAccionista" 
AS
SELECT 
  CLIENTE_DERECHO."IdCliente"   						AS "IdCliente"
, CLIENTE."DocumentoIdentidad"  						AS "DocumentoIdentidad"
, CLIENTE."NombreCompleto" 								AS "NombreCliente"
, CLIENTE_DERECHO."IndicadorMandatoRepresentacion"		AS "IndicadorMandatoRepresentacion"
, CLIENTE_DERECHO."CantidadDerecho" 					AS "CantidadDerecho"
, CLIENTE_DERECHO."IdTipoInstruccion"					AS "IdTipoInstruccion"
, TIPOJUNTA."Descripcion"								AS "TipoInstruccion"
, CLIENTE_DERECHO."Instruccion" 						AS "Instruccion"
, ESTADO."Nombre" 										AS "NombreEstado"
, CLIENTE_DERECHO."IdEstado"							AS "IdEstado"
, CLIENTE_DERECHO."IdUsuario" 							AS "IdUsuario"
, USUARIO."CodigoUsuario"								AS "NombreUsuario"
, CLIENTE_DERECHO."FechaModificacionRegistro" 			AS "FechaModificacionRegistro"
, CLIENTE_DERECHO."IdInstrumento"						AS "IdInstrumento"
, INSTRUMENTO."Nemotecnico"								AS "Nemotecnico"
, CLIENTE_DERECHO."IdClienteConDerechoJuntaAccionista"	AS "IdClienteConDerechoJuntaAccionista"
, CLIENTE_DERECHO."IdEventoJuntaAccionista" 			AS "IdEventoJuntaAccionista"
, EVENTO."IdEmisor"										AS "IdEmisor"
, SOCIO."IdSocioDeNegocio"								AS "IdSocioDeNegocio"
, INSTRUMENTO."ISIN"									AS "CodigoISIN"
, CLIENTE_DERECHO."CantidadDerechoCalculado"			AS "CantidadDerechoCalculado"
, CLIENTE_DERECHO."FechaCalculo"						AS "FechaCalculo"
, CLIENTE_DERECHO."Origen"								AS "Origen"							
FROM "BSCL.Custodia.BD::CUS.ClienteConDerechoJuntaAccionista" AS CLIENTE_DERECHO
INNER JOIN "BSCL.Custodia.BD::CUS.EventoJuntaAccionista" AS EVENTO ON (EVENTO."IdEventoJuntaAccionista" = CLIENTE_DERECHO."IdEventoJuntaAccionista")
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLIENTE ON (CLIENTE."IdCliente" = CLIENTE_DERECHO."IdCliente")
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INSTRUMENTO ON (CLIENTE_DERECHO."IdInstrumento" = INSTRUMENTO."IdInstrumento")
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS SOCIO ON (SOCIO."IdCliente" = CLIENTE."IdCliente")
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuario" AS USUARIO ON (USUARIO."IdUsuario" = CLIENTE_DERECHO."IdUsuario")
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" AS ESTADO ON (ESTADO."IdEstado" = CLIENTE_DERECHO."IdEstado")
LEFT OUTER JOIN "BSCL.Custodia.BD::CUS.TipoJuntaAccionista" AS TIPOJUNTA ON (TIPOJUNTA."Codigo" = CLIENTE_DERECHO."IdTipoInstruccion" AND TIPOJUNTA."Entidad" = 'TIPOINSTRUCCION');

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarClientesConDerechoJuntaAccionista" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
