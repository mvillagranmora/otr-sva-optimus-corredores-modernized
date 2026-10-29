SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoSubyacente"; 

CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoSubyacente"
	
	AS Select 
	iSub."IdInstrumentoSubyacente" as "IdInstrumentoSubyacente"
	,cla."IdClasificacionTipoInstrumento"
	,iSub."IdTipoInstrumento" as "IdTipoInstrumento"
	,cti."Nombre" as "NombreTipoInstrumento"
	,iSub."IdBolsa" as "IdBolsa"
	,bolsa."Nombre" as "NombreBolsa"
	,iSub."IdTipoInstrumentoSubyacente" as "IdTipoInstrumentoSubyacente"
	,ctis."Nombre" as "Nombre"
	,iSub."IdInstrumento" as "IdInstrumento"
	,inst."Nemotecnico" as "Nemotecnico"
	,inst."ValorClasificacionRiesgo"
	,inst."FechaVencimiento" AS "FechaVencimientoInstrumentoSubyacente"
from "BSCL.Plataforma.BD::INS.InstrumentoSubyacente" as iSub	
	inner join "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" bti ON bti."IdTipoInstrumento" = iSub."IdTipoInstrumento" and bti."IdBolsa" = iSub."IdBolsa" and bti."IdEstado" = 'ACT' 
	inner join "BSCL.Plataforma.BD::INS.TipoInstrumento" cti  on cti."IdTipoInstrumento" = iSub."IdTipoInstrumento"
	inner join "BSCL.Plataforma.BD::INS.Bolsa" bolsa on bolsa."IdBolsa" = iSub."IdBolsa"
	inner join "BSCL.Plataforma.BD::INS.TipoInstrumento" ctis  on ctis."IdTipoInstrumento" = iSub."IdTipoInstrumentoSubyacente"
	inner join "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" cla  on cla."IdClasificacionTipoInstrumento" = ctis."IdClasificacionTipoInstrumento"
	inner join "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" inst on inst."IdInstrumento" = iSub."IdInstrumento"
WITH READ ONLY;
     
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoSubyacente" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
