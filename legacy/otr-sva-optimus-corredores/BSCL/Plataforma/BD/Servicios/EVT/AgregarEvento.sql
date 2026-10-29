SET SCHEMA BSCL;
GO
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::EVT.AgregarEvento";
GO
CREATE PROCEDURE
-- 
--  Proposito: Agrega evento que será utilizado por el negocio
--  Autor: VARIACODE - BCS 
--  Fecha Creacion: 04/03/2016
--   
"BSCL.Plataforma.BD.Servicios::EVT.AgregarEvento"
 (
 									IN p_IdEmpresa          BIGINT,
									IN p_IdUsuario          BIGINT,
	                                IN p_IdModulo           NVARCHAR(3),
	                                
 									IN p_Eventos table(
 									 "CodigoTipoEvento"			nvarchar(40),
  									 "IdSecuencia"			    bigint,
 									 "FechaCreacionSistema"     SECONDDATE,
 									 "IdPersonaRemitente"		BIGINT,
 									 "IdPersonaDestinatario"	BIGINT,
 									 "IdPortafolio"				BIGINT
  									  ),
  									 
 									IN p_Valores table("IdSecuencia" BIGINT, "Llave" NVARCHAR(40), "Valor" NVARCHAR(5000)),
 									IN p_ArchivosAdjuntos table("IdSecuencia" BIGINT,"ArchivoAdjunto" BLOB, "Nombre" NVARCHAR(520),"Contrasenia" NVARCHAR(520)),
 									IN p_IdDirecciones table("IdSecuencia" BIGINT, "IdDireccion" BIGINT,"TipoDestinatario" NVARCHAR(6)),
 									IN p_AplicaOrigenEvento  NVARCHAR(1) DEFAULT '',
 									OUT p_out_IdEvento		TABLE("IdEvento" BIGINT,"IdSecuencia" BIGINT)
 									
 
	-- Salidas para mensajes de advertencias y errores									
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN 

 		v_EventosAux = select "IdSecuencia","BSCL.Plataforma.BD.Secuencias::EVT.Evento".nextval as "IdEvento","BSCL.Plataforma.BD.Secuencias::EVT.OrigenEvento".nextval	AS "IdOrigenEvento","CodigoTipoEvento", "FechaCreacionSistema","IdPersonaRemitente","IdPersonaDestinatario","IdPortafolio" from :p_Eventos;
 		
 		IF :p_AplicaOrigenEvento  = 'S' THEN
				INSERT INTO "BSCL.Plataforma.BD::EVT.OrigenEvento" 
				( 
				   "IdOrigenEvento"           ,
	  			   "IdEmpresa"                ,
	 			   "IdUsuario"           	  , 
	 			   "IdModulo"                 ,
	 			   "FechaIngreso"             ,
	 			   "CodigoTipoEvento"         ,
	 			   "CantidadEventos"          ,
	 			   "IdEstado"             	 
	 			)  
				SELECT 
					p_ev."IdOrigenEvento"		,
	 				p_IdEmpresa					,
	 				p_IdUsuario					,
	 				p_IdModulo					,
		 			CURRENT_UTCTIMESTAMP		,
					p_ev."CodigoTipoEvento"		,
		  			'1'        ,
	 			  	CASE WHEN CE."ControlProceso"='S' THEN 'VIG' ELSE 'APD' END         							   		
	 			FROM :v_EventosAux p_ev 
	  			INNER JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarDireccionEvento" TIPOEVT
	 			ON TIPOEVT."CodigoTipoEvento" = p_ev."CodigoTipoEvento"
	 			INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle" EVTDET
	 			ON EVTDET."CodigoTipoEvento" = p_ev."CodigoTipoEvento" and EVTDET."IdEstado" = 'ACT'
	 			INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" CE ON CE."Descripcion" = TIPOEVT."CodigoTipoEvento" 
	 			WHERE EVTDET."IdEstado" = 'ACT';
	 		
		 		INSERT INTO "BSCL.Plataforma.BD::EVT.Evento" 
				( 
				   "IdEmpresa"                  ,
	  			   "IdEvento"                   ,
	 			   "CodigoTipoEvento"           , 
	 			   "IdUsuario"                  ,
	 			   "FechaCreacion"              ,
	 			   "Procesado"                  ,
	 			   "TipoEntidadOrigen"          ,
	 			   "IdEntidadOrigen"            ,
	 			   "TipoEntidadDestino"         ,
	 			   "IdEntidadDestino"           ,
	 			   "IdPersonaRemitente"			,
	 			   "IdPersonaDestinatario"		,
	 			   "IdPortafolio"				,
	 			   "IdOrigenEvento" 
	 	 
	 			)  
				SELECT 
	 				p_IdEmpresa					,
	 				p_ev."IdEvento"				,
	 				p_ev."CodigoTipoEvento"		,
		 			p_IdUsuario					,
		 			CURRENT_UTCTIMESTAMP		,
		 			'N' 						,
		  			"TipoEntidadOrigen"         ,
	 			  	"IdEntidadOrigen"           ,
	 			 	"TipoEntidadDestino"        ,
	 			   	"IdEntidadDestino"       	,
	 			   	"IdPersonaRemitente"		,
	 			    "IdPersonaDestinatario"		,
	 			    "IdPortafolio"				,
	 			     p_ev."IdOrigenEvento"			
	 			   		
	 			from :v_EventosAux p_ev 
	  			INNER JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarDireccionEvento" TIPOEVT
	 			ON TIPOEVT."CodigoTipoEvento" = p_ev."CodigoTipoEvento"
	 			INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle" evtdet
	 			ON evtdet."CodigoTipoEvento" = p_ev."CodigoTipoEvento" and "IdEstado" = 'ACT'
	 			Where "IdEstado" = 'ACT';
			
 		ELSE
 	
 			INSERT INTO "BSCL.Plataforma.BD::EVT.Evento" 
			( 
			   "IdEmpresa"                  ,
  			   "IdEvento"                   ,
 			   "CodigoTipoEvento"           , 
 			   "IdUsuario"                  ,
 			   "FechaCreacion"              ,
 			   "Procesado"                  ,
 			   "TipoEntidadOrigen"          ,
 			   "IdEntidadOrigen"            ,
 			   "TipoEntidadDestino"         ,
 			   "IdEntidadDestino"           ,
 			   "IdPersonaRemitente"			,
 			   "IdPersonaDestinatario"		,
 			   "IdPortafolio"				
 	 
 			)  
			SELECT 
 				p_IdEmpresa					,
 				p_ev."IdEvento"				,
 				p_ev."CodigoTipoEvento"		,
	 			p_IdUsuario					,
	 			CURRENT_UTCTIMESTAMP		,
	 			'N' 						,
	  			"TipoEntidadOrigen"         ,
 			  	"IdEntidadOrigen"           ,
 			 	"TipoEntidadDestino"        ,
 			   	"IdEntidadDestino"       	,
 			   	"IdPersonaRemitente"		,
 			    "IdPersonaDestinatario"		,
 			    "IdPortafolio"				
 			   		
 			from :v_EventosAux p_ev 
  			INNER JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarDireccionEvento" TIPOEVT
 			ON TIPOEVT."CodigoTipoEvento" = p_ev."CodigoTipoEvento"
 			INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle" evtdet
 			ON evtdet."CodigoTipoEvento" = p_ev."CodigoTipoEvento" and "IdEstado" = 'ACT'
 			Where "IdEstado" = 'ACT';
 		
 		 END IF;
			
 		 INSERT INTO "BSCL.Plataforma.BD::EVT.EventoDetalle"
			(
				"IdEventoDetalle",
				"IdEvento",
				"Llave",
				"Valor"
			) 
			 SELECT  
				"BSCL.Plataforma.BD.Secuencias::EVT.EventoDetalle".nextval AS "IdDetalle",
				v_ex."IdEvento" as "IdEvento",
				"Llave",
				COALESCE("Valor",'')
			FROM :p_Valores p_va	  		
			INNER JOIN :v_EventosAux v_ex
			ON v_ex."IdSecuencia" = p_va."IdSecuencia" 
			INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle" evtdet
 			ON evtdet."CodigoTipoEvento" = v_ex."CodigoTipoEvento"
 			Where "IdEstado" = 'ACT';
			
			INSERT INTO "BSCL.Plataforma.BD::EVT.EventoArchivoAdjunto"
			(
				"IdEventoArchivoAdjunto" ,
   			    "IdEvento"               , 
  			    "Archivo"       		 ,
  			    "NombreArchivo"				 ,
  			    "Contrasenia"			 
			)
			SELECT 
			 "BSCL.Plataforma.BD.Secuencias::EVT.EventoArchivoAdjunto".nextval,
			 "IdEvento",
			 "ArchivoAdjunto",
			 "Nombre" ,
  			 "Contrasenia"
			from :p_ArchivosAdjuntos v_ex 
 			INNER JOIN :v_EventosAux p_ev 
 			ON v_ex."IdSecuencia" = p_ev."IdSecuencia"
 			INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle" evtdet
 			ON evtdet."CodigoTipoEvento" = p_ev."CodigoTipoEvento"
 			Where "IdEstado" = 'ACT';
 			
 		
 			IF :p_AplicaOrigenEvento  = 'S' THEN
 				INSERT INTO "BSCL.Plataforma.BD::EVT.EventoDirecciones" 
				(
				  "IdEventoEventoDireccion"   	 , 
	   			  "IdEvento"                  	 , 
	    		  "IdDireccionElectronica"       		     , 
				  "TipoDestinatario" 			 
				)
				SELECT
				"BSCL.Plataforma.BD.Secuencias::EVT.EventoDirecciones".nextval,
				p_ev."IdEvento",
				v_ex."IdDireccion",
				'TO'
				from :p_IdDirecciones v_ex
				INNER JOIN :v_EventosAux p_ev ON v_ex."IdSecuencia" = p_ev."IdSecuencia";	
 			
 			ELSE
	 			INSERT INTO "BSCL.Plataforma.BD::EVT.EventoDirecciones" 
				(
				  "IdEventoEventoDireccion"   	 , 
	   			  "IdEvento"                  	 , 
	    		  "IdDireccionElectronica"       , 
				  "TipoDestinatario" 			 
				)
				SELECT
				"BSCL.Plataforma.BD.Secuencias::EVT.EventoDirecciones".nextval,
				p_ev."IdEvento",
				CASE WHEN p_ev."IdPortafolio" <> 0 THEN CPGDEG."IdDireccionElectronica" ELSE v_ex."IdDireccion" END AS "IdDireccionElectronica",
				v_ex."TipoDestinatario" 
				from :p_IdDirecciones v_ex 
	 			INNER JOIN :v_EventosAux p_ev ON v_ex."IdSecuencia" = p_ev."IdSecuencia"
	 			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaGrupoDireccionElectronicaGlobal" CPGDEG ON CPGDEG."IdGrupo" = v_ex."IdDireccion"
	 			INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle" evtdet ON evtdet."CodigoTipoEvento" = p_ev."CodigoTipoEvento"
	 			Where evtdet."IdEstado" = 'ACT';
	 		
 			END IF;
 		
 		
			p_out_IdEvento =
			 		 	SELECT 
 			 				E."IdEvento",EA."IdSecuencia"
			 			from :v_EventosAux EA
			 			INNER JOIN "BSCL.Plataforma.BD::EVT.Evento" E ON E."IdEvento" = EA."IdEvento";
			
 			
END;