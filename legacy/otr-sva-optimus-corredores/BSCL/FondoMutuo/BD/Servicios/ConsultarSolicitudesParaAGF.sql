SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::ConsultarSolicitudesParaAGF";

CREATE PROCEDURE
--  Proposito: Obtiene solicitudes de fondos mutuos para enviar a la AGF .
--  Autor: Gat-Blac 
--  Fecha Creacion: 09/11/2016
"BSCL.FondoMutuo.BD.Servicios::ConsultarSolicitudesParaAGF" (
	--VARIABLES OBLIGATORIAS
	IN p_IdEmpresa 								BIGINT,
	IN p_IdUsuario 								BIGINT,
	IN p_IdModulo 								NVARCHAR(3),
  	OUT p_out_Resultado							TABLE(
  													"IdInterfazAgf"         BIGINT,
  													"TipoRegistro"			NVARCHAR(2),
  													"Registro"				NVARCHAR(450)
  												),
  	OUT p_out_AdvertenciasContador 				INT,
	OUT p_out_AdvertenciasDetalle 				TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000)),
	OUT p_out_ErroresContador 					INT,
	OUT p_out_ErroresDetalle 					TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------
	DECLARE v_out_AdvertenciasDetalle 	TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	DECLARE v_out_ErroresDetalle  		TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	BEGIN
		v_out_AdvertenciasDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
		v_out_ErroresDetalle 	  = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
	END;
	
	BEGIN
		DECLARE SALIDA_CONTROLADA CONDITION;
		DECLARE v_FechaSistema NVARCHAR(10);
		
		DECLARE EXIT HANDLER FOR SQLEXCEPTION
		BEGIN
			RESIGNAL;
		END; 
		
		DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA  
		BEGIN
			select count(1) into p_out_ErroresContador 
			from :v_out_ErroresDetalle;
			select count(1) into p_out_AdvertenciasContador 
			from :v_out_AdvertenciasDetalle;
			
			v_TablaCodigosMensajes = SELECT "Codigo" AS "CodigoMensaje",
									"NombreParametro" as "CodigoDato",
									"ValorParametro" as "ValorDato" 
									FROM :v_out_ErroresDetalle
									UNION ALL
									SELECT "Codigo" AS "CodigoMensaje",
									"NombreParametro" as "CodigoDato",
									"ValorParametro" as "ValorDato" 
									FROM :v_out_AdvertenciasDetalle;
									
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
			
			p_out_ErroresDetalle = SELECT 	"errores"."IdInterno",
											"errores"."Codigo",
											"msj"."Descripcion"
									FROM :v_out_ErroresDetalle AS "errores"
									INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "errores"."Codigo" = "msj"."Codigo";
			
			p_out_AdvertenciasDetalle = SELECT 	"adv"."IdInterno",
												"adv"."Codigo",
												"adv"."RequiereAutorizacion",
												"msj"."Descripcion"
										FROM :v_out_AdvertenciasDetalle AS "adv"
										INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "adv"."Codigo" = "msj"."Codigo";
						
		END;
		
		p_out_AdvertenciasContador	:= 0;
		p_out_ErroresContador		:= 0;
		
		BEGIN
		DECLARE v_resultadoRegla 	NVARCHAR(40);
		DECLARE v_IdPaisSistema 	BIGINT;
		DECLARE p_out_AdvertenciasContadorEstado BIGINT;
		DECLARE p_out_ErroresContadorEstado BIGINT;
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CONSULTAR SOLICITUD AGF','INICIO','INICIO');
		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		
		if :v_resultadoRegla = 'RECH' then
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		end if;
		
		v_solicitudes = SELECT  "InterfazAgf"."IdInterfazAgf",
								"InterfazAgf"."TipoRegistro",
								LPAD("InterfazAgf"."DocumentoIdentidad",11,' ') ||
								LPAD("InterfazAgf"."CodigoPortafolio",2,' ') ||
								LPAD("InterfazAgf"."FechaLiquidacion",10,' ') ||
								LPAD("InterfazAgf"."FechaAceptacion",10,' ') ||
								CASE "InterfazAgf"."TipoSolicitud" WHEN 'APO' THEN 'I'
								                                   WHEN 'RES' THEN 'R'
								                                   ELSE ''
								END ||
								'I' || --LPAD("InterfazAgf"."IdEstadoSolicitud",1,' ') ||
								RPAD("SerieDeFondo"."IdSerieFondo",10,' ') ||
								LPAD("SerieDeFondo"."CodigoSerie",3,' ') || --codigo ser
								RPAD("InterfazAgf"."IdUsuario",4,' ') ||
								LPAD(FLOOR ("InterfazAgf"."NumeroCuotas"),14,'0') ||
								LPAD(substring(("InterfazAgf"."NumeroCuotas" - floor("InterfazAgf"."NumeroCuotas")),3,4),4,0) ||
								LPAD(FLOOR ("InterfazAgf"."ValorCuota"),14,'0') ||
								LPAD(substring(("InterfazAgf"."ValorCuota" - floor("InterfazAgf"."ValorCuota")),3,4),4,0) ||
								LPAD(FLOOR ("InterfazAgf"."Monto"),14,'0') ||
								LPAD(substring(("InterfazAgf"."Monto" - floor("InterfazAgf"."Monto")),3,4),4,0) ||
								LPAD("InterfazAgf"."CalculoPorMontoCuota",1,' ') ||
								RPAD(CASE "InterfazAgf"."IdMoneda" WHEN 'CLP' THEN '$' WHEN 'USD' THEN 'US$' WHEN 'EUR' THEN 'EU' ELSE '' END,3,' ') ||
								LPAD(CASE WHEN "Solicitud"."IdRegimenTributario" IN ('APV-A','APV-B','APVC') THEN 'APV' 
										  WHEN "Solicitud"."IdRegimenTributario" IN ('CVO') THEN 'CVO'
										  WHEN "Solicitud"."IdRegimenTributario" IN ('DCO') THEN 'DCO'
										  WHEN "Solicitud"."IdRegimenTributario" IN ('57BIS') THEN '57B'
										  WHEN "Solicitud"."IdRegimenTributario" IN ('54BIS') THEN '54B'
										  WHEN "Solicitud"."IdRegimenTributario" IN ('NINGUNO','ART108','ART107') THEN 'FMU'
										  ELSE '' END,3,' ') ||
								CASE "InterfazAgf"."CalculoPorMontoCuota" WHEN 'M' THEN 'P'
																		  WHEN 'C' THEN 'P'
																		  ELSE ''
							    END ||
								LPAD('B',1,' ') || 	 --COM_RTE
								LPAD('SIS',3,' ') || --ORIGEN
								LPAD("InterfazAgf"."IdSolicitudAporteRescate",8,' ') || -- FOLIO GESTION
								LPAD("InterfazAgf"."IdSerieFondoDestino",10,' ') ||
								LPAD('',3,' ') || --CODIGO SER DES
								LPAD('',8,'0') || --FOLIO GESTION REINVERSION
								LPAD('',3,' ') || --TRASPTRANS
								LPAD('',3,' ') || --CALIDAD PARTICIPE
								RPAD('',3,' ') || --CAMPOS FILLER EN BLANCO PARA HACER CALZAR EL REGISTRO
								RPAD('0-0',11,' ') || --RUT AUT
								LPAD("InterfazAgf"."IdEstadoSerieFondo",1,' ') ||
								LPAD("InterfazAgf"."IndicadorMLT",1,' ') ||
								LPAD('',3,' ') || --ORIGEN GES
								LPAD('',3,' ') --MONEDA ORIGEN
								AS "Registro"
								FROM "BSCL.FondoMutuo.BD::FMM.InterfazAgf" AS "InterfazAgf"
								LEFT JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSerieDeFondo" AS "SerieDeFondo" ON ("SerieDeFondo"."IdSerieFondo" = "InterfazAgf"."IdSerieFondo")
								LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "Solicitud" ON ("Solicitud"."IdSolicitudAporteRescate" = "InterfazAgf"."IdSolicitudAporteRescate")
								WHERE "InterfazAgf"."IdEmpresa" = p_IdEmpresa
								AND "InterfazAgf"."IdEstado" = 'PEN'
								AND "InterfazAgf"."TipoRegistro" = 'AR'
								UNION ALL
						SELECT  INTERFAZ."IdInterfazAgf",
								INTERFAZ."TipoRegistro",
								LPAD(INTERFAZ."DocumentoIdentidad",11,' ') ||
								LPAD(INTERFAZ."CodigoPortafolio",2,' ') ||
								RPAD(INTERFAZ."NombreCompleto",62,' ') || 
								RPAD(COALESCE(DIRECCION_FISICA."NombreLargo",''),50,' ') || 
								RPAD('',4,' ') || -- COMPAR
								RPAD(COALESCE(TELEFONO."Numero",''),20,' ') ||
								RPAD(COALESCE(DIRECCION_FISICA."NombreLargo",''),50,' ') ||
								RPAD('',4,' ') || -- COMCOM
								RPAD(COALESCE(TELEFONO."Numero",''),20,' ') ||
								RPAD('',20,' ') || --CASPOS
								RPAD('',20,' ') || --CORPOS
								RPAD('',4,' ') || --COMPOS
								RPAD(COALESCE(DIRECCION_ELECTRONICA."Direccion",''),50,' ') ||
								RPAD(COALESCE(CLIENTE."Tipo",''),1,' ') || --TIPPAR
								RPAD(COALESCE(CLIENTE."Sexo",''),1,' ') || --SEXO
								RPAD('',1,' ') || --ESTCIV
								RPAD('',1,' ') || --REGCON
								RPAD('',1,' ') || --ENVCOR
								RPAD('',1,' ') || --NACION
								RPAD('',4,' ') || --CODUSU
								RPAD('',50,' ') || --EMPLEA
								RPAD('',10,' ') || --CODBCO
								RPAD('',20,' ') || --CTACTE
								RPAD(COALESCE(TO_CHAR(CLIENTE."FechaIngreso",'YYYY-MM-DD'),''),10,' ') --FECING
								AS "Registro"
								FROM "BSCL.FondoMutuo.BD::FMM.InterfazAgf" INTERFAZ
								LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLIENTE ON (CLIENTE."IdCliente" = INTERFAZ."IdCliente")
								LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" DIRECCION_FISICA ON (DIRECCION_FISICA."IdDireccionFisica" = CLIENTE."IdDireccionFisicaPrincipal")
								LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" TELEFONO ON (TELEFONO."IdTelefono" = CLIENTE."IdTelefonoPrincipal")
								LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" DIRECCION_ELECTRONICA ON (DIRECCION_ELECTRONICA."IdDireccionElectronica" = CLIENTE."IdDireccionElectronicaPrincipal")
								WHERE INTERFAZ."IdEmpresa" = p_IdEmpresa
								AND INTERFAZ."IdEstado" = 'PEN'
								AND INTERFAZ."TipoRegistro" = 'PA';
						/*		UNION ALL
					    SELECT "IdInterfazAgf",
							   "TipoRegistro",
							    LPAD('',11,' ') ||
								LPAD('',2,' ') ||
								LPAD('',10,' ') ||
								LPAD('',10,' ') ||
								LPAD("EstadoBloqueoFondo",1,' ') ||
								LPAD('',1,' ') ||
								LPAD("IdSerieFondo",10,' ') ||
								LPAD('',4,' ') ||
								LPAD('',14,' ') ||
								LPAD('',14,' ') ||
								LPAD('',14,' ') ||
								LPAD('',1,' ') ||
								LPAD('',3,' ') ||
								LPAD('',3,' ') ||
								LPAD('',10,' ') ||
								LPAD('',3,' ') ||
								LPAD('',1,' ') AS "Registro"
								FROM "BSCL.FondoMutuo.BD::FMM.InterfazAgf" 
								WHERE "IdEmpresa" = p_IdEmpresa
								AND "IdEstado" = 'PEN'
								AND "TipoRegistro" = 'BD'; */
		
		v_estado_solicitudes = SELECT "IdInterfazAgf",
		                              'RES' as "IdEstado",
		                              '' as "Observacion"
		                       FROM :v_solicitudes;
		
		CALL "BSCL.FondoMutuo.BD.Servicios::CambiarEstadoSolicitudAgf"(:p_IdEmpresa,:v_estado_solicitudes,p_out_ResultadoEstado,:p_out_AdvertenciasContadorEstado,p_out_AdvertenciasDetalleEstado,:p_out_ErroresContadorEstado, p_out_ErroresDetalleEstado);
												
		p_out_Resultado = SELECT "IdInterfazAgf" AS "IdInterfazAgf",
								 "TipoRegistro",
								 "Registro" AS "Registro"
						  FROM :v_solicitudes
						  ORDER BY "IdInterfazAgf" ASC;
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CONSULTAR SOLICITUD AGF','FIN','FIN');
		
		SIGNAL SALIDA_CONTROLADA;
		
		END;
	END;
END;
							   
