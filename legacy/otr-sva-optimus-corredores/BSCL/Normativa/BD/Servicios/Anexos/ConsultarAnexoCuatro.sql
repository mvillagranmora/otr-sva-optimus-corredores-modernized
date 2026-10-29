SET SCHEMA "BSCL";
GO 
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.Anexos::ConsultarAnexoCuatro";
GO 
CREATE PROCEDURE 
--  Tecnologia : CrystalReports
--  Proposito: Servicio para consultar información para emitir el Anexo 13.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 25/07/2017

"BSCL.Normativa.BD.Servicios.Anexos::ConsultarAnexoCuatro" ( 

	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa			BIGINT,

    --VARIABLES DEL PROCESO
	IN	p_FechaOperacionDesde					NVARCHAR(10)	DEFAULT '',--'YYYY-MM-DD'
	IN	p_FechaOperacionHasta					NVARCHAR(10)	DEFAULT '',--'YYYY-MM-DD'
	IN	p_IdCliente 							BIGINT 			DEFAULT '0',

    --VARIABLES DE SALIDA OBLIGATORIAS

	OUT p_out_TablaAnexo4	"BSCL.Normativa.BD::NOR.SALIDA_ANEXO4"

)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
		--------------------
		--Declare Variables
		--------------------
		DECLARE v_IdNormativa 								NVARCHAR(15):='ANEXO';
		DECLARE v_IdTipoDocumento   		 				NVARCHAR(10):='ANEXO4';
		DECLARE v_NombreDestinatario						NVARCHAR(100):='';
		DECLARE v_CargoDestinatario							NVARCHAR(40):='';				
		
  		DECLARE v_fechaHoraSistema 							 NVARCHAR(40):='';
  		DECLARE v_FechaOperacionDesde, v_FechaOperacionHasta NVARCHAR(10):='';
		DECLARE v_IdPersonaCtaPropia,v_IdClienteCtaPropia,v_Count ,v_IdTrabajoParidad		BIGINT:=0;

		DECLARE v_out_error			  			 NVARCHAR(500);

	  	v_FechaOperacionDesde:=p_FechaOperacionDesde;
  		v_FechaOperacionHasta:=p_FechaOperacionHasta;  		
  
  		
		IF v_FechaOperacionDesde='' AND v_FechaOperacionHasta='' THEN
			call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
				p_IdEmpresa => p_IdEmpresa,
				p_out_FechaHoraSistema => v_fechaHoraSistema
			);
			v_FechaOperacionDesde=TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD');
			v_FechaOperacionHasta=v_FechaOperacionDesde;
		END IF;

		IF v_FechaOperacionHasta='' THEN
			v_FechaOperacionHasta:=v_FechaOperacionDesde;  		
		END IF;
		
		IF v_FechaOperacionDesde='' THEN
			v_FechaOperacionDesde:=v_FechaOperacionHasta;  		
		END IF;

		---Parametros

		--Valores de la cuenta propia	
		SELECT   COALESCE(SOC."IdPersona",0),COALESCE(SOC."IdCliente",0) INTO v_IdPersonaCtaPropia,v_IdClienteCtaPropia
		FROM "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" as SOC 	
			INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" EMP ON (SOC."IdSocioDeNegocio"=EMP."IdSocioDeNegocio")	
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON (CLI."IdPersona"=SOC."IdPersona" AND CLI."IdEmpresa"=EMP."IdEmpresa")
		WHERE EMP."IdEmpresa" = p_IdEmpresa AND EMP."IdEstado"='ACT';

	

		---- Transacciones desde servicionNormativo de Operaciones

		--v_TablaModulo = Select '' as "IdModulo" from dummy where 1=2;
		v_TablaModulo = Select 'FAC' as "IdModulo" from dummy;
		---Acotar busqueda servicio BASE
		v_registroParametro = SELECT 'FECHADESDE' AS "Codigo", v_FechaOperacionDesde AS "Valor" FROM DUMMY
					UNION ALL SELECT 'FECHAHASTA' AS "Codigo", v_FechaOperacionHasta AS "Valor" FROM DUMMY;
		
			CALL "BSCL.Normativa.BD.Servicios.NOR::ConsultarOperaciones"
			(
				p_IdEmpresa			=> p_IdEmpresa,
				p_IdNormativa		=> v_IdNormativa,
				p_IdTipoDocumento	=> v_IdTipoDocumento,
				p_tablaModulo		=> :v_TablaModulo,
				p_registroParametro	=> :v_registroParametro,
				p_out_TablaSalida	=> v_SalidaConsultarOperaciones
			);
		

		---- Transacciones 
		v_tablaOperaciones=
		SELECT
			ROW_NUMBER() OVER() AS "IdInterno",
			CUS."FechaMovimiento",
			CUS."IdOperacion",
			CUS."IdCliente",
			CUS."IdPortafolio",
			CUS."IdClasificacionTipoInstrumento",
			CUS."HIdClasificacionTipoInstrumento",
			CUS."IdTipoInstrumento",
			CUS."IdTipoOperacion",
			CUS."IdMoneda",
			CUS."Monto",
			(CASE WHEN CUS."IdCliente"= v_IdClienteCtaPropia THEN 'S' ELSE 'N' END) AS "EsCtaPropia",
			COALESCE (IND."ValorIndicador",'N') AS "DerechoPrivilegiado",
			COALESCE (CDB."ValorIndicador",'N') AS "ConvenioDerechoBolsa",
			--(CASE WHEN COALESCE(REL."IdPersonaDestino",0)=0 THEN 'N' ELSE 'S' END) AS "RelacionCorredor",
			(CASE WHEN COALESCE(REL."RELACION",'NOVINCULADO')='NOVINCULADO' THEN 'N' ELSE 'S' END) AS "RelacionCorredor",
			CUS."Precio",
			CUS."Cantidad",
			CUS."Nemotecnico",
			CUS."IdInstrumento",
			CUS."IdBolsa",
			CUS."CodigoOperadorContraparte",
			CUS."AccionOperacion",
			CUS."Uso"			
			FROM  :v_SalidaConsultarOperaciones CUS
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" SOC
						ON (SOC."IdCliente" =CUS."IdCliente")
		LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IND  --DERECHOS PRIVILEGIADOS
						ON (IND."IdUso"='IND' AND IND."CodigoTipoIndicador"='DERPRIV' 
							AND IND."IdCliente" =SOC."IdCliente")
		LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" CDB  --CONVENIO DE PAGO DERECHOS BOLSA
						ON (CDB."IdUso"='IND' AND CDB."CodigoTipoIndicador"='DRCHCNV' 
							AND CDB."IdCliente" =SOC."IdCliente")	
							
		LEFT OUTER JOIN 						 --RELACIONADOS CON LA CORREDORA
				(
						--SELECT DISTINCT PER."IdPersonaDestino"--,PER."IdTipoRelacion",PER."NombreTipoRelacion" ,PER."Administracion"
						--FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona" PER
						--WHERE PER."IdPersonaOrigen"=v_IdPersonaCtaPropia --Corredor
						--AND PER."IdEstado"='ACT'
						
						SELECT COALESCE(REL."CodigoValorIndicador" ,'NOVINCULADO') AS "RELACION",REL."IdCliente",COALESCE(REL."NombreValorIndicador",'NO VINCULADO') AS "NombreValorIndicador"
		     			FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" REL
					 	WHERE (REL."IdUso"='IND' AND REL."CodigoTipoIndicador"='1064PROP' AND REL."CodigoValorIndicador" <>'')
						
						
						
				) REL 
					--ON (REL."IdPersonaDestino"=SOC."IdPersona")							
					ON (REL."IdCliente"=SOC."IdCliente")
							
		WHERE 
		 (CUS."IdCliente" = p_IdCliente OR p_IdCliente= 0);
		
	
		--Verificar si existen operaciones para la conversion monetaria
		SELECT COUNT(1) INTO v_Count FROM :v_tablaOperaciones;
 				 
		IF v_Count > 0 THEN
		
		
			---IGUALAR PARIDADES			
						
			SELECT "BSCL.Plataforma.BD.Secuencias::MER.IdTrabajoParidad".NEXTVAL INTO v_IdTrabajoParidad FROM DUMMY;
			
			INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"("IdTrabajo","IdInterno","Fecha","IdMonedaOrigen","IdMonedaDestino","ValorCambioLocalModificado","MontoOrigen","IdUso")
			SELECT :v_IdTrabajoParidad,CAR."IdInterno" AS "IdInterno",TO_VARCHAR (CAR."FechaMovimiento", 'YYYY-MM-DD') AS "Fecha", CAR."IdMoneda" as "IdMonedaOrigen", 'CLP' as "IdMonedaDestino",
			0 as "ValorCambioLocalModificado",CAR."Monto" as "MontoOrigen",'ORDENES' as "IdUso"
			FROM :v_tablaOperaciones CAR;

			call "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"(
				p_IdEmpresa => :p_IdEmpresa,
				p_IdTrabajo => :v_IdTrabajoParidad
			);
	
	
			v_out_Paridad=
					SELECT DISTINCT COALESCE(outt."MontoDestino",0) AS  "MontoDestino",outt."IdMonedaDestino", par.*
					FROM :v_tablaOperaciones par
					LEFT JOIN "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" outt 
					on par."IdInterno" = outt."IdInterno" AND outt."IdTrabajo" = :v_IdTrabajoParidad;
				
		
		--RESUMEN 1 			
					
			-----FECHAS		
		v_Tab_SalFechas = SELECT DISTINCT TO_DATE("FechaMovimiento") AS "FechaMovimiento" FROM :v_out_Paridad;
		
		
			----CTA ESPECIALES
		v_Tab_SalCTEspeciales= SELECT SUM("MontoDestino") AS "Monto",TO_DATE("FechaMovimiento") AS "FechaMovimiento" FROM :v_out_Paridad
							 WHERE "EsCtaPropia"='N' AND "RelacionCorredor"='N'
							 GROUP BY TO_DATE("FechaMovimiento");
							 
			----CTA TERCEROS
		v_Tab_SalCTTerceros= SELECT SUM("MontoDestino") AS "Monto",TO_DATE("FechaMovimiento") AS "FechaMovimiento" FROM :v_out_Paridad
							 WHERE "EsCtaPropia"='N' AND "RelacionCorredor"='S'
							 GROUP BY TO_DATE("FechaMovimiento");
		
			-----CTA PROPIA	
		v_Tab_SalCTPAcciones= SELECT SUM("MontoDestino") AS "Monto",TO_DATE("FechaMovimiento") AS "FechaMovimiento" FROM :v_out_Paridad
							 WHERE "EsCtaPropia"='S' AND "HIdClasificacionTipoInstrumento"='ACC' AND "Uso" <>'PLA'
							 GROUP BY TO_DATE("FechaMovimiento");
							 
		v_Tab_SalCTPPlazo= SELECT SUM("MontoDestino") AS "Monto",TO_DATE("FechaMovimiento") AS "FechaMovimiento" FROM :v_out_Paridad
							 WHERE "EsCtaPropia"='S' AND "Uso" ='PLA' --Operaciones a Plazo
							 GROUP BY TO_DATE("FechaMovimiento");
							 
		-----CLIENTES					 
		v_Tab_SalClientesPlazo= SELECT SUM("MontoDestino") AS "Monto",TO_DATE("FechaMovimiento") AS "FechaMovimiento" FROM :v_out_Paridad
							 WHERE "EsCtaPropia"='N' AND "DerechoPrivilegiado"='N'AND "ConvenioDerechoBolsa"='N' AND "Uso" ='PLA' --Operaciones a Plazo
							 GROUP BY TO_DATE("FechaMovimiento");
							 
		v_Tab_SalClientesPrivilegiados= SELECT SUM("MontoDestino") AS "Monto",TO_DATE("FechaMovimiento") AS "FechaMovimiento" FROM :v_out_Paridad
							 WHERE "EsCtaPropia"='N' AND "DerechoPrivilegiado"='S'
							 GROUP BY TO_DATE("FechaMovimiento");			
							 
		v_Tab_SalClientesConvenios	= SELECT SUM("MontoDestino") AS "Monto",TO_DATE("FechaMovimiento") AS "FechaMovimiento" FROM :v_out_Paridad
							 WHERE "EsCtaPropia"='N' AND "ConvenioDerechoBolsa"='S'
							 GROUP BY TO_DATE("FechaMovimiento");
							 
							
							 
		--Total Declarado					 
		v_Tab_SalTotalDeclarado = SELECT SUM("MontoDestino") AS "Monto",TO_DATE("FechaMovimiento") AS "FechaMovimiento" FROM :v_out_Paridad
							 		 GROUP BY TO_DATE("FechaMovimiento");					 
					
		--RESUMEN 2					 		 
							 		 
		v_Tab_SalTotalCompraAcciones= SELECT SUM("MontoDestino") AS "Monto",TO_DATE("FechaMovimiento") AS "FechaMovimiento" FROM :v_out_Paridad
							 WHERE "HIdClasificacionTipoInstrumento"='ACC' AND "Uso" <>'PLA' AND "AccionOperacion"='COMPRA'
							 GROUP BY TO_DATE("FechaMovimiento");	
							 
		v_Tab_SalTotalCompraplazo= SELECT SUM("MontoDestino") AS "Monto",TO_DATE("FechaMovimiento") AS "FechaMovimiento" FROM :v_out_Paridad
							 WHERE  "Uso" = 'PLA' AND "AccionOperacion"='COMPRA'
							 GROUP BY TO_DATE("FechaMovimiento");						 		 
				
		v_Tab_SalTotalVentaAcciones= SELECT SUM("MontoDestino") AS "Monto",TO_DATE("FechaMovimiento") AS "FechaMovimiento" FROM :v_out_Paridad
							 WHERE "HIdClasificacionTipoInstrumento"='ACC' AND "Uso" <>'PLA' AND "AccionOperacion"='VENTA'
							 GROUP BY TO_DATE("FechaMovimiento");
							 
		v_Tab_SalTotalVentaplazo= SELECT SUM("MontoDestino") AS "Monto",TO_DATE("FechaMovimiento") AS "FechaMovimiento" FROM :v_out_Paridad
							 WHERE  "Uso" = 'PLA' AND "AccionOperacion"='VENTA'
							 GROUP BY TO_DATE("FechaMovimiento");						 
			--Total Declarado					 
		v_Tab_SalTotalGeneral = SELECT SUM("MontoDestino") AS "Monto",TO_DATE("FechaMovimiento") AS "FechaMovimiento" FROM :v_out_Paridad
						 	 GROUP BY TO_DATE("FechaMovimiento");	
							 
						--SELECT COUNT(1) INTO v_Count FROM :v_Tab_SalFechas;	 
		--Tabla de Salida
			p_out_TablaAnexo4=
			SELECT 
				OPE."FechaMovimiento",
				OPE."CuentaTerceros",
				OPE."EspecialesOtros",
				OPE."CuentaPropiaAcciones",
				OPE."CuentaPropiaPlazo",
				OPE."CliOperacionPlazo",
				OPE."CliPrivilegiados",
				OPE."CliConvenio",
				OPE."TotalDeclarado",
				OPE."TotalCompraAcciones",
				OPE."TotalCompraPlazo" ,
				OPE."TotalVentaAcciones",
				OPE."TotalVentaPlazo",
				OPE."TotalGeneral"
			FROM	
			(			
	     		SELECT 
					TO_DATE(OP."FechaMovimiento") AS "FechaMovimiento",
					COALESCE(SalCTTerceros."Monto",0) 				AS "CuentaTerceros",
					COALESCE(SalCTEspeciales."Monto",0) 			AS "EspecialesOtros",
					COALESCE(SalCTPAcciones."Monto",0) 				AS "CuentaPropiaAcciones",
					COALESCE(SalCTPPlazo."Monto",0) 				AS "CuentaPropiaPlazo",
					COALESCE(SalClientesPlazo."Monto",0) 			AS "CliOperacionPlazo",
					COALESCE(SalClientesPrivilegiados."Monto",0) 	AS "CliPrivilegiados",
					COALESCE(SalClientesConvenios."Monto",0) 		AS "CliConvenio",
					COALESCE(SalTotalDeclarado."Monto",0) 			AS "TotalDeclarado",
					COALESCE(SalTotalCompraAcciones."Monto",0) 		AS "TotalCompraAcciones",
					COALESCE(SalTotalCompraplazo."Monto",0) 		AS "TotalCompraPlazo" ,
					COALESCE(SalTotalVentaAcciones."Monto",0) 		AS "TotalVentaAcciones",
					COALESCE(SalTotalVentaplazo."Monto",0) 			AS "TotalVentaPlazo",
					COALESCE(SalTotalGeneral."Monto",0) 			AS "TotalGeneral"
     			FROM :v_Tab_SalFechas OP
     			LEFT OUTER JOIN :v_Tab_SalCTTerceros 				AS SalCTTerceros    			ON (OP."FechaMovimiento" =SalCTTerceros."FechaMovimiento"				)
     			LEFT OUTER JOIN :v_Tab_SalCTEspeciales 			    AS SalCTEspeciales    		    ON (OP."FechaMovimiento" =SalCTEspeciales."FechaMovimiento"			)
     			LEFT OUTER JOIN :v_Tab_SalCTPAcciones 				AS SalCTPAcciones    			ON (OP."FechaMovimiento" =SalCTPAcciones."FechaMovimiento"				)
     			LEFT OUTER JOIN :v_Tab_SalCTPPlazo 	  				AS SalCTPPlazo 					ON (OP."FechaMovimiento" =SalCTPPlazo."FechaMovimiento"					)
     			LEFT OUTER JOIN :v_Tab_SalClientesPlazo  			AS SalClientesPlazo 			ON (OP."FechaMovimiento" =SalClientesPlazo."FechaMovimiento"			)
     			LEFT OUTER JOIN :v_Tab_SalClientesPrivilegiados		AS SalClientesPrivilegiados		ON (OP."FechaMovimiento" =SalClientesPrivilegiados."FechaMovimiento"	)
     			LEFT OUTER JOIN :v_Tab_SalClientesConvenios			AS SalClientesConvenios			ON (OP."FechaMovimiento" =SalClientesConvenios."FechaMovimiento"		)
     			LEFT OUTER JOIN :v_Tab_SalTotalDeclarado			AS SalTotalDeclarado			ON (OP."FechaMovimiento" =SalTotalDeclarado."FechaMovimiento"			)
     			LEFT OUTER JOIN :v_Tab_SalTotalCompraAcciones		AS SalTotalCompraAcciones		ON (OP."FechaMovimiento" =SalTotalCompraAcciones."FechaMovimiento"		)
     			LEFT OUTER JOIN :v_Tab_SalTotalCompraplazo			AS SalTotalCompraplazo			ON (OP."FechaMovimiento" =SalTotalCompraplazo."FechaMovimiento"			)
     			LEFT OUTER JOIN :v_Tab_SalTotalVentaAcciones		AS SalTotalVentaAcciones		ON (OP."FechaMovimiento" =SalTotalVentaAcciones."FechaMovimiento"		)
     			LEFT OUTER JOIN :v_Tab_SalTotalVentaplazo			AS SalTotalVentaplazo			ON (OP."FechaMovimiento" =SalTotalVentaplazo."FechaMovimiento"			)
     			LEFT OUTER JOIN :v_Tab_SalTotalGeneral				AS SalTotalGeneral				ON (OP."FechaMovimiento" =SalTotalGeneral."FechaMovimiento"				)
     			
	     			
			) OPE 
			ORDER BY OPE."FechaMovimiento";
				
		ELSE
		p_out_TablaAnexo4=SELECT
				'' AS "FechaMovimiento",
				0 AS  "CuentaTerceros",
				0 AS  "EspecialesOtros",
				0 AS  "CuentaPropiaAcciones",
				0 AS  "CuentaPropiaPlazo",
				0 AS  "CliOperacionPlazo",
				0 AS  "CliPrivilegiados",
				0 AS  "CliConvenio",
				0 AS  "TotalDeclarado",
				0 AS  "TotalCompraAcciones",
				0 AS  "TotalCompraPlazo" ,
				0 AS  "TotalVentaAcciones",
				0 AS  "TotalVentaPlazo",
				0 AS  "TotalGeneral"
				FROM DUMMY WHERE 1=2;


		
		END IF;
	SELECT * FROM :p_out_TablaAnexo4;			

END; 