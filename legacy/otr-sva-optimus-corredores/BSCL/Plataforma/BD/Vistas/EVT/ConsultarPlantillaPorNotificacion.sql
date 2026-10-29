SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarPlantillaPorNotificacion"; 

CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarPlantillaPorNotificacion"  
AS
select DISTINCT
plt."IdPlantilla",
plt."IdEmpresa",
 ccevt."FechaCreacion",
REPLACE(TO_CHAR(BINTOSTR(CAST(to_char("Template") AS binary))),'!!}','!}') as "Template",
ccevt."Agrupa",
cnr."IdConfiguracionEvento",
plt."Asunto",
cnr."TokenConfirmacion",
conf."Valor"||'?token='||cnr."TokenConfirmacion" as "UrlConfirmacion",
ccevt."IdUsuario",
cnr."IdNotificacion"
FROM "BSCL.Plataforma.BD::EVT.Plantilla" plt
INNER JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionEventos" ccevt ON ccevt."IdPlantilla" = plt."IdPlantilla"
INNER JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarNotificacion" cnr ON cnr."IdConfiguracionEvento" = ccevt."IdConfiguracionEvento"
INNER JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionEventoDetalles" cced ON cnr."IdConfiguracionEvento" = ccevt."IdConfiguracionEvento" 
INNER JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarTipoEventoCombo" ctec ON ctec."CodigoTipoEvento" = cced."CodigoTipoEvento"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" conf ON "CodigoParametro" = 'PLA.UrlPortalConfirmacion' AND cnr."IdEmpresa" = conf."IdEmpresa"
WHERE ccevt."Agrupa" is not null;

 GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarPlantillaPorNotificacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


 


