SET SCHEMA "BSCL";

GO
DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::ConsultarCustodiaPortafolioFondoMutuo";


--  Proposito: Consulta Custodia via Servicio de Custodia Sumando movimientos en transito de Fondo Mutuos.
--  Autor: Gat-Blac 
--  Fecha Creacion: 11/10/2016
GO
CREATE PROCEDURE "BSCL.FondoMutuo.BD.Servicios::ConsultarCustodiaPortafolioFondoMutuo" (
	--VARIABLES OBLIGATORIAS
	IN p_IdEmpresa						BIGINT,
    IN p_IdUsuario						BIGINT,
    IN p_IdModulo						NVARCHAR(3), 
  
	IN p_IdCliente						BIGINT			DEFAULT 0,
	IN p_IdPortafolio					BIGINT			DEFAULT 0,
	IN p_IdInstrumento					BIGINT			DEFAULT 0,
	IN p_IdBoveda						NVARCHAR(3)		DEFAULT '',
	IN p_AgrupaPorBoveda 				NVARCHAR(1)		DEFAULT 'N',
	IN p_FechaConsulta					NVARCHAR(10)	DEFAULT '0001-01-01',
	IN p_IdProductoAgrupador			BIGINT			DEFAULT NULL,
	IN p_IdEmisor						NVARCHAR(40)	DEFAULT NULL,
	
	IN p_IdFamiliaFondo 				NVARCHAR(40)    DEFAULT '',
	IN p_BuscaMLT						NVARCHAR(1)		DEFAULT 'N',
	
	-- Parametros de Salida
   	OUT p_out_tbl table (
		"IdClasificacionTipoInstrumento"			NVARCHAR(3)
	   ,"NombreClasificacionTipoInstrumento"		NVARCHAR(100)
	   ,"IdTipoInstrumento"							NVARCHAR(3)
	   ,"NombreTipoInstrumento"						NVARCHAR(100)
	   ,"IdInstrumento"								BIGINT
	   ,"Nemotecnico"								NVARCHAR(40)
	   ,"IdSocioDeNegocio"							BIGINT
	   ,"IdCliente"									BIGINT
	   ,"IdPortafolio"								BIGINT
	   ,"DocumentoIdentidad"						NVARCHAR(40)
	   ,"RazonSocial"								NVARCHAR(100)
	   ,"CodigoPortafolio"							NVARCHAR(100)
	   ,"IdBoveda"									NVARCHAR(3)
	   ,"Relacion"									NVARCHAR(10)
	   ,"CantidadCustodia"							DECIMAL(26,8)
	   ,"Monto"										DECIMAL(26,8)
	   ,"IdMoneda"									NVARCHAR(3)
	   ,"ValorCuota"								DECIMAL(26,8)
	   ,"DecimalesValorCuota"						BIGINT
	   ,"CantidadGarantia"							DECIMAL(26,8)
	   ,"CantidadTransSolicitudesAporte"			DECIMAL(26,8)
	   ,"CantidadTransSolicitudesRescate"			DECIMAL(26,8)
	   ,"INGPrecioPromedioCompra"					DECIMAL(26,8)
	   ,"INGMontoPrecioPromedioCompra"				DECIMAL(26,8)
	   ,"INGMontoPrecioPromedioMonedaInstrumento"	DECIMAL(26,8)
	   ,"IdMonedaPrincipal"							NVARCHAR(3)
	   ,"IdMonedaFondo"								NVARCHAR(3)
	   ,"Producto"									NVARCHAR(100)
	   ,"IdEmisor"									NVARCHAR(40)
	   ,"MLT"										NVARCHAR(1)
	   ,"IdSerieFondo"								NVARCHAR(100)
	   ,"IdEmpresa"									BIGINT
	   ,"IdFamiliaFondo"							NVARCHAR(40)
	   ,"FondoExtranjero"							NVARCHAR(1)
	   ,"MontoMonedaSistema"						DECIMAL(26,8)
	   ,"FechaValorCuota"                           NVARCHAR(10)
	   ,"RegimenTributarioCartera"                  NVARCHAR(40)
	   ,"RegimenTributarioGarantia"                 NVARCHAR(40)
	)
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"

AS
BEGIN
	
	DECLARE v_CustodiaFondoMutuo table(
		"IdClasificacionTipoInstrumento"			NVARCHAR(3)
	   ,"NombreClasificacionTipoInstrumento"		NVARCHAR(100)
	   ,"IdTipoInstrumento"							NVARCHAR(3)
	   ,"NombreTipoInstrumento"						NVARCHAR(100)
	   ,"IdInstrumento"								BIGINT
	   ,"Nemotecnico"								NVARCHAR(40)
	   ,"IdEmisor"									NVARCHAR(40)
	   ,"IdSocioDeNegocio"							BIGINT
	   ,"IdCliente"									BIGINT
	   ,"IdPortafolio"								BIGINT
	   ,"DocumentoIdentidad"						NVARCHAR(40)
	   ,"RazonSocial"								NVARCHAR(100)
	   ,"CodigoPortafolio"							NVARCHAR(100)
	   ,"IdBoveda"									NVARCHAR(3)
	   ,"Relacion"									NVARCHAR(20)
	   ,"CantidadCustodia"							DECIMAL(26,8)
	   ,"CantidadGarantia"							DECIMAL(26,8)
	   ,"INGPrecioPromedioCompra"					DECIMAL(26,8)
	   ,"INGMontoPrecioPromedioCompra"				DECIMAL(26,8)
	   ,"INGMontoPrecioPromedioMonedaInstrumento"	DECIMAL(26,8)
	   ,"TotalPosicion"								DECIMAL(26,8)
	   ,"RegimenTributarioCartera"   				NVARCHAR(40)
	   ,"RegimenTributarioGarantia"   				NVARCHAR(40)
	);
	
	DECLARE v_Solicitudes table(
		"IdSerieFondo"			NVARCHAR(40)
	   ,"CantidadAporte"		DECIMAL(26,8)
	   ,"CantidadRescate"		DECIMAL(26,8)
	   ,"IdCliente"				BIGINT
	   ,"IdPortafolio"			BIGINT
	   ,"IdSocioDeNegocio"		BIGINT
	   ,"DocumentoIdentidad"	NVARCHAR(40)
	   ,"RazonSocial"			NVARCHAR(100)
	   ,"CodigoPortafolio"		NVARCHAR(100)
	   ,"IdInstrumento"			BIGINT
	   ,"IdEmisor"				NVARCHAR(40)
	);
	
	
	DECLARE v_Valores_Cuota_Valorizacion table (
	    "IdSerieFondo"          NVARCHAR(40),
	    "ValorCuota"            DECIMAL(26,8),
	    "FechaValorCuota"       NVARCHAR(10)
	);  
	
	DECLARE v_Valores_Cuotas_Actual table (
	    "IdSerieFondo"    NVARCHAR(40),
	    "ValorCuota"      DECIMAL(26,8),
	    "FechaValorCuota" NVARCHAR(10)  
	);    
	
	DECLARE v_Valores_Cuota_His table (
	    "IdSerieFondo"    NVARCHAR(40),
	    "ValorCuota"      DECIMAL(26,8),
	    "FechaValorCuota" NVARCHAR(10)  
	); 
	
	DECLARE v_FechaSistema NVARCHAR(10);
	DECLARE valor_cuota    INTEGER = 0;
	DECLARE valor_cuota_his INTEGER = 0;
	
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
	
	v_Valores_Cuota_Valorizacion = Select 0 As "IdSerieFondo", 0 As "ValorCuota", '' As "FechaValorCuota"  from dummy where 1=2;
	v_Valores_Cuotas_Actual      = Select 0 As "IdSerieFondo", 0 As "ValorCuota", '' As "FechaValorCuota"  from dummy where 1=2; 
	v_Valores_Cuota_His          = Select 0 As "IdSerieFondo", 0 As "ValorCuota", '' As "FechaValorCuota"  from dummy where 1=2; 
	
	
	v_Solicitudes =
		SELECT
			"Solicitudes"."IdSerieFondo"
		   --,TO_DECIMAL(SUM(CASE "Solicitudes"."TipoSolicitud" WHEN 'APO' THEN COALESCE("Solicitudes"."NumeroCuotas",0) ELSE 0 END),26,8) AS "CantidadAporte"
           ,TO_DECIMAL(SUM(CASE "Solicitudes"."TipoSolicitud" WHEN 'APO' THEN COALESCE(("Solicitudes"."Monto" / "ValorCuota"."ValorCuota"), 0) ELSE 0 END),26,8) AS "CantidadAporte"
		   --,TO_DECIMAL(SUM(CASE "Solicitudes"."TipoSolicitud" WHEN 'RES' THEN COALESCE("Solicitudes"."NumeroCuotas",0) ELSE 0 END),26,8) AS "CantidadRescate"
           ,TO_DECIMAL(SUM(CASE WHEN "Solicitudes"."TipoSolicitud" = 'RES' AND "Solicitudes"."CalculoPorMontoCuota" = 'M' THEN COALESCE(("Solicitudes"."Monto" / "ValorCuota"."ValorCuota"), 0) ELSE 0 END),26,8) AS "CantidadRescate"
		   ,"Solicitudes"."IdCliente"
		   ,"Solicitudes"."IdPortafolio"
		   ,"SocioNegocio"."IdSocioDeNegocio"
		   ,"ConsultarCliente"."DocumentoIdentidad"
		   ,"ConsultarCliente"."RazonSocial"
		   ,"ConsultarPortafolio"."CodigoPortafolio"
		   ,"SerieFondo"."IdInstrumento"
		   ,Fondo."IdEmisor"
		FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "Solicitudes"
		LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS "ConsultarPortafolio" ON  ( "ConsultarPortafolio"."IdPortafolio" = "Solicitudes"."IdPortafolio")
		LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "ConsultarCliente" ON  ( "ConsultarCliente"."IdCliente" = "Solicitudes"."IdCliente" AND "ConsultarCliente"."IdEmpresa" = :p_IdEmpresa)
		LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS "SocioNegocio" ON  ( "SocioNegocio"."IdCliente" = "Solicitudes"."IdCliente" AND "SocioNegocio"."IdEmpresa" = :p_IdEmpresa)
		LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieFondo" ON ("SerieFondo"."IdSerieFondo" = "Solicitudes"."IdSerieFondo" AND "SerieFondo"."IdEmpresa" = :p_IdEmpresa)
		LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.ValorCuota" AS "ValorCuota" ON "ValorCuota"."IdSerieFondo" = "SerieFondo"."IdSerieFondo"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = "Solicitudes"."IdPortafolio" AND Portafolio."IdCliente" = "Solicitudes"."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
		LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS Fondo ON Fondo."IdFondo" = "SerieFondo"."IdFondo" AND Fondo."IdEmpresa" = "SerieFondo"."IdEmpresa"
		WHERE
			"Solicitudes"."Estado" = 'ING'
		AND "Solicitudes"."FechaCertificacion" <= :p_FechaConsulta
		AND "Solicitudes"."IdEmpresa" = :p_IdEmpresa
		AND "Solicitudes"."TipoSolicitud" IN ('APO','RES')
		AND (:p_IdCliente = 0 OR "Solicitudes"."IdCliente" = :p_IdCliente)
		AND (:p_IdPortafolio = 0 OR "Solicitudes"."IdPortafolio" = :p_IdPortafolio)
		AND (:p_IdInstrumento = 0 OR "SerieFondo"."IdInstrumento" = :p_IdInstrumento)
		AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador)
		AND (:p_IdEmisor IS NULL OR Fondo."IdEmisor" = :p_IdEmisor)
		AND (:p_IdFamiliaFondo ='' OR "SerieFondo"."IdFamiliaFondo" = :p_IdFamiliaFondo)
		
		
		
		GROUP BY
			"Solicitudes"."IdSerieFondo"
		   ,"Solicitudes"."IdCliente"
		   ,"Solicitudes"."IdPortafolio"
		   ,"SocioNegocio"."IdSocioDeNegocio"
		   ,"ConsultarCliente"."DocumentoIdentidad"
		   ,"ConsultarCliente"."RazonSocial"
		   ,"ConsultarPortafolio"."CodigoPortafolio"
		   ,"SerieFondo"."IdInstrumento" 
		   ,Fondo."IdEmisor";
	
	
	-- LLamamos servicio de fondos mutuos
	CALL "BSCL.FondoMutuo.BD.Utilitarios::ConsultarCustodiaPortafolioFFMM"(
		p_IdEmpresa							=> :p_IdEmpresa,
		p_IdUsuario							=> :p_IdUsuario,
		p_IdModulo							=> :p_IdModulo,
		p_IdCliente							=> :p_IdCliente,
		p_IdPortafolio						=> :p_IdPortafolio,
		p_IdProductoAgrupador				=> :p_IdProductoAgrupador,
		p_IdEmisor							=> :p_IdEmisor,
		p_IdClasificacionTipoInstrumento	=> 'FM',
		p_IdTipoInstrumento					=> 'FFM',
		p_IdInstrumento						=> :p_IdInstrumento,
		p_IdBoveda							=> :p_IdBoveda,
		p_AgrupaPorBoveda					=> :p_AgrupaPorBoveda,
		p_FechaConsulta						=> :p_FechaConsulta,
		p_IdFamiliaFondo					=> :p_IdFamiliaFondo,
		p_out_tbl							=> v_CustodiaFondoMutuo
	);
	
	
	v_series =
		SELECT DISTINCT
			"SerieDeFondo"."IdSerieFondo"
		   ,"SerieDeFondo"."IdInstrumento"
		   ,"Fondo"."IdMoneda"
		   ,"SerieDeFondo"."DecimalesValorCuota"
		   ,"Fondo"."IdFondo"
		   ,"Fondo"."IdEmpresa"
		   ,"SerieDeFondo"."IdFamiliaFondo"
		FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieDeFondo"
		LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON ("Fondo"."IdFondo" = "SerieDeFondo"."IdFondo" AND "Fondo"."IdEmpresa" = "SerieDeFondo"."IdEmpresa"); 
	

	If To_Date(:p_FechaConsulta) >= To_Date(:v_FechaSistema) THEN 
	   v_Valores_Cuota_Valorizacion = Select VC."IdSerieFondo",
	                                    VC."ValorCuota",
	                                    :p_FechaConsulta As "FechaValorCuota"
	                             From "BSCL.FondoMutuo.BD::FMM.ValorCuota" VC
	                             Where (VC."FechaCierre" = :p_FechaConsulta OR :p_FechaConsulta = v_FechaSistema)
	                                And VC."IdEmpresa" = :p_IdEmpresa;
	 Else
	   v_Valores_Cuota_Valorizacion = Select VCH."IdSerieFondo",
	                                Max(VCH."ValorCuota") As "ValorCuota",
	                                Max("FechaCierre") As "FechaValorCuota"
	                         From "BSCL.FondoMutuo.BD::FMM.ValorCuotaHistorico" VCH
	                         Where VCH."FechaCierre" = :p_FechaConsulta
	                           And VCH."IdEmpresa" = :p_IdEmpresa
	                         Group By VCH."IdSerieFondo";
	                         
	                         
	    Select Count("IdSerieFondo")
	    Into valor_cuota_his
	    From :v_Valores_Cuota_Valorizacion;
	    
	    select valor_cuota_his As "valor_cuota_his" from dummy;
	    
	    if (valor_cuota_his = 0) Then 
	        
	    v_Valores_Cuota_Valorizacion =  Select VC."IdSerieFondo",
	                                           VC."ValorCuota",
	                                           :v_FechaSistema As "FechaValorCuota"
	                                    From "BSCL.FondoMutuo.BD::FMM.ValorCuota" VC
	                                    Where VC."IdEmpresa" = :p_IdEmpresa;
	                                         
	                                         
	    end If;                  
	 End if;       
	 
	 	
	
	v_salida = SELECT  ROW_NUMBER() OVER(ORDER BY "IdInstrumento") AS "IdInterno",
                       * 
               FROM	(SELECT
			"Custodia"."IdClasificacionTipoInstrumento"
		   ,"Custodia"."NombreClasificacionTipoInstrumento"
		   ,"Custodia"."IdTipoInstrumento"
		   ,"Custodia"."NombreTipoInstrumento"
		   ,"Custodia"."IdInstrumento"
		   ,"Custodia"."Nemotecnico"
		   ,"Custodia"."IdSocioDeNegocio"
		   ,"Custodia"."IdCliente"
		   ,"Custodia"."IdPortafolio"
		   ,"Custodia"."DocumentoIdentidad"
		   ,"Custodia"."RazonSocial"
		   ,"Custodia"."CodigoPortafolio"
		   ,"Custodia"."IdBoveda"
		   ,"Custodia"."Relacion"
		   ,"Custodia"."CantidadCustodia"
		   ,"Custodia"."CantidadCustodia" * COALESCE("ValoresCuota"."ValorCuota",0) AS "Monto"
		   ,"Series"."IdMoneda"
		   ,COALESCE("ValoresCuota"."ValorCuota",0) AS "ValorCuota"
		   ,"Series"."DecimalesValorCuota"
		   ,"Custodia"."CantidadGarantia"
		   ,COALESCE("Solicitudes"."CantidadAporte",0) AS "CantidadTransSolicitudesAporte"
		   ,COALESCE("Solicitudes"."CantidadRescate",0) AS "CantidadTransSolicitudesRescate"
		   ,"Custodia"."INGPrecioPromedioCompra"
		   ,"Custodia"."INGMontoPrecioPromedioCompra"
		   ,"Custodia"."INGMontoPrecioPromedioMonedaInstrumento"
		   ,"Series"."IdMoneda" AS "IdMonedaPrincipal"
		   ,"Series"."IdMoneda" AS "IdMonedaFondo"
		   ,ProductoAgrupador."NombreProductoAgrupador" AS "Producto"
		   ,"Custodia"."IdEmisor" AS "IdEmisor"
		   ,"Series"."IdSerieFondo"
		   ,"Series"."IdEmpresa"
		   ,"Series"."IdFamiliaFondo"
		   ,Fondo."FondoExtranjero"
		   ,"ValoresCuota"."FechaValorCuota"
		   ,"Custodia"."RegimenTributarioCartera"
		   ,"Custodia"."RegimenTributarioGarantia"
		FROM :v_CustodiaFondoMutuo AS "Custodia"
		LEFT JOIN :v_Solicitudes AS "Solicitudes" ON ("Custodia"."IdInstrumento" = "Solicitudes"."IdInstrumento" AND "Custodia"."IdCliente" = "Solicitudes"."IdCliente" AND "Custodia"."IdPortafolio" = "Solicitudes"."IdPortafolio")
		LEFT JOIN :v_series AS "Series" ON ("Series"."IdInstrumento" = "Custodia"."IdInstrumento")
		LEFT JOIN :v_Valores_Cuota_Valorizacion "ValoresCuota" ON ("ValoresCuota"."IdSerieFondo" = "Series"."IdSerieFondo")
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = "Custodia"."IdPortafolio" AND Portafolio."IdCliente" = "Custodia"."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
		LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador ON ProductoAgrupador."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
		LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS Fondo ON Fondo."IdFondo" = "Series"."IdFondo" AND Fondo."IdEmpresa" = "Series"."IdEmpresa"
		UNION ALL
		SELECT
			'FM' AS "IdClasificacionTipoInstrumento"
		   ,'FM' AS "NombreClasificacionTipoInstrumento"
		   ,'FFM' AS "IdTipoInstrumento"
		   ,'FFM' AS "NombreTipoInstrumento"
		   ,"Solicitudes"."IdInstrumento"
		   ,"Solicitudes"."IdSerieFondo" AS "Nemotecnico"
		   ,"Solicitudes"."IdSocioDeNegocio"
		   ,"Solicitudes"."IdCliente"
		   ,"Solicitudes"."IdPortafolio"
		   ,"Solicitudes"."DocumentoIdentidad"
		   ,"Solicitudes"."RazonSocial"
		   ,"Solicitudes"."CodigoPortafolio"
		   ,'COR' AS "IdBoveda"
		   ,'' AS "Relacion"
		   ,0 AS "CantidadCustodia"
		   ,0 AS "Monto"
		   ,"Series"."IdMoneda"
		   ,COALESCE("ValoresCuota"."ValorCuota",0) AS "ValorCuota"
		   ,"Series"."DecimalesValorCuota"
		   ,0 AS "CantidadGarantia"
		   ,COALESCE("Solicitudes"."CantidadAporte",0) AS "CantidadTransSolicitudesAporte"
		   ,COALESCE("Solicitudes"."CantidadRescate",0) AS "CantidadTransSolicitudesRescate"
		   ,0 AS "INGPrecioPromedioCompra"
		   ,0 AS "INGMontoPrecioPromedioCompra"
		   ,0 AS "INGMontoPrecioPromedioMonedaInstrumento"
		   ,"Series"."IdMoneda" AS "IdMonedaPrincipal"
		   ,"Series"."IdMoneda" AS "IdMonedaFondo"
		   ,ProductoAgrupador."NombreProductoAgrupador" AS "Producto"
		   ,"Solicitudes"."IdEmisor" AS "IdEmisor"
		   ,"Series"."IdSerieFondo"
		   ,"Series"."IdEmpresa"
		   ,"Series"."IdFamiliaFondo"
		   ,Fondo."FondoExtranjero"
		   ,"ValoresCuota"."FechaValorCuota"     
		   ,'' as "RegimenTributarioCartera"
		   ,'' as "RegimenTributarioCustodia"         
		FROM :v_Solicitudes AS "Solicitudes"
		LEFT JOIN :v_CustodiaFondoMutuo AS "Custodia" ON ("Custodia"."IdInstrumento" = "Solicitudes"."IdInstrumento" AND "Custodia"."IdCliente" = "Solicitudes"."IdCliente" AND "Custodia"."IdPortafolio" = "Solicitudes"."IdPortafolio")
		LEFT JOIN :v_series AS "Series" ON ("Series"."IdInstrumento" = "Solicitudes"."IdInstrumento")
		LEFT JOIN :v_Valores_Cuota_Valorizacion "ValoresCuota" ON ("ValoresCuota"."IdSerieFondo" = "Series"."IdSerieFondo")
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = "Solicitudes"."IdPortafolio" AND Portafolio."IdCliente" = "Solicitudes"."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
		LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador ON ProductoAgrupador."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
		LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS Fondo ON Fondo."IdFondo" = "Series"."IdFondo" AND Fondo."IdEmpresa" = "Series"."IdEmpresa"
		WHERE COALESCE("Custodia"."IdInstrumento",0) = 0);
		

	IF p_BuscaMLT = 'S' THEN
		v_salida2 = SELECT Salida.*,CASE WHEN RegimenFM."RescateConMandato" = 'S' OR RegimenFM."AceptaMandatoEnReinversion" = 'S' THEN 'S' ELSE 'N' END AS "MLT"
					FROM :v_salida AS Salida
					LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo" AS Regimen ON Regimen."IdSerieFondo" = Salida."IdSerieFondo" AND Regimen."IdEstado"='ACT'
					LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS RegimenFM ON RegimenFM."IdRegimenFondoMutuo" = Regimen."IdRegimenFondoMutuo";
	ELSE
		v_salida2 = SELECT Salida.*,'N' AS "MLT" FROM :v_salida AS Salida;		
	END IF;
	
	v_valorizacion = SELECT
                "IdInterno",:p_FechaConsulta AS "Fecha","Nemotecnico" AS "IdSerieFondo",'C' AS "CalculoPorMontoCuota",0 AS "Monto",
                "CantidadCustodia" AS "NumeroCuotas","IdMonedaFondo","IdMonedaFondo" AS "IdMonedaInversion",'APO' AS "TipoSolicitud",
                'UF' AS "TipoFactorAjuste",:p_IdEmpresa AS "IdEmpresa"
            FROM :v_salida2
            WHERE "CantidadCustodia" != 0 AND "Monto" != 0;
    
    CALL "BSCL.FondoMutuo.BD.Utilitarios::CalcularMontoNumeroCuota"(
            p_IdEmpresa     => :p_IdEmpresa,
            p_Series        => :v_valorizacion,
            p_out_Resultado => v_calculo_valorizacion);
	
	p_out_tbl =
		SELECT
		V."IdClasificacionTipoInstrumento"
	   ,V."NombreClasificacionTipoInstrumento"
	   ,V."IdTipoInstrumento"
	   ,V."NombreTipoInstrumento"
	   ,V."IdInstrumento"
	   ,V."Nemotecnico"
	   ,V."IdSocioDeNegocio"
	   ,V."IdCliente"
	   ,V."IdPortafolio"
	   ,V."DocumentoIdentidad"
	   ,V."RazonSocial"
	   ,V."CodigoPortafolio"
	   ,V."IdBoveda"
	   ,V."Relacion"
	   ,V."CantidadCustodia"
	   ,V."Monto"
	   ,V."IdMoneda"
	   ,V."ValorCuota"
	   ,V."DecimalesValorCuota"
	   ,V."CantidadGarantia"
	   ,V."CantidadTransSolicitudesAporte"
	   ,V."CantidadTransSolicitudesRescate"
	   ,V."INGPrecioPromedioCompra"
	   ,V."INGMontoPrecioPromedioCompra"
	   ,V."INGMontoPrecioPromedioMonedaInstrumento"
	   ,V."IdMonedaPrincipal"
	   ,V."IdMonedaFondo"
	   ,V."Producto"
	   ,V."IdEmisor"
	   ,V."MLT"
	   ,V."IdSerieFondo"
	   ,V."IdEmpresa"
	   ,V."IdFamiliaFondo"
	   ,V."FondoExtranjero"
	   ,VC."MontoMonedaSistema"
	   ,V."FechaValorCuota"
	   ,V."RegimenTributarioCartera"
	   ,V."RegimenTributarioGarantia"
	   FROM :v_salida2 AS V
	   LEFT JOIN :v_calculo_valorizacion VC ON (V."IdInterno" = VC."IdInterno");
	
END;
