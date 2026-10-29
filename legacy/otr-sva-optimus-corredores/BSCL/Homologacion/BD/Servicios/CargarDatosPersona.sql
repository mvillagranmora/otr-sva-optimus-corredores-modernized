	SET SCHEMA "BSCL"; 
	
	DROP PROCEDURE   "BSCL.Homologacion.BD.Servicios::CargarDatosPersona";

	CREATE PROCEDURE "BSCL.Homologacion.BD.Servicios::CargarDatosPersona"
	 (
	 -- Parametros entrada comunes en todos los servicios publicos
	 IN  p_IdEmpresa		   BIGINT,			-- ID Empresa destino
     IN  p_IdUsuario		   BIGINT,			-- ID del usuario que solicita el servicio
	 IN  p_IdCarga 			   BIGINT,          -- ID de la carga 
	 IN  p_Modulo              NVARCHAR(3),     -- modulo 
     IN  P_Modalidad           NVARCHAR(1),     -- Ingreso I incremental, 
     IN  p_RutCliente          NVARCHAR(11) default '0-0',    -- rut del cliente sebra
	 OUT p_ResumenDatosProcesados Table ("CodigoProceso" nvarchar(40),"Modulo" nvarchar(3),"TotalRegistrosSebra" DECIMAL(26,8),"TotalRegistrosSap" DECIMAL(26,8)),
	 OUT p_out_ErroresContador BIGINT,
	 OUT p_out_ErroresDetalle  table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))	
	 ) 
	   
		LANGUAGE SQLSCRIPT
		SQL SECURITY INVOKER 
		DEFAULT SCHEMA "BSCL"
		---READS SQL DATA 
	AS
	BEGIN	
		-- VARIABLES VALIDACIONES BASICAS
		DECLARE v_out_Resultado  				CHAR(1); 	   --RESULTADO S/N/E
	    DECLARE v_out_Mensaje	 				NVARCHAR(520); --MENSAJE SALIDA
	    DECLARE v_out_CodigoErrorSQL 			BIGINT;
	    
	    DECLARE v_erroroutmsg  					nvarchar(520);    
	    DECLARE v_TotalPersonas_leidas			BIGINT;
	    DECLARE v_TotalPersonas_grabadas		BIGINT;
	    DECLARE v_SecuenciaMovto				BIGINT;
	    DECLARE v_PerId                 		NVARCHAR(9) :='0';
	    DECLARE v_CiudadDefecto              	BIGINT := 0;
	    DECLARE v_DivisionDefecto				BIGINT := 0;
	    -- Para LOg
	    DECLARE v_out_refID						BIGINT;    
	    DECLARE v_out_ErroresContador			BIGINT; 
	    DECLARE v_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));	    
	    
	
		--------------------------------------------------------- CONTROL DE ERRORES
		--- Controlado
		DECLARE SALIDA_CONTROLADA CONDITION;    	
		DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
		BEGIN
			IF :p_out_ErroresContador = 0 THEN	 
		 	    p_out_ErroresDetalle       =  select 'INGRESO OK' as "Codigo",'LOG REGISTRADO' as "Descripcion" from dummy;-- UNION ALL
			END IF;		 					 	
		END;	
	
		--- Excepcion
		DECLARE EXIT HANDLER FOR SQLEXCEPTION
		BEGIN
			declare v_errorExt nvarchar(40);
			IF ::SQL_ERROR_CODE > 10000 THEN
				call "BSCL"."BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
				p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
									   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
				select count(1) into p_out_ErroresContador 
				from :p_out_ErroresDetalle;										
			ELSE
				--Error interno no manejado  (El log será captado por la capa de XS control)
				p_out_ErroresDetalle       =  select 'ERROR' as "Codigo", ::SQL_ERROR_CODE as "Descripcion" from dummy;-- UNION ALL		
				RESIGNAL; 
			END IF;			
		END;
		
		-- convierto el rut a formato sebra 
		select lpad(p_RutCliente,11) into p_RutCliente from dummy;
		select TRIM(SUBSTRING(p_RutCliente,1,9)) into v_PerId from dummy;
		
		-- 0) Capturar N° DATOS LEIDOS DEL ORIGEN que no existan en el destino
	    select Count(1) into v_TotalPersonas_leidas from  "BSCL_VIRTUAL"."GES_PERSONA" PERSONA
	    --- Crear Personas que no existan	
		where not exists (  select 1 from "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" PER
							where PERSONA."per_id"   = PER."IdOrigen"
						
						 )
		and PERSONA."per_id" = case when v_PerId = '0' then PERSONA."per_id" else v_PerId end;

										
		--1) Persona : Crear Personas que no existan previamente en el modelo
			
				insert into "BSCL"."BSCL.SocioNegocio.BD::PER.Persona"(				
					"IdPersona"
					,"IdEstado"
					,"IdDocumentoIdentidad"			
					,"Tipo"
					,"Nombres"
					,"ApellidoPaterno"
					,"ApellidoMaterno"
					,"RazonSocial"
					,"IdGiroPrincipal"
					,"IdPaisNacimiento"
					,"IdPaisResidencia"
					,"FechaNacimiento"
					,"FechaDefuncion"	
					,"Sexo"
					,"EsExtranjero"
					,"EstadoCivil"
					,"RegimenConyugal"
					,"Cargo"
					,"Empleador"
					,"IdDireccionFisicaPrincipal"
					,"IdDireccionElectronicaPrincipal"		
					,"IdTelefonoPrincipal"	
					,"IdProfesion" 	
					,"FechaInicioActividad"
					,"FechaIngreso"
					,"FechaCreacion"
					,"FechaModificacion"
					,"EsEnrolado"
					,"IdCarga"
					,"Alias"
					,"IdOrigen"
				)
				
				select 		
					"BSCL"."BSCL.SocioNegocio.BD.Secuencias::Persona".NEXTVAL as IdPersona --key 	IdPersona :Generar con SECUENCIA	
					,ifnull(EstadoPersona."ValorDestino",'ACT') as "IdEstado" 
					,0 							as IdDocumentoIdentidad --Documento."IdDocumentoIdentidad" 
					,COALESCE(TipoPersona."ValorDestino",'') as Tipo
					,COALESCE(PERSONA."per_nombres",'')		as Nombres
					,COALESCE(PERSONA."per_paterno",'')		as ApellidoPaterno
					,COALESCE(PERSONA."per_materno",'')		as ApellidoMaterno
					,COALESCE(PERSONA."per_raz_social",'') 	as RazonSocial
					,0 							as IdGiroPrincipal
					,COALESCE(PaisNacimiento."IdPais",0) 	as IdPaisNacimiento
					,COALESCE(PaisResidencia."IdPais",0)    as IdPaisResidencia--, PERSONA."per_pais_iso"
					,PERSONA."per_fec_naci"					as FechaNacimiento
					,'0001-01-01'               			as FechaDefuncion
					,PERSONA."per_sexo"						as Sexo
					,CASE when 
						 PERSONA."nac_id" <> 'CL' 
						 then 'S' 
						 else 'N'
					END												as EsExtranjero
					,ifnull(EstadoCivil."ValorDestino",'SO') 		as EstadoCivil
					,ifnull(RegimenConyugal."ValorDestino",'SC') 	as RegimenConyugal
					,''
					,''
					,0 										as IdDireccionFisicaPrincipal 					
					,0 										as IdDireccionElectronicaPrincipal		
					,0 										as IdTelefonoPrincipal	
					,COALESCE(Profesion."IdProfesion",0)	as IdProfesion 	
					,PERSONA."per_fec_ingreso"  as FechaInicioActividad
					,PERSONA."per_fec_ingreso"  as FechaIngreso
					,PERSONA."per_fec_ingreso"  as FechaCreacion 			
					,PERSONA."per_fec_modifica" as FechaModificacion
					,'S'
					,:p_IdCarga 				as IdCarga
					,'' 						as Alias
					, PERSONA."per_id"			as "IdOrigen"
				from "BSCL_VIRTUAL"."GES_PERSONA" PERSONA		 
					left outer join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as TipoPersona
					  on  (TipoPersona."TipoValor" = 'TipoPersona' and  
					  	   TipoPersona."Entidad" = 'CLIENTE' and
					       TipoPersona."ValorOrigen" = PERSONA."per_tip_per"
					      )
					      
					left outer join "BSCL"."BSCL.Plataforma.BD::REG.Pais" as PaisResidencia
					  on  (PaisResidencia."CodigoPais"= PERSONA."per_pais_iso")
					  
					left outer join "BSCL"."BSCL.Plataforma.BD::REG.Pais" as PaisNacimiento
					  on  (PaisNacimiento."CodigoPais"= PERSONA."nac_id")
					  
		      		left outer join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as EstadoCivil
					  on  (EstadoCivil."TipoValor" = 'EstadoCivil' and  
					  	   EstadoCivil."Entidad" = 'CLIENTE' and
					       EstadoCivil."ValorOrigen" = PERSONA."par_est_civil"
					      )
			  
		      		left outer join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as RegimenConyugal
					  on  (RegimenConyugal."TipoValor" = 'RegimenConyugal' and  
					  	   RegimenConyugal."Entidad" = 'CLIENTE' and
					       RegimenConyugal."ValorOrigen" = PERSONA."par_reg_conyugal"
					      )
					left outer join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as EstadoPersona
					  on  (EstadoPersona."TipoValor" = 'EstadoPersona' and  
					  	   EstadoPersona."Entidad" = 'Persona' and
					       EstadoPersona."ValorOrigen" = PERSONA."est_id"
					      )
					left outer join "BSCL.Plataforma.BD::REG.Profesion" as Profesion
					  on (Profesion."IdProfesion" = PERSONA."per_actividad_id")

				--- Crear Personas que no existan	
				where not exists (  select 1 from "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" PER
									where PERSONA."per_id"   = PER."IdOrigen"
								 )
				and PERSONA."per_rutdv" = case when p_RutCliente = '        0-0' then PERSONA."per_rutdv" else p_RutCliente end; 
				 
		--2) Socio Negocio: Crear Personas de esta carga como socio de Negocio
        -- siempre y cuando no haya creado anteriormente					 
		insert into "BSCL"."BSCL.SocioNegocio.BD::SOC.SocioDeNegocio"(
					"IdSocioDeNegocio",
					"IdPersona", 
					"IdEmpresa"
				)
		select 
			"BSCL"."BSCL.SocioNegocio.BD.Secuencias::SocioDeNegocio".NEXTVAL as IdSocioDeNegocio --SECUENCIA
	        ,Persona."IdPersona"       	as IdPersona	
			,p_IdEmpresa				as IdEmpresa
			from  "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as Persona
			where Persona."IdCarga" = :p_IdCarga;
			
			

			
      	-----------------------------------------------------------------------------------			
		------------------------------ DATOS COMPLEMENTARIOS ------------------------------
      	-----------------------------------------------------------------------------------							 																				
		-- 2) Crear Direcciones Fisicas, Electronicas, Telefono, 
			----------------------
			--- Fisicas ----------
			----------------------
            -- la direcciones deben ser ingresadas aunque su nombre sea igual ya que
            -- DECLARE v_CiudadDefecto              	BIGINT;
	    	-- DECLARE v_DivisionDefecto				BIGINT;
            
            -- ciudad por defecto en caso que no tenga ninguna direccion fisica
             select "IdCiudad","IdDivisionPais" into v_CiudadDefecto,v_DivisionDefecto 
               from "BSCL"."BSCL.Plataforma.BD::REG.DivisionPais"
             where "NombreDivisionPais" = 'SANTIAGO';
             
            -- 
             
    
			insert into "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionFisica"(
			 "IdDireccionFisica"		
			 ,"Calle"					
			 ,"Numero" 					
			 ,"Departamento"			
			 ,"Sector" 					
			 ,"CodigoPostal" 			
			 ,"IdCiudad" 				
			 ,"CodigoProposito"			
			 ,"Comentario" 				
			 ,"IdEstado" 				
			 ,"IdPersona" 				
			 ,"IdEstadoAutorizacion"	
			 ,"IdCarga" 				
			 ,"IdOrigen"
			 ,"IdDivisionPais") 	
			 			
			select 
				"BSCL"."BSCL.SocioNegocio.BD.Secuencias::DireccionFisica".NEXTVAL as "IdDireccionFisica" -- Generar  Secuencia
				,COALESCE(TO_VARCHAR(DIRECCION."dir_nombre"),'POR DEFINIR') 		  as "Calle"
	 			,COALESCE(DIRECCION."dir_numero",'0')							  as "Numero" 
	 			,SUBSTRING(COALESCE(TO_VARCHAR(DIRECCION."dir_complemento"),'0'),1,40) 	as "Departamento"
	 			,'' 												as "Sector"
	 			,COALESCE(DIRECCION."dir_cod_postal",'') 			as "CodigoPostal" 
	 			,COALESCE(DIV."IdCiudad",v_CiudadDefecto)           as "IdCiudad" --por defecto  dejamso defecto a santiago
	 			,COALESCE(CodigoProposito."ValorDestino",'PART')	as "CodigoProposito"
	 			,'' 												as "Comentario" 
	 			,'ACT' 												as "IdEstado"
				,PER."IdPersona"									as "IdPersona"
				,'ING' 												as "IdEstadoAutorizacion"
				,p_IdCarga							 			    as "IdCarga"
				,PER."IdOrigen"									    as "IdOrigen"
				,COALESCE(DIV."IdDivisionPais",:v_DivisionDefecto)   as "IdDivisionPais"
			from  "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" PER 	
			left join "BSCL_VIRTUAL"."GES_DIRECCION" DIRECCION
				on DIRECCION."per_id" = PER."IdOrigen"
			    and DIRECCION."per_id" = case when v_PerId = '0' then DIRECCION."per_id" else v_PerId end
			    and left(DIRECCION."par_tdi_id",6)='TDIREF'
			inner join "BSCL_VIRTUAL"."GES_COR_COMUNA" AS COM
				on COM."com_id" = DIRECCION."com_id"
		    inner join "BSCL"."BSCL.Plataforma.BD::REG.DivisionPais" AS DIV
				on upper(trim(DIV."NombreDivisionPais")) = upper(trim(COM."com_nombre"))
			inner join "BSCL.Homologacion.BD::HOM.Homologacion" AS CodigoProposito
			   on  CodigoProposito."TipoValor" = 'TipoDireccion'
			   and CodigoProposito."ValorOrigen" = TRIM(DIRECCION."par_tdi_id")
			where not exists ( select 1 from "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionFisica" DIR
								where DIRECCION."per_id" = DIR."IdOrigen"
								and DIRECCION."dir_nombre" = DIR."Calle" -- criterio incorporado para permitir agregar direcciones a una misma persona
							   -- and DIR."CodigoProposito" = COALESCE(CodigoProposito."ValorDestino",'PART')
							 )
			and DIRECCION."com_id" <> 0;
		 
							 
			
			------------------------
			-- Direccion Electronica
			------------------------
			insert into "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionElectronica"(
			"IdDireccionElectronica"		,
	 		"Direccion"					 	,
	 		"CodigoProposito" 				,
			"Comentario" 					,
	 		"IdEstado" 						,
	  		"IdPersona" 					,
	 		"IdEstadoAutorizacion"			,
	 		"IdCarga" 						,
			"IdOrigen" 						
			) 			
			select 
				"BSCL"."BSCL.SocioNegocio.BD.Secuencias::DireccionElectronica".NEXTVAL as "IdDireccionElectronica" -- Generar  Secuencia
				,COALESCE(DIRECCION."dir_nombre",'PORDEFINIR@pordefinir.cl')	as "Direccion"
	 			,COALESCE(CodigoProposito."ValorDestino",'PART')as "CodigoProposito"  
	 			,''								as "Comentario" 
	 			,'ACT' 							as "IdEstado"
				,PER."IdPersona"				as "IdPersona" -- aun no tengo ID
				,'ING' 							as "IdEstadoAutorizacion"
				,p_IdCarga					 			as "IdCarga"
				,PER."IdOrigen"					as "IdOrigen"
			from "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PER	
			left join "BSCL_VIRTUAL"."GES_DIRECCION" DIRECCION 
				on DIRECCION."per_id" = PER."IdOrigen"
			    and DIRECCION."per_id" = case when  v_PerId = '0' then DIRECCION."per_id" else v_PerId end
				and left(DIRECCION."par_tdi_id",6)='TDIREE'
			inner join "BSCL.Homologacion.BD::HOM.Homologacion" AS CodigoProposito
			   on  CodigoProposito."TipoValor" = 'TipoDireccion'
			   and CodigoProposito."ValorOrigen" = TRIM(DIRECCION."par_tdi_id")
			--- Crear direcciones que no existan como direccion electronica	
			where not exists ( select 1 from "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionElectronica" DIR
								where DIRECCION."per_id" = DIR."IdOrigen"
								and DIRECCION."dir_nombre" = DIR."Direccion" -- criterio incorporado para permitir agregar direcciones a una misma persona
							 );
			
			
			----------------------					
			-----Telefono					
			----------------------
			insert into "BSCL"."BSCL.SocioNegocio.BD::PER.Telefono" (
			 "IdTelefono"					
			 ,"IdPais" 						
			 ,"Numero"						
			 ,"Anexo" 						
			 ,"CodigoProposito" 			
			 ,"Comentario" 					
			 ,"IdEstado" 							
			 ,"IdPersona" 					
			 ,"IdEstadoAutorizacion"		
			 ,"IdCarga" 					
			 ,"IdOrigen"
			 ,"IdTipoTelefono"					
			)
			select
			    "BSCL"."BSCL.SocioNegocio.BD.Secuencias::Telefono".NEXTVAL as "IdTelefono" -- Generar  Secuencia
				,0 												as "IdPais"
				, COALESCE(TELEFONO."tel_numero",'56999999999') as "Numero"	
				, '' 							as "Anexo"
				, 'PART' 						as "CodigoProposito" --particular se coloca en duro debido a que no existe dicha info en sebra
				, COALESCE(TELEFONO."tel_observacion",'POR DEFINIR') as "Comentario"
			 	,'ACT'							as "IdEstado"
			 	,PER."IdPersona"				as "IdPersona"
			 	,'ING'							as "IdEstadoAutorizacion"
				,p_IdCarga 							as "IdCarga"
				,PER."IdOrigen"					as "IdOrigen" -- criterio incorporado para permitir agregar telefonos a una misma persona
			    ,COALESCE(TipoTelefono."ValorDestino",'')			    
			from "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" AS PER 
			left join "BSCL_VIRTUAL"."GES_TELEFONO" TELEFONO 
           		on TELEFONO."per_id" = PER."IdOrigen"
				and TELEFONO."per_id" = case when v_PerId = '0' then TELEFONO."per_id" else v_PerId end
			inner join "BSCL.Homologacion.BD::HOM.Homologacion" AS TipoTelefono
			   on  TipoTelefono."TipoValor" = 'TipoTelefono'
			   and TipoTelefono."ValorOrigen" = TRIM(TELEFONO."par_tte_id")
			--- Crear direcciones que no existan	
			where not exists ( select 1 from "BSCL"."BSCL.SocioNegocio.BD::PER.Telefono" FONO
							   where TELEFONO."per_id"   = FONO."IdOrigen"
							   and TELEFONO."tel_numero" = FONO."Numero"	
							 );	
							 
	    --3) Crear un Documento Identidad de la persona
		
				insert into "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"(
					"IdDocumentoIdentidad"
					,"IdPersona"
					,"DocumentoIdentidad"
					,"IdTipoDocumentoIdentidad"
					,"NumeroSerie"
					,"FechaEmision"
					,"FechaVencimiento"
					,"IdEstado"
					,"FechaIngreso"
					,"FechaCreacion"
					,"FechaModificacion"
					,"IdCarga"
					,"IdEstadoAutorizacion"
					,"IdOrigen"
				)
				select
					"BSCL"."BSCL.SocioNegocio.BD.Secuencias::DocumentoIdentidad".NEXTVAL as "IdDocumentoIdentidad" -- Generar IdDocumentoIdentidad Secuencia
					,PER."IdPersona"									as "IdPersona"
					,TRIM(coalesce(PERSONA."per_num_doc",'')) 			as "DocumentoIdentidad"
					,ifnull(TipoDocumentoIdentidad."ValorDestino",'R') 	as "IdTipoDocumentoIdentidad"
					,'' AS NumeroSerie
					,TO_DATE(PERSONA."per_fec_naci") as FechaEmision
					,'0001-01-01' as FechaVencimiento
					,'ACT' AS IdEstado
					,'0001-01-01 00:00:00' AS FechaIngreso
					,'0001-01-01 00:00:00' AS FechaCreacion
					,'0001-01-01 00:00:00' AS FechaModificacion
					,:p_IdCarga as IdCarga
					,'ING' as "IdEstadoAutorizacion"
					,PERSONA."per_id"
				from "BSCL_VIRTUAL"."GES_PERSONA" PERSONA 
					-- De personas creadas en la presente carga
					inner join  "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" PER on (PERSONA."per_id" = case when v_PerId = '0' then PER."IdOrigen" else v_PerId end and PER."IdCarga" = :p_IdCarga)
				left outer join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as TipoDocumentoIdentidad
					on  (TipoDocumentoIdentidad."TipoValor" = 'TipoDocumentoIdentidad' and  
					     TipoDocumentoIdentidad."Entidad" = 'CLIENTE' and
						 TipoDocumentoIdentidad."ValorOrigen" = PERSONA."per_tip_doc"
						)
				--- Crear documentos identidad que no existan	
				where not exists (  select 1 from "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC
									where trim(PERSONA."per_num_doc") = trim(DOC."DocumentoIdentidad")
								    and PERSONA."per_id"   = DOC."IdOrigen"
								 );
	
	
			-- cargar nombre fansia solo para personas de las persona
        insert into "BSCL.SocioNegocio.BD::PER.NombreFantasia"
                    ("IdNombreFantasia","DescripcionNombreFantasia","IdPersona","FechaCreacion"
					,"FechaModificacion","FechaIngreso","IdEstado","IdEstadoAutorizacion"
					,"Borrado","IdUsuarioCreador","IdCarga")
        select "BSCL.SocioNegocio.BD.Secuencias::NombreFantasia".NEXTVAL AS "IdNombreFantasia"
               ,PER_SEBRA."per_paterno" AS "DescripcionNombreFantasia"
               ,PER_SAP."IdPersona" as "IdPersona"
               ,PER_SAP."FechaIngreso" AS "FechaCreacion"
               ,'0001-01-01' AS "FechaModificacion"
               ,PER_SAP."FechaIngreso" AS "FechaCreacion"
               ,'ACT' AS "IdEstado"
               ,'ING' AS "IdEstadoAutorizacion"
               ,'N' AS "Borrado"
               ,1 AS "IdUsuarioCreador"
               ,p_IdCarga  AS "IdCarga"
         FROM "BSCL_VIRTUAL"."GES_PERSONA" AS PER_SEBRA	
		 INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC
		   ON DOC."DocumentoIdentidad" = TRIM(upper(PER_SEBRA."per_num_doc"))
		 INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER_SAP
		   ON PER_SAP."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad"
		 LEFT JOIN  "BSCL.SocioNegocio.BD::PER.NombreFantasia" AS NOM_FANTASIA_SAP
		 	ON NOM_FANTASIA_SAP."IdPersona" = PER_SAP."IdPersona"
		 WHERE LENGTH(PER_SEBRA."per_paterno") > 0
		   AND PER_SEBRA."per_tip_per" = 'JUR'
		   and NOM_FANTASIA_SAP."IdPersona" IS NULL; 
		   
	
		-- 4) Asociar Principal Direccion y telefono por defecto a Persona que se creaan recien en ESTA CARGA
	
			UPDATE 	"BSCL"."BSCL.SocioNegocio.BD::PER.Persona" 
			SET 	"IdDireccionFisicaPrincipal" = IFNULL((select max(DIR."IdDireccionFisica") 
													from "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionFisica" DIR 
													where DIR."IdPersona" = "BSCL"."BSCL.SocioNegocio.BD::PER.Persona"."IdPersona" 
													and "BSCL"."BSCL.SocioNegocio.BD::PER.Persona"."IdCarga" = :p_IdCarga 
													),0)
			WHERE "IdCarga" = p_IdCarga;
			  
							
			UPDATE 	"BSCL"."BSCL.SocioNegocio.BD::PER.Persona"  
			SET 	"IdDireccionElectronicaPrincipal" = IFNULL((select max(DIR."IdDireccionElectronica") 
													from "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionElectronica" DIR 
													where DIR."IdPersona" = "BSCL"."BSCL.SocioNegocio.BD::PER.Persona"."IdPersona" 
													and "BSCL"."BSCL.SocioNegocio.BD::PER.Persona"."IdCarga" = :p_IdCarga
													),0)
			WHERE "IdCarga" = p_IdCarga;
			
													
			UPDATE 	"BSCL"."BSCL.SocioNegocio.BD::PER.Persona"  
			SET 	"IdTelefonoPrincipal" = IFNULL((select max(TEL."IdTelefono") 
													from "BSCL"."BSCL.SocioNegocio.BD::PER.Telefono" TEL 
													where TEL."IdPersona" = "BSCL"."BSCL.SocioNegocio.BD::PER.Persona"."IdPersona" 
													and "BSCL"."BSCL.SocioNegocio.BD::PER.Persona"."IdCarga" = :p_IdCarga													
													),0)
			WHERE "IdCarga" = p_IdCarga;
													
			UPDATE 	"BSCL"."BSCL.SocioNegocio.BD::PER.Persona"  
			SET 	"IdDocumentoIdentidad" = IFNULL((select  max(DOC."IdDocumentoIdentidad")
													from "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as  DOC 	
													where  DOC."IdPersona" = "BSCL"."BSCL.SocioNegocio.BD::PER.Persona"."IdPersona"
													and DOC."IdTipoDocumentoIdentidad" = 'R'
													and "BSCL"."BSCL.SocioNegocio.BD::PER.Persona"."IdCarga" = :p_IdCarga													
													),0)
			WHERE "IdCarga" = p_IdCarga;
			
			
			UPDATE 	"BSCL"."BSCL.SocioNegocio.BD::PER.Persona"  
			SET 	"IdNombreFantasiaPrincipal" = IFNULL((select  max(DOC."IdNombreFantasia")
													from "BSCL"."BSCL.SocioNegocio.BD::PER.NombreFantasia" as  DOC 	
													where  DOC."IdPersona" = "BSCL"."BSCL.SocioNegocio.BD::PER.Persona"."IdPersona"
													and "BSCL"."BSCL.SocioNegocio.BD::PER.Persona"."IdCarga" = :p_IdCarga													
													),0)
			WHERE "IdCarga" = :p_IdCarga	
			  and  "Tipo" ='J';
													


		-- cargamos las relaciones 
        -- se comenta mientras cargan las tablas
        /*
        
        insert into "BSCL"."BSCL.SocioNegocio.BD::REL.RelacionPersona" 
        (	 "IdRelacionPersona","IdTipoRelacion","IdPersonaOrigen","IdPersonaDestino","IdEstado","FechaIngreso","FechaInicioRelacion"
			,"FechaFinRelacion","FechaCreacion","FechaModificacion","IdUsuarioCreador","IdEstadoAutorizacion","PorcentajeParticipacion"
            ,"RetiraDocumentos"
         )   
		 select "BSCL.SocioNegocio.BD.Secuencias::RelacionPersona".NEXTVAL  as "IdRelacionPersona"
				,HOMOLOGACION."ValorDestino" as "IdTipoRelacion"
				,PERSONA_ORIGEN_RELACION."IdPersona" as "IdPersonaOrigen"
				,PERSONA_DESTINO_RELACION."IdPersona" as "IdPersonaDestino"
				,'ACT' as "IdEstado"
				,RELACION_SEBRA."rel_fec_inicio" as "FechaIngreso"
				,RELACION_SEBRA."rel_fec_inicio" as "FechaInicioRelacion"
				,RELACION_SEBRA."rel_fec_termino"as "FechaFinRelacion"
				,RELACION_SEBRA."rel_fec_inicio" as "FechaCreacion"
				,'0001-01-01' as "FechaModificacion"
				,p_IdUsuario as "IdUsuarioCreador"
				,'ING' as "IdEstadoAutorizacion"
				,0 as "PorcentajeParticipacion"
				,'N' as "RetiraDocumentos"
		  from "BSCL_VIRTUAL"."GES_RELACIONES" as RELACION_SEBRA
		  INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" as PERSONA_ORIGEN_RELACION
		  	ON PERSONA_ORIGEN_RELACION."IdOrigen" = RELACION."per_id_per"
		  INNER "BSCL.SocioNegocio.BD::PER.Persona" as PERSONA_DESTINO_RELACION
		  	ON PERSONA_ORIGEN_RELACION."IdOrigen" = RELACION_SEBRA."per_id_rel"
		  INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS HOMOLOGACION
		  	ON HOMOLOGACION."TipoValor" = 'CodigosRelacionados'
		  	AND HOMOLOGACION."ValorOrigen" = RELACION."par_tre_id"
		  LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD::REL.RelacionPersona" AS RELACION
		  	ON RELACION."IdPersonaOrigen"	= PERSONA_ORIGEN_RELACION."IdPersona"
		  	AND RELACION."IdPersonaDestino" = PERSONA_DESTINO_RELACION."IdPersona"
		  	AND RELACION."IdTipoRelacion"   = HOMOLOGACION."ValorDestino"
          WHERE RELACION."IdRelacionPersona" IS NULL;
		*/   
	        	
		-- Validar Datos Leidos v/s Datos Cargados
		select Count(1) into v_TotalPersonas_grabadas from "BSCL"."BSCL.SocioNegocio.BD::PER.Persona"  AS PER where PER."IdCarga" = :p_IdCarga;
    
        p_ResumenDatosProcesados =  select 'CARGAPERSONA' as "CodigoProceso" ,'PER' as "Modulo" ,v_TotalPersonas_leidas as "TotalRegistrosSebra",v_TotalPersonas_grabadas as "TotalRegistrosSap"  FROM DUMMY
       							    union all
       							    select * from :p_ResumenDatosProcesados;
       							    
	    IF v_TotalPersonas_grabadas <>  v_TotalPersonas_leidas THEN		
	    --- Listar clientes no grabados en Clientes, eliminarlos del modelo completo (persona, Documento, documentopersona y portafolio) y grabar en LOG
			v_hash= select 'DatosLeidos'   as "Codigo", v_TotalPersonas_leidas   as "Valor" FROM DUMMY UNION ALL
			        select 'DatosGrabados' as "Codigo", v_TotalPersonas_grabadas as "Valor" FROM DUMMY;
			CALL "BSCL"."BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('CARGA_CON_DIFERENCIAS',:v_hash,:v_erroroutmsg);
			p_out_ErroresDetalle =  select 'CARGA_CON_DIFERENCIAS' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy; 
			p_out_ErroresContador := p_out_ErroresContador + 1;
		END IF ;

		SIGNAL SALIDA_CONTROLADA;    
	END;
