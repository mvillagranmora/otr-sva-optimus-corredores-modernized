SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Normativa.BD.Servicios::FATCA.ConsultarFatcaDetalle";
GO
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::FATCA.ConsultarFatcaDetalle"
(
	--VARIABLES DE ENTRADA OBLIGATORIAS
 	IN p_IdEmpresa			BIGINT,
 	
 	--VARIABLES DEL PROCESO
	IN p_IdCliente							BIGINT 			DEFAULT 0,
	IN p_MontoPatrimonioMinimo				DECIMAL(26,8)	DEFAULT 0,
	IN p_MontoPatrimonioMaximo				DECIMAL(26,8)	DEFAULT 0,
	IN p_FechaEjecucion						NVARCHAR(10)	DEFAULT '',
	IN p_FechaEnrolamiento					NVARCHAR(10)	DEFAULT '',
	IN p_ClasificacionFatca					NVARCHAR(100)	DEFAULT '',
	IN p_IndicadorFatca						NVARCHAR(1)		DEFAULT '0',
	IN p_TipoPersona						NVARCHAR(1)		DEFAULT '',
	IN p_Indicios							NVARCHAR(1)		DEFAULT '',
	
	IN p_IdTrabajoParametros				BIGINT DEFAULT 0, --Se asumen valores por defecto en los parametros
	
	-- VARIABLES DE SALIDA
	OUT p_out_IdInfoTran					BIGINT
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
		DECLARE v_Count BIGINT;
		DECLARE v_IdCliente BIGINT;
		DECLARE v_MontoPatrimonioMinimo DECIMAL(26,8);
		DECLARE v_MontoPatrimonioMaximo DECIMAL(26,8);
		DECLARE v_FechaEjecucion NVARCHAR(10);
		DECLARE v_FechaEnrolamiento NVARCHAR(10);
		DECLARE v_ClasificacionFatca NVARCHAR(100);
		DECLARE v_IndicadorFatca NVARCHAR(1);
		DECLARE v_TipoPersona NVARCHAR(1);
		DECLARE v_Indicios NVARCHAR(1);
		----Obtener parametros Tabla Global
		SELECT  COUNt(1) INTO v_Count FROM "BSCL.Normativa.BD::GBL.Parametros" 
		WHERE   "IdInterno"=p_IdTrabajoParametros;
		
		IF v_Count > 0 THEN
			v_GBLParametros=SELECT "IdInterno","Tipo","NombreParametro" AS "TipoParametro","Valor"   FROM "BSCL.Normativa.BD::GBL.Parametros" WHERE  "IdInterno"=p_IdTrabajoParametros;
			v_Datos = SELECT 
							  CASE WHEN SOL."TipoParametro"='IDCLI'	THEN "Valor" ELSE NULL END AS "IDCLI"
							, CASE WHEN SOL."TipoParametro"='MMIN'	THEN "Valor" ELSE NULL END AS "MMIN"
							, CASE WHEN SOL."TipoParametro"='MMAX'	THEN "Valor" ELSE NULL END AS "MMAX"
							, CASE WHEN SOL."TipoParametro"='FEJEC'	THEN "Valor" ELSE NULL END AS "FEJEC"
							, CASE WHEN SOL."TipoParametro"='FENR'	THEN "Valor" ELSE NULL END AS "FENR"
							, CASE WHEN SOL."TipoParametro"='CFAT'	THEN "Valor" ELSE NULL END AS "CFAT"
							, CASE WHEN SOL."TipoParametro"='IFAT'	THEN "Valor" ELSE NULL END AS "IFAT"
							, CASE WHEN SOL."TipoParametro"='TP'	THEN "Valor" ELSE NULL END AS "TP"
							, CASE WHEN SOL."TipoParametro"='RIND'	THEN "Valor" ELSE NULL END AS "RIND"
					 FROM :v_GBLParametros SOL;
			
			v_DatosAgrupado =SELECT  
					  MAX(TRIM(SOL."IDCLI"))		AS "IDCLI"
					, MAX(TRIM(SOL."MMIN"))			AS "MMIN"
					, MAX(TRIM(SOL."MMAX"))			AS "MMAX"
					, MAX(TRIM(SOL."FEJEC"))		AS "FEJEC"
					, MAX(TRIM(SOL."FENR"))			AS "FENR"
					, MAX(TRIM(SOL."CFAT"))			AS "CFAT"
					, MAX(TRIM(SOL."IFAT"))			AS "IFAT"
					, MAX(TRIM(SOL."TP"))			AS "TP"
					, MAX(TRIM(SOL."RIND"))			AS "RIND"
					FROM :v_Datos SOL;
			
			SELECT TOP 1 "IDCLI","MMIN","MMAX","FEJEC","FENR","CFAT","IFAT","TP","RIND" 
   			INTO v_IdCliente,v_MontoPatrimonioMinimo, v_MontoPatrimonioMaximo, v_FechaEjecucion, v_FechaEnrolamiento, v_ClasificacionFatca, v_IndicadorFatca, v_TipoPersona, v_Indicios 
   			FROM :v_DatosAgrupado;
   		ELSE 
   			v_IdCliente = p_IdCliente;
   			v_MontoPatrimonioMinimo = p_MontoPatrimonioMinimo;
   			v_MontoPatrimonioMaximo = p_MontoPatrimonioMaximo;
   			v_FechaEjecucion = p_FechaEjecucion;
   			v_FechaEnrolamiento = p_FechaEnrolamiento;
   			v_ClasificacionFatca = p_ClasificacionFatca;
   			v_IndicadorFatca = p_IndicadorFatca;
   			v_TipoPersona = p_TipoPersona;
   			v_Indicios = p_Indicios;
		END IF;
		
		SELECT "BSCL.Normativa.BD.Secuencias::GBL.IdFATCAD".NEXTVAL INTO p_out_IdInfoTran FROM DUMMY;
		
		INSERT INTO "BSCL.Normativa.BD::GBL.SALIDA_FATCADETALLE"
		("IdInterno","Rut","TipoPersona","Indicios","ClasificacionFatca","ClienteFatca" ,"AutorizaOrgFatca","PatrimonioClp","PatrimonioUsd","FechaEnrolamiento"   
		,"FechaModIndicios","FechaModFacta","Residencia" ,"LugarNacimiento","PaisConstitucion","CasaMatriz","Nacionalidad","CodPostal","NumTelefono"
		,"OrdenPermanente","PoderEfectivo","Beneficiario","NumIdentificacion","TipoPersonaRel","TipoRelacion","ClasificacionFatcaRel","ClienteFatcaRel"
		,"UsPersonRel","ResidenciaRel","LugarNacimientoRel","PaisConstitucionRel","CasaMatrizRel","NacionalidadRel","CodPostalRel","NumTelefonoRel"
		,"OrdenPermanenteRel","PoderEfectivoRel","BeneficiarioRel","FechaCargaInformacion")
		SELECT
			p_out_IdInfoTran,
			DF."rut_cli" as "Rut",    
			CASE WHEN DF."tipo_persona" = 'N' THEN 'Persona Natural' ELSE 'Persona Júridica' END as "TipoPersona",
			CASE WHEN DF."ind_us_person" = '0' THEN 'NO' ELSE 'SI' END AS "Indicios",    
			CCF."Nombre" as "ClasificacionFatca",     
			CASE WHEN DF."indicador_fatca" = 'N' THEN 'NO' ELSE 'SI' END AS "ClienteFatca",    
			CASE WHEN DF."auth_fatca" = 'N' THEN 'NO' ELSE 'SI' END as "AutorizaOrgFatca",    
			DF."patrimonio_peso" as "PatrimonioClp",    
			DF."patrimonio_dolar" as "PatrimonioUsd",    
			DF."fecha_enrolamiento" as "FechaEnrolamiento",    
			DF."fecha_ult_modificacion" as "FechaModIndicios",    
			DF."fecha_ult_modificacion_fatca" as "FechaModFacta",    
			CASE DF."tipo_persona" 
				WHEN 'N' THEN 
						CASE DF."ind_residencia" 
									 WHEN '0' THEN 'NO'
									 WHEN 'E' THEN 'Excepción'
									 ELSE 'SI' END 
				ELSE 'N/A' END as "Residencia", 
			CASE DF."tipo_persona" 
				WHEN 'N' THEN 
					CASE DF."ind_nacimiento" WHEN '0' THEN 'NO'
						  WHEN 'E' THEN 'Excepción'
						  ELSE 'SI' END 
				ELSE 'N/A' END as "LugarNacimiento",
			CASE DF."tipo_persona" 
				WHEN 'J' THEN 
					CASE DF."ind_nacimiento" WHEN '0' THEN 'NO'
						  WHEN 'E' THEN 'Excepción'
						  ELSE 'SI' END 
				ELSE 'N/A' END  as "PaisConstitucion",    
			CASE DF."tipo_persona" 
				WHEN 'J' THEN 
						CASE DF."ind_residencia" 
									 WHEN '0' THEN 'NO'
									 WHEN 'E' THEN 'Excepción'
									 ELSE 'SI' END 
				ELSE 'N/A' END as "CasaMatriz",    
			CASE DF."ind_nacionalidad" 
									 WHEN '0' THEN 'NO'
									 WHEN 'E' THEN 'Excepción'
									 ELSE 'SI' END as "Nacionalidad",
			CASE DF."ind_direccion" 
									 WHEN '0' THEN 'NO'
									 WHEN 'E' THEN 'Excepción'
									 ELSE 'SI' END  as "CodPostal",
			CASE DF."ind_telefono" 
									 WHEN '0' THEN 'NO'
									 WHEN 'E' THEN 'Excepción'
									 ELSE 'SI' END  as "NumTelefono", 
			CASE DF."ind_ordenes" 
									 WHEN '0' THEN 'NO'
									 WHEN 'E' THEN 'Excepción'
									 ELSE 'SI' END as "OrdenPermanente",    
			CASE DF."ind_firma" 
									 WHEN '0' THEN 'NO'
									 WHEN 'E' THEN 'Excepción'
									 ELSE 'SI' END  as "PoderEfectivo",    
			CASE DF."ind_benef_empresa" 
									 WHEN '0' THEN 'NO'
									 WHEN 'E' THEN 'Excepción'
									 ELSE 'SI' END as "Beneficiario", 
			DF2."rut_cli" as "NumIdentificacion",
			CASE WHEN DF2."tipo_persona" = 'N' THEN 'Persona Natural' ELSE 'Persona Júridica' END as "TipoPersonaRel",
			CTR."NombreTipoRelacion" as "TipoRelacion",
			CCF2."Nombre" as "ClasificacionFatcaRel",
			CASE WHEN DF2."indicador_fatca" = 'N' THEN 'NO' ELSE 'SI' END as "ClienteFatcaRel",
			CASE WHEN DF2."auth_fatca" = 'N' THEN 'NO' ELSE 'SI' END as "UsPersonRel",
			CASE DF2."tipo_persona" 
				WHEN 'N' THEN 
						CASE DF2."ind_residencia" 
									 WHEN '0' THEN 'NO'
									 WHEN 'E' THEN 'Excepción'
									 ELSE 'SI' END 
				ELSE 'N/A' END as "ResidenciaRel",
			CASE DF2."tipo_persona" 
				WHEN 'N' THEN 
					CASE DF2."ind_nacimiento" WHEN '0' THEN 'NO'
						  WHEN 'E' THEN 'Excepción'
						  ELSE 'SI' END 
				ELSE 'N/A' END as "LugarNacimientoRel",
			CASE DF2."tipo_persona" 
				WHEN 'J' THEN 
					CASE DF2."ind_nacimiento" WHEN '0' THEN 'NO'
						  WHEN 'E' THEN 'Excepción'
						  ELSE 'SI' END 
				ELSE 'N/A' END as "PaisConstitucionRel",
			CASE DF2."tipo_persona" 
				WHEN 'J' THEN 
						CASE DF2."ind_residencia" 
									 WHEN '0' THEN 'NO'
									 WHEN 'E' THEN 'Excepción'
									 ELSE 'SI' END 
				ELSE 'N/A' END as "CasaMatrizRel",
			CASE DF2."ind_nacionalidad" 
									 WHEN '0' THEN 'NO'
									 WHEN 'E' THEN 'Excepción'
									 ELSE 'SI' END  as "NacionalidadRel",
			CASE DF2."ind_direccion" 
									 WHEN '0' THEN 'NO'
									 WHEN 'E' THEN 'Excepción'
									 ELSE 'SI' END  as "CodPostalRel",
			CASE DF2."ind_telefono" 
									 WHEN '0' THEN 'NO'
									 WHEN 'E' THEN 'Excepción'
									 ELSE 'SI' END as "NumTelefonoRel",
			CASE DF2."ind_ordenes" 
									 WHEN '0' THEN 'NO'
									 WHEN 'E' THEN 'Excepción'
									 ELSE 'SI' END as "OrdenPermanenteRel",
			CASE DF2."ind_firma" 
									 WHEN '0' THEN 'NO'
									 WHEN 'E' THEN 'Excepción'
									 ELSE 'SI' END as "PoderEfectivoRel",
			CASE DF2."ind_benef_empresa" 
									 WHEN '0' THEN 'NO'
									 WHEN 'E' THEN 'Excepción'
									 ELSE 'SI' END as "BeneficiarioRel",	
			DF."FechaCargaInformacion" as "FechaCargaInformacion"
		FROM "BSCL.Normativa.BD::NOR.DatosFatca" DF
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarClasificacionFatca" as CCF on (DF."clasificacion_fatca" = CCF."IdClasificacionFatca")
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacion" RL ON (DF."IdPersona" = RL."IdPersonaOrigen")
		LEFT JOIN "BSCL.Normativa.BD::NOR.DatosFatca" DF2 ON (DF2."IdPersona" = RL."IdPersonaDestino")
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarClasificacionFatca" as CCF2 on (DF2."clasificacion_fatca" = CCF2."IdClasificacionFatca")
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacion" AS CTR ON (RL."IdTipoRelacion" = CTR."IdTipoRelacion")
		WHERE ((COALESCE(v_MontoPatrimonioMinimo,0) = 0 AND COALESCE(v_MontoPatrimonioMaximo,0) = 0) OR (DF."patrimonio_dolar" BETWEEN v_MontoPatrimonioMinimo AND v_MontoPatrimonioMaximo))
		AND (COALESCE(v_IdCliente,0) = 0 OR DF."IdCliente" = v_IdCliente)
		AND (COALESCE(v_FechaEnrolamiento,'') = '' OR TO_DATE(DF."fecha_enrolamiento",'YYYY-MM-DD') <= v_FechaEnrolamiento)
		AND (COALESCE(v_ClasificacionFatca,'0') = '0' OR DF."clasificacion_fatca" = v_ClasificacionFatca)
		AND DF."FechaCargaInformacion" = v_FechaEjecucion
		AND (COALESCE(v_TipoPersona,'0') = '0' OR DF."tipo_persona" = v_TipoPersona)
		AND (COALESCE(v_IndicadorFatca,'N') = 'N' OR DF."indicador_fatca" = v_IndicadorFatca)
		AND (v_Indicios = 'N' OR (DF."ind_us_person" = '1' AND v_Indicios = 'S'));
		
		
END;