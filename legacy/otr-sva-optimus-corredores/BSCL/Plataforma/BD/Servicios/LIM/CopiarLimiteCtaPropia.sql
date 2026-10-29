SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::LIM.CopiarLimiteCtaPropia" ;

CREATE PROCEDURE
-- 
--  Proposito: Servicio para la creacion de un Limite para la Cta. Propia.
--  Autor: CFlores - BCS
--  Fecha Creacion: 03/07/2016
-- 

"BSCL.Plataforma.BD.Servicios::LIM.CopiarLimiteCtaPropia" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					BIGINT,			-- ID  Empresa destino
	IN  p_IdUsuario					BIGINT,			-- ID  del usuario que solicita el servicio
	IN  p_IdModulo					NVARCHAR(3),  	-- ID  del Modulo que lo invoca
	IN  p_UsaReglasNegocio			NVARCHAR(1)	,	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	IN  p_IdLimiteOrigen			BIGINT			, ---IdDetalle 
	IN  p_IdTipoOperacion			NVARCHAR(6)		DEFAULT '',
	IN  p_IdTipoComportamiento      NVARCHAR(40)    DEFAULT '',
	
	-- Parametros salida del servicio publico
    OUT p_out_RefId bigint,
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 	int,
	OUT p_out_AdvertenciasDetalle 	table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador 		int,
	OUT p_out_ErroresDetalle 		table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
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
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	BEGIN	
		--------------------
		--Declare Variables
		--------------------
		DECLARE v_IdLimite			 			bigint:=0;
		DECLARE v_FechaIngreso					SECONDDATE;-- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
		DECLARE v_IdConcepto	       			NVARCHAR(6):='COLICP';--Copiar Limite Cta. Propia
		DECLARE v_RequiereAutorizacion			NVARCHAR(1):='';
		DECLARE v_IdEstadoAutorizacion			NVARCHAR(3):='PEN';
		
	    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
	    DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
        DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
       
    	DECLARE v_IdEntidadBase 				BIGINT:=0;
        DECLARE V_IdCategoriaInversionista		NVARCHAR(40):='';
		DECLARE v_IdCliente			 			bigint:=0;
		DECLARE v_Count,v_IdLimiteDetalle		bigint:=0;
		DECLARE v_CountPlantilla 				bigint:=0;
		DECLARE v_IdPlantillaExtra				NVARCHAR(40):='';
		
		DECLARE v_Nombre						NVARCHAR(100):='';
		DECLARE v_IdTipoLimite					NVARCHAR(40):='';
		DECLARE v_IdTipoInstrumento				NVARCHAR(3):='';
		DECLARE v_IdTipoOperacion				NVARCHAR(6):='';
		
		
		-----------------------------------------------------
		--Validaciones básicas (Codigos,Existencias,etc)
	    -----------------------------------------------------
	    SELECT COUNT(1) into v_Count
	    FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
	   	INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP ON (CLI."IdPersona"=EMP."IdPersona")
		WHERE EMP."IdEmpresa" = p_IdEmpresa;
			    		
		IF v_Count=0 THEN
			p_out_ErroresDetalle = SELECT 'UI_ENTIDAD_NO_ES_CUENTA_PROPIA' AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM dummy;
			SIGNAL SALIDA_CONTROLADA;
		ELSE
			SELECT CLI."IdCategoriaInversionista", CLI."IdPersona", CLI."IdPlantillaLimiteExtra" INTO V_IdCategoriaInversionista, v_IdEntidadBase, v_IdPlantillaExtra
			FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
			INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP ON (CLI."IdPersona"=EMP."IdPersona")
			WHERE EMP."IdEmpresa" = p_IdEmpresa;

		END IF;
		--------------------
		--Negocio Aqui
		--------------------
		
		BEGIN
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaIngreso);
			
			IF(v_IdPlantillaExtra='') THEN
				p_out_ErroresDetalle = SELECT 'PLANTILLA_EXTRA_NO_CONFIGURADA' AS "Codigo",:v_erroroutmsg AS "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			--Existencia Limite Origen
			SELECT COUNT(1) INTO v_Count 
			FROM "BSCL.Plataforma.BD::LIM.Limite" WHERE "IdLimite"=p_IdLimiteOrigen;
			
			IF(v_Count=0) THEN
				p_out_ErroresDetalle = select 'ENTIDAD_NO_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA;
			ELSE
				SELECT "Nombre","IdTipoLimite","IdTipoInstrumento","IdTipoOperacion" INTO v_Nombre,v_IdTipoLimite,v_IdTipoInstrumento,v_IdTipoOperacion
				FROM "BSCL.Plataforma.BD::LIM.Limite" WHERE "IdLimite"=p_IdLimiteOrigen;
			END IF;
			
			SELECT COUNT(1) INTO v_Count 
			FROM "BSCL.Plataforma.BD::LIM.Limite" LOR
			WHERE "IdTipoLimite"		= v_IdTipoLimite
			  AND "IdTipoInstrumento"	= v_IdTipoInstrumento
			  AND "IdTipoOperacion"		= v_IdTipoOperacion;
			  
			IF(v_Count=0) THEN
				p_out_ErroresDetalle = select 'LIMITE_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			
			--Existencia Limite Destino 
			/*SELECT COUNT(1) INTO v_Count 
			FROM "BSCL.Plataforma.BD::LIM.Limite" LOR
			INNER JOIN "BSCL.Plataforma.BD::LIM.Limite" LDE
			ON (	LOR."IdLimite"			=p_IdLimiteOrigen
				AND LOR."IdTipoLimite"		=LDE."IdTipoLimite"
				AND LOR."IdTipoInstrumento"	=LDE."IdTipoInstrumento"
				AND (LOR."IdTipoOperacion"	=:p_IdTipoOperacion OR LOR."IdTipoOperacion" = LDE."IdTipoOperacion")
				AND LOR."IdMoneda"			=LDE."IdMoneda"
				AND LOR."CodigoOperador"	=LDE."CodigoOperador"
				);*/
			
			SELECT COUNT(1) INTO v_Count 
			FROM "BSCL.Plataforma.BD::LIM.Limite" LOR
			WHERE "IdTipoLimite"		= v_IdTipoLimite
			  AND "IdTipoInstrumento"	= v_IdTipoInstrumento
			  AND "IdTipoOperacion"		= :p_IdTipoOperacion;
			  
			IF(v_Count>0) THEN
				p_out_ErroresDetalle = select 'LIMITE_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
				
			
			-- recuperamos numero de secuencia que usaremos para el id 
		    SELECT "BSCL.Plataforma.BD.Secuencias::LIM.IdLimite".NEXTVAL INTO v_IdLimite FROM DUMMY;
	
			p_out_RefId:=v_IdLimite;
			
		 	------------------------------------------
			--Crear 
			------------------------------------------
			insert into "BSCL.Plataforma.BD::LIM.Limite"(		
 				"IdLimite",
			    "IdPlantillaLimite",
				"IdTipoLimite",
				"Nombre",
				"IdMesa",
				"IdCanalIngreso",
				"IdCategoriaInversionista",
				"IdTipoInstrumento",
				"IdTipoOperacion",
			    "IdTipoProducto",
			    "IdInstrumento",
			    "IdClasificacionTipoInstrumento",
				"UnidadPlazo",
				"Plazo",
				"IdTipoReajuste",
				"CodigoOperador",
				"MontoTotal",
				"MontoIntraday",
			    "MontoOvernight",
				"IdMoneda",
				"IdMonedaCalculo",
				"ClasificacionRiesgoMinima",
				"IdEstado",
				"EsPersonalizada",
				"FechaVigencia",
				"EsTotalizador",
				"FechaIngreso",
				"FechaCreacion",
				"FechaModificacion",
				"IdEstadoAutorizacion",
				"IdUsuarioCreador",
				"UI_Limite",
				"IdClasificacionRiesgo",
				"IdAgente",
				"IdTipoComportamiento"
			)SELECT v_IdLimite,
			    "IdPlantillaLimite",
				"IdTipoLimite",
				"Nombre",
				"IdMesa",
				"IdCanalIngreso",
				"IdCategoriaInversionista",
				"IdTipoInstrumento",
				:p_IdTipoOperacion,
			    "IdTipoProducto",
			    "IdInstrumento",
			    "IdClasificacionTipoInstrumento",
				"UnidadPlazo",
				"Plazo",
				"IdTipoReajuste",
				"CodigoOperador",
				"MontoTotal",
				"MontoIntraday",
			    "MontoOvernight",
				"IdMoneda",
				"IdMonedaCalculo",
				"ClasificacionRiesgoMinima",
				"IdEstado",
				"EsPersonalizada",
				"FechaVigencia",
				"EsTotalizador",
				v_FechaIngreso,
				CURRENT_UTCTIMESTAMP,
				CURRENT_UTCTIMESTAMP,
				v_IdEstadoAutorizacion,
				p_IdUsuario,
				"UI_Limite",
				"IdClasificacionRiesgo",
				"IdAgente",
				:p_IdTipoComportamiento
				FROM "BSCL.Plataforma.BD::LIM.Limite" WHERE "IdLimite"=p_IdLimiteOrigen;
				
			---Identificar si el Limte contiene detalle segun tipo de Limite	
				
			IF v_IdTipoLimite='LIMCTPEXCLUIREMISOR' THEN
				insert into "BSCL.Plataforma.BD::LIM.LimiteEmisor"(
					"IdLimiteEmisor",
					"IdEmisor",
					"IdLimite",
					"IdEstado",
					"FechaIngreso",
					"FechaCreacion",
					"FechaModificacion",
					"IdEstadoAutorizacion",
					"IdUsuarioCreador"
				) SELECT
					"BSCL.Plataforma.BD.Secuencias::LIM.IdLimiteEmisor".NEXTVAL AS "IdLimiteEmisor",
					"IdEmisor",
					v_IdLimite,
					"IdEstado",
					v_FechaIngreso,						
					CURRENT_UTCTIMESTAMP,
					CURRENT_UTCTIMESTAMP,
					v_IdEstadoAutorizacion,
					p_IdUsuario
				FROM "BSCL.Plataforma.BD::LIM.LimiteEmisor" WHERE "IdLimite"=p_IdLimiteOrigen;
			END IF;
			
			
			IF v_IdTipoLimite='LIMCTPMARGENTASAS' THEN
				insert into "BSCL.Plataforma.BD::LIM.RangosMontoPlazo"(
					"IdRangosMontoPlazo",
					"IdLimite",
					"MontoDesde",
					"MontoHasta",
					"PlazoDesde",
				    "PlazoHasta",
					"Tasa",
				    "PuntosTasaMenos",
				    "PuntosTasaMas",
				    "PuntosTasaMenosAgente",
				    "PuntosTasaMasAgente",
				    "IdEstado",
				    "FechaIngreso",
				    "FechaCreacion",
				    "FechaModificacion",
				    "IdEstadoAutorizacion",
				    "IdUsuarioCreador"
				)SELECT 
					"BSCL.Plataforma.BD.Secuencias::LIM.IdRangosMontoPlazo".NEXTVAL AS "IdRangosMontoPlazo",
					v_IdLimite,
					"MontoDesde",
					"MontoHasta",
					"PlazoDesde",
				    "PlazoHasta",
					"Tasa",
				    "PuntosTasaMenos",
				    "PuntosTasaMas",
				    "PuntosTasaMenosAgente",
				    "PuntosTasaMasAgente",
				    "IdEstado",
				    v_FechaIngreso,						
					CURRENT_UTCTIMESTAMP,
					CURRENT_UTCTIMESTAMP,
					v_IdEstadoAutorizacion,
					p_IdUsuario
				FROM "BSCL.Plataforma.BD::LIM.RangosMontoPlazo" WHERE "IdLimite"=p_IdLimiteOrigen;
			END IF;	
			
		
   			------------------------------------------
			--Pregunta Si requiere autorizacion
			------------------------------------------
			V_RegistroSolicitudCambio =SELECT * FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" WHERE 1=2;
			
			call "BSCL.Plataforma.BD.Servicios::AUT.RequiereEventoAutorizacion" (
				:p_IdEmpresa,
				:p_IdUsuario,
				:p_IdModulo,
				:V_RegistroSolicitudCambio,
				:v_IdConcepto,
				:v_RequiereAutorizacion
			);	
			
	
			if v_RequiereAutorizacion='S' then
			------------------------------------------
			--Genera Evento de Autorizacion
			------------------------------------------

				DECLARE	v_Glosa							NVARCHAR(520);
				DECLARE v_out_idAuth					BIGINT;
				DECLARE v_out_AuthAdvertenciasContador 	int;
				DECLARE v_out_AuthErroresContador 		int;
				v_out_AuthAdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
				v_out_AuthErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
				SELECT "DocumentoIdentidad" || ' / ' || "NombreCompleto" || ' / Limite: ' || v_Nombre into v_Glosa 
				FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" WHERE "IdPersona" = v_IdEntidadBase;
				
				v_tablaEventos = SELECT
									v_IdConcepto AS "IdTipoEvento",
								    p_IdModulo 	 AS "IdModuloSolicita",
								    p_IdUsuario	 AS "IdUsuarioSolicita",
								    TO_VARCHAR (v_FechaIngreso, 'YYYY-MM-DD') AS "Fecha",
								    p_IdTipoOperacion AS "IdTipoOperacion",
								    v_IdConcepto AS	"IdConcepto",
								    CAST (v_IdLimite AS NVARCHAR(40)) AS "NumeroOperacionConcepto",
								    TO_VARCHAR (v_FechaIngreso, 'YYYY-MM-DD') AS "FechaOperacionConcepto",
								    0  AS "IdCliente",
								    0  AS "IdFuncionario",
								    0  AS "IdProveedor",
								    '' AS "IdTipoProducto",
									'' AS "IdClasificacionTipoInstrumento",
									'' AS "IdTipoInstrumento",
									0  AS "IdInstrumento",
									'' AS "Nemo",
								    0  AS "Cantidad",
								    0  AS "Monto",
								    0  AS "Precio",
								    0  AS "Tasa",
								    '' AS "IdMoneda",
								    v_Glosa AS "Glosa",
								    0 as "IdUnion" 
								FROM DUMMY;
			----------------------------------------------------------------------------------------------------------------							
	        v_tablaEventosDetalle = select  "IdEventoDetalle" as "IdUnion","NombreCampo","TipoDato","Valor","Clasificacion"
	   									from "BSCL.Plataforma.BD::AUT.EventoDetalle" 
	   									where "IdEventoDetalle" = 0;
	   		-----------------------------------------------------------------------------------------------------------------
				CALL"BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacionMasivo"(
						p_IdEmpresa => p_IdEmpresa,
						p_IdUsuario => p_IdUsuario,
						p_IdModulo => p_IdModulo,
						p_UsaReglasNegocio=> :p_UsaReglasNegocio,
					    p_simularOperacion=> 'N',
						p_tablaEventos=> :v_tablaEventos,	
						p_tablaDetalle => :v_tablaEventosDetalle,
						--salida
						p_out_IdEvento => v_out_idAuthtable,
						p_out_AdvertenciasContador => v_out_AuthAdvertenciasContador,
						p_out_AdvertenciasDetalle =>  v_out_AuthAdvertenciasDetalle,
						p_out_ErroresContador =>  v_out_AuthErroresContador,
						p_out_ErroresDetalle =>  v_out_AuthErroresDetalle);
				
				p_out_AdvertenciasContador := p_out_AdvertenciasContador + v_out_AuthAdvertenciasContador;
				p_out_AdvertenciasDetalle = Select "Codigo","RequiereAutorizacion", "Descripcion" 
				                            From :v_out_AuthAdvertenciasDetalle Union All 
				                            Select "Codigo","RequiereAutorizacion", "Descripcion"
				                            From :p_out_AdvertenciasDetalle;
				p_out_ErroresContador := p_out_ErroresContador + v_out_AuthErroresContador;
				p_out_ErroresDetalle = Select "Codigo", "Descripcion" 
				                       From :v_out_AuthErroresDetalle Union All 
				                       Select "Codigo", "Descripcion" 
				                       From :p_out_ErroresDetalle;
				                       
				IF p_out_ErroresContador > 0 THEN
					SIGNAL SALIDA_CONTROLADA;
				ELSE
					--Mensaje de Salida
					p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;
					v_hash= select '' as "Codigo",'' as "Valor" FROM DUMMY;
					CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('REQUIERE_AUTORIZACION',:v_hash,:v_erroroutmsg);
					p_out_AdvertenciasDetalle =  select 'REQUIERE_AUTORIZACION' as "Codigo",'S' as "RequiereAutorizacion",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
											     select * from :p_out_AdvertenciasDetalle;

					SIGNAL SALIDA_CONTROLADA;
				END IF;
			ELSE
				--Confirma la creacion y cambia el estado del documento

				v_tablaIdLimite = SELECT v_IdLimite AS "IdLimite" FROM DUMMY;
				v_IdEstadoAutorizacion:='ING';
				CALL "BSCL.Plataforma.BD.Servicios::LIM.ConfirmarCopiarLimiteCtaPropia"(
					p_IdEmpresa,
					p_IdUsuario,
					p_IdModulo,
					p_UsaReglasNegocio,
					:v_tablaIdLimite,
					v_IdEstadoAutorizacion,
					v_out_AdvertenciasContador,
					v_out_AdvertenciasDetalle,
					v_out_ErroresContador,
					v_out_ErroresDetalle);
				
				p_out_AdvertenciasContador := p_out_AdvertenciasContador + v_out_AdvertenciasContador;
				p_out_AdvertenciasDetalle = Select "Codigo","RequiereAutorizacion", "Descripcion" 
				                            From :v_out_AdvertenciasDetalle Union All 
				                            Select "Codigo","RequiereAutorizacion", "Descripcion"
				                            From :p_out_AdvertenciasDetalle;  
				                                         
					                                          
				 p_out_ErroresContador := p_out_ErroresContador + v_out_ErroresContador;
					                                         
				 p_out_ErroresDetalle = Select "Codigo", "Descripcion" 
				 						From :v_out_ErroresDetalle Union All 
				 						Select "Codigo", "Descripcion" 
				 						From :p_out_ErroresDetalle;
			END IF;
		END;
	
		--------------------
		-- Normativo 
		--------------------
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;