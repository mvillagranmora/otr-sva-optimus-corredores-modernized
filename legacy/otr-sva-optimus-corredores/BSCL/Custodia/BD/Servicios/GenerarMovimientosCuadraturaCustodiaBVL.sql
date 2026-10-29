SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::GenerarMovimientosCuadraturaCustodiaBVL";
GO
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::GenerarMovimientosCuadraturaCustodiaBVL" (
	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa                		BIGINT,
    IN p_IdUsuario                		BIGINT,
    IN p_IdModulo                 		NVARCHAR(3), 
  	IN p_AgrupaPorBoveda 				NVARCHAR(1) default 'N',
	IN p_FechaConsulta 					NVARCHAR(10) default '0001-01-01',

	-- Salidas para mensajes de advertencias y errores
	OUT p_out_InformacionContador		INT,
	OUT p_out_InformacionDetalle		TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)),

	OUT p_out_AdvertenciasContador  	int,
	OUT p_out_AdvertenciasDetalle  	 	table ("Codigo" nvarchar(41),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),

	OUT p_out_ErroresContador  	 	 	int,
	OUT p_out_ErroresDetalle  	 	 	table ("Codigo" nvarchar(41),"Descripcion" nvarchar(5000))
 ) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------
	DECLARE SALIDA_CONTROLADA CONDITION;
	DECLARE v_errorcuenta INT DEFAULT 0;
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);
	
	DECLARE v_DoctoIdentidadCpro 		NVARCHAR(40) := '';
	DECLARE v_IdPersonaCPro 			BIGINT := 0;
	DECLARE v_FechaConsulta 			NVARCHAR(10) := '2300-12-31';
	

	DECLARE p_out_tbl table (
   		 "Fecha" 							NVARCHAR(10) 
   		,"IdClasificacionTipoInstrumento"	nvarchar(3)
   		,"IdTipoInstrumento" 				nvarchar(3)
   		,"IdInstrumento" 					bigint
  		,"Nemotecnico" 						nvarchar(40)
  		,"Relacion" 						nvarchar(10)
  		,"IdBoveda" 						nvarchar(3)
  		,"CuentaBoveda" 					nvarchar(80)
		,"CantidadCustodia" 				DECIMAL(28,8)
		,"CantidadSimulCom" 				DECIMAL(28,8)
		,"CantidadSimulVen" 				DECIMAL(28,8)
		,"CantidadPrestamo" 				DECIMAL(28,8)
		,"CantidadEmpresti" 				DECIMAL(28,8)
		,"CantidadGarantia" 				DECIMAL(28,8)
		,"CantidadTransIng" 				DECIMAL(28,8)
		,"CantidadTransEgr" 				DECIMAL(28,8)
		,"CantidadTotal" 					DECIMAL(35,8)
		,"MontoValorizado" 					DECIMAL(33,8)
   	  );
   	  
	DECLARE v_tblInst table (
   		 "IdClasificacionTipoInstrumento" nvarchar(3)
   		,"IdTipoInstrumento" nvarchar(3)
   		,"IdInstrumento" bigint
   		,"Nemotecnico" nvarchar(40)
   		,"IdCliente" bigint
   		,"IdPortafolio" bigint
   		,"IdBoveda" nvarchar(3)
   		,"IdCuentaCustodioPortafolio" bigint
	 );

	DECLARE v_tblCustodia table (
   		 "IdClasificacionTipoInstrumento" nvarchar(3)
   		,"IdTipoInstrumento" nvarchar(3)
   		,"IdInstrumento" bigint
   		,"Nemotecnico" nvarchar(40)
   		,"IdCliente" bigint
   		,"IdPortafolio" bigint
   		,"IdBoveda" nvarchar(3)
   		,"Cantidad" DECIMAL(28,8)
   		,"MontoValorizado" DECIMAL(33,8)
   		,"IdCuentaCustodioPortafolio" bigint
	 );
	 
	DECLARE v_tblSimulC table (
   		 "IdClasificacionTipoInstrumento" nvarchar(3)
   		,"IdTipoInstrumento" nvarchar(3)
   		,"IdInstrumento" bigint
   		,"Nemotecnico" nvarchar(40)
   		,"IdCliente" bigint
   		,"IdPortafolio" bigint
   		,"IdBoveda" nvarchar(3)
   		,"Cantidad" DECIMAL(28,8)
   		,"MontoValorizado" DECIMAL(33,8)
   		,"IdCuentaCustodioPortafolio" bigint
	 );
	 
	DECLARE v_tblSimulV table (
   		 "IdClasificacionTipoInstrumento" nvarchar(3)
   		,"IdTipoInstrumento" nvarchar(3)
   		,"IdInstrumento" bigint
   		,"Nemotecnico" nvarchar(40)
   		,"IdCliente" bigint
   		,"IdPortafolio" bigint
   		,"IdBoveda" nvarchar(3)
   		,"Cantidad" DECIMAL(28,8)
   		,"MontoValorizado" DECIMAL(33,8)
   		,"IdCuentaCustodioPortafolio" bigint
	 );

	DECLARE v_tblPrestamos table (
   		 "IdClasificacionTipoInstrumento" nvarchar(3)
   		,"IdTipoInstrumento" nvarchar(3)
   		,"IdInstrumento" bigint
   		,"Nemotecnico" nvarchar(40)
   		,"IdCliente" bigint
   		,"IdPortafolio" bigint
   		,"IdBoveda" nvarchar(3)
   		,"Cantidad" DECIMAL(28,8)
   		,"MontoValorizado" DECIMAL(33,8)
   		,"IdCuentaCustodioPortafolio" bigint
	 );
	 
	DECLARE v_tblEmprestitos table (
   		 "IdClasificacionTipoInstrumento" nvarchar(3)
   		,"IdTipoInstrumento" nvarchar(3)
   		,"IdInstrumento" bigint
   		,"Nemotecnico" nvarchar(40)
   		,"IdCliente" bigint
   		,"IdPortafolio" bigint
   		,"IdBoveda" nvarchar(3)
   		,"Cantidad" DECIMAL(28,8)
   		,"MontoValorizado" DECIMAL(33,8)
   		,"IdCuentaCustodioPortafolio" bigint
	 );
	 
	DECLARE v_tblGarantias table (
   		 "IdClasificacionTipoInstrumento" nvarchar(3)
   		,"IdTipoInstrumento" nvarchar(3)
   		,"IdInstrumento" bigint
   		,"Nemotecnico" nvarchar(40)
   		,"IdCliente" bigint
   		,"IdPortafolio" bigint
   		,"IdBoveda" nvarchar(3)
   		,"Cantidad" DECIMAL(28,8)
   		,"MontoValorizado" DECIMAL(33,8)
   		,"IdCuentaCustodioPortafolio" bigint
	 );
	 
	DECLARE v_tblTrasnIng table (
   		 "IdClasificacionTipoInstrumento" nvarchar(3)
   		,"IdTipoInstrumento" nvarchar(3)
   		,"IdInstrumento" bigint
   		,"Nemotecnico" nvarchar(40)
   		,"IdCliente" bigint
   		,"IdPortafolio" bigint
   		,"IdBoveda" nvarchar(3)
   		,"Cantidad" DECIMAL(28,8)
   		,"MontoValorizado" DECIMAL(33,8)
   		,"IdCuentaCustodioPortafolio" bigint
	 );
	 
	DECLARE v_tblTrasnEgr table (
   		 "IdClasificacionTipoInstrumento" nvarchar(3)
   		,"IdTipoInstrumento" nvarchar(3)
   		,"IdInstrumento" bigint
   		,"Nemotecnico" nvarchar(40)
   		,"IdCliente" bigint
   		,"IdPortafolio" bigint
   		,"IdBoveda" nvarchar(3)
   		,"Cantidad" DECIMAL(28,8)
   		,"MontoValorizado" DECIMAL(33,8)
   		,"IdCuentaCustodioPortafolio" bigint
	 );	
 
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

	p_out_InformacionContador	:= 0;
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;

	-----------------------------------------------------
	--Validaciones basicas (Codigos,Existencias,etc)
	-----------------------------------------------------
	BEGIN
		SELECT count(1) INTO p_out_ErroresContador
	    FROM "BSCL.Custodia.BD::CUS.CuadraturaCustodia"
		WHERE "FechaCuadraturaCustodia" = p_FechaConsulta
		  and "IdEstado" = 'VIG';
		  

		IF (p_out_ErroresContador > 0) THEN

           	v_TablaCodigosMensajes =	Select 	11 									As "IdInterno", 
											'CUADRATURA_CUSTODIA_YA_EJECUTADA'		As "Codigo",
											'p_FechaConsulta'						As "CodigoDato1", 
											Cast(p_FechaConsulta As NVARCHAR(520))	As "ValorDato1",
											''			 							As "CodigoDato2", 
											''										As "ValorDato2" 
									From dummy;
		                        
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_out_hash);
			
			p_out_ErroresDetalle = 	Select "Codigo", "Descripcion" 
									From :v_out_hash;
	 	END IF;
		 	
		--Si existen errores en las validaciones debe salir
		select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle;
		
		IF :p_out_ErroresContador > 0 THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;				
	END;
	-----------------------------------------------------
	--fin Validaciones basicas (Codigos,Existencias,etc)
	-----------------------------------------------------
	
	IF p_FechaConsulta = '0001-01-01' THEN
		Select "Valor" into v_FechaConsulta from "BSCL.Plataforma.BD::PLA.Configuracion" where "CodigoParametro" = 'PLA.FechaSistema';
	else
		v_FechaConsulta := p_FechaConsulta;
	END IF;
	
	----- Saca el DoctoIdentidad de la cuenta propia 
	SELECT  CC."DocumentoIdentidad", CC."IdPersona" into v_DoctoIdentidadCpro, v_IdPersonaCPro
	FROM "BSCL.Plataforma.BD::PLA.Empresa" AS E
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS CSN ON CSN."IdSocioDeNegocio" = E."IdSocioDeNegocio"
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CC ON CC."IdCliente" = CSN."IdCliente"
	WHERE E."IdEmpresa" = p_IdEmpresa;
	

	----------------------------------------------------------
	---- inicializa las tablas de paso

	v_tblCustodia = select  '' as "IdClasificacionTipoInstrumento", '' as "IdTipoInstrumento", 0 as "IdInstrumento", '' as  "Nemotecnico", 0 as "IdCliente", 0 as "IdPortafolio", '' as "IdBoveda", 0 as "Cantidad", 0 as "MontoValorizado", 0 AS "IdCuentaCustodioPortafolio"
					from DUMMY where 1=2;
	v_tblSimulC = select  '' as "IdClasificacionTipoInstrumento", '' as "IdTipoInstrumento", 0 as "IdInstrumento", '' as  "Nemotecnico", 0 as "IdCliente", 0 as "IdPortafolio", '' as "IdBoveda", 0 as "Cantidad", 0 as "MontoValorizado", 0 AS "IdCuentaCustodioPortafolio"
					from DUMMY where 1=2;
	v_tblSimulV = select  '' as "IdClasificacionTipoInstrumento", '' as "IdTipoInstrumento", 0 as "IdInstrumento", '' as  "Nemotecnico", 0 as "IdCliente", 0 as "IdPortafolio", '' as "IdBoveda", 0 as "Cantidad", 0 as "MontoValorizado", 0 AS "IdCuentaCustodioPortafolio"
					from DUMMY where 1=2;
	v_tblPrestamos = select  '' as "IdClasificacionTipoInstrumento", '' as "IdTipoInstrumento", 0 as "IdInstrumento", '' as  "Nemotecnico", 0 as "IdCliente", 0 as "IdPortafolio", '' as "IdBoveda", 0 as "Cantidad", 0 as "MontoValorizado", 0 AS "IdCuentaCustodioPortafolio"
					from DUMMY where 1=2;
	v_tblEmprestitos = select  '' as "IdClasificacionTipoInstrumento", '' as "IdTipoInstrumento", 0 as "IdInstrumento", '' as  "Nemotecnico", 0 as "IdCliente", 0 as "IdPortafolio", '' as "IdBoveda", 0 as "Cantidad", 0 as "MontoValorizado", 0 AS "IdCuentaCustodioPortafolio"
					from DUMMY where 1=2;
	v_tblGarantias = select  '' as "IdClasificacionTipoInstrumento", '' as "IdTipoInstrumento", 0 as "IdInstrumento", '' as  "Nemotecnico", 0 as "IdCliente", 0 as "IdPortafolio", '' as "IdBoveda", 0 as "Cantidad", 0 as "MontoValorizado", 0 AS "IdCuentaCustodioPortafolio"
					from DUMMY where 1=2;
	v_tblTrasnIng = select  '' as "IdClasificacionTipoInstrumento", '' as "IdTipoInstrumento", 0 as "IdInstrumento", '' as  "Nemotecnico", 0 as "IdCliente", 0 as "IdPortafolio", '' as "IdBoveda", 0 as "Cantidad", 0 as "MontoValorizado", 0 AS "IdCuentaCustodioPortafolio"
					from DUMMY where 1=2;
	v_tblTrasnEgr = select  '' as "IdClasificacionTipoInstrumento", '' as "IdTipoInstrumento", 0 as "IdInstrumento", '' as  "Nemotecnico", 0 as "IdCliente", 0 as "IdPortafolio", '' as "IdBoveda", 0 as "Cantidad", 0 as "MontoValorizado", 0 AS "IdCuentaCustodioPortafolio"
					from DUMMY where 1=2;
    
	  
	-------------------------------------------------------------
	-- llena en una tabla todos los instrumentos del portafolio
	begin
		-- CARTERA
		v_tblInst =
				select  "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", 
						case when "IdClasificacionTipoInstrumento" = 'IF' then "NemoSVS" else "Nemotecnico" end as "Nemotecnico", 
						"IdCliente", "IdPortafolio",
						case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda",
						"IdCuentaCustodioPortafolio"
				from "BSCL.Custodia.BD::CUS.Cartera"
				where "IdEmpresa" = p_IdEmpresa AND
				  ----******* por ahora usa "FechaLiquidación", posteriormente cambiar por "INGFechaLiquidacion" cuando se implemente el nuevo modelo
				  v_FechaConsulta BETWEEN "INGFechaOperacion" and case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end and
				  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
				  v_FechaConsulta < case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end and
				  ---*************************
					  "IdEstado" <> 'ANU' and 
					  "IdTipoInstrumento" NOT IN ('PRT', 'EMP', 'SIM','RRV','RRF') 
				group by "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", 
							case when "IdClasificacionTipoInstrumento" = 'IF' then "NemoSVS" else "Nemotecnico" end, 
							"IdCliente", "IdPortafolio",case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,"IdCuentaCustodioPortafolio";

		-- GARANTIAS
		v_tblInst =
				select  "IdClasificacionTipoInstrumentoAval" as "IdClasificacionTipoInstrumento", 
					"IdTipoInstrumentoAval" as "IdTipoInstrumento", 
					"IdInstrumento", 
					case when "IdClasificacionTipoInstrumento" = 'IF' then "NemoSVS" else "Nemotecnico" end as "Nemotecnico", 
					"IdCliente", "IdPortafolio",
					case when p_AgrupaPorBoveda = 'S' then "IdBovedaOperacionOrigen" else '' end as "IdBoveda",
					"IdCuentaCustodioPortafolio"
				from "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantias"
				where "IdEmpresa" = p_IdEmpresa AND
					  "IdEstado" <> 'ANU' AND 
					  ----******* por ahora usa "FechaLiquidación", posteriormente cambiar por "INGFechaLiquidacion" cuando se implemente el nuevo modelo
					  v_FechaConsulta BETWEEN "INGFechaOperacion" and case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end and
					  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
					  v_FechaConsulta < case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') END
					  AND "IdBovedaOperacionOrigen" <> ''
					  ---*************************
				group by "IdClasificacionTipoInstrumentoAval", "IdTipoInstrumentoAval", "IdInstrumento", 
						case when "IdClasificacionTipoInstrumento" = 'IF' then "NemoSVS" else "Nemotecnico" end, 
						"IdCliente", "IdPortafolio", case when p_AgrupaPorBoveda = 'S' then "IdBovedaOperacionOrigen" else '' end,"IdCuentaCustodioPortafolio"
					
				UNION ALL 
					SELECT "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "IdCliente", "IdPortafolio", "IdBoveda","IdCuentaCustodioPortafolio"
					from :v_tblInst;

					
		-- MOVIMIENTOS EN TRANSITO
		/*v_tblInst = 
				select  "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", 
						case when "IdClasificacionTipoInstrumento" = 'IF' then "NemoSVS" else "Nemotecnico" end as "Nemotecnico", 
						"IdCliente", "IdPortafolio",
						case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda",
						"IdCuentaCustodioPortafolio"
				from "BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraEnTransito"
				where 	"IdEmpresa" = p_IdEmpresa AND
						"IdEstado" = 'PEN'
				group by "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", 
								case when "IdClasificacionTipoInstrumento" = 'IF' then "NemoSVS" else "Nemotecnico" end, 
								"IdCliente", "IdPortafolio",case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end,"IdCuentaCustodioPortafolio"
				UNION ALL 
					SELECT "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "IdCliente", "IdPortafolio", "IdBoveda","IdCuentaCustodioPortafolio"
					from :v_tblInst;*/
 		-- PRESTAMOS				
		v_tblInst = select  "IdClasificacionTipoInstrumentoSubyacente" as "IdClasificacionTipoInstrumento"
							, "IdTipoInstrumentoSubyacente" as "IdTipoInstrumento"
							, "IdInstrumentoSubyacente" as "IdInstrumento"
							, "NemoSubyacente" as "Nemotecnico"
							,"IdCliente", "IdPortafolio"
							, case when p_AgrupaPorBoveda = 'S' then "IdBovedaSubyacente" else '' end as "IdBoveda"
							, "IdCuentaCustodioPortafolio"
					from "BSCL.Custodia.BD.Vistas::PRT.ConsultarCarteraPrestamo"
					where "IdEmpresa" = p_IdEmpresa AND
						  ----******* por ahora usa "FechaLiquidación", posteriormente cambiar por "INGFechaLiquidacion" cuando se implemente el nuevo modelo
						  v_FechaConsulta BETWEEN "INGFechaOperacion" and case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end and
						  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
						  v_FechaConsulta < case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end and
						  ---*************************
							  "IdEstado" <> 'ANU'
						group by "IdClasificacionTipoInstrumentoSubyacente", "IdTipoInstrumentoSubyacente", "IdInstrumentoSubyacente", "NemoSubyacente","IdCliente", "IdPortafolio", case when p_AgrupaPorBoveda = 'S' then "IdBovedaSubyacente" else '' end,"IdCuentaCustodioPortafolio"
						
				UNION ALL 
					SELECT "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "IdCliente", "IdPortafolio", "IdBoveda","IdCuentaCustodioPortafolio"
					from :v_tblInst;
		-- SIMULTANEA	
					/*
		v_tblInst = select  "suby_IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
							, "suby_IdTipoInstrumento" as "IdTipoInstrumento"
							, "suby_IdInstrumento" as "IdInstrumento"
							, case when "suby_IdClasificacionTipoInstrumento" = 'IF' then "suby_NemoSVS" else "suby_Nemotecnico" end as "Nemotecnico"
							,"IdCliente", "IdPortafolio"
							, case when p_AgrupaPorBoveda = 'S' then "suby_IdBoveda" else '' end as "IdBoveda"
							, "IdCuentaCustodioPortafolio"
					from "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea"
					where "IdEmpresa" = p_IdEmpresa AND
						  ----******* por ahora usa "FechaLiquidación", posteriormente cambiar por "INGFechaLiquidacion" cuando se implemente el nuevo modelo
						  v_FechaConsulta BETWEEN "INGFechaOperacion" and case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end and
						  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
						  v_FechaConsulta < case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end and
						  ---*************************
							  "IdEstado" <> 'ANU' 
						group by "suby_IdClasificacionTipoInstrumento", "suby_IdTipoInstrumento", "suby_IdInstrumento", case when "suby_IdClasificacionTipoInstrumento" = 'IF' then "suby_NemoSVS" else "suby_Nemotecnico" end, "IdCliente", "IdPortafolio",case when p_AgrupaPorBoveda = 'S' then "suby_IdBoveda" else '' end,"IdCuentaCustodioPortafolio"
					UNION ALL 
						SELECT "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "IdCliente", "IdPortafolio", "IdBoveda","IdCuentaCustodioPortafolio"
						from :v_tblInst;
						*/

		v_tblInst = SELECT distinct "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "IdCliente", "IdPortafolio", "IdBoveda", "IdCuentaCustodioPortafolio"
						from :v_tblInst;					
			
			
	end;

	------------------ 
	--custodia
	 begin 

		v_tblCustodia =
			select  "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", 
				case when "IdClasificacionTipoInstrumento" = 'IF' then "NemoSVS" else "Nemotecnico" end as "Nemotecnico", 
				"IdCliente", "IdPortafolio",
				case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda", 
				IFNULL(SUM("INGCantidad"),0) as "Cantidad",
				IFNULL(SUM("INGMontoATasaMercadoActual"),0) as "MontoValorizado",
				"IdCuentaCustodioPortafolio"
			from "BSCL.Custodia.BD::CUS.Cartera"
			where "IdEmpresa" = p_IdEmpresa AND
				  "IdTipoInstrumento" NOT IN ('PRT', 'EMP', 'SIM','RRV','RRF') AND 
				  ----******* por ahora usa "FechaLiquidación", posteriormente cambiar por "INGFechaLiquidacion" cuando se implemente el nuevo modelo
				  v_FechaConsulta BETWEEN "INGFechaOperacion" and case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end and
				  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
				  v_FechaConsulta < case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end and
				  ---*************************
				  "IdEstado" <> 'ANU'
			group by "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", 
					case when "IdClasificacionTipoInstrumento" = 'IF' then "NemoSVS" else "Nemotecnico" end, 
					"IdCliente", "IdPortafolio",case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,"IdCuentaCustodioPortafolio";
	 
	 end; --custodia

	--------------------------
	--Simultaneas de compra
	 begin 
		v_tblSimulC =
			select  "suby_IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento", "suby_IdTipoInstrumento" as "IdTipoInstrumento", "suby_IdInstrumento" as "IdInstrumento", 
				case when "suby_IdClasificacionTipoInstrumento" = 'IF' then "suby_NemoSVS" else "suby_Nemotecnico" end as "Nemotecnico", 
				"IdCliente", "IdPortafolio",
				case when p_AgrupaPorBoveda = 'S' then "suby_IdBoveda" else '' end as "IdBoveda", 
				IFNULL(SUM("INGCantidad"),0) as "Cantidad",
				IFNULL(SUM("INGMontoATasaMercadoActual"),0) as "MontoValorizado",
				"IdCuentaCustodioPortafolio"
			from "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea"
			where "IdEmpresa" = p_IdEmpresa AND
				  "IdTipoOperacion" = 'COMSIM' AND 
				  ----******* por ahora usa "FechaLiquidación", posteriormente cambiar por "INGFechaLiquidacion" cuando se implemente el nuevo modelo
				  v_FechaConsulta BETWEEN "INGFechaOperacion" and case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end and
				  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
				  v_FechaConsulta < case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end and
				  ---*************************
				  "IdEstado" <> 'ANU' 
			group by "suby_IdClasificacionTipoInstrumento", "suby_IdTipoInstrumento", "suby_IdInstrumento",  "IdCliente", "IdPortafolio",
			case when "suby_IdClasificacionTipoInstrumento" = 'IF' then "suby_NemoSVS" else "suby_Nemotecnico" end, 
			case when p_AgrupaPorBoveda = 'S' then "suby_IdBoveda" else '' END,"IdCuentaCustodioPortafolio";
	 
	 end; --Simultaneas de compra

	--------------------------
	--Simultaneas de venta
	 begin 
		v_tblSimulV =
			select  "suby_IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento", "suby_IdTipoInstrumento" as"IdTipoInstrumento", "suby_IdInstrumento" as "IdInstrumento", 
				case when "suby_IdClasificacionTipoInstrumento" = 'IF' then "suby_NemoSVS" else "suby_Nemotecnico" end as "Nemotecnico", 
				"IdCliente", "IdPortafolio",
				case when p_AgrupaPorBoveda = 'S' then "suby_IdBoveda" else '' end as "IdBoveda", 
				IFNULL(SUM("INGCantidad"),0) as "Cantidad",
				IFNULL(SUM("INGMontoATasaMercadoActual"),0) as "MontoValorizado",
				"IdCuentaCustodioPortafolio"
			from "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea"
			where "IdEmpresa" = p_IdEmpresa AND
				  "IdTipoOperacion" = 'VENSIM' AND
				  ----******* por ahora usa "FechaLiquidación", posteriormente cambiar por "INGFechaLiquidacion" cuando se implemente el nuevo modelo
				  v_FechaConsulta BETWEEN "INGFechaOperacion" and case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end and
				  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
				  v_FechaConsulta < case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end and
				  ---*************************
				  "IdEstado" <> 'ANU'
			group by "suby_IdClasificacionTipoInstrumento", "suby_IdTipoInstrumento", "suby_IdInstrumento", 
					case when "suby_IdClasificacionTipoInstrumento" = 'IF' then "suby_NemoSVS" else "suby_Nemotecnico" end, 
					"IdCliente", "IdPortafolio",case when p_AgrupaPorBoveda = 'S' then "suby_IdBoveda" else '' END,"IdCuentaCustodioPortafolio";
	 
	 end; --Simultaneas de venta

	-----------------
	--Prestamos
	 begin 
	 	v_tblPrestamos = 
			select  "IdClasificacionTipoInstrumentoSubyacente" as "IdClasificacionTipoInstrumento"
				, "IdTipoInstrumentoSubyacente" as "IdTipoInstrumento"
				, "IdInstrumentoSubyacente" as "IdInstrumento"
				, case when "IdClasificacionTipoInstrumentoSubyacente" = 'IF' then "NemoSVSSubyacente" else "NemoSubyacente" end as "Nemotecnico"
				,"IdCliente", "IdPortafolio"
				, case when p_AgrupaPorBoveda = 'S' then "IdBovedaSubyacente" else '' end as "IdBoveda"
				, IFNULL(SUM("INGCantidad"),0) as "Cantidad"
				, IFNULL(SUM("INGMontoMercadoInstrumento"),0) as "MontoValorizado"
				, "IdCuentaCustodioPortafolio"
			from "BSCL.Custodia.BD.Vistas::PRT.ConsultarCarteraPrestamo"
			where "IdEmpresa" = p_IdEmpresa AND
				  ----******* por ahora usa "FechaLiquidación", posteriormente cambiar por "INGFechaLiquidacion" cuando se implemente el nuevo modelo
				  v_FechaConsulta BETWEEN "INGFechaOperacion" and case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end and
				  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
				  v_FechaConsulta < case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end
				  ---*************************
			group by "IdClasificacionTipoInstrumentoSubyacente", "IdTipoInstrumentoSubyacente", "IdInstrumentoSubyacente", 
					case when "IdClasificacionTipoInstrumentoSubyacente" = 'IF' then "NemoSVSSubyacente" else "NemoSubyacente" end, 
					"IdCliente", "IdPortafolio",case when p_AgrupaPorBoveda = 'S' then "IdBovedaSubyacente" else '' END,"IdCuentaCustodioPortafolio";
	 
	 end; --Prestamos

	----------------
	--Emprestitos
	 begin 
		v_tblEmprestitos =
			select  "IdClasificacionTipoInstrumentoSubyacente" as "IdClasificacionTipoInstrumento"
				, "IdTipoInstrumentoSubyacente" as "IdTipoInstrumento"
				, "IdInstrumentoSubyacente" as "IdInstrumento"
				, case when "IdClasificacionTipoInstrumentoSubyacente" = 'IF' then "NemoSVSSubyacente" else "NemoSubyacente" end as "Nemotecnico"
				,"IdCliente", "IdPortafolio"
				, case when p_AgrupaPorBoveda = 'S' then "IdBovedaSubyacente" else '' end as "IdBoveda"
				, IFNULL(SUM("INGCantidad"),0) as "Cantidad"
				, IFNULL(SUM("INGMontoMercadoInstrumento"),0) as "MontoValorizado"
				, "IdCuentaCustodioPortafolio"
			from "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenEmprestitoHistorico"
			where "IdEmpresa" = p_IdEmpresa AND
				  ----******* por ahora usa "FechaLiquidación", posteriormente cambiar por "INGFechaLiquidacion" cuando se implemente el nuevo modelo
				  v_FechaConsulta BETWEEN "INGFechaOperacion" and case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end and
				  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
				  v_FechaConsulta < case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end
				  ---*************************
			group by "IdClasificacionTipoInstrumentoSubyacente", "IdTipoInstrumentoSubyacente", "IdInstrumentoSubyacente", "IdCliente", "IdPortafolio", 
				case when "IdClasificacionTipoInstrumentoSubyacente" = 'IF' then "NemoSVSSubyacente" else "NemoSubyacente" end, 
				case when p_AgrupaPorBoveda = 'S' then "IdBovedaSubyacente" else '' END,"IdCuentaCustodioPortafolio";
	 
	 end; --Emprestitos
	
	 
	----------------
	--Movimiento en transito, ingreso
	 begin 
		v_tblTrasnIng =
			select  "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", 
				case when "IdClasificacionTipoInstrumento" = 'IF' then "NemoSVS" else "Nemotecnico" end as "Nemotecnico", 
				"IdCliente", "IdPortafolio",
				case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda",
				IFNULL(SUM("Cantidad"),0) as "Cantidad",
				0 as "MontoValorizado",
				"IdCuentaCustodioPortafolio"
			from "BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraEnTransito"
			where "IdEmpresa" = p_IdEmpresa AND
				  "IdEstado" = 'PEN' AND 
				  "Accion" = 'INGRESO' 
			group by "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", 
				case when "IdClasificacionTipoInstrumento" = 'IF' then "NemoSVS" else "Nemotecnico" end, 
				"IdCliente", "IdPortafolio",case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,"IdCuentaCustodioPortafolio";
	 
	 end; --Movimiento en transito, ingreso

	----------------
	--Movimiento en transito, egreso
	 begin 
		v_tblTrasnEgr =
			select  "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", 
				case when "IdClasificacionTipoInstrumento" = 'IF' then "NemoSVS" else "Nemotecnico" end as "Nemotecnico", 
				"IdCliente", "IdPortafolio",
				case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda",
				IFNULL(SUM("Cantidad"),0) * -1 as "Cantidad",
				0 as "MontoValorizado",
				"IdCuentaCustodioPortafolio"
			from "BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraEnTransito"
			where "IdEmpresa" = p_IdEmpresa AND
				  "IdEstado" = 'PEN' AND 
				  "Accion" = 'RETIRO'
			group by "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", 
					case when "IdClasificacionTipoInstrumento" = 'IF' then "NemoSVS" else "Nemotecnico" end, 
					"IdCliente", "IdPortafolio",case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,"IdCuentaCustodioPortafolio";
	 
	 end; --Movimiento en transito, ingreso
	
	----------------
	--Garantias
	 begin 
		v_tblGarantias =
			select  "IdClasificacionTipoInstrumentoAval" as "IdClasificacionTipoInstrumento", 
				"IdTipoInstrumentoAval" as "IdTipoInstrumento", 
				"IdInstrumento", 
				case when "IdClasificacionTipoInstrumento" = 'IF' then "NemoSVS" else "Nemotecnico" end as "Nemotecnico", 
				"IdCliente", "IdPortafolio",
				case when p_AgrupaPorBoveda = 'S' then "IdBovedaOperacionOrigen" else '' end as "IdBoveda",
				IFNULL(SUM("INGCantidad"),0) as "Cantidad",
				IFNULL(SUM("INGMontoATasaMercadoActual"),0) as "MontoValorizado",
				"IdCuentaCustodioPortafolio"
			from "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantias"
			where "IdEmpresa" = p_IdEmpresa AND
				  "IdEstado" <> 'ANU' AND 
				  ----******* por ahora usa "FechaLiquidación", posteriormente cambiar por "INGFechaLiquidacion" cuando se implemente el nuevo modelo
				  v_FechaConsulta BETWEEN "INGFechaOperacion" and case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end and
				  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
				  v_FechaConsulta < case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end 
				  ---*************************
			group by "IdClasificacionTipoInstrumentoAval", "IdTipoInstrumentoAval", "IdInstrumento", 
					case when "IdClasificacionTipoInstrumento" = 'IF' then "NemoSVS" else "Nemotecnico" end, 
					"IdCliente", "IdPortafolio", case when p_AgrupaPorBoveda = 'S' then "IdBovedaOperacionOrigen" else '' END,"IdCuentaCustodioPortafolio";
	 
	 end; --garantias
	
	v_tblRelacionados = SELECT CLI."IdPersona" AS "IdPersonaDestino" 
			FROM "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
			LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" IND ON IND."IdEntidad" = CLI."IdCliente"
			LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TPO ON IND."IdTipoIndicador" = TPO."IdTipoIndicador"
			WHERE TPO."IdTipoEntidad" = 'CLIENTE' AND TPO."CodigoTipoIndicador"  = 'RELCORR' AND IND."ValorIndicador" = 'S';

	p_out_tbl = 
		SELECT 	 :v_FechaConsulta as "Fecha"
				,ins."IdClasificacionTipoInstrumento"
				,ins."IdTipoInstrumento"
				,ins."IdInstrumento"
				,ins."Nemotecnico"
				,case 
					when cli."DocumentoIdentidad" = v_DoctoIdentidadCpro then 'CPRO' 
					when ifnull(REL."IdPersonaDestino",0) > 0 then 'REL'
					else 'TER' 
				 end as "Relacion"
				,COALESCE(ins."IdBoveda", '') AS "IdBoveda"
				,COALESCE(CUE."Codigo",'') AS "CuentaBoveda"
				, sum(ifnull(cus."Cantidad",0)) as "CantidadCustodia"
				--, sum(0) as "CantidadSimulCom"
				--, sum(0) as "CantidadSimulVen"
				, sum(ifnull(simc."Cantidad",0)) as "CantidadSimulCom"
				, sum(ifnull(simv."Cantidad",0)) as "CantidadSimulVen"
				, sum(ifnull(prt."Cantidad",0)) as "CantidadPrestamo"
				, sum(ifnull(emp."Cantidad",0)) as "CantidadEmpresti"
				, sum(ifnull(gar."Cantidad",0)) as "CantidadGarantia"
				--, sum(0) as "CantidadTransIng"
				--, sum(0) as "CantidadTransEgr"
				, sum(ifnull(mti."Cantidad",0)) as "CantidadTransIng"
				, sum(ifnull(mte."Cantidad",0)) as "CantidadTransEgr"
				, (sum(ifnull(cus."Cantidad",0))+sum(ifnull( prt."Cantidad",0))+sum(ifnull( emp."Cantidad",0))+sum(ifnull( gar."Cantidad",0))+sum(0)+sum(0)) as "CantidadTotal"
				, (sum(ifnull(cus."MontoValorizado",0))+sum(ifnull( prt."MontoValorizado",0))+sum(ifnull( emp."MontoValorizado",0))+sum(ifnull( gar."MontoValorizado",0))) as "MontoValorizado"
				--, (sum(ifnull(cus."Cantidad",0))+sum(ifnull( simc."Cantidad",0))+sum(ifnull( simv."Cantidad",0))+sum(ifnull( prt."Cantidad",0))+sum(ifnull( emp."Cantidad",0))+sum(ifnull( gar."Cantidad",0))+sum(0)+sum(0)) as "CantidadTotal"
				--, (sum(ifnull(cus."MontoValorizado",0))+sum(ifnull( simc."MontoValorizado",0))+sum(ifnull( simv."MontoValorizado",0))+sum(ifnull( prt."MontoValorizado",0))+sum(ifnull( emp."MontoValorizado",0))+sum(ifnull( gar."MontoValorizado",0))) as "MontoValorizado"
				--, (sum(ifnull(cus."Cantidad",0))+sum(ifnull( simc."Cantidad",0))+sum(ifnull( simv."Cantidad",0))+sum(ifnull( prt."Cantidad",0))+sum(ifnull( emp."Cantidad",0))+sum(ifnull( gar."Cantidad",0))+sum(ifnull( mti."Cantidad",0))+sum(ifnull( mte."Cantidad",0))) as "CantidadTotal"
				--, (sum(ifnull(cus."MontoValorizado",0))+sum(ifnull( simc."MontoValorizado",0))+sum(ifnull( simv."MontoValorizado",0))+sum(ifnull( prt."MontoValorizado",0))+sum(ifnull( emp."MontoValorizado",0))+sum(ifnull( gar."MontoValorizado",0))) as "MontoValorizado"
		FROM :v_tblInst as ins
		inner JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as Port ON Port."IdPortafolio" = ins."IdPortafolio" 
		inner join "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as Cli ON Cli."IdCliente" = Port."IdCliente" 
		
		LEFT JOIN :v_tblRelacionados as REL ON (REL."IdPersonaDestino" = cli."IdPersona")
		LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolio" CUE ON (Port."IdPortafolio" = CUE."IdPortafolio" 
																						 AND ins."IdBoveda" = CUE."IdBoveda")
		left join :v_tblCustodia AS cus    on cus."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
		                                  AND cus."IdTipoInstrumento" = ins."IdTipoInstrumento"
		                                  AND cus."IdInstrumento" = ins."IdInstrumento"
		                                  AND cus."IdCliente" = ins."IdCliente"
		                                  AND cus."IdPortafolio" = ins."IdPortafolio"
		                                  AND cus."IdBoveda" = ins."IdBoveda"
		                                  AND cus."IdCuentaCustodioPortafolio" = CUE."IdCuentaCustodioPortafolio"
		left join :v_tblSimulC as simc     on simc."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
		                                  AND simc."IdTipoInstrumento" = ins."IdTipoInstrumento"
		                                  AND simc."IdInstrumento" = ins."IdInstrumento"
		                                  AND simc."IdBoveda" = ins."IdBoveda"
		                                  AND simc."IdCuentaCustodioPortafolio" = CUE."IdCuentaCustodioPortafolio"
		left join :v_tblSimulV as simv     on simv."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
		                                  AND simv."IdTipoInstrumento" = ins."IdTipoInstrumento"
		                                  AND simv."IdInstrumento" = ins."IdInstrumento"
		                                  AND simv."IdBoveda" = ins."IdBoveda"
		                                  AND simv."IdCuentaCustodioPortafolio" = CUE."IdCuentaCustodioPortafolio"                                  
		left join :v_tblPrestamos as prt   on prt."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
		                                  AND prt."IdTipoInstrumento" = ins."IdTipoInstrumento"
		                                  AND prt."IdInstrumento" = ins."IdInstrumento"
		                                  AND prt."IdBoveda" = ins."IdBoveda"
		                                  AND prt."IdCuentaCustodioPortafolio" = CUE."IdCuentaCustodioPortafolio"
		left join :v_tblEmprestitos as emp on emp."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
		                                  AND emp."IdTipoInstrumento" = ins."IdTipoInstrumento"
		                                  AND emp."IdInstrumento" = ins."IdInstrumento"
		                                  AND emp."IdBoveda" = ins."IdBoveda"
		                                  AND emp."IdCuentaCustodioPortafolio" = CUE."IdCuentaCustodioPortafolio"
		left join :v_tblGarantias as gar   on gar."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
		                                  AND gar."IdTipoInstrumento" = ins."IdTipoInstrumento"
		                                  AND gar."IdInstrumento" = ins."IdInstrumento"
		                                  AND gar."IdBoveda" = ins."IdBoveda"
		                                  AND gar."IdCuentaCustodioPortafolio" = CUE."IdCuentaCustodioPortafolio"
		left join :v_tblTrasnIng as mti    on mti."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
		                                  AND mti."IdTipoInstrumento" = ins."IdTipoInstrumento"
		                                  AND mti."IdInstrumento" = ins."IdInstrumento"
		                                  AND mti."IdCliente" = ins."IdCliente"
		                                  AND mti."IdPortafolio" = ins."IdPortafolio"
		                                  AND mti."IdBoveda" = ins."IdBoveda"
		                                  AND mti."IdCuentaCustodioPortafolio" = CUE."IdCuentaCustodioPortafolio"
		left join :v_tblTrasnEgr as mte    on mte."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
		                                  AND mte."IdTipoInstrumento" = ins."IdTipoInstrumento"
		                                  AND mte."IdInstrumento" = ins."IdInstrumento"
		                                  AND mte."IdBoveda" = ins."IdBoveda"
										  AND mte."IdCuentaCustodioPortafolio" = CUE."IdCuentaCustodioPortafolio"
		
	    group by ins."IdClasificacionTipoInstrumento"
				,ins."IdTipoInstrumento"
				,ins."IdInstrumento"
				,ins."Nemotecnico"
				,case 
					when cli."DocumentoIdentidad" = v_DoctoIdentidadCpro then 'CPRO' 
					when ifnull(REL."IdPersonaDestino",0) > 0 then 'REL'
					else 'TER' 
				 end
				, ins."IdBoveda"
				, CUE."Codigo"
	    order by ins."IdClasificacionTipoInstrumento"
	    		,ins."IdTipoInstrumento"
	    		,ins."Nemotecnico"
	    		,ins."IdBoveda";

		INSERT INTO "BSCL.Custodia.BD::CUS.CuadraturaCustodia"(
			 "IdCuadraturaCustodia" 			
			,"FechaCuadraturaCustodia" 		
			,"IdClasificacionTipoInstrumento"
			,"IdTipoInstrumento"				
			,"IdInstrumento" 				
			,"Nemotecnico"					
			,"TipoCuenta"					
			,"IdBoveda" 						
			,"CuentaBoveda"					
			,"CantidadCustodia"				
			,"CantidadSimultaneaCompra"		
			,"CantidadSimultaneaVenta"		
			,"CantidadPrestamo"				
			,"CantidadEmprestito"			
			,"CantidadGarantia"				
			,"CantidadTransitoIngreso"		
			,"CantidadTransitoEgreso"		
			,"CantidadTotal"					
			,"MontoATasaMercadoActual"		
			,"PosicionBoveda"
			,"PosicionBovedaValorizadaParaAFP"				
			,"PosicionBloqueada"	
			,"PosicionBloqueadaValorizadaParaAFP"			
			,"PosicionReventa"				
			,"PosicionRecompra"				
			,"Diferencia"
			,"RutBeneficiario"				
			,"NombreBeneficiario"			
			,"FechaModificacion"				
			,"IdUsuario" 					
			,"IdEstado"
			,"ExplicacionDescuadratura")
		SELECT 
			 "BSCL.Custodia.BD.Secuencias::CuadraturaCustodia".NEXTVAL as "IdCuadraturaCustodia"
			,"Fecha" 							
			,"IdClasificacionTipoInstrumento"	
			,"IdTipoInstrumento" 				
			,"IdInstrumento" 					
			,"Nemotecnico" 						
			,"Relacion" 						
			,"IdBoveda" 						
			,"CuentaBoveda" 					
			,"CantidadCustodia" 				
			,"CantidadSimulCom" 				
			,"CantidadSimulVen" 				
			,"CantidadPrestamo" 				
			,"CantidadEmpresti" 				
			,"CantidadGarantia" 				
			,"CantidadTransIng" 				
			,"CantidadTransEgr" 				
			,"CantidadTotal" 					
			,"MontoValorizado"
			,0										as "PosicionBoveda"
			,0										as "PosicionBovedaValorizadaParaAFP"
			,0										as "PosicionBloqueada"
			,0										as "PosicionBloqueadaValorizadaParaAFP"
			,0										as "PosicionReventa"
			,0										as "PosicionRecompra"
			,("CantidadTotal" * -1)					as "Diferencia"
			,''										as "RutBeneficiario"
			,''										as "NombreBeneficiario"
			,Current_UTCTIMESTAMP					as "FechaModificacion"
			,p_IdUsuario							as "IdUsuario"
			,'VIG'									as "IdEstado"
			,''
		FROM :p_out_tbl WHERE "IdInstrumento" > 0 AND ("CantidadCustodia" <> 0 OR "CantidadSimulCom" <> 0 OR "CantidadSimulVen" <> 0 OR "CantidadPrestamo" <> 0 OR "CantidadEmpresti" <> 0 
	   	OR "CantidadGarantia" <> 0 OR "CantidadTransIng" <> 0 OR "CantidadTransEgr" <> 0 OR "CantidadTotal" <> 0 OR "MontoValorizado" <> 0) AND  "IdClasificacionTipoInstrumento" NOT IN ('DV','RE');
		
		IF (p_out_ErroresContador > 0) then 
			SIGNAL SALIDA_CONTROLADA;
		ELSE
			v_TablaCodigosMensajes =	Select 	22											As "IdInterno", 
												'UI_CUADRATURA_CUSTODIA_CARGADA_CON_EXITO'	As "Codigo",
												'p_FechaConsulta'							As "CodigoDato1", 
												Cast(p_FechaConsulta As NVARCHAR(520))		As "ValorDato1",
												'' 											As "CodigoDato2", 
												'' 											As "ValorDato2" 
											From dummy;
				                        
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_out_hash);
			
			p_out_InformacionContador = 1;
			p_out_InformacionDetalle = 	Select "Codigo", "Descripcion" 
										From :v_out_hash;				
		END IF;
END;