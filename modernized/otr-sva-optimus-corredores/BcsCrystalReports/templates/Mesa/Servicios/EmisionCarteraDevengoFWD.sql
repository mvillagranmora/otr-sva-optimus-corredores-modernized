SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::EmisionCarteraDevengoFWD";

--
--  Proposito: servicio para consultar Emision De Devengo FWD 
--  Autor: EORTIZ - PRONET-IT
--  Fecha Creacion: /10/2016
--

CREATE PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::EmisionCarteraDevengoFWD"
(
-- Parametros entrada 
IN p_IdEmpresa				BIGINT,
IN p_FechaOperacion			DATE,
IN p_Usuario				VARCHAR(100)

)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER  
	DEFAULT SCHEMA "BSCL"
	--READS SQL DATA   
AS
 BEGIN
	BEGIN
		--------------------
		--Variables
		--------------------
		DECLARE varFECHA 		DATE;
		DECLARE varUF 			BIGINT;
		DECLARE varDOLAR 		BIGINT;
		DECLARE varExisteOp		INT;
		DECLARE varExisteDolar 	INT;
		DECLARE varExisteUf		INT;
		DECLARE varMoneda		VARCHAR(3);
		DECLARE varPlazo		INT;
		DECLARE p_out_Factor	DECIMAL(26,8);
		
	--Se obtiene fecha de Operacion					
		SELECT count(1) INTO varExisteDolar
			FROM "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PTC
				WHERE PTC."Origen" 		= 'OBSERVADO' 
					AND PTC."Fecha" 	= :p_FechaOperacion 
					AND PTC."IdMoneda" 	= 'USD';
							

IF (varExisteDolar != 0) THEN

		--Se obtiene el valor del dolar
		SELECT coalesce(PTC."TipoCambioMercado",0) INTO varDOLAR
			FROM "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PTC
				WHERE PTC."Origen" 		= 'OBSERVADO' 
					AND PTC."Fecha" 	= :p_FechaOperacion 
					AND PTC."IdMoneda" 	= 'USD';
 
		v_Cartera = SELECT   MF."FolioOperacion" 			AS "FolioOperacion"
							,MF."NombreInstrumento"			AS "NombreInstrumento"
							,MF."RutCliente"				AS "RutCliente"
							,MF."NombreCliente"				AS "NombreCliente"
							,MF."FechaOperacion"			AS "FechaInicio"
							,MF."FechaVencimiento"			AS "FechaVencimiento"
							,MF."Plazo"						AS "PlazoOperacion"
							,MF."IdMonedaPrincipal"			AS "IdMonedaPrincipal"
							,MF."IdMonedaSecundaria"		AS "IdMonedaSecundaria"
							,MF."MontoMonedaPrincipal"		AS "MontoNocional"
							,MF."TipoCambioCierre"			AS "TipoCambioFWD"
							,MF."PlazoResidual"				AS "PlazoRem"
							,MF."MontoValorRazonableActivo" AS "MontoValorRazonableActivo"
							,MF."MontoValorRazonablePasivo" AS "MontoValorRazonablePasivo"
							,MF."TasaValorRazonableActivo"	AS "TasaValorRazonableActivo"
							,MF."TasaValorRazonablePasivo"  AS "TasaValorRazonablePasivo"
							,CASE
							WHEN MF."IdTipoOperacion" = 'COMFWD' THEN 'COMPRA' 
							WHEN MF."IdTipoOperacion" = 'VENFWD' THEN 'VENTA' END AS "TipoOperacion"		
							,:p_FechaOperacion				AS "FechaConsulta"
							,:varDOLAR						AS "DolarObservado"
							,:p_Usuario 					AS "Usuario"
							,CC."INGMontoDiferido"			AS "MontoDiferido"
							,CC."INGMontoDiferidoDiario"	AS "MontoDiferidoDiario"
							,CC."INGMontoDiferidoAcumulado"	AS "MontoDiferidoAcumulado"	
							,CC."INGMontoDevengoTipoCambio" AS "MontoDevengo"
					FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWD" MF
					 INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CC
					 	ON CC."IdOperacion" = MF."FolioOperacion"
					 WHERE MF."Estado"='VIG'
					  AND MF."FechaOperacion" <= '2016-12-05'--:p_FechaOperacion
					GROUP BY MF."FolioOperacion",MF."NombreInstrumento",MF."RutCliente",MF."NombreCliente",MF."FechaOperacion"
							,MF."FechaVencimiento",MF."Plazo",MF."IdMonedaPrincipal",MF."IdMonedaSecundaria",MF."MontoMonedaPrincipal"
							,MF."TipoCambioCierre",MF."PlazoResidual",MF."MontoValorRazonableActivo",MF."MontoValorRazonablePasivo"
							,MF."TasaValorRazonableActivo",MF."TasaValorRazonablePasivo",MF."IdTipoOperacion",CC."INGMontoDiferido"
							,CC."INGMontoDiferidoDiario",CC."INGMontoDiferidoAcumulado",CC."INGMontoDevengoTipoCambio";
	ELSE
			v_Cartera = SELECT  0 		AS "FolioOperacion"
								,'' 	AS "NombreInstrumento"
								,''		AS "RutCliente"
								,''  	AS "NombreCliente"
								,''		AS "FechaInicio"		
								,''		AS "FechaVencimiento"
								,0		AS "PlazoOperacion"
								,''		AS "IdMonedaPrincipal"
								,''		AS "IdMonedaSecundaria"
								,0 		AS "MontoNocional"
								,0		AS "TipoCambioFWD"
								,0		AS "PlazoRem"
								,0 		AS "MontoValorRazonableActivo"
								,0 		AS "MontoValorRazonablePasivo"
								,0		AS "TasaValorRazonableActivo"
								,0  	AS "TasaValorRazonablePasivo"
								,''		AS "TipoOperacion"
								,''		AS "FechaConsulta"
								,0		AS "DolarObservado"
								,'No Existe' AS "Usuario" 
								,0		AS "MontoDiferido"
								,0		AS "MontoDiferidoDiario"
								,0		AS "MontoDiferidoAcumulado"	
								,0 		AS "MontoDevengo"
					FROM	DUMMY;
	END IF;
		BEGIN
			SELECT * FROM :v_Cartera; 
		END;
	 END;		
 END;