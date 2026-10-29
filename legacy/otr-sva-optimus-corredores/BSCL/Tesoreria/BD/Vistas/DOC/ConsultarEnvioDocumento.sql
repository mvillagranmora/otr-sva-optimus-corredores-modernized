SET SCHEMA "BSCL";

DROP VIEW "BSCL.Tesoreria.BD.Vistas::DOC.ConsultarEnvioDocumento";

CREATE VIEW "BSCL.Tesoreria.BD.Vistas::DOC.ConsultarEnvioDocumento" AS

SELECT
	 ED."IdEnvioDocumento"
	,ED."IdCaja"
	,CAJ."Codigo" AS "CodigoCaja"	
	,ED."FechaHoraEnvio"
	,CAST(ED."FechaHoraEnvio" AS DATE) AS "FechaEnvio"
	,ED."IdEmpresa"
	,ED."IdUsuario"
	,USU."CodigoUsuario"
	,ED."IdEstado"
	,EST."Nombre" AS "NombreEstado"
	,REDD."CantidadDocumentos"  -- Cantidad total de documentos por envio
	,REDD."MontoEnvio"          -- Monto total por envio
	,ED."IdMedioDePago"
	,MDP."Nombre" AS "NombreMedioDePago"
	,ED."IdMoneda"

FROM "BSCL.Tesoreria.BD::TES.EnvioDocumento" AS ED
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS USU ON (USU."IdUsuario" = ED."IdUsuario")
LEFT JOIN "BSCL.Tesoreria.BD::TES.Caja" AS CAJ ON (CAJ."IdCaja" = ED."IdCaja")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EST ON (EST."IdEstado" =  ED."IdEstado")
LEFT JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" AS MDP ON (MDP."IdMedioDePago" = ED."IdMedioDePago")
LEFT JOIN (SELECT EDD."IdEnvioDocumento", COUNT("IdEnvioDocumento") AS "CantidadDocumentos", SUM("Monto") AS "MontoEnvio"
		   FROM "BSCL.Tesoreria.BD::TES.EnvioDocumentoDetalle" AS EDD
		   INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP ON (CFP."IdComprobanteFormaDePago" = EDD."IdComprobanteFormaDePago")		   		   
		   GROUP BY "IdEnvioDocumento") AS REDD ON REDD."IdEnvioDocumento" = ED."IdEnvioDocumento"		
WITH READ ONLY;

GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::DOC.ConsultarEnvioDocumento" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
