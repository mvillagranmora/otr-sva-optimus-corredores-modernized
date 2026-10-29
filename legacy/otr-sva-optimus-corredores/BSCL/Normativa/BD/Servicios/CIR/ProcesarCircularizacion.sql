SET SCHEMA "BSCL";
DROP PROCEDURE  "BSCL.Normativa.BD.Servicios::CIR.ProcesarCircularizacion";
CREATE PROCEDURE 
--
--  Proposito: Servicio para procesar la nomina de Circularizacion del periodo.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 25/07/2016

"BSCL.Normativa.BD.Servicios::CIR.ProcesarCircularizacion" ( 

    --VARIABLES DEL PROCESO
	IN p_IdSolicitudCircular 		  BIGINT ,

    --VARIABLES DE SALIDA OBLIGATORIAS

	OUT p_out_TablaSalida	TABLE("IdCliente"							BIGINT)

)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
		--------------------
		--Declare Variables
		
		DECLARE v_IdNormativa 								 NVARCHAR(15):='CIRCULARIZACION';
		DECLARE v_TipoTarea  	              				 NVARCHAR(40):='PROCESONOMINACIRCULARIZACION';
		DECLARE v_out_MensajeSalida							 NVARCHAR(200):='CIRCULARIZACION INACTIVA';
  		DECLARE v_fechaHoraSistema 							 NVARCHAR(40):='';
  		DECLARE v_periodoUltimoDia 							 NVARCHAR(40):='';
  		DECLARE v_FechaOperacionDesde, v_FechaOperacionHasta NVARCHAR(10):='';
		DECLARE v_IdPersonaCtaPropia,v_Count,v_Clicount,v_IdDocumento,v_IdEmpresa,v_IdProceso	 BIGINT:=0;
		DECLARE v_RUT,v_IDCLI,v_TRUT,v_PERI,v_USUA 			 NVARCHAR(40):='';
		DECLARE v_out_error			  						 NVARCHAR(500);
		DECLARE v_Notificar								 	 NVARCHAR(520):='';
		DECLARE	v_IdClienteCtaPropia,v_IdTrabajoParidad		 BIGINT:=0;
		
		DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
		DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
	    DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
		DECLARE v_paramContratoCustodia,v_paramClientesSinCustodia 					NVARCHAR(10):= 'INA';

		---Parametros




		IF p_IdSolicitudCircular>0 THEN
		
			SELECT TOP 1 "IdDocumento" INTO v_IdDocumento 
			FROM "BSCL.Normativa.BD::NOR.SolicitudCircular"
			WHERE "IdSolicitudCircular" = p_IdSolicitudCircular;
			
			SELECT TOP 1 "IdEmpresa" INTO v_IdEmpresa 
			FROM "BSCL.Normativa.BD::DOC.Documento"
			WHERE "IdDocumento" = v_IdDocumento;
		
			v_Parametros=SELECT * FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" SOL
			 	WHERE SOL."IdSolicitudCircular" = p_IdSolicitudCircular;
			
			v_Datos = SELECT 
				  CASE WHEN SOL."TipoParametro"='RUT'	THEN "Valor" ELSE NULL END AS "RUT"
				, CASE WHEN SOL."TipoParametro"='IDCLI'	THEN "Valor" ELSE NULL END AS "IDCLI"
				, CASE WHEN SOL."TipoParametro"='TRUT'	THEN "Valor" ELSE NULL END AS "TRUT"
				, CASE WHEN SOL."TipoParametro"='PERI'	THEN "Valor" ELSE NULL END AS "PERI"				
				, CASE WHEN SOL."TipoParametro"='USUA'	THEN "Valor" ELSE NULL END AS "USUA"
				FROM :v_Parametros SOL
			 	WHERE SOL."TipoParametro"<>'RUT_MAS';

			v_IndicadorCertificadoCustodia = SELECT
			IND."IdEntidad",
			IND."ValorIndicador"
			FROM "BSCL.Plataforma.BD::IND.Indicador" AS IND 
			INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" AS TIND ON TIND."IdTipoIndicador" = IND."IdTipoIndicador" 
			AND TIND."NombreTipoIndicador" = 'Contrato de Custodia' 
			AND TIND."ClaseDato" = 'CHECKBOX' 
			AND TIND."IdTipoEntidad" = 'CLIENTE';

			SELECT "IdEstado" INTO v_paramContratoCustodia FROM "BSCL.Normativa.BD::CIR.Parametro" AS SOL
						WHERE SOL."Valor"= 'filtraContratoCustodia' AND SOL."Codigo"='CLIENTE' AND SOL."Periodo"='';	
			
			SELECT "IdEstado" INTO v_paramClientesSinCustodia FROM "BSCL.Normativa.BD::CIR.Parametro" AS SOL
						WHERE SOL."Valor"= 'filtraIncluirClientesSinCustodia' AND SOL."Codigo"='CLIENTE' AND SOL."Periodo"='';	
				
		END IF;

		
		v_DatosAgrupado =SELECT  
							  MAX(SOL."RUT") 	AS "RUT"
							, MAX(SOL."IDCLI")	AS "IDCLI"
							, MAX(SOL."TRUT")	AS "TRUT"
							, MAX(SOL."PERI")	AS "PERI"														
							, MAX(SOL."USUA")	AS "USUA"	
							FROM :v_Datos SOL;
							
	
		SELECT TOP 1 "RUT","IDCLI","TRUT","PERI","USUA"  INTO v_RUT,v_IDCLI,v_TRUT,v_PERI,v_USUA FROM :v_DatosAgrupado;	
		
		call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
				p_IdEmpresa => v_IdEmpresa,
				p_out_FechaHoraSistema => v_fechaHoraSistema
		);
		---------------------------------------------------------------------------	
		------------			Parametros especiificos para clientes
		---------------------------------------------------------------------------
		v_ParamClientes=SELECT	0 AS "IdCliente", 0 AS "IdPortafolio"	FROM DUMMY WHERE 1=2;

		--Identifica parametros por RUT
		IF v_TRUT > 0 THEN
			v_ParamClientes =SELECT	NULL AS "IdCliente",NULL AS "IdPortafolio"
							FROM DUMMY;
		ELSE
			 SELECT COUNT(1) INTO v_Count FROM :v_Parametros SOL WHERE SOL."TipoParametro"='RUT_MAS';
			 
			IF v_Count>0 THEN
			 
			  v_ParamClientes=
				SELECT CLI."IdCliente",POR."IdPortafolio" AS "IdPortafolio"
				  FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI
				  INNER JOIN 
				  		(SELECT DISTINCT "Valor" AS "RUT" FROM :v_Parametros
				  			WHERE "TipoParametro"='RUT_MAS'
				  		) AS PAR
				  ON (RTRIM(PAR."RUT") = CLI."DocumentoIdentidad")
				  INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR
				  ON (POR."IdCliente"=CLI."IdCliente")		
				WHERE CLI."TipoDocumentoIdentidad"='R'
				UNION ALL SELECT * FROM :v_ParamClientes WHERE "IdCliente" IS NOT NULL;
			END IF;		
			
			IF v_IDCLI > 0 THEN
				v_ParamClientes=SELECT	POR."IdCliente" AS "IdCliente",	POR."IdPortafolio" AS "IdPortafolio"
							FROM  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR
							WHERE POR."IdCliente"=:v_IDCLI
						 UNION ALL SELECT * FROM :v_ParamClientes WHERE "IdCliente" IS NOT NULL;
						
			END IF ;
		
		
		
	    END IF;
						
	
		
		---Valores de la cuenta propia	
		SELECT CLI."IdCliente" INTO v_IdClienteCtaPropia
		FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
		--INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP ON (CLI."IdPersona"=EMP."IdPersona")
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" as SOC ON CLI."IdPersona" =SOC."IdPersona"		
		INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" EMP ON (SOC."IdSocioDeNegocio"=EMP."IdSocioDeNegocio")	
		WHERE EMP."IdEmpresa" = v_IdEmpresa;
		
		---------------------------------------------------------------------------	
		------------			Parametros especiificos para Clasificacion
		---------------------------------------------------------------------------

		 SELECT TO_VARCHAR (LAST_DAY (TO_DATE(v_PERI, 'YYYYMM')), 'YYYY-MM-DD') "last day"  INTO v_periodoUltimoDia FROM DUMMY;

		
		/*FLUJO  CUSTODIA*/
		v_tablaClientes=SELECT "IdCliente", "IdPortafolio"	FROM :v_ParamClientes;
		v_tablaInstrumentos=SELECT NULL AS "IdClasificacionTipoInstrumento" FROM DUMMY;
							
		CALL "BSCL.Normativa.BD.Servicios::CIR.ConsultarCustodia"(
				p_IdEmpresa                   => v_IdEmpresa,
				p_FechaConsulta               => :v_periodoUltimoDia,-- 'YYYY-MM-DD'),
				p_FormaCustodia				  => 'CUSTODIA_TOTAL',
				p_TablaCarteras				  => :v_tablaClientes,
				p_TablaInstrumentos			  => :v_tablaInstrumentos,
				p_out_TablaSalida    		  => V_out_FlujoTotalizado
		);		
		--Borrar : Filtrar solo clientes validos del sistema
		--V_out_FlujoTotalizado=SELECT fl.* FROM :V_out_FlujoTotalizado  AS FL
	--	WHERE fl."INGMontoATasaMercadoActual"<69105601200548.43080300;

		--BUSCAR TODO EL UNIVERSO DE CLIENTES Y PORTAFOLIOS ACTIVOS
		IF v_TRUT > 0 AND v_paramClientesSinCustodia = 'ACT' THEN
			v_tablaClientes = SELECT POR."IdCliente"  AS "IdCliente" , POR."IdPortafolio"  AS "IdPortafolio"
								FROM "BSCL.Portafolio.BD::POR.Portafolio" AS POR LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON POR."IdCliente" = CLI."IdCliente";
		END IF;

		/*Se acota el universo de clientes a Procesar: Solo considerar clientes que no estan en la nomina*/
		
		V_out_FlujoTotalizadoFinal= SELECT ROW_NUMBER() OVER() AS "IdInterno" ,FLU.* FROM :V_out_FlujoTotalizado FLU
									  LEFT OUTER JOIN "BSCL.Normativa.BD::CIR.Nomina" PRO
									   ON (PRO."IdCliente"=FLU."IdCliente" AND PRO."IdPortafolio"=FLU."IdPortafolio"
									   AND  PRO. "IdDocumento"=v_IdDocumento)
									   WHERE PRO."IdCliente" IS NULL;
									   
		v_tablaClientes			  = SELECT CLI.* FROM :v_tablaClientes CLI
									  LEFT OUTER JOIN "BSCL.Normativa.BD::CIR.Nomina" PRO
									   ON (PRO."IdCliente"=CLI."IdCliente" AND PRO."IdPortafolio"=CLI."IdPortafolio"
									   AND  PRO. "IdDocumento"=v_IdDocumento)
									   WHERE PRO."IdCliente" IS NULL;
		
	
		SELECT COUNT(1) INTO v_Count FROM :V_out_FlujoTotalizadoFinal;
 		SELECT COUNT(1) INTO v_Clicount FROM :v_tablaClientes;		
 		
		IF v_Count > 0 OR v_Clicount>0 THEN ---INICIAL
			
		
			---IGUALAR PARIDADES			
			v_out_MensajeSalida='CLIENTES : ' || CAST (:v_Count AS VARCHAR(15)) || ' PERIODO : ' || v_PERI;		
		
			SELECT "BSCL.Plataforma.BD.Secuencias::MER.IdTrabajoParidad".NEXTVAL INTO v_IdTrabajoParidad FROM DUMMY;
		
						
			INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"("IdTrabajo","IdInterno","Fecha","IdMonedaOrigen","IdMonedaDestino","ValorCambioLocalModificado","MontoOrigen","IdUso")
			SELECT :v_IdTrabajoParidad,CAR."IdInterno" AS "IdInterno",TO_VARCHAR (CAR."FechaOperacion", 'YYYY-MM-DD') AS "Fecha", CASE WHEN RTRIM(LTRIM(COALESCE(CAR."IdMoneda",'')))='' THEN 'CLP' ELSE CAR."IdMoneda" END as "IdMonedaOrigen", 'CLP' as "IdMonedaDestino",
			0 as "ValorCambioLocalModificado",CAR."ValorMercadoCustodia" as "MontoOrigen",'ORDENES' as "IdUso"
			FROM :V_out_FlujoTotalizadoFinal CAR  where CAR."IdMoneda"<> 'CLP';

			call "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"(
				p_IdEmpresa => :v_IdEmpresa,
				p_IdTrabajo => :v_IdTrabajoParidad
			);
			
			
			v_out_ParidadDisponibles=
					SELECT DISTINCT COALESCE(outt."MontoDestino",0) AS  "MontoDestino",outt."IdMonedaDestino",par."IdInterno"--, par.*
					FROM :V_out_FlujoTotalizadoFinal par
					LEFT JOIN "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" outt 
					on par."IdInterno" = outt."IdInterno" AND outt."IdTrabajo" = :v_IdTrabajoParidad
					where par."IdMoneda"<> 'CLP'
					UNION ALL 
					SELECT  CAR."ValorMercadoCustodia" AS  "MontoDestino",CAR."IdMoneda" AS "IdMonedaDestino",CAR."IdInterno"--, par.*
					FROM :V_out_FlujoTotalizadoFinal CAR  where CAR."IdMoneda"= 'CLP'
					;
			
			
			v_out_ParidadDisponibles=SELECT DISTINCT * FROM  :v_out_ParidadDisponibles;
			
											
			---SUMATORIA DE MONTOS SEGUN RESUMEN (CLIENTE/INSTRUMENTO)

			v_MontoOperacionParidadCliente=
				SELECT SUM(PAR."MontoDestino") AS "Monto", CAR."IdCliente",CAR."IdPortafolio",PAR."IdMonedaDestino"
				FROM :V_out_FlujoTotalizadoFinal CAR 
				INNER JOIN :v_out_ParidadDisponibles PAR
