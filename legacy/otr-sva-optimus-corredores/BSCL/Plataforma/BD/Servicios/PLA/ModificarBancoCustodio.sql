SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::PLA.ModificarBancoCustodio" ;

CREATE PROCEDURE
-- 
--  Proposito: Servicio para la modificacion de Banco Custodio
--  Autor: JGarces - BCS
--  Fecha Creacion: 02/05/2016
-- 

"BSCL.Plataforma.BD.Servicios::PLA.ModificarBancoCustodio" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					bigint,			-- ID  Empresa destino
	IN  p_IdUsuario					bigint,			-- ID  del usuario que solicita el servicio
	IN  p_IdModulo					nvarchar(3),  	-- ID  del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 		nvarchar(1)	DEFAULT 'S',	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	IN  p_Codigo					NVARCHAR(40),
	IN  p_IdEstado	 				NVARCHAR(3), 
	IN  p_IdPersona			 		BIGINT,
	IN  p_EsBancoLiquidador		 	NVARCHAR(1), 
	IN  p_EsBancoCustodio           NVARCHAR(1),
		
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_InformacionContador 	int,
	OUT p_out_InformacionDetalle 	table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520)) DEFAULT EMPTY,
	OUT p_out_AdvertenciasContador 	int,
	OUT p_out_AdvertenciasDetalle 	table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520))  DEFAULT EMPTY,	
	OUT p_out_ErroresContador 		int,
	OUT p_out_ErroresDetalle 		table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))  DEFAULT EMPTY
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
AS
BEGIN
	DECLARE v_mensajes TABLE(	"IdInterno" BIGINT,"Resultado" NVARCHAR(100),"Codigo" NVARCHAR(100),"CodigoDato1" NVARCHAR(100),"ValorDato1" NVARCHAR(520),"CodigoDato2" NVARCHAR(100),"ValorDato2" NVARCHAR(520)) DEFAULT
	 					SELECT 	0 as "IdInterno",'' AS "Resultado",'' AS "Codigo",'' AS "CodigoDato1",'' AS "ValorDato1",'' AS "CodigoDato2",'' AS "ValorDato2" FROM DUMMY where 1=2;
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		v_TablaCodigosMensajes = 	SELECT "IdInterno","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2"
									FROM :v_mensajes msjs;
		
		
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
		
		p_out_InformacionDetalle = SELECT msj."Codigo",det."Descripcion" FROM :v_mensajes msj INNER JOIN :v_out_ErroresDetalleMensajes det on msj."IdInterno" = det."IdInterno" and msj."Codigo" = det."Codigo" WHERE msj."Resultado" = 'INFORMACION';
		p_out_AdvertenciasDetalle = SELECT msj."Codigo",CASE WHEN msj."Resultado" = 'AUTORIZACION' THEN 'S' ELSE 'N' END AS "RequiereAutorizacion",det."Descripcion" FROM :v_mensajes msj INNER JOIN :v_out_ErroresDetalleMensajes det on msj."IdInterno" = det."IdInterno" and msj."Codigo" = det."Codigo" WHERE msj."Resultado" in ('ADVERTENCIA','AUTORIZACION');
		p_out_ErroresDetalle 	= SELECT msj."Codigo",det."Descripcion" FROM :v_mensajes msj INNER JOIN :v_out_ErroresDetalleMensajes det on msj."IdInterno" = det."IdInterno" and msj."Codigo" = det."Codigo" WHERE msj."Resultado" = 'RECHAZO';
		
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_InformacionContador 
		from :p_out_InformacionDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
	END;
	
	BEGIN
		
		--codigo repetido
		v_codigoRepetido = SELECT "Codigo" FROM "BSCL.Plataforma.BD::PLA.BancoCustodio" where "Codigo" = :p_Codigo and "IdPersona" != :p_IdPersona;
		IF (NOT IS_EMPTY(:v_codigoRepetido)) THEN
			v_mensajes = SELECT 0 as "IdInterno",'RECHAZO' AS "Resultado",'CODIGO_YA_EXISTE' AS "Codigo",'' AS "CodigoDato1",'' AS "ValorDato1",'' AS "CodigoDato2",'' AS "ValorDato2" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
				
		UPDATE "BSCL.Plataforma.BD::PLA.BancoCustodio" 
		SET "Codigo" = :p_Codigo,
		"IdEstado" =  :p_IdEstado,
		"EsBancoLiquidador" = :p_EsBancoLiquidador,
		"EsBancoCustodio" = :p_EsBancoCustodio,
		"FechaUltimaModificacion" = CURRENT_TIMESTAMP,
		"IdUsuarioUltimaModificacion" = :p_IdUsuario
		WHERE "IdPersona" = :p_IdPersona;
		
		v_mensajes = SELECT 0 as "IdInterno",'INFORMACION' AS "Resultado",'BANCO_CUSTODIO_ACTUALIZADO' AS "Codigo",'' AS "CodigoDato1",'' AS "ValorDato1",'' AS "CodigoDato2",'' AS "ValorDato2" FROM DUMMY;
		SIGNAL SALIDA_CONTROLADA;
						
	END;	
END;
