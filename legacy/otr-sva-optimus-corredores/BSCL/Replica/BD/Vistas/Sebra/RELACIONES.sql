SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.RELACIONES"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.RELACIONES" AS

	SELECT  
		"PER_ID_PER", 
		"PER_ID_REL", 
		"PAR_TRE_ID", 
			
		min("REL_FEC_INICIO") AS "REL_FEC_INICIO", 
		max("REL_FEC_TERMINO") AS "REL_FEC_TERMINO",
		max("REL_VIGENCIA") AS "REL_VIGENCIA",
		'DESCONOCID' AS "REL_FATCA", 
		'0' AS "REL_IND_BENEF", 
		'' AS "REL_RUT_CONYUGE",
		'' AS "REL_NOMBRE_CONYUGE", 
		'' AS "REL_PATERNO_CONYUGE",
		'' AS "REL_MATERNO_CONYUGE",
		'' AS "TIPO_REPRESENTANTE", 
		CURRENT_TIMESTAMP AS "REL_FEC_MODIFICA"
		
	FROM (
		SELECT 
			"IdStack" AS "IDSTACK",ACTION,
		
			CAST(
					CASE 
						WHEN instr(DOCPerCli."DocumentoIdentidad" , '-') = 0 THEN -1 
						ELSE trim(LEFT(DOCPerCli."DocumentoIdentidad"  , instr(DOCPerCli."DocumentoIdentidad" , '-')-1)) END AS INT)  AS "PER_ID_PER", --[per_id_per] [int] NOT NULL,
						
			CAST(
					CASE 
						WHEN instr(DOCRel."DocumentoIdentidad" , '-') = 0 THEN -1 
						ELSE trim(LEFT(DOCRel."DocumentoIdentidad"  , instr(DOCRel."DocumentoIdentidad" , '-')-1)) END AS INT)  AS "PER_ID_REL", --[per_id_rel] [int] NOT NULL,
						
			
			CAST(COALESCE( "ValorOrigen" ,"IdTipoRelacion" )   AS VARCHAR(10)) AS "PAR_TRE_ID", --[par_tre_id] [varchar](10) NOT NULL,
			
			CASE WHEN "FechaInicioRelacion" < '1900-01-01' THEN '1900-01-01' ELSE  "FechaInicioRelacion" end 	 AS "REL_FEC_INICIO", --[rel_fec_inicio] [datetime] NOT NULL,
			CASE WHEN "FechaFinRelacion" < '1900-01-01' THEN '1900-01-01' ELSE  "FechaFinRelacion"	END 	 AS "REL_FEC_TERMINO", --[rel_fec_termino] [datetime] NOT NULL,
			
			CASE WHEN tb1."IdEstado" = 'ACT' THEN 'S' ELSE 'N' end AS "REL_VIGENCIA", --[rel_vigencia] [char](1) NOT NULL,
			CAST('DESCONOCID' AS VARCHAR(50)) AS "REL_FATCA", --[rel_fatca] [varchar](50) NULL,
			CAST('0' AS VARCHAR(10)) AS "REL_IND_BENEF", --[rel_ind_benef] [char](10) NULL,
			CAST('' AS VARCHAR(20)) AS "REL_RUT_CONYUGE", --[rel_rut_conyuge] [varchar](20) NOT NULL,
			CAST('' AS VARCHAR(30)) AS "REL_NOMBRE_CONYUGE", --[rel_nombre_conyuge] [varchar](30) NOT NULL,
			CAST('' AS VARCHAR(50)) AS "REL_PATERNO_CONYUGE", --[rel_paterno_conyuge] [varchar](50) NOT NULL,
			CAST('' AS VARCHAR(50)) AS "REL_MATERNO_CONYUGE", --[rel_materno_conyuge] [varchar](50) NOT NULL,
			CAST('' AS VARCHAR(10)) AS "TIPO_REPRESENTANTE", --[tipo_representante] [varchar](10) NOT NULL,
			CURRENT_TIMESTAMP AS "REL_FEC_MODIFICA"--[rel_fec_modifica] [datetime] NOT NULL
		
		FROM (
				
				SELECT
					WST."IdStack" AS "IdStack","Action" AS ACTION
					,REL."IdPersonaOrigen"
					,REL."IdPersonaDestino"
					,REL."IdTipoRelacion" 
					
					,REL."FechaInicioRelacion"
					,REL."FechaFinRelacion"
					,REL."IdEstado"
				 
				FROM "BSCL.SocioNegocio.BD::REL.RelacionPersona" REL
							left JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST
									ON  (WST."Table" = 'Relacion_Persona' AND WST."Key" = REL."IdRelacionPersona"  ) 
				
					union 
				
			---Persona Natural / Conyuge
				SELECT 
					WST."IdStack" AS "IdStack" , "Action" AS ACTION,
					PER."IdPersona", 
					PER."IdPersonaConyuge" ,
					'CY' AS "IdTipoRelacion" ,
					PER."FechaInicioConyuge" AS "FechaInicioRelacion",
					'0001-01-01 00:00:00' AS "FechaFinRelacion", 
					'ACT' AS "IdEstado"   
			
				FROM "BSCL.SocioNegocio.BD::PER.Persona" PER 
					left JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST
						ON  (WST."Table" = 'PERSONA_RELACIONES' AND WST."Key" = PER."IdPersona" ) 
					
					WHERE PER."EstadoCivil"='CA' AND PER."Tipo"='N' AND PER."IdPersonaConyuge" >0
				
				UNION 
				
			--Persona / Empleado
				 SELECT
					WST."IdStack" AS "IdStack" , "Action" AS ACTION,
					PER."IdPersona", 
					PER."IdPersonaConyuge" ,
					'PEMDOR' AS "IdTipoRelacion" ,
					PER."FechaInicioActividad" AS "FechaInicioRelacion",
					'0001-01-01 00:00:00' AS "FechaFinRelacion", 
					'ACT' AS "IdEstado"   	 
				 FROM "BSCL.SocioNegocio.BD::PER.Persona" PER	
				 			left JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST
								ON  (WST."Table" = 'PERSONA_RELACIONES' AND WST."Key" = PER."IdPersona" ) 
				 
				 WHERE PER."IdPersonaEmpleador" >0 AND PER."CodigoTipoCargoEmpleado" <>'' --AND CPER."IdPersonaEmpleador" <> V_IdPersonaCorredor;
			
				UNION 
					
			---Persona / Representa Legal 
				  SELECT 
					WST."IdStack" AS "IdStack" , "Action" AS ACTION,
					PER."IdPersona", 
					PER."IdPersonaConyuge" ,
					'REP' AS "IdTipoRelacion" ,
					PER."FechaInicioConyuge" AS "FechaInicioRepresentante",
					'0001-01-01 00:00:00' AS "FechaFinRelacion", 
					'ACT' AS "IdEstado"  	  
				  
				  FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
				  	 		left JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST
								ON  (WST."Table" = 'PERSONA_RELACIONES' AND WST."Key" = PER."IdPersona" ) 
			
				  WHERE PER."Tipo"='J' AND PER."IdPersonaRepresentante" >0	
					/*	
				UNION ALL
				
			---ORIGEN EMPLEADO
			-- Empleador / (Corredora)
				SELECT COR."IdPersona", CEMP."IdPersona" AS "IdPersonaDestino",CEMP."CodigoTipoCargoEmpleado" AS "IdTipoRelacion"  ,'EMPLEADO' AS "Administracion",CEMP."FechaInicioCargo" AS "FechaInicioRelacion",CEMP."FechaTerminoCargo" AS "FechaFinRelacion", 0 AS "IdRelacionPersona",CEMP."IdEstado" 
						,'' AS "CodigoTipoCargoEmpleado",0 as "PorcentajeParticipacion", '' as  "FinalidadRelacion",'' as "CondicionesRelacion", 0 as "IdGradoVinculacionParental",'0001-01-01 00:00:00' AS "FechaModificacion"
						FROM "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CEMP
						INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" COR ON (COR."IdEmpresa"=CEMP."IdEmpresa" AND COR."IdEstado"='ACT')
			
				UNION ALL
			
						*/
						
				UNION 	
				
			---ORIGEN AUTORIZADO	
				SELECT 
					WST."IdStack" AS "IdStack" , "Action" AS ACTION,
					SOC."IdPersona", 
					 AUT."IdPersona" ,
					AUT."IdTipoRelacion" ,
					MIN(AUT."FechaIngreso") AS "FechaInicioRelacion",
					'0001-01-01 00:00:00' AS "FechaFinRelacion", 
					MIN(AUT."IdEstado")  AS "IdEstado"  	
				
				FROM "BSCL.Portafolio.BD.Vistas::ConsultarAutorizadoOrden" 		      AUT
					INNER JOIN "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacion"	  REL ON  (AUT."IdTipoRelacion"=REL."IdTipoRelacion")	---Segun tipo relacion (agrupador de tipo autorizado)
					INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 		  POR ON  (POR."IdPortafolio"= AUT."IdPortafolio")
					INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" SOC ON  (POR."IdCliente"= SOC."IdCliente")
					
					INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" 										  PER ON  (PER."IdPersona"= SOC."IdPersona")
					
					left JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST
								ON  (WST."Table" = 'RELACION_AUTORIZADO' AND WST."Key" = AUT."IdAutorizado" ) 
					
					WHERE PER."Tipo"='N' AND REL."Administracion"='AUTORIZADO'
					
				GROUP BY "IdStack" , SOC."IdPersona", AUT."IdPersona",	AUT."IdTipoRelacion","Action"
				
			) AS tb1
					INNER JOIN  "BSCL.SocioNegocio.BD::PER.Persona" as PER1 
						ON (PER1."IdPersona" = tb1."IdPersonaOrigen" )
	
					INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCPerCli	
						ON  (DOCPerCli."IdDocumentoIdentidad" = PER1."IdDocumentoIdentidad")
						
					INNER JOIN  "BSCL.SocioNegocio.BD::PER.Persona" as PER2
						ON (PER2."IdPersona" = tb1."IdPersonaDestino" )
	
					INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCRel	
						ON  (DOCRel."IdDocumentoIdentidad" = PER2."IdDocumentoIdentidad")
						
					LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS hr 
						ON (hr."TipoValor" = 'TipoRelacion' and hr."ValorDestino" = tb1."IdTipoRelacion"  )
						WHERE DOCREL."DocumentoIdentidad" not like '%.%' and DOCPerCli."DocumentoIdentidad" not like '%.%'

		) AS TB2
		WHERE "PER_ID_PER" > 0 AND 
			"PER_ID_REL" > 0 
			
		GROUP BY PER_ID_PER,
					PER_ID_REL,
					PAR_TRE_ID ,action	


WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.RELACIONES" TO  BCS_DESARROLLADOR WITH GRANT OPTION;