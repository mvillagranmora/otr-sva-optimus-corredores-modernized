SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::LIM.ConsultarNivelClasificacionRiesgoEmisor"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::LIM.ConsultarNivelClasificacionRiesgoEmisor"  
AS
-- 
--  Proposito: Permite obtener el Nivel de Riesgo a Evaluar del intrumento (Origen) segun Rango y Entidad de Clasificacion
--  Equivalente a la Escala del Limite (Destino)
--  Autor: CFLORES - BCS
--  Fecha Creacion: 13-05-2016

SELECT
 IFNULL(val1."IdEmisor",val2."IdEmisor") 	as "IdEmisor" ,
 IFNULL(val1."IdEntidadClasificadoraRiesgo",val2."IdEntidadClasificadoraRiesgo") 	as "IdEntidadClasificadoraRiesgoOrigen" ,
 IFNULL(val1."IdTipoClasificacionRiesgoOrigen",val2."IdTipoClasificacionRiesgoOrigen") 	as "IdTipoClasificacionRiesgoOrigen" ,
 IFNULL(val1."ValorClasificacionRiesgo",val2."ValorClasificacionRiesgo") 	as "ValorClasificacionRiesgoOrigen" ,
 val1."NivelOrigen" AS "NivelOrigen1",
 val2."NivelOrigen" AS "NivelOrigen2",
 IFNULL(val1."IdEntidadClasificadoraRiesgoDestino",val2."IdEntidadClasificadoraRiesgoDestino") 	as "IdEntidadClasificadoraRiesgoDestino" ,
 IFNULL(val1."IdTipoClasificacionRiesgoDestino",val2."IdTipoClasificacionRiesgoDestino") 	as "IdTipoClasificacionRiesgoDestino" ,
 val1."NivelDestino" AS "NivelDestino1",
 val2."NivelDestino" AS "NivelDestino2",
(
CASE WHEN 
(CASE WHEN COALESCE(val1."NivelDestino",0) < COALESCE(val2."NivelDestino",0) THEN COALESCE(val1."NivelDestino",0) ELSE COALESCE(val2."NivelDestino",0) END ) 
> 0 THEN (CASE WHEN COALESCE(val1."NivelDestino",0) < COALESCE(val2."NivelDestino",0) THEN COALESCE(val1."NivelDestino",0) ELSE COALESCE(val2."NivelDestino",0) END ) 
ELSE COALESCE(val1."NivelDestino",0) + COALESCE(val2."NivelDestino",0)
END
)AS "NivelDeEvaluacion"
FROM		 
		 
		 
	(
		( SELECT   
		  EMI."IdEmisor" ,
		  EMI."ValorClasificacionRiesgo",
      	  CLS."IdClasificacionRiesgoOrigen",
     	  CLS."IdTipoClasificacionRiesgoOrigen",
		  EMI."IdEntidadClasificadoraRiesgo",		
  		  CLS."ClasificacionRiesgoOrigen",
    	  CLS."NivelOrigen",
    	  CLS."IdClasificacionRiesgoDestino",
		  CLS."IdTipoClasificacionRiesgoDestino",
		  CLS."IdEntidadClasificadoraRiesgoDestino",
		  CLS."ClasificacionRiesgoDestino",
		  CLS."NivelDestino"
		 FROM "BSCL.Plataforma.BD.Vistas::LIM.ConsultarTipoClasificacionRiesgoEmisor" EMI
		 INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarEquivalenciaClasificacionRiesgo" CLS
		 ON (CLS."ClasificacionRiesgoOrigen"=EMI."Valor1" AND CLS."IdEntidadClasificadoraRiesgoOrigen"=EMI."IdEntidadClasificadoraRiesgo" AND CLS."IdTipoClasificacionRiesgoOrigen"=EMI."IdTipoClasificacionRiesgo" AND CLS."IdTipoClasificacionRiesgoDestino"=EMI."IdTipoClasificacionRiesgo" )
		 )val1
FULL JOIN
		(
		 SELECT 
  	   	  EMI."IdEmisor" ,
		  EMI."ValorClasificacionRiesgo",
      	  CLS."IdClasificacionRiesgoOrigen",
     	  CLS."IdTipoClasificacionRiesgoOrigen",
		  EMI."IdEntidadClasificadoraRiesgo",		
  		  CLS."ClasificacionRiesgoOrigen",
    	  CLS."NivelOrigen",
    	  CLS."IdClasificacionRiesgoDestino",
		  CLS."IdTipoClasificacionRiesgoDestino",
		  CLS."IdEntidadClasificadoraRiesgoDestino",
		  CLS."ClasificacionRiesgoDestino",
		  CLS."NivelDestino"
		 FROM "BSCL.Plataforma.BD.Vistas::LIM.ConsultarTipoClasificacionRiesgoEmisor" EMI
		 INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarEquivalenciaClasificacionRiesgo" CLS
		 ON (CLS."ClasificacionRiesgoOrigen"=EMI."Valor2" AND CLS."IdEntidadClasificadoraRiesgoOrigen"=EMI."IdEntidadClasificadoraRiesgo" AND CLS."IdTipoClasificacionRiesgoOrigen"=EMI."IdTipoClasificacionRiesgo" AND CLS."IdTipoClasificacionRiesgoDestino"=EMI."IdTipoClasificacionRiesgo" )

		 )val2
		ON  	(val1."IdEmisor" = val2."IdEmisor" AND val1."IdEntidadClasificadoraRiesgoDestino" = val2."IdEntidadClasificadoraRiesgoDestino")
	
	);
	

  	
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::LIM.ConsultarNivelClasificacionRiesgoEmisor" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
