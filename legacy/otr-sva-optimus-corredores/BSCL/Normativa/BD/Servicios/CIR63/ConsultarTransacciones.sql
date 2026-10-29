SET SCHEMA "BSCL";
 GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.CIR63::ConsultarTransacciones";
 GO
CREATE PROCEDURE 
--
--  Proposito: Servicio para consultar información para emitir la Nomina de Relacionados.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 05/08/2016

"BSCL.Normativa.BD.Servicios.CIR63::ConsultarTransacciones" ( 

	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa			BIGINT,

    --VARIABLES DEL PROCESO
	IN	p_TipoInformacion						NVARCHAR(3)	DEFAULT 'AM',--(RE)RELACIONADO, (IP)INFORMACION PRIVILEGIADA,(AM)AMBOS
	IN	p_FechaOperacionDesde					NVARCHAR(10)	DEFAULT '',--'YYYY-MM-DD'
	IN	p_FechaOperacionHasta					NVARCHAR(10)	DEFAULT '',--'YYYY-MM-DD'
	IN  p_IdClasificacionTipoInstrumento		NVARCHAR(3) 	DEFAULT '',
	IN  p_IdEmisor			       			  	NVARCHAR(40)	Default '',
	IN  p_IdInstrumento							BIGINT			Default 0,
	IN	p_IdCliente 							BIGINT 			DEFAULT 0,

    --VARIABLES DE SALIDA OBLIGATORIAS

	OUT p_out_TablaTransac	"BSCL.Normativa.BD::NOR.SALIDA_CIR63TRANSAC"

)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
		--------------------
		--Declare Variables
		--------------------
		DECLARE v_IdNormativa 								NVARCHAR(15):='CIR63';
		DECLARE v_NombreDestinatario						NVARCHAR(100):='';
		DECLARE v_CargoDestinatario							NVARCHAR(40):='';				
		DECLARE v_IdSinRelacion								NVARCHAR(6):='SINREL';
		DECLARE v_DescripcionSinRelacion					NVARCHAR(520):='';
		
		DECLARE v_fechaHoraSistema 							 NVARCHAR(40):='';
  		DECLARE v_FechaOperacionDesde, v_FechaOperacionHasta NVARCHAR(10):='';
	
		DECLARE v_IdPersonaCtaPropia,v_Count,v_IdTrabajoParidad 				BIGINT:=0;

		DECLARE v_out_error			  			 			NVARCHAR(500);
		
		DECLARE v_Esquema,v_Version, v_FechaVersion		NVARCHAR(10) DEFAULT '';
		DECLARE v_IdTipoDocumento  						NVARCHAR(10) DEFAULT 'CI63Transa';
		 
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
		
		
		/* Obtener version del esquema */	
		SELECT TOP 1 "Esquema" INTO v_Esquema
		FROM "BSCL.Normativa.BD::DOC.TipoDocumento"
		WHERE "IdTipoDocumento" = v_IdTipoDocumento;
			
		v_FechaVersion :=TO_VARCHAR (v_FechaOperacionDesde, 'DD-MM-YYYY');
		SELECT COUNT(1) INTO v_count FROM "BSCL.Normativa.BD.Utilitarios::NOR.ObtenerVersionEsquema"(:v_IdTipoDocumento,:v_FechaVersion);

		IF(v_count>0) THEN
			SELECT TOP 1 "P_OUT_VERSION" INTO v_Version FROM "BSCL.Normativa.BD.Utilitarios::NOR.ObtenerVersionEsquema"(:v_IdTipoDocumento,:v_FechaVersion);
  		END IF;
		/* Obtener parametros del destinatario */
  		SELECT COUNT(1) INTO v_Count
			FROM  "BSCL.Normativa.BD::DOC.Parametro" WHERE "Esquema"=v_Esquema AND "Version"=v_Version AND "IdEmpresa"=p_IdEmpresa AND "Codigo"='CARGO_DESTINATARIO';
		IF v_Count>0 THEN
			SELECT trim("Valor") INTO v_CargoDestinatario
				FROM  "BSCL.Normativa.BD::DOC.Parametro" WHERE "Esquema"=v_Esquema AND "Version"=v_Version AND "IdEmpresa"=p_IdEmpresa AND "Codigo"='CARGO_DESTINATARIO';
			ELSE
			v_CargoDestinatario:=' ';
		END IF;
  		
  		SELECT COUNT(1) INTO v_Count
			FROM  "BSCL.Normativa.BD::DOC.Parametro" PAR INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON (PER."DocumentoIdentidad"=trim(PAR."Valor"))
			WHERE PAR."Esquema"=v_Esquema AND PAR."Version"=v_Version AND PAR."IdEmpresa"=p_IdEmpresa AND PAR."Codigo"='DOCIDENT_DESTINATARIO';

		IF v_Count>0 THEN
			SELECT trim(PER."NombreCompleto") INTO v_NombreDestinatario
			FROM  "BSCL.Normativa.BD::DOC.Parametro" PAR INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON (PER."DocumentoIdentidad"=trim(PAR."Valor"))
			WHERE PAR."Esquema"=v_Esquema AND PAR."Version"=v_Version AND PAR."IdEmpresa"=p_IdEmpresa AND PAR."Codigo"='DOCIDENT_DESTINATARIO';

			ELSE
			v_NombreDestinatario:=' ';
		END IF;
  		
		
		---Parametros
		--Valores de la cuenta propia	
		SELECT COALESCE(SOC."IdPersona",0) INTO v_IdPersonaCtaPropia
		FROM "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" as SOC 	
			INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" EMP ON (SOC."IdSocioDeNegocio"=EMP."IdSocioDeNegocio")	
		WHERE EMP."IdEmpresa" = p_IdEmpresa AND EMP."IdEstado"='ACT';

		--Destinatario

		--SELECT "NombreDestinatario", "CargoDestinatario" INTO v_NombreDestinatario, v_CargoDestinatario
		--FROM "BSCL.Normativa.BD::NOR.Normativa" WHERE "IdNormativa"=v_IdNormativa;
		
		--Homologo Tipo Sin Relacion
		
		SELECT COUNT(1) INTO v_Count
			FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacion"
		WHERE "IdEmpresa"=p_IdEmpresa AND "IdTipoRelacion"=v_IdSinRelacion AND "Administracion"='SISTEMA';
		
		IF v_Count>0 THEN
			SELECT "NombreTipoRelacion" INTO v_DescripcionSinRelacion
				FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacion"
			WHERE "IdEmpresa"=p_IdEmpresa AND "IdTipoRelacion"=v_IdSinRelacion AND "Administracion"='SISTEMA';
		END IF;
		
		---- Transacciones desde servicionNormativo de Operaciones

		v_TablaModulo = Select '' as "IdModulo" from dummy where 1=2;
			---Acotar busqueda servicio BASE
		v_registroParametro = SELECT 'FECHADESDE' AS "Codigo", v_FechaOperacionDesde AS "Valor" FROM DUMMY
					UNION ALL SELECT 'FECHAHASTA' AS "Codigo", v_FechaOperacionHasta AS "Valor" FROM DUMMY;	
			CALL "BSCL.Normativa.BD.Servicios.NOR::ConsultarOperaciones"
			(
				p_IdEmpresa			=> p_IdEmpresa,
				p_IdNormativa		=> v_IdNormativa,
				p_tablaModulo		=> :v_TablaModulo,
				p_registroParametro	=> :v_registroParametro,				
				p_out_TablaSalida	=> v_SalidaConsultarOperaciones
			);
			
		---Omitir operaciones con NOtas de Credito Asociadas
		v_SalidaConsultar = SELECT ROW_NUMBER() OVER() AS "IdInterno", OP.* FROM :v_SalidaConsultarOperaciones OP
									LEFT  OUTER JOIN 
									(
										SELECT"IdComprobanteReferencia"
										FROM "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" NC
										WHERE NC."IdEstadoFacturacion"='FAC' AND NC."IdTipoComprobante"='NCE' AND NC."IdComprobanteReferencia">0
									) NC
									ON (OP."IdOperacion"=NC."IdComprobanteReferencia")--  AND OP."IdTipoComprobante"='FAC' ) 
										WHERE NC."IdComprobanteReferencia" IS NULL
										AND NOT (OP."HIdClasificacionTipoInstrumento" = 'RF' AND (OP."HIdTipoOperacion" = '' AND OP."HIdTipoInstrumento" = ''));
		
										
			---IGUALAR PARIDADES			
						
			SELECT "BSCL.Plataforma.BD.Secuencias::MER.IdTrabajoParidad".NEXTVAL INTO v_IdTrabajoParidad FROM DUMMY;
			
			INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"("IdTrabajo","IdInterno","Fecha","IdMonedaOrigen","IdMonedaDestino","ValorCambioLocalModificado","MontoOrigen","IdUso")
			SELECT :v_IdTrabajoParidad,CAR."IdInterno" AS "IdInterno",TO_VARCHAR (CAR."FechaMovimiento", 'YYYY-MM-DD') AS "Fecha", 
			CASE WHEN CAR."IdMoneda" = 'UF' AND CAR."IdTipoProducto" = 'MERF' THEN 'CLP' ELSE CAR."IdMoneda" END AS "IdMonedaOrigen", 'CLP' as "IdMonedaDestino",
			0 as "ValorCambioLocalModificado",CAR."Monto" as "MontoOrigen",'ORDENES' as "IdUso"
			FROM :v_SalidaConsultar CAR;

			call "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"(
				p_IdEmpresa => :p_IdEmpresa,
				p_IdTrabajo => :v_IdTrabajoParidad
			);
			
										
			v_out_ParidadDisponibles=
				SELECT DISTINCT COALESCE(outt."MontoDestino",0) AS  "MontoDestino",outt."IdMonedaDestino", par.*
				FROM :v_SalidaConsultar par
				LEFT JOIN "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" outt 
				on par."IdInterno" = outt."IdInterno" AND outt."IdTrabajo" = :v_IdTrabajoParidad;
										
			
			---Precios				
			SELECT "BSCL.Plataforma.BD.Secuencias::MER.IdTrabajoParidad".NEXTVAL INTO v_IdTrabajoParidad FROM DUMMY;
			
			INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"("IdTrabajo","IdInterno","Fecha","IdMonedaOrigen","IdMonedaDestino","ValorCambioLocalModificado","MontoOrigen","IdUso")
			SELECT :v_IdTrabajoParidad,CAR."IdInterno" AS "IdInterno",TO_VARCHAR (CAR."FechaMovimiento", 'YYYY-MM-DD') AS "Fecha", CASE WHEN CAR."IdMoneda" = 'UF' AND CAR."IdTipoProducto" = 'MERF' THEN 'CLP' ELSE CAR."IdMoneda" END AS "IdMonedaOrigen", 'CLP' as "IdMonedaDestino",
			0 as "ValorCambioLocalModificado",CAR."Precio" as "MontoOrigen",'ORDENES' as "IdUso"
			FROM :v_SalidaConsultar CAR;

			call "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"(
				p_IdEmpresa => :p_IdEmpresa,
				p_IdTrabajo => :v_IdTrabajoParidad
			);
			
				v_out_Precios=
				SELECT DISTINCT COALESCE(outt."MontoDestino",0) AS  "MontoDestino",outt."IdMonedaDestino", par.*
				FROM :v_SalidaConsultar par
				LEFT JOIN "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" outt 
				on par."IdInterno" = outt."IdInterno" AND outt."IdTrabajo" = :v_IdTrabajoParidad;
				
				
				
		v_tablaClientes=
		SELECT
			TO_VARCHAR (CUS."FechaMovimiento" , 'YYYY-MM-DD') AS "FechaOperacion",
			CUS."IdOperacion",
			CUS."Nemotecnico", 			
			CUS."Cantidad",				
			PRE."MontoDestino"	 		AS "Precio",
			CUS."MontoDestino" 			AS "Monto",
			CASE WHEN CUS."IdMoneda" = 'UF' AND CUS."IdTipoProducto" = 'MERF' THEN 'CLP' ELSE CUS."IdMoneda" END AS "IdMoneda",
			--CUS."MonedaSistema" 		AS "Moneda",
			CUS."IdCliente",
			CUS."IdPortafolio",
			CUS."IdInstrumento",
			CUS."IdTipoProducto",
			CUS."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento",
			--CUS."IdTipoInstrumento",
			CUS."IdTipoOperacion",
			CUS."HIdTipoOperacion",
			--CUS."DocumentoIdentidad" 	AS "DocumentoIdentidad",
			IFNULL(REL."CodigoValorIndicador",v_IdSinRelacion)				AS "IdTipoRelacion",
			IFNULL(REL."NombreValorIndicador",v_DescripcionSinRelacion)	AS "NombreTipoRelacion"
		FROM  :v_out_ParidadDisponibles CUS INNER JOIN :v_out_Precios PRE ON (PRE."IdInterno" = CUS."IdInterno")
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" SOC
					ON (SOC."IdCliente" =CUS."IdCliente")		
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IND  --INFORMACION PRIVILEGIADA
						ON ( IND."IdUso"='IND' AND IND."CodigoTipoIndicador"='INFPRV' AND IND."ValorIndicador"='S'
							AND IND."IdCliente" =SOC."IdCliente")
				
					 --RELACIONADOS CON LA CORREDORA
		/*
		LEFT JOIN (
					SELECT PER."IdPersonaDestino",PER."IdTipoRelacion",PER."NombreTipoRelacion" ,PER."Administracion"
					FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona" PER
					WHERE PER."IdPersonaOrigen"=v_IdPersonaCtaPropia --Corredor
					AND PER."IdEstado"='ACT'
					) REL 
					ON (REL."IdPersonaDestino"=SOC."IdPersona")
		*/
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" REL
		ON (REL."IdUso"='IND' AND REL."CodigoTipoIndicador"='NCG69PROP' AND  REL."CodigoValorIndicador" <>''
							AND REL."IdCliente" =SOC."IdCliente")			
					
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" SOCC
		ON (SOCC."IdCliente"=CUS."IdCliente")		
		
		WHERE 
		--CUS."FechaMovimiento" BETWEEN TO_DATE(v_FechaOperacionDesde) AND TO_DATE(v_FechaOperacionHasta)
		--AND
		 (CUS."IdClasificacionTipoInstrumento" =p_IdClasificacionTipoInstrumento  OR p_IdClasificacionTipoInstrumento='')
		AND (CUS."IdEmisor" =p_IdEmisor  			OR p_IdEmisor='')
		--AND (CUS."IdInstrumento" =p_IdInstrumento  	OR p_IdInstrumento=0)		
		AND (CUS."IdCliente" = p_IdCliente 			OR p_IdCliente= 0)
 		AND (
				(:p_TipoInformacion='AM' AND ((IND."IdCliente" IS NOT NULL OR p_TipoInformacion='RE') OR (REL."CodigoValorIndicador" IS NOT NULL OR p_TipoInformacion='IP')))
				OR 
			 	(:p_TipoInformacion<>'AM' AND ((IND."IdCliente" IS NOT NULL OR p_TipoInformacion='RE') AND (REL."CodigoValorIndicador" IS NOT NULL OR p_TipoInformacion='IP')))
			);
 		
 	/*	
 		---- Transacciones de Nominales que no pasan por Custodia
 		UNION ALL
 		
		SELECT
			CUS."FechaTransaccion" 					AS "FechaOperacion",
			CUS."IdMovimientoComprobante" 			AS "IdOperacion",
			CUS."DescripcionTipoOperacion"			AS "NombreTipoOperacion", 	
			CUS."NemotecnicoInstrumento" 			AS "Nemotecnico", 			
			CUS."Cantidad",				
			CUS."Precio" 							AS "Precio",
			CUS."Monto" 							AS "Monto",
			CUS."IdMoneda" 							AS "IdMoneda",
			CUS."NombreMoneda"		 				AS "Moneda",
			CUS."IdCliente",
			CUS."IdPortafolio",
			CUS."IdInstrumento",
			CUS."IdTipoProducto",
			CUS."IdClasificacionTipoInstrumento",
			CUS."IdTipoInstrumento",
			CUS."IdTipoOperacion",
			CUS."DocumentoIdentidad" 	AS "DocumentoIdentidad",
			IFNULL(REL."IdTipoRelacion",v_IdSinRelacion)				AS "IdTipoRelacion",
			IFNULL(REL."NombreTipoRelacion",v_DescripcionSinRelacion)	AS "NombreTipoRelacion"			
		FROM  "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante"  CUS
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" SOC
					ON (SOC."IdCliente" =CUS."IdCliente")		
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IND  --INFORMACION PRIVILEGIADA
						ON ( IND."IdUso"='IND' AND IND."CodigoTipoIndicador"='DERPRIV' AND IND."ValorIndicador"='S'
							AND IND."IdCliente" =SOC."IdCliente")
		INNER JOIN "BSCL.Normativa.BD::NOR.Producto" NPRO 							 --PRODUCTO NORMATIVA
						 ON (NPRO."IdNormativa"=v_IdNormativa
						 AND CUS."IdTipoProducto"= NPRO."IdTipoProducto"
						 AND (CUS."IdClasificacionTipoInstrumento"	= NPRO."IdClasificacionTipoInstrumento" OR CUS."IdClasificacionTipoInstrumento"='') 
						 AND (CUS."IdTipoInstrumento" 				= NPRO."IdTipoInstrumento"  			OR CUS."IdTipoInstrumento"='') 
						 AND (CUS."IdTipoOperacion" 				= NPRO."IdTipoOperacion" 				OR CUS."IdTipoOperacion"=''))							
							
					 --RELACIONADOS CON LA CORREDORA
		LEFT JOIN (
					SELECT PER."IdPersonaDestino",PER."IdTipoRelacion",PER."NombreTipoRelacion" ,PER."Administracion"
					FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona" PER
					WHERE PER."IdPersonaOrigen"=v_IdPersonaCtaPropia --Corredor
					AND PER."IdEstado"='ACT'
					) REL 
					ON (REL."IdPersonaDestino"=SOC."IdPersona")
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" SOCC
		ON (SOCC."IdCliente"=CUS."IdCliente")		
		WHERE 
		CUS."FechaTransaccion" BETWEEN TO_DATE(v_FechaOperacionDesde) AND TO_DATE(v_FechaOperacionHasta)
		AND (CUS."IdClasificacionTipoInstrumento" =p_IdClasificacionTipoInstrumento  OR p_IdClasificacionTipoInstrumento='')
		--AND (CUS."IdEmisor" =p_IdEmisor  			OR p_IdEmisor='')
		AND (CUS."IdInstrumento" =p_IdInstrumento  	OR p_IdInstrumento=0)		
		AND (CUS."IdCliente" = p_IdCliente 			OR p_IdCliente= 0)
 		AND (IND."IdCliente" IS NOT NULL  			OR p_TipoInformacion='RE')
 		AND (REL."IdPersonaDestino" IS NOT NULL 			OR p_TipoInformacion='IP')
 		AND CUS."IdFormaCustodia" = 'NOM' AND CUS."IdEstadoFacturacion"='FAC' ;
	*/
		--Verificar si existen operaciones para la conversion monetaria
		SELECT COUNT(1) INTO v_Count FROM :v_tablaClientes;
 				 
		IF v_Count > 0 THEN

		--Tabla de Salida

			v_meses = SELECT  TO_BIGINT(1) "NUMERO",'Enero' AS "MES" FROM DUMMY
					  UNION ALL SELECT  TO_BIGINT(2) "NUMERO",'Febrero' AS "MES" FROM DUMMY
					  UNION ALL SELECT  TO_BIGINT(3) "NUMERO",'Marzo' AS "MES" FROM DUMMY
					  UNION ALL SELECT  TO_BIGINT(4) "NUMERO",'Abril' AS "MES" FROM DUMMY
					  UNION ALL SELECT  TO_BIGINT(5) "NUMERO",'Mayo' AS "MES" FROM DUMMY
					  UNION ALL SELECT  TO_BIGINT(6) "NUMERO",'Junio' AS "MES" FROM DUMMY
					  UNION ALL SELECT  TO_BIGINT(7) "NUMERO",'Julio' AS "MES" FROM DUMMY
					  UNION ALL SELECT  TO_BIGINT(8) "NUMERO",'Agosto' AS "MES" FROM DUMMY
					  UNION ALL SELECT  TO_BIGINT(9) "NUMERO",'Septiembre' AS "MES" FROM DUMMY
					  UNION ALL SELECT  TO_BIGINT(10) "NUMERO",'Octubre' AS "MES" FROM DUMMY
					  UNION ALL SELECT  TO_BIGINT(11) "NUMERO",'Noviembre' AS "MES" FROM DUMMY
					  UNION ALL SELECT  TO_BIGINT(12) "NUMERO",'Diciembre' AS "MES" FROM DUMMY;	
					  
			p_out_TablaTransac=
			SELECT 
				v_NombreDestinatario AS "NombreDestinatario",
				v_CargoDestinatario AS "CargoDestinatario",
				OPE."FechaOperacion",
				CASE WHEN TRIM(OPE."IdClasificacionTipoInstrumento")= 'RF' AND TRIM(OPE."IdTipoOperacion") <> '' THEN TRIM(OPE."IdTipoOperacion")
					 WHEN TRIM(OPE."IdClasificacionTipoInstrumento")= 'RF' AND TRIM(OPE."IdTipoOperacion") = '' THEN TRIM(OPE."HIdTipoOperacion")
					 WHEN TRIM(OPE."HIdTipoOperacion")='' THEN TRIM(OPE."IdTipoOperacion")
					 ELSE TRIM(OPE."HIdTipoOperacion")  END AS "IdTipoOperacion",				
				OPE."NombreTipoOperacion", 	
				OPE."IdClasificacionTipoInstrumento",				
				OPE."IdInstrumento",				
				OPE."Nemotecnico", 			
				OPE."Cantidad",				
				OPE."Precio",
				OPE."Monto",
				OPE."IdMoneda",
				OPE."Moneda",
				OPE."IdCliente",
				OPE."NombreCliente",
				OPE."DocumentoIdentidad",
				OPE."TipoDocumentoIdentidad",
				OPE."NombreAbrTipoDocumentoIdentidad" AS "NombreTipoDocumentoIdentidad",
				OPE."IdTipoRelacion",
				OPE."NombreTipoRelacion" ,
				OPE."IdTipoRelacion" AS "CodigoRelacionNorma",
				OPE."NombreTipoRelacion" AS "DescripcionRelacionNorma",				
				OPE."IdOperacion",
				(SELECT "MES" FROM :v_meses WHERE "NUMERO" = TO_BIGINT(month(TO_DATE(OPE."FechaOperacion"))))	AS "Mes"
				--MONTHNAME (OPE."FechaOperacion") AS "Mes"
			FROM	
			(			
					SELECT
						OP."FechaOperacion",
						OP."IdTipoOperacion",
						OP."HIdTipoOperacion",
						COALESCE(TPRO."Nombre",'') AS "NombreTipoOperacion", 	
						OP."Nemotecnico", 			
						OP."Cantidad" AS "Cantidad",				
						OP."Precio",
						OP."Monto"	AS "Monto",
						OP."IdMoneda",
						COALESCE(MON."Nombre",'')AS "Moneda",
						OP."IdCliente",
						CLI."NombreCompleto" AS "NombreCliente",
						OP."IdInstrumento",
						OP."IdClasificacionTipoInstrumento",
						CLI."DocumentoIdentidad" AS "DocumentoIdentidad",
						CLI."TipoDocumentoIdentidad",
						TIDEN."NombreAbrTipoDocumentoIdentidad",
						OP."IdTipoRelacion",
						OP."NombreTipoRelacion",
						OP."IdOperacion"
	     			FROM :v_tablaClientes OP
	     			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
	     				ON (OP."IdCliente"=CLI."IdCliente")
	     			INNER JOIN "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" as TIDEN 
	     				ON  (CLI."TipoDocumentoIdentidad" = TIDEN."IdTipoDocumentoIdentidad" )
					LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TPRO	
					ON (OP."IdTipoOperacion"=TPRO."IdTipoOperacion")
	     			LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda"  MON
	     			ON (OP."IdMoneda" = MON."IdMoneda") 	
			) OPE ;
			--LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::NOR.ConsultarRelacionNormativa" NOR
			--ON (OPE."IdTipoRelacion"=NOR."IdTipoRelacion" AND NOR."IdNormativa"=v_IdNormativa);

		ELSE
		p_out_TablaTransac=SELECT
				'' AS "NombreDestinatario",
				'' AS "CargoDestinatario", 
				'' AS "FechaOperacion",
				'' AS "IdTipoOperacion",
				'' AS "NombreTipoOperacion", 	
				'' AS "IdClasificacionTipoInstrumento",				
				0 AS "IdInstrumento",				
				'' AS "Nemotecnico", 			
				0 AS "Cantidad",				
				0 AS "Precio",
				0 AS "Monto",
				'' AS "IdMoneda",
				'' AS "Moneda",				
				0 AS "IdCliente",
				'' AS "NombreCliente",
				'' AS "DocumentoIdentidad",
				'' AS "TipoDocumentoIdentidad",
				'' AS "NombreTipoDocumentoIdentidad",
				'' AS "IdTipoRelacion",
				'' AS "NombreTipoRelacion",
				'' AS "CodigoRelacionNorma",
				'' AS "DescripcionRelacionNorma",
				0  AS "IdOperacion",
				'' AS "Mes"
				FROM DUMMY WHERE 1=2;	
		
		END IF;
		SELECT * FROM :p_out_TablaTransac;				

END; 