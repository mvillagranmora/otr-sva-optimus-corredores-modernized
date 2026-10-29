DROP PROCEDURE "BSCL"."BSCL.Homologacion.BD.Servicios::ActualizarIndicadoresCliente";

CREATE PROCEDURE "BSCL"."BSCL.Homologacion.BD.Servicios::ActualizarIndicadoresCliente"
 (
 
 IN  p_RutCliente			VARCHAR(40),
 IN  p_IdCarga              VARCHAR(40),
 IN  p_IdEmpresa            BIGINT,
 OUT p_out_ErroresContador int,
 OUT p_out_ErroresDetalle table ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520))
 ) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
AS
BEGIN
    -- ESTE SERVICIO PERMITE ACTUALIZAR LA CATEGORIA DE INVERSIONISTA Y EL SEGMENTO DE CLIENTE
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" NVARCHAR(40),"Valor" NVARCHAR(5000));
	DECLARE v_erroroutmsg  NVARCHAR(5000);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;		 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	
	p_out_ErroresContador		:= 0;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	BEGIN	
		
		DECLARE v_PerId BIGINT :=0;
		DECLARE v_IdTipoIndicador NVARCHAR(100):='';
		SELECT SUBSTR_BEFORE(p_RutCliente,'-') INTO v_PerId FROM DUMMY;
		
		-- insertamos los indicadores normativos art 100
        
         select "IdTipoIndicador" into v_IdTipoIndicador
		   from "BSCL.Plataforma.BD::IND.TipoIndicador"	
         where  "IdTipoEntidad" = 'CLIENTE' 
			and "GrupoIndicador" ='NORMATIVOS'
			and "CodigoTipoIndicador"= 'RELCORR';

         insert into "BSCL.Plataforma.BD::IND.Indicador" ("IdIndicador"
         ,"IdEntidad","IdTipoEntidad","IdTipoIndicador","ModeloCliente","IdEmpresa","ValorIndicador"
         ,"FechaIngreso","FechaCreacion","FechaModificacion","IdEstadoAutorizacion","FechaVencimiento","IdCarga")
         Select "BSCL.Plataforma.BD.Secuencias::Indicador".NEXTVAL as "IdIndicador" 			  
               , ClienteH."IdCliente" as "IdEntidad"
               ,'CLIENTE' as "TipoEntidad"
               ,v_IdTipoIndicador  as "IdTipoIndicador"
               ,'' as "ModeloCliente"
               ,p_IdEmpresa as "IdEmpresa"
               ,'S' as "ValorIndicador"
               ,'0001-01-01' as "FechaIngreso"
               ,'0001-01-01' as "FechaCreacion"
               ,'0001-01-01' as "FechaModificacion"
               ,'ING' as "IdEstadoAutorizacion"
               ,'0001-01-01' as "FechaVencimiento"
               ,p_IdCarga as "IdCarga"				  
		 from "BSCL_VIRTUAL"."GES_PERSONA" AS PERSONA
			inner join "BSCL_VIRTUAL"."GES_CLIENTE" AS  CLIENTE 
				on PERSONA."per_id" = CLIENTE."per_id"
				and PERSONA."per_id" = case when v_PerId = '0' then PERSONA."per_id" else v_PerId end 				
				and CLIENTE."par_rel_corre" <> 'RELACNINGU' 
			inner join "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PersonaH 
				on  PersonaH."IdOrigen" = PERSONA."per_id"
			inner join "BSCL.SocioNegocio.BD::CLI.Cliente" as ClienteH
				on ClienteH."IdPersona" = PersonaH."IdPersona"
		  where not exists (select 1 from 
		                          "BSCL.Plataforma.BD::IND.Indicador" as indicador2
		                         inner join "BSCL.Plataforma.BD::IND.TipoIndicador" as TipoIndicador2
		                           on TipoIndicador2."IdTipoIndicador" = indicador2."IdTipoIndicador" 
		                           and TipoIndicador2."CodigoTipoIndicador"= 'RELCORR'
		                         where  indicador2."IdEntidad" = ClienteH."IdCliente"
		                         and indicador2."IdTipoEntidad" ='CLIENTE' 
		                     );   
			
				 		  
	    -- insertamos valores de indicadores 1064
	    -- tipo relacion con la corredora.

         select "IdTipoIndicador" into v_IdTipoIndicador
		   from "BSCL.Plataforma.BD::IND.TipoIndicador"	
         where  "IdTipoEntidad" = 'CLIENTE' 
			and "GrupoIndicador" ='NORMATIVOS'
			and "CodigoTipoIndicador"= '1064PROP';

         -- se inserta el tipo de indicador de 1064 no obstante existe aquellos con una relacion de 
         -- sociedad de inversion que no se encuentran en los indicadores de la 1064 y se encuentran en produccion
         -- aquellos clientes con dicho indicador no seran cargados hasta que exista un definicion.

         insert into "BSCL.Plataforma.BD::IND.Indicador" ("IdIndicador"
         ,"IdEntidad","IdTipoEntidad","IdTipoIndicador","ModeloCliente","IdEmpresa","ValorIndicador"
         ,"FechaIngreso","FechaCreacion","FechaModificacion","IdEstadoAutorizacion","FechaVencimiento","IdCarga")
         Select "BSCL.Plataforma.BD.Secuencias::Indicador".NEXTVAL as "IdIndicador" 			  
               , ClienteH."IdCliente" as "IdEntidad"
               , 'CLIENTE' as "TipoEntidad"
               , v_IdTipoIndicador  as "IdTipoIndicador"
               , '' as "ModeloCliente"
               , p_IdEmpresa as "IdEmpresa"
               , DETALLE_INDICADOR."IdDetalleIndicador" as "ValorIndicador"
               , '0001-01-01' as "FechaIngreso"
               , '0001-01-01' as "FechaCreacion"
               , '0001-01-01' as "FechaModificacion"
               , 'ING' as "IdEstadoAutorizacion"
               , '0001-01-01' as "FechaVencimiento"
               , p_IdCarga as "IdCarga"
		 from "BSCL_VIRTUAL"."GES_PERSONA" AS PERSONA
			inner join "BSCL_VIRTUAL"."GES_CLIENTE" AS  CLIENTE 
				on PERSONA."per_id" = CLIENTE."per_id"
				and PERSONA."per_id" = case when v_PerId = '0' then PERSONA."per_id" else v_PerId end 				
			inner join "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PersonaH 
				on  PersonaH."IdOrigen" = PERSONA."per_id"
			inner join "BSCL.SocioNegocio.BD::CLI.Cliente" as ClienteH
				on ClienteH."IdPersona" = PersonaH."IdPersona"
			inner join "BSCL.Homologacion.BD::HOM.Homologacion" AS HOMOLOGACION
			    on HOMOLOGACION."TipoValor" = 'TipoRelacionCliente'
			    and HOMOLOGACION."ValorOrigen" = CLIENTE."par_rel_corre"
			inner join "BSCL.Plataforma.BD::IND.DetalleIndicador" AS DETALLE_INDICADOR
			    on DETALLE_INDICADOR."IdTipoIndicador" = v_IdTipoIndicador
			    and DETALLE_INDICADOR."Codigo" = HOMOLOGACION."ValorDestino"
		  where not exists (select 1 from 
		                          "BSCL.Plataforma.BD::IND.Indicador" as indicador2
		                         inner join "BSCL.Plataforma.BD::IND.TipoIndicador" as TipoIndicador2
		                           on TipoIndicador2."IdTipoIndicador" = indicador2."IdTipoIndicador" 
		                           and TipoIndicador2."CodigoTipoIndicador"= '1064PROP'
		                         where  indicador2."IdEntidad" = ClienteH."IdCliente"
		                         and indicador2."IdTipoEntidad" ='CLIENTE' 
		                     );                   
		
		-- relacion con la corredora 380
         select "IdTipoIndicador" into v_IdTipoIndicador
		   from "BSCL.Plataforma.BD::IND.TipoIndicador"	
         where  "IdTipoEntidad" = 'CLIENTE' 
			and "GrupoIndicador" = 'NORMATIVOS'
			and "CodigoTipoIndicador"= 'NCG380';     
		
         insert into "BSCL.Plataforma.BD::IND.Indicador" ("IdIndicador"
         ,"IdEntidad","IdTipoEntidad","IdTipoIndicador","ModeloCliente","IdEmpresa","ValorIndicador"
         ,"FechaIngreso","FechaCreacion","FechaModificacion","IdEstadoAutorizacion","FechaVencimiento","IdCarga")
         Select "BSCL.Plataforma.BD.Secuencias::Indicador".NEXTVAL as "IdIndicador" 			  
               , ClienteH."IdCliente" as "IdEntidad"
               , 'CLIENTE' as "TipoEntidad"
               , v_IdTipoIndicador  as "IdTipoIndicador"
               , '' as "ModeloCliente"
               ,  p_IdEmpresa as "IdEmpresa"
               ,  DETALLE_INDICADOR."IdDetalleIndicador"  as "ValorIndicador"
               , '0001-01-01' as "FechaIngreso"
               , '0001-01-01' as "FechaCreacion"
               , '0001-01-01' as "FechaModificacion"
               , 'ING' as "IdEstadoAutorizacion"
               , '0001-01-01' as "FechaVencimiento"
               , p_IdCarga as "IdCarga"
		 from "BSCL_VIRTUAL"."GES_PERSONA" AS PERSONA
			inner join "BSCL_VIRTUAL"."GES_CLIENTE" AS  CLIENTE 
				on PERSONA."per_id" = CLIENTE."per_id"
				and PERSONA."per_id" = case when v_PerId = '0' then PERSONA."per_id" else v_PerId end 				
			inner join "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PersonaH 
				on  PersonaH."IdOrigen" = PERSONA."per_id"
			inner join "BSCL.SocioNegocio.BD::CLI.Cliente" as ClienteH
				on ClienteH."IdPersona" = PersonaH."IdPersona"
			inner join "BSCL.Homologacion.BD::HOM.Homologacion" AS HOMOLOGACION
			    on HOMOLOGACION."TipoValor" = 'TipoRelacionCliente'
			    and HOMOLOGACION."ValorOrigen" = CLIENTE."par_tip_ncg" 
			inner join "BSCL.Plataforma.BD::IND.DetalleIndicador" AS DETALLE_INDICADOR
			    on DETALLE_INDICADOR."IdTipoIndicador" = v_IdTipoIndicador
			    and DETALLE_INDICADOR."Codigo" = HOMOLOGACION."ValorDestino" 
		  where not exists (select 1 from 
		                          "BSCL.Plataforma.BD::IND.Indicador" as indicador2
		                         inner join "BSCL.Plataforma.BD::IND.TipoIndicador" as TipoIndicador2
		                           on TipoIndicador2."IdTipoIndicador" = indicador2."IdTipoIndicador" 
		                           and TipoIndicador2."CodigoTipoIndicador"= 'NCG380'
		                         where  indicador2."IdEntidad" = ClienteH."IdCliente"
		                         and indicador2."IdTipoEntidad" ='CLIENTE' 
		                     );
		                     
		 -- contrato de condiciones generales futuro
         select "IdTipoIndicador" into v_IdTipoIndicador
		   from "BSCL.Plataforma.BD::IND.TipoIndicador"	
         where  "IdTipoEntidad" = 'CLIENTE' 
			and "GrupoIndicador" = 'Contratos'
			and "CodigoTipoIndicador"= 'CCGF';  

         insert into "BSCL.Plataforma.BD::IND.Indicador" ("IdIndicador"
         ,"IdEntidad","IdTipoEntidad","IdTipoIndicador","ModeloCliente","IdEmpresa","ValorIndicador"
         ,"FechaIngreso","FechaCreacion","FechaModificacion","IdEstadoAutorizacion","FechaVencimiento","IdCarga")
         Select "BSCL.Plataforma.BD.Secuencias::Indicador".NEXTVAL as "IdIndicador" 			  
               , ClienteH."IdCliente" as "IdEntidad"
               , 'CLIENTE' as "TipoEntidad"
               , v_IdTipoIndicador  as "IdTipoIndicador"
               , '' as "ModeloCliente"
               ,  p_IdEmpresa as "IdEmpresa"
               , INDICADOR_CLIENTE."din_id"  as "ValorIndicador"
               , '0001-01-01' as "FechaIngreso"
               , '0001-01-01' as "FechaCreacion"
               , '0001-01-01' as "FechaModificacion"
               , 'ING' as "IdEstadoAutorizacion"
               , '0001-01-01' as "FechaVencimiento"
               , p_IdCarga as "IdCarga"
		 from "BSCL_VIRTUAL"."GES_PERSONA" AS PERSONA
			inner join "BSCL_VIRTUAL"."GES_CLIENTE" AS  CLIENTE 
				on PERSONA."per_id" = CLIENTE."per_id"
				and PERSONA."per_id" = case when v_PerId = '0' then PERSONA."per_id" else v_PerId end 				
			inner join "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PersonaH 
				on  PersonaH."IdOrigen" = PERSONA."per_id"
			inner join "BSCL.SocioNegocio.BD::CLI.Cliente" as ClienteH
				on ClienteH."IdPersona" = PersonaH."IdPersona"
			inner join "BSCL_VIRTUAL"."GES_INDICADORES_CLIENTE" as INDICADOR_CLIENTE
				on INDICADOR_CLIENTE."per_id" =  PERSONA."per_id" 	
            inner join "BSCL_VIRTUAL"."GES_INDICADOR" as INDICADOR
            	on  INDICADOR."ind_id" = INDICADOR_CLIENTE."ind_id"
            inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION
            	on  HOMOLOGACION."TipoValor" = 'Indicadores'
            	and HOMOLOGACION."ValorOrigen" = INDICADOR."reg_id"
            	and HOMOLOGACION."ValorOrigen" = 'COGEF'
		  where not exists (select 1 from 
		                          "BSCL.Plataforma.BD::IND.Indicador" as indicador2
		                         inner join "BSCL.Plataforma.BD::IND.TipoIndicador" as TipoIndicador2
		                           on TipoIndicador2."IdTipoIndicador" = indicador2."IdTipoIndicador" 
		                           and TipoIndicador2."CodigoTipoIndicador"= 'CCGF'
		                         where  indicador2."IdEntidad" = ClienteH."IdCliente"
		                         and indicador2."IdTipoEntidad" ='CLIENTE' 
		                     );  
		                           
        -- contrato de custodia                                                   
        select "IdTipoIndicador" into v_IdTipoIndicador
		   from "BSCL.Plataforma.BD::IND.TipoIndicador"	
         where  "IdTipoEntidad" = 'CLIENTE' 
			and "GrupoIndicador" = 'Contratos'
			and "CodigoTipoIndicador"= 'CNTCUST';  

         insert into "BSCL.Plataforma.BD::IND.Indicador" ("IdIndicador"
         ,"IdEntidad","IdTipoEntidad","IdTipoIndicador","ModeloCliente","IdEmpresa","ValorIndicador"
         ,"FechaIngreso","FechaCreacion","FechaModificacion","IdEstadoAutorizacion","FechaVencimiento","IdCarga")
         Select "BSCL.Plataforma.BD.Secuencias::Indicador".NEXTVAL as "IdIndicador" 			  
               , ClienteH."IdCliente" as "IdEntidad"
               , 'CLIENTE' as "TipoEntidad"
               , v_IdTipoIndicador  as "IdTipoIndicador"
               , '' as "ModeloCliente"
               ,  p_IdEmpresa as "IdEmpresa"
               , INDICADOR_CLIENTE."din_id"  as "ValorIndicador"
               , '0001-01-01' as "FechaIngreso"
               , '0001-01-01' as "FechaCreacion"
               , '0001-01-01' as "FechaModificacion"
               , 'ING' as "IdEstadoAutorizacion"
               , '0001-01-01' as "FechaVencimiento"
               , p_IdCarga as "IdCarga"
		 from "BSCL_VIRTUAL"."GES_PERSONA" AS PERSONA
			inner join "BSCL_VIRTUAL"."GES_CLIENTE" AS  CLIENTE 
				on PERSONA."per_id" = CLIENTE."per_id"
				and PERSONA."per_id" = case when v_PerId = '0' then PERSONA."per_id" else v_PerId end 				
			inner join "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PersonaH 
				on  PersonaH."IdOrigen" = PERSONA."per_id"
			inner join "BSCL.SocioNegocio.BD::CLI.Cliente" as ClienteH
				on ClienteH."IdPersona" = PersonaH."IdPersona"
			inner join "BSCL_VIRTUAL"."GES_INDICADORES_CLIENTE" as INDICADOR_CLIENTE
				on INDICADOR_CLIENTE."per_id" =  PERSONA."per_id" 	
            inner join "BSCL_VIRTUAL"."GES_INDICADOR" as INDICADOR
            	on  INDICADOR."ind_id" = INDICADOR_CLIENTE."ind_id"
            inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION
            	on  HOMOLOGACION."TipoValor" = 'Indicadores'
            	and HOMOLOGACION."ValorOrigen" = INDICADOR."reg_id"
            	and HOMOLOGACION."ValorOrigen" = 'COCUS'
		  where not exists (select 1 from 
		                          "BSCL.Plataforma.BD::IND.Indicador" as indicador2
		                         inner join "BSCL.Plataforma.BD::IND.TipoIndicador" as TipoIndicador2
		                           on TipoIndicador2."IdTipoIndicador" = indicador2."IdTipoIndicador" 
		                           and TipoIndicador2."CodigoTipoIndicador"= 'CNTCUST'
		                         where  indicador2."IdEntidad" = ClienteH."IdCliente"
		                         and indicador2."IdTipoEntidad" ='CLIENTE' 
		                     );  		 
		 
		-- contrato de custodia                                          
        select "IdTipoIndicador" into v_IdTipoIndicador
		   from "BSCL.Plataforma.BD::IND.TipoIndicador"	
         where  "IdTipoEntidad" = 'CLIENTE' 
			and "GrupoIndicador" = 'Contratos'
			and "CodigoTipoIndicador"= 'CS';  

         insert into "BSCL.Plataforma.BD::IND.Indicador" ("IdIndicador"
         ,"IdEntidad","IdTipoEntidad","IdTipoIndicador","ModeloCliente","IdEmpresa","ValorIndicador"
         ,"FechaIngreso","FechaCreacion","FechaModificacion","IdEstadoAutorizacion","FechaVencimiento","IdCarga")
         Select "BSCL.Plataforma.BD.Secuencias::Indicador".NEXTVAL as "IdIndicador" 			  
               , ClienteH."IdCliente" as "IdEntidad"
               , 'CLIENTE' as "TipoEntidad"
               , v_IdTipoIndicador  as "IdTipoIndicador"
               , '' as "ModeloCliente"
               ,  p_IdEmpresa as "IdEmpresa"
               , INDICADOR_CLIENTE."din_id"  as "ValorIndicador"
               , '0001-01-01' as "FechaIngreso"
               , '0001-01-01' as "FechaCreacion"
               , '0001-01-01' as "FechaModificacion"
               , 'ING' as "IdEstadoAutorizacion"
               , '0001-01-01' as "FechaVencimiento"
               , p_IdCarga as "IdCarga"
		 from "BSCL_VIRTUAL"."GES_PERSONA" AS PERSONA
			inner join "BSCL_VIRTUAL"."GES_CLIENTE" AS  CLIENTE 
				on PERSONA."per_id" = CLIENTE."per_id"
				and PERSONA."per_id" = case when v_PerId = '0' then PERSONA."per_id" else v_PerId end 				
			inner join "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PersonaH 
				on  PersonaH."IdOrigen" = PERSONA."per_id"
			inner join "BSCL.SocioNegocio.BD::CLI.Cliente" as ClienteH
				on ClienteH."IdPersona" = PersonaH."IdPersona"
			inner join "BSCL_VIRTUAL"."GES_INDICADORES_CLIENTE" as INDICADOR_CLIENTE
				on INDICADOR_CLIENTE."per_id" =  PERSONA."per_id" 	
            inner join "BSCL_VIRTUAL"."GES_INDICADOR" as INDICADOR
            	on  INDICADOR."ind_id" = INDICADOR_CLIENTE."ind_id"
            inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION
            	on  HOMOLOGACION."TipoValor" = 'Indicadores'
            	and HOMOLOGACION."ValorOrigen" = INDICADOR."reg_id"
            	and HOMOLOGACION."ValorOrigen" = 'COSIM'
		  where not exists (select 1 from 
		                          "BSCL.Plataforma.BD::IND.Indicador" as indicador2
		                         inner join "BSCL.Plataforma.BD::IND.TipoIndicador" as TipoIndicador2
		                           on TipoIndicador2."IdTipoIndicador" = indicador2."IdTipoIndicador" 
		                           and TipoIndicador2."CodigoTipoIndicador"= 'CS'
		                         where  indicador2."IdEntidad" = ClienteH."IdCliente"
		                         and indicador2."IdTipoEntidad" ='CLIENTE' 
		                     );                  

		 --contrato por venta corta
         select "IdTipoIndicador" into v_IdTipoIndicador
		   from "BSCL.Plataforma.BD::IND.TipoIndicador"	
         where  "IdTipoEntidad" = 'CLIENTE' 
			and "GrupoIndicador" = 'Contratos'
			and "CodigoTipoIndicador"= 'CVC';  

         insert into "BSCL.Plataforma.BD::IND.Indicador" ("IdIndicador"
         ,"IdEntidad","IdTipoEntidad","IdTipoIndicador","ModeloCliente","IdEmpresa","ValorIndicador"
         ,"FechaIngreso","FechaCreacion","FechaModificacion","IdEstadoAutorizacion","FechaVencimiento","IdCarga")
         Select "BSCL.Plataforma.BD.Secuencias::Indicador".NEXTVAL as "IdIndicador" 			  
               , ClienteH."IdCliente" as "IdEntidad"
               , 'CLIENTE' as "TipoEntidad"
               , v_IdTipoIndicador  as "IdTipoIndicador"
               , '' as "ModeloCliente"
               ,  p_IdEmpresa as "IdEmpresa"
               , INDICADOR_CLIENTE."din_id"  as "ValorIndicador"
               , '0001-01-01' as "FechaIngreso"
               , '0001-01-01' as "FechaCreacion"
               , '0001-01-01' as "FechaModificacion"
               , 'ING' as "IdEstadoAutorizacion"
               , '0001-01-01' as "FechaVencimiento"
               , p_IdCarga as "IdCarga"
		 from "BSCL_VIRTUAL"."GES_PERSONA" AS PERSONA
			inner join "BSCL_VIRTUAL"."GES_CLIENTE" AS  CLIENTE 
				on PERSONA."per_id" = CLIENTE."per_id"
				and PERSONA."per_id" = case when v_PerId = '0' then PERSONA."per_id" else v_PerId end 				
			inner join "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PersonaH 
				on  PersonaH."IdOrigen" = PERSONA."per_id"
			inner join "BSCL.SocioNegocio.BD::CLI.Cliente" as ClienteH
				on ClienteH."IdPersona" = PersonaH."IdPersona"
			inner join "BSCL_VIRTUAL"."GES_INDICADORES_CLIENTE" as INDICADOR_CLIENTE
				on INDICADOR_CLIENTE."per_id" =  PERSONA."per_id" 	
            inner join "BSCL_VIRTUAL"."GES_INDICADOR" as INDICADOR
            	on  INDICADOR."ind_id" = INDICADOR_CLIENTE."ind_id"
            inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION
            	on  HOMOLOGACION."TipoValor" = 'Indicadores'
            	and HOMOLOGACION."ValorOrigen" = INDICADOR."reg_id"
            	and HOMOLOGACION."ValorOrigen" = 'COVCO'
		  where not exists (select 1 from 
		                          "BSCL.Plataforma.BD::IND.Indicador" as indicador2
		                         inner join "BSCL.Plataforma.BD::IND.TipoIndicador" as TipoIndicador2
		                           on TipoIndicador2."IdTipoIndicador" = indicador2."IdTipoIndicador" 
		                           and TipoIndicador2."CodigoTipoIndicador"= 'CVC'
		                         where  indicador2."IdEntidad" = ClienteH."IdCliente"
		                         and indicador2."IdTipoEntidad" ='CLIENTE' 
		                     );                  
		 -- cuadro resumen
         SELECT tipoindicador."NombreTipoIndicador"
		      ,indicador."IdTipoIndicador"
		      ,coalesce(DetalleIndicador."DetalleIndicador",'') as "DetalleIndicador"
		      ,indicador."ValorIndicador"
		      ,count(1)  
		FROM "BSCL"."BSCL.Plataforma.BD::IND.Indicador" as indicador
		INNER JOIN "BSCL"."BSCL.Plataforma.BD::IND.TipoIndicador"  as tipoindicador
			ON tipoindicador."IdTipoIndicador" = indicador."IdTipoIndicador"
		LEFT JOIN "BSCL"."BSCL.Plataforma.BD::IND.DetalleIndicador"	as DetalleIndicador
			on DetalleIndicador."IdTipoIndicador" = indicador."IdTipoIndicador"
			and to_varchar(DetalleIndicador."IdDetalleIndicador") = indicador."ValorIndicador"
		where indicador."IdCarga" = p_IdCarga
		group by tipoindicador."NombreTipoIndicador" ,indicador."IdTipoIndicador",indicador."ValorIndicador" 
		,coalesce(DetalleIndicador."DetalleIndicador",'')
		order by "IdTipoIndicador";
                            
		                      
   END;
END;

