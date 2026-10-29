SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.PAC::ConsultarPac";

CREATE PROCEDURE "BSCL.Tesoreria.BD.Servicios.PAC::ConsultarPac"(
	IN p_IdEmpresa				BIGINT,
	IN p_IdFormatoArchivo		BIGINT,
	IN p_DiaDescuento      		BIGINT,
	IN p_FechaCargo				NVARCHAR(10),
	-- Datos para archivo
	OUT p_out_Transferencias TABLE(	 "TipoRegistroOptimus"		INTEGER -- Este campo debe ir en todas las tablas de salida y su valor concordar con el tipo de registro
									,"pac-codigo-banco" 		NVARCHAR(3)
									,"pac-codigo-empresa"  		NVARCHAR(3)
									,"pac-codigo-convenio"  	NVARCHAR(3)
									,"pac-tipo-registro" 		NVARCHAR(1)
									,"pac-ident-servicio" 		NVARCHAR(22)
									,"pac-filler"				NVARCHAR(1)
									,"pac-inf-adicional"  		NVARCHAR(10)
									,"pac-monto-cargo"  		NVARCHAR(11)
									,"pac-fecha-facturacion"	NVARCHAR(8)
									,"pac-fecha-vencimiento"	NVARCHAR(8)
									,"pac-filler2"				NVARCHAR(10))
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	DECLARE v_fechaCargo NVARCHAR(10);
	DECLARE v_bancoConcentrador NVARCHAR(520) := '';
	DECLARE v_codigoBancoTotales NVARCHAR(3) := '';
	DECLARE v_codigoCorredora NVARCHAR(3) := '';
	DECLARE v_montoCargos DECIMAL(26,8) := 0;
	DECLARE v_numeroRegistros BIGINT := 0;
	
	DECLARE v_detalle TABLE( "TipoRegistroOptimus"		INTEGER
							,"pac-codigo-banco" 		NVARCHAR(3)
							,"pac-codigo-empresa"  		NVARCHAR(3)
							,"pac-codigo-convenio"  	NVARCHAR(3)
							,"pac-tipo-registro" 		NVARCHAR(1)
							,"pac-ident-servicio" 		NVARCHAR(22)
							,"pac-filler"				NVARCHAR(1)
							,"pac-inf-adicional"  		NVARCHAR(10)
							,"pac-monto-cargo"  		DECIMAL(26,8)
							,"pac-fecha-facturacion"	NVARCHAR(8)
							,"pac-fecha-vencimiento"	NVARCHAR(8)
							,"pac-filler2"				NVARCHAR(10));
	
	v_fechaCargo = TO_NVARCHAR(TO_DATE(:p_FechaCargo),'YYYYMMDD');
	
	SELECT TOP 1 BCOCON."Valor" INTO v_bancoConcentrador
	FROM "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria" BCOCON
	WHERE BCOCON."IdEmpresa" = :p_IdEmpresa
	  AND BCOCON."CodigoParametro" = 'BANCOCONCENTRADORCARTANOTIFICACIONPAC';
	
	SELECT TOP 1
		CASE WHEN LENGTH(BCO."CodigoBanco") > 3 THEN LPAD(SUBSTRING(BCO."CodigoBanco",LENGTH(BCO."CodigoBanco")-2),3,'0')
		ELSE LPAD(BCO."CodigoBanco",3,'0') END INTO v_codigoBancoTotales
	FROM "BSCL.Plataforma.BD::BAN.Banco" BCO
	WHERE BCO."IdEmpresa" = :p_IdEmpresa
	  AND BCO."NombreBanco" LIKE ('%'||:v_bancoConcentrador||'%');
	
	SELECT TOP 1 LPAD("CodigoCorredora",3,'0') INTO v_codigoCorredora
	FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa"
	WHERE "IdEmpresa" = :p_IdEmpresa;
	
	-- Salida para archivo
	v_detalle = select "TipoRegistroOptimus", 
	                   "pac-codigo-banco", 
	                   "pac-codigo-empresa",
	                   "pac-codigo-convenio",
	                   "pac-tipo-registro",
	                   "pac-ident-servicio",
	                   "pac-filler",
	                   "pac-inf-adicional",
	                   "pac-monto-cargo",
	                   "pac-fecha-facturacion",
	                   "pac-fecha-vencimiento",
	                   "pac-filler2" from (
							SELECT DISTINCT
								 4 AS "TipoRegistroOptimus"
								,CASE 
									  		WHEN PAC."IdentServicio" IS NOT NULL 
									  			THEN LPAD(SUBSTRING(PAC."IdentServicio",LENGTH(PAC."IdentServicio")-3),3,'0') 
									  		ELSE '000' 
								 END AS "pac-codigo-banco"
								,'006'           				AS "pac-codigo-empresa"
								,'023'							AS "pac-codigo-convenio"
								,'D'							AS "pac-tipo-registro"
								,PAC."IdentServicio"			AS "pac-ident-servicio"
								,''								AS "pac-filler"
								,''								AS "pac-inf-adicional"
								,COALESCE(MONTOPACTOTAL."TotalMontoPesos",0) + COALESCE(MONTOPACCSCTOTAL."TotalMontoPesos",0) AS "pac-monto-cargo"
								,:v_fechaCargo					AS "pac-fecha-facturacion"
								,:v_fechaCargo					AS "pac-fecha-vencimiento"
								,''								AS "pac-filler2"
								,PAC."Estado"                   AS "Estado"
								,PAC."EstadoCargo"              AS "EstadoCargo"
							FROM "BSCL.Tesoreria.BD::PAC.MandatoPAC" AS PAC
							LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CC
								ON (CC."IdCliente" = PAC."IdCliente") 
							LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SN
								ON (SN."IdCliente" = PAC."IdCliente")
							LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCuentasBancarias" AS CBP 
								ON (CBP."NumeroCuentaBancaria" = PAC."CuentaCorriente" AND CBP."IdPersona" = SN."IdPersona")
							LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS CP
								ON (CP."IdPortafolio" = PAC."IdPortafolio")
							LEFT JOIN "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotas" AS CSC
								ON (PAC."FolioCSC" = CSC."FolioCSC")
							LEFT JOIN "BSCL.APV.BD.Vistas::AFI.ConsultarGestionContratosSAAPV" AS SAP
								ON (TO_NVARCHAR(PAC."FolioSAAPV") = SAP."FolioContratoSAAPV")
							LEFT JOIN (
									SELECT SUM (por."MontoPesos") AS "TotalMontoPesos", SUM (por."MontoUF") AS "TotalMontoUF", PAC."IdentServicio"
									FROM "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" por 
									INNER JOIN "BSCL.APV.BD.Vistas::AFI.ConsultarGestionContratosSAAPV" AS SAP
										ON (por."IdContratoSAAPV" = SAP."IdContratoSAAPV") 
									LEFT OUTER JOIN "BSCL.Tesoreria.BD::PAC.MandatoPAC" AS PAC
										ON (TO_NVARCHAR(PAC."FolioSAAPV") = SAP."FolioContratoSAAPV" AND PAC."EstadoCargo" = 'ACT')
									WHERE por."IdEstado" = 'VIG'
									  AND SAP."IdEstadoNuevo" = 'VIG'
									  AND :p_DiaDescuento = 0 OR SAP."DiaDescuento" = :p_DiaDescuento
									GROUP BY PAC."IdentServicio"
								) AS MONTOPACTOTAL
								ON (MONTOPACTOTAL."IdentServicio" = PAC."IdentServicio")
							LEFT JOIN (
									SELECT SUM (CSC."MontoPesos") AS "TotalMontoPesos" , SUM (CSC."MontoUF") AS "TotalMontoUF" , PAC."IdentServicio"
									FROM "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotas" AS CSC
									LEFT OUTER JOIN "BSCL.Tesoreria.BD::PAC.MandatoPAC" AS PAC
										ON (PAC."FolioCSC" = CSC."FolioCSC" AND PAC."EstadoCargo" = 'ACT')
									WHERE CSC."Estado" = 'ING'
									  AND :p_DiaDescuento = 0 OR CSC."DiaDescuento" = :p_DiaDescuento
									GROUP BY PAC."IdentServicio"
								) AS MONTOPACCSCTOTAL
								ON (MONTOPACCSCTOTAL."IdentServicio" = PAC."IdentServicio")
							WHERE CC."IdEstado" = 'ACT'
							  AND CP."IdEstado" = 'ACT'
							  AND ((PAC."FolioCSC" IS NOT NULL AND PAC."FolioCSC" <> '0' AND CSC."Estado" = 'ING') OR (PAC."FolioSAAPV" IS NOT NULL AND PAC."FolioSAAPV" <> '0' AND SAP."IdEstadoNuevo" = 'VIG'))
							  AND PAC."IdEmpresa" = :p_IdEmpresa
						) where "Estado" = 'ING' and "EstadoCargo" = 'ACT' and "pac-monto-cargo" > 0;
	
	SELECT SUM("pac-monto-cargo"), COUNT(1) INTO v_montoCargos, v_numeroRegistros
	FROM :v_detalle;
	
	p_out_Transferencias =
		SELECT
			 "TipoRegistroOptimus"
			,"pac-codigo-banco"
			,"pac-codigo-empresa"
			,"pac-codigo-convenio"
			,"pac-tipo-registro"
			,"pac-ident-servicio"
			,"pac-filler"
			,"pac-inf-adicional"
			,CAST ("pac-monto-cargo" AS DECIMAL(26,1)) AS "pac-monto-cargo"
			,"pac-fecha-facturacion"
			,"pac-fecha-vencimiento"
			,"pac-filler2"
		FROM :v_detalle
		UNION ALL
		SELECT TOP 1
			 "TipoRegistroOptimus"
			,'001' AS "pac-codigo-banco"
			,"pac-codigo-empresa"
			,"pac-codigo-convenio"
			,'T' AS "pac-tipo-registro"
			,'' AS "pac-ident-servicio"
			,"pac-filler"
			,'' AS "pac-inf-adicional"
			,CAST (:v_montoCargos AS DECIMAL(26,1)) AS "pac-monto-cargo"
			,LPAD(TO_VARCHAR(:v_numeroRegistros),6,'0') AS "pac-fecha-facturacion"
			,'' AS "pac-fecha-vencimiento"
			,"pac-filler2"
		FROM :v_detalle;
	
END;
