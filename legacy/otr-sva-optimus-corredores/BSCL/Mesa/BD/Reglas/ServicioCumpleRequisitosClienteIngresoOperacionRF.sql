SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Mesa.BD.Reglas::ServicioCumpleRequisitosClienteIngresoOperacionRF";

CREATE PROCEDURE "BSCL.Mesa.BD.Reglas::ServicioCumpleRequisitosClienteIngresoOperacionRF"(
	-- Parametros entrada comunes
	IN p_IdEmpresa			BIGINT,			--ID Empresa destino
	IN p_IdUsuario			BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo			NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN p_UsaReglasNegocio	NVARCHAR(1),	--S/N se usa en caso de servicios anidados.		  
-- Parametros entrada
	--"BSCL.Mesa.BD::PAR.ServicioCumpleRequisitosClienteIngresoOperacionRF_IN"
-- Parametros Salida
	--"BSCL.Mesa.BD::PAR.ServicioCumpleRequisitosClienteIngresoOperacionRF_Informacion_OUT"
-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador	INT,
	--"BSCL.Mesa.BD::PAR.ServicioCumpleRequisitosClienteIngresoOperacionRF_Advertencias_OUT"
	OUT p_out_ErroresContador		INT
	--"BSCL.Mesa.BD::PAR.ServicioCumpleRequisitosClienteIngresoOperacionRF_Errores_OUT"
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

	DECLARE v_resultado TABLE ("IdInterno" BIGINT,"IdResultado" BIGINT,"Codigo" NVARCHAR(100),"NombreParametro1" NVARCHAR(100),"ValorParametro1" NVARCHAR(621),"NombreParametro2" NVARCHAR(100),"ValorParametro2" NVARCHAR(621))
					DEFAULT SELECT 0 as "IdInterno",0 as "IdResultado",'' as  "Codigo",'' as "NombreParametro1",'' as "ValorParametro1",'' as "NombreParametro2",'' as "ValorParametro2" FROM dummy WHERE 1=2;
	DECLARE v_Out_FlujoCaja	TABLE("NombreUso"					NVARCHAR(520)
								  ,"FechaSaldo"					NVARCHAR(10)
								  ,"IdPortafolio"				BIGINT
                                  ,"IdMoneda"					NVARCHAR(3)
                                  ,"Saldo"						DECIMAL(26,8))
                    DEFAULT SELECT '' as "NombreUso",'' as "FechaSaldo",0 as "IdPortafolio",'' as "IdMoneda",0 as "Saldo" FROM DUMMY WHERE 1=2;
    DECLARE v_Out_FlujoCajaDetallado	TABLE(	"FechaSaldo"				NVARCHAR(10)
												,"IdPortafolio"				BIGINT
									            ,"IdMoneda"					NVARCHAR(3)
									            ,"IdUsoFlujoCaja"			BIGINT
									            ,"NombreUso"				NVARCHAR(520)
									            ,"IdConfiguracionFlujoCaja"	BIGINT
									            ,"ItemCalculo"				NVARCHAR(100)
									            --,"FuncionOperador"			NVARCHAR(1)
									            ,"Monto"					DECIMAL(26,8))
					DEFAULT SELECT '' as "FechaSaldo",0 as "IdPortafolio",'' as "IdMoneda",0 as "IdUsoFlujoCaja",'' as "NombreUso",0 as "IdConfiguracionFlujoCaja",'' as "ItemCalculo",0 as "Monto" FROM DUMMY WHERE 1=2;

	DECLARE v_out_ErroresContadorFlujoCaja           INT;
	DECLARE v_out_ErroresDetalleFlujoCaja            TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000))
					DEFAULT SELECT '' as "Codigo",'' as "Descripcion" FROM DUMMY WHERE 1=2;
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		
		--consulta diccionario 
		v_TablaCodigosMensajes= SELECT 
									 "IdInterno"
									,"Codigo"
									,"NombreParametro1" as "CodigoDato1"
									,"ValorParametro1" as "ValorDato1"
									,"NombreParametro2" as "CodigoDato2"
									,"ValorParametro2" as "ValorDato2"
								FROM :v_resultado;
								
		--select 'v_TablaCodigosMensajes' as "v_TablaCodigosMensajes", * from :v_TablaCodigosMensajes;
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,v_out_Diccionario);	
		
		INSERT INTO "BSCL.Mesa.BD::PAR.ServicioCumpleRequisitosClienteIngresoOperacionRF_Errores_OUT"
		SELECT dic."IdInterno",dic."Codigo",dic."Descripcion" 
		FROM :v_out_Diccionario dic INNER JOIN :v_resultado err ON (err."IdInterno" = dic."IdInterno" and err."Codigo" = dic."Codigo" and err."IdResultado" = 4);
		
		INSERT INTO "BSCL.Mesa.BD::PAR.ServicioCumpleRequisitosClienteIngresoOperacionRF_Advertencias_OUT"
		SELECT dic."IdInterno",dic."Codigo",'' as "RequiereAutorizacion",dic."Descripcion" 
		FROM :v_out_Diccionario dic INNER JOIN :v_resultado err ON (err."IdInterno" = dic."IdInterno" and err."Codigo" = dic."Codigo" and err."IdResultado" = 2);
		
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from "BSCL.Mesa.BD::PAR.ServicioCumpleRequisitosClienteIngresoOperacionRF_Errores_OUT";
		select count(1) into p_out_AdvertenciasContador 
		from "BSCL.Mesa.BD::PAR.ServicioCumpleRequisitosClienteIngresoOperacionRF_Advertencias_OUT";
		
	END;

	BEGIN
		--------------------
		--Declare Variables
		--------------------
		DECLARE v_FechaDesde, v_FechaHasta,v_FechaSistema NVARCHAR(10) := '';
		p_out_AdvertenciasContador	:= 0;
		p_out_ErroresContador		:= 0;
		
		--------------------
		--Reglas Negocio
		--------------------
		
		IF p_UsaReglasNegocio = 'S' THEN
		--select 'BSCL.Mesa.BD.Vistas::MRF.ConsultarRequisitosOperativosMesaRF' as "BSCL.Mesa.BD.Vistas::MRF.ConsultarRequisitosOperativosMesaRF",* from "BSCL.Mesa.BD.Vistas::MRF.ConsultarRequisitosOperativosMesaRF";
		--fecha de sistema
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
		--Valido Flujo de caja
			v_PortafoliosPreferencia = 	SELECT DISTINCT 
												par."IdTipoOperacion" 						as "PAR_IdTipoOperacion"
												,par."IdMoneda"								as "PAR_IdMoneda"
												,par."IdCondicionDeLiquidacion"				as "PAR_IdCondicionDeLiquidacion"
												,par."FechaOperacion"						as "PAR_FechaOperacion"
												,par."FechaLiquidacion"						as "PAR_FechaLiquidacion"
												,par."MontoOperacion"						as "PAR_MontoOperacion"
												,cli."DocumentoIdentidad"					as "CLI_DocumentoIdentidad"
												,cli."IdEstado" 							as "CLI_IdEstado"
												,cli."IdEstadoPersona"						as "CLI_IdEstadoPersona"
												,cli."NombreCompleto"						as "CLI_NombreCompleto"
												,por."IdPortafolio"							as "POR_IdPortafolio"
												,por."CodigoPortafolio"						as "POR_CodigoPortafolio"
												,por."DescripcionPortafolio"				as "POR_DescripcionPortafolio"
												,por."Proposito"							as "POR_Proposito"
												,por."IdCliente"							as "POR_IdCliente"
												,por."IdEstado"								as "POR_IdEstado"
												,por."Estado"								as "POR_Estado"
												,por."FechaBloqueo"							as "POR_FechaBloqueo"
												,por."ComentarioBloqueo"					as "POR_ComentarioBloqueo"
												,por."IdTipoBloqueo"						as "POR_IdTipoBloqueo"
												,por."IdPerfilComercialBaseOrigen"			as "POR_IdPerfilComercialBaseOrigen"
												,por."IdPerfilProductos"					as "POR_IdPerfilProductos"
												--,por."IdPersonaBrokerExtranjero"			as "POR_IdPersonaBrokerExtranjero"
												,por."IdPerfilProductosExtra"				as "POR_IdPerfilProductosExtra"
												,por."IdPerfilCobros"						as "POR_IdPerfilCobros"
												,por."IdPerfilCobrosExtra"					as "POR_IdPerfilCobrosExtra"
												--,por."IdPerfilCobrosPeru"					as "POR_IdPerfilCobrosPeru"
												--,por."IdPerfilCobrosPeruExtra"				as "POR_IdPerfilCobrosPeruExtra"
												,por."IdPerfilLimites"						as "POR_IdPerfilLimites"
												,por."IdPerfilLimitesExtra"					as "POR_IdPerfilLimitesExtra"
												,por."IdPerfilCondicionesOperativas"		as "POR_IdPerfilCondicionesOperativas"
												,por."IdPerfilCondicionesOperativasExtra"	as "POR_IdPerfilCondicionesOperativasExtra"
												,por."IdEstadoAutorizacion"					as "POR_IdEstadoAutorizacion"
												,por."FechaIngreso"							as "POR_FechaIngreso"
												,por."FechaCreacion"						as "POR_FechaCreacion"
												,por."FechaModificacion"					as "POR_FechaModificacion"
												,por."IdDireccionEnvioCorrespondencia"		as "POR_IdDireccionEnvioCorrespondencia"
												,por."IdTelefonoContacto"					as "POR_IdTelefonoContacto"
												,por."IdCarga"								as "POR_IdCarga"
												,por."IdEmpleadoEjecutivo"					as "POR_IdEmpleadoEjecutivo"
												,por."EsEjecutivoExclusivo"					as "POR_EsEjecutivoExclusivo"
												,por."CodigoPropositoPortafolio"			as "POR_CodigoPropositoPortafolio"
												,por."NumeroFicha"							as "POR_NumeroFicha"
												,por."IdNombreFantasia"						as "POR_IdNombreFantasia"
												,por."Alias"								as "POR_Alias"
												,por."EsCuentaPropia"						as "POR_EsCuentaPropia"
												,por."IdEmpresaCuentaPropia"				as "POR_IdEmpresaCuentaPropia"
												,con."OperaCustodia"						as "PRE_OperaCustodia"
												,con."SectorEconomico"						as "PRE_SectorEconomico"
												,con."CodigoOTC"							as "PRE_CodigoOTC"
												,con."DataTec"								as "PRE_DataTec"
												,con."OperaCuentaInversion"					as "PRE_OperaCuentaInversion"
												,con."SobregiroCuentaInversion"				as "PRE_SobregiroCuentaInversion"
												,indCli."GrupoIndicador"					as "IND_GrupoIndicador"
												,indCli."NombreTipoIndicador"				as "IND_NombreTipoIndicador"
												,indCli."ValorIndicador"					as "IND_ValorIndicador"	
												,reqOp."IdTipoEtapa"						as "REQ_IdTipoEtapa"
												,reqOp."IdCondicionOperativa"				as "REQ_IdCondicionOperativa"
												,reqOp."CodigoTipoIndicador"				as "REQ_CodigoTipoIndicador"
												,reqOp."IdTipoComportamiento"				as "REQ_IdTipoComportamiento"
										FROM "BSCL.Mesa.BD::PAR.ServicioCumpleRequisitosClienteIngresoOperacionRF_IN" par 
											CROSS JOIN "BSCL.Mesa.BD.Vistas::MRF.ConsultarRequisitosOperativosMesaRF" reqOp
											LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" por ON (por."IdPortafolio" = par."IdPortafolio")
											LEFT JOIN "BSCL.Mesa.BD.Vistas::MRF.ConsultarPreferenciaPortafolioMesaRF" con ON (con."IdPortafolio" = par."IdPortafolio")
											LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli ON (cli."IdCliente" = por."IdCliente")
											LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" indCli ON (indCli."IdCliente" = por."IdCliente" and indCli."CodigoTipoIndicador" = reqOp."CodigoTipoIndicador" AND indCli."IdTipoEntidad" = 'CLIENTE' and indCli."IdUso" = 'REQ')
			 							WHERE reqOp."IdTipoEtapa" = 'INGRESOOPMESARF' --and reqOp."IdCondicionOperativa" in ('CONTCONDGRAPACTO','PORTBLOQ','PERSONABLOQUEADA','CLIENTEBLOQUEADO','SALDOFLUJOCAJA')
			 							ORDER BY por."IdPortafolio";
			
			--SELECT 'v_PortafoliosPreferencia' as "v_PortafoliosPreferencia",* FROM :v_PortafoliosPreferencia;

			-- Clientes que operan con cuenta inversion
			v_PortafolioCtaInv = SELECT "POR_IdPortafolio" as "IdPortafolio","PAR_IdMoneda" as "IdMoneda","PAR_FechaLiquidacion" as "FechaLiquidacion" 
								 FROM :v_PortafoliosPreferencia 
								 WHERE "PRE_OperaCuentaInversion" = 'S' 
								 AND "PAR_FechaLiquidacion" != ''
								 AND "REQ_IdCondicionOperativa" = 'SALDOFLUJOCAJA';
			--select 'v_PortafolioCtaInv' as "v_PortafolioCtaInv",* from :v_PortafolioCtaInv;
							 							
			IF (NOT IS_EMPTY(:v_PortafolioCtaInv)) THEN
								 
				select min("FechaLiquidacion") as "FechaDesde",max("FechaLiquidacion") as "FechaHasta" 
				into v_FechaDesde, v_FechaHasta
				from :v_PortafolioCtaInv;
				select v_FechaDesde as "v_FechaDesde", v_FechaHasta as "v_FechaHasta" from dummy;
			
				--Consultar FLujo Caja para la moneda ingresada para los portafolios que operan con cuenta inversion
				call "BSCL.Plataforma.BD.Servicios::SaldoFlujoCaja"(
										    p_IdEmpresa 			=> p_IdEmpresa
											,p_FechaDesde 			=> v_FechaDesde
										    ,p_FechaHasta 			=> v_FechaHasta
										    ,p_Uso 					=> 'MESA_RF'
											,p_DesplegarEnUI 		=> 'N'
											,p_AgruparPorOrigen 	=> 'N'
										    ,p_PortafolioAConsultar	=> :v_PortafolioCtaInv
											-- Parametros salida
										    ,p_out_FlujoCaja			=> :v_Out_FlujoCaja 
											,p_out_FlujoCajaDetallado=> :v_Out_FlujoCajaDetallado
											-- Salida errores
											,p_out_ErroresContador	=> v_out_ErroresContadorFlujoCaja
											,p_out_ErroresDetalle	=> v_out_ErroresDetalleFlujoCaja);
									
				--select 'v_Out_FlujoCaja' as "v_Out_FlujoCaja",* from :v_Out_FlujoCaja;
				--select 'v_Out_FlujoCajaDetallado' as "v_Out_FlujoCajaDetallado",* from :v_Out_FlujoCajaDetallado;
			END IF; -- IF (NOT IS_EMPTY(:v_PortafolioCtaInv))			

				
			v_PortafolioMRF = 
							SELECT pr.*, 0 as "FCA_Saldo", COALESCE("IdTipoProducto",'MERF') as "PRO_IdTipoProducto", COALESCE(det."IdTipoOperacion",'1') AS "PRO_IdTipoOperacion"
								 FROM :v_PortafoliosPreferencia 								pr
								 INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio"  				por	on por."IdPortafolio"			= pr."POR_IdPortafolio"
								 INNER JOIN "BSCL.Plataforma.BD::COM.PerfilProductos" 			pp 	on por."IdPerfilProductosExtra" = pp."IdPerfilProductos"
								 INNER JOIN "BSCL.Plataforma.BD::COM.DetallePerfilProductos"	det on det."IdPerfilProductos" 		= pp."IdPerfilProductos" 
								 WHERE 
								 	 "PAR_FechaLiquidacion" 	!= ''
								 AND "REQ_IdCondicionOperativa" = 'PRODUCTOINVALIDO' 
								 AND  det."IdEstado" 			= 'ACT' 
								 AND "IdTipoProducto" 			= 'MERF'
							UNION 
							
							SELECT pr.*, 0 as "FCA_Saldo", COALESCE("IdTipoProducto",'MERF') as "PRO_IdTipoProducto", COALESCE(det."IdTipoOperacion",'1') AS "PRO_IdTipoOperacion"
								 FROM :v_PortafoliosPreferencia 								pr
								 INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio"  				por	on por."IdPortafolio"	= pr."POR_IdPortafolio"
								 INNER JOIN "BSCL.Plataforma.BD::COM.PerfilProductos" 			pp 	on por."IdPerfilProductos" = pp."IdPerfilProductos"
								 INNER JOIN "BSCL.Plataforma.BD::COM.DetallePerfilProductos"	det on det."IdPerfilProductos" = pp."IdPerfilProductos"-- AND  det."IdEstado" = 'ACT' AND "IdTipoProducto" = 'MERF'
								 WHERE 
								 	 "PAR_FechaLiquidacion" 	!= ''
								 AND "REQ_IdCondicionOperativa" = 'PRODUCTOINVALIDO'
 								 AND  det."IdEstado" 			= 'ACT' 
 								 AND "IdTipoProducto" 			= 'MERF';
								 
			v_PortafolioMRFFinal = 	SELECT pr.*, 0 as "FCA_Saldo", COALESCE(mrf."PRO_IdTipoProducto", 'PROINV') as "PRO_IdTipoProducto", COALESCE(mrf."PRO_IdTipoOperacion",'OPERINV') AS "PRO_IdTipoOperacion" 
									FROM :v_PortafolioMRF mrf
									RIGHT JOIN :v_PortafoliosPreferencia pr ON mrf."POR_IdPortafolio"	= pr."POR_IdPortafolio" AND (mrf."PRO_IdTipoOperacion" = pr."PAR_IdTipoOperacion" OR mrf."PRO_IdTipoOperacion" = '0')
									WHERE pr."REQ_IdCondicionOperativa" = 'PRODUCTOINVALIDO';
								
								
					
										
			v_ClientesVal = SELECT por.*, COALESCE(fCaja."Saldo",0) as "FCA_Saldo", '0' as "PRO_IdTipoProducto", '0' AS "PRO_IdTipoOperacion"
							FROM :v_PortafoliosPreferencia por
							LEFT JOIN :v_Out_FlujoCaja fCaja ON (por."POR_IdPortafolio" = fCaja."IdPortafolio" and por."PAR_IdMoneda" = fCaja."IdMoneda" and por."PAR_FechaLiquidacion" = fCaja."FechaSaldo")
							WHERE  "REQ_IdCondicionOperativa" <> 'PRODUCTOINVALIDO' 
							UNION ALL
							SELECT * FROM :v_PortafolioMRFFinal;
			
			v_resultado = SELECT "POR_IdPortafolio" as "IdInterno"
							,CASE 
								WHEN "REQ_IdCondicionOperativa" = 'CONTCONDGRAPACTO' and ("PAR_IdTipoOperacion" = 'COMTER' or "PAR_IdTipoOperacion"= 'VENTER'  or "PAR_IdTipoOperacion"='') THEN 1
								WHEN "REQ_IdCondicionOperativa" = 'CONTCONDGRAPACTO' and ("PAR_IdTipoOperacion" = 'COMPAC' or "PAR_IdTipoOperacion"= 'VENPAC') and "IND_ValorIndicador" = 'S' THEN 1
								WHEN "REQ_IdCondicionOperativa" = 'PERSONABLOQUEADA' and "CLI_IdEstadoPersona" = 'ACT' THEN 1
								WHEN "REQ_IdCondicionOperativa" = 'CLIENTEBLOQUEADO' and "CLI_IdEstado" = 'ACT' THEN 1
								WHEN "REQ_IdCondicionOperativa" = 'PORTBLOQ' and "POR_IdEstado" = 'ACT' THEN 1
								WHEN "REQ_IdCondicionOperativa" = 'SALDOFLUJOCAJA' and ("PRE_OperaCuentaInversion"='N' or "PAR_IdTipoOperacion"= 'COMTER' or "PAR_IdTipoOperacion"= 'COMPAC'  or "PAR_IdTipoOperacion"='') THEN 1
								WHEN "REQ_IdCondicionOperativa" = 'SALDOFLUJOCAJA' and "PRE_OperaCuentaInversion"='S' and ("PAR_MontoOperacion" <= "FCA_Saldo" or ("PAR_MontoOperacion" >= "FCA_Saldo" and "PRE_SobregiroCuentaInversion"='S')) and ("PAR_IdTipoOperacion"= 'VENTER' or "PAR_IdTipoOperacion"= 'VENPAC') THEN 1
								WHEN "REQ_IdCondicionOperativa" = 'FECHAINVALIDA' and "PAR_FechaOperacion" = :v_FechaSistema THEN 1
								WHEN "REQ_IdCondicionOperativa" = 'PRODUCTOINVALIDO' AND "PRO_IdTipoProducto" = 'MERF' 	AND ("PRO_IdTipoOperacion" = "PAR_IdTipoOperacion" OR "PRO_IdTipoOperacion" = 0 ) THEN 1
								ELSE 
									case 
										when "REQ_IdTipoComportamiento" = 'RECHAZO' then 4
										when "REQ_IdTipoComportamiento" = 'AUTORIZACION' then 3
										when "REQ_IdTipoComportamiento" = 'ADVERTENCIA' then 2
										else 4
									end
								END 								AS "IdResultado"
							,CASE 
								WHEN "REQ_IdCondicionOperativa" = 'CONTCONDGRAPACTO' 	THEN 'CLIENTE_SIN_CONTRATO_COND_GRAL_PACTOS'
								WHEN "REQ_IdCondicionOperativa" = 'PERSONABLOQUEADA'	THEN 'PERSONA_NO_ACTIVA'
								WHEN "REQ_IdCondicionOperativa" = 'CLIENTEBLOQUEADO' 	THEN 'CLIENTE_BLOQUEADO'
								WHEN "REQ_IdCondicionOperativa" = 'PORTBLOQ' 			THEN 'PORTAFOLIO_BLOQUEADO'
								WHEN "REQ_IdCondicionOperativa" = 'SALDOFLUJOCAJA' 		THEN 'FLUJO_CAJA_INSUFICIENTE_PORTAFOLIO' 
								WHEN "REQ_IdCondicionOperativa" = 'FECHAINVALIDA' 		THEN 'FECHA_OPERACION_DISTINTA_FECHA_PROCESO'
								WHEN "REQ_IdCondicionOperativa" = 'PRODUCTOINVALIDO' 	THEN 'PRODUCTO_INVALIDO'
								ELSE '' END								AS "Codigo"
				  			
							,CASE 
								WHEN "REQ_IdCondicionOperativa" = 'CONTCONDGRAPACTO'  THEN 'DocumentoIdentidad'
								WHEN "REQ_IdCondicionOperativa" = 'PERSONABLOQUEADA' THEN 'NombrePersona'
								WHEN "REQ_IdCondicionOperativa" = 'PORTBLOQ' THEN 'IdPortafolio'
								WHEN "REQ_IdCondicionOperativa" = 'SALDOFLUJOCAJA' THEN 'IdMonedaLiquidacionOrden' 
								ELSE '' END 								AS "NombreParametro1"
								
							,CASE 
								WHEN "REQ_IdCondicionOperativa" = 'CONTCONDGRAPACTO'  THEN coalesce("CLI_DocumentoIdentidad",'')
								WHEN "REQ_IdCondicionOperativa" = 'PERSONABLOQUEADA' THEN coalesce("CLI_NombreCompleto",'')
								WHEN "REQ_IdCondicionOperativa" = 'CLIENTEBLOQUEADO' THEN coalesce("CLI_DocumentoIdentidad",'')
								WHEN "REQ_IdCondicionOperativa" = 'PORTBLOQ' THEN CONCAT("POR_CodigoPortafolio",CONCAT(' ',"POR_Proposito"))
								WHEN "REQ_IdCondicionOperativa" = 'SALDOFLUJOCAJA' THEN coalesce("PAR_IdMoneda",'')
								WHEN "REQ_IdCondicionOperativa" = 'PRODUCTOINVALIDO'  THEN coalesce("CLI_DocumentoIdentidad",'')
								ELSE '' END AS "ValorParametro1"
								
							,CASE 
								WHEN "REQ_IdCondicionOperativa" = 'SALDOFLUJOCAJA' THEN 'FlujoCajaPortafolio' 
								ELSE '' END 								AS "NombreParametro2"
								
							,CASE 
								WHEN "REQ_IdCondicionOperativa" = 'SALDOFLUJOCAJA' THEN CONCAT("POR_CodigoPortafolio",CONCAT(' ',"POR_Proposito"))
								ELSE '' END AS "ValorParametro2"
							
							
							
						FROM :v_ClientesVal;
			
			--select 'v_resultado' as "v_resultado",* from :v_resultado;


		SIGNAL SALIDA_CONTROLADA;
		END IF;
		

	END;	
END;
