SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarNotificacionRecibidas"; 

CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarNotificacionRecibidas"  
AS
select DISTINCT
N."IdEmpresa",
N."IdNotificacion",
N."IdConfiguracionEvento",
N."FechaCreacion",
N."FormaDespacho" as "FormaDespachoSrv",
N."TiempoDespacho" as "TiempoDespachoSrv",
CE."Agrupa",
CASE N."FormaDespacho" WHEN 'U' THEN 'UI_UI' WHEN 'C' THEN 'UI_CORREO' WHEN 'T' THEN 'UI_AMBOS' END as "FormaDespacho" ,
CASE N."TiempoDespacho" WHEN 'M' THEN 'UI_MANUAL' WHEN 'H' THEN 'UI_HORA' WHEN 'I' THEN 'UI_INMEDIATO' END as "TiempoDespacho",
N."FechaDespachoOrginal",
N."FechaDespachoRecordatorio",
N."Enviado" as "EnviadoSrv",
N."Enviar" as "EnviarSrv",
CASE N."Enviar" WHEN 'S' THEN 'UI_SI' WHEN 'N' THEN 'UI_NO' END as "Enviar",
CASE N."Enviado" WHEN 'S' THEN 'UI_SI' WHEN 'N' THEN 'UI_NO' END as "Enviado",
N."FechaEnviado",
N."IdUsuarioEnviado",
N."ReEnviar" as "ReEnviarSrv",
CASE N."ReEnviar" WHEN 'S' THEN 'UI_SI' WHEN 'N' THEN 'UI_NO' END as "ReEnviar",
CASE N."ReEnviado" WHEN 'S' THEN 'UI_SI' WHEN 'N' THEN 'UI_NO' END as "ReEnviado",
N."FechaReEnviado",
N."IdUsuarioReEnviado",
CASE N."ConfirmacionDespacho" WHEN 'S' THEN 'UI_SI' WHEN 'N' THEN 'UI_NO' END as "ConfirmacionDespacho" ,
N."TokenConfirmacion",
CASE N."Confirmado" WHEN 'S' THEN 'UI_SI' WHEN 'N' THEN 'UI_NO' END as "Confirmado",
N."FechaConfirmado",
CASE N."AccionConfirmada" 
	WHEN 'PEN' THEN 'UI_PENDIENTE'	
	WHEN 'RZD' THEN 'UI_RECHAZADA' 
	WHEN 'ACP' THEN 'UI_APROBADA' 
	WHEN 'VCD' THEN 'UI_VENCIDA'
END as "AccionConfirmada",
N."AccionConfirmada" as "EstadoConfirmacion",
N."IdUsuarioConfimado",
N."FechaVencimiento",
N."FechaRecordatorio",
MAP(N."Observacion", 'PEN','UI_PENDIENTE',"Observacion") as "Observacion",
CASE N."Descartado" WHEN 'S' THEN 'UI_SI' WHEN 'N' THEN 'UI_NO' END as "Descartado",
N."IdUsuarioDescarte",
N."FechaDescarte",
N."Leida",
N."DireccionAlternativa",
COALESCE(EC."Nemo",'') AS "Nemo",
ctipoevt."DirEvt" as "DireccionEvento",
cconftip."NotificaTodosUsuarios",
USU."IdUsuario",
ctipoevt."DescripcionConfiguracion",

CASE ctipoevt."DirEvt" 
	WHEN 'SYSEMP' THEN '1'
	ELSE CP."DocumentoIdentidad"
END as "IdRemitente",

CASE ctipoevt."DirEvt" 
	WHEN 'SYSEMP' THEN 'Sistema'
	ELSE CP."NombreCompleto"
END as "NombreRemitente",
CP."DocumentoIdentidad" as "IdDestinatario",
CP."NombreCompleto" as "NombreDestinatario",
usu."CodigoUsuario" as "CodigoUsuario",
usu."IdUsuario" as "IdUsuarioRecibe",
CASE "ExcluyeDestinatario" 
	WHEN 'S' THEN 'E'
	WHEN 'N' THEN CASE "TipoEntidad" WHEN 'CLI' THEN CPDE."Direccion" WHEN 'EMP' THEN CPDE2."Direccion" END
