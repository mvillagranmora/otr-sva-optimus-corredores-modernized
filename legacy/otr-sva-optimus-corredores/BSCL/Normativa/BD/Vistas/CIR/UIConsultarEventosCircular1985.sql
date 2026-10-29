SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Normativa.BD.Vistas::CIR.UIConsultarEventosCircular1985";

CREATE VIEW "BSCL.Normativa.BD.Vistas::CIR.UIConsultarEventosCircular1985" AS
SELECT 	
	     				ORDENES."FechaEjecucion"
	     				,ORDENES."IdCliente" 
						,ORDENES."IdPortafolio"
	     				,COALESCE("POR"."CodigoPortafolio",'') AS "CodigoPortafolio"
	     				,"CLI"."NombreCompleto"
	     				,COALESCE("CPDF"."NombreLargo",'-') AS "DireccionFisica"
	     				,COALESCE(DIRELE."Direccion",'-') AS "DireccionElectronica"
	     				,CASE  WHEN EVT."Procesado" = 'S' AND COALESCE(DIRELE."Direccion",'-') <> '-'  THEN 'Enviada' ELSE 'No Enviado' END AS "Procesado"
	     				,TO_NVARCHAR(ADD_SECONDS(EVT."FechaCreacion",Conf."Valor"),'DD-MM-YYYY') AS "FechaCreacion"
	     				,EVT."IdEvento" 
                        ,ORDENES."UIFechaEjecucion"
						,"CLI"."DocumentoIdentidad"
	     		FROM (SELECT ORD."IdCliente" 
	     							,ORD."IdPortafolio"
	     							,TO_VARCHAR(COALESCE(ORD."FechaReferencia", ORD."FechaEjecucion"),'YYYY-MM-DD') AS "FechaEjecucion"
									,TO_VARCHAR(COALESCE(ORD."FechaReferencia", ORD."FechaEjecucion"),'DD-MM-YYYY') AS "UIFechaEjecucion"
	     							,ORD."IdEmpresa" 
	     					  	FROM "BSCL.Normativa.BD::EJORD.EjecucionOrdenes" AS ORD
	     						GROUP BY ORD."IdCliente" 
	     						,ORD."IdPortafolio" 
	     						,TO_VARCHAR(COALESCE(ORD."FechaReferencia", ORD."FechaEjecucion"),'YYYY-MM-DD')
	     						,TO_VARCHAR(COALESCE(ORD."FechaReferencia", ORD."FechaEjecucion"),'DD-MM-YYYY')
	     						,ORD."IdEmpresa" ) AS ORDENES
	     		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI ON ORDENES."IdCliente" = CLI."IdCliente"
	     		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS "POR" ON ("ORDENES"."IdPortafolio" = "POR"."IdPortafolio")
	     		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS "CPDF" ON ("CPDF"."IdDireccionFisica" = "CLI"."IdDireccionFisicaPrincipal")
	     		LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS Conf ON ORDENES."IdEmpresa" = Conf."IdEmpresa" AND  Conf."CodigoParametro" = ('PLA.SegundosZonaHoraria')
	     		LEFT JOIN (SELECT
								EVT."IdEvento",
								EVT."IdPersonaDestinatario",
								EVT."IdPortafolio",
								EVT."FechaCreacion",
								EVT."Procesado",
								EVTDET."Valor" AS "FechaEjecucion"
							FROM
								"BSCL.Plataforma.BD::EVT.Evento" AS EVT
							LEFT JOIN "BSCL.Plataforma.BD::EVT.EventoDetalle" AS EVTDET ON
								EVTDET."IdEvento" = EVT."IdEvento"
								AND EVTDET."Llave" = 'FechaEjecucion'
							WHERE
								EVT."CodigoTipoEvento" = 'NOTIFICAR_NORMATIVA_CIR1985') AS EVT ON
					CLI."IdPersona" = EVT."IdPersonaDestinatario"
					AND ORDENES."IdPortafolio" = EVT."IdPortafolio"
					AND ORDENES."UIFechaEjecucion" = EVT."FechaEjecucion"
	     		LEFT JOIN "BSCL.Plataforma.BD::EVT.EventoDirecciones" AS EVTDIR ON EVTDIR."IdEvento" = EVT."IdEvento"
	     		LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS  DIRELE ON DIRELE."IdDireccionElectronica" = EVTDIR."IdDireccionElectronica";

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::CIR.UIConsultarEventosCircular1985" TO  BCS_DESARROLLADOR WITH GRANT OPTION;