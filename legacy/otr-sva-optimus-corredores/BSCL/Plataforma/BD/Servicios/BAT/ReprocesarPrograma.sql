SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::BAT.ReprocesarPrograma";

CREATE PROCEDURE
-- 
--  Proposito: reprocesar programa, clona registro de programa.
--  Autor: JG - BCS 
--  Fecha Creacion: 01/12/2016
--   
/*
	call "BSCL.Plataforma.BD.Servicios::BAT.ReprocesarPrograma" (
		p_IdEmpresa				=> 1,
		p_IdUsuario				=> 1,
	 	p_IdModulo				=> 'PLA',
	 	p_IdPrograma			=> 882771,
	 	p_Segundos				=> 0,
	 	SALIDA					=> ?,
		p_out_ErroresContador 	=> ?
	); 
*/

"BSCL.Plataforma.BD.Servicios::BAT.ReprocesarPrograma" (
	IN p_IdEmpresa				BIGINT,
	IN p_IdUsuario				BIGINT,
 	IN p_IdModulo				NVARCHAR(3),
 	IN p_IdPrograma				BIGINT,	
 	IN p_Segundos				BIGINT default 0, --seg extra para ejecucion inmediata			
 	OUT SALIDA					NVARCHAR(520),
 	OUT p_out_ErroresContador 	INT
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	declare v_IdProgramaNuevo bigint:=0;
	declare v_countPrograma bigint:=0;
	declare v_IdAgenda bigint := 0;
	declare v_IdEstado nvarchar(3):='';
	declare v_countRegistroPendiente bigint:=0;
	declare v_continuar nvarchar(1):='S';
 	declare v_fechaAProgramar seconddate;
 	declare fechaSistemaUTC NVARCHAR(40);
	declare fechaSistemaLocal NVARCHAR(40);
	declare fechaHorasistemaUTC NVARCHAR(40); 
	declare fechaHorasistemaLOCAL NVARCHAR(40);
	declare fechaHoraUTC NVARCHAR(40);
	declare fechaHoraLocal NVARCHAR(40);	
	declare v_fechaSistema DATE;
	SALIDA := 'ERROR';
	p_out_ErroresContador:=1;
	v_DETALLELOG = SELECT 0 AS "IdCabeceraLog",'' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo", '' as "ValorNuevo" from dummy where 1=2;
	-- validaciones:
	select top 1 1,"IdAgenda","idEstado" into v_countPrograma,v_IdAgenda,v_IdEstado from "BSCL.Plataforma.BD::BAT.Programa" where "IdPrograma" = :p_IdPrograma;
	--select 'Programa',* from "BSCL.Plataforma.BD::BAT.Programa" where "IdPrograma" = :p_IdPrograma;--borrar
	
	if(v_countPrograma=0) then
		SALIDA := 'No existe programa';
		v_continuar := 'N';
	end if;
	if(v_continuar='S' and v_IdAgenda=0) then
		SALIDA := 'Existe programa pero no es agenda';
		v_continuar := 'N';
	end if;
	if( v_continuar='S' and v_IdEstado!='PEN') then
		SALIDA := 'Programa no presenta error';
		v_continuar := 'N';
	end if;
	
 	IF( v_continuar='S') THEN -- ejecucion
		IF :p_Segundos = 0 THEN
	 	    select CURRENT_UTCTIMESTAMP into v_fechaAProgramar from dummy;
		ELSE
			select add_seconds(CURRENT_UTCTIMESTAMP,p_Segundos) into v_fechaAProgramar from dummy;
		END IF; 	
		call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechas"(
			p_IdEmpresa					=> p_IdEmpresa,
			p_out_FechaSistemaUTC   	=> fechaSistemaUTC,
			p_out_FechaSistemaLOCAL   	=> fechaSistemaLocal,
			p_out_FechaHoraSistemaUTC   => fechaHorasistemaUTC,
			p_out_FechaHoraSistemaLOCAL => fechaHorasistemaLOCAL,
			p_out_FechaHoraUTC   		=> fechaHoraUTC,
			p_out_FechaHoraLOCAL   		=> fechaHoraLocal
		);
		SELECT TO_DATE(to_char(SUBSTRING(fechaHorasistemaUTC,1,10),'YYYY-MM-DD'),'YYYY-MM-DD') into v_fechaSistema  FROM DUMMY;
		-- ACTUALIZACION DEL REGISTRO
	 	SELECT "BSCL.Plataforma.BD.Secuencias::Programa".NEXTVAL into v_IdProgramaNuevo from dummy;
	 	
	 	INSERT INTO "BSCL.Plataforma.BD::BAT.Programa"(
	 		"IdPrograma",
	 		"IdAgenda",
	 		"IdTipoPrograma",
	 		"Intento",
	 		"IdUsuario",
	 		"idEstado",
	 		"Resultado",
	 		"fechaCreacion",
	 		"fechaEjecucion",
	 		"fechaSistema",
	 		"Nodo",
	 		"IdProgramaReprocesado")
		SELECT 
			v_IdProgramaNuevo,
			"IdAgenda",
			"IdTipoPrograma",
			0,
			p_IdUsuario,
			'PEN',
			'Esperando ejecución',
			fechaHoraUTC,
			v_fechaAProgramar,
			v_fechaSistema,
			"Nodo",
			"IdPrograma" -- mantener la referencia
		FROM "BSCL.Plataforma.BD::BAT.Programa" WHERE "IdPrograma" = :p_IdPrograma;
       
	 	
		-- LOG GENERAL SISTEMA
		v_log = SELECT	
			"BSCL.Plataforma.BD.Secuencias::Log".nextval as "IdCabeceraLog",
			'INFORMATIVO' 		as "IdTipoLog",
			'BAT' 				as "IdModulo",
	 		:p_IdUsuario 		as "IdUsuario",
	 		:p_IdEmpresa 		as "IdEmpresa",
	 		:v_fechaSistema 	as "FechaSistemaLog",
	 		:p_IdUsuario 		as "IdUsuarioAtendido",
	 		0 					as "IdPersona",
	 		0					as "IdCliente",
			''					as "EstadoCliente",
			''					as "NombreCliente",
			''					as "IdTipoDocumentoIdentidad",
			''					as "IdDocumentoIdentidad",
			0					as "IdPortafolio",
			''					as "EstadoPortafolio",
			'EJEPRO' 			as "IdTipoRegistro", 
			:v_IdProgramaNuevo 	as "IdRegistro",
			'PEN' 				as "EstadoRegistro",
			0					as "IdTipoOperacion",
			0	 				as "Folio",
			0	 				as "Cantidad",
			0	 				as "PrecioTasa",
			0	 				as "Monto",
			0	 				as "IdMonedaOrigen",
			0	 				as "IdMonedaLiquidacion",
			0	 				as "TipoCambio",
			0	 				as "Nemo",
			:v_IdProgramaNuevo	as "IdProceso",
			'PEN' 				as "EstadoProceso",
			'REPROCESARPROGRAMA: IdPrograma Origen: '|| cast(:p_IdPrograma as nvarchar) as "NombreProceso"
		FROM dummy;
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_DETALLELOG);
	 	SALIDA := 'OK';
	 	p_out_ErroresContador:=0;
	END IF; -- ejecucion
END;				
 