END as "Direccion",
plt."Asunto"
FROM "BSCL.Plataforma.BD::EVT.Notificacion" N
INNER JOIN "BSCL.Plataforma.BD::EVT.NotificacionDetalle" ND ON ND."IdNotificacion" = N."IdNotificacion"
INNER JOIN "BSCL.Plataforma.BD::EVT.Evento" E ON ND."IdEvento" = E."IdEvento"
INNER JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarTipoEventosAsociadosAConfiguracion"  ctipoevt ON N."IdConfiguracionEvento" = ctipoevt."IdConfiguracionEvento"
INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionTipoEvento" cconftip ON cconftip."CodigoTipoEvento" = ctipoevt."CodigoTipoEvento"
INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" CE ON CE."IdConfiguracionEvento" = N."IdConfiguracionEvento" 
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" CP ON CP."IdPersona" = E."IdPersonaDestinatario"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" CSN ON  CSN."IdPersona" = E."IdPersonaDestinatario"
INNER JOIN  "BSCL.Plataforma.BD::ACC.Usuario" USU 
	ON (CSN."IdCliente" = USU."IdEntidad" and "TipoEntidad" = 'CLI') OR
   (CSN."IdEmpleado" = USU."IdEntidad" and "TipoEntidad" = 'EMP') OR
   (CSN."IdProveedor" = USU."IdEntidad" and "TipoEntidad" = 'PRO') OR
   (CSN."IdContraparte" = USU."IdEntidad" and "TipoEntidad" = 'CNT')
LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalEvento" ECE ON ECE."IdEvento" = E."IdEvento"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarPlantillas"  plt ON plt."IdPlantilla" = ctipoevt."IdPlantilla"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" CPDE ON CP."IdPersona" = CPDE."IdPersona"   
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CEMP ON "TipoEntidad" = 'EMP' AND CEMP."IdPersona" = CP."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" CPDE2 ON CPDE2."IdDireccionElectronica" = CEMP."IdDireccionElectronicaPrincipal"
LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapital" EC ON EC."IdEventoCapital" = ECE."IdEventoCapital"
WHERE ("DirEvt" = 'SYSEMP' or "DirEvt" = 'CLIEMP' OR "DirEvt" = 'EMPEMP') AND "Enviado" = 'S' AND E."CodigoTipoEvento" !='NOTIFICAR_EVENTO_AUTORIZACION'
UNION
select DISTINCT
N."IdEmpresa",
N."IdNotificacion",
N."IdConfiguracionEvento",
N."FechaCreacion",
N."FormaDespacho" as "FormaDespachoSrv",
N."TiempoDespacho" as "TiempoDespachoSrv",
CE."Agrupa",
CASE N."FormaDespacho" WHEN 'U' THEN 'UI_UI' WHEN 'C' THEN 'UI_CORREO' WHEN 'T' THEN 'UI_AMBOS' END as "FormaDespacho" ,
CASE N."TiempoDespacho" WHEN 'M' THEN 'UI_MANUAL' WHEN 'H' THEN 'UI_HORA' WHEN 'I' THEN 'UI_INMEDIATO' END as "TiempoDespacho",
N."FechaDespachoOrginal",
N."FechaDespachoRecordatorio",
N."Enviado" as "EnviadoSrv",
N."Enviar" as "EnviarSrv",
CASE N."Enviar" WHEN 'S' THEN 'UI_SI' WHEN 'N' THEN 'UI_NO' END as "Enviar",
CASE N."Enviado" WHEN 'S' THEN 'UI_SI' WHEN 'N' THEN 'UI_NO' END as "Enviado",
N."FechaEnviado",
N."IdUsuarioEnviado",
N."ReEnviar" as "ReEnviarSrv",
CASE N."ReEnviar" WHEN 'S' THEN 'UI_SI' WHEN 'N' THEN 'UI_NO' END as "ReEnviar",
CASE N."ReEnviado" WHEN 'S' THEN 'UI_SI' WHEN 'N' THEN 'UI_NO' END as "ReEnviado",
N."FechaReEnviado",
N."IdUsuarioReEnviado",
CASE N."ConfirmacionDespacho" WHEN 'S' THEN 'UI_SI' WHEN 'N' THEN 'UI_NO' END as "ConfirmacionDespacho" ,
N."TokenConfirmacion",
CASE N."Confirmado" WHEN 'S' THEN 'UI_SI' WHEN 'N' THEN 'UI_NO' END as "Confirmado",
N."FechaConfirmado",
CASE N."AccionConfirmada" 
	WHEN 'PEN' THEN 'UI_PENDIENTE'	
	WHEN 'RZD' THEN 'UI_RECHAZADA' 
	WHEN 'ACP' THEN 'UI_APROBADA' 
	WHEN 'VCD' THEN 'UI_VENCIDA'
