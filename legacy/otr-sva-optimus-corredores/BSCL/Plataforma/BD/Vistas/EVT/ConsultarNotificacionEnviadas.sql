SET SCHEMA "BSCL"; 
go
DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarNotificacionEnviadas"; 
go
CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarNotificacionEnviadas" AS WITH "CTENOTIFICACIONES" AS (
SELECT
	N."IdEmpresa",
	N."IdNotificacion",
	ROW_NUMBER() OVER(PARTITION BY N."IdNotificacion") AS "NumeroNotificacion",
	E."IdEvento" AS "IdEvento",
	E."CodigoTipoEvento" AS "CodigoTipoEvento",
	N."IdConfiguracionEvento",
	N."FechaCreacion",
	COALESCE(USU."IdUsuario", USUDEF."IdUsuario") AS "IdUsuario",
	COALESCE(USU."CodigoUsuario", USUDEF."CodigoUsuario") AS "UsuarioReEnviado",
	CASE
		N."Confirmado" WHEN 'S' THEN CP."NombreCompleto"
		WHEN 'N' THEN ''
	END AS "UsuarioConfirma",
	COALESCE(USU."CodigoUsuario", USUDEF."CodigoUsuario") AS "UsuarioDescarta",
	"DescripcionConfiguracion",
	N."FormaDespacho" AS "FormaDespachoSrv",
	N."TiempoDespacho" AS "TiempoDespachoSrv",
	CASE
		N."FormaDespacho" WHEN 'U' THEN 'UI_UI'
		WHEN 'C' THEN 'UI_CORREO'
		WHEN 'T' THEN 'UI_AMBOS'
	END AS "FormaDespacho",
	CASE
		N."TiempoDespacho" WHEN 'M' THEN 'UI_MANUAL'
		WHEN 'H' THEN 'UI_HORA'
		WHEN 'I' THEN 'UI_INMEDIATO'
	END AS "TiempoDespacho",
	"FechaDespachoOrginal",
	"FechaDespachoRecordatorio",
	"Enviado" AS "EnviadoSrv",
	"Enviar" AS "EnviarSrv",
	CASE
		"Enviar" WHEN 'S' THEN 'UI_SI'
		WHEN 'N' THEN 'UI_NO'
	END AS "Enviar",
	"Enviado",
	"FechaEnviado",
	"ReEnviar" AS "ReEnviarSrv",
	CASE
		"ReEnviar" WHEN 'S' THEN 'UI_SI'
		WHEN 'N' THEN 'UI_NO'
	END AS "ReEnviar",
	CASE
		"ReEnviado" WHEN 'S' THEN 'UI_SI'
		WHEN 'N' THEN 'UI_NO'
	END AS "ReEnviado",
	"FechaReEnviado",
	"IdUsuarioReEnviado",
	CASE
		N."ConfirmacionDespacho" WHEN 'S' THEN 'UI_SI'
		WHEN 'N' THEN 'UI_NO'
	END AS "ConfirmacionDespacho",
	N."TokenConfirmacion",
	CASE
		"Confirmado" WHEN 'S' THEN 'UI_SI'
		WHEN 'N' THEN 'UI_NO'
	END AS "Confirmado",
	CASE
		N."Confirmado" WHEN 'S' THEN N."FechaConfirmado"
		WHEN 'N' THEN ''
	END AS "FechaConfirmado",
	CASE
		"AccionConfirmada" WHEN 'PEN' THEN 'UI_PENDIENTE'
		WHEN 'RZD' THEN 'UI_RECHAZADA'
		WHEN 'ACP' THEN 'UI_APROBADA'
		WHEN 'VCD' THEN 'UI_VENCIDA'
	END AS "AccionConfirmada",
	"IdUsuarioConfimado",
	N."FechaVencimiento",
	N."FechaRecordatorio",
	MAP(
      "Observacion",
	'PEN',
	'UI_PENDIENTE',
	"Observacion"
    ) AS "Observacion",
	CASE
		"Descartado" WHEN 'S' THEN 'UI_SI'
		WHEN 'N' THEN 'UI_NO'
	END AS "Descartado",
	"IdUsuarioDescarte",
	"FechaDescarte",
	"Leida",
	"DireccionAlternativa",
	"AccionConfirmada" AS "EstadoConfirmacion",
	"DirEvt" AS "DireccionEvento",
	CP1."DocumentoIdentidad" AS "IdRemitente",
	CP1."NombreCompleto" AS "NombreRemitente",
	CASE
		WHEN CE."Descripcion" = 'NOTIFICAR_EVENTO_AUTORIZACION' THEN DI."DocumentoIdentidad"
		ELSE CP."DocumentoIdentidad"
	END AS "IdDestinatario",
	CASE
		WHEN CE."Descripcion" = 'NOTIFICAR_EVENTO_AUTORIZACION'
		AND CP2."Tipo" = 'N' THEN CP2."Nombres" || ' ' || CP2."ApellidoPaterno" || ' ' || CP2."ApellidoMaterno"
		WHEN CE."Descripcion" = 'NOTIFICAR_EVENTO_AUTORIZACION'
		AND CP2."Tipo" = 'J' THEN CP2."RazonSocial"
		ELSE CP."NombreCompleto"
	END AS "NombreDestinatario",
	COALESCE(USU."IdUsuario", USUDEF."IdUsuario") AS "IdUsuarioEnviado",
	COALESCE(USU."CodigoUsuario", USUDEF."CodigoUsuario") AS "UsuarioEnviado",
	COALESCE(EC."Nemo",
	'') AS "Nemo",
	CASE
		WHEN "ExcluyeDestinatario" = 'S' THEN 'E'
		WHEN "ExcluyeDestinatario" = 'N'
		AND CE."Descripcion" != 'NOTIFICAR_EVENTO_AUTORIZACION' 
    	THEN UPPER(CPDE."Direccion") 
		WHEN "ExcluyeDestinatario" = 'N'
		AND CE."Descripcion" = 'NOTIFICAR_EVENTO_AUTORIZACION' 
		THEN UPPER(CPDE2."Direccion")
		
	END AS "Direccion",
	"Asunto",
	COALESCE(EC."Secuencia",
	EDET."Valor") AS "Secuencia",
	E."IdOrigenEvento"
