SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Orden.BD.Servicios::CalcularLineaRiesgoOrdenes";
GO
CREATE PROCEDURE
--  Proposito: Calcular linea de riesgo para simultaneas
--  Autor: - TEAM ORDENES - AWW YEAH
--  Fecha Creacion: 

 "BSCL.Orden.BD.Servicios::CalcularLineaRiesgoOrdenes" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN p_IdEmpresa				BIGINT,			--ID Empresa destino
	IN p_IdUsuario				BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo				NVARCHAR(3),  	--ID del Modulo que lo invoca
		
	IN p_tablaOperacion			TABLE(
									"IdInterno"			BIGINT,
									"IdPortafolio"		BIGINT,
									"IdTipoOperacion"	NVARCHAR(6),
									"EsVentaCorta"		NVARCHAR(1),
									"EsODCuentaPropia"	NVARCHAR(1),
									"IdInstrumento" 	BIGINT, 
									"Monto" 			DECIMAL(26,8),
									"Tasa" 				DECIMAL(26,8),
									"PlazoEnDias" 		BIGINT,
									"IdBolsa" 			NVARCHAR(3),
									"IdTipoCanalIngreso" NVARCHAR(3)
									),
	
	OUT p_out_Limites			TABLE("IdInterno" BIGINT,"MontoCarteras" DECIMAL(26,8),"MontoDisponible" DECIMAL(26,8),"MontoSimulacion" DECIMAL(26,8), "EsCuentaPropia" NVARCHAR(1)),
	
    -- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 	int,
	OUT p_out_AdvertenciasDetalle 	table ("Codigo" NVARCHAR(40),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),	
	OUT p_out_ErroresContador 		int,
	OUT p_out_ErroresDetalle 		table ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520))
 
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
BEGIN

	DECLARE v_FechaSistema NVARCHAR(10);
	DECLARE v_IdInstrumentoCOMSIM			BIGINT;
	
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
	SELECT "IdInstrumento" into v_IdInstrumentoCOMSIM FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" WHERE "IdTipoOperacion" = 'COMSIM';
		
	v_TablaOperacionVC = SELECT  ope."IdPortafolio",port."IdCliente",ope."Monto" as "MontoUtilizado",'CLP' as "IdMoneda",0 as "TipoCambio","IdInterno" as "IdOperacion",'PRESTA' as "IdTipoOperacion",
								'PRT' as "IdTipoInstrumento",'RV' as "IdClasificacionTipoInstrumento",COALESCE(ins."IdTipoReajuste",'') as "IdTipoReajuste",ope."IdInstrumento",COALESCE(ins."IdEmisor",'') AS "IdEmisor",ope."Tasa",ope."PlazoEnDias" as "DiasPlazo",
								:v_FechaSistema as "FechaOperacion",	'' as "CodigoOperador",0 as "IdAgente",ope."IdTipoCanalIngreso" as "IdCanalIngreso"
								FROM :p_tablaOperacion ope
								LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS port ON  ope."IdPortafolio" = port."IdPortafolio"
								LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS ins ON ins."IdInstrumento" = ope."IdInstrumento"
								LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS tipoins ON tipoins."IdTipoInstrumento" = ins."IdTipoInstrumento"
								WHERE ope."EsVentaCorta" = 'S';
								
								
	v_OtrasOperaciones = 		SELECT  ope."IdPortafolio",port."IdCliente",ope."Monto" as "MontoUtilizado",'CLP' as "IdMoneda",0 as "TipoCambio","IdInterno" as "IdOperacion",ope."IdTipoOperacion",
								ins."IdTipoInstrumento" as "IdTipoInstrumento",tipoins."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento",COALESCE(ins."IdTipoReajuste",'') as "IdTipoReajuste",ope."IdInstrumento",COALESCE(ins."IdEmisor",'') AS "IdEmisor",ope."Tasa",ope."PlazoEnDias" as "DiasPlazo",
								:v_FechaSistema as "FechaOperacion",'' as "CodigoOperador",0 as "IdAgente",ope."IdTipoCanalIngreso" as "IdCanalIngreso"
								FROM :p_tablaOperacion ope
								LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS port ON  ope."IdPortafolio" = port."IdPortafolio"
								LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS ins ON ins."IdInstrumento" = ope."IdInstrumento"
								LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS tipoins ON tipoins."IdTipoInstrumento" = ins."IdTipoInstrumento"
								WHERE ope."EsVentaCorta" = 'N';					
	
	v_DatosCuentaPropia = SELECT PORT."IdPortafolio",CLI."IdCliente"
					FROM "BSCL.Plataforma.BD::PLA.Empresa" emp
					INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" soc on emp."IdSocioDeNegocio" = soc."IdSocioDeNegocio"
					INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON SOC."IdCliente" = CLI."IdCliente"
					INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" PORT ON PORT."IdCliente" = CLI."IdCliente"
					where emp."IdEmpresa" =:p_IdEmpresa;
	
	v_tablaOperacionOD = SELECT  prop."IdPortafolio" as "IdPortafolio",prop."IdCliente", "Monto" as "MontoUtilizado",'CLP' as "IdMoneda",0 as "TipoCambio", "IdInterno"*-1 as "IdOperacion",'COMSIM' as "IdTipoOperacion",
					ins."IdTipoInstrumento" as "IdTipoInstrumento",'RV' as "IdClasificacionTipoInstrumento",'CLP' as "IdTipoReajuste",:v_IdInstrumentoCOMSIM as "IdInstrumento",'' AS "IdEmisor",ope."Tasa",ope."PlazoEnDias" as "DiasPlazo",
					:v_FechaSistema as "FechaOperacion",	'' as "CodigoOperador",0 as "IdAgente",'AGE' as "IdCanalIngreso"
					FROM :p_tablaOperacion ope
					INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS ins ON ins."IdInstrumento" = :v_IdInstrumentoCOMSIM, :v_DatosCuentaPropia prop 
					WHERE ope."IdTipoOperacion" = 'VENSIM' and ope."EsVentaCorta" = 'N' and ope."EsODCuentaPropia" = 'S';
	
	select 'v_tablaOperacionOD',* from :v_tablaOperacionOD;
	
	v_tablaOperacion2 = select * from :v_TablaOperacionVC union all select * from :v_OtrasOperaciones union all select * from :v_tablaOperacionOD;						
	
	/*IF (p_EsVentaCorta = 'S') THEN
		v_tablaOperacion2 	= SELECT  ope."IdPortafolio",port."IdCliente",ope."Monto" as "MontoUtilizado",'CLP' as "IdMoneda",0 as "TipoCambio","IdInterno" as "IdOperacion",'PRESTA' as "IdTipoOperacion",
								'PRT' as "IdTipoInstrumento",'RV' as "IdClasificacionTipoInstrumento",COALESCE(ins."IdTipoReajuste",'') as "IdTipoReajuste",ope."IdInstrumento",COALESCE(ins."IdEmisor",'') AS "IdEmisor",ope."Tasa",ope."PlazoEnDias" as "DiasPlazo",
								:v_FechaSistema as "FechaOperacion",	'' as "CodigoOperador",0 as "IdAgente",ope."IdTipoCanalIngreso" as "IdCanalIngreso"
								FROM :p_tablaOperacion ope
								LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS port ON  ope."IdPortafolio" = port."IdPortafolio"
								LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS ins ON ins."IdInstrumento" = ope."IdInstrumento"
								LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS tipoins ON tipoins."IdTipoInstrumento" = ins."IdTipoInstrumento";

	ELSE							
		select count(1) into v_count from :p_tablaOperacion;
		if (v_count = 0 ) THEN
			v_tablaOperacion2 	= SELECT  ope."IdPortafolio",port."IdCliente",0 as "MontoUtilizado",'CLP' as "IdMoneda",0 as "TipoCambio",0 as "IdOperacion",:p_IdTipoOperacion as "IdTipoOperacion",
								ins."IdTipoInstrumento" as "IdTipoInstrumento",tipoins."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento",'' as "IdTipoReajuste",0 as "IdInstrumento",'' as "IdEmisor",0 as "Tasa",0 as "DiasPlazo",
								:v_FechaSistema as "FechaOperacion",'' as "CodigoOperador",0 as "IdAgente",ope."IdTipoCanalIngreso" as "IdCanalIngreso"
								FROM :p_tablaOperacion ope
								LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS port ON  ope."IdPortafolio" = port."IdPortafolio"
								LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS ins ON ins."IdInstrumento" = ope."IdInstrumento"
								LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS tipoins ON tipoins."IdTipoInstrumento" = ins."IdTipoInstrumento"							
								;
		ELSE
			v_tablaOperacion2 	= SELECT  ope."IdPortafolio",port."IdCliente",ope."Monto" as "MontoUtilizado",'CLP' as "IdMoneda",0 as "TipoCambio","IdInterno" as "IdOperacion",:p_IdTipoOperacion as "IdTipoOperacion",
								ins."IdTipoInstrumento" as "IdTipoInstrumento",tipoins."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento",COALESCE(ins."IdTipoReajuste",'') as "IdTipoReajuste",ope."IdInstrumento",COALESCE(ins."IdEmisor",'') AS "IdEmisor",ope."Tasa",ope."PlazoEnDias" as "DiasPlazo",
								:v_FechaSistema as "FechaOperacion",'' as "CodigoOperador",0 as "IdAgente",ope."IdTipoCanalIngreso" as "IdCanalIngreso"
								FROM :p_tablaOperacion ope
								LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS port ON  ope."IdPortafolio" = port."IdPortafolio"
								LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS ins ON ins."IdInstrumento" = ope."IdInstrumento"
								LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS tipoins ON tipoins."IdTipoInstrumento" = ins."IdTipoInstrumento"
								;
	
			SELECT SUM("MontoUtilizado") INTO V_MONTO_TOTAL FROM :v_tablaOperacion2;
			SELECT MAX("IdOperacion") + 1 into V_IdOperacion from :v_tablaOperacion2;
			SELECT MAX("DiasPlazo"), MAX("Tasa") into v_plazo, v_tasa from :v_tablaOperacion2;
			SELECT MAX("IdBolsa") into v_IdBolsa from :p_tablaOperacion;
	
			select "IdInstrumento" into v_IdInstrumentoCOMSIM from "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoInstrumento" where "IdTipoOperacion" = 'COMSIM' and "IdBolsa"=  :v_IdBolsa;
	
			if (p_EsODCuentaPropia = 'S' AND p_IdTipoOperacion = 'VENSIM') THEN
				v_tablaOperacionOD = SELECT  PORT."IdPortafolio" as "IdPortafolio",CLI."IdCliente",:V_MONTO_TOTAL as "MontoUtilizado",'CLP' as "IdMoneda",0 as "TipoCambio", :V_IdOperacion as "IdOperacion",'COMSIM' as "IdTipoOperacion",
					'SIM' as "IdTipoInstrumento",'RV' as "IdClasificacionTipoInstrumento",'CLP' as "IdTipoReajuste",:v_IdInstrumentoCOMSIM as "IdInstrumento",'' AS "IdEmisor",v_tasa as "Tasa",v_plazo as "DiasPlazo",
					:v_FechaSistema as "FechaOperacion",	'' as "CodigoOperador",0 as "IdAgente",'AGE' as "IdCanalIngreso"
					FROM "BSCL.Plataforma.BD::PLA.Empresa" emp
					LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" soc on emp."IdSocioDeNegocio" = soc."IdSocioDeNegocio"
					LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON SOC."IdCliente" = CLI."IdCliente"
					LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" PORT ON PORT."IdCliente" = CLI."IdCliente"
					where emp."IdEmpresa" =:p_IdEmpresa
					;
	
				v_tablaOperacion2 = select * from :v_tablaOperacion2 UNION ALL select * from :v_tablaOperacionOD;
			
			END IF;
			
		END IF;
	
		
	END IF;*/
	
	select 'v_tablaOperacion2',* from :v_tablaOperacion2;
	
	CALL "BSCL.Plataforma.BD.Servicios::LIM.EvaluacionLimite" (
		p_IdEmpresa 					=> :p_IdEmpresa,
		p_IdUsuario 					=> :p_IdUsuario,
		p_IdModulo 						=> :p_IdModulo,
		p_UsaReglasNegocio				=> 'N',
		p_IdTipoEntidad					=> 'ORDEN',
		p_GeneraAutorizacion			=> 'N',
		p_simularOperacion				=> 'S',
		p_tablaOperacion 				=> :v_tablaOperacion2,
		p_out_RegistroEvaluacionLimite 	=> v_out_RegistroEvaluacionLimite,
		p_out_idAuthtable 				=> v_out_idAuthtable,
		p_out_EstadoOperacion			=> v_out_EstadoOperacion,
		p_out_AdvertenciasContador		=> p_out_AdvertenciasContador,
		p_out_AdvertenciasDetalle 		=> p_out_AdvertenciasDetalle,
		p_out_ErroresContador 			=> p_out_ErroresContador,
		p_out_ErroresDetalle 			=> p_out_ErroresDetalle
	);
	
	select 'v_out_RegistroEvaluacionLimite',* from :v_out_RegistroEvaluacionLimite;
	
	
	p_out_Limites = 
		SELECT "IdOperacion" as "IdInterno" ,"MontoCalculado" - "MontoUtilizado" as "MontoCarteras","MontoLimite" - "MontoCalculado" as "MontoDisponible",
				"MontoUtilizado" as "MontoSimulacion"
				, CASE WHEN "IdOperacion" < 0 THEN 'S' ELSE 'N' END as "EsCuentaPropia" 
		from :v_out_RegistroEvaluacionLimite where "IdTipoLimite" = 'LIMPORTPRODUCTO';
		
END;
GO
