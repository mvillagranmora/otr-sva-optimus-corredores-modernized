
SELECT * FROM "BSCL.Plataforma.BD::BAT.Programa" WHERE "IdTipoPrograma" = 'DTEENV' ORDER BY "IdPrograma" desc

--HANA MBI
--NOOK
SELECT "FechaSistema", "IdComprobante", "IdRespuestaDTE", "IdEstadoComprobante", "IdEstadoDTE", "IdEstadoSII", "RespuestaDTEEnvioSII", "RespuestaXmlDTE",  "RespuestaLocationDTE", *  
FROM "BSCL.Facturacion.BD::FAC.Comprobante" WHERE "FechaSistema" >= '2022-04-01' 
AND "IdEstadoComprobante" NOT IN ('ANU','FAC') AND "IdEstadoSII" = 'PEN'
ORDER BY 1 

--DTE CONNECT (.202)

select CONCAT(CONCAT(CONCAT(CONCAT('UPDATE "BSCL.Facturacion.BD::FAC.Comprobante" SET "IdRespuestaDTE" = ',id),' WHERE "IdComprobante" = '),clave_origen),' AND "IdRespuestaDTE" = 0;'), id, clave_origen, estado_sii,* 
from documentoeditable where clave_origen in ('373635', '373634') 
order by 2

--HANA MBI

UPDATE "BSCL.Facturacion.BD::FAC.Comprobante" SET "IdRespuestaDTE" = 6841285 WHERE "IdComprobante" = 373634 AND "IdRespuestaDTE" = 0;
UPDATE "BSCL.Facturacion.BD::FAC.Comprobante" SET "IdRespuestaDTE" = 6841326 WHERE "IdComprobante" = 373635 AND "IdRespuestaDTE" = 0;

UPDATE "BSCL.Facturacion.BD::FAC.Comprobante" SET  
"IdEstadoDTE" = 'REC', 
"RespuestaXmlDTE" = CONCAT('https://mbidte.bcs.cl:6280/files/xml/',"IdRespuestaDTE"), 
"RespuestaPdfDTE" = CONCAT('https://mbidte.bcs.cl:6280/files/pdf/',"IdRespuestaDTE"), 
"RespuestaLocationDTE" = CONCAT('/api/documentoEditables/',"IdRespuestaDTE") 
WHERE "FechaSistema" > '2022-03-17' AND "IdEstadoComprobante" NOT IN ('ANU','FAC') 
AND "IdComprobante" IN (369650,369652) 
AND "RespuestaXmlDTE" = '' AND "IdRespuestaDTE" <> 0;