END as "AccionConfirmada",
N."AccionConfirmada" as "EstadoConfirmacion",
N."IdUsuarioConfimado",
N."FechaVencimiento",
N."FechaRecordatorio",
MAP(N."Observacion", 'PEN','UI_PENDIENTE',"Observacion") as "Observacion",
CASE N."Descartado" WHEN 'S' THEN 'UI_SI' WHEN 'N' THEN 'UI_NO' END as "Descartado",
N."IdUsuarioDescarte",
N."FechaDescarte",
N."Leida",
N."DireccionAlternativa",
'' AS "Nemo",
ctipoevt."DirEvt" as "DireccionEvento",
cconftip."NotificaTodosUsuarios",
USU."IdUsuario",
ctipoevt."DescripcionConfiguracion",

CASE ctipoevt."DirEvt" 
	WHEN 'SYSEMP' THEN '1'
	ELSE CP."DocumentoIdentidad"
END as "IdRemitente",

CASE ctipoevt."DirEvt" 
	WHEN 'SYSEMP' THEN 'Sistema'
	ELSE CP."NombreCompleto"
END as "NombreRemitente",
CP."DocumentoIdentidad" as "IdDestinatario",
CP."NombreCompleto" as "NombreDestinatario",
usu."CodigoUsuario" as "CodigoUsuario",
usu."IdUsuario" as "IdUsuarioRecibe",
CASE "ExcluyeDestinatario" 
	WHEN 'S' THEN 'E'
	WHEN 'N' THEN CASE "TipoEntidad" WHEN 'CLI' THEN CPDE."Direccion" WHEN 'EMP' THEN CPDE."Direccion" END
END as "Direccion",
plt."Asunto"
FROM "BSCL.Plataforma.BD::EVT.Notificacion" N
INNER JOIN "BSCL.Plataforma.BD::EVT.NotificacionDetalle" ND ON ND."IdNotificacion" = N."IdNotificacion"
INNER JOIN "BSCL.Plataforma.BD::EVT.Evento" E ON ND."IdEvento" = E."IdEvento"
INNER JOIN "BSCL.Plataforma.BD::EVT.EventoDirecciones" ED ON ED."IdEvento" = E."IdEvento"
INNER JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarTipoEventosAsociadosAConfiguracion"  ctipoevt ON N."IdConfiguracionEvento" = ctipoevt."IdConfiguracionEvento"
INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionTipoEvento" cconftip ON cconftip."CodigoTipoEvento" = ctipoevt."CodigoTipoEvento"
INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" CE ON CE."IdConfiguracionEvento" = N."IdConfiguracionEvento" 
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" CPDE ON ED."IdDireccionElectronica" = CPDE."IdDireccionElectronica" 
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" CP ON CP."IdPersona" = CPDE."IdPersona"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" CSN ON  CSN."IdPersona" = CPDE."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CEMP ON  CEMP."IdPersona" = CP."IdPersona"
INNER JOIN  "BSCL.Plataforma.BD::ACC.Usuario" USU 
	ON (CSN."IdEmpleado" = USU."IdEntidad" and "TipoEntidad" = 'EMP')
LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalEvento" ECE ON ECE."IdEvento" = E."IdEvento"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarPlantillas"  plt ON plt."IdPlantilla" = ctipoevt."IdPlantilla"
LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapital" EC ON EC."IdEventoCapital" = ECE."IdEventoCapital"
WHERE ("DirEvt" = 'SYSEMP') AND "Enviado" = 'S' AND E."CodigoTipoEvento" ='NOTIFICAR_EVENTO_AUTORIZACION';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarNotificacionRecibidas" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 