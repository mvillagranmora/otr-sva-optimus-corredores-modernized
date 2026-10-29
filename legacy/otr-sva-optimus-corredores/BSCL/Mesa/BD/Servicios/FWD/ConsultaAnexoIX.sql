SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Mesa.BD.Servicios::FWD.ConsultaAnexoIX";
-- 
--  Proposito: 
--  Fecha Creacion: 08/12/2015
--  
CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::FWD.ConsultaAnexoIX"(
	IN p_IdEmpresa      BIGINT,		
	IN p_FechaDesde     NVARCHAR(10),
	IN p_FechaHasta     NVARCHAR(10),
	
	/*OUT p_out_encabezadoAnexoIX table (
		 "FlagDeControl"				NVARCHAR(10)
		,"RutDelReportante"				NVARCHAR(9)
		,"DV"            				NVARCHAR(1)
		,"NumeroRegistrosDeContratos"	NVARCHAR(4)
		,"SumaMontoRecibido"			NVARCHAR(22)--DECIMAL(17,4)
		,"SumaMontoEntregado"			NVARCHAR(22)--DECIMAL(17,4)
		,"GlosaArchivo"					NVARCHAR(30)
		,"Fecha"						NVARCHAR(10)
		,"CodigoSuperOrigen"			NVARCHAR(3)),*/
	
	OUT p_out_encabezadoAnexoIX table (
		 "FlagDeControl"				NVARCHAR(10)
		,"NombreCliente"				NVARCHAR(50)
		,"CodigoEntidadMCF"				NVARCHAR(40)
		,"RutDelReportante"				NVARCHAR(9)
		,"DV"            				NVARCHAR(1)
		,"CodigoSectorEconomico"		NVARCHAR(3)---
		,"DomicilioCiudad"				NVARCHAR(50)
		,"NombrePersonaEncargada"		NVARCHAR(50)
		,"CargoPersonaEncargada"		NVARCHAR(50)
		,"TelefonoPersonaEncargada"		NVARCHAR(40)
		,"CorreoElectronicoPersonaEncargada"	NVARCHAR(50)
		,"FechaDeEnvio"					NVARCHAR(10)
		,"PeríodoInformado"				NVARCHAR(20)
		),
			 
   	/*OUT p_out_registrosAnexoIX table (
   		 "FlagDeControl"			NVARCHAR(10)
   		,"RutReportante"			NVARCHAR(9)
   		,"DV"						NVARCHAR(1)
   		,"NombreCliente"			NVARCHAR(50)
   		,"NumeroContrato"			NVARCHAR(12)
   		,"FechaSuscripcion"			NVARCHAR(10)
   		,"FechaVencimiento"			NVARCHAR(10)
   		,"NumeroDeDias"				NVARCHAR(5)
   		,"ModalidadDePago"			NVARCHAR(1)
   		,"CodMonedaRecibida"		NVARCHAR(4)
   		,"MontoRecibido"			NVARCHAR(19)--DECIMAL(14,4)
   		,"CodMonedaEntregada"		NVARCHAR(4)
   		,"MontoEntregado"			NVARCHAR(19)--DECIMAL(14,4)
   		,"PrecioSpot"				NVARCHAR(9)--DECIMAL(5,4)
   		,"ForwardFuturo"			NVARCHAR(9)--DECIMAL(4,4)--DECIMAL(5,4)
   		,"CodigoInstrumento"		NVARCHAR(2)
   		,"CodigoSectorEconomico"	NVARCHAR(3)
   		,"PrimaTotalDolares"		NVARCHAR(15)--DECIMAL(10,4)*/
   	
   	OUT p_out_registrosAnexoIX table (
   		 "FlagDeControl"			NVARCHAR(10)
   		,"RutReportante"			NVARCHAR(9)
   		,"DV"						NVARCHAR(1)
   		,"NombreCliente"			NVARCHAR(50)
   		,"CodigoSectorEconomico"	NVARCHAR(3)---
   		,"CodigoPais"				NVARCHAR(25)
   		,"Pais"						NVARCHAR(25)
   		,"NumeroContrato"			NVARCHAR(12)
   		,"FechaSuscripcion"			NVARCHAR(10)
   		,"FechaVencimiento"			NVARCHAR(10)
   		,"NumeroDeDias"				NVARCHAR(5)
   		,"ModalidadDePago"			NVARCHAR(1)
   		,"CodigoInstrumento"		NVARCHAR(2)
   		,"NombreInstrumento"		NVARCHAR(7)
   		,"CodMonedaRecibida"		NVARCHAR(4)
   		,"MonedaRecibida"			NVARCHAR(30)
   		,"MontoRecibido"			NVARCHAR(19)--DECIMAL(14,4)
   		,"CodMonedaEntregada"		NVARCHAR(4)
   		,"MonedaEntregada"			NVARCHAR(30)
   		,"MontoEntregado"			NVARCHAR(19)--DECIMAL(14,4)
   		,"PrimaTotalDolares"		NVARCHAR(15)--DECIMAL(10,4)
   		,"ForwardFuturo"			NVARCHAR(10)--DECIMAL(4,4)--DECIMAL(5,4)
   		,"PrecioSpot"				NVARCHAR(10)--DECIMAL(5,4)
	)
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN SEQUENTIAL EXECUTION
		
		DECLARE p_operacionesFWD  TABLE (
			 "FlagDeControl"			NVARCHAR(10)
	   		,"RutReportante"			NVARCHAR(9)
	   		,"DV"						NVARCHAR(1)
	   		,"NombreCliente"			NVARCHAR(50)
	   		,"CodigoSectorEconomico"	NVARCHAR(3)
	   		,"CodigoPais"				NVARCHAR(25)
   			,"Pais"						NVARCHAR(25)
	   		,"NumeroContrato"			NVARCHAR(12)
	   		,"FechaSuscripcion"			NVARCHAR(10)
	   		,"FechaVencimiento"			NVARCHAR(10)
	   		,"NumeroDeDias"				NVARCHAR(5)
	   		,"ModalidadDePago"			NVARCHAR(1)
	   		,"CodigoInstrumento"		NVARCHAR(2)
	   		,"NombreInstrumento"		NVARCHAR(7)
	   		,"CodMonedaRecibida"		NVARCHAR(4)
	   		,"MonedaRecibida"			NVARCHAR(30)
	   		,"MontoRecibido"			DECIMAL(14,4)
	   		,"CodMonedaEntregada"		NVARCHAR(4)
	   		,"MonedaEntregada"			NVARCHAR(30)
	   		,"MontoEntregado"			DECIMAL(14,4)
	   		,"PrimaTotalDolares"		NVARCHAR(15)--DECIMAL(10,4)
	   		,"ForwardFuturo"			DECIMAL(14,4)--DECIMAL(4,4)
	   		,"PrecioSpot"				DECIMAL(14,4)
		);
		
		DECLARE TipoComprobanteDefecto	NVARCHAR(3);
		DECLARE v_CountOperaciones		BIGINT;
		DECLARE v_SumaMontoRecibido		DECIMAL(17,4);
		DECLARE v_SumaMontoEntregado	DECIMAL(17,4);
		DECLARE v_FechaSistema			NVARCHAR(10);
		
		DECLARE v_FechaSistemaUTC   	NVARCHAR(40);
		DECLARE v_FechaSistemaLOCAL   	NVARCHAR(40);
		DECLARE v_FechaHoraSistemaUTC   NVARCHAR(40);
		DECLARE v_FechaHoraSistemaLOCAL SECONDDATE;
		DECLARE v_FechaHoraUTC   		NVARCHAR(40);
		DECLARE v_FechaHoraLOCAL   		NVARCHAR(40);
		
		DECLARE SALIDA_CONTROLADA CONDITION;    	
		DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
		BEGIN
		END;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema"(:p_IdEmpresa,:v_FechaSistema);
		
		-- Se obtiene la misma FechaHoraSistema que se muestra en la UI
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechas"(
			p_IdEmpresa					=> p_IdEmpresa,
			p_out_FechaSistemaUTC		=> v_FechaSistemaUTC,
			p_out_FechaSistemaLOCAL		=> v_FechaSistemaLOCAL,
			p_out_FechaHoraSistemaUTC	=> v_FechaHoraSistemaUTC,
			p_out_FechaHoraSistemaLOCAL	=> v_FechaHoraSistemaLOCAL,
			p_out_FechaHoraUTC			=> v_FechaHoraUTC,
			p_out_FechaHoraLOCAL		=> v_FechaHoraLOCAL
		);
		
		p_operacionesFWD =
			SELECT
				 'IIIIIIIIII'														as "FlagDeControl"
				,LPAD(SUBSTR(FWD."RutCliente",0,LENGTH(FWD."RutCliente")-2),9,0)	as "RutReportante"
				,LPAD(SUBSTR(FWD."RutCliente",LENGTH(FWD."RutCliente"),LENGTH(FWD."RutCliente")),1,0)	as "DV"
				,LPAD(FWD."NombreCliente",50,0)										as "NombreCliente"
				,LPAD('',3,0)														as "CodigoSectorEconomico"
				,LPAD(COALESCE(PAIS."CodigoPais",''),25,0)							as "CodigoPais"
        		,LPAD(COALESCE(PAIS."NombrePais",''),25,0)							as "Pais"
				,LPAD(FWD."IdOperacion",12,0)										as "NumeroContrato"
				,TO_VARCHAR(FWD."FechaOperacion",'DD/MM/YYYY')						as "FechaSuscripcion"
				,TO_VARCHAR(FWD."FechaVencimiento",'DD/MM/YYYY')					as "FechaVencimiento"
				,LPAD(FWD."Plazo",5,0)												as "NumeroDeDias"
				,CASE WHEN FWD."ModalidadLiquidacionValor" = 'ENTREGA_FISICA' THEN 'E'
					  WHEN FWD."ModalidadLiquidacionValor" = 'COMPENSADA' THEN 'C'
					  ELSE '' END													as "ModalidadDePago"
				,'01'																as "CodigoInstrumento"
				,'FORWARD'															as "NombreInstrumento"
				,LPAD(FWD."IdMonedaPrincipal",4,0)									as "CodMonedaRecibida"
				,LPAD(MONPRI."IdMoneda",30,0)										as "MonedaRecibida"
				,FWD."MontoMonedaPrincipal"											as "MontoRecibido"
				,LPAD(FWD."IdMonedaSecundaria",4,0)									as "CodMonedaEntregada"
				,LPAD(MONSEC."IdMoneda",30,0)										as "MonedaEntregada"
				,FWD."MontoMonedaSecundaria"										as "MontoEntregado"
				,LPAD('0,0000',15,0)												as "PrimaTotalDolares" -- no aplica para FWD
				,CASE WHEN FWD."IdMonedaSecundaria" = 'USD' THEN COALESCE(FWD."ParidadCierre",0)
					  ELSE COALESCE(FWD."TipoCambioCierre",0) END					as "ForwardFuturo"
				/*,CASE WHEN FWD."ModalidadLiquidacionValor" = 'ENTREGA_FISICA' AND (FWD."Nemotecnico" = 'ARBITR' OR FWD."Nemotecnico" = 'SECANO') THEN
						CASE WHEN FWD."IdMonedaSecundaria" = 'USD' THEN FWD."ParidadCosto" ELSE FWD."TipoCambioCosto" END
					  ELSE 0 END												as "PrecioSpot"*/
				,CASE WHEN FWD."IdMonedaSecundaria" = 'USD' THEN COALESCE(FWD."ParidadCosto",0)
					  ELSE COALESCE(FWD."TipoCambioCosto",0) END					as "PrecioSpot"
			FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWD" FWD
			LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" MONPRI
				ON FWD."IdMonedaPrincipal" = MONPRI."IdMoneda" AND MONPRI."IdEstado" = 'ACT'
			LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" MONSEC
				ON FWD."IdMonedaSecundaria" = MONSEC."IdMoneda" AND MONSEC."IdEstado" = 'ACT'
			LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" AS PAIS
				ON FWD."IdPaisResidencia" = PAIS."IdPais"
			WHERE "FechaOperacion" BETWEEN (:p_FechaDesde || ' 00:00:00') AND (:p_FechaHasta ||' 23:59:59')
			ORDER BY "IdOperacion" DESC, "IdCartera" DESC, "FechaOperacionOPE" DESC, "Estado" DESC;
			
		select 'p_operacionesFWD', * from :p_operacionesFWD;
		
		
		SELECT COUNT(1) INTO v_CountOperaciones FROM :p_operacionesFWD;
		
		SELECT SUM("MontoRecibido"), SUM("MontoEntregado") INTO v_SumaMontoRecibido, v_SumaMontoEntregado FROM :p_operacionesFWD;
		
		v_SumaMontoRecibido = COALESCE(:v_SumaMontoRecibido,0);
		v_SumaMontoEntregado = COALESCE(:v_SumaMontoEntregado,0);
		
		
		/*p_out_encabezadoAnexoIX =
			SELECT
				 'IIIIIIIIII'																		as "FlagDeControl"
				,LPAD(SUBSTR(per."DocumentoIdentidad",0,LENGTH(per."DocumentoIdentidad")-2),9,0)	as "RutDelReportante"
				,LPAD(SUBSTR(per."DocumentoIdentidad",LENGTH(per."DocumentoIdentidad"),LENGTH(per."DocumentoIdentidad")),1,0)	as "DV"
				,LPAD(:v_CountOperaciones,4,0)														as "NumeroRegistrosDeContratos"
				,REPLACE(LPAD(:v_SumaMontoRecibido,22,0),'.',',')									as "SumaMontoRecibido"
				,REPLACE(LPAD(:v_SumaMontoEntregado,22,0),'.',',')									as "SumaMontoEntregado"
				,LPAD('FWD-CONTRATO FORWARD',30,0)													as "GlosaArchivo"
				,TO_VARCHAR(:v_FechaHoraSistemaLOCAL,'DD/MM/YYYY')									as "Fecha"
				,LPAD(corrCod."CodigoCorredora",3,0)												as "CodigoSuperOrigen"
			FROM "BSCL.Plataforma.BD::PLA.Empresa" emp
			INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" soc on emp."IdSocioDeNegocio" = soc."IdSocioDeNegocio" and soc."IdEmpresa" = :p_IdEmpresa
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" per on per."IdPersona" = soc."IdPersona"
			INNER JOIN "BSCL.Plataforma.BD::INS.Corredora" corr on corr."IdPersona" = soc."IdPersona"
			INNER JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" corrCod on corr."IdCorredora" = corrCod."IdCorredora"
			INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" plaCon on corrCod."IdBolsa" = plaCon."Valor" and plaCon."IdEmpresa" = :p_IdEmpresa and plaCon."CodigoParametro" = 'PLA.BolsaDefectoSistema'
			;*/
		
		p_out_encabezadoAnexoIX =
			SELECT
				 ''																					AS "FlagDeControl"
				,LPAD(per."NombreCompleto",50,0)													AS "NombreCliente"
				,LPAD(COREMP."CodigoCorredora",40,0)												AS "CodigoEntidadMCF"
				,LPAD(SUBSTR(per."DocumentoIdentidad",0,LENGTH(per."DocumentoIdentidad")-2),9,0)	AS "RutDelReportante"
				,LPAD(SUBSTR(per."DocumentoIdentidad",LENGTH(per."DocumentoIdentidad"),LENGTH(per."DocumentoIdentidad")),1,0)	AS "DV"
				,LPAD('',3,0)																		AS "CodigoSectorEconomico"	/********/
				,LPAD(DIR."NombreLargo",50,0)														AS "DomicilioCiudad"
				,LPAD(PERREP."NombreCompleto",50,0)													AS "NombrePersonaEncargada"
				,LPAD(PERREP."Cargo",50,0)															AS "CargoPersonaEncargada"
				,LPAD(TEL."Numero",40,0)															AS "TelefonoPersonaEncargada"
				,LPAD(DIRELE."Direccion",50,0)														AS "CorreoElectronicoPersonaEncargada"
				,TO_VARCHAR(:v_FechaSistema,'DD/MM/YYYY')											AS "FechaDeEnvio"
				,TO_VARCHAR(:p_FechaDesde,'DD/MM/YYYY')||TO_VARCHAR(:p_FechaHasta,'DD/MM/YYYY')		AS "PeríodoInformado"
			FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa" COREMP
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" per
				ON per."IdPersona" = COREMP."IdPersona"
			INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" plaCon
				ON COREMP."IdBolsa" = plaCon."Valor" AND plaCon."IdEmpresa" = :p_IdEmpresa AND plaCon."CodigoParametro" = 'PLA.BolsaDefectoSistema'
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" DIR
				ON per."IdDireccionFisicaPrincipal" = DIR."IdDireccionFisica" AND DIR."Predeterminado" = 'UI_SI'
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PERREP
				ON per."IdPersonaRepresentante" = PERREP."IdPersona"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" TEL
				ON PERREP."IdPersona" = TEL."IdPersona" AND PERREP."IdTelefonoPrincipal" = TEL."IdTelefono" AND TEL."Predeterminado" = 'UI_SI'
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" DIRELE
				ON PERREP."IdPersona" = DIRELE."IdPersona" AND PERREP."IdDireccionElectronicaPrincipal" = DIRELE."IdDireccionElectronica" AND TEL."Predeterminado" = 'UI_SI'
			WHERE COREMP."IdEmpresa" = :p_IdEmpresa;
		
		
		/*p_out_registrosAnexoIX =
			SELECT
				 "FlagDeControl"
				,"RutReportante"
				,"DV"
				,"NombreCliente"
				,"NumeroContrato"
				,"FechaSuscripcion"
				,"FechaVencimiento"
				,"NumeroDeDias"
				,"ModalidadDePago"
				,"CodMonedaRecibida"
				,REPLACE(LPAD("MontoRecibido",19,0),'.',',') as "MontoRecibido"
				,"CodMonedaEntregada"
				,REPLACE(LPAD("MontoEntregado",19,0),'.',',') as "MontoEntregado"
				,REPLACE(LPAD("PrecioSpot",10,0),'.',',') as "PrecioSpot"
				,REPLACE(LPAD("ForwardFuturo",10,0),'.',',') as "ForwardFuturo"
				,"CodigoInstrumento"
				,"CodigoSectorEconomico"
				,"PrimaTotalDolares"
			FROM :p_operacionesFWD;*/
		
		p_out_registrosAnexoIX =
			SELECT
				 "FlagDeControl"
				,"RutReportante"
				,"DV"
				,"NombreCliente"
				,"CodigoSectorEconomico"
				,"CodigoPais"
				,"Pais"
				,"NumeroContrato"
				,"FechaSuscripcion"
				,"FechaVencimiento"
				,"NumeroDeDias"
				,"ModalidadDePago"
				,"CodigoInstrumento"
				,"NombreInstrumento"
				,"CodMonedaRecibida"
				,"MonedaRecibida"
				,REPLACE(LPAD("MontoRecibido",19,0),'.',',') as "MontoRecibido"
				,"CodMonedaEntregada"
				,"MonedaEntregada"
				,REPLACE(LPAD("MontoEntregado",19,0),'.',',') as "MontoEntregado"
				,"PrimaTotalDolares"
				,REPLACE(LPAD("ForwardFuturo",10,0),'.',',') as "ForwardFuturo"
				,REPLACE(LPAD("PrecioSpot",10,0),'.',',') as "PrecioSpot"
			FROM :p_operacionesFWD
			UNION ALL
			SELECT
				 'IIIIIIIIII' AS "FlagDeControl"
				,LPAD('',9,0) AS "RutReportante"
				,LPAD('',1,0) AS "DV"
				,LPAD('',50,0) AS "NombreCliente"
				,LPAD('',3,0) AS "CodigoSectorEconomico"
				,LPAD('',25,0) AS "CodigoPais"
				,LPAD('',25,0) AS "Pais"
				,LPAD('',12,0) AS "NumeroContrato"
				,LPAD('',10,0) AS "FechaSuscripcion"
				,LPAD('',10,0) AS "FechaVencimiento"
				,LPAD('',5,0) AS "NumeroDeDias"
				,LPAD('',1,0) AS "ModalidadDePago"
				,LPAD('',2,0) AS "CodigoInstrumento"
				,LPAD('',7,0) AS "NombreInstrumento"
				,LPAD('',4,0) AS "CodMonedaRecibida"
				,LPAD('',30,0) AS "MonedaRecibida"
				,REPLACE(LPAD(:v_SumaMontoRecibido,19,0),'.',',') as "MontoRecibido"
				,LPAD('',4,0) AS "CodMonedaEntregada"
				,LPAD('',30,0) AS "MonedaEntregada"
				,REPLACE(LPAD(:v_SumaMontoEntregado,19,0),'.',',') as "MontoEntregado"
				,LPAD('',15,0) AS "PrimaTotalDolares"
				,LPAD('',10,0) as "ForwardFuturo"
				,LPAD('',10,0) AS "PrecioSpot"
			FROM DUMMY;
		
		INSERT INTO "BSCL.Mesa.BD::FWD.LogInforme"
		("IdEmpresa","Informe","FechaEjecucion","FechaInforme")
		VALUES(p_IdEmpresa,'ANEXO_IX',v_FechaSistema,p_FechaHasta);
			
		SIGNAL SALIDA_CONTROLADA;		     
END;
