SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.CuentaInversion.BD.Reglas::ServicioCumpleRequisitoCrearCargoAbono";

CREATE PROCEDURE
-- 
--  Proposito: Validar si se puede crear el movimiento de cargo o abono
--  Autor: NRM - BCS 
--  Fecha Creacion: 07/01/2016
-- 
-- 
"BSCL.CuentaInversion.BD.Reglas::ServicioCumpleRequisitoCrearCargoAbono" (
	IN p_IdEmpresa				BIGINT,	
    IN p_IdModulo				NVARCHAR(3),
    IN p_FechaSistema           NVARCHAR(40),
    IN p_EsIngresoManual		NVARCHAR(1)
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
AS
BEGIN
	DECLARE v_Count				BIGINT;
	DECLARE v_ErroresContador	BIGINT;
	DECLARE v_FechaDesde		NVARCHAR(10);
	DECLARE v_FechaHasta		NVARCHAR(10);	
	DECLARE v_Validaciones_V1   TABLE  ("IdInterno" 								BIGINT,
									   	"CUENTA_INVERSION_INVALIDA"					NVARCHAR(4),
									   	"CUENTA_INVERSION_TRASPASO_INVALIDA"		NVARCHAR(4),
									   	"CUENTA_INVERSION_NO_EXISTE"				NVARCHAR(4),
									   	"CUENTA_INVERSION_NO_ACTIVA"				NVARCHAR(4),
									   	"TIPO_MOVIMIENTO_INVALIDO"					NVARCHAR(4),
									   	"CONCEPTO_CUENTA_INVERSION_INVALIDO"		NVARCHAR(4),
									   	"FECHA_LIQUIDACION_MENOR_FECHA_MOVIMIENTO"	NVARCHAR(4),
									   	"MONTO_MENOR_IGUAL_A_CERO"					NVARCHAR(4),
									   	"FECHA_LIQUIDACION_INVALIDA"				NVARCHAR(4),
									   	"FECHA_OPERACION_ORIGINAL_INVALIDO"			NVARCHAR(4),
									   	"FECHA_LIQUIDACION_ORIGINAL_INVALIDO"		NVARCHAR(4),
									   	"FECHA_VENCIMIENTO_ORIGINAL_INVALIDO"		NVARCHAR(4),
									   	"FECHA_COMPROBANTE_INVALIDA"				NVARCHAR(4),
									   	"SECUENCIA_O_FOLIO_COMPROBANTE_VACIO"		NVARCHAR(4),
									   	"FOLIO_OPERACION_VACIO"						NVARCHAR(4),
									   	"TIPO_MOVIMIENTO_NO_EXISTE"					NVARCHAR(4),
									   	"CONCEPTO_CUENTA_INVERSION_NO_EXISTE"		NVARCHAR(4),
									   	"MONEDA_NO_EXISTE"							NVARCHAR(4),
									   	"MONEDA_NO_ACTIVA"							NVARCHAR(4),
									   	"TIPO_OPERACION_NO_EXISTE"					NVARCHAR(4),
									   	"PRODUCTO_NO_EXISTE"						NVARCHAR(4),
									   	"TIPO_INSTRUMENTO_NO_EXISTE"				NVARCHAR(4));
	
	DECLARE v_Validaciones_V2  TABLE  ("IdInterno" 									BIGINT,
									   "ERROR_CONSULTA_FLUJO_CAJA"					NVARCHAR(4),
									   "SALDO_CAJA_INSUFICIENTE"					NVARCHAR(4),
									   "CLIENTE_NO_OPERA_CON_LINEA_SOBREGIRO"		NVARCHAR(4));
	
	DECLARE v_Validaciones_V3  TABLE  ("IdInterno" 									BIGINT,
									   "CLIENTE_SIN_CONTRATO_MERCANTIL"				NVARCHAR(4));
									   
	DECLARE v_PortafolioAConsultar	TABLE ("IdPortafolio"							BIGINT,
											"IdMoneda"								NVARCHAR(3));
	
	DECLARE v_Resultado				TABLE ("IdInterno"								BIGINT,
										   "Codigo"									NVARCHAR(40),
										   "Resultado"								NVARCHAR(4),
										   "CodigoDato1"							NVARCHAR(40),
										   "ValorDato1"								NVARCHAR(520),
										   "CodigoDato2"							NVARCHAR(40),
										   "ValorDato2"								NVARCHAR(520));
	
	DELETE FROM "BSCL.CuentaInversion.BD::CTA.ServicioCumpleRequisitoCrearCargoAbono_OUT_Resultado";
										   
	v_Validaciones_V2 = SELECT 0 as "IdInterno",'' as "ERROR_CONSULTA_FLUJO_CAJA",'' as "SALDO_CAJA_INSUFICIENTE",'' as "CLIENTE_NO_OPERA_CON_LINEA_SOBREGIRO"
						  FROM DUMMY WHERE 1=2; 
	
	v_MatrizValidaciones_v1 = 	
	SELECT   CAG."IdInterno"
			,CAG."IdPortafolio" AS "IdPortafolioInformado" 
			,CAG."IdMoneda" AS "IdMonedaInformado"
			,CAG."IdTipoMovimiento" AS "IdTipoMovimientoInformado"
			,CAG."IdConcepto" AS "IdConceptoInformado"
			,CAG."Descripcion"
			,CAG."IdCondicionDeLiquidacion"
			,CAG."FechaCondicionDeLiquidacion"
			,CAG."Monto"
			,CAG."IdEjecutivo"
 			,CAG."IdSucursal"
			,CAG."IdTipoProducto" AS "IdTipoProductoInformado"
			,CAG."IdTipoOperacion" AS "IdTipoOperacionInformado"
			,CAG."IdOperacion"
			,CAG."FechaOperacion"
			,CAG."FechaLiquidacionOperacion"
			,CAG."FechaVencimientoOperacion"
			,CAG."IdMonedaOperacion"
			,CAG."MontoOperacion"
			,CAG."TipoCambioOperacion"
			,CAG."IdContraparte"
			,CAG."IdTipoComprobante"
			,CAG."IdComprobante"
			,CAG."FolioComprobante"
			,CAG."IdComprobanteDetalle"
			,CAG."IdComprobanteFormaDePago"
			,CAG."IdTipoMovimientoAsociado"
 			,CAG."IdMovimientoAsociado"
 			,CAG."FechaComprobante"
			,CAG."IdPersonaPagoCobro"
			,CAG."IdInstrumento"
			,CAG."IdTipoInstrumento" AS "IdTipoInstrumentoInformado"
			,CAG."Nemo"
			,CAG."EsIndicadorBilateral"
			,CAG."EsCargoAbonoPorTraspaso"
			,CAG."IdPortafolioTraspaso"
			,CAG."GeneroContraparteEnTesoreria"
			,CAG."CertificadoAutomatico"
			,CI."IdCuentaInversion"
			,CI."IdPortafolio"
			,CI."IdMoneda"
			,CI."IdEstado"			
			,CASE WHEN CAG."EsCargoAbonoPorTraspaso" = 'S' AND CI."IdPortafolio" <> CAG."IdPortafolio" AND TMV."IdTipoMovimiento" = 'MOVCAR' THEN 'MOVABN'
				  WHEN CAG."EsCargoAbonoPorTraspaso" = 'S' AND CI."IdPortafolio" <> CAG."IdPortafolio" AND TMV."IdTipoMovimiento" = 'MOVABN' THEN 'MOVCAR'
				  ELSE TMV."IdTipoMovimiento"
			  END AS "IdTipoMovimiento"			  			
			,CCP."IdConcepto"
			,MON."IdMoneda" AS "IdMonedaTesoreria"
			,MON."IdEstadoMonedaTesoreria"			
			,TOper."IdTipoOperacion"
			,Tprod."IdTipoProducto"
			,TIns."IdTipoInstrumento"
			,IndPor."OperaLineaSobregiro"
			
			,DIDE."DocumentoIdentidad"	
			,CASE "PER"."Tipo" 
				WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"			
				ELSE PER."RazonSocial"
			END  as "NombreCompleto"
	
			,1 as "CUENTA_INVERSION_INVALIDA"
			,1 as "CUENTA_INVERSION_TRASPASO_INVALIDA"
			,1 as "CUENTA_INVERSION_NO_EXISTE"
			,1 as "CUENTA_INVERSION_NO_ACTIVA"						
			,1 as "TIPO_MOVIMIENTO_INVALIDO"
		    ,1 as "CONCEPTO_CUENTA_INVERSION_INVALIDO"
			,(CASE  WHEN (:p_IdModulo = 'FMM') THEN 0 ELSE 1 END) As "FECHA_LIQUIDACION_MENOR_FECHA_MOVIMIENTO" -- FMM envía movimientos 1 día después ya que debe esperar valor cuota de pauta
		    ,1 as "MONTO_MENOR_IGUAL_A_CERO"
		    ,1 as "FECHA_LIQUIDACION_INVALIDA"
		    ,1 as "FECHA_OPERACION_ORIGINAL_INVALIDO"
		    ,1 as "FECHA_LIQUIDACION_ORIGINAL_INVALIDO"
		    ,1 as "FECHA_VENCIMIENTO_ORIGINAL_INVALIDO"
		    ,1 as "FECHA_COMPROBANTE_INVALIDA"
		    ,1 as "SECUENCIA_O_FOLIO_COMPROBANTE_VACIO"
		    ,1 as "FOLIO_OPERACION_VACIO"		    
		    ,1 as "TIPO_MOVIMIENTO_NO_EXISTE"
		    ,1 as "CONCEPTO_CUENTA_INVERSION_NO_EXISTE"		    
		    ,1 as "MONEDA_NO_EXISTE"
			,1 as "MONEDA_NO_ACTIVA"			
			,1 as "TIPO_OPERACION_NO_EXISTE"
			,1 as "PRODUCTO_NO_EXISTE"
			,1 as "TIPO_INSTRUMENTO_NO_EXISTE"
			,CASE WHEN (:p_EsIngresoManual = 'S' OR :p_IdModulo = 'TES') THEN 1 ELSE 0 END "CLIENTE_NO_OPERA_CON_LINEA_SOBREGIRO"
			,CASE WHEN :p_EsIngresoManual = 'S' THEN 1 ELSE 0 END "SALDO_CAJA_INSUFICIENTE"
			,CASE WHEN :p_EsIngresoManual = 'S' THEN 1 ELSE 0 END "ERROR_CONSULTA_FLUJO_CAJA"
	  FROM "BSCL.CuentaInversion.BD::CTA.ServicioCumpleRequisitoCrearCargoAbono_IN_CargoAbonoAGenerar" AS CAG
	  LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CI 
	  		 ON CI."IdMoneda" = CAG."IdMoneda" 
	  		AND CI."IdPortafolio" IN (CAG."IdPortafolio",CAG."IdPortafolioTraspaso")	  
	  LEFT JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarIndicadoresPortafolioCuentaInversion" AS IndPor 
	  		 ON IndPor."IdPortafolio" IN (CASE WHEN CAG."IdTipoMovimiento" = 'MOVCAR' THEN CAG."IdPortafolio"
								 	           WHEN CAG."IdTipoMovimiento" = 'MOVABN' AND CAG."EsCargoAbonoPorTraspaso" = 'S' THEN CAG."IdPortafolioTraspaso"
                                           END)	  
	  LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoMovimiento" as TMV 
	  		 ON TMV."IdModulo" = 'CTA'
	  		AND TMV."IdTipoMovimiento" = CAG."IdTipoMovimiento"
	  LEFT JOIN "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion" as CCP ON CCP."IdConcepto" = CAG."IdConcepto"
	  LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarMonedaTesoreria" as MON ON MON."IdMoneda" = CAG."IdMoneda"	  
	  LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TOper ON TOper."IdTipoOperacion" = CAG."IdTipoOperacion"
	  LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto" as Tprod ON Tprod."IdTipoProducto" = CAG."IdTipoProducto"
	  LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TIns ON TIns."IdTipoInstrumento" = CAG."IdTipoInstrumento"
	  
	  
	  LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdCliente"=CI."IdCliente")
	  LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = CLI."IdPersona")
      LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DIDE ON (DIDE."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad");
	

	v_Validaciones_V1 =
	 SELECT  "IdInterno"			
			,CASE WHEN "CUENTA_INVERSION_INVALIDA" = 1 AND COALESCE("IdPortafolioInformado",0) = 0 OR COALESCE("IdMonedaInformado",'') = '' THEN 'RECH' ELSE 'OK' END AS "CUENTA_INVERSION_INVALIDA"
			,CASE WHEN "CUENTA_INVERSION_TRASPASO_INVALIDA" = 1 AND "EsCargoAbonoPorTraspaso" = 'S' AND COALESCE("IdPortafolioTraspaso",0) = 0 THEN 'RECH' ELSE 'OK' END AS "CUENTA_INVERSION_TRASPASO_INVALIDA"
			,CASE WHEN "CUENTA_INVERSION_NO_EXISTE" = 1 AND COALESCE("IdCuentaInversion",0) = 0 THEN 'RECH' ELSE 'OK' END AS "CUENTA_INVERSION_NO_EXISTE"
			,CASE WHEN "CUENTA_INVERSION_NO_ACTIVA" = 1 AND "IdEstado" = 'INA' THEN 'RECH' ELSE 'OK' END AS "CUENTA_INVERSION_NO_ACTIVA"						
			,CASE WHEN "TIPO_MOVIMIENTO_INVALIDO" = 1 AND LENGTH(COALESCE("IdTipoMovimientoInformado",'')) = 0 THEN 'RECH' ELSE 'OK' END AS "TIPO_MOVIMIENTO_INVALIDO"
		    ,CASE WHEN "CONCEPTO_CUENTA_INVERSION_INVALIDO" = 1 AND LENGTH(COALESCE("IdConceptoInformado",'')) = 0 THEN 'RECH' ELSE 'OK' END AS "CONCEPTO_CUENTA_INVERSION_INVALIDO"
		    ,CASE WHEN "FECHA_LIQUIDACION_MENOR_FECHA_MOVIMIENTO" = 1 AND TO_DATE("FechaCondicionDeLiquidacion",'YYYY-MM-DD') < TO_DATE(:p_FechaSistema,'YYYY-MM-DD') THEN 'RECH' ELSE 'OK' END AS "FECHA_LIQUIDACION_MENOR_FECHA_MOVIMIENTO"
		    ,CASE WHEN "MONTO_MENOR_IGUAL_A_CERO" = 1 AND ("Monto" < 0) THEN 'RECH' ELSE 'OK' END AS "MONTO_MENOR_IGUAL_A_CERO"
		    ,CASE WHEN "FECHA_LIQUIDACION_INVALIDA" = 1 AND ("BSCL.Plataforma.BD.Utilitarios::EsFecha" ("FechaCondicionDeLiquidacion") = 'N') OR ("FechaCondicionDeLiquidacion" = '0001-01-01') THEN 'RECH' ELSE 'OK' END AS "FECHA_LIQUIDACION_INVALIDA"
		    ,CASE WHEN "FECHA_OPERACION_ORIGINAL_INVALIDO" = 1 AND ("BSCL.Plataforma.BD.Utilitarios::EsFecha" (SUBSTRING ("FechaOperacion",1,10)) = 'N') THEN 'RECH' ELSE 'OK' END AS "FECHA_OPERACION_ORIGINAL_INVALIDO"
		    ,CASE WHEN "FECHA_LIQUIDACION_ORIGINAL_INVALIDO" = 1 AND ("BSCL.Plataforma.BD.Utilitarios::EsFecha" (SUBSTRING ("FechaLiquidacionOperacion",1,10)) = 'N') THEN 'RECH' ELSE 'OK' END AS "FECHA_LIQUIDACION_ORIGINAL_INVALIDO"
		    ,CASE WHEN "FECHA_VENCIMIENTO_ORIGINAL_INVALIDO" = 1 AND ("BSCL.Plataforma.BD.Utilitarios::EsFecha" (SUBSTRING ("FechaVencimientoOperacion",1,10)) = 'N') THEN 'RECH' ELSE 'OK' END AS "FECHA_VENCIMIENTO_ORIGINAL_INVALIDO"
		    ,CASE WHEN "FECHA_COMPROBANTE_INVALIDA" = 1 AND ("BSCL.Plataforma.BD.Utilitarios::EsFecha" (SUBSTRING ("FechaComprobante",1,10)) = 'N') THEN 'RECH' ELSE 'OK' END AS "FECHA_COMPROBANTE_INVALIDA"
		    ,CASE WHEN "SECUENCIA_O_FOLIO_COMPROBANTE_VACIO" = 1 AND LENGTH(COALESCE("IdTipoComprobante",'')) > 0 AND (COALESCE("IdComprobante",0) = 0 AND COALESCE("FolioComprobante",'') = '') THEN 'RECH' ELSE 'OK' END AS "SECUENCIA_O_FOLIO_COMPROBANTE_VACIO"
		    ,CASE WHEN "FOLIO_OPERACION_VACIO" = 1 AND LENGTH(COALESCE("IdTipoOperacion",'')) > 0 AND COALESCE("IdOperacion",0) = 0 THEN 'RECH' ELSE 'OK' END AS "FOLIO_OPERACION_VACIO"
			,CASE WHEN "TIPO_MOVIMIENTO_NO_EXISTE" = 1 AND COALESCE("IdTipoMovimiento",'') = '' THEN 'RECH' ELSE 'OK' END AS "TIPO_MOVIMIENTO_NO_EXISTE"
		    ,CASE WHEN "CONCEPTO_CUENTA_INVERSION_NO_EXISTE" = 1 AND COALESCE("IdConcepto",'') = '' THEN 'RECH' ELSE 'OK' END AS "CONCEPTO_CUENTA_INVERSION_NO_EXISTE"						
			,CASE WHEN "MONEDA_NO_EXISTE" = 1 AND COALESCE("IdMonedaTesoreria",'') = '' THEN 'RECH' ELSE 'OK' END AS "MONEDA_NO_EXISTE"
			,CASE WHEN "MONEDA_NO_ACTIVA" = 1 AND "IdEstadoMonedaTesoreria" = 'INA' THEN 'RECH' ELSE 'OK' END AS "MONEDA_NO_ACTIVA"			
			,CASE WHEN "TIPO_OPERACION_NO_EXISTE" = 1 AND LENGTH(COALESCE("IdTipoOperacionInformado",'')) > 0 AND LENGTH(COALESCE("IdTipoOperacion",'')) = 0 THEN 'RECH' ELSE 'OK' END AS "TIPO_OPERACION_NO_EXISTE"
			,CASE WHEN "PRODUCTO_NO_EXISTE" = 1 AND LENGTH(COALESCE("IdTipoProductoInformado",'')) > 0 AND LENGTH(COALESCE("IdTipoProducto",'')) = 0 THEN 'RECH' ELSE 'OK' END AS "PRODUCTO_NO_EXISTE"
			,CASE WHEN "TIPO_INSTRUMENTO_NO_EXISTE" = 1 AND LENGTH(COALESCE("IdTipoInstrumentoInformado",'')) > 0 AND LENGTH(COALESCE("IdTipoInstrumento",'')) = 0  THEN 'RECH' ELSE 'OK' END AS "TIPO_INSTRUMENTO_NO_EXISTE"
			--,CASE WHEN "CLIENTE_SIN_CONTRATO_MERCANTIL" = 1 AND "ContratoMercantil" = 'N' THEN 'RECH' ELSE 'OK' END AS "CLIENTE_SIN_CONTRATO_MERCANTIL"
	  FROM :v_MatrizValidaciones_v1;
	  

	IF (:p_EsIngresoManual = 'S' OR :p_IdModulo = 'TES') THEN  
		v_PortafolioAConsultar = SELECT DISTINCT "IdPortafolio","IdMoneda" FROM :v_MatrizValidaciones_v1 WHERE "IdTipoMovimiento" = 'MOVCAR';		
		SELECT MIN("FechaCondicionDeLiquidacion") INTO v_FechaDesde FROM :v_MatrizValidaciones_v1 WHERE "IdTipoMovimiento" = 'MOVCAR';
		SELECT MAX("FechaCondicionDeLiquidacion") INTO v_FechaHasta FROM :v_MatrizValidaciones_v1 WHERE "IdTipoMovimiento" = 'MOVCAR';
		SELECT COUNT(DISTINCT "IdPortafolio") INTO v_Count FROM :v_PortafolioAConsultar;
		
		IF :v_Count > 0 THEN 					
			CALL "BSCL.Plataforma.BD.Servicios::SaldoFlujoCaja" (
			     p_IdEmpresa				=> p_IdEmpresa				    
				,p_FechaDesde				=> :v_FechaDesde
			    ,p_FechaHasta				=> :v_FechaHasta
			    ,p_uso						=> 'CUENTA_INVERSION'
			    ,p_PortafolioAConsultar		=> :v_PortafolioAConsultar
			    ,p_out_FlujoCaja			=> :v_FlujoCaja
			    ,p_out_FlujoCajaDetallado	=> :v_FlujoCajaDetallado
				,p_out_ErroresContador		=> :v_ErroresContador
				,p_out_ErroresDetalle		=> :v_ErroresDetalle
			);
			
			v_Validaciones_V2 =
			SELECT "IdInterno"
				   ,CASE WHEN "ERROR_CONSULTA_FLUJO_CAJA" = 1 AND :v_ErroresContador > 0 THEN 'RECH' ELSE 'OK' END AS "ERROR_CONSULTA_FLUJO_CAJA"
			       ,CASE WHEN "SALDO_CAJA_INSUFICIENTE" = 1 AND :v_ErroresContador = 0 AND "SaldoFinal" < 0 AND "OperaLineaSobregiro" = 'N' THEN 'RECH' ELSE 'OK' END AS "SALDO_CAJA_INSUFICIENTE"
			       ,CASE WHEN "CLIENTE_NO_OPERA_CON_LINEA_SOBREGIRO" = 1 AND :v_ErroresContador = 0 AND "SaldoFinal" < 0 AND "OperaLineaSobregiro" = 'N' THEN 'RECH' ELSE 'OK' END AS "CLIENTE_NO_OPERA_CON_LINEA_SOBREGIRO"			       
			  FROM (SELECT  POR."IdInterno"
						   ,POR."OperaLineaSobregiro"
						   ,POR."CLIENTE_NO_OPERA_CON_LINEA_SOBREGIRO"
						   ,POR."SALDO_CAJA_INSUFICIENTE"
						   ,POR."ERROR_CONSULTA_FLUJO_CAJA"
						   ,FC."Saldo" - SUM(POR."Monto") over (partition by POR."IdPortafolio",POR."IdMoneda" order by POR."IdInterno" asc 
									 	                     		 rows between unbounded preceding and current row) as "SaldoFinal"
					  FROM :v_FlujoCaja as FC
					 INNER JOIN :v_MatrizValidaciones_v1 as POR 
					     	 ON POR."IdPortafolio" 					= FC."IdPortafolio" 
					     	AND POR."IdMoneda" 						= FC."IdMoneda"
							AND POR."FechaCondicionDeLiquidacion"  	= FC."FechaSaldo"
				   ) as SF;			
		END IF;
	END IF;
	
	
	v_MatrizValidaciones_v2 = 
	SELECT  CAG."IdInterno"
	 	   ,IndCli."ContratoMercantil"
	 	   ,IndCli."RQ_ContratoMercantil"
		   ,CASE WHEN :p_EsIngresoManual = 'S' THEN 1 ELSE 0 END "CLIENTE_SIN_CONTRATO_MERCANTIL"
	  FROM "BSCL.CuentaInversion.BD::CTA.ServicioCumpleRequisitoCrearCargoAbono_IN_CargoAbonoAGenerar" AS CAG
	  LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CI 
	  		 ON CI."IdMoneda" 	  = CAG."IdMoneda" 
	  		AND CI."IdPortafolio" IN (CAG."IdPortafolio",CAG."IdPortafolioTraspaso")
	  LEFT JOIN ( SELECT IndCli."IdCliente", IndCli."ContratoMercantil",ROper."RQ_ContratoMercantil"
				    FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarRequisitosOperativosCuentaInversion" as ROper,
  	   					  "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresClientePivot" as IndCli
	  			)IndCli ON IndCli."IdCliente" = CI."IdCliente";
	  
	
	v_Validaciones_V3 =
	 SELECT  "IdInterno"			
			,CASE WHEN "CLIENTE_SIN_CONTRATO_MERCANTIL" = 1 AND "ContratoMercantil" = 'N' THEN 
					(CASE WHEN "RQ_ContratoMercantil" = 'AUTORIZACION' THEN 'RAUT'
						  WHEN "RQ_ContratoMercantil" = 'ADVERTENCIA' THEN 'ADVE'
						  WHEN "RQ_ContratoMercantil" = 'RECHAZO' THEN 'RECH'
						  ELSE 'OK'
					  END )
			  END AS "CLIENTE_SIN_CONTRATO_MERCANTIL"
	  FROM :v_MatrizValidaciones_v2;
	
	
	------RESULTADO	
	v_Resultado = 
	 SELECT "IdInterno","Codigo","Resultado",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2"
	   FROM (
			 SELECT "IdInterno",
			 		COALESCE(			 			
			 			 "1" ||'CUENTA_INVERSION_INVALIDA'
						,"2" ||'CUENTA_INVERSION_TRASPASO_INVALIDA'
						,"3" ||'CUENTA_INVERSION_NO_EXISTE'
						,"4" ||'CUENTA_INVERSION_NO_ACTIVA'
						,"5" ||'TIPO_MOVIMIENTO_INVALIDO'
					    ,"6" ||'CONCEPTO_CUENTA_INVERSION_INVALIDO'
					    ,"7" ||'FECHA_LIQUIDACION_MENOR_FECHA_MOVIMIENTO'
					    ,"8" ||'MONTO_MENOR_IGUAL_A_CERO'
					    ,"9" ||'FECHA_LIQUIDACION_INVALIDA'
					    ,"10" ||'FECHA_OPERACION_ORIGINAL_INVALIDO'
					    ,"11" ||'FECHA_LIQUIDACION_ORIGINAL_INVALIDO'
					    ,"12" ||'FECHA_VENCIMIENTO_ORIGINAL_INVALIDO'
					    ,"13" ||'FECHA_COMPROBANTE_INVALIDA'
					    ,"14" ||'SECUENCIA_O_FOLIO_COMPROBANTE_VACIO'
					    ,"15" ||'FOLIO_OPERACION_VACIO'    
					    ,"16" ||'TIPO_MOVIMIENTO_NO_EXISTE'
					    ,"17" ||'CONCEPTO_CUENTA_INVERSION_NO_EXISTE'
					    ,"18" ||'MONEDA_NO_EXISTE'
						,"19" ||'MONEDA_NO_ACTIVA'	
						,"20" ||'TIPO_OPERACION_NO_EXISTE'
						,"21" ||'PRODUCTO_NO_EXISTE'
						,"22" ||'TIPO_INSTRUMENTO_NO_EXISTE'
			 		) AS "Codigo",
			 		COALESCE(			 			
			 			 "1" ||"CUENTA_INVERSION_INVALIDA"
						,"2" ||"CUENTA_INVERSION_TRASPASO_INVALIDA"
						,"3" ||"CUENTA_INVERSION_NO_EXISTE"
						,"4" ||"CUENTA_INVERSION_NO_ACTIVA"
						,"5" ||"TIPO_MOVIMIENTO_INVALIDO"
					    ,"6" ||"CONCEPTO_CUENTA_INVERSION_INVALIDO"
					    ,"7" ||"FECHA_LIQUIDACION_MENOR_FECHA_MOVIMIENTO"
					    ,"8" ||"MONTO_MENOR_IGUAL_A_CERO"
					    ,"9" ||"FECHA_LIQUIDACION_INVALIDA"
					    ,"10" ||"FECHA_OPERACION_ORIGINAL_INVALIDO"
					    ,"11" ||"FECHA_LIQUIDACION_ORIGINAL_INVALIDO"
					    ,"12" ||"FECHA_VENCIMIENTO_ORIGINAL_INVALIDO"
					    ,"13" ||"FECHA_COMPROBANTE_INVALIDA"
					    ,"14" ||"SECUENCIA_O_FOLIO_COMPROBANTE_VACIO"
					    ,"15" ||"FOLIO_OPERACION_VACIO"    
					    ,"16" ||"TIPO_MOVIMIENTO_NO_EXISTE"
					    ,"17" ||"CONCEPTO_CUENTA_INVERSION_NO_EXISTE"
					    ,"18" ||"MONEDA_NO_EXISTE"
						,"19" ||"MONEDA_NO_ACTIVA"	
						,"20" ||"TIPO_OPERACION_NO_EXISTE"
						,"21" ||"PRODUCTO_NO_EXISTE"
						,"22" ||"TIPO_INSTRUMENTO_NO_EXISTE"
			 		) AS "Resultado"
			   FROM :v_Validaciones_V1,"BSCL.Plataforma.BD::PLA.Identidad")
	  WHERE "Resultado" IS NOT NULL
	  UNION ALL
	 SELECT "IdInterno","Codigo","Resultado",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2"
	   FROM (
			 SELECT "IdInterno",
			 		COALESCE(			 			
			 			 "1" ||'CLIENTE_NO_OPERA_CON_LINEA_SOBREGIRO'
						,"2" ||'SALDO_CAJA_INSUFICIENTE'
						,"3" ||'ERROR_CONSULTA_FLUJO_CAJA'
			 		) AS "Codigo",
			 		COALESCE(			 			
			 			 "1" ||"CLIENTE_NO_OPERA_CON_LINEA_SOBREGIRO"
						,"2" ||"SALDO_CAJA_INSUFICIENTE"
						,"3" ||"ERROR_CONSULTA_FLUJO_CAJA"
			 		) AS "Resultado"
			   FROM :v_Validaciones_V2,"BSCL.Plataforma.BD::PLA.Identidad")
	  WHERE "Resultado" IS NOT NULL
	  UNION ALL
	 SELECT "IdInterno","Codigo","Resultado",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2"
	   FROM (
			 SELECT "IdInterno",
			 		"1" ||'CLIENTE_SIN_CONTRATO_MERCANTIL' AS "Codigo",
			 		"1" ||"CLIENTE_SIN_CONTRATO_MERCANTIL" AS "Resultado"
			   FROM :v_Validaciones_V3,"BSCL.Plataforma.BD::PLA.Identidad")
	  WHERE "Resultado" IS NOT NULL;
	
	
	INSERT INTO "BSCL.CuentaInversion.BD::CTA.ServicioCumpleRequisitoCrearCargoAbono_OUT_Resultado"
	 SELECT  DISTINCT R."IdInterno"
		   	,R."Codigo"
	 		,R."Resultado"
	 		,CASE WHEN "Codigo" = 'CUENTA_INVERSION_NO_EXISTE' THEN 'NombreCliente'
	 			  WHEN "Codigo" = 'CUENTA_INVERSION_NO_ACTIVA' THEN 'NombreMoneda'
	 			  WHEN "Codigo" = 'TIPO_MOVIMIENTO_INVALIDO' THEN 'IdTipoMovimiento'
	 			  WHEN "Codigo" = 'FECHA_LIQUIDACION_MENOR_FECHA_MOVIMIENTO' THEN ''
	 			  WHEN "Codigo" = 'CONCEPTO_CUENTA_INVERSION_NO_EXISTE' THEN 'IdConcepto'
	 			  WHEN "Codigo" = 'MONEDA_NO_EXISTE' THEN 'IdMoneda'
	 			  WHEN "Codigo" = 'MONEDA_NO_ACTIVA' THEN 'IdMoneda'
	 			  WHEN "Codigo" = 'TIPO_OPERACION_NO_EXISTE' THEN 'IdTipoOperacion'
	 			  WHEN "Codigo" = 'PRODUCTO_NO_EXISTE' THEN 'IdProducto'
	 			  WHEN "Codigo" = 'TIPO_INSTRUMENTO_NO_EXISTE' THEN 'IdTipoInstrumento'	
	 			  WHEN "Codigo" = 'ERROR_CONSULTA_FLUJO_CAJA' THEN '' 
	 			  

	 			  
	 			  WHEN "Codigo" = 'SALDO_CAJA_INSUFICIENTE' THEN 'NombreCompleto'
	 			  
	 			  WHEN "Codigo" = 'CLIENTE_NO_OPERA_CON_LINEA_SOBREGIRO' THEN 'NombreCompleto'

	 			  
	 			  
	 			  ELSE ''
	 		  END "CodigoDato1"
	 		,CASE WHEN "Codigo" = 'CUENTA_INVERSION_NO_EXISTE' THEN ''
	 			  WHEN "Codigo" = 'CUENTA_INVERSION_NO_ACTIVA' THEN M."IdMoneda"
	 			  WHEN "Codigo" = 'TIPO_MOVIMIENTO_INVALIDO' THEN M."IdTipoMovimiento"
	 			  WHEN "Codigo" = 'FECHA_LIQUIDACION_MENOR_FECHA_MOVIMIENTO' THEN ''
	 			  WHEN "Codigo" = 'CONCEPTO_CUENTA_INVERSION_NO_EXISTE' THEN M."IdConcepto"
	 			  WHEN "Codigo" = 'MONEDA_NO_EXISTE' THEN M."IdMoneda"
	 			  WHEN "Codigo" = 'MONEDA_NO_ACTIVA' THEN M."IdMoneda"
	 			  WHEN "Codigo" = 'TIPO_OPERACION_NO_EXISTE' THEN M."IdTipoOperacion"
	 			  WHEN "Codigo" = 'PRODUCTO_NO_EXISTE' THEN M."IdTipoProducto"
	 			  WHEN "Codigo" = 'TIPO_INSTRUMENTO_NO_EXISTE' THEN M."IdTipoInstrumento"	
	 			  WHEN "Codigo" = 'ERROR_CONSULTA_FLUJO_CAJA' THEN '' 
	 			  
	 			  
	 			  WHEN "Codigo" = 'SALDO_CAJA_INSUFICIENTE' THEN MTR."NombreCompleto"
	 			  WHEN "Codigo" = 'CLIENTE_NO_OPERA_CON_LINEA_SOBREGIRO' THEN MTR."NombreCompleto"
	 			  
	 			  ELSE ''
	 		  END "ValorDato1"
	 		,CASE WHEN "Codigo" = 'CUENTA_INVERSION_NO_EXISTE' THEN 'NombreMoneda'
	 			  WHEN "Codigo" = 'CUENTA_INVERSION_NO_ACTIVA' THEN 'NombreCliente'
	 			  WHEN "Codigo" = 'FECHA_LIQUIDACION_MENOR_FECHA_MOVIMIENTO' THEN ''
	 			  
	 			  
	 			  WHEN "Codigo" = 'CLIENTE_NO_OPERA_CON_LINEA_SOBREGIRO' THEN 'DocumentoIdentidad'
	 			  
	 			  ELSE ''
	 		  END "CodigoDato2"
	 		,CASE WHEN "Codigo" = 'CUENTA_INVERSION_NO_EXISTE' THEN M."IdMoneda"
	 			  WHEN "Codigo" = 'CUENTA_INVERSION_NO_ACTIVA' THEN ''
	 			  WHEN "Codigo" = 'FECHA_LIQUIDACION_MENOR_FECHA_MOVIMIENTO' THEN ''
	 			  
	 			  
	 			  WHEN "Codigo" = 'CLIENTE_NO_OPERA_CON_LINEA_SOBREGIRO' THEN MTR."DocumentoIdentidad"
	 			  
	 			  
	 			  ELSE ''
	 		  END "ValorDato2"
	   FROM :v_Resultado as R
	  INNER JOIN "BSCL.CuentaInversion.BD::CTA.ServicioCumpleRequisitoCrearCargoAbono_IN_CargoAbonoAGenerar" as M ON M."IdInterno" = R."IdInterno"
	  
	  
	  
	  LEFT JOIN :v_MatrizValidaciones_v1 AS MTR ON (MTR."IdInterno" = R."IdInterno");
	
	  DELETE FROM "BSCL.CuentaInversion.BD::CTA.ServicioCumpleRequisitoCrearCargoAbono_IN_CargoAbonoAGenerar";
	  
END;
