SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarTareaDTE";
 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarTareaDTE"  
AS
SELECT
	TAR."IdTareaDTE",
	TAR."IdTareaDTEGrupo",
	TAR."TipoTareaDTE",
	TAR."IdRespuestaDTE",
	TAR."RespuestaDTEEnvioSII",
	TAR."IdComprobante",
	TAR."IdTipoComprobante",
	TAR."FolioComprobanteRespuestaDTE",
	TAR."FechaComprobante",
	TAR."IdEstadoTareaDTE",
	TAR."IdEstadoDTE",
	TAR."IdEstadoSII",
	TAR."FechaEnvioDte"
FROM "BSCL.Facturacion.BD::FAC.TareaDTE" AS TAR
ORDER BY TAR."IdTareaDTE" DESC;

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarTareaDTE" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


