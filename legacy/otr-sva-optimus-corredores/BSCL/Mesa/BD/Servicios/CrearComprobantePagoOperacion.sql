SET SCHEMA "BSCL";
go
DROP PROCEDURE "BSCL.Mesa.BD.Servicios::CrearComprobantePagoOperacion";
  go
CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::CrearComprobantePagoOperacion" (
	-- Parametros entrada comunes
	IN p_IdEmpresa			BIGINT,			--ID Empresa destino
	IN p_IdUsuario			BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo			NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN p_UsaReglasNegocio	NVARCHAR(1),	--S/N se usa en caso de servicios anidados.		  
	-- Parametros entrada
	--"BSCL.Mesa.BD::PAR.CrearComprobantePagoOperacion_IN"
	-- Parametros salida
	--"BSCL.Mesa.BD::PAR.CrearComprobantePagoOperacion_OUT"
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador	INT,
	--"BSCL.Mesa.BD::PAR.CrearComprobantePagoOperacionAdvertencias_OUT"
	OUT p_out_ErroresContador		INT
	--"BSCL.Mesa.BD::PAR.CrearComprobantePagoOperacionErrores_OUT"
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
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from "BSCL.Mesa.BD::PAR.CrearComprobantePagoOperacion_Errores_OUT";
		select count(1) into p_out_AdvertenciasContador 
		from "BSCL.Mesa.BD::PAR.CrearComprobantePagoOperacion_Advertencias_OUT";		 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from "BSCL.Mesa.BD::PAR.CrearComprobantePagoOperacion_Errores_OUT" UNION ALL  
								   select 0,v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador 
			from "BSCL.Mesa.BD::PAR.CrearComprobantePagoOperacion_Errores_OUT";										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	
	--p_out_AdvertenciasContador	:= 0;
	--p_out_ErroresContador		:= 0;
	--p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	--p_out_ErroresDetalle		= select 0 as "IdOperacion",'' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	BEGIN
		--------------------
		--Declare Variables
		--------------------
		DECLARE v_countPagoIngreso INT := 0;		
		p_out_AdvertenciasContador	:= 0;
		p_out_ErroresContador		:= 0;
		--------------------
		--Reglas Negocio
		--------------------
		
		--------------------
		--Lógica de Negocio 
		--------------------
