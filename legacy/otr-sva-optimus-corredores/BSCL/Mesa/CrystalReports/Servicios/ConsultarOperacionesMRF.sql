SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::ConsultarOperacionesMRF"; 

CREATE PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::ConsultarOperacionesMRF" (
	-- Parametros entrada comunes en todos los servicios privados
	IN p_IdEmpresa				BIGINT,			--ID Empresa destino
	IN p_IdModulo				NVARCHAR(3),	--ID del Modulo que lo invoca

	-- Parametros entrada del servicio privado
	IN p_IdOperacion			BIGINT,
	IN p_IdPais					BIGINT
	
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN

	BEGIN
		--------------------
		--Variables
		--------------------
		
		DECLARE v_FechaSistema	DATE;
		DECLARE v_HoraSistema	SECONDDATE;
		DECLARE v_MontoOpIni	DECIMAL(26,8);
		DECLARE v_MontoOpFin	DECIMAL(26,8);
		DECLARE v_Pais			NVARCHAR(10);
		DECLARE v_Moneda		NVARCHAR(3);
		DECLARE v_MontoOpIniTxt NVARCHAR(2000);
		DECLARE v_MontoOpFinTxt NVARCHAR(2000);
		DECLARE v_FecOpeMasPlazo DATE;
		DECLARE v_NombrePais	NVARCHAR(100);
		DECLARE	v_Registros		INT;
		DECLARE	v_DecimalesMoneda	INT DEFAULT 0;
		DECLARE	v_SumaMontoOP	DECIMAL(26,4);
		DECLARE	v_SumaMontoOPTxt NVARCHAR(2000);
		DECLARE	v_SumaMontoOPMonedaTxt NVARCHAR(2000);
		DECLARE v_MontoOperacionFinal DECIMAL(26,8);
		DECLARE v_MontoOperacionFinalTxt  NVARCHAR(2000);
		DECLARE v_SegundosZona  		BIGINT;
		DECLARE v_FechaHoraSistemaLOC 	SECONDDATE;
		DECLARE v_DireccionPrimariaCLI  NVARCHAR(1);
		DECLARE v_DireccionPrimariaCOR  NVARCHAR(1);
		DECLARE v_DireccionCliente 	 NVARCHAR(100);
		DECLARE v_ComunaCliente 	 NVARCHAR(100);
		DECLARE v_DireccionCorredora NVARCHAR(100);		
		DECLARE v_IdApoderadoCLI1 BIGINT;
		DECLARE v_IdApoderadoCLI2 BIGINT;
		DECLARE v_IdApoderadoCOR1 BIGINT;
		DECLARE v_IdApoderadoCOR2 BIGINT;
		DECLARE v_IdPortafolio BIGINT;
		DECLARE v_IdPortafolioCorredor BIGINT;
		DECLARE v_ExisteRegistro INT;
	
		
		DECLARE v_logo TABLE("LogoCorredora" BLOB);
		
		--Se obtiene fecha y hora de sistema
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,v_HoraSistema);
		
		-- Se Obtiene fecha y hora local
		SELECT TO_BIGINT("Valor") INTO v_SegundosZona
	  	FROM "BSCL.Plataforma.BD::PLA.Configuracion"
		WHERE "IdEmpresa" =  :p_IdEmpresa and  "CodigoParametro" = 'PLA.SegundosZonaHoraria';
	
		
		--Se obtiene monto moneda en texto
		SELECT OP."MontoOperacionInicialMoneda", OP."MontoOperacionFinalMoneda", MDA."Simbolo", OP."IdTipoReajuste" INTO v_MontoOpIni, v_MontoOpFin, v_Pais, v_Moneda
		FROM "BSCL.Mesa.BD::MES.Operacion" OP
		INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda" MDA
			ON OP."IdTipoReajuste" = MDA."IdMoneda"
		WHERE OP."IdOperacion" = :p_IdOperacion;
	
		
		CALL "BSCL.Plataforma.BD.Servicios::ConvertirMontoEscrito"(:v_MontoOpIni, :v_Pais, :v_Moneda, :v_MontoOpIniTxt);
		CALL "BSCL.Plataforma.BD.Servicios::ConvertirMontoEscrito"(:v_MontoOpFin, :v_Pais, :v_Moneda, :v_MontoOpFinTxt);
		
		
		--Se obtiene FechaOperacion mas PlazoEnDias
		SELECT ADD_DAYS(OP."FechaOperacion",OP."PlazoEnDias") INTO v_FecOpeMasPlazo
		FROM "BSCL.Mesa.BD::MES.Operacion" OP
		WHERE OP."IdOperacion" = :p_IdOperacion;
		
		
		--Se obtiene nombre pais
		SELECT PA."NombrePais" INTO v_NombrePais
		FROM "BSCL.Plataforma.BD.Vistas::REG.ConsultarPais" PA
		WHERE PA."IdPais" = :p_IdPais;
		
		
		IF :v_Moneda != 'CLP' THEN
			SELECT 
			"PrecioDecimales" INTO v_DecimalesMoneda
			FROM "BSCL.Plataforma.BD::PLA.MonedaFormato"
			WHERE "IdUsoMonedaFormato"= 'MES'
			  AND "IdMoneda"=:v_Moneda
			LIMIT 1;
		END IF;
		
		v_logo =
		 SELECT TOP 1 "Binario" AS "LogoCorredora"
		 FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
		 WHERE "NombreImagen" = 
		    CASE
		        WHEN EXISTS (
		            SELECT 1
		            FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
		            WHERE "NombreImagen" = 'LOGO_CORREDOR_HORIZONTAL'
		        )
		        THEN 'LOGO_CORREDOR_HORIZONTAL'
		        ELSE 'LOGO_CORREDOR'
		    END
		 ORDER BY "Version" DESC;
	
		--INDICADORES
		--Apoderados Cliente y corredora
		
		
		SELECT count(1) INTO v_ExisteRegistro
		FROM  "BSCL.Mesa.BD::MES.Operacion" AS OP 
		INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS IND 
		ON IND."IdPortafolio" = OP."IdPortafolio" AND IND."CodigoTipoIndicador" IN ('MES_APO1')
		WHERE OP."IdOperacion" = :p_IdOperacion;
		
		IF :v_ExisteRegistro > 0 THEN		
			SELECT IND."ValorIndicador", OP."IdPortafolio"  INTO v_IdApoderadoCLI1, v_IdPortafolio
			FROM  "BSCL.Mesa.BD::MES.Operacion" AS OP 
			INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS IND 
			ON IND."IdPortafolio" = OP."IdPortafolio" AND IND."CodigoTipoIndicador" IN ('MES_APO1')
			WHERE OP."IdOperacion" = :p_IdOperacion;
		ELSE
			v_IdApoderadoCLI1 = 0;
			v_IdPortafolio    = 0;
		END IF;			
			
			
		
		SELECT count(1) INTO v_ExisteRegistro
		FROM  "BSCL.Mesa.BD::MES.Operacion" AS OP 
		INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS IND 
		ON IND."IdPortafolio" = OP."IdPortafolio" AND IND."CodigoTipoIndicador" IN ('MES_APO2')
		WHERE OP."IdOperacion" = :p_IdOperacion;
		
		IF :v_ExisteRegistro > 0 THEN
			SELECT IND."ValorIndicador", OP."IdPortafolio"  INTO v_IdApoderadoCLI2, v_IdPortafolio
			FROM  "BSCL.Mesa.BD::MES.Operacion" AS OP 
			INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS IND 
			ON IND."IdPortafolio" = OP."IdPortafolio" AND IND."CodigoTipoIndicador" IN ('MES_APO2')
			WHERE OP."IdOperacion" = :p_IdOperacion;
		ELSE
			v_IdApoderadoCLI2 = 0;
			v_IdPortafolio    = 0;
		END IF;			
						
		SELECT count(1) INTO v_ExisteRegistro	
		FROM  "BSCL.Mesa.BD::MES.Operacion" AS OP 
		INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS IND 
		ON IND."IdPortafolio" = OP."IdPortafolioCorredor" AND IND."CodigoTipoIndicador" IN ('MES_APO1')
		WHERE OP."IdOperacion" = :p_IdOperacion;
			
			
		IF :v_ExisteRegistro > 0 THEN
			SELECT IND."ValorIndicador",OP."IdPortafolioCorredor" INTO v_IdApoderadoCOR1, v_IdPortafolioCorredor
			FROM  "BSCL.Mesa.BD::MES.Operacion" AS OP 
			INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS IND 
			ON IND."IdPortafolio" = OP."IdPortafolioCorredor" AND IND."CodigoTipoIndicador" IN ('MES_APO1')
			WHERE OP."IdOperacion" = :p_IdOperacion;
		ELSE
			v_IdApoderadoCOR1 = 0;
			v_IdPortafolioCorredor    = 0;
		END IF;				
			
			
		SELECT count(1) INTO v_ExisteRegistro
		FROM  "BSCL.Mesa.BD::MES.Operacion" AS OP 
		INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS IND 
		ON IND."IdPortafolio" = OP."IdPortafolioCorredor" AND IND."CodigoTipoIndicador" IN ('MES_APO2')
		WHERE OP."IdOperacion" = :p_IdOperacion;
	
		IF :v_ExisteRegistro > 0 THEN	
			SELECT IND."ValorIndicador",OP."IdPortafolioCorredor" INTO v_IdApoderadoCOR2, v_IdPortafolioCorredor
			FROM  "BSCL.Mesa.BD::MES.Operacion" AS OP 
			INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS IND 
			ON IND."IdPortafolio" = OP."IdPortafolioCorredor" AND IND."CodigoTipoIndicador" IN ('MES_APO2')
			WHERE OP."IdOperacion" = :p_IdOperacion;
		ELSE
			v_IdApoderadoCOR2 = 0;
			v_IdPortafolioCorredor    = 0;
		END IF;				
	
		v_ApoderadosCorredor1 = SELECT 
								 PER."NombreCompleto"
								,PER."DocumentoIdentidad"
								,:v_IdPortafolioCorredor AS "IdPortafolioCorredor"
								FROM  "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER WHERE  PER."IdPersona"= :v_IdApoderadoCOR1;
							
		v_ApoderadosCorredor2 = SELECT 
								 PER."NombreCompleto"
								,PER."DocumentoIdentidad"
								,:v_IdPortafolioCorredor AS "IdPortafolioCorredor"
								FROM  "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER WHERE  PER."IdPersona"= :v_IdApoderadoCOR2;
							
		v_ApoderadoCliente1 = SELECT 
								 PER."NombreCompleto"
								,PER."DocumentoIdentidad"
								,:v_IdPortafolio AS "IdPortafolio"
								FROM  "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER WHERE PER."IdPersona"= :v_IdApoderadoCLI1;
		v_ApoderadoCliente2 = SELECT 
								 PER."NombreCompleto"
								,PER."DocumentoIdentidad"
								,:v_IdPortafolio AS "IdPortafolio"
								FROM  "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER WHERE PER."IdPersona"= :v_IdApoderadoCLI2;
								
								
		SELECT count(1) INTO v_ExisteRegistro
		FROM  "BSCL.Mesa.BD::MES.Operacion" AS OP 
		INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS IND ON IND."IdPortafolio" = OP."IdPortafolio" AND IND."CodigoTipoIndicador" IN ('GEN_DIR_PROPIA')
		WHERE OP."IdOperacion" = :p_IdOperacion;
		
		IF :v_ExisteRegistro > 0 THEN								
			SELECT  IND."ValorIndicador" INTO v_DireccionPrimariaCLI 
			FROM  "BSCL.Mesa.BD::MES.Operacion" AS OP 
			INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS IND ON IND."IdPortafolio" = OP."IdPortafolio" AND IND."CodigoTipoIndicador" IN ('GEN_DIR_PROPIA')
			WHERE OP."IdOperacion" = :p_IdOperacion;
		ELSE
				v_DireccionPrimariaCLI = '';
		END IF;			
	
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------		
		SELECT count(1) INTO v_ExisteRegistro
		FROM  "BSCL.Mesa.BD::MES.Operacion" AS OP 
		INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS IND ON IND."IdPortafolio" = OP."IdPortafolioCorredor" AND IND."CodigoTipoIndicador" IN ('GEN_DIR_PROPIA')
		WHERE OP."IdOperacion" = :p_IdOperacion;
		
		IF :v_ExisteRegistro > 0 THEN
			SELECT  IND."ValorIndicador" INTO v_DireccionPrimariaCOR 
			FROM  "BSCL.Mesa.BD::MES.Operacion" AS OP 
			INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS IND ON IND."IdPortafolio" = OP."IdPortafolioCorredor" AND IND."CodigoTipoIndicador" IN ('GEN_DIR_PROPIA')
			WHERE OP."IdOperacion" = :p_IdOperacion;
		ELSE
			v_DireccionPrimariaCOR = '';
		END IF;	
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------		
	
		if(v_DireccionPrimariaCLI='S') THEN 

			SELECT count(1) INTO v_ExisteRegistro
			FROM  "BSCL.Mesa.BD::MES.Operacion" AS OP 
			INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS IND ON IND."IdPortafolio" = OP."IdPortafolio" AND IND."CodigoTipoIndicador" IN ('GEN_CODDIR')
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS DIR ON DIR."IdDireccionFisica" = IND."ValorIndicador"
			WHERE OP."IdOperacion" = :p_IdOperacion;

			IF :v_ExisteRegistro > 0 THEN		
				SELECT COALESCE (DIR."Calle"||' '||DIR."Numero"||' '||DIR."Departamento"||' - '||DIR."NombreCiudad",'-') AS "NombreLargo",DIR."NombreDivisionPais"  INTO v_DireccionCliente, v_ComunaCliente
				FROM  "BSCL.Mesa.BD::MES.Operacion" AS OP 
				INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS IND ON IND."IdPortafolio" = OP."IdPortafolio" AND IND."CodigoTipoIndicador" IN ('GEN_CODDIR')
				INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS DIR ON DIR."IdDireccionFisica" = IND."ValorIndicador"
				WHERE OP."IdOperacion" = :p_IdOperacion;
			ELSE
				v_DireccionCliente = '';
				v_ComunaCliente	   = '';
			END IF;		
		
		ELSE 
			SELECT count(1) INTO v_ExisteRegistro
			FROM  "BSCL.Mesa.BD::MES.Operacion" AS OP 
			INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS IND ON IND."IdPortafolio" = OP."IdPortafolio" AND IND."CodigoTipoIndicador" IN ('GEN_CODDIR_REL')
			INNER JOIN "BSCL.SocioNegocio.BD::REL.RelacionPersona" AS REL ON REL."IdRelacionPersona" = IND."ValorIndicador"
			LEFT  JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS DIR ON DIR."IdPersona" = REL."IdPersonaDestino"
			WHERE OP."IdOperacion" = :p_IdOperacion;
			
			IF :v_ExisteRegistro > 0 THEN
				SELECT REL."Calle"||' '||REL."Numero"||'-'||REL."Ciudad",REL."Ciudad"  INTO v_DireccionCliente, v_ComunaCliente
				FROM  "BSCL.Mesa.BD::MES.Operacion" AS OP 
				INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS IND ON IND."IdPortafolio" = OP."IdPortafolio" AND IND."CodigoTipoIndicador" IN ('GEN_CODDIR_REL')
				INNER JOIN "BSCL.SocioNegocio.BD::REL.RelacionPersona" AS REL ON REL."IdRelacionPersona" = IND."ValorIndicador"
				LEFT  JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS DIR ON DIR."IdPersona" = REL."IdPersonaDestino"
				WHERE OP."IdOperacion" = :p_IdOperacion;
			ELSE
				v_DireccionCliente = '';
				v_ComunaCliente	   = '';
			END IF;	
		
		END IF;
	
		if(v_DireccionPrimariaCOR='S') THEN 
			SELECT count(1) INTO v_ExisteRegistro
			FROM  "BSCL.Mesa.BD::MES.Operacion" AS OP 
			INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS IND ON IND."IdPortafolio" = OP."IdPortafolioCorredor" AND IND."CodigoTipoIndicador" IN ('GEN_CODDIR')
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS DIR ON DIR."IdDireccionFisica" = IND."ValorIndicador"
			WHERE OP."IdOperacion" = :p_IdOperacion;
			
			IF :v_ExisteRegistro > 0 THEN
				SELECT DIR."NombreLargo" INTO v_DireccionCorredora
				FROM  "BSCL.Mesa.BD::MES.Operacion" AS OP 
				INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS IND ON IND."IdPortafolio" = OP."IdPortafolioCorredor" AND IND."CodigoTipoIndicador" IN ('GEN_CODDIR')
				INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS DIR ON DIR."IdDireccionFisica" = IND."ValorIndicador"
				WHERE OP."IdOperacion" = :p_IdOperacion;
			ELSE
				v_DireccionCorredora = '';
			END IF;					
		
		ELSE 
			SELECT count(1) INTO v_ExisteRegistro
			FROM  "BSCL.Mesa.BD::MES.Operacion" AS OP 
			INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS IND ON IND."IdPortafolio" = OP."IdPortafolioCorredor" AND IND."CodigoTipoIndicador" IN ('GEN_CODDIR_REL')
			INNER JOIN "BSCL.SocioNegocio.BD::REL.RelacionPersona" AS REL ON REL."IdRelacionPersona" = IND."ValorIndicador"
			WHERE OP."IdOperacion" = :p_IdOperacion;
			
			IF :v_ExisteRegistro > 0 THEN			
				SELECT COALESCE (REL."Calle"||' '||REL."Numero"||'-'||REL."Ciudad",'-') INTO v_DireccionCorredora
				FROM  "BSCL.Mesa.BD::MES.Operacion" AS OP 
				INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS IND ON IND."IdPortafolio" = OP."IdPortafolioCorredor" AND IND."CodigoTipoIndicador" IN ('GEN_CODDIR_REL')
				INNER JOIN "BSCL.SocioNegocio.BD::REL.RelacionPersona" AS REL ON REL."IdRelacionPersona" = IND."ValorIndicador"
				WHERE OP."IdOperacion" = :p_IdOperacion;
			ELSE
				v_DireccionCorredora = '';
			END IF;					
				
		
		END IF;
	
			
			
		v_Corredor =
			SELECT
				OPE."IdClienteCorredor" AS "IdCliente",
				CASE WHEN CORR."Tipo"='J' THEN COALESCE(CORR."RazonSocial",'-') ELSE COALESCE(CORR."NombreCompleto",'-') END AS "Corredora",
				COALESCE(CORR."DocumentoIdentidad",'-') AS "DocumentoIdentidadCorredora",
				COALESCE (DCORR."Calle"||' '||DCORR."Numero"||' - '||DCORR."Departamento",'-') AS "DireccionCorredora",
				COALESCE(TELCORR."Numero",'-')			AS "FonoCorredora",	
				COALESCE(DCORR."NombreDivisionPais",'-')   AS "ComunaCorredora",
 				COALESCE(DCORR."NombreCiudad",'-')   AS "CiudadCorredora",
				CORR."IdPersona" AS "IdPersonaCorredor"
			FROM "BSCL.Mesa.BD.Vistas::ConsultarOperaciones" OPE
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CORR
				ON OPE."IdClienteCorredor" = CORR."IdCliente"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" DCORR
				ON CORR."IdDireccionFisicaPrincipal" = DCORR."IdDireccionFisica"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" TELCORR
				ON CORR."IdTelefonoPrincipal" = TELCORR."IdTelefono"
			WHERE OPE."IdOperacion" = :p_IdOperacion;			
		
		v_Operacion = 
			SELECT
				---------- DATOS OPERACION ----------
				OPE."IdOperacion",
				TO_NVARCHAR(OPE."FechaOperacion",'DD-MM-YYYY') AS "FechaOperacion",
				OPE."FolioOperacion",
				OPE."IdTipoProducto",
				OPE."IdTipoOperacion",
				OPE."IdMesaOrigen"	AS "IdMesa",
				OPE."IdCliente",
				OPE."IdPortafolio",
				OPE."IdClienteCorredor",
				OPE."IdPortafolioCorredor",
				OPE."MontoOperacion" AS "MontoOperacionOPE",
				OPE."MontoOperacionInicialMoneda" AS "MontoOperacionInicial",
				OPE."IdOperacionRelacionada",
				OPE."IdOperacionOrigenReasignacion",
				:v_MontoOpIniTxt	AS "MontoOpIniTxt",
				CASE WHEN OPE."IdEstado"!='ANT' THEN  OPE."MontoOperacionFinalMoneda" ELSE OPE."MontoOperacionFinalMonedaAnticipo" END AS  "MontoOperacionFinal",  
				:v_MontoOpFinTxt	AS "MontoOpFinTxt",
				OPE."IdTipoReajuste",
				(  ( MDA."IdMoneda" || ' - ' )  || MDA."Nombre" ) AS "NombreLargoMoneda",
				MDA."Simbolo"		AS "SimboloMoneda",
				CASE WHEN OPE."IdEstado"!='ANT' THEN OPE."Tasa" ELSE OPE."TasaAnticipo" END AS "Tasa" ,
				OPE."PlazoEnDias",
				TO_NVARCHAR(OPE."FechaVencimiento",'DD-MM-YYYY') AS "FechaVencimiento",
				OPE."IdCondicionLiquidacion",
				TO_NVARCHAR(OPE."FechaCondicionLiquidacion",'DD-MM-YYYY') AS "FechaCondicionLiquidacion",
				OPE."IdAgente",
				OPE."IdEstado",
				OPE."FechaCreacion",
				OPE."IdUsuarioCreacion",
				OPE."Glosa",
				'S'					AS "EsCustodia", -- OPE."EsCustodia"
				"AfectaCustodia",
				TO_NVARCHAR(:v_FecOpeMasPlazo,'DD-MM-YYYY')	AS "FechaPromesa",
				TO_NVARCHAR(:v_FecOpeMasPlazo,'DD') AS "DiaPromesa",
				TO_NVARCHAR(:v_FecOpeMasPlazo, 'MM') AS "MesPromesa",
				TO_NVARCHAR(:v_FecOpeMasPlazo,'YYYY') AS "AnoPromesa",
				TO_CHAR(:v_FecOpeMasPlazo,'DD')||' de '||TO_CHAR(:v_FecOpeMasPlazo,'MONTH')||' de '||TO_CHAR(:v_FecOpeMasPlazo,'YYYY') AS "FechaPromesaTxt",
				CLI."IdEmpresa",
				
				---------- DATOS GENERALES ----------
				OPE."RazonSocial",
				"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(	SUBSTR_BEFORE(CLI."DocumentoIdentidad",'-'),'i',0)||'-'||SUBSTR_AFTER(CLI."DocumentoIdentidad",'-')	 AS "DocumentoIdentidad",						
				COALESCE(TEL."Numero",'-')				AS "Fono",
			
				COALESCE(CASE "REPR"."Tipo" 
								WHEN 'N' 
									THEN "REPR"."Nombres" ||' '|| "REPR"."ApellidoPaterno" || ' ' ||"REPR"."ApellidoMaterno" 
								ELSE "REPR"."RazonSocial"
							END,'-')		AS "Representante",
				COALESCE(POR."CodigoPortafolio",'-')	AS "CodigoPortafolio",
				CUS."Codigo" AS "CodigoCustodio",
				CORR."Corredora",
				CORR."DocumentoIdentidadCorredora",
				'N'										AS "TipoClienteEsBanco",-- REVISAR si es el TipoCliente del Cliente de la Operacion. VER si aplica este campo
				COALESCE(BOL."Nombre",'-')			AS "NombreBolsa",
				COALESCE(SUC."CodigoSucursal",'-')||' '||SUC."NombreSucursal" AS "Sucursal",
				"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(	SUBSTR_BEFORE(IDENT."DocumentoIdentidad",'-'),'i',0)||'-'||SUBSTR_AFTER(IDENT."DocumentoIdentidad",'-') ||' '|| (							CASE "AGE"."Tipo" 
								WHEN 'N' 
									THEN "AGE"."Nombres" ||' '|| "AGE"."ApellidoPaterno" || ' ' ||"AGE"."ApellidoMaterno" 
								ELSE "AGE"."RazonSocial"
							END) AS "Agente", 
				TO_CHAR(:v_FechaSistema,'DD-MM-YYYY')	AS "FechaSistema",
				TO_CHAR(:v_FechaSistema,'DD')||' de '||TO_CHAR(:v_FechaSistema,'MONTH')||' de '||TO_CHAR(:v_FechaSistema,'YYYY') AS "FechaSistemaTxt",		
				TO_CHAR(ADD_SECONDS(OPE."FechaCreacion",v_SegundosZona),'HH24:MI') AS "HoraSistema",
				:v_NombrePais									AS "NombrePais",
				CORR."DireccionCorredora",
				CORR."ComunaCorredora",
 				CORR."CiudadCorredora",
				CORR."FonoCorredora",	
				CORR."IdPersonaCorredor",
				CLI."IdPersona" as "IdPersonaCliente",
				---------- DATOS CARTERA ----------
				COALESCE(RES."IdOperacion",0)		AS "IdOperacionCartera",
				COALESCE(RES."IdReserva",0)			AS "IdReserva",
				COALESCE(RES."IdInstrumento",0)		AS "IdInstrumento",
				COALESCE(INS."Nemotecnico",'-') 	AS "Instrumento",
 				CASE WHEN INS."IdClasificacionTipoInstrumento" = 'IF' THEN '' ELSE COALESCE(INS."Nemotecnico",'-') END AS "Nemotecnico",				
				COALESCE(RES."IdEmisor",'-')		AS "Emisor",
				COALESCE(RES."IdTipoReajuste",'-')	AS "ReajustePapel",
				COALESCE(IR."ValorClasificacionRiesgo",INS."ValorClasificacionRiesgo",'') 	AS "C/R",				
				COALESCE(TO_NVARCHAR(RES."FechaVencimiento",'DD-MM-YYYY'),TO_NVARCHAR('0001-01-01','DD-MM-YYYY'))	AS "FechaVcto",
				COALESCE(RES."CorteMinimo",0)					AS "Cortes",
				CASE WHEN RES."IdClasificacionTipoInstrumento" = 'RF' THEN COALESCE(RES."IngCantidad",0) ELSE COALESCE(RES."IngValorRescate",0) END				AS "ValorNominal",
				CASE WHEN RES."IdClasificacionTipoInstrumento" = 'RF' THEN 0 ELSE RES."IngCantidad" END AS "Cantidad",
				CASE WHEN COALESCE(RES."Tasa",0) = 0 THEN COALESCE(RES."Precio",0) ELSE COALESCE(RES."Tasa",0) END AS "PrecioTasa",
				COALESCE(RES."MontoValorizado",0)				AS "MontoOperacion",
				CASE WHEN OPE."IdTipoOperacion" IN ('COMTER','VENTER') THEN COALESCE(RES."MontoLiquidacionOperacion",0)
				ELSE CASE WHEN RES."IdMonedaLiquidacionOperacion" IN ('UF', 'USD') THEN COALESCE(RES."MontoLiquidacionOperacion",0) ELSE 0 END END AS "MontoOperacionMoneda",
				
				--CASE
				-- SI ES IF VALA FECHA DE LA CREACION DE LA CARTERA ORIGINAL 
				
				CASE WHEN RES."IdClasificacionTipoInstrumento"= 'IF' THEN ''					
				ELSE 
					COALESCE(TO_NVARCHAR(INS."FechaEmision",'DD-MM-YYYY') ,'-')  					
 				END  AS "FechaEmisionInstrumento",		

				COALESCE(TO_NVARCHAR(OPE."FolioOperacion"), '0') AS "OperacionRelacionada",
				CDL."Nombre" AS "CondicionLiquidacion",
				OPE."IdMonedaLiquidacion" AS "IdMonedaLiquidacion",
				:v_DecimalesMoneda AS "DecimalesMoneda",
				IFNULL(RES."IdMonedaEmisionInstrumento",'') AS "IdMonedaPapel",
				LOGO."LogoCorredora",
				"BSCL.Plataforma.BD.Utilitarios::BuscarNemoSVSFunction"(
					COALESCE(RES."IdEmisor",'-'),
					'',
					0,
					COALESCE(INS."Nemotecnico",'-'),
					COALESCE(RES."IdTipoReajuste",'-'),
					'',
					TO_DATE(COALESCE(TO_NVARCHAR(RES."FechaVencimiento",'DD-MM-YYYY'),TO_NVARCHAR('0001-01-01','DD-MM-YYYY')), 'DD-MM-YYYY'))  AS "NemoSVS",
				MOF."PrecioDecimales",
				MOF."TasaDecimales",
				COALESCE(MORES."PrecioDecimales",0) as "DecimalesNominal"
			FROM "BSCL.Mesa.BD.Vistas::ConsultarOperaciones" OPE
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
				ON (OPE."IdCliente" = CLI."IdCliente")
			INNER JOIN "BSCL.SocioNegocio.BD::PER.Telefono" TEL
				ON CLI."IdTelefonoPrincipal" = TEL."IdTelefono"
			LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" REPR
				ON CLI."IdPersonaRepresentante" = REPR."IdPersona"
			INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR
				ON (OPE."IdPortafolio" = POR."IdPortafolio" AND CLI."IdCliente" = POR."IdCliente")
			LEFT JOIN (SELECT "IdPortafolio", MAX("Codigo") AS "Codigo"
				  FROM "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio"
				  WHERE "IdBoveda" = 'DCV' AND "IdEstado" = 'ACT' AND "IdEstadoAutorizacion" = 'AUT'
				  GROUP BY "IdPortafolio") CUS 
				ON OPE."IdPortafolio" = CUS."IdPortafolio"
			INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AGE
				ON CLI."IdPersonaEjecutivo" = AGE."IdPersona"
			INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT
				ON AGE."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
			LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" SUC
				ON CLI."IdSucursalEjecutivo" = SUC."IdSucursal"
			LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" MDA
				ON OPE."IdTipoReajuste" = MDA."IdMoneda"
			LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MOF
				ON MOF."IdMoneda" = OPE."IdTipoReajuste"
				AND MOF."IdUsoMonedaFormato" = 'GEN'
				AND MOF."IdTipoInstrumento" = COALESCE(OPE."IdTipoInstrumento",'')
				AND MOF."IdTipoOperacion" = ''
			LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarOperacionesReserva" RES
				ON (OPE."IdOperacion" = RES."IdOperacion")
			LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MORES
				 ON MORES."IdMoneda" = RES."IdTipoReajuste"
				 AND MORES."IdUsoMonedaFormato" = 'MES'
				 AND MORES."IdTipoInstrumento" = COALESCE(RES."IdTipoInstrumento",'')
				 AND MORES."IdTipoOperacion" = ''
			LEFT JOIN "BSCL.Plataforma.BD::INS.Bolsa" AS BOL
				ON (RES."IdBolsa" = BOL."IdBolsa")
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS
				ON (RES."IdInstrumento" = INS."IdInstrumento")
			LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" IR
				ON IR."IdInstrumento" = RES."IdInstrumento" AND IR."IdEntidadClasificadoraRiesgo" ='BCS' 
			LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" CDL 
			   ON CDL."IdCondicionDeLiquidacion" = OPE."IdCondicionLiquidacion"
			LEFT JOIN :v_Corredor AS CORR
				ON OPE."IdClienteCorredor" = CORR."IdCliente"
			LEFT JOIN :v_logo LOGO
				ON (LOGO."LogoCorredora" IS NOT NULL)
			
			WHERE OPE."IdOperacion" = :p_IdOperacion;
							
		--------------------
		--Lógica de Negocio 
		--------------------
		
		SELECT	COUNT(1)
		INTO	v_Registros
		FROM	:v_Operacion OP;
		
		IF v_Registros >= 1 THEN
		
		    SELECT
		    	 SUM("MontoOperacionMoneda") ,"MontoOperacionFinal" INTO v_SumaMontoOP ,v_MontoOperacionFinal
		    FROM :v_Operacion
		    GROUP BY "IdCliente","MontoOperacionFinal";
		    
		    CALL "BSCL.Plataforma.BD.Servicios::ConvertirMontoEscrito"(:v_SumaMontoOP, :v_Pais, :v_Moneda,:v_SumaMontoOPTxt);
		    CALL "BSCL.Plataforma.BD.Servicios::ConvertirMontoEscrito"(:v_MontoOperacionFinal, :v_Pais, :v_Moneda,:v_MontoOperacionFinalTxt);
				   
		
		   SELECT OP.*
				,COALESCE(APOCORR1."NombreCompleto",'-')		AS "ApoderadoCorredora1"
				,COALESCE(APOCORR1."DocumentoIdentidad",'-')	AS "DocIdenApoderadoCorredora1"
				,COALESCE(APOCORR2."NombreCompleto",'-')		AS "ApoderadoCorredora2"
				,COALESCE(APOCORR2."DocumentoIdentidad",'-')	AS "DocIdenApoderadoCorredora2"
				
				,COALESCE(APOCLI1."NombreCompleto",'-')		AS "ApoderadoCliente1"
				,COALESCE(APOCLI1."DocumentoIdentidad",'-')	AS "DocIdenApoderadoCliente1"
				,COALESCE(APOCLI2."NombreCompleto",'-')		AS "ApoderadoCliente2"
				,COALESCE(APOCLI2."DocumentoIdentidad",'-')	AS "DocIdenApoderadoCliente2"
				,:v_DireccionCorredora AS "DireccionCorredoraIndicador"
				,:v_DireccionCliente   AS "DireccionClienteIndicador"
				,:v_ComunaCliente	   AS "ComunaClienteIndicador"
				,CURRENT_TIME AS "Hora_Server"
				,:v_SumaMontoOP AS "SumaMontoOp"
				,:v_SumaMontoOPTxt AS "SumaMontoOPTxt"
				,:v_Moneda
				,:v_MontoOperacionFinalTxt AS "MontoOperacionFinalMonedaTxt"
				
			FROM :v_Operacion OP
				LEFT JOIN :v_ApoderadosCorredor1 APOCORR1 ON (OP."IdPortafolioCorredor" = APOCORR1."IdPortafolioCorredor" )
				LEFT JOIN :v_ApoderadosCorredor2 APOCORR2 ON (OP."IdPortafolioCorredor" = APOCORR2."IdPortafolioCorredor")
				LEFT JOIN :v_ApoderadoCliente1 APOCLI1 ON (OP."IdPortafolio" = APOCLI1."IdPortafolio")
				LEFT JOIN :v_ApoderadoCliente2 APOCLI2 ON (OP."IdPortafolio" = APOCLI2."IdPortafolio");
			
		ELSE
		
			SELECT
				OPE."IdOperacion",
				TO_NVARCHAR(OPE."FechaOperacion",'DD-MM-YYYY') AS "FechaOperacion",
				OPE."FolioOperacion",
				OPE."IdTipoProducto",
				OPE."IdTipoOperacion",
				OPE."IdMesaOrigen"	AS "IdMesa",
				OPE."IdCliente",
				OPE."IdPortafolio",
				OPE."IdClienteCorredor",
				OPE."IdPortafolioCorredor",
				OPE."MontoOperacion" AS "MontoOperacionOPE",
				OPE."MontoOperacionInicial",
				OPE."IdOperacionRelacionada",
				OPE."IdOperacionOrigenReasignacion",
				:v_MontoOpIniTxt	AS "MontoOpIniTxt",
				OPE."MontoOperacionFinal",
				:v_MontoOpFinTxt	AS "MontoOpFinTxt",
				OPE."IdTipoReajuste",
				'N/A'	AS "NombreLargoMoneda",
				'N/A'	AS "SimboloMoneda",
				OPE."Tasa",
				OPE."PlazoEnDias",
				TO_NVARCHAR(OPE."FechaVencimiento",'DD-MM-YYYY') AS "FechaVencimiento",
				--TO_NVARCHAR(OPE."FechaLiquidacion",'DD-MM-YYYY') AS "FechaLiquidacion",
				OPE."IdCondicionLiquidacion",
				TO_NVARCHAR(OPE."FechaCondicionLiquidacion",'DD-MM-YYYY') AS "FechaCondicionLiquidacion",
				OPE."IdAgente",
				OPE."IdEstado",
				OPE."FechaCreacion", 
				OPE."IdUsuarioCreacion",
				OPE."Glosa",
				'S'					AS "EsCustodia", -- OPE."EsCustodia"
				"AfectaCustodia",
				TO_NVARCHAR(:v_FecOpeMasPlazo,'DD-MM-YYYY')	AS "FechaPromesa",
				TO_NVARCHAR(:v_FecOpeMasPlazo,'DD') AS "DiaPromesa",
				TO_NVARCHAR(:v_FecOpeMasPlazo, 'MM') AS "MesPromesa",
				TO_NVARCHAR(:v_FecOpeMasPlazo,'YYYY') AS "AnoPromesa",
				
				TO_CHAR(:v_FecOpeMasPlazo,'DD')||' de '||TO_CHAR(:v_FecOpeMasPlazo,'MONTH')||' de '||TO_CHAR(:v_FecOpeMasPlazo,'YYYY') AS "FechaPromesaTxt",
				'-' AS "IdEmpresa",
				
				---------- DATOS GENERALES ----------
				OPE."RazonSocial",
				'-'	AS "DocumentoIdentidad",
				'-' AS "Direccion",
				'-' AS "Fono",
				'-' AS "Representante",
				'-' AS "CodigoPortafolio",
				'-' AS "CodigoCustodio",
				'-' AS "Corredora",
				'-' AS "DocumentoIdentidadCorredora",
				'N'	AS "TipoClienteEsBanco",-- REVISAR si es el TipoCliente del Cliente de la Operacion. VER si aplica este campo
				'-' AS "NombreBolsa",
				'-' AS "Sucursal",
				'-' AS "Agente",
				TO_CHAR(:v_FechaSistema,'DD-MM-YYYY')	AS "FechaSistema",
				TO_CHAR(:v_FechaSistema,'DD')||' de '||TO_CHAR(:v_FechaSistema,'MONTH')||' de '||TO_CHAR(:v_FechaSistema,'YYYY') AS "FechaSistemaTxt",
				TO_CHAR(:v_HoraSistema,'HH24:MI:SS')	AS "HoraSistema",				
				:v_NombrePais									AS "NombrePais",
				'-' AS "DireccionCorredora",
				'-' AS "Comuna",												
				'-' AS "FonoCorredora",				
				'-' AS "ComunaCorredora",
 				'-' AS "CiudadCorredora",
				'-' AS "IdPersonaCorredor",
				'-' AS "IdPersonaCliente",
				---------- DATOS CARTERA ----------
				'-' AS "IdMonedaLiquidacion",
 				'-' AS "IdMonedaPapel",
				0 AS "IdOperacionCartera",
				0 AS "IdReserva",
				0 AS "IdInstrumento",
				'-' AS "Instrumento",
				'-' AS "Emisor",
				'-' AS "ReajustePapel",
				'-' AS "C/R",
				'01-01-0001' AS "FechaVcto",
				0 AS "Cortes",
				0 AS "ValorNominal",
				0 AS "PrecioTasa",
				0 AS "MontoOperacion",
				0 AS "MontoOperacionMoneda",
				'01-01-0001' AS "FechaEmisionInstrumento",
				'01-01-0001' AS "OperacionRelacionada",
				CDL."Nombre" AS "CondicionLiquidacion"
				-------------------------------------------------------------------------------------------
				,'-' AS "ApoderadoCorredora1"
				,'-' AS "DocIdenApoderadoCorredora1"
				,'-' AS "ApoderadoCorredora2"
				,'-' AS "DocIdenApoderadoCorredora2"
				,'-' AS "ApoderadoCliente1"
				,'-' AS "DocIdenApoderadoCliente1"
				,'-' AS "ApoderadoCliente2"
				,'-' AS "DocIdenApoderadoCliente2"				
				,CURRENT_TIME AS "Hora_Server"
				,0   AS "DecimalesMoneda"
				,''  AS "SumaMontoOPTxt"
				,0  AS "SumaMontoOp"
				,'' AS "MontoOperacionFinalMonedaTxt"
				,LOGO."LogoCorredora"
				,'-' AS "DireccionCorredoraIndicador"
				,'-' AS "DireccionClienteIndicador"
				,'-' AS "ComunaClienteIndicador"
				,0 AS "PrecioDecimales"
				,0 AS "TasaDecimales"
				,0 AS "DecimalesNominal"
			FROM "BSCL.Mesa.BD.Vistas::ConsultarOperaciones" OPE
			LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" CDL 
			   ON CDL."IdCondicionDeLiquidacion" = OPE."IdCondicionLiquidacion"
			LEFT JOIN :v_logo LOGO
				ON (LOGO."LogoCorredora" IS NOT NULL)
			WHERE OPE."IdOperacion" = p_IdOperacion;
		
		END IF;

		
		--------------------
		--FIN SP 
		--------------------
	END;
END;