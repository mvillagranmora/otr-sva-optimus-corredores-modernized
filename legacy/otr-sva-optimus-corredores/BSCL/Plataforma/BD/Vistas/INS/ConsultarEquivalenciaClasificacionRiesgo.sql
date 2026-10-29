SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarEquivalenciaClasificacionRiesgo"; 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarEquivalenciaClasificacionRiesgo"
AS

SELECT 
   EQU."IdEquivalenciaClasificacion",
   CLOR."IdClasificacionRiesgo"			AS "IdClasificacionRiesgoOrigen",
   CLOR."IdTipoClasificacionRiesgo"		AS "IdTipoClasificacionRiesgoOrigen",
   CLOR."IdEntidadClasificadoraRiesgo"	AS "IdEntidadClasificadoraRiesgoOrigen",
   CLOR."ClasificacionRiesgo"			AS "ClasificacionRiesgoOrigen",
   CLOR."Nivel"							AS "NivelOrigen",
   CLDE."IdClasificacionRiesgo"			AS "IdClasificacionRiesgoDestino",
   CLDE."IdTipoClasificacionRiesgo"		AS "IdTipoClasificacionRiesgoDestino",
   CLDE."IdEntidadClasificadoraRiesgo"	AS "IdEntidadClasificadoraRiesgoDestino",
   CLDE."ClasificacionRiesgo"			AS "ClasificacionRiesgoDestino",
   CLDE."Nivel"							AS "NivelDestino"
 FROM  
 "BSCL.Plataforma.BD::INS.EquivalenciaClasificacion" EQU
 INNER JOIN "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" CLOR ON (EQU."IdClasificacionRiesgoOrigen" =CLOR."IdClasificacionRiesgo"
 																	 AND CLOR."IdEstado" ='ACT'  )
 INNER JOIN "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" CLDE ON (EQU."IdClasificacionRiesgoDestino"=CLDE."IdClasificacionRiesgo" 
  																	 AND CLDE."IdEstado" ='ACT'  )
  																	 
UNION ALL   																	 
SELECT DISTINCT 
   0 as "IdEquivalenciaClasificacion",
   CLOR."IdClasificacionRiesgo"			AS "IdClasificacionRiesgoOrigen",
   CLOR."IdTipoClasificacionRiesgo"		AS "IdTipoClasificacionRiesgoOrigen",
   CLOR."IdEntidadClasificadoraRiesgo"	AS "IdEntidadClasificadoraRiesgoOrigen",
   CLOR."ClasificacionRiesgo"			AS "ClasificacionRiesgoOrigen",
   CLOR."Nivel"							AS "NivelOrigen",
   CLOR."IdClasificacionRiesgo"			AS "IdClasificacionRiesgoDestino",
   CLOR."IdTipoClasificacionRiesgo"		AS "IdTipoClasificacionRiesgoDestino",
   CLOR."IdEntidadClasificadoraRiesgo"	AS "IdEntidadClasificadoraRiesgoDestino",
   CLOR."ClasificacionRiesgo"			AS "ClasificacionRiesgoDestino",
   CLOR."Nivel"							AS "NivelDestino"
 FROM  
 "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" CLOR 
	WHERE CLOR."IdEstado" ='ACT';
 																	 
 																	 

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarEquivalenciaClasificacionRiesgo" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
