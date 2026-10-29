SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Mesa.BD.Servicios::ConsultarOperaciones";


CREATE PROCEDURE 
--  
--  Proposito: Servicio Consulta Operaciones. Original de Vista BSCL.Mesa.BD.Vistas::ConsultarOperaciones. Se cambio a servicio para poder consultar el Flujo de caja
--  Autor: - BCS 
--  Fecha Creacion: 22-05-2017
--  

"BSCL.Mesa.BD.Servicios::ConsultarOperaciones" (
	 IN p_IdEmpresa          		BIGINT
	,IN p_IdMesa						BIGINT
	,IN p_IdTipoProducto				NVARCHAR(4)
	,IN p_FechaVencimiento 	       NVARCHAR(10)
	,IN p_FechaVencimientoFin      NVARCHAR(10)
	--"BSCL.Mesa.BD::PAR.ConsultarOperacionesTipoOperacion_IN"
	--"BSCL.Mesa.BD::PAR.ConsultarOperacionesEstados_IN"
	--"BSCL.Mesa.BD::PAR.ConsultarOperaciones_OUT"
    
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
	--READS SQL DATA WITH RESULT VIEW "BSCL.Mesa.BD.Vistas::ConsultarOperacionesUI" 
AS 
BEGIN
	BEGIN
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
		
		--select * from "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.FechaSistema';
		--select * from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa";

		v_Operaciones = SELECT op.* 
						FROM "BSCL.Mesa.BD.Vistas::ConsultarOperaciones" op
							INNER JOIN "BSCL.Mesa.BD::PAR.ConsultarOperacionesTipoOperacion_IN" tipOp ON (tipOp."IdTipoOperacion" = op."IdTipoOperacion")
							INNER JOIN "BSCL.Mesa.BD::PAR.ConsultarOperacionesEstados_IN" est ON (est."IdEstado" = op."IdEstado")
						WHERE 
							op."IdEmpresa" = p_IdEmpresa
							AND op."IdMesaOrigen" = p_IdMesa
							AND op."IdTipoProducto" = p_IdTipoProducto
							AND (op."FechaVencimiento" BETWEEN TO_SECONDDATE(p_FechaVencimiento) AND ADD_SECONDS(TO_SECONDDATE(p_FechaVencimientoFin), 86399) 
							OR  (op."FechaCondicionLiquidacion" BETWEEN TO_SECONDDATE(p_FechaVencimiento) AND ADD_SECONDS(TO_SECONDDATE(p_FechaVencimientoFin), 86399) AND op."IdEstado" IN ('LIQ') )
							OR  (op."FechaLiquidacion" BETWEEN TO_SECONDDATE(p_FechaVencimiento) AND ADD_SECONDS(TO_SECONDDATE(p_FechaVencimientoFin), 86399) AND op."IdEstado" IN ('ANT') ))
							AND op."Anticipado" IN ('N', 'S')
							AND op."IdEstado" NOT IN ('ANU');
 
							--and op."FechaVencimiento" = p_FechaVencimiento;
        --SELECT 'v_Operaciones' AS "v_Operaciones",* FROM :v_Operaciones;							
		v_PortafolioFlujoCaja = SELECT DISTINCT "IdPortafolio" as "IdPortafolio"
									, "IdTipoReajuste" as "IdMoneda" 
								FROM :v_Operaciones;
		--Consultar FLujo Caja para la moneda ingresada para los portafolios que operan con cuenta inversion
		call "BSCL.Plataforma.BD.Servicios::SaldoFlujoCaja"(
								    p_IdEmpresa 			=> p_IdEmpresa
									,p_FechaDesde 			=> p_FechaVencimiento--v_FechaDesde
								    ,p_FechaHasta 			=> p_FechaVencimiento--v_FechaHasta
								    ,p_Uso 					=> 'MESA_RF'
									,p_DesplegarEnUI 		=> 'N'
									,p_AgruparPorOrigen 	=> 'N'
								    ,p_PortafolioAConsultar	=> :v_PortafolioFlujoCaja
									-- Parametros salida
								    ,p_out_FlujoCaja			=> :v_Out_FlujoCaja 
									,p_out_FlujoCajaDetallado=> :v_Out_FlujoCajaDetallado
									-- Salida errores
									,p_out_ErroresContador	=> v_out_ErroresContadorFlujoCaja
									,p_out_ErroresDetalle	=> v_out_ErroresDetalleFlujoCaja);
		
		--select * from :v_Out_FlujoCaja;
		INSERT INTO "BSCL.Mesa.BD::PAR.ConsultarOperaciones_OUT"(
		"IdOperacion"
		, "FolioOperacion"
		, "FechaOperacion"
		, "IdTipoProducto"
		, "IdTipoOperacion"
		, "NombreOperacion"
		, "IdOperacionRelacionada"
		, "Intermesa"
		, "IdMesaOrigen"
		, "NombreMesaOrigen"
		, "IdMesaDestino"
		, "NombreMesaDestino"
		, "IdCliente"
		, "IdPortafolio"
		, "CodigoPortafolio"
		, "IdClienteCorredor"
		, "IdPortafolioCorredor"
		, "RazonSocial"
		, "MontoOperacion"
		, "MontoOperacionInicial"
		, "MontoOperacionFinal"
		, "IdTipoReajuste"
		, "IdMoneda"
		, "Tasa"
		, "MontoOperacionMoneda"
		, "MontoOperacionInicialMoneda"
		, "MontoOperacionFinalMoneda"
		, "IdMonedaLiquidacion"
		, "PlazoEnDias"
		, "FechaVencimiento"
		, "FechaLiquidacion"
		, "IdCondicionLiquidacion"
		, "FechaCondicionLiquidacion"
		, "IdAgente"
		, "TasaAgente"
		, "IdEstado"
		, "NombreEstado"
		, "FechaCreacion"
		, "IdUsuarioCreacion"
		, "CodigoUsuario"
		, "Glosa"
		, "IdEmpresa"
		, "DocumentoIdentidad"
		, "IdTipoCanalIngreso"
		, "AfectaCustodia"
		, "MontoOperacionAnticipo"
		, "MontoOperacionInicialAnticipo"
		, "MontoOperacionFinalAnticipo"
		, "TasaAnticipo"
		, "MontoOperacionMonedaAnticipo"
		, "MontoOperacionInicialMonedaAnticipo"
		, "MontoOperacionFinalMonedaAnticipo"
		, "IdMonedaLiquidacionAnticipo"
		, "IdCondicionLiquidacionAnticipo"
		, "FechaCondicionLiquidacionAnticipo"
		, "SaldoCaja"
		, "FechaVencimientoUI"
		, "TipoDocumento"
		, "ConfirmaAdemdum"
 		, "IdUsuarioConfirma"
 		, "IdTipoInstrumento"
		, "IdOperacionOrigenReasignacion"
		)
		SELECT op."IdOperacion"
		, op."FolioOperacion"
		, op."FechaOperacion"
		, op."IdTipoProducto"
		, op."IdTipoOperacion"
		, op."NombreOperacion"
		, op."IdOperacionRelacionada"
		, op."Intermesa"
		, op."IdMesaOrigen"
		, op."NombreMesaOrigen"
		, op."IdMesaDestino"
		, op."NombreMesaDestino"
		, op."IdCliente"
		, op."IdPortafolio"
		, op."CodigoPortafolio"
		, op."IdClienteCorredor"
		, op."IdPortafolioCorredor"
		, op."RazonSocial"
		, op."MontoOperacion"
		, op."MontoOperacionInicial"
		, op."MontoOperacionFinal"
		, op."IdTipoReajuste"
		, op."IdMoneda"
		, op."Tasa"
		, op."MontoOperacionMoneda"
		, op."MontoOperacionInicialMoneda"
		, op."MontoOperacionFinalMoneda"
		, op."IdMonedaLiquidacion"
		, op."PlazoEnDias"
		, op."FechaVencimiento"
		, op."FechaLiquidacion"
		, op."IdCondicionLiquidacion"
		, CASE op."IdEstado" WHEN 'ANT' THEN op."FechaCondicionLiquidacionAnticipo"  ELSE op."FechaCondicionLiquidacion" END AS "FechaCondicionLiquidacion"
		, op."IdAgente"
		, op."TasaAgente"
		, op."IdEstado"
		, op."NombreEstado"
		, op."FechaCreacion"
		, op."IdUsuarioCreacion"
		, op."CodigoUsuario"
		, op."Glosa"
		, op."IdEmpresa"
		, op."DocumentoIdentidad"
		, op."IdTipoCanalIngreso"
		, op."AfectaCustodia"
		, op."MontoOperacionAnticipo"
		, op."MontoOperacionInicialAnticipo"
		, op."MontoOperacionFinalAnticipo"
		, op."TasaAnticipo"
		, op."MontoOperacionMonedaAnticipo"
		, op."MontoOperacionInicialMonedaAnticipo"
		, op."MontoOperacionFinalMonedaAnticipo"
		, op."IdMonedaLiquidacionAnticipo"
		, op."IdCondicionLiquidacionAnticipo"
		, op."FechaCondicionLiquidacionAnticipo"
		, flu."Saldo" as "SaldoCaja"
		, op."FechaVencimientoUI"
		, op."TipoDocumento"
		, op."ConfirmaAdemdum"
 		, op."IdUsuarioConfirma"
 		, op."IdTipoInstrumento"
		, op."IdOperacionOrigenReasignacion"
		FROM :v_Operaciones op
			INNER JOIN :v_Out_FlujoCaja flu ON (flu."IdPortafolio" = op."IdPortafolio" and flu."IdMoneda" = op."IdTipoReajuste");
	END;
END;