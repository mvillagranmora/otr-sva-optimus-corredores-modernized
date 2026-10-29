SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.ConsultarInstructivoVotacionJuntaAccionistas";

CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.ConsultarInstructivoVotacionJuntaAccionistas"
(
    IN p_IdEmpresa BIGINT,
    IN p_IdUsuario BIGINT,
    IN p_IdModulo  NVARCHAR(3),
    IN p_IdEvento  BIGINT
)
    LANGUAGE SQLSCRIPT
    SQL SECURITY INVOKER
    DEFAULT SCHEMA "BSCL"
AS
BEGIN
/*
    v_consulta = SELECT 'MAS' AS "IdTipoInstruccion",
                        'Instrucciones Masiva' AS "TipoInstruccion",
                        'CPRO' AS "IdTipoCuenta",
                        'Por Cuenta Propia' AS "TipoCuenta",
                        5000 AS "CantidadAcciones",
                        'Invertir en fundacion el joan jose' AS "Opcion Votacion",
                        'COPEC PRIMERA EN SERVICIO S.A.' AS "Sociedad",
                        CURRENT_DATE AS "FechaRealizacion",
                        CURRENT_TIME AS "Hora",
                        'La bolsa 666' AS "Lugar"
                 FROM DUMMY
                 UNION ALL
                 SELECT 'MAS' AS "IdTipoInstruccion",
                        'Instrucciones Masiva' AS "TipoInstruccion",
                        'TER' AS "IdTipoCuenta",
                        'Por Cuenta Terceros' AS "TipoCuenta",
                        6000 AS "CantidadAcciones",
                        'Invertir en fundacion la Khu Quita' AS "Opcion Votacion",
                        'COPEC PRIMERA EN SERVICIO S.A.' AS "Sociedad",
                        CURRENT_DATE AS "FechaRealizacion",
                        CURRENT_TIME AS "Hora",
                        'La bolsa 666' AS "Lugar"
                 FROM DUMMY
                 UNION ALL
                 SELECT 'ESP' AS "IdTipoInstruccion",
                        'Instrucciones Especiales' AS "TipoInstruccion",
                        'TER' AS "IdTipoCuenta",
                        'Por Cuenta Terceros' AS "TipoCuenta",
                        15000 AS "CantidadAcciones",
                        'Invertir en bar Lapi Ruda' AS "Opcion Votacion",
                        'COPEC PRIMERA EN SERVICIO S.A.' AS "Sociedad",
                        CURRENT_DATE AS "FechaRealizacion",
                        CURRENT_TIME AS "Hora",
                        'La bolsa 666' AS "Lugar"
                 FROM DUMMY
                 UNION ALL
                 SELECT 'ESP' AS "IdTipoInstruccion",
                        'Instrucciones Especiales' AS "TipoInstruccion",
                        'TER' AS "IdTipoCuenta",
                        'Por Cuenta Terceros' AS "TipoCuenta",
                        1020 AS "CantidadAcciones",
                        'No Invertir parfavar' AS "Opcion Votacion",
                        'COPEC PRIMERA EN SERVICIO S.A.' AS "Sociedad",
                        CURRENT_DATE AS "FechaRealizacion",
                        CURRENT_TIME AS "Hora",
                        'La bolsa 666' AS "Lugar"
                 FROM DUMMY
                 UNION ALL
                 SELECT 'ESP' AS "IdTipoInstruccion",
                        'Instrucciones Especiales' AS "TipoInstruccion",
                        'TER' AS "IdTipoCuenta",
                        'Por Cuenta Terceros' AS "TipoCuenta",
                        666 AS "CantidadAcciones",
                        'Salir al extranjero a buscar nuevos horizontes de inversion' AS "Opcion Votacion",
                        'COPEC PRIMERA EN SERVICIO S.A.' AS "Sociedad",
                        CURRENT_DATE AS "FechaRealizacion",
                        CURRENT_TIME AS "Hora",
                        'La bolsa 666' AS "Lugar"
                 FROM DUMMY; 
            
                 */
     v_consulta = SELECT "TipoJuntaAccionista"."Codigo" AS "IdTipoInstruccion",
     					 "TipoJuntaAccionista"."Descripcion" AS "TipoInstruccion",
     					 SUM("ClienteConDerechoJuntaAccionista"."CantidadDerecho") AS "CantidadAcciones",
     					 "ClienteConDerechoJuntaAccionista"."Instruccion" AS "Opcion Votacion",
     					 "ConsultarEmisor"."NombreCompleto" AS "Sociedad",
     					 "EventoJuntaAccionista"."FechaRealizacion",
     					 "EventoJuntaAccionista"."HoraRealizacion" AS "Hora",
     					 "EventoJuntaAccionista"."Lugar",
     					 CASE WHEN COALESCE("Empresa"."Codigo",'') = '' THEN 'TER' 
                         												ELSE 'CPRO'
                         END AS "IdTipoCuenta",
                         CASE WHEN COALESCE("Empresa"."Codigo",'') = '' THEN 'Por Cuenta Terceros' 
                         												ELSE 'Por Cuenta Propia'
                         END AS "TipoCuenta"
                  FROM "BSCL.Custodia.BD::CUS.EventoJuntaAccionista" AS "EventoJuntaAccionista"
                  INNER JOIN "BSCL.Custodia.BD::CUS.ClienteConDerechoJuntaAccionista" AS "ClienteConDerechoJuntaAccionista" ON ("ClienteConDerechoJuntaAccionista"."IdEventoJuntaAccionista" = "EventoJuntaAccionista"."IdEventoJuntaAccionista")
                  INNER JOIN "BSCL.Custodia.BD::CUS.TipoJuntaAccionista" AS "TipoJuntaAccionista" ON ("TipoJuntaAccionista"."Codigo" = "ClienteConDerechoJuntaAccionista"."IdTipoInstruccion" AND "TipoJuntaAccionista"."Entidad" = 'TIPOINSTRUCCION')
                  LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS "ConsultarSocioDeNegocio" ON ("ConsultarSocioDeNegocio"."IdCliente" = "ClienteConDerechoJuntaAccionista"."IdCliente")
                  LEFT JOIN "BSCL.Plataforma.BD::PLA.Empresa" AS "Empresa" ON ("Empresa"."IdSocioDeNegocio" = "ConsultarSocioDeNegocio"."IdSocioDeNegocio") 
                  LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS "ConsultarEmisor" ON ("ConsultarEmisor"."IdEmisor" = "EventoJuntaAccionista"."IdEmisor")
                  WHERE "EventoJuntaAccionista"."IdEventoJuntaAccionista" = :p_IdEvento
                  AND "EventoJuntaAccionista"."IdEmpresa" = :p_IdEmpresa
                  AND "ClienteConDerechoJuntaAccionista"."IdEstado" = 'ACT'
                  GROUP BY "TipoJuntaAccionista"."Codigo","TipoJuntaAccionista"."Descripcion","ClienteConDerechoJuntaAccionista"."Instruccion",
                  "ConsultarEmisor"."NombreCompleto","EventoJuntaAccionista"."FechaRealizacion","EventoJuntaAccionista"."HoraRealizacion",
                  "EventoJuntaAccionista"."Lugar","Empresa"."Codigo"
                  ORDER BY "TipoJuntaAccionista"."Descripcion" DESC;
     select * from :v_consulta;
END;
