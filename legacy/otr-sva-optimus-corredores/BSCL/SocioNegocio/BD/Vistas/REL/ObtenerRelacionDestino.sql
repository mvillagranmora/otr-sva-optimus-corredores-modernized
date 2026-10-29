SET SCHEMA "BSCL";

 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ObtenerRelacionDestino";
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ObtenerRelacionDestino"
AS

---ORIGEN RELACIONADOS : LECTURA DE VUELTA A LA RELACION

    	SELECT  REL."IdPersonaDestino" AS "IdPersona",REL."IdPersona" AS "IdPersonaDestino"	,(CASE WHEN TREL."IdTipoRelacionDestino"='' THEN TREL."IdTipoRelacion" ELSE TREL."IdTipoRelacionDestino" END ) AS "IdTipoRelacion"
			,'RELACIONADOS_V' AS "Administracion",REL."FechaInicioRelacion"	,REL."FechaFinRelacion",REL."IdRelacionPersona"	,REL."IdEstado" 
			,REL."PorcentajeParticipacion",REL."FinalidadRelacion",REL."CondicionesRelacion",REL."IdGradoVinculacionParental", REL."FechaModificacion"
			FROM "BSCL.SocioNegocio.BD.Vistas::REL.ObtenerRelacionOrigen" REL 
			INNER JOIN "BSCL.SocioNegocio.BD::REL.TipoRelacion" AS "TREL" ON  (TREL."IdTipoRelacion"= REL."IdTipoRelacion")
			LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::REL.ObtenerRelacionOrigen" ORI 
			  ON  (ORI."IdPersona"  =REL."IdPersonaDestino" 
				 AND ORI."IdPersonaDestino"  =REL."IdPersona"
				 AND ORI."IdTipoRelacion"    =TREL."IdTipoRelacionDestino" AND ORI."Administracion" ='RELACIONADOS')				
			WHERE ORI."IdPersona" IS NULL AND REL."Administracion" ='RELACIONADOS'
			
			
	UNION ALL	
			
---ORIGEN PERSONA : LECTURA DE VUELTA A LA RELACION
---Persona Natural / Conyuge

  SELECT PER."IdPersonaDestino" AS "IdPersona", PER."IdPersona" AS "IdPersonaDestino",'CY' AS "IdTipoRelacion" ,'PERSONA_V' AS "Administracion",PER."FechaInicioRelacion" AS "FechaInicioRelacion",'0001-01-01 00:00:00' AS "FechaFinRelacion", 0 AS "IdRelacionPersona",'ACT' AS "IdEstado"        
			,0 as "PorcentajeParticipacion", '' as  "FinalidadRelacion",'' as "CondicionesRelacion", 0 as "IdGradoVinculacionParental", PER."FechaModificacion"
			FROM "BSCL.SocioNegocio.BD.Vistas::REL.ObtenerRelacionOrigen" PER
				LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::REL.ObtenerRelacionOrigen" ORI
				 		ON (ORI."IdPersona"  =PER."IdPersonaDestino" AND ORI."IdPersonaDestino" =PER."IdPersona"
						    AND ORI."IdTipoRelacion"   ='CY' AND ORI."Administracion" ='PERSONA')
				WHERE PER."IdTipoRelacion" ='CY' AND ORI."IdPersona" IS NULL AND PER."Administracion" ='PERSONA'
	
	UNION ALL
--Persona / Empleado ---> Empleador

	SELECT PER."IdPersonaDestino" AS "IdPersona", PER."IdPersona" AS "IdPersonaDestino",PER."CodigoTipoCargoEmpleado" AS "IdTipoRelacion" ,'PERSONA_V' AS "Administracion",PER."FechaInicioRelacion" AS "FechaInicioRelacion",'0001-01-01 00:00:00' AS "FechaFinRelacion", 0 AS "IdRelacionPersona",'ACT' AS "IdEstado"        
			,0 as "PorcentajeParticipacion", '' as  "FinalidadRelacion",'' as "CondicionesRelacion", 0 as "IdGradoVinculacionParental", PER."FechaModificacion"
			FROM "BSCL.SocioNegocio.BD.Vistas::REL.ObtenerRelacionOrigen" PER
			LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::REL.ObtenerRelacionOrigen" ORI
					ON (ORI."IdPersona"  =PER."IdPersonaDestino" AND ORI."IdPersonaDestino" =PER."IdPersona"
						    AND ORI."IdTipoRelacion"   = PER."CodigoTipoCargoEmpleado" AND ORI."Administracion" ='PERSONA')
			WHERE  PER."IdTipoRelacion" ='PEMDOR' AND PER."CodigoTipoCargoEmpleado" <>'' AND ORI."IdPersona" IS NULL AND PER."Administracion" ='PERSONA'	
			
	UNION ALL	