/*
CREATE COLUMN TABLE "BSCL.Mesa.BD::MES.ComprobantePagoOperacion"
(
  "IdComprobantePagoOperacion" 	BIGINT			default '0' not null
, "IdEmpresa"					BIGINT			default '0' not null
, "IdOperacion"					BIGINT			default '0' not null
, "IdTipoProducto" 				NVARCHAR(4)		default '' not null
, "IdTipoMovimiento" 			NVARCHAR(6)		default '' not null
, "IdFormaDePago" 				NVARCHAR(40)	default '' not null
, "IdModalidadPago" 			NVARCHAR(6)		default '' not null
, "IdMoneda" 					NVARCHAR(3)		default '' not null
, "Monto" 						DECIMAL(26,8)	default '0' not null
, "FechaIngresoComprobantePagoOperacion" 		DATE 			default '0001-01-01' not null
, "FechaLiquidacionComprobantePagoOperacion"	DATE 			default '0001-01-01' not null
, "IdEstado" 					NVARCHAR(3)		default '' not null
, PRIMARY KEY ("IdComprobantePagoOperacion")
);
*/
		--las que son ingreso
		v_pagoIngreso 	= SELECT * FROM "BSCL.Mesa.BD::PAR.CrearComprobantePagoOperacion_IN" WHERE /*"IdComprobantePagoOperacion" = 0 AND*/ "Monto" > 0;
		--v_pagoEditar	= SELECT * FROM "BSCL.Mesa.BD::PAR.CrearComprobantePagoOperacion_IN" WHERE "IdComprobantePagoOperacion" > 0 AND "Monto" > 0;
		--v_pagoEliminar	= SELECT * FROM "BSCL.Mesa.BD::PAR.CrearComprobantePagoOperacion_IN" WHERE "IdComprobantePagoOperacion" > 0 AND "Monto" = 0;
		select count(1)
		into v_countPagoIngreso 
		from :v_pagoIngreso;
		/*
		UPDATE comp SET 
			  comp."IdTipoProducto"		= edit."IdTipoProducto"
			, comp."IdIngresoEgreso"	= edit."IdIngresoEgreso"
			, comp."IdAbono"			= edit."IdAbono"
			, comp."IdCargo"			= edit."IdCargo"
			, comp."IdFormaDePago"		= edit."IdFormaDePago"
			, comp."RecibePaga"			= edit."RecibePagaPortafolio"
			, comp."IdModalidadPago"	= edit."IdModalidadPago"
			, comp."IdMoneda"			= edit."IdMoneda"
			, comp."IdMonedaLiquidacion"= edit."IdMonedaLiquidacion"
			, comp."Monto"				= edit."Monto"
			, comp."MontoLiquidacion"	= edit."MontoLiquidacion"
			, comp."FechaIngresoComprobantePagoOperacion" 		= edit."FechaIngresoComprobantePagoOperacion"
			, comp."FechaLiquidacionComprobantePagoOperacion"	= edit."FechaLiquidacionComprobantePagoOperacion"
			, comp."IdEstado"			= edit."IdEstado"
		    , comp."AfectarTesoreria"   = edit."AfectarTesoreria"  
		FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion" comp
			INNER JOIN :v_pagoEditar edit ON (comp."IdComprobantePagoOperacion" = edit."IdComprobantePagoOperacion");
		*/
		DELETE FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion" comp 
        WHERE (comp."IdOperacion",comp."IdTipoProducto",comp."IdEmpresa") IN (SELECT ent."IdOperacion", ent."IdTipoProducto",p_IdEmpresa FROM "BSCL.Mesa.BD::PAR.CrearComprobantePagoOperacion_IN" ent); 	
		
		IF (v_countPagoIngreso > 0 ) THEN
			INSERT INTO "BSCL.Mesa.BD::MES.ComprobantePagoOperacion"
			(
				  "IdComprobantePagoOperacion"
				, "IdEmpresa"
				, "IdOperacion"
				, "IdTipoProducto"
				, "IdIngresoEgreso"
				, "IdAbono"
				, "IdCargo"
				, "IdIngresoEgresoAnticipo"
                , "IdAbonoAnticipo"
                , "IdCargoAnticipo"
				, "IdFormaDePago"
				, "RecibePaga"
				, "IdModalidadPago"
				, "IdMoneda"
				--, "IdMonedaLiquidacion"
				, "Monto"
				--, "MontoLiquidacion"
				, "FechaIngresoComprobantePagoOperacion"
				, "FechaLiquidacionComprobantePagoOperacion"
				, "IdEstado"
				, "AfectarTesoreria"
			)
			SELECT CASE WHEN "IdComprobantePagoOperacion" = 0 THEN "BSCL.Mesa.BD.Secuencias::IdComprobantePagoOperacion".NEXTVAL ELSE FMP."IdComprobantePagoOperacion" END AS "IdComprobantePagoOperacion" 
				 , :p_IdEmpresa    			AS "IdEmpresa"
				 , FMP."IdOperacion"
				 , FMP."IdTipoProducto" 				
				 , FMP."IdIngresoEgreso"
				 , FMP."IdAbono"
				 , FMP."IdCargo"
				 , FMP."IdIngresoEgresoAnticipo"
                 , FMP."IdAbonoAnticipo"
                 , FMP."IdCargoAnticipo"
				 , FMP."IdFormaDePago" 
				 , FMP."RecibePagaPortafolio" AS "RecibePaga"				
				 , FMP."IdModalidadPago" 			
				 , FMP."IdMoneda" 					
				 --, FMP."IdMonedaLiquidacion"
				 , FMP."Monto" 						
				 --, FMP."MontoLiquidacion"
				 , FMP."FechaIngresoComprobantePagoOperacion" 		
				 , FMP."FechaLiquidacionComprobantePagoOperacion"	
				 , FMP."IdEstado"
				 , FMP."AfectarTesoreria"
			FROM :v_pagoIngreso AS FMP;
		END IF;
		
		INSERT INTO "BSCL.Mesa.BD::PAR.CrearComprobantePagoOperacion_OUT"
		SELECT FMP."IdInternoPagoOperacion"
			 , COMP."IdOperacion"
			 , COMP."IdComprobantePagoOperacion"
			 , COMP."IdFormaDePago"
		FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion" AS COMP
			INNER JOIN "BSCL.Mesa.BD::PAR.CrearComprobantePagoOperacion_IN" AS FMP	ON (FMP."IdOperacion" = COMP."IdOperacion" AND FMP."IdTipoProducto" = COMP."IdTipoProducto" AND FMP."IdFormaDePago" = COMP."IdFormaDePago");
		
		
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