--				ON (CAR."IdMoneda"= PAR."MonedaOrigen" AND  CAR."MontoDisponible"= PAR."MontoOrigen" )
				ON (CAR."IdInterno"= PAR."IdInterno")
				GROUP BY CAR."IdCliente",CAR."IdPortafolio",PAR."IdMonedaDestino";
				
			v_MontoOperacionParidadInstrumento=
				SELECT CAR."IdCliente", CAR."IdPortafolio",SUM(PAR."MontoDestino") AS "Monto", CAR."IdInstrumento",CAR."IdClasificacionTipoInstrumento",CAR."IdEmisor",CAR."Nemotecnico",PAR."IdMonedaDestino"
				FROM :V_out_FlujoTotalizadoFinal CAR 
				INNER JOIN :v_out_ParidadDisponibles PAR
				--ON (CAR."IdMoneda"= PAR."MonedaOrigen" AND  CAR."MontoDisponible"= PAR."MontoOrigen" )
				ON (CAR."IdInterno"= PAR."IdInterno")
				WHERE  CAR."IdCliente" <> v_IdClienteCtaPropia
				GROUP BY CAR."IdCliente", CAR."IdPortafolio",CAR."IdInstrumento",CAR."IdClasificacionTipoInstrumento",CAR."IdEmisor"
				,CAR."Nemotecnico",PAR."IdMonedaDestino";
						

						
			v_MontoOperacionParidadCTPROPIA =
				SELECT CAR."IdCliente", CAR."IdPortafolio",SUM(PAR."MontoDestino") AS "Monto", CAR."IdInstrumento",CAR."IdClasificacionTipoInstrumento",CAR."IdEmisor",CAR."Nemotecnico",PAR."IdMonedaDestino"
				FROM :V_out_FlujoTotalizadoFinal CAR 
				INNER JOIN :v_out_ParidadDisponibles PAR