---Persona / Representa Legal 

	SELECT PER."IdPersonaDestino" AS "IdPersona", PER."IdPersona" AS "IdPersonaDestino",'REP' AS "IdTipoRelacion" ,'PERSONA_V' AS "Administracion",PER."FechaInicioRelacion" AS "FechaInicioRelacion",'0001-01-01 00:00:00' AS "FechaFinRelacion", 0 AS "IdRelacionPersona",'ACT' AS "IdEstado"        
			,0 as "PorcentajeParticipacion", '' as  "FinalidadRelacion",'' as "CondicionesRelacion", 0 as "IdGradoVinculacionParental", PER."FechaModificacion"
			FROM "BSCL.SocioNegocio.BD.Vistas::REL.ObtenerRelacionOrigen" PER
			LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::REL.ObtenerRelacionOrigen" ORI
			   		ON (ORI."IdPersona"  =PER."IdPersonaDestino" AND ORI."IdPersonaDestino" =PER."IdPersona"
						    AND ORI."IdTipoRelacion"   ='REP' AND ORI."Administracion" ='PERSONA')
			WHERE PER."IdTipoRelacion" ='REP' AND ORI."IdPersona" IS NULL AND PER."Administracion" ='PERSONA'					
			
	UNION ALL	
---ORIGEN EMPLEADO : LECTURA DE VUELTA A LA RELACION
-- Empleador / (Corredora)

	SELECT CEMP."IdPersona", COR."IdPersona" AS "IdPersonaDestino",'CEMDOR' AS "IdTipoRelacion"  ,'EMPLEADO_V' AS "Administracion",CEMP."FechaInicioCargo" AS "FechaInicioRelacion",CEMP."FechaTerminoCargo" AS "FechaFinRelacion", 0 AS "IdRelacionPersona",CEMP."IdEstado"  
		,0 as "PorcentajeParticipacion", '' as  "FinalidadRelacion",'' as "CondicionesRelacion", 0 as "IdGradoVinculacionParental",'0001-01-01 00:00:00' AS "FechaModificacion"
		FROM "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CEMP  
		INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" COR ON (COR."IdEmpresa"=CEMP."IdEmpresa" AND COR."IdEstado"='ACT')	
		
	UNION ALL			
---ORIGEN AUTORIZADO : LECTURA DE VUELTA A LA RELACION						
			
	SELECT   AUT."IdPersona", SOC."IdPersona" AS "IdPersonaDestino",REL."IdTipoRelacionDestino" AS "IdTipoRelacion" ,'AUTORIZADO_V' AS "Administracion",MIN(AUT."FechaIngreso") AS  "FechaInicioRelacion",'0001-01-01 00:00:00' AS "FechaFinRelacion", 0 AS "IdRelacionPersona",MIN(AUT."IdEstado")   
		,0 as "PorcentajeParticipacion", '' as  "FinalidadRelacion",'' as "CondicionesRelacion", 0 as "IdGradoVinculacionParental",'0001-01-01 00:00:00' AS "FechaModificacion"
		FROM "BSCL.Portafolio.BD.Vistas::ConsultarAutorizadoOrden" 		      AUT
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacion"	  REL ON  (AUT."IdTipoRelacion"=REL."IdTipoRelacion")	---Segun tipo relacion (agrupador de tipo autorizado)
		INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona"						  PER ON  (PER."IdPersona"= AUT."IdPersona")
		INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 		  POR ON  (POR."IdPortafolio"= AUT."IdPortafolio")
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" SOC ON  (POR."IdCliente"= SOC."IdCliente")
		WHERE PER."Tipo"='N'  AND REL."Administracion"='AUTORIZADO'
	GROUP BY AUT."IdPersona",SOC."IdPersona",REL."IdTipoRelacionDestino";				
			
			
			
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::REL.ObtenerRelacionDestino" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
