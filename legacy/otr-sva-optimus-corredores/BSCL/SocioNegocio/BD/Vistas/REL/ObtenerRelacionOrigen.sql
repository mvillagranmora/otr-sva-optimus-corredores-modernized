SET SCHEMA "BSCL";
 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ObtenerRelacionOrigen";
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ObtenerRelacionOrigen"
AS
--ORIGEN RELACIONADOS
SELECT
  PER."IdPersona"
 ,REL."IdPersonaDestino"
 ,REL."IdTipoRelacion" 
 ,'RELACIONADOS' AS "Administracion"
 ,REL."FechaInicioRelacion"
 ,REL."FechaFinRelacion"
 ,REL."IdRelacionPersona"
 ,REL."IdEstado"
 ,'' AS "CodigoTipoCargoEmpleado"
 ,REL."PorcentajeParticipacion"
 ,REL."FinalidadRelacion"
 ,REL."CondicionesRelacion"
 ,REL."IdGradoVinculacionParental"
 ,REL."FechaModificacion"
FROM "BSCL.SocioNegocio.BD::REL.RelacionPersona" REL 
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON  (PER."IdPersona"= REL."IdPersonaOrigen")

	UNION ALL
	
--ORIGEN PERSONA
---Persona Natural / Conyuge
	SELECT PER."IdPersona", PER."IdPersonaConyuge" AS "IdPersonaDestino",'CY' AS "IdTipoRelacion" ,'PERSONA' AS "Administracion", PER."FechaInicioConyuge" AS "FechaInicioRelacion",'0001-01-01 00:00:00' AS "FechaFinRelacion", 0 AS "IdRelacionPersona",'ACT' AS "IdEstado"    
			,'' AS "CodigoTipoCargoEmpleado" ,0 as "PorcentajeParticipacion", '' as  "FinalidadRelacion",'' as "CondicionesRelacion", 0 as "IdGradoVinculacionParental",'0001-01-01 00:00:00' AS "FechaModificacion"
		FROM "BSCL.SocioNegocio.BD::PER.Persona" PER 
			WHERE PER."EstadoCivil"='CA' AND PER."Tipo"='N' AND PER."IdPersonaConyuge" >0
	
	UNION ALL
	
--Persona / Empleado
	 SELECT PER."IdPersona", PER."IdPersonaEmpleador" AS "IdPersonaDestino",'PEMDOR' AS "IdTipoRelacion"  ,'PERSONA' AS "Administracion",PER."FechaInicioActividad" AS "FechaInicioRelacion",'0001-01-01 00:00:00' AS "FechaFinRelacion", 0 AS "IdRelacionPersona",'ACT' AS "IdEstado"       
			,PER."CodigoTipoCargoEmpleado",0 as "PorcentajeParticipacion", '' as  "FinalidadRelacion",'' as "CondicionesRelacion", 0 as "IdGradoVinculacionParental",'0001-01-01 00:00:00' AS "FechaModificacion"
				FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
				WHERE PER."IdPersonaEmpleador" >0 AND PER."CodigoTipoCargoEmpleado" <>'' --AND CPER."IdPersonaEmpleador" <> V_IdPersonaCorredor;

	UNION ALL
		
---Persona / Representa Legal 
	  SELECT PER."IdPersona", PER."IdPersonaRepresentante" AS "IdPersonaDestino",'REP' AS "IdTipoRelacion"  ,'PERSONA' AS "Administracion",PER."FechaInicioRepresentante" AS "FechaInicioRelacion",'0001-01-01 00:00:00' AS "FechaFinRelacion", 0 AS "IdRelacionPersona",'ACT' AS "IdEstado"       
	  		,'' AS "CodigoTipoCargoEmpleado",0 as "PorcentajeParticipacion", '' as  "FinalidadRelacion",'' as "CondicionesRelacion", 0 as "IdGradoVinculacionParental",'0001-01-01 00:00:00' AS "FechaModificacion"
			FROM "BSCL.SocioNegocio.BD::PER.Persona" PER 
			WHERE PER."Tipo"='J' AND PER."IdPersonaRepresentante" >0	
			
	UNION ALL
	
