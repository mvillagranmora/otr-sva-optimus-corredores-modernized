SET SCHEMA "BSCL"; 
 -- 
DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarRelacionInstrumento"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarRelacionInstrumento" AS
	SELECT 	
		REL."IdRelacionInstrumento" 					as "IdRelacionInstrumento",
		TIP."IdTipoRelacionInstrumento"					as "IdTipoRelacionInstrumento",
		TIP."TipoRelacionInstrumento"					as "TipoRelacionInstrumento",
		REL."IdClasificacionTipoInstrumento"			as "IdClasificacionTipoInstrumento",
		REL."IdTipoInstrumento"							as "IdTipoInstrumento",
		REL."IdInstrumento"								as "IdInstrumento",
		REL."Nemo"										as "Nemo",
		REL."IdClasificacionTipoInstrumentoRelacionado"	as "IdClasificacionTipoInstrumentoRelacionado",
		REL."IdTipoInstrumentoRelacionado"				as "IdTipoInstrumentoRelacionado",
		REL."IdInstrumentoRelacionado"					as "IdInstrumentoRelacionado",
		REL."NemoRelacionado"							as "NemoRelacionado",
		REL."Descripcion"								as "Descripcion",
		REL."FechaInicioRelacion"						as "FechaInicioRelacion",
		REL."FechaFinRelacion"							as "FechaFinRelacion",
		REL."IdUsuario"									as "IdUsuario",
		REL."IdEstado"									as "IdEstado"
	FROM "BSCL.Plataforma.BD::INS.RelacionInstrumento" 		REL
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoRelacionInstrumento"	TIP
	ON (REL."IdTipoRelacionInstrumento" = TIP."IdTipoRelacionInstrumento");
			
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarRelacionInstrumento" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

