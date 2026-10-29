SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarMailDestinatarios"; 
 
CREATE VIEW   "BSCL.Plataforma.BD.Vistas::EVT.ConsultarMailDestinatarios"
AS
Select  DISTINCT
"IdUsuarioRol",
"IdConfiguracionEvento",
evt."IdEvento",
evt."CodigoTipoEvento",
"Direccion", 
TO_CHAR("MensajeBody") AS "MensajeBody"
FROM "BSCL.Plataforma.BD::EVT.ConfiguracionTipoEvento" cte
LEFT JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionUsuarioTipoEvento" cute
ON cte."IdConfiguracionTipoEvento" = cute."IdConfiguracionTipoEvento" 
LEFT JOIN "BSCL.Plataforma.BD::EVT.TipoEvento" te
ON te."CodigoTipoEvento" = cte."CodigoTipoEvento"
LEFT JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle" ced
ON te."CodigoTipoEvento" = ced."CodigoTipoEvento"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarMailPorRol" cmpr
ON cmpr."IdRol" = "IdUsuarioRol"
LEFT JOIN "BSCL.Plataforma.BD::EVT.Evento" evt
ON evt."CodigoTipoEvento" = ced."CodigoTipoEvento";

 
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarMailDestinatarios" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


 