--				ON (CAR."IdMoneda"= PAR."MonedaOrigen" AND  CAR."MontoDisponible"= PAR."MontoOrigen" )
				ON (CAR."IdInterno"= PAR."IdInterno")
				WHERE  CAR."IdCliente" = v_IdClienteCtaPropia
				GROUP BY CAR."IdCliente", CAR."IdPortafolio",CAR."IdInstrumento",CAR."IdClasificacionTipoInstrumento",CAR."IdEmisor"
				,CAR."Nemotecnico",PAR."IdMonedaDestino";
		---------------------------------------------------------------------------	
		------------			NOMINA DE CLIENTES
		---------------------------------------------------------------------------			
		
		---Clientes con custodia
			v_tablaCustodia=	
							SELECT CUS."IdCliente",CUS."IdPortafolio"
										FROM :V_out_FlujoTotalizadoFinal CUS
										GROUP BY CUS."IdCliente",CUS."IdPortafolio";
		 
		--Insercion de Nomina (Con Custodia y clientes enviados por parametro)
		
			v_tablaNomina=SELECT * FROM :v_tablaCustodia;
			
			--Incluye Clientes sin custodia
			v_tablaNomina=
			SELECT C."IdCliente",C."IdPortafolio" 
			FROM :v_tablaClientes C
			LEFT OUTER JOIN :v_tablaNomina N
			ON (C."IdCliente"=N."IdCliente" AND C."IdPortafolio"=N."IdPortafolio")
			WHERE N."IdPortafolio" IS NULL
			UNION ALL SELECT * FROM :v_tablaNomina;
		
			
		IF v_paramContratoCustodia = 'ACT' THEN
			v_tablaGrabarNomina	 =
			SELECT
				("BSCL.Normativa.BD.Secuencias::CIR.Nomina".NEXTVAL) 	 AS  "IdNomina",
				v_IdDocumento 				 AS  "IdDocumento",
				NOM."IdCliente",
				NOM."IdPortafolio",
				POR."CodigoPortafolio",
				SOC."IdPersona",
				--PER."IdDireccionElectronicaPrincipal" AS "IdDireccionElectronica",
				--POR."IdTelefonoContacto",
				--POR."IdDireccionEnvioCorrespondencia" AS "IdDireccionFisica",
				NULL AS "DireccionElectronica",
				NULL AS "DireccionFisica",
				IND."CodigoValorIndicador"   AS "FormaEnvio",    
				v_fechaHoraSistema 			 AS "FechaIngreso",
				CURRENT_UTCTIMESTAMP 		 AS "FechaCreacion",
				CURRENT_UTCTIMESTAMP 		 AS "FechaModificacion",
				v_USUA 						 AS "IdUsuarioCreador"
			FROM :v_tablaNomina NOM 
			INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR
			ON (NOM."IdCliente"=POR."IdCliente" AND NOM."IdPortafolio"=POR."IdPortafolio")
			INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC
			ON (SOC."IdCliente"=NOM."IdCliente")
			INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" IND  --ENVIO  DE CIRCULARIZACION
				ON (IND."IdUso"='IND' AND IND."CodigoTipoIndicador"='CIRCENVIO' 
					AND IND."IdCliente" =NOM."IdCliente" AND  NOM."IdPortafolio"=IND."IdPortafolio")
			LEFT OUTER JOIN :v_MontoOperacionParidadCliente  PAR
			ON (PAR."IdCliente"=NOM."IdCliente" AND PAR."IdPortafolio"=NOM."IdPortafolio")
			-- INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER
			--ON (SOC."IdPersona"=PER."IdPersona")
			LEFT OUTER JOIN "BSCL.Normativa.BD::CIR.Nomina" PRO
			ON (PRO."IdCliente"=POR."IdCliente" AND PRO."IdPortafolio"=POR."IdPortafolio" AND  PRO. "IdDocumento"=v_IdDocumento)
			INNER JOIN :v_IndicadorCertificadoCustodia  AS CER
			ON CER."IdEntidad" = NOM."IdCliente"
			AND CER."ValorIndicador" = 'S'
			WHERE PRO."IdCliente" IS NULL AND POR."CodigoPortafolio" IS NOT NULL;
		ELSE
			v_tablaGrabarNomina	 =
			SELECT
				("BSCL.Normativa.BD.Secuencias::CIR.Nomina".NEXTVAL) 	 AS  "IdNomina",
				v_IdDocumento 				 AS  "IdDocumento",
				NOM."IdCliente",
				NOM."IdPortafolio",
				POR."CodigoPortafolio",
				SOC."IdPersona",
				--PER."IdDireccionElectronicaPrincipal" AS "IdDireccionElectronica",
				--POR."IdTelefonoContacto",
				--POR."IdDireccionEnvioCorrespondencia" AS "IdDireccionFisica",
				NULL AS "DireccionElectronica",
				NULL AS "DireccionFisica",
				IND."CodigoValorIndicador"   AS "FormaEnvio",    
				v_fechaHoraSistema 			 AS "FechaIngreso",
				CURRENT_UTCTIMESTAMP 		 AS "FechaCreacion",
				CURRENT_UTCTIMESTAMP 		 AS "FechaModificacion",
				v_USUA 						 AS "IdUsuarioCreador"
			FROM :v_tablaNomina NOM 
			INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR
			ON (NOM."IdCliente"=POR."IdCliente" AND NOM."IdPortafolio"=POR."IdPortafolio")
			INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC
			ON (SOC."IdCliente"=NOM."IdCliente")
			INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" IND  --ENVIO  DE CIRCULARIZACION
				ON (IND."IdUso"='IND' AND IND."CodigoTipoIndicador"='CIRCENVIO' 
					AND IND."IdCliente" =NOM."IdCliente" AND  NOM."IdPortafolio"=IND."IdPortafolio")
			LEFT OUTER JOIN :v_MontoOperacionParidadCliente  PAR
			ON (PAR."IdCliente"=NOM."IdCliente" AND PAR."IdPortafolio"=NOM."IdPortafolio")
			-- INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER
			--ON (SOC."IdPersona"=PER."IdPersona")
			LEFT OUTER JOIN "BSCL.Normativa.BD::CIR.Nomina" PRO
			ON (PRO."IdCliente"=POR."IdCliente" AND PRO."IdPortafolio"=POR."IdPortafolio" AND  PRO. "IdDocumento"=v_IdDocumento)
			WHERE PRO."IdCliente" IS NULL AND POR."CodigoPortafolio" IS NOT NULL;
		END IF;

			INSERT INTO "BSCL.Normativa.BD::CIR.Nomina"
			 (
				   "IdNomina" ,
				   "IdDocumento" ,
				   "IdCliente",
				   "IdPortafolio",
				   "CodigoPortafolio",
				   "IdPersona",
				   "DireccionElectronica",
				   "DireccionFisica",
				   "FormaEnvio",   
				   "FechaIngreso",
				   "FechaCreacion",
				   "FechaModificacion",
				   "IdUsuarioCreador"
			 )SELECT
			 	   "IdNomina" ,
				   "IdDocumento" ,
				   "IdCliente",
				   "IdPortafolio",
				   "CodigoPortafolio",
				   "IdPersona",
				   "DireccionElectronica",
				   "DireccionFisica",
				   "FormaEnvio",   
				   "FechaIngreso",
				   "FechaCreacion",
				   "FechaModificacion",
				   "IdUsuarioCreador" FROM :v_tablaGrabarNomina;
		---------------------------------------------------------------------------	
		------------			RESUMEN DE CLIENTES
		---------------------------------------------------------------------------	
			DELETE FROM "BSCL.Normativa.BD::CIR.ResumenCliente" RCLI
			WHERE  RCLI."IdDocumento" =v_IdDocumento
			AND EXISTS (SELECT 1 FROM :v_ParamClientes CLI
						WHERE (RCLI."IdCliente"   =CLI."IdCliente"    OR CLI."IdCliente"    IS NULL )
						AND   (RCLI."IdPortafolio"=CLI."IdPortafolio" OR CLI."IdPortafolio" IS NULL));
						
			INSERT INTO "BSCL.Normativa.BD::CIR.ResumenCliente"
			 (
			 
				   "IdDocumento",
				   "IdCliente",
				   "IdPortafolio",
				   "CodigoPortafolio",
				   "IdPersona",
				   "ValorPresente",
				   "FechaIngreso",
				   "FechaCreacion",
				   "FechaModificacion",
				   "IdUsuarioCreador"

			 )
		   SELECT
		    v_IdDocumento 				 AS  "IdDocumento",
			NOM."IdCliente",
			NOM."IdPortafolio",
			NOM."CodigoPortafolio",
			NOM."IdPersona",
			COALESCE(PAR."Monto",0) AS "ValorPresente",
		    v_fechaHoraSistema 			 AS "FechaIngreso",
			CURRENT_UTCTIMESTAMP 		 AS "FechaCreacion",
			CURRENT_UTCTIMESTAMP 		 AS "FechaModificacion",
			v_USUA 						 AS "IdUsuarioCreador"
		   FROM :v_tablaGrabarNomina NOM
		   LEFT OUTER JOIN :v_MontoOperacionParidadCliente  PAR
		   ON (PAR."IdCliente"=NOM."IdCliente" AND PAR."IdPortafolio"=NOM."IdPortafolio")
		   LEFT OUTER JOIN "BSCL.Normativa.BD::CIR.ResumenCliente" PRO
		   ON (PRO."IdCliente"=NOM."IdCliente" AND PRO."IdPortafolio"=NOM."IdPortafolio" AND  PRO. "IdDocumento"=v_IdDocumento)
		   WHERE PRO."IdCliente" IS NULL;


		---------------------------------------------------------------------------	
		------------			RESUMEN DE INSTRUMENTOS
		---------------------------------------------------------------------------	
		SELECT COUNT(1) INTO v_Count FROM :v_MontoOperacionParidadInstrumento;
		IF v_Count>0 THEN	
		
			/*
			DELETE FROM "BSCL.Normativa.BD::CIR.ResumenInstrumento" RINS 
			WHERE  RINS."IdDocumento" =v_IdDocumento
			AND EXISTS (SELECT 1 FROM :v_tablaInstrumentos INS
						WHERE (RINS."IdClasificacionTipoInstrumento" =INS."IdClasificacionTipoInstrumento" OR INS."IdClasificacionTipoInstrumento" IS NULL ));
			*/
			
			--Actualizacion del resumen segun clientes agregados a la nomina
	
			UPDATE RINS
			SET RINS."TotalValorPresente"=(RINS."TotalValorPresente" + COALESCE(INS."Monto",0) )
			FROM "BSCL.Normativa.BD::CIR.ResumenInstrumento" RINS
			INNER JOIN  :v_MontoOperacionParidadInstrumento INS
			ON (RINS."IdClasificacionTipoInstrumento" =INS."IdClasificacionTipoInstrumento" AND RINS."IdInstrumento"=INS."IdInstrumento"
				AND RINS."IdEmisor"=INS."IdEmisor")
				WHERE  RINS."IdDocumento" =v_IdDocumento;
	
			--Insercion de Instrumentos que nuevos en el resumen	
			INSERT INTO "BSCL.Normativa.BD::CIR.ResumenInstrumento"
			(
			   "IdDocumento",
			   "IdCliente",
			   "IdPortafolio",
			   "IdInstrumento",
			   "IdEmisor",
			   "Nemotecnico",
			   "IdClasificacionTipoInstrumento",
			   "EsCtaPropia",
			   "TotalValorPresente",
			   "FechaIngreso",
			   "FechaCreacion",
			   "FechaModificacion",
			   "IdUsuarioCreador"
			)
		    SELECT
			    v_IdDocumento 				 AS  "IdDocumento",
				INS."IdCliente",
			    INS."IdPortafolio",
				INS."IdInstrumento",
				INS."IdEmisor",
				INS."Nemotecnico",
				INS."IdClasificacionTipoInstrumento",
				'N' AS "EsCtaPropia",
				COALESCE(INS."Monto",0) AS "TotalValorPresente",
			    v_fechaHoraSistema 			 AS "FechaIngreso",
				CURRENT_UTCTIMESTAMP 		 AS "FechaCreacion",
				CURRENT_UTCTIMESTAMP 		 AS "FechaModificacion",
				v_USUA 						 AS "IdUsuarioCreador"
			   FROM :v_MontoOperacionParidadInstrumento  INS
			   LEFT OUTER JOIN "BSCL.Normativa.BD::CIR.ResumenInstrumento" PRO
			   ON (PRO."IdInstrumento"=INS."IdInstrumento" AND PRO."IdClasificacionTipoInstrumento"=INS."IdClasificacionTipoInstrumento"
			   	AND  PRO."IdEmisor"=INS."IdEmisor" AND PRO."Nemotecnico"=INS."Nemotecnico"	
			   	AND  PRO."IdCliente" =INS."IdCliente" AND  PRO."IdPortafolio" =INS."IdPortafolio"
			    AND  PRO."IdDocumento"=v_IdDocumento
			   )
			   WHERE PRO."IdCliente" IS NULL;		

		END IF;
		
		---CUENTA PROPIA (INSTRUMENTOS)
		SELECT COUNT(1) INTO v_Count FROM :v_MontoOperacionParidadCTPROPIA;
		IF v_Count>0 THEN
		
			DELETE FROM "BSCL.Normativa.BD::CIR.ResumenInstrumento" RINS 
				WHERE  RINS."IdDocumento" =v_IdDocumento
				AND RINS."EsCtaPropia"='S';
		
		
			INSERT INTO "BSCL.Normativa.BD::CIR.ResumenInstrumento"
			(
			   "IdDocumento",
			   "IdCliente",
			   "IdPortafolio",
			   "IdInstrumento",
			   "IdEmisor",
			   "Nemotecnico",
			   "IdClasificacionTipoInstrumento",
			   "EsCtaPropia",
			   "TotalValorPresente",
			   "FechaIngreso",
			   "FechaCreacion",
			   "FechaModificacion",
			   "IdUsuarioCreador"
			)
		    SELECT
			    v_IdDocumento 				 AS  "IdDocumento",
				INS."IdCliente",
			    INS."IdPortafolio",
				INS."IdInstrumento",
				INS."IdEmisor",
				INS."Nemotecnico",
				INS."IdClasificacionTipoInstrumento",
				'S' AS "EsCtaPropia",
				COALESCE(INS."Monto",0) AS "TotalValorPresente",
			    v_fechaHoraSistema 			 AS "FechaIngreso",
				CURRENT_UTCTIMESTAMP 		 AS "FechaCreacion",
				CURRENT_UTCTIMESTAMP 		 AS "FechaModificacion",
				v_USUA 						 AS "IdUsuarioCreador"
			   FROM :v_MontoOperacionParidadCTPROPIA  INS
			   LEFT OUTER JOIN "BSCL.Normativa.BD::CIR.ResumenInstrumento" PRO
			   ON (PRO."IdInstrumento"=INS."IdInstrumento" AND PRO."IdClasificacionTipoInstrumento"=INS."IdClasificacionTipoInstrumento"
			   	AND  PRO."IdEmisor"=INS."IdEmisor" AND PRO."Nemotecnico"=INS."Nemotecnico"	
			   	AND  PRO."IdCliente" =INS."IdCliente" AND  PRO."IdPortafolio" =INS."IdPortafolio"			   	
			    AND  PRO."IdDocumento"=v_IdDocumento
			    AND  PRO."EsCtaPropia"='S')
			   WHERE PRO."IdInstrumento" IS NULL;		
			   
		END IF;


		
		---------------------------------------------------------------------------	
		------------			NOTIFICAR A CLIENTES
		---------------------------------------------------------------------------			
