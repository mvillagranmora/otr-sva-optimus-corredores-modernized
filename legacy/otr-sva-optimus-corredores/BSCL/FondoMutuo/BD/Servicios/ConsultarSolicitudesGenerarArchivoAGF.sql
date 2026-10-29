SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::ConsultarSolicitudesGenerarArchivoAGF";

CREATE PROCEDURE
--  Proposito: Obtiene solicitudes de fondos mutuos para generar archivo para AGF.
--  Autor: Gat-Blac 
--  Fecha Creacion: 23/11/2016
"BSCL.FondoMutuo.BD.Servicios::ConsultarSolicitudesGenerarArchivoAGF" (
	--VARIABLES OBLIGATORIAS
	IN p_IdEmpresa 								BIGINT,
	IN p_IdPortafolioAGF						BIGINT,
	IN p_FechaDesde     						NVARCHAR(10),
	IN p_FechaHasta     						NVARCHAR(10),
  	OUT p_out_Resultado							TABLE (
											  		"Registro" NVARCHAR(5000)
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
		
		DECLARE v_FechaSistema 		NVARCHAR(10);
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
		
		p_tablaModulo = SELECT 'FAC' AS "IdModulo"
						FROM DUMMY;
				
		p_registroParametro = SELECT 'FECHA' AS "Codigo", :v_FechaSistema AS "Valor" 
							  FROM DUMMY 
                              UNION ALL
                              SELECT 'FECHADESDE' AS "Codigo", :p_FechaDesde AS "Valor" 
							  FROM DUMMY 
                              UNION ALL
                              SELECT 'FECHAHASTA' AS "Codigo", :p_FechaHasta AS "Valor" 
							  FROM DUMMY;
		
		CALL "BSCL.Normativa.BD.Servicios.NOR::ConsultarOperaciones"
		(  :p_IdEmpresa,
		   '',
		   '',
		   :p_tablaModulo,
		   :p_registroParametro,
		   'N',
		   p_out_Salida_FAC
		);
		
		p_tablaModulo = SELECT 'MRF' AS "IdModulo"
						FROM DUMMY;
		
		CALL "BSCL.Normativa.BD.Servicios.NOR::ConsultarOperaciones"
		(  :p_IdEmpresa,
		   '',
		   '',
		   :p_tablaModulo,
		   :p_registroParametro,
		   'N',
		   p_out_Salida_MRF
		);	  	
        				  --Facturas de compra
        p_out_Resultado = SELECT top 100 COALESCE("Operaciones"."FolioTransaccion",'') || ';' ||
								 CASE "Operaciones"."FechaMovimiento" WHEN '' THEN '0001-01-01'
								                                      ELSE TO_NVARCHAR("Operaciones"."FechaMovimiento",'YYYY-MM-DD')
								 END ||	';' || 	
								 COALESCE("Operaciones"."IdMovimientoRelacionado",0) || ';' || --falta FolEve
								 COALESCE("SerieDeFondo"."IdFondo",'') || ';' ||
								 '' || ';' ||
								 COALESCE("ConsultarCliente"."DocumentoIdentidad",'') || ';' ||
								 COALESCE("ConsultarPortafolio"."CodigoPortafolio",'') || ';' ||
								 COALESCE("Operaciones"."MontoCompra",0)|| ';' ||
								 COALESCE("Operaciones"."Precio",0) || ';' ||
								 COALESCE("Operaciones"."Cantidad",0) || ';' ||
								 COALESCE("Operaciones"."Tasa",0) || ';' ||
								 CASE "TipoOperacion"."Accion" WHEN 'COMPRA' THEN 'C'
															   ELSE ''
								 END || ';' || --TipoMovimiento
								 CASE "TipoOperacion"."Accion" WHEN 'COMPRA' THEN 'C'
															   ELSE ''
								 END || ';' || --TipoOperacion
                                 COALESCE("Operaciones"."Nemotecnico",'') || ';' ||
                                 COALESCE("Operaciones"."IdEmisor",'') || ';' ||
                                 CASE "Instrumento"."IdTipoReajuste" WHEN 'USD' THEN 'US$'
                                 									 WHEN 'CLP' THEN '$'
                                 									 WHEN ''  THEN '$'
                                 									 ELSE '$'
                                 END || ';' ||
                                 COALESCE("Instrumento"."FechaVencimiento",'0001-01-01') || ';' ||
                                 COALESCE("Operaciones"."ValorRescateInicial",0) || ';' ||
                                 COALESCE("Operaciones"."IdBoveda",'') || ';' ||
                                 COALESCE("CondicionDeLiquidacion"."Codigo",'') || ';' ||
                                 '' || ';' || --AS "FechaVencimientoPacto"
                                 '0' || ';' || --AS "PlazoPacto"
                                 '0' || ';' || --AS "TasaCompromiso"
                                 CASE 'CLP'                          WHEN 'USD' THEN 'US$'
                                 									 WHEN 'CLP' THEN '$'
                                 									 WHEN 'UF' THEN 'UF'
                                 									 WHEN ''  THEN '$'
                                 									 ELSE '$'
                                 END || ';' || --dummy, reajuste por compromiso
                                 '0' || ';' || --Monto total operacion, hay que ver de donde sale
								 'GESFFMM' || ';' ||
								 
								 '' AS "Registro"
						  FROM :p_out_Salida_FAC AS "Operaciones"
						  LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS "ConsultarPortafolio" ON  ( "ConsultarPortafolio"."IdPortafolio" = "Operaciones"."IdPortafolio" )
						  LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "ConsultarCliente" ON  ( "ConsultarCliente"."IdCliente" = "ConsultarPortafolio"."IdCliente" )
						  LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS "TipoOperacion" ON ("TipoOperacion"."IdTipoOperacion" = "Operaciones"."IdTipoOperacion")
						  LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento" ON ("Instrumento"."IdInstrumento" = "Operaciones"."IdInstrumento")
						  LEFT OUTER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSerieDeFondo" AS "SerieDeFondo" ON ("SerieDeFondo"."IdPortafolio" = "Operaciones"."IdPortafolio")
						  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS "CondicionDeLiquidacion" ON ("CondicionDeLiquidacion"."IdCondicionDeLiquidacion" = "Operaciones"."IdCondicionLiquidacion")
						  WHERE "Operaciones"."IdTipoComprobante" in ('FAE','FAC')
						  AND "TipoOperacion"."Accion" = 'COMPRA'
						  AND "SerieDeFondo"."IdPortafolio" = :p_IdPortafolioAGF
						  UNION ALL
						  --Facturas de venta
						  SELECT top 100 COALESCE("Operaciones"."FolioTransaccion",'') || ';' ||
								 CASE "Operaciones"."FechaMovimiento" WHEN '' THEN '0001-01-01'
								                                      ELSE TO_NVARCHAR("Operaciones"."FechaMovimiento",'YYYY-MM-DD')
								 END ||	';' || 	
								 COALESCE("Operaciones"."IdMovimientoRelacionado",0) || ';' || --falta FolEve
								 COALESCE("SerieDeFondo"."IdFondo",'') || ';' ||
								 '' || ';' ||
								 COALESCE("ConsultarCliente"."DocumentoIdentidad",'') || ';' ||
								 COALESCE("ConsultarPortafolio"."CodigoPortafolio",'') || ';' ||
								 COALESCE("Operaciones"."MontoVenta",0) || ';' ||
								 COALESCE("Operaciones"."Precio",0) || ';' ||
								 COALESCE("Operaciones"."Cantidad",0) || ';' ||
								 COALESCE("Operaciones"."Tasa",0) || ';' ||
								 CASE "TipoOperacion"."Accion" WHEN 'VENTA' THEN 'V'
															   ELSE ''
								 END || ';' || --TipoMovimiento
								 CASE "TipoOperacion"."Accion" WHEN 'VENTA' THEN 'V'
															   ELSE ''
								 END || ';' || --TipoOperacion
                                 COALESCE("Operaciones"."Nemotecnico",'') || ';' ||
                                 COALESCE("Operaciones"."IdEmisor",'') || ';' ||
                                 CASE "Instrumento"."IdTipoReajuste" WHEN 'USD' THEN 'US$'
                                 									 WHEN 'CLP' THEN '$'
                                 									 WHEN ''  THEN '$'
                                 									 ELSE '$'
                                 END || ';' ||
                                 COALESCE("Instrumento"."FechaVencimiento",'0001-01-01') || ';' ||
                                 COALESCE("Operaciones"."ValorRescateInicial",0) || ';' ||
                                 COALESCE("Operaciones"."IdBoveda",'') || ';' ||
                                 COALESCE("CondicionDeLiquidacion"."Codigo",'') || ';' ||
                                 '' || ';' || --AS "FechaVencimientoPacto"
                                 '0' || ';' || --AS "PlazoPacto"
                                 '0' || ';' || --AS "TasaCompromiso"
                                 CASE 'CLP'                          WHEN 'USD' THEN 'US$'
                                 									 WHEN 'CLP' THEN '$'
                                 									 WHEN 'UF' THEN 'UF'
                                 									 WHEN ''  THEN '$'
                                 									 ELSE '$'
                                 END || ';' || --dummy, reajuste por compromiso
                                 '0' || ';' || --Monto total operacion, hay que ver de donde sale
								 'GESFFMM' || ';' ||
								 
								 '' AS "Registro"
						  FROM :p_out_Salida_FAC AS "Operaciones"
						  LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS "ConsultarPortafolio" ON  ( "ConsultarPortafolio"."IdPortafolio" = "Operaciones"."IdPortafolio" )
						  LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "ConsultarCliente" ON  ( "ConsultarCliente"."IdCliente" = "ConsultarPortafolio"."IdCliente" )
						  LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS "TipoOperacion" ON ("TipoOperacion"."IdTipoOperacion" = "Operaciones"."IdTipoOperacion")
						  LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento" ON ("Instrumento"."IdInstrumento" = "Operaciones"."IdInstrumento")
						  LEFT OUTER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSerieDeFondo" AS "SerieDeFondo" ON ("SerieDeFondo"."IdPortafolio" = "Operaciones"."IdPortafolio")
						  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS "CondicionDeLiquidacion" ON ("CondicionDeLiquidacion"."IdCondicionDeLiquidacion" = "Operaciones"."IdCondicionLiquidacion")
						  WHERE "Operaciones"."IdTipoComprobante" in ('FAE','FAC')
						  AND "TipoOperacion"."Accion" = 'VENTA'
						  AND "SerieDeFondo"."IdPortafolio" = :p_IdPortafolioAGF
						  UNION ALL
						  --Compras a termino fuera de rueda
						  SELECT top 100 COALESCE("Operaciones"."FolioTransaccion",'') || ';' ||
								 CASE "Operaciones"."FechaMovimiento" WHEN '' THEN '0001-01-01'
								                                      ELSE TO_NVARCHAR("Operaciones"."FechaMovimiento",'YYYY-MM-DD')
								 END ||	';' || 	
								 COALESCE("Operaciones"."IdMovimientoRelacionado",0) || ';' || --falta FolEve
								 COALESCE("SerieDeFondo"."IdFondo",'') || ';' ||
								 '' || ';' ||
								 COALESCE("ConsultarCliente"."DocumentoIdentidad",'') || ';' ||
								 COALESCE("ConsultarPortafolio"."CodigoPortafolio",'') || ';' ||
								 COALESCE("Operaciones"."MontoCompra",0) || ';' ||
								 COALESCE("Operaciones"."Precio",0) || ';' ||
								 COALESCE("Operaciones"."Cantidad",0) || ';' ||
								 COALESCE("Operaciones"."Tasa",0) || ';' ||
								 CASE "TipoOperacion"."Accion" WHEN 'COMPRA' THEN 'C'
															   ELSE ''
								 END || ';' || --TipoMovimiento
								 'CP' || ';' || --TipoOperacion
                                 COALESCE("Operaciones"."Nemotecnico",'') || ';' ||
                                 COALESCE("Operaciones"."IdEmisor",'') || ';' ||
                                 CASE "Instrumento"."IdTipoReajuste" WHEN 'USD' THEN 'US$'
                                 									 WHEN 'CLP' THEN '$'
                                 									 WHEN ''  THEN '$'
                                 									 ELSE '$'
                                 END || ';' ||
                                 COALESCE("Instrumento"."FechaVencimiento",'0001-01-01') || ';' ||
                                 COALESCE("Operaciones"."ValorRescateInicial",0) || ';' ||
                                 COALESCE("Operaciones"."IdBoveda",'') || ';' ||
                                 COALESCE("CondicionDeLiquidacion"."Codigo",'') || ';' ||
                                 CASE "Operaciones"."FechaVencimientoPacto" WHEN '0001-01-01' THEN ''
								                                            ELSE TO_NVARCHAR("Operaciones"."FechaMovimiento",'YYYY-MM-DD')
								 END || ';' || --AS "FechaVencimientoPacto"
                                 DAYS_BETWEEN (TO_NVARCHAR("Operaciones"."FechaMovimiento",'YYYY-MM-DD'),TO_NVARCHAR("Operaciones"."FechaVencimientoPacto",'YYYY-MM-DD')) || ';' || --AS "PlazoPacto"
                                 COALESCE("Operaciones"."TasaPlazo",0) || ';' || --AS "TasaCompromiso"
                                 CASE 'CLP'                          WHEN 'USD' THEN 'US$'
                                 									 WHEN 'CLP' THEN '$'
                                 									 WHEN 'UF' THEN 'UF'
                                 									 WHEN ''  THEN '$'
                                 									 ELSE '$'
                                 END || ';' || --dummy, reajuste por compromiso
                                 '0' || ';' || --Monto total operacion, hay que ver de donde sale
								 'GESFFMM' || ';' ||
								 
								 '' AS "Registro"
						  FROM :p_out_Salida_MRF AS "Operaciones"
						  LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS "ConsultarPortafolio" ON  ( "ConsultarPortafolio"."IdPortafolio" = "Operaciones"."IdPortafolio" )
						  LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "ConsultarCliente" ON  ( "ConsultarCliente"."IdCliente" = "ConsultarPortafolio"."IdCliente" )
						  LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS "TipoOperacion" ON ("TipoOperacion"."IdTipoOperacion" = "Operaciones"."IdTipoOperacion")
						  LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento" ON ("Instrumento"."IdInstrumento" = "Operaciones"."IdInstrumento")
						  LEFT OUTER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSerieDeFondo" AS "SerieDeFondo" ON ("SerieDeFondo"."IdPortafolio" = "Operaciones"."IdPortafolio")
						  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS "CondicionDeLiquidacion" ON ("CondicionDeLiquidacion"."IdCondicionDeLiquidacion" = "Operaciones"."IdCondicionLiquidacion")
						  WHERE "Operaciones"."IdTipoOperacion" = 'COMTER'
						  AND "SerieDeFondo"."IdPortafolio" = :p_IdPortafolioAGF
						  UNION ALL
						  --Ventas a termino fuera de rueda
						  SELECT top 100 COALESCE("Operaciones"."FolioTransaccion",'') || ';' ||
								 CASE "Operaciones"."FechaMovimiento" WHEN '' THEN '0001-01-01'
								                                      ELSE TO_NVARCHAR("Operaciones"."FechaMovimiento",'YYYY-MM-DD')
								 END ||	';' || 	
								 COALESCE("Operaciones"."IdMovimientoRelacionado",0) || ';' || --falta FolEve
								 COALESCE("SerieDeFondo"."IdFondo",'') || ';' ||
								 '' || ';' ||
								 COALESCE("ConsultarCliente"."DocumentoIdentidad",'') || ';' ||
								 COALESCE("ConsultarPortafolio"."CodigoPortafolio",'') || ';' ||
								 COALESCE("Operaciones"."MontoVenta",0) || ';' ||
								 COALESCE("Operaciones"."Precio",0) || ';' ||
								 COALESCE("Operaciones"."Cantidad",0) || ';' ||
								 COALESCE("Operaciones"."Tasa",0) || ';' ||
								 CASE "TipoOperacion"."Accion" WHEN 'VENTA' THEN 'V'
															   ELSE ''
								 END || ';' || --TipoMovimiento
								 'VP' || ';' || --TipoOperacion
                                 COALESCE("Operaciones"."Nemotecnico",'') || ';' ||
                                 COALESCE("Operaciones"."IdEmisor",'') || ';' ||
                                 CASE "Instrumento"."IdTipoReajuste" WHEN 'USD' THEN 'US$'
                                 									 WHEN 'CLP' THEN '$'
                                 									 WHEN ''  THEN '$'
                                 									 ELSE '$'
                                 END || ';' ||
                                 COALESCE("Instrumento"."FechaVencimiento",'0001-01-01') || ';' ||
                                 COALESCE("Operaciones"."ValorRescateInicial",0) || ';' ||
                                 COALESCE("Operaciones"."IdBoveda",'') || ';' ||
                                 COALESCE("CondicionDeLiquidacion"."Codigo",'') || ';' ||
                                 CASE "Operaciones"."FechaVencimientoPacto" WHEN '0001-01-01' THEN ''
								                                            ELSE TO_NVARCHAR("Operaciones"."FechaMovimiento",'YYYY-MM-DD')
								 END || ';' || --AS "FechaVencimientoPacto"
                                 DAYS_BETWEEN (TO_NVARCHAR("Operaciones"."FechaMovimiento",'YYYY-MM-DD'),TO_NVARCHAR("Operaciones"."FechaVencimientoPacto",'YYYY-MM-DD')) || ';' || --AS "PlazoPacto"
                                 COALESCE("Operaciones"."TasaPlazo",0) || ';' || --AS "TasaCompromiso"
                                 CASE 'CLP'                          WHEN 'USD' THEN 'US$'
                                 									 WHEN 'CLP' THEN '$'
                                 									 WHEN 'UF' THEN 'UF'
                                 									 WHEN ''  THEN '$'
                                 									 ELSE '$'
                                 END || ';' || --dummy, reajuste por compromiso
                                 '0' || ';' || --Monto total operacion, hay que ver de donde sale
								 'GESFFMM' || ';' ||
								 
								 '' AS "Registro"
						  FROM :p_out_Salida_MRF AS "Operaciones"
						  LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS "ConsultarPortafolio" ON  ( "ConsultarPortafolio"."IdPortafolio" = "Operaciones"."IdPortafolio" )
						  LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "ConsultarCliente" ON  ( "ConsultarCliente"."IdCliente" = "ConsultarPortafolio"."IdCliente" )
						  LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS "TipoOperacion" ON ("TipoOperacion"."IdTipoOperacion" = "Operaciones"."IdTipoOperacion")
						  LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento" ON ("Instrumento"."IdInstrumento" = "Operaciones"."IdInstrumento")
						  LEFT OUTER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSerieDeFondo" AS "SerieDeFondo" ON ("SerieDeFondo"."IdPortafolio" = "Operaciones"."IdPortafolio")
						  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS "CondicionDeLiquidacion" ON ("CondicionDeLiquidacion"."IdCondicionDeLiquidacion" = "Operaciones"."IdCondicionLiquidacion")
						  WHERE "Operaciones"."IdTipoOperacion" = 'VENTER'
						  AND "SerieDeFondo"."IdPortafolio" = :p_IdPortafolioAGF
						  UNION ALL
						  --Compras con pacto
						  SELECT top 100 COALESCE("Operaciones"."FolioTransaccion",'') || ';' ||
								 CASE "Operaciones"."FechaMovimiento" WHEN '' THEN '0001-01-01'
								                                      ELSE TO_NVARCHAR("Operaciones"."FechaMovimiento",'YYYY-MM-DD')
								 END ||	';' || 	
								 COALESCE("Operaciones"."IdMovimientoRelacionado",0) || ';' || --falta FolEve
								 COALESCE("SerieDeFondo"."IdFondo",'') || ';' ||
								 '' || ';' ||
								 COALESCE("ConsultarCliente"."DocumentoIdentidad",'') || ';' ||
								 COALESCE("ConsultarPortafolio"."CodigoPortafolio",'') || ';' ||
								 COALESCE("Operaciones"."MontoCompra",0) || ';' ||
								 COALESCE("Operaciones"."Precio",0) || ';' ||
								 COALESCE("Operaciones"."Cantidad",0) || ';' ||
								 COALESCE("Operaciones"."Tasa",0) || ';' ||
								 CASE "TipoOperacion"."Accion" WHEN 'COMPRA' THEN 'C'
															   ELSE ''
								 END || ';' || --TipoMovimiento
								 'CI' || ';' || --TipoOperacion
                                 COALESCE("Operaciones"."Nemotecnico",'') || ';' ||
                                 COALESCE("Operaciones"."IdEmisor",'') || ';' ||
                                 CASE "Instrumento"."IdTipoReajuste" WHEN 'USD' THEN 'US$'
                                 									 WHEN 'CLP' THEN '$'
                                 									 WHEN ''  THEN '$'
                                 									 ELSE '$'
                                 END || ';' ||
                                 COALESCE("Instrumento"."FechaVencimiento",'0001-01-01') || ';' ||
                                 COALESCE("Operaciones"."ValorRescateInicial",0) || ';' ||
                                 COALESCE("Operaciones"."IdBoveda",'') || ';' ||
                                 COALESCE("CondicionDeLiquidacion"."Codigo",'') || ';' ||
                                 '' || ';' || --AS "FechaVencimientoPacto"
                                 '0' || ';' || --AS "PlazoPacto"
                                 '0' || ';' || --AS "TasaCompromiso"
                                 CASE 'CLP'                          WHEN 'USD' THEN 'US$'
                                 									 WHEN 'CLP' THEN '$'
                                 									 WHEN 'UF' THEN 'UF'
                                 									 WHEN ''  THEN '$'
                                 									 ELSE '$'
                                 END || ';' || --dummy, reajuste por compromiso
                                 '0' || ';' || --Monto total operacion, hay que ver de donde sale
								 'GESFFMM' || ';' ||
								 
								 '' AS "Registro"
						  FROM :p_out_Salida_MRF AS "Operaciones"
						  LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS "ConsultarPortafolio" ON  ( "ConsultarPortafolio"."IdPortafolio" = "Operaciones"."IdPortafolio" )
						  LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "ConsultarCliente" ON  ( "ConsultarCliente"."IdCliente" = "ConsultarPortafolio"."IdCliente" )
						  LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS "TipoOperacion" ON ("TipoOperacion"."IdTipoOperacion" = "Operaciones"."IdTipoOperacion")
						  LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento" ON ("Instrumento"."IdInstrumento" = "Operaciones"."IdInstrumento")
						  LEFT OUTER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSerieDeFondo" AS "SerieDeFondo" ON ("SerieDeFondo"."IdPortafolio" = "Operaciones"."IdPortafolio")
						  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS "CondicionDeLiquidacion" ON ("CondicionDeLiquidacion"."IdCondicionDeLiquidacion" = "Operaciones"."IdCondicionLiquidacion")
						  WHERE "Operaciones"."IdTipoOperacion" = 'COMPAC'
						  AND "SerieDeFondo"."IdPortafolio" = :p_IdPortafolioAGF
						  UNION ALL
						  --Ventas con pacto
						  SELECT top 100 COALESCE("Operaciones"."FolioTransaccion",'') || ';' ||
								 CASE "Operaciones"."FechaMovimiento" WHEN '' THEN '0001-01-01'
								                                      ELSE TO_NVARCHAR("Operaciones"."FechaMovimiento",'YYYY-MM-DD')
								 END ||	';' || 	
								 COALESCE("Operaciones"."IdMovimientoRelacionado",0) || ';' || --falta FolEve
								 COALESCE("SerieDeFondo"."IdFondo",'') || ';' ||
								 '' || ';' ||
								 COALESCE("ConsultarCliente"."DocumentoIdentidad",'') || ';' ||
								 COALESCE("ConsultarPortafolio"."CodigoPortafolio",'') || ';' ||
								 COALESCE("Operaciones"."MontoVenta",0) || ';' ||
								 COALESCE("Operaciones"."Precio",0) || ';' ||
								 COALESCE("Operaciones"."Cantidad",0) || ';' ||
								 COALESCE("Operaciones"."Tasa",0) || ';' ||
								 CASE "TipoOperacion"."Accion" WHEN 'VENTA' THEN 'V'
															   ELSE ''
								 END || ';' || --TipoMovimiento
								 'VI' || ';' || --TipoOperacion
                                 COALESCE("Operaciones"."Nemotecnico",'') || ';' ||
                                 COALESCE("Operaciones"."IdEmisor",'') || ';' ||
                                 CASE "Instrumento"."IdTipoReajuste" WHEN 'USD' THEN 'US$'
                                 									 WHEN 'CLP' THEN '$'
                                 									 WHEN ''  THEN '$'
                                 									 ELSE '$'
                                 END || ';' ||
                                 COALESCE("Instrumento"."FechaVencimiento",'0001-01-01') || ';' ||
                                 COALESCE("Operaciones"."ValorRescateInicial",0) || ';' ||
                                 COALESCE("Operaciones"."IdBoveda",'') || ';' ||
                                 COALESCE("CondicionDeLiquidacion"."Codigo",'') || ';' ||
                                 '' || ';' || --AS "FechaVencimientoPacto"
                                 '0' || ';' || --AS "PlazoPacto"
                                 '0' || ';' || --AS "TasaCompromiso"
                                 CASE 'CLP'                          WHEN 'USD' THEN 'US$'
                                 									 WHEN 'CLP' THEN '$'
                                 									 WHEN 'UF' THEN 'UF'
                                 									 WHEN ''  THEN '$'
                                 									 ELSE '$'
                                 END || ';' || --dummy, reajuste por compromiso
                                 '0' || ';' || --Monto total operacion, hay que ver de donde sale
								 'GESFFMM' || ';' ||
								 
								 '' AS "Registro"
						  FROM :p_out_Salida_MRF AS "Operaciones"
						  LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS "ConsultarPortafolio" ON  ( "ConsultarPortafolio"."IdPortafolio" = "Operaciones"."IdPortafolio" )
						  LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "ConsultarCliente" ON  ( "ConsultarCliente"."IdCliente" = "ConsultarPortafolio"."IdCliente" )
						  LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS "TipoOperacion" ON ("TipoOperacion"."IdTipoOperacion" = "Operaciones"."IdTipoOperacion")
						  LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento" ON ("Instrumento"."IdInstrumento" = "Operaciones"."IdInstrumento")
						  LEFT OUTER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSerieDeFondo" AS "SerieDeFondo" ON ("SerieDeFondo"."IdPortafolio" = "Operaciones"."IdPortafolio")
						  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS "CondicionDeLiquidacion" ON ("CondicionDeLiquidacion"."IdCondicionDeLiquidacion" = "Operaciones"."IdCondicionLiquidacion")
						  WHERE "Operaciones"."IdTipoOperacion" = 'VENPAC'
						  AND "SerieDeFondo"."IdPortafolio" = :p_IdPortafolioAGF
						  UNION ALL
						  --Compra simultaneas en rueda
						  SELECT top 100 COALESCE("Operaciones"."FolioTransaccion",'') || ';' ||
								 CASE "Operaciones"."FechaMovimiento" WHEN '' THEN '0001-01-01'
								                                      ELSE TO_NVARCHAR("Operaciones"."FechaMovimiento",'YYYY-MM-DD')
								 END ||	';' || 	
								 COALESCE("Operaciones"."IdMovimientoRelacionado",0) || ';' || --falta FolEve
								 COALESCE("SerieDeFondo"."IdFondo",'') || ';' ||
								 '' || ';' ||
								 COALESCE("ConsultarCliente"."DocumentoIdentidad",'') || ';' ||
								 COALESCE("ConsultarPortafolio"."CodigoPortafolio",'') || ';' ||
								 COALESCE("Operaciones"."MontoCompra",0)|| ';' ||
								 COALESCE("Operaciones"."Precio",0) || ';' ||
								 COALESCE("Operaciones"."Cantidad",0) || ';' ||
								 COALESCE("Operaciones"."Tasa",0) || ';' ||
								 CASE "TipoOperacion"."Accion" WHEN 'COMPRA' THEN 'C'
															   ELSE ''
								 END || ';' || --TipoMovimiento
								 'CS' || ';' || --TipoOperacion
                                 COALESCE("Operaciones"."Nemotecnico",'') || ';' ||
                                 COALESCE("Operaciones"."IdEmisor",'') || ';' ||
                                 CASE "Instrumento"."IdTipoReajuste" WHEN 'USD' THEN 'US$'
                                 									 WHEN 'CLP' THEN '$'
                                 									 WHEN ''  THEN '$'
                                 									 ELSE '$'
                                 END || ';' ||
                                 COALESCE("Instrumento"."FechaVencimiento",'0001-01-01') || ';' ||
                                 COALESCE("Operaciones"."ValorRescateInicial",0) || ';' ||
                                 COALESCE("Operaciones"."IdBoveda",'') || ';' ||
                                 COALESCE("CondicionDeLiquidacion"."Codigo",'') || ';' ||
                                 '' || ';' || --AS "FechaVencimientoPacto"
                                 '0' || ';' || --AS "PlazoPacto"
                                 '0' || ';' || --AS "TasaCompromiso"
                                 CASE 'CLP'                          WHEN 'USD' THEN 'US$'
                                 									 WHEN 'CLP' THEN '$'
                                 									 WHEN 'UF' THEN 'UF'
                                 									 WHEN ''  THEN '$'
                                 									 ELSE '$'
                                 END || ';' || --dummy, reajuste por compromiso
                                 '0' || ';' || --Monto total operacion, hay que ver de donde sale
								 'GESFFMM' || ';' ||
								 
								 '' AS "Registro"
						  FROM :p_out_Salida_FAC AS "Operaciones"
						  LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS "ConsultarPortafolio" ON  ( "ConsultarPortafolio"."IdPortafolio" = "Operaciones"."IdPortafolio" )
						  LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "ConsultarCliente" ON  ( "ConsultarCliente"."IdCliente" = "ConsultarPortafolio"."IdCliente" )
						  LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS "TipoOperacion" ON ("TipoOperacion"."IdTipoOperacion" = "Operaciones"."IdTipoOperacion")
						  LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento" ON ("Instrumento"."IdInstrumento" = "Operaciones"."IdInstrumento")
						  LEFT OUTER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSerieDeFondo" AS "SerieDeFondo" ON ("SerieDeFondo"."IdPortafolio" = "Operaciones"."IdPortafolio")
						  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS "CondicionDeLiquidacion" ON ("CondicionDeLiquidacion"."IdCondicionDeLiquidacion" = "Operaciones"."IdCondicionLiquidacion")
						  WHERE "Operaciones"."IdTipoComprobante" IN ('FAC','BOE','FAE')
						  AND "Operaciones"."IdTipoOperacion" = 'COMSIM'
						  AND "SerieDeFondo"."IdPortafolio" = :p_IdPortafolioAGF
						  UNION ALL
						  --Venta simultaneas en rueda
						  SELECT top 100 COALESCE("Operaciones"."FolioTransaccion",'') || ';' ||
								 CASE "Operaciones"."FechaMovimiento" WHEN '' THEN '0001-01-01'
								                                      ELSE TO_NVARCHAR("Operaciones"."FechaMovimiento",'YYYY-MM-DD')
								 END ||	';' || 	
								 COALESCE("Operaciones"."IdMovimientoRelacionado",0) || ';' || --falta FolEve
								 COALESCE("SerieDeFondo"."IdFondo",'') || ';' ||
								 '' || ';' ||
								 COALESCE("ConsultarCliente"."DocumentoIdentidad",'') || ';' ||
								 COALESCE("ConsultarPortafolio"."CodigoPortafolio",'') || ';' ||
								 COALESCE("Operaciones"."MontoVenta",0)|| ';' ||
								 COALESCE("Operaciones"."Precio",0) || ';' ||
								 COALESCE("Operaciones"."Cantidad",0) || ';' ||
								 COALESCE("Operaciones"."Tasa",0) || ';' ||
								 CASE "TipoOperacion"."Accion" WHEN 'VENTA' THEN 'V'
															   ELSE ''
								 END || ';' || --TipoMovimiento
								 'VS' || ';' || --TipoOperacion
                                 COALESCE("Operaciones"."Nemotecnico",'') || ';' ||
                                 COALESCE("Operaciones"."IdEmisor",'') || ';' ||
                                 CASE "Instrumento"."IdTipoReajuste" WHEN 'USD' THEN 'US$'
                                 									 WHEN 'CLP' THEN '$'
                                 									 WHEN ''  THEN '$'
                                 									 ELSE '$'
                                 END || ';' ||
                                 COALESCE("Instrumento"."FechaVencimiento",'0001-01-01') || ';' ||
                                 COALESCE("Operaciones"."ValorRescateInicial",0) || ';' ||
                                 COALESCE("Operaciones"."IdBoveda",'') || ';' ||
                                 COALESCE("CondicionDeLiquidacion"."Codigo",'') || ';' ||
                                 '' || ';' || --AS "FechaVencimientoPacto"
                                 '0' || ';' || --AS "PlazoPacto"
                                 '0' || ';' || --AS "TasaCompromiso"
                                 CASE 'CLP'                          WHEN 'USD' THEN 'US$'
                                 									 WHEN 'CLP' THEN '$'
                                 									 WHEN 'UF' THEN 'UF'
                                 									 WHEN ''  THEN '$'
                                 									 ELSE '$'
                                 END || ';' || --dummy, reajuste por compromiso
                                 '0' || ';' || --Monto total operacion, hay que ver de donde sale
								 'GESFFMM' || ';' ||
								 
								 '' AS "Registro"
						  FROM :p_out_Salida_FAC AS "Operaciones"
						  LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS "ConsultarPortafolio" ON  ( "ConsultarPortafolio"."IdPortafolio" = "Operaciones"."IdPortafolio" )
						  LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "ConsultarCliente" ON  ( "ConsultarCliente"."IdCliente" = "ConsultarPortafolio"."IdCliente" )
						  LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS "TipoOperacion" ON ("TipoOperacion"."IdTipoOperacion" = "Operaciones"."IdTipoOperacion")
						  LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento" ON ("Instrumento"."IdInstrumento" = "Operaciones"."IdInstrumento")
						  LEFT OUTER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSerieDeFondo" AS "SerieDeFondo" ON ("SerieDeFondo"."IdPortafolio" = "Operaciones"."IdPortafolio")
						  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS "CondicionDeLiquidacion" ON ("CondicionDeLiquidacion"."IdCondicionDeLiquidacion" = "Operaciones"."IdCondicionLiquidacion")
						  WHERE "Operaciones"."IdTipoComprobante" IN ('FAC','BOE','FAE')
						  AND "Operaciones"."IdTipoOperacion" = 'VENSIM'
						  AND "SerieDeFondo"."IdPortafolio" = :p_IdPortafolioAGF
						  UNION ALL
						  --Nota de credito compra simultaneas
						  SELECT top 100 COALESCE("Operaciones"."FolioTransaccion",'') || ';' ||
								 CASE "Operaciones"."FechaMovimiento" WHEN '' THEN '0001-01-01'
								                                      ELSE TO_NVARCHAR("Operaciones"."FechaMovimiento",'YYYY-MM-DD')
								 END ||	';' || 	
								 COALESCE("Operaciones"."IdMovimientoRelacionado",0) || ';' || --falta FolEve
								 COALESCE("SerieDeFondo"."IdFondo",'') || ';' ||
								 '' || ';' ||
								 COALESCE("ConsultarCliente"."DocumentoIdentidad",'') || ';' ||
								 COALESCE("ConsultarPortafolio"."CodigoPortafolio",'') || ';' ||
								 COALESCE("Operaciones"."MontoCompra",0)|| ';' ||
								 COALESCE("Operaciones"."Precio",0) || ';' ||
								 COALESCE("Operaciones"."Cantidad",0) || ';' ||
								 COALESCE("Operaciones"."Tasa",0) || ';' ||
								 CASE "TipoOperacion"."Accion" WHEN 'COMPRA' THEN 'C'
															   ELSE ''
								 END || ';' || --TipoMovimiento
								 'PC' || ';' || --TipoOperacion
                                 COALESCE("Operaciones"."Nemotecnico",'') || ';' ||
                                 COALESCE("Operaciones"."IdEmisor",'') || ';' ||
                                 CASE "Instrumento"."IdTipoReajuste" WHEN 'USD' THEN 'US$'
                                 									 WHEN 'CLP' THEN '$'
                                 									 WHEN ''  THEN '$'
                                 									 ELSE '$'
                                 END || ';' ||
                                 COALESCE("Instrumento"."FechaVencimiento",'0001-01-01') || ';' ||
                                 COALESCE("Operaciones"."ValorRescateInicial",0) || ';' ||
                                 COALESCE("Operaciones"."IdBoveda",'') || ';' ||
                                 COALESCE("CondicionDeLiquidacion"."Codigo",'') || ';' ||
                                 '' || ';' || --AS "FechaVencimientoPacto"
                                 '0' || ';' || --AS "PlazoPacto"
                                 '0' || ';' || --AS "TasaCompromiso"
                                 CASE 'CLP'                          WHEN 'USD' THEN 'US$'
                                 									 WHEN 'CLP' THEN '$'
                                 									 WHEN 'UF' THEN 'UF'
                                 									 WHEN ''  THEN '$'
                                 									 ELSE '$'
                                 END || ';' || --dummy, reajuste por compromiso
                                 '0' || ';' || --Monto total operacion, hay que ver de donde sale
								 'GESFFMM' || ';' ||
								 
								 '' AS "Registro"
						  FROM :p_out_Salida_FAC AS "Operaciones"
						  LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS "ConsultarPortafolio" ON  ( "ConsultarPortafolio"."IdPortafolio" = "Operaciones"."IdPortafolio" )
						  LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "ConsultarCliente" ON  ( "ConsultarCliente"."IdCliente" = "ConsultarPortafolio"."IdCliente" )
						  LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS "TipoOperacion" ON ("TipoOperacion"."IdTipoOperacion" = "Operaciones"."IdTipoOperacion")
						  LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento" ON ("Instrumento"."IdInstrumento" = "Operaciones"."IdInstrumento")
						  LEFT OUTER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSerieDeFondo" AS "SerieDeFondo" ON ("SerieDeFondo"."IdPortafolio" = "Operaciones"."IdPortafolio")
						  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS "CondicionDeLiquidacion" ON ("CondicionDeLiquidacion"."IdCondicionDeLiquidacion" = "Operaciones"."IdCondicionLiquidacion")
						  WHERE "Operaciones"."IdTipoComprobante" IN ('NCE','NCR')
						  AND "Operaciones"."IdTipoOperacion" = 'COMSIM'
						  AND "SerieDeFondo"."IdPortafolio" = :p_IdPortafolioAGF
						  UNION ALL
						  --Nota de credito venta simultaneas
						  SELECT top 100 COALESCE("Operaciones"."FolioTransaccion",'') || ';' ||
								 CASE "Operaciones"."FechaMovimiento" WHEN '' THEN '0001-01-01'
								                                      ELSE TO_NVARCHAR("Operaciones"."FechaMovimiento",'YYYY-MM-DD')
								 END ||	';' || 	
								 COALESCE("Operaciones"."IdMovimientoRelacionado",0) || ';' || --falta FolEve
								 COALESCE("SerieDeFondo"."IdFondo",'') || ';' ||
								 '' || ';' ||
								 COALESCE("ConsultarCliente"."DocumentoIdentidad",'') || ';' ||
								 COALESCE("ConsultarPortafolio"."CodigoPortafolio",'') || ';' ||
								 COALESCE("Operaciones"."MontoVenta",0)|| ';' ||
								 COALESCE("Operaciones"."Precio",0) || ';' ||
								 COALESCE("Operaciones"."Cantidad",0) || ';' ||
								 COALESCE("Operaciones"."Tasa",0) || ';' ||
								 CASE "TipoOperacion"."Accion" WHEN 'VENTA' THEN 'C'
															   ELSE ''
								 END || ';' || --TipoMovimiento
								 'PV' || ';' || --TipoOperacion
                                 COALESCE("Operaciones"."Nemotecnico",'') || ';' ||
                                 COALESCE("Operaciones"."IdEmisor",'') || ';' ||
                                 CASE "Instrumento"."IdTipoReajuste" WHEN 'USD' THEN 'US$'
                                 									 WHEN 'CLP' THEN '$'
                                 									 WHEN ''  THEN '$'
                                 									 ELSE '$'
                                 END || ';' ||
                                 COALESCE("Instrumento"."FechaVencimiento",'0001-01-01') || ';' ||
                                 COALESCE("Operaciones"."ValorRescateInicial",0) || ';' ||
                                 COALESCE("Operaciones"."IdBoveda",'') || ';' ||
                                 COALESCE("CondicionDeLiquidacion"."Codigo",'') || ';' ||
                                 '' || ';' || --AS "FechaVencimientoPacto"
                                 '0' || ';' || --AS "PlazoPacto"
                                 '0' || ';' || --AS "TasaCompromiso"
                                 CASE 'CLP'                          WHEN 'USD' THEN 'US$'
                                 									 WHEN 'CLP' THEN '$'
                                 									 WHEN 'UF' THEN 'UF'
                                 									 WHEN ''  THEN '$'
                                 									 ELSE '$'
                                 END || ';' || --dummy, reajuste por compromiso
                                 '0' || ';' || --Monto total operacion, hay que ver de donde sale
								 'GESFFMM' || ';' ||
								 
								 '' AS "Registro"
						  FROM :p_out_Salida_FAC AS "Operaciones"
						  LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS "ConsultarPortafolio" ON  ( "ConsultarPortafolio"."IdPortafolio" = "Operaciones"."IdPortafolio" )
						  LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "ConsultarCliente" ON  ( "ConsultarCliente"."IdCliente" = "ConsultarPortafolio"."IdCliente" )
						  LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS "TipoOperacion" ON ("TipoOperacion"."IdTipoOperacion" = "Operaciones"."IdTipoOperacion")
						  LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento" ON ("Instrumento"."IdInstrumento" = "Operaciones"."IdInstrumento")
						  LEFT OUTER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSerieDeFondo" AS "SerieDeFondo" ON ("SerieDeFondo"."IdPortafolio" = "Operaciones"."IdPortafolio")
						  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS "CondicionDeLiquidacion" ON ("CondicionDeLiquidacion"."IdCondicionDeLiquidacion" = "Operaciones"."IdCondicionLiquidacion")
						  WHERE "Operaciones"."IdTipoComprobante" IN ('NCE','NCR')
						  AND "Operaciones"."IdTipoOperacion" = 'VENSIM'
						  AND "SerieDeFondo"."IdPortafolio" = :p_IdPortafolioAGF;
		SIGNAL SALIDA_CONTROLADA;
		
		END;
	END;
END;
							   
