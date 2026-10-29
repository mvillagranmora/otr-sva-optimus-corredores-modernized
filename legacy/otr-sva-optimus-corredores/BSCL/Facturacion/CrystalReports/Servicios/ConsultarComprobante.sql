
SET SCHEMA "BSCL";
GO
 
DROP PROCEDURE "BSCL.Facturacion.CrystalReports.Servicios::ConsultarComprobante";
GO

--
--  Proposito: Obtener datos para la confeccion de comprobante con detalle
--  Autor: NSM
--  Fecha Creacion: 22-05-2017
--
 
CREATE PROCEDURE "BSCL.Facturacion.CrystalReports.Servicios::ConsultarComprobante" (
	-- Parametros entrada 
    IN p_IdEmpresa          BIGINT,	
	IN p_IdComprobante      BIGINT	
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN
   --------------------
   --Variables
   --------------------
   DECLARE v_FechaSistema date;
   DECLARE v_logo TABLE("LogoCorredora" BLOB);
   DECLARE v_Sistema, v_GlosaFacturaOtrosConceptosPeriodo  NVARCHAR(2);
   DECLARE v_DireccionEmpresa, v_DireccionSucursal NVARCHAR(60);
   DECLARE v_IndicadorPresenciaBursatil, v_CountIndPreBur, v_CountDirCor, v_CountDirSucCor, v_CountLocSis BIGINT;
      
   
   SELECT Count(*) INTO v_CountLocSis
   FROM "BSCL.Plataforma.BD::PLA.Configuracion"
   WHERE "IdEmpresa" = p_IdEmpresa AND "CodigoParametro" = 'PLA.LocalizacionSistema';
   
   If (v_CountLocSis > 0) then
     SELECT "Valor" INTO v_Sistema  
     FROM "BSCL.Plataforma.BD::PLA.Configuracion"
     WHERE "IdEmpresa" = p_IdEmpresa AND "CodigoParametro" = 'PLA.LocalizacionSistema';	
   else
     v_Sistema := 'No Identificado';
   end if;
      
   
   SELECT Count(*) INTO v_CountIndPreBur  
   FROM "BSCL.Orden.BD::ORD.ConfiguracionOrdenes"
   WHERE "IdEmpresa" = p_IdEmpresa AND "CodigoParametro" =  'IndicadorPresenciaBursatil';
   
   If (v_CountDirCor > 0) then
      SELECT "Valor" INTO v_IndicadorPresenciaBursatil  
      FROM "BSCL.Orden.BD::ORD.ConfiguracionOrdenes"
      WHERE "IdEmpresa" = p_IdEmpresa AND "CodigoParametro" =  'IndicadorPresenciaBursatil';
   else
      v_IndicadorPresenciaBursatil := 25;
   end if;

   v_logo = Select TOP 1 "Binario" AS "LogoCorredora"
	        From "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
	        Where "NombreImagen" = 'LOGO_CORREDOR' 
	        Order By "Version" Desc;
		    ----------------------------
			SELECT Count("Direccion") INTO v_CountDirCor
	        FROM "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales";
	        
	       	        
	        If (v_CountDirCor > 0) then
	          SELECT "Direccion" INTO v_DireccionEmpresa
	          FROM "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales";
	        else
	          v_DireccionEmpresa := 'No Identificado';
	        end if;
	        
	        ----------------------------
	        SELECT Count(*) INTO v_CountDirSucCor
	        FROM "BSCL.Plataforma.BD::PLA.Configuracion"
	        WHERE "CodigoParametro" = 'PLA.DireccionSucursalCorredora' 
	        AND  "IdEmpresa" = p_IdEmpresa;
	        
	        If (v_CountDirSucCor > 0) then
	           SELECT "Valor" INTO v_DireccionSucursal
	           FROM "BSCL.Plataforma.BD::PLA.Configuracion"
	           WHERE "CodigoParametro" = 'PLA.DireccionSucursalCorredora' 
	           AND  "IdEmpresa" = p_IdEmpresa;
	        else
	           v_DireccionSucursal := 'No Identificado';
	        end if;	        
	
	        
   Select Top 1 "Valor" 
   Into v_FechaSistema 
   From "BSCL.Plataforma.BD::PLA.Configuracion" 
   Where "CodigoParametro" = 'PLA.FechaSistema' 
	 And "IdEmpresa" = :p_IdEmpresa ;

   ----Condiciona segun indicador glosa para facturas FOE   	 
   SELECT "Valor" 
   INTO v_GlosaFacturaOtrosConceptosPeriodo
   FROM "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion"
   WHERE "IdEmpresa" = :p_IdEmpresa
     AND "CodigoParametro" = 'GLOSAFACTURAOTROSCONCEPTOSPERIODO';	 

	 	 
	SELECT ConsCmbte."IdComprobante",
		ConsCmbte."FolioComprobante",
		ConsCmbte."IdTipoComprobante",
		CAST(ConsCmbte."IdComprobante" AS BIGINT) AS "Comprobante",
		CAST(ConsCmbte."FolioComprobante" AS BIGINT) AS "Folio",
		TO_VARCHAR(ConsCmbte."FechaComprobante", 'DD-MM-YYYY') AS "FechaComprobante",
		ConsCmbte."NombreTipoComprobante",
		CASE
	        WHEN UPPER(ConsCmbteConDet."AccionTipoOperacion") = 'RESCATE' THEN 'COMPRA'
	        WHEN UPPER(ConsCmbteConDet."IdTipoOperacion") = 'COMSPT' THEN 'VENTA' --Se cambia al punto de vista del cliente
	        WHEN UPPER(ConsCmbteConDet."IdTipoOperacion") = 'VENSPT' THEN 'COMPRA' --Se cambia al punto de vista del cliente
	      	ELSE ConsCmbteConDet."AccionTipoOperacion"
		END AS "AccionTipoOperacion",
		ConsCmbte."IdCliente",          
		--1era linea
          COALESCE(IDENT."IdTipoDocumentoIdentidad", IDENT."IdTipoDocumentoIdentidad", '') As "TipoDocumentoIdentidadReceptor",
          COALESCE(IDENT."DocumentoIdentidad", IDENT."DocumentoIdentidad",'') As "DocumentoIdentidadReceptor",
          --
          CASE
  	        WHEN Per."Tipo" = 'N' THEN 'D.N.I.'
  	        WHEN Per."Tipo" = 'J' THEN 'R.U.C.'
  	      ELSE ''
  	      END AS "TipoDocumentoIdentidadReceptorpE",
          CASE
  	        WHEN Per."Tipo" = 'N' THEN ''--DocIdenPe."DNI"
  	        WHEN Per."Tipo" = 'J' THEN ''--DocIdenPe."RUC"
  	      ELSE ''
  	      END AS "DocumentoIdentidadReceptorPe",
  	      --2da linea
  	      /*COALESCE(DoctoInden."IdTipoDocumentoIdentidadAsociado", DoctoInden."IdTipoDocumentoIdentidadAsociado", '') As "TipoDocumentoIdentidadAsociado",
  	      COALESCE(DoctoInden."DocumentoIdentidadAsociado", DoctoInden."DocumentoIdentidadAsociado", '') As "DocumentoIdentidadAsociado",*/
          --
          COALESCE(IDENT."IdTipoDocumentoIdentidadAsociado", IDENT."IdTipoDocumentoIdentidadAsociado", '') As "TipoDocumentoIdentidadAsociado",
  	      COALESCE(IDENT."DocumentoIdentidadAsociado", IDENT."DocumentoIdentidadAsociado", '') As "DocumentoIdentidadAsociado",
  	      
  	      
          --3era linea          
  	      COALESCE(IDENTADICIONAL."IdTipoDocumentoIdentidad", IDENTADICIONAL."IdTipoDocumentoIdentidad", '') As "TipoDocumentoIdentidadAdicionalReceptor",
          COALESCE(IDENTADICIONAL."DocumentoIdentidad",IDENTADICIONAL."DocumentoIdentidad", '') As "DocumentoIdentidadAdicionalReceptor",
		--
		CAST(COALESCE(NULLIF(POR."Alias", ''), ConsCmbteConDet."RazonSocial") AS VARCHAR(200)) AS "RazonSocialReceptor",
		ConsCmbte."NombrePortafolio",
		Pais."NombrePais" AS "NacionalidadReceptor",
		(IfNull(DirFis."Calle", '') || ' ' || IfNull(DirFis."Numero", '') || ' ' || IfNull(DirFis."Departamento",'')) AS "DireccionReceptor",                  
		CASE 
	        WHEN LENGTH(Ciudad."NombreCiudad") > 0 THEN Ciudad."NombreCiudad"
	      	ELSE 'En definicion'
		END AS "CiudadReceptor",
		CASE
			WHEN LENGTH(DivPais."NombreDivisionPais") > 0 THEN DivPais."NombreDivisionPais"
			ELSE 'En definicion'
		END AS "ComunaReceptor", 
		ConsCmbte."IdCondicionLiquidacion",
		CAST(CondLiq."Codigo" AS VARCHAR(200)) AS "CodigoCondicionLiquidacion",
		TO_VARCHAR(ConsCmbte."FechaLiquidacion", 'DD-MM-YYYY') AS "FechaLiquidacion",
		ConsCmbte."IdEmpleadoEjecutivoPortafolio",
		ConsCmbte."NombreEmpleadoEjecutivoPortafolio",
		ConsCmbte."NombreEmpresa",
		v_DireccionSucursal AS "Sucursal",
		CASE 
	   		WHEN ConsCmbte."IdTipoInstrumento" IN ('PRT', 'EMP', 'SIM','RRV','RRF') THEN ConsCmbteConDet."NemotecnicoSubyacente" 
	   	  	ELSE ConsCmbteConDet."NemotecnicoInstrumento"
		END AS "NemotecnicoInstrumento",  
		ConsCmbte."Monto",
		ConsCmbte."MontoLiquidacion",
		CASE 
	        WHEN ConsCmbte."MontoLiquidacionFinal" < 0 THEN (ConsCmbte."MontoLiquidacionFinal" * -1)
	      	ELSE ConsCmbte."MontoLiquidacionFinal"
		END "MontoLiquidacionFinal",
	    ConsCmbte."TotalComisCmbte",
	  	ConsCmbte."TotalImptoCmbte",
		ConsCmbte."TotalDereCmbte",
		ConsCmbte."TotalGastoCmbte",
		ConsCmbte."TotalRetencionCmbte",
		CASE
		    WHEN  (SELECT COUNT(1) FROM "BSCL.Plataforma.BD::PLA.UsoGlosaTipoInstrumento" glo
        		WHERE glo."IdTipoInstrumento" = ConsCmbte."IdTipoInstrumento") > 0
				THEN  IfNull(ConsCmbteConDet."GlosaSimplificada", '')
		ELSE
			CASE 
				WHEN ConsCmbte."IdTipoComprobante" = 'FOC' AND ConsCmbte."IdConcepto" = 'COMMEN' THEN ConsCmbte."Glosa" || ' ' || TO_VARCHAR(ConsCmbteConDet."FechaLiquidacion", 'DD-MM-YYYY') 
				WHEN ConsCmbte."IdTipoComprobante" IN ('FAE', 'FOE', 'FOX', 'FAC', 'FEE','POL') THEN CASE
				WHEN ConsCmbte."IdConcepto" = 'CMMNRV' AND LENGTH(ConsCmbte."Glosa") > 0 THEN ConsCmbte."Glosa"
				WHEN ConsCmbte."IdConcepto" = 'CMMNRV' THEN 'COMISI�N TRIMESTRAL NO APV - Acciones, CFI, ETFs - Trimestre'
				WHEN ConsCmbte."IdConcepto" = 'CMTAPV' AND LENGTH(ConsCmbte."Glosa") > 0 THEN ConsCmbte."Glosa"
				WHEN ConsCmbte."IdConcepto" = 'CMTAPV' THEN 'COMISI?N TRIMESTRAL APV - Acciones, CFI, ETFs - Trimestre'
				WHEN ConsCmbte."IdConcepto" = 'SUECOS' THEN IfNull(ConsCmbteConDet."AccionTipoOperacion" || ' ', '') || IfNull(ConsTipOpe."Nombre" || ':', '') || ifNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteConDet."Cantidad",'dx',2, 'CL') || ' ', '') || 'unidades de ' || IfNull(ConsCmbteConDet."NemotecnicoInstrumento" || ', ', '') || 'a un valor de ' || CASE 
					WHEN ConsCmbteConDet."IdMonedaLiquidacion" = 'USD' THEN ifNull(TO_DECIMAL(ROUND(ConsCmbteConDet."Precio", 2), 26, 2) || MonLiq."Simbolo" || ' ', '')
					ELSE ifNull(MonLiq."Simbolo" || TO_DECIMAL(ROUND(ConsCmbteConDet."Precio", 2), 26, 2) || ' ', '') 
					END || 'por Acción. ' || CASE
											WHEN length(MarkMak."IdInstrumento") > 0 OR MarkMak."IdInstrumento" > 0 THEN 'APB '
											WHEN length(Inst."IdInstrumento") > 0 OR Inst."IdInstrumento" > 0 THEN 'APB '
											ELSE ''
											END
				WHEN ConsCmbte."IdConcepto" IN ('VNOSAC', 'CMOSAC')  THEN IfNull(ConsCmbteConDet."AccionTipoOperacion" || ' ', '') || IfNull(ConsCmbteConDet."NemotecnicoInstrumento" || ':', '') || ifNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteConDet."Cantidad",'dx',2, 'CL') || ' ', '') || 'unidades ' || 'a un valor de ' || CASE 
					WHEN ConsCmbteConDet."IdMonedaLiquidacion" = 'USD' THEN ifNull(TO_DECIMAL(ROUND(ConsCmbteConDet."Precio", 2), 26, 2) || MonLiq."Simbolo" || ' ', '')
					ELSE ifNull(MonLiq."Simbolo" || TO_DECIMAL(ROUND(ConsCmbteConDet."Precio", 2), 26, 2) || ' ', '') 
					END || 'por Acción. ' || CASE
											WHEN length(MarkMak."IdInstrumento") > 0 OR MarkMak."IdInstrumento" > 0 THEN 'APB '
											WHEN length(Inst."IdInstrumento") > 0 OR Inst."IdInstrumento" > 0 THEN 'APB '
											ELSE ''
											END                                    
				WHEN ConsCmbte."IdTipoInstrumento" IN ('ACN', 'ACX', 'CFI', 'CFX', 'FFM', 'ETN', 'ETX', 'MON', 'FIP', 'MIL') THEN
					IfNull(UPPER(ConsCmbteConDet."AccionTipoOperacion") || ' ', '') || -- IfNull(ConsCmbteConDet."FolioTransaccion" || ' ', '') ||
					IfNull(ConsCmbteConDet."DetalleGlosaFacturacion" || ' ', '') || IfNull(ConsCmbteConDet."NemotecnicoInstrumento" || ' ', '') || 
					CASE WHEN length(MarkMak."IdInstrumento") > 0 OR MarkMak."IdInstrumento" > 0 THEN 'APB ' 
						WHEN length(Inst."IdInstrumento") > 0 OR Inst."IdInstrumento" > 0 THEN 'APB ' 
						WHEN ConsCmbte."IdTipoInstrumento" IN ('MON') THEN IfNull(ConsCmbteConDet."IdMoneda" || ' ', '') ELSE '' END || 
					CASE WHEN ConsCmbte."EsVentaCorta"= 'S' THEN IfNull(TO_NVARCHAR(ConsCmbte."FechaVencimiento", 'DD/MM/YYYY'), '') || ' Tasa ' || IfNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteConDet."Tasa", 'dx',2, 'CL') || '% ', '') 
						ELSE IfNull(TO_NVARCHAR(ConsCmbte."FechaComprobante", 'DD/MM/YYYY'), '') END
				WHEN ConsCmbte."IdTipoInstrumento" = 'SPT' THEN CASE
																WHEN ConsCmbteConDet."AccionTipoOperacion" = 'COMPRA' THEN 'VENTA FX-COMPRA ' 
																ELSE 'COMPRA FX-VENTA ' 
																END || CASE 
																		WHEN MovCom."NemotecnicoInstrumento" IN ('ARBDIS','ARBINT') AND OpSpt."IdOperacionSpot" = MovCom."IdTransaccion" AND MovCom."IdMovimientoComprobante" = OpSpt."IdComprobanteFacturacion" 
																			THEN OpSpt."IdMonedaPrincipal" || ' '  || ifNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteConDet."Cantidad",'dx',2, 'CL') || ' ', '') || ' a un valor de ' || TO_DECIMAL(ROUND(NULLIF(ConsCmbte."ValorTipoCambio",0), 2), 26, 2) || ' ' || OpSpt."IdMonedaPrincipal" || '/' || OpSpt."IdMonedaSecundaria"
																		WHEN MovCom."NemotecnicoInstrumento" IN ('ARBDIS','ARBINT') AND OpSpt."IdOperacionSpot" = MovCom."IdTransaccion" AND MovCom."IdMovimientoComprobante" <> OpSpt."IdComprobanteFacturacion" 
																			THEN OpSpt."IdMonedaSecundaria" || ' '  || ifNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteConDet."Cantidad",'dx',2, 'CL') || ' ', '') || ' a un valor de ' || TO_DECIMAL(ROUND(NULLIF(ConsCmbte."ValorTipoCambio",0), 2), 26, 2) || ' ' || OpSpt."IdMonedaSecundaria" || '/' || OpSpt."IdMonedaPrincipal"
																		WHEN MovCom."NemotecnicoInstrumento" IN ('DISTRI','INTERB') AND OpSpt."IdOperacionSpot" = MovCom."IdTransaccion" AND MovCom."IdMovimientoComprobante" = OpSpt."IdComprobanteFacturacion" 
																			THEN OpSpt."IdMonedaPrincipal" || ' '  || ifNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteConDet."Cantidad",'dx',2, 'CL') || ' ', '') || ' a un valor de ' || TO_DECIMAL(ROUND(NULLIF(ConsCmbte."ValorTipoCambio",0), 2), 26, 2) || ' ' || OpSpt."IdMonedaPrincipal" || '/' || OpSpt."IdMonedaSecundaria"	                                                        			
																	END		
				WHEN ConsCmbte."IdTipoComprobante" = 'FOE' AND :v_GlosaFacturaOtrosConceptosPeriodo = 'S' THEN IfNull(ConsCmbte."NombreConcepto" || ', ', '') || (SELECT "MES"
					FROM (SELECT TO_BIGINT(1) "NUMERO",'Enero' AS "MES" FROM DUMMY
						UNION ALL SELECT TO_BIGINT(2) "NUMERO",'Febrero' AS "MES" FROM DUMMY
						UNION ALL SELECT TO_BIGINT(3) "NUMERO",'Marzo' AS "MES" FROM DUMMY
						UNION ALL SELECT TO_BIGINT(4) "NUMERO",'Abril' AS "MES" FROM DUMMY
						UNION ALL SELECT TO_BIGINT(5) "NUMERO",'Mayo' AS "MES" FROM DUMMY
						UNION ALL SELECT TO_BIGINT(6) "NUMERO",'Junio' AS "MES" FROM DUMMY
						UNION ALL SELECT TO_BIGINT(7) "NUMERO",'Julio' AS "MES" FROM DUMMY
						UNION ALL SELECT TO_BIGINT(8) "NUMERO",'Agosto' AS "MES" FROM DUMMY
						UNION ALL SELECT TO_BIGINT(9) "NUMERO",'Septiembre' AS "MES" FROM DUMMY
						UNION ALL SELECT TO_BIGINT(10) "NUMERO",'Octubre' AS "MES" FROM DUMMY
						UNION ALL SELECT TO_BIGINT(11) "NUMERO",'Noviembre' AS "MES" FROM DUMMY
						UNION ALL SELECT TO_BIGINT(12) "NUMERO",'Diciembre' AS "MES" FROM DUMMY
						)
					WHERE "NUMERO" = TO_BIGINT(MONTH(ConsCmbte."FechaLiquidacion"))) || ' ' || Year(ConsCmbte."FechaLiquidacion") || '' || CASE 
					WHEN LENGTH(ConsCmbteConDet."Glosa") > 0 THEN (IfNull(', ' || ConsCmbteConDet."Glosa", ''))
				ELSE ''  END
				WHEN ConsCmbte."IdTipoComprobante" = 'FOE' THEN IfNull(ConsCmbte."NombreConcepto", '') || CASE WHEN LENGTH(ConsCmbteConDet."Glosa") > 0 THEN (IfNull(', ' || ConsCmbteConDet."Glosa", '')) ELSE ''  END
				WHEN ConsCmbte."IdTipoInstrumento" = 'SIM' THEN 	
							IfNull(ConsCmbteConDet."AccionTipoOperacion" || ' ', '') || IfNull(ConsCmbteConDet."DetalleGlosaFacturacion" || ' ', '')
							|| IfNull(ConsCmbteConDet."NemotecnicoInstrumento" || ' ', '') 
							|| CASE
									WHEN length(MarkMak."IdInstrumento") > 0 OR MarkMak."IdInstrumento" > 0 THEN 'APB '
									WHEN length(Inst."IdInstrumento") > 0 OR Inst."IdInstrumento" > 0 THEN 'APB '
									ELSE ''
								END
							|| IfNull (To_varchar (ConsCmbte."FechaLiquidacion", 'DD-MM-YYYY')|| ' ' , ' ') 
							|| 'Nota: Operacion de Simultanea ' || IfNull( CondLiq."Codigo" ||', ','' ) || ' Tasa de Referencia ' || IFNULL("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteConDet."Tasa", 'dx',2, 'CL')|| ', ', '')|| ' Nro. de dias '
							|| IfNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero" (ConsCmbte."DiasPlazo", 'i',0,'CL') || ', ', '')|| ' Fecha Vencimiento ' || IfNull (To_varchar (ConsCmbte."FechaVencimiento", 'DD-MM-YYYY'), ' ')|| ', Boveda '|| IfNull(ConsCmbte."IdBoveda" || ' ', '')             
					WHEN ConsCmbte."IdTipoComprobante" = 'FAE' THEN 
					CASE 
					WHEN ConsCmbte."IdClasificacionTipoInstrumento" = 'IF' THEN
						IfNull(ConsCmbteConDet."AccionTipoOperacion" || ' ', '')
						|| CASE WHEN ConsCmbteConDet."IndicadorSeparaTransacciones" = 'S' THEN IfNull(ConsCmbteConDet."FolioTransaccion" || ' ', '') ELSE '' END  
						|| IfNull(ConsCmbteConDet."DetalleGlosaFacturacion" || ' ', '')
						|| IfNull(MovCom."IdEmisor" || ' ', '') || 'Tasa ' || IfNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteConDet."Tasa", 'dx',2, 'CL') || '% ', '')  ||
						CASE WHEN ConsCmbteConDet."InstrumentoReajustable" = 'N' THEN 'NR ' ELSE '' END
						|| IfNull (To_varchar (ConsCmbteConDet."FechaVencimiento", 'DD-MM-YYYY'), ' ')
					WHEN ConsCmbte."IdClasificacionTipoInstrumento" = 'RF' THEN
						IfNull(ConsCmbteConDet."AccionTipoOperacion" || ' ', '') 
						|| CASE WHEN ConsCmbteConDet."IndicadorSeparaTransacciones" = 'S' THEN IfNull(ConsCmbteConDet."FolioTransaccion" || ' ', '') ELSE '' END  
						|| IfNull(ConsCmbteConDet."DetalleGlosaFacturacion" || ' ', '')
						|| IfNull(ConsCmbteConDet."NemotecnicoInstrumento" || ' ', '')  || 'Tasa ' || IfNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteConDet."Tasa", 'dx',2, 'CL') || '% ', '')
					ELSE
						IfNull(ConsCmbteConDet."AccionTipoOperacion" || ' ', '') || 
						IfNull(ConsCmbteConDet."FolioTransaccion" || ' ', '') || 
						IfNull(ConsCmbte."IdClasificacionTipoInstrumento" || ' ', '') || 
						CASE WHEN ConsCmbte."IdClasificacionTipoInstrumento" ='IF'  THEN ifNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(MovCom."ValorRescate",'dx',2, 'CL') || ' ', '') ELSE
						ifNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteConDet."Cantidad",'dx',2, 'CL') || ' ', '') END ||
						IfNull(InstEmisor."IdEmisor" || ' ', '') ||  
						CASE WHEN  ConsCmbte."IdClasificacionTipoInstrumento" ='IF' THEN IfNull(MovCom."IdEmisor" || ' ', '') ELSE
						IfNull(ConsCmbteConDet."NemotecnicoInstrumento" || ' ', '')
						END ||
						IfNull(ConsCmbte."IdBoveda" || ' ', '')
					END
					ELSE ConsCmbte."NombreConcepto"                                                  
				END
				WHEN ConsCmbte."IdTipoComprobante" IN ('NCR', 'NCE', 'NEE', 'NEO') THEN CASE
				WHEN ConsCmbte."IdConcepto" IN ('CMMNRV', 'ANVSPT','ANCSPT') AND LENGTH(ConsCmbte."Glosa") > 0 THEN 'ANULACION ' || ConsCmbte."Glosa"
				WHEN ConsCmbte."IdConcepto" IN ('ANTSIC', 'ANTSIV') AND LENGTH(ConsCmbte."Glosa") > 0 THEN ConsCmbte."Glosa"
				WHEN ConsCmbte."IdConcepto" = 'CMMNRV' THEN 'ANULACION COMISI�N TRIMESTRAL NO APV - Acciones, CFI, ETFs - Trimestre'
				WHEN ConsCmbte."IdConcepto" = 'CMTAPV' AND LENGTH(ConsCmbte."Glosa") > 0 THEN 'ANULACION ' || ConsCmbte."Glosa"
				WHEN ConsCmbte."IdConcepto" = 'CMTAPV' THEN 'ANULACION COMISI�N TRIMESTRAL APV - Acciones, CFI, ETFs - Trimestre'
				WHEN ConsCmbte."IdTipoInstrumento" IN ('ACN', 'ACX', 'CFI', 'CFX', 'FFM', 'ETN', 'ETX', 'MON', 'FIP', 'MIL') 
					THEN 'ANULACION ' ||
						IfNull(TipOpeNC."Accion" || ' ', '') -- || IfNull(ConsCmbteConDet."FolioTransaccion" || ' ', '')
					|| IfNull(ConsCmbteConDet."DetalleGlosaFacturacion" || ' ', '') || IfNull(ConsCmbteConDet."NemotecnicoInstrumento" || ' ', '') || 
					CASE WHEN length(MarkMak."IdInstrumento") > 0 OR MarkMak."IdInstrumento" > 0 THEN 'APB ' 
						WHEN length(Inst."IdInstrumento") > 0 OR Inst."IdInstrumento" > 0 THEN 'APB ' 
						WHEN ConsCmbte."IdTipoInstrumento" IN ('MON') THEN IfNull(ConsCmbteConDet."IdMoneda" || ' ', '') ELSE '' END || 
					CASE WHEN ConsCmbte."EsVentaCorta"= 'S' THEN IfNull(TO_NVARCHAR(ConsCmbte."FechaVencimiento", 'DD/MM/YYYY'), '') || ' Tasa ' || IfNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteConDet."Tasa", 'dx',2, 'CL') || '% ', '')
						ELSE IfNull(TO_NVARCHAR(ConsCmbte."FechaComprobante", 'DD/MM/YYYY'), '') END
				WHEN ConsCmbte."IdTipoInstrumento" = 'SPT' THEN 'ANULACION ' || CASE 
					WHEN TipOpeNC."Accion" = 'COMPRA' THEN 'VENTA FX-COMPRA '
						ELSE 'COMPRA FX-VENTA ' 
						END ||  CASE 
									WHEN MovCom."NemotecnicoInstrumento" IN ('ARBDIS','ARBINT') AND OpSpt."IdOperacionSpot" = MovCom."IdTransaccion" AND MovCom."IdMovimientoComprobante" = OpSpt."IdComprobanteFacturacion" 
										THEN OpSpt."IdMonedaPrincipal" || ' '  || ifNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteConDet."Cantidad",'dx',2, 'CL') || ' ', '') || ' a un valor de (T/C)' || OpSpt."IdMonedaPrincipal" || '/' || OpSpt."IdMonedaSecundaria"
									WHEN MovCom."NemotecnicoInstrumento" IN ('ARBDIS','ARBINT') AND OpSpt."IdOperacionSpot" = MovCom."IdTransaccion" AND MovCom."IdMovimientoComprobante" <> OpSpt."IdComprobanteFacturacion" 
										THEN OpSpt."IdMonedaSecundaria" || ' '  || ifNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteConDet."Cantidad",'dx',2, 'CL') || ' ', '') || ' a un valor de (T/C)' || OpSpt."IdMonedaSecundaria" || '/' || OpSpt."IdMonedaPrincipal"
									WHEN MovCom."NemotecnicoInstrumento" IN ('DISTRI','INTERB') AND OpSpt."IdOperacionSpot" = MovCom."IdTransaccion" AND MovCom."IdMovimientoComprobante" = OpSpt."IdComprobanteFacturacion" 
										THEN OpSpt."IdMonedaPrincipal" || ' '  || ifNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteConDet."Cantidad",'dx',2, 'CL') || ' ', '') || ' a un valor de (T/C)' || OpSpt."IdMonedaPrincipal" || '/' || OpSpt."IdMonedaSecundaria"
								END		
				WHEN ConsCmbte."IdTipoInstrumento" = 'SIM' THEN 
						'ANULACION ' || IfNull(ConsCmbteConDet."AccionTipoOperacion" || ' ', '') || IfNull(ConsCmbteConDet."DetalleGlosaFacturacion" || ' ', '')
					|| IfNull(ConsCmbteConDet."NemotecnicoInstrumento" || ' ', '')
					|| CASE
							WHEN length(MarkMak."IdInstrumento") > 0 OR MarkMak."IdInstrumento" > 0 THEN 'APB '
							WHEN length(Inst."IdInstrumento") > 0 OR Inst."IdInstrumento" > 0 THEN 'APB '
							ELSE ''
						END
					|| IfNull (To_varchar (ConsCmbte."FechaLiquidacion", 'DD-MM-YYYY')|| ' ' , ' ') 
					|| 'Nota: Operacion de Simultanea ' || IfNull( CondLiq."Codigo" ||', ','' ) || ' Tasa de Referencia ' || IFNULL("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteConDet."Tasa", 'dx',2, 'CL')|| ', ', '')|| ' Nro. de dias '
					|| IfNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero" (ConsCmbte."DiasPlazo", 'i',0,'CL') || ', ', '')|| ' Fecha Vencimiento ' || IfNull (To_varchar (ConsCmbte."FechaVencimiento", 'DD-MM-YYYY') , ' ')||
					CASE 
						WHEN ConsCmbte."IdComprobanteReferencia" > 0 
							THEN ', Factura Asociada ' || CmbteAsocANC."FolioComprobante" 
							ELSE ''
					END
					|| ', Boveda '|| IfNull(ConsCmbte."IdBoveda" || ' ', '')        
				WHEN ConsCmbte."IdClasificacionTipoInstrumento" = 'IF' THEN
					IfNull(ConsCmbteConDet."AccionAnulacionSubyacente" || ' ', '') 
					|| CASE WHEN ConsCmbteConDet."IndicadorSeparaTransacciones" = 'S' THEN IfNull(ConsCmbteConDet."FolioTransaccion" || ' ', '') ELSE '' END  
					|| IfNull(ConsCmbteConDet."DetalleGlosaFacturacion" || ' ', '')
					|| IfNull(MovCom."IdEmisor" || ' ', '') || 'Tasa ' || IfNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteConDet."Tasa", 'dx',2, 'CL') || '% ', '')  ||
					CASE WHEN ConsCmbteConDet."InstrumentoReajustable" = 'N' THEN 'NR ' ELSE '' END
					|| IfNull (To_varchar (ConsCmbteConDet."FechaVencimiento", 'DD-MM-YYYY'), ' ')
				WHEN ConsCmbte."IdClasificacionTipoInstrumento" = 'RF' THEN
					IfNull(ConsCmbteConDet."AccionAnulacionSubyacente" || ' ', '')
					|| CASE WHEN ConsCmbteConDet."IndicadorSeparaTransacciones" = 'S' THEN IfNull(ConsCmbteConDet."FolioTransaccion" || ' ', '') ELSE '' END  
					|| IfNull(ConsCmbteConDet."DetalleGlosaFacturacion" || ' ', '')
					|| IfNull(ConsCmbteConDet."NemotecnicoInstrumento" || ' ', '')  || 'Tasa ' || IfNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteConDet."Tasa", 'dx',2, 'CL') || '% ', '')
				ELSE ConsCmbte."NombreConcepto"                                                                
				END
			ELSE (IfNull(ConsCmbteConDet."AccionTipoOperacion" || ' ', '') || IfNull(ConsCmbteConDet."IdTipoInstrumento" || ',', '')  || ifNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteConDet."Cantidad",'dx',2, 'CL') || ' ', '') || IfNull(ConsCmbteConDet."NemotecnicoInstrumento" || ',', '') || CASE 
				WHEN ConsCmbteConDet."IdMonedaLiquidacion" = 'USD' THEN ifNull(TO_DECIMAL(ROUND(ConsCmbteConDet."Precio", 2), 26, 2) || MonLiq."Simbolo" || ',', '')
				ELSE ifNull(MonLiq."Simbolo" || TO_DECIMAL(ROUND(ConsCmbteConDet."Precio", 2), 26, 2) || ',', '') 
				END || IfNull(ConsCmbteConDet."FolioTransaccion" || ',', '') || ifnull(CASE 
					WHEN TO_VARCHAR(ConsCmbteConDet."FechaLiquidacion", 'DD-MM-YYYY') = '01-01-0001' THEN ''
					ELSE TO_VARCHAR(ConsCmbteConDet."FechaLiquidacion", 'DD-MM-YYYY')
					END, ''))
			END ||
			CASE 
		WHEN (ConsCmbte."IdTipoInstrumento" = 'SIM' AND ConsCmbte."IdMoneda" <> 'USD') THEN ''
		WHEN (ConsCmbte."EsVentaCorta" = 'S' AND ConsCmbte."IdMoneda" <> 'USD') THEN ''
		ELSE ''
		END
		END
		AS "Detalle",           
		CASE
	        WHEN ConsCmbte."IdTipoInstrumento" = 'SPT' and ConsCmbte."IdTipoComprobante" <> 'BOE' Then TO_DECIMAL(ROUND(NULLIF(ConsCmbte."ValorTipoCambio",0), 2), 26, 2) 
			ELSE ConsCmbteConDet."Precio"
		END AS Precio,
		CASE
			WHEN ConsCmbte."IdClasificacionTipoInstrumento" = 'RV' AND ConsCmbteConDet."IdMonedaLiquidacion" = 'USD' AND ConsCmbte."IdMonedaNegociacion" = 'CLP'
				THEN COALESCE(ROUND(NULLIF((ConsCmbte."ValorParidad" * ConsCmbteConDet."Precio"),0),26),0)
			WHEN ConsCmbte."IdClasificacionTipoInstrumento" = 'RV' AND ConsCmbteConDet."IdMonedaLiquidacion" = 'CLP' AND ConsCmbte."IdMonedaNegociacion" = 'USD'
				THEN COALESCE(ROUND(NULLIF((ConsCmbte."ValorTipoCambio" * ConsCmbteConDet."Precio"),0),26),0)
			ELSE 0
		END AS "PrecioOtraMoneda",
		CASE
			WHEN ConsCmbteConDet."Cantidad" !=0 AND ConsCmbte."IdClasificacionTipoInstrumento" = 'IF' AND ConsCmbte."IdTipoComprobante" = 'FAE' THEN MovCom."ValorRescate"
			WHEN ConsCmbteConDet."Cantidad" !=0 AND ConsCmbte."IdClasificacionTipoInstrumento" = 'IF' THEN ConsCmbteConDet."ValorRescate"
	        WHEN ConsCmbteConDet."Cantidad" !=0 THEN ConsCmbteConDet."Cantidad"
	        ELSE ConsCmbteConDet."Cantidad"
		END AS "Cantidad",        
		CASE 
	        WHEN ConsCmbte."CantidadTotal" = 0 THEN 0 
	      	ELSE (ConsCmbte."MontoLiquidacion" / ConsCmbte."CantidadTotal")  
		END AS "PrecioPromedio",
		ConsCmbteConDet."IdMoneda" AS "IdMonedaComprobanteDetalle",          
		CASE 
	      	WHEN v_Sistema = 'PE' AND ConsCmbte."IdMoneda" <> ConsCmbte."IdMonedaLiquidacion" AND ConsCmbte."ValorTipoCambio" > 0 THEN   ConsCmbte."ValorTipoCambio" 
	      	ELSE
	      		CASE 	
		            WHEN ConsCmbte."IdTipoInstrumento" = 'MIL' THEN 
                  CASE
                    WHEN ConsCmbte."ValorParidad" = 0 THEN 0
                    ELSE (COALESCE(ROUND((NULLIF(ConsCmbte."ValorTipoCambio",0)),4),0) / ConsCmbte."ValorParidad")
                    END
		            WHEN ConsCmbte."IdTipoInstrumento" in ('ACX', 'ETX') THEN COALESCE(ROUND((NULLIF(ConsCmbte."ValorTipoCambio",0)),4),0)
		            WHEN ConsCmbte."IdTipoInstrumento" = 'SPT' THEN ConsCmbteConDet."Precio" 
					WHEN ConsCmbte."IdTipoInstrumento" = 'CFI' THEN
		            	CASE
		            		WHEN ConsCmbteConDet."IdMonedaLiquidacion" = 'USD' AND ConsCmbte."Monto" != 0 THEN ROUND(ConsCmbteConDet."MontoComprobanteDetalle" /  ConsCmbte."Monto", 2)
		            		ELSE 0
		            	END
		            WHEN ConsCmbte."ValorTipoCambio" = 1 OR ConsCmbte."ValorTipoCambio" < 0 THEN 0
		            WHEN ConsCmbte."IdClasificacionTipoInstrumento" = 'RF' THEN COALESCE(ROUND((NULLIF(ConsCmbte."ValorTipoCambio",0)),4),0)
		            WHEN ConsCmbte."IdTipoInstrumento" in ('ACN') THEN 
		                CASE 
		                    WHEN ConsCmbteConDet."IdMonedaLiquidacion" = 'USD' THEN COALESCE(ROUND((NULLIF(ConsCmbte."ValorTipoCambio",0)),4),0)  
		                    ELSE 0	
		                END
					ELSE 0    
			END 
		END AS "ValorTipoCambio",          
		CAST(Mon."Codigo" AS VARCHAR(200)) AS "CodigoMonedaComprobanteDetalle",
		ConsCmbteConDet."IdMonedaComprobante" AS "IdMonedaComprobanteComprobanteDetalle",
		MonLoc."Codigo" AS "CodigoMonedaComprobanteComprobanteDetalle",
		CAST(MonLoc."Simbolo" AS VARCHAR(200)) AS "SimboloMonedaComprobanteComprobanteDetalle",
	    CASE 
	        WHEN ConsCmbte."IdTipoComprobante" IN ('NEO','NCE','NEE') AND UPPER(ConsCmbteConDet."AccionTipoOperacion") = 'VENTA' THEN COALESCE("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(ConsCmbteConDet."MontoComprobanteDetalle", FormCmbteLoc."MontoDecimales", FormCmbteLoc."MontoTrucaRedondea"), ConsCmbteConDet."MontoComprobanteDetalle")
            WHEN ConsCmbte."IdTipoComprobante" NOT IN ('NEO','NCE','NEE') AND UPPER(ConsCmbteConDet."AccionTipoOperacion") = UPPER('COMPRA') AND ConsCmbte."IdTipoComprobante" = 'FAE' AND ConsCmbte."IdClasificacionTipoInstrumento" = 'IF' THEN MovCom."MontoMovimientoComprobante"
	        WHEN ConsCmbte."IdTipoComprobante" NOT IN ('NEO','NCE','NEE') AND UPPER(ConsCmbteConDet."AccionTipoOperacion") IN (UPPER('COMPRA'), UPPER('RESCATE')) THEN COALESCE("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(ConsCmbteConDet."MontoComprobanteDetalle", FormCmbteLoc."MontoDecimales", FormCmbteLoc."MontoTrucaRedondea"), ConsCmbteConDet."MontoComprobanteDetalle")
	      	ELSE 0
		END  AS "Compra",
		CASE
	        WHEN ConsCmbte."IdTipoComprobante" IN ('NEO','NCE','NEE') AND UPPER(ConsCmbteConDet."AccionTipoOperacion") IN (UPPER('COMPRA'), UPPER('RESCATE')) THEN COALESCE("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(ConsCmbteConDet."MontoComprobanteDetalle", FormCmbteLoc."MontoDecimales", FormCmbteLoc."MontoTrucaRedondea"), ConsCmbteConDet."MontoComprobanteDetalle")
	        WHEN ConsCmbte."IdTipoComprobante" NOT IN ('NEO','NCE','NEE') AND UPPER(ConsCmbteConDet."AccionTipoOperacion") = UPPER('VENTA') AND ConsCmbte."IdTipoComprobante" = 'FAE' AND ConsCmbte."IdClasificacionTipoInstrumento" = 'IF' THEN MovCom."MontoMovimientoComprobante"
	      	WHEN ConsCmbte."IdTipoComprobante" NOT IN ('NEO','NCE','NEE') AND UPPER(ConsCmbteConDet."AccionTipoOperacion") = UPPER('VENTA') THEN COALESCE("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(ConsCmbteConDet."MontoComprobanteDetalle", FormCmbteLoc."MontoDecimales", FormCmbteLoc."MontoTrucaRedondea"), ConsCmbteConDet."MontoComprobanteDetalle")
	        ELSE 0
		END AS "Venta",
	    ConsCmbteConDet."IdMonedaLiquidacion" AS "IdMonedaLiquidacion",
		MonLiq."Codigo" AS "CodigoMonedaLiquidacion",
		MonLiq."Simbolo" AS "SimboloMonedaLiquidacion",          
		TO_VARCHAR(:v_FechaSistema, 'DD-MM-YYYY') AS "FechaSistema",  	      
		CASE
	        WHEN LENGTH(PersEmisor."RazonSocial") > 0 THEN PersEmisor."RazonSocial"
			ELSE ''
		END AS "RazonSocialEmisor",  	      
		CASE 
			WHEN LENGTH (GiroEmisor."Nombre") > 0 THEN CAST(GiroEmisor."Nombre" AS Nvarchar(50))
	      	ELSE 'En definicion'
		END AS "GiroEmisor",
		CASE 
			WHEN LENGTH(GiroReceptor."Nombre") > 0 THEN GiroReceptor."Nombre"
			ELSE 'En definicion'
		END AS "GiroReceptor",
		COALESCE(ConsEmp."TipoDocumentoIdentidadAdicional", ConsEmp."TipoDocumentoIdentidad", '') AS "TipoDocumentoIdentidadEmisor",
		COALESCE(ConsEmp."DocumentoIdentidadAdicional", ConsEmp."DocumentoIdentidad", '') AS "DocumentoIdentidadEmisor",  	      
		v_DireccionEmpresa AS "DireccionEmisor",
		ConsEmp."DireccionFisicaPrincipalNombreDivisionPais" AS "ComunaEmisor",
		ConsEmp."DireccionFisicaPrincipalNombreCiudad" AS "CiudadEmisor",
		CAST(ConsEmp."TelefonoPrincipal" AS VARCHAR(200)) AS "TelefonoEmisor",
		CAST(ConsEmp."DireccionElectronicaPrincipal" AS VARCHAR(200)) AS "DireccionElectronicaEmisor",
		CASE
	        WHEN ConsCmbte."IdComprobanteReferencia" > 0 AND ConsCmbte."IdTipoComprobante" IN ('NCE', 'NCR', 'NEO') THEN 'NOTA DE CREDITO ASOCIADA A FACTURA' || ' ' || ConsCmbte."IdComprobanteReferencia"
	        WHEN CmbteAsocANC."IdComprobante" > 0  AND CmbteAsocANC."IdTipoComprobante" IN ('NCE', 'NCR', 'NEO') THEN 'FACTURA ASOCIADA A NOTA DE CREDITO' || ' ' || CmbteAsocANC."IdComprobante"
	      	ELSE ''
		END AS "GlosaNCredito",  	      
		TipCmbteNC."TipoComprobanteDTEPE" AS "IdTipoComprobanteAsocANC",
		TipCmbteNC."Nombre" AS "TipoComprobanteAsocANC",
		COALESCE(CmbteAsocANC."IdComprobante",0) AS "IdComprobanteReferenciaAsocANC",
		COALESCE(CmbteAsocANC."FolioComprobante",0) AS "FolioComprobanteReferenciaAsocANC",  	       
		TO_VARCHAR(CmbteAsocANC."FechaComprobante", 'DD-MM-YYYY') AS "FechaComprobanteAsocANC",
		CASE 
	        WHEN CmbteAsocANC."Monto" = ConsCmbte."Monto" THEN ('ANULACI�N' || ' ' || TipCmbteNC."Nombre")
	      	ELSE 'CORRIGE MONTO'  
		END AS "Motivo",
		CASE
	        WHEN ConsCmbteConDet."IdTipoOperacion" IN ('EMILIB', 'DIVACN') THEN '' 
	        WHEN ConsCmbteConDet."IdTipoOperacion" IN ('COMSPT','VENSPT') THEN
	        	(CASE WHEN ConsCmbte."IdTipoComprobante" IN ('FAC', 'FAE', 'FOE','FOC', 'BOE', 'FEE', 'FOX', 'NCE') AND ConsTipOpe."Accion" = 'VENTA' THEN 'en CONTRA'
	        		WHEN ConsCmbte."IdTipoComprobante" IN ('FAC', 'FAE', 'FOE','FOC', 'BOE', 'FEE', 'FOX', 'NCE') AND ConsTipOpe."Accion" = 'COMPRA' AND (ConsCmbte."MontoLiquidacion" - COALESCE(ConsCmbte."MontoLiquidacionCobro", 0) > 0) THEN 'a FAVOR'
	        		WHEN ConsCmbte."IdTipoComprobante" IN ('FAC', 'FAE', 'FOE','FOC', 'BOE', 'FEE', 'FOX', 'NCE') AND ConsTipOpe."Accion" = 'COMPRA' AND (ConsCmbte."MontoLiquidacion" - COALESCE(ConsCmbte."MontoLiquidacionCobro", 0) < 0) THEN 'en CONTRA'
	        	END)
	        WHEN ConsCmbte."IdTipoComprobante" IN ('FAC', 'FAE', 'FOE','FOC', 'BOE', 'FEE', 'FOX', 'NCE') AND ConsTipOpe."Accion" = 'COMPRA' THEN 'en CONTRA'
	        WHEN ConsCmbte."IdTipoComprobante" IN ('FAC', 'FAE', 'FOE','FOC', 'BOE', 'FEE', 'FOX', 'NCE') AND ConsTipOpe."Accion" = 'VENTA' AND (ConsCmbte."MontoLiquidacion" - COALESCE(ConsCmbte."MontoLiquidacionCobro", 0) > 0) THEN 'a FAVOR'
	        WHEN ConsCmbte."IdTipoComprobante" IN ('FAC', 'FAE', 'FOE','FOC', 'BOE', 'FEE', 'FOX', 'NCE') AND ConsTipOpe."Accion" = 'VENTA' AND (ConsCmbte."MontoLiquidacion" - COALESCE(ConsCmbte."MontoLiquidacionCobro", 0) < 0) THEN 'en CONTRA'
	      	ELSE ''
		END AS "GlosaSaldo",          
		CASE
	        WHEN ConsCmbteConDet."IdTipoOperacion" IN ('EMILIB', 'DIVACN') THEN ''
	        WHEN ConsCmbte."IdConcepto" = 'COMMEN' THEN ''         
	      	ELSE CASE WHEN length(MarkMak."IdInstrumento") > 0 OR MarkMak."IdInstrumento" > 0 THEN 'APB: Accion con Presencia Bursatil'
	                WHEN length(Inst."IdInstrumento") > 0 OR Inst."IdInstrumento" > 0 THEN 'APB: Accion con Presencia Bursatil' ELSE '' END
		END AS "GlosaAPB",          
		CASE 
	        WHEN ConsCmbte."EsVentaCorta"= 'S' THEN ConsCmbteConDet."DetalleGlosaTipoProducto" || ' VENTA CORTA' 
	        WHEN ConsCmbteConDet."IdTipoOperacion" IN ('EMILIB', 'DIVACN') THEN '' 
	        WHEN ConsCmbte."IdConcepto" = 'COMMEN' THEN 'FACTURACION COMISION MENSUAL' 
	      	ELSE ConsCmbteConDet."DetalleGlosaTipoProducto"
		END AS "GlosaTipoProducto",
		CASE 
	        WHEN Coalesce(ConsCmbteConDet."NemotecnicoInstrumento", '') = '' THEN ConsCmbteConDet."IdConcepto"   
	      	ELSE ConsCmbteConDet."CodigoTipoProducto"
		END AS "Codigo",          
		CASE
	      	WHEN v_Sistema = 'CL' THEN 
	      	CASE 
	            WHEN (COALESCE(ConsCmbteConDet."NemotecnicoInstrumento", '') = '' AND ConsCmbteConDet."IdTipoComprobante" IN ('BOE', 'FOE', 'FOX', 'NEO', 'NCE') AND ConsCmbte."ConcepEsReembolso" = 'S') THEN (ConsCmbte."NombreConcepto" || COALESCE((' - ' || ConsCmbteConDet."Glosa"), ''))
	            WHEN (COALESCE(ConsCmbteConDet."NemotecnicoInstrumento", '') = '' AND ConsCmbteConDet."IdTipoComprobante" IN ('BOE', 'FOE', 'FOX', 'NEO', 'NCE') AND ConsCmbte."ConcepEsReembolso" = 'N') THEN ConsCmbteConDet."Glosa"
	            WHEN (COALESCE(ConsCmbteConDet."NemotecnicoInstrumento", '') = '' AND ConsCmbteConDet."IdTipoComprobante" NOT IN ('BOE', 'FOE', 'FOX', 'NEO')) THEN (ConsCmbteConDet."NombreTipoOperacion" || ' - ' || ConsCmbteConDet."NombreConcepto")
	            ELSE ConsCmbte."NombreTipoProducto"
			END 
	        WHEN v_Sistema = 'PE' THEN
	        CASE 
	        	WHEN (COALESCE(ConsCmbteConDet."NemotecnicoInstrumento", '') = '' AND ConsCmbteConDet."IdTipoComprobante" IN ('BOE',  'FOX', 'NEO', 'NCE') AND ConsCmbte."ConcepEsReembolso" = 'S') THEN (ConsCmbte."NombreConcepto" || COALESCE((' - ' || ConsCmbteConDet."Glosa"), ''))
	        	WHEN (COALESCE(ConsCmbteConDet."NemotecnicoInstrumento", '') = '' AND ConsCmbteConDet."IdTipoComprobante" IN ('BOE',  'FOX', 'NEO', 'NCE') AND ConsCmbte."ConcepEsReembolso" = 'N') THEN ConsCmbteConDet."Glosa"
	        	WHEN (COALESCE(ConsCmbteConDet."NemotecnicoInstrumento", '') = '' AND ConsCmbteConDet."IdTipoComprobante" IN ('FOE')) 																THEN (ConsCmbteConDet."NombreConcepto" || ' - ' || ConsCmbteConDet."Glosa")
	        	WHEN (COALESCE(ConsCmbteConDet."NemotecnicoInstrumento", '') = '' AND ConsCmbteConDet."IdTipoComprobante" NOT IN ('BOE', 'FOX', 'NEO','FOE')) THEN (ConsCmbteConDet."NombreTipoOperacion" || ' - ' || ConsCmbteConDet."NombreConcepto")
	        	ELSE ConsCmbte."NombreTipoProducto"
	        END 
		END AS "DescripcionPE",
		"LOGO"."LogoCorredora" as "LogoCorredora",
		ConsCmbte."ModPrefFact",
		CASE
	        WHEN ConsCmbte."IdClasificacionTipoInstrumento" = 'RF' THEN Inst."CorteMinimo"
	      	ELSE 1
		END AS "ValorUnitario",
		COALESCE(ConsCmbteCobPe."ComisionBVLMonedaLiquidacion", 0) AS "ComisionBVLMonedaLiquidacion",
		COALESCE(ConsCmbteCobPe."ComisionSABMonedaLiquidacion", 0) AS "ComisionSABMonedaLiquidacion",
		COALESCE(ConsCmbteCobPe."ComisionSMVMonedaLiquidacion", 0) AS "ComisionSMVMonedaLiquidacion",
		COALESCE(ConsCmbteCobPe."ComisionCavaliMonedaLiquidacion", 0) AS "ComisionCavaliMonedaLiquidacion",
		COALESCE(ConsCmbteCobPe."FondoGarantiaMonedaLiquidacion", 0) AS "FondoGarantiaMonedaLiquidacion",
		COALESCE(ConsCmbteCobPe."FondoLiquidacionMonedaLiquidacion", 0) AS "FondoLiquidacionMonedaLiquidacion",
		COALESCE(ConsCmbteCobPe."OtrasComisionesMonedaLiquidacion", 0) AS "OtrasComisionesMonedaLiquidacion",
		COALESCE(ConsCmbteCobPe."ImpuestoIGVMonedaLiquidacion", 0) AS "ImpuestoIGVMonedaLiquidacion",
		ConsCmbte."FechaVencimiento",
		ConsCmbte."Tasa",
		ConsCmbte."DiasPlazo",
		COALESCE(ConsCmbte."NombreBovedaCuentaCustodioPortafolio",'') AS "NombreBovedaCuentaCustodioPortafolio",
		COALESCE(TIPOCUSTODIA."NombreTipoCustodia",'') AS "NombreTipoCustodia",
		CASE "PERCUS"."Tipo" 
			WHEN 'N' THEN "PERCUS"."Nombres" ||' '|| "PERCUS"."ApellidoPaterno" || ' ' ||"PERCUS"."ApellidoMaterno" 
	        WHEN 'M' THEN "PERCUS"."Nombres" ||' '|| "PERCUS"."ApellidoPaterno" || ' ' ||"PERCUS"."ApellidoMaterno" 
	      	ELSE "PERCUS"."RazonSocial"
		END AS "NombreCompletoBancoCustodio",
		CASE "PERLIQ"."Tipo" 
	        WHEN 'N' THEN "PERLIQ"."Nombres" ||' '|| "PERLIQ"."ApellidoPaterno" || ' ' ||"PERLIQ"."ApellidoMaterno" 
	        WHEN 'M' THEN "PERLIQ"."Nombres" ||' '|| "PERLIQ"."ApellidoPaterno" || ' ' ||"PERLIQ"."ApellidoMaterno" 
			ELSE "PERLIQ"."RazonSocial"
		END AS "NombreCompletoBancoLiquidador",  
		CCP."Codigo" AS "CodigoDeCuenta",
		ConsCmbte."Intereses",
		ConsCmbte."ReajustesVAC",          
		CASE 
			WHEN ConsCmbte."IdTipoInstrumento" in ('MIL', 'ACX', 'ETX', 'CFX', 'CFI') THEN 
				CASE 
				WHEN ConsCmbte."IdMonedaLiquidacion" = 'USD' AND ConsCmbte."IdMonedaNegociacion" = 'CLP' THEN Coalesce(ROUND(NULLIF((1/ConsCmbte."ValorParidad"),0),2),0)
				WHEN ConsCmbte."IdMoneda" IN ('USD','CLP') 
				THEN Coalesce(ROUND((NULLIF(ConsCmbte."ValorTipoCambio",0)),26),0)
	            ELSE 
                	CASE
                    WHEN ConsCmbte."ValorParidad" IS NULL OR ConsCmbte."ValorParidad" = 0 
                    THEN 0
                    ELSE Coalesce(ROUND((1/ConsCmbte."ValorParidad"),26),0)
                    END 
                END
	        WHEN ConsCmbte."IdTipoInstrumento" = 'SPT' THEN 0
		When ConsCmbte."IdClasificacionTipoInstrumento" = 'IF' and ConsCmbte."IdMonedaLiquidacion" = 'USD' THEN Coalesce(ROUND((NULLIF(ConsCmbte."ValorTipoCambio",0)),26),0)
	        When ConsCmbte."IdClasificacionTipoInstrumento" = 'RF' and ConsCmbte."IdMonedaLiquidacion" = 'USD' THEN Coalesce(ROUND((NULLIF(ConsCmbte."ValorTipoCambio",0)),26),0)
	        WHEN ConsCmbte."ValorParidad" <= 1 AND NOT ConsCmbte."IdTipoInstrumento" IN ('ACN')  THEN 0
			WHEN ConsCmbte."IdClasificacionTipoInstrumento" = 'RV' AND ConsCmbte."IdMonedaLiquidacion" = 'USD' AND ConsCmbte."IdMonedaNegociacion" = 'CLP' THEN Coalesce(ROUND(NULLIF((1/ConsCmbte."ValorParidad"),0),2),0)
	        WHEN ConsCmbte."IdTipoInstrumento" in ('ACN') THEN 
		    	CASE 
		           WHEN ConsCmbteConDet."IdMonedaLiquidacion" = 'USD' THEN COALESCE(ROUND(NULLIF(ConsCmbte."ValorTipoCambio",0),4),0)  
		           ELSE 0	
		           END	        
			ELSE ConsCmbte."ValorParidad"
		END AS "ValorParidad",
		CASE When ConsCmbte."IdTipoInstrumento" in ('MIL', 'ACX', 'ETX', 'CFX') AND ConsCmbte."IdMoneda" NOT IN ('USD','CLP')
	        THEN  Coalesce( ROUND(ConsCmbte."ValorTipoCambio" * NULLIF(ConsCmbte."ValorParidad",0),26),0)
		    ELSE 0
	    End As "ValorParidadSecundaria",
		CASE WHEN ConsCmbte."IdTipoComprobante" = 'FOE' THEN 'N' ELSE 'S' END AS "MuestraObservacion",
		ConsCmbte."IdEstadoModPrefFact",
		CAST(ConsCmbte."IdConcepto" AS NVARCHAR(6)) AS "IdConceptoCmbte",
		ConsCmbte."IdClasificacionTipoInstrumento",
	    CAST(COALESCE(FormCmbteLoc."PrecioDecimales", 0) AS INTEGER) AS "PrecioDecimales",
		CAST(COALESCE(FormCmbteLoc."TasaDecimales", 0) AS INTEGER) AS "TasaDecimales",
		
		-- Referencia para movimientos de otros conceptos
    CASE
      WHEN (ConsCmbte."IdTipoComprobante" IN ('FOE', 'FOX', 'NEO') AND (MCR."Referencia" IS NOT NULL) AND (MC."TipoReferencia" IS NOT NULL))
      THEN MC."TipoReferencia"
      ELSE ''
      END AS "TipoReferenciaOtrosConceptos",
    CASE
      WHEN (ConsCmbte."IdTipoComprobante" IN ('FOE', 'FOX', 'NEO') AND (MCR."Referencia" IS NOT NULL) AND (MC."TipoReferencia" IS NOT NULL))
      THEN MCR."Referencia"
      ELSE ''
      END AS "FolioReferenciaOtrosConceptos",
    CASE
      WHEN (ConsCmbte."IdTipoComprobante" IN ('FOE', 'FOX', 'NEO') AND (MCR."Referencia" IS NOT NULL) AND (MC."TipoReferencia" IS NOT NULL))
      THEN TO_NVARCHAR(TO_DATE(ConsCmbte."FechaComprobante"), 'DD-MM-YYYY')
      ELSE '01-01-0001'
      END AS "FechaReferenciaOtrosConceptos",
    CASE
      WHEN (ConsCmbte."IdTipoComprobante" IN ('FOE', 'FOX', 'NEO') AND (MCR."Referencia" IS NOT NULL) AND (MC."TipoReferencia" IS NOT NULL))
      THEN SUBSTRING(MC."Razon", 1, 18)
      ELSE ''
      END AS "RazonReferenciaOtrosConceptos",
	  ConsCmbteConDet."IdTipoOperacion",
	  Compbte."MontoLiquidacion" + Compbte."MontoLiquidacionCobro" AS "ValorAPagar",
    -- FIN: Referencia para movimientos de otros conceptos
    	ConsCmbte."Glosa" AS "Glosa"
	FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" As ConsCmbte
	LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" As CmbteAsocANC
	 	ON CmbteAsocANC."IdComprobante" = ConsCmbte."IdComprobanteReferencia"
	 	AND CmbteAsocANC."IdEstadoComprobante" <> 'ANU'
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipOpeNC 
	 	ON TipOpeNC."IdTipoOperacion" = CmbteAsocANC."IdTipoOperacion"
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" As ConsTipOpe
	 	ON ConsTipOpe."IdTipoOperacion" = ConsCmbte."IdTipoOperacion"	     
	INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalle" As ConsCmbteConDet
	 	ON ConsCmbteConDet."IdComprobante" = ConsCmbte."IdComprobante"
	INNER JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" As CondLiq
	 	ON CondLiq."IdCondicionDeLiquidacion" = ConsCmbte."IdCondicionLiquidacion"   
    LEFT JOIN (                    
				SELECT MovCom."NemotecnicoInstrumento",MovCom."IdTransaccion",MovCom."IdMovimientoComprobante",MovCom."IdEmisor",MovCom."ValorRescate",MovCom."MontoMovimientoComprobante",MovCmbteDet."IdComprobanteDetalle" 
				FROM BSCL."BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" AS MovCmbteDet
				LEFT JOIN BSCL."BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS MovCom
				ON MovCom."IdMovimientoComprobante" = MovCmbteDet."IdMovimientoComprobante"
				WHERE MovCmbteDet."IdTipoInstrumento" ='SPT'
				UNION ALL 
				SELECT MovCom."NemotecnicoInstrumento",MovCmbteDet."IdComprobanteDetalle" AS "IdTransaccion", MovCmbteDet."IdComprobanteDetalle"AS "IdMovimientoComprobante",MovCom."IdEmisor",sum (MovCom."ValorRescate"),sum (MovCom."MontoMovimientoComprobante"),MovCmbteDet."IdComprobanteDetalle" 
				FROM BSCL."BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" AS MovCmbteDet
				inner JOIN BSCL."BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS MovCom
				ON MovCom."IdMovimientoComprobante" = MovCmbteDet."IdMovimientoComprobante"
				WHERE MovCmbteDet."IdClasificacionTipoInstrumento"= 'IF' 
				GROUP BY MovCom."NemotecnicoInstrumento",MovCom."IdEmisor",MovCmbteDet."IdComprobanteDetalle" 
               )AS MovCom ON Movcom."IdComprobanteDetalle" = ConsCmbteConDet."IdComprobanteDetalle" 
	LEFT JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" AS OpSpt 
		ON MovCom."IdTransaccion" = OpSpt."IdOperacionSpot"	
	LEFT JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante" AS TipCmbteNC 
	 	ON TipCmbteNC."IdTipoComprobante" = CmbteAsocANC."IdTipoComprobante"
	 LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLI"   ON ( ConsCmbte."IdCliente" = "CLI"."IdCliente" )    
	 Left Join "BSCL.SocioNegocio.BD::PER.Persona" As PER     On PER."IdPersona" = CLI."IdPersona" 
	 LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
	 LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENTADICIONAL ON PER."IdDocumentoIdentidadAdicional" = IDENTADICIONAL."IdDocumentoIdentidad"  
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" As DirFis
	     On DirFis."IdDireccionFisica" = Per."IdDireccionFisicaPrincipal"
		AND DirFis."IdEstado" = 'ACT' 
	LEFT JOIN "BSCL.Plataforma.BD::REG.Ciudad" As Ciudad
	 	ON Ciudad."IdCiudad" = DirFis."IdCiudad"
	LEFT JOIN "BSCL.Plataforma.BD::REG.DivisionPais" As DivPais
	 	ON DivPais."IdDivisionPais" = DirFis."IdDivisionPais"
	LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" AS Pais
	     ON Pais."IdPais" = Per."IdPaisResidencia"	     
	LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" As FormCmbteLoc
	 	ON FormCmbteLoc."IdMoneda" = ConsCmbteConDet."IdMonedaComprobante"
		AND FormCmbteLoc."IdTipoOperacion" = ''
		AND FormCmbteLoc."IdTipoInstrumento" = ConsCmbte."IdTipoInstrumento"
		AND FormCmbteLoc."IdUsoMonedaFormato" = 'GEN'                
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" As MonLoc
	 	ON MonLoc."IdMoneda" = ConsCmbteConDet."IdMonedaComprobante"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" As Mon
	 	ON Mon."IdMoneda" = ConsCmbteConDet."IdMoneda"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" As MonLiq
	 	ON MonLiq."IdMoneda" = ConsCmbteConDet."IdMonedaLiquidacion"
	LEFT JOIN (
		SELECT
			EMP."IdEmpresa",
			PER."IdPersona",
			PER."DocumentoIdentidad",
			PER."TipoDocumentoIdentidad",
			PER."DocumentoIdentidadAdicional",
			PER."TipoDocumentoIdentidadAdicional",
			ELE."Direccion" AS "DireccionElectronicaPrincipal",
			CIU."NombreCiudad" AS "DireccionFisicaPrincipalNombreCiudad",
			DIV."NombreDivisionPais" AS "DireccionFisicaPrincipalNombreDivisionPais",
			TEL."Numero" AS "TelefonoPrincipal",
			INSC."IdCorredora"
		FROM "BSCL.Plataforma.BD::PLA.Empresa" AS EMP
		LEFT OUTER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC
		    ON EMP."IdSocioDeNegocio" = SOC."IdSocioDeNegocio" AND SOC."Borrado"='N'
		LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER
		    ON PER."IdPersona" = SOC."IdPersona"
		LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS ELE
		    ON PER."IdDireccionElectronicaPrincipal" = ELE."IdDireccionElectronica"
		LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" AS FIS
		    ON PER."IdDireccionFisicaPrincipal" = FIS."IdDireccionFisica"
		LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.Ciudad" AS CIU
		    ON FIS."IdCiudad" = CIU."IdCiudad"
		LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.DivisionPais" AS DIV
		    ON FIS."IdDivisionPais" = DIV."IdDivisionPais"
		LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.Telefono" AS TEL
		    ON PER."IdTelefonoPrincipal" = TEL."IdTelefono"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Corredora" INSC
			ON INSC."IdPersona" = PER."IdPersona"
	) AS ConsEmp
	 	ON ConsEmp."IdEmpresa" = :p_IdEmpresa 
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PersEmisor
		ON PersEmisor."IdPersona" = ConsEmp."IdPersona"
	LEFT JOIN "BSCL.Plataforma.BD::REG.Giro" As GiroEmisor
		ON GiroEmisor."IdGiro" = PersEmisor."IdGiroPrincipal"  
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::REG.ConsultarGiro" AS GiroReceptor
		ON GiroReceptor."IdGiro" = PER."IdGiroPrincipal"     
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Inst
		ON Inst."IdInstrumento" = 
		CASE 
			WHEN ConsCmbteConDet."IdTipoInstrumento" = 'SIM' 
			THEN ConsCmbteConDet."IdInstrumentoSubyacente" 
			ELSE ConsCmbteConDet."IdInstrumento"
		END
		AND (Inst."PresenciaBursatilAjustada" >= v_IndicadorPresenciaBursatil OR ConsCmbte."IdClasificacionTipoInstrumento" = 'DV')
	LEFT JOIN "BSCL.Plataforma.BD::INS.MarketMaker" AS MarkMak
		ON MarkMak."IdInstrumento" = 
			CASE 
				WHEN ConsCmbteConDet."IdTipoInstrumento" = 'SIM' 
				THEN ConsCmbteConDet."IdInstrumentoSubyacente" 
				ELSE ConsCmbteConDet."IdInstrumento"
			END
		AND ((ConsCmbte."FechaComprobante" >= MarkMak."FechaInicio") AND (ConsCmbte."FechaComprobante" <= MarkMak."FechaFin"))
		AND MarkMak."IdEstado" = 'ACT' AND ConsEmp."IdCorredora" = MarkMak."IdCorredora"
	LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobrosPeruTotalizados" AS ConsCmbteCobPe 
		ON ConsCmbteCobPe."IdComprobante" = ConsCmbte."IdComprobante"
	LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" as CCP 
		ON CCP."IdCuentaCustodioPortafolio" = ConsCmbte."IdCuentaCustodioPortafolio"
	LEFT JOIN "BSCL.Portafolio.BD::POR.TipoCustodia" AS TIPOCUSTODIA 
		ON TIPOCUSTODIA."IdTipoCustodia" = CCP."IdTipoCustodia"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.BancoCustodio" BANCOLIQ 
		ON BANCOLIQ."IdPersona" = CCP."IdPersonaBancoLiquidador"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PERCUS 
		ON PERCUS."IdPersona" = BANCOLIQ."IdPersona"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.BancoCustodio" BANCOCUSTODIO 
		ON BANCOCUSTODIO."IdPersona" = CCP."IdPersonaBancoCustodio" 
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PERLIQ 
		ON PERLIQ."IdPersona" = BANCOCUSTODIO."IdPersona"
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS InstEmisor
		ON InstEmisor."IdInstrumento" = ConsCmbteConDet."IdInstrumento"
	LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobanteReferencia" AS MCR
    ON ConsCmbte."IdComprobante" = CASE WHEN MCR."IdComprobante" != '' THEN TO_BIGINT(MCR."IdComprobante") ELSE -1 END
  LEFT JOIN "BSCL.Facturacion.BD::FAC.ReferenciaDocumento" AS MC
    ON MCR."TipoReferencia" = MC."TipoReferencia"
	LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" As Compbte
		ON Compbte."IdComprobante" = ConsCmbte."IdComprobante"
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR
		ON POR."IdPortafolio" = ConsCmbte."IdPortafolio"		
	
	Cross Join :v_logo As Logo
	WHERE ConsCmbte."IdComprobante" = :p_IdComprobante;
	 
END;

GO