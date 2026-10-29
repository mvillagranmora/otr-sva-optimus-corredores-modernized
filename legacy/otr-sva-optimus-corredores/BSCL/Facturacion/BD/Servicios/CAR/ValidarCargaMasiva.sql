SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Facturacion.BD.Servicios.CAR::ValidarCargaMasiva";
GO                                         
CREATE PROCEDURE
-- 
--  Proposito: Valida el detalle de los requisitos de la carga masiva
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 08/06/2017
--     
"BSCL.Facturacion.BD.Servicios.CAR::ValidarCargaMasiva"(	

	IN  p_CargaMasivaValidar         TABLE (  -- Cargas masivas a validar
												"IdCargaMasiva"       BIGINT,
												"CodigoArchivo"		  NVARCHAR(40)    -- Ej. CARGA_MOVIMIENTOS_FACTURACION
										   ),

	OUT p_out_CargaMasivaInvalida    TABLE ("IdCargaMasiva"       BIGINT),  -- Cargas masivas con registros invalidos	
	OUT p_out_ErroresValidacion      TABLE (                                -- Detalle registros invalidos
											"IdCargaMasiva"			 BIGINT,
											"IdCargaMasivaDetalle"   BIGINT,
											"ObservacionValidacion"	 NVARCHAR(520)
											)

) 	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER   
	DEFAULT SCHEMA BSCL