---ORIGEN EMPLEADO
-- Empleador / (Corredora)
	SELECT PER."IdPersona", CEMP."IdPersona" AS "IdPersonaDestino",CEMP."CodigoTipoCargoEmpleado" AS "IdTipoRelacion"  ,'EMPLEADO' AS "Administracion",CEMP."FechaInicioCargo" AS "FechaInicioRelacion",CEMP."FechaTerminoCargo" AS "FechaFinRelacion", 0 AS "IdRelacionPersona",CEMP."IdEstado" 
		,'' AS "CodigoTipoCargoEmpleado",0 as "PorcentajeParticipacion", '' as  "FinalidadRelacion",'' as "CondicionesRelacion", 0 as "IdGradoVinculacionParental",'0001-01-01 00:00:00' AS "FechaModificacion"
		FROM "BSCL.SocioNegocio.BD::EMP.Empleado" CEMP
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Empresa" COR ON (COR."IdEmpresa"=CEMP."IdEmpresa" AND COR."IdEstado"='ACT')
		LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON COR."IdSocioDeNegocio" = SOC."IdSocioDeNegocio"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON SOC."IdPersona" = PER."IdPersona"

	UNION ALL	
	
---ORIGEN AUTORIZADO	
	SELECT  SOC."IdPersona", AUT."IdPersona" AS "IdPersonaDestino",	AUT."IdTipoRelacion" AS "IdTipoRelacion" ,'AUTORIZADO' AS "Administracion",MIN(AUT."FechaIngreso") AS  "FechaInicioRelacion",'0001-01-01 00:00:00' AS "FechaFinRelacion", 0 AS "IdRelacionPersona", MIN(AUT."IdEstado") 
		,'' AS "CodigoTipoCargoEmpleado",0 as "PorcentajeParticipacion", '' as  "FinalidadRelacion",'' as "CondicionesRelacion", 0 as "IdGradoVinculacionParental",'0001-01-01 00:00:00' AS "FechaModificacion"
	FROM (SELECT   
		"AUT"."IdPortafolio",
	   	"AUT"."IdPersona",
		"TPO"."IdTipoRelacion",
	   	CASE 
	   	   WHEN COALESCE(CR."IdEstado",'')='' OR CR."IdEstado" = 'ACT' THEN "AUT"."IdEstado" 
	   	   ELSE CR."IdEstado"
	   	END AS "IdEstado",
		"AUT"."FechaIngreso"
		FROM "BSCL.Portafolio.BD::POR.Autorizado" AS "AUT"
		LEFT JOIN "BSCL.Portafolio.BD::POR.TipoAutorizado" AS "TPO" ON "TPO"."IdTipoAutorizado" = "AUT"."IdTipoAutorizado"
		LEFT JOIN (
			SELECT MAX("IdEstado") AS "IdEstado", MAX("NombreEstado") AS "NombreEstado", "IdPersonaDestino", "IdTipoRelacion" 
			FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacion"  
			WHERE "IdEstado" != 'VCD' GROUP BY "IdPersonaDestino", "IdTipoRelacion"
		) AS CR ON "AUT"."IdPersona" = CR."IdPersonaDestino" AND "TPO"."IdTipoRelacion" = CR."IdTipoRelacion") AUT
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacion"	  REL ON  (AUT."IdTipoRelacion"=REL."IdTipoRelacion")	---Segun tipo relacion (agrupador de tipo autorizado)
		INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON  (POR."IdPortafolio"= AUT."IdPortafolio")
		INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON (POR."IdCliente"= SOC."IdCliente")
		INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON (PER."IdPersona"= SOC."IdPersona")
		WHERE PER."Tipo"='N' AND REL."Administracion"='AUTORIZADO'
	GROUP BY SOC."IdPersona", AUT."IdPersona",	AUT."IdTipoRelacion";
			
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::REL.ObtenerRelacionOrigen" TO  BCS_DESARROLLADOR WITH GRANT OPTION;