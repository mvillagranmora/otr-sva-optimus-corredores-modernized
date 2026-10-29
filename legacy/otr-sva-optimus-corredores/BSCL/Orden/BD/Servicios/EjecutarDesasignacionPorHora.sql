SET SCHEMA BSCL;

DROP PROCEDURE "BSCL.Orden.BD.Servicios::EjecutarDesasignacionPorHora";

CREATE PROCEDURE
-- 
--  Proposito: desasignacion por hora
--  Autor: TEAM ORDENES
--  Fecha Creacion: 28/12/2015
--   
"BSCL.Orden.BD.Servicios::EjecutarDesasignacionPorHora" (

	IN p_IdEmpresa          	BIGINT,
    IN p_IdUsuario          	BIGINT,
    IN p_IdModulo           	NVARCHAR(3), 
	IN p_simularOperacion		NVARCHAR(1) DEFAULT 'N', -- S/N se usa para simular operacion (algunos casos)
    IN p_UsaReglasNegocio	 	NVARCHAR(1) DEFAULT 'S', -- S/N se usa en caso de servicios anidados.
	IN p_HoraDesde				NVARCHAR(10),
	IN p_IdLibroOrdenes			BIGINT DEFAULT NULL,
	IN p_IdClasificacionTipoInstrumento		NVARCHAR(2) DEFAULT NULL,
	IN p_IdTipoInstrumento		NVARCHAR(3) DEFAULT NULL,
	IN p_IdAccionOrden		NVARCHAR(40) DEFAULT NULL,
	IN p_IdBolsa				NVARCHAR(3) DEFAULT NULL,
	IN p_IdInstrumento			BIGINT,
	
	/*OUT p_out_Resultado			TABLE(
									"IdAsignacion" 		BIGINT									
							),*/
	
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("IdInterno" BIGINT,"Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("IdInterno" BIGINT,"Codigo" nvarchar(100),"Descripcion" nvarchar(5000))
	
	 
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
	DECLARE v_ERRORES "BSCL.Orden.BD.Tipos::SalidaDetalle" DEFAULT SELECT 0 as "IdInterno",0 as "IdOperacion",'' AS "Resultado",'' AS "Codigo",'' as "CodigoDato1",'' as "ValorDato1" ,'' as "CodigoDato2", '' as "ValorDato2" FROM DUMMY WHERE 1=2;

	DECLARE SALIDA_CONTROLADA CONDITION;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN
		
		v_TablaCodigosMensajes = SELECT 
								"IdInterno",
								"Codigo",
								"CodigoDato1",
								"ValorDato1" ,
								'' as "CodigoDato2",
								'' as "ValorDato2"
								FROM :v_ERRORES err
								WHERE err."Resultado" in ('RECHAZO','AUTORIZACION','ADVERTENCIA') ;
		  
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
		
		p_out_ErroresDetalle = 	SELECT err."IdInterno",
									err."Codigo",
									msj."Descripcion"
								FROM :v_ERRORES err
								INNER JOIN :v_out_ErroresDetalleMensajes msj on err."Codigo" = msj."Codigo" and err."IdInterno" = msj."IdInterno"
								where err."Resultado" in ('RECHAZO');
								
		p_out_AdvertenciasDetalle = SELECT adv."IdInterno",
									adv."Codigo",
									CASE adv."Resultado" WHEN 'AUTORIZACION' THEN 'S' ELSE 'N' END AS "RequiereAutorizacion",
									msj."Descripcion"
								FROM :v_ERRORES adv
								INNER JOIN :v_out_ErroresDetalleMensajes msj on adv."Codigo" = msj."Codigo" and adv."IdInterno" = msj."IdInterno"
								where adv."Resultado" in ('ADVERTENCIA','AUTORIZACION');
		
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;
		
	END;
	
	BEGIN
	
		DECLARE v_out_InformacionContador 	INTEGER;
		DECLARE v_out_AdvertenciasContador 	INTEGER;
		DECLARE v_out_ErroresContador 		INTEGER;
		DECLARE v_FechaSistema				NVARCHAR(10);
		DECLARE v_FechaOperacionDesde		SECONDDATE;
		DECLARE v_FechaOperacionHasta		SECONDDATE;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
		v_FechaOperacionDesde = TO_SECONDDATE(CONCAT (:v_FechaSistema, CONCAT(' ',:p_HoraDesde)));
		v_FechaOperacionHasta = TO_SECONDDATE(CONCAT (:v_FechaSistema, CONCAT(' ','23:59:59')));
			
		v_Asignaciones = SELECT "IdAsignacion"
								
								
							FROM "BSCL.Orden.BD.Vistas::ConsultarAsignacion"
							WHERE "FechaAsignacion" >= :v_FechaOperacionDesde AND "FechaAsignacion" <= :v_FechaOperacionHasta
							AND "IdLibroOrdenes" = COALESCE(:p_IdLibroOrdenes,"IdLibroOrdenes")
							AND "IdClasificacionTipoInstrumento" = COALESCE(:p_IdClasificacionTipoInstrumento,"IdClasificacionTipoInstrumento")
							AND "IdTipoInstrumento" = COALESCE(:p_IdTipoInstrumento,"IdTipoInstrumento")
							AND "AccionOrden" = COALESCE(:p_IdAccionOrden,"AccionOrden")
							AND "IdBolsa" = COALESCE(:p_IdBolsa,"IdBolsa")
							AND "IdInstrumento" = COALESCE(:p_IdInstrumento,"IdInstrumento")
							AND "IdEstadoAsignacion" = 'NEF';

		
		CALL "BSCL.Orden.BD.Servicios::AnularAsignacionMasiva" ( 
		    p_IdEmpresa 		=> :p_IdEmpresa,
		    p_IdUsuario			=> :p_IdUsuario,
		    p_IdModulo			=> :p_IdModulo,
			p_asignaciones		=> :v_asignaciones,
			
			p_out_OrdenesAnuladas 		=> v_out_OrdenesAnuladas,
			p_out_SalidaDetalle			=> v_out_SalidaDetalle,
			p_out_InformacionContador	=> v_out_InformacionContador,
			p_out_InformacionDetalle	=> v_out_InformacionDetalle,
			p_out_AdvertenciasContador	=> v_out_AdvertenciasContador,
			p_out_AdvertenciasDetalle	=> v_out_AdvertenciasDetalle,
			p_out_ErroresContador		=> v_out_ErroresContador,
			p_out_ErroresDetalle 		=> v_out_ErroresDetalle
		    
		);
								
		SIGNAL SALIDA_CONTROLADA;
	
	END;

END