AS
BEGIN
	DECLARE SALIDA_CONTROLADA_NO_DATA CONDITION; 
	DECLARE v_ErrorNoData  			NVARCHAR(100); 
	DECLARE v_FechaSistema          NVARCHAR(10);
	DECLARE v_CantidadOT            INT;
	DECLARE v_resultadoRegla       	NVARCHAR(40);
	DECLARE v_ErroresContador 		INT;
	DECLARE v_MensajesValidacion	TABLE (
											"Codigo"		NVARCHAR(100),
											"Descripcion"	NVARCHAR(520)											
										  );

	p_out_CargaMasivaInvalida   = SELECT 0 AS "IdCargaMasiva" FROM dummy WHERE 1=2;
	p_out_ErroresValidacion		= SELECT 0 AS "IdCargaMasiva", 0 AS "IdCargaMasivaDetalle", '' AS "ObservacionValidacion" FROM dummy WHERE 1=2;	
	v_MensajesValidacion		= SELECT '' AS "Codigo", '' AS "Descripcion" FROM dummy WHERE 1=2;

	
	BEGIN	
		DECLARE v_TablaCodigosMensajes	TABLE ("CodigoMensaje" NVARCHAR(100),"CodigoDato" NVARCHAR(100),"ValorDato" NVARCHAR(520));
		DECLARE v_TablaValidaciones    	TABLE ("Cumple" BIGINT, "CodigoError" NVARCHAR(40), 
											   "CodigoDato" NVARCHAR(100), "ValorDato" NVARCHAR(520),"IdCargaMasivaDetalle" BIGINT, "IdCargaMasiva" BIGINT );
	
		
			
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;

		v_TablaCodigosMensajes	= SELECT '' AS "CodigoMensaje",'' AS "CodigoDato",'' AS "ValorDato" FROM dummy WHERE 1=2;	
		v_TablaValidaciones    	= SELECT  0 AS "Cumple",'' AS "CodigoError",'' AS "CodigoDato", '' AS "ValorDato", 0 AS "IdCargaMasivaDetalle", 0 AS "IdCargaMasiva"  FROM dummy WHERE (1=2);		



		--------------------------------------------   
		-- VALIDACIONES
		--------------------------------------------
		-- Obtiene detalles de carga masiva a validar
		v_CargaMasivaDetalle = 
		SELECT
			CMD."IdCargaMasiva",
			CMD."IdCargaMasivaDetalle",			
			CMV."CodigoArchivo",  -- Ej. CARGA_MOVIMIENTOS_FACTURACION
			CMD."IdConcepto",
			CMD."IdTipoOperacion",
			CMD."DocumentoIdentidad",
			CMD."IdPortafolio",			
			CMD."CodigoPortafolio",
			CMD."IdTipoComprobante",
			CMD."IdCondicionDeLiquidacion",
			CMD."IdMoneda",
			CMD."Monto",
			CMD."Glosa",			
			CMD."IdMedioDePago",
			CMD."IdModalidadDePago",
			CMD."TipoComprobanteDestino",
			CMD."FolioComprobante",
			CMD."FechaLiquidacion",
			EMP."IdPais"		
		FROM :p_CargaMasivaValidar AS CMV
		INNER JOIN "BSCL.Facturacion.BD::CAR.CargaMasivaDetalle" AS CMD ON (CMD."IdCargaMasiva" = CMV."IdCargaMasiva")
		INNER JOIN "BSCL.Facturacion.BD::CAR.CargaMasiva" CM ON CMD."IdCargaMasiva" = CM."IdCargaMasiva"
		INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" EMP ON EMP."IdEmpresa" = CM."IdEmpresa";



		-- Valida Tipo Operación 
		v_TablaValidaciones = 
		SELECT
				CASE WHEN COALESCE(OPE."IdTipoOperacion",'') = '' THEN 0
					 ELSE  1
				END AS "Cumple",
				CASE WHEN COALESCE(OPE."IdTipoOperacion",'') = '' THEN 'TIPO_OPERACION_VALIDA'
					 ELSE  ''
				END AS "CodigoError",
				'IdTipoOperacion' AS "CodigoDato",
		       	CAST(CMD."IdTipoOperacion" as NVARCHAR(50)) AS "ValorDato",
		      	CMD."IdCargaMasivaDetalle",
				CMD."IdCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" AS OPE ON    -- VENOTC / COMOTC
		(
			    OPE."IdTipoOperacion" 			= CMD."IdTipoOperacion"
			AND OPE."EsOtrosConceptos"  		= 'S' 
			AND OPE."EsMovimientoComprobante"   = 'S'			
		)
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" FROM :v_TablaValidaciones;		
		

		
		-- Valida Concepto otro conceptos
		v_TablaValidaciones = 
		SELECT
				CASE WHEN COALESCE(CON."IdConcepto",'') = '' THEN 0
					 ELSE  1
				END AS "Cumple",
				CASE WHEN COALESCE(CON."IdConcepto",'') = '' THEN 'CONCEPTO_NO_VALIDO'
					 ELSE  ''
				END AS "CodigoError",
				'IdConcepto' AS "CodigoDato",
		       	CAST(CMD."IdConcepto" as NVARCHAR(50)) AS "ValorDato",
		       	CMD."IdCargaMasivaDetalle",
				CMD."IdCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD
		LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarConceptosFacturacion" AS CON ON 
		(
			    CON."IdConcepto" 			= CMD."IdConcepto"
			AND CON."EsOtrosConceptos"  	= 'S' 
			AND CON."IdEstado"   			= 'VIG'
			AND CON."ConfiguradoEnModulo" 	= 'S'
		)
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" FROM :v_TablaValidaciones;	
	
	
		
		-- Valida tipo comprobante otros conceptos
		v_TablaValidaciones =  
		SELECT
				CASE WHEN COALESCE(TCO."IdTipoComprobante",'') = '' THEN 0
					 ELSE  1
				END AS "Cumple",
				CASE WHEN COALESCE(TCO."IdTipoComprobante",'') = '' THEN 'TIPO_COMPROBANTE_INVALIDO'
					 ELSE  ''
				END AS "CodigoError",
				'IdTipoComprobante' AS "CodigoDato",
		       	CAST(CMD."IdTipoComprobante" as NVARCHAR(50)) AS "ValorDato",
		       	CMD."IdCargaMasivaDetalle",
				CMD."IdCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD
		LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarTipoComprobante" AS TCO ON 
		(
			    TCO."IdTipoComprobante" = CMD."IdTipoComprobante"  
			AND TCO."IdEstadoFacturacion" = 'ACT'  
			AND TCO."IdTipoComprobante" IN ('FOE','FOX','FEE','FOC','BOE','NEO')  -- debe estar alineado con la UI de ingreso manual de mov. otros conceptos
		)
		WHERE CMD."CodigoArchivo"= 'CARGA_MOVIMIENTOS_OTROS_CONCEPTOS'
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" FROM :v_TablaValidaciones;
		
		
		
		-- Valida tipo comprobante nota de credito
		v_TablaValidaciones =  
		SELECT
				CASE WHEN COALESCE(TCO."IdTipoComprobante",'') = '' THEN 0
					 ELSE  1
				END AS "Cumple",
				CASE WHEN COALESCE(TCO."IdTipoComprobante",'') = '' THEN 'TIPO_COMPROBANTE_INVALIDO'
					 ELSE  ''
				END AS "CodigoError",
				'IdTipoComprobante' AS "CodigoDato",
		       	CAST(CMD."IdTipoComprobante" as NVARCHAR(50)) AS "ValorDato",
		       	CMD."IdCargaMasivaDetalle",
				CMD."IdCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD
		LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarTipoComprobante" AS TCO ON 
		(
			    TCO."IdTipoComprobante" = CMD."IdTipoComprobante"  
			AND TCO."IdEstadoFacturacion" = 'ACT'  
			AND TCO."IdTipoComprobante" IN ('NCR','NCO','NCE','NEO')  -- debe estar alineado con la UI de ingreso manual de mov. otros conceptos
		)
		WHERE CMD."CodigoArchivo"= 'CARGA_NOTA_DE_CREDITO'
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" FROM :v_TablaValidaciones;
	
	
	
	    -- Valida monto nota de credito no supere monto de comision SAB
	    v_TablaValidaciones = 
	    SELECT
	          CASE WHEN (SELECT Sum(CmbteCobro."MontoLiquidacionCobro") 
	                     FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobrosPeru" AS CmbteCobro 
	                     WHERE CmbteCobro."IdComprobante" = CMD."FolioComprobante" AND CmbteCobro."IdCobroClasificacion" = 'COMISION_SAB' AND CmbteCobro."IdEstado" = 'VIG') 
	                           < (Sum(CMD."Monto") + COALESCE((SELECT Sum(CmbteAsoc."MontoLiquidacion") 
	                                                           FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS CmbteOrig 
	                                                             INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS CmbteAsoc
	                                                                ON CmbteAsoc."IdComprobanteReferencia" = CmbteOrig."IdComprobante" 
	                                                           WHERE CmbteOrig."IdComprobante" = CMD."FolioComprobante" 
	                                                             AND CmbteAsoc."IdEstadoComprobante" <> 'ANU'),0)
	                                                + COALESCE((SELECT Sum(CargPend."Monto")
	                                                            FROM "BSCL.Facturacion.BD::CAR.CargaMasivaDetalle" AS CargPend
	                                                            WHERE CargPend."FolioComprobante" = CMD."FolioComprobante"
	                                                              AND CargPend."IdEstado" = 'ING'
	                                                              AND CargPend."IdCargaMasiva" <> CMD."IdCargaMasiva"),0)) THEN 0
	          ELSE  1
              END AS "Cumple",
              CASE
                WHEN (SELECT Sum(CmbteCobro."MontoLiquidacionCobro") 
                      FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobrosPeru" AS CmbteCobro 
                      WHERE CmbteCobro."IdComprobante" = CMD."FolioComprobante" AND CmbteCobro."IdCobroClasificacion" = 'COMISION_SAB' AND CmbteCobro."IdEstado" = 'VIG') 
                            < (Sum(CMD."Monto") + COALESCE((SELECT Sum(CmbteAsoc."MontoLiquidacion") 
	                                                        FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS CmbteOrig 
	                                                          INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS CmbteAsoc
	                                                             ON CmbteAsoc."IdComprobanteReferencia" = CmbteOrig."IdComprobante" 
	                                                        WHERE CmbteOrig."IdComprobante" = CMD."FolioComprobante" 
	                                                          AND CmbteAsoc."IdEstadoComprobante" <> 'ANU'),0)
                                                + COALESCE((SELECT Sum(CargPend."Monto")
	                                                        FROM "BSCL.Facturacion.BD::CAR.CargaMasivaDetalle" AS CargPend
	                                                        WHERE CargPend."FolioComprobante" = CMD."FolioComprobante"
	                                                          AND CargPend."IdEstado" = 'ING'
	                                                          AND CargPend."IdCargaMasiva" <> CMD."IdCargaMasiva"),0)) THEN 'MONTO_COMPROBANTE_SUPERA_COBRO_SAB'
              ELSE  ''
              END AS "CodigoError",
              'IdComprobante' AS "CodigoDato",
              CAST(CMD."FolioComprobante" as NVARCHAR(50)) AS "ValorDato",
              Max(CMD."IdCargaMasivaDetalle") AS "IdCargaMasivaDetalle",
              Max(CMD."IdCargaMasiva") AS "IdCargaMasiva"
        FROM :v_CargaMasivaDetalle AS CMD
        WHERE CMD."CodigoArchivo"= 'CARGA_NOTA_DE_CREDITO' 
        GROUP BY CMD."FolioComprobante", CMD."IdCargaMasiva" 
        UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" FROM :v_TablaValidaciones;
		
       
		-- Valida la moneda
		v_TablaValidaciones = 
		SELECT
				CASE WHEN COALESCE(MON."IdMoneda",'') = '' THEN 0
					 ELSE  1
				END AS "Cumple",
				CASE WHEN COALESCE(MON."IdMoneda",'') = '' THEN 'MONEDA_NO_EXISTE'
					 ELSE  ''
				END AS "CodigoError",
				'IdMoneda' AS "CodigoDato",
		       	CAST(CMD."IdMoneda" as NVARCHAR(50)) AS "ValorDato",
		       	CMD."IdCargaMasivaDetalle",
				CMD."IdCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON ON (MON."IdMoneda" = CMD."IdMoneda" AND MON."IdEstado" = 'ACT')
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" FROM :v_TablaValidaciones;



		-- Valida condición de liquidación
		v_TablaValidaciones = 
		SELECT                     
				CASE WHEN COALESCE(CON."IdCondicionDeLiquidacion",'') = '' THEN 0
					 ELSE  1
				END AS "Cumple",
				CASE WHEN COALESCE(CON."IdCondicionDeLiquidacion",'') = '' THEN 'CONDICION_DE_LIQUIDACION_NO_EXISTE'
					 ELSE  ''
				END AS "CodigoError",
				'IdCondicionDeLiquidacion' AS "CodigoDato",
		       	CAST(CON."IdCondicionDeLiquidacion" as NVARCHAR(50)) AS "ValorDato",
		       	CMD."IdCargaMasivaDetalle",
				CMD."IdCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD
		LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS CON ON (CON."IdCondicionDeLiquidacion" = CMD."IdCondicionDeLiquidacion")
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" FROM :v_TablaValidaciones;

		


		-- Valida que exista Socio de Negocio(De acuerdo al rut)
		v_TablaValidaciones = 
		SELECT  
				CASE WHEN COALESCE(PER."DocumentoIdentidad",'') = '' THEN 0					 
					 ELSE  1
				END AS "Cumple",
				CASE WHEN COALESCE(PER."DocumentoIdentidad",'') = '' THEN 'SOCIO_NEGOCIO_NO_EXISTE'
					 ELSE  ''
				END AS "CodigoError",
				'DocumentoIdentidad' AS "CodigoDato",
		       	CMD."DocumentoIdentidad" AS "ValorDato",
		       	CMD."IdCargaMasivaDetalle",
				CMD."IdCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON (PER."DocumentoIdentidad" = CMD."DocumentoIdentidad")
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" FROM :v_TablaValidaciones;


		-- Valida que el portafolio exista y pertenesca al cliente
		v_TablaValidaciones = 
		SELECT  
				CASE WHEN COALESCE(POR."CodigoPortafolio",'') = '' THEN 0					 
					 ELSE  1
				END AS "Cumple",
				CASE WHEN COALESCE(POR."CodigoPortafolio",'') = '' THEN 'PORTAFOLIO_NO_EXISTE'
					 ELSE  ''
				END AS "CodigoError",
				'CodigoPortafolio' AS "CodigoDato",
		       	CAST(CMD."CodigoPortafolio" AS NVARCHAR(50))AS "ValorDato",
		       	CMD."IdCargaMasivaDetalle",
				CMD."IdCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD		
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON (PER."DocumentoIdentidad" = CMD."DocumentoIdentidad")
		LEFT JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" POR ON (POR."IdPersona" = PER."IdPersona" AND RTRIM(LTRIM(UCASE(POR."CodigoPortafolio"))) = RTRIM(LTRIM(UCASE(CMD."CodigoPortafolio"))))
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" FROM :v_TablaValidaciones;

		
		-- Valida que el monto sea mayor a cero
		v_TablaValidaciones = 
		SELECT  
				CASE WHEN COALESCE(CMD."Monto",0) <= 0 THEN 0					 
					 ELSE  1
				END AS "Cumple",				
				CASE WHEN COALESCE(CMD."Monto",0) <= 0 THEN 'MONTO_INVALIDO'
					 ELSE  ''
				END AS "CodigoError",
				'Monto' AS "CodigoDato",
		       	CAST(CMD."Monto" AS NVARCHAR(50))AS "ValorDato",		       	
		       	CMD."IdCargaMasivaDetalle",
				CMD."IdCargaMasiva"				
		FROM :v_CargaMasivaDetalle AS CMD
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" FROM :v_TablaValidaciones;


		-- Valida medio de pago(dato opcional)
		v_TablaValidaciones = 
		SELECT
				CASE WHEN COALESCE(MPA."IdMedioDePago",'') = '' THEN 0
					 ELSE  1
				END AS "Cumple",
				CASE WHEN COALESCE(MPA."IdMedioDePago",'') = '' THEN 'MEDIO_DE_PAGO_NO_EXISTE'
					 ELSE  ''
				END AS "CodigoError",
				'IdMedioDePago' AS "CodigoDato",
		       	CAST(CMD."IdMedioDePago" as NVARCHAR(50)) AS "ValorDato",
		       	CMD."IdCargaMasivaDetalle",
				CMD."IdCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD
		LEFT JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" AS MPA ON (MPA."IdMedioDePago" = CMD."IdMedioDePago" AND MPA."IdEstado" = 'ACT')
		WHERE CMD."IdMedioDePago" <> ''  -- validar solo cuando el dato exista
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" FROM :v_TablaValidaciones;



		-- Valida modalidad de pago(dato opcional)
		v_TablaValidaciones = 
		SELECT
				CASE WHEN COALESCE(MOP."IdModalidadDePago",'') = '' THEN 0
					 ELSE  1
				END AS "Cumple",
				CASE WHEN COALESCE(MOP."IdModalidadDePago",'') = '' THEN 'MODALIDAD_DE_PAGO_INVALIDO'
					 ELSE  ''
				END AS "CodigoError",
				'IdModalidadDePago' AS "CodigoDato",
		       	CAST(CMD."IdModalidadDePago" as NVARCHAR(50)) AS "ValorDato",
		       	CMD."IdCargaMasivaDetalle",
				CMD."IdCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD
		LEFT JOIN "BSCL.Plataforma.BD::PAG.ModalidadDePago" AS MOP ON (MOP."IdModalidadDePago" = CMD."IdModalidadDePago" AND MOP."IdEstado" = 'ACT')
		WHERE CMD."IdModalidadDePago" <> ''  -- validar solo cuando el dato exista
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" FROM :v_TablaValidaciones;
		
		
			
		
		--valida tipo comprobante destino

		v_TablaValidaciones = 
		SELECT
				CASE WHEN COALESCE(TCO."IdTipoComprobante",'') = '' THEN 0
					 ELSE  1
				END AS "Cumple",
				CASE WHEN COALESCE(TCO."IdTipoComprobante",'') = '' THEN 'TIPO_COMPROBANTE_DESTINO_INVALIDO'
					 ELSE  ''
				END AS "CodigoError",
				'IdTipoComprobante' AS "CodigoDato",
		       	CAST(CMD."IdTipoComprobante" as NVARCHAR(50)) AS "ValorDato",
		       	CMD."IdCargaMasivaDetalle",
				CMD."IdCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD
		LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarTipoComprobante" AS TCO ON 
		(
			    TCO."IdTipoComprobante" = CMD."TipoComprobanteDestino"  
			AND TCO."IdEstadoFacturacion" = 'ACT'  
			AND TCO."IdTipoComprobante" IN ('FOE','FOX','FEE','FOC','FAE','BOE','POL')  
		)
		WHERE CMD."CodigoArchivo"= 'CARGA_NOTA_DE_CREDITO'
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" FROM :v_TablaValidaciones;
		
		
		
		-- valida folio comprobante destino
		
		v_TablaValidaciones = 
		SELECT
				CASE WHEN COALESCE(CO."IdTipoComprobante",'') = '' THEN 0
					 ELSE  1
				END AS "Cumple",
				CASE WHEN COALESCE(CO."IdTipoComprobante",'') = '' THEN 'NO_EXISTE_FACTURA'
					 ELSE  ''
				END AS "CodigoError",
				'IdTipoComprobante' AS "CodigoDato",
		       	CAST(CMD."IdTipoComprobante" as NVARCHAR(50)) AS "ValorDato",
		       	CMD."IdCargaMasivaDetalle",
				CMD."IdCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD
		LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" AS CO ON 
		(
			    CO."IdComprobante" = CMD."FolioComprobante"
			AND CO."IdEstadoComprobante" ='FAC'
			AND	CO."IdEstadoDTE" = 'REC' 
		)
		WHERE CMD."Monto" >= CO."Monto" 
		AND CMD."CodigoArchivo"= 'CARGA_NOTA_DE_CREDITO'
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" FROM :v_TablaValidaciones;

		-- Valida movimiento si es movimiento en USD en Contrapartida
		v_TablaValidaciones = 
		SELECT
				CASE WHEN CON."Contrapartida" = 'S' AND CMD."IdMoneda" != 'USD' THEN 0
					 ELSE  1
				END AS "Cumple",
				CASE WHEN CON."Contrapartida" = 'S' AND CMD."IdMoneda" != 'USD' THEN 'CONTRAPARTIDA_MONEDA_DISTINTA'
					 ELSE  ''
				END AS "CodigoError",
				'IdConcepto' AS "CodigoDato",
		       	CAST(CMD."IdConcepto" as NVARCHAR(50)) AS "ValorDato",
		       	CMD."IdCargaMasivaDetalle",
				CMD."IdCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD
		LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarConceptosFacturacion" AS CON ON 
		(
			    CON."IdConcepto" 			= CMD."IdConcepto"
			AND CON."EsOtrosConceptos"  	= 'S' 
			AND CON."IdEstado"   			= 'VIG'
			AND CON."ConfiguradoEnModulo" 	= 'S'
		)
		UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" FROM :v_TablaValidaciones;

		--valida fecha de liquidacion cuando existe condicion de liquidacion OT dentro del detalle de la carga masiva
		SELECT 
			count(1)
		INTO
			v_CantidadOT
		FROM 
			:v_CargaMasivaDetalle
		WHERE "IdCondicionDeLiquidacion" = 'OT';
	
		IF v_CantidadOT > 0 THEN 
			SELECT
				"Valor"
			INTO v_FechaSistema
			FROM
				"BSCL.Plataforma.BD::PLA.Configuracion"
			WHERE
				"CodigoParametro" = 'PLA.FechaSistema';
			
			v_TablaValidaciones = 	
			SELECT
				CASE 
					WHEN FER."Fecha" IS NOT NULL THEN 0
					WHEN CMD."FechaLiquidacion" = '' OR CMD."FechaLiquidacion" IS NULL THEN 0
					WHEN WEEKDAY(CMD."FechaLiquidacion") IN (5,6) THEN 0 
					WHEN CMD."FechaLiquidacion" < v_FechaSistema THEN 0
					ELSE 1
				END AS "Cumple", 
				CASE 
					WHEN FER."Fecha" IS NOT NULL THEN 'OT_FECHA_LIQUIDACION_INVALIDA'
					WHEN CMD."FechaLiquidacion" = '' OR CMD."FechaLiquidacion" IS NULL THEN 'OT_FECHA_LIQUIDACION_INVALIDA'
					WHEN WEEKDAY(CMD."FechaLiquidacion") IN (5,6) THEN 'OT_FECHA_LIQUIDACION_INVALIDA' 
					WHEN CMD."FechaLiquidacion" < v_FechaSistema THEN 'OT_FECHA_LIQUIDACION_INVALIDA'
					ELSE ''
				END AS "CodigoError",
				'FechaLiquidacion' AS "CodigoDato",
				"FechaLiquidacion" AS "ValorDato",
				"IdCargaMasivaDetalle",
				"IdCargaMasiva"
			FROM
				:v_CargaMasivaDetalle CMD
			LEFT JOIN "BSCL.Plataforma.BD::REG.DiaFeriado" FER ON FER."IdPais" = CMD."IdPais" AND FER."Fecha" = CMD."FechaLiquidacion"
			WHERE CMD."IdCondicionDeLiquidacion" = 'OT'
			UNION ALL SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" FROM :v_TablaValidaciones;
		END IF; 
		
		-------------------------------------------------
		-- ANALISIS DE DATOS
		-----------------------------------------------	   	
	   	 
		-- Cargas Masivas con registros invalidos
	    p_out_CargaMasivaInvalida =
	    SELECT "IdCargaMasiva"
	    FROM :v_TablaValidaciones
	    WHERE "Cumple" = 0
		GROUP BY "IdCargaMasiva";
	    
	    
	    v_TablaCodigosMensajes =
		SELECT 	"CodigoError" AS "CodigoMensaje"
			   ,"CodigoDato"
			   ,"ValorDato" 
		FROM :v_TablaValidaciones
		WHERE "Cumple" = 0;
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes, v_MensajesValidacion);


		p_out_ErroresValidacion = 
		SELECT 
			TV."IdCargaMasiva",
			TV."IdCargaMasivaDetalle",
			(CASE
				WHEN COALESCE(TV."ValorDato",'') = '' THEN MV."Descripcion"
				ELSE MV."Descripcion" || ' (' || TV."ValorDato" || ')'
			END) AS "ObservacionValidacion"
		FROM  :v_TablaValidaciones AS TV
		INNER JOIN :v_MensajesValidacion AS MV  ON (TV."CodigoError" = MV."Codigo")
		WHERE TV."Cumple" = 0;		 
    END;
	
END;
