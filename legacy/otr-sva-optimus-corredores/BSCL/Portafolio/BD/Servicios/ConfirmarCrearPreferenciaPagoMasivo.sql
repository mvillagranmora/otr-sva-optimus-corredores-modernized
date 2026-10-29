SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Portafolio.BD.Servicios::ConfirmarCrearPreferenciaPagoMasivo";
GO
CREATE PROCEDURE
 --
 --  Proposito: Servicio para confirmar la modificacion de la preferencia de pago.
 --  Invocado desde el modulo de autorizaciones o del propio negocio al no requerir autorizacion.
 --  Autor: CFLORES - BCS
 --  Fecha Creacion: 31-03-2016
 --
 "BSCL.Portafolio.BD.Servicios::ConfirmarCrearPreferenciaPagoMasivo" (
	 -- Parametros entrada comunes en todos los servicios publicos
	 IN  p_IdEmpresa         			BIGINT,   		--ID Empresa destino
	 IN  p_IdUsuario         			BIGINT,   		--ID del usuario que solicita el servicio
	 IN  p_IdModulo         			NVARCHAR(3),   	--ID del Modulo que lo invoca
	 IN  p_UsaReglasNegocio        		NVARCHAR(1), 	-- S/N se usa en caso de servicios anidados.
	 -- Parametros entrada del servicio publico
	 IN  p_PreferenciaPagoPortafolio    TABLE ("IdPreferenciaPagoPortafolio" BIGINT),
	 IN  p_IdEstadoAutorizacion      	NVARCHAR(3),
	 -- Parametros salida del servicio publico
	 --OUT p_out_RefId         BIGINT,
	 -- Salidas para mensajes de advertencias y errores
	 OUT p_out_AdvertenciasContador   	int,
	 OUT p_out_AdvertenciasDetalle   	table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),
	 OUT p_out_ErroresContador    		int,
	 OUT p_out_ErroresDetalle    		table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
 )
 LANGUAGE SQLSCRIPT
 SQL SECURITY INVOKER
 DEFAULT SCHEMA "BSCL"
 AS
 BEGIN
 --------------------
 --DECLARE Manejo de Errores
 --------------------
 DECLARE SALIDA_CONTROLADA   CONDITION;
 DECLARE v_errorcuenta    INT DEFAULT 0;
 DECLARE v_errorhashmsg    table ("Codigo" nvarchar(40),"Valor" nvarchar(5000));
 DECLARE v_erroroutmsg     nvarchar(5000);
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
			 call "BSCL"."BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);
			
			 p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL
			 select v_errorExt,::SQL_ERROR_MESSAGE from dummy;
			 select count(1) into p_out_ErroresContador
			 from :p_out_ErroresDetalle;
		 ELSE
			 --Error interno no manejado  (El log será captado por la capa de XS control)
			 RESIGNAL;
		 END IF;
	 END;
	 p_out_AdvertenciasContador := 0;
	 p_out_ErroresContador  := 0;
	 p_out_AdvertenciasDetalle = select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	 p_out_ErroresDetalle  = select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
		
	 BEGIN
		 --------------------
		 --Declare Variables
		 --------------------
		 DECLARE v_IdEntidadOrigen        NVARCHAR(40);
		 --PRONETIT
		 DECLARE v_countIdPortafolio INT:=0;
		 DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
		 DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
		 DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));
		 DECLARE v_IdEntidad    				BIGINT:= 0;
		 DECLARE v_TipoEntidadReplica 			NVARCHAR(50):= 'PREFERENCIASDEPAGO';
		 DECLARE v_TipoOperacion  				NVARCHAR(1):= 'I';
		 DECLARE v_CountPreferencia        		BIGINT;
		 DECLARE v_CantRegistrosPreferencia     BIGINT;
		 DECLARE v_TipoEntidad					NVARCHAR(40):= 'PREFERENCIAPAGO';
		 DECLARE v_IdPortafolio         		BIGINT;
		 DECLARE v_IdTipoOperacion				NVARCHAR(6);
		 DECLARE v_IdMoneda						NVARCHAR(3);
		 DECLARE v_EsDefecto					NVARCHAR(1);
		
		 --------------------
		 --Validaciones
		 --------------------
		 --------------------
		 --Reglas Negocio
		 --------------------
		 --------------------
		 --Lógica Negocio
		 --------------------
		 BEGIN
			 --v_IdEntidadOrigen:=CAST (p_IdPreferenciaPagoPortafolio AS NVARCHAR(40));
			 UPDATE PRE
			 SET "IdEstadoAutorizacion"=p_IdEstadoAutorizacion
			 FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" PRE
			 JOIN :p_PreferenciaPagoPortafolio P ON P."IdPreferenciaPagoPortafolio" = PRE."IdPreferenciaPagoPortafolio";
			
			 --PRONETIT
			 v_CountPreferencia:= 1;
			 SELECT COUNT("IdPreferenciaPagoPortafolio") INTO v_CantRegistrosPreferencia FROM :p_PreferenciaPagoPortafolio;
			
			 v_PreferenciaRecorrido = SELECT
					 ROW_NUMBER() OVER(ORDER BY PRE."IdPreferenciaPagoPortafolio")  AS "Id"
					 ,PRE."IdPreferenciaPagoPortafolio"        AS "IdPreferenciaPagoPortafolio"
					 FROM :p_PreferenciaPagoPortafolio AS PRE;
					
			 WHILE :v_CountPreferencia <= :v_CantRegistrosPreferencia DO
				 v_IdPortafolio  := 0;
				 v_IdEntidad  := 0;
				
				 SELECT PRE."IdPreferenciaPagoPortafolio" INTO v_IdEntidad
				 FROM :v_PreferenciaRecorrido AS PRE
				 WHERE "Id" = :v_CountPreferencia;
				
				 SELECT PRE."IdPortafolio" INTO v_IdPortafolio
				 FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" AS PRE
				 WHERE "IdPreferenciaPagoPortafolio" = :v_IdEntidad;
				
			
				 IF(v_IdPortafolio != 0) THEN
					 SELECT COUNT("IdPortafolio") INTO v_countIdPortafolio
					 FROM "BSCL.Portafolio.BD::POR.Portafolio"
					 WHERE "IdPortafolio" = v_IdPortafolio
					 AND "Proposito" = 'PROPOSITOAPV';
					
					 IF(v_countIdPortafolio > 0)THEN
						 CALL "BSCL.APV.BD.Servicios::ReplicaPortafolioAPV" (
							 :p_IdEmpresa,
							 :p_IdUsuario,
							 :p_IdModulo,
							 :p_UsaReglasNegocio,
							 v_IdPortafolio,
							 v_IdEntidad,
							 v_TipoEntidadReplica,
							 v_TipoOperacion,
							 v_out_AdvertenciasContador,
							 v_out_AdvertenciasDetalle,
							 v_out_ErroresContador,
							 v_out_ErroresDetalle
						 );
						
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
						
						 IF p_out_ErroresContador > 0 then
						 	SIGNAL SALIDA_CONTROLADA;
						 END if;
					 END IF;
					
					 --EVALUAR SI ES NECESARIO QUITAR LA MARCA POR DEFECTO DE LOS DEMAS 
					 SELECT TOP 1 DOC."IdPortafolio" , PP."IdTipoOperacion", PP."IdMoneda", DOC."PorDefecto" INTO v_IdPortafolio, v_IdTipoOperacion, v_IdMoneda, v_EsDefecto
					 FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" DOC
					 INNER JOIN "BSCL.Plataforma.BD::PAG.PreferenciaPago" PP ON DOC."IdPreferenciaPago" = PP."IdPreferenciaPago" AND PP."IdEmpresa" = DOC."IdEmpresa" 
					 WHERE DOC."IdPreferenciaPagoPortafolio" = :v_IdEntidad AND DOC."IdEmpresa" = p_IdEmpresa; 					
					
					 --QUITAR MARCA POR DEFECTO A TODOS LOS INDICADORES DEL PORTAFOLIO POR TIPO DE OPERACION Y MONEDA.
					 IF v_EsDefecto = 'S' THEN
				 		 UPDATE  DOC
						 SET
					 		 DOC."PorDefecto" = 'N',
							 DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
						 FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" DOC, "BSCL.Plataforma.BD::PAG.PreferenciaPago" PP
						 WHERE DOC."IdPreferenciaPago" 	= PP."IdPreferenciaPago" 
						 AND DOC."IdPortafolio" 		= v_IdPortafolio
						 AND PP."IdEmpresa" 			= DOC."IdEmpresa" 
						 AND PP."IdEmpresa" 			= p_IdEmpresa
						 AND DOC."IdPreferenciaPagoPortafolio" <> :v_IdEntidad
						 AND PP."IdTipoOperacion" 		= v_IdTipoOperacion
						 AND PP."IdMoneda" 				= v_IdMoneda
						 AND DOC."IdPreferenciaPagoPortafolio" != :v_IdEntidad;
					 END IF;
					
					--LOG DE AUDITORIA (inicio)
					BEGIN 
						DECLARE v_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));
						DECLARE v_IdCliente					BIGINT;
						DECLARE v_EstadoCliente				NVARCHAR(3);
						DECLARE	v_NombreCliente				NVARCHAR(100);
						DECLARE	v_IdTipoDocumentoIdentidad	NVARCHAR(40);
						DECLARE	v_IdDocumentoIdentidad		NVARCHAR(40);				
						DECLARE v_fechaHoraSistema 			NVARCHAR(40);
						DECLARE v_NombreProceso				NVARCHAR(100):='Ingreso de Preferencia de Pago';
						
						--DATOS CABECERA LOG
						call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
							p_IdEmpresa => :p_IdEmpresa,
							p_out_FechaHoraSistema => :v_fechaHoraSistema
						);					 
						
						SELECT  CLI."IdCliente"
							   ,CLI."NombreCompleto"
							   ,CLI."TipoDocumentoIdentidad"
							   ,CLI."DocumentoIdentidad"
							   ,CLI."IdEstado"
						INTO v_IdCliente,
							 v_NombreCliente,
							 v_IdTipoDocumentoIdentidad,
							 v_IdDocumentoIdentidad,
							 v_EstadoCliente					   
						  FROM "BSCL.Portafolio.BD::POR.Portafolio" POR
						  INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON CLI."IdCliente" = POR."IdCliente"
						  WHERE POR."IdPortafolio" = :v_IdPortafolio;					
						
						--DATOS DETALLE LOG
						p_DetalleLog = 
						(Select :v_TipoEntidad		 as "SubRegistro",
								''	 				 as "CampoRegistro",
								''				 	 as "ValorAntiguo",
								''					 as "ValorNuevo" 
						FROM DUMMY);
	
						--GRABAR LOG
						CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
						p_IdTipoLog => 'INGRESO' 
						,p_IdModulo => 'SOC' 
						,p_IdUsuario => :p_IdUsuario 
						,p_IdEmpresa => :p_IdEmpresa 
						,p_FechaSistemaLog => TO_VARCHAR (:v_fechaHoraSistema, 'YYYY-MM-DD')
						,p_IdUsuarioAtendido => :p_IdUsuario
						,p_IdPortafolio => :v_IdPortafolio
						,p_IdTipoRegistro => :v_TipoEntidad--'PREFERENCIAPAGO'
						,p_DetalleLog => :p_DetalleLog	-- hacia arriba obligatorios
						,p_NombreProceso => :v_NombreProceso-- hacia abajo opcionales
						,p_IdRegistro => :v_IdEntidad
						,p_EstadoProceso => :p_IdEstadoAutorizacion
						,p_EstadoRegistro=> :p_IdEstadoAutorizacion
						,p_EstadoCliente=> :v_EstadoCliente --Cliente
						,p_IdCliente => :v_IdCliente
						,p_NombreCliente=> :v_NombreCliente
						,p_IdTipoDocumentoIdentidad=> :v_IdTipoDocumentoIdentidad
						,p_IdDocumentoIdentidad=> :v_IdDocumentoIdentidad						
						);					
		
					END;
					--LOG DE AUDITORIA (Fin)						
					
				 END IF;
				
				 v_CountPreferencia:= :v_CountPreferencia + 1;
				
			 END WHILE;
		 END;
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
