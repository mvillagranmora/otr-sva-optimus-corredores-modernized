		
SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Mesa.CrystalReports.Servicios::OperacionesFicticias";

CREATE PROCEDURE 
-- 
--  Proposito: 
--  Fecha Creacion: 01/07/2016
--  
"BSCL.Mesa.CrystalReports.Servicios::OperacionesFicticias" ( 
	--Entrada
	IN p_IdEmpresa			BIGINT,
	IN p_FechaReporte 		DATE,
	IN p_Nemotecnico 		VARCHAR(20),
	IN p_IdUsuario			BIGINT	   
)
    
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"

AS
BEGIN

	DECLARE v_codigoUsuario  VARCHAR(100);
	DECLARE v_nombreCompleto VARCHAR(100);
	DECLARE v_countOperaciones 	BIGINT;
	DECLARE v_LogoCorredora BLOB;
	DECLARE v_FechaSistema    NVARCHAR(10);
	DECLARE v_SegundosZona INTEGER;

	SELECT TO_INTEGER("Valor") INTO v_SegundosZona
	FROM "BSCL.Plataforma.BD::PLA.Configuracion"
	WHERE "IdEmpresa" =  p_IdEmpresa and  "CodigoParametro" = 'PLA.SegundosZonaHoraria';
	
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
	
	SELECT 
		"CodigoUsuario", "NombreCompleto" INTO v_codigoUsuario, v_nombreCompleto
	FROM "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto"
	WHERE "IdUsuario" = :p_IdUsuario;
	
	CALL "BSCL.Mesa.BD.Servicios::SPT.ConsultarPreciosPromedios"(:p_FechaReporte, :v_preciosPromedios);


	SELECT TOP 1 "Binario" INTO v_LogoCorredora FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
	WHERE "IdEmpresa" = p_IdEmpresa AND "NombreImagen" = 'LOGO_CORREDOR' order by "Version" desc;
				
	v_OperacionesFicticias = SELECT 
									MSP."IdOperacion" AS "FolioOperacion"
									,CC."NombreCompleto" AS "NombreCliente"
									,MSP."IdMonedaPrincipal" AS "MonedaPrincipal"
									,MSP."IdMonedaSecundaria" AS "MonedaSecundaria"
									,MSP."MontoMonedaPrincipal" AS "Monto"
									,CASE 
										WHEN MSP."NombreInstrumento" NOT IN ('DISTRI','INTERB')
											AND IFNULL(MSP."TipoCambioCierre",0) = 0
										THEN MSP."ParidadCierre" * MSP."TipoCambioCosto" * MSP."MontoMonedaPrincipal"

										ELSE MSP."MontoEquivalente"
									END AS "MontoEquivalente"
									,MSP."TipoCambioCierre"
									,MSP."IdTipoCanalIngreso" AS "Origen"
									,TO_VARCHAR(:p_FechaReporte, 'DD-MM-YYYY') AS "Fecha"
									,TO_VARCHAR(:v_FechaSistema, 'DD-MM-YYYY') AS "FechaSistema"
									,TO_VARCHAR(
										ADD_SECONDS(
											CURRENT_UTCTIMESTAMP,
											v_SegundosZona
										),
										'HH24:MI'
									) AS "HoraSistema"
									,FPI."NombreMedioDePago" AS "FPRecibimos"
									,FPE."NombreMedioDePago" AS "FPEntregamos"
									,TO_VARCHAR(UTCTOLOCAL (TO_TIMESTAMP(MSP."FechaOperacion", 'YYYY-MM-DD HH24:MI:SS'), 'America/Santiago','platform'),'HH24:MI:SS') AS "HoraIngreso"
									,MSP."Estado"
									,MSP."TipoOperacion"
									,MSP."NombreInstrumento"
									,v_codigoUsuario AS "Usuario"
									,v_nombreCompleto AS "NombreCompletoUsuario"
									,PP."PrecioPromedio"
									,'' AS "MensajeError"
									,v_LogoCorredora AS "LogoCorredora"
								FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot" MSP
								LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CC
								ON MSP."IdCliente" = CC."IdCliente"
								LEFT JOIN ( SELECT
												"NombreMedioDePago", CFPO1."IdOperacion"
						   					FROM "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion" CFPO1
						   					WHERE CFPO1."RecibePaga" = 'R'
						   					ORDER BY CFPO1."IdComprobantePagoOperacion") FPE
								ON MSP."IdOperacion" = FPE."IdOperacion"
								LEFT JOIN ( SELECT
												"NombreMedioDePago", CFPO2."IdOperacion"
						   					FROM "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion" CFPO2
						   					WHERE CFPO2."RecibePaga" = 'P'
						   					ORDER BY CFPO2."IdComprobantePagoOperacion") FPI
								ON MSP."IdOperacion" = FPI."IdOperacion"
								LEFT JOIN :v_preciosPromedios PP
								ON MSP."IdInstrumento" = PP."IdInstrumento"
								AND MSP."IdTipoOperacion" = PP."IdTipoOperacion"
								AND MSP."IdMonedaPrincipal" = PP."IdMonedaPrincipal"
								AND	MSP."IdMonedaSecundaria" = PP."IdMonedaSecundaria"
								WHERE (MSP."Estado" = 'VIG'
								OR MSP."Estado" = 'PEL'
								OR MSP."Estado" = 'PEN'								
								OR MSP."Estado" = 'PDA')
								AND MSP."OperacionFicticia" = 'S'
								AND MSP."IdEmpresa" = :p_IdEmpresa
								AND MSP."Nemotecnico" = CASE WHEN :p_Nemotecnico = ' TODOS' THEN MSP."Nemotecnico" ELSE :p_Nemotecnico END 
								AND MSP."FechaOperacion" BETWEEN TO_SECONDDATE(:p_FechaReporte) AND ADD_SECONDS(TO_SECONDDATE(:p_FechaReporte), 86399); 
							 
	SELECT COUNT(*) INTO v_countOperaciones  FROM :v_OperacionesFicticias;
	
	IF (v_countOperaciones > 0) THEN
		BEGIN	
			SELECT * FROM :v_OperacionesFicticias;				
		END;	
	ELSE 
		v_OperacionesFicticiasVacio = SELECT 
										0 	AS "FolioOperacion"
										,'' AS "NombreCliente"
										,'' AS "MonedaPrincipal"
										,'' AS "MonedaSecundaria"
										,0 	AS "Monto"
										,0 	AS "MontoEquivalente"
										,0 	AS "TipoCambioCierre"
										,'' AS "Origen"
										,TO_VARCHAR(:p_FechaReporte, 'DD-MM-YYYY') AS "Fecha"
										,TO_VARCHAR(:v_FechaSistema, 'DD-MM-YYYY') AS "FechaSistema"
										,TO_VARCHAR(
											ADD_SECONDS(
												CURRENT_UTCTIMESTAMP,
												v_SegundosZona
											),
											'HH24:MI'
										) AS "HoraSistema"
										,'' AS "FPRecibimos"
										,'' AS "FPEntregamos"
										,'00:00:00' AS "HoraIngreso"
										,'' AS "Estado"
										,'' AS "TipoOperacion"
										,'' AS "NombreInstrumento"
										,v_codigoUsuario	AS "Usuario"
										,'' AS "NombreCompletoUsuario"
										,0 	AS "PrecioPromedio"
										,'No se encontraron datos.' AS "MensajeError"
										,v_LogoCorredora AS "LogoCorredora"
									 FROM DUMMY;
		BEGIN	
			SELECT * FROM :v_OperacionesFicticiasVacio;				
		END;
	END IF;				
					
END;