/*
		SELECT COUNT(1) INTO v_Count
			FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
		WHERE "IdEmpresa"=v_IdEmpresa AND "CodigoParametro"='NOTIFiCA_NOMINA_CIRCULARIZA' AND "IdNormativa"=v_IdNormativa;
		
		IF v_Count>0 THEN
			SELECT "Valor" INTO v_Notificar
				FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
			WHERE "IdEmpresa"=v_IdEmpresa AND "CodigoParametro"='NOTIFiCA_NOMINA_CIRCULARIZA' AND "IdNormativa"=v_IdNormativa;
			ELSE
			v_Notificar:='N';
		END IF;
		
		
		
			IF TRIM(UPPER(v_Notificar))='S' THEN
				v_TablaNotificacion	=
						SELECT "IdCliente", "IdPortafolio", "IdPersona"
						FROM :v_tablaGrabarNomina;
						
									  
					CALL"BSCL.Normativa.BD.Servicios::CIR.NotificarCliente"(
							p_IdEmpresa 		=> v_IdEmpresa,
							p_IdUsuario 		=> v_USUA,
							p_IdModulo 			=> 'NOR',
							p_TablaNotificacion	=> :v_TablaNotificacion,	
							p_Periodo			=> v_PERI,
							--salida
							p_out_IdEvento 		=> V_out_IdEvento,
							p_out_AdvertenciasContador	 => v_out_AdvertenciasContador,
							p_out_AdvertenciasDetalle 	 => v_out_AdvertenciasDetalle,
							p_out_ErroresContador 		 => v_out_ErroresContador,
							p_out_ErroresDetalle 		 => v_out_ErroresDetalle);
			
			
			END IF;
		*/
		ELSE
			v_out_MensajeSalida='NO SE ENCUENTRAN CLIENTES PERIODO : ' || v_PERI;

		END IF; ---INICIAL
				
				
		---------------------------------------------------------------------------	
		------------			GRABAR/ACTUALIZAR CERTIFICADOS CUSTODIA
		---------------------------------------------------------------------------				
		
	CALL "BSCL.Normativa.BD.Servicios.CER::SolicitudGrabarCertificado" ( 
			    p_IdModulo					        => 'NOR',
			    p_IdUsuario							=> v_USUA,
			    p_IdEmpresa  						=> v_IdEmpresa,
			    p_Periodo        				 	=> v_PERI,
			    p_IdTipoDocumento        		 	=> 'CIRCCarta',
			    p_IdSolicitudCircular				=> p_IdSolicitudCircular,
			    --salida
				p_out_AdvertenciasContador	 => v_out_AdvertenciasContador,
				p_out_AdvertenciasDetalle 	 => v_out_AdvertenciasDetalle,
				p_out_ErroresContador 		 => v_out_ErroresContador,
				p_out_ErroresDetalle 		 => v_out_ErroresDetalle);				
				
		--------------------------------------------------------------------------------
		-- Registrar tabla Procesos
		--------------------------------------------------------------------------------

		SELECT "BSCL.Normativa.BD.Secuencias::PRO.IdProceso".NEXTVAL INTO v_IdProceso FROM DUMMY;
		
		INSERT INTO "BSCL.Normativa.BD::PRO.Proceso" (
				   "IdProceso",
				   "IdEmpresa",
				   "IdModulo",
				   "IdNormativa",
				   "IdTipoDocumento",
				   "IdTipoTarea", --Tarea de inscripcion en BAT
				   "IdSolicitudCircular",
				   "IdProcesoBatch",
				   "IdEstado",
				   "Descripcion",
				   "FechaIngreso",
				   "FechaCreacion"
				   )
					SELECT
					v_IdProceso,
					v_IdEmpresa,
					'NOR',
					v_IdNormativa,
					'CIRCNomina',
					v_TipoTarea,
					p_IdSolicitudCircular,
					0,
					'TER',
					v_out_MensajeSalida,
					TO_VARCHAR (:v_fechaHoraSistema, 'YYYY-MM-DD'),
					CURRENT_UTCTIMESTAMP
					FROM DUMMY;		
				

p_out_TablaSalida=
		SELECT				
				0 AS "IdCliente"
				FROM DUMMY WHERE 1=2;	
		
SELECT				
				0 AS "IdCliente"
				FROM DUMMY WHERE 1=2;

END; 