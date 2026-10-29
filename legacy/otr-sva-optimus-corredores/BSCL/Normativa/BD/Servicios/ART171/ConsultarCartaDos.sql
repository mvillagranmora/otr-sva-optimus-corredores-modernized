SET SCHEMA "BSCL";
 
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.ART171::ConsultarCartaDos";
 
CREATE PROCEDURE 
--  Tecnologia : CrystalReports
--  Proposito: Servicio para consultar información para emitir la Carta N°2 del Art. 171.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 25/07/2016

"BSCL.Normativa.BD.Servicios.ART171::ConsultarCartaDos" ( 

	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa			BIGINT,

    --VARIABLES DEL PROCESO
	IN	p_FechaOperacionDesde					NVARCHAR(10)	DEFAULT '',--'YYYY-MM-DD'
	IN	p_FechaOperacionHasta					NVARCHAR(10)	DEFAULT '',--'YYYY-MM-DD'
	IN	p_IdCliente 							BIGINT 			DEFAULT '0',

    --VARIABLES DE SALIDA OBLIGATORIAS

	OUT p_out_TablaCarta2	 "BSCL.Normativa.BD::NOR.SALIDA_ART171CARTA2"

)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
		--------------------
		--Declare Variables
		--------------------
		DECLARE v_IdNormativa 								NVARCHAR(15):='ART171CartaDos';
	
		DECLARE v_NombreDestinatario						NVARCHAR(100):='';
		DECLARE v_CargoDestinatario							NVARCHAR(40):='';
		DECLARE v_Esquema									NVARCHAR(10):='';
		DECLARE v_FechaVersion								NVARCHAR(40):='';
		DECLARE v_Version									NVARCHAR(10):='';

		
		DECLARE v_ValorLimite								 DECIMAL (26,8):=0;	
		DECLARE v_ValorParametro							 DECIMAL (26,8):=0;	
  		DECLARE v_fechaHoraSistema 							 NVARCHAR(40):='';
  		DECLARE v_FechaOperacionDesde, v_FechaOperacionHasta NVARCHAR(10):='';
  		DECLARE v_VAL_MONTO_ART171, v_MON_MONTO_ART171 	 	 NVARCHAR(520):='';
		DECLARE v_IdPersonaCtaPropia,v_Count,v_IdTrabajoParidad,v_Monto500UF 	BIGINT:=0;
	
        DECLARE v_MesFechaSistema								NVARCHAR(30):='';
		
	    DECLARE v_MesConsulta									NVARCHAR(30):='';
	
		DECLARE v_FechaSistema 									DATE;

		DECLARE v_out_error			  			   				NVARCHAR(500);
		DECLARE v_count_folio									BIGINT;
		DECLARE v_ultimo_folio									NVARCHAR(100):='0';
		DECLARE v_folio_obligatorio								NVARCHAR(10):='';
		DECLARE v_firma_obligatorio								NVARCHAR(10):='';
		DECLARE v_max_folio										BIGINT;
		DECLARE v_firma 										TABLE("FirmaCorredora" BLOB);
		DECLARE v_logo											TABLE("LogoCorredora" BLOB);

		Call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema"(:p_IdEmpresa, v_FechaSistema);  

	  	v_FechaOperacionDesde:=p_FechaOperacionDesde;
  		v_FechaOperacionHasta:=p_FechaOperacionHasta;  		
  		
		IF v_FechaOperacionDesde='' AND v_FechaOperacionHasta='' THEN
			call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
				p_IdEmpresa => p_IdEmpresa,
				p_out_FechaHoraSistema => v_fechaHoraSistema
			);
			v_FechaOperacionDesde=TO_VARCHAR (:v_fechaHoraSistema, 'YYYY-MM-DD');
			v_FechaOperacionHasta=v_FechaOperacionDesde;
		END IF;

		IF v_FechaOperacionHasta='' THEN
			v_FechaOperacionHasta:=v_FechaOperacionDesde;  		
		END IF;
		
		IF v_FechaOperacionDesde='' THEN
			v_FechaOperacionDesde:=v_FechaOperacionHasta;  		
		END IF;

		---Parametros
		SELECT COUNT(1) INTO v_Count
			FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
		WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='VAL_MONTO_ART171' AND "IdNormativa"=v_IdNormativa;
		
		IF v_Count>0 THEN
			SELECT "Valor" INTO v_VAL_MONTO_ART171
				FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
			WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='VAL_MONTO_ART171' AND "IdNormativa"=v_IdNormativa;
			ELSE
			v_VAL_MONTO_ART171:='500';
		END IF;

		SELECT COUNT(1) INTO v_Count
			FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
			WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='MON_MONTO_ART171' AND "IdNormativa"=v_IdNormativa;
		IF v_Count>0 THEN	
			SELECT "Valor" INTO v_MON_MONTO_ART171
				FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
			WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='MON_MONTO_ART171' AND "IdNormativa"=v_IdNormativa;
			ELSE
			v_MON_MONTO_ART171:= 'UF';			
			--v_MON_MONTO_ART171:= 'USD';
		END IF;
		v_ValorLimite=	CAST(TRIM(v_VAL_MONTO_ART171) AS DECIMAL (26,8));
		
		--Valores de la cuenta propia	
		SELECT COALESCE(SOC."IdPersona",0) INTO v_IdPersonaCtaPropia
		FROM "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" as SOC 	
			INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" EMP ON (SOC."IdSocioDeNegocio"=EMP."IdSocioDeNegocio")	
		WHERE EMP."IdEmpresa" = p_IdEmpresa AND EMP."IdEstado"='ACT';

	
	
		--Destinatario
		  /* Obtener version del esquema */      
	
                SELECT TOP 1 "Esquema" INTO v_Esquema
                FROM "BSCL.Normativa.BD::DOC.TipoDocumento"
                WHERE "IdTipoDocumento" = 'ART171Car2';
                       
                v_FechaVersion :=TO_VARCHAR (v_FechaOperacionDesde, 'DD-MM-YYYY');
                SELECT COUNT(1) INTO v_count FROM "BSCL.Normativa.BD.Utilitarios::NOR.ObtenerVersionEsquema"('ART171Car2',:v_FechaVersion);
 
                IF(v_count>0) THEN
                        SELECT TOP 1 "P_OUT_VERSION" INTO v_Version FROM "BSCL.Normativa.BD.Utilitarios::NOR.ObtenerVersionEsquema"('ART171Car2',:v_FechaVersion);
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
               
		
		--Folio
		SELECT "Obligatorio" into v_folio_obligatorio
		FROM "BSCL.Normativa.BD::DOC.Campo"
		WHERE "Esquema" = 'ART171Car2' AND "Version" = v_Version AND "Nombre" = 'Folio';

		IF v_folio_obligatorio = 'S' THEN
			SELECT COUNT(1) into v_count_folio
			FROM "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
			WHERE "Esquema" = 'ART171Car2' AND "Version" = v_Version;
				
			IF v_count_folio = 0 THEN
				INSERT INTO "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
				("Esquema","Version","UltimoFolio","CodigoCertificado","NombreCertificado","CodigoPrograma")
				VALUES('ART171Car2',v_Version,1,'ART171Car2','Carta Dos Art. 171','ConsultarCartaDos');
			END IF;
				
			SELECT "UltimoFolio" into v_ultimo_folio
			FROM "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
			WHERE "Esquema" = 'ART171Car2' AND "Version" = v_Version AND "CodigoCertificado" = 'ART171Car2';
		END IF;

		--Firma
		SELECT "Obligatorio" into v_firma_obligatorio
		FROM "BSCL.Normativa.BD::DOC.Campo"
		WHERE "Esquema" = 'ART171Car2' AND "Version" = v_Version AND "Nombre" = 'Firma';

		v_firma = SELECT TOP 1 "Binario" AS "FirmaCorredora"
                FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
                WHERE "NombreImagen" = 'FIRMA_CORREDORA'
                ORDER BY "Version" DESC;

				v_logo =
                SELECT TOP 1
                    "Binario" AS "LogoCorredora"
                FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
                WHERE "NombreImagen" = 'LOGO_CORREDOR'
                  AND "IdEmpresa" = :p_IdEmpresa
                ORDER BY "Version" DESC;

		v_datos_corredor= SELECT "DocumentoIdentidadRepresentante" AS "rut_representante", PERSONA."NombreCompleto" AS "nombre_representante" FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMPRESA
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PERSONA ON (PERSONA."DocumentoIdentidad" = EMPRESA."DocumentoIdentidadRepresentante");

		
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
		v_SalidaConsultarOperaciones = SELECT OP.* FROM :v_SalidaConsultarOperaciones OP
									LEFT  OUTER JOIN 
									(
										SELECT"IdComprobanteReferencia"
										FROM "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" NC
										WHERE NC."IdEstadoFacturacion"='FAC' AND NC."IdTipoComprobante"='NCE' AND NC."IdComprobanteReferencia">0
									) NC
									ON (OP."IdOperacion"=NC."IdComprobanteReferencia" ) 
										WHERE NC."IdComprobanteReferencia" IS NULL;
		---Omitir operaciones menores a 500 UF	
		SELECT top 1 PF."Valor" INTO v_ValorParametro FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" PF 
		WHERE PF."IdParametroFinanciero" =v_MON_MONTO_ART171 AND PF."Fecha"<=TO_VARCHAR (v_FechaOperacionHasta, 'YYYY-MM-DD')
		ORDER BY "Fecha" DESC;
		
		v_SalidaConsultarOperaciones = SELECT OP1.* FROM :v_SalidaConsultarOperaciones OP1 WHERE OP1."Monto">=(v_ValorLimite*v_ValorParametro);					
		---- Transacciones de Custodia
		v_tablaOperaciones=
		SELECT
			ROW_NUMBER() OVER() AS "IdInterno",
			TO_VARCHAR (CUS."FechaMovimiento", 'YYYY-MM-DD') AS "FechaOperacion",
			CUS."IdOperacion",
			--CUS."NombreTipoOperacion", 	
			CUS."Nemotecnico", 			
			CUS."Cantidad",				
			CASE 
				WHEN CUS."IdClasificacionTipoInstrumento" IN ('RF', 'IF') THEN CUS."Tasa"
				ELSE CUS."Precio" 
			END 						AS "Precio",
			CUS."Monto"		 			AS "Monto",
			CUS."IdMoneda" 				AS "IdMoneda",
			--CUS."MonedaSistema" 		AS "Moneda",
			CUS."IdCliente",
			CUS."IdPortafolio",
			CUS."IdInstrumento",
			CUS."IdTipoProducto",
			CUS."IdClasificacionTipoInstrumento",
			CUS."IdTipoInstrumento",
			CUS."IdTipoOperacion",
			CUS."HIdTipoOperacion",
			--CUS."DocumentoIdentidad" 	AS "DocumentoIdentidad",
			REL."CodigoValorIndicador" AS  "IdTipoRelacion",
			REL."NombreValorIndicador" AS  "NombreTipoRelacion"
		FROM  :v_SalidaConsultarOperaciones CUS
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" SOC
					ON (SOC."IdCliente" =CUS."IdCliente")
		INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IND  --INFORMACION PRIVILEGIADA
						ON (IND."IdUso"='IND' AND IND."CodigoTipoIndicador"='INFPRV' AND IND."ValorIndicador"='S'
							AND IND."IdCliente" =SOC."IdCliente")
		INNER JOIN "BSCL.Normativa.BD::NOR.Producto" NPRO 							 --PRODUCTO NORMATIVA
						 ON (NPRO."IdNormativa"=v_IdNormativa
						 AND CUS."IdTipoProducto"= NPRO."IdTipoProducto"
						 AND (CUS."IdClasificacionTipoInstrumento"	= NPRO."IdClasificacionTipoInstrumento" OR CUS."IdClasificacionTipoInstrumento"='') 
						 AND (CUS."IdTipoInstrumento" 				= NPRO."IdTipoInstrumento"  			OR CUS."IdTipoInstrumento"='') 
						 AND (CUS."IdTipoOperacion" 				= NPRO."IdTipoOperacion" 				OR CUS."IdTipoOperacion"='')
						 AND (NPRO."IdEstado" = 'ACT'))

			INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" REL
		ON (REL."IdUso"='IND' AND REL."CodigoTipoIndicador"='CI1237' AND REL."CodigoValorIndicador" <>''
							AND REL."IdCliente" =SOC."IdCliente")			
					
		WHERE 
		--CUS."FechaMovimiento" BETWEEN TO_DATE(v_FechaOperacionDesde) AND TO_DATE(v_FechaOperacionHasta)
		--AND 
		(CUS."IdCliente" = p_IdCliente OR p_IdCliente= 0);
		
	
	   --NOTAS DE CREDITO, para omitir los comprobantes relacionados
		
	   v_NotaCredito=SELECT COM."IdComprobante",COM."IdOrden"
					FROM "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" COM
					INNER JOIN 
						(
						SELECT NC."IdOrden","IdComprobanteReferencia"
						FROM "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" NC
						WHERE NC."IdEstadoFacturacion"='FAC' AND NC."IdTipoComprobante"='NCE' AND NC."IdComprobanteReferencia">0
						) NCE
					ON (NCE."IdComprobanteReferencia"=COM."IdComprobante" AND COM."IdEstadoFacturacion"='FAC')
					GROUP BY COM."IdComprobante",COM."IdOrden";
				
				
				
				
				
	
		--Verificar si existen operaciones para la conversion monetaria
		--Sin Notas de Credito asociadas
		SELECT COUNT(1) INTO v_Count FROM :v_tablaOperaciones OP 
									LEFT OUTER JOIN :v_NotaCredito NC ON (OP."IdOperacion"=NC."IdOrden") 
									WHERE NC."IdOrden" IS NULL;
 				 
		IF v_Count > 0 THEN
		
			v_tablaOperaciones=SELECT OP.* FROM :v_tablaOperaciones OP 
									LEFT OUTER JOIN :v_NotaCredito NC ON (OP."IdOperacion"=NC."IdOrden") 
									WHERE NC."IdOrden" IS NULL;
			---IGUALAR PARIDADES			
						
			SELECT "BSCL.Plataforma.BD.Secuencias::MER.IdTrabajoParidad".NEXTVAL INTO v_IdTrabajoParidad FROM DUMMY;
			
			INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"("IdTrabajo","IdInterno","Fecha","IdMonedaOrigen","IdMonedaDestino","ValorCambioLocalModificado","MontoOrigen","IdUso")
			SELECT :v_IdTrabajoParidad,CAR."IdInterno" AS "IdInterno",TO_VARCHAR (CAR."FechaOperacion", 'YYYY-MM-DD') AS "Fecha", CAR."IdMoneda" as "IdMonedaOrigen", v_MON_MONTO_ART171 as "IdMonedaDestino",
			0 as "ValorCambioLocalModificado",CAR."Monto" as "MontoOrigen",'ORDENES' as "IdUso"
			FROM :v_tablaOperaciones CAR;

			call "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"(
				p_IdEmpresa => :p_IdEmpresa,
				p_IdTrabajo => :v_IdTrabajoParidad
			);
			


	
			v_out_ParidadDisponibles=
					SELECT DISTINCT COALESCE(outt."MontoDestino",0) AS  "MontoDestino",outt."IdMonedaDestino", par.*
					FROM :v_tablaOperaciones par
					LEFT JOIN "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" outt 
					on par."IdInterno" = outt."IdInterno" AND outt."IdTrabajo" = :v_IdTrabajoParidad;
				
		
			
											
			--Igualar montos de Paridades por cliente (no portafolio) e identifica aquella transacciones que sumadas alcanzan las 500 UF (limite parametro)
			v_MontoOperacionParidad=
				SELECT SUM(PAR."MontoDestino") AS "Monto", CAR."IdCliente",CAR."IdClasificacionTipoInstrumento",PAR."IdMonedaDestino"
				FROM :v_tablaOperaciones CAR 
				INNER JOIN :v_out_ParidadDisponibles PAR
				--ON (CAR."IdMoneda"= PAR."MonedaOrigen" AND  CAR."Monto"= PAR."MontoOrigen" AND PAR."FechaValor"=TO_VARCHAR (CAR."FechaOperacion", 'YYYY-MM-DD'))--AND v_MON_MONTO_ART171= PAR."MonedaDestino")
				ON (CAR."IdInterno"= PAR."IdInterno")
				GROUP BY CAR."IdCliente",CAR."IdClasificacionTipoInstrumento",PAR."IdMonedaDestino"
    			HAVING SUM(PAR."MontoDestino") >= v_ValorLimite;
			--Identifica los clientes cuyas transacciones sumadas alcanzan las 500 UF (limite parametro)
    			
			v_tablaOperacionesTmp=
			  	SELECT 	DO."Monto",DO."FechaOperacion",DO."IdOperacion",DO."IdTipoOperacion",DO."IdClasificacionTipoInstrumento", 
						DO."IdInstrumento",DO."Nemotecnico",DO."Cantidad",DO."Precio",DO."IdMoneda",DO."IdCliente",DO."IdPortafolio",
						DO."IdTipoProducto",DO."IdTipoInstrumento",DO."HIdTipoOperacion",DO."IdTipoRelacion",DO."NombreTipoRelacion"			
			  	FROM  :v_tablaOperaciones AS DO;
			 
			 --v_fechaHoraSistema
			 --p_FechaOperacionHasta
			 
			 
			 --SELECT 'FEcha version', v_FechaVersion FROM dummy;
			 --SELECT 'FEcha sis', v_FechaSistema FROM dummy;
			
		
			 
			  SELECT 
				 CASE WHEN MONTH (TO_DATE(v_FechaSistema))=1 THEN 'Enero'
					  WHEN MONTH (TO_DATE(v_FechaSistema))=2 THEN 'Febrero'
					  WHEN MONTH (TO_DATE(v_FechaSistema))=3 THEN 'Marzo'
					  WHEN MONTH (TO_DATE(v_FechaSistema))=4 THEN 'Abril'
					  WHEN MONTH (TO_DATE(v_FechaSistema))=5 THEN 'Mayo'
					  WHEN MONTH (TO_DATE(v_FechaSistema))=6 THEN 'Junio'
			  		  WHEN MONTH (TO_DATE(v_FechaSistema))=7 THEN 'Julio'
					  WHEN MONTH (TO_DATE(v_FechaSistema))=8 THEN 'Agosto'
					  WHEN MONTH (TO_DATE(v_FechaSistema))=9 THEN 'Septiembre'
					  WHEN MONTH (TO_DATE(v_FechaSistema))=10 THEN 'Octubre'
					  WHEN MONTH (TO_DATE(v_FechaSistema))=11 THEN 'Noviembre'
					  WHEN MONTH (TO_DATE(v_FechaSistema))=12 THEN 'Diciembre' ELSE ''END 
			 	
			 	INTO v_MesFechaSistema	
			 FROM dummy;
			
			 SELECT 
				 CASE WHEN MONTH (TO_DATE(p_FechaOperacionHasta))=1 THEN 'Enero'
					  WHEN MONTH (TO_DATE(p_FechaOperacionHasta))=2 THEN 'Febrero'
					  WHEN MONTH (TO_DATE(p_FechaOperacionHasta))=3 THEN 'Marzo'
					  WHEN MONTH (TO_DATE(p_FechaOperacionHasta))=4 THEN 'Abril'
					  WHEN MONTH (TO_DATE(p_FechaOperacionHasta))=5 THEN 'Mayo'
					  WHEN MONTH (TO_DATE(p_FechaOperacionHasta))=6 THEN 'Junio'
			  		  WHEN MONTH (TO_DATE(p_FechaOperacionHasta))=7 THEN 'Julio'
					  WHEN MONTH (TO_DATE(p_FechaOperacionHasta))=8 THEN 'Agosto'
					  WHEN MONTH (TO_DATE(p_FechaOperacionHasta))=9 THEN 'Septiembre'
					  WHEN MONTH (TO_DATE(p_FechaOperacionHasta))=10 THEN 'Octubre'
					  WHEN MONTH (TO_DATE(p_FechaOperacionHasta))=11 THEN 'Noviembre'
					  WHEN MONTH (TO_DATE(p_FechaOperacionHasta))=12 THEN 'Diciembre' ELSE ''END  INTO v_MesConsulta
			  FROM dummy;
			
			
			--Tabla de Salida
			p_out_TablaCarta2=
			SELECT
				CASE when v_folio_obligatorio = 'S' then (v_ultimo_folio + DENSE_RANK() OVER (ORDER BY OPE."IdCliente") - 1) else NULL end AS "Folio",
				v_NombreDestinatario AS "NombreDestinatario",
				v_CargoDestinatario AS "CargoDestinatario",				
				TO_VARCHAR(OPE."FechaOperacion",'DD-MM-YYYY') AS "FechaOperacion",
				OPE."HIdTipoOperacion" AS "IdTipoOperacion",				
				OPE."NombreTipoOperacion", 	
				OPE."IdClasificacionTipoInstrumento",				
				OPE."IdInstrumento",				
				OPE."Nemotecnico", 			
				OPE."Cantidad",
				RTRIM(RTRIM("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(OPE."Cantidad", 'dx', 4),'0'),',') AS "CantidadReporte",
				OPE."Precio",
				CASE 
					WHEN OPE."IdClasificacionTipoInstrumento" IN ('RF','IF')
						THEN '% ' || RTRIM(RTRIM("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(OPE."Precio", 'dx', 6),'0'),',')
						ELSE COALESCE(OPE."IdMoneda", '') || ' ' || RTRIM(RTRIM("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(OPE."Precio", 'dx', 6),'0'),',')
				END AS "PrecioReporte",
				OPE."Monto",
				"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(OPE."Monto", 'dx', 0) AS "MontoReporte",
				OPE."IdMoneda",
				OPE."Moneda",
				OPE."IdCliente",
				OPE."NombreCliente",
				OPE."DocumentoIdentidad",
				OPE."TipoDocumentoIdentidad",
				OPE."NombreAbrTipoDocumentoIdentidad" AS"NombreTipoDocumentoIdentidad",
				OPE."IdTipoRelacion",
				OPE."NombreTipoRelacion" ,
				OPE."IdTipoRelacion"	 AS "CodigoRelacionNorma",
				OPE."NombreTipoRelacion" AS "DescripcionRelacionNorma",
				v_MesFechaSistema AS "MesSistema",
				v_MesConsulta AS "MesConsulta",
				YEAR(v_FechaSistema) AS "YearSistema",
				YEAR(p_FechaOperacionHasta) AS "YearConsulta",
				DAYOFMONTH (v_FechaSistema) AS "DiaSistema",
				DAYOFMONTH (p_FechaOperacionHasta) AS "DiaConsulta",
				"Logo"."LogoCorredora" AS "LogoCorredora",
				CASE when v_firma_obligatorio = 'S' then "Firma"."FirmaCorredora" else '' end as "FirmaCorredora",
				--"Firma"."FirmaCorredora" AS "LogoCorredora",
				CASE when v_firma_obligatorio = 'S' then "Rep"."rut_representante" else '' end AS "rut_representante",
				CASE when v_firma_obligatorio = 'S' then "Rep"."nombre_representante" else '' end AS "nombre_representante"
			FROM	
			(			
	     			SELECT 
						OP."FechaOperacion",
						--OP."IdOperacion",
						OP."IdTipoOperacion",
						OP."HIdTipoOperacion",
						COALESCE(TPRO."Nombre",'') AS "NombreTipoOperacion", 	
						OP."Nemotecnico", 			
						SUM(OP."Cantidad") AS "Cantidad",				
						OP."Precio",
						SUM(OP."Monto")	AS "Monto",
						OP."IdMoneda",
						COALESCE(MON."Nombre",'') AS "Moneda",
						COALESCE(MON."Simbolo",'') AS "SimboloMoneda",
						OP."IdCliente",
						CLI."NombreCompleto" AS "NombreCliente",
						--OP."IdPortafolio",
						OP."IdInstrumento",
						--OP."IdTipoProducto",
						OP."IdClasificacionTipoInstrumento",
						--OP."IdTipoInstrumento",
						CLI."DocumentoIdentidad" AS "DocumentoIdentidad",
						CLI."TipoDocumentoIdentidad",
						TIDEN."NombreAbrTipoDocumentoIdentidad",
						OP."IdTipoRelacion",
						OP."NombreTipoRelacion"
	     			FROM :v_tablaOperacionesTmp OP INNER JOIN :v_MontoOperacionParidad PA
	     				ON (OP."IdCliente"=PA."IdCliente" AND OP."IdClasificacionTipoInstrumento"=PA."IdClasificacionTipoInstrumento")
	     			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
	     				ON (OP."IdCliente"=CLI."IdCliente")
	     			INNER JOIN "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" as TIDEN ON  CLI."TipoDocumentoIdentidad" = TIDEN."IdTipoDocumentoIdentidad" 
	     			LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TPRO	
					ON (OP."IdTipoOperacion"=TPRO."IdTipoOperacion")
	     			LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda"  MON
	     			ON (OP."IdMoneda" = MON."IdMoneda") 		
					GROUP BY OP."FechaOperacion",OP."IdCliente",CLI."NombreCompleto",OP."IdClasificacionTipoInstrumento",OP."IdTipoOperacion",OP."HIdTipoOperacion",COALESCE(TPRO."Nombre",''),OP."Nemotecnico",
					OP."Precio",OP."IdMoneda",COALESCE(MON."Nombre",''),COALESCE(MON."Simbolo",''),OP."IdInstrumento",CLI."DocumentoIdentidad",CLI."TipoDocumentoIdentidad",TIDEN."NombreAbrTipoDocumentoIdentidad" ,OP."IdTipoRelacion",OP."NombreTipoRelacion"
					ORDER BY OP."IdCliente",OP."FechaOperacion",OP."Nemotecnico",OP."HIdTipoOperacion" 
			) OPE
				LEFT JOIN :v_firma AS "Firma" ON "Firma"."FirmaCorredora" IS NOT NULL
				LEFT JOIN :v_logo AS "Logo" ON "Logo"."LogoCorredora" IS NOT NULL
				LEFT JOIN :v_datos_corredor AS "Rep" ON "Rep"."rut_representante" IS NOT NULL
				ORDER BY OPE."IdCliente",OPE."FechaOperacion",OPE."Nemotecnico",OPE."HIdTipoOperacion";
			--LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::NOR.ConsultarRelacionNormativa" NOR
			--ON (OPE."IdTipoRelacion"=NOR."IdTipoRelacion" AND NOR."IdNormativa"=v_IdNormativa);
			
			
			
				SELECT MAX("Folio") into v_max_folio
				FROM :p_out_TablaCarta2;
	
				UPDATE "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
				SET "UltimoFolio" = CASE WHEN v_max_folio IS NULL THEN (v_ultimo_folio+1) ELSE (v_max_folio+1) END
				WHERE "Esquema" = 'ART171Car2' AND "Version" = v_Version;
			
			
			
			
			ELSE
			
				p_out_TablaCarta2=SELECT
					v_ultimo_folio AS "Folio",
					'' AS "NombreDestinatario",
					'' AS "CargoDestinatario",					
					'' AS "FechaOperacion",
					'' AS "IdTipoOperacion",
					'' AS "NombreTipoOperacion", 	
					'' AS "IdClasificacionTipoInstrumento",				
					0 AS "IdInstrumento",				
					'' AS "Nemotecnico", 			
					0 AS "Cantidad",				
					'' AS "CantidadReporte",
					0 AS "Precio",
					'' AS "PrecioReporte",
					0 AS "Monto",
					'0' AS "MontoReporte",
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
					'' AS "MesSistema",				    
				    '' AS "MesConsulta",
				    '' AS "YearSistema",
					'' AS "YearConsulta",
					'' AS "DiaSistema",
					'' AS "DiaConsulta",
					"Logo"."LogoCorredora" AS "LogoCorredora", 
					CASE when v_firma_obligatorio = 'S' then "Firma"."FirmaCorredora" else '' end as "FirmaCorredora",
					CASE when v_firma_obligatorio = 'S' then "Rep"."rut_representante" else '' end AS "rut_representante",
					CASE when v_firma_obligatorio = 'S' then "Rep"."nombre_representante" else '' end AS "nombre_representante"
					FROM DUMMY
					LEFT JOIN :v_firma AS "Firma" ON "Firma"."FirmaCorredora" IS NOT NULL
					LEFT JOIN :v_logo AS "Logo" ON "Logo"."LogoCorredora" IS NOT NULL
					LEFT JOIN :v_datos_corredor AS "Rep" ON "Rep"."rut_representante" IS NOT NULL
					WHERE 1=1;

		END IF;


	SELECT * FROM :p_out_TablaCarta2
	ORDER BY "Folio" ASC;					

END;