FROM
	"BSCL.Plataforma.BD::EVT.Notificacion" N
LEFT JOIN "BSCL.Plataforma.BD::EVT.NotificacionDetalle" ND ON
	ND."IdNotificacion" = N."IdNotificacion"
LEFT JOIN "BSCL.Plataforma.BD::EVT.Evento" E ON
	ND."IdEvento" = E."IdEvento"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarTipoEventosAsociadosAConfiguracion" ctipoevt ON
	N."IdConfiguracionEvento" = ctipoevt."IdConfiguracionEvento"
INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionTipoEvento" cconftip ON
	cconftip."CodigoTipoEvento" = ctipoevt."CodigoTipoEvento"
LEFT JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" CE ON
	CE."IdConfiguracionEvento" = N."IdConfiguracionEvento"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" CSN ON
	CSN."IdPersona" = E."IdPersonaRemitente"
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" USU ON
	USU."IdUsuario" = E."IdUsuario"
LEFT JOIN (SELECT "IdUsuario", "IdEmpresa", "CodigoUsuario" FROM BSCL."BSCL.Plataforma.BD::ACC.Usuario" WHERE "CodigoUsuario" LIKE '%SISTEMA%') USUDEF ON
	USUDEF."IdEmpresa"= N."IdEmpresa"	
LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalEvento" ECE ON
	ECE."IdEvento" = E."IdEvento"
LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" CEMP ON
	CEMP."IdPersona" = E."IdPersonaRemitente"
LEFT JOIN ( SELECT "PER"."IdPersona","PER"."IdDocumentoIdentidad","PER"."Tipo","PER"."Nombres",
            "PER"."ApellidoPaterno","PER"."ApellidoMaterno","PER"."RazonSocial",	
            CASE "PER"."Tipo" WHEN 'N' 
            THEN "PER"."Nombres" ||' '|| "PER"."ApellidoPaterno" || ' ' ||"PER"."ApellidoMaterno" 
            ELSE "PER"."RazonSocial" 
            END  as "NombreCompleto",
            "IDENT"."DocumentoIdentidad"  AS "DocumentoIdentidad" 
            FROM   "BSCL.SocioNegocio.BD::PER.Persona" AS "PER"
            LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "IDENT" 
            ON "PER"."IdDocumentoIdentidad" = "IDENT"."IdDocumentoIdentidad"
            WHERE "PER"."Borrado"='N' AND "IDENT"."Borrado"='N') CP ON
	CP."IdPersona" = CASE
		WHEN E."CodigoTipoEvento" = 'NOTIFICAR_EVENTO_AUTORIZACION' THEN 0
		ELSE E."IdPersonaDestinatario"
	END
