	SET SCHEMA "BSCL"; 
	
	DROP PROCEDURE   "BSCL.Homologacion.BD.Servicios::CargarContraparte";
	CREATE PROCEDURE "BSCL.Homologacion.BD.Servicios::CargarContraparte"
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
	    DECLARE v_Cantidad 						BIGINT :=0;
	    DECLARE v_ContrapartesLeidas            BIGINT :=0;
	    DECLARE v_ContrapartesCargadas          BIGINT :=0;
	     
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

        create local temporary column table #v_ContraparteMigracion
         ( 
	          							"Rut" NVARCHAR(11)
	          							,"NombreFantasia"  NVARCHAR(200)
	          							,"RazonSocial"  NVARCHAR(200)
	          							,"Tipo"  NVARCHAR(40)
	     ); 

	    -- en caso que el tipo modalidad sea 'R'
         if p_Modalidad = 'R' then 
    	   -- quitar la relacion de socio de negocio 
           update A1 set A1."IdContraparte" = null 
           from "BSCL"."BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" as A1
           where "IdContraparte" in (select "IdContraparte" from "BSCL"."BSCL.Homologacion.BD::HOM.Contraparte" 
                                     where "IdCarga" = p_IdCarga);
          
           delete from "BSCL"."BSCL.SocioNegocio.BD::CNT.Contraparte" 
           where "IdContraparte" in (select "IdContraparte" from "BSCL"."BSCL.Homologacion.BD::HOM.Contraparte" 
                                     where "IdCarga" = p_IdCarga);
  
          --borro todo lo de la tabla homologacion contraparte
           delete from "BSCL"."BSCL.Homologacion.BD::HOM.Contraparte" 
           where "IdCarga" = p_IdCarga;
        end if;
        
		-- lleno las contrapartes bolsa
        -- se llenan las bolsas de chile ya que la bolsa de colombia y peru no tienen rut
        -- es decir cargaremos las tres bolsas de chile se debe revisar que hacer con las otras bolsas

        --v_Contrapartes = 
        
        INSERT INTO #v_ContraparteMigracion("Rut","NombreFantasia","RazonSocial","Tipo")
                         select '90249000-0' as "Rut" ,'BOLSA DE COMERCIO DE SANTIAGO' as "NombreFantasia", 'BOLSA DE COMERCIO DE SANTIAGO' as "RazonSocial", 'BOLSA' as "Tipo" from dummy           
                         union all
                         select '90263000-7' as "Rut" ,'BOLSA DE VALORES DE VALPARAISO' as "NombreFantasia" ,'BOLSA DE VALORES DE VALPARAISO' as "RazonSocial", 'BOLSA' as "Tipo"  from dummy
           				 union all
           				 select '96551730-8' as "Rut" ,'BOLSA ELECTRONICA DE CHILE' as "NombreFantasia" ,'BOLSA ELECTRONICA DE CHILE' as "RazonSocial", 'BOLSA' as "Tipo" from dummy      
                         union all
        --contraparte central
                         select '96572920-8' as "Rut",'CCLV' as "NombreFantasia",'CCLV CONTRAPARTE CENTRAL S.A.' as "RazonSocial", 'CONTRAPARTECENTRAL' as "Tipo" from dummy
                         union all      
        --lleno las contrapartes bancos  
						 select "RutBanco" as "Rut","NombreBanco" as "NombreFantasia","NombreBanco" as "RazonSocial", 'BANCO' as "Tipo"
                         from "BSCL"."BSCL.Plataforma.BD::BAN.Banco"
                         where "RutBanco" NOT IN ('99999999-9','')
		-- deposito
                        union all
                        select '96666140-2' as "Rut",'DEPOSITO CENTRAL DE VALORES' as "NombreFantasia",'Deposito Central de Valores S.A. Deposito de Valores' as "RazonSocial", 'DEPOSITO' as "Tipo" from dummy
                        union all          
        -- corredoras
                         select TRIM(USU."RUT_CORR") as "Rut",TRIM(USU."RAZON_SOCIAL") as "NombreFantasia",TRIM(USU."RAZON_SOCIAL") as "RazonSocial", 'CORREDORA' as "Tipo"
                         from  "RUEBAS"."TBUTUSUA" AS USU 
				         where USU."TIPO_ENT"  = '08' 
						 	and USU."ESTADO"   = 'V'
						 	and USU."TIPO_USU" = 'C' 
						 	and USU."RUT_CORR" <>'         -0'
						 	and USU."NEMO" NOT IN ('CCLV      ','BOLSA     ','BCS       ','PRUEBA 3  ')  
                      union all
          -- emisores                         
					SELECT trim("RutEmisor") as "Rut",MAX("RazonSocial") as "NombreFantasia" ,MAX("RazonSocial") as "RazonSocial",'EMISOR' AS "Tipo"
					FROM "BSCL"."BSCL.Mercado.BD::MER.Emisor"
					WHERE  "RutEmisor" NOT IN ('',' 99999999-9','         -0','','-0')
					group by "RutEmisor";
                
        -- recupero la tabla pivote con todos los rut unicos
        v_TotalRutContraparte   = select distinct "Rut" as "Rut" from  #v_ContraparteMigracion;

        -- luego genero la estructura para crear la contraparte
        v_contrapartesagrupados = select A0."Rut" 
                                         ,COALESCE(A1."NombreFantasia",null) as "NombreFantasiaBolsa"
                                         ,COALESCE(A1."RazonSocial",null) as "RazonSocialBolsa" 
                                         ,COALESCE(A2."NombreFantasia",null) as "NombreFantasiaBanco"
                                         ,COALESCE(A2."RazonSocial",null) as "RazonSocialBanco" 
                                         ,COALESCE(A3."NombreFantasia",null) as "NombreFantasiaDeposito"
                                         ,COALESCE(A3."RazonSocial",null) as "RazonSocialDeposito" 
                                         ,COALESCE(A4."NombreFantasia",null) as "NombreFantasiaEmisores"
                                         ,COALESCE(A4."RazonSocial",null) as "RazonSocialEmisores"
                                         ,COALESCE(A5."NombreFantasia",null) as "NombreFantasiaContraparteCentral"
                                         ,COALESCE(A5."RazonSocial",null) as "RazonSocialContraparteCentral"
                                         ,COALESCE(A6."NombreFantasia",null) as "NombreFantasiaCorredora"
                                         ,COALESCE(A6."RazonSocial",null) as "RazonSocialCorredora"
                                         ,CASE WHEN IFNULL(A1."Tipo",'') ='' THEN 'N' ELSE 'S' END as "EsBolsa"
                                         ,CASE WHEN IFNULL(A2."Tipo",'') ='' THEN 'N' ELSE 'S' END as "EsBanco"
                                         ,CASE WHEN IFNULL(A3."Tipo",'') ='' THEN 'N' ELSE 'S' END as "EsDeposito"
                                         ,CASE WHEN IFNULL(A4."Tipo",'') ='' THEN 'N' ELSE 'S' END as "EsEmisor"
                                         ,CASE WHEN IFNULL(A5."Tipo",'') ='' THEN 'N' ELSE 'S' END as "EsContraparteCentral"
                                         ,CASE WHEN IFNULL(A6."Tipo",'') ='' THEN 'N' ELSE 'S' END as "EsCorredora"
                                         FROM :v_TotalRutContraparte AS A0
                                         LEFT JOIN #v_ContraparteMigracion AS A1
                                         ON A1."Rut" = A0."Rut"
                                         AND A1."Tipo" = 'BOLSA' 
                                         LEFT JOIN #v_ContraparteMigracion AS A2
                                         ON A2."Rut" = A0."Rut"
                                         AND A2."Tipo" = 'BANCO'                                          
                                         LEFT JOIN #v_ContraparteMigracion AS A3
                                         ON A3."Rut" = A0."Rut"
                                         AND A3."Tipo" = 'DEPOSITO'  
                                         LEFT JOIN #v_ContraparteMigracion AS A4
                                         ON A4."Rut" = A0."Rut"
                                         AND A4."Tipo" = 'EMISOR'  
                                         LEFT JOIN #v_ContraparteMigracion AS A5
                                         ON A5."Rut" = A0."Rut"
                                         AND A5."Tipo" = 'CONTRAPARTECENTRAL'
                                         LEFT JOIN #v_ContraparteMigracion AS A6
                                         ON A6."Rut" = A0."Rut"
                                         AND A6."Tipo" = 'CORREDORA'
                                         WHERE A0."Rut" <> '';

       -- revisamos si existe una persona asociada al rut que tenga un registro de contraparte de ser asi actualizamos el registro de contraparte
       UPDATE A4 set  A4."Emisor" = A1."EsEmisor"
                     ,A4."Depositos" = A1."EsDeposito" 
                     ,A4."Bolsa" = A1."EsBolsa" 
                     ,A4."Corredora"  = A1."EsCorredora" 
                     ,A4."CamaraDeCompensacion" =A1."EsContraparteCentral" 
                     ,A4."Banco" = A1."EsBanco"
       FROM :v_contrapartesagrupados as A1
       INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as A2
       ON A2."DocumentoIdentidad"= A1."Rut"
       INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as A3
       ON A3."IdDocumentoIdentidad" = A2."IdDocumentoIdentidad"
       INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::CNT.Contraparte" as A4
       ON A4."IdPersona" = A3."IdPersona";

       -- de las contrapartes actualizadas actualizamos la tabla socio de negocio en el campo contraparte

       UPDATE A5 set A5."IdContraparte" = A4."IdContraparte"
       FROM :v_contrapartesagrupados as A1
       INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as A2
       ON A2."DocumentoIdentidad"= A1."Rut"
       INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as A3
       ON A3."IdDocumentoIdentidad" = A2."IdDocumentoIdentidad"
       INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::CNT.Contraparte" as A4
       ON A4."IdPersona" = A3."IdPersona"
       INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" as A5
       ON A5."IdPersona" = A4."IdPersona";
       
       -- revisamos aquellos registro en los cuales exista la persona pero no exista registro de contraparte 
       -- en dicho caso debemos ingresar el registro de contraparte y luego actualizar el registro en socio de negocios
       -- en caso que no exista en la tabla socio de negocio debemos ingresarlo.
	   -- le insertamos un registro por defecto para que siempre se cree la tabla temporal 

	  v_NuevasContrapartes = select  0 as  "IdPersona"	
									,0 as  "IdEmpresa"
									,0 as  "CodigoInscripcion"	
									,'' as "EsEmisor"
									,''	as "EsDeposito"
									,'' as "EsBolsa"
									,'' as "EsCorredora"
									,''	as "EsContraparteCentral"
									,'' as "EsBanco"
									,'' as "EsBoveda"
									,'' as "IdEstado"	
									,'' as "EsEnrolado"	
									,'' as "Borrado"
									,'0001-01-01' as "FechaIngreso"
									,'0001-01-01' as "FechaCreacion"
									,'0001-01-01' as "FechaModificacion"
									, 0 as "IdUsuarioCreador"
									, '' as "IdTipoBloqueo"
									,'0001-01-01' as "FechaBloqueo"
									,'' as "ComentarioBloqueo" 	
									,'' as "IdTipoDesbloqueo" 	
									,'0001-01-01' as "FechaDesbloqueo"
									,'' as "ComentarioDesbloqueo"
									,'0-0' as "Rut"
                                    ,p_IdCarga  as "IdCarga"
                                    ,'0'  as "IdOrigen" 
						     FROM dummy;
					     
	     v_NuevasContrapartes =     select distinct
									 A3."IdPersona" as "IdPersona"	
									,p_IdEmpresa as "IdEmpresa"
									,0 as "CodigoInscripcion"	
									,"EsEmisor"
									,"EsDeposito"	
									,"EsBolsa"
									,"EsCorredora"	
									,"EsContraparteCentral" 
									,"EsBanco"	
									,'N' as  "EsBoveda"
									,'ACT' as "IdEstado"	
									,'S' as "EsEnrolado"	
									,'N' as "Borrado"
									,'0001-01-01' as "FechaIngreso"
									,'0001-01-01' as "FechaCreacion"
									,'0001-01-01' as "FechaModificacion"
									, p_IdUsuario as "IdUsuarioCreador"
									, null as "IdTipoBloqueo"
									,'0001-01-01' as "FechaBloqueo"
									,'' as "ComentarioBloqueo" 	
									, null as "IdTipoDesbloqueo" 	
									,'0001-01-01' as "FechaDesbloqueo"
									,'' as "ComentarioDesbloqueo"
									,"Rut" as "Rut"
                                    ,p_IdCarga as "IdCarga"
                                    ,CASE WHEN LOCATE (A1."Rut",'-') = 0 then "Rut" ELSE  SUBSTR_BEFORE (A1."Rut", '-') END as "IdOrigen" 
						   FROM :v_contrapartesagrupados as A1
					       INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as A2
					       ON A2."DocumentoIdentidad"= A1."Rut"
					       INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as A3
					       ON A3."IdDocumentoIdentidad" = A2."IdDocumentoIdentidad"
					       INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" as A4
					       ON A4."IdPersona" = A3."IdPersona"
					       LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD::CNT.Contraparte" as A5
					       ON A5."IdOrigen" = A2."IdOrigen"
					       WHERE A5."IdOrigen" IS NULL
					       AND "Rut" <> '0-0'
					       union all 
					       select * from :v_NuevasContrapartes;

          v_NuevasContrapartes1 = select "BSCL.SocioNegocio.BD.Secuencias::Contraparte".NEXTVAL as "IdContraparte",* 
                                 from :v_NuevasContrapartes where "IdOrigen" > 0;   
        
         -- insertamos las contrapartes 

	      insert into "BSCL"."BSCL.SocioNegocio.BD::CNT.Contraparte" (
	        "IdContraparte","IdPersona","IdEmpresa","CodigoInscripcion","Emisor","Depositos","Bolsa","Corredora"
			,"CamaraDeCompensacion","Banco","Boveda","IdEstado","EsEnrolado","Borrado","FechaIngreso","FechaCreacion"
			,"FechaModificacion","IdUsuarioCreador","IdTipoBloqueo","FechaBloqueo","ComentarioBloqueo" ,"IdTipoDesbloqueo" 
			,"FechaDesbloqueo","ComentarioDesbloqueo","IdCarga","IdOrigen")
			
			select "IdContraparte","IdPersona","IdEmpresa","CodigoInscripcion","EsEmisor","EsDeposito","EsBolsa","EsCorredora"
			,"EsContraparteCentral","EsBanco","EsBoveda","IdEstado","EsEnrolado","Borrado","FechaIngreso","FechaCreacion"
			,"FechaModificacion","IdUsuarioCreador","IdTipoBloqueo","FechaBloqueo","ComentarioBloqueo" ,"IdTipoDesbloqueo" 
			,"FechaDesbloqueo","ComentarioDesbloqueo","IdCarga","IdOrigen"
			from :v_NuevasContrapartes1
			where "IdOrigen" > 0;

			-- debemos actualizar la tabla socio de negocio en el campo idcontraparte --
            -- la tabla nuevas contrapartes siempre tendra por  lo menos un registro  --

           UPDATE A5 set A5."IdContraparte" = A4."IdContraparte"
		     FROM :v_NuevasContrapartes1 as A1
		       INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as A2
		       ON A2."DocumentoIdentidad"= A1."Rut"
		       INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as A3
		       ON A3."IdDocumentoIdentidad" = A2."IdDocumentoIdentidad"
		       INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::CNT.Contraparte" as A4
		       ON A4."IdPersona" = A3."IdPersona"
		       INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" as A5
		       ON A5."IdPersona" = A4."IdPersona";
		        
		-- en el caso que la contraparte tenga persona debemso actualizar su nombre  y razon social
        update PER SET PER."Nombres"     = COALESCE("NombreFantasiaBolsa","NombreFantasiaBanco","NombreFantasiaDeposito","NombreFantasiaEmisores","NombreFantasiaContraparteCentral","NombreFantasiaCorredora",'')
                       ,PER."RazonSocial" = COALESCE("NombreFantasiaBolsa","NombreFantasiaBanco","NombreFantasiaDeposito","NombreFantasiaEmisores","NombreFantasiaContraparteCentral","NombreFantasiaCorredora",'')
        FROM "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" AS PER
		INNER JOIN :v_contrapartesagrupados AS AGRU
		ON PER."IdOrigen" = CASE WHEN LOCATE (AGRU."Rut",'-') = 0 then "Rut" ELSE  SUBSTR_BEFORE (AGRU."Rut", '-') END;
	
        -- en el caso que hayan contrapartes que no tengan persona cargaremos los datos basicos en caso de no contar 
        -- con la informacion de direcciones y telefonos ira direccion por defecto para ellos luego se deberan corregir por sistema		
		-- recuperamos los valores de la persona en caso que la persona exista no debe ingresarse

		v_DatosPersona_aux =select  '' as "IdEstado" 
								,0 as "IdDocumentoIdentidad" 
								,'' as "Tipo"
								,'' as "Nombres"
								,'' as "ApellidoPaterno"
								,'' as "ApellidoMaterno"
								,'' as "RazonSocial"
								,0	as "IdGiroPrincipal"
								,0	as "IdPaisNacimiento"
								,0	as "IdPaisResidencia"
								,'0001-01-01' as "FechaNacimiento"
								,'0001-01-01' as "FechaDefuncion"
								,'' as "Sexo"
								,''	as "EsExtranjero"
								,''	as "EstadoCivil"
								,''	as "RegimenConyugal"
								,'' as "Cargo"
								,'' as "Empleador" 
								,0  as "IdDireccionFisicaPrincipal" 					
								,0 	as "IdDireccionElectronicaPrincipal"		
								,0 	as "IdTelefonoPrincipal"	
								,389 as "IdProfesion" 	
								,'0001-01-01'	as "FechaInicioActividad"
								,'0001-01-01'	as "FechaIngreso"
								,'0001-01-01'	as "FechaCreacion" 			
								,'0001-01-01'	as "FechaModificacion"
								,'' as "EsEnrolado"
								,0 	as "IdCarga"
								,''	as "Alias"
								,'0' as "IdOrigen"
								,'0-0'  as "Rut"
							from dummy;
               v_DatosPersona_aux = select 		
									'ACT' as "IdEstado" 
									,0 as "IdDocumentoIdentidad" 
									,'J' as "Tipo"
									,COALESCE("NombreFantasiaBolsa","NombreFantasiaBanco","NombreFantasiaDeposito","NombreFantasiaEmisores","NombreFantasiaContraparteCentral","NombreFantasiaCorredora",'') as "Nombres"
									,'' as "ApellidoPaterno"
									,'' as "ApellidoMaterno"
									,COALESCE("RazonSocialBolsa","RazonSocialBanco","RazonSocialDeposito","RazonSocialEmisores","RazonSocialContraparteCentral","RazonSocialCorredora",'') as "RazonSocial"
									,0 							as "IdGiroPrincipal"
									,null						as "IdPaisNacimiento"
									,44   						as "IdPaisResidencia"
									,'0001-01-01'				as "FechaNacimiento"
									,'0001-01-01'               as "FechaDefuncion"
									,'N'     					as "Sexo"
									,'N'						as "EsExtranjero"
									,null					 	as "EstadoCivil"
									,null 						as "RegimenConyugal"
									,''                         as "Cargo"
									,''                         as "Empleador" 
									,0 							as "IdDireccionFisicaPrincipal" 					
									,0 							as "IdDireccionElectronicaPrincipal"		
									,0 							as "IdTelefonoPrincipal"	
									,389						as "IdProfesion" 	
									,'0001-01-01'				as "FechaInicioActividad"
									,'0001-01-01'  				as "FechaIngreso"
									,'0001-01-01'  				as "FechaCreacion" 			
									,'0001-01-01'  				as "FechaModificacion"
									,'S'                        as "EsEnrolado"
									,:p_IdCarga 				as "IdCarga"
									,'' 						as "Alias"
									, CASE WHEN LOCATE (AGRU."Rut",'-') = 0 then "Rut" ELSE  SUBSTR_BEFORE (AGRU."Rut", '-') END as "IdOrigen"
									,"Rut" as "Rut"
								from :v_contrapartesagrupados AS AGRU
							    LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" AS PER
							    ON PER."IdOrigen" = CASE WHEN LOCATE (AGRU."Rut",'-') = 0 then "Rut" ELSE  SUBSTR_BEFORE (AGRU."Rut", '-') END
							    WHERE PER."IdOrigen" IS NULL
							    UNION ALL
							    SELECT * FROM :v_DatosPersona_aux;
							    
		v_DatosPersona = select "BSCL"."BSCL.SocioNegocio.BD.Secuencias::Persona".NEXTVAL as "IdPersona", * 
		                 from :v_DatosPersona_aux; 					    
 
		-- recuperamos los datos de los documentos de identidad
        -- estos no deben existir en la tabla de documento de identidad en caso de existir no se deben insertar
 		
		v_DocumentoIdentidadPersona = 	select
											"BSCL"."BSCL.SocioNegocio.BD.Secuencias::DocumentoIdentidad".NEXTVAL as "IdDocumentoIdentidad" 
											,PERSONA."IdPersona" as "IdPersona"
											,PERSONA."Rut" as "DocumentoIdentidad"
											,CASE WHEN LOCATE (PERSONA."Rut",'-') = 0 then  'X' ELSE 'R' END as "IdTipoDocumentoIdentidad"
											,'' AS "NumeroSerie"
											,'0001-01-01' as "FechaEmision"
											,'0001-01-01' as "FechaVencimiento"
											,'ACT' AS "IdEstado"
											,'0001-01-01 00:00:00' AS "FechaIngreso"
											,'0001-01-01 00:00:00' AS "FechaCreacion"
											,'0001-01-01 00:00:00' AS "FechaModificacion"
											,:p_IdCarga as "IdCarga"
											,'ING' as "IdEstadoAutorizacion"
											,PERSONA."IdOrigen"
										from :v_DatosPersona AS PERSONA
										LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC
										ON PERSONA."Rut" = DOC."DocumentoIdentidad"
										WHERE DOC."IdDocumentoIdentidad" IS NULL
										AND PERSONA."IdOrigen" > 0;

		 -- debemos ingresar los datos de contacto direcciones fisica y electronica y telefono
         -- como no tenemos la informacion en el sistema debemos  ingresar informacion por default
         -- esta informacion en caso de ser encontrada debe ser corregida por sistema o se debera crear 
         -- un script  masivo o mejorar este servicio.

         -- direccion Fisica 
         v_DireccionFisica = select 
									"BSCL"."BSCL.SocioNegocio.BD.Secuencias::DireccionFisica".NEXTVAL as "IdDireccionFisica" -- Generar  Secuencia
									,'POR DEFINIR'		 		as "Calle"
						 			,'9999999999999'	        	as "Numero" 
						 			,'POR DEFINIR'			 	as "Departamento"
						 			,'' 							as "Sector"
						 			,'XXXX'							as "CodigoPostal" 
						 			,2931							as "IdCiudad"
						 			,'' 							as "CodigoProposito"
						 			,'' 							as "Comentario" 
						 			,'ACT' 							as "IdEstado"
									,PERSONA."IdPersona"			as "IdPersona" -- aun no tengo ID
									,'ING' 							as "IdEstadoAutorizacion"
									,p_IdCarga					 	as "IdCarga"
									,PERSONA."IdOrigen"				as "IdOrigen"
								from :v_DatosPersona AS PERSONA
							 	LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DIR
							 	ON PERSONA."IdOrigen" = DIR."IdOrigen"
							 	WHERE DIR."IdOrigen" IS NULL;
		
		--direccion electronica
		v_DireccionElectronica = select 
								"BSCL"."BSCL.SocioNegocio.BD.Secuencias::DireccionElectronica".NEXTVAL as "IdDireccionElectronica" 
								,'POR DEFINIRY@PORDEFINIR.COM' 	as "Direccion"
					 			,'' 							as "CodigoProposito"  
					 			,''								as "Comentario" 
					 			,'ACT' 							as "IdEstado"
								,PERSONA."IdPersona"			as "IdPersona" -- aun no tengo ID
								,'ING' 							as "IdEstadoAutorizacion"
								,p_IdCarga					 	as "IdCarga"
								,PERSONA."IdOrigen"				as "IdOrigen"
							    from :v_DatosPersona AS PERSONA
							    LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DIR
								ON DIR."IdOrigen" = DIR."IdOrigen"
								WHERE DIR."IdOrigen" IS NULL;
		-- telefono
        v_Telefono =	select
							"BSCL"."BSCL.SocioNegocio.BD.Secuencias::Telefono".NEXTVAL as "IdTelefono" 
							, 44 							as "IdPais"
							, '5699999999'					as "Numero"	
							, '' 							as "Anexo"
							, 'TTELEMOVIL'					as "CodigoProposito" 
							, '' 							as "Comentario"
						 	, 'ACT'							as "IdEstado"
						 	, PERSONA."IdPersona"			as "IdPersona"
						 	,'ING'							as "IdEstadoAutorizacion"
							,p_IdCarga 					    as "IdCarga"
							,PERSONA."IdOrigen"				as "IdOrigen"
						from :v_DatosPersona AS PERSONA
						LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Telefono" AS TEL
						ON PERSONA."IdOrigen" = TEL."IdOrigen"
						WHERE TEL."IdOrigen" IS NULL;
			
          -- comenzamos con la insercion de las tablas desde persona.
          -- antes de realizar la insercion debemos revisar que tengan datos para evitar errores.

	        	insert into "BSCL"."BSCL.SocioNegocio.BD::PER.Persona"(				
						"IdPersona","IdEstado","IdDocumentoIdentidad","Tipo","Nombres","ApellidoPaterno","ApellidoMaterno","RazonSocial"
						,"IdGiroPrincipal","IdPaisNacimiento","IdPaisResidencia","FechaNacimiento","FechaDefuncion","Sexo"
						,"EsExtranjero","EstadoCivil","RegimenConyugal","Cargo","Empleador","IdDireccionFisicaPrincipal","IdDireccionElectronicaPrincipal"		
						,"IdTelefonoPrincipal","IdProfesion","FechaInicioActividad","FechaIngreso","FechaCreacion","FechaModificacion"
						,"EsEnrolado","IdCarga","Alias","IdOrigen"
				)
	            select 
	                 "IdPersona","IdEstado","IdDocumentoIdentidad","Tipo","Nombres","ApellidoPaterno","ApellidoMaterno","RazonSocial"
					,"IdGiroPrincipal","IdPaisNacimiento","IdPaisResidencia","FechaNacimiento","FechaDefuncion","Sexo"
					,"EsExtranjero","EstadoCivil","RegimenConyugal","Cargo","Empleador","IdDireccionFisicaPrincipal","IdDireccionElectronicaPrincipal"		
					,"IdTelefonoPrincipal","IdProfesion","FechaInicioActividad","FechaIngreso","FechaCreacion","FechaModificacion"
					,"EsEnrolado","IdCarga","Alias","IdOrigen"
				 from :v_DatosPersona where "IdOrigen" > 0;
				 
               -- cargo documentos de identidad
	           INSERT INTO "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"(
	             "IdDocumentoIdentidad","IdPersona","DocumentoIdentidad","IdTipoDocumentoIdentidad","NumeroSerie"
				,"FechaEmision","FechaVencimiento","IdEstado","FechaIngreso","FechaCreacion","FechaModificacion"
				,"IdCarga","IdEstadoAutorizacion","IdOrigen")
			   SELECT 
	             "IdDocumentoIdentidad","IdPersona","DocumentoIdentidad","IdTipoDocumentoIdentidad","NumeroSerie"
				,"FechaEmision","FechaVencimiento","IdEstado","FechaIngreso","FechaCreacion","FechaModificacion"
				,"IdCarga","IdEstadoAutorizacion","IdOrigen"
			   FROM :v_DocumentoIdentidadPersona
			   WHERE "IdOrigen" > 0;				 
				 
              -- recuperamos generamos las contrapartes asociadas a las personas que estamos creando 
              -- anteriormente habiamos creados contrapartes para aquellas personas que estaban en el sistema
              -- y no estaban en la tabla contraparte.
 
              v_contrapartes_aux2 = select 
									 0 as  "IdPersona"	
									,0 as  "IdEmpresa"
									,0 as  "CodigoInscripcion"	
									,'' as "EsEmisor"
									,'' as "EsDeposito"	
									,'' as "EsBolsa"
									,'' as "EsCorredora"	
									,'' as "EsContraparteCentral" 
									,'' as "EsBanco"	
									,'' as "EsBoveda"	
									,'' as "IdEstado"	
									,'' as "EsEnrolado"	
									,'' as "Borrado"
									,'0001-01-01' as "FechaIngreso"
									,'0001-01-01' as "FechaCreacion"
									,'0001-01-01' as "FechaModificacion"
									, 0 as "IdUsuarioCreador"
									, '' as "IdTipoBloqueo"
									,'0001-01-01' as "FechaBloqueo"
									,'' as "ComentarioBloqueo" 	
									,'' as "IdTipoDesbloqueo" 	
									,'0001-01-01' as "FechaDesbloqueo"
									,'' as "ComentarioDesbloqueo"
									,'0-0' as "Rut"
                                    ,p_IdCarga  as "IdCarga"
                                    ,0  as "IdOrigen" 
						     FROM dummy;
						     
              v_contrapartes_aux2 =  select distinct
									 A3."IdPersona" as "IdPersona"	
									,p_IdEmpresa as "IdEmpresa"
									,0 as "CodigoInscripcion"	
									,"EsEmisor"
									,"EsDeposito"	
									,"EsBolsa"
									,"EsCorredora"	
									,"EsContraparteCentral" 
									,"EsBanco"	
									,'N' as "EsBoveda"	
									,'ACT' as "IdEstado"	
									,'S' as "EsEnrolado"	
									,'N' as "Borrado"
									,'0001-01-01' as "FechaIngreso"
									,'0001-01-01' as "FechaCreacion"
									,'0001-01-01' as "FechaModificacion"
									, p_IdUsuario as "IdUsuarioCreador"
									, null as "IdTipoBloqueo"
									,'0001-01-01' as "FechaBloqueo"
									,'' as "ComentarioBloqueo" 	
									,null as "IdTipoDesbloqueo" 	
									,'0001-01-01' as "FechaDesbloqueo"
									,'' as "ComentarioDesbloqueo"
									,A1."Rut" as "Rut"
									,p_IdCarga as "IdCarga"
									,CASE WHEN LOCATE (A1."Rut",'-') = 0 then A1."Rut" ELSE  SUBSTR_BEFORE (A1."Rut", '-') END as "IdOrigen"
						   FROM :v_contrapartesagrupados as A1
                           INNER JOIN :v_DatosPersona as A2
                           ON A2."IdOrigen" = CASE WHEN LOCATE (A1."Rut",'-') = 0 then A1."Rut" ELSE  SUBSTR_BEFORE (A1."Rut", '-') END
                           INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" as A3
                           ON A3."IdOrigen" = A2."IdOrigen"
                           LEFT JOIN "BSCL.SocioNegocio.BD::CNT.Contraparte" as A4
                           ON A4."IdOrigen" = A3."IdOrigen"
					       WHERE A4."IdOrigen" IS NULL
					       union all
					       select * from :v_contrapartes_aux2; 

					 v_contrapartes2 = select "BSCL.SocioNegocio.BD.Secuencias::Contraparte".NEXTVAL as "IdContraparte", * 
					                   from :v_contrapartes_aux2;
		 
		  -- insertamos las contrapartes

           	       insert into "BSCL.SocioNegocio.BD::CNT.Contraparte" (
			        "IdContraparte","IdPersona","IdEmpresa","CodigoInscripcion","Emisor","Depositos","Bolsa","Corredora"
					,"CamaraDeCompensacion","Banco","Boveda","IdEstado","EsEnrolado","Borrado","FechaIngreso","FechaCreacion"
					,"FechaModificacion","IdUsuarioCreador","IdTipoBloqueo","FechaBloqueo","ComentarioBloqueo" ,"IdTipoDesbloqueo" 
					,"FechaDesbloqueo","ComentarioDesbloqueo","IdCarga","IdOrigen")
					
					select A1."IdContraparte",A1."IdPersona",A1."IdEmpresa",A1."CodigoInscripcion",A1."EsEmisor",A1."EsDeposito",A1."EsBolsa"
						,A1."EsCorredora",A1."EsContraparteCentral",A1."EsBanco",A1."EsBoveda",A1."IdEstado",A1."EsEnrolado",A1."Borrado"
						,A1."FechaIngreso",A1."FechaCreacion",A1."FechaModificacion",A1."IdUsuarioCreador",A1."IdTipoBloqueo"
						,A1."FechaBloqueo",A1."ComentarioBloqueo",A1."IdTipoDesbloqueo",A1."FechaDesbloqueo",A1."ComentarioDesbloqueo" 
					    ,A1."IdCarga",A1."IdOrigen"
					from :v_contrapartes2 AS A1
					LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD::CNT.Contraparte" AS A2
					ON A2."IdOrigen" = A1."IdOrigen"
					AND A2."IdContraparte" IS NULL
					WHERE A1."IdOrigen" > 0;

             -- insertamos los registro en la tabla socio de negocio con el campo IdContraparte actualizado 
             			
				--insertamos la tabla socio de negocio
	            insert into "BSCL"."BSCL.SocioNegocio.BD::SOC.SocioDeNegocio"
	            ("IdSocioDeNegocio","IdPersona","IdEmpresa","IdCliente"
				,"IdEmpleado","IdOperador","IdProveedor","IdContraparte"
				,"Borrado")
				select "BSCL.SocioNegocio.BD.Secuencias::SocioDeNegocio".NEXTVAL as "IdSocioDeNegocio"
				      ,A1."IdPersona"
				      ,p_IdEmpresa
				      ,null
				      ,null
				      ,null
				      ,null
				      ,A2."IdContraparte"
				      ,'N'
				from :v_DatosPersona AS A1 
				     ,:v_contrapartes2 AS A2
				where A1."IdOrigen" = A2."IdOrigen"
				and A1."IdOrigen" > 0;      

			   -- actualizamos a la persona con el correpondiente documento identidad asociado
               update A1 set A1."IdDocumentoIdentidad" = A2."IdDocumentoIdentidad"
               from "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" AS A1
               ,:v_DocumentoIdentidadPersona AS A2 
               WHERE A2."IdPersona" = A1."IdPersona";
		  
          --reviso si tengo direcciones fisicas
	         insert into "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionFisica"(
	             "IdDireccionFisica","Calle","Numero","Departamento","Sector" 					
				,"CodigoPostal","IdCiudad","CodigoProposito","Comentario" 				
				,"IdEstado","IdPersona","IdEstadoAutorizacion","IdCarga","IdOrigen")
				
			   select  "IdDireccionFisica","Calle","Numero","Departamento","Sector" 					
				,"CodigoPostal","IdCiudad","CodigoProposito","Comentario" 				
				,"IdEstado","IdPersona","IdEstadoAutorizacion","IdCarga","IdOrigen"
			   from :v_DireccionFisica
			   WHERE "IdOrigen" >0;

			  -- actualizo la direccion fisica de la persona 
            update A1 set A1."IdDireccionFisicaPrincipal" = A2."IdDireccionFisica"
              from "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" AS  A1
              ,:v_DireccionFisica AS A2 
              WHERE A2."IdPersona" = A1."IdPersona";

          --reviso si tengo direcciones electronicas
           insert into "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionElectronica"
           ("IdDireccionElectronica","Direccion","CodigoProposito","Comentario","IdEstado"
             ,"IdPersona","IdEstadoAutorizacion","IdCarga","IdOrigen")
           select  "IdDireccionElectronica","Direccion","CodigoProposito","Comentario","IdEstado"
             ,"IdPersona","IdEstadoAutorizacion","IdCarga","IdOrigen"
           from :v_DireccionElectronica
           WHERE "IdOrigen" >0;
          
           -- actualizo la direccion electronica de la persona
           update A1 set A1."IdDireccionElectronicaPrincipal" = A2."IdDireccionElectronica"
              from "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" AS A1
              ,:v_DireccionElectronica AS A2 
              WHERE A2."IdPersona" = A1."IdPersona";
          -- reviso si tengo telefonos

            INSERT INTO "BSCL"."BSCL.SocioNegocio.BD::PER.Telefono"
            ("IdTelefono","IdPais","Numero","Anexo","CodigoProposito","Comentario","IdEstado"	
             ,"IdPersona","IdEstadoAutorizacion","IdCarga","IdOrigen")
            select "IdTelefono","IdPais","Numero","Anexo","CodigoProposito","Comentario","IdEstado"	
             ,"IdPersona","IdEstadoAutorizacion","IdCarga","IdOrigen"
            from :v_Telefono
            WHERE "IdOrigen" > 0;
            
            -- actualizo telefono de la persona
            update A1 set A1."IdTelefonoPrincipal" = A2."IdTelefono"
            from "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" AS  A1
              ,:v_Telefono AS A2 
            WHERE A2."IdPersona" = A1."IdPersona";
              
        
        --actualizamos los bancos con su idpersona correspondiente
        
        update BAN set BAN."IdPersona" = CNT."IdPersona"
		from "BSCL"."BSCL.SocioNegocio.BD::CNT.Contraparte" as CNT
		inner join "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOC
		ON CNT."IdPersona" = DOC."IdPersona"
		inner join "BSCL"."BSCL.Plataforma.BD::BAN.Banco" as BAN
		ON BAN."RutBanco" = DOC."DocumentoIdentidad";
        
        --insertamos en la tabla contraparte de homologacion las contrapartes ingresadas por el servicio 
        

        insert into "BSCL"."BSCL.Homologacion.BD::HOM.Contraparte" (
	        "IdContraparte","IdPersona","IdEmpresa","CodigoInscripcion","Emisor","Depositos","Bolsa","Corredora"
			,"CamaraDeCompensacion","Banco","Boveda","IdEstado","EsEnrolado","Borrado","FechaIngreso","FechaCreacion"
			,"FechaModificacion","IdUsuarioCreador","IdTipoBloqueo","FechaBloqueo","ComentarioBloqueo" ,"IdTipoDesbloqueo" 
			,"FechaDesbloqueo","ComentarioDesbloqueo","IdCarga","IdOrigen")
		select A1."IdContraparte",A1."IdPersona",A1."IdEmpresa",A1."CodigoInscripcion",A1."EsEmisor",A1."EsDeposito",A1."EsBolsa",A1."EsCorredora"
			,A1."EsContraparteCentral",A1."EsBanco",A1."EsBoveda",A1."IdEstado",A1."EsEnrolado",A1."Borrado",A1."FechaIngreso",A1."FechaCreacion"
			,A1."FechaModificacion",A1."IdUsuarioCreador",A1."IdTipoBloqueo",A1."FechaBloqueo",A1."ComentarioBloqueo" ,A1."IdTipoDesbloqueo" 
			,A1."FechaDesbloqueo",A1."ComentarioDesbloqueo",A1."IdCarga",A1."IdOrigen"
	    from :v_NuevasContrapartes1 AS A1
	    left join "BSCL.Homologacion.BD::HOM.Contraparte" AS A2
        on A2."IdOrigen" = A1."IdOrigen"
	    where A1."IdOrigen" > 0
	    and A2."IdOrigen" IS NULL;
	    
	  drop table  #v_ContraparteMigracion; 
	    
	  -- contabilizamos las contrapartes guardadas
 
	  select count(*) into v_ContrapartesLeidas from "BSCL"."BSCL.Homologacion.BD::HOM.Contraparte" 
	  where "IdCarga" = p_IdCarga;   
	  
	  select count(*) into v_ContrapartesCargadas from "BSCL"."BSCL.SocioNegocio.BD::CNT.Contraparte" 
	  where "IdCarga" = p_IdCarga;   

       p_ResumenDatosProcesados = select 'CARGACONTRAPARTES' AS "CodigoProceso"
      									, 'SOC' AS "Modulo" 
      									, v_ContrapartesLeidas AS "TotalRegistrosSebra"
      									, v_ContrapartesCargadas as "TotalRegistrosSap"
      							 from dummy;

       IF v_ContrapartesLeidas <>  v_ContrapartesCargadas THEN		
	    --- Listar clientes no grabados en Clientes, eliminarlos del modelo completo (persona, Documento, documentopersona y portafolio) y grabar en LOG
			v_hash= select 'DatosLeidos'   as "Codigo", v_ContrapartesLeidas   as "Valor" FROM DUMMY UNION ALL
			        select 'DatosGrabados' as "Codigo", v_ContrapartesCargadas as "Valor" FROM DUMMY;
			CALL "BSCL"."BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('CARGA_CON_DIFERENCIAS',:v_hash,:v_erroroutmsg);
			p_out_ErroresDetalle =  select 'CARGA_CON_DIFERENCIAS' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy; 
			p_out_ErroresContador := p_out_ErroresContador + 1;
		END IF ;

		SIGNAL SALIDA_CONTROLADA;    
	END;