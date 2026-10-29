SET SCHEMA "BSCL"; 
DROP PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::SPT.ResultadoContableSpot";
CREATE PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::SPT.ResultadoContableSpot" (
	--Entrada
	IN p_IdEmpresa	BIGINT,
	IN p_IdUsuario	BIGINT,
	IN p_Fecha		DATE,
	IN p_Nemotecnico NVARCHAR(6)
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN
DECLARE v_monedaPrincipal     DECIMAL(26,8);
	DECLARE v_monedaSecundaria     DECIMAL(26,8);
	DECLARE v_idMonedaPrincipal    NVARCHAR(3);
	DECLARE v_idMonedaSecundaria    NVARCHAR(3);
	DECLARE v_out_montoEscritoPrincipal  NVARCHAR(1000);
	DECLARE v_out_montoEscritoSecundario  NVARCHAR(1000);
	DECLARE varFechaHoraSistema             NVARCHAR(40);
	DECLARE v_logo TABLE("LogoCorredora" BLOB);
	DECLARE v_count			BIGINT := 0;
	DECLARE v_CountErrores	BIGINT := 0;
	DECLARE v_msjDO			NVARCHAR(40) := '';
	
	--Variables para insert a tabla BSCL.Plataforma.BD::PAU.ControlProceso
	DECLARE v_FechaSistema			NVARCHAR(10);
	DECLARE v_FechaSistemaUTC       NVARCHAR(40);
    DECLARE v_FechaSistemaLOCAL     NVARCHAR(40);
    DECLARE v_FechaHoraSistemaUTC   NVARCHAR(40);
    DECLARE v_FechaHoraSistemaLOCAL SECONDDATE;
    DECLARE v_FechaHoraUTC          NVARCHAR(40);
    DECLARE v_FechaHoraLOCAL        NVARCHAR(40);
    DECLARE v_CodigoUsuario         NVARCHAR(40);
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema"(p_IdEmpresa => :p_IdEmpresa, p_out_FechaSistema => :v_FechaSistema);
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechas"(
        p_IdEmpresa                 => p_IdEmpresa,
        p_out_FechaSistemaUTC       => v_FechaSistemaUTC,
        p_out_FechaSistemaLOCAL     => v_FechaSistemaLOCAL,
        p_out_FechaHoraSistemaUTC   => v_FechaHoraSistemaUTC,
        p_out_FechaHoraSistemaLOCAL => v_FechaHoraSistemaLOCAL,
        p_out_FechaHoraUTC          => v_FechaHoraUTC,
        p_out_FechaHoraLOCAL        => v_FechaHoraLOCAL
    );
    SELECT IFNULL(USR."CodigoUsuario",' - ') INTO v_CodigoUsuario
    FROM "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuario" USR
    WHERE USR."IdUsuario" = :p_IdUsuario
      AND USR."IdEmpresa" = :p_IdEmpresa;
	
	
	SELECT COUNT(1) INTO v_count FROM "BSCL.Mesa.BD::SPT.ParidadYTipoCambio"
	WHERE "IdMoneda" = 'DO' AND "Fecha" = :p_Fecha AND "TipoCambioMercado" <> 0;
	IF v_Count = 0 THEN
		v_msjDO := 'Dolar Observado sin valor.';
		v_CountErrores := v_CountErrores + 1;
	END IF;
	
	v_logo =
		SELECT TOP 1 "Binario" AS "LogoCorredora"
		FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
		WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;
	
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
		p_IdEmpresa => :p_IdEmpresa,
		p_out_FechaHoraSistema => :varFechaHoraSistema
	);
	varFechaHoraSistema = TO_CHAR(:varFechaHoraSistema,'yyyy-MM-dd')||' 00:00:00';
	
	v_Movimientos_Spot =
		SELECT
			 SPOT."IdOperacionSpot"							as "Nro_Oper"
			,TO_CHAR(SPOT."FechaOperacion",'YYYY-MM-DD')	as "Fecha"
			,TO_CHAR(SPOT."FechaOperacion",'HH24:MI:SS')	as "Hora"
			,TO_CHAR(UTCTOLOCAL (TO_TIMESTAMP(SPOT."FechaOperacion", 'YYYY-MM-DD HH24:MI:SS'), 'America/Santiago','platform'),'HH24:MI:SS') as "HoraLocal"
			,CLI."NombreCompleto"							as "Cliente"
			,POR."CodigoPortafolio"							as "Portafolio"
			,CO."Nombre"									as "TipoOperacion"
			,CASE LTRIM(coalesce(INS."Nemotecnico",'')) 	 WHEN 'INTERB' THEN 'TRADING'
									 						 WHEN 'ARBINT' THEN 'TRADING'
									 			 				ELSE LTRIM(coalesce(POR."AreaCliente",''))  END as "Categoria"
			,SPOT."TipoCambioCierre"						as "Precio"
			,SPOT."MontoMonedaPrincipal"					as "MontoMO"
			,SPOT."MontoMonedaSecundaria"					as "Monto$"
			,SPOT."ParidadCierre"							as "Paridad"
			,CASE WHEN SPOT."IdMonedaPrincipal" = 'USD' THEN SPOT."MontoMonedaPrincipal"
				  WHEN SPOT."IdMonedaSecundaria" = 'USD' THEN SPOT."MontoMonedaSecundaria"
				  ELSE 0 END 								as "MontoUSD"
			,SPOT."MontoMonedaPrincipal" * SPOT."TipoCambioCosto" as "MontoPunta"
			,SPOT."Margen"									as "Margen"
			,SPOT."TipoCambioCosto"							AS "TCCosto"
		    ,coalesce(PENT."IdMedioDePago",'')				as "Fp"
		  	,coalesce(PREC."IdMedioDePago",'')   			as "Fr"
		    ,coalesce(SUC."CodigoSucursal",'')			   	as "Sucursal"
		    ,coalesce(PREC."NombreMedioDePago",'')			as "FormaRecibo"
			,coalesce(PREC."IdCondicionDeLiquidacion",'')	as "ValutaRecibo"
		    ,coalesce(MOVNCR."CodigoBancoLiquidador",'')	as "Banco"
		    ,SPOT."IdMonedaPrincipal"	as "Moneda"
			,SPOT."TipoComprobanteFacturacion"				as "TD"
			,COALESCE(FAC."IdMovimientoComprobante",0)		as "Documento" --Facturacion
			,CASE WHEN AGE."Tipo" = 'J' THEN AGE."RazonSocial" 
				  --WHEN AGE."Tipo" = 'M' THEN AGE."RazonSocial" --PROYECTO PERU (Mancomunados) --http://jira.bcs.cl/browse/OPTIMUSPE-202
				  ELSE AGE."Nombres" ||' '|| AGE."ApellidoPaterno" || ' ' || AGE."ApellidoMaterno"  
			 END											as "Agente"
			,SPOT."IdMonedaPrincipal"						as "MonedaPrincipal"
			,SPOT."IdMonedaSecundaria"						as "MonedaSecundaria"
			,SPOT."IdTipoCanalIngreso"						as "Origen"
			,:p_IdEmpresa									as "p_IdEmpresa"
			,TO_CHAR(:p_Fecha,'DD-MM-YYYY')					AS "p_Fecha"
			,COALESCE(MONPRI."MontoDecimales",4)			as "MontoDecimalesPrincipal"
			,COALESCE(MONSEC."MontoDecimales",4)			as "MontoDecimalesSecundaria"
			,LOGO."LogoCorredora"
			,:v_msjDO										as "MensajeError"
			,:v_CountErrores								as "ErroresContador"
			,''												as "MensajeErrorOp"
			,:v_msjDO										as "MensajeErrorDO"
			,CASE
				WHEN SPOT."IdMonedaPrincipal" <> 'USD' AND SPOT."IdMonedaSecundaria" <> 'CLP' THEN 'ARBITRAJE'
				ELSE '' END as "GrupoArbitraje"
			,CO."Nombre"||' '||SPOT."IdMonedaPrincipal"||' / '||SPOT."IdMonedaSecundaria"
			||' / '|| CASE LTRIM(coalesce(INS."Nemotecnico",'')) 	 WHEN 'INTERB' THEN 'TRADING'
									 						 WHEN 'ARBINT' THEN 'TRADING'
									 			 				ELSE LTRIM(coalesce(POR."AreaCliente",'')) END
			as "Grupo"
		FROM "BSCL.Mesa.BD::SPT.OperacionSpot" SPOT
		INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" TI
			ON SPOT."IdInstrumento" = TI."IdInstrumento"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
			ON SPOT."IdCliente" = CLI."IdCliente"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarOperacion" CO
			ON SPOT."IdTipoOperacion" = CO."IdTipoOperacion"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AGE
			ON SPOT."IdAgente" = AGE."IdPersona"
		LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" FAC
			ON (SPOT."IdOperacionSpot"		= FAC."IdTransaccion" AND 
				FAC."IdTipoProducto"		= 'MEME' AND 
				FAC."IdEstadoFacturacion"	= 'FAC' AND
				SPOT."IdMonedaPrincipal"	= FAC."IdMonedaSpot")
		LEFT JOIN (
				SELECT SPOT."IdOperacionSpot",MOVNCR."CodigoBancoLiquidador"
				FROM "BSCL.Mesa.BD::SPT.OperacionSpot" SPOT
				INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" FAC
					ON (SPOT."IdOperacionSpot"		= FAC."IdTransaccion" AND 
						FAC."IdTipoProducto"		= 'MEME' AND 
						FAC."IdEstadoFacturacion"	= 'FAC' AND
						SPOT."IdMonedaPrincipal"	= FAC."IdMonedaSpot")
				INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MOVNCR
					ON (FAC."IdComprobante"			> 0 AND 
						FAC."IdComprobante"			= MOVNCR."IdComprobanteReferencia" AND 
						MOVNCR."IdTipoComprobante"	IN ('NCE','NCR'))
				WHERE LEFT(TO_CHAR("FechaOperacion"),10) = :p_Fecha
				  AND SPOT."Estado" = 'VIG') MOVNCR
			ON (SPOT."IdOperacionSpot" = MOVNCR."IdOperacionSpot")
		LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONPRI
			ON (MONPRI."IdUsoMonedaFormato" = 'FWD' AND MONPRI."IdMoneda" = SPOT."IdMonedaPrincipal"  AND  MONPRI."IdTipoOperacion"='')
		LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONSEC
			ON (MONSEC."IdUsoMonedaFormato" = 'FWD' AND MONSEC."IdMoneda" = SPOT."IdMonedaSecundaria" AND  MONSEC."IdTipoOperacion"='')
		LEFT JOIN :v_logo LOGO
			ON (LOGO."LogoCorredora" IS NOT NULL)
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSucursal" SUC
			ON (CLI."IdSucursalEjecutivo" = SUC."IdSucursal")	
		LEFT JOIN  (	
						SELECT FAC."IdOperacion",PAG."IdMedioDePago",PAG."NombreMedioDePago",PAG."IdCondicionDeLiquidacion",
							ROW_NUMBER() OVER (PARTITION BY FAC."IdOperacion" ORDER BY FAC."IdComprobantePagoOperacion" DESC) as rn
						FROM  "BSCL.Mesa.BD::MES.ComprobantePagoOperacion" FAC 
						INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarFormasDePagoMesa" PAG ON (FAC."IdFormaDePago"=PAG."IdFormaDePago"  AND  PAG."RecibePagaPortafolio"='P' )
				   ) PENT ON  (PENT."IdOperacion"= SPOT."IdOperacionSpot") AND PENT.rn = 1 
				   
				   
			
		LEFT JOIN  (	
						SELECT FAC."IdOperacion",PAG."IdMedioDePago",PAG."NombreMedioDePago",PAG."IdCondicionDeLiquidacion",
							ROW_NUMBER() OVER (PARTITION BY FAC."IdOperacion" ORDER BY FAC."IdComprobantePagoOperacion" DESC) as rn
						FROM  "BSCL.Mesa.BD::MES.ComprobantePagoOperacion" FAC 
						INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarFormasDePagoMesa" PAG ON (FAC."IdFormaDePago"=PAG."IdFormaDePago"  AND  PAG."RecibePagaPortafolio"='R' )
				   ) PREC ON  (PREC."IdOperacion"= SPOT."IdOperacionSpot")AND PREC.rn = 1
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento"            INS ON (INS."IdInstrumento"=SPOT."IdInstrumento") 	
		LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR ON (POR."IdPortafolio"=SPOT."IdPortafolio")
		WHERE LEFT(TO_CHAR(SPOT."FechaOperacion"),10) = :p_Fecha
		  AND SPOT."Estado" = 'VIG'
		  AND SPOT."IdMonedaSecundaria" ='CLP'
		  AND MOVNCR."IdOperacionSpot" IS NULL
		  AND (TI."Nemotecnico" = :p_Nemotecnico OR :p_Nemotecnico = '')
		ORDER BY SPOT."IdOperacionSpot";
	
	
	SELECT COUNT(*) INTO v_count FROM :v_Movimientos_Spot;
	
	IF (:v_count = 0) THEN
		v_CountErrores := v_CountErrores + 1;
		
		v_Movimientos_Spot =
			SELECT 
				 0	as "Nro_Oper"
				,''	as "Fecha"
				,''	as "Hora"
				,''	as "HoraLocal"
				,''	as "Cliente"
				,'' as "Portafolio"
				,''	as "TipoOperacion"
				,'' as "Categoria"
				,0	as "Precio"
				,0	as "MontoMO"
				,0	as "Monto$"
				,0	as "Paridad"
				,0	as "MontoUSD"
				,0	as "MontoPunta"
   			    ,0	as "Margen"		
   			    ,0	as "TCCosto"
   			    ,''	as "Fp"
   			    ,''	as "Fr"
   			    ,''	as "Sucursal"
   			    ,''	as "FormaRecibo"
				,'' as "ValutaRecibo"
   			    ,''	as "Banco"
   			    ,''	as "Moneda"
				,''	as "TD"
				,0	as "Documento" --Facturacion
				,''	as "Agente"
				,''	as "MonedaPrincipal"
				,''	as "MonedaSecundaria"
				,''	as "Origen"
				,:p_IdEmpresa	as "p_IdEmpresa"
				,:p_Fecha		AS "p_Fecha"
				,0	as "MontoDecimalesPrincipal"
				,0	as "MontoDecimalesSecundaria"
				,LOGO."LogoCorredora"
				,'ERRORES : No se encontraron operaciones facturadas. '||:v_msjDO as "MensajeError"
				,:v_CountErrores as "ErroresContador"
				,'No se encontraron operaciones facturadas.' as "MensajeErrorOp"
				,:v_msjDO as "MensajeErrorDO"
				,'' as "GrupoArbitraje"
				,'' as "Grupo"
			FROM DUMMY
			INNER JOIN :v_logo LOGO
				ON (LOGO."LogoCorredora" IS NOT NULL);
	END IF;
	
	
	--Se incorpora insert a tabla BSCL.Plataforma.BD::PAU.ControlProceso al intentar generar Moposi
	BEGIN
		INSERT INTO "BSCL.Plataforma.BD::PAU.ControlProceso" ("IdEmpresa", "IdControlProceso", "CodigoModulo", "CodigoProceso", "FechaSistema", "FechaRegistro", "CodigoUsuario")
	    SELECT
	    	:p_IdEmpresa,
	        "BSCL.Plataforma.BD.Secuencias::IdControlProceso".NEXTVAL,
	        'MSP',
	        'CALCULO_MOPOSI',
	        :v_FechaSistema,
	        :v_FechaHoraLOCAL,
	        :v_CodigoUsuario
	    FROM DUMMY
	    WHERE :v_msjDO = '';
	    
	    COMMIT;
	END;
	
	
	--Salida del SP
	SELECT * FROM :v_Movimientos_Spot;
	
	END;