SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoInstrumentoSubyacente"; 

CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoInstrumentoSubyacente"(
	"IdInstrumentoSubyacente"
	,"IdTipoInstrumento"
	,"NombreTipoInstrumento"
	,"IdBolsa"
    ,"NombreBolsa"
    ,"IdTipoInstrumentoSubyacente"
	,"NombreTipoInstrumentoSubyacente"
	,"IdEstadoSubyacente"
	,"NombreEstadoSubyacente"
	,"NombreLargoTipoInstrumento"
	,"IdClasificacionTipoInstrumento"
	,"IdClasificacionTipoInstrumentoSubyacente"
	,"ExigeGarantia"
	) AS Select 
	tis."IdInstrumentoSubyacente"
	,CASE WHEN bti."IdTipoInstrumento" = 'IOA' THEN bti."IdTipoInstrumento" ELSE tis."IdTipoInstrumento" END "IdTipoInstrumento"
	,CASE WHEN bti."IdTipoInstrumento" IN ('FDO','FIP','FBC','FIC','FUF','IOE','IOI','IOD','IOB','IOC','IOU','IOM','IOF','FBO') THEN tis."Nemotecnico" ELSE  cti."Nombre" END AS "Nombre"
	,tis."IdBolsa" --as "CodigoBolsa"
    ,bolsa."Nombre" as "NombreBolsa"
    ,tis."IdTipoInstrumentoSubyacente"
	,ctis."Nombre" as "NombreTipoInstrumentoSubyacente"
	,tis."IdEstado" as "IdEstadoSubyacente"
	,est."Nombre" as "NombreEstadoSubyacente"
	,CASE WHEN bti."IdTipoInstrumento" IN ('FDO','FIP','FBC','FIC','FUF','IOE','IOI','IOD','IOB','IOC','IOU','IOM','IOF','FBO') THEN tis."IdTipoInstrumento" || ' - ' || tis."Nemotecnico" ELSE  cti."IdTipoInstrumento" || ' - ' || cti."Nombre" END AS "NombreLargoTipoInstrumento"
	,cti."IdClasificacionTipoInstrumento" 	AS "IdClasificacionTipoInstrumento" 
	,ctis."IdClasificacionTipoInstrumento" 	AS "IdClasificacionTipoInstrumentoSubyacente"
	,bti."ExigeGarantia"
from "BSCL.Plataforma.BD::INS.TipoInstrumentoSubyacente" as tis	
	inner join "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" bti ON (bti."IdTipoInstrumento" = tis."IdTipoInstrumento" 
		OR (tis."IdTipoInstrumento" = 'DER' AND bti."IdTipoInstrumento" IN ('FDO','FIP','FBC','FIC','FUF','IOA','IOE','IOI','IOD','IOB','IOC','IOU','IOM','IOF','FBO')) and bti."IdBolsa" = tis."IdBolsa" and bti."IdEstado" = 'ACT' and tis."IdEstado" = 'ACT')
	inner join "BSCL.Plataforma.BD::INS.TipoInstrumento" cti  on cti."IdTipoInstrumento" = bti."IdTipoInstrumento" and cti."IdEstado" ='VIG'--and cti."IdBolsa" = tis."IdBolsa" 
	inner join "BSCL.Plataforma.BD::INS.TipoInstrumento" ctis  on ctis."IdTipoInstrumento" = tis."IdTipoInstrumentoSubyacente" and ctis."IdEstado" ='VIG'--and ctis."IdBolsa" = tis."IdBolsa" 
	inner join "BSCL.Plataforma.BD::INS.Bolsa" bolsa on bolsa."IdBolsa" = bti."IdBolsa" and Bolsa."IdEstado" = 'ACT' 
	inner join "BSCL.Plataforma.BD::PLA.Estado" est on est."IdEstado" = bti."IdEstado"
		
WITH READ ONLY;
     
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoInstrumentoSubyacente" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
     
     
     
