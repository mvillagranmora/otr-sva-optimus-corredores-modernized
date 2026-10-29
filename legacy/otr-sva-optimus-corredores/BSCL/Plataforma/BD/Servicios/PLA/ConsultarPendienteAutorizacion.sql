SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::PLA.ConsultarPendienteAutorizacion";

CREATE PROCEDURE
-- 
--  Proposito: Servicio que permite obtener autorizaciones de una entidad que se encuentren en estado Pendientes, 
--  Autor:  Cristián Flores O.
--  Fecha Creacion: 14/12/2015
--  
"BSCL.Plataforma.BD.Servicios::PLA.ConsultarPendienteAutorizacion" ( 
	IN  p_IdEmpresa						 	bigint,			--ID Empresa destino
	IN  p_IdUsuario						 	bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo						 	NVARCHAR(3),    --ID del Modulo que lo invoca
	IN	p_IdTipoEntidad						NVARCHAR(40),	--Ej:'PERSONA'
	IN  p_IdEntidadOrigen    				NVARCHAR(40),         --Ej:'IdPersona, IdCliente'


	-- Parametros salida del servicio publico
	OUT p_out_Cambio 						NVARCHAR(1),  	--('S','N','')
	OUT p_out_RegistroCambio		  TABLE("IdConcepto"			NVARCHAR(6),
										   "IdSolicitudCambio"  	BIGINT,
										   "IdSecuenciaCambio"  	BIGINT,
										   "IdTipoEntidad"      	NVARCHAR(40),
  									       "IdEntidadOrigen"        NVARCHAR(40),
										   "CampoModificado"    	NVARCHAR(255),
										   "TipoDato"           	NVARCHAR(255),
										   "ValorAntiguo"       	NVARCHAR(255),
										   "ValorNuevo"        	 	NVARCHAR(255),
										   "FechaSolicitud"     	SECONDDATE,
										   "FechaRevision"      	SECONDDATE,
										   "IdEstadoAutorizacion" 	NVARCHAR(3),
										   "Tipo" 					NVARCHAR(1)),
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE v_out_Resultado 	NVARCHAR(1);
	DECLARE v_CodigoError 	INT;
    DECLARE v_MensajeError 	NVARCHAR(1000);	
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

 

	BEGIN
	
		DECLARE  v_Cambio_Count INT:=0;
		DECLARE  v_Nuevo_Count  INT:=0;
		DECLARE v_CampoModificado	NVARCHAR(255):='';
		
		Concepto= select '' as "IdConcepto",'' as "Tipo" from dummy where 1=2;
		v_RegistroNuevo		= select * from :p_out_RegistroCambio where 1=2;
		v_out_RegistroNuevo	= select * from :p_out_RegistroCambio where 1=2;
		
		p_out_Cambio:='';

		p_out_AdvertenciasContador	:= 0;
		p_out_ErroresContador		:= 0;
		p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
		p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;



	
		--Se utiliza Concepto del Evento segun Tipo Identidad
		IF p_IdTipoEntidad='PERSONA' THEN --SIN DETALLE
			Concepto=	SELECT 'MODPER' AS "IdConcepto",'M' AS "Tipo" FROM  dummy
			  UNION ALL SELECT "IdConcepto","Tipo"  FROM :Concepto ;
		END IF;	
		IF p_IdTipoEntidad='DOCUMENTOIDENTIDAD' THEN --CON DETALLE
			Concepto=	SELECT 'MODDOC' AS "IdConcepto",'M' AS "Tipo" FROM  dummy
			  UNION ALL SELECT 'NUEDOC' AS "IdConcepto",'N' AS "Tipo" FROM  dummy
			  UNION ALL SELECT "IdConcepto","Tipo"  FROM :Concepto ;
		END IF;	
		

		--Se obtienen valores desde las solicitud de cambio y vista de Pendientes
		v_out_RegistroCambio = select  PEN."IdConcepto",
										(CASE Length(SOL."IdSolicitudCambio") 	WHEN 0 THEN 0  ELSE SOL."IdSolicitudCambio" 	END) AS "IdSolicitudCambio",
										(CASE Length(SOL."IdSecuenciaCambio") 	WHEN 0 THEN 0  ELSE SOL."IdSecuenciaCambio" 	END) AS "IdSecuenciaCambio",
										(CASE Length(SOL."IdTipoEntidad")		WHEN 0 THEN '' ELSE SOL."IdTipoEntidad" 		END) AS "IdTipoEntidad",
										PEN."NumeroOperacionConcepto"  AS		"IdEntidadOrigen", 
										(CASE Length(SOL."CampoModificado")		WHEN 0 THEN '' ELSE SOL."CampoModificado" 		END) AS "CampoModificado",
										(CASE Length(SOL."TipoDato")			WHEN 0 THEN '' ELSE SOL."TipoDato" 				END) AS "TipoDato",
										(CASE Length(SOL."ValorAntiguo")		WHEN 0 THEN '' ELSE SOL."ValorAntiguo" 			END) AS "ValorAntiguo",
										(CASE Length(SOL."ValorNuevo")			WHEN 0 THEN '' ELSE SOL."ValorNuevo" 			END) AS "ValorNuevo",
										(CASE Length(SOL."FechaSolicitud")		WHEN 0 THEN '' ELSE SOL."FechaSolicitud" 		END) AS "FechaSolicitud",		
										(CASE Length(SOL."FechaRevision")		WHEN 0 THEN '' ELSE SOL."FechaRevision" 		END) AS "FechaRevision",				
										(CASE Length(SOL."IdEstadoAutorizacion")WHEN 0 THEN '' ELSE SOL."IdEstadoAutorizacion" 	END) AS "IdEstadoAutorizacion",
										CON."Tipo"
								 from "BSCL.Plataforma.BD.Vistas::AUT.ConsultarAutorizacionesPendientes" PEN
								LEFT JOIN "BSCL.Plataforma.BD::PLA.SolicitudCambio" SOL
								ON (PEN."NumeroOperacionConcepto" =SOL."IdEntidadOrigen" )
								INNER JOIN  :Concepto CON ON (CON."IdConcepto" = PEN."IdConcepto")
								WHERE 	SOL."IdTipoEntidad"				 	= p_IdTipoEntidad--'PERSONA'
										 AND SOL."IdEstadoAutorizacion" <>'ING'
										-- AND PEN."IdConcepto"='MODPER'
										 AND PEN."NumeroOperacionConcepto"	= p_IdEntidadOrigen--10004
								ORDER BY PEN."IdConcepto",SOL."IdSolicitudCambio", SOL."IdSecuenciaCambio";
		
		
			SELECT COUNT("IdSolicitudCambio") INTO v_Cambio_Count FROM :v_out_RegistroCambio;

			IF v_Cambio_Count > 0 THEN
				p_out_Cambio:='S';
				ELSE
				p_out_Cambio:='N';
			END IF;
			
		
			
		--CON DETALLE: Se identifican registros nuevos segun la identidad, los cuales no se registran en solicitud de cambio 	
			SELECT COUNT("IdSolicitudCambio") INTO v_Nuevo_Count FROM :v_out_RegistroCambio PEN
			INNER JOIN :Concepto CON ON (PEN."IdConcepto"=CON."IdConcepto")
			WHERE CON."Tipo"='N';

			IF v_Nuevo_Count > 0 THEN 
			
				v_RegistroNuevo=SELECT PEN.*  FROM :v_out_RegistroCambio PEN
				INNER JOIN :Concepto CON ON (PEN."IdConcepto"=CON."IdConcepto")
				WHERE CON."Tipo"='N';
				
				IF p_IdTipoEntidad='DOCUMENTOIDENTIDAD' THEN 
					
						
					--Se obtiene el detalle desde la misma entidad de Negocio
					v_RegistroEntidad = SELECT DOC.*
											FROM :v_RegistroNuevo NUE
											INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC
											ON (NUE."IdEntidadOrigen"=DOC."IdDocumentoIdentidad");
		
					--Se insertan ATRIBUTOS segun la ENTIDAD

					v_CampoModificado:='IdTipoDocumentoIdentidad';
					v_out_RegistroNuevo = SELECT 	NUE."IdConcepto", NUE."IdSolicitudCambio", NUE."IdSecuenciaCambio", NUE."IdTipoEntidad", NUE."IdEntidadOrigen",
													v_CampoModificado AS "CampoModificado",
													NUE."TipoDato",NUE."ValorAntiguo",
													DOC."IdTipoDocumentoIdentidad" 	AS "ValorNuevo",
													NUE."FechaSolicitud", NUE."FechaRevision", NUE."IdEstadoAutorizacion",NUE."Tipo"
										  FROM :v_RegistroNuevo NUE INNER JOIN :v_RegistroEntidad DOC	ON (NUE."IdEntidadOrigen"=DOC."IdDocumentoIdentidad")
     				  					  UNION ALL SELECT * FROM :v_out_RegistroNuevo ;
		
					v_CampoModificado:='DocumentoIdentidad';
					v_out_RegistroNuevo = SELECT 	NUE."IdConcepto", NUE."IdSolicitudCambio", NUE."IdSecuenciaCambio", NUE."IdTipoEntidad", NUE."IdEntidadOrigen",
													v_CampoModificado AS "CampoModificado",
													NUE."TipoDato",NUE."ValorAntiguo",
													DOC."DocumentoIdentidad" 	AS "ValorNuevo",
													NUE."FechaSolicitud", NUE."FechaRevision", NUE."IdEstadoAutorizacion",NUE."Tipo"
										  FROM :v_RegistroNuevo NUE INNER JOIN :v_RegistroEntidad DOC	ON (NUE."IdEntidadOrigen"=DOC."IdDocumentoIdentidad")
     				  					  UNION ALL SELECT * FROM :v_out_RegistroNuevo ;	
		
					v_CampoModificado:='NumeroSerie';
					v_out_RegistroNuevo = SELECT 	NUE."IdConcepto", NUE."IdSolicitudCambio", NUE."IdSecuenciaCambio", NUE."IdTipoEntidad", NUE."IdEntidadOrigen",
													v_CampoModificado AS "CampoModificado",
													NUE."TipoDato",NUE."ValorAntiguo",
													DOC."NumeroSerie" 	AS "ValorNuevo",
													NUE."FechaSolicitud", NUE."FechaRevision", NUE."IdEstadoAutorizacion",NUE."Tipo"
										  FROM :v_RegistroNuevo NUE INNER JOIN :v_RegistroEntidad DOC	ON (NUE."IdEntidadOrigen"=DOC."IdDocumentoIdentidad")
     				  					  UNION ALL SELECT * FROM :v_out_RegistroNuevo ;	
		
					v_CampoModificado:='FechaEmision';
					v_out_RegistroNuevo = SELECT 	NUE."IdConcepto", NUE."IdSolicitudCambio", NUE."IdSecuenciaCambio", NUE."IdTipoEntidad", NUE."IdEntidadOrigen",
													v_CampoModificado AS "CampoModificado",
													NUE."TipoDato",NUE."ValorAntiguo",
													DOC."FechaEmision" 	AS "ValorNuevo",
													NUE."FechaSolicitud", NUE."FechaRevision", NUE."IdEstadoAutorizacion",NUE."Tipo"
										  FROM :v_RegistroNuevo NUE INNER JOIN :v_RegistroEntidad DOC	ON (NUE."IdEntidadOrigen"=DOC."IdDocumentoIdentidad")
     				  					  UNION ALL SELECT * FROM :v_out_RegistroNuevo ;	
		
		
					v_CampoModificado:='FechaVencimiento';
					v_out_RegistroNuevo = SELECT 	NUE."IdConcepto", NUE."IdSolicitudCambio", NUE."IdSecuenciaCambio", NUE."IdTipoEntidad", NUE."IdEntidadOrigen",
													v_CampoModificado AS "CampoModificado",
													NUE."TipoDato",NUE."ValorAntiguo",
													DOC."FechaVencimiento" 	AS "ValorNuevo",
													NUE."FechaSolicitud", NUE."FechaRevision", NUE."IdEstadoAutorizacion",NUE."Tipo"
										  FROM :v_RegistroNuevo NUE INNER JOIN :v_RegistroEntidad DOC	ON (NUE."IdEntidadOrigen"=DOC."IdDocumentoIdentidad")
     				  					  UNION ALL SELECT * FROM :v_out_RegistroNuevo ;	
		
		
					v_CampoModificado:='IdEstado';
					v_out_RegistroNuevo = SELECT 	NUE."IdConcepto", NUE."IdSolicitudCambio", NUE."IdSecuenciaCambio", NUE."IdTipoEntidad", NUE."IdEntidadOrigen",
													v_CampoModificado AS "CampoModificado",
													NUE."TipoDato",NUE."ValorAntiguo",
													DOC."IdEstado" 	AS "ValorNuevo",
													NUE."FechaSolicitud", NUE."FechaRevision", NUE."IdEstadoAutorizacion",NUE."Tipo"
										  FROM :v_RegistroNuevo NUE INNER JOIN :v_RegistroEntidad DOC	ON (NUE."IdEntidadOrigen"=DOC."IdDocumentoIdentidad")
     				  					  UNION ALL SELECT * FROM :v_out_RegistroNuevo ;	

				END IF;	
						
			END IF;
			
		
		p_out_RegistroCambio= SELECT * FROM :v_out_RegistroCambio WHERE	"Tipo"='N'
					UNION ALL SELECT * FROM :v_out_RegistroNuevo	
					UNION ALL SELECT * FROM :p_out_RegistroCambio;
			
			
	
	
	END;

	
END;