LEFT JOIN ( SELECT "PER"."IdPersona","PER"."IdDocumentoIdentidad","PER"."Tipo","PER"."Nombres",
            "PER"."ApellidoPaterno","PER"."ApellidoMaterno","PER"."RazonSocial",	
            CASE "PER"."Tipo" WHEN 'N' 
            THEN "PER"."Nombres" ||' '|| "PER"."ApellidoPaterno" || ' ' ||"PER"."ApellidoMaterno" 
            ELSE "PER"."RazonSocial" 
            END  as "NombreCompleto",
            "IDENT"."DocumentoIdentidad"  AS "DocumentoIdentidad" 
            FROM   "BSCL.SocioNegocio.BD::PER.Persona" AS "PER"
            LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "IDENT" 
            ON "PER"."IdDocumentoIdentidad" = "IDENT"."IdDocumentoIdentidad"
            WHERE "PER"."Borrado"='N' AND "IDENT"."Borrado"='N')CP1 ON
	CP1."IdPersona" = E."IdPersonaRemitente"		
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" CPDE ON
	CPDE."IdPersona" = CASE
		WHEN E."CodigoTipoEvento" = 'NOTIFICAR_EVENTO_AUTORIZACION' THEN 0
		ELSE E."IdPersonaDestinatario"
	END
LEFT JOIN "BSCL.Plataforma.BD::EVT.Plantilla" plt ON
	plt."IdPlantilla" = ctipoevt."IdPlantilla"
LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapital" EC ON
	EC."IdEventoCapital" = ECE."IdEventoCapital"
LEFT JOIN "BSCL.Plataforma.BD::EVT.EventoDirecciones" ED ON
	ED."IdEvento" = E."IdEvento"
	AND E."CodigoTipoEvento" = 'NOTIFICAR_EVENTO_AUTORIZACION'
LEFT JOIN "BSCL.Plataforma.BD::EVT.EventoDetalle" EDET ON
	EDET."IdEvento" = E."IdEvento"
	AND EDET."Llave" = 'ID_EVENTO_AUT'
	AND ED."IdEvento" = EDET."IdEvento"
LEFT JOIN "BSCL.Plataforma.BD::EVT.EventoDetalle" EDET2 ON
	EDET2."IdEvento" = E."IdEvento"
	AND EDET2."Llave" = 'USUARIO'
	AND ED."IdEvento" = EDET2."IdEvento"
	AND EDET."IdEvento" = EDET2."IdEvento"
LEFT JOIN "BSCL.Plataforma.BD::EVT.EventoDetalle" EDET3 ON
	EDET3."IdEvento" = E."IdEvento"
	AND ED."IdEvento" = EDET3."IdEvento"
	AND EDET."IdEvento" = EDET2."IdEvento"
	AND EDET3."IdEvento" = EDET2."IdEvento"
	AND EDET3."Llave" = 'ID_ETAPAEVENTO_AUT'
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" CPDE2 ON
	CPDE2."IdDireccionElectronica" = ED."IdDireccionElectronica"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" CP2 ON
	CP2."IdPersona" = CPDE2."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI ON
	DI."IdDocumentoIdentidad" = CP2."IdDocumentoIdentidad"
LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" CEMP2 ON
	CEMP2."IdPersona" = CPDE2."IdPersona"
	AND CP2."IdPersona" = CEMP2."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" USU2 ON
	UPPER(EDET2."Valor") = UPPER(USU2."CodigoUsuario")
LEFT JOIN "BSCL.Plataforma.BD::AUT.AutorizadoEtapaEvento" AEE ON
	CAST(EDET3."Valor" AS BIGINT)= AEE."IdEtapaEvento"
WHERE (
      "DirEvt" = 'EMPEMP'
		OR "DirEvt" = 'EMPCLI'
		OR "DirEvt" = 'EMPPOR'
		OR (
        "DirEvt" = 'SYSEMP'
			AND CE."Descripcion" = 'NOTIFICAR_EVENTO_AUTORIZACION'
			AND EDET2."Llave" = 'USUARIO'
			AND ED."IdEvento" = EDET2."IdEvento"
			AND UPPER(EDET2."Valor") = UPPER(USU."CodigoUsuario")
				AND USU2."IdUsuario" = AEE."IdUsuario"
      )
    )
	AND "Enviado" = 'S'
	AND CPDE."IdEstado" = 'ACT' AND CPDE."Predeterminado" = 'S' AND CPDE."Borrado" = 'N'
) 
SELECT
	CTN."IdEmpresa",
	"IdEvento",
	CTN."CodigoTipoEvento",
	CTN."IdNotificacion",
	CTN."IdConfiguracionEvento",
	M."FechaCreacion",
	CTN."IdUsuario",
	"UsuarioReEnviado",
	"UsuarioConfirma",
	"UsuarioDescarta",
	"DescripcionConfiguracion",
	"FormaDespachoSrv",
	"TiempoDespachoSrv",
	CTN."FormaDespacho",
	CTN."TiempoDespacho",
	M."FechaEnviado" AS"FechaDespachoOrginal",
	"FechaDespachoRecordatorio",
	"EnviadoSrv",
	"EnviarSrv",
	CASE
		WHEN CE."Agrupa" = 'S' THEN
      CASE
			WHEN M."IdEnvio" = "IdEvento" THEN M."EstadoEnviado"
			ELSE CTN."EnviarSrv"
		END
		WHEN CE."Agrupa" = 'N' THEN
      CASE
			WHEN M."IdEnvio" = CTN."IdNotificacion" THEN M."EstadoEnviado"
			ELSE CTN."EnviarSrv"
		END
		ELSE 
      CTN."EnviarSrv"
	END AS "Enviar",
	M."FechaEnviado",
	"ReEnviarSrv",
	"ReEnviar",
	"ReEnviado",
	"FechaReEnviado",
	"IdUsuarioReEnviado",
	CTN."ConfirmacionDespacho",
	"TokenConfirmacion",
	"Confirmado",
	"FechaConfirmado",
	"AccionConfirmada",
	"IdUsuarioConfimado",
	CTN."FechaVencimiento",
	CTN."FechaRecordatorio",
	"Observacion",
	"Descartado",
	"IdUsuarioDescarte",
	"FechaDescarte",
	"Leida",
	"DireccionAlternativa",
	"EstadoConfirmacion",
	"DireccionEvento",
	"IdRemitente",
	"NombreRemitente",
	"IdDestinatario",
	"NombreDestinatario",
	"IdUsuarioEnviado",
	"UsuarioEnviado",
	"Nemo",
	"Direccion",
	CTN."Asunto",
	"Secuencia",
	COALESCE(M."EstadoEnviado",
	CTN."EnviarSrv") AS "Enviado",
	ARC."Nombre" AS "NombreAdjunto",
	COALESCE(M."LogEnvio", '') AS "LogEnvio"
FROM
	CTENotificaciones CTN
LEFT JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" CE ON
	CE."IdConfiguracionEvento" = CTN."IdConfiguracionEvento"
LEFT JOIN "BSCL.Plataforma.BD::EVT.Mail" M ON
	M."IdEnvio" = CTN."IdEvento"
	OR M."IdEnvio" = CTN."IdNotificacion"
LEFT JOIN  "BSCL.Plataforma.BD::EVT.OrigenEvento" OE ON 
	OE."IdOrigenEvento" = CTN."IdOrigenEvento"
LEFT JOIN "BSCL.Plataforma.BD::EVT.ArchivoAdjunto"  ARC ON M."IdEmail" = ARC."IdEmail"
WHERE  OE."IdEstado" = 'APD'
 AND
  (
    CTN."NumeroNotificacion" = '1' 
    AND CTN."DescripcionConfiguracion" != 'NOTIFICAR_EVENTO_AUTORIZACION'
  ) 
  OR (
    CTN."DescripcionConfiguracion" = 'NOTIFICAR_EVENTO_AUTORIZACION'
  );